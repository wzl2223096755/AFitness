package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.dto.request.NutritionGoalRequest;
import com.wzl.fitness.dto.request.NutritionRecordRequest;
import com.wzl.fitness.dto.response.*;
import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.entity.UserNutritionGoal;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.NutritionCalculationService;
import com.wzl.fitness.service.NutritionService;
import com.wzl.fitness.service.UserNutritionGoalService;
import com.wzl.fitness.util.JwtUtil;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 营养记录控制器
 * 
 * @author 系统管理员
 */
@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
@Tag(name = "营养记录管理", description = "营养记录相关接口")
public class NutritionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);
    private final NutritionService nutritionService;
    private final NutritionCalculationService nutritionCalculationService;
    private final UserNutritionGoalService userNutritionGoalService;
    private final JwtUtil jwtUtil;
    private final com.wzl.fitness.service.UserService userService;
    private final AuditLogService auditLogService;

    /**
     * 从Request中获取用户信息
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 获取指定日期的营养记录列表
     */
    @GetMapping("/records/{date}")
    @RequireUser
    @Operation(summary = "获取指定日期的营养记录", description = "根据日期获取用户当天的所有营养记录")
    public ResponseEntity<ApiResponse<List<NutritionRecordDTO>>> getNutritionRecordsByDate(
            @Parameter(description = "日期，格式：yyyy-MM-dd") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            List<NutritionRecordDTO> records = nutritionService.getNutritionRecordsByDate(user, date);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            logger.error("获取营养记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养记录失败: " + e.getMessage()));
        }
    }

    /**
     * 添加营养记录
     */
    @PostMapping("/records")
    @RequireUser
    @Operation(summary = "添加营养记录", description = "添加一条新的营养记录")
    public ResponseEntity<ApiResponse<NutritionRecordDTO>> addNutritionRecord(
            @Valid @RequestBody NutritionRecordRequest recordDTO,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            NutritionRecord record = nutritionService.addNutritionRecord(user, recordDTO);
            NutritionRecordDTO responseDTO = convertToDTO(record);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
        } catch (Exception e) {
            logger.error("添加营养记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加营养记录失败: " + e.getMessage()));
        }
    }

    /**
     * 更新营养记录
     */
    @PutMapping("/records/{id}")
    @RequireUser
    @Operation(summary = "更新营养记录", description = "更新指定的营养记录")
    public ResponseEntity<ApiResponse<NutritionRecordDTO>> updateNutritionRecord(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Valid @RequestBody NutritionRecordRequest recordDTO,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            NutritionRecord record = nutritionService.updateNutritionRecord(id, recordDTO);
            NutritionRecordDTO responseDTO = convertToDTO(record);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (Exception e) {
            logger.error("更新营养记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新营养记录失败: " + e.getMessage()));
        }
    }

    /**
     * 删除营养记录
     */
    @DeleteMapping("/records/{id}")
    @RequireUser
    @Operation(summary = "删除营养记录", description = "删除指定的营养记录")
    public ResponseEntity<ApiResponse<Void>> deleteNutritionRecord(
            @Parameter(description = "记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            nutritionService.deleteNutritionRecord(id, user);
            
            // 记录数据删除审计日志
            auditLogService.logDataDelete(user.getId(), user.getUsername(), "营养记录", id);
            
            return ResponseEntity.ok(ApiResponse.success("记录删除成功", null));
        } catch (Exception e) {
            logger.error("删除营养记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除营养记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定日期的营养统计
     */
    @GetMapping("/stats/{date}")
    @RequireUser
    @Operation(summary = "获取营养统计", description = "获取指定日期的营养摄入统计数据")
    public ResponseEntity<ApiResponse<NutritionStatsResponse>> getNutritionStatsByDate(
            @Parameter(description = "日期，格式：yyyy-MM-dd") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            NutritionStatsResponse stats = nutritionService.getNutritionStatsByDate(user, date);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            logger.error("获取营养统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定日期范围的营养统计
     */
    @GetMapping("/stats/range")
    @RequireUser
    @Operation(summary = "获取日期范围内的营养统计", description = "获取指定日期范围内的每日营养统计数据列表")
    public ResponseEntity<ApiResponse<List<NutritionStatsResponse>>> getNutritionStatsByDateRange(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            List<NutritionStatsResponse> statsList = nutritionService.getNutritionStatsByDateRange(user, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(statsList));
        } catch (Exception e) {
            logger.error("获取范围统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取范围统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取营养建议
     */
    @GetMapping("/advice")
    @RequireUser
    @Operation(summary = "获取营养建议", description = "根据用户信息和健身目标获取个性化营养建议")
    public ResponseEntity<ApiResponse<List<String>>> getNutritionAdvice(
            @Parameter(description = "日期，格式：yyyy-MM-dd，不指定则默认为今天") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            if (date == null) {
                date = LocalDate.now();
            }
            List<String> advice = nutritionService.getNutritionAdvice(user, date);
            return ResponseEntity.ok(ApiResponse.success(advice));
        } catch (Exception e) {
            logger.error("获取营养建议失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养建议失败: " + e.getMessage()));
        }
    }

    /**
     * 导出营养数据
     */
    @GetMapping("/export")
    @RequireUser
    @Operation(summary = "导出营养数据", description = "导出指定日期范围的营养数据为Excel文件")
    public ResponseEntity<byte[]> exportNutritionData(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            byte[] excelData = nutritionService.exportNutritionData(user, startDate, endDate);
            
            String filename = "nutrition_data_" + startDate + "_to_" + endDate + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);
        } catch (Exception e) {
            logger.error("导出营养数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 将NutritionRecord实体转换为NutritionRecordDTO
     */
    private NutritionRecordDTO convertToDTO(NutritionRecord record) {
        NutritionRecordDTO dto = new NutritionRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUser().getId());
        dto.setRecordDate(record.getRecordDate());
        dto.setMealType(record.getMealType());
        dto.setFoodName(record.getFoodName());
        dto.setCalories(record.getCalories());
        dto.setProtein(record.getProtein());
        dto.setCarbs(record.getCarbs());
        dto.setFat(record.getFat());
        dto.setFiber(record.getFiber());
        dto.setSugar(record.getSugar());
        dto.setSodium(record.getSodium());
        dto.setAmount(record.getAmount());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
    
    // ==================== 营养目标相关API ====================
    
    /**
     * 获取用户营养目标
     */
    @GetMapping("/goals")
    @RequireUser
    @Operation(summary = "获取营养目标", description = "获取用户的营养目标设置")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> getNutritionGoals(HttpServletRequest request) {
        try {
            User user = getUser(request);
            return userNutritionGoalService.getUserNutritionGoal(user)
                    .map(goal -> ResponseEntity.ok(ApiResponse.success(goal)))
                    .orElse(ResponseEntity.ok(ApiResponse.success("未设置营养目标", null)));
        } catch (Exception e) {
            logger.error("获取营养目标失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养目标失败: " + e.getMessage()));
        }
    }
    
    /**
     * 设置用户营养目标
     */
    @PostMapping("/goals")
    @RequireUser
    @Operation(summary = "设置营养目标", description = "设置或更新用户的营养目标")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> setNutritionGoals(
            @Valid @RequestBody NutritionGoalRequest goalRequest,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            UserNutritionGoal goal = userNutritionGoalService.setUserNutritionGoal(user, goalRequest);
            return ResponseEntity.ok(ApiResponse.success(goal));
        } catch (Exception e) {
            logger.error("设置营养目标失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "设置营养目标失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取推荐营养摄入
     */
    @GetMapping("/recommendation")
    @RequireUser
    @Operation(summary = "获取推荐营养摄入", description = "根据用户信息和目标计算推荐的每日营养摄入")
    public ResponseEntity<ApiResponse<NutritionRecommendation>> getNutritionRecommendation(
            @Parameter(description = "训练目标: fat_loss, muscle_gain, maintenance") 
            @RequestParam(required = false) String trainingGoal,
            @Parameter(description = "活动水平: sedentary, light, moderate, active, very_active") 
            @RequestParam(required = false) String activityLevel,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            NutritionRecommendation recommendation;
            
            if (trainingGoal != null || activityLevel != null) {
                // 使用指定参数计算
                recommendation = nutritionCalculationService.calculateRecommendedIntake(
                        user, 
                        trainingGoal != null ? trainingGoal : "maintenance",
                        activityLevel != null ? activityLevel : "moderate");
            } else {
                // 使用用户已保存的目标设置
                recommendation = userNutritionGoalService.getEffectiveRecommendation(user);
            }
            
            return ResponseEntity.ok(ApiResponse.success(recommendation));
        } catch (Exception e) {
            logger.error("获取推荐营养摄入失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取推荐营养摄入失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取营养摄入趋势
     */
    @GetMapping("/trend")
    @RequireUser
    @Operation(summary = "获取营养摄入趋势", description = "获取指定日期范围内的营养摄入趋势分析")
    public ResponseEntity<ApiResponse<NutritionTrend>> getNutritionTrend(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            NutritionTrend trend = nutritionCalculationService.calculateNutritionTrend(user, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(trend));
        } catch (Exception e) {
            logger.error("获取营养趋势失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养趋势失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取营养状态检查
     */
    @GetMapping("/status")
    @RequireUser
    @Operation(summary = "获取营养状态", description = "检查指定日期的营养摄入是否达标")
    public ResponseEntity<ApiResponse<NutritionStatus>> getNutritionStatus(
            @Parameter(description = "日期，格式：yyyy-MM-dd，不指定则默认为今天") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            if (date == null) {
                date = LocalDate.now();
            }
            NutritionStatus status = nutritionCalculationService.checkNutritionStatus(user, date);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            logger.error("获取营养状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取营养状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取个性化营养建议
     */
    @GetMapping("/personalized-advice")
    @RequireUser
    @Operation(summary = "获取个性化营养建议", description = "根据用户目标和当日摄入生成个性化建议")
    public ResponseEntity<ApiResponse<List<String>>> getPersonalizedAdvice(
            @Parameter(description = "日期，格式：yyyy-MM-dd，不指定则默认为今天") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            User user = getUser(request);
            if (date == null) {
                date = LocalDate.now();
            }
            List<String> advice = nutritionCalculationService.generatePersonalizedAdvice(user, date);
            return ResponseEntity.ok(ApiResponse.success(advice));
        } catch (Exception e) {
            logger.error("获取个性化建议失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取个性化建议失败: " + e.getMessage()));
        }
    }
}
