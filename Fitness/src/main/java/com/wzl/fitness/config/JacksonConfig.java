package com.wzl.fitness.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson JSON序列化配置
 * 优化API响应的JSON序列化性能和格式
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 配置ObjectMapper
     * 优化JSON序列化/反序列化性能
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置LocalDate序列化/反序列化
        javaTimeModule.addSerializer(LocalDate.class, 
            new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, 
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        
        // 配置LocalDateTime序列化（输出格式）
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        
        // 配置LocalDateTime反序列化（支持多种输入格式）
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        return Jackson2ObjectMapperBuilder.json()
                .modules(javaTimeModule)
                // 忽略null值，减少响应体积
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                // 禁用空对象序列化失败
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 禁用未知属性反序列化失败
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 禁用日期作为时间戳输出
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 禁用缩进输出（生产环境减少响应体积）
                .featuresToDisable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }
    
    /**
     * 灵活的LocalDateTime反序列化器
     * 支持多种日期时间格式：
     * - ISO 8601格式（带Z后缀）: 2026-01-10T00:00:00.000Z
     * - ISO 8601格式（不带毫秒）: 2026-01-10T00:00:00Z
     * - 标准格式: yyyy-MM-dd HH:mm:ss
     * - ISO本地格式: yyyy-MM-ddTHH:mm:ss
     */
    private static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        
        private static final DateTimeFormatter STANDARD_FORMATTER = 
            DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        private static final DateTimeFormatter ISO_LOCAL_FORMATTER = 
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateStr = p.getText().trim();
            
            if (dateStr.isEmpty()) {
                return null;
            }
            
            // 处理ISO 8601格式（带Z后缀，表示UTC时间）
            if (dateStr.endsWith("Z")) {
                try {
                    Instant instant = Instant.parse(dateStr);
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } catch (DateTimeParseException e) {
                    // 继续尝试其他格式
                }
            }
            
            // 处理带时区偏移的ISO格式（如 2026-01-10T00:00:00+08:00）
            if (dateStr.contains("+") || (dateStr.contains("T") && dateStr.lastIndexOf("-") > 10)) {
                try {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (DateTimeParseException e) {
                    // 继续尝试其他格式
                }
            }
            
            // 处理ISO本地格式（带T分隔符，不带时区）
            if (dateStr.contains("T")) {
                try {
                    return LocalDateTime.parse(dateStr, ISO_LOCAL_FORMATTER);
                } catch (DateTimeParseException e) {
                    // 继续尝试其他格式
                }
            }
            
            // 处理标准格式 yyyy-MM-dd HH:mm:ss
            try {
                return LocalDateTime.parse(dateStr, STANDARD_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IOException("无法解析日期时间: " + dateStr + 
                    "。支持的格式: yyyy-MM-dd HH:mm:ss, yyyy-MM-ddTHH:mm:ss, ISO 8601 (带Z或时区偏移)", e);
            }
        }
    }
}
