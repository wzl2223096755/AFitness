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
          :loading="actionLoading.training"
        >
          <el-icon><Plus /></el-icon>
          记录新训练
        </el-button>
        <el-button 
          type="default" 
          size="large" 
          @click="navigateToAnalysis"
          class="action-button"
          :loading="actionLoading.analysis"
        >
          <el-icon><TrendCharts /></el-icon>
          查看分析
        </el-button>
        <el-button 
          type="success" 
          size="large" 
          @click="navigateToNutrition"
          class="action-button"
          :loading="actionLoading.nutrition"
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
      
      <div class="weather-widget" v-if="weather">
        <div class="weather-icon">{{ weather.icon }}</div>
        <div class="weather-info">
          <div class="temperature">{{ weather.temperature }}°C</div>
          <div class="weather-desc">{{ weather.description }}</div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
// 优化后的组件
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Plus, TrendCharts, Apple, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fitnessApi } from '../api/fitness'

const router = useRouter()
const userStore = useUserStore()

const currentUser = computed(() => userStore.currentUser)
const isAnimated = ref(false)
const pulseMotivation = ref(false)
const quoteLoading = ref(false)
const actionLoading = ref({
  training: false,
  analysis: false,
  nutrition: false
})

const weather = ref(null)
const currentQuote = ref({
  text: "今天的努力是明天更强的基础",
  author: "健身格言"
})

// 优化励志格言，减少内存占用
const motivationalQuotes = [
  { text: "今天的努力是明天更强的基础", author: "健身格言" },
  { text: "坚持不是一时的热情，而是永恒的信念", author: "励志名言" },
  { text: "每一次训练都是对更好自己的投资", author: "健身哲学" },
  { text: "汗水是最好的化妆品，坚持是最美的姿态", author: "运动美学" },
  { text: "强健的体魄是灵魂的客厅，病弱的身体是灵魂的监狱", author: "培根" },
  { text: "生命在于运动，健康在于坚持", author: "健康箴言" },
  { text: "今天的汗水，明天的笑容", author: "健身感悟" },
  { text: "没有天生的强者，只有不懈的努力", author: "成功学" }
]

// 缓存统计数据，避免频繁重新计算
const quickStats = ref([])
const statsCache = ref({
  data: null,
  timestamp: 0,
  ttl: 300000 // 5分钟缓存
})

// 优化时间计算，避免每次重新计算
const currentTime = ref(new Date())
let timeInterval = null

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
  const emojis = ["🌙", "☀️", "🌞", "🌅", "🌆", "🌙"]
  const indexes = [0, 1, 2, 3, 4, 5]
  const timeRanges = [6, 12, 14, 18, 22, 24]
  
  for (let i = 0; i < timeRanges.length; i++) {
    if (hour < timeRanges[i]) {
      return emojis[indexes[i]]
    }
  }
  return emojis[0]
})

const motivationalSubtitle = computed(() => {
  const hour = currentTime.value.getHours()
  const subtitles = [
    { start: 0, end: 12, text: "新的一天，新的开始！让我们用充满活力的训练开启美好的一天" },
    { start: 12, end: 18, text: "下午时光正好！继续保持训练的节奏，为今天画上完美的句号" },
    { start: 18, end: 24, text: "晚上训练有助于放松身心，让我们一起查看今天的训练状态和进度" }
  ]
  
  return subtitles.find(s => hour >= s.start && hour < s.end)?.text || subtitles[0].text
})

const navigateWithLoading = async (route, loadingKey, successMessage) => {
  if (actionLoading.value[loadingKey]) return
  
  actionLoading.value[loadingKey] = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    router.push(`/${route}`)
    ElMessage.success(successMessage)
  } catch (error) {
    console.error('导航失败:', error)
    ElMessage.error('跳转失败，请重试')
  } finally {
    actionLoading.value[loadingKey] = false
  }
}

const navigateToTraining = () => navigateWithLoading('training-data', 'training', '正在跳转到训练记录页面...')
const navigateToAnalysis = () => navigateWithLoading('load-analysis', 'analysis', '正在跳转到数据分析页面...')
const navigateToNutrition = () => navigateWithLoading('nutrition', 'nutrition', '正在跳转到营养记录页面...')

const refreshQuote = async () => {
  if (quoteLoading.value) return
  
  quoteLoading.value = true
  pulseMotivation.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    const availableQuotes = motivationalQuotes.filter(q => q.text !== currentQuote.value.text)
    if (availableQuotes.length > 0) {
      const randomQuote = availableQuotes[Math.floor(Math.random() * availableQuotes.length)]
      currentQuote.value = randomQuote
      ElMessage.success('已更换励志格言')
    }
  } catch (error) {
    console.error('更换励志格言失败:', error)
    ElMessage.error('更换失败，请重试')
  } finally {
    quoteLoading.value = false
    setTimeout(() => {
      pulseMotivation.value = false
    }, 600)
  }
}

const fetchWeather = async () => {
  try {
    // 模拟天气数据，实际项目中应该调用真实API
    const weatherData = {
      temperature: Math.floor(Math.random() * 15) + 15,
      description: ['晴朗', '多云', '阴天', '微风'][Math.floor(Math.random() * 4)],
      icon: ['☀️', '⛅', '☁️', '🌤️'][Math.floor(Math.random() * 4)]
    }
    weather.value = weatherData
  } catch (error) {
    console.error('获取天气信息失败:', error)
    // 不显示错误消息，因为天气不是核心功能
  }
}

const fetchUserStats = async () => {
  const now = Date.now()
  
  // 检查缓存
  if (statsCache.value.data && 
      statsCache.value.timestamp && 
      now - statsCache.value.timestamp < statsCache.value.ttl) {
    quickStats.value = statsCache.value.data
    return
  }
  
  try {
    // 从API获取用户统计数据
    const response = await fitnessApi.getUserStatsOverview()
    
    if (response.data) {
      const stats = [
        { 
          label: "本周训练", 
          value: (response.data.weeklyTrainingCount || 0).toString(), 
          change: `+${response.data.weeklyChange || 0}`, 
          changeType: response.data.weeklyChange >= 0 ? "positive" : "negative" 
        },
        { 
          label: "总训练时长", 
          value: `${(response.data.totalTrainingHours || 0).toFixed(1)}h`, 
          change: `+${(response.data.trainingHoursChange || 0).toFixed(1)}h`, 
          changeType: response.data.trainingHoursChange >= 0 ? "positive" : "negative" 
        },
        { 
          label: "消耗卡路里", 
          value: (response.data.totalCalories || 0).toLocaleString(), 
          change: `+${response.data.caloriesChange || 0}`, 
          changeType: response.data.caloriesChange >= 0 ? "positive" : "negative" 
        },
        { 
          label: "完成目标", 
          value: `${response.data.goalCompletionRate || 0}%`, 
          change: `+${response.data.goalChange || 0}%`, 
          changeType: response.data.goalChange >= 0 ? "positive" : "negative" 
        }
      ]
      
      quickStats.value = stats
      statsCache.value = {
        data: stats,
        timestamp: now,
        ttl: 300000 // 5分钟缓存
      }
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
    ElMessage.error('获取统计数据失败')
    
    // 如果API调用失败，使用默认值避免页面空白
    const defaultStats = [
      { 
        label: "本周训练", 
        value: "0", 
        change: "0", 
        changeType: "positive" 
      },
      { 
        label: "总训练时长", 
        value: "0.0h", 
        change: "0.0h", 
        changeType: "positive" 
      },
      { 
        label: "消耗卡路里", 
        value: "0", 
        change: "0", 
        changeType: "positive" 
      },
      { 
        label: "完成目标", 
        value: "0%", 
        change: "0%", 
        changeType: "positive" 
      }
    ]
    
    quickStats.value = defaultStats
    statsCache.value = {
      data: defaultStats,
      timestamp: now,
      ttl: 300000 // 5分钟缓存
    }
  }
}

// 优化日期格式化
const formatDate = (date) => {
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    }).format(date)
  } catch (error) {
    console.error('日期格式化失败:', error)
    return date.toLocaleDateString('zh-CN')
  }
}

onMounted(() => {
  // 启动动画
  setTimeout(() => {
    isAnimated.value = true
  }, 100)
  
  // 初始化数据
  fetchWeather()
  fetchUserStats()
  
  // 更新当前时间
  timeInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 60000) // 每分钟更新一次
  
  // 每30秒自动刷新励志格言
  const quoteInterval = setInterval(() => {
    refreshQuote()
  }, 30000)
  
  // 清理定时器
  onUnmounted(() => {
    if (timeInterval) {
      clearInterval(timeInterval)
    }
    if (quoteInterval) {
      clearInterval(quoteInterval)
    }
  })
})

// 监听用户变化，刷新统计数据
watch(currentUser, (newUser) => {
  if (newUser) {
    // 清除缓存，重新获取数据
    statsCache.value = {
      data: null,
      timestamp: 0,
      ttl: 300000
    }
    fetchUserStats()
  }
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
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(147, 51, 234, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.welcome-section.animated {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.welcome-header {
  text-align: left;
}

.welcome-title {
  font-size: 2.8rem;
  font-weight: 800;
  color: var(--text-primary, #ffffff);
  margin: 0 0 16px 0;
  line-height: 1.2;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.greeting {
  background: linear-gradient(135deg, var(--color-primary, #8020ff), var(--color-accent, #00f2fe));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.username {
  color: var(--text-primary, #ffffff);
  position: relative;
}

.username::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary, #8020ff), var(--color-accent, #00f2fe));
  border-radius: 2px;
  animation: underlineGlow 2s ease-in-out infinite;
}

@keyframes underlineGlow {
  0%, 100% { opacity: 0.6; transform: scaleX(0.8); }
  50% { opacity: 1; transform: scaleX(1); }
}

.emoji {
  font-size: 2rem;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.welcome-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary, #8888aa);
  margin: 0;
  font-weight: 400;
  line-height: 1.6;
  max-width: 600px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 20px;
  padding: 24px;
  background: rgba(18, 18, 37, 0.5);
  border-radius: 16px;
  border: 1px solid var(--border-light, rgba(112, 0, 255, 0.1));
  backdrop-filter: blur(5px);
}

.stat-item {
  text-align: center;
  padding: 16px 12px;
  background: rgba(18, 18, 37, 0.8);
  border-radius: 12px;
  border: 1px solid var(--border-light, rgba(112, 0, 255, 0.1));
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-light, 0 0 10px rgba(112, 0, 255, 0.2));
  background: rgba(18, 18, 37, 0.95);
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary, #ffffff);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary, #8888aa);
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-change {
  font-size: 0.85rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.stat-change.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.stat-change.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.welcome-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.action-button {
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.action-button:hover::before {
  width: 300px;
  height: 300px;
}

.primary-action {
  background: linear-gradient(135deg, var(--color-primary, #8020ff), var(--color-secondary, #ff00ff));
  border: none;
  color: white;
  box-shadow: 0 0 15px rgba(128, 32, 255, 0.4);
}

.primary-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 25px rgba(128, 32, 255, 0.6);
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
  backdrop-filter: blur(10px);
  position: relative;
  transition: all 0.3s ease;
}

.daily-motivation.pulse {
  animation: pulse 0.6s ease-in-out;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.motivation-content {
  margin-bottom: 20px;
}

.motivation-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.motivation-quote {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary, #ffffff);
  margin-bottom: 12px;
  font-style: italic;
  line-height: 1.5;
}

.motivation-author {
  font-size: 0.95rem;
  color: var(--text-secondary, #8888aa);
  font-weight: 500;
  font-style: normal;
}

.motivation-date {
  font-size: 0.9rem;
  color: var(--text-secondary, #8888aa);
  font-weight: 500;
  margin-bottom: 16px;
}

.motivation-actions {
  display: flex;
  justify-content: center;
}

.quote-refresh-btn {
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.weather-widget {
  background: rgba(18, 18, 37, 0.8);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.weather-widget:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-light, 0 0 10px rgba(112, 0, 255, 0.2));
}

.weather-icon {
  font-size: 2.5rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.weather-info {
  flex: 1;
}

.temperature {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary, #ffffff);
  margin-bottom: 4px;
}

.weather-desc {
  font-size: 0.95rem;
  color: var(--text-secondary, #8888aa);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .welcome-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .welcome-visual {
    min-width: auto;
    flex-direction: row;
    justify-content: space-between;
  }
  
  .daily-motivation {
    flex: 1;
    margin-right: 20px;
  }
  
  .weather-widget {
    flex-shrink: 0;
    min-width: 200px;
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
    font-size: 1.1rem;
  }
  
  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 20px;
  }
  
  .welcome-actions {
    justify-content: center;
  }
  
  .action-button {
    flex: 1;
    min-width: 140px;
  }
  
  .welcome-visual {
    flex-direction: column;
  }
  
  .daily-motivation {
    margin-right: 0;
    margin-bottom: 0;
  }
  
  .weather-widget {
    width: 100%;
    justify-content: center;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .welcome-section {
    padding: 20px 16px;
  }
  
  .welcome-title {
    font-size: 1.8rem;
  }
  
  .quick-stats {
    grid-template-columns: 1fr;
  }
  
  .welcome-actions {
    flex-direction: column;
  }
  
  .action-button {
    width: 100%;
  }
}
</style>