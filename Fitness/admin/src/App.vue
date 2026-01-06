<template>
  <div class="admin-app-container">
    <!-- 登录页面直接显示内容 -->
    <template v-if="isLoginPage">
      <router-view v-slot="{ Component }">
        <transition name="slide-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </template>

    <!-- 管理端主布局 -->
    <template v-else>
      <!-- 侧边栏 -->
      <aside class="admin-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
        <div class="sidebar-header">
          <div class="logo">
            <span class="logo-icon">⚙️</span>
            <span class="logo-text" v-if="!sidebarCollapsed">AFitness Admin</span>
          </div>
          <button class="collapse-btn" @click="toggleSidebar">
            <span>{{ sidebarCollapsed ? '→' : '←' }}</span>
          </button>
        </div>
        
        <nav class="sidebar-nav">
          <router-link to="/dashboard" class="nav-item" :class="{ active: activeMenu === '/dashboard' }">
            <span class="nav-icon">📊</span>
            <span class="nav-text" v-if="!sidebarCollapsed">仪表盘</span>
          </router-link>
          <router-link to="/users" class="nav-item" :class="{ active: activeMenu === '/users' }">
            <span class="nav-icon">👥</span>
            <span class="nav-text" v-if="!sidebarCollapsed">用户管理</span>
          </router-link>
          <router-link to="/statistics" class="nav-item" :class="{ active: activeMenu === '/statistics' }">
            <span class="nav-icon">📈</span>
            <span class="nav-text" v-if="!sidebarCollapsed">系统统计</span>
          </router-link>
          <router-link to="/audit-logs" class="nav-item" :class="{ active: activeMenu === '/audit-logs' }">
            <span class="nav-icon">📋</span>
            <span class="nav-text" v-if="!sidebarCollapsed">审计日志</span>
          </router-link>
          <router-link to="/settings" class="nav-item" :class="{ active: activeMenu === '/settings' }">
            <span class="nav-icon">⚙️</span>
            <span class="nav-text" v-if="!sidebarCollapsed">系统设置</span>
          </router-link>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info" v-if="!sidebarCollapsed">
            <span class="user-avatar">👤</span>
            <span class="user-name">{{ currentUser?.username || '管理员' }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" :title="sidebarCollapsed ? '退出登录' : ''">
            <span class="logout-icon">🚪</span>
            <span class="logout-text" v-if="!sidebarCollapsed">退出登录</span>
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="admin-main">
        <header class="admin-header">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
          <div class="header-actions">
            <span class="admin-badge">管理员</span>
          </div>
        </header>
        
        <div class="admin-content">
          <router-view v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)
const currentUser = ref(null)

const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')

const currentPageTitle = computed(() => {
  const routeMap = {
    '/dashboard': '管理仪表盘',
    '/users': '用户管理',
    '/statistics': '系统统计',
    '/audit-logs': '审计日志',
    '/settings': '系统设置'
  }
  return routeMap[route.path] || '管理后台'
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleLogout = async () => {
  try {
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(async () => {
  try {
    if (userStore.token) {
      await userStore.getCurrentUser()
      currentUser.value = userStore.user
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})
</script>

<style scoped>
.admin-app-container {
  display: flex;
  min-height: 100vh;
  background: #1a1a2e;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 侧边栏 */
.admin-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #16213e 0%, #1a1a2e 100%);
  border-right: 1px solid rgba(233, 69, 96, 0.2);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  z-index: 100;
}

.admin-sidebar.collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(233, 69, 96, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #e94560, #f39c12);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.collapse-btn {
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  color: #e94560;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(233, 69, 96, 0.2);
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 10px;
  margin-bottom: 6px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #e94560, #f39c12);
  border-radius: 0 2px 2px 0;
  transition: height 0.3s ease;
}

.nav-item:hover {
  background: rgba(233, 69, 96, 0.1);
  color: #f8fafc;
}

.nav-item.active {
  background: rgba(233, 69, 96, 0.15);
  color: #f8fafc;
}

.nav-item.active::before {
  height: 60%;
}

.nav-icon {
  font-size: 18px;
  min-width: 24px;
  text-align: center;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(233, 69, 96, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 10px;
  margin-bottom: 12px;
}

.user-avatar {
  font-size: 20px;
}

.user-name {
  font-size: 14px;
  color: #f8fafc;
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 12px;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(255, 107, 107, 0.2);
  border-color: #ff6b6b;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.3s ease;
}

.admin-sidebar.collapsed + .admin-main {
  margin-left: 70px;
}

.admin-header {
  padding: 20px 30px;
  background: rgba(22, 33, 62, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(233, 69, 96, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0;
}

.admin-badge {
  background: linear-gradient(135deg, #e94560, #f39c12);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.admin-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 70px;
  }
  
  .admin-sidebar .logo-text,
  .admin-sidebar .nav-text,
  .admin-sidebar .user-info,
  .admin-sidebar .logout-text {
    display: none;
  }
  
  .admin-main {
    margin-left: 70px;
  }
  
  .admin-header {
    padding: 16px 20px;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .admin-content {
    padding: 20px;
  }
}
</style>
