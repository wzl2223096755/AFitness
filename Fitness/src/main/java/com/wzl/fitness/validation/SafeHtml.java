package com.wzl.fitness.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 安全HTML验证注解
 * 用于验证字符串只包含安全的HTML内容
 */
@Documented
@Constraint(validatedBy = SafeHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeHtml {
    String message() default "输入包含不安全的HTML内容";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 是否允许基本HTML标签（如 b, i, u, br）
     */
    boolean allowBasicTags() default false;
}
