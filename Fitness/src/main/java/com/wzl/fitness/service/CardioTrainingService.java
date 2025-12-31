package com.wzl.fitness.service;

import com.wzl.fitness.entity.CardioTrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 有氧训练数据服务接口
 */
public interface CardioTrainingService {
    
    /**
     * 创建有氧训练数据
     */
    CardioTrainingData createCardioTrainingData(Long userId, CardioTrainingData data);
    
    /**
     * 更新有氧训练数据
     */
    CardioTrainingData updateCardioTrainingData(Long id, CardioTrainingData data);
    
    /**
     * 删除有氧训练数据
     */
    void deleteCardioTrainingData(Long id);
    
    /**
     * 根据ID获取有氧训练数据
     */
    CardioTrainingData getCardioTrainingDataById(Long id);
    
    /**
     * 获取用户的有氧训练数据（分页）
     */
    Page<CardioTrainingData> getUserCardioTrainingData(Long userId, Pageable pageable);
    
    /**
     * 根据时间范围获取用户的有氧训练数据
     */
    List<CardioTrainingData> getCardioTrainingDataByDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 根据运动类型获取用户的有氧训练数据
     */
    List<CardioTrainingData> getCardioTrainingDataByExerciseType(Long userId, String exerciseType);
    
    /**
     * 获取用户有氧训练统计
     */
    Map<String, Object> getCardioStats(Long userId, LocalDateTime start, LocalDateTime end);
}
