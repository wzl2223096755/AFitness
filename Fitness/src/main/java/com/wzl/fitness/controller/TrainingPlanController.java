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
    public ResponseEntity<ApiResponse<Page<TrainingPlan>>> getPlansPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
    public ResponseEntity<ApiResponse<TrainingPlan>> getPlanById(@PathVariable Long id, HttpServletRequest request) {
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
    public ResponseEntity<ApiResponse<TrainingPlan>> createPlan(@RequestBody TrainingPlanRequestDTO planDto, HttpServletRequest request) {
        User user = getUser(request);
        TrainingPlan savedPlan = trainingPlanService.createPlanFromDto(planDto, user);
        return ResponseEntity.ok(ApiResponse.success("计划创建成功", savedPlan));
    }

    /**
     * 更新计划
     */
    @PutMapping("/{id}")
    @RequireUser
    public ResponseEntity<ApiResponse<TrainingPlan>> updatePlan(@PathVariable Long id, @RequestBody TrainingPlan plan, HttpServletRequest request) {
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
    public ResponseEntity<ApiResponse<Void>> toggleExerciseStatus(@PathVariable Long id, HttpServletRequest request) {
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
    public ResponseEntity<ApiResponse<List<TrainingPlan>>> getPlansByStatus(
            @PathVariable String status,
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
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long id, HttpServletRequest request) {
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
    public ResponseEntity<ApiResponse<TrainingPlan>> saveWeeklyPlan(@RequestBody TrainingPlan plan, HttpServletRequest request) {
        User user = getUser(request);
        TrainingPlan savedPlan = trainingPlanService.saveWeeklyPlan(user, plan);
        return ResponseEntity.ok(ApiResponse.success("周计划保存成功", savedPlan));
    }

    /**
     * 按日期范围获取计划
     */
    @GetMapping("/range")
    @RequireUser
    public ResponseEntity<ApiResponse<List<TrainingPlan>>> getPlansByRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest request) {
        User user = getUser(request);
        List<TrainingPlan> plans = trainingPlanService.getPlansByDateRange(user, 
                LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
