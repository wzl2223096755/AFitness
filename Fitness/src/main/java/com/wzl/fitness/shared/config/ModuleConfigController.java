package com.wzl.fitness.shared.config;

import com.wzl.fitness.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块配置控制器
 * 提供模块配置信息的REST API
 * 
 * 用于前端获取当前启用的模块列表，实现动态功能显示/隐藏
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 * @see Requirements 7.4 - 自动生成模块化的OpenAPI文档
 */
@RestController
@RequestMapping("/api/v1/modules")
@Tag(name = "模块配置", description = "模块配置管理接口，包括模块状态查询、模块启用/禁用等")
public class ModuleConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(ModuleConfigController.class);
    
    private final ModuleConfig moduleConfig;
    private final ModuleRegistry moduleRegistry;
    
    public ModuleConfigController(ModuleConfig moduleConfig, ModuleRegistry moduleRegistry) {
        this.moduleConfig = moduleConfig;
        this.moduleRegistry = moduleRegistry;
    }
    
    /**
     * 获取所有模块配置信息
     * 
     * @return 模块配置列表
     */
    @GetMapping
    @Operation(
            summary = "获取所有模块配置",
            description = "获取系统中所有模块的配置信息，包括启用状态、依赖关系、加载状态等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "模块配置示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "user": {
                                                  "name": "user",
                                                  "enabled": true,
                                                  "order": 1,
                                                  "description": "用户认证和管理模块",
                                                  "dependencies": [],
                                                  "status": "LOADED"
                                                },
                                                "training": {
                                                  "name": "training",
                                                  "enabled": true,
                                                  "order": 2,
                                                  "description": "训练记录和计划管理模块",
                                                  "dependencies": ["user"],
                                                  "status": "LOADED"
                                                }
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<Map<String, ModuleInfoDTO>> getAllModules() {
        log.debug("获取所有模块配置信息");
        
        Map<String, ModuleInfoDTO> modules = moduleConfig.getAllModules().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    ModuleConfig.ModuleSettings settings = entry.getValue();
                    ModuleRegistry.ModuleStatus status = moduleRegistry.getModuleStatus(entry.getKey());
                    return new ModuleInfoDTO(
                        entry.getKey(),
                        settings.isEnabled(),
                        settings.getOrder(),
                        settings.getDescription(),
                        settings.getDependencies(),
                        status.name()
                    );
                }
            ));
        
        return ApiResponse.success(modules);
    }
    
    /**
     * 获取已启用的模块列表
     * 
     * @return 已启用的模块名称列表
     */
    @GetMapping("/enabled")
    @Operation(
            summary = "获取已启用的模块",
            description = "获取系统中所有模块的启用状态，用于前端动态显示/隐藏功能"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "启用状态示例",
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": {
                                                "user": true,
                                                "training": true,
                                                "nutrition": true,
                                                "recovery": true,
                                                "admin": true
                                              },
                                              "timestamp": "2024-01-01 12:00:00",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<Map<String, Boolean>> getEnabledModules() {
        log.debug("获取已启用的模块列表");
        
        Map<String, Boolean> enabledModules = new HashMap<>();
        moduleConfig.getAllModules().forEach((name, settings) -> {
            enabledModules.put(name, settings.isEnabled());
        });
        
        return ApiResponse.success(enabledModules);
    }
    
    /**
     * 模块信息DTO
     */
    public static class ModuleInfoDTO {
        private String name;
        private boolean enabled;
        private int order;
        private String description;
        private java.util.List<String> dependencies;
        private String status;
        
        public ModuleInfoDTO() {}
        
        public ModuleInfoDTO(String name, boolean enabled, int order, String description, 
                            java.util.List<String> dependencies, String status) {
            this.name = name;
            this.enabled = enabled;
            this.order = order;
            this.description = description;
            this.dependencies = dependencies;
            this.status = status;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public java.util.List<String> getDependencies() { return dependencies; }
        public void setDependencies(java.util.List<String> dependencies) { this.dependencies = dependencies; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
