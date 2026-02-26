/**
 * 用户端错误处理器
 * 简化版本，用于 GitHub Pages 演示
 */

// 错误消息映射
export const errorMessages = {
  400: '请求参数错误',
  401: '未授权，请重新登录',
  403: '权限不足',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  network: '网络连接失败',
  timeout: '请求超时',
  unknown: '未知错误'
}

// 获取错误消息
export function getErrorMessage(error) {
  if (error.response) {
    const status = error.response.status
    return errorMessages[status] || error.response.data?.message || errorMessages.unknown
  }
  if (error.code === 'NETWORK_ERROR') return errorMessages.network
  if (error.code === 'TIMEOUT') return errorMessages.timeout
  return error.message || errorMessages.unknown
}

// 错误类型判断
export function isNetworkError(error) {
  return error.code === 'NETWORK_ERROR' || !error.response
}

export function isAuthError(error) {
  return error.response?.status === 401
}

export function isPermissionError(error) {
  return error.response?.status === 403
}

export function isRetryableError(error) {
  return isNetworkError(error) || error.response?.status >= 500
}

// 简单的错误处理器类
export class ErrorHandler {
  static handle(error, defaultMessage = '操作失败') {
    console.error('Error:', error)
    return getErrorMessage(error) || defaultMessage
  }
}

// API 响应处理器
export class ResponseHandler {
  static handle(response) {
    return response.data
  }
}

// API 包装器
export function apiWrapper(apiCall) {
  return async (...args) => {
    try {
      const response = await apiCall(...args)
      return ResponseHandler.handle(response)
    } catch (error) {
      throw ErrorHandler.handle(error)
    }
  }
}
