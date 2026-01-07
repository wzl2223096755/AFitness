package com.wzl.fitness.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

/**
 * 日期范围验证器实现
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    
    private String startField;
    private String endField;
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            Field startFieldObj = value.getClass().getDeclaredField(startField);
            Field endFieldObj = value.getClass().getDeclaredField(endField);
            
            startFieldObj.setAccessible(true);
            endFieldObj.setAccessible(true);
            
            Object startValue = startFieldObj.get(value);
            Object endValue = endFieldObj.get(value);
            
            // 如果任一值为null，跳过验证
            if (startValue == null || endValue == null) {
                return true;
            }
            
            // 处理LocalDate
            if (startValue instanceof LocalDate && endValue instanceof LocalDate) {
                LocalDate start = (LocalDate) startValue;
                LocalDate end = (LocalDate) endValue;
                return !start.isAfter(end);
            }
            
            // 处理LocalDateTime
            if (startValue instanceof LocalDateTime && endValue instanceof LocalDateTime) {
                LocalDateTime start = (LocalDateTime) startValue;
                LocalDateTime end = (LocalDateTime) endValue;
                return !start.isAfter(end);
            }
            
            return true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }
}
