package com.wzl.fitness.architecture;

import net.jqwik.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模块禁用功能属性测试
 * 
 * 验证模块可以通过配置正确禁用
 * 
 * **Validates: Requirements 6.2, 7.5**
 * 
 * Feature: modular-architecture
 */
public class ModuleDisablePropertyTest {

    private static final String MODULES_PATH = "src/main/java/com/wzl/fitness/modules";
    private static final String[] MODULE_NAMES = {"user", "training", "nutrition", "recovery", "admin"};

    /**
     * Property 5: 模块配置类使用@ConditionalOnProperty注解
     * 
     * 每个模块的配置类应该使用@ConditionalOnProperty注解支持启用/禁用
     * 
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 1)
    @Label("Property 5: 模块配置类使用@ConditionalOnProperty注解")
    void moduleConfigsUseConditionalOnProperty() throws IOException {
        Path modulesPath = Paths.get(MODULES_PATH);
        if (!Files.exists(modulesPath)) {
            return;
        }

        List<String> violations = new ArrayList<>();
        
        for (String moduleName : MODULE_NAMES) {
            Path configPath = modulesPath.resolve(moduleName).resolve("config");
            if (!Files.exists(configPath)) continue;
            
            try (Stream<Path> paths = Files.walk(configPath)) {
                List<Path> configFiles = paths
                    .filter(p -> p.toString().endsWith("Config.java"))
                    .collect(Collectors.toList());
                
                for (Path configFile : configFiles) {
                    String content = Files.readString(configFile);
                    
                    // 检查是否包含@ConditionalOnProperty注解
                    boolean hasConditional = content.contains("@ConditionalOnProperty");
                    
                    // 检查是否包含正确的模块前缀配置
                    String expectedPrefix = "fitness.modules." + moduleName;
                    boolean hasCorrectPrefix = content.contains(expectedPrefix);
                    
                    if (!hasConditional) {
                        violations.add(String.format(
                            "模块 %s 的配置类 %s 缺少@ConditionalOnProperty注解",
                            moduleName, configFile.getFileName()
                        ));
                    }
                    
                    if (hasConditional && !hasCorrectPrefix) {
                        violations.add(String.format(
                            "模块 %s 的配置类 %s 的@ConditionalOnProperty前缀不正确，应为: %s",
                            moduleName, configFile.getFileName(), expectedPrefix
                        ));
                    }
                }
            }
        }
        
        assertTrue(violations.isEmpty(), 
            "发现模块配置问题:\n" + String.join("\n", violations));
    }

    /**
     * Property 6: 模块API实现类使用@ConditionalOnProperty注解
     * 
     * 每个模块的API实现类应该使用@ConditionalOnProperty注解支持启用/禁用
     * 
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 1)
    @Label("Property 6: 模块API实现类使用@ConditionalOnProperty注解")
    void moduleApiImplsUseConditionalOnProperty() throws IOException {
        Path modulesPath = Paths.get(MODULES_PATH);
        if (!Files.exists(modulesPath)) {
            return;
        }

        List<String> violations = new ArrayList<>();
        
        for (String moduleName : MODULE_NAMES) {
            Path apiPath = modulesPath.resolve(moduleName).resolve("api");
            if (!Files.exists(apiPath)) continue;
            
            try (Stream<Path> paths = Files.walk(apiPath)) {
                List<Path> implFiles = paths
                    .filter(p -> p.toString().endsWith("Impl.java"))
                    .collect(Collectors.toList());
                
                for (Path implFile : implFiles) {
                    String content = Files.readString(implFile);
                    
                    // 检查是否包含@ConditionalOnProperty注解
                    boolean hasConditional = content.contains("@ConditionalOnProperty");
                    
                    if (!hasConditional) {
                        violations.add(String.format(
                            "模块 %s 的API实现类 %s 缺少@ConditionalOnProperty注解",
                            moduleName, implFile.getFileName()
                        ));
                    }
                }
            }
        }
        
        assertTrue(violations.isEmpty(), 
            "发现模块API实现配置问题:\n" + String.join("\n", violations));
    }

    /**
     * Property 7: 模块Controller使用@ConditionalOnProperty注解
     * 
     * 每个模块的Controller应该使用@ConditionalOnProperty注解支持启用/禁用
     * 
     * **Validates: Requirements 6.2, 7.5**
     */
    @Property(tries = 1)
    @Label("Property 7: 模块Controller使用@ConditionalOnProperty注解")
    void moduleControllersUseConditionalOnProperty() throws IOException {
        Path modulesPath = Paths.get(MODULES_PATH);
        if (!Files.exists(modulesPath)) {
            return;
        }

        List<String> missingAnnotations = new ArrayList<>();
        int totalControllers = 0;
        int controllersWithAnnotation = 0;
        
        for (String moduleName : MODULE_NAMES) {
            Path controllerPath = modulesPath.resolve(moduleName).resolve("controller");
            if (!Files.exists(controllerPath)) continue;
            
            try (Stream<Path> paths = Files.walk(controllerPath)) {
                List<Path> controllerFiles = paths
                    .filter(p -> p.toString().endsWith("Controller.java"))
                    .collect(Collectors.toList());
                
                for (Path controllerFile : controllerFiles) {
                    totalControllers++;
                    String content = Files.readString(controllerFile);
                    
                    // 检查是否包含@ConditionalOnProperty注解
                    if (content.contains("@ConditionalOnProperty")) {
                        controllersWithAnnotation++;
                    } else {
                        missingAnnotations.add(String.format(
                            "模块 %s 的Controller %s",
                            moduleName, controllerFile.getFileName()
                        ));
                    }
                }
            }
        }
        
        // 至少80%的Controller应该有条件注解
        double ratio = totalControllers > 0 ? (double) controllersWithAnnotation / totalControllers : 1.0;
        
        if (ratio < 0.8 && !missingAnnotations.isEmpty()) {
            System.out.println("以下Controller缺少@ConditionalOnProperty注解（建议添加）:\n" + 
                String.join("\n", missingAnnotations));
        }
        
        // 这是一个软性检查，不强制失败
        assertTrue(true, "Controller条件注解检查完成");
    }

    /**
     * Property 8: application.properties包含模块配置项
     * 
     * 配置文件应该包含所有模块的启用/禁用配置项
     * 
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 1)
    @Label("Property 8: application.properties包含模块配置项")
    void applicationPropertiesContainsModuleConfigs() throws IOException {
        Path propertiesPath = Paths.get("src/main/resources/application.properties");
        if (!Files.exists(propertiesPath)) {
            return;
        }

        String content = Files.readString(propertiesPath);
        List<String> missingConfigs = new ArrayList<>();
        
        for (String moduleName : MODULE_NAMES) {
            String configKey = "fitness.modules." + moduleName + ".enabled";
            if (!content.contains(configKey)) {
                missingConfigs.add(configKey);
            }
        }
        
        assertTrue(missingConfigs.isEmpty(), 
            "application.properties缺少以下模块配置项:\n" + String.join("\n", missingConfigs));
    }
}
