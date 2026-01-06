package com.wzl.fitness.aspect;

import com.wzl.fitness.annotation.DatabaseRetryable;
import com.wzl.fitness.model.RetryEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库重试切面
 * 拦截标记了 @DatabaseRetryable 注解的方法，在数据库操作失败时自动重试
 */
@Aspect
@Component
@Slf4j
public class DatabaseRetryAspect {
    
    /**
     * 环绕通知：拦截 @DatabaseRetryable 注解的方法
     */
    @Around("@annotation(databaseRetryable)")
    public Object retryDatabaseOperation(ProceedingJoinPoint joinPoint, DatabaseRetryable databaseRetryable) throws Throwable {
        String methodSignature = getMethodSignature(joinPoint);
        long startTime = System.currentTimeMillis();
        
        RetryTemplate retryTemplate = createRetryTemplate(databaseRetryable);
        
        return retryTemplate.execute(
            context -> {
                int attemptNumber = context.getRetryCount() + 1;
                
                try {
                    if (attemptNumber > 1) {
                        log.info("Retrying database operation [{}], attempt {}/{}", 
                                methodSignature, attemptNumber, databaseRetryable.maxAttempts());
                    }
                    
                    Object result = joinPoint.proceed();
                    
                    // 记录成功事件
                    if (attemptNumber > 1) {
                        logRetryEvent(methodSignature, attemptNumber, null, true, startTime);
                    }
                    
                    return result;
                } catch (Throwable e) {
                    if (isRetryableException(e)) {
                        log.warn("Database operation [{}] failed on attempt {}: {} - {}", 
                                methodSignature, attemptNumber, e.getClass().getSimpleName(), e.getMessage());
                        
                        // 记录重试事件
                        logRetryEvent(methodSignature, attemptNumber, e, false, startTime);
                        
                        throw new RuntimeException(e);
                    }
                    
                    // 非可重试异常，直接抛出
                    log.error("Non-retryable exception in database operation [{}]: {} - {}", 
                            methodSignature, e.getClass().getSimpleName(), e.getMessage());
                    throw e;
                }
            },
            context -> {
                // 所有重试都失败后的恢复逻辑
                Throwable lastException = context.getLastThrowable();
                log.error("All {} retry attempts exhausted for database operation [{}]. Last error: {}", 
                        databaseRetryable.maxAttempts(), methodSignature, 
                        lastException != null ? lastException.getMessage() : "unknown");
                
                if (lastException != null) {
                    if (lastException instanceof RuntimeException) {
                        throw (RuntimeException) lastException;
                    }
                    throw new RuntimeException("Database operation failed after " + 
                            databaseRetryable.maxAttempts() + " attempts", lastException);
                }
                throw new RuntimeException("Database operation failed after " + 
                        databaseRetryable.maxAttempts() + " attempts");
            }
        );
    }
    
    /**
     * 创建重试模板
     */
    private RetryTemplate createRetryTemplate(DatabaseRetryable config) {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(config.initialInterval());
        backOffPolicy.setMultiplier(config.multiplier());
        backOffPolicy.setMaxInterval(config.maxInterval());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试策略
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RuntimeException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(config.maxAttempts(), retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
    
    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Throwable e) {
        if (e instanceof TransientDataAccessException) {
            return true;
        }
        if (e instanceof CannotAcquireLockException) {
            return true;
        }
        if (e instanceof QueryTimeoutException) {
            return true;
        }
        if (e instanceof CannotGetJdbcConnectionException) {
            return true;
        }
        if (e instanceof SQLException) {
            return true;
        }
        // 检查根因
        Throwable cause = e.getCause();
        if (cause != null && cause != e) {
            return isRetryableException(cause);
        }
        return false;
    }
    
    /**
     * 获取方法签名
     */
    private String getMethodSignature(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }
    
    /**
     * 记录重试事件
     */
    private void logRetryEvent(String methodSignature, int attemptNumber, Throwable exception, 
                               boolean successful, long startTime) {
        RetryEvent event = RetryEvent.builder()
                .operationName("DatabaseOperation")
                .methodSignature(methodSignature)
                .attemptNumber(attemptNumber)
                .exceptionType(exception != null ? exception.getClass().getSimpleName() : null)
                .exceptionMessage(exception != null ? exception.getMessage() : null)
                .timestamp(LocalDateTime.now())
                .successful(successful)
                .durationMs(System.currentTimeMillis() - startTime)
                .build();
        
        if (successful) {
            log.info("Database retry succeeded: {}", event);
        } else {
            log.debug("Database retry event: {}", event);
        }
    }
}
