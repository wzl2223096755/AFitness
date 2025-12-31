import { defineStore } from 'pinia'
import { userApi } from '@/api/user'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    userProfile: null,
    isAuthenticated: false,
    loading: false,
    error: null,
    
    // 权限相关
    permissions: [],
    roles: [],
    
    // 用户设置
    settings: {
      theme: 'light',
      language: 'zh-CN',
      notifications: true,
      autoSave: true
    }
  }),

  getters: {
    // 获取token
    token: () => localStorage.getItem('token'),
    
    // 获取用户信息
    user: (state) => state.currentUser,
    
    // 获取用户显示名称
    displayName: (state) => {
      if (!state.currentUser) return '未登录'
      return state.currentUser.nickname || state.currentUser.username || '用户'
    },
    
    // 检查是否为管理员
    isAdmin: (state) => {
      return state.roles.includes('ROLE_ADMIN') || state.roles.includes('ADMIN')
    },
    
    // 检查是否有特定权限
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission) || state.roles.includes('ROLE_ADMIN')
    },
    
    // 检查是否有特定角色
    hasRole: (state) => (role) => {
      return state.roles.includes(role)
    },
    
    // 获取用户头像
    avatar: (state) => {
      return state.currentUser?.avatar || '/default-avatar.png'
    }
  },

  actions: {
    // 登录
    async login(credentials) {
      this.loading = true
      this.error = null
      
      try {
        const response = await authApi.login(credentials)
        const { accessToken: token, refreshToken, userId, username, role } = response.data
        
        // 保存token
        localStorage.setItem('token', token)
        localStorage.setItem('refreshToken', refreshToken)
        localStorage.setItem('isLoggedIn', 'true')
        
        // 设置用户信息
        this.currentUser = { id: userId, username, role }
        this.roles = [role]
        this.isAuthenticated = true
        
        return response
      } catch (error) {
        this.error = error.response?.data?.message || error.message || '登录失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    // 登出
    async logout() {
      this.loading = true
      
      try {
        await authApi.logout()
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        // 清除本地数据
        this.clearUserData()
        this.loading = false
      }
    },

    // 刷新token
    async refreshToken() {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        throw new Error('没有刷新token')
      }
      
      try {
        const response = await authApi.refreshToken({ refreshToken })
        const { accessToken: token } = response.data
        
        localStorage.setItem('token', token)
        return token
      } catch (error) {
        this.clearUserData()
        throw error
      }
    },

    // 获取当前用户信息
    async getCurrentUser() {
      if (!this.token) {
        return null
      }
      
      this.loading = true
      try {
        const response = await authApi.getCurrentUser()
        if (response.code === 200 && response.data) {
          this.setCurrentUser(response.data)
          this.isAuthenticated = true
        }
        return response
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      } finally {
        this.loading = false
      }
    },

    // 获取当前用户信息
    async fetchCurrentUser() {
      return this.getCurrentUser()
    },

    // 更新用户信息
    async updateProfile(profileData) {
      this.loading = true
      this.error = null
      
      try {
        const response = await userApi.updateProfile(profileData)
        this.currentUser = { ...this.currentUser, ...response.data }
        return response
      } catch (error) {
        this.error = error.message || '更新用户信息失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    // 修改密码
    async changePassword(passwordData) {
      this.loading = true
      this.error = null
      
      try {
        const response = await userApi.changePassword(passwordData)
        return response
      } catch (error) {
        this.error = error.message || '修改密码失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    // 设置当前用户
    setCurrentUser(user) {
      this.currentUser = user
      this.roles = user.roles || []
      this.permissions = user.permissions || []
    },

    // 清除用户数据
    clearUserData() {
      this.currentUser = null
      this.userProfile = null
      this.isAuthenticated = false
      this.roles = []
      this.permissions = []
      this.error = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userInfo')
    },

    // 更新用户设置
    updateSettings(newSettings) {
      this.settings = { ...this.settings, ...newSettings }
      localStorage.setItem('userSettings', JSON.stringify(this.settings))
    },

    // 加载用户设置
    loadSettings() {
      const savedSettings = localStorage.getItem('userSettings')
      if (savedSettings) {
        try {
          this.settings = { ...this.settings, ...JSON.parse(savedSettings) }
        } catch (error) {
          console.error('加载用户设置失败:', error)
        }
      }
    },

    // 检查认证状态
    checkAuthStatus() {
      const token = localStorage.getItem('token')
      if (token) {
        // 这里可以添加token验证逻辑
        return true
      }
      return false
    },

    // 初始化用户状态
    async initializeUser() {
      this.loadSettings()
      
      if (this.checkAuthStatus()) {
        try {
          await this.fetchCurrentUser()
        } catch (error) {
          console.error('初始化用户状态失败:', error)
          this.clearUserData()
        }
      }
    }
  }
})