package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 恢复状态数据实体
 */
@Entity
@Data
@Table(name = "recovery_data")
public class RecoveryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer recoveryScore; // 恢复评分（1-100）

    @Column(nullable = false)
    private Double sleepHours; // 睡眠时长

    @Column(nullable = false)
    private Integer sleepQuality; // 睡眠质量（1-10）

    @Column(nullable = false)
    private Integer heartRateVariability; // 心率变异性

    @Column(nullable = false)
    private Integer restingHeartRate; // 静息心率

    @Column
    private Double muscleSoreness; // 肌肉酸痛度（1-10）

    @Column
    private Double stressLevel; // 压力水平（1-10）

    @Column
    private String notes; // 恢复备注

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
    
    public Integer getRecoveryScore() {
        return recoveryScore;
    }
    
    public Double getSleepHours() {
        return sleepHours;
    }
    
    public Integer getSleepQuality() {
        return sleepQuality;
    }
    
    public Integer getHeartRateVariability() {
        return heartRateVariability;
    }
    
    public Integer getRestingHeartRate() {
        return restingHeartRate;
    }
    
    public Double getMuscleSoreness() {
        return muscleSoreness;
    }
    
    public Double getStressLevel() {
        return stressLevel;
    }
    
    public String getNotes() {
        return notes;
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
    
    public void setRecoveryScore(Integer recoveryScore) {
        this.recoveryScore = recoveryScore;
    }
    
    public void setRecoveryScore(int recoveryScore) {
        this.recoveryScore = recoveryScore;
    }
    
    public void setSleepHours(Double sleepHours) {
        this.sleepHours = sleepHours;
    }
    
    public void setSleepHours(double sleepHours) {
        this.sleepHours = sleepHours;
    }
    
    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }
    
    public void setSleepQuality(int sleepQuality) {
        this.sleepQuality = sleepQuality;
    }
    
    public void setHeartRateVariability(Integer heartRateVariability) {
        this.heartRateVariability = heartRateVariability;
    }
    
    public void setHeartRateVariability(int heartRateVariability) {
        this.heartRateVariability = heartRateVariability;
    }
    
    public void setRestingHeartRate(Integer restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
    
    public void setRestingHeartRate(int restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
    
    public void setMuscleSoreness(Double muscleSoreness) {
        this.muscleSoreness = muscleSoreness;
    }
    
    public void setMuscleSoreness(double muscleSoreness) {
        this.muscleSoreness = muscleSoreness;
    }
    
    public void setStressLevel(Double stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    public void setStressLevel(double stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}