package com.wzl.fitness.modules.recovery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复状态数据传输对象
 * 用于模块间传输用户恢复状态信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryStatusDTO {
    
    private Long userId;
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
    private Integer estimatedRecoveryDays;
    private List<String> recoveryAdvice;
}
