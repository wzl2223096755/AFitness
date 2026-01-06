package com.wzl.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * 数据库重试配置类
 * 配置 Spring Retry 用于处理瞬态数据库连接失败
 */
@Configuration
@EnableRetry
public class DatabaseRetryConfig {
    
    /**
     * 创建数据库重试模板
     * 使用指数退避策略，最多重试3次
     */
    @Bean
    public RetryTemplate databaseRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 初始间隔 1 秒
        backOffPolicy.setMultiplier(2.0);        // 倍数 2
        backOffPolicy.setMaxInterval(10000);     // 最大间隔 10 秒
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试策略 - 针对特定异常类型
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(TransientDataAccessException.class, true);
        retryableExceptions.put(CannotAcquireLockException.class, true);
        retryableExceptions.put(QueryTimeoutException.class, true);
        retryableExceptions.put(CannotGetJdbcConnectionException.class, true);
        retryableExceptions.put(SQLException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}
