<template>
  <div class="audit-logs">
    <!-- 页面标题和操作栏 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <span class="title-icon">📋</span>
          审计日志
        </h1>
        <p class="page-subtitle">查看系统操作日志记录</p>
      </div>
      <div class="header-actions">
        <button class="admin-btn secondary" @click="exportLogs" :disabled="exporting">
          <span class="btn-icon">📥</span>
          {{ exporting ? '导出中...' : '导出日志' }}
        </button>
        <button class="admin-btn secondary" @click="refreshLogs">
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
          placeholder="搜索用户名或描述..."
          class="search-input"
          @input="handleSearch"
        />
        <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">✕</button>
      </div>
      
      <div class="filter-group">
        <select v-model="filterAction" class="filter-select" @change="handleFilter">
          <option value="">全部操作</option>
          <option value="LOGIN">登录</option>
          <option value="LOGIN_FAILED">登录失败</option>
          <option value="LOGOUT">登出</option>
          <option value="REGISTER">注册</option>
          <option value="PASSWORD_CHANGE">密码修改</option>
          <option value="PROFILE_UPDATE">资料更新</option>
          <option value="DATA_CREATE">数据创建</option>
          <option value="DATA_UPDATE">数据更新</option>
          <option value="DATA_DELETE">数据删除</option>
          <option value="DATA_RESTORE">数据恢复</option>
          <option value="ADMIN_ACTION">管理操作</option>
          <option value="DATA_EXPORT">数据导出</option>
        </select>
        
        <select v-model="filterResult" class="filter-select" @change="handleFilter">
          <option value="">全部结果</option>
          <option value="SUCCESS">成功</option>
          <option value="FAILURE">失败</option>
          <option value="BLOCKED">已阻止</option>
        </select>

        <input
          type="date"
          v-model="filterStartDate"
          class="filter-input"
          @change="handleFilter"
          placeholder="开始日期"
        />
        
        <input
          type="date"
          v-model="filterEndDate"
          class="filter-input"
          @change="handleFilter"
          placeholder="结束日期"
        />
      </div>
    </section>

    <!-- 统计栏 -->
    <section class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">总日志数</span>
        <span class="stat-value">{{ totalLogs }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">当前显示</span>
        <span class="stat-value">{{ filteredLogs.length }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">成功操作</span>
        <span class="stat-value success">{{ successCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">失败操作</span>
        <span class="stat-value failure">{{ failureCount }}</span>
      </div>
    </section>

    <!-- 日志列表 -->
    <section class="logs-table-section">
      <div class="table-container" v-if="!loading">
        <table class="logs-table" v-if="filteredLogs.length > 0">
          <thead>
            <tr>
              <th>ID</th>
              <th>时间</th>
              <th>用户</th>
              <th>操作类型</th>
              <th>描述</th>
              <th>资源</th>
              <th>结果</th>
              <th>IP地址</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in paginatedLogs" :key="log.id">
              <td class="id-cell">{{ log.id }}</td>
              <td class="time-cell">{{ formatDateTime(log.createdAt) }}</td>
              <td class="username-cell">
                <div class="user-info">
                  <div class="user-avatar">{{ getAvatarText(log) }}</div>
                  <span>{{ log.username || '-' }}</span>
                </div>
              </td>
              <td>
                <span class="action-badge" :class="getActionClass(log.action)">
                  {{ getActionIcon(log.action) }} {{ getActionText(log.action) }}
                </span>
              </td>
              <td class="description-cell">{{ log.description || '-' }}</td>
              <td class="resource-cell">
                <span v-if="log.resourceType">
                  {{ log.resourceType }}
                  <span v-if="log.resourceId" class="resource-id">#{{ log.resourceId }}</span>
                </span>
                <span v-else>-</span>
              </td>
              <td>
                <span class="result-badge" :class="getResultClass(log.result)">
                  {{ getResultText(log.result) }}
                </span>
              </td>
              <td class="ip-cell">{{ log.ipAddress || '-' }}</td>
              <td class="actions-cell">
                <button class="action-btn view" @click="openDetailModal(log)" title="查看详情">
                  👁️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <p class="empty-text">{{ searchKeyword || filterAction || filterResult ? '未找到匹配的日志' : '暂无审计日志' }}</p>
          <button v-if="searchKeyword || filterAction || filterResult" class="admin-btn secondary" @click="clearFilters">
            清除筛选
          </button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div class="loading-state" v-else>
        <div class="loading-spinner"></div>
        <p>加载审计日志中...</p>
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

    <!-- 日志详情弹窗 -->
    <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h2>日志详情</h2>
          <button class="close-btn" @click="closeDetailModal">✕</button>
        </div>
        <div class="detail-content" v-if="selectedLog">
          <div class="detail-row">
            <span class="detail-label">日志ID</span>
            <span class="detail-value">{{ selectedLog.id }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作时间</span>
            <span class="detail-value">{{ formatDateTime(selectedLog.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用户ID</span>
            <span class="detail-value">{{ selectedLog.userId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用户名</span>
            <span class="detail-value">{{ selectedLog.username || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作类型</span>
            <span class="detail-value">
              <span class="action-badge" :class="getActionClass(selectedLog.action)">
                {{ getActionIcon(selectedLog.action) }} {{ getActionText(selectedLog.action) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作描述</span>
            <span class="detail-value">{{ selectedLog.description || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">资源类型</span>
            <span class="detail-value">{{ selectedLog.resourceType || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">资源ID</span>
            <span class="detail-value">{{ selectedLog.resourceId || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作结果</span>
            <span class="detail-value">
              <span class="result-badge" :class="getResultClass(selectedLog.result)">
                {{ getResultText(selectedLog.result) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">IP地址</span>
            <span class="detail-value">{{ selectedLog.ipAddress || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">请求路径</span>
            <span class="detail-value code">{{ selectedLog.requestPath || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">请求方法</span>
            <span class="detail-value">{{ selectedLog.requestMethod || '-' }}</span>
          </div>
          <div class="detail-row" v-if="selectedLog.errorMessage">
            <span class="detail-label">错误信息</span>
            <span class="detail-value error">{{ selectedLog.errorMessage }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">User Agent</span>
            <span class="detail-value small">{{ selectedLog.userAgent || '-' }}</span>
          </div>
        </div>
        <div class="form-actions">
          <button class="admin-btn secondary" @click="closeDetailModal">关闭</button>
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
const exporting = ref(false)
const logs = ref([])
const searchKeyword = ref('')
const filterAction = ref('')
const filterResult = ref('')
const filterStartDate = ref('')
const filterEndDate = ref('')
const currentPage = ref(1)
const pageSize = 15

// 弹窗状态
const showDetailModal = ref(false)
const selectedLog = ref(null)

// 计算属性
const totalLogs = computed(() => logs.value.length)

const successCount = computed(() => 
  logs.value.filter(l => l.result === 'SUCCESS').length
)

const failureCount = computed(() => 
  logs.value.filter(l => l.result === 'FAILURE' || l.result === 'BLOCKED').length
)

const filteredLogs = computed(() => {
  let result = [...logs.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(log => 
      log.username?.toLowerCase().includes(keyword) ||
      log.description?.toLowerCase().includes(keyword) ||
      log.resourceType?.toLowerCase().includes(keyword)
    )
  }
  
  // 操作类型过滤
  if (filterAction.value) {
    result = result.filter(log => log.action === filterAction.value)
  }
  
  // 结果过滤
  if (filterResult.value) {
    result = result.filter(log => log.result === filterResult.value)
  }
  
  // 日期范围过滤
  if (filterStartDate.value) {
    const startDate = new Date(filterStartDate.value)
    startDate.setHours(0, 0, 0, 0)
    result = result.filter(log => {
      const logDate = new Date(log.createdAt)
      return logDate >= startDate
    })
  }
  
  if (filterEndDate.value) {
    const endDate = new Date(filterEndDate.value)
    endDate.setHours(23, 59, 59, 999)
    result = result.filter(log => {
      const logDate = new Date(log.createdAt)
      return logDate <= endDate
    })
  }
  
  return result
})

const totalPages = computed(() => Math.ceil(filteredLogs.value.length / pageSize))

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredLogs.value.slice(start, start + pageSize)
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
const loadLogs = async () => {
  loading.value = true
  try {
    const response = await adminApi.getAuditLogs({ page: 0, size: 1000 })
    if (response.data) {
      if (Array.isArray(response.data.content)) {
        logs.value = response.data.content
      } else if (Array.isArray(response.data)) {
        logs.value = response.data
      }
    }
  } catch (error) {
    console.error('加载审计日志失败:', error)
    // 使用模拟数据
    logs.value = generateMockLogs()
  } finally {
    loading.value = false
  }
}

const generateMockLogs = () => {
  const actions = ['LOGIN', 'LOGIN_FAILED', 'LOGOUT', 'REGISTER', 'PASSWORD_CHANGE', 'PROFILE_UPDATE', 'DATA_CREATE', 'DATA_DELETE', 'ADMIN_ACTION']
  const results = ['SUCCESS', 'FAILURE', 'SUCCESS', 'SUCCESS', 'SUCCESS'] // 偏向成功
  const users = ['admin', 'user1', 'user2', 'user3', 'fitness_lover']
  const resourceTypes = ['USER', 'TRAINING_RECORD', 'TRAINING_PLAN', 'NUTRITION_RECORD', null]
  
  const mockLogs = []
  const now = new Date()
  
  for (let i = 1; i <= 50; i++) {
    const action = actions[Math.floor(Math.random() * actions.length)]
    const result = action === 'LOGIN_FAILED' ? 'FAILURE' : results[Math.floor(Math.random() * results.length)]
    const user = users[Math.floor(Math.random() * users.length)]
    const resourceType = resourceTypes[Math.floor(Math.random() * resourceTypes.length)]
    
    mockLogs.push({
      id: i,
      userId: Math.floor(Math.random() * 10) + 1,
      username: user,
      action: action,
      description: getDescriptionForAction(action, user),
      resourceType: resourceType,
      resourceId: resourceType ? Math.floor(Math.random() * 100) + 1 : null,
      result: result,
      ipAddress: `192.168.1.${Math.floor(Math.random() * 255)}`,
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
      requestPath: getRequestPathForAction(action),
      requestMethod: getRequestMethodForAction(action),
      errorMessage: result === 'FAILURE' ? '操作失败：权限不足或参数错误' : null,
      createdAt: new Date(now.getTime() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
    })
  }
  
  // 按时间倒序排列
  return mockLogs.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
}

const getDescriptionForAction = (action, user) => {
  const descriptions = {
    'LOGIN': `用户 ${user} 登录成功`,
    'LOGIN_FAILED': `用户 ${user} 登录失败`,
    'LOGOUT': `用户 ${user} 登出系统`,
    'REGISTER': `新用户 ${user} 注册`,
    'PASSWORD_CHANGE': `用户 ${user} 修改密码`,
    'PROFILE_UPDATE': `用户 ${user} 更新个人资料`,
    'DATA_CREATE': `用户 ${user} 创建数据记录`,
    'DATA_DELETE': `用户 ${user} 删除数据记录`,
    'ADMIN_ACTION': `管理员 ${user} 执行管理操作`
  }
  return descriptions[action] || `${user} 执行了 ${action} 操作`
}

const getRequestPathForAction = (action) => {
  const paths = {
    'LOGIN': '/api/v1/auth/login',
    'LOGIN_FAILED': '/api/v1/auth/login',
    'LOGOUT': '/api/v1/auth/logout',
    'REGISTER': '/api/v1/auth/register',
    'PASSWORD_CHANGE': '/api/v1/user/password',
    'PROFILE_UPDATE': '/api/v1/user/profile',
    'DATA_CREATE': '/api/v1/training/records',
    'DATA_DELETE': '/api/v1/training/records',
    'ADMIN_ACTION': '/api/v1/admin/users'
  }
  return paths[action] || '/api/v1/unknown'
}

const getRequestMethodForAction = (action) => {
  const methods = {
    'LOGIN': 'POST',
    'LOGIN_FAILED': 'POST',
    'LOGOUT': 'POST',
    'REGISTER': 'POST',
    'PASSWORD_CHANGE': 'PUT',
    'PROFILE_UPDATE': 'PUT',
    'DATA_CREATE': 'POST',
    'DATA_DELETE': 'DELETE',
    'ADMIN_ACTION': 'PUT'
  }
  return methods[action] || 'GET'
}

const refreshLogs = () => {
  currentPage.value = 1
  loadLogs()
}

const exportLogs = async () => {
  exporting.value = true
  try {
    await adminApi.exportAuditLogs({
      action: filterAction.value,
      result: filterResult.value,
      startDate: filterStartDate.value,
      endDate: filterEndDate.value
    })
    // 模拟导出成功
    alert('日志导出成功！')
  } catch (error) {
    console.error('导出日志失败:', error)
    alert('日志导出功能暂未实现')
  } finally {
    exporting.value = false
  }
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

const clearFilters = () => {
  searchKeyword.value = ''
  filterAction.value = ''
  filterResult.value = ''
  filterStartDate.value = ''
  filterEndDate.value = ''
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const getAvatarText = (log) => {
  return (log.username || '?').charAt(0).toUpperCase()
}

const getActionClass = (action) => {
  const classes = {
    'LOGIN': 'login',
    'LOGIN_FAILED': 'login-failed',
    'LOGOUT': 'logout',
    'REGISTER': 'register',
    'PASSWORD_CHANGE': 'password',
    'PROFILE_UPDATE': 'profile',
    'DATA_CREATE': 'create',
    'DATA_UPDATE': 'update',
    'DATA_DELETE': 'delete',
    'DATA_RESTORE': 'restore',
    'ADMIN_ACTION': 'admin',
    'DATA_EXPORT': 'export'
  }
  return classes[action] || 'default'
}

const getActionIcon = (action) => {
  const icons = {
    'LOGIN': '🔑',
    'LOGIN_FAILED': '🚫',
    'LOGOUT': '🚪',
    'REGISTER': '📝',
    'PASSWORD_CHANGE': '🔐',
    'PROFILE_UPDATE': '👤',
    'DATA_CREATE': '➕',
    'DATA_UPDATE': '✏️',
    'DATA_DELETE': '🗑️',
    'DATA_RESTORE': '♻️',
    'ADMIN_ACTION': '⚙️',
    'DATA_EXPORT': '📤'
  }
  return icons[action] || '📋'
}

const getActionText = (action) => {
  const texts = {
    'LOGIN': '登录',
    'LOGIN_FAILED': '登录失败',
    'LOGOUT': '登出',
    'REGISTER': '注册',
    'PASSWORD_CHANGE': '密码修改',
    'PROFILE_UPDATE': '资料更新',
    'DATA_CREATE': '数据创建',
    'DATA_UPDATE': '数据更新',
    'DATA_DELETE': '数据删除',
    'DATA_RESTORE': '数据恢复',
    'ADMIN_ACTION': '管理操作',
    'DATA_EXPORT': '数据导出'
  }
  return texts[action] || action
}

const getResultClass = (result) => {
  const classes = {
    'SUCCESS': 'success',
    'FAILURE': 'failure',
    'BLOCKED': 'blocked'
  }
  return classes[result] || 'default'
}

const getResultText = (result) => {
  const texts = {
    'SUCCESS': '成功',
    'FAILURE': '失败',
    'BLOCKED': '已阻止'
  }
  return texts[result] || result
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return dateStr
  }
}

// 详情弹窗
const openDetailModal = (log) => {
  selectedLog.value = log
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedLog.value = null
}

// 生命周期
onMounted(() => {
  loadLogs()
})
</script>


<style scoped>
/* 管理端主题色 */
.audit-logs {
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

.admin-btn.secondary:hover:not(:disabled) {
  background: rgba(233, 69, 96, 0.2);
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
  flex-wrap: wrap;
}

.filter-select,
.filter-input {
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

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: var(--admin-primary);
}

.filter-select option {
  background: var(--admin-bg-darker);
  color: var(--admin-text);
}

.filter-input::-webkit-calendar-picker-indicator {
  filter: invert(1);
}

/* 统计栏 */
.stats-bar {
  display: flex;
  gap: 24px;
  padding: 16px 24px;
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(233, 69, 96, 0.1);
  flex-wrap: wrap;
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

.stat-item .stat-value.success {
  color: #10b981;
}

.stat-item .stat-value.failure {
  color: #ef4444;
}

/* 表格区域 */
.logs-table-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.table-container {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

.logs-table th,
.logs-table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
}

.logs-table th {
  background: rgba(233, 69, 96, 0.1);
  color: var(--admin-text);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.logs-table th:first-child {
  border-radius: 8px 0 0 8px;
}

.logs-table th:last-child {
  border-radius: 0 8px 8px 0;
}

.logs-table td {
  color: var(--admin-text-muted);
  font-size: 13px;
}

.logs-table tr:hover td {
  background: rgba(233, 69, 96, 0.05);
  color: var(--admin-text);
}

.id-cell {
  color: var(--admin-text-muted);
  font-family: monospace;
  font-size: 12px;
}

.time-cell {
  white-space: nowrap;
  font-size: 12px;
}

.username-cell {
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--admin-primary), var(--admin-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.description-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-cell {
  font-size: 12px;
}

.resource-id {
  color: var(--admin-primary);
  font-family: monospace;
}

.ip-cell {
  font-family: monospace;
  font-size: 12px;
}

/* 操作类型徽章 */
.action-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.action-badge.login {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.action-badge.login-failed {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.action-badge.logout {
  background: rgba(148, 163, 184, 0.15);
  color: #94a3b8;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.action-badge.register {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.action-badge.password {
  background: rgba(168, 85, 247, 0.15);
  color: #a855f7;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

.action-badge.profile {
  background: rgba(14, 165, 233, 0.15);
  color: #0ea5e9;
  border: 1px solid rgba(14, 165, 233, 0.3);
}

.action-badge.create {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.action-badge.update {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.action-badge.delete {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.action-badge.restore {
  background: rgba(6, 182, 212, 0.15);
  color: #06b6d4;
  border: 1px solid rgba(6, 182, 212, 0.3);
}

.action-badge.admin {
  background: rgba(233, 69, 96, 0.15);
  color: var(--admin-primary);
  border: 1px solid rgba(233, 69, 96, 0.3);
}

.action-badge.export {
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.action-badge.default {
  background: rgba(148, 163, 184, 0.15);
  color: #94a3b8;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

/* 结果徽章 */
.result-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.result-badge.success {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.result-badge.failure {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.result-badge.blocked {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
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

.action-btn:hover {
  background: rgba(233, 69, 96, 0.1);
}

.action-btn.view:hover {
  background: rgba(59, 130, 246, 0.15);
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
  max-width: 600px;
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

/* 详情内容 */
.detail-content {
  padding: 24px;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid rgba(233, 69, 96, 0.05);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  flex: 0 0 120px;
  color: var(--admin-text-muted);
  font-size: 0.85rem;
}

.detail-value {
  flex: 1;
  color: var(--admin-text);
  font-size: 0.9rem;
  word-break: break-all;
}

.detail-value.code {
  font-family: monospace;
  background: rgba(0, 0, 0, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
}

.detail-value.error {
  color: #ef4444;
}

.detail-value.small {
  font-size: 0.8rem;
  color: var(--admin-text-muted);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(233, 69, 96, 0.1);
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
  
  .filter-select,
  .filter-input {
    flex: 1;
    min-width: 0;
  }
  
  .stats-bar {
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
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-label {
    flex: none;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .logs-table-section {
    padding: 16px;
  }
  
  .modal-content {
    margin: 10px;
  }
}
</style>
