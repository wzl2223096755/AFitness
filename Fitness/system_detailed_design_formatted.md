# 第三章 系统详细设计

## 3.1 系统架构设计

### 3.1.1 总体架构

力量训练专业计算器APP采用分层架构设计，系统整体架构分为表现层、业务逻辑层和数据访问层，遵循高内聚、低耦合的设计原则。系统采用前后端分离架构，前端使用Vue3框架实现用户界面，后端采用Spring Boot框架提供RESTful API服务，数据库采用MySQL进行数据持久化存储。

**系统架构图描述：**
- 最上层为表现层，包括Web前端和移动端应用
  - Web前端：基于Vue3的单页应用，提供丰富的用户交互界面
  - 移动端：响应式设计，支持手机和平板设备访问
  - 统一网关：提供统一的访问入口和路由转发

- 中间层为业务逻辑层，包含各个核心业务模块
  - 用户管理模块：处理用户注册、登录、权限管理等功能
  - 训练数据管理模块：处理训练记录的录入、查询、统计和分析
  - 负荷计算模块：计算训练负荷、估算1RM等关键指标
  - 恢复监控模块：收集和分析用户的恢复状态数据
  - 训练建议模块：基于用户数据和恢复状态生成个性化建议
  - 数据可视化模块：将训练数据以直观的图表形式展示

- 最下层为数据访问层，包括数据库和缓存系统
  - 数据库：MySQL主从架构，提供数据持久化存储
  - 缓存：Redis集群，提供高性能数据缓存
  - 文件存储：对象存储服务，存储用户上传的文件和图片

- 各层之间通过明确的接口进行通信，确保系统的可维护性和扩展性
  - 表现层与业务逻辑层通过RESTful API进行通信
  - 业务逻辑层与数据访问层通过数据访问对象(DAO)模式进行交互
  - 使用依赖注入(DI)和控制反转(IoC)管理组件之间的依赖关系

### 3.1.2 技术架构

**前端技术架构：**
- 框架：Vue 3 + TypeScript
  - Vue 3 Composition API：提供更灵活的代码组织方式
  - TypeScript：提供静态类型检查，提高代码质量和可维护性
- UI组件库：Element Plus
  - 提供丰富的UI组件，支持主题定制
  - 响应式设计，适配多种设备屏幕
- 状态管理：Pinia
  - 轻量级状态管理库，支持TypeScript
  - 模块化状态管理，支持热更新
- 图表库：ECharts
  - 提供丰富的图表类型和交互功能
  - 支持大数据量渲染和动态更新
- 构建工具：Vite
  - 快速的开发服务器和构建工具
  - 支持热更新和模块联邦
- 路由管理：Vue Router
  - 支持懒加载和路由守卫
  - 提供嵌套路由和动态路由
- HTTP客户端：Axios
  - 支持请求和响应拦截
  - 提供并发请求处理和取消功能
- 移动端适配：响应式设计
  - 使用CSS媒体查询和弹性布局
  - 支持触摸手势和移动端交互

**后端技术架构：**
- 框架：Spring Boot 3.2.5
  - 自动配置和起步依赖，简化应用开发
  - 内嵌Tomcat服务器，支持独立运行
- 安全框架：Spring Security + JWT
  - 提供认证和授权功能
  - 支持多种认证方式和权限控制
- ORM框架：Spring Data JPA + Hibernate
  - 简化数据库操作，提供CRUD接口
  - 支持复杂查询和事务管理
- 数据库：MySQL 8.0
  - 高性能关系型数据库
  - 支持事务、索引和分区
- 缓存：Redis
  - 高性能内存数据库
  - 支持多种数据结构和持久化
- 连接池：HikariCP
  - 高性能数据库连接池
  - 支持连接监控和泄漏检测
- API文档：Swagger
  - 自动生成API文档
  - 支持在线测试和调试
- 日志框架：Logback
  - 灵活的日志配置
  - 支持多种输出格式和滚动策略

### 3.1.3 部署架构

系统采用容器化部署方式，支持水平扩展。前端应用部署在Nginx服务器上，后端应用部署在Tomcat服务器上，数据库采用主从复制架构，缓存使用Redis集群。

**部署架构图描述：**
- 用户通过负载均衡器访问系统
  - 使用Nginx作为反向代理和负载均衡器
  - 支持SSL/TLS加密传输
  - 配置健康检查和故障转移

- 前端静态资源由CDN分发
  - 静态资源上传到对象存储服务
  - CDN节点全球分布，提高访问速度
  - 支持缓存策略和版本控制

- 后端服务部署在多个服务器节点上
  - 使用Docker容器化部署
  - Kubernetes集群管理容器生命周期
  - 支持自动扩缩容和滚动更新

- 数据库采用主从架构，读写分离
  - 主数据库负责写操作
  - 从数据库负责读操作
  - 配置自动故障转移和数据同步

- Redis集群提供缓存服务
  - 主从复制保证高可用性
  - 分片存储提高性能
  - 支持数据持久化和恢复

**部署环境配置：**
- 开发环境：单机部署，使用H2内存数据库
- 测试环境：容器化部署，模拟生产环境配置
- 预生产环境：与生产环境配置一致，用于用户验收测试
- 生产环境：高可用集群部署，支持大规模并发访问

**监控与日志：**
- 应用监控：使用Prometheus收集应用指标
- 基础设施监控：使用Grafana展示服务器状态
- 日志聚合：使用ELK Stack收集和分析日志
- 告警系统：配置关键指标告警规则

## 3.2 数据库设计

### 3.2.1 数据库概念设计

系统数据库设计遵循第三范式，确保数据的一致性和完整性。数据库包含用户管理、训练记录、恢复监控、数据分析等核心功能模块的数据表。

**数据库设计原则：**
- 数据一致性：通过外键约束保证数据完整性
- 性能优化：合理设计索引，优化查询性能
- 扩展性：预留扩展空间，支持功能迭代
- 安全性：多层次安全防护，保护用户隐私
- 可维护性：清晰的表结构设计，便于后期维护

**数据库E-R图描述：**
- 用户表为核心表，与其他所有表存在一对多关系
  - 用户表包含用户基本信息和认证信息
  - 每个用户可以有多条训练记录、恢复数据等

- 训练记录表与动作详情表存在一对多关系
  - 一条训练记录可以包含多个动作详情
  - 动作详情表存储每个动作的具体参数

- 力量训练数据表和有氧训练数据表分别存储不同类型的训练数据
  - 力量训练数据表存储重量、组数、次数等力量训练特有数据
  - 有氧训练数据表存储距离、时长、心率等有氧训练特有数据

- 恢复数据表和恢复指标表存储用户恢复状态信息
  - 恢复数据表存储详细的恢复数据，包括睡眠、心率等
  - 恢复指标表存储每日恢复指标的汇总数据

- 训练建议表存储系统生成的个性化建议
  - 每条建议关联特定用户和日期
  - 建议内容根据用户训练数据和恢复状态生成

**数据关系说明：**
- 用户与训练记录：一对多关系，一个用户可以有多个训练记录
- 训练记录与动作详情：一对多关系，一个训练记录可以包含多个动作
- 用户与恢复数据：一对多关系，一个用户可以有多条恢复数据记录
- 用户与训练建议：一对多关系，一个用户可以接收多条训练建议

**数据完整性约束：**
- 实体完整性：每个表都有主键，确保记录唯一性
- 参照完整性：通过外键约束保证关联数据的一致性
- 域完整性：通过数据类型和约束条件保证字段值的有效性
- 用户自定义完整性：通过触发器和存储过程实现业务规则

### 3.2.2 数据库物理设计

**用户表(user_table)：**
- id：主键，自增长
- username：用户名，唯一索引
- password：加密密码
- email：邮箱地址
- created_at：创建时间
- role：用户角色

**训练记录表(training_records)：**
- id：主键，自增长
- user_id：用户ID，外键
- exercise_name：运动名称
- sets：组数
- reps：次数
- weight：重量
- training_date：训练日期
- duration：训练时长
- notes：备注
- total_volume：总训练量
- training_stress：训练压力

**动作详情表(exercise_details)：**
- id：主键，自增长
- record_id：训练记录ID，外键
- exercise_name：动作名称
- weight：重量
- sets：组数
- reps：次数
- rpe：感知疲劳指数
- volume：训练量
- exercise_type：动作类型

**力量训练数据表(strength_training_data)：**
- id：主键，自增长
- user_id：用户ID，外键
- timestamp：记录时间
- exercise_name：动作名称
- weight：重量
- sets：组数
- reps：次数
- exercise_type：动作类型
- one_rep_max：最大重量估算
- training_volume：训练量
- perceived_exertion：主观疲劳度

**有氧训练数据表(cardio_training_data)：**
- id：主键，自增长
- user_id：用户ID，外键
- timestamp：记录时间
- exercise_type：运动类型
- duration：运动时长
- distance：距离
- average_heart_rate：平均心率
- max_heart_rate：最大心率
- calories_burned：消耗卡路里
- average_speed：平均速度
- pace：配速
- perceived_exertion：主观疲劳度

**恢复数据表(recovery_data)：**
- id：主键，自增长
- user_id：用户ID，外键
- timestamp：记录时间
- recovery_score：恢复评分
- sleep_hours：睡眠时长
- sleep_quality：睡眠质量
- heart_rate_variability：心率变异性
- resting_heart_rate：静息心率
- muscle_soreness：肌肉酸痛度
- stress_level：压力水平
- notes：恢复备注

**恢复指标表(recovery_metrics)：**
- id：主键，自增长
- user_id：用户ID，外键
- record_date：记录日期
- muscle_soreness：肌肉酸痛度
- sleep_quality：睡眠质量
- resting_heart_rate：静息心率
- subjective_energy：主观能量

**健身数据表(fitness_data)：**
- id：主键，自增长
- user_id：用户ID，外键
- timestamp：数据时间戳
- exercise_name：动作名称
- weight：重量
- sets：组数
- reps：次数
- exercise_type：动作类型
- one_rep_max：最大重量估算
- training_volume：训练量
- perceived_exertion：主观疲劳度
- recovery_score：恢复评分
- recovery_status：恢复状态描述
- sleep_hours：睡眠时长
- stress_level：压力水平

**训练建议表(training_advices)：**
- id：主键，自增长
- user_id：用户ID，外键
- advice_date：建议日期
- advice_type：建议类型
- content：建议内容
- confidence_score：置信度评分

### 3.2.3 数据库索引设计

系统设计了合理的索引策略，优化查询性能：
- 用户表：username(唯一索引)、email、created_at
- 训练记录表：user_id、training_date、exercise_name、user_id+training_date(复合索引)
- 动作详情表：record_id、exercise_name
- 力量训练数据表：user_id、timestamp、exercise_type、user_id+timestamp(复合索引)
- 有氧训练数据表：user_id、timestamp、exercise_type
- 恢复数据表：user_id、timestamp、user_id+timestamp(复合索引)
- 恢复指标表：user_id、record_date、user_id+record_date(复合索引，唯一约束)
- 健身数据表：user_id、timestamp、user_id+timestamp(复合索引)
- 训练建议表：user_id、advice_date、advice_type、user_id+advice_date(复合索引)

## 3.3 核心模块设计

### 3.3.1 用户管理模块

**功能概述：**
用户管理模块负责处理用户注册、登录、权限管理等功能，确保系统安全性和用户数据的隐私保护。该模块是系统的基础模块，为其他业务模块提供用户身份验证和权限控制服务。

**核心组件：**
- UserController：处理用户相关HTTP请求
  - 提供用户注册、登录、信息修改等RESTful API
  - 实现请求参数验证和响应格式统一
  - 处理异常情况并返回友好的错误信息

- UserService：实现用户业务逻辑
  - 处理用户注册逻辑，包括用户名唯一性检查
  - 实现用户登录验证，支持多种登录方式
  - 管理用户信息更新和密码重置功能

- UserRepository：提供用户数据访问接口
  - 基于Spring Data JPA实现数据访问层
  - 提供自定义查询方法和分页查询功能
  - 实现数据访问层的性能优化

- JwtTokenProvider：处理JWT令牌生成和验证
  - 生成访问令牌和刷新令牌
  - 验证令牌有效性和解析用户信息
  - 管理令牌生命周期和刷新机制

- CustomUserDetailsService：实现用户详情加载
  - 实现Spring Security的UserDetailsService接口
  - 从数据库加载用户信息和权限
  - 支持用户权限动态加载和缓存

**关键流程：**
1. 用户注册流程：
   - 接收用户注册信息（用户名、密码、邮箱等）
   - 验证输入信息的合法性和完整性
   - 检查用户名和邮箱的唯一性
   - 使用BCrypt算法加密用户密码
   - 保存用户信息到数据库
   - 返回注册结果和用户基本信息

2. 用户登录流程：
   - 接收用户登录凭证（用户名/邮箱、密码）
   - 验证用户凭证的有效性
   - 检查账户状态（是否锁定、是否激活等）
   - 生成JWT访问令牌和刷新令牌
   - 记录登录日志和更新最后登录时间
   - 返回令牌信息和用户基本信息

3. 权限验证流程：
   - 从HTTP请求中解析JWT令牌
   - 验证令牌的有效性和过期时间
   - 加载用户信息和权限列表
   - 执行资源访问权限检查
   - 记录访问日志和异常情况

**安全设计：**
- 密码安全：
  - 使用BCrypt算法加密存储密码，盐值随机生成
  - 密码最小长度6位，支持复杂度验证
  - 实现密码重置和修改功能，需要验证旧密码

- 令牌安全：
  - JWT令牌设置有效期（访问令牌2小时，刷新令牌7天）
  - 令牌包含用户ID、角色、权限等关键信息
  - 支持令牌黑名单机制，防止令牌被滥用

- 权限控制：
  - 实现基于角色的访问控制(RBAC)
  - 支持细粒度的权限控制到方法级别
  - 管理员角色具有系统管理权限，普通用户只能访问自己的数据

- 安全防护：
  - 实现登录尝试限制，防止暴力破解
  - 支持账户锁定机制，多次失败后临时锁定
  - 记录安全相关操作日志，便于审计和追踪

### 3.3.2 训练数据管理模块

**功能概述：**
训练数据管理模块负责处理训练记录的录入、查询、统计和分析功能，为用户提供全面的训练数据管理服务。

**核心组件：**
- TrainingRecordController：处理训练记录相关HTTP请求
  - 提供训练记录的创建、查询、更新、删除API
  - 实现请求参数验证和响应格式统一
  - 支持批量操作和数据导入导出功能

- TrainingRecordService：实现训练记录业务逻辑
  - 处理训练记录的创建和验证
  - 实现复杂查询条件和排序功能
  - 提供训练统计和分析功能

- TrainingRecordRepository：提供训练记录数据访问接口
  - 基于Spring Data JPA实现数据访问层
  - 提供自定义查询方法和分页查询功能
  - 实现数据访问层的性能优化

- ExerciseDetailRepository：提供动作详情数据访问接口
  - 管理训练记录中的动作详情数据
  - 支持批量操作和事务管理
  - 提供动作统计和分析功能

**关键流程：**
1. 训练记录录入流程：
   - 接收训练记录数据（动作名称、重量、组数、次数等）
   - 验证输入数据的合法性和完整性
   - 计算训练量和训练压力等衍生数据
   - 保存训练记录和动作详情到数据库
   - 返回保存结果和计算数据

2. 训练数据查询流程：
   - 接收查询条件（时间范围、动作类型、用户等）
   - 构建数据库查询语句
   - 执行查询并获取结果数据
   - 格式化查询结果并返回

3. 训练统计分析流程：
   - 聚合训练数据（按时间、动作类型等维度）
   - 计算统计指标（总训练量、平均强度等）
   - 生成分析报告和趋势图表
   - 返回统计结果和分析数据

**数据处理：**
- 自动计算训练量(重量×组数×次数)
- 支持多种查询条件(时间、动作、部位等)
- 提供数据导出功能(Excel、PDF格式)
- 实现数据验证和异常处理

### 3.3.3 负荷计算模块

**功能概述：**
负荷计算模块是系统的核心模块，负责计算训练负荷、估算1RM(一次最大重复次数)等关键指标，为训练计划制定提供科学依据。该模块基于运动生理学理论和经验公式，结合用户历史数据，提供准确的负荷计算和预测功能。

**核心组件：**
- FitnessCalculationController：处理负荷计算相关HTTP请求
  - 提供1RM计算、训练负荷计算等API接口
  - 实现请求参数验证和计算结果格式化
  - 处理计算异常情况并提供友好的错误信息

- LocalFitnessCalculationService：实现本地负荷计算逻辑
  - 实现多种1RM估算算法
  - 提供训练负荷计算方法
  - 支持批量计算和历史数据分析

- StrengthTrainingService：处理力量训练相关计算
  - 专门处理力量训练数据的计算
  - 实现训练强度和训练密度计算
  - 提供训练进度分析功能

**核心算法：**
1. **训练量计算算法：**
   ```
   单动作训练量 = 重量 × 组数 × 次数
   单次训练总训练量 = Σ(各动作训练量)
   相对训练量 = 训练量 / 体重
   训练强度 = 平均重量 / 1RM
   训练密度 = 训练量 / 训练时长
   ```
   
   算法说明：
   - 训练量是衡量训练负荷的基础指标，表示总重量负荷
   - 相对训练量考虑个体体重差异，便于不同用户比较
   - 训练强度反映训练的重量水平，与1RM相关
   - 训练密度表示单位时间内的训练量，反映训练紧凑程度

2. **1RM估算算法：**
   ```
   Epley公式：1RM = 重量 × (1 + 次数/30)
   Brzycki公式：1RM = 重量 × 36/(37-次数)
   Lombardi公式：1RM = 重量 × 次数^0.10
   Mayhew公式：1RM = 重量 × (100 / (101.3 - 2.67123 × 次数))
   O'Conner公式：1RM = 重量 × (1 + 0.025 × 次数)
   
   综合估算：1RM = (Epley结果 + Brzycki结果 + Lombardi结果 + Mayhew结果 + O'Conner结果) / 5
   ```
   
   算法适用范围：
   - Epley公式：适用于次数较少的情况(1-10次)
   - Brzycki公式：适用于中等次数的情况(2-12次)
   - Lombardi公式：适用于各种次数范围
   - Mayhew公式：适用于高次数的情况(>10次)
   - O'Conner公式：适用于低次数的情况(1-5次)

3. **训练压力计算算法：**
   ```
   训练压力 = 训练量 × 强度系数 × 疲劳系数
   
   强度系数根据训练强度确定：
   - 低强度(<60% 1RM)：0.8
   - 中强度(60-80% 1RM)：1.0
   - 高强度(>80% 1RM)：1.2
   
   疲劳系数根据RPE确定：
   - RPE 1-3：0.8
   - RPE 4-6：1.0
   - RPE 7-10：1.2
   ```

**算法优化：**
- 基于历史数据校准：
  - 收集用户实际1RM测试数据
  - 计算各种算法的误差率
  - 根据误差率调整算法权重
  - 实现个性化算法适配

- 考虑疲劳度影响：
  - 分析训练顺序对1RM的影响
  - 考虑组间休息时间的影响
  - 调整算法参数以反映疲劳状态

- 动作特性调整：
  - 根据动作类型调整算法参数
  - 考虑复合动作和孤立动作的差异
  - 针对不同肌群特点优化计算

- 个体差异适配：
  - 考虑训练水平对1RM估算的影响
  - 根据年龄、性别等因素调整参数
  - 实现动态学习和自适应机制

**计算流程：**
1. 接收计算请求和参数
2. 验证参数的有效性和合理性
3. 选择合适的计算算法
4. 执行计算并获取结果
5. 根据历史数据校准结果
6. 格式化并返回计算结果
7. 记录计算日志用于后续优化

**性能优化：**
- 使用缓存存储常用计算结果
- 实现批量计算减少数据库访问
- 采用异步处理复杂计算任务
- 优化算法实现提高计算效率

### 3.3.4 恢复监控模块

**功能概述：**
恢复监控模块负责收集和分析用户的恢复状态数据，评估用户的恢复情况，为训练计划调整提供依据。

**核心组件：**
- LoadRecoveryController：处理恢复监控相关HTTP请求
  - 提供恢复数据录入、查询等API接口
  - 实现恢复状态评估和分析功能
  - 处理恢复预警和建议生成

- LoadRecoveryService：实现恢复状态评估逻辑
  - 实现多维度恢复评分算法
  - 提供恢复趋势分析和预测功能
  - 生成个性化恢复建议

- RecoveryDataRepository：提供恢复数据访问接口
  - 管理用户恢复状态数据
  - 支持时间序列查询和聚合分析
  - 实现数据访问层的性能优化

**恢复评估算法：**
```
恢复评分 = 睡眠权重×40% + 疲劳权重×30% + 心率权重×20% + 主观权重×10%

状态分类：
- 恢复良好(80-100分)：建议高强度训练
- 恢复一般(60-79分)：建议中等强度训练
- 轻度疲劳(40-59分)：建议低强度训练
- 中度疲劳(20-39分)：建议休息或轻度活动
- 严重疲劳(0-19分)：建议完全休息
```

**数据处理：**
- 多维度数据采集(睡眠、心率、主观感受等)
- 动态权重调整机制
- 时间衰减因子应用
- 趋势分析和预警

### 3.3.5 训练建议模块

**功能概述：**
训练建议模块基于用户训练数据和恢复状态，生成个性化的训练建议，帮助用户优化训练效果。

**核心组件：**
- TrainingController：处理训练建议相关HTTP请求
  - 提供训练建议查询和生成API
  - 实现建议内容个性化定制
  - 支持建议效果反馈和优化

- TrainingLoadService：实现训练建议生成逻辑
  - 基于用户数据和恢复状态生成建议
  - 实现建议算法的动态调整
  - 提供建议效果评估功能

- TrainingAdviceRepository：提供训练建议数据访问接口
  - 管理系统生成的训练建议
  - 支持建议历史查询和分析
  - 实现建议效果跟踪功能

**建议生成算法：**
1. **强度建议算法：**
   ```
   建议重量 = 当前1RM × 目标强度百分比 × 恢复系数
   恢复系数根据恢复评分动态调整：
   - 恢复良好：0.9-1.0
   - 恢复一般：0.8-0.9
   - 轻度疲劳：0.7-0.8
   - 中度疲劳：0.6-0.7
   - 严重疲劳：0.5-0.6
   ```

2. **训练容量建议算法：**
   ```
   建议训练容量 = 基础容量 × 进阶系数 × 恢复系数
   进阶系数根据训练水平调整：
   - 新手：0.8-0.9
   - 进阶：0.9-1.0
   - 高级：1.0-1.1
   ```

**个性化机制：**
- 基于用户历史数据学习偏好
- 根据训练目标调整建议策略
- 结合恢复状态动态优化
- 基于用户反馈持续改进

### 3.3.6 数据可视化模块

**功能概述：**
数据可视化模块负责将复杂的训练数据以直观的图表形式展示，帮助用户更好地理解自己的训练状态和进步情况。

**核心组件：**
- 前端图表组件：基于ECharts实现各种图表
  - 提供丰富的图表类型和交互功能
  - 支持动态数据更新和响应式设计
  - 实现图表主题和样式定制

- 数据聚合服务：处理图表所需的数据聚合
  - 实现多维度数据聚合算法
  - 提供实时数据更新机制
  - 优化大数据量处理性能

- 图表配置服务：管理图表的样式和交互
  - 提供图表模板和配置选项
  - 支持用户自定义图表设置
  - 实现图表导出和分享功能

**图表类型：**
1. **趋势图表：**
   - 训练量趋势图(折线图)
   - 1RM变化趋势(面积图)
   - 训练频率分布(柱状图)
   - 恢复状态时间轴

2. **对比图表：**
   - 不同动作训练量对比(雷达图)
   - 部位训练分布(饼图)
   - 个人vs平均水平(对比图)
   - 目标达成进度(进度条)

3. **统计报表：**
   - 月度训练报告
   - 年度训练总结
   - 个人最佳记录
   - 训练习惯分析

**性能优化：**
- 数据懒加载和分页
- 图表缓存机制
- 响应式设计适配
- 交互性能优化

## 3.4 接口设计

### 3.4.1 RESTful API设计原则

系统API设计遵循RESTful架构风格，采用统一的URL命名规范和HTTP状态码，确保接口的一致性和可维护性。

**URL命名规范：**
- 使用小写字母和连字符
- 使用复数形式表示资源集合
- 使用层级关系表示资源关联
- 避免动词，使用名词表示资源

**HTTP状态码使用：**
- 200：请求成功
- 201：创建成功
- 400：请求参数错误
- 401：未授权
- 403：禁止访问
- 404：资源不存在
- 500：服务器内部错误

### 3.4.2 用户管理接口

**用户注册接口：**
```
POST /api/users/register
Content-Type: application/json

Request Body:
{
    "username": "string",
    "password": "string",
    "email": "string"
}

Response:
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "userId": "number",
        "username": "string",
        "email": "string",
        "role": "USER"
    }
}
```

**用户登录接口：**
```
POST /api/users/login
Content-Type: application/json

Request Body:
{
    "username": "string",
    "password": "string"
}

Response:
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "string",
        "refreshToken": "string",
        "expiresIn": "number",
        "user": {
            "id": "number",
            "username": "string",
            "email": "string",
            "role": "string"
        }
    }
}
```

**刷新令牌接口：**
```
POST /api/users/refresh
Content-Type: application/json

Request Body:
{
    "refreshToken": "string"
}

Response:
{
    "code": 200,
    "message": "令牌刷新成功",
    "data": {
        "token": "string",
        "refreshToken": "string",
        "expiresIn": "number"
    }
}
```

### 3.4.3 训练记录接口

**创建训练记录接口：**
```
POST /api/training/records
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
    "exerciseName": "string",
    "sets": "number",
    "reps": "number",
    "weight": "number",
    "trainingDate": "date",
    "duration": "number",
    "notes": "string"
}

Response:
{
    "code": 200,
    "message": "训练记录创建成功",
    "data": {
        "id": "number",
        "exerciseName": "string",
        "sets": "number",
        "reps": "number",
        "weight": "number",
        "trainingDate": "date",
        "duration": "number",
        "notes": "string",
        "totalVolume": "number",
        "trainingStress": "number"
    }
}
```

**查询训练记录接口：**
```
GET /api/training/records?startDate={date}&endDate={date}&exerciseName={string}&page={number}&size={number}
Authorization: Bearer {token}

Response:
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "content": [
            {
                "id": "number",
                "exerciseName": "string",
                "sets": "number",
                "reps": "number",
                "weight": "number",
                "trainingDate": "date",
                "duration": "number",
                "notes": "string",
                "totalVolume": "number",
                "trainingStress": "number"
            }
        ],
        "totalElements": "number",
        "totalPages": "number",
        "size": "number",
        "number": "number"
    }
}
```

### 3.4.4 负荷计算接口

**1RM计算接口：**
```
POST /api/fitness/calculate/1rm
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
    "exerciseName": "string",
    "weight": "number",
    "reps": "number"
}

Response:
{
    "code": 200,
    "message": "计算成功",
    "data": {
        "exerciseName": "string",
        "weight": "number",
        "reps": "number",
        "oneRepMax": {
            "epley": "number",
            "brzycki": "number",
            "lombardi": "number",
            "mayhew": "number",
            "oconner": "number",
            "average": "number"
        }
    }
}
```

**训练负荷计算接口：**
```
POST /api/fitness/calculate/load
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
    "records": [
        {
            "exerciseName": "string",
            "weight": "number",
            "sets": "number",
            "reps": "number"
        }
    ]
}

Response:
{
    "code": 200,
    "message": "计算成功",
    "data": {
        "totalVolume": "number",
        "exerciseVolumes": [
            {
                "exerciseName": "string",
                "volume": "number"
            }
        ],
        "averageIntensity": "number",
        "trainingDensity": "number"
    }
}
```

### 3.4.5 恢复监控接口

**创建恢复数据接口：**
```
POST /api/recovery/data
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
    "recoveryScore": "number",
    "sleepHours": "number",
    "sleepQuality": "number",
    "heartRateVariability": "number",
    "restingHeartRate": "number",
    "muscleSoreness": "number",
    "stressLevel": "number",
    "notes": "string"
}

Response:
{
    "code": 200,
    "message": "恢复数据创建成功",
    "data": {
        "id": "number",
        "recoveryScore": "number",
        "sleepHours": "number",
        "sleepQuality": "number",
        "heartRateVariability": "number",
        "restingHeartRate": "number",
        "muscleSoreness": "number",
        "stressLevel": "number",
        "notes": "string",
        "recoveryStatus": "string"
    }
}
```

**获取恢复状态接口：**
```
GET /api/recovery/status?startDate={date}&endDate={date}
Authorization: Bearer {token}

Response:
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "recoveryData": [
            {
                "date": "date",
                "recoveryScore": "number",
                "recoveryStatus": "string"
            }
        ],
        "averageRecoveryScore": "number",
        "recoveryTrend": "string",
        "recommendations": [
            "string"
        ]
    }
}
```

### 3.4.6 训练建议接口

**获取训练建议接口：**
```
GET /api/training/suggestions?date={date}
Authorization: Bearer {token}

Response:
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "date": "date",
        "suggestions": [
            {
                "type": "string",
                "content": "string",
                "confidenceScore": "number"
            }
        ],
        "recommendedIntensity": "number",
        "recommendedVolume": "number",
        "recommendedExercises": [
            {
                "name": "string",
                "recommendedSets": "number",
                "recommendedReps": "number",
                "recommendedWeight": "number"
            }
        ]
    }
}
```

### 3.4.7 数据统计接口

**获取训练统计接口：**
```
GET /api/statistics/training?startDate={date}&endDate={date}&type={string}
Authorization: Bearer {token}

Response:
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "totalWorkouts": "number",
        "totalVolume": "number",
        "averageIntensity": "number",
        "frequency": "number",
        "exerciseDistribution": [
            {
                "exerciseType": "string",
                "percentage": "number"
            }
        ],
        "progress": [
            {
                "date": "date",
                "value": "number"
            }
        ]
    }
}
```

## 3.5 安全设计

### 3.5.1 身份认证与授权

系统采用基于JWT的无状态认证机制，实现安全的用户身份验证和权限控制。

**认证流程：**
1. 用户使用用户名和密码登录
2. 服务器验证用户凭据
3. 验证成功后生成JWT访问令牌和刷新令牌
4. 客户端存储令牌并在后续请求中携带
5. 服务器验证令牌的有效性

**授权机制：**
- 基于角色的访问控制(RBAC)
- 用户角色：USER(普通用户)、ADMIN(管理员)
- 资源级权限控制
- 方法级权限验证

### 3.5.2 数据安全

**传输安全：**
- 使用HTTPS/TLS 1.3加密传输
- 敏感数据传输加密
- API请求签名验证

**存储安全：**
- 密码使用BCrypt算法加密存储
- 敏感个人信息加密存储
- 数据库连接加密
- 定期数据备份

**访问控制：**
- 用户只能访问自己的数据
- 管理员可访问所有数据
- 操作日志记录
- 异常访问检测

### 3.5.3 安全防护

**常见攻击防护：**
- SQL注入：使用参数化查询
- XSS攻击：输入输出过滤
- CSRF攻击：Token验证
- 暴力破解：登录尝试限制

**安全监控：**
- 异常登录检测
- 敏感操作监控
- 安全事件日志
- 定期安全审计

## 3.6 性能设计

### 3.6.1 数据库性能优化

**索引优化：**
- 合理设计索引策略
- 避免过度索引
- 定期分析查询性能
- 优化慢查询

**查询优化：**
- 使用分页查询
- 避免N+1查询问题
- 合理使用缓存
- 优化复杂查询

**连接池配置：**
- 合理设置连接池大小
- 连接超时配置
- 连接泄漏检测
- 连接池监控

### 3.6.2 应用性能优化

**缓存策略：**
- Redis缓存热点数据
- 本地缓存配置
- 缓存更新策略
- 缓存穿透防护

**异步处理：**
- 异步任务处理
- 消息队列应用
- 批量数据处理
- 非阻塞IO操作

**资源优化：**
- JVM参数调优
- 线程池配置
- 内存使用优化
- CPU使用优化

### 3.6.3 前端性能优化

**加载优化：**
- 代码分割和懒加载
- 资源压缩和合并
- CDN加速
- 浏览器缓存

**渲染优化：**
- 虚拟滚动
- 图表渲染优化
- 动画性能优化
- 响应式设计优化

**交互优化：**
- 防抖和节流
- 预加载策略
- 离线缓存
- 错误处理

## 3.7 可扩展性设计

### 3.7.1 水平扩展

**服务扩展：**
- 无状态服务设计
- 负载均衡配置
- 服务自动扩缩容
- 健康检查机制

**数据库扩展：**
- 读写分离
- 数据库分片
- 数据归档策略
- 分布式事务

### 3.7.2 功能扩展

**插件机制：**
- 插件接口定义
- 插件生命周期管理
- 插件配置管理
- 插件安全控制

**API扩展：**
- 版本控制策略
- 向后兼容设计
- API文档管理
- 开放API平台

### 3.7.3 多端扩展

**移动端适配：**
- 响应式设计
- 移动端优化
- 离线功能支持
- 原生应用开发

**第三方集成：**
- 智能设备集成
- 社交平台集成
- 健康平台集成
- 数据导入导出

## 3.8 测试设计

### 3.8.1 测试策略

**测试类型：**
- 单元测试：覆盖核心业务逻辑
- 集成测试：验证模块间交互
- 系统测试：验证整体功能
- 性能测试：验证系统性能

**测试环境：**
- 开发环境：开发人员自测
- 测试环境：QA团队测试
- 预生产环境：用户验收测试
- 生产环境：监控和回归测试

### 3.8.2 测试用例设计

**功能测试用例：**
- 用户注册登录功能
- 训练记录管理功能
- 负荷计算功能
- 恢复监控功能
- 数据可视化功能

**性能测试用例：**
- 并发用户测试
- 响应时间测试
- 吞吐量测试
- 稳定性测试

**安全测试用例：**
- 身份认证测试
- 权限控制测试
- 数据安全测试
- 攻击防护测试

## 3.9 部署设计

### 3.9.1 部署架构

**容器化部署：**
- Docker容器化
- Kubernetes编排
- 服务网格
- 自动化部署

**环境配置：**
- 开发环境配置
- 测试环境配置
- 生产环境配置
- 配置管理

### 3.9.2 监控与运维

**系统监控：**
- 应用性能监控
- 基础设施监控
- 业务指标监控
- 日志聚合分析

**运维自动化：**
- 自动化部署
- 自动化测试
- 自动化扩容
- 故障自动恢复

### 3.9.3 备份与恢复

**数据备份：**
- 定期全量备份
- 实时增量备份
- 异地备份
- 备份验证

**灾难恢复：**
- 恢复时间目标(RTO)
- 恢复点目标(RPO)
- 恢复流程设计
- 恢复演练

## 本章小结

本章详细设计了力量训练专业计算器APP的系统架构、数据库、核心模块、接口、安全、性能、可扩展性、测试和部署等方面的内容。系统采用分层架构设计，前后端分离，使用现代化的技术栈，确保系统的可靠性、安全性和可扩展性。通过合理的数据库设计和索引策略，优化系统性能；通过完善的接口设计，确保系统的可用性和可维护性；通过全面的安全设计，保护用户数据安全；通过灵活的可扩展性设计，支持系统的长期发展。整体设计遵循软件工程最佳实践，为系统的实现和维护提供了坚实的基础。