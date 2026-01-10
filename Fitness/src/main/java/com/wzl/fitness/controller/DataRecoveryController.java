package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.TrainingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据恢复控制器
 * 提供已删除数据的查看和恢复功能
 * 
 * 注意：路径从 /api/v1/recovery 改为 /api/v1/data-recovery
 * 以避免与恢复评估模块 RecoveryModuleController 的路由冲突
 */
@RestController
@RequestMapping("/api/v1/data-recovery")
@RequiredArgsConstructor
@Tag(name = "数据恢复", description = "已删除数据的查看和恢复接口")
public class DataRecoveryController extends BaseController {

    private final TrainingRecordService trainingRecordService;
    private final AuditLogService auditLogService;

    @GetMapping("/training-records")
    @Operation(summary = "获取已删除的训练记录", description = "获取当前用户已删除的训练记录列表")
    @RequireUser
    public ResponseEntity<ApiResponse<List<TrainingRecord>>> getDeletedTrainingRecords(
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<TrainingRecord> deletedRecords = trainingRecordService.findDeletedByUserId(userId);
        return ResponseEntity.ok(success(deletedRecords));
    }

    @GetMapping("/training-records/count")
    @Operation(summary = "获取已删除训练记录数量", description = "获取当前用户已删除的训练记录数量")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDeletedTrainingRecordsCount(
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        long count = trainingRecordService.countDeletedByUserId(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(success(result));
    }

    @PostMapping("/training-records/{id}/restore")
    @Operation(summary = "恢复训练记录", description = "恢复指定的已删除训练记录")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Object>>> restoreTrainingRecord(
            HttpServletRequest request,
            @Parameter(description = "记录ID") @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        
        boolean restored = trainingRecordService.restoreTrainingRecord(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("restored", restored);
        result.put("recordId", id);
        
        if (restored) {
            // 记录恢复操作审计日志
            auditLogService.logDataRestore(userId, getCurrentUsername(), "训练记录", id);
            return ResponseEntity.ok(success(result));
        } else {
            return ResponseEntity.ok(ApiResponse.error(404, "记录不存在或未被删除"));
        }
    }

    @PostMapping("/training-records/restore-all")
    @Operation(summary = "批量恢复训练记录", description = "恢复当前用户所有已删除的训练记录")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Object>>> restoreAllTrainingRecords(
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        int restoredCount = trainingRecordService.restoreAllByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("restoredCount", restoredCount);
        
        if (restoredCount > 0) {
            // 记录批量恢复操作审计日志
            auditLogService.logBatchDataRestore(userId, getCurrentUsername(), "训练记录", restoredCount);
        }
        
        return ResponseEntity.ok(success(result));
    }
}
