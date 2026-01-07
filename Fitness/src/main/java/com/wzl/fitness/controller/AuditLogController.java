package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.entity.AuditLog;
import com.wzl.fitness.entity.AuditLog.AuditAction;
import com.wzl.fitness.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审计日志控制器
 * 提供审计日志查询和导出功能
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "审计日志", description = "审计日志管理接口")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * 获取审计日志列表（分页）
     */
    @GetMapping
    @RequireAdmin
    @Operation(summary = "获取审计日志列表", description = "分页查询审计日志，支持按操作类型、用户ID、时间范围筛选")
    public ApiResponse<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("查询审计日志: page={}, size={}, action={}, userId={}", page, size, action, userId);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logs;
        
        if (action != null && !action.isEmpty()) {
            try {
                AuditAction auditAction = AuditAction.valueOf(action.toUpperCase());
                logs = auditLogRepository.findByActionOrderByCreatedAtDesc(auditAction, pageRequest);
            } catch (IllegalArgumentException e) {
                logs = auditLogRepository.findAll(pageRequest);
            }
        } else if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
        } else if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);
            logs = auditLogRepository.findByTimeRange(start, end, pageRequest);
        } else {
            logs = auditLogRepository.findAll(pageRequest);
        }
        
        return ApiResponse.success(logs);
    }

    /**
     * 获取单条审计日志详情
     */
    @GetMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "获取审计日志详情", description = "根据ID获取单条审计日志详情")
    public ApiResponse<AuditLog> getAuditLogById(@PathVariable Long id) {
        log.info("查询审计日志详情: id={}", id);
        
        return auditLogRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "审计日志不存在"));
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    @RequireAdmin
    @Operation(summary = "导出审计日志", description = "导出审计日志数据")
    public ApiResponse<List<AuditLog>> exportAuditLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String action) {
        
        log.info("导出审计日志: startDate={}, endDate={}, action={}", startDate, endDate, action);
        
        List<AuditLog> logs;
        
        if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);
            logs = auditLogRepository.findByTimeRange(start, end, PageRequest.of(0, 10000)).getContent();
        } else {
            logs = auditLogRepository.findAll(PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        }
        
        return ApiResponse.success(logs);
    }
}
