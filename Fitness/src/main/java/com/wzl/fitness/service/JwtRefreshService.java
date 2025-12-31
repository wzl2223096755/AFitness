package com.wzl.fitness.service;

import com.wzl.fitness.security.JwtTokenProvider;
import com.wzl.fitness.security.JwtTokenProvider.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * JWT刷新服务
 */
@Service
@RequiredArgsConstructor
public class JwtRefreshService {

    private static final Logger log = LoggerFactory.getLogger(JwtRefreshService.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Nullable
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    
    // 内存存储，用于Redis不可用时的备用方案
    private final ConcurrentHashMap<String, String> memoryTokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> memoryBlacklist = new ConcurrentHashMap<>();

    /**
     * 刷新访问token
     */
    public String refreshToken(String refreshToken) {
        try {
            // 验证刷新token
            if (!jwtTokenProvider.validateToken(refreshToken) || 
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("无效的刷新token");
            }

            // 提取token信息
            TokenInfo tokenInfo = jwtTokenProvider.extractTokenInfo(refreshToken);
            if (tokenInfo == null) {
                throw new IllegalArgumentException("无法解析token信息");
            }

            // 检查刷新token是否在存储中存在
            String refreshKey = REFRESH_TOKEN_PREFIX + tokenInfo.getUserId();
            String storedToken = getStoredToken(refreshKey);
            if (!refreshToken.equals(storedToken)) {
                throw new IllegalArgumentException("刷新token不存在或已失效");
            }

            // 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(tokenInfo.getUsername());

            // 生成新的访问token
            String newAccessToken = jwtTokenProvider.generateToken(userDetails, tokenInfo.getUserId());

            log.info("用户 {} 的token已刷新", tokenInfo.getUsername());
            return newAccessToken;

        } catch (Exception e) {
            log.error("刷新token失败", e);
            throw new RuntimeException("刷新token失败: " + e.getMessage());
        }
    }

    public String getUsernameFromToken(String token) {
        TokenInfo tokenInfo = jwtTokenProvider.extractTokenInfo(token);
        if (tokenInfo == null) {
            throw new IllegalArgumentException("无法解析token信息");
        }
        return tokenInfo.getUsername();
    }

    /**
     * 生成并保存刷新token
     */
    public String generateAndSaveRefreshToken(UserDetails userDetails, Long userId) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, userId);
        
        // 将刷新token保存到存储，设置过期时间为7天
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        saveToken(refreshKey, refreshToken, 7 * 24 * 60 * 60 * 1000L); // 7天
        
        return refreshToken;
    }

    /**
     * 将token加入黑名单
     */
    public void blacklistToken(String token) {
        try {
            TokenInfo tokenInfo = jwtTokenProvider.extractTokenInfo(token);
            if (tokenInfo != null) {
                String blacklistKey = BLACKLIST_PREFIX + token;
                // 设置黑名单过期时间为token的剩余时间
                long ttl = tokenInfo.getExpiration().getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    if (redisTemplate != null) {
                        try {
                            redisTemplate.opsForValue().set(blacklistKey, "true", ttl, TimeUnit.MILLISECONDS);
                        } catch (Exception e) {
                            log.warn("Redis操作失败，使用内存存储: {}", e.getMessage());
                            memoryBlacklist.put(blacklistKey, System.currentTimeMillis() + ttl);
                        }
                    } else {
                        memoryBlacklist.put(blacklistKey, System.currentTimeMillis() + ttl);
                    }
                }
            }
        } catch (Exception e) {
            log.error("将token加入黑名单失败", e);
        }
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            if (redisTemplate != null) {
                return hasRedisKey(blacklistKey);
            } else {
                Long expiryTime = memoryBlacklist.get(blacklistKey);
                if (expiryTime == null) {
                    return false;
                }
                // 检查是否过期
                if (System.currentTimeMillis() > expiryTime) {
                    memoryBlacklist.remove(blacklistKey);
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            log.error("检查token黑名单失败", e);
            return false;
        }
    }

    /**
     * 撤销用户的所有token
     */
    public void revokeAllUserTokens(Long userId) {
        try {
            // 删除刷新token
            String refreshKey = REFRESH_TOKEN_PREFIX + userId;
            if (redisTemplate != null) {
                deleteRedisKey(refreshKey);
            } else {
                memoryTokenStore.remove(refreshKey);
            }
            
            log.info("用户 {} 的所有token已被撤销", userId);
        } catch (Exception e) {
            log.error("撤销用户token失败", e);
        }
    }

    /**
     * 验证token是否有效（包括黑名单检查）
     */
    public boolean isTokenValid(String token) {
        try {
            // 首先检查token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return false;
            }
            
            // 然后验证token本身
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.error("验证token失败", e);
            return false;
        }
    }
    
    private String getStoredToken(String key) {
        if (redisTemplate != null) {
            try {
                return (String) redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis操作失败，使用内存存储: {}", e.getMessage());
            }
        }
        return memoryTokenStore.get(key);
    }
    
    private void saveToken(String key, String value, long ttlMillis) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
                return;
            } catch (Exception e) {
                log.warn("Redis操作失败，使用内存存储: {}", e.getMessage());
            }
        }
        memoryTokenStore.put(key, value);
        // 简单的过期处理，实际应用中需要更复杂的清理机制
    }
    
    private boolean hasRedisKey(String key) {
        if (redisTemplate != null) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            } catch (Exception e) {
                log.warn("Redis操作失败: {}", e.getMessage());
            }
        }
        return false;
    }
    
    private void deleteRedisKey(String key) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis删除操作失败: {}", e.getMessage());
            }
        }
    }
}
