package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 营养目标请求DTO
 */
public class NutritionGoalRequest {
    
    @Pattern(regexp = "^(fat_loss|muscle_gain|maintenance)$", message = "训练目标必须是 fat_loss, muscle_gain 或 maintenance")
    private String trainingGoal;
    
    @Pattern(regexp = "^(sedentary|light|moderate|active|very_active)$", message = "活动水平必须是 sedentary, light, moderate, active 或 very_active")
    private String activityLevel;
    
    @Min(value = 1000, message = "目标卡路里不能低于1000")
    @Max(value = 10000, message = "目标卡路里不能超过10000")
    private Double targetCalories;
    
    @Min(value = 0, message = "目标蛋白质不能为负数")
    @Max(value = 500, message = "目标蛋白质不能超过500g")
    private Double targetProtein;
    
    @Min(value = 0, message = "目标碳水不能为负数")
    @Max(value = 1000, message = "目标碳水不能超过1000g")
    private Double targetCarbs;
    
    @Min(value = 0, message = "目标脂肪不能为负数")
    @Max(value = 500, message = "目标脂肪不能超过500g")
    private Double targetFat;
    
    private Boolean useCustomTargets;
    
    public NutritionGoalRequest() {}
    
    public NutritionGoalRequest(String trainingGoal, String activityLevel, Double targetCalories, Double targetProtein,
                                Double targetCarbs, Double targetFat, Boolean useCustomTargets) {
        this.trainingGoal = trainingGoal;
        this.activityLevel = activityLevel;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
        this.useCustomTargets = useCustomTargets;
    }
    
    // Getters and Setters
    public String getTrainingGoal() { return trainingGoal; }
    public void setTrainingGoal(String trainingGoal) { this.trainingGoal = trainingGoal; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public Double getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Double targetCalories) { this.targetCalories = targetCalories; }
    public Double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(Double targetProtein) { this.targetProtein = targetProtein; }
    public Double getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(Double targetCarbs) { this.targetCarbs = targetCarbs; }
    public Double getTargetFat() { return targetFat; }
    public void setTargetFat(Double targetFat) { this.targetFat = targetFat; }
    public Boolean getUseCustomTargets() { return useCustomTargets; }
    public void setUseCustomTargets(Boolean useCustomTargets) { this.useCustomTargets = useCustomTargets; }
    
    // Builder
    public static NutritionGoalRequestBuilder builder() { return new NutritionGoalRequestBuilder(); }
    
    public static class NutritionGoalRequestBuilder {
        private String trainingGoal;
        private String activityLevel;
        private Double targetCalories;
        private Double targetProtein;
        private Double targetCarbs;
        private Double targetFat;
        private Boolean useCustomTargets;
        
        public NutritionGoalRequestBuilder trainingGoal(String v) { this.trainingGoal = v; return this; }
        public NutritionGoalRequestBuilder activityLevel(String v) { this.activityLevel = v; return this; }
        public NutritionGoalRequestBuilder targetCalories(Double v) { this.targetCalories = v; return this; }
        public NutritionGoalRequestBuilder targetProtein(Double v) { this.targetProtein = v; return this; }
        public NutritionGoalRequestBuilder targetCarbs(Double v) { this.targetCarbs = v; return this; }
        public NutritionGoalRequestBuilder targetFat(Double v) { this.targetFat = v; return this; }
        public NutritionGoalRequestBuilder useCustomTargets(Boolean v) { this.useCustomTargets = v; return this; }
        
        public NutritionGoalRequest build() {
            return new NutritionGoalRequest(trainingGoal, activityLevel, targetCalories, targetProtein, targetCarbs, targetFat, useCustomTargets);
        }
    }
}
