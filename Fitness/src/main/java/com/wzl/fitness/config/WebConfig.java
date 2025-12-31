package com.wzl.fitness.config;

import com.wzl.fitness.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 提供RestTemplate等Web相关Bean
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Autowired
    private Environment environment;

    /**
     * 配置RestTemplate Bean
     * 用于HTTP客户端调用
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 在测试环境中不注册拦截器
        if (isTestEnvironment()) {
            return;
        }
        
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/check-username",
                        "/api/auth/check-email"
                );
    }
    
    /**
     * 检查是否为测试环境
     */
    private boolean isTestEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.equals("test")) {
                return true;
            }
        }
        return false;
    }
}
