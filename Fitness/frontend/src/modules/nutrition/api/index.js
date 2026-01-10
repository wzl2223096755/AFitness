/**
 * 营养模块 API
 * Nutrition Module API - handles nutrition records, stats, and advice
 */

import { get, post, put, del } from '@shared/api/request.js'

// 营养记录 API
export const nutritionApi = {
  // 获取指定日期的营养记录列表
  getNutritionRecordsByDate: (date) => get(`/api/v1/nutrition/records/${date}`),
  
  // 添加营养记录
  addNutritionRecord: (data) => post('/api/v1/nutrition/records', data),
  
  // 更新营养记录
  updateNutritionRecord: (id, data) => put(`/api/v1/nutrition/records/${id}`, data),
  
  // 删除营养记录
  deleteNutritionRecord: (id) => del(`/api/v1/nutrition/records/${id}`),
  
  // 获取指定日期的营养统计
  getNutritionStatsByDate: (date) => get(`/api/v1/nutrition/stats/${date}`),
  
  // 获取指定日期范围的营养统计
  getNutritionStatsByDateRange: (startDate, endDate) => 
    get('/api/v1/nutrition/stats/range', { params: { startDate, endDate } }),
  
  // 获取营养建议
  getNutritionAdvice: (date) => get('/api/v1/nutrition/advice', { date }),
  
  // 导出营养数据
  exportNutritionData: (startDate, endDate) => 
    get('/api/v1/nutrition/export', { startDate, endDate }),
  
  // 获取营养目标
  getNutritionGoals: (userId) => get(`/api/v1/nutrition/goals/${userId}`),
  
  // 设置营养目标
  setNutritionGoals: (data) => post('/api/v1/nutrition/goals', data),
  
  // 更新营养目标
  updateNutritionGoals: (id, data) => put(`/api/v1/nutrition/goals/${id}`, data),
  
  // 获取每日营养摘要
  getDailyNutritionSummary: (userId, date) => 
    get(`/api/v1/nutrition/daily/${userId}/${date}`)
}

// 导出所有 API 函数（兼容旧的导入方式）
export const {
  getNutritionRecordsByDate,
  addNutritionRecord,
  updateNutritionRecord,
  deleteNutritionRecord,
  getNutritionStatsByDate,
  getNutritionStatsByDateRange,
  getNutritionAdvice,
  exportNutritionData,
  getNutritionGoals,
  setNutritionGoals,
  updateNutritionGoals,
  getDailyNutritionSummary
} = nutritionApi
