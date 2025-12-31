<template>
  <div class="dynamic-dashboard p-5">
    <!-- 粒子背景 -->
    <ParticleBackground />
    
    <div class="dashboard-header mb-6 p-6">
      <h1 class="dashboard-title responsive-h1 font-extrabold">
        <span class="title-gradient">健身数据仪表盘</span>
      </h1>
      <div class="dashboard-controls gap-4">
        <el-select v-model="currentLayout" @change="onLayoutChange" placeholder="选择布局" class="modern-select">
          <el-option
            v-for="layout in availableLayouts"
            :key="layout.id"
            :label="layout.name"
            :value="layout.id"
          >
            <div class="layout-option">
              <span>{{ layout.name }}</span>
              <span class="layout-description">{{ layout.description }}</span>
            </div>
          </el-option>
        </el-select>
        <el-button @click="toggleEditMode" :type="editMode ? 'primary' : 'default'" class="modern-button">
          <el-icon><Edit /></el-icon>
          {{ editMode ? '完成编辑' : '编辑布局' }}
        </el-button>
        <el-button @click="resetLayout" type="warning" class="modern-button">
          <el-icon><Refresh /></el-icon>
          重置布局
        </el-button>
      </div>
    </div>

    <DynamicLayout
      :layout="currentLayoutData"
      :edit-mode="editMode"
      @layout-change="onLayoutChange"
      @item-resize="onItemResize"
      @item-move="onItemMove"
      @item-add="onItemAdd"
      @item-remove="onItemRemove"
    >
      <template #welcome="{ item }">
        <WelcomeSection :data="item.data" />
      </template>
      
      <template #metrics="{ item }">
        <MetricsOverview :data="item.data" />
      </template>
      
      <template #analytics="{ item }">
        <AnalyticsSection :data="item.data" />
      </template>
      
      <template #activity="{ item }">
        <RecentActivity :data="item.data" />
      </template>
    </DynamicLayout>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Refresh } from '@element-plus/icons-vue'
import DynamicLayout from './DynamicLayout.vue'
import WelcomeSection from './WelcomeSection.vue'
import MetricsOverview from './MetricsOverview.vue'
import AnalyticsSection from './AnalyticsSection.vue'
import RecentActivity from './RecentActivity.vue'
import ParticleBackground from './ParticleBackground.vue'
import { useLayoutStore } from '@/stores/layout'
import { PerformanceUtils } from '@/utils/performance'

// 响应式数据
const layoutStore = useLayoutStore()
const editMode = ref(false)
const currentLayout = ref('default')

// 性能优化：防抖布局变化
const debouncedLayoutChange = PerformanceUtils.debounce((layoutId) => {
  if (layoutId !== currentLayout.value) {
    currentLayout.value = layoutId
    layoutStore.applyLayout(layoutId)
    ElMessage.success(`已切换到 ${layoutStore.getLayoutName(layoutId)} 布局`)
  }
}, 300)

// 计算属性
const currentLayoutData = computed(() => {
  return layoutStore.currentLayout
})

const availableLayouts = computed(() => {
  return layoutStore.availableLayouts
})

// 方法
const onLayoutChange = debouncedLayoutChange

const onItemResize = PerformanceUtils.throttle((itemId, newSize) => {
  layoutStore.updateItem(itemId, { size: newSize })
}, 100)

const onItemMove = PerformanceUtils.throttle((itemId, newPosition) => {
  layoutStore.updateItem(itemId, { position: newPosition })
}, 50)

const onItemAdd = (itemType, position) => {
  layoutStore.addItem(itemType, position)
  ElMessage.success(`已添加 ${itemType} 组件`)
}

const onItemRemove = (itemId) => {
  layoutStore.removeItem(itemId)
  ElMessage.success('已移除组件')
}

const toggleEditMode = () => {
  editMode.value = !editMode.value
  if (editMode.value) {
    ElMessage.info('已进入布局编辑模式，您可以拖拽调整组件位置和大小')
  }
}

const resetLayout = () => {
  layoutStore.resetLayout()
  currentLayout.value = 'default'
  ElMessage.success('布局已重置为默认状态')
}

// 监听布局变化
watch(currentLayoutData, (newLayout) => {
  if (newLayout && newLayout.id !== currentLayout.value) {
    currentLayout.value = newLayout.id
  }
}, { deep: true })

// 生命周期
onMounted(() => {
  // 初始化布局
  layoutStore.initializeLayout()
  
  // 设置初始布局
  if (layoutStore.currentLayout) {
    currentLayout.value = layoutStore.currentLayout.id
  }
  
  // 启动性能监控
  PerformanceUtils.monitorPerformance()
  
  // 清理过期缓存
  PerformanceUtils.clearExpiredCache()
})

onUnmounted(() => {
  // 清理缓存
  PerformanceUtils.cache.clear()
})
</script>

<style scoped>
/* DynamicDashboard - Using design tokens from typography and layout system */
/* Requirements: 3.2, 6.1, 6.2 */

.dynamic-dashboard {
  width: 100%;
  min-height: 100vh;
  position: relative;
}

.dashboard-header {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dashboard-header:hover {
  transform: translateY(-2px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

/* Using responsive typography from _typography.scss */
.dashboard-title {
  margin: 0;
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient-shift 3s ease-in-out infinite;
}

.title-gradient {
  position: relative;
}

.title-gradient::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  border-radius: 2px;
  animation: slide-in 0.6s ease-out;
}

.dashboard-controls {
  display: flex;
  align-items: center;
}

.modern-select {
  border-radius: 12px;
  border: 2px solid rgba(103, 126, 234, 0.2);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.modern-select:hover {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modern-button {
  border-radius: 12px;
  padding: var(--spacing-3, 0.75rem) var(--spacing-6, 1.5rem);
  font-weight: var(--font-weight-semibold, 600);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  position: relative;
  overflow: hidden;
}

.modern-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.modern-button:hover::before {
  left: 100%;
}

.modern-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.layout-option {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1, 0.25rem);
}

.layout-description {
  font-size: var(--font-size-xs, 0.8rem);
  color: var(--text-secondary, #64748b);
  font-weight: var(--font-weight-normal, 400);
}

/* Animations */
@keyframes gradient-shift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes slide-in {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 100%;
    opacity: 1;
  }
}

/* Responsive design using design token breakpoints */
@media (max-width: 1200px) {
  .dashboard-header {
    flex-direction: column;
    gap: var(--spacing-5, 1.25rem);
    align-items: stretch;
    padding: var(--spacing-5, 1.25rem) var(--spacing-6, 1.5rem);
  }
  
  .dashboard-title {
    text-align: center;
  }
  
  .dashboard-controls {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .dynamic-dashboard {
    padding: var(--spacing-3, 0.75rem);
  }
  
  .dashboard-header {
    padding: var(--spacing-4, 1rem) var(--spacing-5, 1.25rem);
    border-radius: 16px;
  }
  
  .dashboard-controls {
    gap: var(--spacing-3, 0.75rem);
  }
  
  .modern-button {
    padding: var(--spacing-2, 0.5rem) var(--spacing-5, 1.25rem);
    font-size: var(--font-size-sm, 0.875rem);
  }
}

@media (max-width: 480px) {
  .dashboard-controls {
    flex-direction: column;
    width: 100%;
  }
  
  .modern-select,
  .modern-button {
    width: 100%;
  }
}

/* Dark theme adaptation */
[data-theme="dark"] .dashboard-header {
  background: rgba(30, 30, 30, 0.95);
  border-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .modern-select {
  background: rgba(30, 30, 30, 0.9);
  border-color: rgba(102, 126, 234, 0.3);
  color: #ffffff;
}

[data-theme="dark"] .layout-description {
  color: #a0a0a0;
}
</style>