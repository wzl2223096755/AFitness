import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// Mock Element Plus components globally
config.global.stubs = {
  'el-button': true,
  'el-input': true,
  'el-form': true,
  'el-form-item': true,
  'el-card': true,
  'el-row': true,
  'el-col': true,
  'el-icon': true,
  'el-message': true,
  'el-dialog': true,
  'el-table': true,
  'el-table-column': true,
  'el-pagination': true,
  'el-select': true,
  'el-option': true,
  'el-date-picker': true,
  'el-input-number': true,
  'el-progress': true,
  'el-statistic': true,
  'el-divider': true,
  'el-tooltip': true,
  'el-tag': true,
  'el-empty': true,
  'el-skeleton': true,
  'el-skeleton-item': true,
  'v-chart': true,
}

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// Mock ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock IntersectionObserver
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock localStorage
const localStorageMock = {
  store: {},
  getItem: vi.fn((key) => localStorageMock.store[key] || null),
  setItem: vi.fn((key, value) => { localStorageMock.store[key] = value }),
  removeItem: vi.fn((key) => { delete localStorageMock.store[key] }),
  clear: vi.fn(() => { localStorageMock.store = {} }),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock import.meta.env
vi.stubGlobal('import.meta', {
  env: {
    DEV: true,
    PROD: false,
    MODE: 'test'
  }
})
