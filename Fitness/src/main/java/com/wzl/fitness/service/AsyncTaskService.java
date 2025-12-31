package com.wzl.fitness.service;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 异步任务服务 - 带监控功能
 * 
 * 提供以下监控指标:
 * - async.task.submitted: 提交的任务总数
 * - async.task.completed: 完成的任务总数
 * - async.task.failed: 失败的任务总数
 * - async.task.active: 当前活跃任务数
 * - async.task.duration: 任务执行时间分布
 * - async.task.queue.size: 任务队列大小
 * - async.task.pool.size: 线程池大小
 * - async.task.pool.active: 活跃线程数
 */
@Service
@EnableAsync
@Slf4j
public class AsyncTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);
    
    private final MeterRegistry meterRegistry;
    private final ThreadPoolTaskExecutor taskExecutor;
    
    // 监控指标
    private Counter submittedCounter;
    private Counter completedCounter;
    private Counter failedCounter;
    private Timer taskDurationTimer;
    private final AtomicLong activeTaskCount = new AtomicLong(0);
    
    public AsyncTaskService(MeterRegistry meterRegistry, 
                           @Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.meterRegistry = meterRegistry;
        this.taskExecutor = taskExecutor;
    }
    
    @PostConstruct
    public void initMetrics() {
        // 任务提交计数器
        submittedCounter = Counter.builder("async.task.submitted")
                .description("提交的异步任务总数")
                .tag("service", "AsyncTaskService")
                .register(meterRegistry);
        
        // 任务完成计数器
        completedCounter = Counter.builder("async.task.completed")
                .description("完成的异步任务总数")
                .tag("service", "AsyncTaskService")
                .register(meterRegistry);
        
        // 任务失败计数器
        failedCounter = Counter.builder("async.task.failed")
                .description("失败的异步任务总数")
                .tag("service", "AsyncTaskService")
                .register(meterRegistry);
        
        // 任务执行时间计时器
        taskDurationTimer = Timer.builder("async.task.duration")
                .description("异步任务执行时间")
                .tag("service", "AsyncTaskService")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);
        
        // 活跃任务数量仪表
        Gauge.builder("async.task.active", activeTaskCount, AtomicLong::get)
                .description("当前活跃的异步任务数")
                .tag("service", "AsyncTaskService")
                .register(meterRegistry);
        
        // 线程池队列大小仪表
        Gauge.builder("async.task.queue.size", taskExecutor, 
                executor -> executor.getThreadPoolExecutor().getQueue().size())
                .description("异步任务队列大小")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        // 线程池大小仪表
        Gauge.builder("async.task.pool.size", taskExecutor,
                executor -> executor.getThreadPoolExecutor().getPoolSize())
                .description("线程池当前大小")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        // 活跃线程数仪表
        Gauge.builder("async.task.pool.active", taskExecutor,
                executor -> executor.getThreadPoolExecutor().getActiveCount())
                .description("线程池活跃线程数")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        // 线程池核心大小仪表
        Gauge.builder("async.task.pool.core.size", taskExecutor,
                executor -> executor.getThreadPoolExecutor().getCorePoolSize())
                .description("线程池核心大小")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        // 线程池最大大小仪表
        Gauge.builder("async.task.pool.max.size", taskExecutor,
                executor -> executor.getThreadPoolExecutor().getMaximumPoolSize())
                .description("线程池最大大小")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        // 已完成任务总数仪表
        Gauge.builder("async.task.pool.completed", taskExecutor,
                executor -> executor.getThreadPoolExecutor().getCompletedTaskCount())
                .description("线程池已完成任务总数")
                .tag("pool", "taskExecutor")
                .register(meterRegistry);
        
        logger.info("AsyncTaskService监控指标初始化完成");
    }

    /**
     * 异步执行任务（带监控）
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return executeAsync(task, "default");
    }
    
    /**
     * 异步执行任务（带监控和任务类型标签）
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeAsync(Runnable task, String taskType) {
        submittedCounter.increment();
        activeTaskCount.incrementAndGet();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            task.run();
            completedCounter.increment();
            logger.debug("异步任务执行成功: type={}", taskType);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            failedCounter.increment();
            logger.error("异步任务执行失败: type={}", taskType, e);
            return CompletableFuture.failedFuture(e);
        } finally {
            activeTaskCount.decrementAndGet();
            sample.stop(Timer.builder("async.task.duration.by.type")
                    .tag("type", taskType)
                    .register(meterRegistry));
            sample.stop(taskDurationTimer);
        }
    }

    /**
     * 异步执行并返回结果（带监控）
     */
    @Async("taskExecutor")
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return supplyAsync(supplier, "default");
    }
    
    /**
     * 异步执行并返回结果（带监控和任务类型标签）
     */
    @Async("taskExecutor")
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, String taskType) {
        submittedCounter.increment();
        activeTaskCount.incrementAndGet();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            T result = supplier.get();
            completedCounter.increment();
            logger.debug("异步任务执行成功: type={}", taskType);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            failedCounter.increment();
            logger.error("异步任务执行失败: type={}", taskType, e);
            return CompletableFuture.failedFuture(e);
        } finally {
            activeTaskCount.decrementAndGet();
            sample.stop(Timer.builder("async.task.duration.by.type")
                    .tag("type", taskType)
                    .register(meterRegistry));
            sample.stop(taskDurationTimer);
        }
    }

    /**
     * 异步发送邮件（带监控）
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmailAsync(String to, String subject, String content) {
        submittedCounter.increment();
        activeTaskCount.incrementAndGet();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            logger.info("异步发送邮件: {}, 主题: {}", to, subject);
            // 模拟邮件发送延迟
            Thread.sleep(100);
            completedCounter.increment();
            
            // 记录邮件发送成功指标
            meterRegistry.counter("async.email.sent", "status", "success").increment();
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            failedCounter.increment();
            logger.error("发送邮件失败: {}", to, e);
            
            // 记录邮件发送失败指标
            meterRegistry.counter("async.email.sent", "status", "failed").increment();
            
            return CompletableFuture.failedFuture(e);
        } finally {
            activeTaskCount.decrementAndGet();
            sample.stop(Timer.builder("async.task.duration.by.type")
                    .tag("type", "email")
                    .register(meterRegistry));
            sample.stop(taskDurationTimer);
        }
    }

    /**
     * 异步记录日志（带监控）
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> logAsync(String message) {
        submittedCounter.increment();
        activeTaskCount.incrementAndGet();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            logger.info("异步日志: {}", message);
            completedCounter.increment();
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            failedCounter.increment();
            logger.error("异步日志记录失败", e);
            return CompletableFuture.failedFuture(e);
        } finally {
            activeTaskCount.decrementAndGet();
            sample.stop(Timer.builder("async.task.duration.by.type")
                    .tag("type", "log")
                    .register(meterRegistry));
            sample.stop(taskDurationTimer);
        }
    }
    
    /**
     * 获取线程池状态信息
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        return new ThreadPoolStatus(
                taskExecutor.getThreadPoolExecutor().getPoolSize(),
                taskExecutor.getThreadPoolExecutor().getActiveCount(),
                taskExecutor.getThreadPoolExecutor().getCorePoolSize(),
                taskExecutor.getThreadPoolExecutor().getMaximumPoolSize(),
                taskExecutor.getThreadPoolExecutor().getQueue().size(),
                taskExecutor.getThreadPoolExecutor().getCompletedTaskCount(),
                activeTaskCount.get()
        );
    }
    
    /**
     * 线程池状态信息
     */
    public record ThreadPoolStatus(
            int poolSize,
            int activeCount,
            int corePoolSize,
            int maxPoolSize,
            int queueSize,
            long completedTaskCount,
            long activeTaskCount
    ) {}
}
