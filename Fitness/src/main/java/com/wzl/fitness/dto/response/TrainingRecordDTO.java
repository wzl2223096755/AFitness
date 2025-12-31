package com.wzl.fitness.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TrainingRecordDTO {
    private Long id;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private LocalDate trainingDate;
    private Double totalVolume;
}
