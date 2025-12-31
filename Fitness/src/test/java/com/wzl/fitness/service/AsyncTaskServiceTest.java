package com.wzl.fitness.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AsyncTaskService单元测试
 * 
 * 测试异步任务执行和监控指标
 */
class AsyncTaskServiceTest {
    
    private AsyncTaskService asyncTaskService;
    private MeterRegistry meterRegistry;
    private ThreadPoolTaskExecutor taskExecutor;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setQueueCapacity(10);
        taskExecutor.setThreadNamePrefix("test-async-");
        taskExecutor.initialize();
        
        asyncTaskService = new AsyncTaskService(meterRegistry, taskExecutor);
        asyncTaskService.initMetrics();
    }
    
    @Test
    void testExecuteAsync_Success() throws ExecutionException, InterruptedException, TimeoutException {
        AtomicBoolean executed = new AtomicBoolean(false);
        
        CompletableFuture<Void> future = asyncTaskService.executeAsync(() -> {
            executed.set(true);
        });
        
        // 等待任务完成
        future.get(5, TimeUnit.SECONDS);
        
        assertTrue(executed.get(), "任务应该被执行");
        assertFalse(future.isCompletedExceptionally(), "任务应该成功完成");
    }
    
    @Test
    void testExecuteAsync_WithTaskType() throws ExecutionException, InterruptedException, TimeoutException {
        AtomicBoolean executed = new AtomicBoolean(false);
        
        CompletableFuture<Void> future = asyncTaskService.executeAsync(() -> {
            executed.set(true);
        }, "test-task");
        
        future.get(5, TimeUnit.SECONDS);
        
        assertTrue(executed.get(), "任务应该被执行");
    }
    
    @Test
    void testSupplyAsync_Success() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> future = asyncTaskService.supplyAsync(() -> "Hello, World!");
        
        String result = future.get(5, TimeUnit.SECONDS);
        
        assertEquals("Hello, World!", result);
    }
    
    @Test
    void testSupplyAsync_WithTaskType() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Integer> future = asyncTaskService.supplyAsync(() -> 42, "calculation");
        
        Integer result = future.get(5, TimeUnit.SECONDS);
        
        assertEquals(42, result);
    }
    
    @Test
    void testExecuteAsync_Failure() {
        CompletableFuture<Void> future = asyncTaskService.executeAsync(() -> {
            throw new RuntimeException("Test exception");
        });
        
        // 等待任务完成
        assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertTrue(future.isCompletedExceptionally(), "任务应该异常完成");
    }
    
    @Test
    void testSupplyAsync_Failure() {
        CompletableFuture<String> future = asyncTaskService.supplyAsync(() -> {
            throw new RuntimeException("Test exception");
        });
        
        assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertTrue(future.isCompletedExceptionally(), "任务应该异常完成");
    }
    
    @Test
    void testSendEmailAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = asyncTaskService.sendEmailAsync(
                "test@example.com", 
                "Test Subject", 
                "Test Content"
        );
        
        future.get(5, TimeUnit.SECONDS);
        
        assertFalse(future.isCompletedExceptionally(), "邮件发送应该成功");
    }
    
    @Test
    void testLogAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = asyncTaskService.logAsync("Test log message");
        
        future.get(5, TimeUnit.SECONDS);
        
        assertFalse(future.isCompletedExceptionally(), "日志记录应该成功");
    }
    
    @Test
    void testGetThreadPoolStatus() {
        AsyncTaskService.ThreadPoolStatus status = asyncTaskService.getThreadPoolStatus();
        
        assertNotNull(status, "线程池状态不应为空");
        assertEquals(2, status.corePoolSize(), "核心线程数应该是2");
        assertEquals(4, status.maxPoolSize(), "最大线程数应该是4");
        assertTrue(status.poolSize() >= 0, "线程池大小应该非负");
        assertTrue(status.activeCount() >= 0, "活跃线程数应该非负");
        assertTrue(status.queueSize() >= 0, "队列大小应该非负");
        assertTrue(status.completedTaskCount() >= 0, "已完成任务数应该非负");
        assertTrue(status.activeTaskCount() >= 0, "活跃任务数应该非负");
    }
    
    @Test
    void testMetricsRegistered() {
        // 验证监控指标已注册
        assertNotNull(meterRegistry.find("async.task.submitted").counter(), 
                "提交任务计数器应该已注册");
        assertNotNull(meterRegistry.find("async.task.completed").counter(), 
                "完成任务计数器应该已注册");
        assertNotNull(meterRegistry.find("async.task.failed").counter(), 
                "失败任务计数器应该已注册");
        assertNotNull(meterRegistry.find("async.task.duration").timer(), 
                "任务执行时间计时器应该已注册");
        assertNotNull(meterRegistry.find("async.task.active").gauge(), 
                "活跃任务数仪表应该已注册");
        assertNotNull(meterRegistry.find("async.task.queue.size").gauge(), 
                "队列大小仪表应该已注册");
        assertNotNull(meterRegistry.find("async.task.pool.size").gauge(), 
                "线程池大小仪表应该已注册");
        assertNotNull(meterRegistry.find("async.task.pool.active").gauge(), 
                "活跃线程数仪表应该已注册");
    }
    
    @Test
    void testMetricsIncrement() throws ExecutionException, InterruptedException, TimeoutException {
        // 执行一个成功的任务
        asyncTaskService.executeAsync(() -> {}).get(5, TimeUnit.SECONDS);
        
        // 验证计数器增加
        double submittedCount = meterRegistry.find("async.task.submitted").counter().count();
        double completedCount = meterRegistry.find("async.task.completed").counter().count();
        
        assertTrue(submittedCount >= 1, "提交任务计数应该至少为1");
        assertTrue(completedCount >= 1, "完成任务计数应该至少为1");
    }
    
    @Test
    void testConcurrentTasks() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 10;
        
        CompletableFuture<?>[] futures = new CompletableFuture[taskCount];
        
        for (int i = 0; i < taskCount; i++) {
            futures[i] = asyncTaskService.executeAsync(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        
        assertEquals(taskCount, counter.get(), "所有任务应该都被执行");
    }
}
