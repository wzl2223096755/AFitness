<template>
  <div class="analytics-section">
    <div class="section-header">
      <h2 class="section-title">
        <span class="title-icon">📈</span>
        数据分析
      </h2>
      <div class="header-controls">
        <div class="time-range-selector">
          <el-radio-group v-model="chartTimeRange" @change="updateCharts" size="small">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="year">全年</el-radio-button>
          </el-radio-group>
        </div>
        <div class="chart-controls">
          <el-tooltip content="导出数据" placement="top">
            <el-button circle size="small" @click="exportData">
              <el-icon><Download /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="全屏查看" placement="top">
            <el-button circle size="small" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="刷新图表" placement="top">
            <el-button circle size="small" @click="updateCharts" :loading="loading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 统计摘要卡片 -->
    <div class="summary-cards">
      <div class="summary-card">
        <div class="card-icon">
          <TrendCharts />
        </div>
        <div class="card-content">
          <div class="card-value">{{ totalWorkouts }}</div>
          <div class="card-label">总训练次数</div>
          <div class="card-trend positive">
            <el-icon><CaretTop /></el-icon>
            +{{ workoutIncrease }}%
          </div>
        </div>
      </div>
      <div class="summary-card">
        <div class="card-icon">
          <Histogram />
        </div>
        <div class="card-content">
          <div class="card-value">{{ totalVolume }}</div>
          <div class="card-label">总训练量(kg)</div>
          <div class="card-trend positive">
            <el-icon><CaretTop /></el-icon>
            +{{ volumeIncrease }}%
          </div>
        </div>
      </div>
      <div class="summary-card">
        <div class="card-icon">
          <Timer />
        </div>
        <div class="card-content">
          <div class="card-value">{{ totalDuration }}</div>
          <div class="card-label">总时长(分钟)</div>
          <div class="card-trend neutral">
            <el-icon><Minus /></el-icon>
            {{ durationChange }}%
          </div>
        </div>
      </div>
      <div class="summary-card">
        <div class="card-icon">
          <Aim />
        </div>
        <div class="card-content">
          <div class="card-value">{{ avgIntensity }}</div>
          <div class="card-label">平均强度</div>
          <div class="card-trend positive">
            <el-icon><CaretTop /></el-icon>
            +{{ intensityIncrease }}%
          </div>
        </div>
      </div>
    </div>

    <div class="analytics-grid" :class="{ 'fullscreen': isFullscreen }">
      <!-- 主要趋势图 -->
      <div class="chart-card main-chart">
        <div class="chart-header">
          <h3>训练量趋势分析</h3>
          <div class="chart-actions">
            <el-select v-model="volumeChartType" size="small" @change="updateVolumeChart">
              <el-option label="折线图" value="line" />
              <el-option label="柱状图" value="bar" />
              <el-option label="面积图" value="area" />
            </el-select>
          </div>
        </div>
        <div ref="volumeTrendChart" class="chart-content large-chart" v-loading="loading"></div>
      </div>

      <!-- 辅助图表 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>动作分布</h3>
          <div class="chart-actions">
            <el-select v-model="distributionChartType" size="small" @change="updateDistributionChart">
              <el-option label="饼图" value="pie" />
              <el-option label="环形图" value="doughnut" />
              <el-option label="玫瑰图" value="rose" />
            </el-select>
          </div>
        </div>
        <div ref="exerciseDistributionChart" class="chart-content" v-loading="loading"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>肌群训练频率</h3>
          <div class="chart-actions">
            <el-select v-model="muscleChartType" size="small" @change="updateMuscleChart">
              <el-option label="柱状图" value="bar" />
              <el-option label="雷达图" value="radar" />
              <el-option label="极坐标图" value="polar" />
            </el-select>
          </div>
        </div>
        <div ref="muscleGroupChart" class="chart-content" v-loading="loading"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>训练强度分析</h3>
          <div class="chart-actions">
            <el-select v-model="intensityChartType" size="small" @change="updateIntensityChart">
              <el-option label="热力图" value="heatmap" />
              <el-option label="散点图" value="scatter" />
              <el-option label="气泡图" value="bubble" />
            </el-select>
          </div>
        </div>
        <div ref="intensityChart" class="chart-content" v-loading="loading"></div>
      </div>

      <!-- 新增图表 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>身体指标变化</h3>
          <div class="chart-actions">
            <el-select v-model="bodyMetricsType" size="small" @change="updateBodyMetricsChart">
              <el-option label="体重" value="weight" />
              <el-option label="体脂率" value="bodyFat" />
              <el-option label="肌肉量" value="muscle" />
            </el-select>
          </div>
        </div>
        <div ref="bodyMetricsChart" class="chart-content" v-loading="loading"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>训练表现评分</h3>
          <div class="chart-actions">
            <el-tooltip content="查看详细评分" placement="top">
              <el-button circle size="small" @click="showPerformanceDetails">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
        <div ref="performanceChart" class="chart-content" v-loading="loading"></div>
      </div>
    </div>

    <!-- 性能详情模态框 -->
    <div v-if="showPerformanceModal" class="modal-overlay" @click="closePerformanceModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>训练表现详细分析</h3>
          <el-button circle size="small" @click="closePerformanceModal">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="performance-details">
          <div class="performance-item" v-for="item in performanceDetails" :key="item.name">
            <div class="performance-label">{{ item.name }}</div>
            <div class="performance-score">
              <el-progress 
                :percentage="item.score" 
                :color="getPerformanceColor(item.score)"
                :stroke-width="8"
              />
              <span class="score-text">{{ item.score }}分</span>
            </div>
            <div class="performance-desc">{{ item.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { 
  Refresh, Download, FullScreen, TrendCharts, Histogram, Timer, Aim,
  CaretTop, CaretBottom, Minus, View, Close
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import echarts from '../utils/echarts'
import { fitnessApi } from '../api/fitness'

// 响应式数据
const loading = ref(false)
const chartTimeRange = ref('week')
const volumeChartType = ref('line')
const distributionChartType = ref('pie')
const muscleChartType = ref('bar')
const intensityChartType = ref('heatmap')
const bodyMetricsType = ref('weight')
const isFullscreen = ref(false)
const showPerformanceModal = ref(false)

// 图表引用
const volumeTrendChart = ref(null)
const exerciseDistributionChart = ref(null)
const muscleGroupChart = ref(null)
const intensityChart = ref(null)
const bodyMetricsChart = ref(null)
const performanceChart = ref(null)

// 图表实例
let volumeChartInstance = null
let distributionChartInstance = null
let muscleChartInstance = null
let intensityChartInstance = null
let bodyMetricsChartInstance = null
let performanceChartInstance = null

// 统计数据
const totalWorkouts = ref(156)
const totalVolume = ref(284750)
const totalDuration = ref(2850)
const avgIntensity = ref(7.8)
const workoutIncrease = ref(12)
const volumeIncrease = ref(8)
const durationChange = ref(2)
const intensityIncrease = ref(15)

// 性能详情数据
const performanceDetails = ref([
  { name: '力量表现', score: 85, description: '您的力量水平在持续提升，继续保持当前训练强度' },
  { name: '耐力水平', score: 72, description: '耐力表现良好，建议增加有氧训练频率' },
  { name: '柔韧性', score: 68, description: '柔韧性有待提高，建议增加拉伸训练' },
  { name: '爆发力', score: 90, description: '爆发力表现优秀，是您的优势项目' },
  { name: '恢复能力', score: 78, description: '恢复能力良好，注意保持充足睡眠' },
  { name: '技术动作', score: 82, description: '动作标准性较高，继续保持' }
])

// 方法
const updateCharts = async () => {
  loading.value = true
  try {
    await nextTick()
    // 等待DOM完全渲染
    setTimeout(async () => {
      initVolumeTrendChart()
      initExerciseDistributionChart()
      initMuscleGroupChart()
      initIntensityChart()
      initBodyMetricsChart()
      initPerformanceChart()
      await updateSummaryData()
    }, 100)
  } finally {
    setTimeout(() => {
      loading.value = false
    }, 1000)
  }
}

const updateVolumeChart = () => {
  initVolumeTrendChart()
}

const updateDistributionChart = () => {
  initExerciseDistributionChart()
}

const updateMuscleChart = () => {
  initMuscleGroupChart()
}

const updateIntensityChart = () => {
  initIntensityChart()
}

const updateBodyMetricsChart = () => {
  initBodyMetricsChart()
}

const updateSummaryData = async () => {
  try {
    // 从API获取分析数据
    const response = await fitnessApi.getAnalyticsData(chartTimeRange.value)
    
    if (response.data) {
      // 更新统计数据
      totalWorkouts.value = response.data.totalWorkouts || 0
      totalVolume.value = response.data.totalVolume || 0
      totalDuration.value = response.data.totalDuration || 0
      avgIntensity.value = response.data.avgIntensity || 0
      workoutIncrease.value = response.data.workoutIncrease || 0
      volumeIncrease.value = response.data.volumeIncrease || 0
      durationChange.value = response.data.durationChange || 0
      intensityIncrease.value = response.data.intensityIncrease || 0
      
      // 更新性能详情数据
      if (response.data.performanceDetails && Array.isArray(response.data.performanceDetails)) {
        performanceDetails.value = response.data.performanceDetails
      }
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('获取分析数据失败:', error)
    ElMessage.error('获取分析数据失败，请稍后重试')
    
    // 如果API调用失败，使用默认值避免页面空白
    totalWorkouts.value = 0
    totalVolume.value = 0
    totalDuration.value = 0
    avgIntensity.value = 0
    workoutIncrease.value = 0
    volumeIncrease.value = 0
    durationChange.value = 0
    intensityIncrease.value = 0
  }
}

const exportData = () => {
  // 导出数据功能
  const data = {
    timeRange: chartTimeRange.value,
    summary: {
      totalWorkouts: totalWorkouts.value,
      totalVolume: totalVolume.value,
      totalDuration: totalDuration.value,
      avgIntensity: avgIntensity.value
    },
    exportTime: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `fitness-analytics-${chartTimeRange.value}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('数据导出成功')
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  nextTick(() => {
    volumeChartInstance?.resize()
    distributionChartInstance?.resize()
    muscleChartInstance?.resize()
    intensityChartInstance?.resize()
    bodyMetricsChartInstance?.resize()
    performanceChartInstance?.resize()
  })
}

const showPerformanceDetails = () => {
  showPerformanceModal.value = true
}

const closePerformanceModal = () => {
  showPerformanceModal.value = false
}

const getPerformanceColor = (score) => {
  if (score >= 80) return '#10b981'
  if (score >= 60) return '#f59e0b'
  return '#ef4444'
}

// 图表初始化方法
const initVolumeTrendChart = () => {
  try {
    if (!volumeTrendChart.value) {
      console.warn('Volume trend chart container not found')
      return
    }
    
    if (volumeChartInstance) {
      volumeChartInstance.dispose()
    }
    
    volumeChartInstance = echarts.init(volumeTrendChart.value)
  
  const option = {
    title: {
      text: '训练量变化趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['训练量', '目标'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: generateDateLabels()
    },
    yAxis: {
      type: 'value',
      name: '训练量 (kg)'
    },
    series: [
      {
        name: '训练量',
        type: volumeChartType.value === 'area' ? 'line' : volumeChartType.value,
        smooth: true,
        data: generateVolumeData(),
        itemStyle: {
          color: '#3b82f6'
        },
        ...(volumeChartType.value === 'area' ? {
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.1)' }
            ])
          }
        } : {})
      },
      {
        name: '目标',
        type: 'line',
        smooth: true,
        data: generateTargetData(),
        itemStyle: {
          color: '#10b981'
        },
        lineStyle: {
          type: 'dashed'
        }
      }
    ]
  }
  
  volumeChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize volume trend chart:', error)
  }
}

const initExerciseDistributionChart = () => {
  try {
    if (!exerciseDistributionChart.value) {
      console.warn('Exercise distribution chart container not found')
      return
    }
    
    if (distributionChartInstance) {
      distributionChartInstance.dispose()
    }
    
    distributionChartInstance = echarts.init(exerciseDistributionChart.value)
  
  const option = {
    title: {
      text: '动作类型分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: '动作类型',
        type: distributionChartType.value === 'rose' ? 'pie' : distributionChartType.value,
        radius: distributionChartType.value === 'doughnut' ? ['40%', '70%'] : '70%',
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: distributionChartType.value === 'pie',
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        ...(distributionChartType.value === 'rose' ? {
          roseType: 'radius'
        } : {}),
        data: [
          { value: 35, name: '深蹲', itemStyle: { color: '#3b82f6' } },
          { value: 25, name: '卧推', itemStyle: { color: '#10b981' } },
          { value: 20, name: '硬拉', itemStyle: { color: '#f59e0b' } },
          { value: 15, name: '引体向上', itemStyle: { color: '#ef4444' } },
          { value: 5, name: '其他', itemStyle: { color: '#8b5cf6' } }
        ]
      }
    ]
  }
  
  distributionChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize exercise distribution chart:', error)
  }
}

const initMuscleGroupChart = () => {
  try {
    if (!muscleGroupChart.value) {
      console.warn('Muscle group chart container not found')
      return
    }
    
    if (muscleChartInstance) {
      muscleChartInstance.dispose()
    }
    
    muscleChartInstance = echarts.init(muscleGroupChart.value)
  
  const option = muscleChartType.value === 'radar' ? {
    title: {
      text: '肌群训练频率',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {},
    legend: {
      data: ['本周', '上周'],
      top: 30
    },
    radar: {
      indicator: [
        { name: '胸部', max: 100 },
        { name: '背部', max: 100 },
        { name: '腿部', max: 100 },
        { name: '肩部', max: 100 },
        { name: '手臂', max: 100 },
        { name: '核心', max: 100 }
      ]
    },
    series: [
      {
        name: '肌群训练频率',
        type: 'radar',
        data: [
          {
            value: [80, 70, 90, 60, 75, 85],
            name: '本周',
            itemStyle: { color: '#3b82f6' }
          },
          {
            value: [70, 65, 85, 55, 70, 80],
            name: '上周',
            itemStyle: { color: '#10b981' }
          }
        ]
      }
    ]
  } : muscleChartType.value === 'polar' ? {
    title: {
      text: '肌群训练频率',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {},
    polar: {},
    angleAxis: {
      type: 'category',
      data: ['胸部', '背部', '腿部', '肩部', '手臂', '核心']
    },
    radiusAxis: {},
    series: [
      {
        name: '本周',
        type: 'bar',
        coordinateSystem: 'polar',
        data: [80, 70, 90, 60, 75, 85],
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '上周',
        type: 'bar',
        coordinateSystem: 'polar',
        data: [70, 65, 85, 55, 70, 80],
        itemStyle: { color: '#10b981' }
      }
    ]
  } : {
    title: {
      text: '肌群训练频率',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['胸部', '背部', '腿部', '肩部', '手臂', '核心']
    },
    yAxis: {
      type: 'value',
      name: '训练频率'
    },
    series: [
      {
        name: '本周',
        type: 'bar',
        data: [80, 70, 90, 60, 75, 85],
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '上周',
        type: 'bar',
        data: [70, 65, 85, 55, 70, 80],
        itemStyle: { color: '#10b981' }
      }
    ]
  }
  
  muscleChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize muscle group chart:', error)
  }
}

const initIntensityChart = () => {
  try {
    if (!intensityChart.value) {
      console.warn('Intensity chart container not found')
      return
    }
    
    if (intensityChartInstance) {
      intensityChartInstance.dispose()
    }
    
    intensityChartInstance = echarts.init(intensityChart.value)
  
  const option = intensityChartType.value === 'heatmap' ? {
    title: {
      text: '训练强度热力图',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '50%',
      top: '20%'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: ['上午', '下午', '晚上'],
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 10,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%'
    },
    series: [
      {
        name: '训练强度',
        type: 'heatmap',
        data: generateHeatmapData(),
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  } : intensityChartType.value === 'bubble' ? {
    title: {
      text: '训练强度气泡图',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        return `强度: ${params.value[1]}kg<br>时长: ${params.value[0]}分钟<br>频次: ${params.value[2]}次`
      }
    },
    xAxis: {
      type: 'value',
      name: '训练时长 (分钟)',
      min: 0,
      max: 120
    },
    yAxis: {
      type: 'value',
      name: '平均重量 (kg)',
      min: 0,
      max: 200
    },
    series: [
      {
        name: '训练强度',
        type: 'scatter',
        data: generateScatterData(),
        symbolSize: function (data) {
          return Math.sqrt(data[2]) * 3
        },
        itemStyle: {
          color: '#3b82f6',
          opacity: 0.8
        }
      }
    ]
  } : {
    title: {
      text: '训练强度分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        return `强度: ${params.value[1]}kg<br>时长: ${params.value[0]}分钟`
      }
    },
    xAxis: {
      type: 'value',
      name: '训练时长 (分钟)',
      min: 0,
      max: 120
    },
    yAxis: {
      type: 'value',
      name: '平均重量 (kg)',
      min: 0,
      max: 200
    },
    series: [
      {
        name: '训练强度',
        type: 'scatter',
        data: generateScatterData(),
        symbolSize: function (data) {
          return Math.sqrt(data[2]) * 2
        },
        itemStyle: {
          color: '#3b82f6',
          opacity: 0.8
        }
      }
    ]
  }
  
  intensityChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize intensity chart:', error)
  }
}

const initBodyMetricsChart = () => {
  try {
    if (!bodyMetricsChart.value) {
      console.warn('Body metrics chart container not found')
      return
    }
    
    if (bodyMetricsChartInstance) {
      bodyMetricsChartInstance.dispose()
    }
    
    bodyMetricsChartInstance = echarts.init(bodyMetricsChart.value)
  
  const option = {
    title: {
      text: `${bodyMetricsType.value === 'weight' ? '体重' : bodyMetricsType.value === 'bodyFat' ? '体脂率' : '肌肉量'}变化趋势`,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: generateDateLabels()
    },
    yAxis: {
      type: 'value',
      name: bodyMetricsType.value === 'weight' ? '体重 (kg)' : bodyMetricsType.value === 'bodyFat' ? '体脂率 (%)' : '肌肉量 (kg)'
    },
    series: [
      {
        name: bodyMetricsType.value === 'weight' ? '体重' : bodyMetricsType.value === 'bodyFat' ? '体脂率' : '肌肉量',
        type: 'line',
        smooth: true,
        data: generateBodyMetricsData(),
        itemStyle: {
          color: bodyMetricsType.value === 'weight' ? '#8b5cf6' : bodyMetricsType.value === 'bodyFat' ? '#f59e0b' : '#10b981'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: bodyMetricsType.value === 'weight' ? 'rgba(139, 92, 246, 0.3)' : bodyMetricsType.value === 'bodyFat' ? 'rgba(245, 158, 11, 0.3)' : 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: bodyMetricsType.value === 'weight' ? 'rgba(139, 92, 246, 0.1)' : bodyMetricsType.value === 'bodyFat' ? 'rgba(245, 158, 11, 0.1)' : 'rgba(16, 185, 129, 0.1)' }
          ])
        }
      }
    ]
  }
  
  bodyMetricsChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize body metrics chart:', error)
  }
}

const initPerformanceChart = () => {
  try {
    if (!performanceChart.value) {
      console.warn('Performance chart container not found')
      return
    }
    
    if (performanceChartInstance) {
      performanceChartInstance.dispose()
    }
    
    performanceChartInstance = echarts.init(performanceChart.value)
  
  const option = {
    title: {
      text: '综合表现评分',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: [
        { name: '力量表现', max: 100 },
        { name: '耐力水平', max: 100 },
        { name: '柔韧性', max: 100 },
        { name: '爆发力', max: 100 },
        { name: '恢复能力', max: 100 },
        { name: '技术动作', max: 100 }
      ]
    },
    series: [
      {
        name: '表现评分',
        type: 'radar',
        data: [
          {
            value: [85, 72, 68, 90, 78, 82],
            name: '当前水平',
            itemStyle: { color: '#3b82f6' },
            areaStyle: {
              color: 'rgba(59, 130, 246, 0.2)'
            }
          },
          {
            value: [75, 70, 65, 80, 75, 75],
            name: '平均水平',
            itemStyle: { color: '#10b981' },
            areaStyle: {
              color: 'rgba(16, 185, 129, 0.1)'
            }
          }
        ]
      }
    ]
  }
  
  performanceChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to initialize performance chart:', error)
  }
}

// 数据生成函数
const generateDateLabels = () => {
  const labels = []
  const days = chartTimeRange.value === 'week' ? 7 : chartTimeRange.value === 'month' ? 30 : 365
  const today = new Date()
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    labels.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
  }
  
  return labels
}

const generateVolumeData = () => {
  const days = chartTimeRange.value === 'week' ? 7 : chartTimeRange.value === 'month' ? 30 : 365
  return Array.from({ length: days }, () => Math.floor(Math.random() * 5000) + 2000)
}

const generateTargetData = () => {
  const days = chartTimeRange.value === 'week' ? 7 : chartTimeRange.value === 'month' ? 30 : 365
  return Array.from({ length: days }, () => 3500)
}

const generateHeatmapData = () => {
  const data = []
  for (let i = 0; i < 7; i++) {
    for (let j = 0; j < 3; j++) {
      data.push([i, j, Math.floor(Math.random() * 10)])
    }
  }
  return data
}

const generateScatterData = () => {
  return Array.from({ length: 50 }, () => [
    Math.floor(Math.random() * 120) + 10,
    Math.floor(Math.random() * 180) + 20,
    Math.floor(Math.random() * 100) + 10
  ])
}

const generateBodyMetricsData = () => {
  const days = chartTimeRange.value === 'week' ? 7 : chartTimeRange.value === 'month' ? 30 : 365
  const baseValue = bodyMetricsType.value === 'weight' ? 70 : bodyMetricsType.value === 'bodyFat' ? 15 : 35
  return Array.from({ length: days }, (_, i) => {
    const trend = i * 0.1 // 轻微趋势
    const noise = (Math.random() - 0.5) * 2 // 随机波动
    return Math.max(0, baseValue + trend + noise)
  })
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    updateCharts()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    volumeChartInstance?.resize()
    distributionChartInstance?.resize()
    muscleChartInstance?.resize()
    intensityChartInstance?.resize()
    bodyMetricsChartInstance?.resize()
    performanceChartInstance?.resize()
  })
})
</script>

<style scoped>
.analytics-section {
  width: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.6s ease-out;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 1.3rem;
}

/* 统计摘要卡片 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.card-trend {
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-trend.positive {
  color: #10b981;
}

.card-trend.negative {
  color: #ef4444;
}

.card-trend.neutral {
  color: #6b7280;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto auto;
  gap: 20px;
}

.analytics-grid.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.98);
  z-index: 1000;
  padding: 20px;
  overflow-y: auto;
  backdrop-filter: blur(20px);
}

.chart-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.chart-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 100%);
}

.main-chart {
  grid-column: 1;
  grid-row: 1 / span 2;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.chart-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chart-content {
  width: 100%;
  height: 300px;
}

.large-chart {
  height: 600px;
}

/* 性能详情模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: white;
  padding: 32px;
  border-radius: 16px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}

.performance-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.performance-item {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.performance-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.performance-label {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.performance-score {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.score-text {
  font-weight: bold;
  font-size: 18px;
  color: #1f2937;
}

.performance-desc {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

/* 动画 */
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

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .analytics-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto auto;
  }
  
  .main-chart {
    grid-column: 1 / span 2;
    grid-row: 1;
  }
}

@media (max-width: 768px) {
  .analytics-section {
    padding: 16px;
  }
  
  .analytics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .main-chart {
    grid-column: 1;
    grid-row: 1;
  }
  
  .chart-card {
    padding: 20px;
  }
  
  .chart-content {
    height: 250px;
  }
  
  .large-chart {
    height: 400px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .summary-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .modal-content {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .analytics-section {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  }
  
  .section-title {
    color: #f1f5f9;
  }
  
  .chart-card {
    background: rgba(30, 41, 59, 0.95);
  }
  
  .summary-card {
    background: rgba(30, 41, 59, 0.95);
  }
  
  .chart-header h3 {
    color: #f1f5f9;
  }
  
  .card-value {
    color: #f1f5f9;
  }
  
  .card-label {
    color: #94a3b8;
  }
}
</style>