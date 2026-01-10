<template>
  <div class="settings animate-fade-in-up">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-header__title">系统设置</h2>
      <p class="page-header__description">管理您的账户和应用程序设置</p>
    </div>

    <div class="settings-content">
      <!-- 个人信息设置 -->
      <div class="card-unified card-unified--primary stagger-1">
        <div class="section-header">
          <div class="section-header__title-area">
            <h3 class="section-header__title">
              <span class="icon-md">👤</span>
              个人信息
            </h3>
          </div>
          <div class="section-header__actions">
            <button class="btn-unified btn-unified--primary btn-unified--sm" @click="editProfile">编辑资料</button>
          </div>
        </div>
        <div class="profile-info">
          <div class="info-item">
            <label>用户名:</label>
            <span>{{ userStore.currentUser?.username || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <label>邮箱:</label>
            <span>{{ userStore.currentUser?.email || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <label>角色:</label>
            <span class="role-badge" :class="userStore.currentUser?.role === 'ADMIN' ? 'role-badge--admin' : 'role-badge--user'">
              {{ userStore.currentUser?.role || 'USER' }}
            </span>
          </div>
          <div class="info-item">
            <label>注册时间:</label>
            <span>{{ formatDate(userStore.currentUser?.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 系统偏好设置 -->
      <div class="card-unified stagger-2">
        <div class="section-header section-header--sm">
          <h3 class="section-header__title">
            <span class="icon-md">⚙️</span>
            系统偏好
          </h3>
        </div>
        <div class="preference-list">
          <div class="preference-item">
            <div class="preference-info">
              <label>深色模式</label>
              <span>启用深色主题界面</span>
            </div>
            <div class="toggle-switch" :class="{ 'active': preferences.darkMode }" @click="toggleDarkMode(!preferences.darkMode)"></div>
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>自动保存</label>
              <span>自动保存训练数据</span>
            </div>
            <div class="toggle-switch" :class="{ 'active': preferences.autoSave }" @click="preferences.autoSave = !preferences.autoSave; updatePreferences()"></div>
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>数据同步</label>
              <span>实时同步健身数据</span>
            </div>
            <div class="toggle-switch" :class="{ 'active': preferences.realTimeSync }" @click="preferences.realTimeSync = !preferences.realTimeSync; updatePreferences()"></div>
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>通知提醒</label>
              <span>接收训练提醒和通知</span>
            </div>
            <div class="toggle-switch" :class="{ 'active': preferences.notifications }" @click="preferences.notifications = !preferences.notifications; updatePreferences()"></div>
          </div>
        </div>
      </div>

      <!-- 数据管理 -->
      <div class="card-unified stagger-3">
        <div class="section-header section-header--sm">
          <h3 class="section-header__title">
            <span class="icon-md">📁</span>
            数据管理
          </h3>
        </div>
        <div class="data-actions">
          <div class="action-item">
            <div class="action-info">
              <label>导出数据</label>
              <span>导出所有训练数据为JSON格式</span>
            </div>
            <button class="btn-unified btn-unified--secondary btn-unified--sm" @click="exportData" :disabled="exporting">
              {{ exporting ? '导出中...' : '导出' }}
            </button>
          </div>
          <div class="action-item">
            <div class="action-info">
              <label>清除缓存</label>
              <span>清除本地缓存数据</span>
            </div>
            <button class="btn-unified btn-unified--secondary btn-unified--sm" @click="clearCache">清除</button>
          </div>
          <div class="action-item action-item--danger">
            <div class="action-info">
              <label>重置数据</label>
              <span>删除所有个人数据（不可恢复）</span>
            </div>
            <button class="btn-unified btn-unified--danger btn-unified--sm" @click="resetData" :disabled="resetting">
              {{ resetting ? '重置中...' : '重置' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 关于 -->
      <div class="card-unified card-unified--sm stagger-4">
        <div class="section-header section-header--sm">
          <h3 class="section-header__title">
            <span class="icon-md">ℹ️</span>
            关于
          </h3>
        </div>
        <div class="about-info">
          <div class="info-item">
            <label>应用名称:</label>
            <span>AFitness 健身管理系统</span>
          </div>
          <div class="info-item">
            <label>版本:</label>
            <span>v1.0.0</span>
          </div>
          <div class="info-item">
            <label>技术栈:</label>
            <span>Vue 3 + Spring Boot + MySQL</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑个人资料对话框 -->
    <el-dialog v-model="showProfileDialog" title="编辑个人资料" width="500px" class="glass-modal">
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" placeholder="请输入用户名" class="input-unified" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" class="input-unified" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="profileForm.age" :min="1" :max="150" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="身高(cm)" prop="height">
          <el-input-number v-model="profileForm.height" :min="50" :max="250" :precision="1" placeholder="请输入身高" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="profileForm.weight" :min="20" :max="300" :precision="1" placeholder="请输入体重" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="profileForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="训练经验" prop="experienceLevel">
          <el-select v-model="profileForm.experienceLevel" placeholder="请选择训练经验" class="select-unified">
            <el-option label="初学者" value="beginner" />
            <el-option label="中级" value="intermediate" />
            <el-option label="高级" value="advanced" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-unified btn-unified--ghost" @click="showProfileDialog = false">取消</button>
        <button class="btn-unified btn-unified--primary" @click="saveProfile" :disabled="saving">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '../api'
import dayjs from 'dayjs'

const userStore = useUserStore()

// 响应式数据
const showProfileDialog = ref(false)
const profileFormRef = ref(null)
const exporting = ref(false)
const resetting = ref(false)
const saving = ref(false)

// 个人资料表单
const profileForm = ref({
  username: '',
  email: '',
  age: null,
  height: null,
  weight: null,
  gender: '',
  experienceLevel: ''
})

// 系统偏好设置
const preferences = reactive({
  darkMode: false,
  autoSave: true,
  realTimeSync: true,
  notifications: true
})

// 表单验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : 'N/A'
}

// 编辑个人资料
const editProfile = () => {
  const user = userStore.currentUser
  profileForm.value = {
    username: user?.username || '',
    email: user?.email || '',
    age: user?.age || null,
    height: user?.height || null,
    weight: user?.weight || null,
    gender: user?.gender || '',
    experienceLevel: user?.experienceLevel || ''
  }
  showProfileDialog.value = true
}

// 保存个人资料
const saveProfile = async () => {
  try {
    await profileFormRef.value.validate()
    saving.value = true
    
    await userApi.updateProfile(profileForm.value)
    
    ElMessage.success('个人资料更新成功')
    showProfileDialog.value = false
    
    await userStore.fetchCurrentUser()
  } catch (error) {
    ElMessage.error('更新失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 切换深色模式
const toggleDarkMode = (value) => {
  preferences.darkMode = value
  if (value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  localStorage.setItem('darkMode', value)
  updatePreferences()
}

// 更新偏好设置
const updatePreferences = async () => {
  try {
    localStorage.setItem('preferences', JSON.stringify(preferences))
    
    await userApi.updateUserSettings({
      theme: preferences.darkMode ? 'dark' : 'light',
      notifications: preferences.notifications,
      autoSave: preferences.autoSave
    })
    
    ElMessage.success('偏好设置已保存')
  } catch (error) {
    ElMessage.success('偏好设置已保存')
  }
}

// 导出数据
const exportData = async () => {
  try {
    exporting.value = true
    
    const response = await userApi.exportUserData()
    
    if (response.success && response.data) {
      const jsonData = JSON.stringify(response.data, null, 2)
      const blob = new Blob([jsonData], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `fitness_data_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      ElMessage.success('数据导出成功')
    } else {
      throw new Error(response.message || '导出失败')
    }
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  } finally {
    exporting.value = false
  }
}

// 清除缓存
const clearCache = () => {
  ElMessageBox.confirm('确定要清除所有缓存数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.clear()
    sessionStorage.clear()
    userApi.invalidateCache()
    ElMessage.success('缓存已清除')
  }).catch(() => {})
}

// 重置数据
const resetData = () => {
  ElMessageBox.prompt('请输入密码确认重置操作', '警告', {
    confirmButtonText: '确定重置',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPlaceholder: '请输入当前密码',
    inputValidator: (value) => {
      if (!value || value.length < 1) {
        return '请输入密码'
      }
      return true
    },
    type: 'error'
  }).then(async ({ value: password }) => {
    try {
      resetting.value = true
      
      const response = await userApi.resetUserData(password)
      
      if (response.success) {
        ElMessage.success('数据重置成功，您的账户信息已保留')
        localStorage.removeItem('fitnessData')
        sessionStorage.clear()
        userApi.invalidateCache()
      } else {
        throw new Error(response.message || '重置失败')
      }
    } catch (error) {
      ElMessage.error('重置失败：' + (error.message || '未知错误'))
    } finally {
      resetting.value = false
    }
  }).catch(() => {})
}

// 初始化偏好设置
const initPreferences = () => {
  const savedPreferences = localStorage.getItem('preferences')
  if (savedPreferences) {
    Object.assign(preferences, JSON.parse(savedPreferences))
  }
  
  const darkMode = localStorage.getItem('darkMode') === 'true'
  if (darkMode) {
    document.documentElement.classList.add('dark')
    preferences.darkMode = true
  }
}

onMounted(() => {
  initPreferences()
  userStore.fetchCurrentUser()
})
</script>

<style scoped>
/* Settings 页面样式 - 从原 Settings.vue 迁移 */
.settings {
  padding: var(--spacing-5, 20px);
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-10, 40px);
}

.page-header__title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-2, 8px) 0;
}

.page-header__description {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.settings-content {
  display: grid;
  gap: var(--spacing-6, 24px);
}

.profile-info,
.about-info {
  display: grid;
  gap: var(--spacing-4, 16px);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-3, 12px) 0;
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: var(--color-text-secondary, #94a3b8);
  min-width: 100px;
}

.info-item span {
  color: var(--color-text-primary, #f8fafc);
  font-weight: 500;
}

.role-badge {
  padding: var(--spacing-1, 4px) var(--spacing-3, 12px);
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.role-badge--admin {
  background: var(--color-danger-bg, rgba(239, 68, 68, 0.15));
  color: var(--color-danger, #ef4444);
}

.role-badge--user {
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
}

.preference-list {
  display: grid;
  gap: var(--spacing-4, 16px);
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-4, 16px);
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
  transition: all 0.3s ease;
}

.preference-item:hover {
  background: rgba(99, 102, 241, 0.08);
}

.preference-info {
  flex: 1;
}

.preference-info label {
  display: block;
  font-weight: 600;
  color: var(--color-text-primary, #f8fafc);
  margin-bottom: var(--spacing-1, 4px);
}

.preference-info span {
  font-size: 0.875rem;
  color: var(--color-text-secondary, #94a3b8);
}

.toggle-switch {
  width: 48px;
  height: 24px;
  background: rgba(136, 136, 170, 0.3);
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-switch::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: all 0.3s ease;
}

.toggle-switch.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.toggle-switch.active::after {
  left: 26px;
}

.data-actions {
  display: grid;
  gap: var(--spacing-4, 16px);
}

.action-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-4, 16px);
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
  transition: all 0.3s ease;
}

.action-item:hover {
  background: rgba(99, 102, 241, 0.08);
}

.action-item--danger {
  background: var(--color-danger-bg, rgba(239, 68, 68, 0.1));
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.action-item--danger:hover {
  background: rgba(239, 68, 68, 0.15);
}

.action-info {
  flex: 1;
}

.action-info label {
  display: block;
  font-weight: 600;
  color: var(--color-text-primary, #f8fafc);
  margin-bottom: var(--spacing-1, 4px);
}

.action-info span {
  font-size: 0.875rem;
  color: var(--color-text-secondary, #94a3b8);
}

@media (max-width: 768px) {
  .settings {
    padding: var(--spacing-3, 12px);
  }
  
  .page-header {
    margin-bottom: var(--spacing-6, 24px);
  }
  
  .page-header__title {
    font-size: 1.5rem;
  }
  
  .preference-item,
  .action-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3, 12px);
  }
  
  .preference-item .toggle-switch,
  .action-item .btn-unified {
    align-self: flex-end;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2, 6px);
  }
}

:deep(.el-dialog) {
  background: var(--glass-bg, rgba(15, 15, 35, 0.95)) !important;
  border: 1px solid var(--glass-border, rgba(99, 102, 241, 0.2)) !important;
  border-radius: 20px !important;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
  padding: var(--spacing-5, 20px) var(--spacing-6, 24px);
}

:deep(.el-dialog__title) {
  color: var(--color-text-primary, #f8fafc) !important;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: var(--spacing-6, 24px);
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba(99, 102, 241, 0.1);
  padding: var(--spacing-4, 16px) var(--spacing-6, 24px);
  display: flex;
  gap: var(--spacing-3, 12px);
  justify-content: flex-end;
}

:deep(.el-form-item__label) {
  color: var(--color-text-secondary, #94a3b8) !important;
}
</style>
