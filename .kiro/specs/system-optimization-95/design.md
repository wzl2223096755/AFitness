# Design Document

## Overview

本设计文档描述了将 Fitness 系统从 85% 完成度提升至 95% 的技术实现方案。优化工作分为文档完善、缓存增强、管理端功能、测试覆盖和数据验证五个主要领域。

## Architecture

### 系统架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3)                          │
├─────────────────────┬───────────────────────────────────────┤
│   User Frontend     │         Admin Panel                   │
│   (Port 3001)       │         (Port 3002)                   │
│   - 错误边界组件     │         - 系统监控仪表盘               │
│   - 离线提示        │         - 统计分析视图                 │
│   - 表单验证        │         - 数据导出功能                 │
└─────────────────────┴───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   后端层 (Spring Boot)                       │
│                      (Port 8080)                            │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Controller  │  │   Service   │  │    Repository       │  │
│  │ + Swagger   │  │ + Caffeine  │  │    + JPA            │  │
│  │ + 验证注解   │  │   Cache     │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    数据层 (MySQL)                            │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Caffeine 缓存组件

```java
// CaffeineCacheConfig.java
@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats());
        cacheManager.setCacheNames(Arrays.asList(
            "users", "trainingRecords", "nutritionStats", "dashboardMetrics"
        ));
        return cacheManager;
    }
}
```

### 2. 缓存统计端点

```java
// CacheStatsController.java
@RestController
@RequestMapping("/api/v1/admin/cache")
public class CacheStatsController {
    
    @GetMapping("/stats")
    public ApiResponse<Map<String, CacheStats>> getCacheStats();
    
    @PostMapping("/evict/{cacheName}")
    public ApiResponse<Void> evictCache(@PathVariable String cacheName);
    
    @PostMapping("/evict-all")
    public ApiResponse<Void> evictAllCaches();
}
```

### 3. 系统监控仪表盘 API

```java
// SystemMonitorController.java
@RestController
@RequestMapping("/api/v1/admin/monitor")
public class SystemMonitorController {
    
    @GetMapping("/system-info")
    public ApiResponse<SystemInfoDTO> getSystemInfo();
    
    @GetMapping("/jvm-metrics")
    public ApiResponse<JvmMetricsDTO> getJvmMetrics();
    
    @GetMapping("/database-stats")
    public ApiResponse<DatabaseStatsDTO> getDatabaseStats();
    
    @GetMapping("/user-activity")
    public ApiResponse<UserActivityDTO> getUserActivity();
}
```

### 4. 前端错误边界组件

```vue
<!-- ErrorBoundary.vue -->
<template>
  <div v-if="hasError" class="error-boundary">
    <el-result icon="error" title="出错了" :sub-title="errorMessage">
      <template #extra>
        <el-button @click="retry">重试</el-button>
        <el-button @click="reportError">报告问题</el-button>
      </template>
    </el-result>
  </div>
  <slot v-else />
</template>
```

### 5. 数据导出服务

```java
// DataExportService.java
public interface DataExportService {
    byte[] exportUsersToExcel();
    byte[] exportTrainingRecordsToExcel(Long userId, LocalDate start, LocalDate end);
    byte[] exportNutritionRecordsToExcel(Long userId, LocalDate start, LocalDate end);
    byte[] exportSystemStatsToExcel();
}
```

## Data Models

### 系统监控 DTO

```java
@Data
public class SystemInfoDTO {
    private String osName;
    private String osVersion;
    private int availableProcessors;
    private long totalMemory;
    private long freeMemory;
    private long usedMemory;
    private double cpuUsage;
    private long uptime;
}

@Data
public class JvmMetricsDTO {
    private long heapUsed;
    private long heapMax;
    private long nonHeapUsed;
    private int threadCount;
    private int peakThreadCount;
    private long gcCount;
    private long gcTime;
}

@Data
public class DatabaseStatsDTO {
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private long totalQueries;
    private double avgQueryTime;
}

@Data
public class UserActivityDTO {
    private long totalUsers;
    private long activeUsersToday;
    private long activeUsersWeek;
    private long newUsersToday;
    private Map<String, Long> usersByRole;
    private List<DailyActivityDTO> dailyActivity;
}

@Data
public class CacheStatsDTO {
    private String cacheName;
    private long hitCount;
    private long missCount;
    private double hitRate;
    private long evictionCount;
    private long size;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 缓存一致性

*For any* 数据更新操作, 相关缓存条目应被正确失效，后续查询应返回最新数据
**Validates: Requirements 2.5**

### Property 2: 数据导出完整性

*For any* 数据导出请求, 导出的数据应与数据库中的原始数据完全一致
**Validates: Requirements 3.5**

### Property 3: 输入验证有效性

*For any* 用户输入, 验证规则应正确识别并拒绝无效输入，同时允许所有有效输入通过
**Validates: Requirements 8.1, 8.2**

### Property 4: 错误处理一致性

*For any* API 错误响应, 前端应显示对应的用户友好错误提示
**Validates: Requirements 7.1**

## Error Handling

### 后端错误处理

```java
// 增强的全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ApiResponse.error(ResponseCode.VALIDATION_ERROR, errors);
    }
    
    @ExceptionHandler(CacheException.class)
    public ApiResponse<Void> handleCacheException(CacheException ex) {
        log.warn("Cache operation failed: {}", ex.getMessage());
        // 缓存失败不影响业务，降级处理
        return ApiResponse.success();
    }
}
```

### 前端错误处理

```javascript
// errorHandler.js 增强
export const errorMessages = {
  400: '请求参数错误，请检查输入',
  401: '登录已过期，请重新登录',
  403: '您没有权限执行此操作',
  404: '请求的资源不存在',
  500: '服务器内部错误，请稍后重试',
  NETWORK_ERROR: '网络连接失败，请检查网络',
  TIMEOUT: '请求超时，请稍后重试',
  UNKNOWN: '发生未知错误，请联系管理员'
};

export function handleApiError(error) {
  const status = error.response?.status;
  const message = errorMessages[status] || errorMessages.UNKNOWN;
  
  if (status === 401) {
    // 自动跳转登录
    router.push('/login');
  }
  
  ElMessage.error(message);
  return Promise.reject(error);
}
```

## Testing Strategy

### 测试类型分布

| 测试类型 | 后端目标 | 前端目标 |
|---------|---------|---------|
| 单元测试 | 80%+ | 75%+ |
| 集成测试 | 关键流程 | E2E |
| 属性测试 | 核心算法 | - |

### 后端测试增强

```java
// CacheServiceTest.java
@SpringBootTest
class CacheServiceTest {
    
    @Test
    void testCacheHitAfterFirstQuery() {
        // 第一次查询，缓存未命中
        userService.findById(1L);
        // 第二次查询，应命中缓存
        userService.findById(1L);
        // 验证数据库只被查询一次
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    void testCacheEvictionOnUpdate() {
        userService.findById(1L);
        userService.updateUser(1L, updateDTO);
        userService.findById(1L);
        // 更新后缓存失效，应再次查询数据库
        verify(userRepository, times(2)).findById(1L);
    }
}
```

### 前端测试增强

```javascript
// ErrorBoundary.test.js
describe('ErrorBoundary', () => {
  it('should display error message when child throws', async () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ThrowingComponent
      }
    });
    await flushPromises();
    expect(wrapper.find('.error-boundary').exists()).toBe(true);
  });
  
  it('should recover after retry', async () => {
    // ...
  });
});
```

### CI/CD 覆盖率检查

```yaml
# ci.yml 增强
- name: Check backend coverage
  run: |
    mvn jacoco:report
    COVERAGE=$(grep -oP 'Total.*?([0-9]+)%' target/site/jacoco/index.html | grep -oP '[0-9]+')
    if [ "$COVERAGE" -lt 80 ]; then
      echo "Coverage $COVERAGE% is below 80% threshold"
      exit 1
    fi

- name: Check frontend coverage
  run: |
    npm run test:coverage
    COVERAGE=$(cat coverage/coverage-summary.json | jq '.total.lines.pct')
    if (( $(echo "$COVERAGE < 75" | bc -l) )); then
      echo "Coverage $COVERAGE% is below 75% threshold"
      exit 1
    fi
```
