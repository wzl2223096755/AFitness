import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MetricsOverview from './MetricsOverview.vue'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

// Mock the API
vi.mock('../api/fitness', () => ({
  fitnessApi: {
    getMetricsOverview: vi.fn().mockResolvedValue({
      data: {
        weeklyTrainingCount: 5,
        weeklyChange: 2,
        totalVolume: 12580,
        recoveryScore: 85,
        goalCompletionRate: 78,
        sleepQuality: 4,
        muscleFatigue: 3,
        mentalState: 4,
        goals: [
          { id: 1, name: '训练次数', progress: 5, target: 7 }
        ]
      }
    })
  }
}))

describe('MetricsOverview', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const createWrapper = () => {
    return mount(MetricsOverview, {
      global: {
        stubs: {
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-icon': true,
          'el-dialog': true,
          'el-rate': true,
          'el-progress': true,
          'Refresh': true,
          'TrendCharts': true,
          'Histogram': true,
          'Timer': true,
          'Aim': true,
          'CaretTop': true,
          'CaretBottom': true
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render the component', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.metrics-overview').exists()).toBe(true)
    })

    it('should render section title', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.section-title').text()).toContain('核心指标概览')
    })

    it('should render four metric cards', () => {
      const wrapper = createWrapper()
      const cards = wrapper.findAll('.metric-card')
      expect(cards.length).toBe(4)
    })

    it('should render primary metric card', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.primary-metric').exists()).toBe(true)
    })

    it('should render success metric card', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.success-metric').exists()).toBe(true)
    })

    it('should render warning metric card', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.warning-metric').exists()).toBe(true)
    })

    it('should render danger metric card', () => {
      const wrapper = createWrapper()
      expect(wrapper.find('.danger-metric').exists()).toBe(true)
    })
  })

  describe('computed properties', () => {
    it('should compute weeklyChangeClass correctly for positive change', async () => {
      const wrapper = createWrapper()
      // Default weeklyChange is 2 (positive)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.weeklyChangeClass).toBe('positive')
    })

    it('should compute recoveryStatus correctly', async () => {
      const wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      // Default recoveryScore is 85
      expect(wrapper.vm.recoveryStatus).toBe('恢复良好')
    })

    it('should compute goalCompletionText correctly', async () => {
      const wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      // Default goalCompletionRate is 78
      expect(wrapper.vm.goalCompletionText).toBe('继续努力')
    })
  })

  describe('methods', () => {
    it('formatNumber should format numbers correctly', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.formatNumber(12580)).toBe('12,580')
    })

    it('getRecoveryClass should return correct class', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.getRecoveryClass(85)).toBe('excellent')
      expect(wrapper.vm.getRecoveryClass(70)).toBe('good')
      expect(wrapper.vm.getRecoveryClass(50)).toBe('poor')
    })

    it('getRecoveryDescription should return correct description', () => {
      const wrapper = createWrapper()
      const description = wrapper.vm.getRecoveryDescription(85)
      expect(description).toContain('非常好')
    })
  })

  describe('time range labels', () => {
    it('should have correct labels for week', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.timeRangeLabels.week.training).toBe('本周训练')
    })

    it('should have correct labels for month', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.timeRangeLabels.month.training).toBe('本月训练')
    })

    it('should have correct labels for year', () => {
      const wrapper = createWrapper()
      expect(wrapper.vm.timeRangeLabels.year.training).toBe('本年训练')
    })
  })
})
