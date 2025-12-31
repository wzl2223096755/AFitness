package com.wzl.fitness.service.impl;

import com.wzl.fitness.dto.response.*;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.service.NutritionCalculationService;
import com.wzl.fitness.service.NutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * 营养计算服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionCalculationServiceImpl implements NutritionCalculationService {
    
    private final NutritionRecordRepository nutritionRecordRepository;
    private final NutritionService nutritionService;
    
    // 活动水平系数
    private static final double SEDENTARY_MULTIPLIER = 1.2;
    private static final double LIGHT_MULTIPLIER = 1.375;
    private static final double MODERATE_MULTIPLIER = 1.55;
    private static final double ACTIVE_MULTIPLIER = 1.725;
    private static final double VERY_ACTIVE_MULTIPLIER = 1.9;
    
    @Override
    public NutritionRecommendation calculateRecommendedIntake(User user, String trainingGoal, String activityLevel) {
        log.info("计算用户 {} 的推荐营养摄入，目标: {}, 活动水平: {}", user.getId(), trainingGoal, activityLevel);
        
        // 获取用户信息
        Double weight = user.getWeight() != null ? user.getWeight() : 70.0;
        Double height = user.getHeight() != null ? user.getHeight().doubleValue() : 170.0;
        int age = user.getAge() != null ? user.getAge() : 25;
        String gender = user.getGender() != null ? user.getGender() : "male";
        
        // 计算BMR和TDEE
        double bmr = calculateBMR(weight, height, age, gender);
        double tdee = calculateTDEE(bmr, activityLevel);
        
        // 根据训练目标调整卡路里
        double targetCalories = adjustCaloriesForGoal(tdee, trainingGoal);
        
        // 计算宏量营养素
        double proteinPerKg = getProteinPerKg(trainingGoal);
        double recommendedProtein = weight * proteinPerKg;
        
        // 根据目标设置宏量营养素比例
        double proteinPercentage, carbsPercentage, fatPercentage;
        switch (trainingGoal != null ? trainingGoal.toLowerCase() : "maintenance") {
            case "fat_loss":
                proteinPercentage = 30;
                carbsPercentage = 35;
                fatPercentage = 35;
                break;
            case "muscle_gain":
                proteinPercentage = 25;
                carbsPercentage = 50;
                fatPercentage = 25;
                break;
            default: // maintenance
                proteinPercentage = 25;
                carbsPercentage = 45;
                fatPercentage = 30;
        }
        
        // 计算克数
        double proteinCalories = targetCalories * proteinPercentage / 100;
        double carbsCalories = targetCalories * carbsPercentage / 100;
        double fatCalories = targetCalories * fatPercentage / 100;
        
        double recommendedCarbs = carbsCalories / 4; // 1g碳水 = 4卡
        double recommendedFat = fatCalories / 9; // 1g脂肪 = 9卡
        
        // 确保蛋白质摄入在合理范围内 (1.6-2.2 g/kg)
        double minProtein = weight * 1.6;
        double maxProtein = weight * 2.2;
        recommendedProtein = Math.max(recommendedProtein, proteinCalories / 4);
        // 确保不超过上限
        recommendedProtein = Math.min(recommendedProtein, maxProtein);
        
        return NutritionRecommendation.builder()
                .recommendedCalories(Math.round(targetCalories * 10) / 10.0)
                .recommendedProtein(Math.round(recommendedProtein * 10) / 10.0)
                .recommendedCarbs(Math.round(recommendedCarbs * 10) / 10.0)
                .recommendedFat(Math.round(recommendedFat * 10) / 10.0)
                .trainingGoal(trainingGoal)
                .bodyWeight(weight)
                .activityLevel(activityLevel)
                .proteinPercentage(proteinPercentage)
                .carbsPercentage(carbsPercentage)
                .fatPercentage(fatPercentage)
                .build();
    }
    
    @Override
    public NutritionTrend calculateNutritionTrend(User user, LocalDate startDate, LocalDate endDate) {
        log.info("计算用户 {} 从 {} 到 {} 的营养趋势", user.getId(), startDate, endDate);
        
        List<NutritionStatsResponse> dailyStats = nutritionService.getNutritionStatsByDateRange(user, startDate, endDate);
        
        if (dailyStats.isEmpty()) {
            return NutritionTrend.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDays((int) (endDate.toEpochDay() - startDate.toEpochDay() + 1))
                    .recordedDays(0)
                    .dailySummaries(new ArrayList<>())
                    .build();
        }
        
        // 计算平均值
        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
        int recordedDays = 0;
        List<DailyNutritionSummary> summaries = new ArrayList<>();
        
        for (NutritionStatsResponse stats : dailyStats) {
            if (stats.getTotalCalories() != null && stats.getTotalCalories() > 0) {
                totalCalories += stats.getTotalCalories();
                totalProtein += stats.getTotalProtein() != null ? stats.getTotalProtein() : 0;
                totalCarbs += stats.getTotalCarbs() != null ? stats.getTotalCarbs() : 0;
                totalFat += stats.getTotalFat() != null ? stats.getTotalFat() : 0;
                recordedDays++;
            }
            
            summaries.add(DailyNutritionSummary.builder()
                    .date(stats.getDate())
                    .totalCalories(stats.getTotalCalories())
                    .totalProtein(stats.getTotalProtein())
                    .totalCarbs(stats.getTotalCarbs())
                    .totalFat(stats.getTotalFat())
                    .recordCount(stats.getDailyRecords() != null ? stats.getDailyRecords().size() : 0)
                    .build());
        }
        
        double avgCalories = recordedDays > 0 ? totalCalories / recordedDays : 0;
        double avgProtein = recordedDays > 0 ? totalProtein / recordedDays : 0;
        double avgCarbs = recordedDays > 0 ? totalCarbs / recordedDays : 0;
        double avgFat = recordedDays > 0 ? totalFat / recordedDays : 0;
        
        // 计算趋势（简单线性回归斜率）
        double caloriesTrend = calculateTrendSlope(summaries, "calories");
        double proteinTrend = calculateTrendSlope(summaries, "protein");
        double carbsTrend = calculateTrendSlope(summaries, "carbs");
        double fatTrend = calculateTrendSlope(summaries, "fat");
        
        return NutritionTrend.builder()
                .startDate(startDate)
                .endDate(endDate)
                .averageCalories(Math.round(avgCalories * 10) / 10.0)
                .averageProtein(Math.round(avgProtein * 10) / 10.0)
                .averageCarbs(Math.round(avgCarbs * 10) / 10.0)
                .averageFat(Math.round(avgFat * 10) / 10.0)
                .caloriesTrend(Math.round(caloriesTrend * 100) / 100.0)
                .proteinTrend(Math.round(proteinTrend * 100) / 100.0)
                .carbsTrend(Math.round(carbsTrend * 100) / 100.0)
                .fatTrend(Math.round(fatTrend * 100) / 100.0)
                .totalDays((int) (endDate.toEpochDay() - startDate.toEpochDay() + 1))
                .recordedDays(recordedDays)
                .dailySummaries(summaries)
                .build();
    }
    
    @Override
    public NutritionStatus checkNutritionStatus(User user, LocalDate date) {
        log.info("检查用户 {} 在 {} 的营养状态", user.getId(), date);
        
        // 获取当日营养统计
        NutritionStatsResponse stats = nutritionService.getNutritionStatsByDate(user, date);
        
        // 获取推荐摄入
        NutritionRecommendation recommendation = calculateRecommendedIntake(user, "maintenance", "moderate");
        
        double actualCalories = stats.getTotalCalories() != null ? stats.getTotalCalories() : 0;
        double actualProtein = stats.getTotalProtein() != null ? stats.getTotalProtein() : 0;
        
        double targetCalories = recommendation.getRecommendedCalories();
        double targetProtein = recommendation.getRecommendedProtein();
        
        // 计算达成率
        double caloriesRate = targetCalories > 0 ? (actualCalories / targetCalories) * 100 : 0;
        double proteinRate = targetProtein > 0 ? (actualProtein / targetProtein) * 100 : 0;
        
        // 判断状态
        String caloriesStatus = getIntakeStatus(caloriesRate);
        String proteinStatus = getIntakeStatus(proteinRate);
        
        // 生成提醒
        List<String> alerts = new ArrayList<>();
        if (caloriesRate < 80) {
            alerts.add("今日卡路里摄入不足，建议增加健康食物摄入");
        } else if (caloriesRate > 120) {
            alerts.add("今日卡路里摄入过量，请注意控制");
        }
        if (proteinRate < 80) {
            alerts.add("蛋白质摄入不足，建议补充瘦肉、蛋类或蛋白粉");
        }
        
        // 整体状态
        String overallStatus = calculateOverallStatus(caloriesRate, proteinRate);
        
        return NutritionStatus.builder()
                .date(date)
                .overallStatus(overallStatus)
                .caloriesStatus(caloriesStatus)
                .proteinStatus(proteinStatus)
                .actualCalories(actualCalories)
                .targetCalories(targetCalories)
                .caloriesAchievementRate(Math.round(caloriesRate * 10) / 10.0)
                .actualProtein(actualProtein)
                .targetProtein(targetProtein)
                .proteinAchievementRate(Math.round(proteinRate * 10) / 10.0)
                .alerts(alerts)
                .build();
    }
    
    @Override
    public List<String> generatePersonalizedAdvice(User user, LocalDate date) {
        List<String> advice = new ArrayList<>();
        
        NutritionStatus status = checkNutritionStatus(user, date);
        NutritionRecommendation recommendation = calculateRecommendedIntake(user, "maintenance", "moderate");
        
        // 基于状态生成建议
        if ("UNDER".equals(status.getCaloriesStatus())) {
            double deficit = status.getTargetCalories() - status.getActualCalories();
            advice.add(String.format("今日还需摄入约 %.0f 卡路里，建议添加一份健康加餐", deficit));
        }
        
        if ("UNDER".equals(status.getProteinStatus())) {
            double proteinNeeded = status.getTargetProtein() - status.getActualProtein();
            advice.add(String.format("蛋白质还差 %.0f 克，可以补充：100g鸡胸肉(约31g蛋白质)或2个鸡蛋(约12g蛋白质)", proteinNeeded));
        }
        
        if ("OVER".equals(status.getCaloriesStatus())) {
            advice.add("今日卡路里摄入已超标，建议增加运动消耗或明日适当减少摄入");
        }
        
        // 通用建议
        if (advice.isEmpty()) {
            advice.add("今日营养摄入均衡，继续保持！");
        }
        
        // 添加水分提醒
        advice.add(String.format("别忘了补充水分，建议每日饮水 %.1f 升", recommendation.getBodyWeight() * 0.033));
        
        return advice;
    }
    
    @Override
    public double calculateBMR(double weight, double height, int age, String gender) {
        // Mifflin-St Jeor公式
        if ("female".equalsIgnoreCase(gender)) {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        } else {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        }
    }
    
    @Override
    public double calculateTDEE(double bmr, String activityLevel) {
        double multiplier = switch (activityLevel != null ? activityLevel.toLowerCase() : "moderate") {
            case "sedentary" -> SEDENTARY_MULTIPLIER;
            case "light" -> LIGHT_MULTIPLIER;
            case "active" -> ACTIVE_MULTIPLIER;
            case "very_active" -> VERY_ACTIVE_MULTIPLIER;
            default -> MODERATE_MULTIPLIER;
        };
        return bmr * multiplier;
    }
    
    // 私有辅助方法
    
    private double adjustCaloriesForGoal(double tdee, String trainingGoal) {
        return switch (trainingGoal != null ? trainingGoal.toLowerCase() : "maintenance") {
            case "fat_loss" -> tdee * 0.8; // 减脂：减少20%
            case "muscle_gain" -> tdee * 1.15; // 增肌：增加15%
            default -> tdee; // 维持
        };
    }
    
    private double getProteinPerKg(String trainingGoal) {
        return switch (trainingGoal != null ? trainingGoal.toLowerCase() : "maintenance") {
            case "fat_loss" -> 2.0; // 减脂时保持高蛋白
            case "muscle_gain" -> 2.2; // 增肌需要更多蛋白质
            default -> 1.6; // 维持
        };
    }
    
    private String getIntakeStatus(double rate) {
        if (rate < 80) return "UNDER";
        if (rate > 120) return "OVER";
        return "ON_TARGET";
    }
    
    private String calculateOverallStatus(double caloriesRate, double proteinRate) {
        double avgRate = (caloriesRate + proteinRate) / 2;
        if (avgRate >= 90 && avgRate <= 110) return "EXCELLENT";
        if (avgRate >= 80 && avgRate <= 120) return "GOOD";
        if (avgRate >= 60 && avgRate <= 140) return "FAIR";
        return "POOR";
    }
    
    private double calculateTrendSlope(List<DailyNutritionSummary> summaries, String nutrient) {
        if (summaries.size() < 2) return 0;
        
        int n = summaries.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double y = switch (nutrient) {
                case "calories" -> summaries.get(i).getTotalCalories() != null ? summaries.get(i).getTotalCalories() : 0;
                case "protein" -> summaries.get(i).getTotalProtein() != null ? summaries.get(i).getTotalProtein() : 0;
                case "carbs" -> summaries.get(i).getTotalCarbs() != null ? summaries.get(i).getTotalCarbs() : 0;
                case "fat" -> summaries.get(i).getTotalFat() != null ? summaries.get(i).getTotalFat() : 0;
                default -> 0;
            };
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumX2 += i * i;
        }
        
        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return 0;
        
        return (n * sumXY - sumX * sumY) / denominator;
    }
}
