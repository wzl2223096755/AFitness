package com.wzl.fitness.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzl.fitness.service.JwtRefreshService;
import com.wzl.fitness.security.JwtTokenProvider;
import com.wzl.fitness.security.JwtAuthenticationFilter;
import com.wzl.fitness.util.JwtUtil;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * 测试配置类，用于提供测试所需的Bean
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        Mockito.when(jwtUtil.getUsernameFromToken(anyString())).thenReturn("testuser");
        return jwtUtil;
    }

    @Bean
    @Primary
    public JwtRefreshService jwtRefreshService() {
        JwtRefreshService jwtRefreshService = Mockito.mock(JwtRefreshService.class);
        Mockito.when(jwtRefreshService.isTokenValid(anyString())).thenReturn(true);
        return jwtRefreshService;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return Mockito.mock(JwtAuthenticationFilter.class);
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
    }
}