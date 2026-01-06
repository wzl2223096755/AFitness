<template>
  <div class="admin-page">
    <AdminLayout>
      <template #header>
        <h2>用户管理</h2>
      </template>
      <div class="page-content">
        <div class="toolbar">
          <input type="text" v-model="searchQuery" placeholder="搜索用户..." class="search-input" />
          <button class="btn-primary">添加用户</button>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td><span class="role-tag" :class="user.role.toLowerCase()">{{ user.role }}</span></td>
              <td><span class="status-dot active"></span> 活跃</td>
              <td>
                <button class="btn-sm">编辑</button>
                <button class="btn-sm btn-danger">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AdminLayout>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminLayout from './AdminLayout.vue'

const searchQuery = ref('')
const users = ref([
  { id: 1, username: 'admin', email: 'admin@afitness.com', role: 'ADMIN' },
  { id: 2, username: 'user01', email: 'user01@example.com', role: 'USER' },
  { id: 3, username: 'user02', email: 'user02@example.com', role: 'USER' },
  { id: 4, username: 'user03', email: 'user03@example.com', role: 'USER' },
  { id: 5, username: 'user04', email: 'user04@example.com', role: 'USER' }
])
</script>

<style scoped>
.page-content { padding: 24px; }
.toolbar { display: flex; gap: 16px; margin-bottom: 24px; }
.search-input {
  flex: 1;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
}
.btn-primary {
  padding: 12px 24px;
  background: linear-gradient(135deg, #e94560, #d63850);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  overflow: hidden;
}
.data-table th, .data-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}
.data-table th { color: #94a3b8; font-weight: 500; }
.data-table td { color: #f8fafc; }
.role-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}
.role-tag.admin { background: rgba(233, 69, 96, 0.2); color: #e94560; }
.role-tag.user { background: rgba(99, 102, 241, 0.2); color: #818cf8; }
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}
.status-dot.active { background: #10b981; }
.btn-sm {
  padding: 6px 12px;
  background: rgba(99, 102, 241, 0.2);
  border: none;
  border-radius: 6px;
  color: #818cf8;
  cursor: pointer;
  margin-right: 8px;
}
.btn-sm.btn-danger { background: rgba(255, 107, 107, 0.2); color: #ff6b6b; }
</style>
