<template>
  <div class="training-stats">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-icon">🏋️</span>
        训练数据统计
      </h1>
      <p class="page-description">全面分析用户训练数据和趋势</p>
    </div>

    <!-- 时间范围选择器 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">统计周期</label>
        <select v-model="selectedPeriod" class="admin-select" @change="handlePeriodChange">
          <option value="week">最近7天</option>
          <option value="month">最近30天</option>
          <option value="quarter">最近90天</option>
        </select>
      </div>
      <button class="admin-btn secondary" @click="refreshData">
        <span>🔄</span>
        刷新数据
      </button>
    </div>

    <!-- 统计概览卡片 -->
    <section class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalRecords) }}</div>
          <div class="stat-label">总训练记录</div>
          <div class="stat-trend" :class="getTrendClass(stats.recordGrowthRate)">
            {{ formatTrend(stats.recordGrowthRate) }}
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">💪</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.strengthRecords) }}</div>
          <div class="stat-label">力量训练</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🏃</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.cardioRecords) }}</div>
          <div class="stat-label">有氧训练</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalPlans) }}</div>
          <div class="stat-label">训练计划</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.activeTrainers) }}</div>
          <div class="stat-label">活跃训练者</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.avgDailyRecords.toFixed(1) }}</div>
          <div class="stat-label">日均记录</div>
        </div>
      </div>
    </section>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 训练记录趋势图 -->
      <div class="chart-container chart-large">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">📈</span>
            训练记录趋势
          </h3>
        </div>
        <div ref="trendChartRef" class="chart-content"></div>
      </div>

      <!-- 训练类型分布 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">🎯</span>
            训练类型分布
          </h3>
        </div>
        <div ref="typeDistChartRef" class="chart-content"></div>
      </div>

      <!-- 训练强度分布 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">⚡</span>
            训练强度分布
          </h3>
        </div>
        <div ref="intensityChartRef" class="chart-content"></div>
      </div>

      <!-- 每周训练分布 -->
      <div class="chart-container chart-large">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">📅</span>
            每周训练分布
          </h3>
        </div>
        <div ref="weeklyChartRef" class="chart-content"></div>
      </div>
    </div>

    <!-- 热门训练项目 -->
    <section class="popular-exercises">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🔥</span>
          热门训练项目
        </h2>
      </div>
      <div class="exercises-grid">
        <div 
          v-for="(exercise, index) in popularExercises" 
          :key="exercise.name"
          class="exercise-card"
        >
          <div class="exercise-rank">{{ index + 1 }}</div>
          <div class="exercise-info">
            <div class="exercise-name">{{ exercise.name }}</div>
            <div class="exercise-count">{{ formatNumber(exercise.count) }} 次</div>
          </div>
          <div class="exercise-bar">
            <div 
              class="exercise-bar-fill" 
              :style="{ width: `${exercise.percentage}%` }"
            ></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载训练数据中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
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

// State
const loading = ref(false)
const selectedPeriod = ref('month')

// Chart refs
const trendChartRef = ref(null)
const typeDistChartRef = ref(null)
const intensityChartRef = ref(null)
const weeklyChartRef = ref(null)

// Chart instances
let trendChart = null
let typeDistChart = null
let intensityChart = null
let weeklyChart = null

// Stats data
const stats = ref({
  totalRecords: 0,
  strengthRecords: 0,
  cardioRecords: 0,
  totalPlans: 0,
  activeTrainers: 0,
  avgDailyRecords: 0,
  recordGrowthRate: 0
})

// Trend data
const trendData = ref({
  dates: [],
  strength: [],
  cardio: []
})

// Popular exercises
const popularExercises = ref([])

// Methods
const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

const getTrendClass = (rate) => {
  if (rate > 0) return 'positive'
  if (rate < 0) return 'negative'
  return 'neutral'
}

const formatTrend = (rate) => {
  if (rate === undefined || rate === null) return '-'
  const sign = rate > 0 ? '+' : ''
  return `${sign}${rate.toFixed(1)}%`
}

const getDaysForPeriod = () => {
  switch (selectedPeriod.value) {
    case 'week': return 7
    case 'month': return 30
    case 'quarter': return 90
    default: return 30
  }
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

const generateMockData = () => {
  const days = getDaysForPeriod()
  const dates = generateDates(days)
  
  // Generate trend data
  const strength = []
  const cardio = []
  for (let i = 0; i < days; i++) {
    strength.push(Math.floor(Math.random() * 30) + 15)
    cardio.push(Math.floor(Math.random() * 20) + 10)
  }
  
  trendData.value = { dates, strength, cardio }
  
  const totalStrength = strength.reduce((a, b) => a + b, 0)
  const totalCardio = cardio.reduce((a, b) => a + b, 0)
  
  stats.value = {
    totalRecords: totalStrength + totalCardio,
    strengthRecords: totalStrength,
    cardioRecords: totalCardio,
    totalPlans: 89 + Math.floor(Math.random() * 20),
    activeTrainers: 45 + Math.floor(Math.random() * 15),
    avgDailyRecords: (totalStrength + totalCardio) / days,
    recordGrowthRate: 8.5 + (Math.random() * 10 - 5)
  }
  
  // Popular exercises
  const exercises = [
    { name: '深蹲', count: 856 },
    { name: '卧推', count: 742 },
    { name: '硬拉', count: 623 },
    { name: '引体向上', count: 512 },
    { name: '肩推', count: 445 },
    { name: '划船', count: 398 },
    { name: '二头弯举', count: 356 },
    { name: '腿举', count: 312 }
  ]
  
  const maxCount = exercises[0].count
  popularExercises.value = exercises.map(e => ({
    ...e,
    percentage: (e.count / maxCount) * 100
  }))
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['力量训练', '有氧训练'],
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
      data: trendData.value.dates,
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8', rotate: trendData.value.dates.length > 15 ? 45 : 0 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.1)' } }
    },
    series: [
      {
        name: '力量训练',
        type: 'line',
        data: trendData.value.strength,
        itemStyle: { color: '#e94560' },
        areaStyle: { color: 'rgba(233, 69, 96, 0.2)' },
        smooth: true
      },
      {
        name: '有氧训练',
        type: 'line',
        data: trendData.value.cardio,
        itemStyle: { color: '#3b82f6' },
        areaStyle: { color: 'rgba(59, 130, 246, 0.2)' },
        smooth: true
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initTypeDistChart = () => {
  if (!typeDistChartRef.value) return
  
  typeDistChart = echarts.init(typeDistChartRef.value)
  updateTypeDistChart()
}

const updateTypeDistChart = () => {
  if (!typeDistChart) return
  
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
        radius: ['40%', '70%'],
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
        data: [
          { value: stats.value.strengthRecords, name: '力量训练', itemStyle: { color: '#e94560' } },
          { value: stats.value.cardioRecords, name: '有氧训练', itemStyle: { color: '#3b82f6' } }
        ]
      }
    ]
  }
  
  typeDistChart.setOption(option)
}

const initIntensityChart = () => {
  if (!intensityChartRef.value) return
  
  intensityChart = echarts.init(intensityChartRef.value)
  updateIntensityChart()
}

const updateIntensityChart = () => {
  if (!intensityChart) return
  
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
        radius: ['40%', '70%'],
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
        data: [
          { value: 35, name: '高强度', itemStyle: { color: '#ef4444' } },
          { value: 45, name: '中强度', itemStyle: { color: '#f39c12' } },
          { value: 20, name: '低强度', itemStyle: { color: '#10b981' } }
        ]
      }
    ]
  }
  
  intensityChart.setOption(option)
}

const initWeeklyChart = () => {
  if (!weeklyChartRef.value) return
  
  weeklyChart = echarts.init(weeklyChartRef.value)
  updateWeeklyChart()
}

const updateWeeklyChart = () => {
  if (!weeklyChart) return
  
  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const strengthData = [45, 52, 38, 65, 48, 72, 55]
  const cardioData = [32, 28, 35, 42, 38, 58, 45]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['力量训练', '有氧训练'],
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
      data: weekdays,
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.1)' } }
    },
    series: [
      {
        name: '力量训练',
        type: 'bar',
        data: strengthData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e94560' },
            { offset: 1, color: '#f39c12' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '有氧训练',
        type: 'bar',
        data: cardioData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#06b6d4' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  
  weeklyChart.setOption(option)
}

const loadData = async () => {
  loading.value = true
  
  try {
    const dateRange = getDateRange()
    const res = await adminApi.getTrainingStats(dateRange)
    
    if (res.data) {
      // Process real data
      stats.value = {
        totalRecords: res.data.totalRecords || 0,
        strengthRecords: res.data.strengthRecords || 0,
        cardioRecords: res.data.cardioRecords || 0,
        totalPlans: res.data.totalPlans || 0,
        activeTrainers: res.data.activeTrainers || 0,
        avgDailyRecords: res.data.avgDailyRecords || 0,
        recordGrowthRate: res.data.recordGrowthRate || 0
      }
      
      if (res.data.trendData) {
        trendData.value = res.data.trendData
      }
      
      if (res.data.popularExercises) {
        const maxCount = res.data.popularExercises[0]?.count || 1
        popularExercises.value = res.data.popularExercises.map(e => ({
          ...e,
          percentage: (e.count / maxCount) * 100
        }))
      }
    } else {
      generateMockData()
    }
  } catch (error) {
    console.error('加载训练数据失败:', error)
    generateMockData()
  }
  
  loading.value = false
  
  await nextTick()
  updateAllCharts()
}

const getDateRange = () => {
  const end = new Date()
  const start = new Date()
  
  switch (selectedPeriod.value) {
    case 'week':
      start.setDate(end.getDate() - 7)
      break
    case 'month':
      start.setDate(end.getDate() - 30)
      break
    case 'quarter':
      start.setDate(end.getDate() - 90)
      break
  }
  
  return {
    startDate: start.toISOString().split('T')[0],
    endDate: end.toISOString().split('T')[0]
  }
}

const updateAllCharts = () => {
  updateTrendChart()
  updateTypeDistChart()
  updateIntensityChart()
  updateWeeklyChart()
}

const handlePeriodChange = () => {
  loadData()
}

const refreshData = () => {
  loadData()
}

const handleResize = () => {
  trendChart?.resize()
  typeDistChart?.resize()
  intensityChart?.resize()
  weeklyChart?.resize()
}

// Lifecycle
onMounted(async () => {
  generateMockData()
  
  await nextTick()
  initTrendChart()
  initTypeDistChart()
  initIntensityChart()
  initWeeklyChart()
  
  loadData()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  typeDistChart?.dispose()
  intensityChart?.dispose()
  weeklyChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.training-stats {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
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

/* Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  color: #94a3b8;
  font-size: 0.9rem;
}

.admin-select {
  padding: 10px 16px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.admin-select:focus {
  outline: none;
  border-color: #e94560;
}

.admin-select option {
  background: #1a1a2e;
  color: #f8fafc;
}

.admin-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.admin-btn.secondary {
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  color: #e94560;
}

.admin-btn.secondary:hover {
  background: rgba(233, 69, 96, 0.2);
  transform: translateY(-1px);
}

/* Stats Overview */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(22, 33, 62, 0.9), rgba(26, 26, 46, 0.9));
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #e94560, #f39c12);
}

.stat-card:hover {
  border-color: rgba(233, 69, 96, 0.4);
  box-shadow: 0 8px 30px rgba(233, 69, 96, 0.15);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.6rem;
  font-weight: 700;
  color: #f8fafc;
  line-height: 1.2;
}

.stat-label {
  color: #94a3b8;
  font-size: 0.85rem;
  margin: 4px 0;
}

.stat-trend {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
  margin-top: 4px;
}

.stat-trend.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.15);
}

.stat-trend.neutral {
  color: #f39c12;
  background: rgba(243, 156, 18, 0.15);
}

.stat-trend.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.15);
}

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-container {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.chart-container.chart-large {
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

.chart-content {
  height: 280px;
}

/* Popular Exercises */
.popular-exercises {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  color: #f8fafc;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.exercises-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 12px;
}

.exercise-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(26, 26, 46, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(233, 69, 96, 0.1);
  transition: all 0.2s ease;
}

.exercise-card:hover {
  background: rgba(233, 69, 96, 0.05);
  border-color: rgba(233, 69, 96, 0.2);
}

.exercise-rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e94560, #f39c12);
  border-radius: 50%;
  color: #fff;
  font-size: 0.85rem;
  font-weight: 700;
  flex-shrink: 0;
}

.exercise-info {
  flex: 1;
  min-width: 0;
}

.exercise-name {
  color: #f8fafc;
  font-size: 0.95rem;
  font-weight: 500;
  margin-bottom: 2px;
}

.exercise-count {
  color: #94a3b8;
  font-size: 0.8rem;
}

.exercise-bar {
  width: 80px;
  height: 6px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.exercise-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #e94560, #f39c12);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(233, 69, 96, 0.2);
  border-top-color: #e94560;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay p {
  color: #94a3b8;
  font-size: 1rem;
}

/* Responsive */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container.chart-large {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .filter-group {
    justify-content: space-between;
  }
  
  .exercises-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    font-size: 1.5rem;
  }
  
  .stat-value {
    font-size: 1.3rem;
  }
  
  .chart-content {
    height: 220px;
  }
}
</style>
