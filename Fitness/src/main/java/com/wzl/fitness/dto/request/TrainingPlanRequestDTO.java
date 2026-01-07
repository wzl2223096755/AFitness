package com.wzl.fitness.dto.request;

import com.wzl.fitness.validation.NoXss;
import lombok.Data;
import java.util.List;

/**
 * 训练计划创建请求DTO
 */
@Data
public class TrainingPlanRequestDTO {
    @NoXss
    private String name;
    private Integer duration;
    @NoXss
    private String goal;
    @NoXss
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
        @NoXss
        private String focus;
        private List<ExerciseDTO> exercises;
    }

    @Data
    public static class ExerciseDTO {
        @NoXss
        private String name;
        private Integer sets;
        @NoXss
        private String reps;
        private Double weight;
        private Integer rest;
        @NoXss
        private String notes;
    }
}
