package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 恢复数据请求DTO
 */
public class RecoveryDataRequest {
    private LocalDate recordDate;
    
    @NotNull(message = "睡眠时长不能为空")
    @Min(value = 0, message = "睡眠时长不能为负数")
    @Max(value = 24, message = "睡眠时长不能超过24小时")
    private Double sleepHours;
    
    @Min(value = 1, message = "睡眠质量评分最小为1")
    @Max(value = 10, message = "睡眠质量评分最大为10")
    private Integer sleepQuality;
    
    @Min(value = 1, message = "肌肉酸痛评分最小为1")
    @Max(value = 10, message = "肌肉酸痛评分最大为10")
    private Integer muscleSoreness;
    
    @Min(value = 1, message = "疲劳程度评分最小为1")
    @Max(value = 10, message = "疲劳程度评分最大为10")
    private Integer fatigueLevel;
    
    @Min(value = 1, message = "压力水平评分最小为1")
    @Max(value = 10, message = "压力水平评分最大为10")
    private Integer stressLevel;
    
    @Min(value = 0, message = "HRV不能为负数")
    private Integer hrv;
    
    @Min(value = 30, message = "静息心率不能低于30")
    @Max(value = 200, message = "静息心率不能超过200")
    private Integer restingHeartRate;
    
    @Min(value = 1, message = "精力水平评分最小为1")
    @Max(value = 10, message = "精力水平评分最大为10")
    private Integer energyLevel;
    
    private String notes;
    
    public RecoveryDataRequest() {}
    
    public RecoveryDataRequest(LocalDate recordDate, Double sleepHours, Integer sleepQuality, Integer muscleSoreness,
                               Integer fatigueLevel, Integer stressLevel, Integer hrv, Integer restingHeartRate,
                               Integer energyLevel, String notes) {
        this.recordDate = recordDate;
        this.sleepHours = sleepHours;
        this.sleepQuality = sleepQuality;
        this.muscleSoreness = muscleSoreness;
        this.fatigueLevel = fatigueLevel;
        this.stressLevel = stressLevel;
        this.hrv = hrv;
        this.restingHeartRate = restingHeartRate;
        this.energyLevel = energyLevel;
        this.notes = notes;
    }
    
    // Getters and Setters
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Double getSleepHours() { return sleepHours; }
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }
    public Integer getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(Integer sleepQuality) { this.sleepQuality = sleepQuality; }
    public Integer getMuscleSoreness() { return muscleSoreness; }
    public void setMuscleSoreness(Integer muscleSoreness) { this.muscleSoreness = muscleSoreness; }
    public Integer getFatigueLevel() { return fatigueLevel; }
    public void setFatigueLevel(Integer fatigueLevel) { this.fatigueLevel = fatigueLevel; }
    public Integer getStressLevel() { return stressLevel; }
    public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }
    public Integer getHrv() { return hrv; }
    public void setHrv(Integer hrv) { this.hrv = hrv; }
    public Integer getRestingHeartRate() { return restingHeartRate; }
    public void setRestingHeartRate(Integer restingHeartRate) { this.restingHeartRate = restingHeartRate; }
    public Integer getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(Integer energyLevel) { this.energyLevel = energyLevel; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Builder
    public static RecoveryDataRequestBuilder builder() { return new RecoveryDataRequestBuilder(); }
    
    public static class RecoveryDataRequestBuilder {
        private LocalDate recordDate;
        private Double sleepHours;
        private Integer sleepQuality;
        private Integer muscleSoreness;
        private Integer fatigueLevel;
        private Integer stressLevel;
        private Integer hrv;
        private Integer restingHeartRate;
        private Integer energyLevel;
        private String notes;
        
        public RecoveryDataRequestBuilder recordDate(LocalDate v) { this.recordDate = v; return this; }
        public RecoveryDataRequestBuilder sleepHours(Double v) { this.sleepHours = v; return this; }
        public RecoveryDataRequestBuilder sleepQuality(Integer v) { this.sleepQuality = v; return this; }
        public RecoveryDataRequestBuilder muscleSoreness(Integer v) { this.muscleSoreness = v; return this; }
        public RecoveryDataRequestBuilder fatigueLevel(Integer v) { this.fatigueLevel = v; return this; }
        public RecoveryDataRequestBuilder stressLevel(Integer v) { this.stressLevel = v; return this; }
        public RecoveryDataRequestBuilder hrv(Integer v) { this.hrv = v; return this; }
        public RecoveryDataRequestBuilder restingHeartRate(Integer v) { this.restingHeartRate = v; return this; }
        public RecoveryDataRequestBuilder energyLevel(Integer v) { this.energyLevel = v; return this; }
        public RecoveryDataRequestBuilder notes(String v) { this.notes = v; return this; }
        
        public RecoveryDataRequest build() {
            return new RecoveryDataRequest(recordDate, sleepHours, sleepQuality, muscleSoreness, fatigueLevel,
                    stressLevel, hrv, restingHeartRate, energyLevel, notes);
        }
    }
}
