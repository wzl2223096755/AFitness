<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="close">
      <div class="modal-card">
        <button class="modal-close" @click="close">×</button>
        <h2 class="modal-title">找回密码</h2>
        <p class="modal-desc">请输入您的注册邮箱，我们将发送重置链接</p>
        
        <form @submit.prevent="handleForgotPassword" class="forgot-form">
          <div class="input-group">
            <input 
              type="email" 
              v-model="email" 
              placeholder="注册邮箱" 
              class="cyber-input" 
            />
            <div class="input-border"></div>
          </div>
          
          <button type="submit" class="cyber-button" :disabled="loading">
            <span class="button-content">{{ loading ? '发送中...' : '发送重置链接' }}</span>
            <div class="button-glow"></div>
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from '@/utils/message.js'
import { authApi } from '../api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

const email = ref('')
const loading = ref(false)

// 关闭弹窗
const close = () => {
  emit('update:visible', false)
  email.value = ''
}

// 监听visible变化，关闭时重置
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    email.value = ''
  }
})

// 处理忘记密码
const handleForgotPassword = async () => {
  if (!email.value) {
    ElMessage.warning('请输入邮箱')
    return
  }
  
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  
  try {
    loading.value = true
    
    await authApi.forgotPassword({ email: email.value })
    
    ElMessage.success('重置链接已发送到您的邮箱')
    close()
  } catch (error) {
    console.error('发送重置链接失败:', error)
    // 即使失败也显示成功消息，防止邮箱枚举攻击
    ElMessage.success('如果该邮箱已注册，重置链接将发送到您的邮箱')
    close()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 弹窗样式 - 支持主题切换 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay, rgba(0, 0, 0, 0.8));
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-elevated, rgba(15, 15, 35, 0.95));
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-brand, rgba(99, 102, 241, 0.3));
  border-radius: 24px;
  padding: 40px;
  position: relative;
  animation: slideUp 0.3s ease;
  box-shadow: var(--shadow-lg);
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: var(--hover-bg, rgba(255, 255, 255, 0.1));
  border: none;
  border-radius: 8px;
  color: var(--text-tertiary, #94a3b8);
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.2);
  color: var(--color-danger, #ef4444);
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  text-align: center;
}

.modal-desc {
  color: var(--text-secondary);
  font-size: 14px;
  text-align: center;
  margin-bottom: 28px;
}

.forgot-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 输入框组 */
.input-group {
  position: relative;
}

.cyber-input {
  width: 100%;
  padding: 16px;
  font-size: 15px;
  color: var(--text-primary);
  background: var(--input-bg, rgba(0, 0, 0, 0.4));
  border: 1px solid var(--border-default, rgba(99, 102, 241, 0.2));
  border-radius: 12px;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
}

.cyber-input::placeholder {
  color: var(--text-disabled, #64748b);
}

.cyber-input:focus {
  border-color: var(--brand-primary, #6366f1);
  box-shadow: 
    0 0 0 3px rgba(99, 102, 241, 0.1),
    0 0 20px rgba(99, 102, 241, 0.2);
}

/* 输入框底部发光边框 */
.input-border {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: var(--brand-gradient, linear-gradient(90deg, #6366f1, #8b5cf6));
  transition: width 0.3s;
  border-radius: 1px;
  opacity: 0;
}

.cyber-input:focus ~ .input-border {
  width: 80%;
  opacity: 1;
}

/* 按钮样式 */
.cyber-button {
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: var(--brand-gradient, linear-gradient(135deg, #6366f1, #8b5cf6));
  border: none;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.cyber-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 
    0 10px 30px rgba(99, 102, 241, 0.4),
    0 0 40px rgba(99, 102, 241, 0.2);
}

.cyber-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.button-content {
  position: relative;
  z-index: 2;
}

.button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.cyber-button:hover .button-glow {
  left: 100%;
}

/* 响应式 */
@media (max-width: 480px) {
  .modal-card {
    padding: 32px 24px;
  }
}
</style>
