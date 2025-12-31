<template>
  <div class="fitness-planner">
    <div class="section-header">
      <h2>健身计划制定</h2>
      <div class="section-actions">
        <el-button type="primary" @click="savePlan" :loading="saving">
          <el-icon><Check /></el-icon>
          保存计划
        </el-button>
        <el-button @click="previewPlan">
          <el-icon><View /></el-icon>
          预览计划
        </el-button>
      </div>
    </div>

    <div class="planner-container">
      <!-- 左侧：计划配置 -->
      <div class="planner-sidebar">
        <el-card class="plan-config">
          <template #header>
            <div class="card-header">
              <span>计划配置</span>
            </div>
          </template>
          
          <el-form :model="planConfig" :rules="configRules" ref="configFormRef" label-width="100px" size="small">
            <el-form-item label="计划名称" prop="name">
              <el-input v-model="planConfig.name" placeholder="输入计划名称" />
            </el-form-item>
            
            <el-form-item label="计划周期" prop="duration">
              <el-select v-model="planConfig.duration" placeholder="选择周期">
                <el-option label="4周" :value="4" />
                <el-option label="8周" :value="8" />
                <el-option label="12周" :value="12" />
                <el-option label="16周" :value="16" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="训练目标" prop="goal">
              <el-select v-model="planConfig.goal" placeholder="选择目标" @change="onGoalChange">
                <el-option label="减脂塑形" value="fat_loss" />
                <el-option label="增肌增重" value="muscle_gain" />
                <el-option label="提升体能" value="endurance" />
                <el-option label="力量提升" value="strength" />
                <el-option label="综合训练" value="general" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="训练水平" prop="level">
              <el-select v-model="planConfig.level" placeholder="选择水平">
                <el-option label="初学者" value="beginner" />
                <el-option label="中级" value="intermediate" />
                <el-option label="高级" value="advanced" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="每周天数" prop="daysPerWeek">
              <el-select v-model="planConfig.daysPerWeek" placeholder="选择天数">
                <el-option label="3天" :value="3" />
                <el-option label="4天" :value="4" />
                <el-option label="5天" :value="5" />
                <el-option label="6天" :value="6" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="每次时长" prop="durationPerSession">
              <el-select v-model="planConfig.durationPerSession" placeholder="选择时长">
                <el-option label="30分钟" :value="30" />
                <el-option label="45分钟" :value="45" />
                <el-option label="60分钟" :value="60" />
                <el-option label="90分钟" :value="90" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 快速模板 -->
        <el-card class="quick-templates">
          <template #header>
            <div class="card-header">
              <span>快速模板</span>
            </div>
          </template>
          
          <div class="template-list">
            <div 
              v-for="template in templates" 
              :key="template.id"
              class="template-item"
              :class="{ active: selectedTemplate === template.id }"
              @click="selectTemplate(template)"
            >
              <div class="template-icon">
                <el-icon>
                  <component :is="template.icon" />
                </el-icon>
              </div>
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-desc">{{ template.description }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：周计划详情 -->
      <div class="planner-main">
        <el-tabs v-model="activeWeek" type="card" editable @edit="handleTabEdit">
          <el-tab-pane
            v-for="(week, weekIndex) in weeklyPlan"
            :key="weekIndex"
            :label="`第${weekIndex + 1}周`"
            :name="weekIndex.toString()"
          >
            <div class="week-plan">
              <div class="week-header">
                <h4>第{{ weekIndex + 1 }}周训练安排</h4>
                <div class="week-actions">
                  <el-button size="small" @click="copyWeek(weekIndex)">复制本周</el-button>
                  <el-button size="small" @click="clearWeek(weekIndex)">清空本周</el-button>
                </div>
              </div>
              
              <div class="days-grid">
                <div
                  v-for="(day, dayIndex) in week.days"
                  :key="dayIndex"
                  class="day-card"
                  :class="{ 'rest-day': !day.hasTraining }"
                >
                  <div class="day-header">
                    <span class="day-name">{{ getDayName(dayIndex) }}</span>
                    <el-switch
                      v-model="day.hasTraining"
                      size="small"
                      @change="toggleTrainingDay(weekIndex, dayIndex)"
                    />
                  </div>
                  
                  <div v-if="day.hasTraining" class="day-content">
                    <div class="training-focus">
                      <el-select
                        v-model="day.focus"
                        placeholder="训练重点"
                        size="small"
                        @change="updateDayExercises(weekIndex, dayIndex)"
                      >
                        <el-option label="胸部" value="chest" />
                        <el-option label="背部" value="back" />
                        <el-option label="腿部" value="legs" />
                        <el-option label="肩部" value="shoulders" />
                        <el-option label="手臂" value="arms" />
                        <el-option label="核心" value="core" />
                        <el-option label="有氧" value="cardio" />
                        <el-option label="全身" value="full_body" />
                      </el-select>
                    </div>
                    
                    <div class="exercises-list">
                      <div
                        v-for="(exercise, exIndex) in day.exercises"
                        :key="exIndex"
                        class="exercise-item"
                      >
                        <div class="exercise-info">
                          <span class="exercise-name">{{ exercise.name }}</span>
                          <span class="exercise-details">
                            {{ exercise.sets }}组 × {{ exercise.reps }}次
                            <span v-if="exercise.weight">× {{ exercise.weight }}kg</span>
                          </span>
                        </div>
                        <div class="exercise-actions">
                          <el-button size="small" text @click="editExercise(weekIndex, dayIndex, exIndex)">
                            <el-icon><Edit /></el-icon>
                          </el-button>
                          <el-button size="small" text type="danger" @click="removeExercise(weekIndex, dayIndex, exIndex)">
                            <el-icon><Delete /></el-icon>
                          </el-button>
                        </div>
                      </div>
                      
                      <el-button
                        size="small"
                        text
                        type="primary"
                        @click="addExercise(weekIndex, dayIndex)"
                        class="add-exercise-btn"
                      >
                        <el-icon><Plus /></el-icon>
                        添加动作
                      </el-button>
                    </div>
                  </div>
                  
                  <div v-else class="rest-day-content">
                    <el-icon><Moon /></el-icon>
                    <span>休息日</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 动作编辑对话框 -->
    <el-dialog v-model="showExerciseDialog" title="编辑动作" width="500px">
      <el-form :model="exerciseForm" :rules="exerciseRules" ref="exerciseFormRef" label-width="80px">
        <el-form-item label="动作名称" prop="name">
          <el-input v-model="exerciseForm.name" placeholder="请输入动作名称" />
        </el-form-item>
        <el-form-item label="组数" prop="sets">
          <el-input-number v-model="exerciseForm.sets" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="次数" prop="reps">
          <el-input v-model="exerciseForm.reps" placeholder="如: 8-12 或 15" />
        </el-form-item>
        <el-form-item label="重量" prop="weight">
          <el-input-number v-model="exerciseForm.weight" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="休息时间" prop="rest">
          <el-input-number v-model="exerciseForm.rest" :min="15" :max="300" />
          <span style="margin-left: 8px;">秒</span>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="exerciseForm.notes" type="textarea" :rows="2" placeholder="动作要点或注意事项" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showExerciseDialog = false">取消</el-button>
          <el-button type="primary" @click="saveExercise">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 计划预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="计划预览" width="80%" top="5vh">
      <div class="plan-preview">
        <div class="preview-header">
          <h2>{{ planConfig.name }}</h2>
          <div class="plan-meta">
            <el-tag>{{ getGoalText(planConfig.goal) }}</el-tag>
            <el-tag type="success">{{ getLevelText(planConfig.level) }}</el-tag>
            <span>{{ planConfig.duration }}周 · 每周{{ planConfig.daysPerWeek }}天</span>
          </div>
        </div>
        
        <div class="preview-content">
          <div v-for="(week, weekIndex) in weeklyPlan" :key="weekIndex" class="preview-week">
            <h3>第{{ weekIndex + 1 }}周</h3>
            <div class="preview-days">
              <div v-for="(day, dayIndex) in week.days" :key="dayIndex" class="preview-day">
                <div class="preview-day-header">
                  <strong>{{ getDayName(dayIndex) }}</strong>
                  <span v-if="!day.hasTraining" class="rest-tag">休息</span>
                </div>
                <div v-if="day.hasTraining && day.exercises.length > 0" class="preview-exercises">
                  <div v-for="exercise in day.exercises" :key="exercise.name" class="preview-exercise">
                    • {{ exercise.name }} {{ exercise.sets }}组×{{ exercise.reps }}次
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fitnessApi } from '../api/fitness'
import { 
  Check, View, Edit, Delete, Plus, Moon,
  TrendCharts, Trophy, Star, Lightning
} from '@element-plus/icons-vue'

// 响应式数据
const saving = ref(false)
const activeWeek = ref('0')
const selectedTemplate = ref('')
const showExerciseDialog = ref(false)
const showPreviewDialog = ref(false)
const configFormRef = ref(null)
const exerciseFormRef = ref(null)

// 当前编辑的动作位置
const currentExercisePosition = ref({ weekIndex: 0, dayIndex: 0, exerciseIndex: -1 })

// 计划配置
const planConfig = reactive({
  name: '',
  duration: 8,
  goal: 'general',
  level: 'intermediate',
  daysPerWeek: 4,
  durationPerSession: 60
})

// 配置验证规则
const configRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  duration: [{ required: true, message: '请选择计划周期', trigger: 'change' }],
  goal: [{ required: true, message: '请选择训练目标', trigger: 'change' }],
  level: [{ required: true, message: '请选择训练水平', trigger: 'change' }],
  daysPerWeek: [{ required: true, message: '请选择每周训练天数', trigger: 'change' }],
  durationPerSession: [{ required: true, message: '请选择每次训练时长', trigger: 'change' }]
}

// 动作表单
const exerciseForm = reactive({
  name: '',
  sets: 3,
  reps: '8-12',
  weight: 0,
  rest: 60,
  notes: ''
})

// 动作验证规则
const exerciseRules = {
  name: [{ required: true, message: '请输入动作名称', trigger: 'blur' }],
  sets: [{ required: true, message: '请输入组数', trigger: 'blur' }],
  reps: [{ required: true, message: '请输入次数', trigger: 'blur' }]
}

// 周计划数据
const weeklyPlan = ref([])

// 模板数据
const templates = ref([
  {
    id: 'fat_loss',
    name: '减脂计划',
    description: '有氧+力量，高效燃脂',
    icon: 'TrendCharts'
  },
  {
    id: 'muscle_gain',
    name: '增肌计划',
    description: '力量训练为主，肌肉增长',
    icon: 'Trophy'
  },
  {
    id: 'endurance',
    name: '体能计划',
    description: '提升心肺功能和耐力',
    icon: 'Star'
  },
  {
    id: 'strength',
    name: '力量计划',
    description: '大重量训练，提升力量',
    icon: 'Lightning'
  }
])

// 动作库
const exerciseLibrary = {
  chest: ['卧推', '上斜卧推', '飞鸟', '双杠臂屈伸', '俯卧撑'],
  back: ['引体向上', '划船', '硬拉', '高位下拉', '坐姿划船'],
  legs: ['深蹲', '腿举', '腿弯举', '腿屈伸', '弓步蹲'],
  shoulders: ['推举', '侧平举', '前平举', '耸肩', '面拉'],
  arms: ['弯举', '臂屈伸', '锤式弯举', '窄距卧推', '引体向上'],
  core: ['平板支撑', '卷腹', '俄罗斯转体', '举腿', '侧平板'],
  cardio: ['跑步', '单车', '划船机', '椭圆机', '跳绳'],
  full_body: ['深蹲', '卧推', '划船', '推举', '硬拉']
}

// 方法
const initializePlan = () => {
  weeklyPlan.value = Array.from({ length: planConfig.duration }, () => ({
    days: Array.from({ length: 7 }, () => ({
      hasTraining: false,
      focus: '',
      exercises: []
    }))
  }))
}

const onGoalChange = () => {
  // 根据目标自动调整配置
  switch (planConfig.goal) {
    case 'fat_loss':
      planConfig.daysPerWeek = 5
      planConfig.durationPerSession = 45
      break
    case 'muscle_gain':
      planConfig.daysPerWeek = 4
      planConfig.durationPerSession = 60
      break
    case 'endurance':
      planConfig.daysPerWeek = 6
      planConfig.durationPerSession = 30
      break
    case 'strength':
      planConfig.daysPerWeek = 3
      planConfig.durationPerSession = 90
      break
  }
}

const selectTemplate = (template) => {
  selectedTemplate.value = template.id
  planConfig.goal = template.id
  planConfig.name = template.name
  onGoalChange()
  
  // 根据模板生成基础计划
  generateTemplatePlan(template.id)
}

const generateTemplatePlan = (templateType) => {
  initializePlan()
  
  // 根据模板类型生成第一周的计划
  const firstWeek = weeklyPlan.value[0]
  
  switch (templateType) {
    case 'fat_loss':
      // 减脂计划：力量+有氧结合
      firstWeek.days[0].hasTraining = true
      firstWeek.days[0].focus = 'full_body'
      firstWeek.days[1].hasTraining = true
      firstWeek.days[1].focus = 'cardio'
      firstWeek.days[2].hasTraining = true
      firstWeek.days[2].focus = 'full_body'
      firstWeek.days[3].hasTraining = true
      firstWeek.days[3].focus = 'cardio'
      firstWeek.days[4].hasTraining = true
      firstWeek.days[4].focus = 'full_body'
      break
      
    case 'muscle_gain':
      // 增肌计划：分化训练
      firstWeek.days[0].hasTraining = true
      firstWeek.days[0].focus = 'chest'
      firstWeek.days[1].hasTraining = true
      firstWeek.days[1].focus = 'back'
      firstWeek.days[2].hasTraining = true
      firstWeek.days[2].focus = 'legs'
      firstWeek.days[3].hasTraining = true
      firstWeek.days[3].focus = 'shoulders'
      firstWeek.days[4].hasTraining = true
      firstWeek.days[4].focus = 'arms'
      break
      
    case 'endurance':
      // 体能计划：有氧为主
      firstWeek.days[0].hasTraining = true
      firstWeek.days[0].focus = 'cardio'
      firstWeek.days[1].hasTraining = true
      firstWeek.days[1].focus = 'core'
      firstWeek.days[2].hasTraining = true
      firstWeek.days[2].focus = 'cardio'
      firstWeek.days[3].hasTraining = true
      firstWeek.days[3].focus = 'full_body'
      firstWeek.days[4].hasTraining = true
      firstWeek.days[4].focus = 'cardio'
      firstWeek.days[5].hasTraining = true
      firstWeek.days[5].focus = 'core'
      break
      
    case 'strength':
      // 力量计划：三大项为主
      firstWeek.days[0].hasTraining = true
      firstWeek.days[0].focus = 'full_body'
      firstWeek.days[2].hasTraining = true
      firstWeek.days[2].focus = 'full_body'
      firstWeek.days[4].hasTraining = true
      firstWeek.days[4].focus = 'full_body'
      break
  }
  
  // 为训练日添加基础动作
  firstWeek.days.forEach((day, index) => {
    if (day.hasTraining && day.focus) {
      updateDayExercises(0, index)
    }
  })
}

const getDayName = (index) => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[index]
}

const getGoalText = (goal) => {
  const goalMap = {
    fat_loss: '减脂塑形',
    muscle_gain: '增肌增重',
    endurance: '提升体能',
    strength: '力量提升',
    general: '综合训练'
  }
  return goalMap[goal] || '综合训练'
}

const getLevelText = (level) => {
  const levelMap = {
    beginner: '初学者',
    intermediate: '中级',
    advanced: '高级'
  }
  return levelMap[level] || '中级'
}

const toggleTrainingDay = (weekIndex, dayIndex) => {
  const day = weeklyPlan.value[weekIndex].days[dayIndex]
  if (!day.hasTraining) {
    day.focus = ''
    day.exercises = []
  }
}

const updateDayExercises = (weekIndex, dayIndex) => {
  const day = weeklyPlan.value[weekIndex].days[dayIndex]
  if (!day.focus) return
  
  const exercises = exerciseLibrary[day.focus] || []
  day.exercises = exercises.slice(0, 4).map(name => ({
    name,
    sets: getDefaultSets(planConfig.level),
    reps: getDefaultReps(day.focus, planConfig.level),
    weight: getDefaultWeight(day.focus, planConfig.level),
    rest: getDefaultRest(day.focus),
    notes: ''
  }))
}

const getDefaultSets = (level) => {
  const setsMap = {
    beginner: 3,
    intermediate: 4,
    advanced: 5
  }
  return setsMap[level] || 4
}

const getDefaultReps = (focus, level) => {
  const repsMap = {
    chest: { beginner: '8-12', intermediate: '6-10', advanced: '4-8' },
    back: { beginner: '8-12', intermediate: '6-10', advanced: '4-8' },
    legs: { beginner: '10-15', intermediate: '8-12', advanced: '6-10' },
    shoulders: { beginner: '10-15', intermediate: '8-12', advanced: '6-10' },
    arms: { beginner: '10-15', intermediate: '8-12', advanced: '6-12' },
    core: { beginner: '15-20', intermediate: '12-18', advanced: '10-15' },
    cardio: { beginner: '20-30分钟', intermediate: '30-45分钟', advanced: '45-60分钟' },
    full_body: { beginner: '8-12', intermediate: '6-10', advanced: '4-8' }
  }
  return repsMap[focus]?.[level] || '8-12'
}

const getDefaultWeight = (focus, level) => {
  const weightMap = {
    beginner: 0,
    intermediate: 20,
    advanced: 40
  }
  return weightMap[level] || 0
}

const getDefaultRest = (focus) => {
  const restMap = {
    chest: 90,
    back: 120,
    legs: 120,
    shoulders: 60,
    arms: 60,
    core: 45,
    cardio: 30,
    full_body: 90
  }
  return restMap[focus] || 60
}

const addExercise = (weekIndex, dayIndex) => {
  currentExercisePosition.value = { weekIndex, dayIndex, exerciseIndex: -1 }
  Object.assign(exerciseForm, {
    name: '',
    sets: 3,
    reps: '8-12',
    weight: 0,
    rest: 60,
    notes: ''
  })
  showExerciseDialog.value = true
}

const editExercise = (weekIndex, dayIndex, exerciseIndex) => {
  currentExercisePosition.value = { weekIndex, dayIndex, exerciseIndex }
  const exercise = weeklyPlan.value[weekIndex].days[dayIndex].exercises[exerciseIndex]
  Object.assign(exerciseForm, exercise)
  showExerciseDialog.value = true
}

const removeExercise = (weekIndex, dayIndex, exerciseIndex) => {
  weeklyPlan.value[weekIndex].days[dayIndex].exercises.splice(exerciseIndex, 1)
}

const saveExercise = async () => {
  try {
    await exerciseFormRef.value.validate()
    
    const { weekIndex, dayIndex, exerciseIndex } = currentExercisePosition.value
    const exerciseData = { ...exerciseForm }
    
    if (exerciseIndex === -1) {
      // 添加新动作
      weeklyPlan.value[weekIndex].days[dayIndex].exercises.push(exerciseData)
    } else {
      // 更新现有动作
      weeklyPlan.value[weekIndex].days[dayIndex].exercises[exerciseIndex] = exerciseData
    }
    
    showExerciseDialog.value = false
    ElMessage.success('动作保存成功')
  } catch (error) {
    console.error('保存动作失败:', error)
  }
}

const copyWeek = (weekIndex) => {
  const weekToCopy = weeklyPlan.value[weekIndex]
  const newWeek = JSON.parse(JSON.stringify(weekToCopy))
  weeklyPlan.value.splice(weekIndex + 1, 0, newWeek)
  ElMessage.success('周计划已复制')
}

const clearWeek = (weekIndex) => {
  weeklyPlan.value[weekIndex].days = Array.from({ length: 7 }, () => ({
    hasTraining: false,
    focus: '',
    exercises: []
  }))
  ElMessage.success('周计划已清空')
}

const handleTabEdit = (targetName, action) => {
  if (action === 'add') {
    const newWeek = Array.from({ length: 7 }, () => ({
      hasTraining: false,
      focus: '',
      exercises: []
    }))
    weeklyPlan.value.push(newWeek)
    planConfig.duration = weeklyPlan.value.length
  } else if (action === 'remove') {
    weeklyPlan.value.splice(parseInt(targetName), 1)
    planConfig.duration = weeklyPlan.value.length
  }
}

const savePlan = async () => {
  try {
    await configFormRef.value.validate()
    
    saving.value = true
    
    // 构造提交数据
    const planData = {
      ...planConfig,
      weeklyPlan: weeklyPlan.value
    }
    
    // 调用API保存计划
    await fitnessApi.createFitnessPlan(planData)
    
    ElMessage.success('健身计划保存成功')
    // 可以跳转到计划列表或预览
    showPreviewDialog.value = true
  } catch (error) {
    console.error('保存计划失败:', error)
    ElMessage.error(error.message || '请完善计划配置')
  } finally {
    saving.value = false
  }
}

const previewPlan = () => {
  showPreviewDialog.value = true
}

// 生命周期
onMounted(() => {
  initializePlan()
})
</script>

<style scoped>
.fitness-planner {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.planner-container {
  display: flex;
  gap: 30px;
}

.planner-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.plan-config {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  color: #333;
}

.quick-templates {
  margin-top: 20px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.template-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.template-item:hover {
  border-color: #409EFF;
  background: #f0f9ff;
}

.template-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.template-icon {
  width: 36px;
  height: 36px;
  background: #409EFF;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
  font-size: 18px;
}

.template-info {
  flex: 1;
}

.template-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.template-desc {
  font-size: 12px;
  color: #666;
}

.planner-main {
  flex: 1;
  min-width: 0;
}

.week-plan {
  padding: 20px 0;
}

.week-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.week-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.week-actions {
  display: flex;
  gap: 8px;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.day-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.day-card:not(.rest-day) {
  background: white;
  border-color: #409EFF;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.day-name {
  font-weight: 600;
  color: #333;
}

.day-content {
  min-height: 200px;
}

.training-focus {
  margin-bottom: 12px;
}

.exercises-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exercise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.exercise-info {
  flex: 1;
}

.exercise-name {
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 2px;
}

.exercise-details {
  font-size: 12px;
  color: #666;
}

.exercise-actions {
  display: flex;
  gap: 4px;
}

.add-exercise-btn {
  width: 100%;
  border: 1px dashed #409EFF;
  color: #409EFF;
  margin-top: 8px;
}

.rest-day-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #999;
}

.rest-day-content .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.plan-preview {
  max-height: 80vh;
  overflow-y: auto;
}

.preview-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
}

.preview-header h2 {
  margin: 0 0 12px 0;
  color: #333;
}

.plan-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  color: #666;
}

.preview-week {
  margin-bottom: 30px;
}

.preview-week h3 {
  margin: 0 0 16px 0;
  color: #333;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.preview-days {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.preview-day {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.preview-day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.rest-tag {
  color: #999;
  font-size: 12px;
}

.preview-exercises {
  font-size: 14px;
  color: #666;
}

.preview-exercise {
  margin-bottom: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 1200px) {
  .planner-container {
    flex-direction: column;
  }
  
  .planner-sidebar {
    width: 100%;
  }
}
</style>