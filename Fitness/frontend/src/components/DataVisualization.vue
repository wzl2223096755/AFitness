<template>
  <div class="data-visualization">
    <div class="section-header">
      <h2>数据可视化分析</h2>
      <div class="section-actions">
        <el-select v-model="selectedChart" placeholder="选择图表类型" style="width: 150px; margin-right: 12px;">
          <el-option label="训练趋势" value="trend" />
          <el-option label="强度分布" value="intensity" />
          <el-option label="类型统计" value="typeStats" />
          <el-option label="进度对比" value="progress" />
        </el-select>
        <el-button @click="refreshCharts" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 时间范围选择 -->
    <div class="time-range-selector">
      <el-radio-group v-model="timeRange" @change="onTimeRangeChange">
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="quarter">本季度</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 图表容器 -->
    <div class="charts-container" v-loading="loading">
      <!-- 训练趋势图 -->
      <div v-if="selectedChart === 'trend'" class="chart-wrapper">
        <div class="chart-header">
          <h3>训练趋势分析</h3>
          <div class="chart-legend">
            <span class="legend-item">
              <span class="legend-color" style="background: #409EFF;"></span>
              训练次数
            </span>
            <span class="legend-item">
              <span class="legend-color" style="background: #67C23A;"></span>
              消耗卡路里
            </span>
          </div>
        </div>
        <div ref="trendChart" class="chart"></div>
      </div>

      <!-- 强度分布图 -->
      <div v-if="selectedChart === 'intensity'" class="chart-wrapper">
        <div class="chart-header">
          <h3>训练强度分布</h3>
        </div>
        <div ref="intensityChart" class="chart"></div>
      </div>

      <!-- 类型统计图 -->
      <div v-if="selectedChart === 'typeStats'" class="chart-wrapper">
        <div class="chart-header">
          <h3>训练类型统计</h3>
        </div>
        <div ref="typeStatsChart" class="chart"></div>
      </div>

      <!-- 进度对比图 -->
      <div v-if="selectedChart === 'progress'" class="chart-wrapper">
        <div class="chart-header">
          <h3>训练进度对比</h3>
        </div>
        <div ref="progressChart" class="chart"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stats-grid">
        <div class="stats-card">
          <div class="stats-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stats-content">
            <div class="stats-value">{{ totalWorkouts }}</div>
            <div class="stats-label">总训练次数</div>
            <div class="stats-change" :class="workoutChangeClass">
              {{ workoutChange > 0 ? '+' : '' }}{{ workoutChange }}%
            </div>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stats-content">
            <div class="stats-value">{{ totalDuration }}</div>
            <div class="stats-label">总训练时长(分钟)</div>
            <div class="stats-change" :class="durationChangeClass">
              {{ durationChange > 0 ? '+' : '' }}{{ durationChange }}%
            </div>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><Odometer /></el-icon>
          </div>
          <div class="stats-content">
            <div class="stats-value">{{ totalCalories }}</div>
            <div class="stats-label">总消耗卡路里</div>
            <div class="stats-change" :class="caloriesChangeClass">
              {{ caloriesChange > 0 ? '+' : '' }}{{ caloriesChange }}%
            </div>
          </div>
        </div>

        <div class="stats-card">
          <div class="stats-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stats-content">
            <div class="stats-value">{{ avgIntensity }}</div>
            <div class="stats-label">平均训练强度</div>
            <div class="stats-change" :class="intensityChangeClass">
              {{ intensityChange > 0 ? '+' : '' }}{{ intensityChange }}%
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, TrendCharts, Timer, Odometer, Star } from '@element-plus/icons-vue'
import echarts from '../utils/echarts'

// 响应式数据
const loading = ref(false)
const selectedChart = ref('trend')
const timeRange = ref('month')

// 图表实例
const trendChart = ref(null)
const intensityChart = ref(null)
const typeStatsChart = ref(null)
const progressChart = ref(null)
let chartInstances = {}

// 统计数据
const totalWorkouts = ref(0)
const totalDuration = ref(0)
const totalCalories = ref(0)
const avgIntensity = ref(0)

// 变化数据
const workoutChange = ref(0)
const durationChange = ref(0)
const caloriesChange = ref(0)
const intensityChange = ref(0)

// 数据缓存
const dataCache = new Map()
const CACHE_TTL = 10 * 60 * 1000 // 10分钟缓存

// 计算属性
const workoutChangeClass = computed(() => workoutChange.value >= 0 ? 'positive' : 'negative')
const durationChangeClass = computed(() => durationChange.value >= 0 ? 'positive' : 'negative')
const caloriesChangeClass = computed(() => caloriesChange.value >= 0 ? 'positive' : 'negative')
const intensityChangeClass = computed(() => intensityChange.value >= 0 ? 'positive' : 'negative')

// 方法
const refreshCharts = async () => {
  loading.value = true
  try {
    // 检查缓存
    const cacheKey = `chart_data_${timeRange.value}`
    const cached = dataCache.get(cacheKey)
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      updateChartData(cached.data)
      return
    }
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = generateMockData(timeRange.value)
    
    updateChartData(mockData)
    
    // 缓存数据
    dataCache.set(cacheKey, {
      data: mockData,
      timestamp: Date.now()
    })
    
    ElMessage.success('图表数据已刷新')
  } catch (error) {
    console.error('刷新图表数据失败:', error)
    ElMessage.error('刷新图表数据失败')
  } finally {
    loading.value = false
  }
}

const generateMockData = (range) => {
  const now = new Date()
  let days = 30
  let dataPoints = []
  
  switch (range) {
    case 'week':
      days = 7
      break
    case 'month':
      days = 30
      break
    case 'quarter':
      days = 90
      break
    case 'year':
      days = 365
      break
  }
  
  // 生成日期数据
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    dataPoints.push({
      date: date.toISOString().split('T')[0],
      workouts: Math.floor(Math.random() * 3) + 1,
      duration: Math.floor(Math.random() * 60) + 30,
      calories: Math.floor(Math.random() * 400) + 200,
      intensity: Math.random() * 2 + 3
    })
  }
  
  return dataPoints
}

const updateChartData = (data) => {
  // 计算统计数据
  totalWorkouts.value = data.reduce((sum, item) => sum + item.workouts, 0)
  totalDuration.value = data.reduce((sum, item) => sum + item.duration, 0)
  totalCalories.value = data.reduce((sum, item) => sum + item.calories, 0)
  avgIntensity.value = (data.reduce((sum, item) => sum + item.intensity, 0) / data.length).toFixed(1)
  
  // 计算变化（模拟）
  workoutChange.value = Math.floor(Math.random() * 20) - 5
  durationChange.value = Math.floor(Math.random() * 15) - 3
  caloriesChange.value = Math.floor(Math.random() * 25) - 8
  intensityChange.value = Math.floor(Math.random() * 10) - 2
  
  // 更新图表
  nextTick(() => {
    drawTrendChart(data)
    drawIntensityChart(data)
    drawTypeStatsChart(data)
    drawProgressChart(data)
  })
}

const drawTrendChart = (data) => {
  if (!trendChart.value) return
  
  if (chartInstances.trend) {
    chartInstances.trend.dispose()
  }
  
  const chart = echarts.init(trendChart.value)
  chartInstances.trend = chart
  
  const dates = data.map(item => item.date)
  const workouts = data.map(item => item.workouts)
  const calories = data.map(item => item.calories)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['训练次数', '消耗卡路里']
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        formatter: (value) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '训练次数',
        position: 'left'
      },
      {
        type: 'value',
        name: '卡路里',
        position: 'right'
      }
    ],
    series: [
      {
        name: '训练次数',
        type: 'line',
        data: workouts,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '消耗卡路里',
        type: 'line',
        yAxisIndex: 1,
        data: calories,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  chart.setOption(option)
}

const drawIntensityChart = (data) => {
  if (!intensityChart.value) return
  
  if (chartInstances.intensity) {
    chartInstances.intensity.dispose()
  }
  
  const chart = echarts.init(intensityChart.value)
  chartInstances.intensity = chart
  
  // 计算强度分布
  const intensityRanges = {
    '低强度 (1-2)': 0,
    '中强度 (3-4)': 0,
    '高强度 (5)': 0
  }
  
  data.forEach(item => {
    if (item.intensity <= 2) intensityRanges['低强度 (1-2)']++
    else if (item.intensity <= 4) intensityRanges['中强度 (3-4)']++
    else intensityRanges['高强度 (5)']++
  })
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '强度分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: Object.entries(intensityRanges).map(([name, value]) => ({ name, value }))
      }
    ]
  }
  
  chart.setOption(option)
}

const drawTypeStatsChart = (data) => {
  if (!typeStatsChart.value) return
  
  if (chartInstances.typeStats) {
    chartInstances.typeStats.dispose()
  }
  
  const chart = echarts.init(typeStatsChart.value)
  chartInstances.typeStats = chart
  
  // 模拟类型数据
  const typeData = [
    { name: '力量训练', value: Math.floor(Math.random() * 20) + 10 },
    { name: '有氧训练', value: Math.floor(Math.random() * 25) + 15 },
    { name: '柔韧训练', value: Math.floor(Math.random() * 15) + 5 }
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: typeData.map(item => item.name)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '训练次数',
        type: 'bar',
        data: typeData.map(item => item.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  
  chart.setOption(option)
}

const drawProgressChart = (data) => {
  if (!progressChart.value) return
  
  if (chartInstances.progress) {
    chartInstances.progress.dispose()
  }
  
  const chart = echarts.init(progressChart.value)
  chartInstances.progress = chart
  
  // 计算累计数据
  let cumulativeWorkouts = 0
  let cumulativeCalories = 0
  
  const progressData = data.map((item, index) => {
    cumulativeWorkouts += item.workouts
    cumulativeCalories += item.calories
    return {
      date: item.date,
      workouts: cumulativeWorkouts,
      calories: cumulativeCalories
    }
  })
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['累计训练次数', '累计卡路里']
    },
    xAxis: {
      type: 'category',
      data: progressData.map(item => item.date),
      axisLabel: {
        formatter: (value) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '训练次数',
        position: 'left'
      },
      {
        type: 'value',
        name: '卡路里',
        position: 'right'
      }
    ],
    series: [
      {
        name: '累计训练次数',
        type: 'line',
        data: progressData.map(item => item.workouts),
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#ff6b6b'
        }
      },
      {
        name: '累计卡路里',
        type: 'line',
        yAxisIndex: 1,
        data: progressData.map(item => item.calories),
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#4ecdc4'
        }
      }
    ]
  }
  
  chart.setOption(option)
}

const onTimeRangeChange = () => {
  refreshCharts()
}

// 窗口大小变化时重新绘制图表
const handleResize = () => {
  Object.values(chartInstances).forEach(chart => {
    if (chart) {
      chart.resize()
    }
  })
}

// 监听图表类型变化
watch(selectedChart, () => {
  nextTick(() => {
    handleResize()
  })
})

// 生命周期
onMounted(() => {
  refreshCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  Object.values(chartInstances).forEach(chart => {
    if (chart) {
      chart.dispose()
    }
  })
  window.removeEventListener('resize', handleResize)
  dataCache.clear()
})
</script>

<style scoped>
.data-visualization {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.section-actions {
  display: flex;
  align-items: center;
}

.time-range-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.charts-container {
  margin-bottom: 30px;
  min-height: 400px;
}

.chart-wrapper {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.chart-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 6px;
}

.chart {
  width: 100%;
  height: 350px;
}

.stats-cards {
  margin-top: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e4e7ed;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 24px;
}

.stats-content {
  flex: 1;
}

.stats-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stats-change {
  font-size: 12px;
  font-weight: 600;
}

.stats-change.positive {
  color: #67c23a;
}

.stats-change.negative {
  color: #f56c6c;
}

:deep(.el-radio-button__inner) {
  padding: 8px 16px;
}

:deep(.el-loading-mask) {
  border-radius: 8px;
}
</style>