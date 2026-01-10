<template>
  <div class="orm-calculator tech-theme">
    <!-- 科技感背景装饰 -->
    <div class="tech-bg">
      <div class="grid-overlay"></div>
      <div class="scan-line"></div>
      <div class="energy-blob blob-1"></div>
      <div class="energy-blob blob-2"></div>
    </div>

    <!-- 顶部状态栏 -->
    <div class="tech-header">
      <div class="header-main">
        <van-icon name="chart-trending-o" class="header-icon" />
        <span class="header-title">1RM 力量评估系统</span>
      </div>
      <div class="header-status">
        <span class="status-dot"></span>
        SYSTEM READY
      </div>
    </div>

    <!-- 输入表单 -->
    <van-form class="tech-form" @submit.prevent>
      <div class="form-container glass-panel">
        <div class="panel-corner top-left"></div>
        <div class="panel-corner bottom-right"></div>
        
        <van-cell-group inset :border="false" class="transparent-group">
          <!-- 重量输入 -->
          <div class="field-wrapper">
            <div class="field-label">WEIGHT / 重量 (kg)</div>
            <van-field
              v-model.number="form.weight"
              name="weight"
              placeholder="0.0"
              type="digit"
              input-align="right"
              :border="false"
              class="tech-input"
              @update:model-value="onInput"
            />
            <div class="input-glow"></div>
          </div>

          <!-- 次数输入 -->
          <div class="field-wrapper">
            <div class="field-label">REPS / 次数</div>
            <van-field
              v-model.number="form.reps"
              name="reps"
              placeholder="0"
              type="digit"
              input-align="right"
              :border="false"
              class="tech-input"
              @update:model-value="onInput"
            />
            <div class="input-glow"></div>
          </div>

          <!-- 公式选择 -->
          <div class="field-wrapper" @click="showPicker = true">
            <div class="field-label">ALGORITHM / 算法</div>
            <div class="tech-select">
              <span class="select-value">{{ form.model }}</span>
              <van-icon name="arrow-down" class="select-icon" />
            </div>
            <div class="input-glow"></div>
          </div>
        </van-cell-group>
      </div>
    </van-form>

    <!-- 计算结果展示 -->
    <transition name="tech-fade">
      <div v-if="hasResults" class="result-container">
        <!-- 核心数值展示 -->
        <div class="main-result glass-panel pulse-border">
          <div class="result-title">ESTIMATED 1RM / 估算极限</div>
          <div class="result-value-box">
            <span class="result-number">{{ average1RM }}</span>
            <span class="result-unit">KG</span>
          </div>
          <div class="scanning-bar"></div>
        </div>

        <!-- 强度分配建议 -->
        <div class="intensity-section">
          <div class="section-header">
            <span class="line"></span>
            <span class="text">INTENSITY SUGGESTIONS / 强度建议</span>
            <span class="line"></span>
          </div>
          
          <div class="intensity-grid">
            <div 
              v-for="item in intensitySuggestions" 
              :key="item.label"
              class="intensity-item glass-panel"
              :style="{ '--accent-color': item.color }"
            >
              <div class="item-percent">{{ item.percent }}%</div>
              <div class="item-weight">{{ item.weight }}kg</div>
              <div class="item-desc">{{ item.desc }}</div>
              <div class="item-progress">
                <div class="progress-fill" :style="{ width: item.percent + '%', backgroundColor: item.color }"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <button class="tech-btn primary" :disabled="saving" @click="saveRecord">
            <span class="btn-text">INITIALIZE STORAGE / 保存数据</span>
            <div class="btn-glow"></div>
          </button>
        </div>
      </div>
    </transition>

    <!-- 公式选择器 -->
    <van-popup v-model:show="showPicker" position="bottom" round class="tech-popup">
      <van-picker
        title="SELECT ALGORITHM"
        :columns="supportedModels"
        @confirm="onPickerConfirm"
        @cancel="showPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
/**
 * OneRepMaxCalculator.vue
 * 1RM 计算器组件 - 科技感升级版
 * 技术栈: Vue 3 (Composition API) + Vant UI
 */
import { ref, computed, onMounted } from 'vue'
import { fitnessApi } from '../api/fitness'
import { debounce } from 'lodash-es'
import { showSuccessToast, showFailToast } from 'vant'

// --- 响应式数据 ---
const form = ref({
  weight: 100,
  reps: 5,
  model: 'Epley'
})

const showPicker = ref(false)
const saving = ref(false)
const results = ref({})
const supportedModels = ref([
  { text: 'Epley (常用)', value: 'Epley' },
  { text: 'Brzycki', value: 'Brzycki' },
  { text: 'Lombardi', value: 'Lombardi' },
  { text: 'OConner', value: 'OConner' },
  { text: 'Mayhew', value: 'Mayhew' }
])

// --- 计算属性 ---
const hasResults = computed(() => Object.keys(results.value).length > 0)

// 计算综合平均 1RM
const average1RM = computed(() => {
  if (!hasResults.value) return 0
  const values = Object.values(results.value).map(v => parseFloat(v))
  const sum = values.reduce((acc, val) => acc + val, 0)
  return (sum / values.length).toFixed(1)
})

// 生成强度建议数据
const intensitySuggestions = computed(() => {
  const avg = parseFloat(average1RM.value)
  return [
    { percent: 95, label: '极限', weight: (avg * 0.95).toFixed(1), desc: '1-2次/组 (力量)', color: '#ff0055' },
    { percent: 85, label: '力量', weight: (avg * 0.85).toFixed(1), desc: '3-5次/组 (强度)', color: '#7000ff' },
    { percent: 75, label: '肌肥大', weight: (avg * 0.75).toFixed(1), desc: '8-12次/组 (增肌)', color: '#ff00ff' },
    { percent: 65, label: '耐力', weight: (avg * 0.65).toFixed(1), desc: '15-20次/组 (减脂)', color: '#00f2fe' }
  ]
})

// --- 方法 ---

// 正整数校验函数
const validatePositive = (val) => {
  if (val === undefined || val === null || val === '') return false
  const num = Number(val)
  return Number.isInteger(num) && num > 0
}

// 防抖计算函数
const autoCalculate = debounce(() => {
  calculate()
}, 500)

// 处理输入，触发防抖计算
const onInput = () => {
  autoCalculate()
}

// 选择公式确认
const onPickerConfirm = ({ selectedOptions }) => {
  form.value.model = selectedOptions[0].value
  showPicker.value = false
  autoCalculate()
}

// 调用后端 API 计算 1RM (实时渲染逻辑)
const calculate = async () => {
  const { weight, reps } = form.value
  
  // 校验输入
  if (!validatePositive(weight) || !validatePositive(reps)) {
    results.value = {}
    return
  }

  try {
    // 获取所有公式结果
    const promises = supportedModels.value.map(async (modelObj) => {
      const model = modelObj.value
      const res = await fitnessApi.calculate1RM({ weight, reps, model })
      return { model, value: res.success ? res.data : null }
    })
    
    const rawResults = await Promise.all(promises)
    const resultMap = {}
    rawResults.forEach(item => {
      if (item.value !== null) {
        const name = item.model.split(' ')[0]
        resultMap[name] = item.value.toFixed(1)
      }
    })
    results.value = resultMap
  } catch (error) {
    console.error('1RM 计算失败:', error)
  }
}

// 保存记录到历史
const saveRecord = async () => {
  const { weight, reps, model } = form.value
  if (!validatePositive(weight) || !validatePositive(reps)) {
    showFailToast('请输入合法的重量和次数')
    return
  }

  saving.value = true
  try {
    const res = await fitnessApi.calculate1RMWithRecord({ weight, reps, model })
    if (res.success) {
      showSuccessToast('数据已存入核心系统')
    } else {
      showFailToast(res.message || '存储失败')
    }
  } catch (error) {
    console.error('保存记录失败:', error)
    showFailToast('系统连接异常')
  } finally {
    saving.value = false
  }
}

// --- 生命周期 ---
onMounted(async () => {
  try {
    const res = await fitnessApi.get1RMModels()
    if (res.success && Array.isArray(res.data)) {
      supportedModels.value = res.data.map(m => ({
        text: m === 'Epley' ? 'Epley (常用)' : m,
        value: m
      }))
    }
  } catch (e) {
    console.warn('获取 1RM 模型列表失败')
  }
  
  calculate()
})
</script>

<style scoped>
.tech-theme {
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-primary);
  padding: 20px 16px 100px;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 背景特效 */
.tech-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.grid-overlay {
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(var(--border-subtle) 1px, transparent 1px),
    linear-gradient(90deg, var(--border-subtle) 1px, transparent 1px);
  background-size: 30px 30px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--brand-primary), transparent);
  animation: scan 4s linear infinite;
  opacity: 0.3;
}

.energy-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
}

.blob-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-accent));
  top: -100px;
  right: -50px;
  animation: float 10s ease-in-out infinite alternate;
}

.blob-2 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, var(--brand-secondary), #ec4899);
  bottom: 100px;
  left: -50px;
  animation: float 12s ease-in-out infinite alternate-reverse;
}

/* 头部样式 */
.tech-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 24px;
  color: var(--brand-primary);
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-primary);
}

.header-status {
  font-size: 10px;
  color: var(--brand-primary);
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--btn-secondary-bg);
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid var(--border-brand);
}

.status-dot {
  width: 6px;
  height: 6px;
  background: var(--brand-primary);
  border-radius: 50%;
  box-shadow: 0 0 5px var(--brand-primary);
  animation: pulse 1.5s infinite;
}

/* 通用面板 */
.glass-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--border-default);
  border-radius: 12px;
  position: relative;
  box-shadow: var(--shadow-md);
}

.panel-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid var(--brand-primary);
  opacity: 0.4;
}

.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

/* 表单样式 */
.tech-form {
  position: relative;
  z-index: 1;
  margin-bottom: 24px;
}

.form-container {
  padding: 16px;
}

.transparent-group {
  background: transparent !important;
}

.field-wrapper {
  margin-bottom: 20px;
  position: relative;
}

.field-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tech-input {
  background: var(--input-bg) !important;
  border: 1px solid var(--input-border) !important;
  border-radius: 8px;
  color: var(--input-text) !important;
  --van-field-input-text-color: var(--input-text);
  --van-field-placeholder-text-color: var(--input-placeholder);
}

.input-glow {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--brand-primary);
  transition: width 0.3s ease;
  box-shadow: var(--shadow-brand);
}

.field-wrapper:focus-within .input-glow {
  width: 100%;
}

.tech-select {
  background: var(--input-bg);
  border: 1px solid var(--input-border);
  border-radius: 8px;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  color: var(--text-primary);
}

.select-value {
  color: var(--brand-primary);
  font-weight: 600;
}

.select-icon {
  color: var(--text-secondary);
}

/* 结果区域 */
.result-container {
  position: relative;
  z-index: 1;
}

.main-result {
  padding: 30px;
  text-align: center;
  margin-bottom: 30px;
  border: 1px solid var(--border-brand);
  overflow: hidden;
}

.result-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.result-value-box {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.result-number {
  font-size: 64px;
  font-weight: 800;
  color: var(--brand-primary);
  font-family: 'DIN Alternate', sans-serif;
}

.result-unit {
  font-size: 20px;
  margin-left: 8px;
  color: var(--brand-primary);
  opacity: 0.8;
}

.scanning-bar {
  position: absolute;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, transparent, var(--btn-secondary-bg), transparent);
  top: -100%;
  animation: scan-vertical 3s linear infinite;
}

/* 强度网格 */
.intensity-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-header .line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-default), transparent);
}

.section-header .text {
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.intensity-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.intensity-item {
  padding: 16px;
  border: 1px solid var(--border-subtle);
}

.item-percent {
  font-size: 12px;
  color: var(--accent-color);
  font-weight: 700;
  margin-bottom: 4px;
}

.item-weight {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 4px;
  font-family: 'DIN Alternate', sans-serif;
  color: var(--text-primary);
}

.item-desc {
  font-size: 10px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.item-progress {
  height: 3px;
  background: var(--border-subtle);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  box-shadow: 0 0 8px var(--accent-color);
}

/* 按钮样式 */
.action-section {
  padding: 0 20px;
}

.tech-btn {
  width: 100%;
  height: 54px;
  background: transparent;
  border: 1px solid var(--brand-primary);
  border-radius: 8px;
  color: var(--brand-primary);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.tech-btn:active {
  background: var(--btn-secondary-bg);
  transform: scale(0.98);
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--btn-secondary-bg), transparent);
  transition: left 0.5s;
}

.tech-btn:hover .btn-glow {
  left: 100%;
}

/* 弹出层 */
:deep(.van-popup) {
  background-color: var(--bg-elevated);
}

:deep(.van-picker) {
  background-color: var(--bg-elevated);
  --van-picker-option-text-color: var(--text-primary);
  --van-picker-title-text-color: var(--brand-primary);
}

:deep(.van-picker__confirm) {
  color: var(--brand-primary);
}

/* 动画 */
@keyframes scan {
  from { transform: translateY(0); }
  to { transform: translateY(100vh); }
}

@keyframes scan-vertical {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

@keyframes float {
  from { transform: translate(0, 0); }
  to { transform: translate(20px, 20px); }
}

@keyframes pulse {
  0% { opacity: 0.5; transform: scale(0.9); }
  100% { opacity: 1; transform: scale(1.1); }
}

.pulse-border {
  animation: pulse-border 2s infinite alternate;
}

@keyframes pulse-border {
  from { border-color: var(--border-default); }
  to { border-color: var(--border-brand); box-shadow: var(--shadow-brand); }
}

.tech-fade-enter-active, .tech-fade-leave-active {
  transition: all 0.5s ease;
}

.tech-fade-enter-from, .tech-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
