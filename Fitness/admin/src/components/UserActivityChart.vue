<template>
  <div class="user-activity-chart">
    <!-- 活跃度概览 -->
    <div class="activity-overview">
      <div class="overview-card">
        <div class="overview-icon">👥</div>
        <div class="overview-content">
          <div class="overview-value">{{ formatNumber(activityData.totalUsers) }}</div>
          <div class="overview-label">总用户数</div>
        </div>
      </div>
      
      <div class="overview-card highlight">
        <div class="overview-icon">🟢</div>
        <div class="overview-content">
          <div class="overview-value">{{ formatNumber(activityData.activeUsersToday) }}</div>
          <div class="overview-label">今日活跃</div>
        </div>
      </div>
      
      <div class="overview-card">
        <div class="overview-icon">📅</div>
        <div class="overview-content">
          <div class="overview-value">{{ formatNumber(activityData.activeUsersWeek) }}</div>
          <div class="overview-label">本周活跃</div>
        </div>
      </div>
      
      <div class="overview-card">
        <div class="overview-icon">✨</div>
        <div class="overview-content">
          <div class="overview-value">{{ formatNumber(activityData.newUsersToday) }}</div>
          <div class="overview-label">今日新增</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 活跃度趋势图 -->
      <div class="chart-card chart-large">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">📈</span>
            用户活跃度趋势
          </h3>
          <div class="chart-actions">
            <button 
              v-for="period in periods" 
              :key="period.value"
              :class="['period-btn', { active: selectedPeriod === period.value }]"
              @click="changePeriod(period.value)"
            >
              {{ period.label }}
            </button>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-content"></div>
      </div>

      <!-- 用户角色分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">🎭</span>
            用户角色分布
          </h3>
        </div>
        <div ref="roleChartRef" class="chart-content"></div>
      </div>

      <!-- 活跃度分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">🎯</span>
            活跃度分布
          </h3>
        </div>
        <div ref="activityDistChartRef" class="chart-content"></div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载活跃度数据中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { adminApi } from '@shared/api/admin'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 echarts 组件
echarts.use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])

// Props
const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: false
  },
  refreshInterval: {
    type: Number,
    default: 60000
  }
})

// Emits
const emit = defineEmits(['error', 'loaded'])

// State
const loading = ref(false)
const selectedPeriod = ref('week')
let refreshTimer = null

const periods = [
  { value: 'week', label: '7天' },
  { value: 'month', label: '30天' },
  { value: 'quarter', label: '90天' }
]

// Chart refs
const trendChartRef = ref(null)
const roleChartRef = ref(null)
const activityDistChartRef = ref(null)

// Chart instances
let trendChart = null
let roleChart = null
let activityDistChart = null

// Activity data
const activityData = ref({
  totalUsers: 0,
  activeUsersToday: 0,
  activeUsersWeek: 0,
  newUsersToday: 0,
  usersByRole: {},
  dailyActivity: []
})

// Methods
const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

const generateDates = (days) => {
  const dates = []
  const end = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(end)
    date.setDate(end.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
  }
  return dates
}

const getDaysForPeriod = () => {
  switch (selectedPeriod.value) {
    case 'week': return 7
    case 'month': return 30
    case 'quarter': return 90
    default: return 7
  }
}

const generateMockData = () => {
  const days = getDaysForPeriod()
  const dates = generateDates(days)
  
  // Generate daily activity data
  const dailyActivity = dates.map((date, index) => ({
    date,
    activeUsers: Math.floor(Math.random() * 30) + 20,
    newUsers: Math.floor(Math.random() * 5) + 1
  }))
  
  activityData.value = {
    totalUsers: 128 + Math.floor(Math.random() * 20),
    activeUsersToday: 35 + Math.floor(Math.random() * 15),
    activeUsersWeek: 85 + Math.floor(Math.random() * 20),
    newUsersToday: Math.floor(Math.random() * 5) + 1,
    usersByRole: {
      'USER': 100 + Math.floor(Math.random() * 20),
      'ADMIN': 3 + Math.floor(Math.random() * 2)
    },
    dailyActivity
  }
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChart) return
  
  const dailyActivity = activityData.value.dailyActivity || []
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['活跃用户', '新增用户'],
      textStyle: { color: '#94a3b8' },
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dailyActivity.map(d => d.date),
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8', rotate: dailyActivity.length > 15 ? 45 : 0 }
    },
    yAxis: [
      {
        type: 'value',
        name: '活跃用户',
        axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.1)' } }
      },
      {
        type: 'value',
        name: '新增用户',
        axisLine: { lineStyle: { color: 'rgba(243, 156, 18, 0.3)' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '活跃用户',
        type: 'line',
        data: dailyActivity.map(d => d.activeUsers),
        itemStyle: { color: '#e94560' },
        areaStyle: { color: 'rgba(233, 69, 96, 0.2)' },
        smooth: true
      },
      {
        name: '新增用户',
        type: 'bar',
        yAxisIndex: 1,
        data: dailyActivity.map(d => d.newUsers),
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f39c12' },
            { offset: 1, color: '#e67e22' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initRoleChart = () => {
  if (!roleChartRef.value) return
  
  roleChart = echarts.init(roleChartRef.value)
  updateRoleChart()
}

const updateRoleChart = () => {
  if (!roleChart) return
  
  const usersByRole = activityData.value.usersByRole || {}
  const roleData = Object.entries(usersByRole).map(([role, count]) => ({
    name: role === 'ADMIN' ? '管理员' : '普通用户',
    value: count
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#1a1a2e',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#f8fafc'
          }
        },
        data: roleData.map((item, index) => ({
          ...item,
          itemStyle: {
            color: ['#3b82f6', '#e94560'][index] || '#94a3b8'
          }
        }))
      }
    ]
  }
  
  roleChart.setOption(option)
}

const initActivityDistChart = () => {
  if (!activityDistChartRef.value) return
  
  activityDistChart = echarts.init(activityDistChartRef.value)
  updateActivityDistChart()
}

const updateActivityDistChart = () => {
  if (!activityDistChart) return
  
  // Calculate activity distribution based on data
  const total = activityData.value.totalUsers || 100
  const activeToday = activityData.value.activeUsersToday || 0
  const activeWeek = activityData.value.activeUsersWeek || 0
  
  const highActive = Math.floor(activeToday * 0.6)
  const mediumActive = Math.floor((activeWeek - activeToday) * 0.7)
  const lowActive = Math.floor((total - activeWeek) * 0.3)
  const inactive = total - highActive - mediumActive - lowActive
  
  const distData = [
    { name: '高活跃', value: Math.max(highActive, 0) },
    { name: '中活跃', value: Math.max(mediumActive, 0) },
    { name: '低活跃', value: Math.max(lowActive, 0) },
    { name: '不活跃', value: Math.max(inactive, 0) }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#1a1a2e',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#f8fafc'
          }
        },
        data: distData.map((item, index) => ({
          ...item,
          itemStyle: {
            color: ['#10b981', '#f39c12', '#3b82f6', '#94a3b8'][index]
          }
        }))
      }
    ]
  }
  
  activityDistChart.setOption(option)
}

const loadActivityData = async () => {
  loading.value = true
  
  try {
    const res = await adminApi.getUserActivityStats()
    if (res.data) {
      activityData.value = {
        totalUsers: res.data.totalUsers || 0,
        activeUsersToday: res.data.activeUsersToday || 0,
        activeUsersWeek: res.data.activeUsersWeek || 0,
        newUsersToday: res.data.newUsersToday || 0,
        usersByRole: res.data.usersByRole || {},
        dailyActivity: res.data.dailyActivity || []
      }
    } else {
      generateMockData()
    }
  } catch (error) {
    console.error('加载用户活跃度数据失败:', error)
    emit('error', error)
    generateMockData()
  }
  
  loading.value = false
  
  await nextTick()
  updateAllCharts()
  emit('loaded')
}

const updateAllCharts = () => {
  updateTrendChart()
  updateRoleChart()
  updateActivityDistChart()
}

const changePeriod = (period) => {
  selectedPeriod.value = period
  loadActivityData()
}

const handleResize = () => {
  trendChart?.resize()
  roleChart?.resize()
  activityDistChart?.resize()
}

const startAutoRefresh = () => {
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(loadActivityData, props.refreshInterval)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// Lifecycle
onMounted(async () => {
  generateMockData()
  
  await nextTick()
  initTrendChart()
  initRoleChart()
  initActivityDistChart()
  
  loadActivityData()
  startAutoRefresh()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopAutoRefresh()
  trendChart?.dispose()
  roleChart?.dispose()
  activityDistChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

// Expose
defineExpose({
  refresh: loadActivityData
})
</script>

<style scoped>
.user-activity-chart {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
}

/* Activity Overview */
.activity-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.overview-card {
  background: linear-gradient(135deg, rgba(22, 33, 62, 0.9), rgba(26, 26, 46, 0.9));
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.overview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #e94560, #f39c12);
}

.overview-card:hover {
  border-color: rgba(233, 69, 96, 0.4);
  box-shadow: 0 8px 30px rgba(233, 69, 96, 0.15);
  transform: translateY(-2px);
}

.overview-card.highlight {
  background: linear-gradient(135deg, rgba(233, 69, 96, 0.15), rgba(26, 26, 46, 0.9));
  border-color: rgba(233, 69, 96, 0.4);
}

.overview-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.overview-content {
  flex: 1;
}

.overview-value {
  font-size: 1.6rem;
  font-weight: 700;
  color: #f8fafc;
  line-height: 1.2;
}

.overview-label {
  color: #94a3b8;
  font-size: 0.85rem;
  margin-top: 4px;
}

/* Charts Container */
.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.chart-card.chart-large {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  color: #f8fafc;
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1rem;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.period-btn {
  padding: 6px 12px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.period-btn:hover {
  border-color: rgba(233, 69, 96, 0.4);
  color: #f8fafc;
}

.period-btn.active {
  background: rgba(233, 69, 96, 0.2);
  border-color: #e94560;
  color: #e94560;
}

.chart-content {
  height: 280px;
}

/* Loading Overlay */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(233, 69, 96, 0.2);
  border-top-color: #e94560;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay p {
  color: #94a3b8;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 1024px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .chart-card.chart-large {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .activity-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .activity-overview {
    grid-template-columns: 1fr;
  }
  
  .overview-card {
    padding: 16px;
  }
  
  .overview-icon {
    font-size: 1.5rem;
  }
  
  .overview-value {
    font-size: 1.3rem;
  }
  
  .chart-content {
    height: 220px;
  }
}
</style>
