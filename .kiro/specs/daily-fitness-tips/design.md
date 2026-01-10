# Technical Design Document

## Introduction

本文档描述"每日健身小贴士"功能的技术设计，包括组件架构、数据模型、接口定义和正确性属性。

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Dashboard.vue                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              DailyTipCard.vue                        │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │    │
│  │  │ Category    │  │ Tip Content │  │ Actions     │  │    │
│  │  │ Badge/Icon  │  │ Title+Body  │  │ Refresh/Fav │  │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    tipService.js                             │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │ getTodayTip()   │  │ getRandomTip()  │                   │
│  └─────────────────┘  └─────────────────┘                   │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │ toggleFavorite()│  │ getFavorites()  │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    fitnessTips.js (Data)                     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ 30+ Pre-defined Tips with Categories                 │    │
│  │ - 力量训练 (Strength)                                │    │
│  │ - 有氧运动 (Cardio)                                  │    │
│  │ - 营养饮食 (Nutrition)                               │    │
│  │ - 恢复休息 (Recovery)                                │    │
│  │ - 训练技巧 (Technique)                               │    │
│  │ - 心理调节 (Mental)                                  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Data Models

### Tip Data Model

```typescript
interface FitnessTip {
  id: number;                    // 唯一标识符
  title: string;                 // 标题 (max 30 chars)
  content: string;               // 内容 (max 200 chars)
  category: TipCategory;         // 分类
}

type TipCategory = 
  | 'strength'    // 力量训练
  | 'cardio'      // 有氧运动
  | 'nutrition'   // 营养饮食
  | 'recovery'    // 恢复休息
  | 'technique'   // 训练技巧
  | 'mental';     // 心理调节

interface CategoryConfig {
  key: TipCategory;
  label: string;                 // 中文名称
  icon: string;                  // Emoji 图标
  color: string;                 // 主题色 (CSS variable)
}
```

### Local Storage Schema

```typescript
interface TipFavorites {
  tipIds: number[];              // 收藏的小贴士 ID 列表
  lastUpdated: string;           // ISO 日期字符串
}

// localStorage key: 'fitness_tip_favorites'
```

## Component Interfaces

### DailyTipCard.vue Props & Events

```typescript
// Props
interface DailyTipCardProps {
  // 无需外部 props，组件自管理状态
}

// Emits
interface DailyTipCardEmits {
  // 无需外部事件
}

// Internal State
interface DailyTipCardState {
  currentTip: FitnessTip | null;
  isLoading: boolean;
  isFavorited: boolean;
  isRefreshing: boolean;
}
```

### tipService.js API

```typescript
// 获取今日小贴士（基于日期确定性选择）
function getTodayTip(): FitnessTip;

// 获取随机小贴士（排除当前显示的）
function getRandomTip(excludeId?: number): FitnessTip;

// 切换收藏状态
function toggleFavorite(tipId: number): boolean;

// 检查是否已收藏
function isFavorited(tipId: number): boolean;

// 获取所有收藏
function getFavorites(): number[];
```

## Category Configuration

```javascript
const CATEGORY_CONFIG = {
  strength: {
    label: '力量训练',
    icon: '💪',
    color: 'var(--color-primary)'
  },
  cardio: {
    label: '有氧运动',
    icon: '🏃',
    color: 'var(--color-success)'
  },
  nutrition: {
    label: '营养饮食',
    icon: '🥗',
    color: 'var(--color-warning)'
  },
  recovery: {
    label: '恢复休息',
    icon: '😴',
    color: 'var(--color-info)'
  },
  technique: {
    label: '训练技巧',
    icon: '🎯',
    color: 'var(--color-secondary)'
  },
  mental: {
    label: '心理调节',
    icon: '🧘',
    color: 'var(--color-accent)'
  }
};
```

## Styling Approach

卡片使用现有设计系统的统一样式：

- 基础样式：`card-unified` 类
- 玻璃态效果：`glass-card` 类
- 入场动画：`animate-fade-in-up` 类
- 响应式：使用 `_responsive.scss` 断点

## Correctness Properties

### Property 1: 日期确定性
```
∀ date D, getTodayTip() called on D always returns the same tip
```

### Property 2: 30天无重复
```
∀ consecutive 30 days, each day's tip is unique
```

### Property 3: 内容长度约束
```
∀ tip T: T.title.length ≤ 30 ∧ T.content.length ≤ 200
```

### Property 4: 分类完整性
```
∀ tip T: T.category ∈ {strength, cardio, nutrition, recovery, technique, mental}
```

### Property 5: 收藏持久性
```
∀ tipId, toggleFavorite(tipId) → isFavorited(tipId) persists across page reloads
```

## File Structure

```
Fitness/frontend/src/
├── components/
│   └── DailyTipCard.vue          # 小贴士卡片组件
├── data/
│   └── fitnessTips.js            # 小贴士数据
└── services/
    └── tipService.js             # 小贴士服务
```

## Integration Point

在 `Dashboard.vue` 的欢迎区域后添加小贴士卡片：

```vue
<!-- 在 welcome-section 之后 -->
<section class="tip-section">
  <DailyTipCard />
</section>
```
