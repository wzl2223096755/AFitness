package com.wzl.fitness.exception;

/**
 * 权限不足异常
 */
public class InsufficientPermissionException extends BusinessException {
    
    public InsufficientPermissionException() {
        super(403, "权限不足");
    }
    
    public InsufficientPermissionException(String message) {
        super(403, message);
    }
}