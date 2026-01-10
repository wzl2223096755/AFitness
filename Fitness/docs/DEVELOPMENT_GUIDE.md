# AFitness 健身管理系统 - 开发文档

> 版本：v1.4.0 | 最后更新：2026-01-07

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [开发环境搭建](#3-开发环境搭建)
4. [项目结构详解](#4-项目结构详解)
5. [后端开发指南](#5-后端开发指南)
6. [前端开发指南](#6-前端开发指南)
7. [数据库设计](#7-数据库设计)
8. [API接口规范](#8-api接口规范)
9. [安全机制](#9-安全机制)
10. [缓存策略](#10-缓存策略)
11. [测试指南](#11-测试指南)
12. [部署指南](#12-部署指南)
13. [故障排除](#13-故障排除)
14. [附录](#14-附录)

---

## 1. 项目概述

### 1.1 项目简介

AFitness 是一个专注于力量训练的智能化负荷计算与恢复监控平台，旨在帮助健身爱好者科学管理训练强度、评估身体恢复状态，并提供个性化训练建议。

### 1.2 核心功能模块

| 模块 | 功能描述 | 技术实现 |
|------|----------|----------|
| 用户管理 | 注册、登录、个人信息管理 | JWT认证、BCrypt加密 |
| 训练数据 | 训练记录CRUD、负荷计算 | JPA、软删除 |
| 恢复评估 | 睡眠、压力评估、恢复建议 | 算法计算 |
| 营养追踪 | 饮食记录、营养分析 | 统计计算 |
| 数据可视化 | 图表展示、趋势分析 | ECharts |
| 管理后台 | 用户管理、系统监控、数据导出 | 权限控制 |

### 1.3 技术栈总览

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层                                │
│  Vue 3 + Vite + Element Plus + ECharts + Pinia + Axios     │
├─────────────────────────────────────────────────────────────┤
│                        网关层                                │
│  Nginx (生产) / Vite Proxy (开发)                           │
├─────────────────────────────────────────────────────────────┤
│                        应用层                                │
│  Spring Boot 3.2.5 + Spring Security + Spring Data JPA     │
├─────────────────────────────────────────────────────────────┤
│                        缓存层                                │
│  Caffeine (L1本地缓存) + Redis (L2分布式缓存，可选)          │
├─────────────────────────────────────────────────────────────┤
│                        数据层                                │
│  MySQL 8.0 + HikariCP连接池                                 │
├─────────────────────────────────────────────────────────────┤
│                        监控层                                │
│  Spring Actuator + Micrometer + Prometheus                  │
└─────────────────────────────────────────────────────────────┘
```


---

## 2. 系统架构

### 2.1 整体架构图

```
                                    ┌─────────────────┐
                                    │   用户浏览器     │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
           ┌───────────────┐       ┌───────────────┐       ┌───────────────┐
           │  用户端前端    │       │  管理端前端    │       │  移动端 H5    │
           │  :3001        │       │  :3002        │       │  响应式适配    │
           │  Vue 3        │       │  Vue 3        │       │               │
           └───────┬───────┘       └───────┬───────┘       └───────┬───────┘
                   │                       │                       │
                   └───────────────────────┼───────────────────────┘
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │    Vite Dev Proxy       │
                              │    (开发环境)            │
                              │    或 Nginx (生产)      │
                              └────────────┬────────────┘
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │   Spring Boot 后端      │
                              │   :8080                 │
                              │                         │
                              │  ┌─────────────────┐   │
                              │  │ Security Filter │   │
                              │  │ Chain           │   │
                              │  └────────┬────────┘   │
                              │           │            │
                              │  ┌────────▼────────┐   │
                              │  │ JWT Auth Filter │   │
                              │  └────────┬────────┘   │
                              │           │            │
                              │  ┌────────▼────────┐   │
                              │  │ XSS Filter      │   │
                              │  └────────┬────────┘   │
                              │           │            │
                              │  ┌────────▼────────┐   │
                              │  │ Controller      │   │
                              │  └────────┬────────┘   │
                              │           │            │
                              │  ┌────────▼────────┐   │
                              │  │ Service + AOP   │   │
                              │  └────────┬────────┘   │
                              │           │            │
                              │  ┌────────▼────────┐   │
                              │  │ Repository      │   │
                              │  └────────┬────────┘   │
                              └───────────┼───────────┘
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    │                     │                     │
                    ▼                     ▼                     ▼
           ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
           │   Caffeine    │     │    MySQL      │     │    Redis      │
           │   本地缓存     │     │   数据库       │     │  分布式缓存    │
           │   (L1)        │     │               │     │   (L2,可选)   │
           └───────────────┘     └───────────────┘     └───────────────┘
```

### 2.2 请求处理流程

```
HTTP请求 ──► CorsFilter ──► XssFilter ──► JwtAuthFilter ──► DispatcherServlet
                                                                    │
                                                                    ▼
                                                            HandlerMapping
                                                                    │
                                                                    ▼
                                                            Controller
                                                                    │
                                                                    ▼
                                                         ┌──────────────────┐
                                                         │ @Valid 参数校验   │
                                                         └────────┬─────────┘
                                                                  │
                                                                  ▼
                                                         ┌──────────────────┐
                                                         │ Service 业务逻辑  │
                                                         │ + @Cacheable     │
                                                         │ + @Transactional │
                                                         │ + @Auditable     │
                                                         └────────┬─────────┘
                                                                  │
                                                                  ▼
                                                         ┌──────────────────┐
                                                         │ Repository       │
                                                         │ + JPA/Hibernate  │
                                                         └────────┬─────────┘
                                                                  │
                                                                  ▼
                                                              Database
```

### 2.3 分层架构说明

| 层级 | 职责 | 主要类/注解 |
|------|------|-------------|
| Controller层 | 接收请求、参数校验、返回响应 | `@RestController`, `@Valid` |
| Service层 | 业务逻辑、事务管理、缓存 | `@Service`, `@Transactional`, `@Cacheable` |
| Repository层 | 数据访问、CRUD操作 | `@Repository`, `JpaRepository` |
| Entity层 | 数据模型、ORM映射 | `@Entity`, `@Table` |
| DTO层 | 数据传输对象 | Request/Response DTO |
| Config层 | 配置类 | `@Configuration`, `@Bean` |
| Security层 | 安全认证 | Filter, Provider |
| Aspect层 | 切面编程 | `@Aspect`, `@Around` |


---

## 3. 开发环境搭建

### 3.1 环境要求

| 软件 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| JDK | 17 | 17 LTS | 后端运行时 |
| Maven | 3.6 | 3.9+ | 后端构建工具 |
| Node.js | 16 | 18 LTS | 前端运行时 |
| npm | 8 | 9+ | 前端包管理 |
| MySQL | 8.0 | 8.0.33 | 生产数据库 |
| Redis | 6.0 | 7.0 | 分布式缓存（可选） |
| Docker | 20.10 | 24+ | 容器化部署（可选） |
| Git | 2.30 | 2.40+ | 版本控制 |

### 3.2 JDK 17 安装配置

#### Windows 安装

```powershell
# 1. 下载 OpenJDK 17 (推荐 Adoptium)
# https://adoptium.net/temurin/releases/?version=17

# 2. 安装后配置环境变量
# 系统属性 -> 高级 -> 环境变量

# JAVA_HOME
C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot

# Path 添加
%JAVA_HOME%\bin

# 3. 验证安装
java -version
# 输出: openjdk version "17.0.x" ...
```

#### macOS 安装

```bash
# 使用 Homebrew 安装
brew install openjdk@17

# 配置环境变量
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 验证
java -version
```

#### Linux (Ubuntu/Debian) 安装

```bash
# 安装 OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# 配置 JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# 验证
java -version
```

### 3.3 MySQL 8.0 安装配置

#### Windows 安装

```powershell
# 1. 下载 MySQL Installer
# https://dev.mysql.com/downloads/installer/

# 2. 运行安装程序，选择 "MySQL Server 8.0"
# 3. 配置 root 密码
# 4. 启动 MySQL 服务
```

#### 数据库初始化

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE fitness_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建专用用户
CREATE USER 'fitness'@'localhost' IDENTIFIED BY 'Fitness@123';

-- 授权
GRANT ALL PRIVILEGES ON fitness_db.* TO 'fitness'@'localhost';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES;
USE fitness_db;
```

### 3.4 Node.js 安装

```bash
# Windows: 下载安装包
# https://nodejs.org/

# macOS
brew install node@18

# Linux
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 验证
node -v  # v18.x.x
npm -v   # 9.x.x

# 配置 npm 镜像（可选，加速下载）
npm config set registry https://registry.npmmirror.com
```

### 3.5 项目克隆与配置

```bash
# 克隆项目
git clone <repository-url>
cd Fitness

# 后端配置
# 编辑 src/main/resources/application.properties
```

#### application.properties 关键配置

```properties
# ==================== 数据库配置 ====================
spring.datasource.url=jdbc:mysql://localhost:3306/fitness_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=fitness
spring.datasource.password=Fitness@123

# ==================== JWT配置 ====================
# 密钥至少64字符（512位），用于HS512算法
jwt.secret=mySecretKey123456789012345678901234567890mySecretKey123456789012345678901234567890
jwt.expiration=86400          # Access Token 有效期（秒）= 24小时
jwt.refresh-expiration=604800 # Refresh Token 有效期（秒）= 7天

# ==================== 服务端口 ====================
server.port=8080
```

### 3.6 启动项目

#### 方式一：使用启动脚本（推荐）

```powershell
# Windows PowerShell
cd Fitness
.\start-all.ps1

# 该脚本会依次启动：
# 1. 后端服务 (8080)
# 2. 用户端前端 (3001)
# 3. 管理端前端 (3002)
```

#### 方式二：手动启动

```bash
# 终端1：启动后端
cd Fitness
mvn clean install -DskipTests
mvn spring-boot:run

# 终端2：启动用户端前端
cd Fitness/frontend
npm install
npm run dev

# 终端3：启动管理端前端
cd Fitness/admin
npm install
npm run dev
```

### 3.7 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端API | http://localhost:8080 | Spring Boot 服务 |
| 用户端 | http://localhost:3001 | Vue 用户界面 |
| 管理端 | http://localhost:3002 | Vue 管理界面 |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档 |
| Actuator | http://localhost:8080/actuator/health | 健康检查 |
| H2 Console | http://localhost:8080/h2-console | 数据库控制台（开发） |


---

## 4. 项目结构详解

### 4.1 整体目录结构

```
Fitness/
├── admin/                          # 管理端前端 (Vue 3)
├── frontend/                       # 用户端前端 (Vue 3)
├── shared/                         # 前端共享代码
├── src/                            # 后端源码 (Spring Boot)
│   ├── main/
│   │   ├── java/com/wzl/fitness/
│   │   └── resources/
│   └── test/
├── docker/                         # Docker 配置
├── docs/                           # 项目文档
├── scripts/                        # 脚本文件
├── logs/                           # 日志目录
├── pom.xml                         # Maven 配置
├── docker-compose.yml              # Docker 编排
└── README.md                       # 项目说明
```

### 4.2 后端目录结构详解

```
src/main/java/com/wzl/fitness/
├── FitnessApplication.java         # 应用入口
│
├── annotation/                     # 自定义注解
│   ├── Auditable.java              # 审计日志注解
│   ├── DatabaseRetryable.java      # 数据库重试注解
│   ├── RequireAdmin.java           # 管理员权限注解
│   ├── RequireRole.java            # 角色权限注解
│   ├── RequireUser.java            # 用户权限注解
│   ├── SensitiveData.java          # 敏感数据脱敏注解
│   └── NoXss.java                  # XSS校验注解
│
├── aspect/                         # AOP切面
│   ├── AuditAspect.java            # 审计日志切面
│   └── DatabaseRetryAspect.java    # 数据库重试切面
│
├── common/                         # 通用组件
│   ├── ApiResponse.java            # 统一响应格式
│   ├── BaseController.java         # 基础控制器
│   ├── PageResponse.java           # 分页响应
│   └── ResponseCode.java           # 响应状态码枚举
│
├── config/                         # 配置类
│   ├── CaffeineCacheConfig.java    # Caffeine缓存配置
│   ├── RedisConfig.java            # Redis缓存配置
│   ├── SecurityConfig.java         # Spring Security配置
│   ├── SwaggerConfig.java          # Swagger/OpenAPI配置
│   ├── WebMvcConfig.java           # Web MVC配置
│   ├── DatabaseRetryConfig.java    # 数据库重试配置
│   ├── TransactionConfig.java      # 事务配置
│   ├── GracefulShutdownConfig.java # 优雅关闭配置
│   └── ConnectionPoolMetricsConfig.java # 连接池监控配置
│
├── controller/                     # 控制器层
│   ├── AuthController.java         # 认证控制器
│   ├── UserController.java         # 用户控制器
│   ├── TrainingController.java     # 训练控制器
│   ├── NutritionController.java    # 营养控制器
│   ├── LoadRecoveryController.java # 负荷恢复控制器
│   ├── DashboardController.java    # 仪表盘控制器
│   ├── HealthController.java       # 健康检查控制器
│   ├── SystemMonitorController.java # 系统监控控制器
│   ├── CacheStatsController.java   # 缓存统计控制器
│   └── DataExportController.java   # 数据导出控制器
│
├── dto/                            # 数据传输对象
│   ├── request/                    # 请求DTO
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── TrainingRecordRequest.java
│   │   └── NutritionRecordRequest.java
│   └── response/                   # 响应DTO
│       ├── LoginResponse.java
│       ├── TrainingStatsResponse.java
│       ├── NutritionStatsResponse.java
│       ├── SystemInfoDTO.java
│       ├── JvmMetricsDTO.java
│       └── CacheStatsDTO.java
│
├── entity/                         # 实体类
│   ├── BaseEntity.java             # 基础实体（软删除支持）
│   ├── User.java                   # 用户实体
│   ├── TrainingRecord.java         # 训练记录实体
│   ├── ExerciseDetail.java         # 动作详情实体
│   ├── NutritionRecord.java        # 营养记录实体
│   ├── RecoveryData.java           # 恢复数据实体
│   ├── RecoveryMetric.java         # 恢复指标实体
│   ├── TrainingAdvice.java         # 训练建议实体
│   ├── TrainingPlan.java           # 训练计划实体
│   ├── AuditLog.java               # 审计日志实体
│   └── Role.java                   # 角色枚举
│
├── exception/                      # 异常处理
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   ├── BusinessException.java      # 业务异常
│   └── ResourceNotFoundException.java # 资源未找到异常
│
├── filter/                         # 过滤器
│   ├── XssFilter.java              # XSS防护过滤器
│   ├── XssHttpServletRequestWrapper.java # XSS请求包装器
│   └── RequestTracingFilter.java   # 请求追踪过滤器
│
├── health/                         # 健康检查
│   └── DatabaseHealthIndicator.java # 数据库健康指示器
│
├── interceptor/                    # 拦截器
│   └── PermissionInterceptor.java  # 权限拦截器
│
├── model/                          # 模型类
│   ├── ConnectionPoolStatus.java   # 连接池状态
│   └── RetryEvent.java             # 重试事件
│
├── repository/                     # 数据访问层
│   ├── UserRepository.java
│   ├── TrainingRecordRepository.java
│   ├── NutritionRecordRepository.java
│   └── AuditLogRepository.java
│
├── security/                       # 安全组件
│   ├── JwtTokenProvider.java       # JWT令牌提供者
│   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   ├── JwtAuthenticationEntryPoint.java # JWT认证入口点
│   └── CustomUserDetailsService.java # 用户详情服务
│
├── serializer/                     # 序列化器
│   └── SensitiveDataSerializer.java # 敏感数据序列化器
│
├── service/                        # 业务逻辑层
│   ├── AuthenticationService.java
│   ├── UserService.java
│   ├── TrainingRecordService.java
│   ├── NutritionService.java
│   ├── DashboardService.java
│   ├── AuditLogService.java
│   ├── JwtRefreshService.java
│   ├── DataExportService.java
│   ├── UserActivityService.java
│   └── impl/                       # 服务实现
│       ├── AuthenticationServiceImpl.java
│       ├── TrainingRecordServiceImpl.java
│       ├── NutritionServiceImpl.java
│       └── ...
│
├── util/                           # 工具类
│   ├── XssUtils.java               # XSS工具类
│   ├── DataMaskingUtils.java       # 数据脱敏工具
│   └── LoggingUtils.java           # 日志工具
│
├── validation/                     # 自定义校验器
│   ├── NoXssValidator.java         # XSS校验器
│   ├── SafeHtmlValidator.java      # 安全HTML校验器
│   ├── PasswordMatchValidator.java # 密码匹配校验器
│   └── ValidDateRangeValidator.java # 日期范围校验器
│
└── vo/                             # 视图对象
    └── ...
```

### 4.3 前端目录结构详解

```
frontend/
├── public/                         # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/                        # API接口封装
│   │   ├── auth.js                 # 认证API
│   │   ├── user.js                 # 用户API
│   │   ├── fitness.js              # 健身数据API
│   │   ├── nutrition.js            # 营养API
│   │   └── request.js              # Axios封装
│   │
│   ├── assets/                     # 资源文件
│   │   ├── images/                 # 图片
│   │   └── styles/                 # 样式
│   │       ├── index.scss          # 主样式入口
│   │       ├── theme.scss          # 主题变量
│   │       ├── _variables.scss     # SCSS变量
│   │       ├── _shadows.scss       # 阴影样式
│   │       ├── _micro-interactions.scss # 微交互动画
│   │       └── _performance.scss   # 性能优化样式
│   │
│   ├── components/                 # 通用组件
│   │   ├── CalorieCalculator.vue   # 卡路里计算器
│   │   ├── DataVisualization.vue   # 数据可视化
│   │   ├── ErrorBoundary.vue       # 错误边界
│   │   ├── OneRepMaxCalculator.vue # 1RM计算器
│   │   ├── TrainingRecordManager.vue # 训练记录管理
│   │   ├── UserProfile.vue         # 用户资料
│   │   ├── FitnessPlanner.vue      # 健身计划
│   │   └── ConnectionStatusIndicator.vue # 连接状态指示器
│   │
│   ├── composables/                # 组合式函数
│   │   ├── useConnectionStatus.js  # 连接状态监控
│   │   ├── useMobileGestures.js    # 移动端手势
│   │   ├── useOfflineSync.js       # 离线同步
│   │   └── useTheme.js             # 主题切换
│   │
│   ├── directives/                 # 自定义指令
│   │   └── ...
│   │
│   ├── router/                     # 路由配置
│   │   └── index.js                # 路由定义
│   │
│   ├── stores/                     # Pinia状态管理
│   │   ├── user.js                 # 用户状态
│   │   ├── training.js             # 训练状态
│   │   └── nutrition.js            # 营养状态
│   │
│   ├── utils/                      # 工具函数
│   │   ├── errorHandler.js         # 错误处理
│   │   ├── offlineStorage.js       # 离线存储
│   │   └── ...
│   │
│   ├── views/                      # 页面视图
│   │   ├── Login.vue               # 登录页
│   │   ├── Dashboard.vue           # 仪表盘
│   │   ├── TrainingData.vue        # 训练数据
│   │   ├── NutritionTracking.vue   # 营养追踪
│   │   ├── RecoveryStatus.vue      # 恢复状态
│   │   ├── TrainingSuggestions.vue # 训练建议
│   │   ├── Settings.vue            # 设置
│   │   ├── NotFound.vue            # 404页面
│   │   └── admin/                  # 管理端页面
│   │       ├── AdminDashboard.vue
│   │       ├── UserManagement.vue
│   │       └── ...
│   │
│   ├── App.vue                     # 根组件
│   └── main.js                     # 入口文件
│
├── tests/                          # 测试配置
├── e2e/                            # E2E测试
├── package.json                    # 依赖配置
├── vite.config.js                  # Vite配置
└── vitest.config.js                # Vitest配置
```


---

## 5. 后端开发指南

### 5.1 Controller 层开发规范

#### 5.1.1 基本结构

```java
@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "训练管理", description = "训练记录相关接口")
public class TrainingController {

    private final TrainingRecordService trainingRecordService;
    private final UserService userService;

    /**
     * 创建训练记录
     */
    @PostMapping("/record")
    @Operation(summary = "创建训练记录", description = "创建新的训练记录")
    public ResponseEntity<ApiResponse<TrainingRecord>> createRecord(
            @Valid @RequestBody TrainingRecordRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        // 1. 获取当前用户
        User user = userService.getCurrentUser(authorization);
        
        // 2. 调用服务层
        TrainingRecord record = trainingRecordService.createRecord(user, request);
        
        // 3. 返回统一响应
        return ResponseEntity.ok(ApiResponse.success(record));
    }
}
```

#### 5.1.2 参数校验

```java
// 请求DTO中使用Jakarta Validation注解
public class TrainingRecordRequest {
    
    @NotBlank(message = "运动名称不能为空")
    @Size(max = 100, message = "运动名称不能超过100个字符")
    private String exerciseName;
    
    @NotNull(message = "组数不能为空")
    @Min(value = 1, message = "组数至少为1")
    @Max(value = 100, message = "组数不能超过100")
    private Integer sets;
    
    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.0", message = "重量不能为负数")
    @DecimalMax(value = "1000.0", message = "重量不能超过1000kg")
    private Double weight;
    
    @NotNull(message = "训练日期不能为空")
    @PastOrPresent(message = "训练日期不能是未来日期")
    private LocalDate trainingDate;
    
    @NoXss  // 自定义XSS校验
    @Size(max = 500, message = "备注不能超过500个字符")
    private String notes;
}
```

#### 5.1.3 统一响应格式

```java
// ApiResponse.java
@Data
@Builder
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private String timestamp;
    private boolean success;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .success(true)
                .build();
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now().toString())
                .success(false)
                .build();
    }
}
```

### 5.2 Service 层开发规范

#### 5.2.1 服务接口定义

```java
public interface TrainingRecordService {
    
    TrainingRecord createTrainingRecord(TrainingRecord record);
    
    Optional<TrainingRecord> updateTrainingRecord(Long id, TrainingRecord record);
    
    boolean deleteTrainingRecord(Long id);
    
    boolean softDeleteTrainingRecord(Long id, Long userId);
    
    boolean restoreTrainingRecord(Long id);
    
    Optional<TrainingRecord> findById(Long id);
    
    List<TrainingRecord> findByUserId(Long userId);
    
    Page<TrainingRecord> findByUserId(Long userId, int page, int size);
    
    TrainingStatsResponse getTrainingStats(Long userId, LocalDate startDate, LocalDate endDate);
}
```

#### 5.2.2 服务实现（含缓存和事务）

```java
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingRecordServiceImpl implements TrainingRecordService {
    
    private final TrainingRecordRepository trainingRecordRepository;
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public TrainingRecord createTrainingRecord(TrainingRecord record) {
        record.setDeleted(false);
        log.debug("创建训练记录，清除缓存");
        return trainingRecordRepository.save(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, key = "'user:' + #userId")
    public List<TrainingRecord> findByUserId(Long userId) {
        log.debug("从数据库查询用户 {} 的训练记录", userId);
        return trainingRecordRepository.findByUserId(userId);
    }
    
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public boolean softDeleteTrainingRecord(Long id, Long userId) {
        log.info("软删除训练记录，ID: {}, 操作用户: {}", id, userId);
        int updated = trainingRecordRepository.softDelete(id, LocalDateTime.now(), userId);
        return updated > 0;
    }
}
```

### 5.3 Repository 层开发规范

#### 5.3.1 JPA Repository 定义

```java
@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    
    // 基础查询方法（Spring Data JPA 自动实现）
    List<TrainingRecord> findByUserId(Long userId);
    
    Page<TrainingRecord> findByUserIdOrderByTrainingDateDesc(Long userId, Pageable pageable);
    
    List<TrainingRecord> findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);
    
    Long countByUserId(Long userId);
    
    // 自定义查询
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.user.id = :userId ORDER BY tr.trainingDate DESC")
    List<TrainingRecord> findTop10ByUserIdOrderByTrainingDateDesc(@Param("userId") Long userId);
    
    // 统计查询
    @Query("SELECT SUM(tr.totalVolume) FROM TrainingRecord tr " +
           "WHERE tr.user.id = :userId AND tr.trainingDate BETWEEN :startDate AND :endDate")
    Double sumVolumeByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 软删除操作
    @Modifying
    @Query("UPDATE TrainingRecord tr SET tr.deleted = true, tr.deletedAt = :deletedAt, " +
           "tr.deletedBy = :deletedBy WHERE tr.id = :id AND tr.deleted = false")
    int softDelete(@Param("id") Long id, 
                   @Param("deletedAt") LocalDateTime deletedAt,
                   @Param("deletedBy") Long deletedBy);
    
    // 恢复删除
    @Modifying
    @Query("UPDATE TrainingRecord tr SET tr.deleted = false, tr.deletedAt = null, " +
           "tr.deletedBy = null WHERE tr.id = :id AND tr.deleted = true")
    int restore(@Param("id") Long id);
}
```

### 5.4 Entity 实体类规范

#### 5.4.1 基础实体（支持软删除）

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private Long deletedBy;
    
    // Getters and Setters
}
```

#### 5.4.2 业务实体示例

```java
@Entity
@Table(name = "training_records")
@SQLRestriction("deleted = false")  // 自动过滤已删除记录
public class TrainingRecord extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  // 避免循环引用
    private User user;
    
    @NotBlank(message = "运动名称不能为空")
    @Size(max = 100)
    private String exerciseName;
    
    @NotNull
    @Min(1)
    private Integer sets;
    
    @NotNull
    @Min(1)
    private Integer reps;
    
    @NotNull
    @DecimalMin("0.0")
    private Double weight;
    
    @NotNull
    @PastOrPresent
    private LocalDate trainingDate;
    
    @Column(name = "total_volume")
    private Double totalVolume;
    
    // 计算属性
    @Transient
    public Double getCalculatedTotalVolume() {
        return sets != null && reps != null && weight != null 
            ? sets * reps * weight : 0.0;
    }
    
    // 关联关系
    @OneToMany(mappedBy = "trainingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExerciseDetail> exerciseDetails;
}
```

### 5.5 AOP 切面开发

#### 5.5.1 审计日志切面

```java
@Aspect
@Component
@Slf4j
public class AuditAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String resource = auditable.resource();
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录审计日志
            auditLogService.log(AuditLog.builder()
                    .action(action)
                    .resource(resource)
                    .success(success)
                    .errorMessage(errorMessage)
                    .duration(duration)
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}
```

#### 5.5.2 数据库重试切面

```java
@Aspect
@Component
@Slf4j
public class DatabaseRetryAspect {
    
    @Around("@annotation(databaseRetryable)")
    public Object retryDatabaseOperation(ProceedingJoinPoint joinPoint, 
                                         DatabaseRetryable databaseRetryable) throws Throwable {
        
        RetryTemplate retryTemplate = createRetryTemplate(databaseRetryable);
        
        return retryTemplate.execute(
            context -> {
                int attemptNumber = context.getRetryCount() + 1;
                
                if (attemptNumber > 1) {
                    log.info("重试数据库操作，第 {} 次尝试", attemptNumber);
                }
                
                return joinPoint.proceed();
            },
            context -> {
                // 所有重试失败后的恢复逻辑
                Throwable lastException = context.getLastThrowable();
                log.error("数据库操作重试失败，已尝试 {} 次", databaseRetryable.maxAttempts());
                throw new RuntimeException("数据库操作失败", lastException);
            }
        );
    }
    
    private RetryTemplate createRetryTemplate(DatabaseRetryable config) {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(config.initialInterval());  // 1000ms
        backOffPolicy.setMultiplier(config.multiplier());            // 2.0
        backOffPolicy.setMaxInterval(config.maxInterval());          // 30000ms
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(config.maxAttempts());
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}
```


---

## 6. 前端开发指南

### 6.1 Vue 3 组件开发规范

#### 6.1.1 组件基本结构（Composition API）

```vue
<template>
  <div class="training-record-form">
    <el-form 
      ref="formRef" 
      :model="formData" 
      :rules="rules" 
      label-width="100px"
    >
      <el-form-item label="运动名称" prop="exerciseName">
        <el-input v-model="formData.exerciseName" placeholder="请输入运动名称" />
      </el-form-item>
      
      <el-form-item label="组数" prop="sets">
        <el-input-number v-model="formData.sets" :min="1" :max="100" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { trainingApi } from '@/api/training'

// Props 定义
const props = defineProps({
  recordId: {
    type: Number,
    default: null
  },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value)
  }
})

// Emits 定义
const emit = defineEmits(['success', 'cancel'])

// 响应式状态
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  exerciseName: '',
  sets: 1,
  reps: 1,
  weight: 0,
  trainingDate: new Date(),
  notes: ''
})

// 表单校验规则
const rules = {
  exerciseName: [
    { required: true, message: '请输入运动名称', trigger: 'blur' },
    { max: 100, message: '运动名称不能超过100个字符', trigger: 'blur' }
  ],
  sets: [
    { required: true, message: '请输入组数', trigger: 'change' }
  ],
  weight: [
    { required: true, message: '请输入重量', trigger: 'change' }
  ]
}

// 计算属性
const isEditMode = computed(() => props.mode === 'edit')
const totalVolume = computed(() => formData.sets * formData.reps * formData.weight)

// 方法
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const api = isEditMode.value 
      ? trainingApi.updateRecord(props.recordId, formData)
      : trainingApi.createRecord(formData)
    
    const response = await api
    
    if (response.code === 200) {
      ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
      emit('success', response.data)
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(async () => {
  if (isEditMode.value && props.recordId) {
    await loadRecord()
  }
})

const loadRecord = async () => {
  try {
    const response = await trainingApi.getRecord(props.recordId)
    Object.assign(formData, response.data)
  } catch (error) {
    ElMessage.error('加载记录失败')
  }
}
</script>

<style lang="scss" scoped>
.training-record-form {
  padding: 20px;
  
  .el-form-item {
    margin-bottom: 20px;
  }
}
</style>
```

### 6.2 API 请求封装

#### 6.2.1 Axios 实例配置

```javascript
// shared/api/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '',  // 使用 Vite 代理
  timeout: 30000,
  withCredentials: true
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加 Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = { ...config.params, _t: Date.now() }
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => response.data,
  async error => {
    const status = error.response?.status
    
    switch (status) {
      case 401:
        // Token 过期，尝试刷新
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken && !error.config._retry) {
          error.config._retry = true
          try {
            const newToken = await refreshAccessToken(refreshToken)
            error.config.headers['Authorization'] = `Bearer ${newToken}`
            return service(error.config)
          } catch (refreshError) {
            handleSessionExpired()
          }
        } else {
          handleSessionExpired()
        }
        break
      case 403:
        ElMessage.error('没有权限访问该资源')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        ElMessage.error(`请求失败: ${status}`)
    }
    
    return Promise.reject(error)
  }
)

// 刷新 Token
const refreshAccessToken = async (refreshToken) => {
  const response = await axios.post('/api/v1/auth/refresh', { refreshToken })
  const newToken = response.data.data.accessToken
  localStorage.setItem('token', newToken)
  return newToken
}

// 处理会话过期
const handleSessionExpired = async () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  
  await ElMessageBox.alert('登录已过期，请重新登录', '会话过期', {
    confirmButtonText: '重新登录',
    type: 'warning'
  })
  
  window.location.href = '/#/login'
}

// 封装请求方法
export const get = (url, params = {}, options = {}) => {
  return service.get(url, { params, ...options })
}

export const post = (url, data = {}) => {
  return service.post(url, data)
}

export const put = (url, data = {}) => {
  return service.put(url, data)
}

export const del = (url, params = {}) => {
  return service.delete(url, { params })
}

export default service
```

#### 6.2.2 API 模块封装

```javascript
// api/training.js
import { get, post, put, del } from '@/shared/api/request'

export const trainingApi = {
  // 获取训练记录列表
  getRecords: (params) => get('/api/v1/training/records', params),
  
  // 获取训练记录（分页）
  getRecordsPaged: (page = 0, size = 20) => 
    get('/api/v1/training/records/page', { page, size }),
  
  // 获取单条记录
  getRecord: (id) => get(`/api/v1/training/records/${id}`),
  
  // 创建记录
  createRecord: (data) => post('/api/v1/training/record', data),
  
  // 更新记录
  updateRecord: (id, data) => put(`/api/v1/training/records/${id}`, data),
  
  // 删除记录
  deleteRecord: (id) => del(`/api/v1/training/records/${id}`),
  
  // 软删除记录
  softDeleteRecord: (id) => post(`/api/v1/training/records/${id}/soft-delete`),
  
  // 恢复记录
  restoreRecord: (id) => post(`/api/v1/training/records/${id}/restore`),
  
  // 获取训练统计
  getStats: (startDate, endDate) => 
    get('/api/v1/training/stats', { startDate, endDate }),
  
  // 获取最近训练记录
  getRecentRecords: () => get('/api/v1/training/records/recent')
}
```

### 6.3 Pinia 状态管理

```javascript
// stores/training.js
import { defineStore } from 'pinia'
import { trainingApi } from '@/api/training'

export const useTrainingStore = defineStore('training', {
  state: () => ({
    records: [],
    currentRecord: null,
    stats: null,
    loading: false,
    error: null,
    pagination: {
      page: 0,
      size: 20,
      total: 0
    }
  }),

  getters: {
    // 获取今日训练记录
    todayRecords: (state) => {
      const today = new Date().toISOString().split('T')[0]
      return state.records.filter(r => r.trainingDate === today)
    },
    
    // 计算总训练量
    totalVolume: (state) => {
      return state.records.reduce((sum, r) => sum + (r.totalVolume || 0), 0)
    },
    
    // 是否有更多数据
    hasMore: (state) => {
      return state.records.length < state.pagination.total
    }
  },

  actions: {
    // 获取训练记录
    async fetchRecords(params = {}) {
      this.loading = true
      this.error = null
      
      try {
        const response = await trainingApi.getRecordsPaged(
          params.page || this.pagination.page,
          params.size || this.pagination.size
        )
        
        if (response.code === 200) {
          this.records = response.data.content
          this.pagination.total = response.data.totalElements
        }
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 创建训练记录
    async createRecord(data) {
      this.loading = true
      
      try {
        const response = await trainingApi.createRecord(data)
        if (response.code === 200) {
          this.records.unshift(response.data)
          return response.data
        }
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 删除训练记录
    async deleteRecord(id) {
      try {
        await trainingApi.softDeleteRecord(id)
        this.records = this.records.filter(r => r.id !== id)
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    // 获取训练统计
    async fetchStats(startDate, endDate) {
      try {
        const response = await trainingApi.getStats(startDate, endDate)
        if (response.code === 200) {
          this.stats = response.data
        }
      } catch (error) {
        this.error = error.message
      }
    },
    
    // 清空状态
    clearState() {
      this.records = []
      this.currentRecord = null
      this.stats = null
      this.error = null
    }
  }
})
```

### 6.4 路由配置与守卫

```javascript
// router/index.js
import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '仪表盘', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AFitness` : 'AFitness'
  
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  const routeRole = to.meta.role
  const userRole = getUserRole()
  
  // 需要认证但没有 token
  if (requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 已登录访问登录页
  if (to.path === '/login' && token) {
    next(userRole === 'ADMIN' ? '/admin/dashboard' : '/dashboard')
    return
  }
  
  // 检查角色权限
  if (routeRole && token) {
    if (routeRole === 'ADMIN' && userRole !== 'ADMIN') {
      next('/dashboard')
      return
    }
  }
  
  next()
})

function getUserRole() {
  try {
    const userInfo = localStorage.getItem('userInfo')
    return userInfo ? JSON.parse(userInfo).role : null
  } catch {
    return null
  }
}

export default router
```

### 6.5 Composables 组合式函数

```javascript
// composables/useConnectionStatus.js
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { ElNotification, ElMessage } from 'element-plus'

export const ConnectionStatus = {
  CONNECTED: 'connected',
  DISCONNECTED: 'disconnected',
  CHECKING: 'checking',
  OFFLINE: 'offline'
}

const globalStatus = ref(ConnectionStatus.UNKNOWN)
const globalIsOnline = ref(navigator.onLine)
let checkInterval = null

export function useConnectionStatus(options = {}) {
  const { checkInterval: interval = 30000, autoStart = true } = options
  
  const isConnected = computed(() => globalStatus.value === ConnectionStatus.CONNECTED)
  const isOffline = computed(() => !globalIsOnline.value)
  
  const statusText = computed(() => {
    switch (globalStatus.value) {
      case ConnectionStatus.CONNECTED: return '已连接'
      case ConnectionStatus.DISCONNECTED: return '连接断开'
      case ConnectionStatus.CHECKING: return '检查中...'
      case ConnectionStatus.OFFLINE: return '离线'
      default: return '未知'
    }
  })
  
  const checkConnection = async () => {
    if (!navigator.onLine) {
      globalStatus.value = ConnectionStatus.OFFLINE
      return
    }
    
    globalStatus.value = ConnectionStatus.CHECKING
    
    try {
      const response = await axios.get('/api/v1/health', { timeout: 5000 })
      globalStatus.value = response.data?.code === 200 
        ? ConnectionStatus.CONNECTED 
        : ConnectionStatus.DISCONNECTED
    } catch {
      globalStatus.value = ConnectionStatus.DISCONNECTED
    }
  }
  
  const handleOnline = () => {
    globalIsOnline.value = true
    ElMessage.success('网络连接已恢复')
    checkConnection()
  }
  
  const handleOffline = () => {
    globalIsOnline.value = false
    globalStatus.value = ConnectionStatus.OFFLINE
    ElNotification.warning({
      title: '网络连接已断开',
      message: '部分功能可能无法使用',
      duration: 0
    })
  }
  
  onMounted(() => {
    if (autoStart) {
      window.addEventListener('online', handleOnline)
      window.addEventListener('offline', handleOffline)
      checkConnection()
      checkInterval = setInterval(checkConnection, interval)
    }
  })
  
  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)
    if (checkInterval) clearInterval(checkInterval)
  })
  
  return {
    status: globalStatus,
    isConnected,
    isOffline,
    statusText,
    checkNow: checkConnection
  }
}
```


---

## 7. 数据库设计

### 7.1 ER 图

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   user_table    │       │training_records │       │exercise_details │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──┐   │ id (PK)         │◄──┐   │ id (PK)         │
│ username        │   │   │ user_id (FK)    │───┘   │ record_id (FK)  │───┐
│ password        │   │   │ exercise_name   │       │ exercise_name   │   │
│ email           │   │   │ sets            │       │ weight          │   │
│ role            │   │   │ reps            │       │ sets            │   │
│ created_at      │   │   │ weight          │       │ reps            │   │
│ updated_at      │   │   │ training_date   │       │ rpe             │   │
│ age             │   │   │ duration        │       │ volume          │   │
│ weight          │   │   │ notes           │       │ created_at      │   │
│ height          │   │   │ total_volume    │       └─────────────────┘   │
│ gender          │   │   │ training_stress │                             │
└─────────────────┘   │   │ deleted         │                             │
                      │   │ deleted_at      │                             │
                      │   │ created_at      │                             │
                      │   └─────────────────┘                             │
                      │                                                   │
                      │   ┌─────────────────┐       ┌─────────────────┐   │
                      │   │nutrition_records│       │  recovery_data  │   │
                      │   ├─────────────────┤       ├─────────────────┤   │
                      │   │ id (PK)         │       │ id (PK)         │   │
                      └───│ user_id (FK)    │       │ user_id (FK)    │───┘
                          │ record_date     │       │ timestamp       │
                          │ meal_type       │       │ recovery_score  │
                          │ food_name       │       │ sleep_hours     │
                          │ calories        │       │ sleep_quality   │
                          │ protein         │       │ stress_level    │
                          │ carbs           │       │ muscle_soreness │
                          │ fat             │       │ notes           │
                          │ fiber           │       │ created_at      │
                          │ amount          │       └─────────────────┘
                          │ notes           │
                          │ created_at      │
                          └─────────────────┘
```

### 7.2 核心表结构

#### 7.2.1 用户表 (user_table)

```sql
CREATE TABLE user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码(BCrypt)',
    email VARCHAR(100) COMMENT '邮箱地址',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    age INT DEFAULT 25 COMMENT '年龄',
    weight DOUBLE DEFAULT 70.0 COMMENT '体重(kg)',
    height INT COMMENT '身高(cm)',
    gender VARCHAR(10) COMMENT '性别',
    experience_level VARCHAR(50) COMMENT '训练经验等级',
    avatar VARCHAR(255) COMMENT '头像URL',
    points INT DEFAULT 0 COMMENT '用户积分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### 7.2.2 训练记录表 (training_records)

```sql
CREATE TABLE training_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    exercise_name VARCHAR(100) NOT NULL COMMENT '运动名称',
    sets INT NOT NULL COMMENT '组数',
    reps INT NOT NULL COMMENT '次数',
    weight DOUBLE NOT NULL COMMENT '重量(kg)',
    training_date DATE NOT NULL COMMENT '训练日期',
    duration INT NOT NULL COMMENT '训练时长(分钟)',
    notes VARCHAR(500) COMMENT '备注',
    total_volume DOUBLE COMMENT '总训练量(sets*reps*weight)',
    training_stress DOUBLE COMMENT '训练压力指数',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    deleted_at TIMESTAMP COMMENT '删除时间',
    deleted_by BIGINT COMMENT '删除操作人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_training_date (training_date),
    INDEX idx_exercise_name (exercise_name),
    INDEX idx_user_date (user_id, training_date),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练记录表';
```

#### 7.2.3 营养记录表 (nutrition_records)

```sql
CREATE TABLE nutrition_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    meal_type VARCHAR(20) NOT NULL COMMENT '餐次(早餐/午餐/晚餐/加餐)',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    calories INT COMMENT '热量(kcal)',
    protein DOUBLE COMMENT '蛋白质(g)',
    carbs DOUBLE COMMENT '碳水化合物(g)',
    fat DOUBLE COMMENT '脂肪(g)',
    fiber DOUBLE COMMENT '纤维(g)',
    sugar DOUBLE COMMENT '糖分(g)',
    sodium DOUBLE COMMENT '钠(mg)',
    amount DOUBLE NOT NULL COMMENT '份量(g)',
    notes VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    INDEX idx_meal_type (meal_type),
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营养记录表';
```

#### 7.2.4 审计日志表 (audit_logs)

```sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource VARCHAR(100) COMMENT '操作资源',
    resource_id VARCHAR(50) COMMENT '资源ID',
    details TEXT COMMENT '操作详情(JSON)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    duration BIGINT COMMENT '操作耗时(ms)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
```

### 7.3 索引策略

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| user_table | PRIMARY | id | 主键 | 自增主键 |
| user_table | idx_username | username | 唯一索引 | 用户名唯一 |
| user_table | idx_email | email | 普通索引 | 邮箱查询 |
| training_records | idx_user_date | user_id, training_date | 复合索引 | 用户+日期查询 |
| training_records | idx_deleted | deleted | 普通索引 | 软删除过滤 |
| nutrition_records | idx_user_date | user_id, record_date | 复合索引 | 用户+日期查询 |
| audit_logs | idx_created_at | created_at | 普通索引 | 时间范围查询 |

### 7.4 数据库配置优化

```properties
# HikariCP 连接池配置
spring.datasource.hikari.maximum-pool-size=20      # 最大连接数
spring.datasource.hikari.minimum-idle=5            # 最小空闲连接
spring.datasource.hikari.idle-timeout=300000       # 空闲超时(5分钟)
spring.datasource.hikari.max-lifetime=1200000      # 连接最大生命周期(20分钟)
spring.datasource.hikari.connection-timeout=30000  # 连接超时(30秒)
spring.datasource.hikari.validation-timeout=5000   # 验证超时(5秒)
spring.datasource.hikari.leak-detection-threshold=60000  # 泄漏检测阈值(1分钟)

# JPA/Hibernate 优化
spring.jpa.hibernate.ddl-auto=update              # 自动更新表结构
spring.jpa.properties.hibernate.jdbc.batch_size=20 # 批量操作大小
spring.jpa.properties.hibernate.order_inserts=true # 批量插入排序
spring.jpa.properties.hibernate.order_updates=true # 批量更新排序
spring.jpa.properties.hibernate.jdbc.fetch_size=100 # 查询批量大小
spring.jpa.open-in-view=false                     # 关闭OSIV
```


---

## 8. API接口规范

### 8.1 RESTful 设计规范

| HTTP方法 | 操作 | 示例 | 说明 |
|----------|------|------|------|
| GET | 查询 | GET /api/v1/training/records | 获取资源列表 |
| GET | 查询 | GET /api/v1/training/records/{id} | 获取单个资源 |
| POST | 创建 | POST /api/v1/training/record | 创建新资源 |
| PUT | 全量更新 | PUT /api/v1/training/records/{id} | 更新整个资源 |
| PATCH | 部分更新 | PATCH /api/v1/training/records/{id} | 更新部分字段 |
| DELETE | 删除 | DELETE /api/v1/training/records/{id} | 删除资源 |

### 8.2 统一响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "exerciseName": "深蹲",
    "sets": 4,
    "reps": 8,
    "weight": 100.0
  },
  "timestamp": "2026-01-07 12:00:00",
  "success": true
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "timestamp": "2026-01-07 12:00:00",
  "success": true
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "请求参数验证失败",
  "data": {
    "fieldErrors": {
      "exerciseName": {
        "field": "exerciseName",
        "message": "运动名称不能为空",
        "rejectedValue": null,
        "constraintType": "Required"
      }
    }
  },
  "timestamp": "2026-01-07 12:00:00",
  "success": false
}
```

### 8.3 错误码定义

| 错误码 | HTTP状态 | 说明 | 处理建议 |
|--------|----------|------|----------|
| 200 | 200 | 操作成功 | - |
| 400 | 400 | 参数错误 | 检查请求参数 |
| 401 | 401 | 未授权 | 重新登录 |
| 403 | 403 | 权限不足 | 检查用户权限 |
| 404 | 404 | 资源不存在 | 检查资源ID |
| 500 | 500 | 服务器错误 | 联系管理员 |
| 1001 | 400 | 用户不存在 | 检查用户名 |
| 1002 | 400 | 用户名已存在 | 更换用户名 |
| 1003 | 400 | 邮箱已存在 | 更换邮箱 |
| 1005 | 400 | 密码错误 | 检查密码 |

### 8.4 认证方式

#### JWT Token 认证

```http
# 请求头
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...
```

#### Token 刷新流程

```
1. Access Token 过期 (401)
       │
       ▼
2. 使用 Refresh Token 调用 /api/v1/auth/refresh
       │
       ▼
3. 获取新的 Access Token
       │
       ▼
4. 重试原请求
```

### 8.5 主要API端点

#### 认证模块 (/api/v1/auth)

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| POST | /login | 用户登录 | 否 |
| POST | /register | 用户注册 | 否 |
| POST | /refresh | 刷新Token | 否 |
| POST | /logout | 用户登出 | 是 |
| GET | /me | 获取当前用户 | 是 |
| GET | /check-username | 检查用户名 | 否 |

#### 训练模块 (/api/v1/training)

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | /records | 获取训练记录列表 | 是 |
| GET | /records/page | 分页获取训练记录 | 是 |
| GET | /records/{id} | 获取单条记录 | 是 |
| POST | /record | 创建训练记录 | 是 |
| PUT | /records/{id} | 更新训练记录 | 是 |
| DELETE | /records/{id} | 删除训练记录 | 是 |
| POST | /records/{id}/soft-delete | 软删除记录 | 是 |
| POST | /records/{id}/restore | 恢复记录 | 是 |
| GET | /stats | 获取训练统计 | 是 |

#### 营养模块 (/api/v1/nutrition)

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | /records/{date} | 获取指定日期记录 | 是 |
| POST | /records | 添加营养记录 | 是 |
| PUT | /records/{id} | 更新营养记录 | 是 |
| DELETE | /records/{id} | 删除营养记录 | 是 |
| GET | /stats/{date} | 获取营养统计 | 是 |
| GET | /recommendation | 获取营养建议 | 是 |

#### 负荷恢复模块 (/api/v1/load-recovery)

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | /one-rep-max | 计算1RM | 是 |
| GET | /one-rep-max/models | 获取计算模型 | 是 |
| POST | /training-data | 保存训练数据 | 是 |
| POST | /recovery-assessment | 评估恢复状态 | 是 |
| GET | /training-suggestions | 获取训练建议 | 是 |
| GET | /load-trend | 获取负荷趋势 | 是 |

#### 管理模块 (/api/v1/admin) - 需要ADMIN角色

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | /monitor/system-info | 获取系统信息 |
| GET | /monitor/jvm-metrics | 获取JVM指标 |
| GET | /monitor/database-stats | 获取数据库统计 |
| GET | /monitor/user-activity | 获取用户活跃度 |
| GET | /cache/stats | 获取缓存统计 |
| POST | /cache/evict/{cacheName} | 清除指定缓存 |
| GET | /export/users | 导出用户数据 |
| GET | /export/training-records | 导出训练记录 |


---

## 9. 安全机制

### 9.1 认证与授权架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     Security Filter Chain                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │ CorsFilter   │───►│ XssFilter    │───►│JwtAuthFilter │       │
│  │ (CORS处理)   │    │ (XSS防护)    │    │ (JWT认证)    │       │
│  └──────────────┘    └──────────────┘    └──────┬───────┘       │
│                                                  │               │
│                                                  ▼               │
│                                          ┌──────────────┐       │
│                                          │ Authorization│       │
│                                          │ (权限检查)    │       │
│                                          └──────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 JWT 认证实现

#### 9.2.1 Token 生成

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400}")
    private int jwtExpirationInSeconds;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSeconds * 1000L);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

#### 9.2.2 JWT 过滤器

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 跳过公开端点
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = getTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT认证失败", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/public/") ||
               path.equals("/actuator/health");
    }
}
```

### 9.3 密码安全

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 算法，强度因子 12
        return new BCryptPasswordEncoder(12);
    }
}

// 使用示例
@Service
public class AuthenticationServiceImpl {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### 9.4 XSS 防护

#### 9.4.1 XSS 过滤器

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(request);
        filterChain.doFilter(wrappedRequest, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/") || path.endsWith(".css") || path.endsWith(".js");
    }
}
```

#### 9.4.2 XSS 请求包装器

```java
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return XssUtils.cleanXss(value);
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        
        return Arrays.stream(values)
                .map(XssUtils::cleanXss)
                .toArray(String[]::new);
    }
    
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return XssUtils.cleanXss(value);
    }
}
```

#### 9.4.3 XSS 工具类

```java
public class XssUtils {
    
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern EVENT_PATTERN = Pattern.compile(
        "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    
    public static String cleanXss(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // 移除 script 标签
        value = SCRIPT_PATTERN.matcher(value).replaceAll("");
        
        // 移除事件处理器
        value = EVENT_PATTERN.matcher(value).replaceAll("");
        
        // HTML 实体编码
        value = value.replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;");
        
        return value;
    }
}
```

### 9.5 敏感数据脱敏

```java
// 自定义注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveDataSerializer.class)
public @interface SensitiveData {
    SensitiveType type() default SensitiveType.DEFAULT;
    
    enum SensitiveType {
        DEFAULT,      // 默认：中间用*替换
        EMAIL,        // 邮箱：u***@example.com
        PHONE,        // 手机：138****1234
        ID_CARD,      // 身份证：110***********1234
        BANK_CARD     // 银行卡：6222***********1234
    }
}

// 序列化器
public class SensitiveDataSerializer extends JsonSerializer<String> {
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) 
            throws IOException {
        
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }
        
        // 获取注解
        SensitiveData annotation = // ... 获取注解
        String masked = DataMaskingUtils.mask(value, annotation.type());
        gen.writeString(masked);
    }
}

// 使用示例
@Entity
public class User {
    
    @SensitiveData(type = SensitiveType.EMAIL)
    private String email;  // 输出: u***@example.com
    
    @SensitiveData(type = SensitiveType.PHONE)
    private String phone;  // 输出: 138****1234
}
```

### 9.6 CORS 配置

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 允许的前端源
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3001",  // 用户端
        "http://localhost:3002",  // 管理端
        "http://localhost:8080"
    ));
    
    // 允许的 HTTP 方法
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // 允许的请求头
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization", "Content-Type", "X-Requested-With"
    ));
    
    // 允许携带凭证
    configuration.setAllowCredentials(true);
    
    // 预检请求缓存时间
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```


---

## 10. 缓存策略

### 10.1 多级缓存架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        请求处理流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  请求 ──► Caffeine (L1)                                         │
│              │                                                   │
│              │ 未命中                                            │
│              ▼                                                   │
│          Redis (L2) ◄── 可选，分布式场景                         │
│              │                                                   │
│              │ 未命中                                            │
│              ▼                                                   │
│          MySQL (持久化)                                          │
│              │                                                   │
│              │ 查询结果                                          │
│              ▼                                                   │
│          回填缓存 (L2 → L1)                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Caffeine 本地缓存配置

```java
@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    
    // 缓存名称常量
    public static final String CACHE_USERS = "users";
    public static final String CACHE_TRAINING_RECORDS = "trainingRecords";
    public static final String CACHE_NUTRITION_STATS = "nutritionStats";
    public static final String CACHE_DASHBOARD_METRICS = "dashboardMetrics";
    
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)           // 初始容量
            .maximumSize(1000)              // 最大缓存条目数
            .expireAfterWrite(Duration.ofMinutes(30))  // 写入后30分钟过期
            .expireAfterAccess(Duration.ofMinutes(15)) // 访问后15分钟过期
            .recordStats());                // 启用统计
        
        cacheManager.setCacheNames(Arrays.asList(
            CACHE_USERS,
            CACHE_TRAINING_RECORDS,
            CACHE_NUTRITION_STATS,
            CACHE_DASHBOARD_METRICS
        ));
        
        return cacheManager;
    }
}
```

### 10.3 Redis 分布式缓存配置

```java
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
    
    @Bean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // 不同缓存区域的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户信息缓存 - 30分钟TTL
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 训练记录缓存 - 5分钟TTL（数据变化频繁）
        cacheConfigurations.put("trainingRecordCache", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 仪表盘统计缓存 - 2分钟TTL
        cacheConfigurations.put("dashboardStatsCache", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // 食物数据库缓存 - 24小时TTL（静态数据）
        cacheConfigurations.put("foodDatabaseCache", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .enableStatistics()
                .build();
    }
}
```

### 10.4 缓存注解使用

```java
@Service
public class TrainingRecordServiceImpl implements TrainingRecordService {
    
    // 查询时使用缓存
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, 
               key = "'user:' + #userId")
    public List<TrainingRecord> findByUserId(Long userId) {
        log.debug("从数据库查询用户 {} 的训练记录", userId);
        return trainingRecordRepository.findByUserId(userId);
    }
    
    // 带条件的缓存
    @Cacheable(value = CACHE_TRAINING_RECORDS, 
               key = "'user:' + #userId + ':range:' + #startDate + ':' + #endDate",
               condition = "#startDate != null && #endDate != null")
    public List<TrainingRecord> findByUserIdAndDateRange(Long userId, 
                                                          LocalDate startDate, 
                                                          LocalDate endDate) {
        return trainingRecordRepository.findByUserIdAndTrainingDateBetween(userId, startDate, endDate);
    }
    
    // 创建/更新时清除缓存
    @Override
    @CacheEvict(value = CaffeineCacheConfig.CACHE_TRAINING_RECORDS, allEntries = true)
    public TrainingRecord createTrainingRecord(TrainingRecord record) {
        log.debug("创建训练记录，清除缓存");
        return trainingRecordRepository.save(record);
    }
    
    // 更新缓存
    @CachePut(value = CACHE_TRAINING_RECORDS, key = "'record:' + #result.id")
    public TrainingRecord updateTrainingRecord(Long id, TrainingRecord record) {
        // 更新逻辑
        return trainingRecordRepository.save(record);
    }
    
    // 组合缓存操作
    @Caching(
        evict = {
            @CacheEvict(value = CACHE_TRAINING_RECORDS, key = "'user:' + #userId"),
            @CacheEvict(value = CACHE_DASHBOARD_METRICS, key = "'user:' + #userId")
        }
    )
    public boolean deleteTrainingRecord(Long id, Long userId) {
        return trainingRecordRepository.deleteById(id) > 0;
    }
}
```

### 10.5 缓存统计与监控

```java
@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
public class CacheStatsController {
    
    private final CacheManager cacheManager;
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, CacheStatsDTO>>> getCacheStats() {
        Map<String, CacheStatsDTO> statsMap = new HashMap<>();
        
        if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
            for (String cacheName : caffeineCacheManager.getCacheNames()) {
                var cache = caffeineCacheManager.getCache(cacheName);
                if (cache != null) {
                    var nativeCache = cache.getNativeCache();
                    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
                        CacheStats stats = caffeineCache.stats();
                        statsMap.put(cacheName, CacheStatsDTO.builder()
                                .hitCount(stats.hitCount())
                                .missCount(stats.missCount())
                                .hitRate(stats.hitRate())
                                .evictionCount(stats.evictionCount())
                                .estimatedSize(caffeineCache.estimatedSize())
                                .build());
                    }
                }
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(statsMap));
    }
    
    @PostMapping("/evict/{cacheName}")
    public ResponseEntity<ApiResponse<String>> evictCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok(ApiResponse.success("缓存已清除: " + cacheName));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "缓存不存在"));
    }
}
```

### 10.6 缓存最佳实践

| 场景 | 策略 | TTL | 说明 |
|------|------|-----|------|
| 用户信息 | 读多写少 | 30分钟 | 用户资料变化不频繁 |
| 训练记录 | 读写均衡 | 5分钟 | 数据变化较频繁 |
| 仪表盘统计 | 实时性要求高 | 2分钟 | 需要较新数据 |
| 食物数据库 | 静态数据 | 24小时 | 基础数据很少变化 |
| 训练建议 | 计算密集 | 1小时 | AI建议不需频繁更新 |

**缓存穿透防护：**
```java
// 缓存空值
@Cacheable(value = "userCache", key = "#id", unless = "#result == null")
public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
}
```

**缓存雪崩防护：**
```java
// 随机TTL
Duration ttl = Duration.ofMinutes(30 + new Random().nextInt(10));
```


---

## 11. 测试指南

### 11.1 测试架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        测试金字塔                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                         ┌─────────┐                             │
│                         │  E2E    │  ◄── Playwright             │
│                         │  测试   │      端到端测试              │
│                       ┌─┴─────────┴─┐                           │
│                       │   集成测试   │  ◄── Spring Boot Test    │
│                       │             │      API集成测试           │
│                     ┌─┴─────────────┴─┐                         │
│                     │    单元测试      │  ◄── JUnit + Vitest    │
│                     │                 │      业务逻辑测试        │
│                   ┌─┴─────────────────┴─┐                       │
│                   │     属性测试         │  ◄── jqwik           │
│                   │                     │      边界条件测试      │
│                   └─────────────────────┘                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.2 后端单元测试

#### 11.2.1 Service 层测试

```java
@ExtendWith(MockitoExtension.class)
class TrainingRecordServiceTest {
    
    @Mock
    private TrainingRecordRepository trainingRecordRepository;
    
    @InjectMocks
    private TrainingRecordServiceImpl trainingRecordService;
    
    @Test
    @DisplayName("创建训练记录 - 成功")
    void createTrainingRecord_Success() {
        // Given
        TrainingRecord record = new TrainingRecord();
        record.setExerciseName("深蹲");
        record.setSets(4);
        record.setReps(8);
        record.setWeight(100.0);
        
        when(trainingRecordRepository.save(any(TrainingRecord.class)))
            .thenReturn(record);
        
        // When
        TrainingRecord result = trainingRecordService.createTrainingRecord(record);
        
        // Then
        assertNotNull(result);
        assertEquals("深蹲", result.getExerciseName());
        assertEquals(4, result.getSets());
        verify(trainingRecordRepository, times(1)).save(any());
    }
    
    @Test
    @DisplayName("查询用户训练记录 - 返回列表")
    void findByUserId_ReturnsRecords() {
        // Given
        Long userId = 1L;
        List<TrainingRecord> records = Arrays.asList(
            createRecord("深蹲", 4, 8, 100.0),
            createRecord("卧推", 3, 10, 80.0)
        );
        
        when(trainingRecordRepository.findByUserId(userId)).thenReturn(records);
        
        // When
        List<TrainingRecord> result = trainingRecordService.findByUserId(userId);
        
        // Then
        assertEquals(2, result.size());
        assertEquals("深蹲", result.get(0).getExerciseName());
    }
    
    @Test
    @DisplayName("软删除训练记录 - 成功")
    void softDeleteTrainingRecord_Success() {
        // Given
        Long recordId = 1L;
        Long userId = 1L;
        
        when(trainingRecordRepository.softDelete(eq(recordId), any(), eq(userId)))
            .thenReturn(1);
        
        // When
        boolean result = trainingRecordService.softDeleteTrainingRecord(recordId, userId);
        
        // Then
        assertTrue(result);
        verify(trainingRecordRepository).softDelete(eq(recordId), any(), eq(userId));
    }
    
    private TrainingRecord createRecord(String name, int sets, int reps, double weight) {
        TrainingRecord record = new TrainingRecord();
        record.setExerciseName(name);
        record.setSets(sets);
        record.setReps(reps);
        record.setWeight(weight);
        return record;
    }
}
```

#### 11.2.2 Controller 层集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrainingControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TrainingRecordService trainingRecordService;
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("创建训练记录 - 成功")
    void createTrainingRecord_Success() throws Exception {
        // Given
        TrainingRecordRequest request = new TrainingRecordRequest();
        request.setExerciseName("深蹲");
        request.setSets(4);
        request.setReps(8);
        request.setWeight(100.0);
        request.setTrainingDate(LocalDate.now());
        
        TrainingRecord savedRecord = new TrainingRecord();
        savedRecord.setId(1L);
        savedRecord.setExerciseName("深蹲");
        
        when(trainingRecordService.createTrainingRecord(any())).thenReturn(savedRecord);
        
        // When & Then
        mockMvc.perform(post("/api/v1/training/record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.exerciseName").value("深蹲"));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("创建训练记录 - 参数校验失败")
    void createTrainingRecord_ValidationFailed() throws Exception {
        // Given - 缺少必填字段
        TrainingRecordRequest request = new TrainingRecordRequest();
        request.setExerciseName("");  // 空字符串
        
        // When & Then
        mockMvc.perform(post("/api/v1/training/record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    
    @Test
    @DisplayName("未认证访问 - 返回401")
    void accessWithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/training/records"))
                .andExpect(status().isUnauthorized());
    }
}
```

### 11.3 属性测试 (Property-Based Testing)

```java
@PropertyDefaults(tries = 100)
class InputValidationPropertyTest {
    
    @Property
    @Label("用户名长度在3-50之间应该有效")
    void validUsernameLengthShouldBeAccepted(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username) {
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("ValidPassword123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = 
            validator.validate(request);
        
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        
        assertFalse(hasUsernameViolation, 
            "用户名 '" + username + "' 应该是有效的");
    }
    
    @Property
    @Label("训练重量应该在合理范围内")
    void trainingWeightShouldBeInValidRange(
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double weight) {
        
        TrainingRecordRequest request = new TrainingRecordRequest();
        request.setWeight(weight);
        
        Set<ConstraintViolation<TrainingRecordRequest>> violations = 
            validator.validate(request);
        
        boolean hasWeightViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("weight"));
        
        assertFalse(hasWeightViolation);
    }
    
    @Property
    @Label("XSS攻击字符串应该被过滤")
    void xssStringShouldBeSanitized(
            @ForAll("xssPayloads") String xssPayload) {
        
        String sanitized = XssUtils.cleanXss(xssPayload);
        
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("javascript:"));
        assertFalse(sanitized.matches(".*on\\w+=.*"));
    }
    
    @Provide
    Arbitrary<String> xssPayloads() {
        return Arbitraries.of(
            "<script>alert('xss')</script>",
            "<img src=x onerror=alert('xss')>",
            "javascript:alert('xss')",
            "<svg onload=alert('xss')>",
            "'\"><script>alert('xss')</script>"
        );
    }
}
```

### 11.4 前端单元测试

```javascript
// components/TrainingRecordForm.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TrainingRecordForm from '@/components/TrainingRecordForm.vue'
import ElementPlus from 'element-plus'

describe('TrainingRecordForm', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  
  it('渲染表单字段', () => {
    const wrapper = mount(TrainingRecordForm, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    expect(wrapper.find('input[placeholder="请输入运动名称"]').exists()).toBe(true)
    expect(wrapper.find('.el-input-number').exists()).toBe(true)
  })
  
  it('提交表单时触发事件', async () => {
    const wrapper = mount(TrainingRecordForm, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    // 填写表单
    await wrapper.find('input[placeholder="请输入运动名称"]').setValue('深蹲')
    
    // 提交表单
    await wrapper.find('button[type="submit"]').trigger('click')
    
    // 验证事件
    expect(wrapper.emitted('submit')).toBeTruthy()
  })
  
  it('显示验证错误', async () => {
    const wrapper = mount(TrainingRecordForm, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    // 不填写必填字段直接提交
    await wrapper.find('button[type="submit"]').trigger('click')
    
    // 等待验证
    await wrapper.vm.$nextTick()
    
    // 验证错误消息
    expect(wrapper.find('.el-form-item__error').exists()).toBe(true)
  })
})
```

### 11.5 E2E 测试

```javascript
// e2e/training.spec.js
import { test, expect } from '@playwright/test'

test.describe('训练记录功能', () => {
  
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/#/login')
    await page.fill('input[placeholder="用户名"]', 'testuser')
    await page.fill('input[placeholder="密码"]', 'password123')
    await page.click('button:has-text("登录")')
    
    // 等待跳转到仪表盘
    await expect(page).toHaveURL(/.*dashboard/)
  })
  
  test('创建训练记录', async ({ page }) => {
    // 导航到训练数据页面
    await page.click('text=训练数据')
    
    // 点击添加按钮
    await page.click('button:has-text("添加记录")')
    
    // 填写表单
    await page.fill('input[placeholder="运动名称"]', '深蹲')
    await page.fill('input[placeholder="组数"]', '4')
    await page.fill('input[placeholder="次数"]', '8')
    await page.fill('input[placeholder="重量"]', '100')
    
    // 提交
    await page.click('button:has-text("保存")')
    
    // 验证成功消息
    await expect(page.locator('.el-message--success')).toBeVisible()
    
    // 验证记录出现在列表中
    await expect(page.locator('text=深蹲')).toBeVisible()
  })
  
  test('删除训练记录', async ({ page }) => {
    await page.click('text=训练数据')
    
    // 点击删除按钮
    await page.click('button:has-text("删除")').first()
    
    // 确认删除
    await page.click('button:has-text("确定")')
    
    // 验证成功消息
    await expect(page.locator('.el-message--success')).toBeVisible()
  })
})
```

### 11.6 运行测试

```bash
# 后端测试
cd Fitness
mvn test                          # 运行所有测试
mvn test -Dtest=*ServiceTest      # 运行Service测试
mvn test -Dtest=*IntegrationTest  # 运行集成测试

# 前端单元测试
cd Fitness/frontend
npm run test:unit                 # 运行单元测试
npm run test:unit -- --coverage   # 带覆盖率报告

# 前端E2E测试
npm run test:e2e                  # 运行E2E测试
npm run test:e2e -- --headed      # 有界面模式

# 性能测试 (Gatling)
cd Fitness
mvn gatling:test
```


---

## 12. 部署指南

### 12.1 Docker 部署

#### 12.1.1 Dockerfile

```dockerfile
# Fitness/Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非root用户
RUN addgroup -S fitness && adduser -S fitness -G fitness

COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R fitness:fitness /app
USER fitness

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 12.1.2 Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: fitness-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: fitness_db
      MYSQL_USER: fitness
      MYSQL_PASSWORD: Fitness@123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - fitness-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis 缓存 (可选)
  redis:
    image: redis:7-alpine
    container_name: fitness-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - fitness-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # 后端服务
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: fitness-backend
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/fitness_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: fitness
      SPRING_DATASOURCE_PASSWORD: Fitness@123
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      JWT_SECRET: your_production_secret_key_at_least_512_bits_long
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - fitness-network
    restart: unless-stopped

  # 用户端前端
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: fitness-frontend
    ports:
      - "3001:80"
    depends_on:
      - backend
    networks:
      - fitness-network
    restart: unless-stopped

  # 管理端前端
  admin:
    build:
      context: ./admin
      dockerfile: Dockerfile
    container_name: fitness-admin
    ports:
      - "3002:80"
    depends_on:
      - backend
    networks:
      - fitness-network
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:

networks:
  fitness-network:
    driver: bridge
```

#### 12.1.3 前端 Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### 12.1.4 Nginx 配置

```nginx
# frontend/nginx.conf
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html;
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # API 代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 12.2 部署命令

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 12.3 生产环境配置

#### 12.3.1 application-prod.properties

```properties
# 生产环境配置
spring.profiles.active=prod

# 数据库配置
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:fitness_db}?useUnicode=true&characterEncoding=UTF-8&useSSL=true&serverTimezone=Asia/Shanghai
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# 连接池优化
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10

# JPA 配置
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# JWT 配置
jwt.secret=${JWT_SECRET}
jwt.expiration=86400

# 日志配置
logging.level.root=WARN
logging.level.com.wzl.fitness=INFO

# Actuator 配置
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
```

### 12.4 监控与告警

#### 12.4.1 Prometheus 配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'fitness-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
```

#### 12.4.2 健康检查端点

```
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
GET /actuator/prometheus
GET /actuator/metrics
```


---

## 13. 故障排除

### 13.1 常见问题

#### 13.1.1 JWT 认证失败

**问题：** 登录返回 500 错误

**原因：** JWT 密钥长度不足（HS512 算法要求至少 512 位）

**解决方案：**
```properties
# application.properties
# 密钥至少64个字符
jwt.secret=mySecretKey123456789012345678901234567890mySecretKey123456789012345678901234567890
```

#### 13.1.2 数据库连接失败

**问题：** `CannotGetJdbcConnectionException`

**排查步骤：**
```bash
# 1. 检查 MySQL 服务状态
systemctl status mysql

# 2. 检查连接配置
mysql -u fitness -p -h localhost fitness_db

# 3. 检查连接池配置
# 查看 HikariCP 日志
```

**解决方案：**
```properties
# 增加连接超时时间
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.validation-timeout=10000
```

#### 13.1.3 CORS 跨域错误

**问题：** 前端请求被 CORS 策略阻止

**解决方案：**
```java
// SecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3001",
    "http://localhost:3002",
    "http://your-production-domain.com"
));
configuration.setAllowCredentials(true);
```

#### 13.1.4 前端无法连接后端

**问题：** 网络错误或 404

**排查步骤：**
1. 检查后端服务是否启动：`curl http://localhost:8080/actuator/health`
2. 检查 Vite 代理配置
3. 检查浏览器控制台网络请求

**解决方案：**
```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

#### 13.1.5 缓存不生效

**问题：** 数据没有被缓存

**排查步骤：**
1. 检查 `@EnableCaching` 注解
2. 检查缓存注解是否正确
3. 检查方法是否被代理调用

**解决方案：**
```java
// 确保通过代理调用
@Service
public class MyService {
    @Autowired
    private MyService self;  // 注入自身
    
    public void method1() {
        self.cachedMethod();  // 通过代理调用
    }
    
    @Cacheable("myCache")
    public Data cachedMethod() {
        // ...
    }
}
```

### 13.2 日志排查

#### 13.2.1 开启调试日志

```properties
# application.properties
logging.level.com.wzl.fitness=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### 13.2.2 日志文件位置

```
Fitness/logs/
├── fitness.log           # 主日志
├── fitness-error.log     # 错误日志
└── fitness-audit.log     # 审计日志
```

### 13.3 性能问题排查

#### 13.3.1 慢查询分析

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log_file';
```

#### 13.3.2 连接池监控

```
GET /actuator/metrics/hikaricp.connections.active
GET /actuator/metrics/hikaricp.connections.idle
GET /actuator/metrics/hikaricp.connections.pending
```

#### 13.3.3 JVM 监控

```
GET /actuator/metrics/jvm.memory.used
GET /actuator/metrics/jvm.gc.pause
GET /actuator/metrics/process.cpu.usage
```

---

## 14. 附录

### 14.1 代码规范

#### Java 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `TrainingRecordService` |
| 方法名 | camelCase | `findByUserId` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | 全小写 | `com.wzl.fitness.service` |

#### Vue 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件名 | PascalCase | `TrainingRecordForm.vue` |
| 变量/函数 | camelCase | `getUserData` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| CSS类名 | kebab-case | `training-record-form` |

### 14.2 Git 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型：**
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

**示例：**
```
feat(training): 添加训练记录软删除功能

- 实现软删除和恢复接口
- 添加删除记录回收站页面
- 更新相关单元测试

Closes #123
```

### 14.3 版本历史

| 版本 | 日期 | 主要更新 |
|------|------|----------|
| v1.4.0 | 2026-01-07 | 文档完善、系统监控、数据导出 |
| v1.3.0 | 2025-12-31 | 测试体系、性能优化、PWA支持 |
| v1.2.0 | 2025-12-30 | 属性测试、安全增强 |
| v1.1.0 | 2025-12-24 | JWT修复 |
| v1.0.0 | 2025-12-23 | 初始版本 |

### 14.4 参考资源

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [Caffeine 缓存](https://github.com/ben-manes/caffeine)
- [jqwik 属性测试](https://jqwik.net/)

---

*文档版本: v1.4.0*
*最后更新: 2026-01-07*
*作者: AFitness 开发团队*

