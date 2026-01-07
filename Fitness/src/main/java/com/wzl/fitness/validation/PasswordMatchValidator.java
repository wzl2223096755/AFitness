package com.wzl.fitness.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

/**
 * 密码匹配验证器实现
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    
    private String passwordField;
    private String confirmPasswordField;
    
    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            Field passwordFieldObj = value.getClass().getDeclaredField(passwordField);
            Field confirmPasswordFieldObj = value.getClass().getDeclaredField(confirmPasswordField);
            
            passwordFieldObj.setAccessible(true);
            confirmPasswordFieldObj.setAccessible(true);
            
            Object password = passwordFieldObj.get(value);
            Object confirmPassword = confirmPasswordFieldObj.get(value);
            
            // 如果任一值为null，跳过验证（由@NotNull处理）
            if (password == null || confirmPassword == null) {
                return true;
            }
            
            boolean isValid = password.equals(confirmPassword);
            
            if (!isValid) {
                // 自定义错误消息位置
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                       .addPropertyNode(confirmPasswordField)
                       .addConstraintViolation();
            }
            
            return isValid;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }
}
