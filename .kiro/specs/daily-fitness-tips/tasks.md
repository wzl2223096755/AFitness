# Implementation Tasks

## Task 1: 创建小贴士数据文件

**Requirements:** REQ-3 (小贴士数据管理)

**Acceptance Criteria:**
- [ ] 创建 `Fitness/frontend/src/data/fitnessTips.js`
- [ ] 定义 30+ 条健身小贴士，覆盖所有 6 个分类
- [ ] 每条小贴士包含 id, title, content, category
- [ ] 标题不超过 30 字符，内容不超过 200 字符
- [ ] 导出分类配置 `CATEGORY_CONFIG`

---

## Task 2: 创建小贴士服务

**Requirements:** REQ-3, REQ-4

**Acceptance Criteria:**
- [ ] 创建 `Fitness/frontend/src/services/tipService.js`
- [ ] 实现 `getTodayTip()` - 基于日期确定性选择
- [ ] 实现 `getRandomTip(excludeId)` - 获取随机小贴士
- [ ] 实现 `toggleFavorite(tipId)` - 切换收藏状态
- [ ] 实现 `isFavorited(tipId)` - 检查收藏状态
- [ ] 使用 localStorage 持久化收藏数据

---

## Task 3: 创建 DailyTipCard 组件

**Requirements:** REQ-1, REQ-2, REQ-5

**Acceptance Criteria:**
- [ ] 创建 `Fitness/frontend/src/components/DailyTipCard.vue`
- [ ] 显示标题、内容、分类图标和徽章
- [ ] 使用统一卡片样式 (`card-unified`, `glass-card`)
- [ ] 实现加载骨架屏动画
- [ ] 实现入场动画 (`animate-fade-in-up`)
- [ ] 响应式布局适配移动端

---

## Task 4: 实现用户交互功能

**Requirements:** REQ-4

**Acceptance Criteria:**
- [ ] 实现刷新按钮 - 显示随机不同的小贴士
- [ ] 实现收藏按钮 - 切换收藏状态
- [ ] 收藏状态视觉反馈（图标变化）
- [ ] 按钮最小触摸区域 44px

---

## Task 5: 集成到 Dashboard

**Requirements:** REQ-1

**Acceptance Criteria:**
- [ ] 在 Dashboard.vue 导入 DailyTipCard 组件
- [ ] 在欢迎区域后添加小贴士卡片
- [ ] 确保布局协调，间距正确

---

## Task 6: 验证与测试

**Requirements:** All

**Acceptance Criteria:**
- [ ] 验证日期确定性（同一天显示相同小贴士）
- [ ] 验证刷新功能正常
- [ ] 验证收藏功能持久化
- [ ] 验证响应式布局（桌面/平板/手机）
- [ ] 验证深色主题兼容性
