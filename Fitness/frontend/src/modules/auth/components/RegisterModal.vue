<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="close">
      <div class="modal-card register-modal">
        <button class="modal-close" @click="close">×</button>
        <h2 class="modal-title">创建账号</h2>
        
        <form @submit.prevent="handleRegister" class="register-form">
          <div class="input-group">
            <input 
              type="text" 
              v-model="registerForm.username" 
              placeholder="用户名" 
              class="cyber-input" 
            />
            <div class="input-border"></div>
          </div>
          
          <div class="input-group">
            <input 
              type="email" 
              v-model="registerForm.email" 
              placeholder="邮箱" 
              class="cyber-input" 
            />
            <div class="input-border"></div>
          </div>
          
          <div class="input-group">
            <input 
              type="password" 
              v-model="registerForm.password" 
              placeholder="密码" 
              class="cyber-input" 
            />
            <div class="input-border"></div>
          </div>
          
          <div class="input-group">
            <input 
              type="password" 
              v-model="registerForm.confirmPassword" 
              placeholder="确认密码" 
              class="cyber-input" 
            />
            <div class="input-border"></div>
          </div>

          <label class="cyber-checkbox agreement">
            <input type="checkbox" v-model="registerForm.agreement" />
            <span class="checkmark"></span>
            <span class="label-text">我已阅读并同意 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a></span>
          </label>
          
          <button type="submit" class="cyber-button" :disabled="loading">
            <span class="button-content">{{ loading ? '注册中...' : '注 册' }}</span>
            <div class="button-glow"></div>
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from '@/utils/message.js'
import { authApi } from '../api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 重置表单
const resetForm = () => {
  registerForm.username = ''
  registerForm.email = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.agreement = false
}

// 关闭弹窗
const close = () => {
  emit('update:visible', false)
  resetForm()
}

// 监听visible变化，关闭时重置表单
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})

// 验证表单
const validateRegisterForm = () => {
  if (!registerForm.username) {
    ElMessage.warning('请输入用户名')
    return false
  }
  if (registerForm.username.length < 3) {
    ElMessage.warning('用户名长度至少为3位')
    return false
  }
  if (!/^[a-zA-Z0-9_]+$/.test(registerForm.username)) {
    ElMessage.warning('用户名只能包含字母、数字和下划线')
    return false
  }
  
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱')
    return false
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return false
  }

  if (!registerForm.password) {
    ElMessage.warning('请输入密码')
    return false
  }
  if (registerForm.password.length < 6) {
    ElMessage.warning('密码长度至少为6位')
    return false
  }
  if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(registerForm.password)) {
    ElMessage.warning('密码必须包含大写字母、小写字母和数字')
    return false
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return false
  }
  
  if (!registerForm.agreement) {
    ElMessage.warning('请同意服务条款')
    return false
  }

  return true
}

// 处理注册
const handleRegister = async () => {
  if (!validateRegisterForm()) return
  
  try {
    loading.value = true
    
    const response = await authApi.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })
    
    if (response.code === 200) {
      ElMessage.success('注册成功！请登录')
      emit('success', registerForm.username)
      close()
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
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
  margin-bottom: 28px;
  text-align: center;
}

.register-form {
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

/* 复选框样式 */
.cyber-checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.cyber-checkbox input {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(99, 102, 241, 0.4);
  border-radius: 4px;
  position: relative;
  transition: all 0.3s;
  flex-shrink: 0;
}

.cyber-checkbox input:checked + .checkmark {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
}

.cyber-checkbox input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.label-text {
  color: var(--text-secondary, #94a3b8);
  font-size: 14px;
}

.agreement {
  margin: 8px 0;
}

.agreement .label-text {
  font-size: 13px;
}

.agreement a {
  color: var(--brand-primary, #6366f1);
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
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
  margin-top: 8px;
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
