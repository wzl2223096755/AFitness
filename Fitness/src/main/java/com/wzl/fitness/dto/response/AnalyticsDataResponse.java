package com.wzl.fitness.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AnalyticsDataResponse {
    private Integer totalWorkouts;
    private Long totalVolume;
    private Integer totalDuration;
    private Double avgIntensity;
    private Integer workoutIncrease;
    private Integer volumeIncrease;
    private Integer durationChange;
    private Integer intensityIncrease;
    private List<Map<String, Object>> performanceDetails;
}
