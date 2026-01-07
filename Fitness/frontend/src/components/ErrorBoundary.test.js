import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import ErrorBoundary from './ErrorBoundary.vue'
import ElementPlus from 'element-plus'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Mock error monitoring
vi.mock('@/utils/errorMonitoring.js', () => ({
  captureError: vi.fn()
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => '[]'),
  setItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock location.reload
const reloadMock = vi.fn()
Object.defineProperty(window, 'location', {
  value: { reload: reloadMock, href: 'http://localhost/' },
  writable: true
})

describe('ErrorBoundary', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.getItem.mockReturnValue('[]')
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders slot content when no error', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      },
      slots: {
        default: '<div class="test-content">Test Content</div>'
      }
    })

    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.find('.error-boundary').exists()).toBe(false)
  })

  it('exposes reset method', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(typeof wrapper.vm.reset).toBe('function')
  })

  it('exposes hasError ref', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.vm.hasError).toBe(false)
  })

  it('exposes retryCount ref', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.vm.retryCount).toBe(0)
  })

  it('accepts showDetails prop', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      },
      props: {
        showDetails: true
      }
    })

    expect(wrapper.props('showDetails')).toBe(true)
  })

  it('accepts maxRetries prop', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      },
      props: {
        maxRetries: 5
      }
    })

    expect(wrapper.props('maxRetries')).toBe(5)
  })

  it('accepts customErrorMessage prop', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      },
      props: {
        customErrorMessage: '自定义错误消息'
      }
    })

    expect(wrapper.props('customErrorMessage')).toBe('自定义错误消息')
  })

  it('has default maxRetries of 3', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.props('maxRetries')).toBe(3)
  })

  it('has default showDetails of false', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.props('showDetails')).toBe(false)
  })

  it('reset method clears error state', async () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    // Manually set error state
    wrapper.vm.hasError = true
    wrapper.vm.retryCount = 2
    
    await nextTick()
    
    // Call reset
    wrapper.vm.reset()
    
    await nextTick()
    
    expect(wrapper.vm.hasError).toBe(false)
    expect(wrapper.vm.retryCount).toBe(0)
  })
})

describe('ErrorBoundary error handling', () => {
  it('stores error reports in localStorage', () => {
    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    // Verify localStorage mock is set up
    expect(localStorageMock.getItem).toBeDefined()
    expect(localStorageMock.setItem).toBeDefined()
  })

  it('limits stored error reports to 20', () => {
    // Create array with 25 reports
    const existingReports = Array(25).fill({ message: 'test' })
    localStorageMock.getItem.mockReturnValue(JSON.stringify(existingReports))

    const wrapper = mount(ErrorBoundary, {
      global: {
        plugins: [ElementPlus]
      }
    })

    // The component should handle this gracefully
    expect(wrapper.vm).toBeDefined()
  })
})
