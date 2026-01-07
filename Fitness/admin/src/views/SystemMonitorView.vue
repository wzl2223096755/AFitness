<template>
  <div class="system-monitor-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-icon">💻</span>
        系统监控
      </h1>
      <p class="page-description">实时监控系统运行状态和资源使用情况</p>
    </div>

    <!-- 标签页切换 -->
    <div class="tab-bar">
      <button 
        v-for="tab in tabs" 
        :key="tab.value"
        :class="['tab-btn', { active: activeTab === tab.value }]"
        @click="activeTab = tab.value"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        {{ tab.label }}
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="tab-content">
      <!-- 系统监控 -->
      <div v-show="activeTab === 'monitor'" class="tab-panel">
        <SystemMonitor 
          ref="systemMonitorRef"
          :auto-refresh="true"
          :refresh-interval="30000"
          @error="handleError"
          @loaded="handleLoaded"
        />
      </div>

      <!-- 用户活跃度 -->
      <div v-show="activeTab === 'activity'" class="tab-panel">
        <UserActivityChart 
          ref="activityChartRef"
          :auto-refresh="false"
          @error="handleError"
          @loaded="handleLoaded"
        />
      </div>

      <!-- 数据导出 -->
      <div v-show="activeTab === 'export'" class="tab-panel">
        <DataExport 
          @exported="handleExported"
          @error="handleError"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import SystemMonitor from '../components/SystemMonitor.vue'
import UserActivityChart from '../components/UserActivityChart.vue'
import DataExport from '../components/DataExport.vue'

// State
const activeTab = ref('monitor')
const systemMonitorRef = ref(null)
const activityChartRef = ref(null)

// Tabs
const tabs = [
  { value: 'monitor', label: '系统监控', icon: '💻' },
  { value: 'activity', label: '用户活跃度', icon: '📊' },
  { value: 'export', label: '数据导出', icon: '📥' }
]

// Methods
const handleError = (error) => {
  console.error('Error:', error)
  ElMessage.error('操作失败，请稍后重试')
}

const handleLoaded = () => {
  // Data loaded successfully
}

const handleExported = ({ type, filename }) => {
  ElMessage.success(`${filename} 导出成功`)
}
</script>

<style scoped>
.system-monitor-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Page Header */
.page-header {
  margin-bottom: 8px;
}

.page-title {
  color: #f8fafc;
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 1.5rem;
}

.page-description {
  color: #94a3b8;
  font-size: 0.95rem;
  margin: 0;
}

/* Tab Bar */
.tab-bar {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(233, 69, 96, 0.1);
  color: #f8fafc;
}

.tab-btn.active {
  background: rgba(233, 69, 96, 0.2);
  color: #e94560;
}

.tab-icon {
  font-size: 1.1rem;
}

/* Tab Content */
.tab-content {
  min-height: 400px;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .tab-bar {
    flex-wrap: wrap;
  }
  
  .tab-btn {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .tab-btn {
    padding: 10px 14px;
    font-size: 0.85rem;
  }
  
  .tab-icon {
    font-size: 1rem;
  }
}
</style>
