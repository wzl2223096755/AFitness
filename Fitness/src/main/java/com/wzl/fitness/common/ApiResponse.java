package com.wzl.fitness.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应类
 * 规范API的响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 响应码
    private int code;
    
    // 响应消息
    private String message;
    
    // 响应数据
    private T data;
    
    // 响应时间
    private String timestamp;
    
    // 是否成功
    private boolean success;
    
    public ApiResponse(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * 成功响应，带数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data, true);
    }
    
    /**
     * 成功响应，自定义消息和数据
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, data, true);
    }
    
    /**
     * 失败响应，使用预定义的响应码
     */
    public static <T> ApiResponse<T> fail(ResponseCode responseCode) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null, false);
    }
    
    /**
     * 失败响应，自定义消息
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }
    
    /**
     * 错误响应，自定义消息
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }
    
    /**
     * 错误响应，自定义消息和数据
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, false);
    }
    
    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> paramError(String message) {
        return new ApiResponse<>(ResponseCode.PARAM_ERROR.getCode(), message, null, false);
    }
    
    /**
     * 服务器错误响应
     */
    public static <T> ApiResponse<T> serverError() {
        return new ApiResponse<>(ResponseCode.SERVER_ERROR.getCode(), ResponseCode.SERVER_ERROR.getMessage(), null, false);
    }
}