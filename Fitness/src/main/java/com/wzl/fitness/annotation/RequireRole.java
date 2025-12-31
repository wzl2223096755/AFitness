package com.wzl.fitness.annotation;

import com.wzl.fitness.entity.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 用于标记需要特定角色才能访问的方法
 * 
 * 使用示例：
 * <code>
 * &#64;RequireRole({Role.ADMIN, Role.TRAINER})
 * public void sensitiveOperation() { ... }
 * </code>
 * 
 * @author AFitness Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 需要的角色数组
     * 支持多个角色，用户拥有其中任意一个角色即可访问
     * 
     * @return 所需的角色数组
     */
    Role[] value();
    
    /**
     * 权限验证失败时的错误消息
     * 支持国际化，默认为中文消息
     * 
     * @return 错误消息
     */
    String message() default "权限不足，无法访问此资源";
}