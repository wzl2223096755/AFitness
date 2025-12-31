package com.wzl.fitness.service;

import com.wzl.fitness.entity.StrengthTrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 力量训练数据服务接口
 */
public interface StrengthTrainingService {
    
    /**
     * 创建力量训练数据
     */
    StrengthTrainingData createStrengthTrainingData(Long userId, StrengthTrainingData data);
    
    /**
     * 更新力量训练数据
     */
    StrengthTrainingData updateStrengthTrainingData(Long id, StrengthTrainingData data);
    
    /**
     * 删除力量训练数据
     */
    void deleteStrengthTrainingData(Long id);
    
    /**
     * 根据ID获取力量训练数据
     */
    StrengthTrainingData getStrengthTrainingDataById(Long id);
    
    /**
     * 获取用户的力量训练数据（分页）
     */
    Page<StrengthTrainingData> getUserStrengthTrainingData(Long userId, Pageable pageable);
    
    /**
     * 根据时间范围获取用户的力量训练数据
     */
    List<StrengthTrainingData> getStrengthTrainingDataByDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据动作名称获取用户的力量训练数据
     */
    List<StrengthTrainingData> getStrengthTrainingDataByExercise(Long userId, String exerciseName);
    
    /**
     * 获取用户各动作的最大重量统计
     */
    Map<String, Object> getMaxWeightStats(Long userId);
    
    /**
     * 计算训练总量统计
     */
    Map<String, Object> getTrainingVolumeStats(Long userId, LocalDateTime start, LocalDateTime end);
}