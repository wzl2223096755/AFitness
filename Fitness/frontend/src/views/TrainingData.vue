<template>
  <div class="training-data-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>训练数据</h1>
      <div class="page-description">记录每次训练的详细数据，追踪您的进步</div>
    </div>
    
    <!-- 数据录入表单卡片 -->
    <div class="form-card">
      <div class="card-header">
        <h3>训练数据录入</h3>
      </div>
      <el-form 
        :model="trainingForm" 
        ref="trainingFormRef"
        :rules="formRules"
        label-width="100px"
        class="training-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="动作名称" prop="exerciseName">
              <el-input 
                v-model="trainingForm.exerciseName" 
                placeholder="请输入动作名称"
                @input="handleExerciseNameInput"
                @keydown.down="handleKeyDownDown"
                @keydown.up="handleKeyDownUp"
                @keydown.enter="handleKeyDownEnter"
                ref="exerciseInputRef"
              >
                <template #suffix>
                  <el-button size="small" @click="toggleExerciseSuggestions">
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                </template>
              </el-input>
              <!-- 动作名称建议 -->
              <div 
                v-if="showExerciseSuggestions && filteredExercises.length > 0" 
                class="exercise-suggestions"
                ref="suggestionsRef"
              >
                <div 
                  v-for="(exercise, index) in filteredExercises" 
                  :key="exercise"
                  class="suggestion-item"
                  :class="{ 'suggestion-item-active': selectedSuggestionIndex === index }"
                  @click="selectExercise(exercise)"
                  @mouseenter="selectedSuggestionIndex = index"
                >
                  {{ exercise }}
                </div>
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="动作类型" prop="exerciseType">
              <el-select v-model="trainingForm.exerciseType" placeholder="请选择动作类型">
                <el-option label="上肢" value="上肢" />
                <el-option label="下肢" value="下肢" />
                <el-option label="全身" value="全身" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="训练日期" prop="date">
              <el-date-picker 
                v-model="trainingForm.date" 
                type="date" 
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="重量(kg)" prop="weight">
              <el-input-number 
                v-model="trainingForm.weight" 
                :min="0.1" 
                :step="0.5" 
                placeholder="请输入重量"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="组数" prop="sets">
              <el-input-number 
                v-model="trainingForm.sets" 
                :min="1" 
                :max="20" 
                placeholder="请输入组数"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="次数" prop="reps">
              <el-input-number 
                v-model="trainingForm.reps" 
                :min="1" 
                :max="100" 
                placeholder="请输入次数"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="时长(min)" prop="duration">
              <el-input-number 
                v-model="trainingForm.duration" 
                :min="1" 
                :max="300" 
                placeholder="训练时长"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="疲劳度" prop="fatigueLevel">
              <el-rate v-model="trainingForm.fatigueLevel" />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="训练量" prop="trainingVolume" class="training-volume">
              <el-input 
                v-model="trainingForm.trainingVolume" 
                placeholder="自动计算" 
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注" prop="notes">
              <el-input 
                v-model="trainingForm.notes" 
                type="textarea" 
                :rows="3" 
                placeholder="添加备注信息（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="24" class="form-actions">
            <el-button type="primary" @click="saveTrainingData" :loading="saving">
              <el-icon><Check /></el-icon>
              保存数据
            </el-button>
            <el-button @click="resetForm">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>
    
    <!-- 最近训练记录 -->
    <div class="recent-records">
      <div class="card-header">
        <h3>最近训练记录</h3>
        <div class="record-actions">
          <el-select v-model="filterOptions.exerciseType" placeholder="筛选动作类型" @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="上肢" value="上肢" />
            <el-option label="下肢" value="下肢" />
            <el-option label="全身" value="全身" />
          </el-select>
          <el-button type="primary" size="small" @click="refreshRecords">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table 
        :data="filteredRecords" 
        style="width: 100%"
        :row-class-name="tableRowClassName"
        @sort-change="handleSort"
        v-loading="loadingRecords"
        element-loading-text="加载中..."
        empty-text="暂无训练记录"
      >
        <el-table-column prop="date" label="日期" width="120" sortable="custom" />
        <el-table-column prop="exerciseName" label="动作名称" width="150" />
        <el-table-column prop="exerciseType" label="动作类型" width="100" />
        <el-table-column prop="weight" label="重量(kg)" width="100" sortable="custom">
          <template #default="scope">
            <span class="weight-value">{{ scope?.row?.weight || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sets" label="组数" width="80" sortable="custom" />
        <el-table-column prop="reps" label="次数" width="80" sortable="custom" />
        <el-table-column prop="trainingVolume" label="训练量" width="100" sortable="custom">
          <template #default="scope">
            <span :class="{ 'highlight-volume': (scope?.row?.trainingVolume || 0) > 3000 }">
              {{ scope?.row?.trainingVolume || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(min)" width="100" sortable="custom" />
        <el-table-column prop="fatigueLevel" label="疲劳度" width="100">
          <template #default="scope">
            <el-rate v-model="scope.row.fatigueLevel" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editRecord(scope?.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteRecord(scope?.row?.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredRecords.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑训练记录"
      width="60%"
      :before-close="handleDialogClose"
    >
      <el-form 
        :model="editForm" 
        ref="editFormRef"
        :rules="formRules"
        label-width="100px"
      >
        <!-- 编辑表单内容，与主表单相同 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="动作名称" prop="exerciseName">
              <el-input v-model="editForm.exerciseName" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="动作类型" prop="exerciseType">
              <el-select v-model="editForm.exerciseType" placeholder="请选择动作类型">
                <el-option label="上肢" value="上肢" />
                <el-option label="下肢" value="下肢" />
                <el-option label="全身" value="全身" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练日期" prop="date">
              <el-date-picker 
                v-model="editForm.date" 
                type="date" 
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="时长(min)" prop="duration">
              <el-input-number 
                v-model="editForm.duration" 
                :min="1" 
                :max="300"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="重量(kg)" prop="weight">
              <el-input-number 
                v-model="editForm.weight" 
                :min="0.1" 
                :step="0.5"
                @change="updateEditVolume"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="组数" prop="sets">
              <el-input-number 
                v-model="editForm.sets" 
                :min="1" 
                :max="20"
                @change="updateEditVolume"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="次数" prop="reps">
              <el-input-number 
                v-model="editForm.reps" 
                :min="1" 
                :max="100"
                @change="updateEditVolume"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="疲劳度" prop="fatigueLevel">
              <el-rate v-model="editForm.fatigueLevel" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="训练量" prop="trainingVolume">
              <el-input v-model="editForm.trainingVolume" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="notes">
              <el-input 
                v-model="editForm.notes" 
                type="textarea" 
                :rows="3"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="updateRecord" :loading="updating">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from '../utils/message.js'
import { Refresh, Check, ArrowDown } from '@element-plus/icons-vue'

import { useFitnessStore } from '../stores/fitness'

// 防抖函数
const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 状态管理
const fitnessStore = useFitnessStore()

// 响应式数据
const trainingFormRef = ref(null)
const editFormRef = ref(null)
const saving = ref(false)
const updating = ref(false)
const loadingRecords = ref(false)
const dialogVisible = ref(false)
const showExerciseSuggestions = ref(false)
const selectedRecordId = ref(null)
const selectedSuggestionIndex = ref(-1)
const exerciseInputRef = ref(null)
const suggestionsRef = ref(null)

// 表单数据
const trainingForm = reactive({
  exerciseName: '',
  exerciseType: '',
  date: new Date().toISOString().split('T')[0],
  weight: null,
  sets: null,
  reps: null,
  trainingVolume: null,
  duration: null,
  fatigueLevel: 3,
  notes: ''
})

// 编辑表单数据
const editForm = reactive({
  id: null,
  exerciseName: '',
  exerciseType: '',
  date: '',
  weight: null,
  sets: null,
  reps: null,
  trainingVolume: null,
  duration: null,
  fatigueLevel: 3,
  notes: ''
})

// 分页和筛选
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const filterOptions = reactive({
  exerciseType: ''
})

// 排序
const sortField = ref('')
const sortOrder = ref('')

// 动作名称建议列表
const exerciseSuggestions = [
  '卧推', '深蹲', '硬拉', '引体向上', '肩推', '划船', 
  '高位下拉', '腿举', '罗马尼亚硬拉', '俯卧撑', '双杠臂屈伸',
  '腹肌轮', '杠铃弯举', '三头臂屈伸', '腿弯举', '提踵',
  '哑铃飞鸟', '侧平举', '卷腹', '平板支撑'
]

// 计算属性
const filteredExercises = computed(() => {
  const input = trainingForm.exerciseName.toLowerCase()
  return exerciseSuggestions.filter(exercise => 
    exercise.toLowerCase().includes(input)
  )
})

// 表单验证规则
const formRules = reactive({
  exerciseName: [
    { required: true, message: '请输入动作名称', trigger: 'blur' }
  ],
  exerciseType: [
    { required: true, message: '请选择动作类型', trigger: 'change' }
  ],
  date: [
    { required: true, message: '请选择训练日期', trigger: 'change' }
  ],
  weight: [
    { required: true, message: '请输入重量', trigger: 'change' },
    { type: 'number', min: 0.1, message: '重量必须大于0', trigger: 'change' }
  ],
  sets: [
    { required: true, message: '请输入组数', trigger: 'change' },
    { type: 'number', min: 1, max: 20, message: '组数必须在1-20之间', trigger: 'change' }
  ],
  reps: [
    { required: true, message: '请输入次数', trigger: 'change' },
    { type: 'number', min: 1, max: 100, message: '次数必须在1-100之间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入训练时长', trigger: 'change' },
    { type: 'number', min: 1, max: 300, message: '时长必须在1-300分钟之间', trigger: 'change' }
  ],
  fatigueLevel: [
    { required: true, message: '请选择疲劳度', trigger: 'change' }
  ]
})



// 获取训练记录数据
const trainingRecords = computed(() => {
  return fitnessStore.fitnessData || []
})

// 筛选后的记录
const filteredRecords = computed(() => {
  let records = [...trainingRecords.value]
  
  // 按动作类型筛选
  if (filterOptions.exerciseType) {
    records = records.filter(record => record.exerciseType === filterOptions.exerciseType)
  }
  
  // 排序
  if (sortField.value && sortOrder.value) {
    records.sort((a, b) => {
      if (sortOrder.value === 'ascending') {
        return a[sortField.value] > b[sortField.value] ? 1 : -1
      } else {
        return a[sortField.value] < b[sortField.value] ? 1 : -1
      }
    })
  } else {
    // 默认按日期降序排序
    records.sort((a, b) => new Date(b.date || b.timestamp) - new Date(a.date || a.timestamp))
  }
  
  return records
})

// 分页数据
const paginatedRecords = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredRecords.value.slice(start, end)
})

// 方法
const calculateTrainingVolume = () => {
  if (trainingForm.weight && trainingForm.sets && trainingForm.reps) {
    trainingForm.trainingVolume = trainingForm.weight * trainingForm.sets * trainingForm.reps
  }
}

const updateEditVolume = () => {
  if (editForm.weight && editForm.sets && editForm.reps) {
    editForm.trainingVolume = editForm.weight * editForm.sets * editForm.reps
  }
}

// 动作名称输入处理（带防抖）
const handleExerciseNameInput = debounce(() => {
  // 显示建议列表
  showExerciseSuggestions.value = true
  selectedSuggestionIndex.value = -1
}, 300)

// 切换动作建议列表
const toggleExerciseSuggestions = () => {
  showExerciseSuggestions.value = !showExerciseSuggestions.value
  if (showExerciseSuggestions.value) {
    selectedSuggestionIndex.value = -1
  }
}

const selectExercise = (exercise) => {
  trainingForm.exerciseName = exercise
  showExerciseSuggestions.value = false
  selectedSuggestionIndex.value = -1
  
  // 根据动作名称自动设置动作类型
  const upperBodyExercises = ['卧推', '引体向上', '肩推', '划船', '高位下拉', '俯卧撑', '双杠臂屈伸', '杠铃弯举', '三头臂屈伸', '哑铃飞鸟', '侧平举', '卷腹', '腹肌轮', '平板支撑']
  const lowerBodyExercises = ['深蹲', '腿举', '罗马尼亚硬拉', '腿弯举', '提踵']
  
  if (upperBodyExercises.includes(exercise)) {
    trainingForm.exerciseType = '上肢'
  } else if (lowerBodyExercises.includes(exercise)) {
    trainingForm.exerciseType = '下肢'
  } else {
    trainingForm.exerciseType = '全身'
  }
}

// 键盘导航 - 向下键
const handleKeyDownDown = () => {
  if (!showExerciseSuggestions.value) return
  
  selectedSuggestionIndex.value++
  if (selectedSuggestionIndex.value >= filteredExercises.value.length) {
    selectedSuggestionIndex.value = 0
  }
  
  scrollToSelected()
}

// 键盘导航 - 向上键
const handleKeyDownUp = () => {
  if (!showExerciseSuggestions.value) return
  
  selectedSuggestionIndex.value--
  if (selectedSuggestionIndex.value < 0) {
    selectedSuggestionIndex.value = filteredExercises.value.length - 1
  }
  
  scrollToSelected()
}

// 键盘导航 - 回车键
const handleKeyDownEnter = () => {
  if (!showExerciseSuggestions.value) return
  
  if (selectedSuggestionIndex.value >= 0 && selectedSuggestionIndex.value < filteredExercises.value.length) {
    selectExercise(filteredExercises.value[selectedSuggestionIndex.value])
  } else {
    showExerciseSuggestions.value = false
  }
}

// 滚动到选中的建议项
const scrollToSelected = () => {
  nextTick(() => {
    if (!suggestionsRef.value) return
    
    const items = suggestionsRef.value.querySelectorAll('.suggestion-item')
    if (items[selectedSuggestionIndex.value]) {
      items[selectedSuggestionIndex.value].scrollIntoView({
        behavior: 'smooth',
        block: 'nearest'
      })
    }
  })
}

const saveTrainingData = async () => {
  try {
    await trainingFormRef.value.validate()
    saving.value = true
    
    // 计算训练量
    calculateTrainingVolume()
    
    // 准备保存的数据 - 字段映射到后端API期望的格式
    const dataToSave = {
      exerciseName: trainingForm.exerciseName,
      exerciseType: trainingForm.exerciseType,
      weight: trainingForm.weight,
      sets: trainingForm.sets,
      reps: trainingForm.reps,
      timestamp: trainingForm.date ? new Date(trainingForm.date).toISOString() : new Date().toISOString(),
      trainingVolume: trainingForm.trainingVolume,
      perceivedExertion: trainingForm.fatigueLevel
    }
    
    // 保存到store
    await fitnessStore.addFitnessData(dataToSave)
    
    ElMessage.success('训练数据保存成功！')
    resetForm()
    
    // 刷新记录列表
    await fetchTrainingRecords()
  } catch (error) {
    console.error('保存训练数据失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  if (trainingFormRef.value) {
    trainingFormRef.value.resetFields()
  }
  trainingForm.date = new Date().toISOString().split('T')[0]
  trainingForm.fatigueLevel = 3
  showExerciseSuggestions.value = false
}

const fetchTrainingRecords = async () => {
  try {
    loadingRecords.value = true
    // 从store获取数据
    await fitnessStore.fetchMyFitnessData()
  } catch (error) {
    console.error('获取训练记录失败:', error)
  } finally {
    loadingRecords.value = false
  }
}

const handleFilterChange = () => {
  pagination.currentPage = 1
}

const handleSort = ({ prop, order }) => {
  sortField.value = prop
  sortOrder.value = order
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

const refreshRecords = async () => {
  await fetchTrainingRecords()
  ElMessage.success('记录已刷新')
}

const editRecord = (record) => {
  // 检查record是否存在
  if (!record) {
    ElMessage.warning('无法编辑记录：记录数据不存在')
    return
  }
  
  // 填充编辑表单 - 处理字段映射
  Object.assign(editForm, {
    id: record.id,
    exerciseName: record.exerciseName || '',
    exerciseType: record.exerciseType || '',
    date: record.timestamp ? new Date(record.timestamp).toISOString().split('T')[0] : '',
    weight: record.weight || 0,
    sets: record.sets || 1,
    reps: record.reps || 1,
    trainingVolume: record.trainingVolume || 0,
    duration: record.duration || 0,
    fatigueLevel: record.perceivedExertion || 3,
    notes: record.notes || ''
  })
  selectedRecordId.value = record.id
  dialogVisible.value = true
}

const updateRecord = async () => {
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    // 更新训练量
    updateEditVolume()
    
    // 准备更新数据 - 字段映射到后端API期望的格式
    const dataToUpdate = {
      id: editForm.id,
      exerciseName: editForm.exerciseName,
      exerciseType: editForm.exerciseType,
      weight: editForm.weight,
      sets: editForm.sets,
      reps: editForm.reps,
      timestamp: editForm.date ? new Date(editForm.date).toISOString() : new Date().toISOString(),
      trainingVolume: editForm.trainingVolume,
      perceivedExertion: editForm.fatigueLevel
    }
    
    // 更新数据
    await fitnessStore.updateFitnessData(dataToUpdate.id, dataToUpdate)
    
    ElMessage.success('记录更新成功！')
    dialogVisible.value = false
    await fetchTrainingRecords()
  } catch (error) {
    console.error('更新记录失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

const deleteRecord = async (id) => {
  // 检查id是否存在
  if (!id) {
    ElMessage.warning('无法删除记录：记录ID不存在')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？此操作不可撤销。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await fitnessStore.deleteFitnessData(id)
    ElMessage.success('记录已删除')
    await fetchTrainingRecords()
  } catch (error) {
    // 用户取消删除不算错误
    if (error !== 'cancel') {
      console.error('删除记录失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
  selectedRecordId.value = null
}

const tableRowClassName = ({ row, rowIndex }) => {
  // 为特定行添加样式
  return rowIndex % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

// 监听表单变化，自动计算训练量
watch(
  () => [trainingForm.weight, trainingForm.sets, trainingForm.reps],
  () => calculateTrainingVolume()
)

// 点击页面其他区域关闭动作建议
const handleClickOutside = (event) => {
  if (exerciseInputRef.value && suggestionsRef.value && 
      !exerciseInputRef.value.$el.contains(event.target) && 
      !suggestionsRef.value.contains(event.target)) {
    showExerciseSuggestions.value = false
    selectedSuggestionIndex.value = -1
  }
}

// 生命周期
onMounted(async () => {
  // 加载训练记录
  await fetchTrainingRecords()
  
  // 添加点击外部关闭建议的事件监听
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  // 移除点击外部关闭建议列表的事件监听
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.training-data-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-text-color);
}

.page-description {
  font-size: 14px;
  color: var(--secondary-text-color);
  margin-bottom: 0;
}

.form-card {
  background: var(--white);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow);
  padding: 24px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--primary-text-color);
}

.record-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.training-form {
  max-width: 100%;
}

.form-actions {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.exercise-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: var(--white);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-lg);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-item {
  padding: 10px 15px;
  cursor: pointer;
  transition: background-color var(--transition-time);
}

.suggestion-item:hover {
  background-color: var(--hover-bg-color);
}

.suggestion-item-active {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
  font-weight: 500;
}

.recent-records {
  background: var(--white);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow);
  padding: 24px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.weight-value {
  font-weight: 500;
}

.highlight-volume {
  font-weight: 600;
  color: var(--success-color);
}

.table-row-even {
  background-color: var(--table-even-row-bg);
}

.table-row-odd {
  background-color: var(--white);
}

.training-volume .el-input__wrapper {
  background-color: var(--disabled-bg-color);
}

/* 响应式调整 */
@media (max-width: 992px) {
  .el-row {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .el-col {
    padding-left: 8px !important;
    padding-right: 8px !important;
  }
}

@media (max-width: 768px) {
  .training-data-page {
    padding: 12px;
  }
  
  .page-header {
    margin-bottom: 16px;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
  
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .record-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .record-actions .el-select {
    flex: 1;
    min-width: 120px;
  }
  
  .form-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .form-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
  
  .pagination-container {
    justify-content: center;
  }
  
  /* 表单列响应式 */
  .el-col {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
  
  .el-col[class*="el-col-8"],
  .el-col[class*="el-col-12"],
  .el-col[class*="el-col-24"] {
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
  
  /* 表单标签左对齐 */
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
  
  /* 输入框全宽 */
  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-date-editor),
  :deep(.el-input-number) {
    width: 100% !important;
  }
  
  /* 表格操作按钮 */
  :deep(.el-table .el-button) {
    padding: 6px 10px;
    font-size: 12px;
  }
  
  /* 建议列表优化 */
  .exercise-suggestions {
    max-height: 180px;
  }
  
  .suggestion-item {
    padding: 12px 16px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .training-data-page {
    padding: 10px;
  }
  
  .form-card,
  .recent-records {
    padding: 16px;
    border-radius: 12px;
  }
  
  .page-header h1 {
    font-size: 18px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  /* 表格紧凑模式 */
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .cell) {
    padding: 8px 6px;
  }
  
  /* 隐藏次要列 */
  :deep(.el-table-column--duration),
  :deep(.el-table [data-column="duration"]) {
    display: none;
  }
}

/* 按钮样式优化 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  border: none;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #059669 0%, #2563eb 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.3);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

:deep(.el-button--default) {
  background: white;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--default:hover) {
  background: #f9fafb;
  border-color: #10b981;
  color: #10b981;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.1);
}

:deep(.el-button--small) {
  padding: 8px 16px;
  font-size: 14px;
}

:deep(.el-button.is-loading) {
  opacity: 0.8;
}

/* 表单输入框样式优化 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

/* 数字输入框样式 */
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 8px;
}

/* 评分组件样式 */
:deep(.el-rate) {
  height: 24px;
}

:deep(.el-rate__item) {
  margin-right: 4px;
}

:deep(.el-rate__icon) {
  font-size: 18px;
  transition: all 0.3s ease;
}

:deep(.el-rate__icon:hover) {
  transform: scale(1.1);
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

:deep(.el-table th) {
  background: transparent;
  color: #475569;
  font-weight: 600;
  border-bottom: 2px solid #e2e8f0;
}

:deep(.el-table tr:hover > td) {
  background-color: rgba(16, 185, 129, 0.05);
}

:deep(.el-table td) {
  border-bottom: 1px solid #f1f5f9;
}

/* 分页器样式 */
:deep(.el-pagination) {
  justify-content: flex-end;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

:deep(.el-pagination .el-pager li:hover) {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  color: white;
}

:deep(.el-pagination button:hover) {
  color: #10b981;
}

/* 选择器样式 */
:deep(.el-select-dropdown) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-select-dropdown__item:hover) {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

:deep(.el-select-dropdown__item.selected) {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
  font-weight: 500;
}
</style>