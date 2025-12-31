package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 有氧训练数据实体
 */
@Entity
@Data
@Table(name = "cardio_training_data")
public class CardioTrainingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 100)
    private String exerciseType; // 运动类型（跑步、骑行、游泳等）

    @Column(nullable = false)
    private Integer duration; // 运动时长（分钟）

    @Column
    private Double distance; // 距离（千米）

    @Column
    private Integer averageHeartRate; // 平均心率

    @Column
    private Integer maxHeartRate; // 最大心率

    @Column
    private Double caloriesBurned; // 消耗卡路里

    @Column
    private Double averageSpeed; // 平均速度（km/h）

    @Column
    private Double pace; // 配速（分钟/千米）

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
    
    public String getExerciseType() {
        return exerciseType;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }
    
    public Integer getMaxHeartRate() {
        return maxHeartRate;
    }
    
    public Double getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public Double getAverageSpeed() {
        return averageSpeed;
    }
    
    public Double getPace() {
        return pace;
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
    
    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public void setAverageHeartRate(Integer averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }
    
    public void setAverageHeartRate(int averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }
    
    public void setMaxHeartRate(Integer maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }
    
    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }
    
    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    public void setPace(Double pace) {
        this.pace = pace;
    }
    
    public void setPace(double pace) {
        this.pace = pace;
    }
    
    public void setPerceivedExertion(Integer perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }
    
    public void setPerceivedExertion(int perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }
}