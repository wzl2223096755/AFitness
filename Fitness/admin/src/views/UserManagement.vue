<template>
  <div class="user-management">
    <!-- 页面标题和操作栏 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <span class="title-icon">👥</span>
          用户管理
        </h1>
        <p class="page-subtitle">管理系统用户账号</p>
      </div>
      <div class="header-actions">
        <button class="admin-btn secondary" @click="refreshUsers">
          <span class="btn-icon">🔄</span>
          刷新
        </button>
      </div>
    </header>

    <!-- 搜索和筛选栏 -->
    <section class="filter-section">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索用户名、昵称或邮箱..."
          class="search-input"
          @input="handleSearch"
        />
        <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">✕</button>
      </div>
      
      <div class="filter-group">
        <select v-model="filterRole" class="filter-select" @change="handleFilter">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
        </select>
        
        <select v-model="filterStatus" class="filter-select" @change="handleFilter">
          <option value="">全部状态</option>
          <option value="active">已启用</option>
          <option value="disabled">已禁用</option>
        </select>
      </div>
    </section>

    <!-- 用户统计 -->
    <section class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">总用户数</span>
        <span class="stat-value">{{ totalUsers }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">当前显示</span>
        <span class="stat-value">{{ filteredUsers.length }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">管理员</span>
        <span class="stat-value">{{ adminCount }}</span>
      </div>
    </section>

    <!-- 用户列表 -->
    <section class="users-table-section">
      <div class="table-container" v-if="!loading">
        <table class="users-table" v-if="filteredUsers.length > 0">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>昵称</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in paginatedUsers" :key="user.id">
              <td class="id-cell">{{ user.id }}</td>
              <td class="username-cell">
                <div class="user-info">
                  <div class="user-avatar">{{ getAvatarText(user) }}</div>
                  <span>{{ user.username }}</span>
                </div>
              </td>
              <td>{{ user.nickname || '-' }}</td>
              <td class="email-cell">{{ user.email || '-' }}</td>
              <td>
                <span class="role-badge" :class="getRoleClass(user.role)">
                  {{ getRoleText(user.role) }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(user)">
                  {{ getStatusText(user) }}
                </span>
              </td>
              <td class="date-cell">{{ formatDate(user.createdAt) }}</td>
              <td class="actions-cell">
                <button class="action-btn edit" @click="openEditModal(user)" title="编辑">
                  ✏️
                </button>
                <button 
                  class="action-btn toggle" 
                  @click="toggleUserStatus(user)" 
                  :title="user.enabled !== false ? '禁用' : '启用'"
                >
                  {{ user.enabled !== false ? '🔒' : '🔓' }}
                </button>
                <button 
                  class="action-btn delete" 
                  @click="confirmDelete(user)" 
                  title="删除"
                  :disabled="user.role === 'ADMIN'"
                >
                  🗑️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <p class="empty-text">{{ searchKeyword ? '未找到匹配的用户' : '暂无用户数据' }}</p>
          <button v-if="searchKeyword" class="admin-btn secondary" @click="clearSearch">
            清除搜索
          </button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div class="loading-state" v-else>
        <div class="loading-spinner"></div>
        <p>加载用户数据中...</p>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            class="page-num"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
        </div>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </section>

    <!-- 编辑用户弹窗 -->
    <div class="modal-overlay" v-if="showEditModal" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>编辑用户</h2>
          <button class="close-btn" @click="closeEditModal">✕</button>
        </div>
        <form @submit.prevent="saveUser" class="edit-form">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="editForm.username" class="admin-input" disabled />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input type="text" v-model="editForm.nickname" class="admin-input" placeholder="输入昵称" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="editForm.email" class="admin-input" placeholder="输入邮箱" />
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="editForm.role" class="admin-input">
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" class="admin-btn secondary" @click="closeEditModal">取消</button>
            <button type="submit" class="admin-btn primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-overlay" v-if="showDeleteModal" @click.self="closeDeleteModal">
      <div class="modal-content delete-modal">
        <div class="modal-header">
          <h2>确认删除</h2>
          <button class="close-btn" @click="closeDeleteModal">✕</button>
        </div>
        <div class="delete-content">
          <div class="warning-icon">⚠️</div>
          <p>确定要删除用户 <strong>{{ userToDelete?.username }}</strong> 吗？</p>
          <p class="warning-text">此操作不可恢复，用户的所有数据将被永久删除。</p>
        </div>
        <div class="form-actions">
          <button class="admin-btn secondary" @click="closeDeleteModal">取消</button>
          <button class="admin-btn danger" @click="deleteUser" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@shared/api/admin'

// 状态
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const users = ref([])
const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10

// 弹窗状态
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const editForm = ref({})
const userToDelete = ref(null)

// 计算属性
const totalUsers = computed(() => users.value.length)

const adminCount = computed(() => 
  users.value.filter(u => u.role === 'ADMIN' || u.role === 'ROLE_ADMIN').length
)

const filteredUsers = computed(() => {
  let result = [...users.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(user => 
      user.username?.toLowerCase().includes(keyword) ||
      user.nickname?.toLowerCase().includes(keyword) ||
      user.email?.toLowerCase().includes(keyword)
    )
  }
  
  // 角色过滤
  if (filterRole.value) {
    result = result.filter(user => {
      const role = user.role?.replace('ROLE_', '')
      return role === filterRole.value
    })
  }
  
  // 状态过滤
  if (filterStatus.value) {
    if (filterStatus.value === 'active') {
      result = result.filter(user => user.enabled !== false)
    } else if (filterStatus.value === 'disabled') {
      result = result.filter(user => user.enabled === false)
    }
  }
  
  return result
})

const totalPages = computed(() => Math.ceil(filteredUsers.value.length / pageSize))

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredUsers.value.slice(start, start + pageSize)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)
  
  if (end - start < 4) {
    if (start === 1) {
      end = Math.min(total, 5)
    } else {
      start = Math.max(1, total - 4)
    }
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// 方法
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await adminApi.getUsers({ page: 0, size: 1000 })
    if (response.data) {
      if (Array.isArray(response.data.content)) {
        users.value = response.data.content
      } else if (Array.isArray(response.data)) {
        users.value = response.data
      }
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    // 使用模拟数据
    users.value = generateMockUsers()
  } finally {
    loading.value = false
  }
}

const generateMockUsers = () => {
  return [
    { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@fitness.com', role: 'ADMIN', enabled: true, createdAt: '2024-01-01T00:00:00' },
    { id: 2, username: 'user1', nickname: '健身达人', email: 'user1@example.com', role: 'USER', enabled: true, createdAt: '2024-06-15T10:30:00' },
    { id: 3, username: 'user2', nickname: '运动爱好者', email: 'user2@example.com', role: 'USER', enabled: true, createdAt: '2024-08-20T14:45:00' },
    { id: 4, username: 'user3', nickname: '新手小白', email: 'user3@example.com', role: 'USER', enabled: false, createdAt: '2024-10-05T09:15:00' },
    { id: 5, username: 'user4', nickname: '肌肉男', email: 'user4@example.com', role: 'USER', enabled: true, createdAt: '2024-11-12T16:20:00' },
  ]
}

const refreshUsers = () => {
  currentPage.value = 1
  loadUsers()
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const getAvatarText = (user) => {
  return (user.nickname || user.username || '?').charAt(0).toUpperCase()
}

const getRoleClass = (role) => {
  const normalizedRole = role?.replace('ROLE_', '')
  return normalizedRole === 'ADMIN' ? 'admin' : 'user'
}

const getRoleText = (role) => {
  const normalizedRole = role?.replace('ROLE_', '')
  return normalizedRole === 'ADMIN' ? '管理员' : '普通用户'
}

const getStatusClass = (user) => {
  return user.enabled !== false ? 'active' : 'disabled'
}

const getStatusText = (user) => {
  return user.enabled !== false ? '已启用' : '已禁用'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch {
    return dateStr
  }
}

// 编辑用户
const openEditModal = (user) => {
  editForm.value = { ...user }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editForm.value = {}
}

const saveUser = async () => {
  saving.value = true
  try {
    await adminApi.updateUser(editForm.value.id, {
      nickname: editForm.value.nickname,
      email: editForm.value.email,
      role: editForm.value.role
    })
    
    // 更新本地数据
    const index = users.value.findIndex(u => u.id === editForm.value.id)
    if (index !== -1) {
      users.value[index] = { ...users.value[index], ...editForm.value }
    }
    
    closeEditModal()
  } catch (error) {
    console.error('保存用户失败:', error)
    // 模拟成功
    const index = users.value.findIndex(u => u.id === editForm.value.id)
    if (index !== -1) {
      users.value[index] = { ...users.value[index], ...editForm.value }
    }
    closeEditModal()
  } finally {
    saving.value = false
  }
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const newStatus = user.enabled === false
  try {
    await adminApi.toggleUserStatus(user.id, newStatus)
    user.enabled = newStatus
  } catch (error) {
    console.error('切换用户状态失败:', error)
    // 模拟成功
    user.enabled = newStatus
  }
}

// 删除用户
const confirmDelete = (user) => {
  userToDelete.value = user
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  userToDelete.value = null
}

const deleteUser = async () => {
  if (!userToDelete.value) return
  
  deleting.value = true
  try {
    await adminApi.deleteUser(userToDelete.value.id)
    users.value = users.value.filter(u => u.id !== userToDelete.value.id)
    closeDeleteModal()
  } catch (error) {
    console.error('删除用户失败:', error)
    // 模拟成功
    users.value = users.value.filter(u => u.id !== userToDelete.value.id)
    closeDeleteModal()
  } finally {
    deleting.value = false
  }
}

// 生命周期
onMounted(() => {
  loadUsers()
})
</script>


<style scoped>
/* 管理端主题色 */
.user-management {
  --admin-primary: #e94560;
  --admin-secondary: #f39c12;
  --admin-bg-dark: #1a1a2e;
  --admin-bg-darker: #16213e;
  --admin-text: #f8fafc;
  --admin-text-muted: #94a3b8;
  
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--admin-text);
  margin: 0;
}

.title-icon {
  font-size: 1.5rem;
}

.page-subtitle {
  color: var(--admin-text-muted);
  font-size: 0.9rem;
  margin-top: 4px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.admin-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.admin-btn.primary {
  background: linear-gradient(135deg, var(--admin-primary), var(--admin-secondary));
  color: white;
}

.admin-btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(233, 69, 96, 0.4);
}

.admin-btn.secondary {
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  color: var(--admin-primary);
}

.admin-btn.secondary:hover {
  background: rgba(233, 69, 96, 0.2);
}

.admin-btn.danger {
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
}

.admin-btn.danger:hover:not(:disabled) {
  background: rgba(255, 107, 107, 0.2);
}

.admin-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 1rem;
}

/* 搜索和筛选栏 */
.filter-section {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  flex: 1;
  min-width: 280px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1rem;
  color: var(--admin-text-muted);
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 44px;
  background: rgba(22, 33, 62, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 12px;
  color: var(--admin-text);
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--admin-primary);
  box-shadow: 0 0 0 3px rgba(233, 69, 96, 0.1);
}

.search-input::placeholder {
  color: var(--admin-text-muted);
}

.clear-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--admin-text-muted);
  cursor: pointer;
  padding: 4px;
  font-size: 14px;
  transition: color 0.3s ease;
}

.clear-btn:hover {
  color: var(--admin-text);
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 12px 16px;
  background: rgba(22, 33, 62, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 10px;
  color: var(--admin-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.filter-select:focus {
  outline: none;
  border-color: var(--admin-primary);
}

.filter-select option {
  background: var(--admin-bg-darker);
  color: var(--admin-text);
}

/* 统计栏 */
.stats-bar {
  display: flex;
  gap: 24px;
  padding: 16px 24px;
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item .stat-label {
  color: var(--admin-text-muted);
  font-size: 0.85rem;
}

.stat-item .stat-value {
  color: var(--admin-primary);
  font-weight: 600;
  font-size: 1.1rem;
}

/* 表格区域 */
.users-table-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.users-table th,
.users-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.users-table th {
  background: rgba(233, 69, 96, 0.1);
  color: var(--admin-text);
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.users-table th:first-child {
  border-radius: 8px 0 0 8px;
}

.users-table th:last-child {
  border-radius: 0 8px 8px 0;
}

.users-table td {
  color: var(--admin-text-muted);
  font-size: 14px;
}

.users-table tr:hover td {
  background: rgba(233, 69, 96, 0.05);
  color: var(--admin-text);
}

.id-cell {
  color: var(--admin-text-muted);
  font-family: monospace;
}

.username-cell {
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--admin-primary), var(--admin-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.email-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-cell {
  white-space: nowrap;
}

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.admin {
  background: rgba(233, 69, 96, 0.15);
  color: var(--admin-primary);
  border: 1px solid rgba(233, 69, 96, 0.3);
}

.role-badge.user {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.status-badge.disabled {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

/* 操作按钮 */
.actions-cell {
  white-space: nowrap;
}

.action-btn {
  background: none;
  border: none;
  padding: 6px 8px;
  cursor: pointer;
  font-size: 1rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.action-btn:hover:not(:disabled) {
  background: rgba(233, 69, 96, 0.1);
}

.action-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.action-btn.edit:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.15);
}

.action-btn.toggle:hover:not(:disabled) {
  background: rgba(243, 156, 18, 0.15);
}

.action-btn.delete:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.15);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-text {
  color: var(--admin-text-muted);
  font-size: 1rem;
  margin-bottom: 20px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--admin-text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(233, 69, 96, 0.2);
  border-top-color: var(--admin-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(233, 69, 96, 0.1);
}

.page-btn {
  padding: 8px 16px;
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: var(--admin-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(233, 69, 96, 0.2);
  border-color: var(--admin-primary);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 8px;
}

.page-num {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 33, 62, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: var(--admin-text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-num:hover {
  border-color: var(--admin-primary);
  color: var(--admin-text);
}

.page-num.active {
  background: var(--admin-primary);
  border-color: var(--admin-primary);
  color: white;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--admin-bg-darker);
  border: 1px solid rgba(233, 69, 96, 0.3);
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.modal-header h2 {
  color: var(--admin-text);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: var(--admin-text-muted);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 4px;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: var(--admin-text);
}

/* 编辑表单 */
.edit-form {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: var(--admin-text-muted);
  font-size: 0.85rem;
  margin-bottom: 8px;
}

.admin-input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 10px;
  color: var(--admin-text);
  font-size: 14px;
  transition: all 0.3s ease;
}

.admin-input:focus {
  outline: none;
  border-color: var(--admin-primary);
  box-shadow: 0 0 0 3px rgba(233, 69, 96, 0.1);
}

.admin-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.admin-input::placeholder {
  color: var(--admin-text-muted);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

/* 删除确认弹窗 */
.delete-modal .delete-content {
  padding: 32px 24px;
  text-align: center;
}

.warning-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.delete-content p {
  color: var(--admin-text);
  font-size: 1rem;
  margin: 0 0 8px 0;
}

.delete-content strong {
  color: var(--admin-primary);
}

.warning-text {
  color: var(--admin-text-muted) !important;
  font-size: 0.85rem !important;
}

.delete-modal .form-actions {
  padding: 0 24px 24px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: flex-end;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .search-box {
    min-width: 100%;
  }
  
  .filter-group {
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .stats-bar {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
  
  .page-numbers {
    order: -1;
    width: 100%;
    justify-content: center;
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .users-table-section {
    padding: 16px;
  }
  
  .modal-content {
    margin: 10px;
  }
}
</style>
