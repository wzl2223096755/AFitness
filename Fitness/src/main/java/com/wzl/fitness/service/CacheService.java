package com.wzl.fitness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存服务
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final CacheManager cacheManager;

    /**
     * 获取缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    log.debug("缓存命中: {} - {}", cacheName, key);
                    return (T) wrapper.get();
                }
            }
            log.debug("缓存未命中: {} - {}", cacheName, key);
            return null;
        } catch (Exception e) {
            log.warn("缓存获取失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
            return null;
        }
    }

    /**
     * 设置缓存值
     */
    public void put(String cacheName, Object key, Object value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                log.debug("缓存设置: {} - {}", cacheName, key);
            }
        } catch (Exception e) {
            log.warn("缓存设置失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
        }
    }

    /**
     * 设置缓存值并指定TTL
     */
    public void put(String cacheName, Object key, Object value, long ttl, TimeUnit timeUnit) {
        put(cacheName, key, value);
        // 注意：EhCache的TTL在配置文件中设置，这里只是记录
        log.debug("缓存设置: {} - {}, TTL: {} {}", cacheName, key, ttl, timeUnit);
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("缓存删除: {} - {}", cacheName, key);
            }
        } catch (Exception e) {
            log.warn("缓存删除失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
        }
    }

    /**
     * 清空指定缓存
     */
    public void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("缓存清空: {}", cacheName);
            }
        } catch (Exception e) {
            log.warn("缓存清空失败: {}, 错误: {}", cacheName, e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        } catch (Exception e) {
            log.warn("缓存检查失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
            return false;
        }
    }

    /**
     * 预热缓存
     */
    public void warmUp(String cacheName, Object key, Runnable loader) {
        if (!exists(cacheName, key)) {
            try {
                loader.run();
                log.debug("缓存预热完成: {} - {}", cacheName, key);
            } catch (Exception e) {
                log.warn("缓存预热失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
            }
        }
    }
    
    /**
     * 批量获取缓存值
     */
    public <K, V> Map<K, V> getAll(String cacheName, Collection<K> keys, Class<V> type) {
        Map<K, V> result = new HashMap<>();
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            keys.forEach(key -> {
                V value = get(cacheName, key, type);
                if (value != null) {
                    result.put(key, value);
                }
            });
        }
        return result;
    }
    
    /**
     * 批量设置缓存值
     */
    public <K, V> void putAll(String cacheName, Map<K, V> entries) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            entries.forEach((key, value) -> {
                try {
                    cache.put(key, value);
                } catch (Exception e) {
                    log.warn("批量缓存设置失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
                }
            });
            log.debug("批量缓存设置完成: {} - {} 个条目", cacheName, entries.size());
        }
    }
    
    /**
     * 批量删除缓存
     */
    public void evictAll(String cacheName, Collection<?> keys) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            keys.forEach(key -> {
                try {
                    cache.evict(key);
                } catch (Exception e) {
                    log.warn("批量缓存删除失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
                }
            });
            log.debug("批量缓存删除完成: {} - {} 个条目", cacheName, keys.size());
        }
    }
    
    /**
     * 根据模式删除缓存
     */
    public void evictByPattern(String cacheName, String pattern) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            try {
                // 简化处理：直接清空整个缓存
                log.warn("模式匹配删除不支持，清空整个缓存: {}", cacheName);
                cache.clear();
                log.debug("根据模式删除缓存完成: {} - 模式: {}", cacheName, pattern);
            } catch (Exception e) {
                log.warn("根据模式删除缓存失败: {} - 模式: {}, 错误: {}", cacheName, pattern, e.getMessage());
            }
        }
    }
    
    /**
     * 获取或加载缓存值
     */
    @SuppressWarnings("unchecked")
    public <K, V> V getOrLoad(String cacheName, K key, Class<V> type, Function<K, V> loader) {
        V value = get(cacheName, key, type);
        if (value == null) {
            try {
                value = loader.apply(key);
                if (value != null) {
                    put(cacheName, key, value);
                }
            } catch (Exception e) {
                log.warn("缓存加载失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
            }
        }
        return value;
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats(String cacheName) {
        Map<String, Object> stats = new HashMap<>();
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put("name", cacheName);
                stats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                // 注意：EhCache的统计信息需要通过原生缓存获取
                log.debug("缓存统计信息: {}", stats);
            }
        } catch (Exception e) {
            log.warn("获取缓存统计失败: {}, 错误: {}", cacheName, e.getMessage());
        }
        return stats;
    }
    
    /**
     * 清理所有缓存
     */
    public void clearAll() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            cacheNames.forEach(this::clear);
            log.debug("所有缓存已清空");
        } catch (Exception e) {
            log.warn("清空所有缓存失败: {}", e.getMessage());
        }
    }
    
    /**
     * 检查缓存是否存在
     */
    public boolean cacheExists(String cacheName) {
        return cacheManager.getCacheNames().contains(cacheName);
    }
    
    /**
     * 获取所有缓存名称
     */
    public Set<String> getAllCacheNames() {
        return new HashSet<>(cacheManager.getCacheNames());
    }
}