package com.wzl.fitness.modules.admin.api;

import com.wzl.fitness.modules.admin.dto.SystemInfoDTO;
import com.wzl.fitness.modules.admin.dto.CacheStatsDTO;
import com.wzl.fitness.modules.admin.dto.UserActivityDTO;
import com.wzl.fitness.modules.admin.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;

/**
 * 管理模块API实现
 * 实现管理模块对外暴露的服务接口
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "fitness.modules.admin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AdminModuleApiImpl implements AdminModuleApi {
    
    private final UserActivityService userActivityService;
    private final CacheManager cacheManager;
    
    @Override
    public SystemInfoDTO getSystemInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        return SystemInfoDTO.builder()
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArch(System.getProperty("os.arch"))
                .availableProcessors(Runtime.getRuntime().availableProcessors())
                .uptime(runtimeMXBean.getUptime())
                .totalMemory(Runtime.getRuntime().totalMemory())
                .freeMemory(Runtime.getRuntime().freeMemory())
                .usedMemory(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                .maxMemory(Runtime.getRuntime().maxMemory())
                .build();
    }
    
    @Override
    public CacheStatsDTO getCacheStats() {
        // 返回第一个缓存的统计信息，或空的统计
        return new CacheStatsDTO();
    }
    
    @Override
    public List<UserActivityDTO> getUserActivityStats(int days) {
        // 返回单个用户活动统计
        UserActivityDTO stats = userActivityService.getUserActivityStats();
        return stats != null ? Collections.singletonList(stats) : Collections.emptyList();
    }
    
    @Override
    public boolean isSystemHealthy() {
        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryMXBean.getHeapMemoryUsage().getMax();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            return memoryUsagePercent < 90;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean clearCache(String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
