package com.wzl.fitness.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.wzl.fitness.annotation.SensitiveData;
import com.wzl.fitness.util.DataMaskingUtils;

import java.io.IOException;

/**
 * 敏感数据序列化器
 * 在JSON序列化时自动对敏感数据进行脱敏处理
 */
public class SensitiveDataSerializer extends JsonSerializer<String> implements ContextualSerializer {
    
    private SensitiveData.SensitiveType sensitiveType;
    
    public SensitiveDataSerializer() {
        this.sensitiveType = SensitiveData.SensitiveType.DEFAULT;
    }
    
    public SensitiveDataSerializer(SensitiveData.SensitiveType sensitiveType) {
        this.sensitiveType = sensitiveType;
    }
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        String maskedValue = maskValue(value);
        gen.writeString(maskedValue);
    }
    
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }
        
        SensitiveData annotation = property.getAnnotation(SensitiveData.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(SensitiveData.class);
        }
        
        if (annotation != null) {
            return new SensitiveDataSerializer(annotation.type());
        }
        
        return this;
    }
    
    /**
     * 根据敏感数据类型进行脱敏
     */
    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        return switch (sensitiveType) {
            case EMAIL -> DataMaskingUtils.maskEmail(value);
            case PHONE -> DataMaskingUtils.maskPhone(value);
            case ID_CARD -> DataMaskingUtils.maskIdCard(value);
            case BANK_CARD -> DataMaskingUtils.maskBankCard(value);
            case USERNAME -> DataMaskingUtils.maskUsername(value);
            case PASSWORD -> DataMaskingUtils.maskPassword(value);
            case ADDRESS -> DataMaskingUtils.maskAddress(value);
            case NAME -> DataMaskingUtils.maskName(value);
            case IP -> DataMaskingUtils.maskIp(value);
            default -> maskDefault(value);
        };
    }
    
    /**
     * 默认脱敏策略
     */
    private String maskDefault(String value) {
        int length = value.length();
        if (length <= 2) {
            return "*".repeat(length);
        } else if (length <= 4) {
            return value.charAt(0) + "*".repeat(length - 2) + value.charAt(length - 1);
        } else {
            int maskLength = length - 4;
            return value.substring(0, 2) + "*".repeat(Math.min(maskLength, 6)) + value.substring(length - 2);
        }
    }
}
