<template>
  <div class="dynamic-layout-container">
    <!-- 布局控制栏 -->
    <div class="layout-controls">
      <div class="control-group">
        <label class="control-label">布局模式：</label>
        <el-select v-model="layoutMode" @change="applyLayoutMode" size="small">
          <el-option label="网格布局" value="grid" />
          <el-option label="列表布局" value="list" />
          <el-option label="卡片布局" value="cards" />
          <el-option label="仪表盘布局" value="dashboard" />
          <el-option label="自定义布局" value="custom" />
        </el-select>
      </div>
      
      <div class="control-group">
        <label class="control-label">列数：</label>
        <el-input-number 
          v-model="gridColumns" 
          @change="updateGridLayout" 
          :min="1" 
          :max="6" 
          size="small"
        />
      </div>
      
      <div class="control-group">
        <el-button @click="resetLayout" size="small" type="warning">
          <el-icon><Refresh /></el-icon>
          重置布局
        </el-button>
        <el-button @click="saveLayout" size="small" type="primary">
          <el-icon><Document /></el-icon>
          保存布局
        </el-button>
      </div>
    </div>

    <!-- 动态布局区域 -->
    <div 
      ref="layoutContainer"
      class="dynamic-layout"
      :class="layoutMode"
      :style="layoutStyles"
    >
      <div 
        v-for="item in layoutItems" 
        :key="item.id"
        class="layout-item"
        :class="{
          'dragging': item.isDragging,
          'resizing': item.isResizing,
          'fullscreen': item.isFullscreen
        }"
        :style="getItemStyles(item)"
        @mousedown="startDrag($event, item)"
      >
        <!-- 组件内容 -->
        <div class="item-content">
          <component 
            :is="item.component" 
            v-bind="item.props"
            :item-id="item.id"
            @update="handleItemUpdate"
          />
        </div>
        
        <!-- 拖拽手柄 -->
        <div class="drag-handle" v-if="layoutMode === 'custom'">
          <el-icon><Rank /></el-icon>
        </div>
        
        <!-- 调整大小手柄 -->
        <div 
          class="resize-handle" 
          v-if="layoutMode === 'custom'"
          @mousedown.stop="startResize($event, item)"
        >
          <el-icon><FullScreen /></el-icon>
        </div>
        
        <!-- 全屏按钮 -->
        <div class="fullscreen-handle" @click="toggleFullscreen(item)">
          <el-icon>
            <FullScreen v-if="!item.isFullscreen" />
            <Aim v-else />
          </el-icon>
        </div>
        
        <!-- 关闭按钮 -->
        <div class="close-handle" @click="removeItem(item)">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </div>

    <!-- 添加新组件面板 -->
    <div class="add-component-panel">
      <el-dropdown @command="addComponent">
        <el-button type="primary" size="small">
          <el-icon><Plus /></el-icon>
          添加组件
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="metrics">指标卡片</el-dropdown-item>
            <el-dropdown-item command="chart">图表组件</el-dropdown-item>
            <el-dropdown-item command="activity">活动列表</el-dropdown-item>
            <el-dropdown-item command="welcome">欢迎区域</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useLayoutStore } from '@/stores/layout'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Document, Rank, FullScreen, Aim, Close, Plus 
} from '@element-plus/icons-vue'
import WelcomeSection from './WelcomeSection.vue'
import MetricsOverview from './MetricsOverview.vue'
import AnalyticsSection from './AnalyticsSection.vue'
import RecentActivity from './RecentActivity.vue'

// 布局状态管理
const layoutStore = useLayoutStore()

// 辅助函数：将布局类型映射到组件名称
const getComponentName = (type) => {
  const componentMap = {
    'welcome': 'WelcomeSection',
    'metrics': 'MetricsOverview',
    'analytics': 'AnalyticsSection',
    'activity': 'RecentActivity'
  }
  return componentMap[type] || 'div'
}

// 辅助函数：将组件名称映射回布局类型
const getComponentType = (componentName) => {
  const typeMap = {
    'WelcomeSection': 'welcome',
    'MetricsOverview': 'metrics',
    'AnalyticsSection': 'analytics',
    'RecentActivity': 'activity'
  }
  return typeMap[componentName] || 'metrics'
}

// 响应式数据
const layoutMode = ref('grid')
const gridColumns = ref(2)
const layoutContainer = ref(null)
const isDragging = ref(false)
const isResizing = ref(false)
const currentItem = ref(null)
const dragOffset = ref({ x: 0, y: 0 })
const resizeStart = ref({ width: 0, height: 0, x: 0, y: 0 })

// 布局项目
const layoutItems = ref([
  {
    id: 'welcome',
    component: 'WelcomeSection',
    props: {},
    position: { x: 0, y: 0 },
    size: { width: 100, height: 200 },
    isDragging: false,
    isResizing: false,
    isFullscreen: false
  },
  {
    id: 'metrics',
    component: 'MetricsOverview',
    props: {},
    position: { x: 0, y: 210 },
    size: { width: 100, height: 300 },
    isDragging: false,
    isResizing: false,
    isFullscreen: false
  },
  {
    id: 'analytics',
    component: 'AnalyticsSection',
    props: {},
    position: { x: 0, y: 520 },
    size: { width: 100, height: 400 },
    isDragging: false,
    isResizing: false,
    isFullscreen: false
  },
  {
    id: 'activity',
    component: 'RecentActivity',
    props: {},
    position: { x: 0, y: 930 },
    size: { width: 100, height: 350 },
    isDragging: false,
    isResizing: false,
    isFullscreen: false
  }
])

// 计算属性
const layoutStyles = computed(() => {
  switch (layoutMode.value) {
    case 'grid':
      return {
        display: 'grid',
        gridTemplateColumns: `repeat(${gridColumns.value}, 1fr)`,
        gap: '20px',
        padding: '20px'
      }
    case 'list':
      return {
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        padding: '20px'
      }
    case 'cards':
      return {
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '20px',
        padding: '20px'
      }
    case 'dashboard':
      return {
        display: 'grid',
        gridTemplateColumns: '2fr 1fr',
        gridTemplateRows: 'auto auto auto',
        gap: '20px',
        padding: '20px'
      }
    case 'custom':
      return {
        position: 'relative',
        minHeight: '1000px',
        padding: '20px'
      }
    default:
      return {}
  }
})

// 方法
const applyLayoutMode = () => {
  // 根据布局模式调整项目样式
  if (layoutMode.value === 'dashboard') {
    // 仪表盘布局的特殊处理
    layoutItems.value.forEach((item, index) => {
      if (index === 0) {
        item.gridArea = '1 / 1 / 2 / 3'
      } else if (index === 1) {
        item.gridArea = '2 / 1 / 3 / 2'
      } else if (index === 2) {
        item.gridArea = '2 / 2 / 4 / 3'
      } else if (index === 3) {
        item.gridArea = '3 / 1 / 4 / 2'
      }
    })
  }
}

const updateGridLayout = () => {
  // 更新网格布局
  applyLayoutMode()
}

const getItemStyles = (item) => {
  if (layoutMode.value === 'custom') {
    return {
      position: 'absolute',
      left: `${item.position.x}px`,
      top: `${item.position.y}px`,
      width: `${item.size.width}px`,
      height: `${item.size.height}px`,
      zIndex: item.isDragging ? 1000 : 1,
      transition: item.isDragging || item.isResizing ? 'none' : 'all 0.3s ease'
    }
  } else if (layoutMode.value === 'dashboard' && item.gridArea) {
    return {
      gridArea: item.gridArea
    }
  }
  return {}
}

const startDrag = (event, item) => {
  if (layoutMode.value !== 'custom') return
  
  event.preventDefault()
  isDragging.value = true
  currentItem.value = item
  item.isDragging = true
  
  const rect = event.target.getBoundingClientRect()
  dragOffset.value = {
    x: event.clientX - item.position.x,
    y: event.clientY - item.position.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (event) => {
  if (!isDragging.value || !currentItem.value) return
  
  const containerRect = layoutContainer.value.getBoundingClientRect()
  const newX = Math.max(0, Math.min(event.clientX - dragOffset.value.x, containerRect.width - currentItem.value.size.width))
  const newY = Math.max(0, Math.min(event.clientY - dragOffset.value.y, containerRect.height - currentItem.value.size.height))
  
  currentItem.value.position.x = newX
  currentItem.value.position.y = newY
}

const stopDrag = () => {
  isDragging.value = false
  if (currentItem.value) {
    currentItem.value.isDragging = false
  }
  currentItem.value = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const startResize = (event, item) => {
  if (layoutMode.value !== 'custom') return
  
  event.preventDefault()
  isResizing.value = true
  currentItem.value = item
  item.isResizing = true
  
  resizeStart.value = {
    width: item.size.width,
    height: item.size.height,
    x: event.clientX,
    y: event.clientY
  }
  
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (event) => {
  if (!isResizing.value || !currentItem.value) return
  
  const deltaX = event.clientX - resizeStart.value.x
  const deltaY = event.clientY - resizeStart.value.y
  
  currentItem.value.size.width = Math.max(200, resizeStart.value.width + deltaX)
  currentItem.value.size.height = Math.max(150, resizeStart.value.height + deltaY)
}

const stopResize = () => {
  isResizing.value = false
  if (currentItem.value) {
    currentItem.value.isResizing = false
  }
  currentItem.value = null
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

const toggleFullscreen = (item) => {
  item.isFullscreen = !item.isFullscreen
}

const removeItem = (item) => {
  const index = layoutItems.value.findIndex(i => i.id === item.id)
  if (index > -1) {
    layoutItems.value.splice(index, 1)
    ElMessage.success('组件已移除')
  }
}

const addComponent = (type) => {
  const newComponent = {
    id: `${type}-${Date.now()}`,
    component: getComponentName(type),
    props: {},
    position: { x: 50, y: 50 },
    size: { width: 300, height: 200 },
    isDragging: false,
    isResizing: false,
    isFullscreen: false
  }
  
  layoutItems.value.push(newComponent)
  ElMessage.success('组件已添加')
}

const resetLayout = () => {
  layoutMode.value = 'grid'
  gridColumns.value = 2
  // 重置所有项目位置和大小
  layoutItems.value.forEach((item, index) => {
    item.position = { x: 0, y: index * 220 }
    item.size = { width: 100, height: 200 }
    item.isFullscreen = false
  })
  ElMessage.success('布局已重置')
}

const saveLayout = () => {
  const layoutConfig = {
    mode: layoutMode.value,
    gridColumns: gridColumns.value,
    items: layoutItems.value.map(item => ({
      id: item.id,
      component: item.component,
      position: item.position,
      size: item.size
    }))
  }
  
  // 使用正确的保存方法
  const layoutId = `custom_${Date.now()}`
  layoutStore.saveLayout(layoutId, {
    id: layoutId,
    name: '自定义布局',
    description: '用户自定义的布局配置',
    type: layoutMode.value,
    config: {
      columns: gridColumns.value,
      gap: 16,
      padding: 24
    },
    items: layoutItems.value.map(item => ({
      id: item.id,
      type: getComponentType(item.component),
      position: item.position,
      size: item.size,
      locked: false,
      data: item.props || {}
    }))
  })
  
  ElMessage.success('布局已保存')
}

const handleItemUpdate = (itemId, updates) => {
  const item = layoutItems.value.find(i => i.id === itemId)
  if (item) {
    Object.assign(item, updates)
  }
}

// 生命周期
onMounted(() => {
  // 初始化布局
  layoutStore.initializeLayout()
  
  // 使用当前布局配置
  const currentLayout = layoutStore.currentLayout
  if (currentLayout) {
    layoutMode.value = currentLayout.type || 'grid'
    gridColumns.value = currentLayout.config?.columns || 2
    if (currentLayout.items) {
      layoutItems.value = currentLayout.items.map(item => ({
        id: item.id,
        component: getComponentName(item.type),
        props: item.data || {},
        position: {
          x: item.position?.x || 0,
          y: item.position?.y || 0
        },
        size: {
          width: item.size?.width || '100%',
          height: item.size?.height || 'auto'
        },
        isDragging: false,
        isResizing: false,
        isFullscreen: false
      }))
    }
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped>
.dynamic-layout-container {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.layout-controls {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-label {
  font-size: 14px;
  color: #e2e8f0;
  font-weight: 500;
}

.dynamic-layout {
  width: 100%;
  min-height: 800px;
  position: relative;
  overflow: auto;
}

.layout-item {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.layout-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.layout-item.dragging {
  opacity: 0.8;
  cursor: grabbing;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.layout-item.resizing {
  opacity: 0.9;
}

.layout-item.fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999 !important;
  border-radius: 0 !important;
}

.item-content {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 16px;
}

.drag-handle {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 24px;
  height: 24px;
  background: rgba(59, 130, 246, 0.9);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.resize-handle {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: rgba(16, 185, 129, 0.9);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: nwse-resize;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.fullscreen-handle {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: rgba(245, 158, 11, 0.9);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.close-handle {
  position: absolute;
  top: 8px;
  right: 40px;
  width: 24px;
  height: 24px;
  background: rgba(239, 68, 68, 0.9);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.layout-item:hover .drag-handle,
.layout-item:hover .resize-handle,
.layout-item:hover .fullscreen-handle,
.layout-item:hover .close-handle {
  opacity: 1;
}

.add-component-panel {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .control-group {
    justify-content: space-between;
  }
  
  .dynamic-layout.grid {
    grid-template-columns: 1fr;
  }
  
  .dynamic-layout.dashboard {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  
  .layout-item.fullscreen {
    border-radius: 0 !important;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .dynamic-layout-container {
    background: rgba(0, 0, 0, 0.3);
    border-color: rgba(255, 255, 255, 0.05);
  }
  
  .layout-controls {
    background: rgba(0, 0, 0, 0.2);
    border-color: rgba(255, 255, 255, 0.05);
  }
  
  .control-label {
    color: #cbd5e1;
  }
  
  .layout-item {
    background: rgba(0, 0, 0, 0.8);
    border-color: rgba(255, 255, 255, 0.1);
  }
}
</style>
