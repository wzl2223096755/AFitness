<template>
  <div class="system-settings">
    <!-- 页面标题 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <span class="title-icon">⚙️</span>
          系统设置
        </h1>
        <p class="page-subtitle">管理系统配置和参数</p>
      </div>
      <div class="header-actions">
        <button class="admin-btn secondary" @click="refreshSettings" :disabled="loading">
          <span class="btn-icon">🔄</span>
          刷新
        </button>
        <button class="admin-btn primary" @click="saveAllSettings" :disabled="saving || !hasChanges">
          <span class="btn-icon">💾</span>
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </header>

    <!-- 设置内容 -->
    <div class="settings-content" v-if="!loading">
      <!-- 基础设置 -->
      <section class="settings-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🏠</span>
            基础设置
          </h2>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">系统名称</label>
              <p class="setting-desc">显示在页面标题和登录页的系统名称</p>
            </div>
            <div class="setting-control">
              <input 
                type="text" 
                v-model="settings.systemName" 
                class="admin-input"
                placeholder="AFitness"
                @input="markChanged"
              />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">维护模式</label>
              <p class="setting-desc">开启后普通用户将无法访问系统</p>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.maintenanceMode" @change="markChanged" />
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </section>

      <!-- 安全设置 -->
      <section class="settings-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🔒</span>
            安全设置
          </h2>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">会话超时时间（分钟）</label>
              <p class="setting-desc">用户无操作后自动登出的时间</p>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.sessionTimeout" 
                class="admin-input"
                min="5"
                max="1440"
                @input="markChanged"
              />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">最大登录尝试次数</label>
              <p class="setting-desc">超过次数后账户将被临时锁定</p>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.maxLoginAttempts" 
                class="admin-input"
                min="3"
                max="10"
                @input="markChanged"
              />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">强制密码复杂度</label>
              <p class="setting-desc">要求密码包含大小写字母、数字和特殊字符</p>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.enforcePasswordComplexity" @change="markChanged" />
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </section>

      <!-- 通知设置 -->
      <section class="settings-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🔔</span>
            通知设置
          </h2>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">启用邮件通知</label>
              <p class="setting-desc">系统事件发生时发送邮件通知</p>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.emailNotifications" @change="markChanged" />
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">管理员邮箱</label>
              <p class="setting-desc">接收系统通知的邮箱地址</p>
            </div>
            <div class="setting-control">
              <input 
                type="email" 
                v-model="settings.adminEmail" 
                class="admin-input"
                placeholder="admin@example.com"
                @input="markChanged"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- 数据设置 -->
      <section class="settings-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">💾</span>
            数据设置
          </h2>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">数据保留天数</label>
              <p class="setting-desc">训练记录等数据的保留时间</p>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.dataRetentionDays" 
                class="admin-input"
                min="30"
                max="3650"
                @input="markChanged"
              />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">自动备份</label>
              <p class="setting-desc">每日自动备份系统数据</p>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.autoBackup" @change="markChanged" />
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-else>
      <div class="loading-spinner"></div>
      <p>加载设置中...</p>
    </div>

    <!-- 保存成功提示 -->
    <div class="toast" :class="{ show: showToast }">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'

// 状态
const loading = ref(true)
const saving = ref(false)
const hasChanges = ref(false)
const showToast = ref(false)
const toastMessage = ref('')

// 设置数据
const settings = reactive({
  systemName: 'AFitness',
  maintenanceMode: false,
  sessionTimeout: 30,
  maxLoginAttempts: 5,
  enforcePasswordComplexity: true,
  emailNotifications: false,
  adminEmail: '',
  dataRetentionDays: 365,
  autoBackup: true
})

// 原始设置（用于检测变化）
const originalSettings = ref({})

// 标记已更改
const markChanged = () => {
  hasChanges.value = JSON.stringify(settings) !== JSON.stringify(originalSettings.value)
}

// 加载设置
const loadSettings = async () => {
  loading.value = true
  try {
    // 模拟API调用 - 实际应调用 adminApi.getSettings()
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    Object.assign(settings, {
      systemName: 'AFitness 健身管理系统',
      maintenanceMode: false,
      sessionTimeout: 30,
      maxLoginAttempts: 5,
      enforcePasswordComplexity: true,
      emailNotifications: false,
      adminEmail: 'admin@afitness.com',
      dataRetentionDays: 365,
      autoBackup: true
    })
    
    originalSettings.value = { ...settings }
    hasChanges.value = false
  } catch (error) {
    showToastMessage('加载设置失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 刷新设置
const refreshSettings = () => {
  loadSettings()
}

// 保存所有设置
const saveAllSettings = async () => {
  saving.value = true
  try {
    // 模拟API调用 - 实际应调用 adminApi.updateSettings(settings)
    await new Promise(resolve => setTimeout(resolve, 800))
    
    originalSettings.value = { ...settings }
    hasChanges.value = false
    showToastMessage('设置保存成功')
  } catch (error) {
    showToastMessage('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 显示提示消息
const showToastMessage = (message) => {
  toastMessage.value = message
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
.system-settings {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color, #e0e0e0);
}

.header-left {
  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary, #1a1a2e);
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .page-subtitle {
    color: var(--text-secondary, #666);
    margin: 0;
    font-size: 14px;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.admin-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    
    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
  
  &.secondary {
    background: var(--bg-secondary, #f5f5f5);
    color: var(--text-primary, #333);
    
    &:hover:not(:disabled) {
      background: var(--bg-hover, #e8e8e8);
    }
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-section {
  background: var(--card-bg, white);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary, #1a1a2e);
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.settings-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-secondary, #f8f9fa);
  border-radius: 8px;
  
  @media (max-width: 600px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

.setting-info {
  flex: 1;
  
  .setting-label {
    font-weight: 500;
    color: var(--text-primary, #333);
    margin-bottom: 4px;
    display: block;
  }
  
  .setting-desc {
    font-size: 13px;
    color: var(--text-secondary, #666);
    margin: 0;
  }
}

.setting-control {
  min-width: 200px;
  
  @media (max-width: 600px) {
    width: 100%;
  }
}

.admin-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  
  &:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
}

// Toggle Switch
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 28px;
  
  input {
    opacity: 0;
    width: 0;
    height: 0;
    
    &:checked + .toggle-slider {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &:checked + .toggle-slider:before {
      transform: translateX(24px);
    }
  }
  
  .toggle-slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: 0.3s;
    border-radius: 28px;
    
    &:before {
      position: absolute;
      content: "";
      height: 22px;
      width: 22px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: 0.3s;
      border-radius: 50%;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
  }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-secondary, #666);
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--border-color, #e0e0e0);
    border-top-color: #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 14px 24px;
  background: #333;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease;
  z-index: 1000;
  
  &.show {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
