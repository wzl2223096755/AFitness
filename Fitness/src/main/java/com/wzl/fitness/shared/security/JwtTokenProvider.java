package com.wzl.fitness.shared.security;

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
 * 提供JWT令牌的生成、验证和解析功能
 * 
 * 注意：此类暂不注册为Bean，待完全迁移后启用
 * 当前系统仍使用 com.wzl.fitness.security.JwtTokenProvider
 */
// @Component("sharedJwtTokenProvider") // 暂时禁用，避免与旧类冲突
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

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, userDetails.getUsername(), jwtExpirationInSeconds * 1000L);
    }

    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpirationInSeconds * 1000L);
    }

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

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

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

    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

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
        
        public String getUsername() { return username; }
        public Long getUserId() { return userId; }
        public Date getExpiration() { return expiration; }
        public Boolean getIsRefresh() { return isRefresh; }
        
        public static class TokenInfoBuilder {
            private String username;
            private Long userId;
            private Date expiration;
            private Boolean isRefresh;
            
            public TokenInfoBuilder username(String username) { this.username = username; return this; }
            public TokenInfoBuilder userId(Long userId) { this.userId = userId; return this; }
            public TokenInfoBuilder expiration(Date expiration) { this.expiration = expiration; return this; }
            public TokenInfoBuilder isRefresh(Boolean isRefresh) { this.isRefresh = isRefresh; return this; }
            
            public TokenInfo build() {
                return new TokenInfo(username, userId, expiration, isRefresh);
            }
        }
    }
}
