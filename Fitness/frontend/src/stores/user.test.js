import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from './user'

// Mock the API modules
vi.mock('@/api/user', () => ({
  userApi: {
    updateProfile: vi.fn(),
    changePassword: vi.fn()
  }
}))

vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    refreshToken: vi.fn(),
    getCurrentUser: vi.fn()
  }
}))

import { userApi } from '@/api/user'
import { authApi } from '@/api/auth'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      const store = useUserStore()
      
      expect(store.currentUser).toBeNull()
      expect(store.userProfile).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
      expect(store.permissions).toEqual([])
      expect(store.roles).toEqual([])
    })

    it('should have default settings', () => {
      const store = useUserStore()
      
      expect(store.settings.theme).toBe('light')
      expect(store.settings.language).toBe('zh-CN')
      expect(store.settings.notifications).toBe(true)
      expect(store.settings.autoSave).toBe(true)
    })
  })

  describe('getters', () => {
    it('displayName should return username when available', () => {
      const store = useUserStore()
      store.currentUser = { username: 'testuser', nickname: null }
      
      expect(store.displayName).toBe('testuser')
    })

    it('displayName should return nickname when available', () => {
      const store = useUserStore()
      store.currentUser = { username: 'testuser', nickname: 'Test User' }
      
      expect(store.displayName).toBe('Test User')
    })

    it('displayName should return "未登录" when no user', () => {
      const store = useUserStore()
      
      expect(store.displayName).toBe('未登录')
    })

    it('isAdmin should return true for admin role', () => {
      const store = useUserStore()
      store.roles = ['ROLE_ADMIN']
      
      expect(store.isAdmin).toBe(true)
    })

    it('isAdmin should return false for non-admin', () => {
      const store = useUserStore()
      store.roles = ['ROLE_USER']
      
      expect(store.isAdmin).toBe(false)
    })

    it('hasPermission should check permissions', () => {
      const store = useUserStore()
      store.permissions = ['read', 'write']
      
      expect(store.hasPermission('read')).toBe(true)
      expect(store.hasPermission('delete')).toBe(false)
    })

    it('hasPermission should return true for admin', () => {
      const store = useUserStore()
      store.roles = ['ROLE_ADMIN']
      
      expect(store.hasPermission('any_permission')).toBe(true)
    })

    it('hasRole should check roles', () => {
      const store = useUserStore()
      store.roles = ['ROLE_USER', 'ROLE_EDITOR']
      
      expect(store.hasRole('ROLE_USER')).toBe(true)
      expect(store.hasRole('ROLE_ADMIN')).toBe(false)
    })
  })

  describe('actions', () => {
    describe('login', () => {
      it('should login successfully', async () => {
        const store = useUserStore()
        const mockResponse = {
          data: {
            accessToken: 'test-token',
            refreshToken: 'test-refresh-token',
            userId: 1,
            username: 'testuser',
            role: 'ROLE_USER'
          }
        }
        authApi.login.mockResolvedValue(mockResponse)
        
        await store.login({ username: 'testuser', password: 'password' })
        
        expect(store.isAuthenticated).toBe(true)
        expect(store.currentUser.username).toBe('testuser')
        expect(localStorage.getItem('token')).toBe('test-token')
      })

      it('should handle login error', async () => {
        const store = useUserStore()
        authApi.login.mockRejectedValue({ 
          response: { data: { message: 'Invalid credentials' } } 
        })
        
        await expect(store.login({ username: 'test', password: 'wrong' }))
          .rejects.toBeDefined()
        
        expect(store.error).toBe('Invalid credentials')
        expect(store.isAuthenticated).toBe(false)
      })
    })

    describe('logout', () => {
      it('should clear user data on logout', async () => {
        const store = useUserStore()
        store.currentUser = { id: 1, username: 'test' }
        store.isAuthenticated = true
        localStorage.setItem('token', 'test-token')
        
        authApi.logout.mockResolvedValue({})
        
        await store.logout()
        
        expect(store.currentUser).toBeNull()
        expect(store.isAuthenticated).toBe(false)
        expect(localStorage.getItem('token')).toBeNull()
      })
    })

    describe('setCurrentUser', () => {
      it('should set current user and roles', () => {
        const store = useUserStore()
        const user = {
          id: 1,
          username: 'testuser',
          roles: ['ROLE_USER'],
          permissions: ['read']
        }
        
        store.setCurrentUser(user)
        
        expect(store.currentUser).toEqual(user)
        expect(store.roles).toEqual(['ROLE_USER'])
        expect(store.permissions).toEqual(['read'])
      })
    })

    describe('clearUserData', () => {
      it('should clear all user data', () => {
        const store = useUserStore()
        store.currentUser = { id: 1 }
        store.isAuthenticated = true
        store.roles = ['ROLE_USER']
        localStorage.setItem('token', 'test')
        
        store.clearUserData()
        
        expect(store.currentUser).toBeNull()
        expect(store.isAuthenticated).toBe(false)
        expect(store.roles).toEqual([])
        expect(localStorage.getItem('token')).toBeNull()
      })
    })

    describe('updateSettings', () => {
      it('should update settings', () => {
        const store = useUserStore()
        
        store.updateSettings({ theme: 'dark' })
        
        expect(store.settings.theme).toBe('dark')
        expect(store.settings.language).toBe('zh-CN') // unchanged
      })

      it('should persist settings to localStorage', () => {
        const store = useUserStore()
        
        store.updateSettings({ theme: 'dark' })
        
        const saved = JSON.parse(localStorage.getItem('userSettings'))
        expect(saved.theme).toBe('dark')
      })
    })

    describe('loadSettings', () => {
      it('should load settings from localStorage', () => {
        const store = useUserStore()
        localStorage.setItem('userSettings', JSON.stringify({ theme: 'dark' }))
        
        store.loadSettings()
        
        expect(store.settings.theme).toBe('dark')
      })

      it('should handle invalid JSON in localStorage', () => {
        const store = useUserStore()
        localStorage.setItem('userSettings', 'invalid-json')
        
        store.loadSettings()
        
        expect(store.settings.theme).toBe('light') // default value
      })
    })

    describe('checkAuthStatus', () => {
      it('should return true when token exists', () => {
        const store = useUserStore()
        localStorage.setItem('token', 'test-token')
        
        expect(store.checkAuthStatus()).toBe(true)
      })

      it('should return false when no token', () => {
        const store = useUserStore()
        
        expect(store.checkAuthStatus()).toBe(false)
      })
    })
  })
})
