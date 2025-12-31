# Requirements Document

## Introduction

本文档定义健身管理系统后端短期改进计划的需求，包括服务层完善、数据验证增强、业务逻辑优化和API接口改进等方面，旨在提升系统的健壮性、可维护性和用户体验。

## Glossary

- **Fitness_System**: 力量训练负荷与恢复监控系统
- **Service_Layer**: 服务层，包含业务逻辑的核心组件
- **DTO**: Data Transfer Object，数据传输对象
- **Validation**: 数据验证，确保输入数据的正确性和完整性
- **Training_Plan**: 训练计划，包含多天训练安排的计划
- **Nutrition_Service**: 营养服务，处理营养记录和建议
- **Recovery_Service**: 恢复服务，评估用户恢复状态
- **Dashboard_Service**: 仪表盘服务，聚合展示数据

## Requirements

### Requirement 1: 训练计划服务完善

**User Story:** As a 用户, I want 创建和管理个性化训练计划, so that 能够系统化地进行力量训练

#### Acceptance Criteria

1. THE Fitness_System SHALL 支持创建包含多天训练的训练计划
2. THE Fitness_System SHALL 支持为每天添加多个训练动作
3. THE Fitness_System SHALL 支持设置每个动作的组数、次数和重量
4. THE Fitness_System SHALL 支持训练计划的启用和禁用
5. WHEN 训练计划被删除 THEN THE Fitness_System SHALL 级联删除关联的训练日和动作

### Requirement 2: 营养服务增强

**User Story:** As a 用户, I want 记录和分析我的营养摄入, so that 能够配合训练优化饮食

#### Acceptance Criteria

1. THE Fitness_System SHALL 支持记录每日营养摄入（蛋白质、碳水、脂肪、卡路里）
2. THE Fitness_System SHALL 根据用户体重和训练目标计算推荐营养摄入
3. THE Fitness_System SHALL 提供营养摄入趋势分析
4. THE Fitness_System SHALL 生成个性化营养建议
5. WHEN 营养摄入不足或过量 THEN THE Fitness_System SHALL 发出提醒

### Requirement 3: 恢复评估服务优化

**User Story:** As a 用户, I want 准确评估我的恢复状态, so that 能够合理安排训练强度

#### Acceptance Criteria

1. THE Fitness_System SHALL 基于睡眠质量、肌肉酸痛、疲劳程度等多维度评估恢复状态
2. THE Fitness_System SHALL 计算综合恢复评分（0-100分）
3. THE Fitness_System SHALL 根据恢复评分给出训练建议
4. THE Fitness_System SHALL 支持恢复数据的历史趋势查看
5. WHEN 恢复评分低于阈值 THEN THE Fitness_System SHALL 建议休息或轻度训练

### Requirement 4: 仪表盘数据聚合

**User Story:** As a 用户, I want 在仪表盘查看训练概览, so that 能够快速了解训练进度

#### Acceptance Criteria

1. THE Fitness_System SHALL 展示本周训练次数和总训练量
2. THE Fitness_System SHALL 展示最近的恢复状态评分
3. THE Fitness_System SHALL 展示主要动作的1RM进步趋势
4. THE Fitness_System SHALL 展示营养摄入达标情况
5. THE Fitness_System SHALL 支持自定义时间范围的数据查询

### Requirement 5: 数据验证增强

**User Story:** As a 开发者, I want 完善数据验证机制, so that 确保数据的正确性和安全性

#### Acceptance Criteria

1. THE Fitness_System SHALL 验证所有输入数据的格式和范围
2. THE Fitness_System SHALL 对训练数据进行合理性校验（如重量、次数范围）
3. THE Fitness_System SHALL 防止重复提交和并发冲突
4. THE Fitness_System SHALL 返回清晰的验证错误信息
5. IF 数据验证失败 THEN THE Fitness_System SHALL 返回400状态码和详细错误信息

### Requirement 6: 训练建议智能化

**User Story:** As a 用户, I want 获得智能训练建议, so that 能够科学地提升训练效果

#### Acceptance Criteria

1. THE Fitness_System SHALL 根据历史训练数据分析训练模式
2. THE Fitness_System SHALL 识别训练薄弱环节并给出改进建议
3. THE Fitness_System SHALL 根据恢复状态调整训练强度建议
4. THE Fitness_System SHALL 提供渐进式超负荷建议
5. WHEN 用户长时间未训练某肌群 THEN THE Fitness_System SHALL 提醒平衡训练

### Requirement 7: 有氧训练数据管理

**User Story:** As a 用户, I want 记录有氧训练数据, so that 能够全面追踪训练情况

#### Acceptance Criteria

1. THE Fitness_System SHALL 支持记录有氧训练类型（跑步、骑行、游泳等）
2. THE Fitness_System SHALL 支持记录训练时长、距离、心率
3. THE Fitness_System SHALL 计算有氧训练消耗的卡路里
4. THE Fitness_System SHALL 支持有氧训练数据的统计分析
5. THE Fitness_System SHALL 将有氧训练纳入整体训练负荷计算

### Requirement 8: API响应优化

**User Story:** As a 前端开发者, I want API返回一致且完整的数据, so that 能够高效地开发前端功能

#### Acceptance Criteria

1. THE Fitness_System SHALL 统一所有API的响应格式
2. THE Fitness_System SHALL 为列表接口提供分页支持
3. THE Fitness_System SHALL 支持字段过滤减少数据传输
4. THE Fitness_System SHALL 提供合理的默认值避免空指针
5. WHEN API调用失败 THEN THE Fitness_System SHALL 返回统一格式的错误响应

