package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.dto.response.ExerciseDetailDTO;
import com.wzl.fitness.dto.response.PageResponse;
import com.wzl.fitness.dto.response.RecoveryMetricDTO;
import com.wzl.fitness.dto.response.TrainingRecordDTO;
import com.wzl.fitness.dto.response.TrainingRecordResponse;
import com.wzl.fitness.dto.request.TrainingRecordRequest;
import com.wzl.fitness.dto.request.ExerciseDetailRequest;
import com.wzl.fitness.entity.ExerciseDetail;
import com.wzl.fitness.entity.RecoveryMetric;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.modules.training.event.TrainingCompletedEvent;
import com.wzl.fitness.repository.RecoveryMetricRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.LoadRecoveryService;
import com.wzl.fitness.service.UserService;
import com.wzl.fitness.shared.event.EventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练管理控制器
 * 
 * @see Requirements 3.2 - 用户完成训练记录时发布TrainingCompletedEvent事件
 */
@RestController
@RequestMapping("/api/v1/training")
@Tag(name = "训练管理", description = "训练记录和恢复指标管理，包括训练数据CRUD、训练分析等")
public class TrainingController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    @Autowired
    private RecoveryMetricRepository recoveryMetricRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LoadRecoveryService loadRecoveryService;
    
    @Autowired
    private EventPublisher eventPublisher;

    /**
     * 获取当前登录用户
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 将TrainingRecord转换为TrainingRecordResponse
     */
    private TrainingRecordResponse convertToResponse(TrainingRecord record) {
        TrainingRecordResponse response = new TrainingRecordResponse();
        response.setId(record.getId());
        response.setExerciseName(record.getExerciseName());
        response.setSets(record.getSets());
        response.setReps(record.getReps());
        response.setWeight(record.getWeight());
        response.setTrainingDate(record.getTrainingDate());
        response.setDuration(record.getDuration());
        response.setNotes(record.getNotes());
        response.setTotalVolume(record.getTotalVolume());
        response.setTrainingStress(record.getTrainingStress());
        response.setCalculatedTotalVolume(record.getCalculatedTotalVolume());
        response.setCaloriesBurned(record.getCaloriesBurned());
        response.setCreatedAt(record.getCreatedAt());
        response.setExerciseDetails(record.getExerciseDetails());
        return response;
    }

    /**
     * 创建训练记录
     */
    @PostMapping("/record")
    @RequireUser
    @Operation(
            summary = "创建训练记录", 
            description = "创建新的训练记录，包含训练动作详情。系统会自动计算训练量和训练压力。"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "创建成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "创建训练记录成功示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "训练记录创建成功",
                                              "data": {
                                                "id": 1,
                                                "exerciseName": "深蹲",
                                                "sets": 4,
                                                "reps": 8,
                                                "weight": 100.0,
                                                "trainingDate": "2024-01-01",
                                                "totalVolume": 5600.0,
                                                "trainingStress": 75.0,
                                                "calculatedTotalVolume": 5600.0,
                                                "createdAt": "2024-01-01T12:00:00",
                                                "exerciseDetails": [...]
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
            description = "训练记录请求参数",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "训练记录请求示例",
                            value = """
                                    {
                                      "trainingDate": "2024-01-01",
                                      "totalVolume": 5600,
                                      "trainingStress": 75,
                                      "exerciseDetails": [
                                        {
                                          "exerciseName": "深蹲",
                                          "weight": 100.0,
                                          "sets": 4,
                                          "reps": 8,
                                          "rpe": 8,
                                          "exerciseType": "COMPOUND"
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<TrainingRecordResponse>> createTrainingRecord(
            @Valid @RequestBody TrainingRecordRequest trainingRecordRequest,
            HttpServletRequest request) {
        
        User user = getUser(request);
        
        // 创建训练记录实体
        TrainingRecord record = new TrainingRecord();
        record.setUser(user);
        record.setTrainingDate(trainingRecordRequest.getTrainingDate());
        record.setTotalVolume(trainingRecordRequest.getTotalVolume());
        record.setTrainingStress(trainingRecordRequest.getTrainingStress());
        
        // 如果有训练动作详情，从第一个动作设置主要信息
        if (!trainingRecordRequest.getExerciseDetails().isEmpty()) {
            ExerciseDetailRequest firstDetail = trainingRecordRequest.getExerciseDetails().get(0);
            record.setExerciseName(firstDetail.getExerciseName());
            record.setWeight(firstDetail.getWeight());
            record.setSets(firstDetail.getSets());
            record.setReps(firstDetail.getReps());
        }
        
        // 设置默认时长（如果没有提供的话）
        record.setDuration(45); // 默认45分钟
        
        // 创建训练动作详情
        List<ExerciseDetail> details = new ArrayList<>();
        for (ExerciseDetailRequest detailRequest : trainingRecordRequest.getExerciseDetails()) {
            ExerciseDetail detail = new ExerciseDetail();
            detail.setTrainingRecord(record);
            detail.setExerciseName(detailRequest.getExerciseName());
            detail.setWeight(detailRequest.getWeight());
            detail.setSets(detailRequest.getSets());
            detail.setReps(detailRequest.getReps());
            detail.setRpe(detailRequest.getRpe());
            detail.setExerciseType(detailRequest.getExerciseType());
            // 计算训练量
            if (detailRequest.getWeight() != null && detailRequest.getReps() != null && detailRequest.getSets() != null) {
                detail.setVolume(detailRequest.getWeight() * detailRequest.getReps() * detailRequest.getSets());
            }
            details.add(detail);
        }
        record.setExerciseDetails(details);
        
        // 计算训练量和训练压力
        TrainingRecord savedRecord = loadRecoveryService.calculateTrainingSummary(record);
        // 保存训练记录
        savedRecord = trainingRecordRepository.save(savedRecord);
        
        // 发布训练完成事件
        publishTrainingCompletedEvent(savedRecord);
        
        // 转换为响应DTO
        TrainingRecordResponse responseDto = convertToResponse(savedRecord);
        
        return ResponseEntity.ok(ApiResponse.success("训练记录创建成功", responseDto));
    }
    
    /**
     * 发布训练完成事件
     * 
     * @param record 保存的训练记录
     * @see Requirements 3.2 - 用户完成训练记录时发布TrainingCompletedEvent事件
     */
    private void publishTrainingCompletedEvent(TrainingRecord record) {
        if (record == null || record.getUser() == null) {
            logger.warn("无法发布训练完成事件：记录或用户为空");
            return;
        }
        
        try {
            TrainingCompletedEvent event = new TrainingCompletedEvent(
                    record.getUser().getId(),
                    record.getId(),
                    record.getTotalVolume() != null ? record.getTotalVolume() : record.getCalculatedTotalVolume(),
                    record.getExerciseName(),
                    record.getTrainingStress(),
                    record.getDuration()
            );
            
            eventPublisher.publish(event);
            logger.info("训练完成事件已发布: recordId={}, userId={}", record.getId(), record.getUser().getId());
        } catch (Exception e) {
            logger.error("发布训练完成事件失败: recordId={}, error={}", record.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 获取当前用户的训练记录
     */
    @GetMapping("/records")
    @RequireUser
    @Operation(
            summary = "获取当前用户的训练记录", 
            description = "获取当前登录用户的所有训练记录，按训练日期倒序排列"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "训练记录列表示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "获取训练记录成功",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "exerciseName": "深蹲",
                                                  "sets": 4,
                                                  "reps": 8,
                                                  "weight": 100.0,
                                                  "trainingDate": "2024-01-01",
                                                  "totalVolume": 3200.0,
                                                  "createdAt": "2024-01-01T12:00:00"
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
    public ResponseEntity<ApiResponse<List<TrainingRecord>>> getMyTrainingRecords(HttpServletRequest request) {
        
        User user = getUser(request);
        
        List<TrainingRecord> records = trainingRecordRepository.findByUserOrderByTrainingDateDesc(user);
        
        return ResponseEntity.ok(ApiResponse.success("获取训练记录成功", records));
    }
    
    /**
     * 获取当前用户的训练记录（分页）
     */
    @GetMapping("/records/page")
    @RequireUser
    @Operation(
            summary = "获取当前用户的训练记录（分页）", 
            description = "获取当前登录用户的训练记录，支持分页和排序"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "分页训练记录示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "content": [...],
                                                "page": 0,
                                                "size": 10,
                                                "totalElements": 25,
                                                "totalPages": 3
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<TrainingRecordResponse>>> getMyTrainingRecordsPaged(
            @Parameter(description = "页码（从0开始）") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页大小") 
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "排序字段") 
            @RequestParam(defaultValue = "trainingDate") String sortBy,
            @Parameter(description = "排序方向 (asc/desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest request) {
        
        User user = getUser(request);
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<TrainingRecord> recordPage = trainingRecordRepository.findByUser(user, pageRequest);
        
        // 转换为响应DTO
        List<TrainingRecordResponse> responseList = recordPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        PageResponse<TrainingRecordResponse> pageResponse = PageResponse.of(
                responseList, page, size, recordPage.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * 获取指定用户的训练记录（仅限管理员或本人，此处简化为本人）
     */
    @GetMapping("/records/{userId}")
    @RequireUser
    @Operation(summary = "获取指定用户训练记录", description = "根据用户ID获取所有训练记录")
    public ResponseEntity<ApiResponse<List<TrainingRecord>>> getUserTrainingRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            HttpServletRequest request) {
        
        Long currentUserId = (Long) request.getAttribute("userId");
        if (!userId.equals(currentUserId)) {
            throw new BusinessException("无权查看他人训练记录");
        }
        
        User user = getUser(request);
        
        List<TrainingRecord> records = trainingRecordRepository.findByUserOrderByTrainingDateDesc(user);
        
        return ResponseEntity.ok(ApiResponse.success("获取训练记录成功", records));
    }

    /**
     * 提交恢复指标
     */
    @PostMapping("/recovery")
    @RequireUser
    @Operation(summary = "提交恢复指标", description = "提交用户的恢复指标数据")
    public ResponseEntity<ApiResponse<RecoveryMetric>> submitRecoveryMetric(
            @Valid @RequestBody RecoveryMetricDTO metricDTO,
            HttpServletRequest request) {
        
        User user = getUser(request);
        
        // 创建恢复指标实体
        RecoveryMetric metric = new RecoveryMetric();
        metric.setUser(user);
        metric.setRecordDate(metricDTO.getRecordDate());
        metric.setMuscleSoreness(metricDTO.getMuscleSoreness());
        metric.setSleepQuality(metricDTO.getSleepQuality());
        metric.setRestingHeartRate(metricDTO.getRestingHeartRate());
        metric.setSubjectiveEnergy(metricDTO.getSubjectiveEnergy());
        
        RecoveryMetric savedMetric = recoveryMetricRepository.save(metric);
        
        return ResponseEntity.ok(ApiResponse.success("恢复指标提交成功", savedMetric));
    }

    /**
     * 获取训练分析
     */
    @GetMapping("/analysis/{userId}")
    @Operation(summary = "获取训练分析", description = "根据用户ID获取训练分析数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingAnalysis(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取训练数据
        List<TrainingRecord> records = trainingRecordRepository.findByUserOrderByTrainingDateDesc(user);
        
        // 计算相关指标
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalRecords", records.size());
        analysis.put("totalVolume", records.stream()
                .mapToDouble(record -> record.getTotalVolume() != null ? record.getTotalVolume() : 0.0)
                .sum());
        analysis.put("averageStress", records.stream()
                .mapToDouble(record -> record.getTrainingStress() != null ? record.getTrainingStress() : 0.0)
                .average()
                .orElse(0.0));
        
        // 获取恢复数据
        List<RecoveryMetric> recoveryMetrics = recoveryMetricRepository.findByUserOrderByRecordDateDesc(user);
        analysis.put("recoveryData", recoveryMetrics);
        
        return ResponseEntity.ok(ApiResponse.success("获取训练分析成功", analysis));
    }
}
