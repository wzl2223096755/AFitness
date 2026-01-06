# Requirements Document

## Introduction

本文档定义了健身管理系统前后端连接优化的需求，涵盖 CORS 配置、请求超时处理、认证机制和错误处理等方面，确保前端（用户端 port 3001、管理端 port 3002）与后端（Spring Boot port 8080）之间的稳定可靠通信。

## Glossary

- **Frontend**: Vue.js 前端应用，包括用户端（port 3001）和管理端（port 3002）
- **Backend**: Spring Boot 后端服务（port 8080）
- **CORS**: Cross-Origin Resource Sharing，跨域资源共享
- **JWT**: JSON Web Token，用于用户认证
- **Preflight_Request**: 浏览器发送的 OPTIONS 预检请求
- **Access_Token**: 短期有效的访问令牌
- **Refresh_Token**: 长期有效的刷新令牌

## Requirements

### Requirement 1: CORS 跨域配置

**User Story:** As a 前端开发者, I want 后端正确配置 CORS, so that 前端可以正常访问后端 API 而不会被浏览器阻止。

#### Acceptance Criteria

1. WHEN 前端发送 OPTIONS 预检请求, THE Backend SHALL 返回正确的 CORS 响应头
2. WHEN 前端从 localhost:3001 或 localhost:3002 发送请求, THE Backend SHALL 允许该请求通过
3. THE Backend SHALL 在响应中包含 Access-Control-Allow-Origin 头
4. THE Backend SHALL 在响应中包含 Access-Control-Allow-Methods 头，允许 GET, POST, PUT, DELETE, OPTIONS
5. THE Backend SHALL 在响应中包含 Access-Control-Allow-Headers 头，允许 Authorization, Content-Type
6. THE Backend SHALL 在响应中包含 Access-Control-Allow-Credentials: true 头
7. WHEN 请求来自未授权的源, THE Backend SHALL 拒绝该请求

### Requirement 2: 请求超时处理

**User Story:** As a 用户, I want 系统能够优雅地处理请求超时, so that 我能获得清晰的反馈而不是无限等待。

#### Acceptance Criteria

1. THE Frontend SHALL 配置合理的请求超时时间（默认 10 秒）
2. WHEN 请求超时发生, THE Frontend SHALL 显示友好的超时提示信息
3. THE Frontend SHALL 对网络错误和服务器错误实现自动重试机制
4. WHEN 重试失败, THE Frontend SHALL 使用指数退避策略增加重试间隔
5. THE Backend SHALL 配置合理的响应超时时间
6. WHEN 后端处理超时, THE Backend SHALL 返回 504 Gateway Timeout 状态码

### Requirement 3: JWT 认证机制

**User Story:** As a 用户, I want 系统能够安全地管理我的登录状态, so that 我的账户安全得到保障。

#### Acceptance Criteria

1. WHEN 用户登录成功, THE Backend SHALL 返回 Access_Token 和 Refresh_Token
2. THE Frontend SHALL 在每个需要认证的请求中携带 Authorization: Bearer {token} 头
3. WHEN Access_Token 过期, THE Frontend SHALL 自动使用 Refresh_Token 获取新的 Access_Token
4. WHEN Refresh_Token 也过期, THE Frontend SHALL 引导用户重新登录
5. THE Backend SHALL 验证每个受保护接口的 JWT 令牌有效性
6. WHEN JWT 验证失败, THE Backend SHALL 返回 401 Unauthorized 状态码
7. THE Frontend SHALL 安全地存储令牌（localStorage）

### Requirement 4: 错误处理和用户反馈

**User Story:** As a 用户, I want 系统能够清晰地告诉我发生了什么错误, so that 我知道如何解决问题。

#### Acceptance Criteria

1. WHEN 发生 401 错误, THE Frontend SHALL 显示"未授权，请重新登录"并跳转登录页
2. WHEN 发生 403 错误, THE Frontend SHALL 显示"没有权限访问该资源"
3. WHEN 发生 404 错误, THE Frontend SHALL 显示"请求的资源不存在"
4. WHEN 发生 500 错误, THE Frontend SHALL 显示"服务器内部错误"
5. WHEN 发生网络错误, THE Frontend SHALL 显示"网络错误，请检查网络连接"
6. THE Backend SHALL 返回统一格式的错误响应 {code, message, data}

### Requirement 5: 健康检查和连接状态

**User Story:** As a 系统管理员, I want 能够监控前后端连接状态, so that 我能及时发现和解决连接问题。

#### Acceptance Criteria

1. THE Backend SHALL 提供 /api/v1/health 健康检查端点
2. THE Frontend SHALL 定期检查后端连接状态
3. WHEN 后端不可用, THE Frontend SHALL 显示连接状态提示
4. THE Backend SHALL 在健康检查响应中包含服务状态信息
