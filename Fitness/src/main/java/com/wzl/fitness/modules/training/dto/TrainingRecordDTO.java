package com.wzl.fitness.modules.training.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录数据传输对象
 * 用于模块间传输训练记录信息，不暴露Entity对象
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRecordDTO {
    
    private Long id;
    private Long userId;
    private String exerciseName;
    private String exerciseType;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private Integer duration;
    private Double totalVolume;
    private LocalDate trainingDate;
    private String notes;
    private LocalDateTime createdAt;
}
