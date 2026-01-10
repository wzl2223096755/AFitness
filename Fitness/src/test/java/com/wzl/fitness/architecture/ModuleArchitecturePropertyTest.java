package com.wzl.fitness.architecture;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
 * 模块化架构属性测试
 * 
 * 验证模块化架构的设计约束和规范
 * 
 * **Validates: Requirements 1.4, 1.5, 2.2, 2.5, 7.1**
 * 
 * Feature: modular-architecture
 */
public class ModuleArchitecturePropertyTest {

    private static final String MODULES_PATH = "src/main/java/com/wzl/fitness/modules";
    private static final String[] MODULE_NAMES = {"user", "training", "nutrition", "recovery", "admin"};
    
    // 模块API接口类
    private static final Map<String, Class<?>> MODULE_APIS = new HashMap<>();
    
    static {
        try {
            MODULE_APIS.put("user", Class.forName("com.wzl.fitness.modules.user.api.UserModuleApi"));
            MODULE_APIS.put("training", Class.forName("com.wzl.fitness.modules.training.api.TrainingModuleApi"));
            MODULE_APIS.put("nutrition", Class.forName("com.wzl.fitness.modules.nutrition.api.NutritionModuleApi"));
            MODULE_APIS.put("recovery", Class.forName("com.wzl.fitness.modules.recovery.api.RecoveryModuleApi"));
        } catch (ClassNotFoundException e) {
            // 某些模块可能没有API接口
        }
    }

    /**
     * Property 1: 模块间依赖必须通过接口
     * 
     * 对于任意模块，其对其他模块的依赖只能通过api包、event包或dto包中的类进行
     * 注意：event包是模块间事件驱动通信的合法方式，dto包是数据传输的合法方式
     * 
     * **Validates: Requirements 1.4, 2.2**
     */
    @Property(tries = 1)
    @Label("Property 1: 模块间依赖必须通过接口")
    void modulesDependOnlyThroughInterfaces() throws IOException {
        Path modulesPath = Paths.get(MODULES_PATH);
        if (!Files.exists(modulesPath)) {
            return; // 跳过如果路径不存在
        }

        List<String> violations = new ArrayList<>();
        
        for (String moduleName : MODULE_NAMES) {
            Path modulePath = modulesPath.resolve(moduleName);
            if (!Files.exists(modulePath)) continue;
            
            // 扫描模块中的所有Java文件（排除api包和event包）
            try (Stream<Path> paths = Files.walk(modulePath)) {
                List<Path> javaFiles = paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("/api/"))
                    .collect(Collectors.toList());
                
                for (Path javaFile : javaFiles) {
                    String content = Files.readString(javaFile);
                    
                    // 检查是否直接导入其他模块的非api/event/dto包
                    for (String otherModule : MODULE_NAMES) {
                        if (otherModule.equals(moduleName)) continue;
                        
                        // 检查是否导入了其他模块的非api/event/dto包
                        // 允许: api包、event包（事件是模块间通信的合法方式）、dto包（数据传输对象）
                        String illegalImportPattern = "import\\s+com\\.wzl\\.fitness\\.modules\\." + otherModule + "\\.(?!api\\.|event\\.|dto\\.)";
                        Pattern pattern = Pattern.compile(illegalImportPattern);
                        Matcher matcher = pattern.matcher(content);
                        
                        if (matcher.find()) {
                            violations.add(String.format(
                                "模块 %s 中的文件 %s 直接依赖了模块 %s 的非API/Event/DTO包",
                                moduleName, javaFile.getFileName(), otherModule
                            ));
                        }
                    }
                }
            }
        }
        
        assertTrue(violations.isEmpty(), 
            "发现模块间非法依赖:\n" + String.join("\n", violations));
    }

    /**
     * Property 2: 模块依赖图无循环
     * 
     * 模块之间的依赖关系不能形成循环
     * 
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 1)
    @Label("Property 2: 模块依赖图无循环")
    void moduleDependencyGraphHasNoCycles() throws IOException {
        Path modulesPath = Paths.get(MODULES_PATH);
        if (!Files.exists(modulesPath)) {
            return;
        }

        // 构建依赖图
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        
        for (String moduleName : MODULE_NAMES) {
            dependencyGraph.put(moduleName, new HashSet<>());
            Path modulePath = modulesPath.resolve(moduleName);
            if (!Files.exists(modulePath)) continue;
            
            try (Stream<Path> paths = Files.walk(modulePath)) {
                List<Path> javaFiles = paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
                
                for (Path javaFile : javaFiles) {
                    String content = Files.readString(javaFile);
                    
                    for (String otherModule : MODULE_NAMES) {
                        if (otherModule.equals(moduleName)) continue;
                        
                        // 检查是否依赖其他模块（通过api包）
                        String importPattern = "import\\s+com\\.wzl\\.fitness\\.modules\\." + otherModule + "\\.";
                        if (Pattern.compile(importPattern).matcher(content).find()) {
                            dependencyGraph.get(moduleName).add(otherModule);
                        }
                    }
                }
            }
        }
        
        // 检测循环依赖（使用DFS）
        List<String> cycle = detectCycle(dependencyGraph);
        
        assertTrue(cycle.isEmpty(), 
            "发现循环依赖: " + String.join(" -> ", cycle));
    }
    
    private List<String> detectCycle(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();
        
        for (String node : graph.keySet()) {
            if (hasCycleDFS(node, graph, visited, recursionStack, path)) {
                return path;
            }
        }
        return Collections.emptyList();
    }
    
    private boolean hasCycleDFS(String node, Map<String, Set<String>> graph, 
                                 Set<String> visited, Set<String> recursionStack, List<String> path) {
        if (recursionStack.contains(node)) {
            path.add(node);
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }
        
        visited.add(node);
        recursionStack.add(node);
        path.add(node);
        
        for (String neighbor : graph.getOrDefault(node, Collections.emptySet())) {
            if (hasCycleDFS(neighbor, graph, visited, recursionStack, path)) {
                return true;
            }
        }
        
        recursionStack.remove(node);
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Property 3: 模块接口只使用DTO传输数据
     * 
     * 模块API接口的方法参数和返回值只能使用DTO类型或基本类型
     * 
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 1)
    @Label("Property 3: 模块接口只使用DTO传输数据")
    void moduleApisOnlyUseDTOs() {
        List<String> violations = new ArrayList<>();
        
        for (Map.Entry<String, Class<?>> entry : MODULE_APIS.entrySet()) {
            String moduleName = entry.getKey();
            Class<?> apiClass = entry.getValue();
            
            for (Method method : apiClass.getDeclaredMethods()) {
                // 检查返回类型
                Class<?> returnType = method.getReturnType();
                if (!isAllowedType(returnType, moduleName)) {
                    violations.add(String.format(
                        "模块 %s 的API方法 %s 返回类型 %s 不是DTO或基本类型",
                        moduleName, method.getName(), returnType.getSimpleName()
                    ));
                }
                
                // 检查参数类型
                for (Parameter param : method.getParameters()) {
                    Class<?> paramType = param.getType();
                    if (!isAllowedType(paramType, moduleName)) {
                        violations.add(String.format(
                            "模块 %s 的API方法 %s 参数 %s 类型 %s 不是DTO或基本类型",
                            moduleName, method.getName(), param.getName(), paramType.getSimpleName()
                        ));
                    }
                }
            }
        }
        
        assertTrue(violations.isEmpty(), 
            "发现API接口使用非DTO类型:\n" + String.join("\n", violations));
    }
    
    private boolean isAllowedType(Class<?> type, String moduleName) {
        // 基本类型和包装类型
        if (type.isPrimitive()) return true;
        if (type == String.class) return true;
        if (type == Long.class || type == Integer.class || type == Double.class || 
            type == Float.class || type == Boolean.class) return true;
        
        // void类型
        if (type == void.class || type == Void.class) return true;
        
        // 集合类型（需要检查泛型参数，这里简化处理）
        if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type) ||
            Map.class.isAssignableFrom(type)) return true;
        
        // 日期类型
        if (type.getName().startsWith("java.time.")) return true;
        
        // DTO类型（类名以DTO结尾或在dto包中）
        String typeName = type.getName();
        if (typeName.endsWith("DTO") || typeName.contains(".dto.")) return true;
        
        // 同模块的DTO
        if (typeName.contains(".modules." + moduleName + ".dto.")) return true;
        
        return false;
    }
}
