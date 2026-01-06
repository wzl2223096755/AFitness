package com.wzl.fitness.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 事务管理配置类
 * 配置事务超时时间和事务模板
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Value("${spring.transaction.default-timeout:30}")
    private int defaultTimeout;
    
    /**
     * 创建事务模板
     * 用于编程式事务管理
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(defaultTimeout);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        log.info("TransactionTemplate configured with timeout: {}s", defaultTimeout);
        return template;
    }
    
    /**
     * 创建只读事务模板
     * 用于查询操作，优化性能
     */
    @Bean
    public TransactionTemplate readOnlyTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(defaultTimeout);
        template.setReadOnly(true);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        
        log.info("ReadOnly TransactionTemplate configured with timeout: {}s", defaultTimeout);
        return template;
    }
}
