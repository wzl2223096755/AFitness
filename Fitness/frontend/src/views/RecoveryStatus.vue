<template>
  <div class="recovery-status-container">
    <h2>恢复状态评估</h2>
    
    <div class="assessment-form">
      <el-form :model="recoveryForm" label-width="120px">
        <el-form-item label="睡眠时长(小时)">
          <el-slider v-model="recoveryForm.sleepHours" :min="0" :max="12" :marks="sleepMarks" />
          <div class="slider-value">{{ recoveryForm.sleepHours }} 小时</div>
        </el-form-item>
        
        <el-form-item label="压力水平">
          <el-slider v-model="recoveryForm.stressLevel" :min="1" :max="10" :marks="stressMarks" />
          <div class="slider-value">{{ recoveryForm.stressLevel }} - {{ getStressLevelText(recoveryForm.stressLevel) }}</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="assessRecovery">评估恢复状态</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div v-if="recoveryResult" class="recovery-result">
      <h3>恢复评估结果</h3>
      
      <div class="score-card">
        <div class="score-circle">
          <div class="score-number">{{ recoveryResult.recoveryScore }}</div>
          <div class="score-label">恢复评分</div>
        </div>
        <div class="score-status">{{ recoveryResult.recoveryStatus }}</div>
      </div>
      
      <div class="recovery-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="睡眠时长">{{ recoveryResult.sleepHours }} 小时</el-descriptions-item>
          <el-descriptions-item label="压力水平">{{ recoveryResult.stressLevel }} - {{ getStressLevelText(recoveryResult.stressLevel) }}</el-descriptions-item>
          <el-descriptions-item label="评估时间">{{ formatDateTime(new Date()) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="recovery-advice">
        <h4>恢复建议</h4>
        <div class="advice-content">
          <p v-if="recoveryResult.recoveryScore >= 80">
            您的恢复状态非常好，可以正常进行训练。建议保持良好的作息习惯，继续当前的训练计划。
          </p>
          <p v-else-if="recoveryResult.recoveryScore >= 60">
            您的恢复状态良好，但仍有提升空间。建议确保充足的睡眠，适当补充营养，训练后进行拉伸放松。
          </p>
          <p v-else-if="recoveryResult.recoveryScore >= 40">
            您处于轻度疲劳状态。建议降低训练强度，增加休息日，保证睡眠质量，考虑补充蛋白质和电解质。
          </p>
          <p v-else-if="recoveryResult.recoveryScore >= 20">
            您的疲劳程度较高。建议减少训练量，增加休息时间，保证充足睡眠，考虑进行低强度的恢复性训练如瑜伽或散步。
          </p>
          <p v-else>
            您处于严重疲劳状态。强烈建议停止训练，完全休息，确保充足睡眠和营养摄入。如症状持续，建议咨询专业医生。
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from '../utils/message.js'
import service from '../api/request'

export default {
  name: 'RecoveryStatus',
  setup() {
    const recoveryForm = ref({
      sleepHours: 7,
      stressLevel: 5
    })
    
    const recoveryResult = ref(null)
    
    const sleepMarks = {
      0: '0',
      4: '4',
      7: '7',
      10: '10',
      12: '12'
    }
    
    const stressMarks = {
      1: '很低',
      5: '中等',
      10: '很高'
    }
    
    const assessRecovery = async () => {
      try {
        const response = await service.post('/api/v1/load-recovery/recovery-assessment', {
          sleepHours: recoveryForm.value.sleepHours,
          stressLevel: recoveryForm.value.stressLevel
        })
        
        if (response.data.success) {
          recoveryResult.value = response.data.data
          ElMessage.success('恢复状态评估完成')
        }
      } catch (error) {
        ElMessage.error('评估失败：' + error.message)
      }
    }
    
    const getStressLevelText = (level) => {
      if (level <= 3) return '低'
      if (level <= 7) return '中'
      return '高'
    }
    
    const formatDateTime = (date) => {
      return date.toLocaleString('zh-CN')
    }
    
    return {
      recoveryForm,
      recoveryResult,
      sleepMarks,
      stressMarks,
      assessRecovery,
      getStressLevelText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.recovery-status-container {
  padding: 20px;
}

.assessment-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.slider-value {
  text-align: center;
  margin-top: 10px;
  color: #409EFF;
  font-weight: bold;
}

.recovery-result {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.score-circle {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  margin-bottom: 20px;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
}

.score-label {
  font-size: 18px;
}

.score-status {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.recovery-details {
  margin-bottom: 30px;
}

.recovery-advice {
  background: #f0f9eb;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.advice-content p {
  line-height: 1.8;
  color: var(--text-secondary);
}
</style>