package com.wzl.fitness.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.db.DatabaseTableMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 连接池指标配置类
 * 配置 HikariCP 指标绑定到 Micrometer
 */
@Slf4j
@Configuration
@ConditionalOnBean(MeterRegistry.class)
public class ConnectionPoolMetricsConfig {
    
    public ConnectionPoolMetricsConfig(DataSource dataSource, MeterRegistry meterRegistry) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            
            // HikariCP 自动注册指标到 Micrometer（通过 register-mbeans=true）
            // 这里添加额外的自定义指标
            log.info("HikariCP metrics configured for pool: {}", hikariDataSource.getPoolName());
            
            // 注册数据库表指标（可选）
            try {
                new DatabaseTableMetrics(dataSource, "fitness_db", "users", null)
                        .bindTo(meterRegistry);
                log.debug("Database table metrics registered");
            } catch (Exception e) {
                log.debug("Could not register database table metrics: {}", e.getMessage());
            }
        } else {
            log.warn("DataSource is not HikariDataSource, HikariCP metrics will not be available");
        }
    }
}
