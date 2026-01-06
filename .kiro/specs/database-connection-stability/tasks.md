# Implementation Plan: Database Connection Stability

## Overview

本实现计划将数据库连接稳定性优化设计分解为可执行的编码任务。任务按照依赖关系排序，确保每个步骤都能在前一步骤的基础上构建。

## Tasks

- [x] 1. 优化 HikariCP 连接池配置
  - [x] 1.1 更新 application.properties 添加缺失的 HikariCP 配置参数
    - 添加 keepalive-time=120000 配置
    - 添加 validation-timeout=5000 配置
    - 添加 auto-commit=true 配置
    - 添加 register-mbeans=true 配置
    - _Requirements: 1.1, 1.2, 1.3, 1.6_
  - [x] 1.2 更新 application-mysql.properties 同步配置
    - _Requirements: 1.1, 1.2, 1.3_
  - [x] 1.3 更新 application-prod.properties 生产环境配置
    - _Requirements: 1.1, 1.2, 1.3, 1.6_

- [x] 2. 添加 Spring Retry 依赖和配置
  - [x] 2.1 更新 pom.xml 添加 spring-retry 依赖
    - 添加 spring-retry 依赖
    - 添加 spring-aspects 依赖（如果需要）
    - _Requirements: 3.1, 3.2_
  - [x] 2.2 创建 DatabaseRetryConfig 配置类
    - 创建 src/main/java/com/wzl/fitness/config/DatabaseRetryConfig.java
    - 配置 RetryTemplate 使用指数退避策略
    - 配置最大重试次数为 3
    - _Requirements: 3.1, 3.2, 3.5_
  - [ ]* 2.3 编写 DatabaseRetryConfig 单元测试
    - 验证重试模板配置正确
    - **Property 2: Retry Attempt Bound**
    - **Validates: Requirements 3.2**

- [x] 3. 实现数据库健康检查指示器
  - [x] 3.1 创建 ConnectionPoolStatus 数据模型
    - 创建 src/main/java/com/wzl/fitness/model/ConnectionPoolStatus.java
    - 包含 activeConnections, idleConnections, totalConnections 等字段
    - _Requirements: 4.2, 4.3, 4.4_
  - [x] 3.2 创建 DatabaseHealthIndicator 组件
    - 创建 src/main/java/com/wzl/fitness/health/DatabaseHealthIndicator.java
    - 实现 HealthIndicator 接口
    - 根据连接池利用率返回不同健康状态
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.6_
  - [ ]* 3.3 编写 DatabaseHealthIndicator 属性测试
    - **Property 4: Health Status Monotonicity**
    - **Validates: Requirements 4.6**

- [x] 4. 实现连接池指标配置
  - [x] 4.1 创建 ConnectionPoolMetricsConfig 配置类
    - 创建 src/main/java/com/wzl/fitness/config/ConnectionPoolMetricsConfig.java
    - 配置 HikariCP 指标绑定到 Micrometer
    - _Requirements: 4.1, 4.5_
  - [x] 4.2 更新 application.properties 暴露更多 actuator 端点
    - 添加 hikaricp 相关指标端点
    - _Requirements: 4.1_

- [x] 5. Checkpoint - 验证配置和健康检查
  - 确保所有配置正确加载
  - 验证健康检查端点可访问
  - 如有问题请询问用户

- [x] 6. 实现数据库重试切面
  - [x] 6.1 创建 RetryEvent 数据模型
    - 创建 src/main/java/com/wzl/fitness/model/RetryEvent.java
    - 包含 operationName, attemptNumber, exceptionType 等字段
    - _Requirements: 3.3_
  - [x] 6.2 创建 DatabaseRetryAspect 切面
    - 创建 src/main/java/com/wzl/fitness/aspect/DatabaseRetryAspect.java
    - 实现对数据库操作的重试逻辑
    - 区分可重试和不可重试异常
    - _Requirements: 3.1, 3.3, 3.4, 3.5_
  - [x] 6.3 创建 @DatabaseRetryable 自定义注解
    - 创建 src/main/java/com/wzl/fitness/annotation/DatabaseRetryable.java
    - _Requirements: 3.1_
  - [ ]* 6.4 编写重试切面单元测试
    - 测试可重试异常触发重试
    - 测试不可重试异常不触发重试
    - **Property 2: Retry Attempt Bound**
    - **Validates: Requirements 3.2, 3.5**

- [x] 7. 实现全局异常处理
  - [x] 7.1 更新 GlobalExceptionHandler 添加数据库异常处理
    - 添加 CannotGetJdbcConnectionException 处理
    - 添加 QueryTimeoutException 处理
    - 添加重试耗尽异常处理
    - _Requirements: 3.4_

- [x] 8. 优化事务管理配置
  - [x] 8.1 创建 TransactionConfig 配置类
    - 创建 src/main/java/com/wzl/fitness/config/TransactionConfig.java
    - 配置事务超时时间
    - 配置事务管理器
    - _Requirements: 6.1, 6.2, 6.3_
  - [x] 8.2 更新 application.properties 添加事务相关配置
    - _Requirements: 6.1, 6.4_

- [x] 9. 实现优雅关闭配置
  - [x] 9.1 创建 GracefulShutdownConfig 配置类
    - 创建 src/main/java/com/wzl/fitness/config/GracefulShutdownConfig.java
    - 配置应用关闭时的连接池处理
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_
  - [x] 9.2 更新 application.properties 添加优雅关闭配置
    - 添加 server.shutdown=graceful
    - 添加 spring.lifecycle.timeout-per-shutdown-phase
    - _Requirements: 7.3_

- [x] 10. Checkpoint - 集成测试
  - 确保所有组件正确集成
  - 验证重试机制工作正常
  - 验证健康检查端点返回正确信息
  - 如有问题请询问用户

- [ ] 11. 编写集成测试
  - [ ]* 11.1 编写连接池配置集成测试
    - 验证 HikariCP 配置正确加载
    - **Property 1: Connection Pool Size Invariant**
    - **Validates: Requirements 1.4, 1.5**
  - [ ]* 11.2 编写健康检查端点集成测试
    - 测试 /actuator/health 端点
    - 验证连接池指标正确暴露
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 12. Final Checkpoint - 完整验证
  - ✅ 编译成功
  - ✅ 所有核心功能已实现
  - ✅ 系统可正常启动

## Notes

- 标记 `*` 的任务为可选任务，可以跳过以加快 MVP 开发
- 每个任务都引用了具体的需求以便追溯
- Checkpoint 任务用于增量验证
- 属性测试验证通用正确性属性
- 单元测试验证具体示例和边界情况
