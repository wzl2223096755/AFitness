package com.wzl.fitness.modules.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 数据库统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseStatsDTO {
    
    /**
     * 活跃连接数
     */
    private Integer activeConnections;
    
    /**
     * 空闲连接数
     */
    private Integer idleConnections;
    
    /**
     * 总连接数
     */
    private Integer totalConnections;
    
    /**
     * 最大连接数
     */
    private Integer maxConnections;
    
    /**
     * 等待连接数
     */
    private Integer pendingConnections;
    
    /**
     * 数据库产品名称
     */
    private String databaseProductName;
    
    /**
     * 数据库产品版本
     */
    private String databaseProductVersion;
    
    /**
     * 驱动名称
     */
    private String driverName;
    
    /**
     * 驱动版本
     */
    private String driverVersion;
    
    /**
     * 数据库 URL
     */
    private String url;
    
    /**
     * 连接池名称
     */
    private String poolName;
}
