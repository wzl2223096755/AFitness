package com.wzl.fitness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 异步任务服务
 */
@Service
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskService {
    
    // 显式添加Logger实例
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    /**
     * 异步执行任务
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeAsync(Runnable task) {
        try {
            task.run();
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("异步任务执行失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步执行并返回结果
     */
    @Async("taskExecutor")
    public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        try {
            T result = supplier.get();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("异步任务执行失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步发送邮件
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmailAsync(String to, String subject, String content) {
        try {
            // 这里可以集成邮件服务
            logger.info("异步发送邮件: {}, 主题: {}", to, subject);
            // 模拟邮件发送延迟
            Thread.sleep(100);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("发送邮件失败: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步记录日志
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> logAsync(String message) {
        try {
            logger.info("异步日志: {}", message);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("异步日志记录失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}