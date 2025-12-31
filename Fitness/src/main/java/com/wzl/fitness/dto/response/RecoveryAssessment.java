package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复评估结果DTO
 * 基于多维度数据评估用户恢复状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryAssessment {
    
    /**
     * 评估日期
     */
    private LocalDate assessmentDate;
    
    /**
     * 综合恢复评分 (0-100)
     */
    private Integer overallScore;
    
    /**
     * 恢复状态: EXCELLENT, GOOD, FAIR, POOR, CRITICAL
     */
    private String recoveryStatus;
    
    /**
     * 睡眠质量评分 (0-100)
     */
    private Integer sleepScore;
    
    /**
     * 睡眠时长(小时)
     */
    private Double sleepHours;
    
    /**
     * 肌肉酸痛评分 (1-10, 10为最痛)
     */
    private Integer muscleSorenessScore;
    
    /**
     * 疲劳程度评分 (1-10, 10为最疲劳)
     */
    private Integer fatigueScore;
    
    /**
     * 压力水平评分 (1-10, 10为最高压力)
     */
    private Integer stressScore;
    
    /**
     * 心率变异性 (HRV)
     */
    private Integer hrv;
    
    /**
     * 静息心率
     */
    private Integer restingHeartRate;
    
    /**
     * 最近训练负荷影响评分
     */
    private Integer trainingLoadImpact;
    
    /**
     * 推荐训练强度: REST, LIGHT, MODERATE, HIGH, INTENSE
     */
    private String recommendedIntensity;
    
    /**
     * 训练建议列表
     */
    private List<String> trainingAdvice;
    
    /**
     * 恢复建议列表
     */
    private List<String> recoveryAdvice;
    
    /**
     * 预计完全恢复所需天数
     */
    private Integer estimatedRecoveryDays;
}
