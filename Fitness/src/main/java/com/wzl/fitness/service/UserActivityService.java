package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.DailyActivityDTO;
import com.wzl.fitness.dto.response.UserActivityDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户活跃度统计服务接口
 */
public interface UserActivityService {
    
    /**
     * 获取用户活跃度统计信息
     * @return 用户活跃度统计 DTO
     */
    UserActivityDTO getUserActivityStats();
    
    /**
     * 获取指定日期范围的每日活跃度数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日活跃度列表
     */
    List<DailyActivityDTO> getDailyActivityStats(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取今日活跃用户数
     * @return 今日活跃用户数
     */
    Long getActiveUsersToday();
    
    /**
     * 获取本周活跃用户数
     * @return 本周活跃用户数
     */
    Long getActiveUsersThisWeek();
    
    /**
     * 获取本月活跃用户数
     * @return 本月活跃用户数
     */
    Long getActiveUsersThisMonth();
    
    /**
     * 获取今日新注册用户数
     * @return 今日新注册用户数
     */
    Long getNewUsersToday();
    
    /**
     * 获取本周新注册用户数
     * @return 本周新注册用户数
     */
    Long getNewUsersThisWeek();
    
    /**
     * 获取本月新注册用户数
     * @return 本月新注册用户数
     */
    Long getNewUsersThisMonth();
}
