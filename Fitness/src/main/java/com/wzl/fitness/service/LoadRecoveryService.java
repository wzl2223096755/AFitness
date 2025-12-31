package com.wzl.fitness.service;

import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LoadRecoveryService {
    
    /**
     * 计算训练负荷
     * @param fitnessData 健身数据
     * @return 计算后的负荷数据
     */
    FitnessData calculateTrainingLoad(FitnessData fitnessData);
    
    /**
     * 评估恢复状态
     * @param userId 用户ID
     * @param sleepHours 睡眠时长
     * @param stressLevel 压力水平
     * @return 恢复状态评估结果
     */
    Map<String, Object> assessRecoveryStatus(Long userId, Integer sleepHours, Integer stressLevel);
    
    /**
     * 生成训练建议
     * @param userId 用户ID
     * @return 训练建议
     */
    Map<String, Object> generateTrainingSuggestions(Long userId);
    
    /**
     * 计算用户的One Rep Max（默认Epley公式）
     * @param weight 重量
     * @param reps 次数
     * @return 估算的1RM
     */
    Double calculateOneRepMax(Double weight, Integer reps);
    
    /**
     * 计算用户的One Rep Max（支持多种模型选择）
     * @param weight 重量
     * @param reps 次数
     * @param model 计算公式模型（Epley、Brzycki、Lombardi、OConner、Mayhew）
     * @return 估算的1RM
     */
    Double calculateOneRepMax(Double weight, Integer reps, String model);
    
    /**
     * 计算训练容量
     * @param weight 重量
     * @param sets 组数
     * @param reps 次数
     * @return 训练容量
     */
    Double calculateVolume(Double weight, Integer sets, Integer reps);
    
    /**
     * 计算消耗热量
     * @param duration 训练时长（分钟）
     * @param intensity 强度系数 (1-10)
     * @return 消耗热量
     */
    Double calculateCalories(Double duration, Double intensity);

    /**
     * 计算训练压力
     */
    Double calculateTrainingStress(Double volume, Double rpe);

    /**
     * 计算疲劳指数
     */
    Double calculateFatigueIndex(Double trainingStress, RecoveryMetric recovery);

    /**
     * 计算恢复得分
     */
    Double calculateRecoveryScore(RecoveryMetric recovery);

    /**
     * 计算总训练量和训练压力
     */
    TrainingRecord calculateTrainingSummary(TrainingRecord trainingRecord);

    /**
     * 获取用户的训练负荷趋势 (基于 LocalDate)
     */
    Map<String, Double> getLoadTrendByDate(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取所有支持的1RM计算公式模型
     */
    List<String> getSupportedOneRepMaxModels();
    
    /**
     * 获取用户的训练负荷趋势
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 训练负荷趋势数据
     */
    Map<String, Double> getLoadTrend(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
