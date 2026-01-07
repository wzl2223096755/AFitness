package com.wzl.fitness.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义XSS防护验证注解
 * 用于验证字符串不包含XSS攻击代码
 */
@Documented
@Constraint(validatedBy = NoXssValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoXss {
    String message() default "输入包含非法字符";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
