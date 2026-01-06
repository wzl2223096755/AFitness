package com.wzl.fitness.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重试事件模型
 * 用于记录数据库操作重试的详细信息
 */
@Data
@Builder
public class RetryEvent {
    
    /** 操作名称 */
    private String operationName;
    
    /** 重试次数 */
    private int attemptNumber;
    
    /** 异常类型 */
    private String exceptionType;
    
    /** 异常消息 */
    private String exceptionMessage;
    
    /** 时间戳 */
    private LocalDateTime timestamp;
    
    /** 是否成功 */
    private boolean successful;
    
    /** 方法签名 */
    private String methodSignature;
    
    /** 耗时（毫秒） */
    private long durationMs;
}
