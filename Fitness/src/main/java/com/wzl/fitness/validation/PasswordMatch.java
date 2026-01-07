package com.wzl.fitness.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 密码匹配验证注解
 * 用于验证密码和确认密码是否一致
 */
@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
    String message() default "两次输入的密码不一致";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 密码字段名
     */
    String passwordField() default "password";
    
    /**
     * 确认密码字段名
     */
    String confirmPasswordField() default "confirmPassword";
}
