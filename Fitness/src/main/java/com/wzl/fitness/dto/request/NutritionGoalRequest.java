package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营养目标请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionGoalRequest {
    
    /**
     * 训练目标: fat_loss, muscle_gain, maintenance
     */
    @Pattern(regexp = "^(fat_loss|muscle_gain|maintenance)$", message = "训练目标必须是 fat_loss, muscle_gain 或 maintenance")
    private String trainingGoal;
    
    /**
     * 活动水平: sedentary, light, moderate, active, very_active
     */
    @Pattern(regexp = "^(sedentary|light|moderate|active|very_active)$", message = "活动水平必须是 sedentary, light, moderate, active 或 very_active")
    private String activityLevel;
    
    /**
     * 自定义目标卡路里
     */
    @Min(value = 1000, message = "目标卡路里不能低于1000")
    @Max(value = 10000, message = "目标卡路里不能超过10000")
    private Double targetCalories;
    
    /**
     * 自定义目标蛋白质(g)
     */
    @Min(value = 0, message = "目标蛋白质不能为负数")
    @Max(value = 500, message = "目标蛋白质不能超过500g")
    private Double targetProtein;
    
    /**
     * 自定义目标碳水化合物(g)
     */
    @Min(value = 0, message = "目标碳水不能为负数")
    @Max(value = 1000, message = "目标碳水不能超过1000g")
    private Double targetCarbs;
    
    /**
     * 自定义目标脂肪(g)
     */
    @Min(value = 0, message = "目标脂肪不能为负数")
    @Max(value = 500, message = "目标脂肪不能超过500g")
    private Double targetFat;
    
    /**
     * 是否使用自定义目标
     */
    private Boolean useCustomTargets;
}
