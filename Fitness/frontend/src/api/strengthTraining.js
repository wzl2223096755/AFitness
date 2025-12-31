import { get, post, put, del } from './request.js'

export const strengthTrainingApi = {
  // 获取力量训练数据列表（分页）
  getStrengthTrainingList: (params) => get('/api/v1/strength-training', params),
  
  // 获取力量训练数据详情
  getStrengthTrainingById: (id) => get(`/api/v1/strength-training/${id}`),
  
  // 添加力量训练数据
  addStrengthTraining: (data) => post('/api/v1/strength-training', data),
  
  // 更新力量训练数据
  updateStrengthTraining: (id, data) => put(`/api/v1/strength-training/${id}`, data),
  
  // 删除力量训练数据
  deleteStrengthTraining: (id) => del(`/api/v1/strength-training/${id}`),
  
  // 按时间范围查询力量训练数据
  getStrengthTrainingByDateRange: (startDate, endDate) => 
    get('/api/v1/strength-training/date-range', { startDate, endDate }),
  
  // 按动作名称查询力量训练数据
  getStrengthTrainingByExercise: (exerciseName) => 
    get(`/api/v1/strength-training/exercise/${exerciseName}`),
  
  // 获取最大重量统计
  getMaxWeightStats: () => get('/api/v1/strength-training/stats/max-weight'),
  
  // 获取训练容量统计
  getVolumeStats: (params) => get('/api/v1/strength-training/stats/volume', params),
  
  // 获取训练频率统计
  getFrequencyStats: (params) => get('/api/v1/strength-training/stats/frequency', params),
  
  // 获取进步分析
  getProgressAnalysis: (exerciseName, params) => 
    get(`/api/v1/strength-training/progress/${exerciseName}`, params),
  
  // 获取推荐训练计划
  getRecommendedPlan: (params) => get('/api/v1/strength-training/recommendations', params),
  
  // 批量导入训练数据
  importTrainingData: (data) => post('/api/v1/strength-training/import', data),
  
  // 导出训练数据
  exportTrainingData: (params) => get('/api/v1/strength-training/export', params),
  
  // 获取训练模板
  getTrainingTemplates: () => get('/api/v1/strength-training/templates'),
  
  // 保存训练模板
  saveTrainingTemplate: (data) => post('/api/v1/strength-training/templates', data),
  
  // 删除训练模板
  deleteTrainingTemplate: (id) => del(`/api/v1/strength-training/templates/${id}`)
}