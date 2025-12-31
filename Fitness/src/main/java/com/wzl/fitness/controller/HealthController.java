package com.wzl.fitness.controller;

import com.wzl.fitness.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "健康检查", description = "系统健康状态检查接口")
public class HealthController {

    /**
     * 系统健康检查
     */
    @GetMapping
    @Operation(summary = "系统健康检查", description = "检查系统是否正常运行")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "Fitness Management System");
        healthInfo.put("version", "1.0.0");
        
        return ApiResponse.success(healthInfo);
    }
}