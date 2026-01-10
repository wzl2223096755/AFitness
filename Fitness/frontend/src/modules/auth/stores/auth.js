/**
 * 认证模块 Store
 * Auth module Pinia store for authentication state management
 */
import { defineStore } from 'pinia'
import { authApi } from '../api'

// 管理端前端URL
const ADMIN_FRONTEND_URL = 'http://localhost:3002'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 认证状态
    isAuthenticated: false,
    token: null,
    refreshToken: null,
    
    // 用户信息
    user: null,
    userId: null,
    username: null,
    role: null,
    
    // 加载状态
    loading: false,
    error: null,
    
    // 记住我
    rememberMe: false
  }),

  getters: {
    /**
     * 获取当前token
     */
    currentToken: (state) => state.token || localStorage.getItem('token'),
    
    /**
     * 检查是否已登录
     */
    isLoggedIn: (state) => state.isAuthenticated || !!localStorage.getItem('token'),
    
    /**
     * 检查是否为管理员
     */
    isAdmin: (state) => {
      const role = state.role || localStorage.getItem('userRole')
      return role === 'ADMIN' || role === 'ROLE_ADMIN'
    },
    
    /**
     * 获取用户显示名称
     */
    displayName: (state) => {
      if (state.user?.nickname) return state.user.nickname
      if (state.username) return state.username
      return localStorage.getItem('username') || '用户'
    },
    
    /**
     * 获取用户信息
     */
    userInfo: (state) => {
      if (state.user) return state.user
      const savedInfo = localStorage.getItem('userInfo')
      if (savedInfo) {
        try {
          return JSON.parse(savedInfo)
        } catch {
          return null
        }
      }
      return null
    }
  },

  actions: {
    /**
     * 用户登录
     * @param {Object} credentials 登录凭证
     * @returns {Promise<Object>} 登录结果
     */
    async login(credentials) {
      this.loading = true
      this.error = null
      
      try {
        const response = await authApi.login({
          username: credentials.username,
          password: credentials.password
        })
        
        if (response.code === 200 && response.data?.accessToken) {
          const data = response.data
          
          // 更新状态
          this.token = data.accessToken
          this.refreshToken = data.refreshToken
          this.userId = data.userId
          this.username = data.username || credentials.username
          this.role = data.role || 'USER'
          this.isAuthenticated = true
          this.rememberMe = credentials.rememberMe || false
          
          // 保存到本地存储
          this._saveToStorage(data, credentials)
          
          // 检查是否为管理员
          const isAdmin = this.role === 'ADMIN' || this.role === 'ROLE_ADMIN'
          
          return {
            success: true,
            isAdmin,
            data
          }
        } else {
          this.error = response.message || '登录失败'
          return {
            success: false,
            message: this.error
          }
        }
      } catch (error) {
        console.error('登录失败:', error)
        this.error = error.response?.data?.message || error.message || '登录失败'
        return {
          success: false,
          message: this.error
        }
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户登出
     */
    async logout() {
      this.loading = true
      
      try {
        await authApi.logout()
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        this._clearAuth()
        this.loading = false
      }
    },

    /**
     * 刷新Token
     * @returns {Promise<string>} 新的token
     */
    async refreshAccessToken() {
      const refreshToken = this.refreshToken || localStorage.getItem('refreshToken')
      
      if (!refreshToken) {
        throw new Error('没有刷新token')
      }
      
      try {
        const response = await authApi.refreshToken({ refreshToken })
        const { accessToken } = response.data
        
        this.token = accessToken
        localStorage.setItem('token', accessToken)
        
        return accessToken
      } catch (error) {
        this._clearAuth()
        throw error
      }
    },

    /**
     * 获取当前用户信息
     */
    async fetchCurrentUser() {
      if (!this.currentToken) {
        return null
      }
      
      this.loading = true
      
      try {
        const response = await authApi.getCurrentUser()
        
        if (response.code === 200 && response.data) {
          this.user = response.data
          this.isAuthenticated = true
          
          // 更新本地存储
          localStorage.setItem('userInfo', JSON.stringify(response.data))
        }
        
        return response
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      } finally {
        this.loading = false
      }
    },

    /**
     * 检查认证状态
     */
    checkAuthStatus() {
      const token = localStorage.getItem('token')
      
      if (token) {
        this.token = token
        this.refreshToken = localStorage.getItem('refreshToken')
        this.username = localStorage.getItem('username')
        this.role = localStorage.getItem('userRole')
        this.userId = localStorage.getItem('userId')
        this.isAuthenticated = true
        
        // 尝试恢复用户信息
        const savedInfo = localStorage.getItem('userInfo')
        if (savedInfo) {
          try {
            this.user = JSON.parse(savedInfo)
          } catch {
            // 忽略解析错误
          }
        }
        
        return true
      }
      
      return false
    },

    /**
     * 初始化认证状态
     */
    async initialize() {
      if (this.checkAuthStatus()) {
        try {
          await this.fetchCurrentUser()
        } catch (error) {
          console.error('初始化认证状态失败:', error)
          this._clearAuth()
        }
      }
    },

    /**
     * 重定向到管理端
     */
    redirectToAdmin() {
      window.location.href = ADMIN_FRONTEND_URL
    },

    /**
     * 保存认证信息到本地存储
     * @private
     */
    _saveToStorage(data, credentials) {
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('username', data.username || credentials.username)
      localStorage.setItem('userRole', data.role || 'USER')
      localStorage.setItem('userId', data.userId)
      
      if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken)
      }
      
      // 保存完整用户信息
      localStorage.setItem('userInfo', JSON.stringify({
        userId: data.userId,
        username: data.username || credentials.username,
        role: data.role
      }))
      
      // 记住用户名
      if (credentials.rememberMe) {
        localStorage.setItem('rememberedUsername', credentials.username)
      }
    },

    /**
     * 清除认证信息
     * @private
     */
    _clearAuth() {
      // 清除状态
      this.token = null
      this.refreshToken = null
      this.user = null
      this.userId = null
      this.username = null
      this.role = null
      this.isAuthenticated = false
      this.error = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
    }
  }
})

export default useAuthStore
