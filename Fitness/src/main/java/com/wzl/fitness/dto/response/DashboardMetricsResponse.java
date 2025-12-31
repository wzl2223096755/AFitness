package com.wzl.fitness.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardMetricsResponse {
    private Integer weeklyTrainingCount;
    private Long totalVolume;
    private Integer recoveryScore;
    private Integer goalCompletionRate;
    private Double sleepQuality;
    private Double muscleFatigue;
    private Double mentalState;
    private Integer weeklyChange;
    private List<Map<String, Object>> goals;
}
