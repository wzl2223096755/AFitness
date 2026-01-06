<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h1 class="logo">🏋️ AFitness</h1>
        <span class="role-badge">管理员</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item">
          <span class="icon">📊</span><span>控制台</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item">
          <span class="icon">👥</span><span>用户管理</span>
        </router-link>
        <router-link to="/admin/stats" class="nav-item">
          <span class="icon">📈</span><span>系统统计</span>
        </router-link>
        <router-link to="/admin/audit-logs" class="nav-item">
          <span class="icon">📋</span><span>审计日志</span>
        </router-link>
        <router-link to="/admin/settings" class="nav-item">
          <span class="icon">⚙️</span><span>系统设置</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <span class="icon">🚪</span><span>退出登录</span>
        </button>
      </div>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <slot name="header"><h2>管理后台</h2></slot>
        <div class="header-info">
          <span class="username">{{ username }}</span>
        </div>
      </header>
      <div class="admin-content">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || 'Admin')

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; background: #0f0f1a; }
.admin-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: 1px solid rgba(233, 69, 96, 0.2);
  display: flex;
  flex-direction: column;
}
.sidebar-header { padding: 24px; border-bottom: 1px solid rgba(233, 69, 96, 0.1); }
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
.sidebar-nav { flex: 1; padding: 16px; }
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
.nav-item:hover { background: rgba(233, 69, 96, 0.1); color: #f8fafc; }
.nav-item.router-link-active {
  background: linear-gradient(135deg, rgba(233, 69, 96, 0.2), rgba(243, 156, 18, 0.1));
  color: #e94560;
  border: 1px solid rgba(233, 69, 96, 0.3);
}
.sidebar-footer { padding: 16px; border-top: 1px solid rgba(233, 69, 96, 0.1); }
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
}
.admin-main { flex: 1; display: flex; flex-direction: column; }
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}
.admin-header h2 { color: #f8fafc; font-size: 20px; margin: 0; }
.header-info { color: #94a3b8; }
.username {
  padding: 6px 12px;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 6px;
  color: #e94560;
}
.admin-content { flex: 1; overflow-y: auto; }
</style>
