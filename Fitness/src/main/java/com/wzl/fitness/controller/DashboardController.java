package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.common.ResponseCode;
import com.wzl.fitness.dto.response.DashboardMetricsResponse;
import com.wzl.fitness.dto.response.UserStatsOverviewResponse;
import com.wzl.fitness.dto.response.AnalyticsDataResponse;
import com.wzl.fitness.dto.response.TrainingRecordResponse;
import com.wzl.fitness.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘管理", description = "仪表盘数据相关接口")
public class DashboardController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardService dashboardService;

    /**
     * 获取仪表盘指标概览
     */
    @GetMapping("/metrics-overview")
    @Operation(summary = "获取仪表盘指标概览", description = "获取用户的训练指标概览数据")
    @RequireUser
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getMetricsOverview(
            HttpServletRequest request,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            DashboardMetricsResponse metrics = dashboardService.getMetricsOverview(getUserIdFromRequest(request), timeRange);
            return ResponseEntity.ok(ApiResponse.success("获取指标概览成功", metrics));
        } catch (Exception e) {
            logger.error("获取指标概览失败", e);
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.SERVER_ERROR.getCode(), "获取指标概览失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户统计概览
     */
    @GetMapping("/user-stats-overview")
    @Operation(summary = "获取用户统计概览", description = "获取用户的统计数据概览")
    @RequireUser
    public ResponseEntity<ApiResponse<UserStatsOverviewResponse>> getUserStatsOverview(HttpServletRequest request) {
        try {
            UserStatsOverviewResponse stats = dashboardService.getUserStatsOverview(getUserIdFromRequest(request));
            return ResponseEntity.ok(ApiResponse.success("获取用户统计概览成功", stats));
        } catch (Exception e) {
            logger.error("获取用户统计概览失败", e);
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.SERVER_ERROR.getCode(), "获取用户统计概览失败: " + e.getMessage()));
        }
    }

    /**
     * 获取分析数据
     */
    @GetMapping("/analytics")
    @Operation(summary = "获取分析数据", description = "获取用户的训练分析数据")
    @RequireUser
    public ResponseEntity<ApiResponse<AnalyticsDataResponse>> getAnalyticsData(
            HttpServletRequest request,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            AnalyticsDataResponse analytics = dashboardService.getAnalyticsData(getUserIdFromRequest(request), timeRange);
            return ResponseEntity.ok(ApiResponse.success("获取分析数据成功", analytics));
        } catch (Exception e) {
            logger.error("获取分析数据失败", e);
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.SERVER_ERROR.getCode(), "获取分析数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取最近训练记录
     */
    @GetMapping("/recent-training-records")
    @Operation(summary = "获取最近训练记录", description = "获取用户最近的训练记录列表")
    @RequireUser
    public ResponseEntity<ApiResponse<List<TrainingRecordResponse>>> getRecentTrainingRecords(HttpServletRequest request) {
        try {
            List<TrainingRecordResponse> records = dashboardService.getRecentTrainingRecords(getUserIdFromRequest(request));
            return ResponseEntity.ok(ApiResponse.success("获取最近训练记录成功", records));
        } catch (Exception e) {
            logger.error("获取最近训练记录失败", e);
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.SERVER_ERROR.getCode(), "获取最近训练记录失败: " + e.getMessage()));
        }
    }
}
