package com.wzl.fitness.dto.response;

import lombok.Data;

@Data
public class ExerciseDetailDTO {
    private Long id;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private String notes;
}
