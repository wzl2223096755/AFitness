package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 健身计划每日安排实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "training_plan_days")
public class TrainingPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private TrainingPlan plan;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 0-6 (周一到周日)

    @Column(name = "week_number")
    private Integer weekNumber; // 周数 (1, 2, ...)

    @Column(name = "day_name", length = 20)
    private String dayName;

    @Column(name = "has_training")
    private Boolean hasTraining;

    @Column(length = 50)
    private String focus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrainingPlanExercise> exercises = new ArrayList<>();
}
