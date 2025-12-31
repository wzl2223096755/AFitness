import { get, post, put, del } from './request.js'

// 获取指定日期的营养记录列表
export function getNutritionRecordsByDate(date) {
  return get(`/api/v1/nutrition/records/${date}`)
}

// 添加营养记录
export function addNutritionRecord(data) {
  return post('/api/v1/nutrition/records', data)
}

// 更新营养记录
export function updateNutritionRecord(id, data) {
  return put(`/api/v1/nutrition/records/${id}`, data)
}

// 删除营养记录
export function deleteNutritionRecord(id) {
  return del(`/api/v1/nutrition/records/${id}`)
}

// 获取指定日期的营养统计
export function getNutritionStatsByDate(date) {
  return get(`/api/v1/nutrition/stats/${date}`)
}

// 获取指定日期范围的营养统计
export function getNutritionStatsByDateRange(startDate, endDate) {
  return get('/api/v1/nutrition/stats/range', {
    params: { startDate, endDate }
  })
}

// 获取营养建议
export function getNutritionAdvice(date) {
  return get('/api/v1/nutrition/advice', {
    date
  })
}

// 导出营养数据
export function exportNutritionData(startDate, endDate) {
  return get('/api/v1/nutrition/export', {
    startDate,
    endDate
  })
}

// 导出营养API对象
export const nutritionApi = {
  getNutritionRecordsByDate,
  addNutritionRecord,
  updateNutritionRecord,
  deleteNutritionRecord,
  getNutritionStatsByDate,
  getNutritionStatsByDateRange,
  getNutritionAdvice,
  exportNutritionData
}