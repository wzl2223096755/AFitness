package com.wzl.fitness.modules.nutrition.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日营养摄入数据传输对象
 * 用于模块间传输每日营养摄入汇总信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyNutritionDTO {
    
    private Long userId;
    private LocalDate date;
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;
    private Double totalFiber;
    private Double totalSugar;
    private Double totalSodium;
    private Integer mealCount;
    private List<NutritionRecordDTO> records;
}
