package com.wzl.fitness.modules.admin.api;

import com.wzl.fitness.modules.admin.dto.SystemInfoDTO;
import com.wzl.fitness.modules.admin.dto.CacheStatsDTO;
import com.wzl.fitness.modules.admin.dto.UserActivityDTO;

import java.util.List;

/**
 * 管理模块对外接口
 * 定义管理模块对其他模块暴露的服务契约
 * 
 * @see Requirements 2.1 - 为每个领域模块定义独立的服务接口
 */
public interface AdminModuleApi {
    
    /**
     * 获取系统信息
     * 
     * @return 系统信息DTO
     */
    SystemInfoDTO getSystemInfo();
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计DTO
     */
    CacheStatsDTO getCacheStats();
    
    /**
     * 获取用户活动统计
     * 
     * @param days 统计天数
     * @return 用户活动列表
     */
    List<UserActivityDTO> getUserActivityStats(int days);
    
    /**
     * 检查系统健康状态
     * 
     * @return 如果系统健康返回true，否则返回false
     */
    boolean isSystemHealthy();
    
    /**
     * 清除指定缓存
     * 
     * @param cacheName 缓存名称
     * @return 如果清除成功返回true，否则返回false
     */
    boolean clearCache(String cacheName);
}
