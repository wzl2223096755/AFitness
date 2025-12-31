package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "exercise_details")
@ToString(exclude = {"trainingRecord"})
public class ExerciseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "训练记录不能为空")
    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    @JsonIgnore
    private TrainingRecord trainingRecord;
    
    @NotBlank(message = "动作名称不能为空")
    @Size(min = 1, max = 100, message = "动作名称长度必须在1-100之间")
    private String exerciseName;
    
    @DecimalMin(value = "0.0", message = "重量不能为负数")
    @DecimalMax(value = "1000.0", message = "重量不能超过1000kg")
    private Double weight;
    
    @Min(value = 0, message = "组数不能为负数")
    @Max(value = 100, message = "组数不能超过100")
    private Integer sets;
    
    @Min(value = 0, message = "次数不能为负数")
    @Max(value = 1000, message = "次数不能超过1000")
    private Integer reps;
    
    @Min(value = 1, message = "RPE值必须在1-10之间")
    @Max(value = 10, message = "RPE值必须在1-10之间")
    private Integer rpe; // 感知疲劳指数 (1-10)
    
    @DecimalMin(value = "0.0", message = "训练量不能为负数")
    private Double volume;
    
    @Size(max = 50, message = "动作类型长度不能超过50")
    private String exerciseType;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 显式添加setter方法
    public void setTrainingRecord(TrainingRecord trainingRecord) {
        this.trainingRecord = trainingRecord;
    }
    
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public void setRpe(Integer rpe) {
        this.rpe = rpe;
    }
    
    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }
    
    public void setVolume(Double volume) {
        this.volume = volume;
    }
    
    // 显式添加getter方法
    public Double getWeight() {
        return weight;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public Integer getRpe() {
        return rpe;
    }
}