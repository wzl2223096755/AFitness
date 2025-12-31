package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.entity.FitnessData;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.repository.FitnessDataRepository;
import com.wzl.fitness.service.LoadRecoveryService;
import com.wzl.fitness.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 负荷恢复控制器
 * 
 * 提供训练负荷计算和恢复状态评估功能，包括：
 * - 训练数据保存和负荷计算
 * - 恢复状态评估
 * - 训练建议生成
 * - 1RM计算（支持多种公式）
 */
@RestController
@RequestMapping("/api/v1/load-recovery")
@Tag(name = "负荷恢复", description = "负荷计算和恢复评估，包括1RM计算、训练负荷、恢复状态评估等")
public class LoadRecoveryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoadRecoveryController.class);

    @Autowired
    private LoadRecoveryService loadRecoveryService;

    @Autowired
    private FitnessDataRepository fitnessDataRepository;

    @Autowired
    private UserService userService;

    /**
     * 从Request中获取用户信息
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 保存训练数据并计算负荷
     */
    @PostMapping("/training-data")
    @RequireUser
    @Operation(
            summary = "保存训练数据", 
            description = "保存训练数据并自动计算训练负荷"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "保存成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "保存训练数据成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "训练数据保存成功",
                                              "data": {
                                                "id": 1,
                                                "exerciseName": "深蹲",
                                                "exerciseType": "STRENGTH",
                                                "weight": 100.0,
                                                "reps": 8,
                                                "sets": 4,
                                                "trainingLoad": 3200.0,
                                                "oneRepMax": 125.0,
                                                "timestamp": "2024-01-01T12:00:00"
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "训练数据",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "训练数据请求示例",
                            value = """
                                    {
                                      "exerciseName": "深蹲",
                                      "exerciseType": "STRENGTH",
                                      "weight": 100.0,
                                      "reps": 8,
                                      "sets": 4
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<FitnessData>> saveTrainingData(@RequestBody FitnessData fitnessData, HttpServletRequest request) {
        User user = getUser(request);
        
        fitnessData.setUser(user);
        fitnessData.setTimestamp(LocalDateTime.now());
        
        // 计算训练负荷
        FitnessData calculatedData = loadRecoveryService.calculateTrainingLoad(fitnessData);
        
        // 保存数据
        FitnessData savedData = fitnessDataRepository.save(calculatedData);
        
        return ResponseEntity.ok(ApiResponse.success("训练数据保存成功", savedData));
    }

    /**
     * 评估恢复状态
     */
    @PostMapping("/recovery-assessment")
    @RequireUser
    @Operation(
            summary = "评估恢复状态", 
            description = "根据睡眠时长和压力水平评估用户的恢复状态，返回恢复评分和建议"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "评估成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "恢复评估成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "恢复状态评估成功",
                                              "data": {
                                                "recoveryScore": 75,
                                                "recoveryStatus": "良好",
                                                "recommendation": "恢复状态良好，可以进行中等强度训练",
                                                "suggestedIntensity": "中等",
                                                "restDaysNeeded": 0
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "恢复评估参数",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "恢复评估请求示例",
                            value = """
                                    {
                                      "sleepHours": 7,
                                      "stressLevel": 3
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> assessRecovery(@RequestBody Map<String, Integer> requestBody, HttpServletRequest request) {
        
        Integer sleepHours = requestBody.get("sleepHours");
        Integer stressLevel = requestBody.get("stressLevel");
        
        User user = getUser(request);
        
        Map<String, Object> result = loadRecoveryService.assessRecoveryStatus(user.getId(), sleepHours, stressLevel);
        
        return ResponseEntity.ok(ApiResponse.success("恢复状态评估成功", result));
    }

    /**
     * 获取训练建议
     */
    @GetMapping("/training-suggestions")
    @RequireUser
    @Operation(
            summary = "获取训练建议", 
            description = "根据用户的训练历史和恢复状态生成个性化训练建议"
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
                                              "message": "训练建议获取成功",
                                              "data": {
                                                "suggestedExercises": ["深蹲", "硬拉", "卧推"],
                                                "suggestedIntensity": "中等",
                                                "suggestedVolume": 3200,
                                                "restRecommendation": "建议训练后休息48小时",
                                                "nutritionTips": ["增加蛋白质摄入", "保持充足水分"]
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingSuggestions(HttpServletRequest request) {
        User user = getUser(request);
        
        Map<String, Object> suggestions = loadRecoveryService.generateTrainingSuggestions(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("训练建议获取成功", suggestions));
    }

    /**
     * 获取负荷趋势
     */
    @GetMapping("/load-trend")
    @RequireUser
    @Operation(
            summary = "获取负荷趋势", 
            description = "获取指定日期范围内的训练负荷趋势数据"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "负荷趋势示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "负荷趋势获取成功",
                                              "data": {
                                                "2024-01-01": 3200.0,
                                                "2024-01-02": 2800.0,
                                                "2024-01-03": 0.0,
                                                "2024-01-04": 3500.0,
                                                "2024-01-05": 3000.0
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Double>>> getLoadTrend(
            @Parameter(description = "开始日期", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期", required = true, example = "2024-01-07")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        
        // 将LocalDate转换为LocalDateTime，使用一天的开始和结束时间
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        User user = getUser(request);
        
        Map<String, Double> trend = loadRecoveryService.getLoadTrend(user.getId(), startDateTime, endDateTime);
        
        return ResponseEntity.ok(ApiResponse.success("负荷趋势获取成功", trend));
    }

    /**
     * 计算1RM并保存记录
     */
    @PostMapping("/one-rep-max/record")
    @RequireUser
    @Operation(
            summary = "计算1RM并保存记录", 
            description = "根据重量和次数计算1RM（最大重复次数），并保存计算记录"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "计算并保存成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "1RM计算保存成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "1RM记录保存成功",
                                              "data": {
                                                "id": 1,
                                                "weight": 100.0,
                                                "reps": 8,
                                                "oneRepMax": 125.0,
                                                "exerciseType": "STRENGTH",
                                                "exerciseName": "RM_CALCULATION",
                                                "timestamp": "2024-01-01T12:00:00"
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "1RM计算参数",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "1RM计算请求示例",
                            value = """
                                    {
                                      "weight": 100.0,
                                      "reps": 8,
                                      "model": "Epley"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<FitnessData>> saveOneRepMaxRecord(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = getUser(request);
        
        Double weight = Double.valueOf(body.get("weight").toString());
        Integer reps = Integer.valueOf(body.get("reps").toString());
        String model = body.getOrDefault("model", "Epley").toString();
        
        Double oneRepMax = loadRecoveryService.calculateOneRepMax(weight, reps, model);
        
        FitnessData fitnessData = new FitnessData();
        fitnessData.setUser(user);
        fitnessData.setTimestamp(LocalDateTime.now());
        fitnessData.setWeight(weight);
        fitnessData.setReps(reps);
        fitnessData.setOneRepMax(oneRepMax);
        fitnessData.setExerciseType("STRENGTH");
        fitnessData.setExerciseName("RM_CALCULATION");
        
        FitnessData savedData = fitnessDataRepository.save(fitnessData);
        
        return ResponseEntity.ok(ApiResponse.success("1RM记录保存成功", savedData));
    }

    /**
     * 获取我的健身数据
     */
    @GetMapping("/my-data")
    @RequireUser
    @Operation(
            summary = "获取我的健身数据", 
            description = "获取当前用户的所有健身数据记录，按时间倒序排列"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "健身数据列表示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取健身数据成功",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "exerciseName": "深蹲",
                                                  "exerciseType": "STRENGTH",
                                                  "weight": 100.0,
                                                  "reps": 8,
                                                  "sets": 4,
                                                  "trainingLoad": 3200.0,
                                                  "timestamp": "2024-01-01T12:00:00"
                                                },
                                                {
                                                  "id": 2,
                                                  "exerciseName": "卧推",
                                                  "exerciseType": "STRENGTH",
                                                  "weight": 80.0,
                                                  "reps": 10,
                                                  "sets": 3,
                                                  "trainingLoad": 2400.0,
                                                  "timestamp": "2024-01-01T11:00:00"
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
    public ResponseEntity<ApiResponse<List<FitnessData>>> getMyFitnessData(HttpServletRequest request) {
        User user = getUser(request);
        List<FitnessData> dataList = fitnessDataRepository.findByUserOrderByTimestampDesc(user);
        return ResponseEntity.ok(ApiResponse.success("获取健身数据成功", dataList));
    }

    /**
     * 删除健身数据
     */
    @DeleteMapping("/data/{id}")
    @RequireUser
    @Operation(
            summary = "删除健身数据", 
            description = "删除指定ID的健身数据记录，只能删除自己的数据"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "删除成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "删除成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "删除成功",
                                              "data": "删除成功",
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "删除失败",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "数据不存在",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "数据不存在",
                                                      "data": null,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": false
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "无权操作",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "message": "无权操作此数据",
                                                      "data": null,
                                                      "timestamp": "2024-01-01 12:00:00",
                                                      "success": false
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> deleteFitnessData(
            @Parameter(description = "数据ID", required = true, example = "1")
            @PathVariable Long id, 
            HttpServletRequest request) {
        User user = getUser(request);
        FitnessData data = fitnessDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));
        
        if (!data.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此数据");
        }
        
        fitnessDataRepository.delete(data);
        return ResponseEntity.ok(ApiResponse.success("删除成功", "删除成功"));
    }

    /**
     * 计算1RM（支持多种模型）
     */
    @GetMapping("/one-rep-max")
    @RequireUser
    @Operation(
            summary = "计算1RM", 
            description = """
                    根据重量和次数计算1RM（最大重复次数）。
                    
                    支持的计算公式：
                    - **Epley**: 1RM = weight × (1 + reps/30)
                    - **Brzycki**: 1RM = weight × (36 / (37 - reps))
                    - **Lombardi**: 1RM = weight × reps^0.1
                    - **OConner**: 1RM = weight × (1 + reps/40)
                    - **Mayhew**: 1RM = (100 × weight) / (52.2 + 41.9 × e^(-0.055 × reps))
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "计算成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "1RM计算成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "1RM计算成功",
                                              "data": 125.0,
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Double>> calculateOneRepMax(
            @Parameter(description = "重量（kg）", required = false, example = "100.0")
            @RequestParam(required = false) Double weight,
            @Parameter(description = "重复次数", required = false, example = "8")
            @RequestParam(required = false) Integer reps,
            @Parameter(description = "计算公式模型", required = false, example = "Epley", 
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            allowableValues = {"Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"}
                    ))
            @RequestParam(required = false, defaultValue = "Epley") String model,
            HttpServletRequest request) {
        
        // 验证用户身份
        getUserIdFromRequest(request);
        
        if (weight == null || reps == null) {
            return ResponseEntity.ok(ApiResponse.success("参数不足，返回默认值", 0.0));
        }
        
        Double oneRepMax = loadRecoveryService.calculateOneRepMax(weight, reps, model);
        
        return ResponseEntity.ok(ApiResponse.success("1RM计算成功", oneRepMax));
    }
    
    /**
     * 获取所有支持的1RM计算公式模型
     */
    @GetMapping("/one-rep-max/models")
    @RequireUser
    @Operation(
            summary = "获取支持的1RM计算模型", 
            description = "获取系统支持的所有1RM计算公式模型列表"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "支持的模型列表示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取支持的模型成功",
                                              "data": ["Epley", "Brzycki", "Lombardi", "OConner", "Mayhew"],
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<String>>> getSupportedOneRepMaxModels(HttpServletRequest request) {
        
        // 验证用户身份
        getUserIdFromRequest(request);
        
        List<String> models = loadRecoveryService.getSupportedOneRepMaxModels();
        
        return ResponseEntity.ok(ApiResponse.success("获取支持的模型成功", models));
    }
}
