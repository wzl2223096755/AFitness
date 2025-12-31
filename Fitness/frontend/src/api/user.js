import { get, post, put, del, upload } from './request.js'
import { cachedUserApi } from './cachedApi.js'

export const userApi = {
  // 获取所有用户列表
  getUsers: (params) => get('/api/v1/users', params),
  
  // 获取当前用户信息 - 使用缓存API
  getCurrentUser: (options) => cachedUserApi.getCurrentUser(options),
  
  // 更新用户资料 - 使用缓存API（会自动使缓存失效）
  updateProfile: (data) => cachedUserApi.updateProfile(data),
  
  // 修改密码
  changePassword: (data) => post('/api/v1/user/change-password', data),
  
  // 上传头像
  uploadAvatar: (file) => upload('/api/v1/user/avatar', file),
  
  // 获取用户设置 - 使用缓存API
  getUserSettings: (options) => cachedUserApi.getUserSettings(options),
  
  // 更新用户设置 - 使用缓存API（会自动使缓存失效）
  updateUserSettings: (data) => cachedUserApi.updateUserSettings(data),
  
  // 检查用户名是否存在
  checkUsername: (username) => get('/api/v1/auth/check-username', { username }),
  
  // 检查邮箱是否存在
  checkEmail: (email) => get('/api/v1/auth/check-email', { email }),
  
  // 获取用户统计数据 - 使用缓存API
  getUserStats: (options) => cachedUserApi.getUserStats(options),
  
  // 获取用户训练历史
  getTrainingHistory: (params) => get('/api/v1/user/training-history', params),
  
  // 获取用户身体数据记录 - 使用缓存API
  getBodyRecords: (params, options) => cachedUserApi.getBodyRecords(params, options),
  
  // 添加身体数据记录
  addBodyRecord: (data) => post('/api/v1/user/body-records', data),
  
  // 更新身体数据记录
  updateBodyRecord: (id, data) => put(`/api/v1/user/body-records/${id}`, data),
  
  // 删除身体数据记录
  deleteBodyRecord: (id) => del(`/api/v1/user/body-records/${id}`),
  
  // 获取用户成就 - 使用缓存API
  getUserAchievements: (options) => cachedUserApi.getUserAchievements(options),
  
  // 获取用户排名
  getUserRanking: () => get('/api/v1/user/ranking'),
  
  // 导出用户数据
  exportUserData: (params) => get('/api/v1/user/export', params),
  
  // 重置用户数据
  resetUserData: (password) => post('/api/v1/user/reset-data', { password }),
  
  // 删除用户账户
  deleteAccount: (password) => post('/api/v1/user/delete-account', { password }),
  
  // 使用户缓存失效
  invalidateCache: () => cachedUserApi.invalidateCache()
}