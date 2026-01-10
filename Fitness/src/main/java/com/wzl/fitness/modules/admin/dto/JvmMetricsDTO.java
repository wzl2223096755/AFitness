package com.wzl.fitness.modules.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * JVM 指标数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JvmMetricsDTO {
    
    /**
     * 堆内存已使用（字节）
     */
    private Long heapUsed;
    
    /**
     * 堆内存最大值（字节）
     */
    private Long heapMax;
    
    /**
     * 堆内存初始值（字节）
     */
    private Long heapInit;
    
    /**
     * 堆内存已提交（字节）
     */
    private Long heapCommitted;
    
    /**
     * 非堆内存已使用（字节）
     */
    private Long nonHeapUsed;
    
    /**
     * 非堆内存最大值（字节）
     */
    private Long nonHeapMax;
    
    /**
     * 非堆内存已提交（字节）
     */
    private Long nonHeapCommitted;
    
    /**
     * 当前线程数
     */
    private Integer threadCount;
    
    /**
     * 峰值线程数
     */
    private Integer peakThreadCount;
    
    /**
     * 守护线程数
     */
    private Integer daemonThreadCount;
    
    /**
     * 总启动线程数
     */
    private Long totalStartedThreadCount;
    
    /**
     * GC 次数
     */
    private Long gcCount;
    
    /**
     * GC 总时间（毫秒）
     */
    private Long gcTime;
    
    /**
     * 已加载类数量
     */
    private Integer loadedClassCount;
    
    /**
     * 总加载类数量
     */
    private Long totalLoadedClassCount;
    
    /**
     * 已卸载类数量
     */
    private Long unloadedClassCount;
}
