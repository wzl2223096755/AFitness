package com.wzl.fitness.modules.training.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 训练模块配置类
 * 使用@ConditionalOnProperty实现模块条件加载
 * 
 * 当fitness.modules.training.enabled=true时，该模块的所有组件才会被加载
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@Configuration
@ConditionalOnProperty(
    prefix = "fitness.modules.training",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ComponentScan(basePackages = {
    "com.wzl.fitness.modules.training.api",
    "com.wzl.fitness.modules.training.controller",
    "com.wzl.fitness.modules.training.service",
    "com.wzl.fitness.modules.training.repository",
    "com.wzl.fitness.modules.training.event"
})
public class TrainingModuleConfig {
    
    private static final Logger log = LoggerFactory.getLogger(TrainingModuleConfig.class);
    
    @PostConstruct
    public void init() {
        log.info("训练模块已启用并加载完成");
    }
}
