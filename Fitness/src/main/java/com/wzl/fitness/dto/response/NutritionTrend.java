package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养趋势DTO
 * 用于展示一段时间内的营养摄入趋势
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrend {
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 平均每日卡路里摄入
     */
    private Double averageCalories;
    
    /**
     * 平均每日蛋白质摄入(g)
     */
    private Double averageProtein;
    
    /**
     * 平均每日碳水化合物摄入(g)
     */
    private Double averageCarbs;
    
    /**
     * 平均每日脂肪摄入(g)
     */
    private Double averageFat;
    
    /**
     * 卡路里趋势（正数表示上升，负数表示下降）
     */
    private Double caloriesTrend;
    
    /**
     * 蛋白质趋势
     */
    private Double proteinTrend;
    
    /**
     * 碳水化合物趋势
     */
    private Double carbsTrend;
    
    /**
     * 脂肪趋势
     */
    private Double fatTrend;
    
    /**
     * 统计天数
     */
    private Integer totalDays;
    
    /**
     * 有记录的天数
     */
    private Integer recordedDays;
    
    /**
     * 每日营养摘要列表
     */
    private List<DailyNutritionSummary> dailySummaries;
}
