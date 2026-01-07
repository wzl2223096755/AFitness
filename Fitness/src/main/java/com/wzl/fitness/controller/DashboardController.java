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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "仪表盘管理", description = "仪表盘数据相关接口，包括指标概览、统计数据、分析数据等")
public class DashboardController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardService dashboardService;

    /**
     * 获取仪表盘指标概览
     */
    @GetMapping("/metrics-overview")
    @Operation(
            summary = "获取仪表盘指标概览", 
            description = "获取用户的训练指标概览数据，包括训练次数、总训练量、平均强度等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "指标概览示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取指标概览成功",
                                              "data": {
                                                "totalWorkouts": 5,
                                                "totalVolume": 15000.0,
                                                "averageIntensity": 72.5,
                                                "caloriesBurned": 2500,
                                                "recoveryScore": 78,
                                                "weeklyProgress": {
                                                  "volumeChange": 12.5,
                                                  "intensityChange": 5.0
                                                }
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @RequireUser
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getMetricsOverview(
            HttpServletRequest request,
            @Parameter(description = "时间范围: day, week, month, year", example = "week")
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
    @Operation(
            summary = "获取用户统计概览", 
            description = "获取用户的统计数据概览，包括训练天数、连续训练天数、个人记录等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "用户统计概览示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取用户统计概览成功",
                                              "data": {
                                                "totalTrainingDays": 120,
                                                "currentStreak": 7,
                                                "longestStreak": 21,
                                                "totalVolume": 500000.0,
                                                "personalRecords": {
                                                  "深蹲": 150.0,
                                                  "卧推": 100.0,
                                                  "硬拉": 180.0
                                                },
                                                "favoriteExercises": ["深蹲", "卧推", "硬拉"]
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
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
    @Operation(
            summary = "获取分析数据", 
            description = "获取用户的训练分析数据，包括训练量趋势、肌群分布、强度分布等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "分析数据示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取分析数据成功",
                                              "data": {
                                                "volumeTrend": [
                                                  { "date": "2024-01-01", "value": 3200 },
                                                  { "date": "2024-01-02", "value": 2800 }
                                                ],
                                                "muscleGroupDistribution": {
                                                  "胸部": 25,
                                                  "背部": 25,
                                                  "腿部": 30
                                                },
                                                "intensityDistribution": {
                                                  "低强度": 20,
                                                  "中强度": 50,
                                                  "高强度": 30
                                                }
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @RequireUser
    public ResponseEntity<ApiResponse<AnalyticsDataResponse>> getAnalyticsData(
            HttpServletRequest request,
            @Parameter(description = "时间范围: day, week, month, year", example = "week")
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
    @Operation(
            summary = "获取最近训练记录", 
            description = "获取用户最近的训练记录列表，默认返回最近10条"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "最近训练记录示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取最近训练记录成功",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "exerciseName": "深蹲",
                                                  "trainingDate": "2024-01-01",
                                                  "totalVolume": 3200.0,
                                                  "duration": 45
                                                }
                                              ],
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
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
