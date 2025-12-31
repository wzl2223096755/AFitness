package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 恢复数据请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryDataRequest {
    
    /**
     * 记录日期
     */
    private LocalDate recordDate;
    
    /**
     * 睡眠时长(小时)
     */
    @NotNull(message = "睡眠时长不能为空")
    @Min(value = 0, message = "睡眠时长不能为负数")
    @Max(value = 24, message = "睡眠时长不能超过24小时")
    private Double sleepHours;
    
    /**
     * 睡眠质量 (1-10)
     */
    @Min(value = 1, message = "睡眠质量评分最小为1")
    @Max(value = 10, message = "睡眠质量评分最大为10")
    private Integer sleepQuality;
    
    /**
     * 肌肉酸痛程度 (1-10, 10为最痛)
     */
    @Min(value = 1, message = "肌肉酸痛评分最小为1")
    @Max(value = 10, message = "肌肉酸痛评分最大为10")
    private Integer muscleSoreness;
    
    /**
     * 疲劳程度 (1-10, 10为最疲劳)
     */
    @Min(value = 1, message = "疲劳程度评分最小为1")
    @Max(value = 10, message = "疲劳程度评分最大为10")
    private Integer fatigueLevel;
    
    /**
     * 压力水平 (1-10, 10为最高压力)
     */
    @Min(value = 1, message = "压力水平评分最小为1")
    @Max(value = 10, message = "压力水平评分最大为10")
    private Integer stressLevel;
    
    /**
     * 心率变异性 (HRV)
     */
    @Min(value = 0, message = "HRV不能为负数")
    private Integer hrv;
    
    /**
     * 静息心率
     */
    @Min(value = 30, message = "静息心率不能低于30")
    @Max(value = 200, message = "静息心率不能超过200")
    private Integer restingHeartRate;
    
    /**
     * 主观精力水平 (1-10)
     */
    @Min(value = 1, message = "精力水平评分最小为1")
    @Max(value = 10, message = "精力水平评分最大为10")
    private Integer energyLevel;
    
    /**
     * 备注
     */
    private String notes;
}
