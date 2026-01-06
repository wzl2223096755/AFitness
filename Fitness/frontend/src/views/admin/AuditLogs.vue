<template>
  <AdminLayout>
    <template #header><h2>审计日志</h2></template>
    <div class="logs-page">
      <div class="filters">
        <select v-model="filterType" class="filter-select">
          <option value="">全部类型</option>
          <option value="LOGIN">登录</option>
          <option value="LOGOUT">登出</option>
          <option value="CREATE">创建</option>
          <option value="UPDATE">更新</option>
          <option value="DELETE">删除</option>
        </select>
        <input type="date" v-model="filterDate" class="filter-input" />
      </div>
      <div class="logs-list">
        <div v-for="log in logs" :key="log.id" class="log-item">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-type" :class="log.type.toLowerCase()">{{ log.type }}</span>
          <span class="log-user">{{ log.user }}</span>
          <span class="log-action">{{ log.action }}</span>
          <span class="log-ip">{{ log.ip }}</span>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref } from 'vue'
import AdminLayout from './AdminLayout.vue'

const filterType = ref('')
const filterDate = ref('')
const logs = ref([
  { id: 1, time: '2026-01-05 10:30:00', type: 'LOGIN', user: 'user01', action: '用户登录', ip: '192.168.1.100' },
  { id: 2, time: '2026-01-05 10:25:00', type: 'CREATE', user: 'user02', action: '创建训练记录', ip: '192.168.1.101' },
  { id: 3, time: '2026-01-05 10:20:00', type: 'UPDATE', user: 'user03', action: '更新个人资料', ip: '192.168.1.102' },
  { id: 4, time: '2026-01-05 10:15:00', type: 'LOGIN', user: 'admin', action: '管理员登录', ip: '192.168.1.1' },
  { id: 5, time: '2026-01-05 10:00:00', type: 'DELETE', user: 'user01', action: '删除营养记录', ip: '192.168.1.100' }
])
</script>

<style scoped>
.logs-page { padding: 24px; }
.filters { display: flex; gap: 16px; margin-bottom: 24px; }
.filter-select, .filter-input {
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
}
.logs-list { display: flex; flex-direction: column; gap: 8px; }
.log-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.1);
  border-radius: 10px;
}
.log-time { color: #64748b; font-size: 13px; min-width: 150px; }
.log-type {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  min-width: 70px;
  text-align: center;
}
.log-type.login { background: rgba(16, 185, 129, 0.2); color: #10b981; }
.log-type.logout { background: rgba(245, 158, 11, 0.2); color: #f59e0b; }
.log-type.create { background: rgba(99, 102, 241, 0.2); color: #818cf8; }
.log-type.update { background: rgba(6, 182, 212, 0.2); color: #06b6d4; }
.log-type.delete { background: rgba(255, 107, 107, 0.2); color: #ff6b6b; }
.log-user { color: #e94560; font-weight: 500; min-width: 80px; }
.log-action { color: #f8fafc; flex: 1; }
.log-ip { color: #64748b; font-size: 13px; }
</style>
