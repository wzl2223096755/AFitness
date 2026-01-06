package com.wzl.fitness.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库重试注解
 * 标记需要在数据库操作失败时自动重试的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DatabaseRetryable {
    
    /**
     * 最大重试次数，默认为 3
     */
    int maxAttempts() default 3;
    
    /**
     * 初始重试间隔（毫秒），默认为 1000
     */
    long initialInterval() default 1000;
    
    /**
     * 重试间隔倍数，默认为 2.0
     */
    double multiplier() default 2.0;
    
    /**
     * 最大重试间隔（毫秒），默认为 10000
     */
    long maxInterval() default 10000;
}
