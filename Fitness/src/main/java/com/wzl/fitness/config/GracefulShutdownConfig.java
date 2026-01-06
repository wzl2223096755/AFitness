package com.wzl.fitness.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * 优雅关闭配置类
 * 确保应用关闭时正确处理数据库连接
 */
@Slf4j
@Configuration
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {
    
    private final DataSource dataSource;
    
    @Value("${spring.lifecycle.timeout-per-shutdown-phase:30s}")
    private String shutdownTimeout;
    
    public GracefulShutdownConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Application shutdown initiated, starting graceful connection pool shutdown...");
        
        if (!(dataSource instanceof HikariDataSource)) {
            log.warn("DataSource is not HikariDataSource, skipping graceful shutdown");
            return;
        }
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        
        if (poolMXBean == null) {
            log.warn("HikariPoolMXBean is not available, skipping graceful shutdown");
            return;
        }
        
        // 记录关闭前的连接池状态
        log.info("Connection pool status before shutdown: active={}, idle={}, total={}, awaiting={}",
                poolMXBean.getActiveConnections(),
                poolMXBean.getIdleConnections(),
                poolMXBean.getTotalConnections(),
                poolMXBean.getThreadsAwaitingConnection());
        
        // 软驱逐空闲连接
        log.info("Soft evicting idle connections...");
        poolMXBean.softEvictConnections();
        
        // 等待活跃连接完成
        int timeoutSeconds = parseTimeoutSeconds(shutdownTimeout);
        int waitedSeconds = 0;
        int checkIntervalMs = 500;
        
        while (poolMXBean.getActiveConnections() > 0 && waitedSeconds < timeoutSeconds) {
            try {
                Thread.sleep(checkIntervalMs);
                waitedSeconds++;
                
                if (waitedSeconds % 5 == 0) {
                    log.info("Waiting for active connections to complete... active={}, waited={}s",
                            poolMXBean.getActiveConnections(), waitedSeconds);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Shutdown wait interrupted");
                break;
            }
        }
        
        // 检查是否还有活跃连接
        int remainingActive = poolMXBean.getActiveConnections();
        if (remainingActive > 0) {
            log.warn("Forcing shutdown with {} active connections remaining after {}s timeout",
                    remainingActive, timeoutSeconds);
        } else {
            log.info("All connections gracefully closed");
        }
        
        // 关闭连接池
        log.info("Closing HikariCP connection pool...");
        hikariDataSource.close();
        log.info("HikariCP connection pool closed successfully");
    }
    
    /**
     * 解析超时时间字符串为秒数
     */
    private int parseTimeoutSeconds(String timeout) {
        if (timeout == null || timeout.isEmpty()) {
            return 30;
        }
        
        timeout = timeout.trim().toLowerCase();
        
        try {
            if (timeout.endsWith("s")) {
                return Integer.parseInt(timeout.substring(0, timeout.length() - 1));
            } else if (timeout.endsWith("m")) {
                return Integer.parseInt(timeout.substring(0, timeout.length() - 1)) * 60;
            } else {
                return Integer.parseInt(timeout);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid shutdown timeout format: {}, using default 30s", timeout);
            return 30;
        }
    }
}
