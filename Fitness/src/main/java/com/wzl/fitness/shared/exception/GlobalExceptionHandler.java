package com.wzl.fitness.shared.exception;

import com.wzl.fitness.shared.common.ApiResponse;
import com.wzl.fitness.shared.common.ResponseCode;
import com.wzl.fitness.dto.response.ValidationErrorResponse;
import com.wzl.fitness.dto.response.ValidationErrorResponse.FieldErrorDetail;
import com.wzl.fitness.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回规范化的错误响应
 * 提供详细的字段验证错误信息
 * 
 * 注意：此类暂不注册为Bean，待完全迁移后启用
 * 当前系统仍使用 com.wzl.fitness.exception.GlobalExceptionHandler
 */
// @RestControllerAdvice // 暂时禁用，避免与旧类冲突
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数校验异常 (RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();
        
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String errorMessage = error.getDefaultMessage();
                Object rejectedValue = fieldError.getRejectedValue();
                String constraintType = extractConstraintType(error.getCode());
                
                fieldErrors.put(fieldName, FieldErrorDetail.of(
                        fieldName, errorMessage, rejectedValue, constraintType));
            } else {
                globalErrors.add(error.getDefaultMessage());
            }
        });
        
        logger.warn("参数校验失败 [{}]: 字段错误数={}, 全局错误数={}", 
                request.getRequestURI(), fieldErrors.size(), globalErrors.size());
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(
                fieldErrors, 
                globalErrors.isEmpty() ? null : globalErrors, 
                request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常 (表单提交)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();
        
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String errorMessage = error.getDefaultMessage();
                Object rejectedValue = fieldError.getRejectedValue();
                String constraintType = extractConstraintType(error.getCode());
                
                fieldErrors.put(fieldName, FieldErrorDetail.of(
                        fieldName, errorMessage, rejectedValue, constraintType));
            } else {
                globalErrors.add(error.getDefaultMessage());
            }
        });
        
        logger.warn("数据绑定失败 [{}]: 字段错误数={}", request.getRequestURI(), fieldErrors.size());
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(
                fieldErrors, 
                globalErrors.isEmpty() ? null : globalErrors, 
                request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常 (路径参数/查询参数验证)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            int lastDot = path.lastIndexOf('.');
            String fieldName = lastDot > 0 ? path.substring(lastDot + 1) : path;
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();
            String constraintType = violation.getConstraintDescriptor()
                    .getAnnotation().annotationType().getSimpleName();
            
            if (fieldErrors.containsKey(fieldName)) {
                FieldErrorDetail existing = fieldErrors.get(fieldName);
                String combinedMessage = existing.getMessage() + "; " + message;
                fieldErrors.put(fieldName, FieldErrorDetail.of(
                        fieldName, combinedMessage, invalidValue, constraintType));
            } else {
                fieldErrors.put(fieldName, FieldErrorDetail.of(
                        fieldName, message, invalidValue, constraintType));
            }
        }
        
        logger.warn("约束违反 [{}]: 字段错误数={}", request.getRequestURI(), fieldErrors.size());
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(fieldErrors, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        fieldErrors.put(e.getParameterName(), FieldErrorDetail.of(
                e.getParameterName(), 
                "参数 '" + e.getParameterName() + "' 不能为空",
                null,
                "Required"));
        
        logger.warn("缺少请求参数 [{}]: {}", request.getRequestURI(), e.getParameterName());
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(fieldErrors, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知";
        fieldErrors.put(e.getName(), FieldErrorDetail.of(
                e.getName(), 
                "参数类型错误，期望类型: " + expectedType,
                e.getValue(),
                "TypeMismatch"));
        
        logger.warn("参数类型不匹配 [{}]: {} 期望 {}", 
                request.getRequestURI(), e.getName(), expectedType);
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(fieldErrors, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理请求体不可读异常（如JSON格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        
        String message = "请求体格式错误";
        Throwable cause = e.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null && causeMessage.length() > 200) {
                causeMessage = causeMessage.substring(0, 200) + "...";
            }
            message = "请求体格式错误: " + causeMessage;
        }
        
        Map<String, FieldErrorDetail> fieldErrors = new HashMap<>();
        fieldErrors.put("requestBody", FieldErrorDetail.of(
                "requestBody", message, null, "JsonParse"));
        
        logger.warn("请求体不可读 [{}]: {}", request.getRequestURI(), e.getMessage());
        
        ValidationErrorResponse response = ValidationErrorResponse.ofDetailed(fieldErrors, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理不支持的HTTP方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        logger.warn("不支持的HTTP方法: {}", e.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ResponseCode.METHOD_NOT_ALLOWED.getCode(), 
                        "不支持的HTTP方法: " + e.getMethod()));
    }
    
    /**
     * 处理不支持的媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        logger.warn("不支持的媒体类型: {}", e.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR.getCode(), 
                        "不支持的媒体类型: " + e.getContentType()));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        logger.warn("认证失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ResponseCode.UNAUTHORIZED.getCode(), "认证失败"));
    }

    /**
     * 处理凭据错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn("用户名或密码错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ResponseCode.UNAUTHORIZED.getCode(), "用户名或密码错误"));
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("访问被拒绝: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ResponseCode.FORBIDDEN.getCode(), "访问被拒绝"));
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("资源未找到: {}", e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ResponseCode.NOT_FOUND.getCode(), "资源未找到"));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR.getCode(), "服务器内部错误"));
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        logger.error("未知异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR.getCode(), "服务器内部错误"));
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST.getCode(), e.getMessage()));
    }

    /**
     * 处理IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        logger.warn("状态错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST.getCode(), e.getMessage()));
    }

    // ==================== 数据库连接异常处理 ====================

    /**
     * 处理数据库连接获取失败异常
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleCannotGetJdbcConnectionException(
            CannotGetJdbcConnectionException e) {
        logger.error("数据库连接获取失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ResponseCode.SERVICE_UNAVAILABLE.getCode(), "数据库连接暂时不可用，请稍后重试"));
    }

    /**
     * 处理数据库查询超时异常
     */
    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleQueryTimeoutException(QueryTimeoutException e) {
        logger.error("数据库查询超时: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR.getCode(), "数据库查询超时，请稍后重试"));
    }

    /**
     * 处理数据库锁获取失败异常
     */
    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ApiResponse<Void>> handleCannotAcquireLockException(CannotAcquireLockException e) {
        logger.error("数据库锁获取失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ResponseCode.CONFLICT.getCode(), "数据库资源繁忙，请稍后重试"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 从验证注解代码中提取约束类型
     */
    private String extractConstraintType(String code) {
        if (code == null) return "Unknown";
        
        return switch (code) {
            case "NotBlank", "NotEmpty", "NotNull" -> "Required";
            case "Size" -> "Size";
            case "Min", "Max" -> "Range";
            case "Pattern" -> "Pattern";
            case "Email" -> "Email";
            case "Past", "PastOrPresent" -> "PastDate";
            case "Future", "FutureOrPresent" -> "FutureDate";
            case "Positive", "PositiveOrZero" -> "Positive";
            case "Negative", "NegativeOrZero" -> "Negative";
            case "Digits" -> "Digits";
            case "DecimalMin", "DecimalMax" -> "DecimalRange";
            case "AssertTrue", "AssertFalse" -> "Boolean";
            case "NoXss", "SafeHtml" -> "Security";
            case "ValidDateRange" -> "DateRange";
            case "PasswordMatch" -> "PasswordMatch";
            default -> code;
        };
    }
}
