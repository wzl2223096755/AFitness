package com.wzl.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "recovery_metrics")
public class RecoveryMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private LocalDate recordDate;
    private Integer muscleSoreness; // 1-5
    private Integer sleepQuality;   // 1-5
    private Integer restingHeartRate;
    private Integer subjectiveEnergy; // 1-5
    
    // Explicit getters and setters to ensure compilation
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public Integer getMuscleSoreness() {
        return muscleSoreness;
    }
    
    public void setMuscleSoreness(Integer muscleSoreness) {
        this.muscleSoreness = muscleSoreness;
    }
    
    public Integer getSleepQuality() {
        return sleepQuality;
    }
    
    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }
    
    public Integer getRestingHeartRate() {
        return restingHeartRate;
    }
    
    public void setRestingHeartRate(Integer restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
    
    public Integer getSubjectiveEnergy() {
        return subjectiveEnergy;
    }
    
    public void setSubjectiveEnergy(Integer subjectiveEnergy) {
        this.subjectiveEnergy = subjectiveEnergy;
    }
}