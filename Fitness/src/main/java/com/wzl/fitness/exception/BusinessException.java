package com.wzl.fitness.exception;

import com.wzl.fitness.common.ResponseCode;

/**
 * 业务异常基类
 * 所有业务相关的异常都应该继承此类
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    // 错误码
    private int code;
    
    // 错误消息
    private String message;
    
    public BusinessException() {
        super();
    }
    
    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResponseCode.ERROR.getCode();
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = ResponseCode.ERROR.getCode();
    }
    
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}