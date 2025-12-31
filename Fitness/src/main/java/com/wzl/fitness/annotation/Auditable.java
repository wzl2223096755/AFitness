package com.wzl.fitness.annotation;

import com.wzl.fitness.entity.AuditLog.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解
 * 标记需要记录审计日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * 审计操作类型
     */
    AuditAction action();
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 资源类型
     */
    String resourceType() default "";
}
