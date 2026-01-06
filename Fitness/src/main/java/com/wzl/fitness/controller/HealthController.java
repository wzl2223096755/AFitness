package com.wzl.fitness.controller;

import com.wzl.fitness.common.ApiResponse;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "健康检查", description = "系统健康状态检查接口")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    /**
     * 系统健康检查
     */
    @GetMapping
    @Operation(summary = "系统健康检查", description = "检查系统是否正常运行")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now().toString());
        healthInfo.put("application", "Fitness Management System");
        healthInfo.put("version", "1.0.0");
        
        // 数据库连接状态
        Map<String, Object> database = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            database.put("status", "UP");
            database.put("database", conn.getMetaData().getDatabaseProductName());
            database.put("url", conn.getMetaData().getURL());
            
            // HikariCP 连接池信息
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikari = (HikariDataSource) dataSource;
                Map<String, Object> pool = new HashMap<>();
                pool.put("poolName", hikari.getPoolName());
                pool.put("maximumPoolSize", hikari.getMaximumPoolSize());
                pool.put("minimumIdle", hikari.getMinimumIdle());
                if (hikari.getHikariPoolMXBean() != null) {
                    pool.put("activeConnections", hikari.getHikariPoolMXBean().getActiveConnections());
                    pool.put("idleConnections", hikari.getHikariPoolMXBean().getIdleConnections());
                    pool.put("totalConnections", hikari.getHikariPoolMXBean().getTotalConnections());
                }
                database.put("connectionPool", pool);
            }
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        healthInfo.put("database", database);
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");
        system.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
        system.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        healthInfo.put("system", system);
        
        return ApiResponse.success(healthInfo);
    }
    
    /**
     * 简单的存活检查（用于负载均衡器）
     */
    @GetMapping("/live")
    @Operation(summary = "存活检查", description = "简单的存活检查，用于负载均衡器")
    public ApiResponse<String> live() {
        return ApiResponse.success("OK");
    }
    
    /**
     * 就绪检查（检查所有依赖是否就绪）
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "检查系统是否准备好接收请求")
    public ApiResponse<Map<String, Object>> ready() {
        Map<String, Object> readiness = new HashMap<>();
        boolean isReady = true;
        
        // 检查数据库连接
        try (Connection conn = dataSource.getConnection()) {
            readiness.put("database", "UP");
        } catch (Exception e) {
            readiness.put("database", "DOWN");
            isReady = false;
        }
        
        readiness.put("status", isReady ? "READY" : "NOT_READY");
        readiness.put("timestamp", LocalDateTime.now().toString());
        
        return ApiResponse.success(readiness);
    }
}