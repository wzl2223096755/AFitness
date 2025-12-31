/**
 * 系统监控API
 */
import request from './request'

/**
 * 获取系统指标
 */
export function getSystemMetrics() {
  return request.get('/api/v1/alerts/metrics')
}

/**
 * 触发测试告警
 */
export function triggerTestAlert() {
  return request.post('/api/v1/alerts/test')
}

/**
 * 手动触发告警
 */
export function triggerAlert(alertType, message, level = 'WARNING') {
  return request.post('/api/v1/alerts/trigger', null, {
    params: { alertType, message, level }
  })
}

/**
 * 解除告警
 */
export function resolveAlert(alertType) {
  return request.post(`/api/v1/alerts/resolve/${alertType}`)
}

/**
 * 获取健康检查状态
 */
export function getHealthStatus() {
  return request.get('/actuator/health')
}

/**
 * 获取应用信息
 */
export function getAppInfo() {
  return request.get('/actuator/info')
}

export default {
  getSystemMetrics,
  triggerTestAlert,
  triggerAlert,
  resolveAlert,
  getHealthStatus,
  getAppInfo
}
