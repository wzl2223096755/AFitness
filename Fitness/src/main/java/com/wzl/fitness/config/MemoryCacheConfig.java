package com.wzl.fitness.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 内存缓存配置类
 * 当Redis不可用时，使用内存缓存作为备选方案
 */
@Configuration
@EnableCaching
@ConditionalOnMissingBean(name = "cacheManager")
public class MemoryCacheConfig {

    /**
     * 配置内存缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // 预定义缓存名称
        cacheManager.setCacheNames(Arrays.asList("users", "trainingRecords", "nutritionRecords", "statistics"));
        // 允许null值
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }
}