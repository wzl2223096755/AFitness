/**
 * 训练模块 Store
 * Training Module Store - manages training data state
 */

import { defineStore } from 'pinia'
import { fitnessApi } from '../../../api/fitness'

export const useTrainingStore = defineStore('training', {
  state: () => ({
    // 力量训练数据
    strengthTrainingData: [],
    strengthLoading: false,
    strengthError: null,
    
    // 有氧训练数据
    cardioTrainingData: [],
    cardioLoading: false,
    cardioError: null,
    
    // 恢复数据
    recoveryData: [],
    recoveryLoading: false,
    recoveryError: null,
    
    // 统计数据
    trainingStats: null,
    statsLoading: false,
    statsError: null,
    
    // 分页信息
    pagination: {
      currentPage: 0,
      pageSize: 20,
      totalElements: 0,
      totalPages: 0
    }
  }),

  getters: {
    // 获取最新的力量训练记录
    latestStrengthTraining: (state) => {
      return state.strengthTrainingData.length > 0 
        ? state.strengthTrainingData[0] 
        : null
    },
    
    // 获取最新的有氧训练记录
    latestCardioTraining: (state) => {
      return state.cardioTrainingData.length > 0 
        ? state.cardioTrainingData[0] 
        : null
    },
    
    // 获取最新的恢复数据
    latestRecoveryData: (state) => {
      return state.recoveryData.length > 0 
        ? state.recoveryData[0] 
        : null
    },
    
    // 检查是否有任何加载状态
    isLoading: (state) => {
      return state.strengthLoading || state.cardioLoading || 
             state.recoveryLoading || state.statsLoading
    },
    
    // 获取所有错误信息
    allErrors: (state) => {
      const errors = []
      if (state.strengthError) errors.push(state.strengthError)
      if (state.cardioError) errors.push(state.cardioError)
      if (state.recoveryError) errors.push(state.recoveryError)
      if (state.statsError) errors.push(state.statsError)
      return errors
    }
  },

  actions: {
    // 重置错误状态
    resetErrors() {
      this.strengthError = null
      this.cardioError = null
      this.recoveryError = null
      this.statsError = null
    },

    // 力量训练相关操作
    async fetchStrengthTrainingData(params = {}) {
      this.strengthLoading = true
      this.strengthError = null
      
      try {
        const response = await fitnessApi.getTrainingRecords(params)
        this.strengthTrainingData = response.data?.records?.filter(r => r.type === 'strength') || []
        this.pagination = {
          currentPage: 0,
          pageSize: 20,
          totalElements: this.strengthTrainingData.length,
          totalPages: 1
        }
        return response
      } catch (error) {
        this.strengthError = error.message || '获取力量训练数据失败'
        throw error
      } finally {
        this.strengthLoading = false
      }
    },

    async createStrengthTrainingData(data) {
      this.strengthLoading = true
      this.strengthError = null
      
      try {
        const response = await fitnessApi.createTrainingRecord({ ...data, type: 'strength' })
        this.strengthTrainingData.unshift(response.data)
        return response
      } catch (error) {
        this.strengthError = error.message || '创建力量训练数据失败'
        throw error
      } finally {
        this.strengthLoading = false
      }
    },

    async updateStrengthTrainingData(id, data) {
      this.strengthLoading = true
      this.strengthError = null
      
      try {
        const response = await fitnessApi.updateTrainingRecord(id, data)
        const index = this.strengthTrainingData.findIndex(item => item.id === id)
        if (index !== -1) {
          this.strengthTrainingData[index] = response.data
        }
        return response
      } catch (error) {
        this.strengthError = error.message || '更新力量训练数据失败'
        throw error
      } finally {
        this.strengthLoading = false
      }
    },

    async deleteStrengthTrainingData(id) {
      this.strengthLoading = true
      this.strengthError = null
      
      try {
        await fitnessApi.deleteTrainingRecord(id)
        this.strengthTrainingData = this.strengthTrainingData.filter(item => item.id !== id)
      } catch (error) {
        this.strengthError = error.message || '删除力量训练数据失败'
        throw error
      } finally {
        this.strengthLoading = false
      }
    },

    // 有氧训练相关操作
    async fetchCardioTrainingData(params = {}) {
      this.cardioLoading = true
      this.cardioError = null
      
      try {
        const response = await fitnessApi.getTrainingRecords(params)
        this.cardioTrainingData = response.data?.records?.filter(r => r.type === 'cardio') || []
        return response
      } catch (error) {
        this.cardioError = error.message || '获取有氧训练数据失败'
        throw error
      } finally {
        this.cardioLoading = false
      }
    },

    async createCardioTrainingData(data) {
      this.cardioLoading = true
      this.cardioError = null
      
      try {
        const response = await fitnessApi.createTrainingRecord({ ...data, type: 'cardio' })
        this.cardioTrainingData.unshift(response.data)
        return response
      } catch (error) {
        this.cardioError = error.message || '创建有氧训练数据失败'
        throw error
      } finally {
        this.cardioLoading = false
      }
    },

    // 恢复与负荷相关操作
    async saveTrainingData(data) {
      this.recoveryLoading = true
      try {
        const response = await fitnessApi.saveFitnessData(data)
        return response
      } catch (error) {
        this.recoveryError = error.message
        throw error
      } finally {
        this.recoveryLoading = false
      }
    },

    async assessRecovery(data) {
      this.recoveryLoading = true
      try {
        const response = await fitnessApi.assessRecovery(data)
        return response
      } catch (error) {
        this.recoveryError = error.message
        throw error
      } finally {
        this.recoveryLoading = false
      }
    },

    async fetchTrainingSuggestions() {
      this.recoveryLoading = true
      try {
        const response = await fitnessApi.getTrainingSuggestions()
        return response
      } catch (error) {
        this.recoveryError = error.message
        throw error
      } finally {
        this.recoveryLoading = false
      }
    },

    async fetchLoadTrend(startDate, endDate) {
      this.recoveryLoading = true
      try {
        const response = await fitnessApi.getLoadTrend({ startDate, endDate })
        return response
      } catch (error) {
        this.recoveryError = error.message
        throw error
      } finally {
        this.recoveryLoading = false
      }
    },

    // 统计数据相关操作
    async fetchTrainingStats(params = {}) {
      this.statsLoading = true
      this.statsError = null
      
      try {
        const response = await fitnessApi.getDashboardStats(params)
        this.trainingStats = response.data
        return response
      } catch (error) {
        this.statsError = error.message || '获取统计数据失败'
        throw error
      } finally {
        this.statsLoading = false
      }
    },

    // 批量操作
    async fetchAllTrainingData(params = {}) {
      this.resetErrors()
      
      try {
        await Promise.all([
          this.fetchStrengthTrainingData(params),
          this.fetchCardioTrainingData(params),
          this.fetchTrainingSuggestions()
        ])
      } catch (error) {
        console.error('批量获取训练数据失败:', error)
        throw error
      }
    },

    // 清空数据
    clearTrainingData() {
      this.strengthTrainingData = []
      this.cardioTrainingData = []
      this.recoveryData = []
      this.trainingStats = null
      this.resetErrors()
    }
  }
})
