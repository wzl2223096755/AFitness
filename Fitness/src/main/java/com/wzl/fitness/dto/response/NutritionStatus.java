package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养状态DTO
 * 用于检查当日营养摄入是否达标
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionStatus {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 整体状态: EXCELLENT, GOOD, FAIR, POOR
     */
    private String overallStatus;
    
    /**
     * 卡路里状态: UNDER, ON_TARGET, OVER
     */
    private String caloriesStatus;
    
    /**
     * 蛋白质状态
     */
    private String proteinStatus;
    
    /**
     * 碳水化合物状态
     */
    private String carbsStatus;
    
    /**
     * 脂肪状态
     */
    private String fatStatus;
    
    /**
     * 实际卡路里摄入
     */
    private Double actualCalories;
    
    /**
     * 目标卡路里摄入
     */
    private Double targetCalories;
    
    /**
     * 卡路里达成率(%)
     */
    private Double caloriesAchievementRate;
    
    /**
     * 实际蛋白质摄入
     */
    private Double actualProtein;
    
    /**
     * 目标蛋白质摄入
     */
    private Double targetProtein;
    
    /**
     * 蛋白质达成率(%)
     */
    private Double proteinAchievementRate;
    
    /**
     * 提醒消息列表
     */
    private List<String> alerts;
}
