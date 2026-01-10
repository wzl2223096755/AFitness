package com.wzl.fitness.modules.training.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 训练负荷数据传输对象
 * 用于模块间传输训练负荷计算结果
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingLoadDTO {
    
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalVolume;
    private Double averageVolume;
    private Integer totalSessions;
    private Double acuteLoad;
    private Double chronicLoad;
    private Double acuteChronicRatio;
    private String loadStatus;
}
