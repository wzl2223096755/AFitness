package com.wzl.fitness.dto.response;

import com.wzl.fitness.entity.ExerciseDetail;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrainingRecordResponse {
    private Long id;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private LocalDate trainingDate;
    private Integer duration;
    private String notes;
    private Double totalVolume;
    private Double trainingStress;
    private Double calculatedTotalVolume;
    private Integer caloriesBurned;
    private LocalDateTime createdAt;
    private List<ExerciseDetail> exerciseDetails;
}
