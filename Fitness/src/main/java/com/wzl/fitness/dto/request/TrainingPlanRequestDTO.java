package com.wzl.fitness.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 训练计划创建请求DTO
 */
@Data
public class TrainingPlanRequestDTO {
    private String name;
    private Integer duration;
    private String goal;
    private String level;
    private Integer daysPerWeek;
    private Integer durationPerSession;
    private List<WeekDTO> weeklyPlan;

    @Data
    public static class WeekDTO {
        private List<DayDTO> days;
    }

    @Data
    public static class DayDTO {
        private Boolean hasTraining;
        private String focus;
        private List<ExerciseDTO> exercises;
    }

    @Data
    public static class ExerciseDTO {
        private String name;
        private Integer sets;
        private String reps;
        private Double weight;
        private Integer rest;
        private String notes;
    }
}
