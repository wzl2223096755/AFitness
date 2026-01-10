import { defineStore } from 'pinia'
import { fitnessApi } from '../api/fitness'
import { userApi } from '../api/user'

export const useFitnessStore = defineStore('fitness', {
  state: () => ({
    users: [],
    devices: [],
    fitnessData: [],
    realTimeData: [],
    onlineDevices: 0,
    loading: false,
    error: null
  }),

  getters: {
    getUserById: (state) => (id) => {
      return state.users.find(user => user.id === id)
    },
    getDeviceById: (state) => (id) => {
      return state.devices.find(device => device.id === id)
    }
  },

  actions: {
    async initializeData() {
      this.loading = true
      this.error = null
      
      try {
        await Promise.all([
          this.fetchDevices(),
          this.fetchMyFitnessData()
        ])
        this.updateOnlineDevices()
      } catch (error) {
        console.error('初始化数据失败:', error)
        // 不设置错误状态，避免影响页面渲染
        // this.error = '初始化数据失败，请检查网络连接'
      } finally {
        this.loading = false
      }
    },

    async fetchUsers() {
      // 此方法需要管理员权限，普通用户不应调用
      // 如果需要获取用户列表，请使用管理员账号
      try {
        const response = await userApi.getUsers()
        this.users = response.data?.data || []
      } catch (error) {
        console.error('获取用户列表失败（可能需要管理员权限）:', error)
        this.users = []
      }
    },

    async fetchDevices() {
      // 如果后端没有设备管理接口，可以先返回空数组或模拟数据
      try {
        // 暂时注释掉，因为 api/fitness.js 中没有 getDevices
        // const response = await fitnessApi.getDevices()
        // this.devices = response.data || []
        this.devices = [] 
      } catch (error) {
        console.error('获取设备列表失败:', error)
        this.devices = []
      }
    },

    async fetchMyFitnessData() {
      try {
        const response = await fitnessApi.getFitnessData()
        this.fitnessData = response.data?.data || response.data || []
      } catch (error) {
        console.error('获取用户健身数据失败:', error)
        this.fitnessData = []
      }
    },

    async getUserStats(userId) {
      try {
        // 使用 getDashboardStats，如果需要特定用户的可以后续扩展
        const response = await fitnessApi.getDashboardStats()
        return response.data?.data || response.data || null
      } catch (error) {
        console.error('获取用户统计数据失败:', error)
        return null
      }
    },

    async addFitnessData(data) {
      try {
        const response = await fitnessApi.saveFitnessData(data)
        // 添加新数据到本地状态
        const newData = response.data?.data || response.data || data
        this.fitnessData.unshift(newData)
        return newData
      } catch (error) {
        console.error('添加健身数据失败:', error)
        throw error
      }
    },

    async updateFitnessData(id, data) {
      try {
        const response = await fitnessApi.updateFitnessData(id, data)
        // 更新本地状态中的数据
        const index = this.fitnessData.findIndex(item => item.id === id)
        if (index !== -1) {
          this.fitnessData[index] = response.data || data
        }
        return response.data
      } catch (error) {
        console.error('更新健身数据失败:', error)
        throw error
      }
    },

    async deleteFitnessData(id) {
      try {
        await fitnessApi.deleteFitnessData(id)
        // 从本地状态中删除数据
        this.fitnessData = this.fitnessData.filter(item => item.id !== id)
      } catch (error) {
        console.error('删除健身数据失败:', error)
        throw error
      }
    },

    async generateFitnessData() {
      try {
        await fitnessApi.generateFitnessData()
        await this.fetchMyFitnessData()
      } catch (error) {
        console.error('生成健身数据失败:', error)
        throw error
      }
    },

    updateOnlineDevices() {
      this.onlineDevices = this.devices.filter(device => device.status === 'ONLINE').length
    },

    addRealTimeData(data) {
      this.fitnessData.unshift(data)
      if (this.fitnessData.length > 100) {
        this.fitnessData.pop()
      }
    },

    clearError() {
      this.error = null
    }
  }
})
