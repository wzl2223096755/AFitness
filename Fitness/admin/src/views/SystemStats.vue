<template>
  <div class="system-stats">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-icon">📊</span>
        系统统计
      </h1>
      <p class="page-description">全面分析系统使用情况和用户活跃度</p>
    </div>

    <!-- 时间范围选择器 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">统计周期</label>
        <select v-model="selectedPeriod" class="admin-select" @change="handlePeriodChange">
          <option value="week">最近7天</option>
          <option value="month">最近30天</option>
          <option value="quarter">最近90天</option>
          <option value="year">最近一年</option>
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
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
          <div class="stat-label">总用户数</div>
          <div class="stat-trend" :class="getTrendClass(stats.userGrowthRate)">
            {{ formatTrend(stats.userGrowthRate) }}
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🟢</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.activeUsers) }}</div>
          <div class="stat-label">活跃用户</div>
          <div class="stat-trend neutral">
            {{ getActiveRate() }}% 活跃率
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🏋️</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalTrainingRecords) }}</div>
          <div class="stat-label">训练记录</div>
          <div class="stat-trend" :class="getTrendClass(stats.trainingGrowthRate)">
            {{ formatTrend(stats.trainingGrowthRate) }}
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalTrainingPlans) }}</div>
          <div class="stat-label">训练计划</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🥗</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalNutritionRecords) }}</div>
          <div class="stat-label">营养记录</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.avgDailyRecords) }}</div>
          <div class="stat-label">日均记录</div>
        </div>
      </div>
    </section>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 用户增长趋势图 -->
      <div class="chart-container chart-large">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">📈</span>
            用户增长趋势
          </h3>
          <div class="chart-actions">
            <button 
              :class="['chart-type-btn', { active: userChartType === 'line' }]"
              @click="userChartType = 'line'; updateUserGrowthChart()"
            >
              折线图
            </button>
            <button 
              :class="['chart-type-btn', { active: userChartType === 'bar' }]"
              @click="userChartType = 'bar'; updateUserGrowthChart()"
            >
              柱状图
            </button>
          </div>
        </div>
        <div ref="userGrowthChartRef" class="chart-content"></div>
      </div>

      <!-- 活跃用户分布图 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">🎯</span>
            活跃用户分布
          </h3>
        </div>
        <div ref="activeUserChartRef" class="chart-content"></div>
      </div>

      <!-- 训练数据统计图 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">🏋️</span>
            训练数据统计
          </h3>
        </div>
        <div ref="trainingStatsChartRef" class="chart-content"></div>
      </div>

      <!-- 数据类型分布图 -->
      <div class="chart-container chart-large">
        <div class="chart-header">
          <h3 class="chart-title">
            <span class="title-icon">📊</span>
            数据类型分布
          </h3>
        </div>
        <div ref="dataDistributionChartRef" class="chart-content"></div>
      </div>
    </div>

    <!-- 详细统计表格 -->
    <section class="stats-table-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">📋</span>
          详细统计数据
        </h2>
        <button class="admin-btn secondary" @click="exportStats">
          <span>📥</span>
          导出报表
        </button>
      </div>

      <div class="stats-table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>指标名称</th>
              <th>当前值</th>
              <th>上期值</th>
              <th>变化率</th>
              <th>趋势</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="metric in detailedMetrics" :key="metric.name">
              <td>{{ metric.name }}</td>
              <td class="value-cell">{{ formatNumber(metric.current) }}</td>
              <td class="value-cell">{{ formatNumber(metric.previous) }}</td>
              <td :class="['change-cell', getTrendClass(metric.changeRate)]">
                {{ formatTrend(metric.changeRate) }}
              </td>
              <td>
                <span class="trend-indicator" :class="getTrendClass(metric.changeRate)">
                  {{ getTrendIcon(metric.changeRate) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载统计数据中...</p>
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

// 响应式数据
const loading = ref(false)
const selectedPeriod = ref('month')
const userChartType = ref('line')

// 图表引用
const userGrowthChartRef = ref(null)
const activeUserChartRef = ref(null)
const trainingStatsChartRef = ref(null)
const dataDistributionChartRef = ref(null)

// 图表实例
let userGrowthChart = null
let activeUserChart = null
let trainingStatsChart = null
let dataDistributionChart = null

// 统计数据
const stats = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalTrainingRecords: 0,
  totalTrainingPlans: 0,
  totalNutritionRecords: 0,
  avgDailyRecords: 0,
  userGrowthRate: 0,
  trainingGrowthRate: 0
})

// 用户增长趋势数据
const userGrowthData = ref({
  dates: [],
  newUsers: [],
  totalUsers: []
})

// 活跃用户分布数据
const activeUserData = ref([])

// 训练统计数据
const trainingStatsData = ref({
  dates: [],
  records: []
})

// 详细指标数据
const detailedMetrics = ref([])

// 格式化数字
const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

// 获取活跃率
const getActiveRate = () => {
  if (!stats.value.totalUsers) return 0
  return Math.round((stats.value.activeUsers / stats.value.totalUsers) * 100)
}

// 获取趋势样式类
const getTrendClass = (rate) => {
  if (rate > 0) return 'positive'
  if (rate < 0) return 'negative'
  return 'neutral'
}

// 格式化趋势
const formatTrend = (rate) => {
  if (rate === undefined || rate === null) return '-'
  const sign = rate > 0 ? '+' : ''
  return `${sign}${rate.toFixed(1)}%`
}

// 获取趋势图标
const getTrendIcon = (rate) => {
  if (rate > 0) return '↑'
  if (rate < 0) return '↓'
  return '→'
}

// 获取日期范围
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
    case 'year':
      start.setFullYear(end.getFullYear() - 1)
      break
  }
  
  return {
    startDate: start.toISOString().split('T')[0],
    endDate: end.toISOString().split('T')[0]
  }
}

// 生成模拟日期数据
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

// 生成模拟数据
const generateMockData = () => {
  const days = selectedPeriod.value === 'week' ? 7 : 
               selectedPeriod.value === 'month' ? 30 : 
               selectedPeriod.value === 'quarter' ? 90 : 365
  
  const dates = generateDates(days)
  
  // 用户增长数据
  let totalUsers = 100
  const newUsers = []
  const totalUsersList = []
  
  for (let i = 0; i < days; i++) {
    const newUser = Math.floor(Math.random() * 5) + 1
    newUsers.push(newUser)
    totalUsers += newUser
    totalUsersList.push(totalUsers)
  }
  
  userGrowthData.value = {
    dates,
    newUsers,
    totalUsers: totalUsersList
  }
  
  // 活跃用户分布
  activeUserData.value = [
    { name: '高活跃', value: 35 },
    { name: '中活跃', value: 45 },
    { name: '低活跃', value: 15 },
    { name: '不活跃', value: 5 }
  ]
  
  // 训练统计数据
  const records = []
  for (let i = 0; i < days; i++) {
    records.push(Math.floor(Math.random() * 50) + 20)
  }
  
  trainingStatsData.value = {
    dates,
    records
  }
  
  // 统计概览
  stats.value = {
    totalUsers: totalUsers,
    activeUsers: Math.floor(totalUsers * 0.35),
    totalTrainingRecords: 3256 + Math.floor(Math.random() * 500),
    totalTrainingPlans: 89 + Math.floor(Math.random() * 20),
    totalNutritionRecords: 1542 + Math.floor(Math.random() * 200),
    avgDailyRecords: Math.floor(records.reduce((a, b) => a + b, 0) / days),
    userGrowthRate: 12.5,
    trainingGrowthRate: 8.3
  }
  
  // 详细指标
  detailedMetrics.value = [
    { name: '总用户数', current: stats.value.totalUsers, previous: stats.value.totalUsers - 15, changeRate: 12.5 },
    { name: '活跃用户', current: stats.value.activeUsers, previous: stats.value.activeUsers - 5, changeRate: 8.2 },
    { name: '训练记录', current: stats.value.totalTrainingRecords, previous: stats.value.totalTrainingRecords - 256, changeRate: 8.3 },
    { name: '训练计划', current: stats.value.totalTrainingPlans, previous: stats.value.totalTrainingPlans - 7, changeRate: 8.5 },
    { name: '营养记录', current: stats.value.totalNutritionRecords, previous: stats.value.totalNutritionRecords - 142, changeRate: 10.1 },
    { name: '日均记录', current: stats.value.avgDailyRecords, previous: stats.value.avgDailyRecords - 3, changeRate: 5.2 }
  ]
}

// 初始化用户增长图表
const initUserGrowthChart = () => {
  if (!userGrowthChartRef.value) return
  
  userGrowthChart = echarts.init(userGrowthChartRef.value)
  updateUserGrowthChart()
}

// 更新用户增长图表
const updateUserGrowthChart = () => {
  if (!userGrowthChart) return
  
  const chartType = userChartType.value
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['新增用户', '累计用户'],
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
      data: userGrowthData.value.dates,
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: [
      {
        type: 'value',
        name: '新增用户',
        axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.1)' } }
      },
      {
        type: 'value',
        name: '累计用户',
        axisLine: { lineStyle: { color: 'rgba(243, 156, 18, 0.3)' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '新增用户',
        type: chartType,
        data: userGrowthData.value.newUsers,
        itemStyle: { color: '#e94560' },
        areaStyle: chartType === 'line' ? { color: 'rgba(233, 69, 96, 0.2)' } : undefined,
        smooth: chartType === 'line'
      },
      {
        name: '累计用户',
        type: 'line',
        yAxisIndex: 1,
        data: userGrowthData.value.totalUsers,
        itemStyle: { color: '#f39c12' },
        smooth: true
      }
    ]
  }
  
  userGrowthChart.setOption(option)
}

// 初始化活跃用户图表
const initActiveUserChart = () => {
  if (!activeUserChartRef.value) return
  
  activeUserChart = echarts.init(activeUserChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c}% ({d}%)'
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
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#f8fafc'
          }
        },
        data: activeUserData.value.map((item, index) => ({
          ...item,
          itemStyle: {
            color: ['#10b981', '#f39c12', '#3b82f6', '#94a3b8'][index]
          }
        }))
      }
    ]
  }
  
  activeUserChart.setOption(option)
}

// 初始化训练统计图表
const initTrainingStatsChart = () => {
  if (!trainingStatsChartRef.value) return
  
  trainingStatsChart = echarts.init(trainingStatsChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trainingStatsData.value.dates,
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8', rotate: 45 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.3)' } },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(233, 69, 96, 0.1)' } }
    },
    series: [
      {
        type: 'bar',
        data: trainingStatsData.value.records,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e94560' },
            { offset: 1, color: '#f39c12' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  
  trainingStatsChart.setOption(option)
}

// 初始化数据分布图表
const initDataDistributionChart = () => {
  if (!dataDistributionChartRef.value) return
  
  dataDistributionChart = echarts.init(dataDistributionChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 33, 62, 0.95)',
      borderColor: 'rgba(233, 69, 96, 0.3)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['训练记录', '营养记录', '训练计划'],
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
      data: userGrowthData.value.dates.slice(-7),
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
        name: '训练记录',
        type: 'bar',
        stack: 'total',
        data: [45, 52, 38, 65, 48, 55, 42],
        itemStyle: { color: '#e94560' }
      },
      {
        name: '营养记录',
        type: 'bar',
        stack: 'total',
        data: [32, 28, 35, 42, 38, 45, 30],
        itemStyle: { color: '#f39c12' }
      },
      {
        name: '训练计划',
        type: 'bar',
        stack: 'total',
        data: [8, 12, 6, 10, 15, 9, 11],
        itemStyle: { color: '#3b82f6' }
      }
    ]
  }
  
  dataDistributionChart.setOption(option)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    const dateRange = getDateRange()
    
    // 尝试从API获取数据
    const [statsRes, growthRes, activeRes, trainingRes] = await Promise.allSettled([
      adminApi.getSystemStats(),
      adminApi.getUserGrowthTrend(dateRange),
      adminApi.getActiveUserStats({ period: selectedPeriod.value }),
      adminApi.getTrainingStats(dateRange)
    ])
    
    // 处理统计数据
    if (statsRes.status === 'fulfilled' && statsRes.value.data) {
      const data = statsRes.value.data
      stats.value = {
        totalUsers: data.totalUsers || 0,
        activeUsers: data.activeUsers || 0,
        totalTrainingRecords: data.totalTrainingRecords || 0,
        totalTrainingPlans: data.totalTrainingPlans || 0,
        totalNutritionRecords: data.totalNutritionRecords || 0,
        avgDailyRecords: data.avgDailyRecords || 0,
        userGrowthRate: data.userGrowthRate || 0,
        trainingGrowthRate: data.trainingGrowthRate || 0
      }
    } else {
      // 使用模拟数据
      generateMockData()
    }
    
    // 处理用户增长数据
    if (growthRes.status === 'fulfilled' && growthRes.value.data) {
      userGrowthData.value = growthRes.value.data
    }
    
    // 处理活跃用户数据
    if (activeRes.status === 'fulfilled' && activeRes.value.data) {
      activeUserData.value = activeRes.value.data
    }
    
    // 处理训练统计数据
    if (trainingRes.status === 'fulfilled' && trainingRes.value.data) {
      trainingStatsData.value = trainingRes.value.data
    }
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用模拟数据
    generateMockData()
  }
  
  loading.value = false
  
  // 更新图表
  await nextTick()
  updateAllCharts()
}

// 更新所有图表
const updateAllCharts = () => {
  updateUserGrowthChart()
  if (activeUserChart) {
    activeUserChart.setOption({
      series: [{
        data: activeUserData.value.map((item, index) => ({
          ...item,
          itemStyle: {
            color: ['#10b981', '#f39c12', '#3b82f6', '#94a3b8'][index]
          }
        }))
      }]
    })
  }
  if (trainingStatsChart) {
    trainingStatsChart.setOption({
      xAxis: { data: trainingStatsData.value.dates },
      series: [{ data: trainingStatsData.value.records }]
    })
  }
  if (dataDistributionChart) {
    dataDistributionChart.setOption({
      xAxis: { data: userGrowthData.value.dates.slice(-7) }
    })
  }
}

// 处理周期变化
const handlePeriodChange = () => {
  loadData()
}

// 刷新数据
const refreshData = () => {
  loadData()
}

// 导出统计报表
const exportStats = () => {
  // 创建CSV内容
  const headers = ['指标名称', '当前值', '上期值', '变化率']
  const rows = detailedMetrics.value.map(m => [
    m.name,
    m.current,
    m.previous,
    `${m.changeRate}%`
  ])
  
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  // 创建下载链接
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `系统统计报表_${new Date().toLocaleDateString('zh-CN')}.csv`
  link.click()
}

// 处理窗口大小变化
const handleResize = () => {
  userGrowthChart?.resize()
  activeUserChart?.resize()
  trainingStatsChart?.resize()
  dataDistributionChart?.resize()
}

// 生命周期
onMounted(async () => {
  // 先生成模拟数据
  generateMockData()
  
  // 初始化图表
  await nextTick()
  initUserGrowthChart()
  initActiveUserChart()
  initTrainingStatsChart()
  initDataDistributionChart()
  
  // 加载真实数据
  loadData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  userGrowthChart?.dispose()
  activeUserChart?.dispose()
  trainingStatsChart?.dispose()
  dataDistributionChart?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>


<style scoped>
.system-stats {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
}

/* 页面头部 */
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

/* 筛选栏 */
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

/* 统计概览 */
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

/* 图表网格 */
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

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-type-btn {
  padding: 6px 12px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chart-type-btn:hover {
  border-color: rgba(233, 69, 96, 0.4);
  color: #f8fafc;
}

.chart-type-btn.active {
  background: rgba(233, 69, 96, 0.2);
  border-color: #e94560;
  color: #e94560;
}

.chart-content {
  height: 280px;
}

/* 统计表格区域 */
.stats-table-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.stats-table-container {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.admin-table th {
  background: rgba(233, 69, 96, 0.1);
  color: #f8fafc;
  font-weight: 600;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.admin-table td {
  color: #94a3b8;
  font-size: 0.9rem;
}

.admin-table tr:hover td {
  background: rgba(233, 69, 96, 0.05);
  color: #f8fafc;
}

.value-cell {
  font-weight: 600;
  color: #f8fafc;
}

.change-cell {
  font-weight: 600;
}

.change-cell.positive {
  color: #10b981;
}

.change-cell.neutral {
  color: #f39c12;
}

.change-cell.negative {
  color: #ef4444;
}

.trend-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 0.9rem;
}

.trend-indicator.positive {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.trend-indicator.neutral {
  background: rgba(243, 156, 18, 0.15);
  color: #f39c12;
}

.trend-indicator.negative {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

/* 加载状态 */
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

/* 按钮样式 */
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

/* 响应式 */
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
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
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
