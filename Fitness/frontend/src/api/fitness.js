import { get, post, put, del, patch } from './request'
import { cachedDashboardApi } from './cachedApi.js'

export const fitnessApi = {
  // 健身记录与1RM计算 (统一由 LoadRecoveryController 处理)
  calculate1RM: (params) => get('/api/v1/load-recovery/one-rep-max', { params }),
  get1RMModels: () => get('/api/v1/load-recovery/one-rep-max/models'),
  calculate1RMWithRecord: (data) => post('/api/v1/load-recovery/one-rep-max/record', data),
  
  // 基础健身数据 (LoadRecoveryController)
  getFitnessData: (params) => get('/api/v1/load-recovery/my-data', { params }),
  saveFitnessData: (data) => post('/api/v1/load-recovery/training-data', data),
  deleteFitnessData: (id) => del(`/api/v1/load-recovery/data/${id}`),
  
  // 训练计划管理 (TrainingPlanController)
  getUserFitnessPlans: () => get('/api/v1/training-plans'),
  getFitnessPlanById: (id) => get(`/api/v1/training-plans/${id}`),
  createFitnessPlan: (data) => post('/api/v1/training-plans', data),
  updateFitnessPlan: (id, data) => put(`/api/v1/training-plans/${id}`, data),
  deleteFitnessPlan: (id) => del(`/api/v1/training-plans/${id}`),
  updateExercise: (id) => patch(`/api/v1/training-plans/exercises/${id}/toggle`),
  getUserFitnessPlansByStatus: (status) => get(`/api/v1/training-plans/status/${status}`),
  getUserFitnessPlansByDateRange: (startDate, endDate) => get('/api/v1/training-plans/range', { params: { startDate, endDate } }),
  saveWeeklyPlan: (data) => post('/api/v1/training-plans/weekly', data),
  
  // 仪表盘数据 (DashboardController / UserProfileController) - 使用缓存API
  getDashboardStats: (options) => cachedDashboardApi.getDashboardStats(options),
  getTrainingHistory: (params, options) => cachedDashboardApi.getTrainingHistory(params, options),
  getMetricsOverview: (timeRange, options) => cachedDashboardApi.getMetricsOverview(timeRange, options),
  getUserStatsOverview: (options) => cachedDashboardApi.getUserStatsOverview(options),
  getAnalyticsData: (timeRange, options) => cachedDashboardApi.getAnalyticsData(timeRange, options),
  getRecentTrainingRecords: (options) => cachedDashboardApi.getRecentTrainingRecords(options),
  
  // 使仪表盘缓存失效
  invalidateDashboardCache: () => cachedDashboardApi.invalidateCache(),

  // 负荷分析与建议 (LoadRecoveryController)
  getLoadTrend: (params) => get('/api/v1/load-recovery/load-trend', { params }),
  assessRecovery: (data) => post('/api/v1/load-recovery/recovery-assessment', data),
  getTrainingSuggestions: () => get('/api/v1/load-recovery/training-suggestions'),
}
