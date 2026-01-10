<template>
  <section class="welcome-section" :class="{ 'animated': isAnimated }">
    <div class="welcome-content">
      <div class="welcome-header">
        <h1 class="welcome-title">
          <span class="greeting">{{ greeting }}</span>，
          <span class="username">{{ currentUser?.username || '运动员' }}</span>
          <span class="emoji">{{ userEmoji }}</span>
        </h1>
        <p class="welcome-subtitle">{{ motivationalSubtitle }}</p>
      </div>
      
      <div class="quick-stats">
        <div class="stat-item" v-for="stat in quickStats" :key="stat.label">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.changeType">
            {{ stat.change }}
          </div>
        </div>
      </div>
      
      <div class="welcome-actions">
        <el-button 
          type="primary" 
          size="large" 
          @click="navigateToTraining"
          class="action-button primary-action"
        >
          <el-icon><Plus /></el-icon>
          记录新训练
        </el-button>
        <el-button 
          type="default" 
          size="large" 
          @click="navigateToAnalysis"
          class="action-button"
        >
          <el-icon><TrendCharts /></el-icon>
          查看分析
        </el-button>
        <el-button 
          type="success" 
          size="large" 
          @click="navigateToNutrition"
          class="action-button"
        >
          <el-icon><Apple /></el-icon>
          营养记录
        </el-button>
      </div>
    </div>
    
    <div class="welcome-visual">
      <div class="daily-motivation" :class="{ 'pulse': pulseMotivation }">
        <div class="motivation-content">
          <div class="motivation-icon">💪</div>
          <div class="motivation-quote">{{ currentQuote.text }}</div>
          <div class="motivation-author">— {{ currentQuote.author }}</div>
        </div>
        <div class="motivation-date">{{ formatDate(new Date()) }}</div>
        <div class="motivation-actions">
          <el-button 
            size="small" 
            @click="refreshQuote" 
            :loading="quoteLoading"
            class="quote-refresh-btn"
          >
            <el-icon><Refresh /></el-icon>
            换一句
          </el-button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Plus, TrendCharts, Apple, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fitnessApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const currentUser = computed(() => userStore.currentUser)
const isAnimated = ref(false)
const pulseMotivation = ref(false)
const quoteLoading = ref(false)

const currentQuote = ref({
  text: "今天的努力是明天更强的基础",
  author: "健身格言"
})

const motivationalQuotes = [
  { text: "今天的努力是明天更强的基础", author: "健身格言" },
  { text: "坚持不是一时的热情，而是永恒的信念", author: "励志名言" },
  { text: "每一次训练都是对更好自己的投资", author: "健身哲学" },
  { text: "汗水是最好的化妆品，坚持是最美的姿态", author: "运动美学" },
  { text: "生命在于运动，健康在于坚持", author: "健康箴言" }
]

const quickStats = ref([])
let timeInterval = null
const currentTime = ref(new Date())

const greeting = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour < 6) return "夜深了"
  if (hour < 12) return "早上好"
  if (hour < 14) return "中午好"
  if (hour < 18) return "下午好"
  if (hour < 22) return "晚上好"
  return "夜深了"
})

const userEmoji = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour < 6) return "🌙"
  if (hour < 12) return "☀️"
  if (hour < 18) return "🌤️"
  return "🌙"
})

const motivationalSubtitle = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour < 12) return "新的一天，新的开始！让我们用充满活力的训练开启美好的一天"
  if (hour < 18) return "下午时光正好！继续保持训练的节奏，为今天画上完美的句号"
  return "晚上训练有助于放松身心，让我们一起查看今天的训练状态和进度"
})

const navigateToTraining = () => router.push('/training-data')
const navigateToAnalysis = () => router.push('/load-analysis')
const navigateToNutrition = () => router.push('/nutrition-tracking')

const refreshQuote = async () => {
  if (quoteLoading.value) return
  
  quoteLoading.value = true
  pulseMotivation.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    const availableQuotes = motivationalQuotes.filter(q => q.text !== currentQuote.value.text)
    if (availableQuotes.length > 0) {
      currentQuote.value = availableQuotes[Math.floor(Math.random() * availableQuotes.length)]
    }
  } finally {
    quoteLoading.value = false
    setTimeout(() => { pulseMotivation.value = false }, 600)
  }
}

const fetchUserStats = async () => {
  try {
    const response = await fitnessApi.getUserStatsOverview()
    if (response.data) {
      quickStats.value = [
        { label: "本周训练", value: (response.data.weeklyTrainingCount || 0).toString(), change: `+${response.data.weeklyChange || 0}`, changeType: "positive" },
        { label: "总训练时长", value: `${(response.data.totalTrainingHours || 0).toFixed(1)}h`, change: `+${(response.data.trainingHoursChange || 0).toFixed(1)}h`, changeType: "positive" },
        { label: "消耗卡路里", value: (response.data.totalCalories || 0).toLocaleString(), change: `+${response.data.caloriesChange || 0}`, changeType: "positive" },
        { label: "完成目标", value: `${response.data.goalCompletionRate || 0}%`, change: `+${response.data.goalChange || 0}%`, changeType: "positive" }
      ]
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
    quickStats.value = [
      { label: "本周训练", value: "0", change: "0", changeType: "positive" },
      { label: "总训练时长", value: "0.0h", change: "0.0h", changeType: "positive" },
      { label: "消耗卡路里", value: "0", change: "0", changeType: "positive" },
      { label: "完成目标", value: "0%", change: "0%", changeType: "positive" }
    ]
  }
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(date)
}

onMounted(() => {
  setTimeout(() => { isAnimated.value = true }, 100)
  fetchUserStats()
  timeInterval = setInterval(() => { currentTime.value = new Date() }, 60000)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
})

watch(currentUser, (newUser) => {
  if (newUser) fetchUserStats()
}, { immediate: true })
</script>

<style scoped>
.welcome-section {
  background: linear-gradient(135deg, rgba(128, 32, 255, 0.15) 0%, rgba(0, 242, 254, 0.1) 100%);
  border-radius: 20px;
  padding: 40px;
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 40px;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  backdrop-filter: blur(10px);
}

.welcome-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.welcome-title {
  font-size: 2.8rem;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.greeting {
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
  max-width: 600px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 20px;
  padding: 24px;
  background: var(--glass-bg);
  border-radius: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px 12px;
  background: var(--glass-bg);
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-3px);
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-change {
  font-size: 0.85rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.stat-change.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.welcome-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-button {
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
}

.primary-action {
  background: linear-gradient(135deg, var(--color-primary, #8020ff), var(--color-secondary, #ff00ff));
  border: none;
  color: white;
}

.welcome-visual {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 320px;
}

.daily-motivation {
  background: rgba(18, 18, 37, 0.9);
  border-radius: 16px;
  padding: 30px;
  text-align: center;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
}

.motivation-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.motivation-quote {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  font-style: italic;
}

.motivation-author {
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.motivation-date {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 16px 0;
}

@media (max-width: 1200px) {
  .welcome-section {
    flex-direction: column;
  }
  
  .welcome-visual {
    min-width: auto;
    flex-direction: row;
  }
  
  .daily-motivation {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    padding: 24px 20px;
    gap: 24px;
  }
  
  .welcome-title {
    font-size: 2.2rem;
    justify-content: center;
    text-align: center;
  }
  
  .welcome-subtitle {
    text-align: center;
  }
  
  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .welcome-actions {
    justify-content: center;
  }
  
  .welcome-visual {
    flex-direction: column;
  }
}
</style>
