<template>
  <div class="dashboard-page animate-fade-in-up">
    <!-- 欢迎区域 - 使用统一玻璃态效果 -->
    <section class="welcome-section glass-card">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="greeting text-gradient-primary">{{ greeting }}</span>，
          <span class="username">{{ currentUser?.username || '运动员' }}</span>
          <span class="emoji">{{ timeEmoji }}</span>
        </h1>
        <p class="welcome-subtitle">{{ motivationalText }}</p>
      </div>
      <div class="welcome-visual">
        <div class="daily-quote card-unified card-unified--sm card-unified--primary">
          <div class="quote-icon icon-lg">💪</div>
          <div class="quote-text">{{ currentQuote }}</div>
          <div class="quote-date">{{ formattedDate }}</div>
        </div>
      </div>
    </section>

    <!-- 快速统计卡片 - 使用统一卡片组件 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div 
          class="card-unified card-unified--interactive card-unified--primary stagger-1" 
          @click="navigateTo('/training-data')"
        >
          <div class="stat-icon icon-lg">🏋️</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.weeklyTrainingCount }}</div>
            <div class="stat-label">本周训练</div>
            <div class="stat-change" :class="stats.weeklyChange >= 0 ? 'positive' : 'negative'">
              {{ stats.weeklyChange >= 0 ? '+' : '' }}{{ stats.weeklyChange }} vs上周
            </div>
          </div>
        </div>

        <div 
          class="card-unified card-unified--interactive card-unified--success stagger-2" 
          @click="navigateTo('/load-analysis')"
        >
          <div class="stat-icon icon-lg">📊</div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.totalVolume) }}</div>
            <div class="stat-label">总训练量(kg)</div>
            <div class="stat-change positive">持续增长</div>
          </div>
        </div>

        <div 
          class="card-unified card-unified--interactive card-unified--warning stagger-3" 
          @click="navigateTo('/recovery-status')"
        >
          <div class="stat-icon icon-lg">💚</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.recoveryScore }}</div>
            <div class="stat-label">恢复评分</div>
            <div class="stat-change" :class="getRecoveryClass(stats.recoveryScore)">
              {{ getRecoveryText(stats.recoveryScore) }}
            </div>
          </div>
        </div>

        <div 
          class="card-unified card-unified--interactive card-unified--danger stagger-4" 
          @click="navigateTo('/fitness-planner')"
        >
          <div class="stat-icon icon-lg">🎯</div>
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

    <!-- 快捷操作 - 使用统一 section header -->
    <section class="quick-actions-section">
      <div class="section-header section-header--sm">
        <h2 class="section-header__title">
          <span class="title-icon icon-md">⚡</span>
          快捷操作
        </h2>
      </div>
      <div class="actions-grid">
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-1" @click="navigateTo('/training-data')">
          <div class="action-icon icon-lg">📝</div>
          <div class="action-text">记录训练</div>
        </div>
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-2" @click="navigateTo('/fitness-planner')">
          <div class="action-icon icon-lg">📋</div>
          <div class="action-text">训练计划</div>
        </div>
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-3" @click="navigateTo('/nutrition-tracking')">
          <div class="action-icon icon-lg">🥗</div>
          <div class="action-text">营养记录</div>
        </div>
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-4" @click="navigateTo('/training-suggestions')">
          <div class="action-icon icon-lg">💡</div>
          <div class="action-text">训练建议</div>
        </div>
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-5" @click="navigateTo('/history-stats')">
          <div class="action-icon icon-lg">📈</div>
          <div class="action-text">历史统计</div>
        </div>
        <div class="action-card card-unified card-unified--sm card-unified--interactive stagger-6" @click="navigateTo('/data-visualization')">
          <div class="action-icon icon-lg">📊</div>
          <div class="action-text">数据可视化</div>
        </div>
      </div>
    </section>

    <!-- 最近训练记录 - 使用统一组件 -->
    <section class="recent-section">
      <div class="section-header">
        <h2 class="section-header__title">
          <span class="title-icon icon-md">🕒</span>
          最近训练
        </h2>
        <div class="section-header__actions">
          <el-button class="btn-unified btn-unified--primary btn-unified--sm" @click="navigateTo('/training-records')">
            查看全部
          </el-button>
        </div>
      </div>
      
      <div class="records-list card-unified" v-loading="loading">
        <!-- 空状态 -->
        <div v-if="recentRecords.length === 0 && !loading" class="empty-state empty-state--compact">
          <div class="empty-state__icon">📭</div>
          <h3 class="empty-state__title">还没有训练记录</h3>
          <p class="empty-state__description">开始记录你的第一次训练吧</p>
          <div class="empty-state__actions">
            <el-button class="btn-unified btn-unified--primary" @click="navigateTo('/training-data')">
              开始记录
            </el-button>
          </div>
        </div>
        
        <!-- 训练记录卡片 -->
        <div v-else class="record-cards">
          <div 
            v-for="(record, index) in recentRecords" 
            :key="record.id" 
            class="record-card card-unified card-unified--sm card-unified--interactive"
            :class="`stagger-${index + 1}`"
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
      <div class="section-header section-header--sm">
        <h2 class="section-header__title">
          <span class="title-icon icon-md">🎯</span>
          今日目标
        </h2>
      </div>
      <div class="goals-list card-unified">
        <div v-for="(goal, index) in todayGoals" :key="goal.id" class="goal-item" :class="`stagger-${index + 1}`">
          <div class="goal-info">
            <span class="goal-name">{{ goal.name }}</span>
            <span class="goal-progress">{{ goal.progress }}/{{ goal.target }}</span>
          </div>
          <el-progress 
            :percentage="Math.min((goal.progress / goal.target) * 100, 100)" 
            :color="goal.progress >= goal.target ? '#10b981' : '#8020ff'"
            :show-text="false"
            class="progress-gradient"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { fitnessApi } from '../../../api/fitness'
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
    // 获取仪表盘统计数据
    const statsRes = await fitnessApi.getDashboardStats()
    console.log('Dashboard stats response:', statsRes)
    
    if (statsRes.data) {
      stats.value = {
        weeklyTrainingCount: statsRes.data.weeklyTrainingCount || 0,
        weeklyChange: statsRes.data.weeklyChange || 0,
        totalVolume: statsRes.data.totalVolume || 0,
        recoveryScore: statsRes.data.recoveryScore || 0,
        goalCompletionRate: statsRes.data.nutritionGoalProgress || 0
      }
      
      // 设置今日目标
      todayGoals.value = [
        { id: 1, name: '训练次数', progress: stats.value.weeklyTrainingCount, target: 7 },
        { id: 2, name: '训练量', progress: Math.min(stats.value.totalVolume, 15000), target: 15000 }
      ]
    }

    // 获取最近训练记录
    const recordsRes = await fitnessApi.getTrainingRecords()
    console.log('Training records response:', recordsRes)
    
    if (recordsRes.data && recordsRes.data.records && Array.isArray(recordsRes.data.records)) {
      recentRecords.value = recordsRes.data.records.slice(0, 6)
    } else if (recordsRes.data && Array.isArray(recordsRes.data)) {
      recentRecords.value = recordsRes.data.slice(0, 6)
    }
    
    console.log('Final recentRecords:', recentRecords.value)
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
/* Dashboard 页面样式 - 从原 Dashboard.vue 迁移 */
.dashboard-page {
  padding: var(--spacing-6, 24px);
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-8, 32px);
  padding: var(--spacing-9, 36px);
  margin-bottom: var(--spacing-7, 28px);
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.5), rgba(139, 92, 246, 0.5), transparent);
}

.welcome-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-4, 14px) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-3, 10px);
  flex-wrap: wrap;
}

.username {
  color: var(--text-primary);
}

.emoji {
  font-size: 2rem;
}

.welcome-subtitle {
  color: var(--text-secondary);
  font-size: 1.05rem;
  margin: 0;
  line-height: 1.7;
  max-width: 500px;
}

.welcome-visual {
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.daily-quote {
  text-align: center;
  min-width: 300px;
}

.quote-icon {
  margin-bottom: var(--spacing-4, 14px);
}

.quote-text {
  color: var(--color-text-primary, #f1f5f9);
  font-size: 1.05rem;
  font-style: italic;
  margin-bottom: var(--spacing-4, 14px);
  line-height: 1.6;
}

.quote-date {
  color: var(--color-text-tertiary, #64748b);
  font-size: 0.85rem;
  font-weight: 500;
}

.stats-section {
  margin-bottom: var(--spacing-7, 28px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-5, 20px);
}

.stats-grid .card-unified {
  display: flex;
  align-items: center;
  gap: var(--spacing-5, 18px);
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: var(--spacing-2, 6px) 0;
  font-weight: 500;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
  padding: var(--spacing-1, 4px) var(--spacing-3, 10px);
  border-radius: 12px;
  display: inline-block;
}

.stat-change.positive {
  color: var(--color-success, #10b981);
  background: var(--color-success-bg, rgba(16, 185, 129, 0.15));
}

.stat-change.negative {
  color: var(--color-danger, #ef4444);
  background: var(--color-danger-bg, rgba(239, 68, 68, 0.15));
}

.stat-change.neutral {
  color: var(--color-warning, #f59e0b);
  background: var(--color-warning-bg, rgba(245, 158, 11, 0.15));
}

.quick-actions-section {
  margin-bottom: var(--spacing-7, 28px);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-4, 14px);
}

.action-card {
  text-align: center;
  padding: var(--spacing-6, 24px) var(--spacing-5, 18px) !important;
}

.action-icon {
  margin-bottom: var(--spacing-3, 10px);
  transition: transform 0.3s ease;
}

.action-card:hover .action-icon {
  transform: scale(1.15);
}

.action-text {
  color: var(--color-text-primary, #e2e8f0);
  font-size: 0.95rem;
  font-weight: 600;
}

.recent-section {
  margin-bottom: var(--spacing-7, 28px);
}

.records-list {
  min-height: 160px;
}

.record-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--spacing-4, 14px);
}

.record-card {
  position: relative;
}

.record-date {
  color: var(--color-text-tertiary, #64748b);
  font-size: 0.8rem;
  margin-bottom: var(--spacing-2, 8px);
  font-weight: 500;
}

.record-name {
  color: var(--color-text-primary, #f8fafc);
  font-size: 1.05rem;
  font-weight: 700;
  margin-bottom: var(--spacing-3, 10px);
}

.record-details {
  display: flex;
  gap: var(--spacing-3, 10px);
  margin-bottom: var(--spacing-3, 10px);
  flex-wrap: wrap;
}

.detail-item {
  color: #06b6d4;
  font-size: 0.85rem;
  font-weight: 600;
  background: rgba(6, 182, 212, 0.12);
  padding: var(--spacing-1, 4px) var(--spacing-3, 10px);
  border-radius: 8px;
}

.record-volume {
  color: var(--color-text-secondary, #94a3b8);
  font-size: 0.8rem;
  font-weight: 500;
}

.goals-section {
  margin-bottom: var(--spacing-7, 28px);
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4, 16px);
}

.goal-item {
  padding: var(--spacing-4, 16px);
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
}

.goal-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-3, 12px);
}

.goal-name {
  color: var(--color-text-primary, #f8fafc);
  font-size: 1rem;
  font-weight: 600;
}

.goal-progress {
  color: var(--color-text-secondary, #94a3b8);
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: var(--spacing-4, 16px);
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    padding: var(--spacing-7, 28px) var(--spacing-5, 20px);
  }
  
  .welcome-title {
    font-size: 1.7rem;
    justify-content: center;
  }
  
  .daily-quote {
    min-width: auto;
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .actions-grid {
    grid-template-columns: repeat(3, 1fr);
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
