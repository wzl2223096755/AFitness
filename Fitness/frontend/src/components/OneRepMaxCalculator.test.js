import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import OneRepMaxCalculator from './OneRepMaxCalculator.vue'

// Mock Vant components and functions
vi.mock('vant', () => ({
  showSuccessToast: vi.fn(),
  showFailToast: vi.fn()
}))

// Mock lodash-es debounce
vi.mock('lodash-es', () => ({
  debounce: (fn) => fn
}))

// Mock the API
vi.mock('../api/fitness', () => ({
  fitnessApi: {
    calculate1RM: vi.fn().mockResolvedValue({ success: true, data: 120 }),
    calculate1RMWithRecord: vi.fn().mockResolvedValue({ success: true }),
    get1RMModels: vi.fn().mockResolvedValue({ 
      success: true, 
      data: ['Epley', 'Brzycki', 'Lombardi', 'OConner', 'Mayhew'] 
    })
  }
}))

import { showSuccessToast, showFailToast } from 'vant'
import { fitnessApi } from '../api/fitness'

describe('OneRepMaxCalculator', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createWrapper = () => {
    return mount(OneRepMaxCalculator, {
      global: {
        stubs: {
          'van-form': true,
          'van-cell-group': true,
          'van-field': true,
          'van-popup': true,
          'van-picker': true,
          'van-icon': true
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render the component', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.orm-calculator').exists()).toBe(true)
    })

    it('should render tech header', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.tech-header').exists()).toBe(true)
      expect(wrapper.find('.header-title').text()).toContain('1RM')
    })

    it('should have default form values', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.form.weight).toBe(100)
      expect(wrapper.vm.form.reps).toBe(5)
      expect(wrapper.vm.form.model).toBe('Epley')
    })
  })

  describe('validation', () => {
    it('validatePositive should return true for positive integers', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.validatePositive(10)).toBe(true)
      expect(wrapper.vm.validatePositive(1)).toBe(true)
      expect(wrapper.vm.validatePositive(100)).toBe(true)
    })

    it('validatePositive should return false for non-positive values', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.validatePositive(0)).toBe(false)
      expect(wrapper.vm.validatePositive(-5)).toBe(false)
      expect(wrapper.vm.validatePositive(null)).toBe(false)
      expect(wrapper.vm.validatePositive(undefined)).toBe(false)
      expect(wrapper.vm.validatePositive('')).toBe(false)
    })

    it('validatePositive should return false for non-integers', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.validatePositive(10.5)).toBe(false)
    })
  })

  describe('computed properties', () => {
    it('hasResults should return false when results is empty', () => {
      const wrapper = createWrapper()
      wrapper.vm.results = {}
      expect(wrapper.vm.hasResults).toBe(false)
    })

    it('hasResults should return true when results has data', async () => {
      const wrapper = createWrapper()
      wrapper.vm.results = { Epley: '120.0' }
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.hasResults).toBe(true)
    })

    it('average1RM should calculate average correctly', async () => {
      const wrapper = createWrapper()
      wrapper.vm.results = { 
        Epley: '120.0', 
        Brzycki: '118.0',
        Lombardi: '122.0'
      }
      await wrapper.vm.$nextTick()
      // Average: (120 + 118 + 122) / 3 = 120
      expect(wrapper.vm.average1RM).toBe('120.0')
    })

    it('average1RM should return 0 when no results', () => {
      const wrapper = createWrapper()
      wrapper.vm.results = {}
      expect(wrapper.vm.average1RM).toBe(0)
    })

    it('intensitySuggestions should calculate correct weights', async () => {
      const wrapper = createWrapper()
      wrapper.vm.results = { Epley: '100.0' }
      await wrapper.vm.$nextTick()
      
      const suggestions = wrapper.vm.intensitySuggestions
      expect(suggestions).toHaveLength(4)
      expect(suggestions[0].percent).toBe(95)
      expect(suggestions[0].weight).toBe('95.0') // 100 * 0.95
      expect(suggestions[1].percent).toBe(85)
      expect(suggestions[1].weight).toBe('85.0') // 100 * 0.85
    })
  })

  describe('picker interaction', () => {
    it('should update model on picker confirm', () => {
      const wrapper = createWrapper()
      wrapper.vm.onPickerConfirm({
        selectedOptions: [{ text: 'Brzycki', value: 'Brzycki' }]
      })
      
      expect(wrapper.vm.form.model).toBe('Brzycki')
      expect(wrapper.vm.showPicker).toBe(false)
    })
  })

  describe('save record', () => {
    it('should call API and show success toast on save', async () => {
      const wrapper = createWrapper()
      
      await wrapper.vm.saveRecord()
      
      expect(fitnessApi.calculate1RMWithRecord).toHaveBeenCalledWith({
        weight: 100,
        reps: 5,
        model: 'Epley'
      })
      expect(showSuccessToast).toHaveBeenCalled()
    })

    it('should show error toast for invalid input', async () => {
      const wrapper = createWrapper()
      wrapper.vm.form.weight = 0
      
      await wrapper.vm.saveRecord()
      
      expect(fitnessApi.calculate1RMWithRecord).not.toHaveBeenCalled()
      expect(showFailToast).toHaveBeenCalledWith('请输入合法的重量和次数')
    })

    it('should show error toast on API failure', async () => {
      fitnessApi.calculate1RMWithRecord.mockResolvedValueOnce({ 
        success: false, 
        message: '保存失败' 
      })
      
      const wrapper = createWrapper()
      await wrapper.vm.saveRecord()
      
      expect(showFailToast).toHaveBeenCalledWith('保存失败')
    })

    it('should handle API exception', async () => {
      fitnessApi.calculate1RMWithRecord.mockRejectedValueOnce(new Error('Network error'))
      
      const wrapper = createWrapper()
      await wrapper.vm.saveRecord()
      
      expect(showFailToast).toHaveBeenCalledWith('系统连接异常')
    })
  })

  describe('supported models', () => {
    it('should have default supported models', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.supportedModels).toHaveLength(5)
      expect(wrapper.vm.supportedModels[0].value).toBe('Epley')
    })
  })
})
