package com.wzl.fitness.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus监控指标配置
 * 配置自定义指标和标签
 * 
 * 包含以下指标类别:
 * - 用户操作指标 (登录、注册)
 * - 业务操作指标 (训练记录、营养记录)
 * - API性能指标 (响应时间)
 * - JVM线程指标
 * - 处理器指标
 */
@Configuration
public class MetricsConfig {

    /**
     * 配置全局标签
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "fitness-system")
                .commonTags("environment", System.getProperty("spring.profiles.active", "default"));
    }
    
    /**
     * JVM线程指标绑定
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }
    
    /**
     * 处理器指标绑定
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * 启用@Timed注解支持
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 用户登录计数器
     */
    @Bean
    public Counter loginCounter(MeterRegistry registry) {
        return Counter.builder("fitness.user.login")
                .description("用户登录次数")
                .tag("type", "total")
                .register(registry);
    }

    /**
     * 用户注册计数器
     */
    @Bean
    public Counter registerCounter(MeterRegistry registry) {
        return Counter.builder("fitness.user.register")
                .description("用户注册次数")
                .tag("type", "total")
                .register(registry);
    }

    /**
     * 训练记录创建计数器
     */
    @Bean
    public Counter trainingRecordCounter(MeterRegistry registry) {
        return Counter.builder("fitness.training.record")
                .description("训练记录创建次数")
                .tag("type", "create")
                .register(registry);
    }

    /**
     * 营养记录创建计数器
     */
    @Bean
    public Counter nutritionRecordCounter(MeterRegistry registry) {
        return Counter.builder("fitness.nutrition.record")
                .description("营养记录创建次数")
                .tag("type", "create")
                .register(registry);
    }

    /**
     * API响应时间计时器
     */
    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("fitness.api.response.time")
                .description("API响应时间")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }
}
