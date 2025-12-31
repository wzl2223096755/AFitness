package com.wzl.fitness.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecoveryMetricDTO {
    private Long id;
    private LocalDate recordDate;
    private Integer muscleSoreness;
    private Integer sleepQuality;
    private Integer subjectiveEnergy;
    private Integer restingHeartRate;
    private Double recoveryScore;
    private String advice;
}
