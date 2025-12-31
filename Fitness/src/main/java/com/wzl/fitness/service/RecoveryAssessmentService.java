package com.wzl.fitness.service;

import com.wzl.fitness.dto.request.RecoveryDataRequest;
import com.wzl.fitness.dto.response.RecoveryAssessment;
import com.wzl.fitness.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复评估服务接口
 * 提供多维度恢复状态评估和训练建议
 */
public interface RecoveryAssessmentService {
    
    /**
     * 记录恢复数据并获取评估结果
     */
    RecoveryAssessment recordAndAssess(User user, RecoveryDataRequest request);
    
    /**
     * 获取指定日期的恢复评估
     */
    RecoveryAssessment getAssessment(User user, LocalDate date);
    
    /**
     * 获取日期范围内的恢复评估历史
     */
    List<RecoveryAssessment> getAssessmentHistory(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 基于当前恢复状态生成训练建议
     */
    List<String> generateTrainingAdvice(User user, RecoveryAssessment assessment);
    
    /**
     * 基于当前恢复状态生成恢复建议
     */
    List<String> generateRecoveryAdvice(User user, RecoveryAssessment assessment);
    
    /**
     * 计算综合恢复评分
     */
    int calculateOverallScore(RecoveryDataRequest request, User user);
    
    /**
     * 获取推荐训练强度
     */
    String getRecommendedIntensity(int recoveryScore);
}
