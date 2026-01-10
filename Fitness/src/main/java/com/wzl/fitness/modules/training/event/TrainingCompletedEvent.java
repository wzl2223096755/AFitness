package com.wzl.fitness.modules.training.event;

import com.wzl.fitness.shared.event.DomainEvent;
import lombok.Getter;

/**
 * 训练完成事件
 * 当用户完成训练记录时发布此事件
 * 
 * @see Requirements 3.2 - 用户完成训练记录时，训练模块发布TrainingCompletedEvent事件
 */
@Getter
public class TrainingCompletedEvent extends DomainEvent {
    
    /**
     * 训练记录ID
     */
    private final Long trainingRecordId;
    
    /**
     * 总训练量
     */
    private final Double totalVolume;
    
    /**
     * 训练类型/动作名称
     */
    private final String exerciseType;
    
    /**
     * 训练压力值
     */
    private final Double trainingStress;
    
    /**
     * 训练时长（分钟）
     */
    private final Integer duration;
    
    /**
     * 创建训练完成事件
     * 
     * @param userId 用户ID
     * @param trainingRecordId 训练记录ID
     * @param totalVolume 总训练量
     * @param exerciseType 训练类型
     */
    public TrainingCompletedEvent(Long userId, Long trainingRecordId, 
                                   Double totalVolume, String exerciseType) {
        super(userId);
        this.trainingRecordId = trainingRecordId;
        this.totalVolume = totalVolume;
        this.exerciseType = exerciseType;
        this.trainingStress = null;
        this.duration = null;
    }
    
    /**
     * 创建训练完成事件（完整版本）
     * 
     * @param userId 用户ID
     * @param trainingRecordId 训练记录ID
     * @param totalVolume 总训练量
     * @param exerciseType 训练类型
     * @param trainingStress 训练压力值
     * @param duration 训练时长
     */
    public TrainingCompletedEvent(Long userId, Long trainingRecordId, 
                                   Double totalVolume, String exerciseType,
                                   Double trainingStress, Integer duration) {
        super(userId);
        this.trainingRecordId = trainingRecordId;
        this.totalVolume = totalVolume;
        this.exerciseType = exerciseType;
        this.trainingStress = trainingStress;
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return String.format("TrainingCompletedEvent[eventId=%s, userId=%s, trainingRecordId=%s, totalVolume=%.2f, exerciseType=%s]",
                getEventId(), getUserId(), trainingRecordId, totalVolume, exerciseType);
    }
}
