package com.wzl.fitness.dto.response;

import lombok.Data;

@Data
public class UserStatsOverviewResponse {
    private Integer weeklyTrainingCount;
    private Integer weeklyChange;
    private Double totalTrainingHours;
    private Double trainingHoursChange;
    private Integer totalCalories;
    private Integer caloriesChange;
    private Integer goalCompletionRate;
    private Integer goalChange;
}
