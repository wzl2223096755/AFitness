package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 每日营养摘要DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionSummary {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 总卡路里
     */
    private Double totalCalories;
    
    /**
     * 总蛋白质(g)
     */
    private Double totalProtein;
    
    /**
     * 总碳水化合物(g)
     */
    private Double totalCarbs;
    
    /**
     * 总脂肪(g)
     */
    private Double totalFat;
    
    /**
     * 记录数量
     */
    private Integer recordCount;
    
    /**
     * 是否达到卡路里目标
     */
    private Boolean caloriesGoalMet;
    
    /**
     * 是否达到蛋白质目标
     */
    private Boolean proteinGoalMet;
}
