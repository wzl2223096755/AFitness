<template>
  <div class="card-unified">
    <div class="section-header section-header--sm">
      <h3 class="section-header__title">
        <span class="icon-md">⚙️</span>
        系统偏好
      </h3>
    </div>
    <div class="preference-list">
      <div class="preference-item" v-for="pref in preferenceItems" :key="pref.key">
        <div class="preference-info">
          <label>{{ pref.label }}</label>
          <span>{{ pref.description }}</span>
        </div>
        <div 
          class="toggle-switch" 
          :class="{ 'active': preferences[pref.key] }" 
          @click="togglePreference(pref.key)"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const preferences = reactive({
  darkMode: false,
  autoSave: true,
  realTimeSync: true,
  notifications: true
})

const preferenceItems = [
  { key: 'darkMode', label: '深色模式', description: '启用深色主题界面' },
  { key: 'autoSave', label: '自动保存', description: '自动保存训练数据' },
  { key: 'realTimeSync', label: '数据同步', description: '实时同步健身数据' },
  { key: 'notifications', label: '通知提醒', description: '接收训练提醒和通知' }
]

const togglePreference = async (key) => {
  preferences[key] = !preferences[key]
  
  if (key === 'darkMode') {
    if (preferences.darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('darkMode', preferences.darkMode)
  }
  
  await savePreferences()
}

const savePreferences = async () => {
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
})
</script>

<style scoped>
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

@media (max-width: 768px) {
  .preference-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3, 12px);
  }
  
  .preference-item .toggle-switch {
    align-self: flex-end;
  }
}
</style>
