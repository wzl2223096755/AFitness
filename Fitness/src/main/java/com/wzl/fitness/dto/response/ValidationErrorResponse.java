package com.wzl.fitness.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一验证错误响应DTO
 * 提供详细的字段验证错误信息
 */
public class ValidationErrorResponse {
    private int code;
    private String message;
    private String timestamp;
    private String path;
    private Map<String, FieldErrorDetail> fieldErrors;
    private List<String> globalErrors;
    private int errorCount;
    
    public ValidationErrorResponse() {}
    
    public ValidationErrorResponse(int code, String message, String timestamp, String path,
                                   Map<String, FieldErrorDetail> fieldErrors, List<String> globalErrors, int errorCount) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.fieldErrors = fieldErrors;
        this.globalErrors = globalErrors;
        this.errorCount = errorCount;
    }
    
    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Map<String, FieldErrorDetail> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, FieldErrorDetail> fieldErrors) { this.fieldErrors = fieldErrors; }
    public List<String> getGlobalErrors() { return globalErrors; }
    public void setGlobalErrors(List<String> globalErrors) { this.globalErrors = globalErrors; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

    /**
     * 字段错误详情
     */
    public static class FieldErrorDetail {
        private String field;
        private String message;
        private Object rejectedValue;
        private String constraintType;
        
        public FieldErrorDetail() {}
        
        public FieldErrorDetail(String field, String message, Object rejectedValue, String constraintType) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
            this.constraintType = constraintType;
        }
        
        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
        public String getConstraintType() { return constraintType; }
        public void setConstraintType(String constraintType) { this.constraintType = constraintType; }
        
        public static FieldErrorDetail of(String field, String message) {
            return new FieldErrorDetail(field, message, null, null);
        }
        
        public static FieldErrorDetail of(String field, String message, Object rejectedValue, String constraintType) {
            return new FieldErrorDetail(field, message, maskSensitiveValue(field, rejectedValue), constraintType);
        }
        
        /**
         * 对敏感字段值进行脱敏处理
         */
        private static Object maskSensitiveValue(String field, Object value) {
            if (value == null) return null;
            String fieldLower = field.toLowerCase();
            if (fieldLower.contains("password") || fieldLower.contains("secret") || 
                fieldLower.contains("token") || fieldLower.contains("key")) {
                return "******";
            }
            return value;
        }
    }

    
    // Builder
    public static ValidationErrorResponseBuilder builder() { return new ValidationErrorResponseBuilder(); }
    
    public static class ValidationErrorResponseBuilder {
        private int code;
        private String message;
        private String timestamp;
        private String path;
        private Map<String, FieldErrorDetail> fieldErrors;
        private List<String> globalErrors;
        private int errorCount;
        
        public ValidationErrorResponseBuilder code(int v) { this.code = v; return this; }
        public ValidationErrorResponseBuilder message(String v) { this.message = v; return this; }
        public ValidationErrorResponseBuilder timestamp(String v) { this.timestamp = v; return this; }
        public ValidationErrorResponseBuilder path(String v) { this.path = v; return this; }
        public ValidationErrorResponseBuilder fieldErrors(Map<String, FieldErrorDetail> v) { this.fieldErrors = v; return this; }
        public ValidationErrorResponseBuilder globalErrors(List<String> v) { this.globalErrors = v; return this; }
        public ValidationErrorResponseBuilder errorCount(int v) { this.errorCount = v; return this; }
        
        public ValidationErrorResponse build() {
            return new ValidationErrorResponse(code, message, timestamp, path, fieldErrors, globalErrors, errorCount);
        }
    }
    
    /**
     * 创建简单的字段错误响应（兼容旧API）
     */
    public static ValidationErrorResponse of(Map<String, String> fieldErrors, String path) {
        Map<String, FieldErrorDetail> detailedErrors = new HashMap<>();
        if (fieldErrors != null) {
            fieldErrors.forEach((field, message) -> 
                detailedErrors.put(field, FieldErrorDetail.of(field, message)));
        }
        return ValidationErrorResponse.builder()
                .code(400)
                .message("请求参数验证失败")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .path(path)
                .fieldErrors(detailedErrors)
                .errorCount(detailedErrors.size())
                .build();
    }
    
    /**
     * 创建包含全局错误的响应（兼容旧API）
     */
    public static ValidationErrorResponse of(Map<String, String> fieldErrors, List<String> globalErrors, String path) {
        Map<String, FieldErrorDetail> detailedErrors = new HashMap<>();
        if (fieldErrors != null) {
            fieldErrors.forEach((field, message) -> 
                detailedErrors.put(field, FieldErrorDetail.of(field, message)));
        }
        int totalErrors = detailedErrors.size() + (globalErrors != null ? globalErrors.size() : 0);
        return ValidationErrorResponse.builder()
                .code(400)
                .message("请求参数验证失败")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .path(path)
                .fieldErrors(detailedErrors)
                .globalErrors(globalErrors)
                .errorCount(totalErrors)
                .build();
    }

    
    /**
     * 创建详细的字段错误响应
     */
    public static ValidationErrorResponse ofDetailed(Map<String, FieldErrorDetail> fieldErrors, String path) {
        return ValidationErrorResponse.builder()
                .code(400)
                .message("请求参数验证失败")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .path(path)
                .fieldErrors(fieldErrors)
                .errorCount(fieldErrors != null ? fieldErrors.size() : 0)
                .build();
    }
    
    /**
     * 创建详细的字段错误响应（包含全局错误）
     */
    public static ValidationErrorResponse ofDetailed(Map<String, FieldErrorDetail> fieldErrors, 
                                                      List<String> globalErrors, String path) {
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
