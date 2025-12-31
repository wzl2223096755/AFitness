<template>
  <div class="settings">
    <div class="settings-header">
      <h2>系统设置</h2>
      <p>管理您的账户和应用程序设置</p>
    </div>

    <div class="settings-content">
      <!-- 个人信息设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h3>个人信息</h3>
          <el-button type="primary" @click="editProfile">编辑资料</el-button>
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
            <el-tag :type="userStore.currentUser?.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ userStore.currentUser?.role || 'USER' }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>注册时间:</label>
            <span>{{ formatDate(userStore.currentUser?.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 系统偏好设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h3>系统偏好</h3>
        </div>
        <div class="preference-list">
          <div class="preference-item">
            <div class="preference-info">
              <label>深色模式</label>
              <span>启用深色主题界面</span>
            </div>
            <el-switch v-model="preferences.darkMode" @change="toggleDarkMode" />
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>自动保存</label>
              <span>自动保存训练数据</span>
            </div>
            <el-switch v-model="preferences.autoSave" @change="updatePreferences" />
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>数据同步</label>
              <span>实时同步健身数据</span>
            </div>
            <el-switch v-model="preferences.realTimeSync" @change="updatePreferences" />
          </div>
          <div class="preference-item">
            <div class="preference-info">
              <label>通知提醒</label>
              <span>接收训练提醒和通知</span>
            </div>
            <el-switch v-model="preferences.notifications" @change="updatePreferences" />
          </div>
        </div>
      </div>

      <!-- 数据管理 -->
      <div class="settings-section">
        <div class="section-header">
          <h3>数据管理</h3>
        </div>
        <div class="data-actions">
          <div class="action-item">
            <div class="action-info">
              <label>导出数据</label>
              <span>导出所有训练数据为CSV格式</span>
            </div>
            <el-button @click="exportData" :loading="exporting">导出</el-button>
          </div>
          <div class="action-item">
            <div class="action-info">
              <label>清除缓存</label>
              <span>清除本地缓存数据</span>
            </div>
            <el-button @click="clearCache" type="warning">清除</el-button>
          </div>
          <div class="action-item danger">
            <div class="action-info">
              <label>重置数据</label>
              <span>删除所有个人数据（不可恢复）</span>
            </div>
            <el-button @click="resetData" type="danger" :loading="resetting">重置</el-button>
          </div>
        </div>
      </div>

      <!-- 关于 -->
      <div class="settings-section">
        <div class="section-header">
          <h3>关于</h3>
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
    <el-dialog v-model="showProfileDialog" title="编辑个人资料" width="500px">
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
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
          <el-select v-model="profileForm.experienceLevel" placeholder="请选择训练经验">
            <el-option label="初学者" value="beginner" />
            <el-option label="中级" value="intermediate" />
            <el-option label="高级" value="advanced" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from '../utils/message.js'
import { useUserStore } from '../stores/user'
import { userApi } from '../api/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

// 响应式数据
const showProfileDialog = ref(false)
const showResetDialog = ref(false)
const profileFormRef = ref(null)
const exporting = ref(false)
const resetting = ref(false)
const saving = ref(false)
const resetPassword = ref('')

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
    
    // 调用API更新用户信息
    await userApi.updateProfile(profileForm.value)
    
    ElMessage.success('个人资料更新成功')
    showProfileDialog.value = false
    
    // 重新获取用户信息
    await userStore.fetchCurrentUser()
  } catch (error) {
    ElMessage.error('更新失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 切换深色模式
const toggleDarkMode = (value) => {
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
    // 保存到本地
    localStorage.setItem('preferences', JSON.stringify(preferences))
    
    // 同步到服务器
    await userApi.updateUserSettings({
      theme: preferences.darkMode ? 'dark' : 'light',
      notifications: preferences.notifications,
      autoSave: preferences.autoSave
    })
    
    ElMessage.success('偏好设置已保存')
  } catch (error) {
    // 本地保存成功即可
    ElMessage.success('偏好设置已保存')
  }
}

// 导出数据
const exportData = async () => {
  try {
    exporting.value = true
    
    // 调用API导出数据
    const response = await userApi.exportUserData()
    
    if (response.success && response.data) {
      // 将数据转换为JSON字符串
      const jsonData = JSON.stringify(response.data, null, 2)
      
      // 创建Blob对象
      const blob = new Blob([jsonData], { type: 'application/json' })
      
      // 创建下载链接
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
    
    // 使API缓存失效
    userApi.invalidateCache()
    
    ElMessage.success('缓存已清除')
  }).catch(() => {
    // 用户取消
  })
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
      
      // 调用API重置数据
      const response = await userApi.resetUserData(password)
      
      if (response.success) {
        ElMessage.success('数据重置成功，您的账户信息已保留')
        
        // 清除本地缓存
        localStorage.removeItem('fitnessData')
        sessionStorage.clear()
        
        // 使API缓存失效
        userApi.invalidateCache()
      } else {
        throw new Error(response.message || '重置失败')
      }
    } catch (error) {
      ElMessage.error('重置失败：' + (error.message || '未知错误'))
    } finally {
      resetting.value = false
    }
  }).catch(() => {
    // 用户取消
  })
}

// 初始化偏好设置
const initPreferences = () => {
  const savedPreferences = localStorage.getItem('preferences')
  if (savedPreferences) {
    Object.assign(preferences, JSON.parse(savedPreferences))
  }
  
  // 初始化深色模式
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
.settings {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.settings-header {
  text-align: center;
  margin-bottom: 40px;
}

.settings-header h2 {
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.settings-header p {
  color: var(--el-text-color-secondary);
}

.settings-content {
  display: grid;
  gap: 24px;
}

.settings-section {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color);
}

.section-header h3 {
  color: var(--el-text-color-primary);
  margin: 0;
}

.profile-info,
.about-info {
  display: grid;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: var(--el-text-color-primary);
  min-width: 100px;
}

.info-item span {
  color: var(--el-text-color-regular);
}

.preference-list {
  display: grid;
  gap: 16px;
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.preference-info {
  flex: 1;
}

.preference-info label {
  display: block;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.preference-info span {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.data-actions {
  display: grid;
  gap: 16px;
}

.action-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.action-item.danger {
  background: var(--el-color-danger-light-9);
  border: 1px solid var(--el-color-danger-light-7);
}

.action-info {
  flex: 1;
}

.action-info label {
  display: block;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.action-info span {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .settings {
    padding: 12px;
  }
  
  .settings-header {
    margin-bottom: 24px;
  }
  
  .settings-header h2 {
    font-size: 20px;
  }
  
  .settings-header p {
    font-size: 14px;
  }
  
  .settings-section {
    padding: 16px;
    border-radius: 12px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .section-header h3 {
    font-size: 16px;
  }
  
  .section-header .el-button {
    width: 100%;
  }
  
  .preference-item,
  .action-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 14px;
  }
  
  .preference-item .el-switch,
  .action-item .el-button {
    align-self: flex-end;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 10px 0;
  }
  
  .info-item label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
  
  .info-item span {
    font-size: 15px;
    font-weight: 500;
  }
}

@media (max-width: 480px) {
  .settings {
    padding: 10px;
  }
  
  .settings-section {
    padding: 14px;
  }
  
  .preference-info label,
  .action-info label {
    font-size: 14px;
  }
  
  .preference-info span,
  .action-info span {
    font-size: 12px;
  }
  
  .action-item .el-button {
    width: 100%;
  }
}

/* 对话框移动端优化 */
:deep(.el-dialog) {
  @media (max-width: 768px) {
    width: 95% !important;
    margin: 0 auto !important;
  }
  
  @media (max-width: 480px) {
    width: 100% !important;
    height: 100vh !important;
    max-height: 100vh !important;
    margin: 0 !important;
    border-radius: 0 !important;
  }
}

:deep(.el-dialog__body) {
  @media (max-width: 480px) {
    max-height: calc(100vh - 140px);
    overflow-y: auto;
  }
}

:deep(.el-form-item__label) {
  @media (max-width: 768px) {
    text-align: left !important;
    float: none !important;
    display: block !important;
    width: 100% !important;
    padding-bottom: 8px;
  }
}

:deep(.el-form-item__content) {
  @media (max-width: 768px) {
    margin-left: 0 !important;
  }
}
</style>