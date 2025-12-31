package com.wzl.fitness.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 测试专用的Web配置，禁用所有拦截器
 */
@TestConfiguration
public class TestWebMvcConfig {

    @Bean
    @Primary
    public WebMvcConfigurer testWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 测试环境：不注册任何拦截器
            }
        };
    }
}