# Design Document: Modular Architecture

## Overview

本设计文档描述AFitness健身管理系统的模块化架构重构方案。采用领域驱动设计(DDD)原则，将单体应用重构为高内聚、低耦合的模块化架构。后端使用Spring Boot的多模块支持和事件驱动机制，前端采用Vue 3的模块化组织方式。

## Architecture

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Application Layer                              │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│
│  │ User Module │ │Training Mod │ │Nutrition Mod│ │  Recovery Module    ││
│  │             │ │             │ │             │ │                     ││
│  │ - Auth      │ │ - Records   │ │ - Records   │ │ - Assessment        ││
│  │ - Profile   │ │ - Plans     │ │ - Goals     │ │ - Suggestions       ││
│  │ - Settings  │ │ - Stats     │ │ - Stats     │ │ - Trends            ││
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────────┬──────────┘│
│         │               │               │                    │           │
│  ┌──────┴───────────────┴───────────────┴────────────────────┴──────────┐│
│  │                         Event Bus (Spring Events)                     ││
│  └──────────────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────────────┤
│                           Shared Kernel                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────────────────┐│
│  │  Security  │ │   Common   │ │   Config   │ │     Infrastructure     ││
│  │  - JWT     │ │  - Response│ │  - Cache   │ │  - Exception Handler   ││
│  │  - Auth    │ │  - Page    │ │  - DB      │ │  - Logging             ││
│  └────────────┘ └────────────┘ └────────────┘ └────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────┘
```


### 后端模块结构

```
src/main/java/com/wzl/fitness/
├── FitnessApplication.java              # 应用入口
├── shared/                              # 共享内核
│   ├── common/                          # 通用组件
│   │   ├── ApiResponse.java
│   │   ├── BaseController.java
│   │   ├── PageResponse.java
│   │   └── ResponseCode.java
│   ├── config/                          # 配置类
│   │   ├── CacheConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── exception/                       # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── security/                        # 安全组件
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   ├── event/                           # 事件基础设施
│   │   ├── DomainEvent.java
│   │   └── EventPublisher.java
│   └── util/                            # 工具类
│
├── modules/                             # 业务模块
│   ├── user/                            # 用户模块
│   │   ├── api/                         # 模块接口
│   │   │   └── UserModuleApi.java
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── event/
│   │
│   ├── training/                        # 训练模块
│   │   ├── api/
│   │   │   └── TrainingModuleApi.java
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── event/
│   │
│   ├── nutrition/                       # 营养模块
│   │   ├── api/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   ├── recovery/                        # 恢复评估模块
│   │   ├── api/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   └── admin/                           # 管理模块
│       ├── api/
│       ├── controller/
│       ├── service/
│       └── dto/
```


### 前端模块结构

```
frontend/src/
├── main.js                              # 应用入口
├── App.vue                              # 根组件
├── shared/                              # 共享内核
│   ├── api/                             # API基础设施
│   │   └── request.js
│   ├── components/                      # 通用组件
│   │   ├── ErrorBoundary.vue
│   │   └── LoadingSpinner.vue
│   ├── composables/                     # 通用组合函数
│   │   ├── useAuth.js
│   │   └── useLoading.js
│   ├── layouts/                         # 布局组件
│   │   ├── MainLayout.vue
│   │   └── AuthLayout.vue
│   ├── plugins/                         # 插件
│   ├── styles/                          # 全局样式
│   └── utils/                           # 工具函数
│
├── modules/                             # 业务模块
│   ├── auth/                            # 认证模块
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   ├── stores/
│   │   └── index.js                     # 模块导出
│   │
│   ├── dashboard/                       # 仪表盘模块
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   └── index.js
│   │
│   ├── training/                        # 训练模块
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   ├── stores/
│   │   └── index.js
│   │
│   ├── nutrition/                       # 营养模块
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   └── index.js
│   │
│   ├── recovery/                        # 恢复模块
│   │   ├── views/
│   │   ├── components/
│   │   └── index.js
│   │
│   └── settings/                        # 设置模块
│       ├── views/
│       ├── components/
│       └── index.js
│
└── router/                              # 路由配置
    ├── index.js
    └── modules/                         # 模块路由
        ├── auth.js
        ├── training.js
        └── ...
```


## Components and Interfaces

### 后端模块接口定义

#### UserModuleApi - 用户模块接口

```java
public interface UserModuleApi {
    /**
     * 根据ID获取用户信息
     */
    UserDTO getUserById(Long userId);
    
    /**
     * 根据用户名获取用户信息
     */
    UserDTO getUserByUsername(String username);
    
    /**
     * 检查用户是否存在
     */
    boolean existsById(Long userId);
    
    /**
     * 获取用户基础信息（用于其他模块引用）
     */
    UserBasicInfo getBasicInfo(Long userId);
}
```

#### TrainingModuleApi - 训练模块接口

```java
public interface TrainingModuleApi {
    /**
     * 获取用户最近的训练记录
     */
    List<TrainingRecordDTO> getRecentRecords(Long userId, int limit);
    
    /**
     * 计算用户训练负荷
     */
    TrainingLoadDTO calculateLoad(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取训练统计数据
     */
    TrainingStatsDTO getStats(Long userId);
}
```

#### NutritionModuleApi - 营养模块接口

```java
public interface NutritionModuleApi {
    /**
     * 获取用户每日营养摄入
     */
    DailyNutritionDTO getDailyNutrition(Long userId, LocalDate date);
    
    /**
     * 获取营养目标
     */
    NutritionGoalDTO getGoals(Long userId);
}
```

#### RecoveryModuleApi - 恢复评估模块接口

```java
public interface RecoveryModuleApi {
    /**
     * 获取当前恢复状态
     */
    RecoveryStatusDTO getCurrentStatus(Long userId);
    
    /**
     * 获取训练建议
     */
    List<TrainingSuggestionDTO> getSuggestions(Long userId);
}
```


### 事件定义

#### 领域事件基类

```java
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long userId;
    
    protected DomainEvent(Long userId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
    }
}
```

#### 训练完成事件

```java
public class TrainingCompletedEvent extends DomainEvent {
    private final Long trainingRecordId;
    private final Double totalVolume;
    private final String exerciseType;
    
    public TrainingCompletedEvent(Long userId, Long trainingRecordId, 
                                   Double totalVolume, String exerciseType) {
        super(userId);
        this.trainingRecordId = trainingRecordId;
        this.totalVolume = totalVolume;
        this.exerciseType = exerciseType;
    }
}
```

#### 恢复状态更新事件

```java
public class RecoveryStatusUpdatedEvent extends DomainEvent {
    private final String recoveryLevel;
    private final Integer recoveryScore;
    
    public RecoveryStatusUpdatedEvent(Long userId, String recoveryLevel, 
                                       Integer recoveryScore) {
        super(userId);
        this.recoveryLevel = recoveryLevel;
        this.recoveryScore = recoveryScore;
    }
}
```

### 事件发布器

```java
@Component
public class EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```


### 前端模块接口

#### 模块导出规范 (index.js)

```javascript
// modules/training/index.js
export { default as routes } from './routes'
export { useTrainingStore } from './stores/training'
export * from './api'
export * from './components'

// 模块元信息
export const moduleInfo = {
  name: 'training',
  displayName: '训练管理',
  icon: 'Dumbbell',
  order: 2,
  permissions: ['training:read', 'training:write']
}
```

#### 模块注册器

```javascript
// shared/plugins/moduleRegistry.js
class ModuleRegistry {
  constructor() {
    this.modules = new Map()
  }
  
  register(moduleInfo, routes, stores) {
    this.modules.set(moduleInfo.name, {
      info: moduleInfo,
      routes,
      stores,
      loaded: false
    })
  }
  
  getModule(name) {
    return this.modules.get(name)
  }
  
  getAllModules() {
    return Array.from(this.modules.values())
  }
  
  async loadModule(name) {
    const module = this.modules.get(name)
    if (module && !module.loaded) {
      // 动态加载模块资源
      module.loaded = true
    }
    return module
  }
}

export const moduleRegistry = new ModuleRegistry()
```


## Data Models

### 模块间数据传输对象

#### UserBasicInfo - 用户基础信息（跨模块引用）

```java
@Data
public class UserBasicInfo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}
```

#### ModuleConfig - 模块配置

```java
@Data
@ConfigurationProperties(prefix = "fitness.modules")
public class ModuleConfig {
    private Map<String, ModuleSettings> settings = new HashMap<>();
    
    @Data
    public static class ModuleSettings {
        private boolean enabled = true;
        private int order = 0;
        private List<String> dependencies = new ArrayList<>();
    }
}
```

### 前端模块状态结构

```typescript
// 模块状态接口
interface ModuleState {
  loading: boolean
  error: Error | null
  initialized: boolean
}

// 训练模块状态
interface TrainingModuleState extends ModuleState {
  records: TrainingRecord[]
  currentRecord: TrainingRecord | null
  stats: TrainingStats | null
  pagination: Pagination
}

// 营养模块状态
interface NutritionModuleState extends ModuleState {
  dailyRecords: NutritionRecord[]
  goals: NutritionGoal | null
  summary: NutritionSummary | null
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 模块间依赖必须通过接口

*For any* 模块A对模块B的依赖注入，注入的类型必须是接口类型（如UserModuleApi）而非具体实现类（如UserModuleApiImpl）。

**Validates: Requirements 1.4, 2.2**

### Property 2: 模块依赖图无循环

*For any* 模块依赖关系图，从任意模块出发沿依赖方向遍历，不应能够回到起始模块（即依赖图是有向无环图DAG）。

**Validates: Requirements 1.5**

### Property 3: 模块接口只使用DTO传输数据

*For any* 模块接口（ModuleApi）的方法，其参数类型和返回类型必须是DTO类或基本类型，不能是Entity类。

**Validates: Requirements 2.5**

### Property 4: API路由前缀规范

*For any* 模块内的Controller端点，其请求路径必须以`/api/v1/{moduleName}`开头，其中moduleName与模块名称一致。

**Validates: Requirements 7.1**

### Property 5: 模块禁用后功能不可用

*For any* 被配置为disabled的模块，其所有API端点应返回404状态码，且其服务Bean不应被加载到Spring容器中。

**Validates: Requirements 6.2, 7.5**


## Error Handling

### 模块加载错误

```java
public class ModuleLoadException extends RuntimeException {
    private final String moduleName;
    private final String reason;
    
    public ModuleLoadException(String moduleName, String reason) {
        super("Failed to load module: " + moduleName + ", reason: " + reason);
        this.moduleName = moduleName;
        this.reason = reason;
    }
}
```

### 模块依赖错误

```java
public class ModuleDependencyException extends RuntimeException {
    private final String moduleName;
    private final List<String> missingDependencies;
    
    public ModuleDependencyException(String moduleName, List<String> missingDependencies) {
        super("Module " + moduleName + " has missing dependencies: " + missingDependencies);
        this.moduleName = moduleName;
        this.missingDependencies = missingDependencies;
    }
}
```

### 循环依赖错误

```java
public class CircularDependencyException extends RuntimeException {
    private final List<String> cycle;
    
    public CircularDependencyException(List<String> cycle) {
        super("Circular dependency detected: " + String.join(" -> ", cycle));
        this.cycle = cycle;
    }
}
```

### 模块接口未找到错误

```java
public class ModuleApiNotFoundException extends RuntimeException {
    private final Class<?> apiClass;
    
    public ModuleApiNotFoundException(Class<?> apiClass) {
        super("Module API implementation not found for: " + apiClass.getName());
        this.apiClass = apiClass;
    }
}
```

### 前端错误处理

```javascript
// shared/utils/moduleError.js
export class ModuleLoadError extends Error {
  constructor(moduleName, cause) {
    super(`Failed to load module: ${moduleName}`)
    this.name = 'ModuleLoadError'
    this.moduleName = moduleName
    this.cause = cause
  }
}

export class ModuleNotFoundError extends Error {
  constructor(moduleName) {
    super(`Module not found: ${moduleName}`)
    this.name = 'ModuleNotFoundError'
    this.moduleName = moduleName
  }
}
```


## Testing Strategy

### 测试框架选择

- **后端单元测试**: JUnit 5 + Mockito
- **后端属性测试**: jqwik 1.8.2
- **后端集成测试**: Spring Boot Test + H2
- **前端单元测试**: Vitest + @vue/test-utils
- **前端E2E测试**: Playwright

### 单元测试策略

1. **模块接口测试**: 验证每个模块接口的实现是否正确
2. **事件处理测试**: 验证事件发布和订阅机制
3. **依赖注入测试**: 验证模块间依赖是否正确注入

### 属性测试策略

属性测试用于验证模块化架构的正确性属性，每个属性测试至少运行100次迭代。

1. **Property 1测试**: 使用反射扫描所有模块间依赖，验证注入类型为接口
2. **Property 2测试**: 构建模块依赖图，使用拓扑排序检测循环
3. **Property 3测试**: 扫描所有ModuleApi接口方法，验证参数和返回类型
4. **Property 4测试**: 扫描所有Controller，验证RequestMapping路径前缀
5. **Property 5测试**: 动态禁用模块，验证API返回404

### 集成测试策略

1. **模块隔离测试**: 单独加载一个模块进行测试，Mock其他模块依赖
2. **模块协作测试**: 加载多个相关模块，测试模块间通信
3. **事件流测试**: 测试完整的事件发布-处理流程

### 测试配置

```java
// 模块隔离测试配置
@SpringBootTest(classes = {TrainingModuleTestConfig.class})
@TestPropertySource(properties = {
    "fitness.modules.user.enabled=false",
    "fitness.modules.nutrition.enabled=false"
})
public class TrainingModuleIsolatedTest {
    @MockBean
    private UserModuleApi userModuleApi;
    
    // 测试代码
}
```

```javascript
// 前端模块测试配置
// vitest.config.js
export default defineConfig({
  test: {
    include: ['src/modules/**/*.test.js'],
    coverage: {
      reporter: ['text', 'json', 'html'],
      include: ['src/modules/**/*.{js,vue}']
    }
  }
})
```

