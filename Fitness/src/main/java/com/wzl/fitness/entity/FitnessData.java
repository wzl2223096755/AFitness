package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 健身数据实体（已废弃）
 * 
 * @deprecated 此实体包含冗余字段，与 {@link StrengthTrainingData} 和 {@link RecoveryData} 存在重复。
 * 建议使用以下替代方案：
 * <ul>
 *   <li>力量训练数据：使用 {@link StrengthTrainingData}</li>
 *   <li>恢复状态数据：使用 {@link RecoveryData}</li>
 * </ul>
 * 
 * <p>数据迁移方案：</p>
 * <ol>
 *   <li>将力量训练相关字段（exerciseName, weight, sets, reps, exerciseType, oneRepMax, trainingVolume, perceivedExertion）
 *       迁移到 strength_training_data 表</li>
 *   <li>将恢复状态相关字段（recoveryScore, recoveryStatus, sleepHours, stressLevel）
 *       迁移到 recovery_data 表</li>
 *   <li>完成迁移后，可以删除 fitness_data 表</li>
 * </ol>
 * 
 * @see StrengthTrainingData
 * @see RecoveryData
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@Entity
@Data
@Table(name = "fitness_data")
public class FitnessData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;



    @Column(nullable = false)
    private LocalDateTime timestamp;

    // 力量训练相关字段
    private String exerciseName; // 动作名称
    private Double weight; // 重量（千克）
    private Integer sets; // 组数
    private Integer reps; // 次数
    private String exerciseType; // 动作类型
    private Double oneRepMax; // 最大重量估算
    private Double trainingVolume; // 训练量（重量*组数*次数）
    private Integer perceivedExertion; // 主观疲劳度（1-10）
    
    // 恢复状态相关字段
    private Integer recoveryScore; // 恢复评分（0-100）
    private String recoveryStatus; // 恢复状态描述
    private Integer sleepHours; // 睡眠时长
    private Integer stressLevel; // 压力水平
    


    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 手动添加getter方法以确保编译通过
    public Double getWeight() {
        return weight;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public void setTrainingVolume(Double trainingVolume) {
        this.trainingVolume = trainingVolume;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getTrainingVolume() {
        return trainingVolume;
    }
    
    public String getExerciseType() {
        return exerciseType;
    }
    
    // 手动添加User相关的getter和setter方法
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    // 添加其他缺失的setter方法
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
    
    public void setOneRepMax(double oneRepMax) {
        this.oneRepMax = oneRepMax;
    }
    
    public void setPerceivedExertion(Integer perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }
    
    public void setPerceivedExertion(int perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }
    
    public void setRecoveryScore(Integer recoveryScore) {
        this.recoveryScore = recoveryScore;
    }
    
    public void setRecoveryScore(int recoveryScore) {
        this.recoveryScore = recoveryScore;
    }
    
    public void setRecoveryStatus(String recoveryStatus) {
        this.recoveryStatus = recoveryStatus;
    }
    
    public void setSleepHours(Integer sleepHours) {
        this.sleepHours = sleepHours;
    }
    
    public void setSleepHours(int sleepHours) {
        this.sleepHours = sleepHours;
    }
    
    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    public void setStressLevel(int stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    // 添加缺失的getter方法
    public String getExerciseName() {
        return exerciseName;
    }
    
    public Double getOneRepMax() {
        return oneRepMax;
    }
    
    public Integer getPerceivedExertion() {
        return perceivedExertion;
    }
    
    public Integer getRecoveryScore() {
        return recoveryScore;
    }
    
    public String getRecoveryStatus() {
        return recoveryStatus;
    }
    
    public Integer getSleepHours() {
        return sleepHours;
    }
    
    public Integer getStressLevel() {
        return stressLevel;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}