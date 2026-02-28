<template>
  <div class="login-page">
    <!-- 简化背景 -->
    <div class="background">
      <div class="gradient-bg"></div>
    </div>

    <!-- 登录容器 -->
    <div class="login-container">
      <div class="login-card">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-icon">🏋️</div>
          <h1 class="logo-text">AFitness</h1>
          <p class="logo-subtitle">智能健身管理系统</p>
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin" class="login-form">
          <!-- 用户名输入 -->
          <div class="input-group">
            <input 
              type="text" 
              v-model="loginForm.username"
              placeholder="用户名"
              class="cyber-input"
              :class="{ 'has-error': errors.username }"
              @focus="clearError('username')"
              autocomplete="username"
            />
            <div class="input-border"></div>
          </div>
          <p v-if="errors.username" class="error-text">{{ errors.username }}</p>

          <!-- 密码输入 -->
          <div class="input-group">
            <input 
              :type="showPassword ? 'text' : 'password'"
              v-model="loginForm.password"
              placeholder="密码"
              class="cyber-input"
              :class="{ 'has-error': errors.password }"
              @focus="clearError('password')"
              @keyup.enter="handleLogin"
              autocomplete="current-password"
            />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword">
              <span v-if="!showPassword">👁️</span>
              <span v-else>🙈</span>
            </button>
            <div class="input-border"></div>
          </div>
          <p v-if="errors.password" class="error-text">{{ errors.password }}</p>

          <!-- 记住我 -->
          <div class="options-row">
            <label class="checkbox">
              <input type="checkbox" v-model="loginForm.rememberMe" />
              <span>记住我</span>
            </label>
          </div>

          <!-- 登录按钮 -->
          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="loading" class="loading-spinner">⏳</span>
            <span v-else>{{ loading ? '登录中...' : '登 录' }}</span>
          </button>
        </form>

        <!-- 版本信息 -->
        <div class="version-info">
          <span>v2.0.0</span>
          <span class="separator">|</span>
          <span>© 2025 AFitness</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 状态
const loading = ref(false)
const showPassword = ref(false)
const hasError = ref(false)

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  username: '',
  password: ''
})

// 清除错误
const clearError = (field) => {
  errors[field] = ''
  hasError.value = false
}

// 验证表单
const validateForm = () => {
  let valid = true
  
  if (!loginForm.username) {
    errors.username = '请输入用户名'
    valid = false
  }
  
  if (!loginForm.password) {
    errors.password = '请输入密码'
    valid = false
  }
  
  if (!valid) {
    hasError.value = true
    setTimeout(() => hasError.value = false, 500)
  }
  
  return valid
}

// 处理登录
const handleLogin = async () => {
  console.log('Login button clicked', loginForm)
  
  if (!validateForm()) return
  
  try {
    loading.value = true
    console.log('Starting login process...')
    
    const result = await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })
    
    console.log('Login result:', result)
    
    if (result.success) {
      ElMessage.success('登录成功！')
      
      setTimeout(() => {
        router.push('/dashboard')
      }, 500)
    } else {
      ElMessage.error(result.message || '登录失败')
      hasError.value = true
      setTimeout(() => hasError.value = false, 500)
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
    hasError.value = true
    setTimeout(() => hasError.value = false, 500)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>

/* =====================================================

   登录页面 - 赛博朋克科技风格 (支持主题切换)

   ===================================================== */



.login-page {

  min-height: 100vh;

  display: flex;

  align-items: center;

  justify-content: center;

  position: relative;

  overflow: hidden;

  background: var(--bg-page);

}



/* 科技感背景 */

.cyber-background {

  position: absolute;

  inset: 0;

  overflow: hidden;

}



/* 网格线 */

.grid-lines {

  position: absolute;

  inset: 0;

  background-image: 

    linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px),

    linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px);

  background-size: 50px 50px;

  animation: gridMove 20s linear infinite;

}



@keyframes gridMove {

  0% { transform: perspective(500px) rotateX(60deg) translateY(0); }

  100% { transform: perspective(500px) rotateX(60deg) translateY(50px); }

}



/* 发光球体 */

.glow-orbs {

  position: absolute;

  inset: 0;

  pointer-events: none;

}



.orb {

  position: absolute;

  border-radius: 50%;

  filter: blur(80px);

  opacity: 0.6;

  animation: orbFloat 15s ease-in-out infinite;

}



.orb-1 {

  width: 400px;

  height: 400px;

  background: radial-gradient(circle, #6366f1, transparent 70%);

  top: -100px;

  left: -100px;

  animation-delay: 0s;

}



.orb-2 {

  width: 300px;

  height: 300px;

  background: radial-gradient(circle, #8b5cf6, transparent 70%);

  bottom: -50px;

  right: -50px;

  animation-delay: -5s;

}



.orb-3 {

  width: 250px;

  height: 250px;

  background: radial-gradient(circle, #06b6d4, transparent 70%);

  top: 50%;

  left: 50%;

  transform: translate(-50%, -50%);

  animation-delay: -10s;

}



@keyframes orbFloat {

  0%, 100% { transform: translate(0, 0) scale(1); }

  25% { transform: translate(30px, -30px) scale(1.1); }

  50% { transform: translate(-20px, 20px) scale(0.9); }

  75% { transform: translate(20px, 30px) scale(1.05); }

}



/* 扫描线 */

.scan-line {

  position: absolute;

  top: 0;

  left: 0;

  right: 0;

  height: 2px;

  background: linear-gradient(90deg, transparent, #00d4ff, transparent);

  animation: scanMove 4s linear infinite;

  opacity: 0.5;

}



@keyframes scanMove {

  0% { top: 0; }

  100% { top: 100%; }

}



/* 登录容器 */

.login-container {

  position: relative;

  z-index: 10;

  display: flex;

  flex-direction: column;

  align-items: center;

  padding: 20px;

  width: 100%;

  max-width: 440px;

}



/* 登录卡片 */

.login-card {

  width: 100%;

  background: rgba(15, 15, 35, 0.85);

  backdrop-filter: blur(20px);

  -webkit-backdrop-filter: blur(20px);

  border: 1px solid rgba(99, 102, 241, 0.3);

  border-radius: 24px;

  padding: 48px 40px;

  position: relative;

  overflow: hidden;

  box-shadow: 

    0 0 40px rgba(99, 102, 241, 0.2),

    0 25px 50px rgba(0, 0, 0, 0.5);

}



/* 卡片顶部发光边框 */

.login-card::before {

  content: '';

  position: absolute;

  top: 0;

  left: 0;

  right: 0;

  height: 2px;

  background: linear-gradient(90deg, 

    transparent, 

    #00d4ff, 

    #8b5cf6, 

    #00d4ff, 

    transparent);

  animation: borderGlow 3s ease-in-out infinite;

}



@keyframes borderGlow {

  0%, 100% { opacity: 0.5; }

  50% { opacity: 1; }

}



/* 角落装饰 */

.corner {

  position: absolute;

  width: 20px;

  height: 20px;

  border: 2px solid #6366f1;

}



.corner-tl { top: 10px; left: 10px; border-right: none; border-bottom: none; }

.corner-tr { top: 10px; right: 10px; border-left: none; border-bottom: none; }

.corner-bl { bottom: 10px; left: 10px; border-right: none; border-top: none; }

.corner-br { bottom: 10px; right: 10px; border-left: none; border-top: none; }



/* 抖动动画 */

.login-card.shake {

  animation: shake 0.5s ease-in-out;

}



@keyframes shake {

  0%, 100% { transform: translateX(0); }

  20%, 60% { transform: translateX(-10px); }

  40%, 80% { transform: translateX(10px); }

}



/* Logo 区域 */

.logo-section {

  text-align: center;

  margin-bottom: 40px;

}



.logo-icon {

  width: 80px;

  height: 80px;

  margin: 0 auto 16px;

  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));

  border-radius: 20px;

  display: flex;

  align-items: center;

  justify-content: center;

  border: 1px solid rgba(99, 102, 241, 0.3);

  position: relative;

}



.icon-glow {

  font-size: 40px;

  filter: drop-shadow(0 0 10px rgba(99, 102, 241, 0.5));

  animation: iconPulse 2s ease-in-out infinite;

}



@keyframes iconPulse {

  0%, 100% { transform: scale(1); }

  50% { transform: scale(1.1); }

}



.logo-text {

  font-size: 32px;

  font-weight: 800;

  background: linear-gradient(135deg, #6366f1, #8b5cf6, #06b6d4);

  -webkit-background-clip: text;

  -webkit-text-fill-color: transparent;

  background-clip: text;

  margin-bottom: 8px;

  letter-spacing: -0.02em;

}



.logo-subtitle {

  color: #94a3b8;

  font-size: 14px;

  letter-spacing: 0.1em;

}



/* 表单样式 */

.login-form {

  display: flex;

  flex-direction: column;

  gap: 20px;

}



/* 输入框组 */

.input-group {

  position: relative;

}



.input-icon {

  position: absolute;

  left: 16px;

  top: 50%;

  transform: translateY(-50%);

  width: 20px;

  height: 20px;

  color: #64748b;

  z-index: 2;

  transition: color 0.3s;

}



.input-icon svg {

  width: 100%;

  height: 100%;

}



.cyber-input {

  width: 100%;

  padding: 16px 48px;

  font-size: 15px;

  color: #f8fafc;

  background: rgba(0, 0, 0, 0.4);

  border: 1px solid rgba(99, 102, 241, 0.2);

  border-radius: 12px;

  outline: none;

  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  font-family: inherit;

}



.cyber-input::placeholder {

  color: #64748b;

}



.cyber-input:focus {

  border-color: #6366f1;

  box-shadow: 

    0 0 0 3px rgba(99, 102, 241, 0.1),

    0 0 20px rgba(99, 102, 241, 0.2);

}



.cyber-input:focus + .input-border {

  opacity: 1;

}



.cyber-input.has-error {

  border-color: #ff006e;

  box-shadow: 0 0 20px rgba(255, 0, 110, 0.2);

}



.input-group:focus-within .input-icon {

  color: #6366f1;

}



/* 输入框底部发光边框 */

.input-border {

  position: absolute;

  bottom: 0;

  left: 50%;

  transform: translateX(-50%);

  width: 0;

  height: 2px;

  background: linear-gradient(90deg, #6366f1, #8b5cf6);

  transition: width 0.3s;

  border-radius: 1px;

  opacity: 0;

}



.cyber-input:focus ~ .input-border {

  width: 80%;

  opacity: 1;

}



/* 密码显示切换 */

.toggle-password {

  position: absolute;

  right: 16px;

  top: 50%;

  transform: translateY(-50%);

  background: none;

  border: none;

  color: #64748b;

  cursor: pointer;

  padding: 4px;

  display: flex;

  align-items: center;

  justify-content: center;

  transition: color 0.3s;

  z-index: 2;

}



.toggle-password:hover {

  color: #6366f1;

}



.toggle-password svg {

  width: 20px;

  height: 20px;

}



/* 错误文本 */

.error-text {

  color: #ff006e;

  font-size: 12px;

  margin-top: 6px;

  padding-left: 16px;

}



/* 选项行 */

.options-row {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin: 4px 0;

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

  color: #94a3b8;

  font-size: 14px;

}



/* 忘记密码链接 */

.forgot-link {

  color: #6366f1;

  font-size: 14px;

  text-decoration: none;

  transition: all 0.3s;

}



.forgot-link:hover {

  color: #818cf8;

  text-shadow: 0 0 10px rgba(99, 102, 241, 0.5);

}



/* 登录按钮 */

.cyber-button {

  width: 100%;

  padding: 16px;

  font-size: 16px;

  font-weight: 600;

  color: white;

  background: linear-gradient(135deg, #6366f1, #8b5cf6);

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



.cyber-button:active:not(:disabled) {

  transform: translateY(0);

}



.cyber-button:disabled {

  opacity: 0.7;

  cursor: not-allowed;

}



.button-content {

  position: relative;

  z-index: 2;

  display: flex;

  align-items: center;

  justify-content: center;

  gap: 8px;

}



/* 按钮发光效果 */

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



/* 加载动画 */

.loading-spinner {

  width: 20px;

  height: 20px;

  border: 2px solid transparent;

  border-top-color: white;

  border-radius: 50%;

  animation: spin 0.8s linear infinite;

}



@keyframes spin {

  to { transform: rotate(360deg); }

}



/* 注册区域 */

.register-section {

  text-align: center;

  margin-top: 28px;

  padding-top: 24px;

  border-top: 1px solid rgba(99, 102, 241, 0.2);

}



.divider-text {

  color: #64748b;

  font-size: 14px;

}



.register-link {

  color: #6366f1;

  font-size: 14px;

  font-weight: 600;

  text-decoration: none;

  margin-left: 8px;

  transition: all 0.3s;

}



.register-link:hover {

  color: #818cf8;

  text-shadow: 0 0 10px rgba(99, 102, 241, 0.5);

}



/* 版本信息 */

.version-info {

  margin-top: 24px;

  color: #475569;

  font-size: 12px;

  display: flex;

  align-items: center;

  gap: 8px;

}



.separator {

  opacity: 0.5;

}



/* =====================================================

   响应式设计

   ===================================================== */



@media (max-width: 480px) {

  .login-card {

    padding: 36px 24px;

    border-radius: 20px;

  }

  

  .logo-icon {

    width: 64px;

    height: 64px;

  }

  

  .icon-glow {

    font-size: 32px;

  }

  

  .logo-text {

    font-size: 26px;

  }

  

  .cyber-input {

    padding: 14px 44px;

    font-size: 14px;

  }

  

  .cyber-button {

    padding: 14px;

    font-size: 15px;

  }

  

  .orb-1 { width: 250px; height: 250px; }

  .orb-2 { width: 200px; height: 200px; }

  .orb-3 { width: 150px; height: 150px; }

}



@media (max-width: 360px) {

  .login-container {

    padding: 16px;

  }

  

  .login-card {

    padding: 28px 20px;

  }

  

  .options-row {

    flex-direction: column;

    gap: 12px;

    align-items: flex-start;

  }

}

</style>

