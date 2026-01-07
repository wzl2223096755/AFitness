package com.wzl.fitness.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 缓存统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsDTO {
    
    /**
     * 缓存名称
     */
    private String cacheName;
    
    /**
     * 缓存命中次数
     */
    private Long hitCount;
    
    /**
     * 缓存未命中次数
     */
    private Long missCount;
    
    /**
     * 缓存命中率（百分比）
     */
    private Double hitRate;
    
    /**
     * 缓存驱逐次数
     */
    private Long evictionCount;
    
    /**
     * 当前缓存条目数量
     */
    private Long size;
    
    /**
     * 加载成功次数
     */
    private Long loadSuccessCount;
    
    /**
     * 加载失败次数
     */
    private Long loadFailureCount;
    
    /**
     * 总加载时间（纳秒）
     */
    private Long totalLoadTime;
    
    /**
     * 平均加载时间（纳秒）
     */
    private Double averageLoadPenalty;
}
