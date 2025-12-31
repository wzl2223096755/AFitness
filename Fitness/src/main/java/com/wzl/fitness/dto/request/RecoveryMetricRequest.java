package com.wzl.fitness.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecoveryMetricRequest {
    private Integer sleepQuality;
    private Integer muscleSoreness;
    private Integer subjectiveEnergy;
    private Integer restingHeartRate;
    private Integer hrv;
    private Double weight;
    private LocalDate recordDate;
    private String notes;
}
