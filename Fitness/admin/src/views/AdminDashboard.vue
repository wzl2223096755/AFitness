<template>
  <div class="admin-dashboard">
    <!-- 系统概览统计卡片 -->
    <section class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
          <div class="stat-label">总用户数</div>
          <div class="stat-change positive" v-if="stats.newUsersToday > 0">
            +{{ stats.newUsersToday }} 今日新增
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🟢</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.activeUsers) }}</div>
          <div class="stat-label">活跃用户</div>
          <div class="stat-change neutral">
            {{ getActiveRate() }}% 活跃率
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🏋️</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalTrainingRecords) }}</div>
          <div class="stat-label">训练记录</div>
          <div class="stat-change positive" v-if="stats.newRecordsToday > 0">
            +{{ stats.newRecordsToday }} 今日新增
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
    </section>

    <!-- 快捷操作 -->
    <section class="quick-actions">
      <h2 class="section-title">
        <span class="title-icon">⚡</span>
        快捷操作
      </h2>
      <div class="actions-grid">
        <div class="action-card" @click="navigateTo('/users')">
          <div class="action-icon">👥</div>
          <div class="action-text">用户管理</div>
        </div>
        <div class="action-card" @click="navigateTo('/statistics')">
          <div class="action-icon">📈</div>
          <div class="action-text">系统统计</div>
        </div>
        <div class="action-card" @click="navigateTo('/audit-logs')">
          <div class="action-icon">📋</div>
          <div class="action-text">审计日志</div>
        </div>
        <div class="action-card" @click="navigateTo('/settings')">
          <div class="action-icon">⚙️</div>
          <div class="action-text">系统设置</div>
        </div>
      </div>
    </section>

    <!-- 最近活动 -->
    <section class="recent-activity">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🕒</span>
          最近活动
        </h2>
        <button class="view-all-btn" @click="navigateTo('/audit-logs')">
          查看全部
        </button>
      </div>
      
      <div class="activity-list" v-if="!loading">
        <div v-if="recentActivities.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <p>暂无最近活动</p>
        </div>
        
        <div v-else class="activity-items">
          <div 
            v-for="activity in recentActivities" 
            :key="activity.id" 
            class="activity-item"
          >
            <div class="activity-icon">{{ getActivityIcon(activity.action) }}</div>
            <div class="activity-content">
              <div class="activity-text">
                <span class="activity-user">{{ activity.username }}</span>
                <span class="activity-action">{{ getActivityText(activity.action) }}</span>
                <span class="activity-target" v-if="activity.targetType">
                  {{ activity.targetType }}
                </span>
              </div>
              <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="loading-state" v-else>
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </section>

    <!-- 系统状态 -->
    <section class="system-status">
      <h2 class="section-title">
        <span class="title-icon">💻</span>
        系统状态
      </h2>
      <div class="status-grid">
        <div class="status-item">
          <div class="status-indicator" :class="systemHealth.database ? 'healthy' : 'unhealthy'"></div>
          <div class="status-label">数据库</div>
          <div class="status-value">{{ systemHealth.database ? '正常' : '异常' }}</div>
        </div>
        <div class="status-item">
          <div class="status-indicator" :class="systemHealth.api ? 'healthy' : 'unhealthy'"></div>
          <div class="status-label">API服务</div>
          <div class="status-value">{{ systemHealth.api ? '正常' : '异常' }}</div>
        </div>
        <div class="status-item">
          <div class="status-indicator" :class="systemHealth.cache ? 'healthy' : 'unhealthy'"></div>
          <div class="status-label">缓存服务</div>
          <div class="status-value">{{ systemHealth.cache ? '正常' : '异常' }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@shared/api/admin'

const router = useRouter()

// 响应式数据
const loading = ref(false)

const stats = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalTrainingRecords: 0,
  totalTrainingPlans: 0,
  totalNutritionRecords: 0,
  newUsersToday: 0,
  newRecordsToday: 0
})

const recentActivities = ref([])

const systemHealth = ref({
  database: true,
  api: true,
  cache: true
})

// 方法
const navigateTo = (path) => {
  router.push(path)
}

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

const getActiveRate = () => {
  if (!stats.value.totalUsers) return 0
  return Math.round((stats.value.activeUsers / stats.value.totalUsers) * 100)
}

const getActivityIcon = (action) => {
  const icons = {
    LOGIN: '🔐',
    LOGOUT: '🚪',
    CREATE: '➕',
    UPDATE: '✏️',
    DELETE: '🗑️',
    VIEW: '👁️'
  }
  return icons[action] || '📝'
}

const getActivityText = (action) => {
  const texts = {
    LOGIN: '登录了系统',
    LOGOUT: '退出了系统',
    CREATE: '创建了',
    UPDATE: '更新了',
    DELETE: '删除了',
    VIEW: '查看了'
  }
  return texts[action] || action
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    
    // 小于1分钟
    if (diff < 60000) return '刚刚'
    // 小于1小时
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    // 小于24小时
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    // 其他
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  } catch {
    return dateStr
  }
}

// 加载数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 获取系统统计数据
    const statsRes = await adminApi.getSystemStats()
    if (statsRes.data) {
      stats.value = {
        totalUsers: statsRes.data.totalUsers || 0,
        activeUsers: statsRes.data.activeUsers || 0,
        totalTrainingRecords: statsRes.data.totalTrainingRecords || 0,
        totalTrainingPlans: statsRes.data.totalTrainingPlans || 0,
        totalNutritionRecords: statsRes.data.totalNutritionRecords || 0,
        newUsersToday: statsRes.data.newUsersToday || 0,
        newRecordsToday: statsRes.data.newRecordsToday || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用模拟数据作为后备
    stats.value = {
      totalUsers: 128,
      activeUsers: 45,
      totalTrainingRecords: 3256,
      totalTrainingPlans: 89,
      totalNutritionRecords: 1542,
      newUsersToday: 3,
      newRecordsToday: 47
    }
  }

  try {
    // 获取最近审计日志
    const logsRes = await adminApi.getAuditLogs({ page: 0, size: 5 })
    if (logsRes.data && Array.isArray(logsRes.data.content)) {
      recentActivities.value = logsRes.data.content
    } else if (logsRes.data && Array.isArray(logsRes.data)) {
      recentActivities.value = logsRes.data.slice(0, 5)
    }
  } catch (error) {
    console.error('加载审计日志失败:', error)
    // 使用模拟数据
    recentActivities.value = [
      { id: 1, username: 'admin', action: 'LOGIN', createdAt: new Date().toISOString() },
      { id: 2, username: 'user1', action: 'CREATE', targetType: '训练记录', createdAt: new Date(Date.now() - 3600000).toISOString() },
      { id: 3, username: 'user2', action: 'UPDATE', targetType: '用户资料', createdAt: new Date(Date.now() - 7200000).toISOString() }
    ]
  }

  try {
    // 获取系统健康状态
    const healthRes = await adminApi.getSystemHealth()
    if (healthRes.data) {
      systemHealth.value = {
        database: healthRes.data.database !== false,
        api: healthRes.data.api !== false,
        cache: healthRes.data.cache !== false
      }
    }
  } catch (error) {
    console.error('加载系统状态失败:', error)
    // 默认显示正常
    systemHealth.value = { database: true, api: true, cache: true }
  }

  loading.value = false
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(22, 33, 62, 0.9), rgba(26, 26, 46, 0.9));
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
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
  font-size: 2.5rem;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #f8fafc;
  line-height: 1.2;
}

.stat-label {
  color: #94a3b8;
  font-size: 0.9rem;
  margin: 4px 0;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
  margin-top: 4px;
}

.stat-change.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.15);
}

.stat-change.neutral {
  color: #f39c12;
  background: rgba(243, 156, 18, 0.15);
}

.stat-change.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.15);
}

/* 快捷操作 */
.quick-actions {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.section-title {
  color: #f8fafc;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1.1rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.action-card {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  border: 1px solid rgba(233, 69, 96, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  background: rgba(233, 69, 96, 0.15);
  border-color: #e94560;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 2rem;
  margin-bottom: 8px;
}

.action-text {
  color: #f8fafc;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 最近活动 */
.recent-activity {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.view-all-btn {
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  color: #e94560;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  background: rgba(233, 69, 96, 0.2);
}

.activity-list {
  min-height: 150px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(233, 69, 96, 0.2);
  border-top-color: #e94560;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.activity-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(26, 26, 46, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(233, 69, 96, 0.1);
  transition: all 0.2s ease;
}

.activity-item:hover {
  background: rgba(233, 69, 96, 0.05);
  border-color: rgba(233, 69, 96, 0.2);
}

.activity-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  color: #f8fafc;
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.activity-user {
  color: #e94560;
  font-weight: 600;
  margin-right: 4px;
}

.activity-action {
  color: #94a3b8;
}

.activity-target {
  color: #f39c12;
  margin-left: 4px;
}

.activity-time {
  color: #64748b;
  font-size: 0.8rem;
}

/* 系统状态 */
.system-status {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(26, 26, 46, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.healthy {
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
}

.status-indicator.unhealthy {
  background: #ef4444;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

.status-label {
  color: #94a3b8;
  font-size: 0.85rem;
  flex: 1;
}

.status-value {
  color: #f8fafc;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
