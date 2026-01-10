package com.wzl.fitness.dto.response;

/**
 * 训练统计响应DTO
 */
public class TrainingStatsResponse {
    private Long totalRecords;
    private Double totalVolume;
    private Long totalDuration;
    
    public TrainingStatsResponse() {}
    
    public TrainingStatsResponse(Long totalRecords, Double totalVolume, Long totalDuration) {
        this.totalRecords = totalRecords;
        this.totalVolume = totalVolume;
        this.totalDuration = totalDuration;
    }
    
    // Getters and Setters
    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }
    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
    public Long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }
}
