<template>
  <div class="nutrition-page p-5">
    <!-- 页面头部 -->
    <div class="page-header mb-8 animate-fade-in-up">
      <h1 class="page-header__title">营养追踪</h1>
      <div class="page-header__subtitle">记录每日饮食，监控营养摄入，助力健身目标</div>
    </div>
    
    <!-- 营养概览卡片 -->
    <div class="overview-cards mb-8">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="card-unified card-unified--sm card-unified--interactive overview-card calories animate-fade-in-up stagger-1">
            <div class="card-icon icon-container icon-container--md icon-container--danger">
              <el-icon size="24"><Odometer /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">今日卡路里</div>
              <div class="card-value">{{ todayCalories }}</div>
              <div class="card-unit">千卡</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="card-unified card-unified--sm card-unified--interactive overview-card protein animate-fade-in-up stagger-2">
            <div class="card-icon icon-container icon-container--md icon-container--success">
              <el-icon size="24"><KnifeFork /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">蛋白质</div>
              <div class="card-value">{{ todayProtein }}</div>
              <div class="card-unit">克</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="card-unified card-unified--sm card-unified--interactive overview-card carbs animate-fade-in-up stagger-3">
            <div class="card-icon icon-container icon-container--md icon-container--warning">
              <el-icon size="24"><Coffee /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">碳水化合物</div>
              <div class="card-value">{{ todayCarbs }}</div>
              <div class="card-unit">克</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="card-unified card-unified--sm card-unified--interactive overview-card fat animate-fade-in-up stagger-4">
            <div class="card-icon icon-container icon-container--md icon-container--primary">
              <el-icon size="24"><IceTea /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">脂肪</div>
              <div class="card-value">{{ todayFat }}</div>
              <div class="card-unit">克</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 营养摄入分布图表 -->
    <div class="card-unified mb-8 animate-fade-in-up stagger-5">
      <div class="section-header">
        <h3 class="section-header__title">今日营养摄入分布</h3>
        <div class="section-header__actions">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            @change="loadNutritionData"
          />
        </div>
      </div>
      <div class="chart-container">
        <v-chart class="nutrition-chart" :option="nutritionChartOption" />
      </div>
    </div>
    
    <!-- 饮食记录表单 -->
    <div class="card-unified card-unified--primary mb-8 animate-fade-in-up stagger-6">
      <div class="section-header">
        <h3 class="section-header__title">添加饮食记录</h3>
      </div>
      <el-form 
        :model="nutritionForm" 
        ref="nutritionFormRef"
        :rules="formRules"
        label-width="100px"
        class="nutrition-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="餐次" prop="mealType">
              <el-select v-model="nutritionForm.mealType" placeholder="请选择餐次">
                <el-option label="早餐" value="早餐" />
                <el-option label="午餐" value="午餐" />
                <el-option label="晚餐" value="晚餐" />
                <el-option label="加餐" value="加餐" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="食物名称" prop="foodName">
              <el-input 
                v-model="nutritionForm.foodName" 
                placeholder="请输入食物名称"
                @input="handleFoodNameInput"
              >
                <template #suffix>
                  <el-button size="small" @click="toggleFoodSuggestions">
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                </template>
              </el-input>
              <!-- 食物名称建议 -->
              <div 
                v-if="showFoodSuggestions && filteredFoods.length > 0" 
                class="food-suggestions"
                ref="foodSuggestionsRef"
              >
                <div 
                  v-for="(food, index) in filteredFoods" 
                  :key="food.name"
                  class="suggestion-item"
                  :class="{ 'suggestion-item-active': selectedFoodIndex === index }"
                  @click="selectFood(food)"
                  @mouseenter="selectedFoodIndex = index"
                >
                  <div class="food-name">{{ food.name }}</div>
                  <div class="food-calories">{{ food.calories }}千卡/100g</div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="份量(g)" prop="amount">
              <el-input-number 
                v-model="nutritionForm.amount" 
                :min="1" 
                :max="1000" 
                placeholder="请输入份量"
                @change="calculateNutrition"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="卡路里" prop="calories">
              <el-input-number 
                v-model="nutritionForm.calories" 
                :min="0" 
                :step="0.1"
                placeholder="自动计算"
                readonly
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="蛋白质(g)" prop="protein">
              <el-input-number 
                v-model="nutritionForm.protein" 
                :min="0" 
                :step="0.1"
                placeholder="自动计算"
                readonly
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="碳水(g)" prop="carbs">
              <el-input-number 
                v-model="nutritionForm.carbs" 
                :min="0" 
                :step="0.1"
                placeholder="自动计算"
                readonly
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="脂肪(g)" prop="fat">
              <el-input-number 
                v-model="nutritionForm.fat" 
                :min="0" 
                :step="0.1"
                placeholder="自动计算"
                readonly
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="备注" prop="notes">
          <el-input 
            v-model="nutritionForm.notes" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        
        <el-form-item>
          <button class="btn-unified btn-unified--primary" @click="submitNutritionRecord" :disabled="submitting">
            <el-icon><Plus /></el-icon>
            {{ submitting ? '添加中...' : '添加记录' }}
          </button>
          <button class="btn-unified btn-unified--secondary" @click="resetForm">重置</button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 今日饮食记录 -->
    <div class="card-unified animate-fade-in-up stagger-7">
      <div class="section-header">
        <h3 class="section-header__title">今日饮食记录</h3>
        <div class="section-header__actions">
          <button class="btn-unified btn-unified--primary btn-unified--sm" @click="exportNutritionData">
            <el-icon><Download /></el-icon>
            导出数据
          </button>
        </div>
      </div>
      
      <el-table :data="nutritionRecords" style="width: 100%" stripe>
        <el-table-column prop="mealType" label="餐次" width="100" />
        <el-table-column prop="foodName" label="食物名称" />
        <el-table-column prop="amount" label="份量(g)" width="100" />
        <el-table-column prop="calories" label="卡路里" width="100" />
        <el-table-column prop="protein" label="蛋白质(g)" width="100" />
        <el-table-column prop="carbs" label="碳水(g)" width="100" />
        <el-table-column prop="fat" label="脂肪(g)" width="100" />
        <el-table-column prop="notes" label="备注" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <button class="btn-unified btn-unified--danger btn-unified--sm" @click="deleteRecord(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Odometer, KnifeFork, Coffee, IceTea, ArrowDown, Plus, Download, Delete 
} from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { nutritionApi } from '../api'

use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

// 响应式数据
const nutritionFormRef = ref()
const foodSuggestionsRef = ref()
const submitting = ref(false)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const showFoodSuggestions = ref(false)
const selectedFoodIndex = ref(-1)

// 表单数据
const nutritionForm = reactive({
  mealType: '',
  foodName: '',
  amount: 100,
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0,
  notes: ''
})

// 表单验证规则
const formRules = {
  mealType: [
    { required: true, message: '请选择餐次', trigger: 'change' }
  ],
  foodName: [
    { required: true, message: '请输入食物名称', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入份量', trigger: 'blur' }
  ]
}

// 食物数据库
const foodDatabase = ref([
  { name: '鸡胸肉', calories: 165, protein: 31, carbs: 0, fat: 3.6 },
  { name: '牛肉', calories: 250, protein: 26, carbs: 0, fat: 15 },
  { name: '鸡蛋', calories: 155, protein: 13, carbs: 1.1, fat: 11 },
  { name: '米饭', calories: 130, protein: 2.7, carbs: 28, fat: 0.3 },
  { name: '燕麦', calories: 389, protein: 16.9, carbs: 66, fat: 6.9 },
  { name: '香蕉', calories: 89, protein: 1.1, carbs: 23, fat: 0.3 },
  { name: '苹果', calories: 52, protein: 0.3, carbs: 14, fat: 0.2 },
  { name: '西兰花', calories: 34, protein: 2.8, carbs: 7, fat: 0.4 },
  { name: '牛奶', calories: 42, protein: 3.4, carbs: 5, fat: 1 },
  { name: '酸奶', calories: 59, protein: 10, carbs: 3.6, fat: 0.4 }
])

// 营养记录
const nutritionRecords = ref([])

// 计算属性
const todayCalories = computed(() => {
  return nutritionRecords.value.reduce((sum, record) => sum + record.calories, 0)
})

const todayProtein = computed(() => {
  return nutritionRecords.value.reduce((sum, record) => sum + record.protein, 0)
})

const todayCarbs = computed(() => {
  return nutritionRecords.value.reduce((sum, record) => sum + record.carbs, 0)
})

const todayFat = computed(() => {
  return nutritionRecords.value.reduce((sum, record) => sum + record.fat, 0)
})

const filteredFoods = computed(() => {
  if (!nutritionForm.foodName) return []
  return foodDatabase.value.filter(food => 
    food.name.toLowerCase().includes(nutritionForm.foodName.toLowerCase())
  )
})

const nutritionChartOption = computed(() => {
  return {
    title: {
      text: '营养素摄入比例',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}g ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '营养素',
        type: 'pie',
        radius: '50%',
        data: [
          { value: todayProtein.value, name: '蛋白质' },
          { value: todayCarbs.value, name: '碳水化合物' },
          { value: todayFat.value, name: '脂肪' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

// 方法
const handleFoodNameInput = () => {
  showFoodSuggestions.value = nutritionForm.foodName.length > 0
  selectedFoodIndex.value = -1
}

const toggleFoodSuggestions = () => {
  showFoodSuggestions.value = !showFoodSuggestions.value
}

const selectFood = (food) => {
  nutritionForm.foodName = food.name
  calculateNutrition()
  showFoodSuggestions.value = false
}

const calculateNutrition = () => {
  const food = foodDatabase.value.find(f => f.name === nutritionForm.foodName)
  if (food && nutritionForm.amount) {
    const ratio = nutritionForm.amount / 100
    nutritionForm.calories = Math.round(food.calories * ratio)
    nutritionForm.protein = Math.round(food.protein * ratio * 10) / 10
    nutritionForm.carbs = Math.round(food.carbs * ratio * 10) / 10
    nutritionForm.fat = Math.round(food.fat * ratio * 10) / 10
  }
}

const submitNutritionRecord = async () => {
  try {
    await nutritionFormRef.value.validate()
    submitting.value = true
    
    const record = {
      ...nutritionForm,
      recordDate: selectedDate.value
    }
    
    await nutritionApi.addNutritionRecord(record)
    ElMessage.success('饮食记录添加成功')
    resetForm()
    loadNutritionData()
  } catch (error) {
    console.error('添加饮食记录失败:', error)
    ElMessage.error('添加饮食记录失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  nutritionFormRef.value?.resetFields()
  showFoodSuggestions.value = false
  selectedFoodIndex.value = -1
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await nutritionApi.deleteNutritionRecord(record.id)
    ElMessage.success('记录删除成功')
    loadNutritionData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除记录失败:', error)
      ElMessage.error('删除记录失败')
    }
  }
}

const loadNutritionData = async () => {
  try {
    const response = await nutritionApi.getNutritionRecordsByDate(selectedDate.value)
    if (response.success) {
      nutritionRecords.value = response.data
    }
  } catch (error) {
    console.error('加载营养数据失败:', error)
    ElMessage.error('加载营养数据失败')
  }
}

const exportNutritionData = () => {
  // 实现数据导出功能
  ElMessage.info('导出功能开发中...')
}

// 生命周期
onMounted(() => {
  loadNutritionData()
})
</script>

<style scoped>
/* NutritionTracking - Using unified component styles */
/* Requirements: 5.1, 5.2, 5.3, 7.1, 7.2, 7.3 */

.nutrition-page {
  background: linear-gradient(135deg, var(--bg-primary, #0a0a14) 0%, var(--bg-secondary, #121225) 100%);
  min-height: calc(100vh - 60px);
}

/* Page header using unified styles */
.page-header {
  text-align: center;
}

.page-header__title {
  color: var(--text-primary);
  font-size: var(--font-size-3xl, 1.875rem);
  font-weight: var(--font-weight-extrabold, 800);
  margin-bottom: var(--spacing-3, 0.75rem);
}

.page-header__subtitle {
  color: var(--text-secondary);
  font-size: var(--font-size-lg, 1.125rem);
  opacity: 0.9;
}

/* Overview cards layout */
.overview-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-4, 1rem);
}

.card-content {
  flex: 1;
}

.card-title {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm, 0.875rem);
  margin-bottom: var(--spacing-1, 0.25rem);
}

.card-value {
  font-size: var(--font-size-2xl, 1.5rem);
  font-weight: var(--font-weight-bold, 700);
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
  line-height: var(--line-height-tight, 1.25);
}

.card-unit {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--text-tertiary);
  margin-top: var(--spacing-1, 0.25rem);
}

/* Chart container */
.chart-container {
  height: 300px;
}

.nutrition-chart {
  height: 100%;
  width: 100%;
}

/* Form styling */
.nutrition-form {
  padding: 0 var(--spacing-2, 0.5rem);
}

/* Food suggestions dropdown */
.food-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--glass-bg, rgba(15, 15, 35, 0.95));
  backdrop-filter: blur(var(--glass-blur, 20px));
  border-radius: var(--border-radius-md, 12px);
  border: 1px solid var(--glass-border, rgba(99, 102, 241, 0.2));
  box-shadow: var(--shadow-lg, 0 0 20px rgba(128, 32, 255, 0.3));
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.suggestion-item {
  padding: var(--spacing-3, 0.75rem) var(--spacing-4, 1rem);
  cursor: pointer;
  transition: background-color var(--duration-fast, 150ms) var(--ease-out, ease-out);
  border-bottom: 1px solid var(--glass-border, rgba(99, 102, 241, 0.1));
  color: var(--text-secondary);
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover,
.suggestion-item-active {
  background-color: var(--hover-bg);
}

.food-name {
  font-weight: var(--font-weight-medium, 500);
  color: var(--text-primary);
}

.food-calories {
  font-size: var(--font-size-sm, 0.875rem);
  color: var(--text-tertiary);
}

/* Table styling - Requirements: 7.1, 7.2, 7.3 */
:deep(.el-table .cell) {
  padding: var(--table-cell-padding-y, 0.75rem) var(--table-cell-padding-x, 1rem);
  font-variant-numeric: tabular-nums;
}

:deep(.el-table th .cell) {
  font-weight: var(--font-weight-semibold, 600);
  color: var(--text-primary);
}

:deep(.el-table) {
  background-color: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(124, 58, 237, 0.08);
  --el-table-header-text-color: var(--text-primary);
  --el-table-text-color: var(--text-secondary);
  --el-table-border-color: var(--border-default);
  --el-table-row-hover-bg-color: rgba(124, 58, 237, 0.06);
  border-radius: var(--border-radius-md, 12px);
  overflow: hidden;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .nutrition-page {
    padding: var(--spacing-3, 0.75rem);
  }
  
  .page-header__title {
    font-size: var(--font-size-xl, 1.25rem);
  }
  
  .page-header__subtitle {
    font-size: var(--font-size-base, 1rem);
  }
  
  /* Overview cards responsive */
  .overview-cards .el-row {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-3, 0.75rem);
  }
  
  .overview-cards .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: none !important;
    padding: 0 !important;
  }
  
  .card-value {
    font-size: var(--font-size-xl, 1.25rem);
  }
  
  .chart-container {
    height: 250px;
  }
  
  /* Form responsive */
  .nutrition-form .el-row {
    display: flex;
    flex-direction: column;
    gap: 0;
  }
  
  .nutrition-form .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: none !important;
    padding: 0 !important;
  }
  
  :deep(.el-form-item__label) {
    text-align: left !important;
    float: none !important;
    display: block !important;
    width: 100% !important;
    padding-bottom: 8px;
  }
  
  :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
  
  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-input-number) {
    width: 100% !important;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3, 0.75rem);
  }
  
  .section-header__actions {
    width: 100%;
  }
  
  .section-header__actions .btn-unified {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .nutrition-page {
    padding: var(--spacing-2, 0.5rem);
    min-height: calc(100vh - 50px);
  }
  
  .page-header__title {
    font-size: var(--font-size-lg, 1.125rem);
  }
  
  .card-value {
    font-size: var(--font-size-lg, 1.125rem);
  }
  
  .chart-container {
    height: 220px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .cell) {
    padding: 8px 6px;
  }
}
</style>
