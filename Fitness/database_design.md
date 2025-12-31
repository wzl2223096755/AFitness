# Fitness应用数据库设计方案

## 1. 数据库概述

Fitness应用是一个综合性的健身管理系统，采用Spring Boot + JPA + H2/MySQL技术栈，支持用户管理、训练记录、恢复监控、数据分析等功能。

### 技术特点
- **数据库类型**: H2内存数据库（开发/测试）→ MySQL（生产环境）
- **ORM框架**: Spring Data JPA + Hibernate
- **连接池**: HikariCP
- **审计功能**: JPA Auditing
- **缓存策略**: 二级缓存禁用，使用一级缓存

## 2. 数据库架构设计

### 2.1 核心实体关系图

```
用户表 (user_table)
├── 训练记录表 (training_records) [1:N]
│   └── 动作详情表 (exercise_details) [1:N]
├── 力量训练数据表 (strength_training_data) [1:N]
├── 有氧训练数据表 (cardio_training_data) [1:N]
├── 恢复数据表 (recovery_data) [1:N]
├── 健身数据表 (fitness_data) [1:N]
├── 恢复指标表 (recovery_metrics) [1:N]
└── 训练建议表 (training_advices) [1:N]
```

## 3. 详细表结构设计

### 3.1 用户管理模块

#### 3.1.1 用户表 (user_table)
```sql
CREATE TABLE user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    email VARCHAR(100) COMMENT '邮箱地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) COMMENT '用户基本信息表';
```

**字段说明:**
- `id`: 主键，自增长
- `username`: 用户名，唯一索引，3-50字符
- `password`: BCrypt加密密码，最少6位
- `email`: 邮箱地址，用于找回密码等
- `created_at`: 账户创建时间
- `role`: 用户角色，USER或ADMIN

### 3.2 训练记录模块

#### 3.2.1 训练记录表 (training_records)
```sql
CREATE TABLE training_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    exercise_name VARCHAR(100) NOT NULL COMMENT '运动名称',
    sets INT NOT NULL COMMENT '组数',
    reps INT NOT NULL COMMENT '次数',
    weight DOUBLE NOT NULL COMMENT '重量(kg)',
    training_date DATE NOT NULL COMMENT '训练日期',
    duration INT NOT NULL COMMENT '训练时长(分钟)',
    notes VARCHAR(500) COMMENT '备注',
    total_volume DOUBLE COMMENT '总训练量',
    training_stress DOUBLE COMMENT '训练压力',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_training_date (training_date),
    INDEX idx_exercise_name (exercise_name),
    INDEX idx_user_date (user_id, training_date)
) COMMENT '训练记录主表';
```

#### 3.2.2 动作详情表 (exercise_details)
```sql
CREATE TABLE exercise_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL COMMENT '训练记录ID',
    exercise_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    weight DOUBLE COMMENT '重量(kg)',
    sets INT COMMENT '组数',
    reps INT COMMENT '次数',
    rpe INT COMMENT '感知疲劳指数(1-10)',
    volume DOUBLE COMMENT '训练量',
    exercise_type VARCHAR(50) COMMENT '动作类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (record_id) REFERENCES training_records(id) ON DELETE CASCADE,
    INDEX idx_record_id (record_id),
    INDEX idx_exercise_name (exercise_name)
) COMMENT '训练动作详情表';
```

### 3.3 专业训练数据模块

#### 3.3.1 力量训练数据表 (strength_training_data)
```sql
CREATE TABLE strength_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    exercise_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    weight DOUBLE NOT NULL COMMENT '重量(kg)',
    sets INT NOT NULL COMMENT '组数',
    reps INT NOT NULL COMMENT '次数',
    exercise_type VARCHAR(50) NOT NULL COMMENT '动作类型',
    one_rep_max DOUBLE COMMENT '最大重量估算',
    training_volume DOUBLE COMMENT '训练量',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '力量训练数据表';
```

#### 3.3.2 有氧训练数据表 (cardio_training_data)
```sql
CREATE TABLE cardio_training_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    exercise_type VARCHAR(100) NOT NULL COMMENT '运动类型',
    duration INT NOT NULL COMMENT '运动时长(分钟)',
    distance DOUBLE COMMENT '距离(km)',
    average_heart_rate INT COMMENT '平均心率',
    max_heart_rate INT COMMENT '最大心率',
    calories_burned DOUBLE COMMENT '消耗卡路里',
    average_speed DOUBLE COMMENT '平均速度(km/h)',
    pace DOUBLE COMMENT '配速(分钟/km)',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_exercise_type (exercise_type)
) COMMENT '有氧训练数据表';
```

### 3.4 恢复监控模块

#### 3.4.1 恢复数据表 (recovery_data)
```sql
CREATE TABLE recovery_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '记录时间',
    recovery_score INT NOT NULL COMMENT '恢复评分(1-100)',
    sleep_hours DOUBLE NOT NULL COMMENT '睡眠时长',
    sleep_quality INT NOT NULL COMMENT '睡眠质量(1-10)',
    heart_rate_variability INT NOT NULL COMMENT '心率变异性',
    resting_heart_rate INT NOT NULL COMMENT '静息心率',
    muscle_soreness DOUBLE COMMENT '肌肉酸痛度(1-10)',
    stress_level DOUBLE COMMENT '压力水平(1-10)',
    notes TEXT COMMENT '恢复备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '恢复状态数据表';
```

#### 3.4.2 恢复指标表 (recovery_metrics)
```sql
CREATE TABLE recovery_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    muscle_soreness INT COMMENT '肌肉酸痛度(1-5)',
    sleep_quality INT COMMENT '睡眠质量(1-5)',
    resting_heart_rate INT COMMENT '静息心率',
    subjective_energy INT COMMENT '主观能量(1-5)',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    INDEX idx_user_date (user_id, record_date),
    UNIQUE KEY uk_user_date (user_id, record_date)
) COMMENT '每日恢复指标表';
```

### 3.5 综合数据模块

#### 3.5.1 健身数据表 (fitness_data)
```sql
CREATE TABLE fitness_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    timestamp TIMESTAMP NOT NULL COMMENT '数据时间戳',
    
    -- 力量训练相关字段
    exercise_name VARCHAR(100) COMMENT '动作名称',
    weight DOUBLE COMMENT '重量(kg)',
    sets INT COMMENT '组数',
    reps INT COMMENT '次数',
    exercise_type VARCHAR(50) COMMENT '动作类型',
    one_rep_max DOUBLE COMMENT '最大重量估算',
    training_volume DOUBLE COMMENT '训练量',
    perceived_exertion INT COMMENT '主观疲劳度(1-10)',
    
    -- 恢复状态相关字段
    recovery_score INT COMMENT '恢复评分(0-100)',
    recovery_status VARCHAR(100) COMMENT '恢复状态描述',
    sleep_hours INT COMMENT '睡眠时长',
    stress_level INT COMMENT '压力水平',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp)
) COMMENT '综合健身数据表';
```

### 3.6 智能建议模块

#### 3.6.1 训练建议表 (training_advices)
```sql
CREATE TABLE training_advices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    advice_date DATE NOT NULL COMMENT '建议日期',
    advice_type VARCHAR(50) COMMENT '建议类型',
    content TEXT COMMENT '建议内容',
    confidence_score DOUBLE COMMENT '置信度评分',
    
    FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_advice_date (advice_date),
    INDEX idx_advice_type (advice_type),
    INDEX idx_user_date (user_id, advice_date)
) COMMENT '智能训练建议表';
```

## 4. 数据库索引策略

### 4.1 主要索引
- **用户相关**: `username`(唯一), `email`, `created_at`
- **时间相关**: `timestamp`, `training_date`, `created_at`
- **用户+时间复合索引**: `(user_id, timestamp)`, `(user_id, training_date)`
- **业务查询索引**: `exercise_name`, `exercise_type`, `advice_type`

### 4.2 索引优化原则
1. **外键索引**: 所有外键字段自动创建索引
2. **查询优化**: 根据常用查询条件创建复合索引
3. **唯一性约束**: 用户名、用户+日期等唯一约束
4. **避免过度索引**: 平衡查询性能和写入性能

## 5. 数据库配置

### 5.1 开发环境 (H2)
```properties
spring.datasource.url=jdbc:h2:mem:strength_monitor_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 5.2 生产环境 (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitness_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=fitness_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## 6. 数据库性能优化

### 6.1 连接池配置
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
```

### 6.2 JPA优化配置
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.fetch_size=100
spring.jpa.open-in-view=false
```

## 7. 数据备份与恢复

### 7.1 备份策略
- **开发环境**: H2内存数据库，重启后自动清空
- **测试环境**: 定期导出SQL脚本
- **生产环境**: 每日全量备份 + 实时增量备份

### 7.2 数据清理
- **测试数据**: 测试完成后自动清理
- **历史数据**: 定期归档超过1年的历史数据
- **无效数据**: 定期清理孤儿数据和无效记录

## 8. 数据安全

### 8.1 密码安全
- 使用BCrypt算法加密存储密码
- 密码最小长度6位
- 支持密码强度验证

### 8.2 数据访问控制
- 基于角色的访问控制(RBAC)
- 用户只能访问自己的数据
- 管理员可访问所有数据

### 8.3 SQL注入防护
- 使用JPA参数化查询
- 禁用动态SQL拼接
- 输入参数验证和过滤

## 9. 监控与维护

### 9.1 健康检查
- 数据库连接状态监控
- 查询性能监控
- 慢查询日志记录

### 9.2 数据质量
- 数据完整性约束
- 业务规则验证
- 数据一致性检查

## 10. 扩展性设计

### 10.1 水平扩展
- 支持读写分离
- 支持分库分表
- 支持数据归档

### 10.2 功能扩展
- 预留扩展字段
- 支持插件化数据模型
- 支持多租户架构

---

**设计原则:**
1. **数据一致性**: 通过外键约束保证数据完整性
2. **性能优化**: 合理设计索引，优化查询性能
3. **扩展性**: 预留扩展空间，支持功能迭代
4. **安全性**: 多层次安全防护，保护用户隐私
5. **可维护性**: 清晰的表结构设计，便于后期维护