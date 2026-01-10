# Implementation Plan: Modular Architecture

## Overview

本实现计划将AFitness健身管理系统重构为模块化架构。采用渐进式重构策略，先建立共享内核和模块基础设施，然后逐步迁移各业务模块，最后添加事件驱动通信和插件化支持。

## Tasks

- [x] 1. 建立后端共享内核结构
  - [x] 1.1 创建shared包结构和基础类迁移
    - 创建`shared/common`、`shared/config`、`shared/security`、`shared/exception`、`shared/util`包
    - 迁移ApiResponse、BaseController、PageResponse、ResponseCode到shared/common
    - 迁移GlobalExceptionHandler到shared/exception
    - 迁移SecurityConfig、JwtTokenProvider、JwtAuthenticationFilter到shared/security
    - _Requirements: 1.3, 5.1_
  - [x] 1.2 创建事件基础设施
    - 创建`shared/event`包
    - 实现DomainEvent基类
    - 实现EventPublisher组件
    - _Requirements: 3.1_
  - [x] 1.3 创建模块注册基础设施
    - 创建ModuleConfig配置类
    - 创建ModuleRegistry组件
    - 实现模块启用/禁用配置支持
    - _Requirements: 6.2_

- [x] 2. 建立后端模块接口层
  - [x] 2.1 创建模块接口定义
    - 创建`modules/user/api/UserModuleApi.java`接口
    - 创建`modules/training/api/TrainingModuleApi.java`接口
    - 创建`modules/nutrition/api/NutritionModuleApi.java`接口
    - 创建`modules/recovery/api/RecoveryModuleApi.java`接口
    - _Requirements: 2.1_
  - [x] 2.2 编写属性测试：模块接口只使用DTO
    - **Property 3: 模块接口只使用DTO传输数据**
    - **Validates: Requirements 2.5**

- [x] 3. 重构用户模块
  - [x] 3.1 创建用户模块包结构
    - 创建`modules/user/controller`、`service`、`repository`、`entity`、`dto`、`event`包
    - 迁移User实体、UserRepository、UserService到用户模块
    - 迁移AuthController、UserController到用户模块
    - _Requirements: 1.1, 1.2_
  - [x] 3.2 实现UserModuleApi接口
    - 创建UserModuleApiImpl实现类
    - 实现getUserById、getUserByUsername、existsById、getBasicInfo方法
    - _Requirements: 2.1, 2.5_
  - [x] 3.3 编写用户模块单元测试
    - 测试UserModuleApi接口实现
    - 测试用户服务业务逻辑
    - _Requirements: 8.1_

- [x] 4. 重构训练模块
  - [x] 4.1 创建训练模块包结构
    - 创建`modules/training/controller`、`service`、`repository`、`entity`、`dto`、`event`包
    - 迁移TrainingRecord实体、TrainingRecordRepository、TrainingRecordService到训练模块
    - 迁移TrainingController到训练模块
    - _Requirements: 1.1, 1.2_
  - [x] 4.2 实现TrainingModuleApi接口
    - 创建TrainingModuleApiImpl实现类
    - 实现getRecentRecords、calculateLoad、getStats方法
    - _Requirements: 2.1, 2.5_
  - [x] 4.3 实现训练完成事件
    - 创建TrainingCompletedEvent事件类
    - 在训练记录保存后发布事件
    - _Requirements: 3.2_
  - [x] 4.4 编写训练模块单元测试
    - 测试TrainingModuleApi接口实现
    - 测试事件发布逻辑
    - _Requirements: 8.1_

- [x] 5. 重构营养模块
  - [x] 5.1 创建营养模块包结构
    - 创建`modules/nutrition/controller`、`service`、`repository`、`entity`、`dto`包
    - 迁移NutritionRecord实体、NutritionRepository、NutritionService到营养模块
    - 迁移NutritionController到营养模块
    - _Requirements: 1.1, 1.2_
  - [x] 5.2 实现NutritionModuleApi接口
    - 创建NutritionModuleApiImpl实现类
    - 实现getDailyNutrition、getGoals方法
    - _Requirements: 2.1, 2.5_

- [x] 6. 重构恢复评估模块
  - [x] 6.1 创建恢复评估模块包结构
    - 创建`modules/recovery/controller`、`service`、`repository`、`entity`、`dto`、`event`包
    - 迁移RecoveryAssessment实体、RecoveryRepository、RecoveryService到恢复模块
    - 迁移LoadRecoveryController到恢复模块
    - _Requirements: 1.1, 1.2_
  - [x] 6.2 实现RecoveryModuleApi接口
    - 创建RecoveryModuleApiImpl实现类
    - 实现getCurrentStatus、getSuggestions方法
    - _Requirements: 2.1, 2.5_
  - [x] 6.3 实现训练完成事件监听
    - 创建TrainingCompletedEventListener
    - 在收到事件后自动更新恢复状态
    - _Requirements: 3.3_

- [x] 7. 重构管理模块
  - [x] 7.1 创建管理模块包结构
    - 创建`modules/admin/controller`、`service`、`dto`包
    - 迁移SystemMonitorController、DataExportController、CacheStatsController到管理模块
    - 迁移相关Service实现到管理模块
    - _Requirements: 1.1, 1.2_

- [x] 8. Checkpoint - 后端模块化验证
  - 确保所有后端模块正确划分
  - 验证模块间依赖通过接口
  - 运行现有测试确保功能正常
  - 如有问题请咨询用户

- [x] 9. 编写后端架构属性测试
  - [x] 9.1 编写属性测试：模块间依赖必须通过接口
    - **Property 1: 模块间依赖必须通过接口**
    - **Validates: Requirements 1.4, 2.2**
  - [x] 9.2 编写属性测试：模块依赖图无循环
    - **Property 2: 模块依赖图无循环**
    - **Validates: Requirements 1.5**
  - [x] 9.3 编写属性测试：API路由前缀规范
    - **Property 4: API路由前缀规范**
    - **Validates: Requirements 7.1**

- [x] 10. 建立前端共享内核结构
  - [x] 10.1 创建shared目录结构（部分完成）
    - 已创建`shared/api`、`shared/styles`、`shared/utils`目录
    - 已迁移request.js到shared/api
    - 已迁移errorHandler.js等通用工具到shared/utils
    - _Requirements: 4.3, 5.2_
  - [x] 10.2 完善shared目录结构
    - 创建`shared/components`目录并迁移ErrorBoundary.vue等通用组件
    - 创建`shared/composables`目录并迁移useAuth.js等通用composables
    - 创建`shared/layouts`目录并迁移布局组件
    - _Requirements: 4.3, 5.2_
  - [x] 10.3 创建模块注册器
    - 实现ModuleRegistry类
    - 支持模块注册、获取、加载功能
    - _Requirements: 6.3_

- [x] 11. 重构前端认证模块
  - [x] 11.1 创建认证模块结构
    - 创建`modules/auth/views`、`components`、`api`、`stores`目录
    - 迁移Login.vue到auth/views
    - 迁移认证相关API到auth/api
    - 创建模块导出文件index.js
    - _Requirements: 4.1, 4.2, 4.5_

- [x] 12. 重构前端训练模块
  - [x] 12.1 创建训练模块结构
    - 创建`modules/training/views`、`components`、`api`、`stores`目录
    - 迁移TrainingData.vue、TrainingRecordManager.vue等到training模块
    - 迁移训练相关API和Store
    - 创建模块导出文件index.js
    - _Requirements: 4.1, 4.2, 4.5_

- [x] 13. 重构前端营养模块
  - [x] 13.1 创建营养模块结构
    - 创建`modules/nutrition/views`、`components`、`api`目录
    - 迁移NutritionTracking.vue等到nutrition模块
    - 迁移营养相关API
    - 创建模块导出文件index.js
    - _Requirements: 4.1, 4.2, 4.5_

- [x] 14. 重构前端仪表盘和设置模块
  - [x] 14.1 创建仪表盘模块结构
    - 创建`modules/dashboard/views`、`components`目录
    - 迁移Dashboard.vue和相关组件
    - _Requirements: 4.1, 4.2_
  - [x] 14.2 创建设置模块结构
    - 创建`modules/settings/views`、`components`目录
    - 迁移Settings.vue和相关组件
    - _Requirements: 4.1, 4.2_

- [x] 15. 重构前端路由为模块化
  - [x] 15.1 创建模块化路由配置
    - 创建`router/modules`目录
    - 为每个模块创建独立路由文件（auth.js、training.js等）
    - 配置路由懒加载
    - 更新主路由文件合并模块路由
    - _Requirements: 4.4, 7.1_

- [x] 16. Checkpoint - 前端模块化验证
  - 确保所有前端模块正确划分
  - 验证路由懒加载正常工作
  - 运行现有测试确保功能正常
  - 如有问题请咨询用户

- [x] 17. 实现模块启用/禁用功能
  - [x] 17.1 后端模块条件加载
    - 使用@ConditionalOnProperty实现模块条件加载
    - 配置application.properties中的模块开关
    - _Requirements: 6.2_
  - [x] 17.2 前端模块动态加载
    - 实现基于配置的模块动态加载
    - 根据后端返回的模块配置显示/隐藏功能
    - _Requirements: 6.2_
  - [x] 17.3 编写属性测试：模块禁用后功能不可用
    - **Property 5: 模块禁用后功能不可用**
    - **Validates: Requirements 6.2, 7.5**

- [x] 18. 更新API文档和配置
  - [x] 18.1 更新OpenAPI配置
    - 配置模块化的API分组
    - 更新Swagger文档注解
    - _Requirements: 7.4_
  - [x] 18.2 更新应用配置
    - 添加模块配置项到application.properties
    - 更新README文档说明模块化架构
    - _Requirements: 6.2_

- [x] 19. Final Checkpoint - 完整性验证
  - 运行所有单元测试
  - 运行所有属性测试
  - 验证模块间通信正常
  - 验证事件发布/订阅正常
  - 如有问题请咨询用户

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- 重构采用渐进式策略，每个模块迁移后都应确保系统可运行
- 模块迁移顺序：共享内核 → 用户模块 → 训练模块 → 营养模块 → 恢复模块 → 管理模块
- 前端重构在后端完成后进行，确保API兼容性
- 属性测试验证架构约束，确保模块化设计的正确性
