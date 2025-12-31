package com.wzl.fitness.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 训练动作详情请求DTO
 */
@Data
@Schema(description = "训练动作详情请求")
public class ExerciseDetailRequest {

    @NotBlank(message = "动作名称不能为空")
    @Size(min = 1, max = 100, message = "动作名称长度必须在1-100之间")
    @Schema(description = "动作名称", example = "深蹲")
    private String exerciseName;

    @DecimalMin(value = "0.0", message = "重量不能为负数")
    @DecimalMax(value = "1000.0", message = "重量不能超过1000kg")
    @Schema(description = "重量(kg)", example = "80.0")
    private Double weight;

    @Min(value = 0, message = "组数不能为负数")
    @Max(value = 100, message = "组数不能超过100")
    @Schema(description = "组数", example = "4")
    private Integer sets;

    @Min(value = 0, message = "次数不能为负数")
    @Max(value = 1000, message = "次数不能超过1000")
    @Schema(description = "每组次数", example = "12")
    private Integer reps;

    @Min(value = 1, message = "RPE值必须在1-10之间")
    @Max(value = 10, message = "RPE值必须在1-10之间")
    @Schema(description = "感知疲劳指数(1-10)", example = "8")
    private Integer rpe;

    @Size(max = 50, message = "动作类型长度不能超过50")
    @Schema(description = "动作类型", example = "力量训练")
    private String exerciseType;
    
    // 显式添加getter方法
    public String getExerciseName() {
        return exerciseName;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public Integer getRpe() {
        return rpe;
    }
    
    public String getExerciseType() {
        return exerciseType;
    }
}