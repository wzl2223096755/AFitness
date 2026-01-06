package com.wzl.fitness.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 连接池状态模型
 * 用于表示数据库连接池的当前状态
 */
@Data
@Builder
public class ConnectionPoolStatus {
    
    /** 活跃连接数 */
    private int activeConnections;
    
    /** 空闲连接数 */
    private int idleConnections;
    
    /** 总连接数 */
    private int totalConnections;
    
    /** 等待连接的线程数 */
    private int threadsAwaitingConnection;
    
    /** 最大连接池大小 */
    private int maxPoolSize;
    
    /** 最小空闲连接数 */
    private int minIdle;
    
    /** 连接池利用率 */
    private double utilizationRate;
    
    /** 连接池名称 */
    private String poolName;
    
    /** 时间戳 */
    private LocalDateTime timestamp;
    
    /** 健康状态 */
    private HealthStatus healthStatus;
    
    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        /** 健康 - 利用率 <= 70% */
        HEALTHY,
        /** 警告 - 利用率 > 70% 且 <= 90% */
        WARNING,
        /** 危险 - 利用率 > 90% */
        CRITICAL
    }
    
    /**
     * 根据利用率计算健康状态
     */
    public static HealthStatus calculateHealthStatus(double utilizationRate) {
        if (utilizationRate > 0.9) {
            return HealthStatus.CRITICAL;
        } else if (utilizationRate > 0.7) {
            return HealthStatus.WARNING;
        }
        return HealthStatus.HEALTHY;
    }
}
