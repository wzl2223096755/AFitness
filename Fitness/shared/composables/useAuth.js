/**
 * 认证状态管理 Composable
 * Authentication state management composable for shared use
 */
import { ref, computed, readonly } from 'vue'

// 全局认证状态
const token = ref(null)
const user = ref(null)
const isAuthenticated = ref(false)

// Token 存储键名
const TOKEN_KEY = 'fitness_token'
const USER_KEY = 'fitness_user'

/**
 * 初始化认证状态（从本地存储恢复）
 */
const initAuth = () => {
  if (typeof localStorage !== 'undefined') {
    const savedToken = localStorage.getItem(TOKEN_KEY)
    const savedUser = localStorage.getItem(USER_KEY)
    
    if (savedToken) {
      token.value = savedToken
      isAuthenticated.value = true
    }
    
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        console.warn('Failed to parse saved user:', e)
      }
    }
  }
}

/**
 * 设置认证信息
 * @param {string} newToken JWT token
 * @param {Object} userInfo 用户信息
 */
const setAuth = (newToken, userInfo = null) => {
  token.value = newToken
  user.value = userInfo
  isAuthenticated.value = !!newToken
  
  if (typeof localStorage !== 'undefined') {
    if (newToken) {
      localStorage.setItem(TOKEN_KEY, newToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
    
    if (userInfo) {
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }
}

/**
 * 清除认证信息
 */
const clearAuth = () => {
  token.value = null
  user.value = null
  isAuthenticated.value = false
  
  if (typeof localStorage !== 'undefined') {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}

/**
 * 更新用户信息
 * @param {Object} userInfo 用户信息
 */
const updateUser = (userInfo) => {
  user.value = { ...user.value, ...userInfo }
  
  if (typeof localStorage !== 'undefined' && user.value) {
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }
}

/**
 * 检查是否有特定角色
 * @param {string|string[]} roles 角色或角色数组
 */
const hasRole = (roles) => {
  if (!user.value || !user.value.roles) return false
  
  const userRoles = Array.isArray(user.value.roles) ? user.value.roles : [user.value.roles]
  const requiredRoles = Array.isArray(roles) ? roles : [roles]
  
  return requiredRoles.some(role => userRoles.includes(role))
}

/**
 * 检查是否是管理员
 */
const isAdmin = computed(() => hasRole(['ADMIN', 'admin', 'ROLE_ADMIN']))

/**
 * 获取用户显示名称
 */
const displayName = computed(() => {
  if (!user.value) return ''
  return user.value.nickname || user.value.username || user.value.email || ''
})

/**
 * 认证状态管理 Composable
 */
export function useAuth() {
  // 初始化
  if (typeof window !== 'undefined') {
    initAuth()
  }
  
  return {
    // 状态
    token: readonly(token),
    user: readonly(user),
    isAuthenticated: readonly(isAuthenticated),
    
    // 计算属性
    isAdmin,
    displayName,
    
    // 方法
    setAuth,
    clearAuth,
    updateUser,
    hasRole,
    initAuth
  }
}

export default useAuth
