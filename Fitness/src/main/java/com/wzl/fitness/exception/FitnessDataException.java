package com.wzl.fitness.exception;

import com.wzl.fitness.common.ResponseCode;

/**
 * 健身数据异常
 * 当健身数据处理过程中发生异常时抛出此异常
 */
public class FitnessDataException extends BusinessException {
    private static final long serialVersionUID = 1L;
    
    public FitnessDataException() {
        super(ResponseCode.FITNESS_DATA_EXCEPTION);
    }
    
    public FitnessDataException(String message) {
        super(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), message);
    }
    
    public FitnessDataException(Long userId) {
        super(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "用户ID: " + userId + " 的健身数据处理异常");
    }
    
    public FitnessDataException(Long userId, Long deviceId) {
        super(ResponseCode.FITNESS_DATA_EXCEPTION.getCode(), "用户ID: " + userId + " 的设备ID: " + deviceId + " 健身数据处理异常");
    }
}