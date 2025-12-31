import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useFitnessStore } from './fitness'

// Mock the API modules
vi.mock('../api/fitness', () => ({
  fitnessApi: {
    getFitnessData: vi.fn(),
    saveFitnessData: vi.fn(),
    updateFitnessData: vi.fn(),
    deleteFitnessData: vi.fn(),
    generateFitnessData: vi.fn(),
    getDashboardStats: vi.fn()
  }
}))

vi.mock('../api/user', () => ({
  userApi: {
    getUsers: vi.fn()
  }
}))

import { fitnessApi } from '../api/fitness'
import { userApi } from '../api/user'

describe('Fitness Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      const store = useFitnessStore()
      
      expect(store.users).toEqual([])
      expect(store.devices).toEqual([])
      expect(store.fitnessData).toEqual([])
      expect(store.realTimeData).toEqual([])
      expect(store.onlineDevices).toBe(0)
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
    })
  })

  describe('getters', () => {
    it('getUserById should return user by id', () => {
      const store = useFitnessStore()
      store.users = [
        { id: 1, name: 'User 1' },
        { id: 2, name: 'User 2' }
      ]
      
      expect(store.getUserById(1)).toEqual({ id: 1, name: 'User 1' })
      expect(store.getUserById(3)).toBeUndefined()
    })

    it('getDeviceById should return device by id', () => {
      const store = useFitnessStore()
      store.devices = [
        { id: 1, name: 'Device 1' },
        { id: 2, name: 'Device 2' }
      ]
      
      expect(store.getDeviceById(1)).toEqual({ id: 1, name: 'Device 1' })
      expect(store.getDeviceById(3)).toBeUndefined()
    })
  })

  describe('actions', () => {
    describe('fetchUsers', () => {
      it('should fetch and set users', async () => {
        const store = useFitnessStore()
        const mockUsers = [{ id: 1, name: 'Test User' }]
        userApi.getUsers.mockResolvedValue({ data: { data: mockUsers } })
        
        await store.fetchUsers()
        
        expect(store.users).toEqual(mockUsers)
      })

      it('should handle fetch users error', async () => {
        const store = useFitnessStore()
        userApi.getUsers.mockRejectedValue(new Error('Network error'))
        
        await store.fetchUsers()
        
        expect(store.users).toEqual([])
      })
    })

    describe('fetchMyFitnessData', () => {
      it('should fetch and set fitness data', async () => {
        const store = useFitnessStore()
        const mockData = [{ id: 1, type: 'strength' }]
        fitnessApi.getFitnessData.mockResolvedValue({ data: { data: mockData } })
        
        await store.fetchMyFitnessData()
        
        expect(store.fitnessData).toEqual(mockData)
      })

      it('should handle fetch fitness data error', async () => {
        const store = useFitnessStore()
        fitnessApi.getFitnessData.mockRejectedValue(new Error('Network error'))
        
        await store.fetchMyFitnessData()
        
        expect(store.fitnessData).toEqual([])
      })
    })

    describe('addFitnessData', () => {
      it('should add new fitness data', async () => {
        const store = useFitnessStore()
        const newData = { type: 'cardio', duration: 30 }
        const savedData = { id: 1, ...newData }
        fitnessApi.saveFitnessData.mockResolvedValue({ data: { data: savedData } })
        
        const result = await store.addFitnessData(newData)
        
        expect(result).toEqual(savedData)
        expect(store.fitnessData[0]).toEqual(savedData)
      })

      it('should throw error on add failure', async () => {
        const store = useFitnessStore()
        fitnessApi.saveFitnessData.mockRejectedValue(new Error('Save failed'))
        
        await expect(store.addFitnessData({})).rejects.toThrow('Save failed')
      })
    })

    describe('deleteFitnessData', () => {
      it('should delete fitness data by id', async () => {
        const store = useFitnessStore()
        store.fitnessData = [
          { id: 1, type: 'strength' },
          { id: 2, type: 'cardio' }
        ]
        fitnessApi.deleteFitnessData.mockResolvedValue({})
        
        await store.deleteFitnessData(1)
        
        expect(store.fitnessData).toHaveLength(1)
        expect(store.fitnessData[0].id).toBe(2)
      })
    })

    describe('updateOnlineDevices', () => {
      it('should count online devices', () => {
        const store = useFitnessStore()
        store.devices = [
          { id: 1, status: 'ONLINE' },
          { id: 2, status: 'OFFLINE' },
          { id: 3, status: 'ONLINE' }
        ]
        
        store.updateOnlineDevices()
        
        expect(store.onlineDevices).toBe(2)
      })
    })

    describe('addRealTimeData', () => {
      it('should add data to the beginning of fitnessData', () => {
        const store = useFitnessStore()
        store.fitnessData = [{ id: 1 }]
        
        store.addRealTimeData({ id: 2 })
        
        expect(store.fitnessData[0]).toEqual({ id: 2 })
        expect(store.fitnessData).toHaveLength(2)
      })

      it('should limit fitnessData to 100 items', () => {
        const store = useFitnessStore()
        store.fitnessData = Array.from({ length: 100 }, (_, i) => ({ id: i }))
        
        store.addRealTimeData({ id: 'new' })
        
        expect(store.fitnessData).toHaveLength(100)
        expect(store.fitnessData[0].id).toBe('new')
      })
    })

    describe('clearError', () => {
      it('should clear error state', () => {
        const store = useFitnessStore()
        store.error = 'Some error'
        
        store.clearError()
        
        expect(store.error).toBeNull()
      })
    })
  })
})
