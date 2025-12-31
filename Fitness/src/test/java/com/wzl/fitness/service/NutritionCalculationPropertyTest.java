package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.NutritionRecommendation;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.service.impl.NutritionCalculationServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 营养计算属性测试
 * 
 * **Property 1: 营养推荐计算一致性**
 * *For any* 用户体重和训练目标，计算的推荐蛋白质摄入应在体重的1.6-2.2倍（克/公斤）范围内
 * 
 * **Property 3: 营养素占比计算正确性**
 * *For any* 营养统计，蛋白质、碳水、脂肪的卡路里占比之和应接近100%（允许±5%误差）
 * 
 * **Validates: Requirements 2.2, 2.3**
 * 
 * Feature: short-term-improvements, Property 1 & 3: 营养推荐计算一致性 & 营养素占比计算正确性
 */
public class NutritionCalculationPropertyTest {

    /**
     * Property 1: 营养推荐计算一致性
     * 
     * 对于任意有效的体重(40-150kg)和训练目标，
     * 计算的推荐蛋白质摄入应在体重的1.6-2.2倍（克/公斤）范围内
     * 
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 50)
    @Label("Property 1: 营养推荐计算一致性 - 蛋白质摄入应在体重的1.6-2.2倍范围内")
    void recommendedProteinShouldBeInRange(
            @ForAll @DoubleRange(min = 40.0, max = 150.0) Double bodyWeight,
            @ForAll("trainingGoals") String trainingGoal,
            @ForAll("activityLevels") String activityLevel) {
        
        // 创建测试用户
        User user = createUserWithWeight(bodyWeight);
        
        // 创建服务实例（直接测试计算逻辑，不依赖Spring容器）
        NutritionCalculationServiceImpl service = new NutritionCalculationServiceImpl(null, null);
        
        // 计算推荐摄入
        NutritionRecommendation recommendation = service.calculateRecommendedIntake(user, trainingGoal, activityLevel);
        
        // 验证蛋白质推荐在合理范围内
        double minProtein = bodyWeight * 1.6;
        double maxProtein = bodyWeight * 2.2;
        double tolerance = 0.2; // 允许0.2g的误差（由于四舍五入）
        
        assertNotNull(recommendation.getRecommendedProtein(), 
                "推荐蛋白质不应为null");
        
        assertTrue(recommendation.getRecommendedProtein() >= minProtein - tolerance,
                String.format("蛋白质推荐过低: weight=%.1fkg, goal=%s, protein=%.1fg, min=%.1fg", 
                        bodyWeight, trainingGoal, recommendation.getRecommendedProtein(), minProtein));
        
        assertTrue(recommendation.getRecommendedProtein() <= maxProtein + tolerance,
                String.format("蛋白质推荐过高: weight=%.1fkg, goal=%s, protein=%.1fg, max=%.1fg", 
                        bodyWeight, trainingGoal, recommendation.getRecommendedProtein(), maxProtein));
    }

    /**
     * Property 3: 营养素占比计算正确性
     * 
     * 对于任意有效的体重和训练目标，
     * 蛋白质、碳水、脂肪的卡路里占比之和应接近100%（允许±5%误差）
     * 
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 50)
    @Label("Property 3: 营养素占比计算正确性 - 三大营养素占比之和应接近100%")
    void macronutrientPercentagesShouldSumToHundred(
            @ForAll @DoubleRange(min = 40.0, max = 150.0) Double bodyWeight,
            @ForAll("trainingGoals") String trainingGoal,
            @ForAll("activityLevels") String activityLevel) {
        
        // 创建测试用户
        User user = createUserWithWeight(bodyWeight);
        
        // 创建服务实例
        NutritionCalculationServiceImpl service = new NutritionCalculationServiceImpl(null, null);
        
        // 计算推荐摄入
        NutritionRecommendation recommendation = service.calculateRecommendedIntake(user, trainingGoal, activityLevel);
        
        // 验证占比之和
        Double proteinPct = recommendation.getProteinPercentage();
        Double carbsPct = recommendation.getCarbsPercentage();
        Double fatPct = recommendation.getFatPercentage();
        
        assertNotNull(proteinPct, "蛋白质占比不应为null");
        assertNotNull(carbsPct, "碳水占比不应为null");
        assertNotNull(fatPct, "脂肪占比不应为null");
        
        double totalPercentage = proteinPct + carbsPct + fatPct;
        double tolerance = 5.0; // 允许±5%误差
        
        assertTrue(Math.abs(totalPercentage - 100.0) <= tolerance,
                String.format("营养素占比之和应接近100%%: protein=%.1f%%, carbs=%.1f%%, fat=%.1f%%, total=%.1f%%", 
                        proteinPct, carbsPct, fatPct, totalPercentage));
    }

    /**
     * 辅助属性：推荐卡路里应为正数
     */
    @Property(tries = 50)
    @Label("推荐卡路里应为正数")
    void recommendedCaloriesShouldBePositive(
            @ForAll @DoubleRange(min = 40.0, max = 150.0) Double bodyWeight,
            @ForAll("trainingGoals") String trainingGoal,
            @ForAll("activityLevels") String activityLevel) {
        
        User user = createUserWithWeight(bodyWeight);
        NutritionCalculationServiceImpl service = new NutritionCalculationServiceImpl(null, null);
        
        NutritionRecommendation recommendation = service.calculateRecommendedIntake(user, trainingGoal, activityLevel);
        
        assertNotNull(recommendation.getRecommendedCalories(), "推荐卡路里不应为null");
        assertTrue(recommendation.getRecommendedCalories() > 0,
                String.format("推荐卡路里应为正数: weight=%.1fkg, goal=%s, calories=%.1f", 
                        bodyWeight, trainingGoal, recommendation.getRecommendedCalories()));
    }

    // 数据提供者

    @Provide
    Arbitrary<String> trainingGoals() {
        return Arbitraries.of("fat_loss", "muscle_gain", "maintenance");
    }

    @Provide
    Arbitrary<String> activityLevels() {
        return Arbitraries.of("sedentary", "light", "moderate", "active", "very_active");
    }

    // 辅助方法

    private User createUserWithWeight(Double weight) {
        User user = new User();
        user.setId(1L);
        user.setWeight(weight);
        user.setHeight(170); // 默认身高170cm
        user.setAge(25); // 默认年龄25岁
        user.setGender("male"); // 默认男性
        return user;
    }
}
