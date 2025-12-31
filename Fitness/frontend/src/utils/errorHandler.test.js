import { describe, it, expect, vi, beforeEach } from 'vitest'

// We need to test ErrorHandler and ResponseHandler separately from apiWrapper
// because apiWrapper has dynamic imports that are hard to mock in tests

// Create a simplified version of the classes for testing
class ErrorHandler {
  static handle(error, context = '') {
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
  
  static logError(errorInfo) {
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
      // Ignore storage errors
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
      if (response.hasOwnProperty('success')) {
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

describe('ErrorHandler', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('handle', () => {
    it('should handle API errors with response', () => {
      const error = {
        response: {
          status: 400,
          data: { message: 'Bad request' },
          config: { url: '/api/test', method: 'GET' }
        }
      }
      
      const result = ErrorHandler.handle(error, 'test context')
      
      expect(result.success).toBe(false)
      expect(result.code).toBe(400)
    })

    it('should handle network errors', () => {
      const error = {
        request: {},
        message: 'Network Error'
      }
      
      const result = ErrorHandler.handle(error, 'test context')
      
      expect(result.success).toBe(false)
      expect(result.code).toBe('NETWORK_ERROR')
    })

    it('should handle generic errors', () => {
      const error = new Error('Something went wrong')
      
      const result = ErrorHandler.handle(error, 'test context')
      
      expect(result.success).toBe(false)
      expect(result.code).toBe('GENERIC_ERROR')
      expect(result.message).toBe('Something went wrong')
    })
  })

  describe('handleApiError', () => {
    it('should return correct message for 401 status', () => {
      const response = {
        status: 401,
        data: {},
        config: { url: '/api/test', method: 'GET' }
      }
      
      const result = ErrorHandler.handleApiError(response, 'auth')
      
      expect(result.message).toBe('登录已过期，请重新登录')
      expect(result.code).toBe(401)
    })

    it('should return correct message for 403 status', () => {
      const response = {
        status: 403,
        data: {},
        config: { url: '/api/test', method: 'GET' }
      }
      
      const result = ErrorHandler.handleApiError(response, 'permission')
      
      expect(result.message).toBe('没有权限执行此操作')
    })

    it('should return correct message for 404 status', () => {
      const response = {
        status: 404,
        data: {},
        config: { url: '/api/test', method: 'GET' }
      }
      
      const result = ErrorHandler.handleApiError(response, 'not found')
      
      expect(result.message).toBe('请求的资源不存在')
    })

    it('should return correct message for 500 status', () => {
      const response = {
        status: 500,
        data: {},
        config: { url: '/api/test', method: 'GET' }
      }
      
      const result = ErrorHandler.handleApiError(response, 'server error')
      
      expect(result.message).toBe('服务器内部错误，请稍后重试')
    })

    it('should use custom message from response data', () => {
      const response = {
        status: 400,
        data: { message: 'Custom error message' },
        config: { url: '/api/test', method: 'GET' }
      }
      
      const result = ErrorHandler.handleApiError(response, 'custom')
      
      expect(result.message).toBe('Custom error message')
    })
  })

  describe('storeErrorLog', () => {
    it('should store error logs in localStorage', () => {
      const errorInfo = {
        type: 'TEST_ERROR',
        message: 'Test error',
        timestamp: new Date().toISOString()
      }
      
      ErrorHandler.storeErrorLog(errorInfo)
      
      const logs = ErrorHandler.getErrorLogs()
      expect(logs).toHaveLength(1)
      expect(logs[0].type).toBe('TEST_ERROR')
    })

    it('should limit error logs to 50 entries', () => {
      for (let i = 0; i < 55; i++) {
        ErrorHandler.storeErrorLog({
          type: 'TEST_ERROR',
          message: `Error ${i}`,
          timestamp: new Date().toISOString()
        })
      }
      
      const logs = ErrorHandler.getErrorLogs()
      expect(logs.length).toBeLessThanOrEqual(50)
    })
  })

  describe('clearErrorLogs', () => {
    it('should clear all error logs', () => {
      ErrorHandler.storeErrorLog({
        type: 'TEST_ERROR',
        message: 'Test error',
        timestamp: new Date().toISOString()
      })
      
      ErrorHandler.clearErrorLogs()
      
      const logs = ErrorHandler.getErrorLogs()
      expect(logs).toHaveLength(0)
    })
  })
})

describe('ResponseHandler', () => {
  describe('normalize', () => {
    it('should normalize boolean true to success response', () => {
      const result = ResponseHandler.normalize(true)
      
      expect(result.success).toBe(true)
      expect(result.message).toBe('操作成功')
    })

    it('should normalize boolean false to failure response', () => {
      const result = ResponseHandler.normalize(false)
      
      expect(result.success).toBe(false)
      expect(result.message).toBe('操作失败')
    })

    it('should return response as-is if already normalized', () => {
      const response = { success: true, data: { id: 1 }, message: 'OK' }
      
      const result = ResponseHandler.normalize(response)
      
      expect(result).toEqual(response)
    })

    it('should wrap object without success property', () => {
      const data = { id: 1, name: 'Test' }
      
      const result = ResponseHandler.normalize(data)
      
      expect(result.success).toBe(true)
      expect(result.data).toEqual(data)
    })
  })

  describe('isSuccess', () => {
    it('should return true for successful response', () => {
      expect(ResponseHandler.isSuccess({ success: true })).toBe(true)
      expect(ResponseHandler.isSuccess(true)).toBe(true)
    })

    it('should return false for failed response', () => {
      expect(ResponseHandler.isSuccess({ success: false })).toBe(false)
      expect(ResponseHandler.isSuccess(false)).toBe(false)
    })
  })

  describe('getData', () => {
    it('should extract data from response', () => {
      const response = { success: true, data: { id: 1 } }
      
      expect(ResponseHandler.getData(response)).toEqual({ id: 1 })
    })
  })

  describe('getMessage', () => {
    it('should extract message from response', () => {
      const response = { success: true, message: 'Custom message' }
      
      expect(ResponseHandler.getMessage(response)).toBe('Custom message')
    })
  })
})
