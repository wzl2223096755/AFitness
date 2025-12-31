package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.common.PageResponse;
import com.wzl.fitness.entity.CardioTrainingData;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.CardioTrainingService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 有氧训练数据控制器
 */
@RestController
@RequestMapping("/api/v1/cardio-training")
@RequiredArgsConstructor
@Tag(name = "有氧训练管理", description = "有氧训练数据的增删改查接口")
public class CardioTrainingController extends BaseController {

    private final CardioTrainingService cardioTrainingService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "获取有氧训练数据列表", description = "分页获取当前用户的有氧训练数据")
    @RequireUser
    public ResponseEntity<PageResponse<CardioTrainingData>> getCardioTrainingData(
            HttpServletRequest request,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CardioTrainingData> dataPage = cardioTrainingService.getUserCardioTrainingData(getUserIdFromRequest(request), pageable);
        return ResponseEntity.ok(pageResponse(dataPage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取有氧训练数据详情", description = "根据ID获取有氧训练数据详情")
    @RequireUser
    public ResponseEntity<ApiResponse<CardioTrainingData>> getCardioTrainingDataById(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id) {
        
        CardioTrainingData data = cardioTrainingService.getCardioTrainingDataById(id);
        checkResourceAccess(data.getUser().getId());
        return ResponseEntity.ok(success(data));
    }

    @PostMapping
    @Operation(summary = "添加有氧训练数据", description = "创建新的有氧训练数据记录")
    @RequireUser
    public ResponseEntity<ApiResponse<CardioTrainingData>> addCardioTrainingData(
            HttpServletRequest request,
            @Valid @RequestBody CardioTrainingData cardioTrainingData) {
        
        cardioTrainingData.setUser(null); // 确保用户ID由服务端设置
        CardioTrainingData savedData = cardioTrainingService.createCardioTrainingData(getUserIdFromRequest(request), cardioTrainingData);
        return ResponseEntity.ok(success(savedData));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新有氧训练数据", description = "更新指定的有氧训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<CardioTrainingData>> updateCardioTrainingData(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id,
            @Valid @RequestBody CardioTrainingData cardioTrainingData) {
        
        CardioTrainingData existingData = cardioTrainingService.getCardioTrainingDataById(id);
        checkResourceAccess(existingData.getUser().getId());
        
        CardioTrainingData updatedData = cardioTrainingService.updateCardioTrainingData(id, cardioTrainingData);
        return ResponseEntity.ok(success(updatedData));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除有氧训练数据", description = "删除指定的有氧训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<Void>> deleteCardioTrainingData(
            HttpServletRequest request,
            @Parameter(description = "数据ID") @PathVariable Long id) {
        
        CardioTrainingData existingData = cardioTrainingService.getCardioTrainingDataById(id);
        checkResourceAccess(existingData.getUser().getId());
        
        Long userId = getUserIdFromRequest(request);
        String username = existingData.getUser().getUsername();
        
        cardioTrainingService.deleteCardioTrainingData(id);
        
        // 记录数据删除审计日志
        auditLogService.logDataDelete(userId, username, "有氧训练数据", id);
        
        return ResponseEntity.ok(success());
    }

    @GetMapping("/date-range")
    @Operation(summary = "按时间范围查询", description = "获取当前用户指定时间范围内的有氧训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<List<CardioTrainingData>>> getCardioTrainingDataByDateRange(
            HttpServletRequest request,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<CardioTrainingData> data = cardioTrainingService.getCardioTrainingDataByDateRange(getUserIdFromRequest(request), startDate, endDate);
        return ResponseEntity.ok(success(data));
    }

    @GetMapping("/exercise-type/{exerciseType}")
    @Operation(summary = "按运动类型查询", description = "获取当前用户指定运动类型的有氧训练数据")
    @RequireUser
    public ResponseEntity<ApiResponse<List<CardioTrainingData>>> getCardioTrainingDataByExerciseType(
            HttpServletRequest request,
            @Parameter(description = "运动类型") @PathVariable String exerciseType) {
        
        List<CardioTrainingData> data = cardioTrainingService.getCardioTrainingDataByExerciseType(getUserIdFromRequest(request), exerciseType);
        return ResponseEntity.ok(success(data));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取有氧训练统计", description = "获取当前用户指定时间范围内的有氧训练统计数据")
    @RequireUser
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCardioStats(
            HttpServletRequest request,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> stats = cardioTrainingService.getCardioStats(getUserIdFromRequest(request), startDate, endDate);
        return ResponseEntity.ok(success(stats));
    }
}
