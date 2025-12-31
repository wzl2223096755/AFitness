import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTrainingStore } from './training'

// Mock the API modules
vi.mock('@/api/strengthTraining', () => ({
  strengthTrainingApi: {
    getStrengthTrainingData: vi.fn(),
    createStrengthTrainingData: vi.fn(),
    updateStrengthTrainingData: vi.fn(),
    deleteStrengthTrainingData: vi.fn(),
    getMaxWeightStats: vi.fn()
  }
}))

vi.mock('@/api/cardioTraining', () => ({
  cardioTrainingApi: {
    getCardioTrainingData: vi.fn(),
    createCardioTrainingData: vi.fn()
  }
}))

vi.mock('@/api/fitness', () => ({
  fitnessApi: {
    saveFitnessData: vi.fn(),
    assessRecovery: vi.fn(),
    getTrainingSuggestions: vi.fn(),
    getLoadTrend: vi.fn()
  }
}))

import { strengthTrainingApi } from '@/api/strengthTraining'
import { cardioTrainingApi } from '@/api/cardioTraining'
import { fitnessApi } from '@/api/fitness'

describe('Training Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      const store = useTrainingStore()
      
      expect(store.strengthTrainingData).toEqual([])
      expect(store.cardioTrainingData).toEqual([])
      expect(store.recoveryData).toEqual([])
      expect(store.trainingStats).toBeNull()
      expect(store.strengthLoading).toBe(false)
      expect(store.cardioLoading).toBe(false)
      expect(store.recoveryLoading).toBe(false)
      expect(store.statsLoading).toBe(false)
    })

    it('should have correct pagination defaults', () => {
      const store = useTrainingStore()
      
      expect(store.pagination.currentPage).toBe(0)
      expect(store.pagination.pageSize).toBe(20)
      expect(store.pagination.totalElements).toBe(0)
      expect(store.pagination.totalPages).toBe(0)
    })
  })

  describe('getters', () => {
    it('latestStrengthTraining should return first item', () => {
      const store = useTrainingStore()
      store.strengthTrainingData = [
        { id: 1, exercise: 'Squat' },
        { id: 2, exercise: 'Bench' }
      ]
      
      expect(store.latestStrengthTraining).toEqual({ id: 1, exercise: 'Squat' })
    })

    it('latestStrengthTraining should return null when empty', () => {
      const store = useTrainingStore()
      
      expect(store.latestStrengthTraining).toBeNull()
    })

    it('latestCardioTraining should return first item', () => {
      const store = useTrainingStore()
      store.cardioTrainingData = [
        { id: 1, type: 'Running' },
        { id: 2, type: 'Cycling' }
      ]
      
      expect(store.latestCardioTraining).toEqual({ id: 1, type: 'Running' })
    })

    it('isLoading should return true when any loading is true', () => {
      const store = useTrainingStore()
      
      expect(store.isLoading).toBe(false)
      
      store.strengthLoading = true
      expect(store.isLoading).toBe(true)
    })

    it('allErrors should collect all errors', () => {
      const store = useTrainingStore()
      store.strengthError = 'Strength error'
      store.cardioError = 'Cardio error'
      
      expect(store.allErrors).toEqual(['Strength error', 'Cardio error'])
    })

    it('allErrors should return empty array when no errors', () => {
      const store = useTrainingStore()
      
      expect(store.allErrors).toEqual([])
    })
  })

  describe('actions', () => {
    describe('resetErrors', () => {
      it('should reset all error states', () => {
        const store = useTrainingStore()
        store.strengthError = 'error1'
        store.cardioError = 'error2'
        store.recoveryError = 'error3'
        store.statsError = 'error4'
        
        store.resetErrors()
        
        expect(store.strengthError).toBeNull()
        expect(store.cardioError).toBeNull()
        expect(store.recoveryError).toBeNull()
        expect(store.statsError).toBeNull()
      })
    })

    describe('fetchStrengthTrainingData', () => {
      it('should fetch and set strength training data', async () => {
        const store = useTrainingStore()
        const mockData = [{ id: 1, exercise: 'Squat' }]
        strengthTrainingApi.getStrengthTrainingData.mockResolvedValue({
          data: mockData
        })
        
        await store.fetchStrengthTrainingData()
        
        expect(store.strengthTrainingData).toEqual(mockData)
        expect(store.strengthLoading).toBe(false)
      })

      it('should handle paginated response', async () => {
        const store = useTrainingStore()
        strengthTrainingApi.getStrengthTrainingData.mockResolvedValue({
          data: {
            content: [{ id: 1 }],
            currentPage: 1,
            pageSize: 20,
            totalElements: 100,
            totalPages: 5
          }
        })
        
        await store.fetchStrengthTrainingData()
        
        expect(store.pagination.totalElements).toBe(100)
        expect(store.pagination.totalPages).toBe(5)
      })

      it('should handle fetch error', async () => {
        const store = useTrainingStore()
        strengthTrainingApi.getStrengthTrainingData.mockRejectedValue(
          new Error('Network error')
        )
        
        await expect(store.fetchStrengthTrainingData()).rejects.toThrow()
        expect(store.strengthError).toBe('Network error')
      })
    })

    describe('createStrengthTrainingData', () => {
      it('should create and add new training data', async () => {
        const store = useTrainingStore()
        const newData = { exercise: 'Deadlift', weight: 100 }
        const savedData = { id: 1, ...newData }
        strengthTrainingApi.createStrengthTrainingData.mockResolvedValue({
          data: savedData
        })
        
        await store.createStrengthTrainingData(newData)
        
        expect(store.strengthTrainingData[0]).toEqual(savedData)
      })
    })

    describe('updateStrengthTrainingData', () => {
      it('should update existing training data', async () => {
        const store = useTrainingStore()
        store.strengthTrainingData = [{ id: 1, weight: 100 }]
        const updatedData = { id: 1, weight: 110 }
        strengthTrainingApi.updateStrengthTrainingData.mockResolvedValue({
          data: updatedData
        })
        
        await store.updateStrengthTrainingData(1, { weight: 110 })
        
        expect(store.strengthTrainingData[0].weight).toBe(110)
      })
    })

    describe('deleteStrengthTrainingData', () => {
      it('should delete training data by id', async () => {
        const store = useTrainingStore()
        store.strengthTrainingData = [
          { id: 1, exercise: 'Squat' },
          { id: 2, exercise: 'Bench' }
        ]
        strengthTrainingApi.deleteStrengthTrainingData.mockResolvedValue({})
        
        await store.deleteStrengthTrainingData(1)
        
        expect(store.strengthTrainingData).toHaveLength(1)
        expect(store.strengthTrainingData[0].id).toBe(2)
      })
    })

    describe('fetchCardioTrainingData', () => {
      it('should fetch and set cardio training data', async () => {
        const store = useTrainingStore()
        const mockData = [{ id: 1, type: 'Running' }]
        cardioTrainingApi.getCardioTrainingData.mockResolvedValue({
          data: mockData
        })
        
        await store.fetchCardioTrainingData()
        
        expect(store.cardioTrainingData).toEqual(mockData)
      })
    })

    describe('createCardioTrainingData', () => {
      it('should create and add new cardio data', async () => {
        const store = useTrainingStore()
        const newData = { type: 'Cycling', duration: 30 }
        const savedData = { id: 1, ...newData }
        cardioTrainingApi.createCardioTrainingData.mockResolvedValue({
          data: savedData
        })
        
        await store.createCardioTrainingData(newData)
        
        expect(store.cardioTrainingData[0]).toEqual(savedData)
      })
    })

    describe('saveTrainingData', () => {
      it('should save training data', async () => {
        const store = useTrainingStore()
        const data = { type: 'strength', weight: 100 }
        fitnessApi.saveFitnessData.mockResolvedValue({ data })
        
        const result = await store.saveTrainingData(data)
        
        expect(result.data).toEqual(data)
        expect(store.recoveryLoading).toBe(false)
      })
    })

    describe('clearTrainingData', () => {
      it('should clear all training data', () => {
        const store = useTrainingStore()
        store.strengthTrainingData = [{ id: 1 }]
        store.cardioTrainingData = [{ id: 2 }]
        store.recoveryData = [{ id: 3 }]
        store.trainingStats = { total: 10 }
        store.strengthError = 'error'
        
        store.clearTrainingData()
        
        expect(store.strengthTrainingData).toEqual([])
        expect(store.cardioTrainingData).toEqual([])
        expect(store.recoveryData).toEqual([])
        expect(store.trainingStats).toBeNull()
        expect(store.strengthError).toBeNull()
      })
    })
  })
})
