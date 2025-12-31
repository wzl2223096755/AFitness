<template>
  <div class="calorie-calculator">
    <van-notice-bar
      left-icon="info-o"
      text="热量消耗估算基于运动时长和强度系数。结果仅供参考，实际消耗受个人体质影响。"
    />

    <van-form class="calorie-form" @submit.prevent>
      <van-cell-group inset>
        <van-field
          v-model.number="form.duration"
          label="时长 (min)"
          placeholder="运动持续时间（分钟）"
          type="digit"
          required
          :rules="[{ required: true, message: '请输入时长' }, { validator: validatePositive, message: '必须为正数' }]"
        />
        
        <van-field
          v-model="form.intensityLabel"
          is-link
          readonly
          label="运动强度"
          placeholder="选择运动强度"
          @click="showPicker = true"
        />
      </van-cell-group>
    </van-form>

    <van-popup v-model:show="showPicker" position="bottom">
      <van-picker
        :columns="intensityOptions"
        @confirm="onPickerConfirm"
        @cancel="showPicker = false"
      />
    </van-popup>

    <div class="result-section" v-if="caloriesBurned > 0">
      <van-cell-group inset title="估算结果">
        <van-cell title="预计消耗">
          <template #value>
            <span class="calorie-value">{{ caloriesBurned }} kcal</span>
          </template>
        </van-cell>
        <van-cell title="强度系数" :value="form.intensity" />
      </van-cell-group>

      <div class="calorie-tips">
        <p class="tip-title"><van-icon name="fire-o" /> 消耗参考</p>
        <p class="tip-content">
          此消耗量大约相当于消耗了：<br/>
          <strong>{{ (caloriesBurned / 250).toFixed(1) }}</strong> 碗米饭 (约250kcal/碗) 或 <br/>
          <strong>{{ (caloriesBurned / 500).toFixed(1) }}</strong> 个巨无霸汉堡 (约500kcal/个)
        </p>
      </div>

      <!-- 保存按钮 -->
      <div style="margin: 16px;">
        <van-button 
          round 
          block 
          type="primary" 
          :loading="saving" 
          @click="saveRecord"
          icon="records-o"
        >
          保存记录到历史
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { fitnessApi } from '../api/fitness'
import { showSuccessToast, showFailToast } from 'vant'

const form = ref({
  duration: 45,
  intensity: 5.0,
  intensityLabel: '中等强度 (5.0)'
})

const showPicker = ref(false)
const saving = ref(false)

const intensityOptions = [
  { text: '低强度 (3.0)', value: 3.0 },
  { text: '中等强度 (5.0)', value: 5.0 },
  { text: '高强度 (8.0)', value: 8.0 },
  { text: '极高强度 (12.0)', value: 12.0 }
]

const validatePositive = (val) => val > 0

const caloriesBurned = computed(() => {
  const { duration, intensity } = form.value
  if (duration > 0) {
    // 后端公式: duration * intensity * 1.2
    return Math.round(duration * intensity * 1.2)
  }
  return 0
})

const onPickerConfirm = ({ selectedOptions }) => {
  const option = selectedOptions[0]
  form.value.intensity = option.value
  form.value.intensityLabel = option.text
  showPicker.value = false
}

const saveRecord = async () => {
  const { duration, intensity } = form.value
  if (!duration) return

  saving.value = true
  try {
    const res = await fitnessApi.calculateCaloriesWithRecord({ duration, intensity })
    if (res.success) {
      showSuccessToast('记录已保存')
    } else {
      showFailToast(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存热量记录失败', error)
    showFailToast('保存失败，请检查网络')
  } finally {
    saving.value = false
  }
}
</script>

<script>
export default {
  name: 'CalorieCalculator'
}
</script>

<style scoped>
.calorie-calculator {
  padding-bottom: 20px;
}
.calorie-form {
  margin-top: 12px;
}
.result-section {
  margin-top: 24px;
}
.calorie-value {
  color: #ee0a24;
  font-size: 20px;
  font-weight: 800;
}
.calorie-tips {
  margin: 16px;
  padding: 12px;
  background-color: #fff2f0;
  border-radius: 8px;
  border: 1px solid #ffccc7;
}
.tip-title {
  margin: 0 0 8px 0;
  font-weight: bold;
  color: #cf1322;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.tip-content {
  margin: 0;
  font-size: 12px;
  color: #cf1322;
  line-height: 1.6;
}
</style>
