package com.wzl.fitness.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块配置类
 * 用于管理各业务模块的启用/禁用状态和配置
 * 
 * 配置示例（application.properties）：
 * <pre>
 * fitness.modules.user.enabled=true
 * fitness.modules.user.order=1
 * fitness.modules.training.enabled=true
 * fitness.modules.training.order=2
 * fitness.modules.training.dependencies=user
 * fitness.modules.nutrition.enabled=true
 * fitness.modules.nutrition.order=3
 * fitness.modules.recovery.enabled=true
 * fitness.modules.recovery.order=4
 * fitness.modules.recovery.dependencies=user,training
 * fitness.modules.admin.enabled=true
 * fitness.modules.admin.order=5
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "fitness.modules")
public class ModuleConfig {
    
    /**
     * 用户模块配置
     */
    private ModuleSettings user = new ModuleSettings();
    
    /**
     * 训练模块配置
     */
    private ModuleSettings training = new ModuleSettings();
    
    /**
     * 营养模块配置
     */
    private ModuleSettings nutrition = new ModuleSettings();
    
    /**
     * 恢复评估模块配置
     */
    private ModuleSettings recovery = new ModuleSettings();
    
    /**
     * 管理模块配置
     */
    private ModuleSettings admin = new ModuleSettings();
    
    /**
     * 获取所有模块配置
     */
    public Map<String, ModuleSettings> getAllModules() {
        Map<String, ModuleSettings> modules = new HashMap<>();
        modules.put("user", user);
        modules.put("training", training);
        modules.put("nutrition", nutrition);
        modules.put("recovery", recovery);
        modules.put("admin", admin);
        return modules;
    }
    
    /**
     * 检查模块是否启用
     */
    public boolean isModuleEnabled(String moduleName) {
        ModuleSettings settings = getAllModules().get(moduleName);
        return settings != null && settings.isEnabled();
    }
    
    /**
     * 模块设置
     */
    @Data
    public static class ModuleSettings {
        /**
         * 模块是否启用
         */
        private boolean enabled = true;
        
        /**
         * 模块加载顺序
         */
        private int order = 0;
        
        /**
         * 模块依赖列表
         */
        private List<String> dependencies = new ArrayList<>();
        
        /**
         * 模块描述
         */
        private String description = "";
    }
}
