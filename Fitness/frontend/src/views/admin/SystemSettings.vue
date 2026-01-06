<template>
  <AdminLayout>
    <template #header><h2>系统设置</h2></template>
    <div class="settings-page">
      <div class="settings-section">
        <h3>基本设置</h3>
        <div class="setting-item">
          <label>系统名称</label>
          <input type="text" v-model="settings.systemName" class="setting-input" />
        </div>
        <div class="setting-item">
          <label>系统描述</label>
          <textarea v-model="settings.description" class="setting-textarea"></textarea>
        </div>
      </div>
      <div class="settings-section">
        <h3>安全设置</h3>
        <div class="setting-item">
          <label>会话超时（分钟）</label>
          <input type="number" v-model="settings.sessionTimeout" class="setting-input" />
        </div>
        <div class="setting-item">
          <label>最大登录尝试次数</label>
          <input type="number" v-model="settings.maxLoginAttempts" class="setting-input" />
        </div>
      </div>
      <div class="settings-actions">
        <button class="btn-primary" @click="saveSettings">保存设置</button>
        <button class="btn-secondary">重置</button>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref } from 'vue'
import AdminLayout from './AdminLayout.vue'

const settings = ref({
  systemName: 'AFitness 健身管理系统',
  description: '智能健身数据管理与分析平台',
  sessionTimeout: 30,
  maxLoginAttempts: 5
})

const saveSettings = () => {
  alert('设置已保存')
}
</script>

<style scoped>
.settings-page { padding: 24px; max-width: 800px; }
.settings-section {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}
.settings-section h3 { color: #f8fafc; margin-bottom: 20px; font-size: 18px; }
.setting-item { margin-bottom: 20px; }
.setting-item label { display: block; color: #94a3b8; margin-bottom: 8px; font-size: 14px; }
.setting-input, .setting-textarea {
  width: 100%;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 8px;
  color: #f8fafc;
  font-size: 14px;
}
.setting-textarea { min-height: 100px; resize: vertical; }
.settings-actions { display: flex; gap: 16px; }
.btn-primary {
  padding: 12px 32px;
  background: linear-gradient(135deg, #e94560, #d63850);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
}
.btn-secondary {
  padding: 12px 32px;
  background: rgba(99, 102, 241, 0.2);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 8px;
  color: #818cf8;
  cursor: pointer;
}
</style>
