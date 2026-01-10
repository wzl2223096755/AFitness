package com.wzl.fitness.modules.nutrition.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养记录数据传输对象
 * 用于模块间传输营养记录信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionRecordDTO {
    
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private String mealType;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    private Double amount;
    private String notes;
    private LocalDateTime createdAt;
}
