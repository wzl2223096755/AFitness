package com.wzl.fitness.modules.nutrition.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 营养目标数据传输对象
 * 用于模块间传输用户营养目标信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionGoalDTO {
    
    private Long id;
    private Long userId;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
    private Double targetFiber;
    private Double targetSugar;
    private Double targetSodium;
    private String goalType;
    private String activityLevel;
    private Boolean useCustomTargets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
