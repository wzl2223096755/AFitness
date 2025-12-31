package com.wzl.fitness.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT认证属性测试
 * 
 * **Property 1: JWT认证一致性**
 * *For any* 有效的用户凭证，登录后获取的JWT令牌应该能够成功验证并返回正确的用户信息
 * 
 * **Validates: Requirements 1.4, 5.2**
 * 
 * Feature: project-evaluation, Property 1: JWT认证一致性
 */
public class JwtAuthenticationPropertyTest {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationPropertyTest() {
        // 创建一个测试用的JwtTokenProvider
        this.jwtTokenProvider = new JwtTokenProvider();
        // 使用反射设置私有字段
        try {
            java.lang.reflect.Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            // HS512 requires at least 512 bits (64 bytes) - using 64 character secret
            secretField.set(jwtTokenProvider, "mySecretKey1234567890123456789012345678901234567890123456789012345678");
            
            java.lang.reflect.Field expirationField = JwtTokenProvider.class.getDeclaredField("jwtExpirationInSeconds");
            expirationField.setAccessible(true);
            expirationField.set(jwtTokenProvider, 86400);
            
            java.lang.reflect.Field refreshField = JwtTokenProvider.class.getDeclaredField("refreshExpirationInSeconds");
            refreshField.setAccessible(true);
            refreshField.set(jwtTokenProvider, 604800);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JwtTokenProvider for testing", e);
        }
    }

    /**
     * Property 1: JWT认证一致性 - 生成的令牌应能成功验证
     * 
     * 对于任意有效的用户名和用户ID，生成的JWT令牌应该能够成功验证
     * 
     * **Validates: Requirements 1.4, 5.2**
     */
    @Property(tries = 100)
    @Label("Property 1: JWT认证一致性 - 生成的令牌应能成功验证")
    void generatedTokenShouldBeValid(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // Given
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token = jwtTokenProvider.generateToken(userDetails, userId);
        
        // Then
        assertNotNull(token, "生成的令牌不应为空");
        assertTrue(jwtTokenProvider.validateToken(token, userDetails), 
                String.format("令牌应该对用户 %s 有效", username));
    }

    /**
     * 属性：从令牌中提取的用户名应与原始用户名一致
     */
    @Property(tries = 100)
    @Label("从令牌中提取的用户名应与原始用户名一致")
    void extractedUsernameShouldMatchOriginal(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // Given
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token = jwtTokenProvider.generateToken(userDetails, userId);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        
        // Then
        assertEquals(username, extractedUsername, 
                String.format("提取的用户名应与原始用户名一致: expected=%s, actual=%s", username, extractedUsername));
    }

    /**
     * 属性：从令牌中提取的用户ID应与原始用户ID一致
     */
    @Property(tries = 100)
    @Label("从令牌中提取的用户ID应与原始用户ID一致")
    void extractedUserIdShouldMatchOriginal(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // Given
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token = jwtTokenProvider.generateToken(userDetails, userId);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        
        // Then
        assertEquals(userId, extractedUserId, 
                String.format("提取的用户ID应与原始用户ID一致: expected=%d, actual=%d", userId, extractedUserId));
    }

    /**
     * 属性：刷新令牌应被正确识别
     */
    @Property(tries = 100)
    @Label("刷新令牌应被正确识别")
    void refreshTokenShouldBeIdentified(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // Given
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String accessToken = jwtTokenProvider.generateToken(userDetails, userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, userId);
        
        // Then
        assertFalse(jwtTokenProvider.isRefreshToken(accessToken), "访问令牌不应被识别为刷新令牌");
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken), "刷新令牌应被正确识别");
    }

    /**
     * 属性：不同用户的令牌应该不同
     */
    @Property(tries = 50)
    @Label("不同用户的令牌应该不同")
    void differentUsersShouldHaveDifferentTokens(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String username1,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String username2,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId1,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId2) {
        
        Assume.that(!username1.equals(username2) || !userId1.equals(userId2));
        
        // Given
        UserDetails user1 = User.builder()
                .username(username1)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        UserDetails user2 = User.builder()
                .username(username2)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token1 = jwtTokenProvider.generateToken(user1, userId1);
        String token2 = jwtTokenProvider.generateToken(user2, userId2);
        
        // Then
        assertNotEquals(token1, token2, "不同用户的令牌应该不同");
    }

    /**
     * 属性：令牌验证应对错误用户失败
     */
    @Property(tries = 50)
    @Label("令牌验证应对错误用户失败")
    void tokenValidationShouldFailForWrongUser(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String username1,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String username2,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        Assume.that(!username1.equals(username2));
        
        // Given
        UserDetails user1 = User.builder()
                .username(username1)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        UserDetails user2 = User.builder()
                .username(username2)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token = jwtTokenProvider.generateToken(user1, userId);
        
        // Then
        assertTrue(jwtTokenProvider.validateToken(token, user1), "令牌应对正确用户有效");
        assertFalse(jwtTokenProvider.validateToken(token, user2), "令牌应对错误用户无效");
    }

    /**
     * 属性：令牌信息提取应返回完整信息
     */
    @Property(tries = 100)
    @Label("令牌信息提取应返回完整信息")
    void tokenInfoExtractionShouldReturnCompleteInfo(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // Given
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        // When
        String token = jwtTokenProvider.generateToken(userDetails, userId);
        JwtTokenProvider.TokenInfo tokenInfo = jwtTokenProvider.extractTokenInfo(token);
        
        // Then
        assertNotNull(tokenInfo, "令牌信息不应为空");
        assertEquals(username, tokenInfo.getUsername(), "用户名应匹配");
        assertEquals(userId, tokenInfo.getUserId(), "用户ID应匹配");
        assertNotNull(tokenInfo.getExpiration(), "过期时间不应为空");
        assertFalse(tokenInfo.getIsRefresh(), "访问令牌不应标记为刷新令牌");
    }

    /**
     * 属性：无效令牌验证应返回false
     */
    @Property(tries = 50)
    @Label("无效令牌验证应返回false")
    void invalidTokenValidationShouldReturnFalse(
            @ForAll @AlphaChars @StringLength(min = 10, max = 50) String invalidToken) {
        
        // When & Then
        assertFalse(jwtTokenProvider.validateToken(invalidToken), "无效令牌验证应返回false");
    }
}
