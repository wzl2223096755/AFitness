/**
 * 前端错误监控服务
 * 集成Sentry进行错误追踪和上报
 */
import * as Sentry from '@sentry/vue'

// 错误监控配置
const ERROR_MONITORING_CONFIG = {
  dsn: import.meta.env.VITE_SENTRY_DSN || '',
  environment: import.meta.env.MODE || 'development',
  enabled: import.meta.env.PROD || import.meta.env.VITE_ENABLE_SENTRY === 'true',
  tracesSampleRate: import.meta.env.PROD ? 0.2 : 1.0,
  debug: import.meta.env.DEV,
  maxBreadcrumbs: 50,
  attachStacktrace: true
}

export function initErrorMonitoring(app, router) {
  if (!ERROR_MONITORING_CONFIG.dsn && !ERROR_MONITORING_CONFIG.enabled) {
    console.log('[ErrorMonitoring] Sentry DSN未配置，使用本地错误处理')
    setupLocalErrorHandling(app)
    return
  }

  if (ERROR_MONITORING_CONFIG.dsn) {
    Sentry.init({
      app,
      dsn: ERROR_MONITORING_CONFIG.dsn,
      environment: ERROR_MONITORING_CONFIG.environment,
      integrations: [
        Sentry.browserTracingIntegration({ router }),
        Sentry.replayIntegration({
          maskAllText: false,
          blockAllMedia: false
        })
      ],
      tracesSampleRate: ERROR_MONITORING_CONFIG.tracesSampleRate,
      replaysSessionSampleRate: 0.1,
      replaysOnErrorSampleRate: 1.0,
      debug: ERROR_MONITORING_CONFIG.debug,
      maxBreadcrumbs: ERROR_MONITORING_CONFIG.maxBreadcrumbs,
      attachStacktrace: ERROR_MONITORING_CONFIG.attachStacktrace,
      beforeSend(event, hint) {
        return filterSensitiveData(event)
      }
    })
    console.log('[ErrorMonitoring] Sentry已初始化')
  }

  setupVueErrorHandler(app)
  setupGlobalErrorHandlers()
}

function setupVueErrorHandler(app) {
  app.config.errorHandler = (err, instance, info) => {
    const componentName = instance?.$options?.name || instance?.$.type?.name || 'Unknown'
    
    if (ERROR_MONITORING_CONFIG.dsn) {
      Sentry.withScope((scope) => {
        scope.setTag('error_type', 'vue_error')
        scope.setTag('component', componentName)
        scope.setExtra('lifecycle_hook', info)
        Sentry.captureException(err)
      })
    }

    logErrorLocally({
      type: 'VUE_ERROR',
      message: err.message,
      stack: err.stack,
      componentName,
      lifecycleHook: info,
      timestamp: new Date().toISOString()
    })

    if (import.meta.env.DEV) {
      console.group('🚨 Vue Error')
      console.error('Error:', err)
      console.error('Component:', componentName)
      console.groupEnd()
    }
  }
}

function setupGlobalErrorHandlers() {
  window.addEventListener('error', (event) => {
    const { message, filename, lineno, colno, error } = event

    if (ERROR_MONITORING_CONFIG.dsn && error) {
      Sentry.withScope((scope) => {
        scope.setTag('error_type', 'uncaught_error')
        Sentry.captureException(error)
      })
    }

    logErrorLocally({
      type: 'UNCAUGHT_ERROR',
      message,
      filename,
      lineno,
      colno,
      timestamp: new Date().toISOString()
    })
  })

  window.addEventListener('unhandledrejection', (event) => {
    const error = event.reason

    if (ERROR_MONITORING_CONFIG.dsn) {
      Sentry.withScope((scope) => {
        scope.setTag('error_type', 'unhandled_rejection')
        if (error instanceof Error) {
          Sentry.captureException(error)
        } else {
          Sentry.captureMessage(`Unhandled Promise Rejection: ${String(error)}`)
        }
      })
    }

    logErrorLocally({
      type: 'UNHANDLED_REJECTION',
      message: error?.message || String(error),
      timestamp: new Date().toISOString()
    })
  })
}

function setupLocalErrorHandling(app) {
  setupVueErrorHandler(app)
  setupGlobalErrorHandlers()
}

function filterSensitiveData(event) {
  const sensitiveFields = ['password', 'token', 'authorization', 'cookie']
  
  if (event.request?.headers) {
    sensitiveFields.forEach(field => {
      if (event.request.headers[field]) {
        event.request.headers[field] = '[FILTERED]'
      }
    })
  }

  return event
}

function logErrorLocally(errorInfo) {
  try {
    const logs = JSON.parse(localStorage.getItem('errorLogs') || '[]')
    logs.push(errorInfo)
    if (logs.length > 100) {
      logs.splice(0, logs.length - 100)
    }
    localStorage.setItem('errorLogs', JSON.stringify(logs))
  } catch (e) {
    console.warn('Failed to store error log:', e)
  }
}

export function captureError(error, context = {}) {
  if (ERROR_MONITORING_CONFIG.dsn) {
    Sentry.withScope((scope) => {
      Object.entries(context).forEach(([key, value]) => {
        scope.setExtra(key, value)
      })
      if (error instanceof Error) {
        Sentry.captureException(error)
      } else {
        Sentry.captureMessage(String(error))
      }
    })
  }

  logErrorLocally({
    type: 'MANUAL_CAPTURE',
    message: error?.message || String(error),
    context,
    timestamp: new Date().toISOString()
  })
}

export function captureMessage(message, level = 'info', context = {}) {
  if (ERROR_MONITORING_CONFIG.dsn) {
    Sentry.withScope((scope) => {
      scope.setLevel(level)
      Object.entries(context).forEach(([key, value]) => {
        scope.setExtra(key, value)
      })
      Sentry.captureMessage(message)
    })
  }
}

export function setUser(user) {
  if (ERROR_MONITORING_CONFIG.dsn && user) {
    Sentry.setUser({
      id: user.id,
      username: user.username,
      email: user.email
    })
  }
}

export function clearUser() {
  if (ERROR_MONITORING_CONFIG.dsn) {
    Sentry.setUser(null)
  }
}

export function addBreadcrumb(breadcrumb) {
  if (ERROR_MONITORING_CONFIG.dsn) {
    Sentry.addBreadcrumb(breadcrumb)
  }
}

export function getLocalErrorLogs() {
  try {
    return JSON.parse(localStorage.getItem('errorLogs') || '[]')
  } catch (e) {
    return []
  }
}

export function clearLocalErrorLogs() {
  localStorage.removeItem('errorLogs')
}

export default {
  initErrorMonitoring,
  captureError,
  captureMessage,
  setUser,
  clearUser,
  addBreadcrumb,
  getLocalErrorLogs,
  clearLocalErrorLogs
}
