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
      backgroundColor: 'rgba(18, 18, 37, 0.95)',
      borderColor: 'rgba(112, 0, 255, 0.3)',
      textStyle: {
        color: '#ffffff'
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
          color: 'rgba(112, 0, 255, 0.3)'
        }
      },
      axisLabel: {
        color: '#8888aa',
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '训练量',
      nameTextStyle: {
        color: '#8888aa'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(112, 0, 255, 0.3)'
        }
      },
      axisLabel: {
        color: '#8888aa'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(112, 0, 255, 0.1)',
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
        color: '#8020ff'
      } : undefined,
      itemStyle: {
        color: '#8020ff',
        ...(seriesType === 'line' && {
          borderWidth: 2,
          borderColor: '#121225'
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
            color: 'rgba(128, 32, 255, 0.4)'
          }, {
            offset: 1,
            color: 'rgba(128, 32, 255, 0.05)'
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
      backgroundColor: 'rgba(18, 18, 37, 0.95)',
      borderColor: 'rgba(112, 0, 255, 0.3)',
      textStyle: {
        color: '#ffffff'
      },
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#8888aa',
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
        borderColor: '#121225',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#ffffff'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(128, 32, 255, 0.5)'
        }
      },
      labelLine: {
        show: false
      },
      data: data,
      color: ['#8020ff', '#00f2fe', '#ff00ff', '#00ff88', '#ffaa00', '#8888ff', '#ff0055', '#00d4ff']
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
      backgroundColor: 'rgba(18, 18, 37, 0.95)',
      borderColor: 'rgba(112, 0, 255, 0.3)',
      textStyle: {
        color: '#ffffff'
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
          color: 'rgba(112, 0, 255, 0.3)'
        }
      },
      axisLabel: {
        color: '#8888aa',
        rotate: 45,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '训练次数',
      nameTextStyle: {
        color: '#8888aa'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(112, 0, 255, 0.3)'
        }
      },
      axisLabel: {
        color: '#8888aa'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(112, 0, 255, 0.1)',
          type: 'dashed'
        }
      }
    },
    series: [{
      data: values,
      type: 'bar',
      itemStyle: {
        color: function(params) {
          const colors = ['#8888aa', '#00ff88', '#ffaa00', '#ff00ff', '#8020ff']
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
/* =====================================================
   历史统计页面 - 统一主题系统
   ===================================================== */

/* 页面容器 */
.history-statistics-page {
  min-height: calc(100vh - 60px);
  background: var(--bg-page);
  padding: 24px;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-primary);
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-description {
  font-size: 1rem;
  color: var(--text-secondary);
}

/* 筛选器容器 */
.filter-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 20px));
  border-radius: 16px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
}

/* 统计卡片网格 */
.stats-card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* 统计卡片 */
.stats-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 20px));
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stats-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--brand-gradient);
}

.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stats-card--success::before {
  background: linear-gradient(90deg, var(--color-success), #34d399);
}

.stats-card--info::before {
  background: linear-gradient(90deg, var(--color-info), var(--brand-accent));
}

.stats-card--warning::before {
  background: linear-gradient(90deg, var(--color-warning), #fbbf24);
}

.stats-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stats-card__title {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-card__icon {
  font-size: 1.5rem;
  color: var(--brand-primary);
}

.stats-card--success .stats-card__icon {
  color: var(--color-success);
}

.stats-card--info .stats-card__icon {
  color: var(--color-info);
}

.stats-card--warning .stats-card__icon {
  color: var(--color-warning);
}

.stats-card__value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
}

.stats-card__description {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

/* 图表容器 */
.chart-container {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 20px));
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
}

.chart-container--large {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-content {
  height: 300px;
  width: 100%;
}

/* 数据表格容器 */
.data-table {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 20px));
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Element Plus 组件主题覆盖 */
:deep(.el-date-editor) {
  --el-input-bg-color: var(--input-bg);
  --el-input-border-color: var(--input-border);
  --el-input-text-color: var(--input-text);
}

:deep(.el-select) {
  --el-select-input-focus-border-color: var(--brand-primary);
}

:deep(.el-input__wrapper) {
  background-color: var(--input-bg) !important;
  border-color: var(--input-border) !important;
  box-shadow: none !important;
}

:deep(.el-input__inner) {
  color: var(--input-text) !important;
}

:deep(.el-input__inner::placeholder) {
  color: var(--input-placeholder) !important;
}

:deep(.el-radio-button__inner) {
  background: var(--input-bg);
  border-color: var(--border-default);
  color: var(--text-secondary);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--brand-gradient);
  border-color: var(--brand-primary);
  color: #ffffff;
  box-shadow: var(--shadow-brand);
}

/* 表格主题 */
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--hover-bg);
  --el-table-row-hover-bg-color: var(--hover-bg);
  --el-table-border-color: var(--border-default);
  --el-table-text-color: var(--text-secondary);
  --el-table-header-text-color: var(--text-primary);
  background-color: transparent;
}

:deep(.el-table th) {
  background: var(--hover-bg) !important;
  color: var(--text-primary) !important;
  border-bottom: 2px solid var(--border-strong) !important;
}

:deep(.el-table th .cell) {
  color: var(--text-primary) !important;
  font-weight: 600;
}

:deep(.el-table td) {
  background: transparent !important;
  color: var(--text-secondary) !important;
  border-bottom: 1px solid var(--border-subtle) !important;
}

:deep(.el-table td .cell) {
  color: var(--text-secondary) !important;
}

:deep(.el-table__body tr:hover > td) {
  background-color: var(--hover-bg) !important;
}

:deep(.el-table__empty-text) {
  color: var(--text-tertiary) !important;
}

/* 分页器主题 */
:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-bg-color: var(--input-bg);
}

:deep(.el-pagination .el-pager li) {
  background: var(--input-bg);
  color: var(--text-secondary);
  border: 1px solid var(--border-default);
}

:deep(.el-pagination .el-pager li:hover) {
  color: var(--brand-primary);
  border-color: var(--brand-primary);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--brand-gradient);
  color: #ffffff;
  border-color: transparent;
}

:deep(.el-pagination button) {
  background: var(--input-bg) !important;
  color: var(--text-secondary) !important;
  border: 1px solid var(--border-default) !important;
}

:deep(.el-pagination button:hover) {
  color: var(--brand-primary) !important;
}

/* 评分组件 */
:deep(.el-rate__icon) {
  color: var(--text-disabled);
}

:deep(.el-rate__icon.is-active) {
  color: var(--color-warning);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

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
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .filter-container {
    flex-direction: column;
    padding: 16px;
  }
  
  .filter-container > * {
    width: 100%;
  }
  
  .stats-card-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .stats-card {
    padding: 20px;
  }
  
  .stats-card__value {
    font-size: 1.5rem;
  }
  
  .charts-grid {
    gap: 16px;
  }
  
  .chart-container {
    padding: 16px;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .chart-content {
    height: 250px;
  }
  
  .data-table {
    padding: 16px;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
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
}

@media (max-width: 480px) {
  .history-statistics-page {
    padding: 12px;
  }
  
  .page-header h1 {
    font-size: 1.25rem;
  }
  
  .stats-card__value {
    font-size: 1.25rem;
  }
  
  .chart-content {
    height: 200px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>
