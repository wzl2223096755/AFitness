<template>
  <div class="history-statistics-page p-5">
    <!-- 页面头部 -->
    <div class="page-header mb-6">
      <h1 class="responsive-h1 font-bold mb-2">历史统计</h1>
      <div class="page-description text-sm leading-relaxed">全面分析您的训练历史数据和进度</div>
    </div>
    
    <!-- 时间范围筛选器 -->
    <div class="filter-container p-4 mb-6 gap-3">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="handleDateRangeChange"
        value-format="YYYY-MM-DD"
      />
      <el-select v-model="exerciseTypeFilter" placeholder="选择动作类型" @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option label="上肢" value="上肢" />
        <el-option label="下肢" value="下肢" />
        <el-option label="全身" value="全身" />
      </el-select>
      <el-button type="primary" @click="applyFilters">应用筛选</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-card-grid auto-grid mb-6">
      <div class="stats-card p-5">
        <div class="stats-card__header mb-3">
          <span class="stats-card__title text-sm">总训练次数</span>
          <el-icon class="stats-card__icon icon-xl"><TrendCharts /></el-icon>
        </div>
        <div class="stats-card__value data-value--lg">{{ totalTrainingSessions }}</div>
        <div class="stats-card__description text-xs">所选时间范围内的训练次数</div>
      </div>
      
      <div class="stats-card stats-card--success p-5">
        <div class="stats-card__header mb-3">
          <span class="stats-card__title text-sm">总训练量</span>
          <el-icon class="stats-card__icon icon-xl"><Histogram /></el-icon>
        </div>
        <div class="stats-card__value data-value--lg">{{ totalVolume }}</div>
        <div class="stats-card__description text-xs">所有训练的重量×组数×次数总和</div>
      </div>
      
      <div class="stats-card stats-card--info p-5">
        <div class="stats-card__header mb-3">
          <span class="stats-card__title text-sm">平均训练时长</span>
          <el-icon class="stats-card__icon icon-xl"><Timer /></el-icon>
        </div>
        <div class="stats-card__value data-value--lg">{{ averageDuration }}</div>
        <div class="stats-card__description text-xs">每次训练的平均时长</div>
      </div>
      
      <div class="stats-card stats-card--warning p-5">
        <div class="stats-card__header mb-3">
          <span class="stats-card__title text-sm">最常训练动作</span>
          <el-icon class="stats-card__icon icon-xl"><Star /></el-icon>
        </div>
        <div class="stats-card__value data-value--lg">{{ mostFrequentExercise }}</div>
        <div class="stats-card__description text-xs">{{ mostFrequentExerciseCount }}次训练</div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 训练量趋势图 -->
      <div class="chart-container chart-container--large">
        <div class="chart-header">
          <h3>训练量趋势</h3>
          <div class="chart-actions">
            <el-radio-group v-model="trendChartType" @change="updateTrendChart">
              <el-radio-button label="line">折线图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-content"></div>
      </div>
      
      <!-- 动作分布图 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>动作分布</h3>
        </div>
        <div ref="distributionChartRef" class="chart-content"></div>
      </div>
      
      <!-- 训练强度分布 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>训练强度分布</h3>
        </div>
        <div ref="intensityChartRef" class="chart-content"></div>
      </div>
    </div>
    
    <!-- 训练详情表格 -->
    <div class="data-table">
      <div class="table-header">
        <h3>训练详情</h3>
        <div class="table-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索动作名称"
            :prefix-icon="Search"
            style="width: 200px; margin-right: 10px"
            @input="handleSearch"
          />
          <el-button type="primary" size="small" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="filteredTrainingData"
        style="width: 100%"
        :row-class-name="tableRowClassName"
        @sort-change="handleSort"
      >
        <el-table-column prop="date" label="日期" width="120" sortable="custom" />
        <el-table-column prop="exerciseName" label="动作名称" width="150" />
        <el-table-column prop="exerciseType" label="动作类型" width="100" />
        <el-table-column prop="weight" label="重量(kg)" width="100" sortable="custom" />
        <el-table-column prop="sets" label="组数" width="80" sortable="custom" />
        <el-table-column prop="reps" label="次数" width="80" sortable="custom" />
        <el-table-column prop="trainingVolume" label="训练量" width="100" sortable="custom" />
        <el-table-column prop="duration" label="时长(min)" width="100" sortable="custom" />
        <el-table-column prop="fatigueLevel" label="疲劳度" width="100">
          <template #default="scope">
            <el-rate v-model="scope.row.fatigueLevel" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" />
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredTrainingData.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from '../utils/message.js'
import { TrendCharts, Histogram, Timer, Star, Download, Search } from '@element-plus/icons-vue'

import echarts from '../utils/echarts'
import { useFitnessStore } from '../stores/fitness'

// 状态管理
const fitnessStore = useFitnessStore()

// 响应式数据
const dateRange = ref([])
const exerciseTypeFilter = ref('')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const trendChartType = ref('line')
const sortField = ref('')
const sortOrder = ref('')

// 图表引用
const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const intensityChartRef = ref(null)
const trendChartInstance = ref(null)
const distributionChartInstance = ref(null)
const intensityChartInstance = ref(null)

// 计算属性
const totalTrainingSessions = computed(() => {
  return filteredTrainingData.value.length
})

const totalVolume = computed(() => {
  return filteredTrainingData.value.reduce((sum, item) => sum + (item.trainingVolume || 0), 0)
})

const averageDuration = computed(() => {
  if (totalTrainingSessions.value === 0) return '0'
  const totalDuration = filteredTrainingData.value.reduce((sum, item) => sum + (item.duration || 0), 0)
  const avg = Math.round(totalDuration / totalTrainingSessions.value)
  return `${avg} 分钟`
})

const mostFrequentExercise = computed(() => {
  const exerciseCount = {}
  filteredTrainingData.value.forEach(item => {
    exerciseCount[item.exerciseName] = (exerciseCount[item.exerciseName] || 0) + 1
  })
  
  let maxCount = 0
  let mostFrequent = '暂无数据'
  
  Object.entries(exerciseCount).forEach(([exercise, count]) => {
    if (count > maxCount) {
      maxCount = count
      mostFrequent = exercise
    }
  })
  
  return mostFrequent
})

const mostFrequentExerciseCount = computed(() => {
  const exerciseCount = {}
  filteredTrainingData.value.forEach(item => {
    exerciseCount[item.exerciseName] = (exerciseCount[item.exerciseName] || 0) + 1
  })
  
  let maxCount = 0
  Object.entries(exerciseCount).forEach(([exercise, count]) => {
    if (count > maxCount) {
      maxCount = count
    }
  })
  
  return maxCount
})

// 获取筛选后的数据
const filteredTrainingData = computed(() => {
  let data = [...trainingData.value]
  
  // 按日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0])
    const endDate = new Date(dateRange.value[1])
    data = data.filter(item => {
      const itemDate = new Date(item.date)
      return itemDate >= startDate && itemDate <= endDate
    })
  }
  
  // 按动作类型筛选
  if (exerciseTypeFilter.value) {
    data = data.filter(item => item.exerciseType === exerciseTypeFilter.value)
  }
  
  // 按搜索关键词筛选
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.trim().toLowerCase()
    data = data.filter(item => 
      item.exerciseName.toLowerCase().includes(query) ||
      (item.notes && item.notes.toLowerCase().includes(query))
    )
  }
  
  // 排序
  if (sortField.value && sortOrder.value) {
    data.sort((a, b) => {
      if (sortOrder.value === 'ascending') {
        return a[sortField.value] > b[sortField.value] ? 1 : -1
      } else {
        return a[sortField.value] < b[sortField.value] ? 1 : -1
      }
    })
  }
  
  return data
})

// 分页数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTrainingData.value.slice(start, end)
})

// 模拟训练数据
const trainingData = ref([])

// 方法
const initializeData = async () => {
  // 从store获取实际数据
  if (fitnessStore.fitnessData.length === 0) {
    await fitnessStore.initializeData()
  }
  
  // 转换数据格式
  if (fitnessStore.fitnessData.length > 0) {
    trainingData.value = fitnessStore.fitnessData.map(item => ({
      ...item,
      date: new Date(item.timestamp || Date.now()).toLocaleDateString(),
      duration: item.duration || 30,
      fatigueLevel: item.fatigueLevel || 3,
      notes: item.notes || ''
    }))
  } else {
    trainingData.value = []
    ElMessage.info('暂无训练数据，请先添加训练记录')
  }
  
  // 默认设置日期范围为最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
}

const handleDateRangeChange = () => {
  // 日期范围变化时的处理
  currentPage.value = 1 // 重置到第一页
}

const handleFilterChange = () => {
  // 筛选条件变化时的处理
  currentPage.value = 1 // 重置到第一页
}

const applyFilters = () => {
  // 应用所有筛选条件
  currentPage.value = 1 // 重置到第一页
  updateCharts()
  ElMessage.success('筛选条件已应用')
}

const resetFilters = () => {
  // 重置所有筛选条件
  dateRange.value = []
  exerciseTypeFilter.value = ''
  searchQuery.value = ''
  currentPage.value = 1
  sortField.value = ''
  sortOrder.value = ''
  updateCharts()
}

const handleSearch = () => {
  // 搜索框输入时的处理
  currentPage.value = 1
}

const handleSort = ({ prop, order }) => {
  // 表格排序处理
  sortField.value = prop
  sortOrder.value = order
}

const handleSizeChange = (size) => {
  // 每页条数变化
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current) => {
  // 当前页变化
  currentPage.value = current
}

const tableRowClassName = ({ row, rowIndex }) => {
  // 根据训练量添加不同的样式
  if (row.trainingVolume > 3000) return 'table-row--highlight'
  return ''
}

const exportData = () => {
  // 导出数据功能
  const csvContent = [
    ['日期', '动作名称', '动作类型', '重量(kg)', '组数', '次数', '训练量', '时长(min)', '疲劳度'].join(','),
    ...filteredTrainingData.value.map(item => [
      item.date,
      item.exerciseName,
      item.exerciseType,
      item.weight,
      item.sets,
      item.reps,
      item.trainingVolume,
      item.duration,
      item.fatigueLevel
    ].join(','))
  ].join('\n')
  
  const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', `训练记录_${new Date().toISOString().split('T')[0]}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('数据导出成功')
}

// 图表相关方法
const initCharts = async () => {
  await nextTick()
  
  // 初始化训练量趋势图
  if (trendChartRef.value) {
    trendChartInstance.value = echarts.init(trendChartRef.value)
    updateTrendChart()
  }
  
  // 初始化动作分布图
  if (distributionChartRef.value) {
    distributionChartInstance.value = echarts.init(distributionChartRef.value)
    updateDistributionChart()
  }
  
  // 初始化强度分布图
  if (intensityChartRef.value) {
    intensityChartInstance.value = echarts.init(intensityChartRef.value)
    updateIntensityChart()
  }
}

const updateCharts = () => {
  updateTrendChart()
  updateDistributionChart()
  updateIntensityChart()
}

const updateTrendChart = () => {
  // 确保图表实例已初始化
  if (!trendChartInstance.value) {
    return
  }
  
  // 按日期分组数据
  const dateGroups = {}
  filteredTrainingData.value.forEach(item => {
    const date = new Date(item.date).toISOString().split('T')[0]
    if (!dateGroups[date]) {
      dateGroups[date] = 0
    }
    dateGroups[date] += item.trainingVolume
  })
  
  // 转换为图表数据格式
  const dates = Object.keys(dateGroups).sort()
  const volumes = dates.map(date => dateGroups[date])
  
  // 格式化日期显示
  const formattedDates = dates.map(date => {
    const d = new Date(date)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  
  const seriesType = trendChartType.value
  
  trendChartInstance.value.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      },
      formatter: function(params) {
        const data = params[0]
        return `${data.axisValue}<br/>训练量: ${data.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: formattedDates,
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666',
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '训练量',
      nameTextStyle: {
        color: '#666'
      },
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: [{
      data: volumes,
      type: seriesType,
      smooth: seriesType === 'line',
      symbol: seriesType === 'line' ? 'circle' : undefined,
      symbolSize: seriesType === 'line' ? 6 : undefined,
      lineStyle: seriesType === 'line' ? {
        width: 3,
        color: '#409eff'
      } : undefined,
      itemStyle: {
        color: '#409eff',
        ...(seriesType === 'line' && {
          borderWidth: 2,
          borderColor: '#fff'
        })
      },
      areaStyle: seriesType === 'line' ? {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1,
            color: 'rgba(64, 158, 255, 0.05)'
          }]
        }
      } : undefined
    }]
  })
}

const updateDistributionChart = () => {
  // 确保图表实例已初始化
  if (!distributionChartInstance.value) {
    return
  }
  
  // 统计不同动作的训练次数
  const exerciseCount = {}
  filteredTrainingData.value.forEach(item => {
    exerciseCount[item.exerciseName] = (exerciseCount[item.exerciseName] || 0) + 1
  })
  
  // 转换为图表数据格式
  const data = Object.entries(exerciseCount)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8) // 只显示前8个
  
  distributionChartInstance.value.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      },
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#666',
        fontSize: 12
      },
      formatter: function(name) {
        // 限制图例文字长度
        return name.length > 8 ? name.substring(0, 8) + '...' : name
      }
    },
    series: [{
      name: '训练动作',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      labelLine: {
        show: false
      },
      data: data
    }]
  })
}

const updateIntensityChart = () => {
  // 确保图表实例已初始化
  if (!intensityChartInstance.value) {
    return
  }
  
  // 按训练量范围统计
  const intensityRanges = {
    '低强度 (<1000)': 0,
    '中低强度 (1000-2000)': 0,
    '中强度 (2000-3000)': 0,
    '中高强度 (3000-4000)': 0,
    '高强度 (>4000)': 0
  }
  
  filteredTrainingData.value.forEach(item => {
    const volume = item.trainingVolume
    if (volume < 1000) {
      intensityRanges['低强度 (<1000)']++
    } else if (volume < 2000) {
      intensityRanges['中低强度 (1000-2000)']++
    } else if (volume < 3000) {
      intensityRanges['中强度 (2000-3000)']++
    } else if (volume < 4000) {
      intensityRanges['中高强度 (3000-4000)']++
    } else {
      intensityRanges['高强度 (>4000)']++
    }
  })
  
  // 转换为图表数据格式
  const categories = Object.keys(intensityRanges)
  const values = Object.values(intensityRanges)
  
  intensityChartInstance.value.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      },
      formatter: function(params) {
        const data = params[0]
        return `${data.axisValue}<br/>训练次数: ${data.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666',
        rotate: 45,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '训练次数',
      nameTextStyle: {
        color: '#666'
      },
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    series: [{
      data: values,
      type: 'bar',
      itemStyle: {
        color: function(params) {
          const colors = ['#909399', '#67c23a', '#e6a23c', '#f56c6c', '#409eff']
          return colors[params.dataIndex % colors.length]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

const handleResize = () => {
  if (trendChartInstance.value) {
    trendChartInstance.value.resize()
  }
  if (distributionChartInstance.value) {
    distributionChartInstance.value.resize()
  }
  if (intensityChartInstance.value) {
    intensityChartInstance.value.resize()
  }
}

// 监听趋势图类型变化
watch(trendChartType, updateTrendChart)

// 生命周期
onMounted(async () => {
  // 初始化数据
  await initializeData()
  
  // 初始化图表
  await initCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  if (trendChartInstance.value) {
    trendChartInstance.value.dispose()
  }
  if (distributionChartInstance.value) {
    distributionChartInstance.value.dispose()
  }
  if (intensityChartInstance.value) {
    intensityChartInstance.value.dispose()
  }
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>


<style scoped>
/* Page Layout - using design tokens */
.history-statistics-page {
  padding: var(--spacing-5);
}

.page-header {
  margin-bottom: var(--spacing-6);
}

.page-header h1 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-2);
  color: var(--el-text-color-primary);
  line-height: var(--line-height-tight);
}

.page-description {
  font-size: var(--font-size-sm);
  color: var(--el-text-color-secondary);
  line-height: var(--line-height-relaxed);
}

/* Filter Container */
.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-6);
  padding: var(--spacing-4);
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Stats Card Grid - using auto-grid pattern */
.stats-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-6);
}

/* Stats Card - consistent internal padding (20-28px per Req 6.1, 6.2) */
.stats-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: var(--spacing-5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-top: 4px solid #409eff;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stats-card--success {
  border-top-color: #67c23a;
}

.stats-card--info {
  border-top-color: #909399;
}

.stats-card--warning {
  border-top-color: #e6a23c;
}

.stats-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3);
}

.stats-card__title {
  font-size: var(--font-size-sm);
  color: var(--el-text-color-secondary);
  line-height: var(--line-height-normal);
}

.stats-card__icon {
  font-size: var(--font-size-2xl);
  color: #409eff;
}

.stats-card__value {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-2);
  font-variant-numeric: tabular-nums;
  line-height: var(--line-height-tight);
}

.stats-card__description {
  font-size: var(--font-size-xs);
  color: var(--el-text-color-secondary);
  line-height: var(--line-height-normal);
}

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-6);
}

/* Chart Container - consistent padding */
.chart-container {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: var(--spacing-5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-container--large {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}

.chart-header h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--el-text-color-primary);
  margin: 0;
  line-height: var(--line-height-snug);
}

.chart-content {
  height: 300px;
  width: 100%;
}

/* Data Table - consistent padding and typography */
.data-table {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: var(--spacing-5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}

.table-header h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--el-text-color-primary);
  margin: 0;
  line-height: var(--line-height-snug);
}

.table-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.pagination-container {
  margin-top: var(--spacing-4);
  display: flex;
  justify-content: flex-end;
}

/* Table styling - Requirements 7.1, 7.2, 7.3 */
:deep(.el-table) {
  font-size: var(--font-size-sm);
}

:deep(.el-table th) {
  font-weight: var(--font-weight-semibold);
  color: var(--el-text-color-primary);
  background-color: var(--el-fill-color-light);
}

:deep(.el-table .cell) {
  padding: var(--spacing-3) var(--spacing-4);
  line-height: var(--line-height-normal);
}

:deep(.el-table td .cell) {
  font-variant-numeric: tabular-nums;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container--large {
    grid-column: 1;
  }
}

@media (max-width: 768px) {
  .history-statistics-page {
    padding: var(--spacing-3);
  }
  
  .page-header {
    margin-bottom: var(--spacing-4);
  }
  
  .page-header h1 {
    font-size: var(--font-size-xl);
  }
  
  .filter-container {
    flex-direction: column;
    padding: var(--spacing-4);
  }
  
  .filter-container .el-date-editor,
  .filter-container .el-select,
  .filter-container .el-button {
    width: 100%;
  }
  
  .stats-card-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-3);
  }
  
  .stats-card {
    padding: var(--spacing-4);
  }
  
  .stats-card__value {
    font-size: var(--font-size-2xl);
  }
  
  .charts-grid {
    gap: var(--spacing-3);
  }
  
  .chart-container {
    padding: var(--spacing-4);
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3);
  }
  
  .chart-content {
    height: 250px;
  }
  
  .data-table {
    padding: var(--spacing-4);
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-3);
  }
  
  .table-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .table-actions .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .table-actions .el-button {
    width: 100%;
  }
  
  .pagination-container {
    justify-content: center;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--spacing-2);
  }
  
  :deep(.el-pagination .el-pagination__sizes),
  :deep(.el-pagination .el-pagination__jump) {
    display: none;
  }
}

@media (max-width: 480px) {
  .history-statistics-page {
    padding: var(--spacing-3);
  }
  
  .filter-container,
  .stats-card,
  .chart-container,
  .data-table {
    border-radius: 10px;
  }
  
  .stats-card__value {
    font-size: var(--font-size-xl);
  }
  
  .chart-content {
    height: 220px;
  }
  
  :deep(.el-table) {
    font-size: var(--font-size-xs);
  }
  
  :deep(.el-table .cell) {
    padding: var(--spacing-2) var(--spacing-2);
  }
}
</style>
