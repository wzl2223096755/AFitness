package com.wzl.fitness.health;

import com.wzl.fitness.model.ConnectionPoolStatus;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接池健康检查指示器
 * 通过 Actuator 暴露连接池详细指标
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private static final Status WARNING_STATUS = new Status("WARNING");
    private static final double WARNING_THRESHOLD = 0.7;
    private static final double CRITICAL_THRESHOLD = 0.9;
    
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        if (!(dataSource instanceof HikariDataSource)) {
            return Health.unknown()
                    .withDetail("error", "DataSource is not HikariDataSource")
                    .build();
        }
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        
        if (poolMXBean == null) {
            return Health.unknown()
                    .withDetail("error", "HikariPoolMXBean is not available")
                    .build();
        }
        
        // 收集连接池指标
        int activeConnections = poolMXBean.getActiveConnections();
        int idleConnections = poolMXBean.getIdleConnections();
        int totalConnections = poolMXBean.getTotalConnections();
        int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
        int maxPoolSize = hikariDataSource.getMaximumPoolSize();
        int minIdle = hikariDataSource.getMinimumIdle();
        String poolName = hikariDataSource.getPoolName();
        
        // 计算利用率
        double utilizationRate = maxPoolSize > 0 ? (double) activeConnections / maxPoolSize : 0;
        
        // 构建详细信息
        Map<String, Object> details = new HashMap<>();
        details.put("poolName", poolName);
        details.put("activeConnections", activeConnections);
        details.put("idleConnections", idleConnections);
        details.put("totalConnections", totalConnections);
        details.put("threadsAwaitingConnection", threadsAwaitingConnection);
        details.put("maxPoolSize", maxPoolSize);
        details.put("minIdle", minIdle);
        details.put("utilizationRate", String.format("%.2f%%", utilizationRate * 100));
        details.put("timestamp", LocalDateTime.now().toString());
        
        // 根据利用率确定健康状态
        ConnectionPoolStatus.HealthStatus healthStatus = 
                ConnectionPoolStatus.calculateHealthStatus(utilizationRate);
        details.put("healthStatus", healthStatus.name());
        
        // 构建健康响应
        if (utilizationRate > CRITICAL_THRESHOLD) {
            log.warn("Database connection pool utilization is critical: {}%", 
                    String.format("%.2f", utilizationRate * 100));
            return Health.down()
                    .withDetails(details)
                    .withDetail("warning", "Connection pool utilization > 90%")
                    .build();
        } else if (utilizationRate > WARNING_THRESHOLD) {
            log.info("Database connection pool utilization is elevated: {}%", 
                    String.format("%.2f", utilizationRate * 100));
            return Health.status(WARNING_STATUS)
                    .withDetails(details)
                    .withDetail("warning", "Connection pool utilization > 70%")
                    .build();
        }
        
        return Health.up().withDetails(details).build();
    }
    
    /**
     * 获取当前连接池状态
     */
    public ConnectionPoolStatus getConnectionPoolStatus() {
        if (!(dataSource instanceof HikariDataSource)) {
            return null;
        }
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        
        if (poolMXBean == null) {
            return null;
        }
        
        int activeConnections = poolMXBean.getActiveConnections();
        int maxPoolSize = hikariDataSource.getMaximumPoolSize();
        double utilizationRate = maxPoolSize > 0 ? (double) activeConnections / maxPoolSize : 0;
        
        return ConnectionPoolStatus.builder()
                .activeConnections(activeConnections)
                .idleConnections(poolMXBean.getIdleConnections())
                .totalConnections(poolMXBean.getTotalConnections())
                .threadsAwaitingConnection(poolMXBean.getThreadsAwaitingConnection())
                .maxPoolSize(maxPoolSize)
                .minIdle(hikariDataSource.getMinimumIdle())
                .utilizationRate(utilizationRate)
                .poolName(hikariDataSource.getPoolName())
                .timestamp(LocalDateTime.now())
                .healthStatus(ConnectionPoolStatus.calculateHealthStatus(utilizationRate))
                .build();
    }
}
