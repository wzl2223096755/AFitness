package com.wzl.fitness.modules.recovery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 训练建议数据传输对象
 * 用于模块间传输基于恢复状态的训练建议
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSuggestionDTO {
    
    private String suggestionType;
    private String title;
    private String description;
    private String recommendedIntensity;
    private Integer recommendedDuration;
    private String targetMuscleGroup;
    private Integer priority;
    private Boolean isRestDay;
}
