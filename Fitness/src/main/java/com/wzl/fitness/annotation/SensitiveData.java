package com.wzl.fitness.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wzl.fitness.serializer.SensitiveDataSerializer;

import java.lang.annotation.*;

/**
 * 敏感数据脱敏注解
 * 用于标记需要在JSON序列化时进行脱敏的字段
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveDataSerializer.class)
public @interface SensitiveData {
    
    /**
     * 敏感数据类型
     */
    SensitiveType type() default SensitiveType.DEFAULT;
    
    /**
     * 敏感数据类型枚举
     */
    enum SensitiveType {
        /**
         * 默认脱敏（中间部分用*替换）
         */
        DEFAULT,
        
        /**
         * 邮箱脱敏
         */
        EMAIL,
        
        /**
         * 手机号脱敏
         */
        PHONE,
        
        /**
         * 身份证号脱敏
         */
        ID_CARD,
        
        /**
         * 银行卡号脱敏
         */
        BANK_CARD,
        
        /**
         * 用户名脱敏
         */
        USERNAME,
        
        /**
         * 密码脱敏（完全隐藏）
         */
        PASSWORD,
        
        /**
         * 地址脱敏
         */
        ADDRESS,
        
        /**
         * 姓名脱敏
         */
        NAME,
        
        /**
         * IP地址脱敏
         */
        IP
    }
}
