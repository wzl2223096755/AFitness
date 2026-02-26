/**
 * 恢复模块 API
 * Recovery Module API - handles recovery status, training suggestions, and fatigue management
 */

import { get, post, put } from '../../../api/request.js'

// 恢复状态 API
export const recoveryApi = {
  // 获取当前恢复状态
  getCurrentRecoveryStatus: (userId) => get(`/api/v1/recovery/status/${userId}`),
  
  // 获取恢复状态历史
  getRecoveryHistory: (userId, startDate, endDate) => 
    get(`/api/v1/recovery/history/${userId}`, { params: { startDate, endDate } }),
  
  // 更新恢复状态
  updateRecoveryStatus: (userId, data) => put(`/api/v1/recovery/status/${userId}`, data),
  
  // 获取训练建议
  getTrainingSuggestions: (userId) => get(`/api/v1/recovery/suggestions/${userId}`),
  
  // 获取疲劳指数
  getFatigueIndex: (userId) => get(`/api/v1/recovery/fatigue/${userId}`),
  
  // 记录睡眠数据
  recordSleepData: (data) => post('/api/v1/recovery/sleep', data),
  
  // 获取睡眠历史
  getSleepHistory: (userId, days = 7) => 
    get(`/api/v1/recovery/sleep/${userId}`, { params: { days } }),
  
  // 获取恢复趋势分析
  getRecoveryTrends: (userId, period = 'week') => 
    get(`/api/v1/recovery/trends/${userId}`, { params: { period } })
}

// 导出所有 API 函数（兼容旧的导入方式）
export const {
  getCurrentRecoveryStatus,
  getRecoveryHistory,
  updateRecoveryStatus,
  getTrainingSuggestions,
  getFatigueIndex,
  recordSleepData,
  getSleepHistory,
  getRecoveryTrends
} = recoveryApi
