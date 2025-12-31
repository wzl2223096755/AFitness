import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import CalorieCalculator from './CalorieCalculator.vue'

// Mock Vant components and functions
vi.mock('vant', () => ({
  showSuccessToast: vi.fn(),
  showFailToast: vi.fn()
}))

// Mock the API
vi.mock('../api/fitness', () => ({
  fitnessApi: {
    calculateCaloriesWithRecord: vi.fn().mockResolvedValue({ success: true })
  }
}))

import { showSuccessToast, showFailToast } from 'vant'
import { fitnessApi } from '../api/fitness'

describe('CalorieCalculator', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createWrapper = () => {
    return mount(CalorieCalculator, {
      global: {
        stubs: {
          'van-notice-bar': true,
          'van-form': true,
          'van-cell-group': true,
          'van-field': true,
          'van-popup': true,
          'van-picker': true,
          'van-cell': true,
          'van-icon': true,
          'van-button': true
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render the component', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.calorie-calculator').exists()).toBe(true)
    })

    it('should have default form values', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.form.duration).toBe(45)
      expect(wrapper.vm.form.intensity).toBe(5.0)
    })
  })

  describe('calorie calculation', () => {
    it('should calculate calories correctly', () => {
      const wrapper = createWrapper()
      // Formula: duration * intensity * 1.2
      // 45 * 5.0 * 1.2 = 270
      expect(wrapper.vm.caloriesBurned).toBe(270)
    })

    it('should return 0 when duration is 0', async () => {
      const wrapper = createWrapper()
      wrapper.vm.form.duration = 0
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.caloriesBurned).toBe(0)
    })

    it('should calculate correctly with different intensity', async () => {
      const wrapper = createWrapper()
      wrapper.vm.form.duration = 30
      wrapper.vm.form.intensity = 8.0
      await wrapper.vm.$nextTick()
      // 30 * 8.0 * 1.2 = 288
      expect(wrapper.vm.caloriesBurned).toBe(288)
    })

    it('should calculate correctly with low intensity', async () => {
      const wrapper = createWrapper()
      wrapper.vm.form.duration = 60
      wrapper.vm.form.intensity = 3.0
      await wrapper.vm.$nextTick()
      // 60 * 3.0 * 1.2 = 216
      expect(wrapper.vm.caloriesBurned).toBe(216)
    })
  })

  describe('validation', () => {
    it('validatePositive should return true for positive numbers', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.validatePositive(10)).toBe(true)
      expect(wrapper.vm.validatePositive(1)).toBe(true)
    })

    it('validatePositive should return false for non-positive numbers', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.validatePositive(0)).toBe(false)
      expect(wrapper.vm.validatePositive(-5)).toBe(false)
    })
  })

  describe('intensity options', () => {
    it('should have correct intensity options', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.intensityOptions).toHaveLength(4)
      expect(wrapper.vm.intensityOptions[0].value).toBe(3.0)
      expect(wrapper.vm.intensityOptions[1].value).toBe(5.0)
      expect(wrapper.vm.intensityOptions[2].value).toBe(8.0)
      expect(wrapper.vm.intensityOptions[3].value).toBe(12.0)
    })
  })

  describe('picker interaction', () => {
    it('should update intensity on picker confirm', () => {
      const wrapper = createWrapper()
      wrapper.vm.onPickerConfirm({
        selectedOptions: [{ text: '高强度 (8.0)', value: 8.0 }]
      })
      
      expect(wrapper.vm.form.intensity).toBe(8.0)
      expect(wrapper.vm.form.intensityLabel).toBe('高强度 (8.0)')
      expect(wrapper.vm.showPicker).toBe(false)
    })
  })

  describe('save record', () => {
    it('should call API and show success toast on save', async () => {
      const wrapper = createWrapper()
      
      await wrapper.vm.saveRecord()
      
      expect(fitnessApi.calculateCaloriesWithRecord).toHaveBeenCalledWith({
        duration: 45,
        intensity: 5.0
      })
      expect(showSuccessToast).toHaveBeenCalledWith('记录已保存')
    })

    it('should not call API when duration is empty', async () => {
      const wrapper = createWrapper()
      wrapper.vm.form.duration = null
      
      await wrapper.vm.saveRecord()
      
      expect(fitnessApi.calculateCaloriesWithRecord).not.toHaveBeenCalled()
    })

    it('should show error toast on API failure', async () => {
      fitnessApi.calculateCaloriesWithRecord.mockResolvedValueOnce({ 
        success: false, 
        message: '保存失败' 
      })
      
      const wrapper = createWrapper()
      await wrapper.vm.saveRecord()
      
      expect(showFailToast).toHaveBeenCalledWith('保存失败')
    })

    it('should handle API exception', async () => {
      fitnessApi.calculateCaloriesWithRecord.mockRejectedValueOnce(new Error('Network error'))
      
      const wrapper = createWrapper()
      await wrapper.vm.saveRecord()
      
      expect(showFailToast).toHaveBeenCalledWith('保存失败，请检查网络')
    })
  })
})
