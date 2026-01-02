package com.wzl.fitness.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复评估结果DTO
 */
public class RecoveryAssessment {
    private LocalDate assessmentDate;
    private Integer overallScore;
    private String recoveryStatus;
    private Integer sleepScore;
    private Double sleepHours;
    private Integer muscleSorenessScore;
    private Integer fatigueScore;
    private Integer stressScore;
    private Integer hrv;
    private Integer restingHeartRate;
    private Integer trainingLoadImpact;
    private String recommendedIntensity;
    private List<String> trainingAdvice;
    private List<String> recoveryAdvice;
    private Integer estimatedRecoveryDays;
    
    public RecoveryAssessment() {}
    
    public RecoveryAssessment(LocalDate assessmentDate, Integer overallScore, String recoveryStatus,
                              Integer sleepScore, Double sleepHours, Integer muscleSorenessScore,
                              Integer fatigueScore, Integer stressScore, Integer hrv, Integer restingHeartRate,
                              Integer trainingLoadImpact, String recommendedIntensity, List<String> trainingAdvice,
                              List<String> recoveryAdvice, Integer estimatedRecoveryDays) {
        this.assessmentDate = assessmentDate;
        this.overallScore = overallScore;
        this.recoveryStatus = recoveryStatus;
        this.sleepScore = sleepScore;
        this.sleepHours = sleepHours;
        this.muscleSorenessScore = muscleSorenessScore;
        this.fatigueScore = fatigueScore;
        this.stressScore = stressScore;
        this.hrv = hrv;
        this.restingHeartRate = restingHeartRate;
        this.trainingLoadImpact = trainingLoadImpact;
        this.recommendedIntensity = recommendedIntensity;
        this.trainingAdvice = trainingAdvice;
        this.recoveryAdvice = recoveryAdvice;
        this.estimatedRecoveryDays = estimatedRecoveryDays;
    }
    
    // Getters and Setters
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }
    public String getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    public Integer getSleepScore() { return sleepScore; }
    public void setSleepScore(Integer sleepScore) { this.sleepScore = sleepScore; }
    public Double getSleepHours() { return sleepHours; }
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }
    public Integer getMuscleSorenessScore() { return muscleSorenessScore; }
    public void setMuscleSorenessScore(Integer muscleSorenessScore) { this.muscleSorenessScore = muscleSorenessScore; }
    public Integer getFatigueScore() { return fatigueScore; }
    public void setFatigueScore(Integer fatigueScore) { this.fatigueScore = fatigueScore; }
    public Integer getStressScore() { return stressScore; }
    public void setStressScore(Integer stressScore) { this.stressScore = stressScore; }
    public Integer getHrv() { return hrv; }
    public void setHrv(Integer hrv) { this.hrv = hrv; }
    public Integer getRestingHeartRate() { return restingHeartRate; }
    public void setRestingHeartRate(Integer restingHeartRate) { this.restingHeartRate = restingHeartRate; }
    public Integer getTrainingLoadImpact() { return trainingLoadImpact; }
    public void setTrainingLoadImpact(Integer trainingLoadImpact) { this.trainingLoadImpact = trainingLoadImpact; }
    public String getRecommendedIntensity() { return recommendedIntensity; }
    public void setRecommendedIntensity(String recommendedIntensity) { this.recommendedIntensity = recommendedIntensity; }
    public List<String> getTrainingAdvice() { return trainingAdvice; }
    public void setTrainingAdvice(List<String> trainingAdvice) { this.trainingAdvice = trainingAdvice; }
    public List<String> getRecoveryAdvice() { return recoveryAdvice; }
    public void setRecoveryAdvice(List<String> recoveryAdvice) { this.recoveryAdvice = recoveryAdvice; }
    public Integer getEstimatedRecoveryDays() { return estimatedRecoveryDays; }
    public void setEstimatedRecoveryDays(Integer estimatedRecoveryDays) { this.estimatedRecoveryDays = estimatedRecoveryDays; }
    
    // Builder
    public static RecoveryAssessmentBuilder builder() { return new RecoveryAssessmentBuilder(); }
    
    public static class RecoveryAssessmentBuilder {
        private LocalDate assessmentDate;
        private Integer overallScore;
        private String recoveryStatus;
        private Integer sleepScore;
        private Double sleepHours;
        private Integer muscleSorenessScore;
        private Integer fatigueScore;
        private Integer stressScore;
        private Integer hrv;
        private Integer restingHeartRate;
        private Integer trainingLoadImpact;
        private String recommendedIntensity;
        private List<String> trainingAdvice;
        private List<String> recoveryAdvice;
        private Integer estimatedRecoveryDays;
        
        public RecoveryAssessmentBuilder assessmentDate(LocalDate v) { this.assessmentDate = v; return this; }
        public RecoveryAssessmentBuilder overallScore(Integer v) { this.overallScore = v; return this; }
        public RecoveryAssessmentBuilder recoveryStatus(String v) { this.recoveryStatus = v; return this; }
        public RecoveryAssessmentBuilder sleepScore(Integer v) { this.sleepScore = v; return this; }
        public RecoveryAssessmentBuilder sleepHours(Double v) { this.sleepHours = v; return this; }
        public RecoveryAssessmentBuilder muscleSorenessScore(Integer v) { this.muscleSorenessScore = v; return this; }
        public RecoveryAssessmentBuilder fatigueScore(Integer v) { this.fatigueScore = v; return this; }
        public RecoveryAssessmentBuilder stressScore(Integer v) { this.stressScore = v; return this; }
        public RecoveryAssessmentBuilder hrv(Integer v) { this.hrv = v; return this; }
        public RecoveryAssessmentBuilder restingHeartRate(Integer v) { this.restingHeartRate = v; return this; }
        public RecoveryAssessmentBuilder trainingLoadImpact(Integer v) { this.trainingLoadImpact = v; return this; }
        public RecoveryAssessmentBuilder recommendedIntensity(String v) { this.recommendedIntensity = v; return this; }
        public RecoveryAssessmentBuilder trainingAdvice(List<String> v) { this.trainingAdvice = v; return this; }
        public RecoveryAssessmentBuilder recoveryAdvice(List<String> v) { this.recoveryAdvice = v; return this; }
        public RecoveryAssessmentBuilder estimatedRecoveryDays(Integer v) { this.estimatedRecoveryDays = v; return this; }
        
        public RecoveryAssessment build() {
            return new RecoveryAssessment(assessmentDate, overallScore, recoveryStatus, sleepScore, sleepHours,
                    muscleSorenessScore, fatigueScore, stressScore, hrv, restingHeartRate, trainingLoadImpact,
                    recommendedIntensity, trainingAdvice, recoveryAdvice, estimatedRecoveryDays);
        }
    }
}
