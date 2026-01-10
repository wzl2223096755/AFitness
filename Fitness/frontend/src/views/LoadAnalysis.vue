<template>
  <div class="tech-analysis">
    <!-- 背景系统 -->
    <div class="tech-bg">
      <div class="grid-layer"></div>
      <div class="nebula-layer">
        <div class="nebula blob-1"></div>
        <div class="nebula blob-2"></div>
      </div>
      <div class="scan-beam"></div>
    </div>

    <van-nav-bar 
      title="分析终端" 
      left-arrow 
      @click-left="$router.back()" 
      class="tech-nav-bar"
    />

    <van-tabs v-model:active="activeTab" sticky animated class="tech-tabs">
      <!-- 训练负荷分析 -->
      <van-tab title="负荷分析">
        <div class="tab-content">
          <div class="tech-card glass-panel filter-panel">
            <div class="panel-header">
              <span class="panel-title">时间范围设定</span>
              <van-icon name="calendar-o" class="panel-icon" />
            </div>
            <div class="date-selector" @click="showCalendar = true">
              <span class="date-text">{{ dateRangeText }}</span>
              <van-icon name="arrow" />
            </div>
          </div>

          <van-calendar
            v-model:show="showCalendar"
            type="range"
            @confirm="onDateConfirm"
            color="#7000ff"
            class="tech-calendar"
          />

          <div class="tech-card glass-panel chart-panel">
            <div class="panel-header">
              <span class="panel-title">训练负荷趋势 (VOL)</span>
              <div class="live-indicator">
                <span class="dot"></span>
                IONIZED
              </div>
            </div>
            <div ref="loadTrendChart" class="tech-chart"></div>
          </div>

          <div class="stats-grid">
            <div class="stat-item glass-panel">
              <div class="stat-icon"><van-icon name="points" /></div>
              <div class="stat-info">
                <span class="stat-label">总训练量</span>
                <span class="stat-value">{{ totalVolume }}<small>kg</small></span>
              </div>
            </div>
            <div class="stat-item glass-panel">
              <div class="stat-icon"><van-icon name="aim" /></div>
              <div class="stat-info">
                <span class="stat-label">平均负荷</span>
                <span class="stat-value">{{ avgLoad }}<small>kg</small></span>
              </div>
            </div>
            <div class="stat-item glass-panel">
              <div class="stat-icon"><van-icon name="fire-o" /></div>
              <div class="stat-info">
                <span class="stat-label">峰值负荷</span>
                <span class="stat-value">{{ maxLoad }}<small>kg</small></span>
              </div>
            </div>
          </div>
        </div>
      </van-tab>

      <!-- 1RM 计算器 -->
      <van-tab title="1RM 计算">
        <div class="tab-content">
          <OneRepMaxCalculator />
        </div>
      </van-tab>

      <!-- 训练容量计算器 -->
      <van-tab title="容量计算">
        <div class="tab-content">
          <TrainingVolumeCalculator />
        </div>
      </van-tab>

      <!-- 计算历史记录 -->
      <van-tab title="计算历史">
        <div class="tab-content">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh" head-height="80">
            <van-list
              v-model:loading="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <div v-for="item in calculationHistory" :key="item.id" class="history-card glass-panel">
                <van-swipe-cell>
                  <div class="history-content">
                    <div class="history-main">
                      <div class="type-badge" :class="item.calculationType.toLowerCase()">
                        {{ formatType(item.calculationType) }}
                      </div>
                      <div class="history-params">{{ formatParams(item) }}</div>
                      <div class="history-date">{{ formatDate(item.createdAt) }}</div>
                    </div>
                    <div class="history-result">
                      <span class="result-val">{{ item.resultValue.toFixed(1) }}</span>
                      <span class="result-unit">{{ item.calculationType === 'CALORIES' ? 'kcal' : 'kg' }}</span>
                    </div>
                  </div>
                  <template #right>
                    <van-button square text="删除" type="danger" class="delete-btn" @click="deleteRecord(item.id)" />
                  </template>
                </van-swipe-cell>
              </div>
            </van-list>
          </van-pull-refresh>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { showNotify, showConfirmDialog, showSuccessToast } from 'vant'
import { fitnessApi } from '../api/fitness'
import echarts from '../utils/echarts'
import { useFitnessStore } from '../stores/fitness'
import OneRepMaxCalculator from '../components/OneRepMaxCalculator.vue'
import TrainingVolumeCalculator from '../components/TrainingVolumeCalculator.vue'

// 状态定义
const activeTab = ref(0)
const showCalendar = ref(false)
const dateRange = ref([])
const loadTrendChart = ref(null)
const chartInstance = ref(null)
const totalVolume = ref(0)
const avgLoad = ref(0)
const maxLoad = ref(0)
const fitnessStore = useFitnessStore()

// 历史记录相关
const calculationHistory = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const dateRangeText = computed(() => {
  if (dateRange.value.length !== 2) return '选择日期范围'
  const start = dateRange.value[0].toLocaleDateString()
  const end = dateRange.value[1].toLocaleDateString()
  return `${start} - ${end}`
})

// 方法
const onDateConfirm = (values) => {
  const [start, end] = values
  dateRange.value = [start, end]
  showCalendar.value = false
  fetchLoadTrend()
}

const fetchLoadTrend = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  try {
    const response = await fitnessApi.getLoadTrend({
      startDate: dateRange.value[0].toISOString().split('T')[0],
      endDate: dateRange.value[1].toISOString().split('T')[0]
    })
    
    if (response.success) {
      const trendData = response.data
      if (Object.keys(trendData).length > 0) {
        calculateStats(trendData)
        updateChart(trendData)
      } else {
        showNotify({ type: 'warning', message: '该时间段内没有训练数据' })
        calculateStats({})
        updateChart({})
      }
    }
  } catch (error) {
    showNotify({ type: 'danger', message: '获取负荷趋势失败' })
  }
}

const onLoad = async () => {
  try {
    const res = await fitnessApi.getCalculationHistory()
    if (res.success) {
      calculationHistory.value = res.data
    }
    finished.value = true
  } catch (error) {
    console.error('获取历史记录失败', error)
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  finished.value = false
  loading.value = true
  await onLoad()
  refreshing.value = false
}

const deleteRecord = async (id) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这条计算记录吗？',
      className: 'tech-dialog'
    })
    
    const res = await fitnessApi.deleteCalculationRecord(id)
    if (res.success) {
      showSuccessToast('数据已移除')
      calculationHistory.value = calculationHistory.value.filter(item => item.id !== id)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除记录失败', error)
    }
  }
}

const formatType = (type) => {
  const map = {
    '1RM': '1RM 极限',
    'VOLUME': '容量负荷',
    'CALORIES': '能量消耗'
  }
  return map[type] || type
}

const formatParams = (item) => {
  try {
    const params = JSON.parse(item.inputParams)
    if (item.calculationType === '1RM') {
      return `${params.weight}kg × ${params.reps}reps`
    } else if (item.calculationType === 'VOLUME') {
      return `${params.weight}kg × ${params.sets}sets × ${params.reps}reps`
    } else if (item.calculationType === 'CALORIES') {
      return `${params.duration}min (${params.intensity})`
    }
  } catch (e) {
    return item.inputParams
  }
  return ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const updateChart = (trendData) => {
  nextTick(() => {
    if (!chartInstance.value && loadTrendChart.value) {
      chartInstance.value = echarts.init(loadTrendChart.value)
    }
    
    if (!chartInstance.value) return

    const dates = Object.keys(trendData).sort()
    const volumes = dates.map(date => trendData[date])
    
    const option = {
      backgroundColor: 'transparent',
      grid: { left: '12%', right: '5%', bottom: '15%', top: '15%' },
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#7c3aed',
        textStyle: { color: '#1e293b' }
      },
      xAxis: { 
        type: 'category', 
        data: dates.map(d => d.substring(5)),
        axisLine: { lineStyle: { color: 'rgba(124, 58, 237, 0.2)' } },
        axisLabel: { color: '#64748b', fontSize: 10 }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(124, 58, 237, 0.08)' } },
        axisLabel: { color: '#64748b', fontSize: 10 }
      },
      series: [{
        data: volumes,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#ec4899' },
        lineStyle: { 
          width: 3, 
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#7c3aed' },
            { offset: 1, color: '#ec4899' }
          ])
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(124, 58, 237, 0.25)' },
            { offset: 1, color: 'rgba(236, 72, 153, 0)' }
          ])
        }
      }]
    }
    chartInstance.value.setOption(option)
  })
}

const calculateStats = (trendData) => {
  const volumes = Object.values(trendData)
  if (volumes.length === 0) {
    totalVolume.value = 0
    avgLoad.value = 0
    maxLoad.value = 0
    return
  }
  totalVolume.value = Math.round(volumes.reduce((sum, vol) => sum + vol, 0))
  avgLoad.value = Math.round(totalVolume.value / volumes.length)
  maxLoad.value = Math.max(...volumes)
}

onMounted(() => {
  // 默认显示最近7天
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  dateRange.value = [start, end]
  fetchLoadTrend()
  onLoad()
})
</script>

<style scoped>
.tech-analysis {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
  position: relative;
  overflow-x: hidden;
}

/* 背景系统 */
.tech-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.grid-layer {
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(var(--border-subtle) 1px, transparent 1px),
    linear-gradient(90deg, var(--border-subtle) 1px, transparent 1px);
  background-size: 30px 30px;
}

.nebula-layer {
  position: absolute;
  inset: 0;
  filter: blur(100px);
  opacity: 0.25;
}

.nebula {
  position: absolute;
  border-radius: 50%;
  animation: nebulaFloat 20s infinite alternate;
}

.blob-1 { width: 400px; height: 400px; background: var(--brand-gradient); top: -50px; right: -100px; }
.blob-2 { width: 300px; height: 300px; background: linear-gradient(135deg, var(--brand-secondary), #ec4899); bottom: 50px; left: -80px; animation-delay: -5s; }

@keyframes nebulaFloat {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(30px, 20px) scale(1.1); }
}

.scan-beam {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--brand-primary), transparent);
  animation: scanMove 4s linear infinite;
  opacity: 0.3;
}

@keyframes scanMove {
  0% { top: -2%; }
  100% { top: 102%; }
}

/* 导航与标签页 */
:deep(.tech-nav-bar) {
  background: var(--bg-elevated) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-default);
}

:deep(.tech-nav-bar .van-nav-bar__title) { color: var(--brand-primary); font-weight: 600; letter-spacing: 1px; }
:deep(.tech-nav-bar .van-icon) { color: var(--brand-primary); }

:deep(.tech-tabs) {
  --van-tabs-nav-background: transparent;
  --van-tab-text-color: var(--text-tertiary);
  --van-tab-active-text-color: var(--brand-primary);
  --van-tabs-bottom-bar-color: var(--brand-primary);
}

.tab-content {
  padding: 16px;
  position: relative;
  z-index: 1;
}

/* 卡片面板 */
.glass-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(15px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--brand-primary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 时间选择 */
.date-selector {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--hover-bg);
}

.date-text { font-size: 16px; color: var(--text-primary); font-family: 'Courier New', monospace; }

/* 图表 */
.tech-chart { height: 240px; padding: 10px; }

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  color: var(--brand-primary);
  opacity: 0.8;
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--brand-primary);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--brand-primary);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-item {
  padding: 16px 8px;
  text-align: center;
}

.stat-icon { font-size: 20px; color: var(--brand-primary); margin-bottom: 8px; }
.stat-label { display: block; font-size: 10px; color: var(--text-tertiary); margin-bottom: 4px; }
.stat-value { font-size: 18px; font-weight: 700; color: var(--text-primary); font-family: 'Inter', sans-serif; }
.stat-value small { font-size: 10px; margin-left: 2px; color: var(--brand-primary); }

/* 历史列表 */
.history-card { margin-bottom: 12px; }

.history-content {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.type-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.type-badge.1rm { background: rgba(124, 58, 237, 0.1); color: var(--brand-primary); border: 1px solid var(--border-brand); }
.type-badge.volume { background: rgba(236, 72, 153, 0.1); color: #ec4899; border: 1px solid rgba(236, 72, 153, 0.3); }
.type-badge.calories { background: rgba(245, 158, 11, 0.1); color: var(--color-warning); border: 1px solid rgba(245, 158, 11, 0.3); }

.history-params { font-size: 14px; color: var(--text-primary); margin-bottom: 4px; }
.history-date { font-size: 11px; color: var(--text-disabled); }

.history-result { text-align: right; }
.result-val { display: block; font-size: 22px; font-weight: 700; color: var(--brand-primary); font-family: 'Inter', sans-serif; }
.result-unit { font-size: 10px; color: var(--text-tertiary); }

.delete-btn { height: 100%; border: none; background: rgba(239, 68, 68, 0.1); color: var(--color-danger); backdrop-filter: blur(5px); }

/* 日历适配 */
:deep(.tech-calendar) {
  --van-calendar-background: var(--bg-base);
  --van-calendar-header-title-text-color: var(--text-primary);
  --van-calendar-day-height: 44px;
  color: var(--text-primary);
}
</style>
