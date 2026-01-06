import { defineStore } from 'pinia'
import { authApi } from '@shared/api/auth'

/**
 * 管理端用户状态管理
 * 专门用于管理员用户的认证和状态管理
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    isAuthenticated: false,
    loading: false,
    error: null,
    roles: []
  }),

  getters: {
    // 获取token
    token: () => localStorage.getItem('token'),
    
    // 获取用户信息
    user: (state) => state.currentUser,
    
    // 获取用户显示名称
    displayName: (state) => {
      if (!state.currentUser) return '管理员'
      return state.currentUser.nickname || state.currentUser.username || '管理员'
    },
    
    // 检查是否为管理员
    isAdmin: (state) => {
      return state.roles.includes('ROLE_ADMIN') || state.roles.includes('ADMIN')
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
        
        // 验证是否为管理员
        if (role !== 'ADMIN' && role !== 'ROLE_ADMIN') {
          throw new Error('仅限管理员登录')
        }
        
        // 保存token
        localStorage.setItem('token', token)
        localStorage.setItem('refreshToken', refreshToken)
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('userRole', role)
        localStorage.setItem('userInfo', JSON.stringify({ id: userId, username, role }))
        
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
        this.clearUserData()
        this.loading = false
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

    // 设置当前用户
    setCurrentUser(user) {
      this.currentUser = user
      this.roles = user.roles || [user.role] || []
    },

    // 清除用户数据
    clearUserData() {
      this.currentUser = null
      this.isAuthenticated = false
      this.roles = []
      this.error = null
      
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
    }
  }
})
