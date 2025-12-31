<template>
  <div class="training-record-manager">
    <div class="section-header">
      <h2>训练记录管理</h2>
      <div class="section-actions">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加记录
        </el-button>
        <el-button @click="refreshRecords" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="训练类型">
          <el-select v-model="filters.trainingType" placeholder="选择类型" clearable>
            <el-option label="力量训练" value="strength" />
            <el-option label="有氧训练" value="cardio" />
            <el-option label="柔韧训练" value="flexibility" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilters">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 记录列表 -->
    <div class="records-table">
      <el-table :data="filteredRecords" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="date" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="trainingType" label="训练类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTrainingTypeTag(row.trainingType)">
              {{ getTrainingTypeText(row.trainingType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exerciseName" label="训练项目" width="150" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="calories" label="消耗卡路里" width="120" />
        <el-table-column prop="intensity" label="强度" width="100">
          <template #default="{ row }">
            <el-rate
              v-model="row.intensity"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="editRecord(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRecord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalRecords"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRecord ? '编辑训练记录' : '添加训练记录'"
      width="600px"
    >
      <el-form :model="recordForm" :rules="formRules" ref="recordFormRef" label-width="100px">
        <el-form-item label="训练日期" prop="date">
          <el-date-picker
            v-model="recordForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="训练类型" prop="trainingType">
          <el-select v-model="recordForm.trainingType" placeholder="选择类型">
            <el-option label="力量训练" value="strength" />
            <el-option label="有氧训练" value="cardio" />
            <el-option label="柔韧训练" value="flexibility" />
          </el-select>
        </el-form-item>
        <el-form-item label="训练项目" prop="exerciseName">
          <el-input v-model="recordForm.exerciseName" placeholder="请输入训练项目" />
        </el-form-item>
        <el-form-item label="训练时长" prop="duration">
          <el-input-number
            v-model="recordForm.duration"
            :min="1"
            :max="300"
            placeholder="分钟"
          />
        </el-form-item>
        <el-form-item label="消耗卡路里" prop="calories">
          <el-input-number
            v-model="recordForm.calories"
            :min="0"
            :max="2000"
            placeholder="卡路里"
          />
        </el-form-item>
        <el-form-item label="训练强度" prop="intensity">
          <el-rate v-model="recordForm.intensity" show-text />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="recordForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRecord" :loading="saving">
            {{ editingRecord ? '更新' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingRecord = ref(null)
const recordFormRef = ref(null)

// 分页数据
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 筛选器
const filters = ref({
  trainingType: '',
  dateRange: []
})

// 表单数据
const recordForm = ref({
  date: '',
  trainingType: '',
  exerciseName: '',
  duration: 30,
  calories: 0,
  intensity: 3,
  notes: ''
})

// 表单验证规则
const formRules = {
  date: [{ required: true, message: '请选择训练日期', trigger: 'change' }],
  trainingType: [{ required: true, message: '请选择训练类型', trigger: 'change' }],
  exerciseName: [{ required: true, message: '请输入训练项目', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入训练时长', trigger: 'blur' }],
  calories: [{ required: true, message: '请输入消耗卡路里', trigger: 'blur' }],
  intensity: [{ required: true, message: '请选择训练强度', trigger: 'change' }]
}

// 训练记录数据
const trainingRecords = ref([])
const recordCache = new Map()
const CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

// 计算属性
const filteredRecords = computed(() => {
  let records = [...trainingRecords.value]
  
  // 应用筛选器
  if (filters.value.trainingType) {
    records = records.filter(record => record.trainingType === filters.value.trainingType)
  }
  
  if (filters.value.dateRange && filters.value.dateRange.length === 2) {
    const [startDate, endDate] = filters.value.dateRange
    records = records.filter(record => {
      return record.date >= startDate && record.date <= endDate
    })
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.slice(start, end)
})

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const getTrainingTypeTag = (type) => {
  const tagMap = {
    strength: 'success',
    cardio: 'warning',
    flexibility: 'info'
  }
  return tagMap[type] || 'info'
}

const getTrainingTypeText = (type) => {
  const textMap = {
    strength: '力量训练',
    cardio: '有氧训练',
    flexibility: '柔韧训练'
  }
  return textMap[type] || '未知'
}

const refreshRecords = async () => {
  loading.value = true
  try {
    // 检查缓存
    const cacheKey = 'training_records'
    const cached = recordCache.get(cacheKey)
    
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      trainingRecords.value = cached.data
      totalRecords.value = cached.data.length
      return
    }
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟数据
    const mockData = [
      {
        id: 1,
        date: '2024-01-15',
        trainingType: 'strength',
        exerciseName: '卧推',
        duration: 45,
        calories: 320,
        intensity: 4,
        notes: '感觉很好，突破了个人记录'
      },
      {
        id: 2,
        date: '2024-01-14',
        trainingType: 'cardio',
        exerciseName: '跑步',
        duration: 30,
        calories: 280,
        intensity: 3,
        notes: '慢跑，保持心率'
      },
      {
        id: 3,
        date: '2024-01-13',
        trainingType: 'flexibility',
        exerciseName: '瑜伽',
        duration: 60,
        calories: 180,
        intensity: 2,
        notes: '放松拉伸'
      }
    ]
    
    trainingRecords.value = mockData
    totalRecords.value = mockData.length
    
    // 缓存数据
    recordCache.set(cacheKey, {
      data: mockData,
      timestamp: Date.now()
    })
    
    ElMessage.success('训练记录已刷新')
  } catch (error) {
    console.error('刷新训练记录失败:', error)
    ElMessage.error('刷新训练记录失败')
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  currentPage.value = 1
  ElMessage.success('筛选条件已应用')
}

const resetFilters = () => {
  filters.value = {
    trainingType: '',
    dateRange: []
  }
  currentPage.value = 1
  ElMessage.success('筛选条件已重置')
}

const editRecord = (record) => {
  editingRecord.value = record
  recordForm.value = { ...record }
  showAddDialog.value = true
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除训练记录"${record.exerciseName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟删除操作
    const index = trainingRecords.value.findIndex(r => r.id === record.id)
    if (index > -1) {
      trainingRecords.value.splice(index, 1)
      totalRecords.value = trainingRecords.value.length
    }
    
    // 清除缓存
    recordCache.clear()
    
    ElMessage.success('训练记录已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除训练记录失败:', error)
      ElMessage.error('删除训练记录失败')
    }
  }
}

const saveRecord = async () => {
  if (!recordFormRef.value) return
  
  try {
    await recordFormRef.value.validate()
    saving.value = true
    
    // 模拟保存操作
    await new Promise(resolve => setTimeout(resolve, 500))
    
    if (editingRecord.value) {
      // 更新记录
      const index = trainingRecords.value.findIndex(r => r.id === editingRecord.value.id)
      if (index > -1) {
        trainingRecords.value[index] = {
          ...editingRecord.value,
          ...recordForm.value
        }
      }
      ElMessage.success('训练记录已更新')
    } else {
      // 添加新记录
      const newRecord = {
        id: Date.now(),
        ...recordForm.value
      }
      trainingRecords.value.unshift(newRecord)
      totalRecords.value = trainingRecords.value.length
      ElMessage.success('训练记录已添加')
    }
    
    // 清除缓存
    recordCache.clear()
    
    showAddDialog.value = false
    resetForm()
  } catch (error) {
    if (error !== 'validation failed') {
      console.error('保存训练记录失败:', error)
      ElMessage.error('保存训练记录失败')
    }
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  recordForm.value = {
    date: '',
    trainingType: '',
    exerciseName: '',
    duration: 30,
    calories: 0,
    intensity: 3,
    notes: ''
  }
  editingRecord.value = null
  recordFormRef.value?.clearValidate()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 生命周期
onMounted(() => {
  refreshRecords()
})

onUnmounted(() => {
  recordCache.clear()
})
</script>

<style scoped>
.training-record-manager {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.filters {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.filter-form {
  margin: 0;
}

.records-table {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-rate__text) {
  font-size: 12px;
  color: #606266;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>