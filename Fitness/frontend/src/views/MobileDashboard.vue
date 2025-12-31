<template>
  <div class="tech-dashboard">
    <!-- 核心科技背景层 -->
    <div class="tech-bg">
      <div class="grid-layer"></div>
      <div class="nebula-layer">
        <div class="nebula blob-1"></div>
        <div class="nebula blob-2"></div>
      </div>
      <div class="scan-beam"></div>
    </div>

    <!-- 顶部状态栏 -->
    <van-nav-bar fixed placeholder class="tech-nav-bar">
      <template #title>
        <div class="dashboard-title">
          <span class="glitch-text" data-text="CORE_DASHBOARD">CORE_DASHBOARD</span>
        </div>
      </template>
      <template #right>
        <van-icon name="sync" class="sync-icon" :class="{ 'spinning': isSyncing }" @click="handleSync" />
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="tech-refresh">
      <div class="content-viewport">
        
        <!-- 用户身份验证与核心摘要 -->
        <div class="identity-card glass-panel">
          <div class="panel-corner tl"></div>
          <div class="panel-corner br"></div>
          <div class="user-profile">
            <div class="avatar-wrapper">
              <van-image round :src="userAvatar" class="avatar-glow" />
              <div class="status-indicator online"></div>
            </div>
            <div class="user-meta">
              <div class="username-row">
                <span class="uid-label">AGENT:</span>
                <h3 class="name">{{ username }}</h3>
              </div>
              <p class="motto-text van-ellipsis">"{{ dailyMotto }}"</p>
            </div>
          </div>
          
          <div class="core-metrics-row">
            <div class="core-item">
              <div class="core-label">今日消耗</div>
              <div class="core-value-box">
                <span class="val">{{ calories }}</span>
                <small>KCAL</small>
              </div>
            </div>
            <div class="divider-v"></div>
            <div class="core-item">
              <div class="core-label">连续打卡</div>
              <div class="core-value-box">
                <span class="val">{{ streak }}</span>
                <small>DAYS</small>
              </div>
            </div>
          </div>
        </div>

        <!-- 关键生命体征与训练指标 -->
        <div class="section-tag">
          <div class="tag-line"></div>
          <span>VITAL_SIGN_ANALYSIS</span>
        </div>
        
        <div class="metrics-grid">
          <div v-for="(metric, index) in metrics" :key="metric.title" 
            class="metric-item glass-panel" 
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="metric-icon-box" :style="{ '--glow-color': metric.color }">
              <van-icon :name="metric.icon" />
            </div>
            <div class="metric-data">
              <div class="label">{{ metric.title }}</div>
              <div class="value-row">
                <span class="num">{{ metric.value }}</span>
                <span class="unit">{{ metric.unit }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 训练负荷数字驾驶舱 -->
        <div class="section-tag">
          <div class="tag-line"></div>
          <span>LOAD_CAPACITY_TREND</span>
        </div>
        
        <div class="chart-panel glass-panel">
          <div class="panel-header">
            <div class="chart-title">LOAD_ENGINE_v1.0</div>
            <van-tag plain color="#00f2fe" size="mini">REAL_TIME</van-tag>
          </div>
          <div class="chart-body">
            <div ref="loadChart" class="load-engine-chart"></div>
          </div>
        </div>

        <!-- 历史作战记录 -->
        <div class="section-tag">
          <div class="tag-line"></div>
          <span>MISSION_LOGS</span>
        </div>
        
        <div class="log-list glass-panel">
          <div v-if="recentWorkouts.length > 0">
            <div 
              v-for="log in recentWorkouts" 
              :key="log.id" 
              class="log-item"
              @click="viewWorkout(log)"
            >
              <div class="log-info">
                <div class="log-name">{{ log.name }}</div>
                <div class="log-time">{{ log.date }}</div>
              </div>
              <div class="log-data">
                <span class="val">{{ log.volume }}</span>
                <span class="unit">KG</span>
                <van-icon name="arrow" class="arrow-icon" />
              </div>
            </div>
          </div>
          <van-empty v-else description="无历史作战记录" class="tech-empty" />
        </div>
      </div>
    </van-pull-refresh>

    <!-- 战术导航栏 -->
    <div class="tactical-nav safe-area-bottom">
      <div class="nav-item" @click="$router.push('/training-data')">
        <van-icon name="records-o" />
        <span>录入</span>
      </div>
      <div class="nav-item active" @click="showRMCalculator = true">
        <div class="center-btn">
          <van-icon name="chart-trending-o" />
        </div>
        <span>RM估算</span>
      </div>
      <div class="nav-item" @click="$router.push('/training-plan-display')">
        <van-icon name="orders-o" />
        <span>计划</span>
      </div>
    </div>

    <!-- 快捷工具弹出层 -->
    <van-popup v-model:show="showRMCalculator" position="bottom" class="tech-popup">
      <div class="popup-handle"></div>
      <div class="popup-content">
        <OneRepMaxCalculator />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
/**
 * MobileDashboard.vue
 * 移动端数字驾驶舱看板
 * 技术栈: Vue 3 + Vant UI + ECharts
 */
import { ref, onMounted, nextTick } from 'vue'
import { fitnessApi } from '../api/fitness'
import { userApi } from '../api/user'
import echarts from '../utils/echarts'
import { useRouter } from 'vue-router'
import { showSuccessToast, showLoadingToast } from 'vant'
import OneRepMaxCalculator from '../components/OneRepMaxCalculator.vue'

const router = useRouter()
const refreshing = ref(false)
const isSyncing = ref(false)
const showRMCalculator = ref(false)
const username = ref('AGENT_UNKNOWN')
const userAvatar = ref('https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg')
const dailyMotto = ref('KEEP_PUSHING_YOUR_LIMITS')
const calories = ref(0)
const streak = ref(0)
const loadChart = ref(null)

const metrics = ref([
  { title: '最新体重', value: '--', unit: 'KG', icon: 'balance-o', color: '#00f2fe' },
  { title: '体脂率', value: '--', unit: '%', icon: 'shield-o', color: '#00ff88' },
  { title: '1RM 峰值', value: '--', unit: 'KG', icon: 'fire-o', color: '#ff00ff' },
  { title: '周训练频次', value: '--', unit: '次', icon: 'calendar-o', color: '#7000ff' }
])

const recentWorkouts = ref([])

const loadDashboardData = async () => {
  try {
    const [statsRes, historyRes, userRes] = await Promise.all([
      fitnessApi.getDashboardStats(),
      fitnessApi.getTrainingHistory({ page: 0, size: 5 }),
      userApi.getUserProfile()
    ])

    if (userRes.success) {
      username.value = userRes.data.nickname || userRes.data.username
      if (userRes.data.avatar) userAvatar.value = userRes.data.avatar
    }

    if (statsRes.success) {
      const d = statsRes.data
      calories.value = d.todayCalories || 0
      streak.value = d.trainingStreak || 0
      metrics.value[0].value = d.latestWeight || '--'
      metrics.value[1].value = d.latestBodyFat || '--'
      metrics.value[2].value = d.avg1RM || '--'
      metrics.value[3].value = d.weeklyFrequency || '--'
    }

    if (historyRes.success) {
      recentWorkouts.value = historyRes.data.content.map(w => ({
        id: w.id,
        name: w.exerciseName || '力量任务',
        date: new Date(w.startTime).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
        volume: w.totalVolume || 0
      }))
    }
    
    initChart()
  } catch (error) {
    console.error('DASHBOARD_SYNC_ERROR:', error)
  } finally {
    refreshing.value = false
  }
}

const initChart = () => {
  nextTick(() => {
    if (!loadChart.value) return
    const chart = echarts.init(loadChart.value)
    const option = {
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'],
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
        axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)', type: 'dashed' } },
        axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 10 }
      },
      series: [{
        data: [4200, 5800, 3900, 7200, 6100, 8500, 7800],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#ff00ff', borderColor: '#05050a', borderWidth: 2 },
        lineStyle: { width: 4, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#7000ff' },
          { offset: 1, color: '#ff00ff' }
        ]) },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(112, 0, 255, 0.2)' },
            { offset: 1, color: 'rgba(112, 0, 255, 0)' }
          ])
        },
        emphasis: {
          scale: 1.5,
          itemStyle: { shadowBlur: 10, shadowColor: '#00f2fe' }
        }
      }]
    }
    chart.setOption(option)
    window.addEventListener('resize', () => chart.resize())
  })
}

const onRefresh = () => loadDashboardData()

const handleSync = async () => {
  if (isSyncing.value) return
  
  isSyncing.value = true
  const toast = showLoadingToast({
    message: '同步华为健康数据...',
    forbidClick: true,
    duration: 0
  })

  try {
    const res = await fitnessApi.syncHuaweiData()
    if (res.success) {
      showSuccessToast(res.data.message || '同步成功')
      // 同步后刷新数据
      loadDashboardData()
    }
  } catch (error) {
    console.error('SYNC_ERROR:', error)
  } finally {
    isSyncing.value = false
    toast.close()
  }
}

const viewWorkout = (log) => router.push({ path: '/training-data', query: { id: log.id } })

onMounted(() => loadDashboardData())
</script>

<style scoped>
.tech-dashboard {
  min-height: 100vh;
  background-color: transparent;
  color: #e0e6ed;
  position: relative;
  overflow-x: hidden;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 背景系统 */
.tech-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.grid-layer {
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(112, 0, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(112, 0, 255, 0.03) 1px, transparent 1px);
  background-size: 25px 25px;
}

.nebula-layer {
  position: absolute;
  inset: 0;
  filter: blur(100px);
  opacity: 0.1;
}

.nebula {
  position: absolute;
  border-radius: 50%;
  animation: nebulaFloat 20s infinite alternate;
}

.blob-1 { width: 500px; height: 500px; background: #7000ff; top: -100px; right: -100px; }
.blob-2 { width: 400px; height: 400px; background: #ff00ff; bottom: -50px; left: -100px; animation-delay: -5s; }

@keyframes nebulaFloat {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(50px, 30px) scale(1.1); }
}

.scan-beam {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #7000ff, transparent);
  animation: scanMove 4s linear infinite;
  opacity: 0.2;
}

@keyframes scanMove {
  0% { top: -2%; }
  100% { top: 102%; }
}

/* 导航 */
:deep(.tech-nav-bar) {
  background: rgba(5, 5, 10, 0.8) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(112, 0, 255, 0.1);
}

.sync-icon {
  font-size: 20px;
  color: #7000ff;
  cursor: pointer;
  transition: all 0.3s;
}

.sync-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.glitch-text {
  font-size: 14px;
  font-weight: 900;
  color: #7000ff;
  letter-spacing: 2px;
  position: relative;
}

/* 身份卡片 */
.content-viewport {
  padding: 16px;
  padding-bottom: 120px;
  position: relative;
  z-index: 1;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(112, 0, 255, 0.08);
  border-radius: 16px;
  position: relative;
}

.panel-corner {
  position: absolute;
  width: 8px;
  height: 8px;
  border: 2px solid #7000ff;
}

.tl { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.br { bottom: -1px; right: -1px; border-left: none; border-top: none; }

.identity-card {
  padding: 24px;
  margin-bottom: 24px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
}

.avatar-glow {
  width: 64px;
  height: 64px;
  border: 2px solid #7000ff;
  box-shadow: 0 0 15px rgba(112, 0, 255, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 12px;
  height: 12px;
  background: #00ff88;
  border-radius: 50%;
  border: 2px solid #05050a;
}

.uid-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
  margin-right: 6px;
}

.username-row {
  display: flex;
  align-items: baseline;
}

.user-meta .name {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
}

.motto-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4px;
}

.core-metrics-row {
  display: flex;
  justify-content: space-around;
  background: rgba(112, 0, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
}

.core-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 4px;
  text-align: center;
}

.core-value-box {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.core-value-box .val {
  font-size: 24px;
  font-weight: 800;
  color: #7000ff;
  font-family: 'DIN Alternate', sans-serif;
}

.core-value-box small {
  font-size: 10px;
  color: rgba(112, 0, 255, 0.6);
}

.divider-v {
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.1);
  align-self: center;
}

/* 标签装饰 */
.section-tag {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 24px 0 16px 4px;
}

.tag-line {
  width: 4px;
  height: 14px;
  background: #7000ff;
  box-shadow: 0 0 8px #7000ff;
}

.section-tag span {
  font-size: 12px;
  font-weight: 700;
  color: rgba(112, 0, 255, 0.8);
  letter-spacing: 1px;
}

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.metric-item {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideUp 0.6s cubic-bezier(0.23, 1, 0.32, 1) forwards;
  opacity: 0;
}

@keyframes slideUp {
  to { opacity: 1; transform: translateY(0); }
}

.metric-icon-box {
  width: 40px;
  height: 40px;
  background: rgba(var(--glow-color-rgb), 0.1);
  border: 1px solid var(--glow-color);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--glow-color);
  font-size: 20px;
  box-shadow: 0 0 10px rgba(var(--glow-color-rgb), 0.2);
}

.metric-data .label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.metric-data .num {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  font-family: 'DIN Alternate', sans-serif;
}

.metric-data .unit {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
  margin-left: 2px;
}

/* 图表面板 */
.chart-panel {
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 1px;
}

.load-engine-chart {
  height: 200px;
  width: 100%;
}

/* 记录列表 */
.log-list {
  padding: 8px 0;
}

.log-item {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.log-item:last-child { border-bottom: none; }

.log-name { font-size: 15px; font-weight: 600; color: #fff; }
.log-time { font-size: 12px; color: rgba(255, 255, 255, 0.3); margin-top: 2px; }

.log-data {
  display: flex;
  align-items: center;
  gap: 4px;
}

.log-data .val {
  font-size: 18px;
  font-weight: 800;
  color: #7000ff;
  font-family: 'DIN Alternate', sans-serif;
}

.log-data .unit { font-size: 10px; color: rgba(255, 255, 255, 0.3); }
.arrow-icon { color: rgba(255, 255, 255, 0.1); margin-left: 8px; }

/* 战术导航 */
.tactical-nav {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 70px;
  background: rgba(5, 5, 10, 0.9);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(112, 0, 255, 0.2);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.4);
}

.nav-item.active {
  color: #7000ff;
}

.nav-item span { font-size: 10px; font-weight: 700; }
.nav-item .van-icon { font-size: 22px; }

.center-btn {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #7000ff, #ff00ff);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -30px;
  box-shadow: 0 4px 15px rgba(112, 0, 255, 0.4);
  color: #fff;
}

/* 弹窗样式 */
:deep(.tech-popup) {
  background: #05050a;
  border-radius: 24px 24px 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.popup-handle {
  width: 40px;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  margin: 12px auto;
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
