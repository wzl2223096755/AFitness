package com.wzl.fitness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT令牌提供者
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private int jwtExpirationInSeconds;

    @Value("${jwt.refresh-expiration:604800}")
    private int refreshExpirationInSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为用户生成token
     */
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, userDetails.getUsername(), jwtExpirationInSeconds * 1000L);
    }

    /**
     * 生成刷新token
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpirationInSeconds * 1000L);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token（不检查用户）
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为刷新token
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中提取所有信息
     */
    public TokenInfo extractTokenInfo(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return TokenInfo.builder()
                    .username(claims.getSubject())
                    .userId(claims.get("userId", Long.class))
                    .expiration(claims.getExpiration())
                    .isRefresh("refresh".equals(claims.get("type")))
                    .build();
        } catch (Exception e) {
            logger.error("提取token信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Token信息类
     */
    public static class TokenInfo {
        private String username;
        private Long userId;
        private Date expiration;
        private Boolean isRefresh;
        
        public TokenInfo() {}
        
        public TokenInfo(String username, Long userId, Date expiration, Boolean isRefresh) {
            this.username = username;
            this.userId = userId;
            this.expiration = expiration;
            this.isRefresh = isRefresh;
        }
        
        public static TokenInfoBuilder builder() {
            return new TokenInfoBuilder();
        }
        
        public String getUsername() {
            return username;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public Date getExpiration() {
            return expiration;
        }
        
        public Boolean getIsRefresh() {
            return isRefresh;
        }
        
        public static class TokenInfoBuilder {
            private String username;
            private Long userId;
            private Date expiration;
            private Boolean isRefresh;
            
            public TokenInfoBuilder username(String username) {
                this.username = username;
                return this;
            }
            
            public TokenInfoBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }
            
            public TokenInfoBuilder expiration(Date expiration) {
                this.expiration = expiration;
                return this;
            }
            
            public TokenInfoBuilder isRefresh(Boolean isRefresh) {
                this.isRefresh = isRefresh;
                return this;
            }
            
            public TokenInfo build() {
                return new TokenInfo(username, userId, expiration, isRefresh);
            }
        }
    }
}