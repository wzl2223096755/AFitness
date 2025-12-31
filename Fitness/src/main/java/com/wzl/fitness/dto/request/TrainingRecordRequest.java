package com.wzl.fitness.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建训练记录请求DTO
 */
@Data
@Schema(description = "创建训练记录请求")
public class TrainingRecordRequest {

    @NotNull(message = "训练日期不能为空")
    @PastOrPresent(message = "训练日期不能是未来日期")
    @Schema(description = "训练日期", example = "2024-01-15")
    private LocalDate trainingDate;

    @DecimalMin(value = "0.0", message = "总训练量不能为负数")
    @Schema(description = "总训练量", example = "1000.0")
    private Double totalVolume;

    @DecimalMin(value = "0.0", message = "训练压力不能为负数")
    @DecimalMax(value = "100.0", message = "训练压力不能超过100")
    @Schema(description = "训练压力", example = "75.5")
    private Double trainingStress;

    @Valid
    @NotEmpty(message = "训练动作详情不能为空")
    @Size(min = 1, message = "至少需要一个训练动作")
    @Schema(description = "训练动作详情列表")
    private List<ExerciseDetailRequest> exerciseDetails;
    
    // 显式添加getter方法
    public LocalDate getTrainingDate() {
        return trainingDate;
    }
    
    public Double getTotalVolume() {
        return totalVolume;
    }
    
    public Double getTrainingStress() {
        return trainingStress;
    }
    
    public List<ExerciseDetailRequest> getExerciseDetails() {
        return exerciseDetails;
    }
}