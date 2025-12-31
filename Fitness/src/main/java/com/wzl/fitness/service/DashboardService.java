package com.wzl.fitness.service;

import com.wzl.fitness.dto.response.DashboardMetricsResponse;
import com.wzl.fitness.dto.response.DashboardOverview;
import com.wzl.fitness.dto.response.UserStatsOverviewResponse;
import com.wzl.fitness.dto.response.AnalyticsDataResponse;
import com.wzl.fitness.dto.response.TrainingRecordResponse;
import com.wzl.fitness.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘指标概览
     * @param userId 用户ID
     * @param timeRange 时间范围 (week, month, year)
     * @return 仪表盘指标数据
     */
    DashboardMetricsResponse getMetricsOverview(Long userId, String timeRange);

    /**
     * 获取用户统计概览
     * @param userId 用户ID
     * @return 用户统计数据
     */
    UserStatsOverviewResponse getUserStatsOverview(Long userId);

    /**
     * 获取分析数据
     * @param userId 用户ID
     * @param timeRange 时间范围 (week, month, year)
     * @return 分析数据
     */
    AnalyticsDataResponse getAnalyticsData(Long userId, String timeRange);

    /**
     * 获取最近训练记录
     * @param userId 用户ID
     * @return 最近训练记录列表
     */
    List<TrainingRecordResponse> getRecentTrainingRecords(Long userId);

    /**
     * 获取训练趋势数据
     * @param userId 用户ID
     * @param timeRange 时间范围 (week, month, year)
     * @return 训练趋势数据
     */
    Object getTrainingTrend(Long userId, String timeRange);

    /**
     * 获取身体指标数据
     * @param userId 用户ID
     * @param timeRange 时间范围 (week, month, year)
     * @return 身体指标数据
     */
    Object getBodyMetrics(Long userId, String timeRange);

    /**
     * 获取训练分布数据
     * @param userId 用户ID
     * @param timeRange 时间范围 (week, month, year)
     * @return 训练分布数据
     */
    Object getTrainingDistribution(Long userId, String timeRange);
    
    /**
     * 获取综合仪表盘概览
     * 聚合训练、营养、恢复等核心数据
     * @param user 用户
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 仪表盘概览数据
     */
    DashboardOverview getDashboardOverview(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取本周仪表盘概览
     * @param user 用户
     * @return 本周仪表盘概览
     */
    DashboardOverview getWeeklyOverview(User user);
    
    /**
     * 获取1RM进步趋势
     * @param user 用户
     * @param exerciseName 动作名称
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 1RM进步数据
     */
    List<DashboardOverview.PersonalRecord> getOneRepMaxProgress(User user, String exerciseName, LocalDate startDate, LocalDate endDate);
}
