package com.wzl.fitness.controller;

import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 告警管理控制器
 */
@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "告警管理", description = "系统告警监控和管理接口")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    /**
     * 获取当前系统指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取当前系统指标", description = "获取请求统计、错误率、内存使用等指标")
    public ApiResponse<Map<String, Object>> getMetrics() {
        return ApiResponse.success(alertService.getCurrentMetrics());
    }
    
    /**
     * 触发测试告警
     */
    @PostMapping("/test")
    @Operation(summary = "触发测试告警", description = "发送一条测试告警消息")
    public ApiResponse<String> triggerTestAlert() {
        alertService.triggerTestAlert();
        return ApiResponse.success("测试告警已触发");
    }
    
    /**
     * 手动触发告警
     */
    @PostMapping("/trigger")
    @Operation(summary = "手动触发告警", description = "手动触发指定类型的告警")
    public ApiResponse<String> triggerAlert(
            @RequestParam String alertType,
            @RequestParam String message,
            @RequestParam(defaultValue = "WARNING") String level) {
        
        AlertService.AlertLevel alertLevel;
        try {
            alertLevel = AlertService.AlertLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            alertLevel = AlertService.AlertLevel.WARNING;
        }
        
        alertService.triggerAlert(alertType, message, alertLevel);
        return ApiResponse.success("告警已触发: " + alertType);
    }
    
    /**
     * 解除告警
     */
    @PostMapping("/resolve/{alertType}")
    @Operation(summary = "解除告警", description = "手动解除指定类型的告警")
    public ApiResponse<String> resolveAlert(@PathVariable String alertType) {
        alertService.resolveAlert(alertType);
        return ApiResponse.success("告警已解除: " + alertType);
    }
}
