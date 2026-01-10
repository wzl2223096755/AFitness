/**
 * Settings模块API导出
 * 重新导出用户相关API
 */

import { get, post, put, del, upload } from '@/api/request.js'
import { cachedUserApi } from '@/api/cachedApi.js'

export const userApi = {
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
  
  // 导出用户数据
  exportUserData: (params) => get('/api/v1/user/export', params),
  
  // 重置用户数据
  resetUserData: (password) => post('/api/v1/user/reset-data', { password }),
  
  // 删除用户账户
  deleteAccount: (password) => post('/api/v1/user/delete-account', { password }),
  
  // 使用户缓存失效
  invalidateCache: () => cachedUserApi.invalidateCache()
}
