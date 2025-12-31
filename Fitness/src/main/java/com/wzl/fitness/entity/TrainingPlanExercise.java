package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健身计划动作详情实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "training_plan_exercises")
public class TrainingPlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    @JsonIgnore
    private TrainingPlanDay day;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer sets;

    @Column(length = 50)
    private String reps;

    private Double weight;

    @Column(name = "duration_minutes")
    private Double durationMinutes;

    private Integer intensity;

    @Column(name = "target_muscles", length = 200)
    private String targetMuscles;

    @Column(name = "rest_time")
    private Integer restTime; // 秒

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Builder.Default
    private Boolean completed = false;
}
