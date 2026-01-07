package com.wzl.fitness.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 内存缓存配置类（备用）
 * 当 Caffeine 缓存不可用时，使用简单的内存缓存作为备选方案
 * 通过配置属性 fitness.cache.type=simple 启用
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "fitness.cache.type", havingValue = "simple", matchIfMissing = false)
public class MemoryCacheConfig {

    /**
     * 配置简单内存缓存管理器
     * 仅在 fitness.cache.type=simple 时启用
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager simpleCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // 预定义缓存名称
        cacheManager.setCacheNames(Arrays.asList("users", "trainingRecords", "nutritionRecords", "statistics"));
        // 允许null值
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }
}