package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.common.PageResponse;
import com.wzl.fitness.entity.StrengthTrainingData;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.StrengthTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 力量训练数据控制器
 */
@RestController
@RequestMapping("/api/v1/strength-training")
@RequiredArgsConstructor
@Tag(name = "力量训练管理", description = "力量训练数据的增删改查接口")
public class StrengthTrainingController extends BaseController {

    private final StrengthTrainingService strengthTrainingService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "获取力量训练数据列表", description = "分页获取当前用户的力量训练数据")
    @RequireUser
    public ResponseEntity<PageResponse<StrengthTrainingData>> getStrengthTrainingData(
            HttpServletRequest request,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<StrengthTrainingData> dataPage = strengthTrainingService.getUserStrengthTrainingData(getUserIdFromRequest(request), pageable);
        return ResponseEntity.ok(pageResponse(dataPage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取力量训练数据详情", description = "根据ID获取力量训练数据详情")
    @RequireUser
    public ResponseEntity<ApiResponse<StrengthTrainingData>> getStrengthTrainingDataById(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id) {
        
        StrengthTrainingData data = strengthTrainingService.getStrengthTrainingDataById(id);
        checkResourceAccess(data.getUser().getId());
        return ResponseEntity.ok(success(data));
    }

    @PostMapping
    @Operation(summary = "添加力量训练数据", description = "创建新的力量训练数据记录")
    @RequireUser
    public ResponseEntity<ApiResponse<StrengthTrainingData>> addStrengthTrainingData(
            HttpServletRequest request,
            @Valid @RequestBody StrengthTrainingData strengthTrainingData) {
        
        strengthTrainingData.setUser(null); // 确保用户ID由服务端设置
        StrengthTrainingData savedData = strengthTrainingService.createStrengthTrainingData(getUserIdFromRequest(request), strengthTrainingData);
        return ResponseEntity.ok(success(savedData));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新力量训练数据", description = "更新指定的力量训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<StrengthTrainingData>> updateStrengthTrainingData(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id,
            @Valid @RequestBody StrengthTrainingData strengthTrainingData) {
        
        StrengthTrainingData existingData = strengthTrainingService.getStrengthTrainingDataById(id);
        checkResourceAccess(existingData.getUser().getId());
        
        StrengthTrainingData updatedData = strengthTrainingService.updateStrengthTrainingData(id, strengthTrainingData);
        return ResponseEntity.ok(success(updatedData));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除力量训练数据", description = "删除指定的力量训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<Void>> deleteStrengthTrainingData(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id) {
        
        StrengthTrainingData existingData = strengthTrainingService.getStrengthTrainingDataById(id);
        checkResourceAccess(existingData.getUser().getId());
        
        Long userId = getUserIdFromRequest(request);
        String username = existingData.getUser().getUsername();
        
        strengthTrainingService.deleteStrengthTrainingData(id);
        
        // 记录数据删除审计日志
        auditLogService.logDataDelete(userId, username, "力量训练数据", id);
        
        return ResponseEntity.ok(success());
    }

    @GetMapping("/date-range")
    @Operation(summary = "按时间范围查询", description = "获取指定时间范围内的力量训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<List<StrengthTrainingData>>> getStrengthTrainingDataByDateRange(
            HttpServletRequest request,
            @Parameter(description = "开始时间") @RequestParam String startDate,
            @Parameter(description = "结束时间") @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        
        List<StrengthTrainingData> dataList = strengthTrainingService.getStrengthTrainingDataByDateRange(
                getUserIdFromRequest(request), start, end);
        return ResponseEntity.ok(success(dataList));
    }

    @GetMapping("/exercise/{exerciseName}")
    @Operation(summary = "按动作名称查询", description = "获取指定动作的力量训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<List<StrengthTrainingData>>> getStrengthTrainingDataByExercise(
            HttpServletRequest request,
            @Parameter(description = "动作名称") @PathVariable String exerciseName) {
        
        List<StrengthTrainingData> dataList = strengthTrainingService.getStrengthTrainingDataByExercise(
                getUserIdFromRequest(request), exerciseName);
        return ResponseEntity.ok(success(dataList));
    }

    @GetMapping("/stats/max-weight")
    @Operation(summary = "获取最大重量统计", description = "获取各动作的最大重量统计")
    @RequireUser
    public ResponseEntity<ApiResponse<Object>> getMaxWeightStats(HttpServletRequest request) {
        Object stats = strengthTrainingService.getMaxWeightStats(getUserIdFromRequest(request));
        return ResponseEntity.ok(success(stats));
    }
}