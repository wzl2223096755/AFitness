// 全局错误处理器
import { captureError, addBreadcrumb } from './errorMonitoring.js'

class ErrorHandler {
  static handle(error, context = '') {
    console.error(`[Error${context ? ` in ${context}` : ''}]:`, error)
    
    addBreadcrumb({
      category: 'error',
      message: `Error in ${context || 'unknown context'}`,
      level: 'error',
      data: { errorMessage: error.message }
    })
    
    if (error.response) {
      return this.handleApiError(error.response, context)
    }
    
    if (error.request) {
      return this.handleNetworkError(error, context)
    }
    
    return this.handleGenericError(error, context)
  }
  
  static handleApiError(response, context) {
    const { status, data } = response
    let message = '请求失败'
    
    switch (status) {
      case 400:
        message = data?.message || '请求参数错误'
        break
      case 401:
        message = '登录已过期，请重新登录'
        this.handleAuthError()
        break
      case 403:
        message = '没有权限执行此操作'
        break
      case 404:
        message = '请求的资源不存在'
        break
      case 409:
        message = data?.message || '数据冲突，请刷新后重试'
        break
      case 422:
        message = data?.message || '数据验证失败'
        break
      case 429:
        message = '请求过于频繁，请稍后再试'
        break
      case 500:
        message = '服务器内部错误，请稍后重试'
        break
      case 502:
        message = '服务暂时不可用，请稍后重试'
        break
      case 503:
        message = '系统维护中，请稍后重试'
        break
      default:
        message = data?.message || `请求失败 (${status})`
    }
    
    this.logError({
      type: 'API_ERROR',
      status,
      message: data?.message || message,
      url: response.config?.url,
      method: response.config?.method,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: status,
      data: data
    }
  }
  
  static handleNetworkError(error, context) {
    const message = navigator.onLine 
      ? '网络连接异常，请检查网络设置' 
      : '网络连接已断开，请检查网络连接'
    
    this.logError({
      type: 'NETWORK_ERROR',
      message: error.message,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: 'NETWORK_ERROR'
    }
  }
  
  static handleGenericError(error, context) {
    const message = error.message || '操作失败，请重试'
    
    this.logError({
      type: 'GENERIC_ERROR',
      message: error.message,
      stack: error.stack,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: 'GENERIC_ERROR'
    }
  }
  
  static handleAuthError() {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLoggedIn')
    
    if (!window.location.pathname.includes('/login')) {
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    }
  }
  
  static logError(errorInfo) {
    if (import.meta.env.DEV) {
      console.group('🚨 Error Details')
      console.error('Type:', errorInfo.type)
      console.error('Message:', errorInfo.message)
      console.error('Context:', errorInfo.context)
      console.groupEnd()
    }
    
    captureError(new Error(errorInfo.message), {
      type: errorInfo.type,
      context: errorInfo.context,
      url: errorInfo.url,
      method: errorInfo.method,
      status: errorInfo.status
    })
    
    this.storeErrorLog(errorInfo)
  }
  
  static storeErrorLog(errorInfo) {
    try {
      const logs = JSON.parse(localStorage.getItem('errorLogs') || '[]')
      logs.push(errorInfo)
      
      if (logs.length > 50) {
        logs.splice(0, logs.length - 50)
      }
      
      localStorage.setItem('errorLogs', JSON.stringify(logs))
    } catch (e) {
      console.warn('Failed to store error log:', e)
    }
  }
  
  static getErrorLogs() {
    try {
      return JSON.parse(localStorage.getItem('errorLogs') || '[]')
    } catch (e) {
      return []
    }
  }
  
  static clearErrorLogs() {
    localStorage.removeItem('errorLogs')
  }
}

// 响应数据标准化处理器
class ResponseHandler {
  static normalize(response) {
    if (typeof response === 'boolean') {
      return {
        success: response,
        data: null,
        message: response ? '操作成功' : '操作失败'
      }
    }
    
    if (response && typeof response === 'object') {
      if (Object.prototype.hasOwnProperty.call(response, 'success')) {
        return response
      }
      
      return {
        success: true,
        data: response,
        message: '操作成功'
      }
    }
    
    return {
      success: true,
      data: response,
      message: '操作成功'
    }
  }
  
  static isSuccess(response) {
    const normalized = this.normalize(response)
    return normalized.success === true
  }
  
  static getData(response) {
    const normalized = this.normalize(response)
    return normalized.data
  }
  
  static getMessage(response) {
    const normalized = this.normalize(response)
    return normalized.message
  }
}

// API调用包装器
export const apiWrapper = {
  async request(requestFn, context = '') {
    try {
      const response = await requestFn()
      return ResponseHandler.normalize(response)
    } catch (error) {
      return ErrorHandler.handle(error, context)
    }
  },
  
  async get(url, params = {}, context = '') {
    return this.request(() => import('../api/request.js').then(m => m.get(url, params)), context)
  },
  
  async post(url, data = {}, context = '') {
    return this.request(() => import('../api/request.js').then(m => m.post(url, data)), context)
  },
  
  async put(url, data = {}, context = '') {
    return this.request(() => import('../api/request.js').then(m => m.put(url, data)), context)
  },
  
  async delete(url, params = {}, context = '') {
    return this.request(() => import('../api/request.js').then(m => m.del(url, params)), context)
  }
}

export { ErrorHandler, ResponseHandler }
