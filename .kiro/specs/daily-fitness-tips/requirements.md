# Requirements Document

## Introduction

本规范定义了 Fitness 健身管理系统的"每日健身小贴士"功能需求。该功能在 Dashboard 页面展示一个精美的卡片，每天为用户提供一条健身相关的知识、技巧或建议，帮助用户学习健身知识，提升训练效果。

## Glossary

- **Daily_Tip_Card**: 每日小贴士卡片组件，展示当天的健身知识
- **Tip_Content**: 小贴士内容，包含标题、正文、分类和图标
- **Tip_Category**: 小贴士分类，如力量训练、有氧运动、营养饮食、恢复休息等
- **Tip_Service**: 小贴士服务，负责获取和管理小贴士数据
- **Local_Storage**: 本地存储，用于缓存已查看的小贴士

## Requirements

### Requirement 1: 每日小贴士卡片展示

**User Story:** As a user, I want to see a daily fitness tip on my dashboard, so that I can learn new fitness knowledge every day.

#### Acceptance Criteria

1. WHEN a user visits the Dashboard page THEN THE Daily_Tip_Card SHALL display the tip of the day
2. THE Daily_Tip_Card SHALL display a title (max 30 characters), content (max 200 characters), and category icon
3. THE Daily_Tip_Card SHALL use the unified card style from the design system with glassmorphism effect
4. WHEN the tip content is loading THEN THE Daily_Tip_Card SHALL display a skeleton loading animation
5. IF the tip fails to load THEN THE Daily_Tip_Card SHALL display a fallback tip from local cache

### Requirement 2: 小贴士内容分类

**User Story:** As a user, I want tips to be categorized, so that I can easily identify the type of fitness knowledge.

#### Acceptance Criteria

1. THE Tip_Content SHALL belong to one of the following categories: 力量训练, 有氧运动, 营养饮食, 恢复休息, 训练技巧, 心理调节
2. WHEN displaying a tip THEN THE Daily_Tip_Card SHALL show a category-specific icon and color accent
3. THE Daily_Tip_Card SHALL display the category name as a badge

### Requirement 3: 小贴士数据管理

**User Story:** As a system, I want to manage a collection of fitness tips, so that users receive different tips each day.

#### Acceptance Criteria

1. THE Tip_Service SHALL provide at least 30 pre-defined fitness tips covering all categories
2. WHEN selecting the daily tip THEN THE Tip_Service SHALL use the current date to deterministically select a tip
3. THE Tip_Service SHALL ensure the same tip is shown throughout the entire day (based on local date)
4. THE Tip_Service SHALL cycle through tips without repetition within a 30-day period

### Requirement 4: 用户交互

**User Story:** As a user, I want to interact with the tip card, so that I can save or share useful tips.

#### Acceptance Criteria

1. WHEN a user clicks the refresh button THEN THE Daily_Tip_Card SHALL display a random different tip
2. WHEN a user clicks the favorite button THEN THE Tip_Service SHALL save the tip to user's favorites in local storage
3. THE Daily_Tip_Card SHALL display a subtle entrance animation when first loaded

### Requirement 5: 响应式设计

**User Story:** As a mobile user, I want the tip card to display properly on my device, so that I can read tips comfortably.

#### Acceptance Criteria

1. THE Daily_Tip_Card SHALL adapt its layout for mobile screens (< 768px)
2. WHEN viewport width is below 480px THEN THE Daily_Tip_Card SHALL use full width layout
3. THE Daily_Tip_Card SHALL maintain minimum touch target size of 44px for interactive elements

