<template>
  <div class="training-suggestions-container">
    <h2>训练建议</h2>
    
    <div class="suggestion-card">
      <el-button type="primary" @click="fetchSuggestions" class="refresh-btn">
        <el-icon><Refresh /></el-icon> 获取最新建议
      </el-button>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在生成个性化建议...</p>
      </div>
      
      <div v-else-if="suggestions" class="suggestions-content">
        <div class="recovery-indicator">
          <div class="indicator-header">
            <span>恢复状态</span>
            <span class="recovery-score">{{ suggestions.recoveryScore }}</span>
          </div>
          <el-progress 
            :percentage="suggestions.recoveryScore" 
            :color="getRecoveryColor(suggestions.recoveryScore)"
            :format="formatProgress"
          />
        </div>
        
        <div class="main-suggestion">
          <h3>今日训练建议</h3>
          <p>{{ suggestions.suggestionText }}</p>
        </div>
        
        <div class="training-tips">
          <h4>训练小贴士</h4>
          <el-timeline>
            <el-timeline-item v-for="(tip, index) in getTrainingTips(suggestions.recoveryScore)" :key="index">
              {{ tip }}
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="exercise-recommendations">
          <h4>推荐训练动作</h4>
          <div class="exercise-grid">
            <div v-for="(exercise, index) in recommendedExercises" :key="index" class="exercise-item">
              <div class="exercise-name">{{ exercise.name }}</div>
              <div class="exercise-type">{{ exercise.type }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无训练建议，请先录入训练数据"></el-empty>
      </div>
    </div>
    
    <div class="training-plan">
      <h3>本周训练规划建议</h3>
      <div class="week-plan">
        <div v-for="day in weekDays" :key="day.name" class="day-plan">
          <div class="day-header">{{ day.name }}</div>
          <div class="day-content">{{ getDayPlan(day.index, suggestions?.recoveryScore) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from '../utils/message.js'
import { Refresh } from '@element-plus/icons-vue'

import service from '../api/request'

export default {
  name: 'TrainingSuggestions',
  components: {
    Refresh
  },
  setup() {
    const suggestions = ref(null)
    const loading = ref(false)
    const weekDays = [
      { name: '周一', index: 1 },
      { name: '周二', index: 2 },
      { name: '周三', index: 3 },
      { name: '周四', index: 4 },
      { name: '周五', index: 5 },
      { name: '周六', index: 6 },
      { name: '周日', index: 0 }
    ]
    
    const recommendedExercises = [
      { name: '深蹲', type: '下肢' },
      { name: '卧推', type: '上肢' },
      { name: '硬拉', type: '全身' },
      { name: '引体向上', type: '上肢' },
      { name: '肩推', type: '上肢' },
      { name: '腹肌训练', type: '核心' }
    ]
    
    const fetchSuggestions = async () => {
      loading.value = true
      try {
        // 调用后端LoadRecoveryController获取训练建议
        const response = await service.get('/api/v1/load-recovery/training-suggestions')
        if (response.data) {
          suggestions.value = response.data
          ElMessage.success('获取训练建议成功')
        } else {
          ElMessage.warning('无法获取训练建议，请稍后重试')
        }
      } catch (error) {
        console.error('获取训练建议失败', error)
        ElMessage.error('获取训练建议失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    const getRecoveryColor = (score) => {
      if (score >= 80) return '#67c23a'
      if (score >= 60) return '#e6a23c'
      if (score >= 40) return '#f56c6c'
      return '#f56c6c'
    }
    
    const formatProgress = (percentage) => {
      if (percentage >= 80) return '良好'
      if (percentage >= 60) return '一般'
      if (percentage >= 40) return '疲劳'
      return '严重疲劳'
    }
    
    const getTrainingTips = (recoveryScore) => {
      if (recoveryScore >= 80) {
        return [
          '今天适合进行高强度训练',
          '注意保持正确的动作姿势',
          '训练后及时补充营养'
        ]
      } else if (recoveryScore >= 60) {
        return [
          '建议中等强度训练',
          '增加组间休息时间',
          '训练后进行充分拉伸'
        ]
      } else {
        return [
          '降低训练强度',
          '缩短训练时间',
          '保证充足睡眠',
          '增加水分摄入'
        ]
      }
    }
    
    const getDayPlan = (dayIndex, recoveryScore) => {
      const plans = [
        '休息日',
        '上肢训练',
        '下肢训练',
        '核心训练',
        '全身训练',
        '上肢训练',
        '下肢训练'
      ]
      
      // 根据恢复状态调整
      if (recoveryScore !== undefined && recoveryScore < 40) {
        if (dayIndex !== 0) return '轻度恢复训练'
      }
      
      return plans[dayIndex]
    }
    
    return {
      suggestions,
      loading,
      weekDays,
      recommendedExercises,
      fetchSuggestions,
      getRecoveryColor,
      formatProgress,
      getTrainingTips,
      getDayPlan
    }
  }
}
</script>

<style scoped>
.training-suggestions-container {
  padding: 20px;
  background: linear-gradient(135deg, var(--bg-primary, #0a0a14) 0%, var(--bg-secondary, #121225) 100%);
  min-height: calc(100vh - 60px);
}

.suggestion-card {
  background: rgba(18, 18, 37, 0.95);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  box-shadow: var(--shadow-base, 0 0 15px rgba(112, 0, 255, 0.3));
}

.refresh-btn {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.recovery-indicator {
  background: #f0f9eb;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.indicator-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-weight: bold;
}

.recovery-score {
  color: #67c23a;
  font-size: 24px;
}

.main-suggestion {
  background: #ecf5ff;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  margin-bottom: 20px;
}

.main-suggestion h3 {
  margin-bottom: 10px;
  color: #409eff;
}

.main-suggestion p {
  line-height: 1.8;
  color: var(--text-secondary);
}

.training-tips {
  margin-bottom: 20px;
}

.exercise-recommendations {
  margin-top: 20px;
}

.exercise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.exercise-item {
  background: rgba(18, 18, 37, 0.8);
  border: 1px solid var(--border-color, rgba(112, 0, 255, 0.2));
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.exercise-name {
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--text-primary);
}

.exercise-type {
  color: var(--text-secondary);
  font-size: 14px;
}

.training-plan {
  background: var(--glass-bg);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-default);
  box-shadow: var(--shadow-md);
}

.training-plan h3 {
  color: var(--text-primary);
}

.week-plan {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  margin-top: 20px;
}

.day-plan {
  background: var(--glass-bg);
  border: 1px solid var(--border-default);
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.day-header {
  font-weight: bold;
  margin-bottom: 10px;
  color: var(--brand-primary);
}

.day-content {
  color: var(--text-primary);
}

.no-data {
  padding: 40px;
}
</style>