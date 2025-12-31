package com.wzl.fitness.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置 - 带监控功能
 * 
 * 提供以下线程池:
 * - taskExecutor: 通用异步任务线程池
 * - cacheWarmupExecutor: 缓存预热专用线程池
 * 
 * 监控指标通过Micrometer自动暴露到Prometheus
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 异步任务线程池（带监控）
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数 = CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数 = CPU核心数 * 2
        executor.setMaxPoolSize(corePoolSize * 2);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");
        
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        // 注册线程池监控指标到Micrometer
        ExecutorServiceMetrics.monitor(
                meterRegistry, 
                executor.getThreadPoolExecutor(), 
                "taskExecutor",
                Tag.of("pool", "async-task")
        );
        
        return executor;
    }

    /**
     * 缓存预热线程池（带监控）
     */
    @Bean("cacheWarmupExecutor")
    public ThreadPoolTaskExecutor cacheWarmupExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("cache-warmup-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        // 注册线程池监控指标到Micrometer
        ExecutorServiceMetrics.monitor(
                meterRegistry, 
                executor.getThreadPoolExecutor(), 
                "cacheWarmupExecutor",
                Tag.of("pool", "cache-warmup")
        );
        
        return executor;
    }
}
