package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 力量训练数据实体
 */
@Entity
@Data
@Table(name = "strength_training_data")
public class StrengthTrainingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 100)
    private String exerciseName; // 动作名称

    @Column(nullable = false)
    private Double weight; // 重量（千克）

    @Column(nullable = false)
    private Integer sets; // 组数

    @Column(nullable = false)
    private Integer reps; // 次数

    @Column(nullable = false, length = 50)
    private String exerciseType; // 动作类型（上肢、下肢、核心等）

    @Column
    private Double oneRepMax; // 最大重量估算

    @Column
    private Double trainingVolume; // 训练量（重量*组数*次数）

    @Column
    private Integer perceivedExertion; // 主观疲劳度（1-10）

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 显式添加getter方法
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getExerciseName() {
        return exerciseName;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public String getExerciseType() {
        return exerciseType;
    }
    
    public Double getOneRepMax() {
        return oneRepMax;
    }
    
    public Double getTrainingVolume() {
        return trainingVolume;
    }
    
    public Integer getPerceivedExertion() {
        return perceivedExertion;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // 显式添加setter方法
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public void setSets(int sets) {
        this.sets = sets;
    }
    
    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public void setReps(int reps) {
        this.reps = reps;
    }
    
    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }
    
    public void setOneRepMax(Double oneRepMax) {
        this.oneRepMax = oneRepMax;
    }
    
    public void setTrainingVolume(Double trainingVolume) {
        this.trainingVolume = trainingVolume;
    }
    
    public void setPerceivedExertion(Integer perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }
}