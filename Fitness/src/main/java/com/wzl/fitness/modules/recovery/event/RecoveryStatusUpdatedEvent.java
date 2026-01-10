package com.wzl.fitness.modules.recovery.event;

import com.wzl.fitness.shared.event.DomainEvent;
import lombok.Getter;

/**
 * 恢复状态更新事件
 * 
 * 当用户的恢复状态发生变化时发布此事件
 * 其他模块可以监听此事件以响应恢复状态变化
 */
@Getter
public class RecoveryStatusUpdatedEvent extends DomainEvent {
    
    /**
     * 恢复等级
     */
    private final String recoveryLevel;
    
    /**
     * 恢复评分
     */
    private final Integer recoveryScore;
    
    /**
     * 推荐训练强度
     */
    private final String recommendedIntensity;
    
    /**
     * 预计恢复天数
     */
    private final Integer estimatedRecoveryDays;
    
    /**
     * 创建恢复状态更新事件
     * 
     * @param userId 用户ID
     * @param recoveryLevel 恢复等级
     * @param recoveryScore 恢复评分
     */
    public RecoveryStatusUpdatedEvent(Long userId, String recoveryLevel, Integer recoveryScore) {
        super(userId);
        this.recoveryLevel = recoveryLevel;
        this.recoveryScore = recoveryScore;
        this.recommendedIntensity = null;
        this.estimatedRecoveryDays = null;
    }
    
    /**
     * 创建恢复状态更新事件（完整版本）
     * 
     * @param userId 用户ID
     * @param recoveryLevel 恢复等级
     * @param recoveryScore 恢复评分
     * @param recommendedIntensity 推荐训练强度
     * @param estimatedRecoveryDays 预计恢复天数
     */
    public RecoveryStatusUpdatedEvent(Long userId, String recoveryLevel, Integer recoveryScore,
                                       String recommendedIntensity, Integer estimatedRecoveryDays) {
        super(userId);
        this.recoveryLevel = recoveryLevel;
        this.recoveryScore = recoveryScore;
        this.recommendedIntensity = recommendedIntensity;
        this.estimatedRecoveryDays = estimatedRecoveryDays;
    }
    
    @Override
    public String toString() {
        return String.format("RecoveryStatusUpdatedEvent[eventId=%s, userId=%s, recoveryLevel=%s, recoveryScore=%d]",
                getEventId(), getUserId(), recoveryLevel, recoveryScore);
    }
}
