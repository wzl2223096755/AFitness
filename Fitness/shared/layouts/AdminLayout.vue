<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <slot name="logo">
          <h1 class="logo">🏋️ AFitness</h1>
        </slot>
        <span class="role-badge">{{ roleBadge }}</span>
      </div>
      <nav class="sidebar-nav">
        <slot name="navigation">
          <router-link to="/admin/dashboard" class="nav-item">
            <span class="icon">📊</span><span class="nav-text">控制台</span>
          </router-link>
          <router-link to="/admin/users" class="nav-item">
            <span class="icon">👥</span><span class="nav-text">用户管理</span>
          </router-link>
          <router-link to="/admin/stats" class="nav-item">
            <span class="icon">📈</span><span class="nav-text">系统统计</span>
          </router-link>
          <router-link to="/admin/audit-logs" class="nav-item">
            <span class="icon">📋</span><span class="nav-text">审计日志</span>
          </router-link>
          <router-link to="/admin/settings" class="nav-item">
            <span class="icon">⚙️</span><span class="nav-text">系统设置</span>
          </router-link>
        </slot>
      </nav>
      <div class="sidebar-footer">
        <slot name="sidebar-footer">
          <button @click="handleLogout" class="logout-btn">
            <span class="icon">🚪</span><span class="nav-text">退出登录</span>
          </button>
        </slot>
      </div>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <slot name="header">
          <h2>{{ title }}</h2>
        </slot>
        <div class="header-info">
          <slot name="header-right">
            <span class="username">{{ username }}</span>
          </slot>
        </div>
      </header>
      <div class="admin-content">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: {
    type: String,
    default: '管理后台'
  },
  roleBadge: {
    type: String,
    default: '管理员'
  },
  username: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['logout'])
const router = useRouter()

const username = ref(props.username || (typeof localStorage !== 'undefined' ? localStorage.getItem('username') : '') || 'Admin')

const handleLogout = () => {
  emit('logout')
  if (typeof localStorage !== 'undefined') {
    localStorage.clear()
  }
  router.push('/login')
}
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
  flex-shrink: 0;
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
  background-clip: text;
  margin: 0 0 8px;
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
  margin-bottom: 8px;
  transition: all 0.3s;
}

.nav-item:hover {
  background: rgba(233, 69, 96, 0.1);
  color: #f8fafc;
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, rgba(233, 69, 96, 0.2), rgba(243, 156, 18, 0.1));
  color: #e94560;
  border: 1px solid rgba(233, 69, 96, 0.3);
}

.icon {
  font-size: 18px;
  flex-shrink: 0;
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
  font-size: 14px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 107, 107, 0.2);
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
  flex-shrink: 0;
}

.admin-header h2 {
  color: #f8fafc;
  font-size: 20px;
  margin: 0;
}

.header-info {
  color: #94a3b8;
}

.username {
  padding: 6px 12px;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 6px;
  color: #e94560;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .admin-sidebar.open {
    transform: translateX(0);
  }
  
  .admin-header {
    padding: 16px;
  }
  
  .admin-content {
    padding: 16px;
  }
  
  .nav-text {
    display: none;
  }
}
</style>
