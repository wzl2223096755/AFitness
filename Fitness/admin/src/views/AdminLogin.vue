<template>
  <div class="admin-login-page">
    <div class="cyber-background">
      <div class="grid-lines"></div>
      <div class="glow-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
      </div>
    </div>
    <div class="login-container">
      <div class="login-card" :class="{ 'shake': hasError }">
        <div class="logo-section">
          <h1 class="logo-text">AFitness Admin</h1>
          <p class="logo-subtitle">系统管理后台</p>
        </div>
        <div v-if="errorMessage" class="error-banner">
          <span>{{ errorMessage }}</span>
        </div>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="input-group">
            <input type="text" v-model="loginForm.username" placeholder="管理员用户名"
              class="cyber-input" :class="{ 'has-error': errors.username }"
              @focus="clearError('username')" autocomplete="username" />
          </div>
          <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
          <div class="input-group">
            <input :type="showPassword ? 'text' : 'password'" v-model="loginForm.password"
              placeholder="密码" class="cyber-input" :class="{ 'has-error': errors.password }"
              @focus="clearError('password')" @keyup.enter="handleLogin" autocomplete="current-password" />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
          <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
          <button type="submit" class="cyber-button" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            <span>{{ loading ? '登录中...' : '管理员登录' }}</span>
          </button>
        </form>
        <div class="admin-notice">仅限管理员账号登录</div>
        <div class="user-link-section">
          <span>普通用户?</span>
          <a :href="userFrontendUrl" class="user-link">前往用户端</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@shared/api/auth'
import { USER_FRONTEND_URL } from '../router/index.js'

const router = useRouter()
const userFrontendUrl = USER_FRONTEND_URL
const loginForm = reactive({ username: '', password: '' })
const loading = ref(false)
const showPassword = ref(false)
const hasError = ref(false)
const errorMessage = ref('')
const errors = reactive({ username: '', password: '' })

function clearError(field) {
  errors[field] = ''
  errorMessage.value = ''
  hasError.value = false
}

function validateForm() {
  let isValid = true
  if (!loginForm.username.trim()) { errors.username = '请输入用户名'; isValid = false }
  if (!loginForm.password) { errors.password = '请输入密码'; isValid = false }
  return isValid
}

function triggerShake() {
  hasError.value = true
  setTimeout(() => { hasError.value = false }, 500)
}

function isAdminRole(user) {
  return user.role === 'ADMIN' || user.role === 'ROLE_ADMIN'
}

async function handleLogin() {
  if (!validateForm()) { triggerShake(); return }
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await authApi.login({ username: loginForm.username.trim(), password: loginForm.password })
    if (response.code === 200 && response.data) {
      const data = response.data
      // 后端返回格式: { accessToken, refreshToken, userId, username, role }
      const role = data.role
      if (!role || (role !== 'ADMIN' && role !== 'ROLE_ADMIN')) {
        errorMessage.value = '仅限管理员账号登录'
        triggerShake()
        loading.value = false
        return
      }
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('userInfo', JSON.stringify({
        userId: data.userId,
        username: data.username,
        role: data.role
      }))
      localStorage.setItem('userRole', data.role)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      router.push('/dashboard')
    } else {
      errorMessage.value = response.message || '登录失败'
      triggerShake()
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '登录失败'
    triggerShake()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
$admin-primary: #e94560;
$admin-bg-dark: #1a1a2e;
$admin-bg-darker: #16213e;
$admin-text: #f8fafc;
$admin-text-muted: #94a3b8;

.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $admin-bg-dark;
  position: relative;
}

.cyber-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .grid-lines {
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba($admin-primary, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba($admin-primary, 0.03) 1px, transparent 1px);
    background-size: 50px 50px;
  }
  .glow-orbs .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.4;
  }
  .orb-1 { width: 400px; height: 400px; background: $admin-primary; top: -100px; right: -100px; }
  .orb-2 { width: 300px; height: 300px; background: #f39c12; bottom: -50px; left: -50px; }
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba($admin-bg-darker, 0.9);
  border: 1px solid rgba($admin-primary, 0.3);
  border-radius: 20px;
  padding: 40px;
  backdrop-filter: blur(20px);
  &.shake { animation: shake 0.5s ease-in-out; }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-10px); }
  40%, 80% { transform: translateX(10px); }
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
  .logo-text {
    font-size: 28px;
    font-weight: 700;
    background: linear-gradient(135deg, $admin-primary, #f39c12);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 8px;
  }
  .logo-subtitle { color: $admin-text-muted; font-size: 14px; }
}

.error-banner {
  padding: 12px 16px;
  background: rgba(#ff6b6b, 0.1);
  border: 1px solid rgba(#ff6b6b, 0.3);
  border-radius: 10px;
  margin-bottom: 20px;
  color: #ff6b6b;
  font-size: 14px;
}

.login-form { display: flex; flex-direction: column; gap: 16px; }

.input-group {
  position: relative;
  .cyber-input {
    width: 100%;
    padding: 14px;
    background: rgba($admin-bg-dark, 0.8);
    border: 1px solid rgba($admin-primary, 0.2);
    border-radius: 12px;
    color: $admin-text;
    font-size: 15px;
    &:focus { outline: none; border-color: $admin-primary; }
    &::placeholder { color: $admin-text-muted; }
    &.has-error { border-color: #ff6b6b; }
  }
  .toggle-password {
    position: absolute;
    right: 14px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: $admin-text-muted;
    cursor: pointer;
    &:hover { color: $admin-text; }
  }
}

.error-text { color: #ff6b6b; font-size: 12px; margin-top: -8px; }

.cyber-button {
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, $admin-primary, darken($admin-primary, 10%));
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 30px rgba($admin-primary, 0.4); }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
  .loading-spinner {
    width: 18px;
    height: 18px;
    border: 2px solid rgba(white, 0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin { to { transform: rotate(360deg); } }

.admin-notice {
  text-align: center;
  margin-top: 24px;
  padding: 12px;
  background: rgba($admin-primary, 0.1);
  border-radius: 10px;
  color: $admin-text-muted;
  font-size: 13px;
}

.user-link-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba($admin-primary, 0.1);
  color: $admin-text-muted;
  font-size: 13px;
  .user-link {
    color: $admin-primary;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}
</style>
