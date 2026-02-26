/**
 * 健身 API - 模拟版本（用于 GitHub Pages 演示）
 */

// 模拟训练数据
const mockTrainingRecords = [
  {
    id: 1,
    date: '2024-01-15',
    type: 'strength',
    exercise: '深蹲',
    sets: 4,
    reps: 12,
    weight: 80,
    rpe: 7,
    duration: null
  },
  {
    id: 2,
    date: '2024-01-14',
    type: 'cardio',
    exercise: '跑步',
    sets: null,
    reps: null,
    weight: null,
    rpe: 6,
    duration: 30
  },
  {
    id: 3,
    date: '2024-01-13',
    type: 'strength',
    exercise: '卧推',
    sets: 3,
    reps: 10,
    weight: 60,
    rpe: 8,
    duration: null
  }
]

// 模拟营养数据
const mockNutritionRecords = [
  {
    id: 1,
    date: '2024-01-15',
    protein: 120,
    carbs: 250,
    fat: 65,
    calories: 2085
  },
  {
    id: 2,
    date: '2024-01-14',
    protein: 110,
    carbs: 280,
    fat: 70,
    calories: 2150
  }
]

// 模拟恢复数据
const mockRecoveryData = {
  sleepQuality: 8,
  muscleSoreness: 3,
  fatigueLevel: 4,
  recoveryScore: 75,
  recommendations: [
    '建议今日进行轻度训练',
    '保持充足睡眠',
    '适当补充蛋白质'
  ]
}

export const fitnessApi = {
  // 训练记录相关
  async getTrainingRecords(params = {}) {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      code: 200,
      data: {
        records: mockTrainingRecords,
        total: mockTrainingRecords.length
      }
    }
  },

  async createTrainingRecord(record) {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      code: 200,
      data: { id: Date.now(), ...record },
      message: '训练记录创建成功'
    }
  },

  // 营养记录相关
  async getNutritionRecords(params = {}) {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      code: 200,
      data: {
        records: mockNutritionRecords,
        total: mockNutritionRecords.length
      }
    }
  },

  async createNutritionRecord(record) {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      code: 200,
      data: { id: Date.now(), ...record },
      message: '营养记录创建成功'
    }
  },

  // 恢复数据相关
  async getRecoveryData() {
    await new Promise(resolve => setTimeout(resolve, 400))
    return {
      code: 200,
      data: mockRecoveryData
    }
  },

  // 统计数据
  async getDashboardStats() {
    await new Promise(resolve => setTimeout(resolve, 600))
    return {
      code: 200,
      data: {
        weeklyTrainingCount: 5,
        totalVolume: 15420,
        weeklyChange: 2,
        recoveryScore: 75,
        nutritionGoalProgress: 85
      }
    }
  },

  // 更新训练记录
  async updateTrainingRecord(id, data) {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      code: 200,
      data: { id, ...data },
      message: '训练记录更新成功'
    }
  },

  // 删除训练记录
  async deleteTrainingRecord(id) {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      code: 200,
      message: '训练记录删除成功'
    }
  },

  // 获取训练建议
  async getTrainingSuggestions() {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      code: 200,
      data: [
        { id: 1, type: 'strength', suggestion: '建议增加深蹲重量，当前强度偏低' },
        { id: 2, type: 'cardio', suggestion: '有氧训练表现良好，可适当增加时长' },
        { id: 3, type: 'recovery', suggestion: '注意休息，建议增加睡眠时间' }
      ]
    }
  },

  // 获取负荷趋势
  async getLoadTrend(params = {}) {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      code: 200,
      data: [
        { date: '2024-01-10', load: 1200 },
        { date: '2024-01-11', load: 1400 },
        { date: '2024-01-12', load: 1100 },
        { date: '2024-01-13', load: 1600 },
        { date: '2024-01-14', load: 1300 },
        { date: '2024-01-15', load: 1500 }
      ]
    }
  }
}
