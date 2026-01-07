package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.dto.response.DatabaseStatsDTO;
import com.wzl.fitness.dto.response.JvmMetricsDTO;
import com.wzl.fitness.dto.response.SystemInfoDTO;
import com.wzl.fitness.dto.response.UserActivityDTO;
import com.wzl.fitness.service.UserActivityService;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * 系统监控控制器
 * 提供系统信息、JVM 指标、数据库统计等监控接口
 */
@RestController
@RequestMapping("/api/v1/admin/monitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "系统监控", description = "系统监控和指标接口")
public class SystemMonitorController {

    private final DataSource dataSource;
    private final UserActivityService userActivityService;

    /**
     * 获取系统信息
     */
    @GetMapping("/system-info")
    @RequireAdmin
    @Operation(
            summary = "获取系统信息", 
            description = "获取操作系统、CPU、内存等系统信息"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "系统信息示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "osName": "Windows 10",
                                                "osVersion": "10.0",
                                                "availableProcessors": 8,
                                                "totalMemory": 8589934592,
                                                "freeMemory": 4294967296,
                                                "usedMemory": 4294967296,
                                                "cpuUsage": 25.5,
                                                "uptime": 3600000
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<SystemInfoDTO> getSystemInfo() {
        log.info("获取系统信息");
        
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // 获取 CPU 使用率
        double cpuUsage = 0.0;
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            cpuUsage = sunOsBean.getCpuLoad() * 100;
            if (cpuUsage < 0) {
                cpuUsage = 0.0; // CPU 负载不可用时返回 0
            }
        }
        
        SystemInfoDTO systemInfo = SystemInfoDTO.builder()
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArch(System.getProperty("os.arch"))
                .availableProcessors(runtime.availableProcessors())
                .totalMemory(totalMemory)
                .freeMemory(freeMemory)
                .usedMemory(usedMemory)
                .maxMemory(maxMemory)
                .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0)
                .uptime(runtimeBean.getUptime())
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .build();
        
        return ApiResponse.success(systemInfo);
    }

    /**
     * 获取 JVM 指标
     */
    @GetMapping("/jvm-metrics")
    @RequireAdmin
    @Operation(summary = "获取 JVM 指标", description = "获取堆内存、线程、GC 等 JVM 指标")
    public ApiResponse<JvmMetricsDTO> getJvmMetrics() {
        log.info("获取 JVM 指标");
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        // 计算 GC 统计
        long gcCount = 0;
        long gcTime = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            long time = gcBean.getCollectionTime();
            if (count >= 0) {
                gcCount += count;
            }
            if (time >= 0) {
                gcTime += time;
            }
        }
        
        JvmMetricsDTO jvmMetrics = JvmMetricsDTO.builder()
                .heapUsed(heapUsage.getUsed())
                .heapMax(heapUsage.getMax())
                .heapInit(heapUsage.getInit())
                .heapCommitted(heapUsage.getCommitted())
                .nonHeapUsed(nonHeapUsage.getUsed())
                .nonHeapMax(nonHeapUsage.getMax())
                .nonHeapCommitted(nonHeapUsage.getCommitted())
                .threadCount(threadBean.getThreadCount())
                .peakThreadCount(threadBean.getPeakThreadCount())
                .daemonThreadCount(threadBean.getDaemonThreadCount())
                .totalStartedThreadCount(threadBean.getTotalStartedThreadCount())
                .gcCount(gcCount)
                .gcTime(gcTime)
                .loadedClassCount(classBean.getLoadedClassCount())
                .totalLoadedClassCount(classBean.getTotalLoadedClassCount())
                .unloadedClassCount(classBean.getUnloadedClassCount())
                .build();
        
        return ApiResponse.success(jvmMetrics);
    }

    /**
     * 获取数据库统计
     */
    @GetMapping("/database-stats")
    @RequireAdmin
    @Operation(summary = "获取数据库统计", description = "获取数据库连接池和数据库信息")
    public ApiResponse<DatabaseStatsDTO> getDatabaseStats() {
        log.info("获取数据库统计");
        
        DatabaseStatsDTO.DatabaseStatsDTOBuilder builder = DatabaseStatsDTO.builder();
        
        // 获取 HikariCP 连接池统计
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            if (poolMXBean != null) {
                builder.activeConnections(poolMXBean.getActiveConnections())
                       .idleConnections(poolMXBean.getIdleConnections())
                       .totalConnections(poolMXBean.getTotalConnections())
                       .pendingConnections(poolMXBean.getThreadsAwaitingConnection());
            }
            builder.maxConnections(hikariDataSource.getMaximumPoolSize())
                   .poolName(hikariDataSource.getPoolName());
        }
        
        // 获取数据库元数据
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            builder.databaseProductName(metaData.getDatabaseProductName())
                   .databaseProductVersion(metaData.getDatabaseProductVersion())
                   .driverName(metaData.getDriverName())
                   .driverVersion(metaData.getDriverVersion())
                   .url(metaData.getURL());
        } catch (SQLException e) {
            log.warn("获取数据库元数据失败: {}", e.getMessage());
        }
        
        return ApiResponse.success(builder.build());
    }

    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/user-activity")
    @RequireAdmin
    @Operation(summary = "获取用户活跃度", description = "获取用户活跃度统计信息")
    public ApiResponse<UserActivityDTO> getUserActivity() {
        log.info("获取用户活跃度统计");
        
        UserActivityDTO userActivity = userActivityService.getUserActivityStats();
        return ApiResponse.success(userActivity);
    }
}
