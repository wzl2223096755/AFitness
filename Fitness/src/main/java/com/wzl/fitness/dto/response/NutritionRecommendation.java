package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营养推荐DTO
 * 根据用户体重和训练目标计算的推荐营养摄入
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecommendation {
    
    /**
     * 推荐每日卡路里摄入
     */
    private Double recommendedCalories;
    
    /**
     * 推荐每日蛋白质摄入(g)
     */
    private Double recommendedProtein;
    
    /**
     * 推荐每日碳水化合物摄入(g)
     */
    private Double recommendedCarbs;
    
    /**
     * 推荐每日脂肪摄入(g)
     */
    private Double recommendedFat;
    
    /**
     * 训练目标: fat_loss, muscle_gain, maintenance
     */
    private String trainingGoal;
    
    /**
     * 用户体重(kg)
     */
    private Double bodyWeight;
    
    /**
     * 活动水平: sedentary, light, moderate, active, very_active
     */
    private String activityLevel;
    
    /**
     * 蛋白质占比(%)
     */
    private Double proteinPercentage;
    
    /**
     * 碳水化合物占比(%)
     */
    private Double carbsPercentage;
    
    /**
     * 脂肪占比(%)
     */
    private Double fatPercentage;
}
