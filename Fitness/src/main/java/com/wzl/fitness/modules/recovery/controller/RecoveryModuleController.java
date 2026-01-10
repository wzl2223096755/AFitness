package com.wzl.fitness.modules.recovery.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.modules.recovery.dto.RecoveryStatusDTO;
import com.wzl.fitness.modules.recovery.dto.TrainingSuggestionDTO;
import com.wzl.fitness.modules.recovery.service.RecoveryModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 恢复评估模块控制器
 * 
 * 提供恢复状态评估和训练建议的REST API
 * 
 * @see Requirements 1.2 - 领域模块包含独立的controller子包
 * @see Requirements 7.1 - API路由前缀规范 /api/v1/recovery
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@RestController
@RequestMapping("/api/v1/recovery")
@Tag(name = "恢复评估模块", description = "恢复状态评估和训练建议API")
@ConditionalOnProperty(
    prefix = "fitness.modules.recovery",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class RecoveryModuleController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(RecoveryModuleController.class);
    
    private final RecoveryModuleService recoveryModuleService;
    
    public RecoveryModuleController(RecoveryModuleService recoveryModuleService) {
        this.recoveryModuleService = recoveryModuleService;
    }
    
    /**
     * 获取当前恢复状态
     */
    @GetMapping("/status")
    @RequireUser
    @Operation(
            summary = "获取当前恢复状态",
            description = "获取当前用户的最新恢复状态评估"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "恢复状态示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取恢复状态成功",
                                              "data": {
                                                "userId": 1,
                                                "assessmentDate": "2024-01-01",
                                                "overallScore": 75,
                                                "recoveryStatus": "GOOD",
                                                "sleepScore": 80,
                                                "sleepHours": 7.5,
                                                "recommendedIntensity": "MODERATE",
                                                "estimatedRecoveryDays": 1
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<RecoveryStatusDTO>> getCurrentStatus(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.debug("获取用户 {} 的当前恢复状态", userId);
        
        RecoveryStatusDTO status = recoveryModuleService.getCurrentStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("获取恢复状态成功", status));
    }
    
    /**
     * 获取指定日期的恢复状态
     */
    @GetMapping("/status/{date}")
    @RequireUser
    @Operation(
            summary = "获取指定日期的恢复状态",
            description = "获取用户在指定日期的恢复状态评估"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功"
            )
    })
    public ResponseEntity<ApiResponse<RecoveryStatusDTO>> getStatusByDate(
            @Parameter(description = "日期", required = true, example = "2024-01-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.debug("获取用户 {} 在 {} 的恢复状态", userId, date);
        
        RecoveryStatusDTO status = recoveryModuleService.getStatusByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.success("获取恢复状态成功", status));
    }
    
    /**
     * 获取训练建议
     */
    @GetMapping("/suggestions")
    @RequireUser
    @Operation(
            summary = "获取训练建议",
            description = "根据用户当前恢复状态生成个性化训练建议"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "训练建议示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取训练建议成功",
                                              "data": [
                                                {
                                                  "suggestionType": "INTENSITY",
                                                  "title": "正常训练日",
                                                  "description": "恢复状态良好，可以进行正常强度训练",
                                                  "recommendedIntensity": "MODERATE",
                                                  "recommendedDuration": 45,
                                                  "priority": 1,
                                                  "isRestDay": false
                                                },
                                                {
                                                  "suggestionType": "MUSCLE_GROUP",
                                                  "title": "肌群建议",
                                                  "description": "最近进行了上肢训练，建议今天训练下肢",
                                                  "targetMuscleGroup": "下肢",
                                                  "priority": 3,
                                                  "isRestDay": false
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
    public ResponseEntity<ApiResponse<List<TrainingSuggestionDTO>>> getSuggestions(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.debug("获取用户 {} 的训练建议", userId);
        
        List<TrainingSuggestionDTO> suggestions = recoveryModuleService.getSuggestions(userId);
        return ResponseEntity.ok(ApiResponse.success("获取训练建议成功", suggestions));
    }
    
    /**
     * 计算恢复评分
     */
    @PostMapping("/calculate-score")
    @RequireUser
    @Operation(
            summary = "计算恢复评分",
            description = "根据提供的恢复指标计算恢复评分"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "计算成功"
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "恢复指标数据",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "恢复指标请求示例",
                            value = """
                                    {
                                      "sleepHours": 7.5,
                                      "sleepQuality": 7,
                                      "muscleSoreness": 3,
                                      "stressLevel": 4,
                                      "hrv": 55,
                                      "restingHeartRate": 60
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<RecoveryScoreResponse>> calculateScore(
            @RequestBody RecoveryScoreRequest scoreRequest,
            HttpServletRequest request) {
        getUserIdFromRequest(request); // 验证用户身份
        
        int score = recoveryModuleService.calculateRecoveryScore(
                scoreRequest.getSleepHours(),
                scoreRequest.getSleepQuality(),
                scoreRequest.getMuscleSoreness(),
                scoreRequest.getStressLevel(),
                scoreRequest.getHrv(),
                scoreRequest.getRestingHeartRate()
        );
        
        String intensity = recoveryModuleService.getRecommendedIntensity(score);
        
        RecoveryScoreResponse response = new RecoveryScoreResponse(score, intensity);
        return ResponseEntity.ok(ApiResponse.success("计算恢复评分成功", response));
    }
    
    /**
     * 恢复评分请求DTO
     */
    public static class RecoveryScoreRequest {
        private Double sleepHours;
        private Integer sleepQuality;
        private Integer muscleSoreness;
        private Integer stressLevel;
        private Integer hrv;
        private Integer restingHeartRate;
        
        public Double getSleepHours() { return sleepHours; }
        public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }
        public Integer getSleepQuality() { return sleepQuality; }
        public void setSleepQuality(Integer sleepQuality) { this.sleepQuality = sleepQuality; }
        public Integer getMuscleSoreness() { return muscleSoreness; }
        public void setMuscleSoreness(Integer muscleSoreness) { this.muscleSoreness = muscleSoreness; }
        public Integer getStressLevel() { return stressLevel; }
        public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }
        public Integer getHrv() { return hrv; }
        public void setHrv(Integer hrv) { this.hrv = hrv; }
        public Integer getRestingHeartRate() { return restingHeartRate; }
        public void setRestingHeartRate(Integer restingHeartRate) { this.restingHeartRate = restingHeartRate; }
    }
    
    /**
     * 恢复评分响应DTO
     */
    public static class RecoveryScoreResponse {
        private final int score;
        private final String recommendedIntensity;
        
        public RecoveryScoreResponse(int score, String recommendedIntensity) {
            this.score = score;
            this.recommendedIntensity = recommendedIntensity;
        }
        
        public int getScore() { return score; }
        public String getRecommendedIntensity() { return recommendedIntensity; }
    }
}
