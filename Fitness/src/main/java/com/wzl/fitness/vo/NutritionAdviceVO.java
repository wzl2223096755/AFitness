package com.wzl.fitness.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养建议VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAdviceVO {
    
    /**
     * 建议日期
     */
    private LocalDate date;
    
    /**
     * 营养建议
     */
    private String advice;
    
    /**
     * 具体建议列表
     */
    private List<String> recommendations;
    
    /**
     * 建议的卡路里摄入量
     */
    private Double recommendedCalories;
    
    /**
     * 建议的蛋白质摄入量(g)
     */
    private Double recommendedProtein;
    
    /**
     * 建议的碳水化合物摄入量(g)
     */
    private Double recommendedCarbs;
    
    /**
     * 建议的脂肪摄入量(g)
     */
    private Double recommendedFat;
    
    /**
     * 当前摄入量与建议量的比较
     */
    private String caloriesComparison;
    
    /**
     * 蛋白质摄入量比较
     */
    private String proteinComparison;
    
    /**
     * 碳水化合物摄入量比较
     */
    private String carbsComparison;
    
    /**
     * 脂肪摄入量比较
     */
    private String fatComparison;
}