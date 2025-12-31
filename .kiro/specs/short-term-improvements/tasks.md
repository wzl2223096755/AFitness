# Implementation Plan: 健身管理系统后端短期改进

## Overview

本任务列表基于需求和设计文档，按优先级组织后端改进任务。

## Tasks

- [x] 1. 营养服务增强
  - [x] 1.1 创建营养计算服务
    - 创建 NutritionCalculationService 接口
    - 实现根据体重和训练目标计算推荐营养摄入
    - 实现营养摄入趋势分析
    - _Requirements: 2.2, 2.3_

  - [x] 1.2 创建营养目标实体和Repository
    - 创建 UserNutritionGoal 实体类
    - 创建 UserNutritionGoalRepository
    - 添加数据库表结构
    - _Requirements: 2.2_

  - [x] 1.3 增强营养建议生成逻辑
    - 根据用户训练目标生成个性化建议
    - 根据营养摄入不足/过量生成提醒
    - 添加营养素平衡建议
    - _Requirements: 2.4, 2.5_

  - [x] 1.4 编写营养计算属性测试

    - **Property 1: 营养推荐计算一致性**
    - **Property 3: 营养素占比计算正确性**
    - **Validates: Requirements 2.2, 2.3**

  - [x] 1.5 添加营养目标API端点
    - GET /api/v1/nutrition/goals - 获取用户营养目标
    - POST /api/v1/nutrition/goals - 设置用户营养目标
    - GET /api/v1/nutrition/recommendation - 获取推荐营养摄入
    - _Requirements: 2.2_

- [x] 2. 检查点 - 营养服务验证
  - 确保所有测试通过，如有问题请咨询用户

- [x] 3. 训练计划服务完善
  - [x] 3.1 完善训练计划CRUD操作
    - 实现创建包含多天训练的计划
    - 实现为每天添加多个训练动作
    - 实现训练计划的启用/禁用
    - _Requirements: 1.1, 1.2, 1.4_

  - [x] 3.2 添加训练计划级联删除
    - 实现删除计划时级联删除训练日和动作
    - 添加软删除支持
    - _Requirements: 1.5_

  - [x] 3.3 编写训练计划集成测试
    - 测试计划创建完整流程 ✓
    - 测试级联删除功能 ✓
    - _Requirements: 1.1, 1.5_

- [x] 4. 恢复评估服务优化
  - [x] 4.1 增强恢复评分算法
    - 基于睡眠质量、肌肉酸痛、疲劳程度多维度评估
    - 计算综合恢复评分（0-100分）
    - _Requirements: 3.1, 3.2_

  - [x] 4.2 添加恢复状态训练建议
    - 根据恢复评分给出训练强度建议
    - 低恢复评分时建议休息或轻度训练
    - _Requirements: 3.3, 3.5_

  - [x] 4.3 编写恢复评分属性测试

    - **Property: 恢复评分范围验证**
    - **Validates: Requirements 3.2**

- [x] 5. 检查点 - 核心服务验证
  - 确保所有测试通过，如有问题请咨询用户

- [x] 6. 仪表盘数据聚合
  - [x] 6.1 实现训练概览数据聚合
    - 本周训练次数统计
    - 总训练量计算
    - _Requirements: 4.1_

  - [x] 6.2 实现进步趋势数据
    - 主要动作1RM进步趋势
    - 营养摄入达标情况
    - _Requirements: 4.3, 4.4_

  - [x] 6.3 支持自定义时间范围查询
    - 添加时间范围参数
    - 优化查询性能
    - _Requirements: 4.5_

- [x] 7. 数据验证增强
  - [x] 7.1 完善输入数据验证
    - 添加训练数据合理性校验
    - 添加营养数据范围验证
    - _Requirements: 5.1, 5.2_

  - [x] 7.2 统一验证错误响应
    - 创建 ValidationErrorResponse DTO
    - 增强 GlobalExceptionHandler 处理各类验证异常
    - 返回清晰的验证错误信息（字段级别和全局级别）
    - 返回400状态码和详细错误
    - _Requirements: 5.4, 5.5_

- [x] 8. API响应优化
  - [x] 8.1 统一响应格式
    - 确保所有API使用ApiResponse格式
    - 添加合理的默认值
    - _Requirements: 8.1, 8.4_

  - [x] 8.2 完善分页支持
    - 创建 PageResponse DTO
    - 创建 PageRequest DTO
    - 为 TrainingController 添加分页查询端点
    - 统一分页响应格式
    - _Requirements: 8.2_

- [x] 9. 最终检查点
  - 所有改进已完成
  - 代码无编译错误

- [x] 10. 前后端API对应检查
  - [x] 10.1 分析fitness.js中的API调用与后端控制器对应关系
    - 所有API端点匹配正确
  - [x] 10.2 分析nutrition.js中的API调用与后端控制器对应关系
    - 修复: `getNutritionStatsByDateRange` 路径从 `/api/v1/nutrition/stats` 改为 `/api/v1/nutrition/stats/range`
  - [x] 10.3 验证DashboardMetricsResponse字段与前端MetricsOverview.vue期望的数据匹配
    - 所有字段完全匹配

## Notes

- 任务标记 `*` 为可选测试任务
- 属性测试使用 jqwik 框架
- 每个检查点确保阶段性功能完整
