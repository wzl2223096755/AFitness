package com.wzl.fitness.modules.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 每日活跃度数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyActivityDTO {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 活跃用户数
     */
    private Long activeUsers;
    
    /**
     * 新注册用户数
     */
    private Long newUsers;
    
    /**
     * 训练记录数
     */
    private Long trainingRecords;
    
    /**
     * 营养记录数
     */
    private Long nutritionRecords;
}
