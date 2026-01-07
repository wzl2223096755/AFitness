<template>
  <div class="data-export">
    <div class="export-header">
      <h3 class="export-title">
        <span class="title-icon">📥</span>
        数据导出
      </h3>
      <p class="export-description">导出系统数据到 Excel 文件</p>
    </div>

    <!-- 导出类型选择 -->
    <div class="export-types">
      <div 
        v-for="type in exportTypes" 
        :key="type.value"
        :class="['export-type-card', { selected: selectedType === type.value }]"
        @click="selectType(type.value)"
      >
        <div class="type-icon">{{ type.icon }}</div>
        <div class="type-info">
          <div class="type-name">{{ type.name }}</div>
          <div class="type-desc">{{ type.description }}</div>
        </div>
        <div class="type-check" v-if="selectedType === type.value">✓</div>
      </div>
    </div>

    <!-- 日期范围选择 (仅对训练和营养记录显示) -->
    <div class="date-range-section" v-if="needsDateRange">
      <h4 class="section-subtitle">选择日期范围</h4>
      <div class="date-inputs">
        <div class="date-input-group">
          <label class="date-label">开始日期</label>
          <input 
            type="date" 
            v-model="startDate" 
            class="date-input"
            :max="endDate || today"
          />
        </div>
        <div class="date-separator">至</div>
        <div class="date-input-group">
          <label class="date-label">结束日期</label>
          <input 
            type="date" 
            v-model="endDate" 
            class="date-input"
            :min="startDate"
            :max="today"
          />
        </div>
      </div>
      <div class="quick-dates">
        <button 
          v-for="quick in quickDateOptions" 
          :key="quick.value"
          class="quick-date-btn"
          @click="setQuickDate(quick.value)"
        >
          {{ quick.label }}
        </button>
      </div>
    </div>

    <!-- 用户选择 (仅对训练和营养记录显示) -->
    <div class="user-select-section" v-if="needsUserSelect">
      <h4 class="section-subtitle">选择用户</h4>
      <div class="user-select-wrapper">
        <select v-model="selectedUserId" class="user-select">
          <option value="">请选择用户</option>
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.username }} (ID: {{ user.id }})
          </option>
        </select>
        <button class="load-users-btn" @click="loadUsers" :disabled="loadingUsers">
          {{ loadingUsers ? '加载中...' : '刷新用户列表' }}
        </button>
      </div>
    </div>

    <!-- 导出按钮 -->
    <div class="export-actions">
      <button 
        class="export-btn primary" 
        @click="handleExport"
        :disabled="!canExport || exporting"
      >
        <span class="btn-icon" :class="{ spinning: exporting }">
          {{ exporting ? '⏳' : '📥' }}
        </span>
        {{ exporting ? '导出中...' : '开始导出' }}
      </button>
    </div>

    <!-- 导出历史 -->
    <div class="export-history" v-if="exportHistory.length > 0">
      <h4 class="section-subtitle">最近导出</h4>
      <div class="history-list">
        <div 
          v-for="(item, index) in exportHistory" 
          :key="index"
          class="history-item"
        >
          <div class="history-icon">{{ getTypeIcon(item.type) }}</div>
          <div class="history-info">
            <div class="history-name">{{ item.filename }}</div>
            <div class="history-time">{{ formatTime(item.time) }}</div>
          </div>
          <div class="history-status" :class="item.status">
            {{ item.status === 'success' ? '✓' : '✗' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@shared/api/admin'
import { ElMessage } from 'element-plus'

// Emits
const emit = defineEmits(['exported', 'error'])

// State
const selectedType = ref('users')
const startDate = ref('')
const endDate = ref('')
const selectedUserId = ref('')
const exporting = ref(false)
const loadingUsers = ref(false)
const users = ref([])
const exportHistory = ref([])

// Today's date for max date
const today = new Date().toISOString().split('T')[0]

// Export types
const exportTypes = [
  {
    value: 'users',
    name: '用户数据',
    description: '导出所有用户基本信息',
    icon: '👥'
  },
  {
    value: 'training',
    name: '训练记录',
    description: '导出指定用户的训练记录',
    icon: '🏋️'
  },
  {
    value: 'nutrition',
    name: '营养记录',
    description: '导出指定用户的营养记录',
    icon: '🥗'
  },
  {
    value: 'system',
    name: '系统统计',
    description: '导出系统统计数据',
    icon: '📊'
  }
]

// Quick date options
const quickDateOptions = [
  { value: 'week', label: '最近7天' },
  { value: 'month', label: '最近30天' },
  { value: 'quarter', label: '最近90天' },
  { value: 'year', label: '最近一年' }
]

// Computed
const needsDateRange = computed(() => {
  return ['training', 'nutrition'].includes(selectedType.value)
})

const needsUserSelect = computed(() => {
  return ['training', 'nutrition'].includes(selectedType.value)
})

const canExport = computed(() => {
  if (!selectedType.value) return false
  
  if (needsDateRange.value) {
    if (!startDate.value || !endDate.value) return false
  }
  
  if (needsUserSelect.value) {
    if (!selectedUserId.value) return false
  }
  
  return true
})

// Methods
const selectType = (type) => {
  selectedType.value = type
  // Reset selections when type changes
  if (!needsDateRange.value) {
    startDate.value = ''
    endDate.value = ''
  }
  if (!needsUserSelect.value) {
    selectedUserId.value = ''
  }
}

const setQuickDate = (period) => {
  const end = new Date()
  const start = new Date()
  
  switch (period) {
    case 'week':
      start.setDate(end.getDate() - 7)
      break
    case 'month':
      start.setDate(end.getDate() - 30)
      break
    case 'quarter':
      start.setDate(end.getDate() - 90)
      break
    case 'year':
      start.setFullYear(end.getFullYear() - 1)
      break
  }
  
  startDate.value = start.toISOString().split('T')[0]
  endDate.value = end.toISOString().split('T')[0]
}

const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await adminApi.getUsers({ page: 0, size: 100 })
    if (res.data && res.data.content) {
      users.value = res.data.content
    } else if (res.data && Array.isArray(res.data)) {
      users.value = res.data
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
  loadingUsers.value = false
}

const getTypeIcon = (type) => {
  const typeObj = exportTypes.find(t => t.value === type)
  return typeObj?.icon || '📄'
}

const formatTime = (date) => {
  if (!date) return ''
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const handleExport = async () => {
  if (!canExport.value || exporting.value) return
  
  exporting.value = true
  const type = selectedType.value
  let filename = ''
  const dateStr = new Date().toISOString().split('T')[0].replace(/-/g, '')
  
  try {
    let response
    
    switch (type) {
      case 'users':
        filename = `users_${dateStr}.xlsx`
        response = await adminApi.exportUsers()
        break
        
      case 'training':
        filename = `training_${selectedUserId.value}_${dateStr}.xlsx`
        response = await adminApi.exportTrainingRecords(
          selectedUserId.value,
          startDate.value,
          endDate.value
        )
        break
        
      case 'nutrition':
        filename = `nutrition_${selectedUserId.value}_${dateStr}.xlsx`
        response = await adminApi.exportNutritionRecords(
          selectedUserId.value,
          startDate.value,
          endDate.value
        )
        break
        
      case 'system':
        filename = `system_stats_${dateStr}.xlsx`
        response = await adminApi.exportSystemStats()
        break
    }
    
    // Handle the response
    if (response && response.data) {
      const blob = response.data instanceof Blob 
        ? response.data 
        : new Blob([response.data], { 
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          })
      
      downloadBlob(blob, filename)
      
      // Add to history
      exportHistory.value.unshift({
        type,
        filename,
        time: new Date(),
        status: 'success'
      })
      
      // Keep only last 5 items
      if (exportHistory.value.length > 5) {
        exportHistory.value = exportHistory.value.slice(0, 5)
      }
      
      ElMessage.success('导出成功')
      emit('exported', { type, filename })
    }
  } catch (error) {
    console.error('导出失败:', error)
    
    // Add to history with error status
    exportHistory.value.unshift({
      type,
      filename: filename || `${type}_export`,
      time: new Date(),
      status: 'error'
    })
    
    ElMessage.error('导出失败，请稍后重试')
    emit('error', error)
  }
  
  exporting.value = false
}

// Lifecycle
onMounted(() => {
  // Set default date range
  setQuickDate('month')
  // Load users if needed
  if (needsUserSelect.value) {
    loadUsers()
  }
})
</script>

<style scoped>
.data-export {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.export-header {
  margin-bottom: 8px;
}

.export-title {
  color: #f8fafc;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1.1rem;
}

.export-description {
  color: #94a3b8;
  font-size: 0.9rem;
  margin: 0;
}

/* Export Types */
.export-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.export-type-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(26, 26, 46, 0.8);
  border: 2px solid rgba(233, 69, 96, 0.15);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.export-type-card:hover {
  border-color: rgba(233, 69, 96, 0.4);
  background: rgba(233, 69, 96, 0.05);
}

.export-type-card.selected {
  border-color: #e94560;
  background: rgba(233, 69, 96, 0.1);
}

.type-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.type-info {
  flex: 1;
  min-width: 0;
}

.type-name {
  color: #f8fafc;
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.type-desc {
  color: #94a3b8;
  font-size: 0.8rem;
}

.type-check {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e94560;
  border-radius: 50%;
  color: #fff;
  font-size: 0.8rem;
  font-weight: 700;
  flex-shrink: 0;
}

/* Section Subtitle */
.section-subtitle {
  color: #f8fafc;
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 12px 0;
}

/* Date Range Section */
.date-range-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.date-inputs {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.date-input-group {
  flex: 1;
}

.date-label {
  display: block;
  color: #94a3b8;
  font-size: 0.85rem;
  margin-bottom: 8px;
}

.date-input {
  width: 100%;
  padding: 10px 14px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.date-input:focus {
  outline: none;
  border-color: #e94560;
}

.date-separator {
  color: #94a3b8;
  font-size: 0.9rem;
  padding-bottom: 10px;
}

.quick-dates {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-date-btn {
  padding: 6px 12px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-date-btn:hover {
  border-color: #e94560;
  color: #e94560;
  background: rgba(233, 69, 96, 0.1);
}

/* User Select Section */
.user-select-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.user-select-wrapper {
  display: flex;
  gap: 12px;
}

.user-select {
  flex: 1;
  padding: 10px 14px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-select:focus {
  outline: none;
  border-color: #e94560;
}

.user-select option {
  background: #1a1a2e;
  color: #f8fafc;
}

.load-users-btn {
  padding: 10px 16px;
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  border-radius: 8px;
  color: #e94560;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.load-users-btn:hover:not(:disabled) {
  background: rgba(233, 69, 96, 0.2);
}

.load-users-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Export Actions */
.export-actions {
  display: flex;
  justify-content: center;
}

.export-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.export-btn.primary {
  background: linear-gradient(135deg, #e94560, #f39c12);
  color: #fff;
  box-shadow: 0 4px 15px rgba(233, 69, 96, 0.3);
}

.export-btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(233, 69, 96, 0.4);
}

.export-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  font-size: 1.1rem;
  display: inline-block;
}

.btn-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Export History */
.export-history {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: rgba(26, 26, 46, 0.6);
  border-radius: 8px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.history-icon {
  font-size: 1.3rem;
  flex-shrink: 0;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-name {
  color: #f8fafc;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  color: #64748b;
  font-size: 0.8rem;
}

.history-status {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 0.8rem;
  font-weight: 700;
  flex-shrink: 0;
}

.history-status.success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.history-status.error {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

/* Responsive */
@media (max-width: 768px) {
  .export-types {
    grid-template-columns: 1fr;
  }
  
  .date-inputs {
    flex-direction: column;
    gap: 12px;
  }
  
  .date-separator {
    display: none;
  }
  
  .user-select-wrapper {
    flex-direction: column;
  }
}
</style>
