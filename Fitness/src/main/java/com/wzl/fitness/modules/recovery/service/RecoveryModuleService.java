package com.wzl.fitness.modules.recovery.service;

import com.wzl.fitness.modules.recovery.dto.RecoveryStatusDTO;
import com.wzl.fitness.modules.recovery.dto.TrainingSuggestionDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复评估模块服务接口
 * 
 * 提供恢复状态评估和训练建议生成的业务逻辑
 * 
 * @see Requirements 1.2 - 领域模块包含独立的service子包
 */
public interface RecoveryModuleService {
    
    /**
     * 获取用户当前恢复状态
     * 
     * @param userId 用户ID
     * @return 恢复状态DTO
     */
    RecoveryStatusDTO getCurrentStatus(Long userId);
    
    /**
     * 获取用户指定日期的恢复状态
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 恢复状态DTO
     */
    RecoveryStatusDTO getStatusByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户训练建议
     * 
     * @param userId 用户ID
     * @return 训练建议列表
     */
    List<TrainingSuggestionDTO> getSuggestions(Long userId);
    
    /**
     * 根据训练完成事件更新恢复状态
     * 
     * @param userId 用户ID
     * @param trainingRecordId 训练记录ID
     * @param totalVolume 总训练量
     * @param exerciseType 训练类型
     */
    void updateRecoveryAfterTraining(Long userId, Long trainingRecordId, Double totalVolume, String exerciseType);
    
    /**
     * 计算恢复评分
     * 
     * @param sleepHours 睡眠时长
     * @param sleepQuality 睡眠质量
     * @param muscleSoreness 肌肉酸痛度
     * @param stressLevel 压力水平
     * @param hrv 心率变异性
     * @param restingHeartRate 静息心率
     * @return 恢复评分
     */
    int calculateRecoveryScore(Double sleepHours, Integer sleepQuality, 
                               Integer muscleSoreness, Integer stressLevel,
                               Integer hrv, Integer restingHeartRate);
    
    /**
     * 获取推荐训练强度
     * 
     * @param recoveryScore 恢复评分
     * @return 推荐训练强度
     */
    String getRecommendedIntensity(int recoveryScore);
}
