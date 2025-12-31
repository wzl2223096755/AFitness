package com.wzl.fitness.exception;

import com.wzl.fitness.common.ResponseCode;

/**
 * 用户不存在异常
 * 当请求的用户不存在时抛出此异常
 */
public class UserNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;
    
    public UserNotFoundException() {
        super(ResponseCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(String message) {
        super(ResponseCode.USER_NOT_FOUND.getCode(), message);
    }
    
    public UserNotFoundException(Long userId) {
        super(ResponseCode.USER_NOT_FOUND.getCode(), "用户ID: " + userId + " 不存在");
    }
}