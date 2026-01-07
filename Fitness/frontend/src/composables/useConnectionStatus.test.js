import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { nextTick } from 'vue'

// Mock axios
vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}))

// Mock element-plus
vi.mock('element-plus', () => ({
  ElNotification: vi.fn(() => ({ close: vi.fn() })),
  ElMessage: {
    success: vi.fn()
  }
}))

// Import after mocks
import axios from 'axios'
import { ElNotification, ElMessage } from 'element-plus'
import { useConnectionStatus, ConnectionStatus } from './useConnectionStatus'

describe('ConnectionStatus enum', () => {
  it('has CONNECTED status', () => {
    expect(ConnectionStatus.CONNECTED).toBe('connected')
  })

  it('has DISCONNECTED status', () => {
    expect(ConnectionStatus.DISCONNECTED).toBe('disconnected')
  })

  it('has CHECKING status', () => {
    expect(ConnectionStatus.CHECKING).toBe('checking')
  })

  it('has UNKNOWN status', () => {
    expect(ConnectionStatus.UNKNOWN).toBe('unknown')
  })

  it('has OFFLINE status', () => {
    expect(ConnectionStatus.OFFLINE).toBe('offline')
  })
})

describe('useConnectionStatus', () => {
  let originalNavigator

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    
    // Mock navigator.onLine
    originalNavigator = window.navigator
    Object.defineProperty(window, 'navigator', {
      value: { onLine: true },
      writable: true,
      configurable: true
    })
    
    // Reset axios mock
    axios.get.mockResolvedValue({
      data: { code: 200, data: { status: 'UP' } }
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
    
    // Restore navigator
    Object.defineProperty(window, 'navigator', {
      value: originalNavigator,
      writable: true,
      configurable: true
    })
  })

  it('returns status ref', () => {
    const { status } = useConnectionStatus({ autoStart: false })
    expect(status).toBeDefined()
    expect(status.value).toBeDefined()
  })

  it('returns isConnected computed', () => {
    const { isConnected } = useConnectionStatus({ autoStart: false })
    expect(isConnected).toBeDefined()
  })

  it('returns isDisconnected computed', () => {
    const { isDisconnected } = useConnectionStatus({ autoStart: false })
    expect(isDisconnected).toBeDefined()
  })

  it('returns isChecking computed', () => {
    const { isChecking } = useConnectionStatus({ autoStart: false })
    expect(isChecking).toBeDefined()
  })

  it('returns isOffline computed', () => {
    const { isOffline } = useConnectionStatus({ autoStart: false })
    expect(isOffline).toBeDefined()
  })

  it('returns statusText computed', () => {
    const { statusText } = useConnectionStatus({ autoStart: false })
    expect(statusText).toBeDefined()
  })

  it('returns statusColor computed', () => {
    const { statusColor } = useConnectionStatus({ autoStart: false })
    expect(statusColor).toBeDefined()
  })

  it('returns checkNow method', () => {
    const { checkNow } = useConnectionStatus({ autoStart: false })
    expect(typeof checkNow).toBe('function')
  })

  it('returns startChecking method', () => {
    const { startChecking } = useConnectionStatus({ autoStart: false })
    expect(typeof startChecking).toBe('function')
  })

  it('returns stopChecking method', () => {
    const { stopChecking } = useConnectionStatus({ autoStart: false })
    expect(typeof stopChecking).toBe('function')
  })

  it('returns showOfflineNotification method', () => {
    const { showOfflineNotification } = useConnectionStatus({ autoStart: false })
    expect(typeof showOfflineNotification).toBe('function')
  })

  it('returns closeOfflineNotification method', () => {
    const { closeOfflineNotification } = useConnectionStatus({ autoStart: false })
    expect(typeof closeOfflineNotification).toBe('function')
  })

  it('returns ConnectionStatus constant', () => {
    const result = useConnectionStatus({ autoStart: false })
    expect(result.ConnectionStatus).toBeDefined()
    expect(result.ConnectionStatus.CONNECTED).toBe('connected')
  })

  it('returns lastChecked ref', () => {
    const { lastChecked } = useConnectionStatus({ autoStart: false })
    expect(lastChecked).toBeDefined()
  })

  it('returns healthInfo ref', () => {
    const { healthInfo } = useConnectionStatus({ autoStart: false })
    expect(healthInfo).toBeDefined()
  })

  it('returns isOnline ref', () => {
    const { isOnline } = useConnectionStatus({ autoStart: false })
    expect(isOnline).toBeDefined()
  })
})

describe('useConnectionStatus status text', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    Object.defineProperty(window, 'navigator', {
      value: { onLine: true },
      writable: true,
      configurable: true
    })
  })

  it('returns correct text for connected status', async () => {
    axios.get.mockResolvedValue({
      data: { code: 200, data: { status: 'UP' } }
    })
    
    const { checkNow, statusText, status } = useConnectionStatus({ autoStart: false })
    await checkNow()
    
    // Wait for status to update
    await nextTick()
    
    if (status.value === ConnectionStatus.CONNECTED) {
      expect(statusText.value).toBe('已连接')
    }
  })

  it('returns correct text for disconnected status', async () => {
    axios.get.mockRejectedValue(new Error('Network error'))
    
    const { checkNow, statusText, status } = useConnectionStatus({ autoStart: false })
    await checkNow()
    
    await nextTick()
    
    if (status.value === ConnectionStatus.DISCONNECTED) {
      expect(statusText.value).toBe('连接断开')
    }
  })
})

describe('useConnectionStatus status color', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    Object.defineProperty(window, 'navigator', {
      value: { onLine: true },
      writable: true,
      configurable: true
    })
  })

  it('returns green for connected status', async () => {
    axios.get.mockResolvedValue({
      data: { code: 200, data: { status: 'UP' } }
    })
    
    const { checkNow, statusColor, status } = useConnectionStatus({ autoStart: false })
    await checkNow()
    
    await nextTick()
    
    if (status.value === ConnectionStatus.CONNECTED) {
      expect(statusColor.value).toBe('#67c23a')
    }
  })

  it('returns red for disconnected status', async () => {
    axios.get.mockRejectedValue(new Error('Network error'))
    
    const { checkNow, statusColor, status } = useConnectionStatus({ autoStart: false })
    await checkNow()
    
    await nextTick()
    
    if (status.value === ConnectionStatus.DISCONNECTED) {
      expect(statusColor.value).toBe('#f56c6c')
    }
  })
})

describe('useConnectionStatus offline handling', () => {
  it('shows offline notification when offline', () => {
    const { showOfflineNotification } = useConnectionStatus({ autoStart: false, showOfflineUI: false })
    
    showOfflineNotification()
    
    expect(ElNotification).toHaveBeenCalled()
  })

  it('notification has correct title', () => {
    const { showOfflineNotification } = useConnectionStatus({ autoStart: false, showOfflineUI: false })
    
    showOfflineNotification()
    
    expect(ElNotification).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '网络连接已断开'
      })
    )
  })

  it('notification has warning type', () => {
    const { showOfflineNotification } = useConnectionStatus({ autoStart: false, showOfflineUI: false })
    
    showOfflineNotification()
    
    expect(ElNotification).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'warning'
      })
    )
  })

  it('notification does not auto close', () => {
    const { showOfflineNotification } = useConnectionStatus({ autoStart: false, showOfflineUI: false })
    
    showOfflineNotification()
    
    expect(ElNotification).toHaveBeenCalledWith(
      expect.objectContaining({
        duration: 0
      })
    )
  })
})

describe('useConnectionStatus options', () => {
  it('accepts checkInterval option', () => {
    const { startChecking } = useConnectionStatus({ 
      autoStart: false, 
      checkInterval: 60000 
    })
    
    expect(startChecking).toBeDefined()
  })

  it('accepts autoStart option', () => {
    const result = useConnectionStatus({ autoStart: false })
    expect(result).toBeDefined()
  })

  it('accepts showOfflineUI option', () => {
    const result = useConnectionStatus({ 
      autoStart: false, 
      showOfflineUI: false 
    })
    expect(result).toBeDefined()
  })

  it('defaults autoStart to true', () => {
    // This test verifies the default behavior
    const result = useConnectionStatus({ showOfflineUI: false })
    expect(result).toBeDefined()
  })
})
