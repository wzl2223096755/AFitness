package com.wzl.fitness.config;

import com.wzl.fitness.security.JwtTokenProvider;
import com.wzl.fitness.security.CustomUserDetailsService;
import com.wzl.fitness.security.JwtAuthenticationEntryPoint;
import com.wzl.fitness.security.JwtAuthenticationFilter;
import com.wzl.fitness.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

/**
 * 测试环境安全配置 - 启用安全验证
 * 用于测试需要安全验证的场景，如未授权访问测试
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfigWithAuth {

    /**
     * 创建模拟的RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> mockTemplate = mock(RedisTemplate.class);
        when(mockTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(mockTemplate.hasKey(anyString())).thenReturn(false);
        return mockTemplate;
    }

    /**
     * 创建模拟的JwtTokenProvider
     */
    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider mockProvider = mock(JwtTokenProvider.class);
        // 模拟token验证失败
        when(mockProvider.validateToken(anyString())).thenReturn(false);
        when(mockProvider.extractTokenInfo(anyString())).thenReturn(null);
        when(mockProvider.getUsernameFromToken(anyString())).thenReturn(null);
        return mockProvider;
    }

    /**
     * 创建模拟的JwtAuthenticationEntryPoint
     */
    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return mock(JwtAuthenticationEntryPoint.class);
    }

    /**
     * 创建模拟的JwtRefreshService
     */
    @Bean
    @Primary
    public com.wzl.fitness.service.JwtRefreshService jwtRefreshService() {
        com.wzl.fitness.service.JwtRefreshService mockService = mock(com.wzl.fitness.service.JwtRefreshService.class);
        try {
            // 模拟token验证失败，触发401错误
            when(mockService.isTokenValid(anyString())).thenReturn(false);
        } catch (Exception e) {
            // Mock设置不会抛出异常
        }
        return mockService;
    }

    /**
     * 创建模拟的JwtAuthenticationFilter
     */
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                                           UserDetailsService userDetailsService,
                                                           com.wzl.fitness.service.JwtRefreshService jwtRefreshService,
                                                           com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        // 创建真实的过滤器实例，确保安全检查正常执行
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtTokenProvider, userDetailsService, jwtRefreshService, objectMapper);
        
        // 模拟过滤器行为：对于没有有效token的请求，抛出异常
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, jwtRefreshService, objectMapper) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain chain) throws ServletException, IOException {
                // 获取Authorization头
                String token = getTokenFromRequest(request);
                
                // 如果没有token或token无效，直接返回401
                if (token == null || !jwtTokenProvider.validateToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                    return;
                }
                
                // 如果有有效token，继续处理
                chain.doFilter(request, response);
            }
            
            private String getTokenFromRequest(HttpServletRequest request) {
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                return null;
            }
        };
    }

    /**
     * 创建测试用的UserDetailsService
     */
    @Bean
    @Primary
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    /**
     * 测试环境安全配置 - 启用安全验证
     * 确保此配置优先于主配置
     */
    @Bean("testSecurityFilterChainWithAuth")
    @Primary
    public SecurityFilterChain testSecurityFilterChainWithAuth(HttpSecurity http, 
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}