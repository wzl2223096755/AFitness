package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.NutritionRecommendation;
import com.wzl.fitness.dto.response.NutritionStatus;
import com.wzl.fitness.dto.response.NutritionTrend;
import com.wzl.fitness.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养计算服务接口
 * 负责营养相关的计算逻辑
 */
public interface NutritionCalculationService {
    
    /**
     * 根据用户体重和训练目标计算推荐营养摄入
     * 
     * @param user 用户
     * @param trainingGoal 训练目标: fat_loss, muscle_gain, maintenance
     * @param activityLevel 活动水平: sedentary, light, moderate, active, very_active
     * @return 营养推荐
     */
    NutritionRecommendation calculateRecommendedIntake(User user, String trainingGoal, String activityLevel);
    
    /**
     * 计算营养摄入趋势
     * 
     * @param user 用户
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 营养趋势
     */
    NutritionTrend calculateNutritionTrend(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 检查营养摄入是否达标
     * 
     * @param user 用户
     * @param date 日期
     * @return 营养状态
     */
    NutritionStatus checkNutritionStatus(User user, LocalDate date);
    
    /**
     * 生成个性化营养建议
     * 
     * @param user 用户
     * @param date 日期
     * @return 营养建议列表
     */
    List<String> generatePersonalizedAdvice(User user, LocalDate date);
    
    /**
     * 计算基础代谢率(BMR)
     * 使用Mifflin-St Jeor公式
     * 
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @param age 年龄
     * @param gender 性别: male, female
     * @return BMR
     */
    double calculateBMR(double weight, double height, int age, String gender);
    
    /**
     * 计算每日总能量消耗(TDEE)
     * 
     * @param bmr 基础代谢率
     * @param activityLevel 活动水平
     * @return TDEE
     */
    double calculateTDEE(double bmr, String activityLevel);
}
