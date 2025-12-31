package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 统一验证错误响应DTO
 * 用于返回清晰的验证错误信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    
    /**
     * 响应码 (400)
     */
    private int code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 响应时间戳
     */
    private String timestamp;
    
    /**
     * 请求路径
     */
    private String path;
    
    /**
     * 字段级别的错误详情
     */
    private Map<String, String> fieldErrors;
    
    /**
     * 全局错误列表（非字段级别）
     */
    private List<String> globalErrors;
    
    /**
     * 错误总数
     */
    private int errorCount;
    
    /**
     * 创建验证错误响应
     */
    public static ValidationErrorResponse of(Map<String, String> fieldErrors, String path) {
        return ValidationErrorResponse.builder()
                .code(400)
                .message("请求参数验证失败")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .path(path)
                .fieldErrors(fieldErrors)
                .errorCount(fieldErrors.size())
                .build();
    }
    
    /**
     * 创建带全局错误的验证错误响应
     */
    public static ValidationErrorResponse of(Map<String, String> fieldErrors, List<String> globalErrors, String path) {
        int totalErrors = (fieldErrors != null ? fieldErrors.size() : 0) + 
                         (globalErrors != null ? globalErrors.size() : 0);
        return ValidationErrorResponse.builder()
                .code(400)
                .message("请求参数验证失败")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .path(path)
                .fieldErrors(fieldErrors)
                .globalErrors(globalErrors)
                .errorCount(totalErrors)
                .build();
    }
}
