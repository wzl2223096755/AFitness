package com.wzl.fitness.controller;

import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异步任务监控控制器
 * 
 * 提供异步任务执行状态和线程池状态的查询接口
 */
@RestController
@RequestMapping("/api/v1/monitor/async")
@RequiredArgsConstructor
@Tag(name = "异步任务监控", description = "异步任务执行状态和线程池监控接口")
public class AsyncTaskMonitorController {
    
    private final AsyncTaskService asyncTaskService;
    
    /**
     * 获取线程池状态
     */
    @GetMapping("/thread-pool/status")
    @Operation(summary = "获取线程池状态", description = "返回异步任务线程池的当前状态信息")
    public ApiResponse<AsyncTaskService.ThreadPoolStatus> getThreadPoolStatus() {
        return ApiResponse.success(asyncTaskService.getThreadPoolStatus());
    }
}
