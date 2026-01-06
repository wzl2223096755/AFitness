# Design Document: Frontend-Backend Connection

## Overview

本设计文档描述健身管理系统前后端连接的技术实现方案，包括 CORS 配置优化、请求超时处理、JWT 认证机制、错误处理和健康检查等方面。

### 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   User Frontend │     │  Admin Frontend │     │                 │
│   (Vue.js)      │     │   (Vue.js)      │     │                 │
│   Port: 3001    │     │   Port: 3002    │     │                 │
└────────┬────────┘     └────────┬────────┘     │                 │
         │                       │              │                 │
         │  HTTP/HTTPS           │              │                 │
         │  (Vite Proxy)         │              │                 │
         ▼                       ▼              │                 │
┌─────────────────────────────────────────┐    │    MySQL        │
│           Spring Boot Backend           │◄───┤    Database     │
│           Port: 8080                    │    │                 │
│  ┌─────────────────────────────────┐   │    │                 │
│  │  Security Filter Chain          │   │    │                 │
│  │  - CORS Filter                  │   │    │                 │
│  │  - JWT Authentication Filter    │   │    │                 │
│  └─────────────────────────────────┘   │    │                 │
└─────────────────────────────────────────┘    └─────────────────┘
```

## Architecture

### 请求流程

1. 前端发起请求 → Vite 代理转发 → 后端接收
2. 后端 CORS 过滤器验证请求源
3. JWT 过滤器验证认证令牌
4. 业务逻辑处理
5. 返回响应（包含 CORS 头）

### 技术栈

- **前端**: Vue 3 + Vite + Axios
- **后端**: Spring Boot 3.2.5 + Spring Security
- **认证**: JWT (JSON Web Token)
- **通信**: RESTful API over HTTP

## Components and Interfaces

### 1. CORS 配置组件

#### 后端 CorsConfigurationSource

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 允许的源
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3001",  // User Frontend
        "http://localhost:3002",  // Admin Frontend
        "http://127.0.0.1:3001",
        "http://127.0.0.1:3002"
    ));
    
    // 允许的方法
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // 允许的请求头
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization", 
        "Content-Type", 
        "X-Requested-With",
        "Accept",
        "Origin"
    ));
    
    // 暴露的响应头
    configuration.setExposedHeaders(Arrays.asList(
        "Authorization",
        "X-Total-Count"
    ));
    
    // 允许凭证
    configuration.setAllowCredentials(true);
    
    // 预检请求缓存时间
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 2. 前端请求配置

#### Axios 实例配置

```javascript
const service = axios.create({
  baseURL: '',  // 使用 Vite 代理
  timeout: 10000,  // 10秒超时
  withCredentials: true  // 携带凭证
})
```

#### 请求拦截器

```javascript
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})
```

### 3. JWT 认证流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Login   │────►│  Backend │────►│  Return  │
│  Request │     │  Verify  │     │  Tokens  │
└──────────┘     └──────────┘     └──────────┘
                                       │
                                       ▼
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Store   │◄────│  Access  │     │  Refresh │
│  Tokens  │     │  Token   │     │  Token   │
└──────────┘     └──────────┘     └──────────┘
```

### 4. Token 刷新机制

```javascript
// Token 刷新流程
async function refreshToken() {
  const refreshTokenValue = localStorage.getItem('refreshToken')
  const response = await axios.post('/api/v1/auth/refresh', {
    refreshToken: refreshTokenValue
  })
  
  if (response.data.code === 200) {
    const newToken = response.data.data.accessToken
    localStorage.setItem('token', newToken)
    return newToken
  }
  throw new Error('刷新token失败')
}
```

## Data Models

### 1. API 响应格式

```java
public class ApiResponse<T> {
    private int code;        // 状态码
    private String message;  // 消息
    private T data;          // 数据
    private long timestamp;  // 时间戳
}
```

### 2. 认证响应

```java
public class AuthResponse {
    private String accessToken;   // 访问令牌
    private String refreshToken;  // 刷新令牌
    private long expiresIn;       // 过期时间（秒）
    private String tokenType;     // 令牌类型 "Bearer"
}
```

### 3. 错误响应

```java
public class ErrorResponse {
    private int code;           // 错误码
    private String message;     // 错误消息
    private String path;        // 请求路径
    private long timestamp;     // 时间戳
    private Map<String, String> errors;  // 详细错误
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: CORS 响应头完整性

*For any* 来自允许源的请求，后端响应必须包含完整的 CORS 头：Access-Control-Allow-Origin、Access-Control-Allow-Methods、Access-Control-Allow-Headers、Access-Control-Allow-Credentials

**Validates: Requirements 1.3, 1.4, 1.5, 1.6**

### Property 2: 未授权源拒绝

*For any* 来自未授权源的请求，后端必须拒绝该请求或不返回 Access-Control-Allow-Origin 头

**Validates: Requirements 1.7**

### Property 3: 重试指数退避

*For any* 失败的请求重试序列，每次重试的间隔时间必须是前一次的 2 倍（指数退避）

**Validates: Requirements 2.4**

### Property 4: JWT 验证一致性

*For any* 无效或过期的 JWT 令牌，后端必须返回 401 Unauthorized 状态码

**Validates: Requirements 3.5, 3.6**

### Property 5: Token 刷新幂等性

*For any* 有效的 Refresh_Token，多次调用刷新接口应该都能成功获取新的 Access_Token（直到 Refresh_Token 过期）

**Validates: Requirements 3.3**

### Property 6: 错误响应格式一致性

*For any* 后端返回的错误响应，必须包含 code、message 字段，格式统一

**Validates: Requirements 4.6**

### Property 7: 健康检查可用性

*For any* 对 /api/v1/health 端点的请求，后端必须返回包含服务状态信息的响应

**Validates: Requirements 5.1, 5.4**

## Error Handling

### 前端错误处理策略

| HTTP 状态码 | 处理方式 | 用户提示 |
|------------|---------|---------|
| 401 | 尝试刷新 Token，失败则跳转登录 | "登录已过期，请重新登录" |
| 403 | 显示权限不足提示 | "没有权限访问该资源" |
| 404 | 显示资源不存在提示 | "请求的资源不存在" |
| 500 | 显示服务器错误提示 | "服务器内部错误" |
| 网络错误 | 显示网络错误提示 | "网络错误，请检查网络连接" |
| 超时 | 自动重试（最多3次） | "请求超时，正在重试..." |

### 后端错误处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthException(AuthenticationException e) {
        return ResponseEntity.status(401)
            .body(ApiResponse.error(401, "认证失败: " + e.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403)
            .body(ApiResponse.error(403, "没有权限访问该资源"));
    }
}
```

## Testing Strategy

### 单元测试

1. **CORS 配置测试**: 验证 CORS 配置正确加载
2. **JWT 工具类测试**: 验证 Token 生成和验证逻辑
3. **请求拦截器测试**: 验证 Token 自动添加

### 属性测试 (Property-Based Testing)

使用 jqwik 框架进行属性测试：

1. **Property 1**: CORS 响应头完整性测试
2. **Property 4**: JWT 验证一致性测试
3. **Property 6**: 错误响应格式一致性测试

### 集成测试

1. **端到端认证流程**: 登录 → 获取 Token → 访问受保护资源 → Token 刷新
2. **CORS 预检请求**: 验证 OPTIONS 请求正确处理
3. **错误场景**: 验证各种错误情况的处理

### 测试配置

```java
// jqwik 属性测试配置
@PropertyDefaults(tries = 100)
public class CorsPropertyTest {
    // 属性测试实现
}
```
