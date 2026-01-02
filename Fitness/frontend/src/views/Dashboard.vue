<template>
  <div class="dashboard-page">
    <!-- 欢迎区域 -->
    <section class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="greeting">{{ greeting }}</span>，
          <span class="username">{{ currentUser?.username || '运动员' }}</span>
          <span class="emoji">{{ timeEmoji }}</span>
        </h1>
        <p class="welcome-subtitle">{{ motivationalText }}</p>
      </div>
      <div class="welcome-visual">
        <div class="daily-quote">
          <div class="quote-icon">💪</div>
          <div class="quote-text">{{ currentQuote }}</div>
          <div class="quote-date">{{ formattedDate }}</div>
        </div>
      </div>
    </section>

    <!-- 快速统计卡片 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card primary" @click="navigateTo('/training-data')">
          <div class="stat-icon">🏋️</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.weeklyTrainingCount }}</div>
            <div class="stat-label">本周训练</div>
            <div class="stat-change" :class="stats.weeklyChange >= 0 ? 'positive' : 'negative'">
              {{ stats.weeklyChange >= 0 ? '+' : '' }}{{ stats.weeklyChange }} vs上周
            </div>
          </div>
        </div>

        <div class="stat-card success" @click="navigateTo('/load-analysis')">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.totalVolume) }}</div>
            <div class="stat-label">总训练量(kg)</div>
            <div class="stat-change positive">持续增长</div>
          </div>
        </div>

        <div class="stat-card warning" @click="navigateTo('/recovery-status')">
          <div class="stat-icon">💚</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.recoveryScore }}</div>
            <div class="stat-label">恢复评分</div>
            <div class="stat-change" :class="getRecoveryClass(stats.recoveryScore)">
              {{ getRecoveryText(stats.recoveryScore) }}
            </div>
          </div>
        </div>

        <div class="stat-card danger" @click="navigateTo('/fitness-planner')">
          <div class="stat-icon">🎯</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.goalCompletionRate }}%</div>
            <div class="stat-label">目标完成</div>
            <div class="stat-change" :class="stats.goalCompletionRate >= 80 ? 'positive' : 'negative'">
              {{ stats.goalCompletionRate >= 80 ? '表现优秀' : '继续努力' }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-actions-section">
      <h2 class="section-title">
        <span class="title-icon">⚡</span>
        快捷操作
      </h2>
      <div class="actions-grid">
        <div class="action-card" @click="navigateTo('/training-data')">
          <div class="action-icon">📝</div>
          <div class="action-text">记录训练</div>
        </div>
        <div class="action-card" @click="navigateTo('/fitness-planner')">
          <div class="action-icon">📋</div>
          <div class="action-text">训练计划</div>
        </div>
        <div class="action-card" @click="navigateTo('/nutrition-tracking')">
          <div class="action-icon">🥗</div>
          <div class="action-text">营养记录</div>
        </div>
        <div class="action-card" @click="navigateTo('/training-suggestions')">
          <div class="action-icon">💡</div>
          <div class="action-text">训练建议</div>
        </div>
        <div class="action-card" @click="navigateTo('/history-stats')">
          <div class="action-icon">📈</div>
          <div class="action-text">历史统计</div>
        </div>
        <div class="action-card" @click="navigateTo('/data-visualization')">
          <div class="action-icon">📊</div>
          <div class="action-text">数据可视化</div>
        </div>
      </div>
    </section>

    <!-- 最近训练记录 -->
    <section class="recent-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🕒</span>
          最近训练
        </h2>
        <el-button type="primary" size="small" @click="navigateTo('/training-records')">
          查看全部
        </el-button>
      </div>
      
      <div class="records-list" v-loading="loading">
        <div v-if="recentRecords.length === 0 && !loading" class="empty-state">
          <div class="empty-icon">📭</div>
          <p>还没有训练记录</p>
          <el-button type="primary" @click="navigateTo('/training-data')">
            开始记录
          </el-button>
        </div>
        
        <div v-else class="record-cards">
          <div 
            v-for="record in recentRecords" 
            :key="record.id" 
            class="record-card"
            @click="navigateTo('/training-records')"
          >
            <div class="record-date">{{ formatDate(record.trainingDate) }}</div>
            <div class="record-name">{{ record.exerciseName }}</div>
            <div class="record-details">
              <span class="detail-item">{{ record.weight }}kg</span>
              <span class="detail-item">{{ record.sets }}组</span>
              <span class="detail-item">{{ record.reps }}次</span>
            </div>
            <div class="record-volume">
              训练量: {{ formatNumber(record.totalVolume) }}kg
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 今日目标 -->
    <section class="goals-section" v-if="todayGoals.length > 0">
      <h2 class="section-title">
        <span class="title-icon">🎯</span>
        今日目标
      </h2>
      <div class="goals-list">
        <div v-for="goal in todayGoals" :key="goal.id" class="goal-item">
          <div class="goal-info">
            <span class="goal-name">{{ goal.name }}</span>
            <span class="goal-progress">{{ goal.progress }}/{{ goal.target }}</span>
          </div>
          <el-progress 
            :percentage="Math.min((goal.progress / goal.target) * 100, 100)" 
            :color="goal.progress >= goal.target ? '#10b981' : '#8020ff'"
            :show-text="false"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { fitnessApi } from '../api/fitness'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const currentUser = computed(() => userStore.user)

const stats = ref({
  weeklyTrainingCount: 0,
  weeklyChange: 0,
  totalVolume: 0,
  recoveryScore: 0,
  goalCompletionRate: 0
})

const recentRecords = ref([])
const todayGoals = ref([])

// 励志语录
const quotes = [
  "今天的努力是明天更强的基础",
  "坚持不是一时的热情，而是永恒的信念",
  "每一次训练都是对更好自己的投资",
  "汗水是最好的化妆品",
  "没有天生的强者，只有不懈的努力",
  "生命在于运动，健康在于坚持"
]

const currentQuote = ref(quotes[Math.floor(Math.random() * quotes.length)])

// 计算属性
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return "夜深了"
  if (hour < 12) return "早上好"
  if (hour < 14) return "中午好"
  if (hour < 18) return "下午好"
  if (hour < 22) return "晚上好"
  return "夜深了"
})

const timeEmoji = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return "🌙"
  if (hour < 12) return "☀️"
  if (hour < 18) return "🌤️"
  return "🌙"
})

const motivationalText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return "新的一天，让我们用充满活力的训练开启美好的一天！"
  if (hour < 18) return "下午时光正好，继续保持训练的节奏！"
  return "晚上训练有助于放松身心，查看今天的训练状态吧！"
})

const formattedDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 方法
const navigateTo = (path) => {
  router.push(path)
}

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  } catch {
    return dateStr
  }
}

const getRecoveryClass = (score) => {
  if (score >= 80) return 'positive'
  if (score >= 60) return 'neutral'
  return 'negative'
}

const getRecoveryText = (score) => {
  if (score >= 80) return '恢复良好'
  if (score >= 60) return '需要休息'
  return '过度疲劳'
}

// 加载数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 获取指标概览
    const metricsRes = await fitnessApi.getMetricsOverview('week')
    if (metricsRes.data) {
      stats.value = {
        weeklyTrainingCount: metricsRes.data.weeklyTrainingCount || 0,
        weeklyChange: metricsRes.data.weeklyChange || 0,
        totalVolume: metricsRes.data.totalVolume || 0,
        recoveryScore: metricsRes.data.recoveryScore || 0,
        goalCompletionRate: metricsRes.data.goalCompletionRate || 0
      }
      
      // 设置今日目标
      if (metricsRes.data.goals && Array.isArray(metricsRes.data.goals)) {
        todayGoals.value = metricsRes.data.goals
      } else {
        todayGoals.value = [
          { id: 1, name: '训练次数', progress: stats.value.weeklyTrainingCount, target: 7 },
          { id: 2, name: '训练量', progress: Math.min(stats.value.totalVolume, 15000), target: 15000 }
        ]
      }
    }

    // 获取最近训练记录
    const recordsRes = await fitnessApi.getRecentTrainingRecords()
    if (recordsRes.data && Array.isArray(recordsRes.data)) {
      recentRecords.value = recordsRes.data.slice(0, 6)
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    ElMessage.error('加载数据失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
  padding: 30px;
  background: linear-gradient(135deg, rgba(128, 32, 255, 0.15), rgba(0, 242, 254, 0.1));
  border-radius: 20px;
  border: 1px solid rgba(128, 32, 255, 0.2);
  margin-bottom: 24px;
}

.welcome-content {
  flex: 1;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.greeting {
  background: linear-gradient(135deg, #8020ff, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.emoji {
  font-size: 1.8rem;
}

.welcome-subtitle {
  color: #8888aa;
  font-size: 1rem;
  margin: 0;
  line-height: 1.6;
}

.welcome-visual {
  flex-shrink: 0;
}

.daily-quote {
  background: rgba(18, 18, 37, 0.8);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  border: 1px solid rgba(128, 32, 255, 0.2);
  min-width: 280px;
}

.quote-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}

.quote-text {
  color: #ffffff;
  font-size: 1rem;
  font-style: italic;
  margin-bottom: 12px;
  line-height: 1.5;
}

.quote-date {
  color: #8888aa;
  font-size: 0.85rem;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: rgba(18, 18, 37, 0.9);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid rgba(128, 32, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(128, 32, 255, 0.3);
}

.stat-card.primary { border-top: 3px solid #3b82f6; }
.stat-card.success { border-top: 3px solid #10b981; }
.stat-card.warning { border-top: 3px solid #f59e0b; }
.stat-card.danger { border-top: 3px solid #ef4444; }

.stat-icon {
  font-size: 2.5rem;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.2;
}

.stat-label {
  color: #8888aa;
  font-size: 0.9rem;
  margin: 4px 0;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
}

.stat-change.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.15);
}

.stat-change.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.15);
}

.stat-change.neutral {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.15);
}

/* 快捷操作 */
.quick-actions-section {
  margin-bottom: 24px;
}

.section-title {
  color: #ffffff;
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1.2rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.action-card {
  background: rgba(18, 18, 37, 0.8);
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  border: 1px solid rgba(128, 32, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  background: rgba(128, 32, 255, 0.2);
  border-color: #8020ff;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 2rem;
  margin-bottom: 8px;
}

.action-text {
  color: #e0e0ff;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 最近训练 */
.recent-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.records-list {
  background: rgba(18, 18, 37, 0.8);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(128, 32, 255, 0.2);
  min-height: 150px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #8888aa;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

.record-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.record-card {
  background: rgba(18, 18, 37, 0.9);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(128, 32, 255, 0.15);
  cursor: pointer;
  transition: all 0.2s ease;
}

.record-card:hover {
  border-color: #8020ff;
  background: rgba(128, 32, 255, 0.1);
}

.record-date {
  color: #8888aa;
  font-size: 0.8rem;
  margin-bottom: 6px;
}

.record-name {
  color: #ffffff;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.record-details {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-item {
  color: #00f2fe;
  font-size: 0.85rem;
  background: rgba(0, 242, 254, 0.1);
  padding: 2px 8px;
  border-radius: 6px;
}

.record-volume {
  color: #8888aa;
  font-size: 0.8rem;
}

/* 今日目标 */
.goals-section {
  margin-bottom: 24px;
}

.goals-list {
  background: rgba(18, 18, 37, 0.8);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(128, 32, 255, 0.2);
}

.goal-item {
  margin-bottom: 16px;
}

.goal-item:last-child {
  margin-bottom: 0;
}

.goal-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.goal-name {
  color: #ffffff;
  font-size: 0.95rem;
}

.goal-progress {
  color: #8888aa;
  font-size: 0.85rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    padding: 24px 20px;
  }
  
  .welcome-title {
    font-size: 1.6rem;
    justify-content: center;
  }
  
  .daily-quote {
    min-width: auto;
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 1.4rem;
  }
  
  .actions-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .action-card {
    padding: 16px 12px;
  }
  
  .action-icon {
    font-size: 1.6rem;
  }
  
  .action-text {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .record-cards {
    grid-template-columns: 1fr;
  }
}
</style>
