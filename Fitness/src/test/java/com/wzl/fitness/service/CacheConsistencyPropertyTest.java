package com.wzl.fitness.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.wzl.fitness.config.CaffeineCacheConfig;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存一致性属性测试
 * 
 * **Property 1: 缓存一致性**
 * *For any* 数据更新操作, 相关缓存条目应被正确失效，后续查询应返回最新数据
 * 
 * **Validates: Requirements 2.2, 2.5**
 * 
 * Feature: system-optimization-95, Property 1: 缓存一致性
 */
public class CacheConsistencyPropertyTest {

    /**
     * 创建测试用的缓存管理器
     */
    private CacheManager createCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(15))
            .recordStats());
        cacheManager.setCacheNames(Arrays.asList(
            CaffeineCacheConfig.CACHE_USERS,
            CaffeineCacheConfig.CACHE_TRAINING_RECORDS,
            CaffeineCacheConfig.CACHE_NUTRITION_STATS,
            CaffeineCacheConfig.CACHE_DASHBOARD_METRICS,
            CaffeineCacheConfig.CACHE_USER_STATS
        ));
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

    /**
     * Property 1: 缓存写入后读取一致性
     * 
     * 对于任意有效的缓存键和值，写入缓存后立即读取应返回相同的值
     * 
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 100)
    @Label("Property 1: 缓存写入后读取一致性 - 写入后立即读取应返回相同值")
    void cacheWriteThenReadShouldReturnSameValue(
            @ForAll @StringLength(min = 1, max = 50) String key,
            @ForAll @StringLength(min = 1, max = 100) String value) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取缓存
        Cache cache = cacheManager.getCache(CaffeineCacheConfig.CACHE_USERS);
        assertNotNull(cache, "缓存应该存在");
        
        // 写入缓存
        cache.put(key, value);
        
        // 读取缓存
        Cache.ValueWrapper wrapper = cache.get(key);
        assertNotNull(wrapper, "缓存值包装器不应为null");
        assertEquals(value, wrapper.get(), 
                String.format("缓存读取值应与写入值相同: key=%s, expected=%s, actual=%s", 
                        key, value, wrapper.get()));
    }

    /**
     * Property 2: 缓存清除后读取一致性
     * 
     * 对于任意有效的缓存键和值，清除缓存后读取应返回null
     * 
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 100)
    @Label("Property 2: 缓存清除后读取一致性 - 清除后读取应返回null")
    void cacheEvictThenReadShouldReturnNull(
            @ForAll @StringLength(min = 1, max = 50) String key,
            @ForAll @StringLength(min = 1, max = 100) String value) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取缓存
        Cache cache = cacheManager.getCache(CaffeineCacheConfig.CACHE_TRAINING_RECORDS);
        assertNotNull(cache, "缓存应该存在");
        
        // 写入缓存
        cache.put(key, value);
        
        // 验证写入成功
        Cache.ValueWrapper wrapper = cache.get(key);
        assertNotNull(wrapper, "写入后缓存值应存在");
        
        // 清除缓存
        cache.evict(key);
        
        // 读取缓存
        Cache.ValueWrapper afterEvict = cache.get(key);
        assertNull(afterEvict, 
                String.format("清除后缓存值应为null: key=%s", key));
    }

    /**
     * Property 3: 缓存全部清除一致性
     * 
     * 对于任意数量的缓存条目，全部清除后所有读取应返回null
     * 
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 50)
    @Label("Property 3: 缓存全部清除一致性 - 全部清除后所有读取应返回null")
    void cacheClearAllThenReadShouldReturnNull(
            @ForAll @Size(min = 1, max = 10) java.util.List<@StringLength(min = 1, max = 20) String> keys) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取缓存
        Cache cache = cacheManager.getCache(CaffeineCacheConfig.CACHE_NUTRITION_STATS);
        assertNotNull(cache, "缓存应该存在");
        
        // 写入多个缓存条目
        for (String key : keys) {
            cache.put(key, "value_" + key);
        }
        
        // 验证写入成功
        for (String key : keys) {
            Cache.ValueWrapper wrapper = cache.get(key);
            assertNotNull(wrapper, String.format("写入后缓存值应存在: key=%s", key));
        }
        
        // 清除所有缓存
        cache.clear();
        
        // 验证所有缓存已清除
        for (String key : keys) {
            Cache.ValueWrapper afterClear = cache.get(key);
            assertNull(afterClear, 
                    String.format("全部清除后缓存值应为null: key=%s", key));
        }
    }

    /**
     * Property 4: 缓存更新一致性
     * 
     * 对于任意有效的缓存键，更新缓存值后读取应返回新值
     * 
     * **Validates: Requirements 2.2, 2.5**
     */
    @Property(tries = 100)
    @Label("Property 4: 缓存更新一致性 - 更新后读取应返回新值")
    void cacheUpdateThenReadShouldReturnNewValue(
            @ForAll @StringLength(min = 1, max = 50) String key,
            @ForAll @StringLength(min = 1, max = 100) String oldValue,
            @ForAll @StringLength(min = 1, max = 100) String newValue) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取缓存
        Cache cache = cacheManager.getCache(CaffeineCacheConfig.CACHE_DASHBOARD_METRICS);
        assertNotNull(cache, "缓存应该存在");
        
        // 写入旧值
        cache.put(key, oldValue);
        
        // 验证旧值
        Cache.ValueWrapper oldWrapper = cache.get(key);
        assertNotNull(oldWrapper, "旧值应存在");
        assertEquals(oldValue, oldWrapper.get(), "应返回旧值");
        
        // 更新为新值
        cache.put(key, newValue);
        
        // 验证新值
        Cache.ValueWrapper newWrapper = cache.get(key);
        assertNotNull(newWrapper, "新值应存在");
        assertEquals(newValue, newWrapper.get(), 
                String.format("更新后应返回新值: key=%s, expected=%s, actual=%s", 
                        key, newValue, newWrapper.get()));
    }

    /**
     * Property 5: 不同缓存区域隔离性
     * 
     * 对于相同的键，不同缓存区域的值应该相互独立
     * 
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 50)
    @Label("Property 5: 不同缓存区域隔离性 - 相同键在不同区域应独立")
    void differentCacheRegionsShouldBeIsolated(
            @ForAll @StringLength(min = 1, max = 50) String key,
            @ForAll @StringLength(min = 1, max = 100) String value1,
            @ForAll @StringLength(min = 1, max = 100) String value2) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取两个不同的缓存区域
        Cache cache1 = cacheManager.getCache(CaffeineCacheConfig.CACHE_USERS);
        Cache cache2 = cacheManager.getCache(CaffeineCacheConfig.CACHE_TRAINING_RECORDS);
        
        assertNotNull(cache1, "用户缓存应该存在");
        assertNotNull(cache2, "训练记录缓存应该存在");
        
        // 在两个缓存区域写入相同键但不同值
        cache1.put(key, value1);
        cache2.put(key, value2);
        
        // 验证两个缓存区域的值相互独立
        Cache.ValueWrapper wrapper1 = cache1.get(key);
        Cache.ValueWrapper wrapper2 = cache2.get(key);
        
        assertNotNull(wrapper1, "缓存1的值应存在");
        assertNotNull(wrapper2, "缓存2的值应存在");
        
        assertEquals(value1, wrapper1.get(), 
                String.format("缓存1应返回value1: key=%s, expected=%s, actual=%s", 
                        key, value1, wrapper1.get()));
        assertEquals(value2, wrapper2.get(), 
                String.format("缓存2应返回value2: key=%s, expected=%s, actual=%s", 
                        key, value2, wrapper2.get()));
    }

    /**
     * Property 6: 缓存空值处理一致性
     * 
     * 缓存应该能够正确处理null值（如果配置允许）
     * 
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 50)
    @Label("Property 6: 缓存空值处理一致性 - 应正确处理null值")
    void cacheShouldHandleNullValues(
            @ForAll @StringLength(min = 1, max = 50) String key) {
        
        CacheManager cacheManager = createCacheManager();
        
        // 获取缓存
        Cache cache = cacheManager.getCache(CaffeineCacheConfig.CACHE_USER_STATS);
        assertNotNull(cache, "缓存应该存在");
        
        // 写入null值
        cache.put(key, null);
        
        // 读取缓存 - 应该返回包含null的ValueWrapper
        Cache.ValueWrapper wrapper = cache.get(key);
        assertNotNull(wrapper, "缓存值包装器不应为null（即使值为null）");
        assertNull(wrapper.get(), "缓存值应为null");
    }
}
