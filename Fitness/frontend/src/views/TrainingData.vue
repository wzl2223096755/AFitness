<template>
  <div class="training-data-page p-5">
    <!-- 页面头部 -->
    <div class="page-header mb-6 animate-fade-in-up">
      <h1 class="page-header__title">训练数据</h1>
      <div class="page-header__subtitle">记录每次训练的详细数据，追踪您的进步</div>
    </div>
    
    <!-- 数据录入表单卡片 -->
    <div class="card-unified card-unified--primary mb-6 animate-fade-in-up stagger-1">
      <div class="section-header">
        <h3 class="section-header__title">训练数据录入</h3>
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
            <el-form-item label="训练类型" prop="exerciseType">
              <el-select v-model="trainingForm.exerciseType" placeholder="请选择训练类型" @change="handleExerciseTypeChange">
                <el-option label="上肢" value="上肢" />
                <el-option label="下肢" value="下肢" />
                <el-option label="核心" value="核心" />
                <el-option label="有氧" value="有氧" />
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
        
        <!-- 训练项目复选框 -->
        <el-row :gutter="20" v-if="trainingForm.exerciseType">
          <el-col :span="24">
            <el-form-item label="训练动作" prop="selectedExercises">
              <div class="exercise-checkbox-group">
                <div class="checkbox-group-header">
                  <span class="group-title">选择今日训练动作（可多选）</span>
                  <span class="selected-count">已选 {{ trainingForm.selectedExercises.length }} 项</span>
                </div>
                <el-checkbox-group v-model="trainingForm.selectedExercises" @change="handleExerciseSelect">
                  <el-checkbox 
                    v-for="exercise in currentExerciseOptions" 
                    :key="exercise.value"
                    :label="exercise.value"
                    class="exercise-checkbox"
                  >
                    <span class="exercise-label">{{ exercise.label }}</span>
                    <span class="exercise-desc">{{ exercise.desc }}</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
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
            <button class="btn-unified btn-unified--primary" @click="saveTrainingData" :disabled="saving">
              <el-icon><Check /></el-icon>
              {{ saving ? '保存中...' : '保存数据' }}
            </button>
            <button class="btn-unified btn-unified--secondary" @click="resetForm">
              <el-icon><Refresh /></el-icon>
              重置
            </button>
          </el-col>
        </el-row>
      </el-form>
    </div>
    
    <!-- 最近训练记录 -->
    <div class="card-unified animate-fade-in-up stagger-2">
      <div class="section-header">
        <h3 class="section-header__title">最近训练记录</h3>
        <div class="section-header__actions">
          <el-select v-model="filterOptions.exerciseType" placeholder="筛选训练类型" @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="上肢" value="上肢" />
            <el-option label="下肢" value="下肢" />
            <el-option label="核心" value="核心" />
            <el-option label="有氧" value="有氧" />
            <el-option label="全身" value="全身" />
          </el-select>
          <button class="btn-unified btn-unified--primary btn-unified--sm" @click="refreshRecords">
            <el-icon><Refresh /></el-icon>
            刷新
          </button>
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
        <el-table-column prop="exerciseType" label="训练类型" width="100" />
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <button class="btn-unified btn-unified--secondary btn-unified--sm" @click="editRecord(scope?.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </button>
            <button class="btn-unified btn-unified--danger btn-unified--sm" @click="deleteRecord(scope?.row?.id)">
              <el-icon><Delete /></el-icon>
              删除
            </button>
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
                <el-option label="核心" value="核心" />
                <el-option label="有氧" value="有氧" />
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
        <div class="dialog-footer">
          <button class="btn-unified btn-unified--secondary" @click="handleDialogClose">取消</button>
          <button class="btn-unified btn-unified--primary" @click="updateRecord" :disabled="updating">
            {{ updating ? '保存中...' : '保存修改' }}
          </button>
        </div>
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
  selectedExercises: [], // 新增：选中的训练项目
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

// 训练项目配置 - 按训练类型分类
const exercisesByType = {
  '上肢': [
    { value: '卧推', label: '卧推', desc: '胸部主要训练' },
    { value: '引体向上', label: '引体向上', desc: '背部/二头' },
    { value: '肩推', label: '肩推', desc: '肩部三角肌' },
    { value: '划船', label: '划船', desc: '背部厚度' },
    { value: '高位下拉', label: '高位下拉', desc: '背阔肌' },
    { value: '俯卧撑', label: '俯卧撑', desc: '胸部/三头' },
    { value: '双杠臂屈伸', label: '双杠臂屈伸', desc: '胸部/三头' },
    { value: '杠铃弯举', label: '杠铃弯举', desc: '二头肌' },
    { value: '三头臂屈伸', label: '三头臂屈伸', desc: '三头肌' },
    { value: '哑铃飞鸟', label: '哑铃飞鸟', desc: '胸部拉伸' },
    { value: '侧平举', label: '侧平举', desc: '肩部中束' }
  ],
  '下肢': [
    { value: '深蹲', label: '深蹲', desc: '腿部综合' },
    { value: '硬拉', label: '硬拉', desc: '后链/腿部' },
    { value: '腿举', label: '腿举', desc: '股四头肌' },
    { value: '罗马尼亚硬拉', label: '罗马尼亚硬拉', desc: '腘绳肌' },
    { value: '腿弯举', label: '腿弯举', desc: '腘绳肌' },
    { value: '提踵', label: '提踵', desc: '小腿' },
    { value: '箭步蹲', label: '箭步蹲', desc: '腿部/臀部' },
    { value: '腿屈伸', label: '腿屈伸', desc: '股四头肌' },
    { value: '臀桥', label: '臀桥', desc: '臀大肌' }
  ],
  '核心': [
    { value: '卷腹', label: '卷腹', desc: '腹直肌' },
    { value: '平板支撑', label: '平板支撑', desc: '核心稳定' },
    { value: '腹肌轮', label: '腹肌轮', desc: '核心综合' },
    { value: '俄罗斯转体', label: '俄罗斯转体', desc: '腹斜肌' },
    { value: '悬垂举腿', label: '悬垂举腿', desc: '下腹' },
    { value: '死虫式', label: '死虫式', desc: '核心稳定' },
    { value: '侧平板', label: '侧平板', desc: '腹斜肌' }
  ],
  '有氧': [
    { value: '跑步', label: '跑步', desc: '心肺耐力' },
    { value: '骑行', label: '骑行', desc: '低冲击有氧' },
    { value: '游泳', label: '游泳', desc: '全身有氧' },
    { value: '跳绳', label: '跳绳', desc: '高强度有氧' },
    { value: '椭圆机', label: '椭圆机', desc: '低冲击有氧' },
    { value: '划船机', label: '划船机', desc: '全身有氧' },
    { value: 'HIIT', label: 'HIIT', desc: '高强度间歇' },
    { value: '快走', label: '快走', desc: '低强度有氧' }
  ],
  '全身': [
    { value: '硬拉', label: '硬拉', desc: '全身力量' },
    { value: '深蹲', label: '深蹲', desc: '下肢为主' },
    { value: '卧推', label: '卧推', desc: '上肢推' },
    { value: '引体向上', label: '引体向上', desc: '上肢拉' },
    { value: '波比跳', label: '波比跳', desc: '全身爆发' },
    { value: '壶铃摆荡', label: '壶铃摆荡', desc: '髋部爆发' },
    { value: '农夫行走', label: '农夫行走', desc: '核心/握力' },
    { value: '土耳其起立', label: '土耳其起立', desc: '全身稳定' }
  ]
}

// 当前训练类型对应的训练项目选项
const currentExerciseOptions = computed(() => {
  return exercisesByType[trainingForm.exerciseType] || []
})

// 处理训练类型变化
const handleExerciseTypeChange = () => {
  // 清空已选择的训练项目
  trainingForm.selectedExercises = []
  // 清空动作名称
  trainingForm.exerciseName = ''
}

// 处理训练项目选择
const handleExerciseSelect = (selected) => {
  // 将选中的项目合并为动作名称
  if (selected.length > 0) {
    trainingForm.exerciseName = selected.join('、')
  } else {
    trainingForm.exerciseName = ''
  }
}

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
  trainingForm.selectedExercises = [] // 清空选中的训练项目
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
/* TrainingData - Using unified component styles */
/* Requirements: 5.1, 5.2, 5.3, 7.1, 7.2, 7.3 */

.training-data-page {
  background: var(--bg-page);
  min-height: 100vh;
}

/* Page header using unified styles */
.page-header__title {
  color: var(--text-primary);
  font-size: var(--font-size-2xl, 1.5rem);
  font-weight: var(--font-weight-bold, 700);
  margin-bottom: var(--spacing-2, 0.5rem);
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header__subtitle {
  color: var(--text-secondary);
  font-size: var(--font-size-sm, 0.875rem);
}

/* Form styling */
.training-form {
  max-width: 100%;
}

.form-actions {
  display: flex;
  gap: var(--spacing-3, 0.75rem);
  padding: var(--spacing-3, 0.75rem) 0;
}

/* Exercise suggestions dropdown */
.exercise-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 20px));
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-md, 12px);
  box-shadow: var(--shadow-lg);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-item {
  padding: var(--spacing-3, 0.75rem) var(--spacing-4, 1rem);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--duration-fast, 150ms) var(--ease-out, ease-out);
}

.suggestion-item:hover {
  background-color: var(--hover-bg);
}

.suggestion-item-active {
  background-color: var(--active-bg);
  color: var(--brand-accent);
  font-weight: var(--font-weight-medium, 500);
}

/* Pagination container */
.pagination-container {
  margin-top: var(--spacing-5, 1.25rem);
  display: flex;
  justify-content: flex-end;
}

/* Table styling - Requirements: 7.1, 7.2, 7.3 */
.weight-value {
  font-weight: var(--font-weight-medium, 500);
  font-variant-numeric: tabular-nums;
  color: var(--text-secondary);
}

.highlight-volume {
  font-weight: var(--font-weight-semibold, 600);
  color: var(--color-success);
  font-variant-numeric: tabular-nums;
}

:deep(.el-table .cell) {
  padding: var(--table-cell-padding-y, 0.75rem) var(--table-cell-padding-x, 1rem);
  font-variant-numeric: tabular-nums;
  color: var(--text-secondary);
}

:deep(.el-table th .cell) {
  font-weight: var(--font-weight-semibold, 600);
  color: var(--text-primary);
}

/* Table theme styles */
:deep(.el-table) {
  background-color: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--hover-bg);
  --el-table-header-text-color: var(--text-primary);
  --el-table-text-color: var(--text-secondary);
  --el-table-border-color: var(--border-default);
  --el-table-row-hover-bg-color: var(--hover-bg);
  border-radius: var(--border-radius-md, 12px);
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background-color: var(--hover-bg);
}

:deep(.el-table__body-wrapper) {
  background-color: transparent;
}

:deep(.el-table__empty-text) {
  color: var(--text-tertiary);
}

.table-row-even {
  background-color: var(--hover-bg);
}

.table-row-odd {
  background-color: transparent;
}

/* Dialog footer */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-3, 0.75rem);
}

/* Training checkbox group */
.exercise-checkbox-group {
  padding: var(--spacing-4, 1rem);
  background: var(--hover-bg);
  border-radius: var(--border-radius-md, 12px);
  border: 1px solid var(--glass-border);
}

.checkbox-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4, 1rem);
  padding-bottom: var(--spacing-3, 0.75rem);
  border-bottom: 1px solid var(--border-default);
}

.group-title {
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: var(--font-weight-semibold, 600);
  color: var(--text-primary);
}

.selected-count {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--brand-accent);
  background: rgba(6, 182, 212, 0.1);
  padding: var(--spacing-1, 0.25rem) var(--spacing-3, 0.75rem);
  border-radius: 20px;
}

.exercise-checkbox-group :deep(.el-checkbox-group) {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--spacing-3, 0.75rem);
}

.exercise-checkbox {
  display: flex;
  flex-direction: column;
  padding: var(--spacing-3, 0.75rem) var(--spacing-4, 1rem);
  background: var(--bg-card);
  border-radius: var(--border-radius-sm, 10px);
  border: 1px solid transparent;
  transition: all var(--duration-fast, 150ms) var(--ease-out, ease-out);
  margin-right: 0 !important;
}

.exercise-checkbox:hover {
  background: var(--hover-bg);
  border-color: var(--brand-primary);
  transform: translateY(-2px);
}

:deep(.exercise-checkbox.is-checked) {
  background: var(--active-bg);
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-brand);
}

:deep(.exercise-checkbox .el-checkbox__inner) {
  background: transparent;
  border-color: var(--text-tertiary);
  border-radius: 4px;
  width: 18px;
  height: 18px;
}

:deep(.exercise-checkbox.is-checked .el-checkbox__inner) {
  background: var(--brand-gradient);
  border-color: transparent;
}

.exercise-label {
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: var(--font-weight-semibold, 600);
  color: var(--text-primary);
}

.exercise-desc {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--text-tertiary);
}

/* Pagination theme */
:deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
  transition: all var(--duration-fast, 150ms) var(--ease-out, ease-out);
  background: transparent;
  color: var(--text-secondary);
}

:deep(.el-pagination .el-pager li:hover) {
  background-color: var(--hover-bg);
  color: var(--brand-primary);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--brand-gradient);
  color: white;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .training-data-page {
    padding: var(--spacing-3, 0.75rem);
  }
  
  .page-header__title {
    font-size: var(--font-size-xl, 1.25rem);
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .btn-unified {
    width: 100%;
  }
  
  .el-col[class*="el-col-8"],
  .el-col[class*="el-col-12"],
  .el-col[class*="el-col-24"] {
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
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
  :deep(.el-date-editor),
  :deep(.el-input-number) {
    width: 100% !important;
  }
  
  .exercise-checkbox-group :deep(.el-checkbox-group) {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  
  .pagination-container {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .exercise-checkbox-group :deep(.el-checkbox-group) {
    grid-template-columns: 1fr;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .cell) {
    padding: 8px 6px;
  }
}
</style>