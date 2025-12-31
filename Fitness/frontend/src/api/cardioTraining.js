import request from './request'

// 有氧训练API
export const cardioTrainingApi = {
  // 获取有氧训练数据
  getCardioTrainingData(params = {}) {
    return request.get('/api/v1/cardio-training', { params })
  },

  // 根据ID获取有氧训练数据
  getCardioTrainingDataById(id) {
    return request.get(`/api/v1/cardio-training/${id}`)
  },

  // 创建有氧训练数据
  createCardioTrainingData(data) {
    return request.post('/api/v1/cardio-training', data)
  },

  // 更新有氧训练数据
  updateCardioTrainingData(id, data) {
    return request.put(`/api/v1/cardio-training/${id}`, data)
  },

  // 删除有氧训练数据
  deleteCardioTrainingData(id) {
    return request.delete(`/api/v1/cardio-training/${id}`)
  },

  // 按时间范围查询
  getCardioTrainingDataByDateRange(startDate, endDate) {
    return request.get('/api/v1/cardio-training/date-range', {
      params: { startDate, endDate }
    })
  },

  // 按运动类型查询
  getCardioTrainingDataByExerciseType(exerciseType) {
    return request.get(`/api/v1/cardio-training/exercise-type/${exerciseType}`)
  }
}