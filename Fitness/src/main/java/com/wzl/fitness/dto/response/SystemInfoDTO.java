package com.wzl.fitness.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 系统信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemInfoDTO {
    
    /**
     * 操作系统名称
     */
    private String osName;
    
    /**
     * 操作系统版本
     */
    private String osVersion;
    
    /**
     * 操作系统架构
     */
    private String osArch;
    
    /**
     * 可用处理器数量
     */
    private Integer availableProcessors;
    
    /**
     * 总内存（字节）
     */
    private Long totalMemory;
    
    /**
     * 空闲内存（字节）
     */
    private Long freeMemory;
    
    /**
     * 已使用内存（字节）
     */
    private Long usedMemory;
    
    /**
     * 最大内存（字节）
     */
    private Long maxMemory;
    
    /**
     * CPU 使用率（百分比）
     */
    private Double cpuUsage;
    
    /**
     * 系统运行时间（毫秒）
     */
    private Long uptime;
    
    /**
     * Java 版本
     */
    private String javaVersion;
    
    /**
     * Java 供应商
     */
    private String javaVendor;
}
