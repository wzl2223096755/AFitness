<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h1 class="logo">🏋️ AFitness</h1>
        <span class="role-badge">管理员</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item" :class="{ active: $route.path === '/admin/dashboard' }">
          <span class="icon">📊</span>
          <span>控制台</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }">
          <span class="icon">👥</span>
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/stats" class="nav-item" :class="{ active: $route.path === '/admin/stats' }">
          <span class="icon">📈</span>
          <span>系统统计</span>
        </router-link>
        <router-link to="/admin/audit-logs" class="nav-item" :class="{ active: $route.path === '/admin/audit-logs' }">
          <span class="icon">📋</span>
          <span>审计日志</span>
        </router-link>
        <router-link to="/admin/settings" class="nav-item" :class="{ active: $route.path === '/admin/settings' }">
          <span class="icon">⚙️</span>
          <span>系统设置</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <span class="icon">🚪</span>
          <span>退出登录</span>
        </button>
      </div>
    </aside>
    
    <main class="admin-main">
      <header class="admin-header">
        <h2>管理控制台</h2>
        <div class="header-info">
          <span class="username">{{ username }}</span>
          <span class="time">{{ currentTime }}</span>
        </div>
      </header>
      
      <div class="admin-content">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalUsers }}</span>
              <span class="stat-label">总用户数</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🏃</div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.activeUsers }}</span>
              <span class="stat-label">活跃用户</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📝</div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalRecords }}</span>
              <span class="stat-label">训练记录</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📅</div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.todayLogins }}</span>
              <span class="stat-label">今日登录</span>
            </div>
          </div>
        </div>
        
        <div class="recent-section">
          <h3>最近活动</h3>
          <div class="activity-list">
            <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
              <span class="activity-time">{{ activity.time }}</span>
              <span class="activity-user">{{ activity.user }}</span>
              <span class="activity-action">{{ activity.action }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || 'Admin')
const currentTime = ref('')

const stats = ref({
  totalUsers: 11,
  activeUsers: 8,
  totalRecords: 150,
  todayLogins: 5
})

const recentActivities = ref([
  { time: '10:30', user: 'user01', action: '完成了一次训练记录' },
  { time: '10:15', user: 'user02', action: '更新了个人资料' },
  { time: '09:45', user: 'user03', action: '登录系统' },
  { time: '09:30', user: 'user04', action: '添加了营养记录' },
  { time: '09:00', user: 'admin', action: '系统启动' }
])

let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #0f0f1a;
}

.admin-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: 1px solid rgba(233, 69, 96, 0.2);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #e94560, #f39c12);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(233, 69, 96, 0.2);
  border: 1px solid rgba(233, 69, 96, 0.3);
  border-radius: 20px;
  color: #e94560;
  font-size: 12px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 10px;
  margin-bottom: 8px;
  transition: all 0.3s;
}

.nav-item:hover {
  background: rgba(233, 69, 96, 0.1);
  color: #f8fafc;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(233, 69, 96, 0.2), rgba(243, 156, 18, 0.1));
  color: #e94560;
  border: 1px solid rgba(233, 69, 96, 0.3);
}

.nav-item .icon {
  font-size: 18px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(233, 69, 96, 0.1);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.2);
  border-radius: 10px;
  color: #ff6b6b;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 107, 107, 0.2);
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.admin-header h2 {
  color: #f8fafc;
  font-size: 20px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #94a3b8;
  font-size: 14px;
}

.username {
  padding: 6px 12px;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 6px;
  color: #e94560;
}

.admin-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.9), rgba(22, 33, 62, 0.9));
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #f8fafc;
}

.stat-label {
  color: #94a3b8;
  font-size: 14px;
}

.recent-section {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.9), rgba(22, 33, 62, 0.9));
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 24px;
}

.recent-section h3 {
  color: #f8fafc;
  margin-bottom: 20px;
  font-size: 18px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.activity-time {
  color: #64748b;
  font-size: 13px;
  min-width: 50px;
}

.activity-user {
  color: #e94560;
  font-weight: 500;
  min-width: 80px;
}

.activity-action {
  color: #94a3b8;
  font-size: 14px;
}
</style>
