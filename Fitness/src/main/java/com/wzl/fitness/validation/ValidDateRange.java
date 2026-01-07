package com.wzl.fitness.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 日期范围验证注解
 * 用于验证开始日期不晚于结束日期
 */
@Documented
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "开始日期不能晚于结束日期";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 开始日期字段名
     */
    String startField();
    
    /**
     * 结束日期字段名
     */
    String endField();
}
