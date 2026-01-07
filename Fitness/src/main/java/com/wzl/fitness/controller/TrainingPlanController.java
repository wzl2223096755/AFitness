package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.dto.request.TrainingPlanRequestDTO;
import com.wzl.fitness.entity.TrainingPlan;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.TrainingPlanService;
import com.wzl.fitness.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 健身计划控制器
 */
@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "训练计划", description = "训练计划管理，包括计划创建、计划执行等")
public class TrainingPlanController extends BaseController {

    private final TrainingPlanService trainingPlanService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    /**
     * 获取当前登录用户
     */
    private User getUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 获取当前用户的所有计划
     */
    @GetMapping
    @RequireUser
    @Operation(summary = "获取所有训练计划", description = "获取当前用户的所有训练计划")
    public ResponseEntity<ApiResponse<List<TrainingPlan>>> getMyPlans(HttpServletRequest request) {
        User user = getUser(request);
        List<TrainingPlan> plans = trainingPlanService.getPlansByUser(user);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * 分页获取计划
     */
    @GetMapping("/page")
    @RequireUser
    @Operation(summary = "分页获取训练计划", description = "分页获取当前用户的训练计划")
    public ResponseEntity<ApiResponse<Page<TrainingPlan>>> getPlansPage(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User user = getUser(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingPlan> plans = trainingPlanService.getPlansByUser(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * 根据ID获取计划详情
     */
    @GetMapping("/{id}")
    @RequireUser
    @Operation(summary = "获取训练计划详情", description = "根据ID获取训练计划的详细信息")
    public ResponseEntity<ApiResponse<TrainingPlan>> getPlanById(
            @Parameter(description = "计划ID") @PathVariable Long id, 
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TrainingPlan plan = trainingPlanService.getPlanById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        // 权限检查
        if (!plan.getUser().getId().equals(userId)) {
            throw new BusinessException("无权访问此计划");
        }
        
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    /**
     * 创建新计划
     */
    @PostMapping
    @RequireUser
    @Operation(summary = "创建训练计划", description = "创建一个新的训练计划")
    public ResponseEntity<ApiResponse<TrainingPlan>> createPlan(
            @RequestBody TrainingPlanRequestDTO planDto, 
            HttpServletRequest request) {
        User user = getUser(request);
        TrainingPlan savedPlan = trainingPlanService.createPlanFromDto(planDto, user);
        return ResponseEntity.ok(ApiResponse.success("计划创建成功", savedPlan));
    }

    /**
     * 更新计划
     */
    @PutMapping("/{id}")
    @RequireUser
    @Operation(summary = "更新训练计划", description = "更新指定的训练计划")
    public ResponseEntity<ApiResponse<TrainingPlan>> updatePlan(
            @Parameter(description = "计划ID") @PathVariable Long id, 
            @RequestBody TrainingPlan plan, 
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TrainingPlan existingPlan = trainingPlanService.getPlanById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!existingPlan.getUser().getId().equals(userId)) {
            throw new BusinessException("无权更新此计划");
        }
        
        TrainingPlan updatedPlan = trainingPlanService.updatePlan(id, plan);
        return ResponseEntity.ok(ApiResponse.success("计划更新成功", updatedPlan));
    }

    /**
     * 更新计划中动作的完成状态
     */
    @PatchMapping("/exercises/{id}/toggle")
    @RequireUser
    @Operation(summary = "切换动作完成状态", description = "切换训练计划中某个动作的完成状态")
    public ResponseEntity<ApiResponse<Void>> toggleExerciseStatus(
            @Parameter(description = "动作ID") @PathVariable Long id, 
            HttpServletRequest request) {
        // 校验权限
        getUserIdFromRequest(request);
        trainingPlanService.toggleExerciseCompletion(id);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
    }

    /**
     * 按状态获取计划
     */
    @GetMapping("/status/{status}")
    @RequireUser
    @Operation(summary = "按状态获取计划", description = "根据状态筛选训练计划")
    public ResponseEntity<ApiResponse<List<TrainingPlan>>> getPlansByStatus(
            @Parameter(description = "计划状态: active, completed, paused") @PathVariable String status,
            HttpServletRequest request) {
        User user = getUser(request);
        List<TrainingPlan> plans = trainingPlanService.getPlansByStatus(user, status);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * 删除计划
     */
    @DeleteMapping("/{id}")
    @RequireUser
    @Operation(summary = "删除训练计划", description = "删除指定的训练计划")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @Parameter(description = "计划ID") @PathVariable Long id, 
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TrainingPlan plan = trainingPlanService.getPlanById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
        
        if (!plan.getUser().getId().equals(userId)) {
            throw new BusinessException("无权删除此计划");
        }
        
        String username = plan.getUser().getUsername();
        
        trainingPlanService.deletePlan(id);
        
        // 记录数据删除审计日志
        auditLogService.logDataDelete(userId, username, "训练计划", id);
        
        return ResponseEntity.ok(ApiResponse.success("计划删除成功", null));
    }

    /**
     * 保存周计划
     */
    @PostMapping("/weekly")
    @RequireUser
    @Operation(summary = "保存周计划", description = "保存一个周训练计划")
    public ResponseEntity<ApiResponse<TrainingPlan>> saveWeeklyPlan(
            @RequestBody TrainingPlan plan, 
            HttpServletRequest request) {
        User user = getUser(request);
        TrainingPlan savedPlan = trainingPlanService.saveWeeklyPlan(user, plan);
        return ResponseEntity.ok(ApiResponse.success("周计划保存成功", savedPlan));
    }

    /**
     * 按日期范围获取计划
     */
    @GetMapping("/range")
    @RequireUser
    @Operation(summary = "按日期范围获取计划", description = "获取指定日期范围内的训练计划")
    public ResponseEntity<ApiResponse<List<TrainingPlan>>> getPlansByRange(
            @Parameter(description = "开始日期 (yyyy-MM-dd)") @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") @RequestParam String endDate,
            HttpServletRequest request) {
        User user = getUser(request);
        List<TrainingPlan> plans = trainingPlanService.getPlansByDateRange(user, 
                LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
