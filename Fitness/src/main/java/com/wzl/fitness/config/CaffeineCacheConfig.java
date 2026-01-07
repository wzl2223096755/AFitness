package com.wzl.fitness.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Caffeine 缓存配置类
 * 提供高性能的本地缓存支持，替代简单的 ConcurrentMapCache
 * 
 * 特性：
 * - 基于时间的过期策略
 * - 基于大小的驱逐策略
 * - 缓存统计支持
 * - 多缓存区域配置
 * 
 * 此配置作为默认缓存管理器，当没有其他 CacheManager 定义时生效
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String CACHE_USERS = "users";
    public static final String CACHE_TRAINING_RECORDS = "trainingRecords";
    public static final String CACHE_NUTRITION_STATS = "nutritionStats";
    public static final String CACHE_DASHBOARD_METRICS = "dashboardMetrics";
    public static final String CACHE_USER_STATS = "userStats";

    /**
     * 配置 Caffeine 缓存管理器
     * 使用统一的缓存配置，支持缓存统计
     * 
     * @Primary 确保此缓存管理器优先于其他缓存管理器
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置 Caffeine 缓存构建器
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)           // 初始容量
            .maximumSize(1000)              // 最大缓存条目数
            .expireAfterWrite(Duration.ofMinutes(30))  // 写入后30分钟过期
            .expireAfterAccess(Duration.ofMinutes(15)) // 访问后15分钟过期
            .recordStats());                // 启用统计
        
        // 预定义缓存名称
        cacheManager.setCacheNames(Arrays.asList(
            CACHE_USERS,
            CACHE_TRAINING_RECORDS,
            CACHE_NUTRITION_STATS,
            CACHE_DASHBOARD_METRICS,
            CACHE_USER_STATS
        ));
        
        // 允许缓存 null 值
        cacheManager.setAllowNullValues(true);
        
        return cacheManager;
    }

    /**
     * 获取缓存统计信息
     * 用于监控和调试
     */
    public static Map<String, CacheStats> getCacheStats(CacheManager cacheManager) {
        Map<String, CacheStats> statsMap = new HashMap<>();
        
        if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
            for (String cacheName : caffeineCacheManager.getCacheNames()) {
                var cache = caffeineCacheManager.getCache(cacheName);
                if (cache != null) {
                    var nativeCache = cache.getNativeCache();
                    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
                        statsMap.put(cacheName, caffeineCache.stats());
                    }
                }
            }
        }
        
        return statsMap;
    }
}
