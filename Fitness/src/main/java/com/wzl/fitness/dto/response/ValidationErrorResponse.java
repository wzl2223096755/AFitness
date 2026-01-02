package com.wzl.fitness.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 统一验证错误响应DTO
 */
public class ValidationErrorResponse {
    private int code;
    private String message;
    private String timestamp;
    private String path;
    private Map<String, String> fieldErrors;
    private List<String> globalErrors;
    private int errorCount;
    
    public ValidationErrorResponse() {}
    
    public ValidationErrorResponse(int code, String message, String timestamp, String path,
                                   Map<String, String> fieldErrors, List<String> globalErrors, int errorCount) {
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
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    public List<String> getGlobalErrors() { return globalErrors; }
    public void setGlobalErrors(List<String> globalErrors) { this.globalErrors = globalErrors; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    
    // Builder
    public static ValidationErrorResponseBuilder builder() { return new ValidationErrorResponseBuilder(); }
    
    public static class ValidationErrorResponseBuilder {
        private int code;
        private String message;
        private String timestamp;
        private String path;
        private Map<String, String> fieldErrors;
        private List<String> globalErrors;
        private int errorCount;
        
        public ValidationErrorResponseBuilder code(int v) { this.code = v; return this; }
        public ValidationErrorResponseBuilder message(String v) { this.message = v; return this; }
        public ValidationErrorResponseBuilder timestamp(String v) { this.timestamp = v; return this; }
        public ValidationErrorResponseBuilder path(String v) { this.path = v; return this; }
        public ValidationErrorResponseBuilder fieldErrors(Map<String, String> v) { this.fieldErrors = v; return this; }
        public ValidationErrorResponseBuilder globalErrors(List<String> v) { this.globalErrors = v; return this; }
        public ValidationErrorResponseBuilder errorCount(int v) { this.errorCount = v; return this; }
        
        public ValidationErrorResponse build() {
            return new ValidationErrorResponse(code, message, timestamp, path, fieldErrors, globalErrors, errorCount);
        }
    }
    
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
    
    public static ValidationErrorResponse of(Map<String, String> fieldErrors, List<String> globalErrors, String path) {
        int totalErrors = (fieldErrors != null ? fieldErrors.size() : 0) + (globalErrors != null ? globalErrors.size() : 0);
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
