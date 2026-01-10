package com.wzl.fitness.modules.admin.controller;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.modules.admin.dto.CacheStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 缓存统计控制器
 * 提供缓存监控和管理功能
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "缓存管理", description = "缓存统计和管理接口")
@ConditionalOnProperty(
    prefix = "fitness.modules.admin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CacheStatsController {

    private final CacheManager cacheManager;

    /**
     * 获取所有缓存的统计信息
     */
    @GetMapping("/stats")
    @RequireAdmin
    @Operation(
            summary = "获取缓存统计", 
            description = "获取所有缓存区域的统计信息，包括命中率、大小等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "缓存统计示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": [
                                                {
                                                  "cacheName": "users",
                                                  "hitCount": 1000,
                                                  "missCount": 100,
                                                  "hitRate": 90.9,
                                                  "evictionCount": 50,
                                                  "size": 500
                                                }
                                              ],
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<List<CacheStatsDTO>> getCacheStats() {
        log.info("获取缓存统计信息");
        
        List<CacheStatsDTO> statsList = new ArrayList<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();
        
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                CacheStatsDTO stats = buildCacheStats(cacheName, cache);
                statsList.add(stats);
            }
        }
        
        return ApiResponse.success(statsList);
    }

    /**
     * 获取指定缓存的统计信息
     */
    @GetMapping("/stats/{cacheName}")
    @RequireAdmin
    @Operation(summary = "获取指定缓存统计", description = "获取指定缓存区域的统计信息")
    public ApiResponse<CacheStatsDTO> getCacheStatsByName(
            @Parameter(description = "缓存名称") @PathVariable String cacheName) {
        log.info("获取缓存 {} 的统计信息", cacheName);
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ApiResponse.error(404, "缓存 " + cacheName + " 不存在");
        }
        
        CacheStatsDTO stats = buildCacheStats(cacheName, cache);
        return ApiResponse.success(stats);
    }

    /**
     * 清除指定缓存
     */
    @PostMapping("/evict/{cacheName}")
    @RequireAdmin
    @Operation(summary = "清除指定缓存", description = "清除指定缓存区域的所有条目")
    public ApiResponse<String> evictCache(
            @Parameter(description = "缓存名称") @PathVariable String cacheName) {
        log.info("清除缓存: {}", cacheName);
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ApiResponse.error(404, "缓存 " + cacheName + " 不存在");
        }
        
        cache.clear();
        log.info("缓存 {} 已清除", cacheName);
        
        return ApiResponse.success("缓存 " + cacheName + " 已清除");
    }

    /**
     * 清除所有缓存
     */
    @PostMapping("/evict-all")
    @RequireAdmin
    @Operation(summary = "清除所有缓存", description = "清除所有缓存区域的所有条目")
    public ApiResponse<String> evictAllCaches() {
        log.info("清除所有缓存");
        
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("缓存 {} 已清除", cacheName);
            }
        }
        
        return ApiResponse.success("所有缓存已清除");
    }

    /**
     * 获取缓存名称列表
     */
    @GetMapping("/names")
    @RequireAdmin
    @Operation(summary = "获取缓存名称列表", description = "获取所有已配置的缓存区域名称")
    public ApiResponse<Collection<String>> getCacheNames() {
        return ApiResponse.success(cacheManager.getCacheNames());
    }

    /**
     * 构建缓存统计DTO
     */
    private CacheStatsDTO buildCacheStats(String cacheName, Cache cache) {
        CacheStatsDTO stats = new CacheStatsDTO();
        stats.setCacheName(cacheName);
        
        // 尝试获取 Caffeine 缓存的详细统计
        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
            com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = caffeineCache.stats();
            
            stats.setHitCount(caffeineStats.hitCount());
            stats.setMissCount(caffeineStats.missCount());
            stats.setHitRate(caffeineStats.hitRate() * 100); // 转换为百分比
            stats.setEvictionCount(caffeineStats.evictionCount());
            stats.setSize(caffeineCache.estimatedSize());
            stats.setLoadSuccessCount(caffeineStats.loadSuccessCount());
            stats.setLoadFailureCount(caffeineStats.loadFailureCount());
            stats.setTotalLoadTime(caffeineStats.totalLoadTime());
            stats.setAverageLoadPenalty(caffeineStats.averageLoadPenalty());
        } else {
            // 对于非 Caffeine 缓存，设置默认值
            stats.setHitCount(0L);
            stats.setMissCount(0L);
            stats.setHitRate(0.0);
            stats.setEvictionCount(0L);
            stats.setSize(0L);
            stats.setLoadSuccessCount(0L);
            stats.setLoadFailureCount(0L);
            stats.setTotalLoadTime(0L);
            stats.setAverageLoadPenalty(0.0);
        }
        
        return stats;
    }
}
