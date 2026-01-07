# Design Document: Frontend Style Optimization

## Overview

本设计文档详细描述 Fitness 健身管理系统前端样式全面优化的技术实现方案。基于现有的 Neon Ionized 设计系统，通过增强动画系统、优化响应式布局、统一组件样式、提升 CSS 性能和完善深色主题，打造一个视觉精致、交互流畅、性能优异的现代化健身应用界面。

## Architecture

### 样式架构层次

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  (Vue Components with scoped styles)                        │
├─────────────────────────────────────────────────────────────┤
│                    Component Layer                           │
│  (Unified component styles: cards, buttons, forms, tables)  │
├─────────────────────────────────────────────────────────────┤
│                    Animation Layer                           │
│  (Keyframes, transitions, micro-interactions)               │
├─────────────────────────────────────────────────────────────┤
│                    Theme Layer                               │
│  (Dark/Light theme variables, semantic colors)              │
├─────────────────────────────────────────────────────────────┤
│                    Layout Layer                              │
│  (Grid system, responsive breakpoints, spacing)             │
├─────────────────────────────────────────────────────────────┤
│                    Foundation Layer                          │
│  (Design tokens: colors, typography, shadows)               │
└─────────────────────────────────────────────────────────────┘
```

### 文件结构

```
src/assets/styles/
├── _tokens.scss           # 设计令牌（增强）
├── _animations.scss       # 动画系统（新增/增强）
├── _shadows.scss          # 阴影系统（新增）
├── _responsive.scss       # 响应式系统（增强）
├── _components.scss       # 组件样式（统一）
├── _theme.scss            # 主题系统（完善）
├── _performance.scss      # 性能优化（新增）
├── _micro-interactions.scss # 微交互（新增）
├── variables.scss         # 变量整合
├── base.scss              # 基础样式
└── index.scss             # 入口文件
```

## Components and Interfaces

### 1. 动画系统 (Animation System)

#### 缓动函数令牌

```scss
// _tokens.scss - Animation Easing Tokens
$ease-linear: linear;
$ease-in: cubic-bezier(0.4, 0, 1, 1);
$ease-out: cubic-bezier(0, 0, 0.2, 1);
$ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
$ease-out-expo: cubic-bezier(0.16, 1, 0.3, 1);
$ease-in-out-cubic: cubic-bezier(0.65, 0, 0.35, 1);
$ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
$ease-bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55);

// Duration Tokens
$duration-instant: 50ms;
$duration-fast: 150ms;
$duration-normal: 300ms;
$duration-slow: 500ms;
$duration-slower: 700ms;
```

#### 关键帧动画

```scss
// _animations.scss - Keyframe Animations

// 淡入上移
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 淡入左移
@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 缩放进入
@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 脉冲发光
@keyframes pulseGlow {
  0%, 100% {
    box-shadow: 0 0 15px rgba(128, 32, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 25px rgba(128, 32, 255, 0.5);
  }
}

// 骨架屏闪烁
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

// 涟漪效果
@keyframes ripple {
  0% {
    transform: scale(0);
    opacity: 0.5;
  }
  100% {
    transform: scale(4);
    opacity: 0;
  }
}

// 弹跳进入
@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

// 滑入通知
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
```

#### 动画工具类

```scss
// Animation Utility Classes
.animate-fade-in-up {
  animation: fadeInUp $duration-normal $ease-out-expo forwards;
}

.animate-fade-in-left {
  animation: fadeInLeft $duration-normal $ease-out-expo forwards;
}

.animate-scale-in {
  animation: scaleIn $duration-normal $ease-spring forwards;
}

.animate-bounce-in {
  animation: bounceIn $duration-slow $ease-spring forwards;
}

// 交错动画延迟
@for $i from 1 through 10 {
  .stagger-#{$i} {
    animation-delay: #{$i * 50}ms;
  }
}

// Reduced Motion Support
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

### 2. 阴影系统 (Shadow System)

```scss
// _shadows.scss - Layered Shadow System

// Neon Glow Shadow Scale
$shadow-xs: 0 0 4px rgba(128, 32, 255, 0.1);
$shadow-sm: 0 0 8px rgba(128, 32, 255, 0.15);
$shadow-md: 0 0 15px rgba(128, 32, 255, 0.25);
$shadow-lg: 0 0 25px rgba(128, 32, 255, 0.35);
$shadow-xl: 0 0 40px rgba(128, 32, 255, 0.45);

// Elevated Shadows (for hover states)
$shadow-elevated-sm: 
  0 4px 12px rgba(0, 0, 0, 0.3),
  0 0 20px rgba(128, 32, 255, 0.2);
$shadow-elevated-md: 
  0 8px 24px rgba(0, 0, 0, 0.4),
  0 0 30px rgba(128, 32, 255, 0.3);
$shadow-elevated-lg: 
  0 12px 40px rgba(0, 0, 0, 0.5),
  0 0 40px rgba(128, 32, 255, 0.4);

// Inner Glow (for focus states)
$inner-glow-primary: inset 0 0 10px rgba(128, 32, 255, 0.3);
$inner-glow-success: inset 0 0 10px rgba(0, 255, 136, 0.3);
$inner-glow-danger: inset 0 0 10px rgba(255, 0, 85, 0.3);

// Shadow Utility Classes
.shadow-xs { box-shadow: $shadow-xs; }
.shadow-sm { box-shadow: $shadow-sm; }
.shadow-md { box-shadow: $shadow-md; }
.shadow-lg { box-shadow: $shadow-lg; }
.shadow-xl { box-shadow: $shadow-xl; }

.shadow-elevated {
  transition: box-shadow $duration-normal $ease-out;
  
  &:hover {
    box-shadow: $shadow-elevated-md;
  }
}
```

### 3. 响应式系统 (Responsive System)

```scss
// _responsive.scss - Enhanced Responsive System

// Breakpoint Tokens
$breakpoint-xs: 320px;
$breakpoint-sm: 480px;
$breakpoint-md: 768px;
$breakpoint-lg: 1024px;
$breakpoint-xl: 1280px;
$breakpoint-2xl: 1536px;

// Responsive Mixins
@mixin xs-only {
  @media (max-width: #{$breakpoint-sm - 1px}) { @content; }
}

@mixin sm-up {
  @media (min-width: $breakpoint-sm) { @content; }
}

@mixin sm-only {
  @media (min-width: $breakpoint-sm) and (max-width: #{$breakpoint-md - 1px}) { @content; }
}

@mixin md-up {
  @media (min-width: $breakpoint-md) { @content; }
}

@mixin md-only {
  @media (min-width: $breakpoint-md) and (max-width: #{$breakpoint-lg - 1px}) { @content; }
}

@mixin lg-up {
  @media (min-width: $breakpoint-lg) { @content; }
}

@mixin xl-up {
  @media (min-width: $breakpoint-xl) { @content; }
}

@mixin touch-device {
  @media (hover: none) and (pointer: coarse) { @content; }
}

// Fluid Typography
@mixin fluid-type($min-size, $max-size, $min-vw: $breakpoint-sm, $max-vw: $breakpoint-xl) {
  font-size: clamp(#{$min-size}, calc(#{$min-size} + (#{strip-unit($max-size)} - #{strip-unit($min-size)}) * ((100vw - #{$min-vw}) / (#{strip-unit($max-vw)} - #{strip-unit($min-vw)}))), #{$max-size});
}

// Touch Target Mixin
@mixin touch-target($size: 44px) {
  min-width: $size;
  min-height: $size;
  
  @include touch-device {
    min-width: $size;
    min-height: $size;
  }
}

// Container Queries Support
.container-query {
  container-type: inline-size;
}

@mixin container-sm {
  @container (min-width: 400px) { @content; }
}

@mixin container-md {
  @container (min-width: 600px) { @content; }
}
```

### 4. 统一组件样式 (Unified Component Styles)

#### 卡片组件

```scss
// _components.scss - Unified Card Styles

.card-unified {
  // Base styles
  background: rgba(15, 15, 35, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.2);
  padding: 24px;
  transition: all $duration-normal $ease-out;
  
  // Variants
  &--sm { padding: 16px; border-radius: 16px; }
  &--lg { padding: 28px; border-radius: 24px; }
  
  // Interactive
  &--interactive {
    cursor: pointer;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: $shadow-elevated-md;
      border-color: rgba(99, 102, 241, 0.4);
    }
    
    &:active {
      transform: translateY(-2px);
    }
  }
  
  // Color accents
  &--primary::before { background: linear-gradient(90deg, #6366f1, #8b5cf6); }
  &--success::before { background: linear-gradient(90deg, #10b981, #34d399); }
  &--warning::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
  &--danger::before { background: linear-gradient(90deg, #ef4444, #f87171); }
  
  &[class*="--primary"],
  &[class*="--success"],
  &[class*="--warning"],
  &[class*="--danger"] {
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      border-radius: 20px 20px 0 0;
    }
    position: relative;
    overflow: hidden;
  }
}
```

#### 按钮组件

```scss
// Unified Button Styles
.btn-unified {
  // Base
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: all $duration-fast $ease-out;
  position: relative;
  overflow: hidden;
  
  @include touch-target;
  
  // Ripple effect container
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
    transform: scale(0);
    opacity: 0;
  }
  
  &:active::after {
    animation: ripple $duration-slow $ease-out;
  }
  
  // Variants
  &--primary {
    background: linear-gradient(135deg, #8020ff, #6366f1);
    color: white;
    box-shadow: 0 0 20px rgba(128, 32, 255, 0.3);
    
    &:hover {
      box-shadow: 0 0 30px rgba(128, 32, 255, 0.5);
      transform: translateY(-2px);
    }
  }
  
  &--secondary {
    background: rgba(99, 102, 241, 0.15);
    color: #e2e8f0;
    border: 1px solid rgba(99, 102, 241, 0.3);
    
    &:hover {
      background: rgba(99, 102, 241, 0.25);
      border-color: rgba(99, 102, 241, 0.5);
    }
  }
  
  &--ghost {
    background: transparent;
    color: #94a3b8;
    
    &:hover {
      background: rgba(99, 102, 241, 0.1);
      color: #e2e8f0;
    }
  }
  
  &--danger {
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: white;
    
    &:hover {
      box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
    }
  }
  
  // Sizes
  &--sm { padding: 8px 16px; font-size: 12px; }
  &--lg { padding: 16px 32px; font-size: 16px; }
  
  // States
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none !important;
  }
}
```

#### 表单输入组件

```scss
// Unified Form Input Styles
.input-unified {
  width: 100%;
  padding: 12px 16px;
  background: rgba(15, 15, 35, 0.8);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 12px;
  color: #f8fafc;
  font-size: 16px;
  transition: all $duration-fast $ease-out;
  
  &::placeholder {
    color: #64748b;
  }
  
  &:focus {
    outline: none;
    border-color: #8020ff;
    box-shadow: 
      0 0 0 3px rgba(128, 32, 255, 0.2),
      $inner-glow-primary;
  }
  
  &:hover:not(:focus) {
    border-color: rgba(99, 102, 241, 0.4);
  }
  
  // Error state
  &--error {
    border-color: #ef4444;
    
    &:focus {
      box-shadow: 
        0 0 0 3px rgba(239, 68, 68, 0.2),
        $inner-glow-danger;
    }
  }
  
  // Success state
  &--success {
    border-color: #10b981;
    
    &:focus {
      box-shadow: 
        0 0 0 3px rgba(16, 185, 129, 0.2),
        $inner-glow-success;
    }
  }
}

// Floating Label
.input-group-unified {
  position: relative;
  
  .input-label {
    position: absolute;
    left: 16px;
    top: 50%;
    transform: translateY(-50%);
    color: #64748b;
    font-size: 16px;
    pointer-events: none;
    transition: all $duration-fast $ease-out;
  }
  
  .input-unified:focus ~ .input-label,
  .input-unified:not(:placeholder-shown) ~ .input-label {
    top: 0;
    transform: translateY(-50%) scale(0.85);
    background: rgba(15, 15, 35, 1);
    padding: 0 8px;
    color: #8020ff;
  }
}
```

### 5. 深色主题系统 (Theme System)

```scss
// _theme.scss - Enhanced Dark Theme System

// Semantic Color Tokens
:root {
  // Surface colors
  --color-surface-0: #0a0a14;
  --color-surface-1: #121225;
  --color-surface-2: rgba(15, 15, 35, 0.85);
  --color-surface-3: rgba(15, 15, 35, 0.95);
  --color-surface-elevated: rgba(25, 25, 50, 0.95);
  
  // Text colors with WCAG AA compliance
  --color-text-primary: #ffffff;      // Contrast: 21:1
  --color-text-secondary: #94a3b8;    // Contrast: 7:1
  --color-text-tertiary: #64748b;     // Contrast: 4.5:1
  --color-text-disabled: #475569;     // Contrast: 3:1
  
  // Border colors
  --color-border-default: rgba(99, 102, 241, 0.2);
  --color-border-hover: rgba(99, 102, 241, 0.4);
  --color-border-focus: #8020ff;
  
  // Status colors (optimized for dark backgrounds)
  --color-success: #10b981;
  --color-success-bg: rgba(16, 185, 129, 0.15);
  --color-warning: #f59e0b;
  --color-warning-bg: rgba(245, 158, 11, 0.15);
  --color-danger: #ef4444;
  --color-danger-bg: rgba(239, 68, 68, 0.15);
  --color-info: #6366f1;
  --color-info-bg: rgba(99, 102, 241, 0.15);
  
  // Interactive states
  --color-interactive-default: rgba(99, 102, 241, 0.15);
  --color-interactive-hover: rgba(99, 102, 241, 0.25);
  --color-interactive-active: rgba(99, 102, 241, 0.35);
  
  // Glassmorphism
  --glass-bg: rgba(15, 15, 35, 0.85);
  --glass-blur: 20px;
  --glass-border: rgba(99, 102, 241, 0.2);
}

// Theme transition
.theme-transition {
  transition: 
    background-color $duration-normal $ease-out,
    color $duration-normal $ease-out,
    border-color $duration-normal $ease-out,
    box-shadow $duration-normal $ease-out;
}

// Element Plus Dark Theme Overrides
.el-button {
  --el-button-bg-color: var(--color-interactive-default);
  --el-button-border-color: var(--color-border-default);
  --el-button-text-color: var(--color-text-primary);
  --el-button-hover-bg-color: var(--color-interactive-hover);
  --el-button-hover-border-color: var(--color-border-hover);
}

.el-input {
  --el-input-bg-color: var(--color-surface-2);
  --el-input-border-color: var(--color-border-default);
  --el-input-text-color: var(--color-text-primary);
  --el-input-placeholder-color: var(--color-text-tertiary);
  --el-input-focus-border-color: var(--color-border-focus);
}

.el-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--color-surface-2);
  --el-table-row-hover-bg-color: var(--color-interactive-default);
  --el-table-border-color: var(--color-border-default);
  --el-table-text-color: var(--color-text-secondary);
  --el-table-header-text-color: var(--color-text-primary);
}

.el-dialog {
  --el-dialog-bg-color: var(--color-surface-3);
  --el-dialog-border-radius: 20px;
}

.el-card {
  --el-card-bg-color: var(--color-surface-2);
  --el-card-border-color: var(--color-border-default);
}
```

### 6. 性能优化 (Performance Optimization)

```scss
// _performance.scss - CSS Performance Optimizations

// CSS Containment for complex components
.contain-layout {
  contain: layout;
}

.contain-paint {
  contain: paint;
}

.contain-strict {
  contain: strict;
}

.contain-content {
  contain: content;
}

// Will-change hints (use sparingly)
.will-animate {
  will-change: transform, opacity;
}

// GPU acceleration
.gpu-accelerated {
  transform: translateZ(0);
  backface-visibility: hidden;
}

// Efficient selectors - avoid deep nesting
// Use BEM naming convention

// Critical CSS marker
.critical {
  // Styles for above-the-fold content
}

// Lazy-loaded styles marker
.lazy-styles {
  // Styles that can be loaded later
}

// CSS Layers for specificity management
@layer base, components, utilities, overrides;

@layer base {
  // Reset and base styles
}

@layer components {
  // Component styles
}

@layer utilities {
  // Utility classes
}

@layer overrides {
  // Third-party overrides (Element Plus)
}
```

### 7. 微交互系统 (Micro-Interactions)

```scss
// _micro-interactions.scss

// Skeleton Loading
.skeleton {
  background: linear-gradient(
    90deg,
    var(--color-surface-2) 25%,
    rgba(99, 102, 241, 0.1) 50%,
    var(--color-surface-2) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
  
  &--text {
    height: 1em;
    margin-bottom: 0.5em;
  }
  
  &--avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
  }
  
  &--card {
    height: 200px;
  }
}

// Success Checkmark Animation
.success-checkmark {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-success);
  position: relative;
  animation: scaleIn $duration-normal $ease-spring;
  
  &::after {
    content: '';
    position: absolute;
    left: 18px;
    top: 28px;
    width: 12px;
    height: 24px;
    border: solid white;
    border-width: 0 4px 4px 0;
    transform: rotate(45deg);
    animation: checkmark $duration-fast $ease-out $duration-fast forwards;
    opacity: 0;
  }
}

@keyframes checkmark {
  from {
    opacity: 0;
    transform: rotate(45deg) scale(0);
  }
  to {
    opacity: 1;
    transform: rotate(45deg) scale(1);
  }
}

// Toggle Switch Animation
.toggle-switch {
  width: 48px;
  height: 28px;
  background: var(--color-surface-2);
  border-radius: 14px;
  position: relative;
  cursor: pointer;
  transition: background $duration-fast $ease-out;
  
  &::after {
    content: '';
    position: absolute;
    left: 4px;
    top: 4px;
    width: 20px;
    height: 20px;
    background: white;
    border-radius: 50%;
    transition: transform $duration-fast $ease-spring;
  }
  
  &.active {
    background: var(--color-primary);
    
    &::after {
      transform: translateX(20px);
    }
  }
}

// Notification Slide-in
.notification-enter {
  animation: slideInRight $duration-normal $ease-spring;
}

.notification-leave {
  animation: slideInRight $duration-fast $ease-in reverse;
}

// Copy Tooltip
.copy-tooltip {
  position: absolute;
  background: var(--color-success);
  color: white;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  animation: fadeInUp $duration-fast $ease-out, fadeOut $duration-fast $ease-in 1s forwards;
}

@keyframes fadeOut {
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

// Parallax Container
.parallax-container {
  overflow: hidden;
  
  .parallax-bg {
    transform: translateY(var(--parallax-offset, 0));
    transition: transform 0.1s linear;
  }
}

// Focus Ring Animation
.focus-ring {
  &:focus-visible {
    outline: none;
    box-shadow: 
      0 0 0 2px var(--color-surface-0),
      0 0 0 4px var(--color-primary);
    animation: focusPulse 1.5s infinite;
  }
}

@keyframes focusPulse {
  0%, 100% {
    box-shadow: 
      0 0 0 2px var(--color-surface-0),
      0 0 0 4px var(--color-primary);
  }
  50% {
    box-shadow: 
      0 0 0 2px var(--color-surface-0),
      0 0 0 6px rgba(128, 32, 255, 0.5);
  }
}
```

## Data Models

### 设计令牌数据结构

```typescript
// Design Token Types
interface DesignTokens {
  colors: {
    primary: string;
    secondary: string;
    accent: string;
    success: string;
    warning: string;
    danger: string;
    surface: Record<string, string>;
    text: Record<string, string>;
    border: Record<string, string>;
  };
  
  spacing: Record<number, string>;
  
  typography: {
    fontFamily: Record<string, string>;
    fontSize: Record<string, string>;
    fontWeight: Record<string, number>;
    lineHeight: Record<string, number>;
    letterSpacing: Record<string, string>;
  };
  
  shadows: Record<string, string>;
  
  animation: {
    duration: Record<string, string>;
    easing: Record<string, string>;
  };
  
  breakpoints: Record<string, string>;
  
  borderRadius: Record<string, string>;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: GPU-Accelerated Animation Properties

*For any* CSS animation or transition in the codebase, the animated properties SHALL only include transform and opacity (compositor-friendly properties), never properties that trigger layout or paint (width, height, top, left, margin, padding, background-color).

**Validates: Requirements 1.7, 8.3**

### Property 2: Responsive Breakpoint Completeness

*For any* responsive layout system, exactly 6 breakpoints SHALL be defined with the values: xs (320px), sm (480px), md (768px), lg (1024px), xl (1280px), 2xl (1536px).

**Validates: Requirements 3.1**

### Property 3: Touch Target Minimum Size

*For any* interactive element (buttons, links, form controls), the minimum touch target size SHALL be 44x44 pixels on touch devices.

**Validates: Requirements 4.1**

### Property 4: Card Component Consistency

*For any* card component in the unified component library, the padding SHALL be within 20-28px range, and border-radius SHALL be within 16-24px range.

**Validates: Requirements 5.1**

### Property 5: Text Contrast Ratio Compliance

*For any* text color and background color combination in the theme system, the contrast ratio SHALL meet WCAG AA standards (4.5:1 for normal text, 3:1 for large text ≥18px or bold ≥14px).

**Validates: Requirements 6.1**

### Property 6: Status Color Distinguishability

*For any* status color (success, warning, danger) displayed on dark backgrounds, the color SHALL have a minimum contrast ratio of 3:1 against the background and be visually distinguishable from other status colors.

**Validates: Requirements 6.5**

### Property 7: CSS Best Practices Compliance

*For any* SCSS/CSS file in the style system:
- No hardcoded light-theme colors (e.g., #ffffff, #f5f5f5 for backgrounds)
- CSS custom properties used for all theme-dependent colors
- No deep selector nesting (max 3 levels)
- Minimal use of !important (only for third-party overrides)

**Validates: Requirements 7.1, 7.2, 8.5**

## Error Handling

### Animation Fallbacks

```scss
// Fallback for browsers without animation support
@supports not (animation: fadeInUp 0.3s) {
  .animate-fade-in-up {
    opacity: 1;
    transform: none;
  }
}

// Fallback for browsers without backdrop-filter
@supports not (backdrop-filter: blur(20px)) {
  .glass-effect {
    background: rgba(15, 15, 35, 0.95);
  }
}
```

### Reduced Motion Handling

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

### Touch Device Detection

```scss
// Disable hover effects on touch devices
@media (hover: none) and (pointer: coarse) {
  .hover-effect:hover {
    transform: none;
    box-shadow: inherit;
  }
}
```

## Testing Strategy

### Unit Tests

Unit tests will verify specific CSS output and class existence:

1. Verify animation keyframes are defined correctly
2. Verify breakpoint variables have correct values
3. Verify color contrast calculations
4. Verify component class structure

### Property-Based Tests

Property-based tests will use a CSS parser to verify universal properties:

1. **Animation Property Test**: Parse all transition/animation declarations and verify only transform/opacity are animated
2. **Breakpoint Test**: Parse breakpoint variables and verify all 6 are defined with correct values
3. **Touch Target Test**: Parse interactive element styles and verify min-width/min-height >= 44px
4. **Card Consistency Test**: Parse card component styles and verify padding/border-radius ranges
5. **Contrast Ratio Test**: Calculate contrast ratios for all text/background combinations
6. **CSS Best Practices Test**: Analyze SCSS files for hardcoded colors, nesting depth, and !important usage

### Testing Framework

- Use **Vitest** for JavaScript/TypeScript tests
- Use **sass** package to compile and parse SCSS
- Use **postcss** with **postcss-scss** for CSS analysis
- Use **color-contrast-checker** for WCAG compliance verification

### Test Configuration

```javascript
// vitest.config.js
export default {
  test: {
    include: ['**/*.{test,spec}.{js,ts}'],
    coverage: {
      reporter: ['text', 'json', 'html'],
    },
  },
}
```

Each property test should run minimum 100 iterations where applicable (e.g., testing multiple color combinations).
