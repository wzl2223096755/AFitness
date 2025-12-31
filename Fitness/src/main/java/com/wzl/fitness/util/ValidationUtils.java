package com.wzl.fitness.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 参数校验工具类
 * 提供参数合法性校验功能
 */
public class ValidationUtils {
    private static final Validator VALIDATOR;
    
    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }
    
    /**
     * 校验对象属性
     * 使用JSR-380规范的注解进行校验
     */
    public static <T> String validate(T object) {
        if (object == null) {
            return "对象不能为空";
        }
        
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                message.append(violation.getMessage()).append(", ");
            }
            return message.substring(0, message.length() - 2);
        }
        return null;
    }
    
    /**
     * 校验字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return !StringUtils.hasText(str);
    }
    
    /**
     * 校验集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 校验Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * 校验数组是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
    
    /**
     * 校验对象是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return isEmpty((String) obj);
        }
        if (obj instanceof Collection) {
            return isEmpty((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return isEmpty((Map<?, ?>) obj);
        }
        if (obj.getClass().isArray()) {
            return isEmpty((Object[]) obj);
        }
        return false;
    }
    
    /**
     * 校验字符串长度
     */
    public static boolean checkLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 校验数值范围
     */
    public static boolean checkRange(Number num, Number min, Number max) {
        if (num == null) {
            return false;
        }
        double value = num.doubleValue();
        if (min != null && value < min.doubleValue()) {
            return false;
        }
        if (max != null && value > max.doubleValue()) {
            return false;
        }
        return true;
    }
    
    /**
     * 校验手机号格式
     */
    public static boolean isMobile(String mobile) {
        if (isEmpty(mobile)) {
            return false;
        }
        // 中国大陆手机号正则
        String pattern = "^1[3-9]\\d{9}$";
        return mobile.matches(pattern);
    }
    
    /**
     * 校验邮箱格式
     */
    public static boolean isEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        // 邮箱正则
        String pattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(pattern);
    }
    
    /**
     * 校验身份证号格式
     */
    public static boolean isIdCard(String idCard) {
        if (isEmpty(idCard)) {
            return false;
        }
        // 简单的身份证号正则（18位）
        String pattern = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        return idCard.matches(pattern);
    }
}