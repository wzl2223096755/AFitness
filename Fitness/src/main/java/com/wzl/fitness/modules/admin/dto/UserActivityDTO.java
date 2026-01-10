package com.wzl.fitness.modules.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 用户活跃度数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityDTO {
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 今日活跃用户数
     */
    private Long activeUsersToday;
    
    /**
     * 本周活跃用户数
     */
    private Long activeUsersWeek;
    
    /**
     * 本月活跃用户数
     */
    private Long activeUsersMonth;
    
    /**
     * 今日新注册用户数
     */
    private Long newUsersToday;
    
    /**
     * 本周新注册用户数
     */
    private Long newUsersWeek;
    
    /**
     * 本月新注册用户数
     */
    private Long newUsersMonth;
    
    /**
     * 按角色统计的用户数
     */
    private Map<String, Long> usersByRole;
    
    /**
     * 每日活跃度趋势（最近30天）
     */
    private List<DailyActivityDTO> dailyActivity;
}
