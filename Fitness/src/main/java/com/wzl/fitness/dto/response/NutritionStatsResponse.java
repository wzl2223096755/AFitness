package com.wzl.fitness.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class NutritionStatsResponse {
    private LocalDate date;
    private List<NutritionRecordDTO> dailyRecords;
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;
    private Double totalFiber;
    private Double totalSugar;
    private Double totalSodium;
    private Double caloriesPercentage;
    private Double proteinPercentage;
    private Double carbsPercentage;
    private Double fatPercentage;
    private List<String> nutritionAdvice;
}
