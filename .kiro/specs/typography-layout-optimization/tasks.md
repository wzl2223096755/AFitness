# Implementation Plan: Typography and Layout Optimization

## Overview

本实现计划将字体排版和内容布局优化分解为可执行的编码任务。采用渐进式实现策略，先建立设计令牌基础，再构建排版和布局系统，最后更新现有组件。

## Tasks

- [x] 1. 创建设计令牌基础文件
  - [x] 1.1 创建 `_tokens.scss` 文件定义所有设计令牌
    - 定义字体族变量（sans、mono）包含中文字体栈
    - 定义模块化字号比例（2xs 到 5xl，1.25 比例）
    - 定义行高令牌（none、tight、snug、normal、relaxed、loose）
    - 定义字重令牌（light 到 extrabold）
    - 定义字间距令牌
    - 定义间距比例尺（0-16，4px 基准单位）
    - _Requirements: 1.1, 1.5, 4.1_

  - [x] 1.2 编写设计令牌属性测试
    - **Property 1: Type Scale Modular Consistency**
    - **Validates: Requirements 1.1**

- [x] 2. 实现排版系统
  - [x] 2.1 创建 `_typography.scss` 文件
    - 实现 heading-1 到 heading-4 混入
    - 实现 body-text 混入（sm、base、lg 变体）
    - 实现 fluid-type 混入使用 CSS clamp()
    - 定义文本工具类（text-xs 到 text-5xl）
    - _Requirements: 1.1, 1.2, 1.3, 2.3_

  - [x] 2.2 编写标题字重属性测试
    - **Property 2: Heading Font Weight Range**
    - **Validates: Requirements 1.2**

  - [x] 2.3 编写正文行高属性测试
    - **Property 3: Body Text Line Height Range**
    - **Validates: Requirements 1.3**

- [x] 3. 实现响应式排版
  - [x] 3.1 添加响应式字体缩放规则
    - 在 768px 断点减少基础字号 10-15%
    - 在 480px 断点按比例减少标题字号
    - 确保最小字号不低于 14px
    - 使用 clamp() 实现流体排版
    - _Requirements: 2.1, 2.2, 2.4_

  - [x] 3.2 编写响应式字体缩放属性测试
    - **Property 4: Responsive Font Scaling with Minimum**
    - **Validates: Requirements 2.1, 2.2, 2.4**

  - [x] 3.3 编写视觉层次比例属性测试
    - **Property 5: Visual Hierarchy Ratio Preservation**
    - **Validates: Requirements 2.5, 5.4**

- [x] 4. Checkpoint - 确保排版系统测试通过
  - 运行所有属性测试确保通过
  - 如有问题请询问用户

- [x] 5. 实现间距系统
  - [x] 5.1 创建 `_spacing.scss` 文件
    - 定义语义化间距令牌（tight、normal、loose、section）
    - 定义卡片内边距变量（sm、md、lg）
    - 定义表单字段间距变量
    - 定义表格单元格内边距变量
    - 创建间距工具类（m-0 到 m-16、p-0 到 p-16）
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 5.2 编写间距比例尺属性测试
    - **Property 6: Spacing Scale Adherence**
    - **Validates: Requirements 4.1, 4.2, 4.4**

- [x] 6. 实现网格布局系统
  - [x] 6.1 创建 `_grid.scss` 文件
    - 实现 grid-container 混入（12列网格）
    - 实现 auto-grid 混入（最小宽度 280px）
    - 实现 content-container 混入（最大宽度 75ch）
    - 实现 section-spacing 混入
    - 定义响应式 gutter 宽度（16px、20px、24px）
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 1.4_

- [x] 7. 更新现有变量文件
  - [x] 7.1 更新 `variables.scss` 整合新令牌
    - 导入新的令牌文件
    - 保持向后兼容性
    - 添加新的响应式断点变量
    - _Requirements: 1.1, 4.1_

- [x] 8. 更新基础样式
  - [x] 8.1 更新 `base.scss` 应用新排版系统
    - 更新 html/body 字体设置
    - 应用新的字号和行高变量
    - 更新文本工具类使用新令牌
    - 添加内容容器最大宽度限制
    - _Requirements: 1.1, 1.3, 1.4, 1.6_

- [x] 9. Checkpoint - 确保基础样式更新正确
  - 运行测试确保无回归
  - 如有问题请询问用户

- [x] 10. 更新组件样式
  - [x] 10.1 更新 `components.scss` 卡片组件样式
    - 更新 stats-card 内边距使用新间距变量
    - 更新 chart-container 间距
    - 更新 metric-card 内部布局
    - 确保卡片内边距在 20-28px 范围
    - _Requirements: 6.1, 6.2, 6.4, 6.5_

  - [x] 10.2 编写组件内边距属性测试
    - **Property 7: Component Padding Range Compliance**
    - **Validates: Requirements 6.1, 6.2, 7.3, 8.2**

  - [x] 10.3 更新表格和列表样式
    - 添加 tabular-nums 字体特性
    - 更新表头样式（字重、颜色）
    - 更新单元格内边距（12-16px）
    - 更新列表项间距
    - 数字列右对齐
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 10.4 更新表单样式
    - 更新标签字号（14px）
    - 更新输入框字号（16px）
    - 更新字段间距（16-24px）
    - 更新水平表单对齐
    - _Requirements: 8.1, 8.2, 8.4, 8.5_

- [x] 11. 实现视觉层次优化
  - [x] 11.1 更新标题层级样式
    - 定义 h1-h4 样式使用新排版混入
    - 确保标题字重在 600-800 范围
    - 更新数据值样式（大于标签）
    - 更新图标尺寸与文本对齐
    - _Requirements: 5.1, 5.4, 5.5_

  - [x] 11.2 编写颜色对比度属性测试
    - **Property 8: Color Contrast WCAG Compliance**
    - **Validates: Requirements 5.3**

- [x] 12. 更新响应式样式
  - [x] 12.1 更新 `responsive.scss` 应用新系统
    - 更新移动端字号缩放
    - 更新移动端间距调整
    - 更新移动端网格布局
    - _Requirements: 2.1, 2.2, 4.5_

- [x] 13. 更新主要视图组件
  - [x] 13.1 更新 Dashboard 相关组件样式
    - 更新 MetricsOverview.vue 使用新排版类
    - 更新 DynamicDashboard.vue 布局
    - 确保卡片网格使用 auto-grid
    - _Requirements: 3.2, 6.1, 6.2_

  - [x] 13.2 更新数据展示页面样式
    - 更新 TrainingData.vue 表格样式
    - 更新 NutritionTracking.vue 布局
    - 更新 HistoryStatistics.vue 图表容器
    - _Requirements: 7.1, 7.2, 7.3_

- [x] 14. 更新样式入口文件
  - [x] 14.1 更新 `index.scss` 导入新样式文件
    - 导入 `_tokens.scss`
    - 导入 `_typography.scss`
    - 导入 `_spacing.scss`
    - 导入 `_grid.scss`
    - 确保导入顺序正确
    - _Requirements: 1.1, 4.1_

- [x] 15. Final Checkpoint - 完整测试验证
  - 运行所有属性测试确保通过
  - 验证响应式布局在各断点正常
  - 如有问题请询问用户

## Notes

- 所有任务均为必须执行，包括属性测试任务
- 每个任务引用具体需求以确保可追溯性
- 检查点任务用于增量验证
- 属性测试验证通用正确性属性
- 单元测试验证具体示例和边界情况
- 任务 1-4 已完成：设计令牌、排版系统、响应式排版及相关属性测试
- 任务 5-15 待完成：间距系统、网格布局、样式整合及组件更新
