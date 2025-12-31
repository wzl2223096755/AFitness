package com.wzl.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * JPA配置
 * 启用审计和性能优化
 * 
 * <p>性能优化配置说明：</p>
 * <ul>
 *   <li>启用事务管理，支持声明式事务</li>
 *   <li>配置批量操作优化（在application.properties中）</li>
 *   <li>启用JPA审计功能，自动记录创建/更新时间</li>
 * </ul>
 * 
 * <p>数据库优化建议：</p>
 * <ul>
 *   <li>使用 @QueryHints 优化只读查询</li>
 *   <li>使用 @EntityGraph 避免N+1查询问题</li>
 *   <li>对于大数据量查询使用分页</li>
 *   <li>使用投影(Projection)减少数据传输</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.wzl.fitness.repository")
@EntityScan(basePackages = "com.wzl.fitness.entity")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * 审计提供者实现
     * 用于自动填充 @CreatedBy 和 @LastModifiedBy 字段
     */
    public static class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            // 从Spring Security上下文获取当前用户
            try {
                org.springframework.security.core.context.SecurityContext context = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext();
                if (context.getAuthentication() != null && 
                    context.getAuthentication().isAuthenticated()) {
                    return Optional.of(context.getAuthentication().getName());
                }
            } catch (Exception e) {
                // 忽略异常，返回系统用户
            }
            return Optional.of("system");
        }
    }
}