package com.wzl.fitness.exception;

import com.wzl.fitness.common.ResponseCode;

/**
 * 设备绑定异常
 * 当设备绑定操作失败时抛出此异常
 */
public class DeviceBindException extends BusinessException {
    private static final long serialVersionUID = 1L;
    
    public DeviceBindException() {
        super(ResponseCode.DEVICE_BIND_FAILED);
    }
    
    public DeviceBindException(String message) {
        super(ResponseCode.DEVICE_BIND_FAILED.getCode(), message);
    }
    
    public DeviceBindException(Long userId, Long deviceId) {
        super(ResponseCode.DEVICE_BIND_FAILED.getCode(), "用户ID: " + userId + " 绑定设备ID: " + deviceId + " 失败");
    }
}