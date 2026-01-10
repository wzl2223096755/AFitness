package com.wzl.fitness.modules.training.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 训练统计数据传输对象
 * 用于模块间传输训练统计信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingStatsDTO {
    
    private Long userId;
    private Long totalRecords;
    private Double totalVolume;
    private Long totalDuration;
    private Integer totalSessions;
    private Double averageVolumePerSession;
    private String mostFrequentExercise;
    private Double personalBestWeight;
}
