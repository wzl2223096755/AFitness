<template>
  <div class="metrics-overview">
    <div class="section-header mb-6">
      <h2 class="section-title responsive-h2 font-bold">
        <span class="title-icon icon-2xl">📊</span>
        核心指标概览
      </h2>
      <div class="section-actions gap-3">
        <el-select v-model="timeRange" @change="refreshMetrics" size="small" class="time-selector">
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本年" value="year" />
        </el-select>
        <el-button text @click="refreshMetrics(true)" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <div class="metrics-grid auto-grid">
      <!-- 主要指标卡片 -->
      <div class="metric-card primary-metric p-5" @click="navigateTo('training-data')" :class="{ 'loading': loading }">
        <div class="metric-header mb-5">
          <div class="metric-info">
            <h3 class="metric-title text-lg font-bold leading-snug">{{ timeRangeLabels[timeRange].training }}</h3>
            <p class="metric-subtitle text-sm leading-normal">保持规律训练</p>
          </div>
          <div class="metric-icon primary">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="metric-value mb-4">
          <span v-if="loading" class="loading-placeholder">...</span>
          <span v-else class="value-number data-value animated-number">{{ weeklyTrainingCount }}</span>
          <span class="value-unit data-unit">次</span>
        </div>
        <div class="metric-trend" :class="weeklyChangeClass">
          <el-icon v-if="weeklyChange > 0"><CaretTop /></el-icon>
          <el-icon v-else-if="weeklyChange < 0"><CaretBottom /></el-icon>
          <span>{{ weeklyChangeText }}</span>
        </div>
        <div class="metric-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${(weeklyTrainingCount / 7) * 100}%` }"></div>
          </div>
          <span class="progress-text">目标: 7次</span>
        </div>
      </div>

      <div class="metric-card success-metric p-5" @click="navigateTo('load-analysis')" :class="{ 'loading': loading }">
        <div class="metric-header mb-5">
          <div class="metric-info">
            <h3 class="metric-title text-lg font-bold leading-snug">{{ timeRangeLabels[timeRange].volume }}</h3>
            <p class="metric-subtitle text-sm leading-normal">累计负荷</p>
          </div>
          <div class="metric-icon success">
            <el-icon><Histogram /></el-icon>
          </div>
        </div>
        <div class="metric-value mb-4">
          <span v-if="loading" class="loading-placeholder">...</span>
          <span v-else class="value-number data-value animated-number">{{ formatNumber(totalVolume) }}</span>
          <span class="value-unit data-unit">kg</span>
        </div>
        <div class="metric-trend positive">
          <el-icon><CaretTop /></el-icon>
          <span>持续增长</span>
        </div>
        <div class="metric-sparkline">
          <canvas ref="volumeSparkline" width="100" height="30"></canvas>
        </div>
      </div>

      <div class="metric-card warning-metric p-5" @click="showRecoveryModal = true" :class="{ 'loading': loading }">
        <div class="metric-header mb-5">
          <div class="metric-info">
            <h3 class="metric-title text-lg font-bold leading-snug">恢复状态</h3>
            <p class="metric-subtitle text-sm leading-normal">身体恢复评估</p>
          </div>
          <div class="metric-icon warning" :class="{ 'pulse': recoveryScore < 60 }">
            <el-icon><Timer /></el-icon>
          </div>
        </div>
        <div class="metric-value mb-4">
          <span v-if="loading" class="loading-placeholder">...</span>
          <span v-else class="value-number data-value animated-number">{{ recoveryScore }}</span>
          <span class="value-unit data-unit">分</span>
        </div>
        <div class="metric-trend" :class="recoveryScore >= 80 ? 'positive' : recoveryScore >= 60 ? 'neutral' : 'negative'">
          <span>{{ recoveryStatus }}</span>
        </div>
        <div class="recovery-gauge">
          <div class="gauge-container">
            <div class="gauge-fill" :style="{ transform: `rotate(${(recoveryScore / 100) * 180 - 90}deg)` }"></div>
          </div>
        </div>
      </div>

      <div class="metric-card danger-metric p-5" @click="showGoalModal = true" :class="{ 'loading': loading }">
        <div class="metric-header mb-5">
          <div class="metric-info">
            <h3 class="metric-title text-lg font-bold leading-snug">{{ timeRangeLabels[timeRange].goals }}</h3>
            <p class="metric-subtitle text-sm leading-normal">完成率</p>
          </div>
          <div class="metric-icon danger">
            <el-icon><Aim /></el-icon>
          </div>
        </div>
        <div class="metric-value mb-4">
          <span v-if="loading" class="loading-placeholder">...</span>
          <span v-else class="value-number data-value animated-number">{{ goalCompletionRate }}</span>
          <span class="value-unit data-unit">%</span>
        </div>
        <div class="metric-trend" :class="goalCompletionRate >= 80 ? 'positive' : 'negative'">
          <span>{{ goalCompletionText }}</span>
        </div>
        <div class="goal-ring">
          <svg width="60" height="60" class="ring-chart">
            <circle cx="30" cy="30" r="25" fill="none" stroke="#e5e7eb" stroke-width="5"/>
            <circle 
              cx="30" cy="30" r="25" 
              fill="none" 
              :stroke="goalCompletionRate >= 80 ? '#10b981' : '#ef4444'" 
              stroke-width="5"
              stroke-linecap="round"
              :stroke-dasharray="`${(goalCompletionRate / 100) * 157} 157`"
              transform="rotate(-90 30 30)"
              class="ring-progress"
            />
          </svg>
        </div>
      </div>
    </div>

    <!-- 恢复状态详情模态框 -->
    <el-dialog v-model="showRecoveryModal" title="恢复状态详情" width="500px">
      <div class="recovery-details">
        <div class="recovery-score-display">
          <div class="score-circle" :class="getRecoveryClass(recoveryScore)">
            {{ recoveryScore }}
          </div>
          <p class="recovery-description">{{ getRecoveryDescription(recoveryScore) }}</p>
        </div>
        <div class="recovery-factors">
          <div class="factor-item">
            <span class="factor-label">睡眠质量</span>
            <el-rate v-model="sleepQuality" disabled show-score text-color="#ff9900"/>
          </div>
          <div class="factor-item">
            <span class="factor-label">肌肉疲劳</span>
            <el-rate v-model="muscleFatigue" disabled show-score text-color="#ff9900"/>
          </div>
          <div class="factor-item">
            <span class="factor-label">精神状态</span>
            <el-rate v-model="mentalState" disabled show-score text-color="#ff9900"/>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 目标详情模态框 -->
    <el-dialog v-model="showGoalModal" title="目标完成情况" width="500px">
      <div class="goal-details">
        <div class="goal-progress-overview">
          <div class="overall-progress">
            <div class="progress-circle">
              <span>{{ goalCompletionRate }}%</span>
            </div>
            <p>{{ timeRangeLabels[timeRange].goals }}完成率</p>
          </div>
        </div>
        <div class="goal-list">
          <div v-for="goal in goals" :key="goal.id" class="goal-item">
            <div class="goal-info">
              <span class="goal-name">{{ goal.name }}</span>
              <span class="goal-progress">{{ goal.progress }}/{{ goal.target }}</span>
            </div>
            <el-progress 
              :percentage="(goal.progress / goal.target) * 100" 
              :color="goal.progress >= goal.target ? '#10b981' : '#3b82f6'"
              :show-text="false"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Refresh, TrendCharts, Histogram, Timer, Aim, 
  CaretTop, CaretBottom 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fitnessApi } from '../api/fitness'

const router = useRouter()

// 优化性能：缓存数据和防抖
const loading = ref(false)
const timeRange = ref('week')
const weeklyTrainingCount = ref(5)
const weeklyChange = ref(2)
const totalVolume = ref(12580)
const recoveryScore = ref(85)
const goalCompletionRate = ref(78)
const showRecoveryModal = ref(false)
const showGoalModal = ref(false)
const volumeSparkline = ref(null)

// 数据缓存
const metricsCache = ref({
  data: null,
  timestamp: 0,
  ttl: 60000 // 1分钟缓存
})

// 防抖定时器
let refreshTimer = null

// 恢复状态相关
const sleepQuality = ref(4)
const muscleFatigue = ref(3)
const mentalState = ref(4)

// 目标数据
const goals = ref([
  { id: 1, name: '训练次数', progress: 5, target: 7 },
  { id: 2, name: '总重量', progress: 12580, target: 15000 },
  { id: 3, name: '有氧时长', progress: 120, target: 150 },
  { id: 4, name: '拉伸次数', progress: 4, target: 7 }
])

// 时间范围标签
const timeRangeLabels = {
  week: {
    training: '本周训练',
    volume: '本周训练量',
    goals: '本周目标'
  },
  month: {
    training: '本月训练',
    volume: '本月训练量',
    goals: '本月目标'
  },
  year: {
    training: '本年训练',
    volume: '本年训练量',
    goals: '本年目标'
  }
}

// 优化计算属性：使用缓存和减少重复计算
const weeklyChangeClass = computed(() => {
  const change = weeklyChange.value
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
})

const weeklyChangeText = computed(() => {
  const rangeText = timeRange.value === 'week' ? '上周' : timeRange.value === 'month' ? '上月' : '去年'
  const change = weeklyChange.value
  if (change > 0) return `+${change}次 vs${rangeText}`
  if (change < 0) return `${change}次 vs${rangeText}`
  return `与${rangeText}持平`
})

const recoveryStatus = computed(() => {
  const score = recoveryScore.value
  if (score >= 80) return '恢复良好'
  if (score >= 60) return '需要休息'
  return '过度疲劳'
})

const goalCompletionText = computed(() => {
  const rate = goalCompletionRate.value
  if (rate >= 80) return '表现优秀'
  if (rate >= 60) return '继续努力'
  return '需要加强'
})

// 优化刷新方法：添加缓存和防抖
const refreshMetrics = async (forceRefresh = false) => {
  const now = Date.now()
  
  // 检查本地缓存（仅用于快速UI响应）
  if (!forceRefresh && 
      metricsCache.value.data && 
      metricsCache.value.timestamp && 
      now - metricsCache.value.timestamp < metricsCache.value.ttl) {
    const cachedData = metricsCache.value.data
    weeklyTrainingCount.value = cachedData.weeklyTrainingCount
    weeklyChange.value = cachedData.weeklyChange
    totalVolume.value = cachedData.totalVolume
    recoveryScore.value = cachedData.recoveryScore
    goalCompletionRate.value = cachedData.goalCompletionRate
    goals.value = [...cachedData.goals]
    sleepQuality.value = cachedData.sleepQuality
    muscleFatigue.value = cachedData.muscleFatigue
    mentalState.value = cachedData.mentalState
    return
  }
  
  loading.value = true
  try {
    // 从API获取指标数据（使用带缓存的API，支持stale-while-revalidate）
    const response = await fitnessApi.getMetricsOverview(timeRange.value, { forceRefresh })
    
    if (response.data) {
      // 更新训练数据
      const newWeeklyTrainingCount = response.data.weeklyTrainingCount || 0
      const newWeeklyChange = response.data.weeklyChange || 0
      const newTotalVolume = response.data.totalVolume || 0
      
      // 更新恢复数据
      const newRecoveryScore = response.data.recoveryScore || 0
      const newSleepQuality = response.data.sleepQuality || 0
      const newMuscleFatigue = response.data.muscleFatigue || 0
      const newMentalState = response.data.mentalState || 0
      
      // 更新目标完成率
      const newGoalCompletionRate = response.data.goalCompletionRate || 0
      
      // 更新目标进度
      let newGoals
      if (response.data.goals && Array.isArray(response.data.goals)) {
        newGoals = response.data.goals
      } else {
        // 如果API没有返回目标数据，使用默认目标
        newGoals = [
          { id: 1, name: '训练次数', progress: newWeeklyTrainingCount, target: 7 },
          { id: 2, name: '总重量', progress: newTotalVolume, target: 15000 },
          { id: 3, name: '有氧时长', progress: 120, target: 150 },
          { id: 4, name: '拉伸次数', progress: 4, target: 7 }
        ]
      }
      
      // 更新数据
      weeklyTrainingCount.value = newWeeklyTrainingCount
      weeklyChange.value = newWeeklyChange
      totalVolume.value = newTotalVolume
      recoveryScore.value = newRecoveryScore
      goalCompletionRate.value = newGoalCompletionRate
      goals.value = newGoals
      sleepQuality.value = newSleepQuality
      muscleFatigue.value = newMuscleFatigue
      mentalState.value = newMentalState
      
      // 缓存数据
      metricsCache.value = {
        data: {
          weeklyTrainingCount: newWeeklyTrainingCount,
          weeklyChange: newWeeklyChange,
          totalVolume: newTotalVolume,
          recoveryScore: newRecoveryScore,
          goalCompletionRate: newGoalCompletionRate,
          goals: [...newGoals],
          sleepQuality: newSleepQuality,
          muscleFatigue: newMuscleFatigue,
          mentalState: newMentalState
        },
        timestamp: now,
        ttl: 60000
      }
      
      ElMessage.success('指标数据已更新')
      
      // 重新绘制图表
      await nextTick()
      drawSparkline()
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('获取指标数据失败:', error)
    ElMessage.error('获取指标数据失败，请稍后重试')
    
    // 如果API调用失败，使用默认值避免页面空白
    const defaultWeeklyTrainingCount = 0
    const defaultWeeklyChange = 0
    const defaultTotalVolume = 0
    const defaultRecoveryScore = 0
    const defaultGoalCompletionRate = 0
    const defaultSleepQuality = 0
    const defaultMuscleFatigue = 0
    const defaultMentalState = 0
    const defaultGoals = [
      { id: 1, name: '训练次数', progress: 0, target: 7 },
      { id: 2, name: '总重量', progress: 0, target: 15000 },
      { id: 3, name: '有氧时长', progress: 0, target: 150 },
      { id: 4, name: '拉伸次数', progress: 0, target: 7 }
    ]
    
    weeklyTrainingCount.value = defaultWeeklyTrainingCount
    weeklyChange.value = defaultWeeklyChange
    totalVolume.value = defaultTotalVolume
    recoveryScore.value = defaultRecoveryScore
    goalCompletionRate.value = defaultGoalCompletionRate
    goals.value = defaultGoals
    sleepQuality.value = defaultSleepQuality
    muscleFatigue.value = defaultMuscleFatigue
    mentalState.value = defaultMentalState
  } finally {
    loading.value = false
  }
}

// 优化导航方法
const navigateTo = (route) => {
  try {
    router.push(`/${route}`)
  } catch (error) {
    console.error('导航失败:', error)
    ElMessage.error('页面跳转失败')
  }
}

// 优化数字格式化
const formatNumber = (num) => {
  try {
    return new Intl.NumberFormat('zh-CN').format(num)
  } catch (error) {
    console.error('数字格式化失败:', error)
    return num.toString()
  }
}

// 优化恢复状态方法
const getRecoveryClass = (score) => {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  return 'poor'
}

const getRecoveryDescription = (score) => {
  const descriptions = {
    excellent: '您的身体恢复状态非常好，可以继续保持当前训练强度。',
    good: '您的身体恢复状态一般，建议适当调整训练强度并增加休息时间。',
    poor: '您的身体恢复状态较差，建议减少训练强度，增加休息和恢复时间。'
  }
  
  const status = score >= 80 ? 'excellent' : score >= 60 ? 'good' : 'poor'
  return descriptions[status]
}

// 优化图表绘制方法
const drawSparkline = () => {
  if (!volumeSparkline.value) return
  
  try {
    const canvas = volumeSparkline.value
    const ctx = canvas.getContext('2d')
    const width = canvas.width
    const height = canvas.height
    
    // 清除画布
    ctx.clearRect(0, 0, width, height)
    
    // 生成随机数据点
    const dataPoints = []
    for (let i = 0; i < 10; i++) {
      dataPoints.push(Math.random() * height)
    }
    
    // 绘制折线图
    ctx.strokeStyle = '#10b981'
    ctx.lineWidth = 2
    ctx.beginPath()
    
    dataPoints.forEach((point, index) => {
      const x = (index / (dataPoints.length - 1)) * width
      const y = height - point
      
      if (index === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    
    ctx.stroke()
    
    // 绘制填充区域
    ctx.fillStyle = 'rgba(16, 185, 129, 0.1)'
    ctx.lineTo(width, height)
    ctx.lineTo(0, height)
    ctx.closePath()
    ctx.fill()
  } catch (error) {
    console.error('绘制图表失败:', error)
  }
}

// 优化监听器：添加防抖
const debouncedRefresh = () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
  refreshTimer = setTimeout(() => {
    refreshMetrics()
  }, 300)
}

// 监听时间范围变化
watch(timeRange, debouncedRefresh)

onMounted(() => {
  refreshMetrics()
})

// 清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
})
</script>

<style scoped>
/* MetricsOverview - Using design tokens from typography and layout system */
/* Requirements: 3.2, 6.1, 6.2 */

.metrics-overview {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  color: var(--text-primary, #1e293b);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-2, 0.5rem);
}

.title-icon {
  display: inline-flex;
  align-items: center;
}

.section-actions {
  display: flex;
}

.time-range-selector {
  display: flex;
  gap: var(--spacing-2, 0.5rem);
}

.time-range-btn {
  padding: var(--spacing-2, 0.5rem) var(--spacing-4, 1rem);
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: var(--font-size-sm, 0.875rem);
  color: #6b7280;
}

.time-range-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.time-range-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

/* Metrics grid uses auto-grid pattern from _grid.scss */
/* Requirements: 3.2 - auto-fit grid with minimum card width of 280px */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--gutter-md, 1.25rem);
}

/* Metric card - Requirements: 6.1, 6.2 - consistent internal padding (20-28px) */
.metric-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--metric-color) 0%, var(--metric-color-light) 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.metric-card:hover::before {
  transform: scaleX(1);
}

.primary-metric {
  --metric-color: #3b82f6;
  --metric-color-light: #60a5fa;
}

.success-metric {
  --metric-color: #10b981;
  --metric-color-light: #34d399;
}

.warning-metric {
  --metric-color: #f59e0b;
  --metric-color-light: #fbbf24;
}

.danger-metric {
  --metric-color: #ef4444;
  --metric-color-light: #f87171;
}

.metric-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.metric-info {
  flex: 1;
}

/* Using typography classes from _typography.scss */
.metric-title {
  color: var(--text-primary, #1e293b);
  margin: 0 0 var(--spacing-1, 0.25rem) 0;
}

.metric-subtitle {
  color: var(--text-secondary, #64748b);
  margin: 0;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl, 1.25rem);
  color: white;
  flex-shrink: 0;
}

.metric-icon.primary {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
}

.metric-icon.success {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.metric-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.metric-icon.danger {
  background: linear-gradient(135deg, #ef4444, #f87171);
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-2, 0.5rem);
}

/* Data value styling - Requirements: 5.4 - larger font sizes and bolder weights */
.value-number {
  font-variant-numeric: tabular-nums;
  line-height: var(--line-height-tight, 1.25);
  color: var(--text-primary, #1e293b);
}

.value-unit {
  color: var(--text-secondary, #64748b);
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: var(--spacing-1, 0.25rem);
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: var(--font-weight-semibold, 600);
  padding: var(--spacing-1, 0.25rem) var(--spacing-3, 0.75rem);
  border-radius: 8px;
  width: fit-content;
}

.metric-trend.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.metric-trend.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.metric-trend.neutral {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
}

.loading-placeholder {
  display: inline-block;
  width: 60px;
  height: 32px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Additional metric styles using design tokens */
.metric-label {
  font-size: var(--font-size-xs, 0.8rem);
  color: #9ca3af;
  margin-bottom: var(--spacing-1, 0.25rem);
  text-transform: uppercase;
  letter-spacing: var(--letter-spacing-wide, 0.025em);
}

.metric-sparkline {
  margin-top: var(--spacing-3, 0.75rem);
  height: 40px;
  border-radius: 6px;
}

.metric-progress {
  margin-top: var(--spacing-3, 0.75rem);
}

.progress-ring {
  transform: rotate(-90deg);
}

.progress-ring-circle {
  transition: stroke-dashoffset 0.5s ease;
}

.recovery-factors {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-3, 0.75rem);
  margin-top: var(--spacing-4, 1rem);
}

.factor-item {
  text-align: center;
  padding: var(--spacing-2, 0.5rem);
  background: #f9fafb;
  border-radius: 8px;
}

.factor-label {
  font-size: var(--font-size-xs, 0.8rem);
  color: #6b7280;
  margin-bottom: var(--spacing-1, 0.25rem);
}

.factor-value {
  display: flex;
  justify-content: center;
  gap: 2px;
}

.goal-details {
  margin-top: var(--spacing-4, 1rem);
}

.goal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-2, 0.5rem) 0;
  border-bottom: 1px solid #f3f4f6;
}

.goal-item:last-child {
  border-bottom: none;
}

.goal-info {
  flex: 1;
}

.goal-name {
  font-size: var(--font-size-sm, 0.875rem);
  color: #374151;
  margin-bottom: var(--spacing-1, 0.25rem);
}

.goal-progress {
  font-size: var(--font-size-xs, 0.8rem);
  color: #6b7280;
}

.goal-bar {
  width: 60px;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.goal-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  transition: width 0.5s ease;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: var(--spacing-6, 1.5rem);
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-5, 1.25rem);
}

.modal-title {
  font-size: var(--font-size-lg, 1.125rem);
  font-weight: var(--font-weight-semibold, 600);
  color: #1f2937;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #e5e7eb;
}

.recovery-score-display {
  text-align: center;
  margin-bottom: var(--spacing-6, 1.5rem);
}

.score-circle {
  width: 120px;
  height: 120px;
  margin: 0 auto var(--spacing-4, 1rem);
  position: relative;
}

.score-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-size-2xl, 1.5rem);
  font-weight: var(--font-weight-bold, 700);
}

.score-label {
  font-size: var(--font-size-sm, 0.875rem);
  color: #6b7280;
  margin-bottom: var(--spacing-2, 0.5rem);
}

.score-description {
  font-size: var(--font-size-sm, 0.875rem);
  color: #374151;
  line-height: var(--line-height-relaxed, 1.625);
}

.recommendations {
  background: #f9fafb;
  border-radius: 12px;
  padding: var(--spacing-4, 1rem);
}

.recommendations-title {
  font-size: var(--font-size-base, 1rem);
  font-weight: var(--font-weight-semibold, 600);
  color: #1f2937;
  margin-bottom: var(--spacing-3, 0.75rem);
}

.recommendation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-2, 0.5rem);
  padding: var(--spacing-2, 0.5rem) 0;
  font-size: var(--font-size-sm, 0.875rem);
  color: #374151;
}

.recommendation-icon {
  color: #10b981;
  margin-top: 2px;
  flex-shrink: 0;
}

/* Responsive styles using design token breakpoints */
@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-4, 1rem);
  }
  
  .metric-card {
    padding: var(--spacing-5, 1.25rem);
  }
  
  .value-number {
    font-size: var(--font-size-2xl, 1.5rem);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-4, 1rem);
  }
  
  .time-range-selector {
    width: 100%;
    justify-content: space-between;
  }
  
  .recovery-factors {
    grid-template-columns: 1fr;
    gap: var(--spacing-2, 0.5rem);
  }
  
  .modal-content {
    padding: var(--spacing-5, 1.25rem);
    margin: var(--spacing-4, 1rem);
  }
}

@media (max-width: 480px) {
  .metric-card {
    padding: var(--spacing-4, 1rem);
  }
  
  .value-number {
    font-size: var(--font-size-xl, 1.25rem);
  }
  
  .time-range-btn {
    padding: var(--spacing-1, 0.25rem) var(--spacing-3, 0.75rem);
    font-size: var(--font-size-xs, 0.8rem);
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .metrics-overview {
    background: #1f2937;
  }
  
  .section-title {
    color: #f9fafb;
  }
  
  .metric-card {
    background: rgba(55, 65, 81, 0.95);
    border-color: rgba(75, 85, 99, 0.3);
  }
  
  .metric-title {
    color: #f9fafb;
  }
  
  .metric-subtitle {
    color: #d1d5db;
  }
  
  .value-number {
    color: #f9fafb;
  }
  
  .value-unit {
    color: #d1d5db;
  }
  
  .time-range-btn {
    background: #374151;
    border-color: #4b5563;
    color: #d1d5db;
  }
  
  .time-range-btn:hover {
    border-color: #60a5fa;
    color: #60a5fa;
  }
  
  .metric-label {
    color: #9ca3af;
  }
  
  .factor-item {
    background: #4b5563;
  }
  
  .factor-label {
    color: #9ca3af;
  }
  
  .goal-name {
    color: #f3f4f6;
  }
  
  .goal-progress {
    color: #d1d5db;
  }
  
  .goal-bar {
    background: #4b5563;
  }
  
  .modal-content {
    background: #374151;
  }
  
  .modal-title {
    color: #f9fafb;
  }
  
  .modal-close {
    background: #4b5563;
  }
  
  .modal-close:hover {
    background: #6b7280;
  }
  
  .score-label {
    color: #d1d5db;
  }
  
  .score-description {
    color: #e5e7eb;
  }
  
  .recommendations {
    background: #4b5563;
  }
  
  .recommendations-title {
    color: #f9fafb;
  }
  
  .recommendation-item {
    color: #e5e7eb;
  }
}
</style>