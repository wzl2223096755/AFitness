package com.wzl.fitness.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模块注册中心
 * 负责模块的注册、发现和生命周期管理
 * 
 * 功能：
 * - 模块注册和注销
 * - 模块状态查询
 * - 模块依赖检查
 * - 循环依赖检测
 */
@Component
public class ModuleRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);
    
    private final ModuleConfig moduleConfig;
    
    /**
     * 已注册的模块信息
     */
    private final Map<String, ModuleInfo> registeredModules = new ConcurrentHashMap<>();
    
    /**
     * 模块加载状态
     */
    private final Map<String, ModuleStatus> moduleStatuses = new ConcurrentHashMap<>();
    
    public ModuleRegistry(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }
    
    @PostConstruct
    public void init() {
        logger.info("初始化模块注册中心...");
        
        // 根据配置初始化模块状态
        moduleConfig.getAllModules().forEach((name, settings) -> {
            ModuleStatus status = settings.isEnabled() ? ModuleStatus.ENABLED : ModuleStatus.DISABLED;
            moduleStatuses.put(name, status);
            logger.info("模块 [{}] 状态: {}", name, status);
        });
        
        // 检查模块依赖
        checkDependencies();
        
        logger.info("模块注册中心初始化完成，共 {} 个模块", moduleStatuses.size());
    }
    
    /**
     * 注册模块
     * 
     * @param moduleName 模块名称
     * @param moduleInfo 模块信息
     */
    public void register(String moduleName, ModuleInfo moduleInfo) {
        if (moduleName == null || moduleName.isBlank()) {
            throw new IllegalArgumentException("模块名称不能为空");
        }
        
        if (registeredModules.containsKey(moduleName)) {
            logger.warn("模块 [{}] 已注册，将被覆盖", moduleName);
        }
        
        registeredModules.put(moduleName, moduleInfo);
        moduleStatuses.putIfAbsent(moduleName, ModuleStatus.ENABLED);
        
        logger.info("模块 [{}] 注册成功: {}", moduleName, moduleInfo.getDescription());
    }
    
    /**
     * 注销模块
     * 
     * @param moduleName 模块名称
     */
    public void unregister(String moduleName) {
        if (registeredModules.remove(moduleName) != null) {
            moduleStatuses.remove(moduleName);
            logger.info("模块 [{}] 已注销", moduleName);
        }
    }
    
    /**
     * 检查模块是否已注册
     */
    public boolean isRegistered(String moduleName) {
        return registeredModules.containsKey(moduleName);
    }
    
    /**
     * 检查模块是否启用
     */
    public boolean isEnabled(String moduleName) {
        return moduleStatuses.get(moduleName) == ModuleStatus.ENABLED;
    }
    
    /**
     * 获取模块信息
     */
    public Optional<ModuleInfo> getModuleInfo(String moduleName) {
        return Optional.ofNullable(registeredModules.get(moduleName));
    }
    
    /**
     * 获取所有已注册的模块名称
     */
    public Set<String> getRegisteredModuleNames() {
        return Collections.unmodifiableSet(registeredModules.keySet());
    }
    
    /**
     * 获取所有启用的模块名称
     */
    public Set<String> getEnabledModuleNames() {
        return moduleStatuses.entrySet().stream()
                .filter(e -> e.getValue() == ModuleStatus.ENABLED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    /**
     * 获取模块状态
     */
    public ModuleStatus getModuleStatus(String moduleName) {
        return moduleStatuses.getOrDefault(moduleName, ModuleStatus.UNKNOWN);
    }
    
    /**
     * 启用模块
     */
    public void enableModule(String moduleName) {
        if (!moduleStatuses.containsKey(moduleName)) {
            throw new IllegalArgumentException("模块不存在: " + moduleName);
        }
        
        // 检查依赖是否满足
        checkModuleDependencies(moduleName);
        
        moduleStatuses.put(moduleName, ModuleStatus.ENABLED);
        logger.info("模块 [{}] 已启用", moduleName);
    }
    
    /**
     * 禁用模块
     */
    public void disableModule(String moduleName) {
        if (!moduleStatuses.containsKey(moduleName)) {
            throw new IllegalArgumentException("模块不存在: " + moduleName);
        }
        
        // 检查是否有其他模块依赖此模块
        checkDependentModules(moduleName);
        
        moduleStatuses.put(moduleName, ModuleStatus.DISABLED);
        logger.info("模块 [{}] 已禁用", moduleName);
    }
    
    /**
     * 检查所有模块依赖
     */
    private void checkDependencies() {
        moduleConfig.getAllModules().forEach((name, settings) -> {
            if (settings.isEnabled()) {
                try {
                    checkModuleDependencies(name);
                } catch (ModuleDependencyException e) {
                    logger.error("模块 [{}] 依赖检查失败: {}", name, e.getMessage());
                    moduleStatuses.put(name, ModuleStatus.ERROR);
                }
            }
        });
        
        // 检查循环依赖
        try {
            detectCircularDependencies();
        } catch (CircularDependencyException e) {
            logger.error("检测到循环依赖: {}", e.getMessage());
        }
    }
    
    /**
     * 检查单个模块的依赖
     */
    private void checkModuleDependencies(String moduleName) {
        ModuleConfig.ModuleSettings settings = moduleConfig.getAllModules().get(moduleName);
        if (settings == null) {
            return;
        }
        
        List<String> missingDependencies = new ArrayList<>();
        for (String dependency : settings.getDependencies()) {
            if (!moduleConfig.isModuleEnabled(dependency)) {
                missingDependencies.add(dependency);
            }
        }
        
        if (!missingDependencies.isEmpty()) {
            throw new ModuleDependencyException(moduleName, missingDependencies);
        }
    }
    
    /**
     * 检查是否有其他模块依赖指定模块
     */
    private void checkDependentModules(String moduleName) {
        List<String> dependentModules = new ArrayList<>();
        
        moduleConfig.getAllModules().forEach((name, settings) -> {
            if (settings.isEnabled() && settings.getDependencies().contains(moduleName)) {
                dependentModules.add(name);
            }
        });
        
        if (!dependentModules.isEmpty()) {
            throw new ModuleDependencyException(
                    "无法禁用模块 [" + moduleName + "]，以下模块依赖它: " + dependentModules);
        }
    }
    
    /**
     * 检测循环依赖
     */
    private void detectCircularDependencies() {
        Map<String, List<String>> graph = new HashMap<>();
        moduleConfig.getAllModules().forEach((name, settings) -> {
            graph.put(name, new ArrayList<>(settings.getDependencies()));
        });
        
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> cycle = new ArrayList<>();
        
        for (String module : graph.keySet()) {
            if (detectCycleDFS(module, graph, visited, recursionStack, cycle)) {
                throw new CircularDependencyException(cycle);
            }
        }
    }
    
    /**
     * DFS检测循环
     */
    private boolean detectCycleDFS(String module, Map<String, List<String>> graph,
                                    Set<String> visited, Set<String> recursionStack,
                                    List<String> cycle) {
        if (recursionStack.contains(module)) {
            cycle.add(module);
            return true;
        }
        
        if (visited.contains(module)) {
            return false;
        }
        
        visited.add(module);
        recursionStack.add(module);
        
        List<String> dependencies = graph.getOrDefault(module, Collections.emptyList());
        for (String dep : dependencies) {
            if (detectCycleDFS(dep, graph, visited, recursionStack, cycle)) {
                cycle.add(0, module);
                return true;
            }
        }
        
        recursionStack.remove(module);
        return false;
    }
    
    /**
     * 模块状态枚举
     */
    public enum ModuleStatus {
        ENABLED,    // 已启用
        DISABLED,   // 已禁用
        LOADING,    // 加载中
        ERROR,      // 错误
        UNKNOWN     // 未知
    }
    
    /**
     * 模块信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ModuleInfo {
        private String name;
        private String description;
        private String version;
        private List<String> dependencies;
        private Class<?> mainClass;
    }
    
    /**
     * 模块依赖异常
     */
    public static class ModuleDependencyException extends RuntimeException {
        private final String moduleName;
        private final List<String> missingDependencies;
        
        public ModuleDependencyException(String moduleName, List<String> missingDependencies) {
            super("模块 [" + moduleName + "] 缺少依赖: " + missingDependencies);
            this.moduleName = moduleName;
            this.missingDependencies = missingDependencies;
        }
        
        public ModuleDependencyException(String message) {
            super(message);
            this.moduleName = null;
            this.missingDependencies = null;
        }
        
        public String getModuleName() {
            return moduleName;
        }
        
        public List<String> getMissingDependencies() {
            return missingDependencies;
        }
    }
    
    /**
     * 循环依赖异常
     */
    public static class CircularDependencyException extends RuntimeException {
        private final List<String> cycle;
        
        public CircularDependencyException(List<String> cycle) {
            super("检测到循环依赖: " + String.join(" -> ", cycle));
            this.cycle = cycle;
        }
        
        public List<String> getCycle() {
            return cycle;
        }
    }
}
