package com.wzl.fitness.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 测试配置类，用于完全禁用拦截器
 */
@TestConfiguration
public class TestDisableInterceptorConfig {

    @Bean
    @Primary
    public WebMvcConfigurer testWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 完全不注册任何拦截器，覆盖原有配置
                System.out.println("=== 测试环境：完全禁用所有拦截器 ===");
            }
        };
    }
}