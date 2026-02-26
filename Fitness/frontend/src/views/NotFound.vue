<template>
  <div class="not-found-container">
    <!-- 主要内容区域 -->
    <div class="not-found-content">
      <!-- 错误代码和标题 -->
      <div class="error-code">
        <span class="digit">4</span>
        <span class="digit oops">0</span>
        <span class="digit">4</span>
      </div>
      
      <h1 class="error-title">页面未找到</h1>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被移除
      </p>
      
      <!-- 装饰性图标 -->
      <div class="error-icon">
        <el-icon class="icon-404"><WarningFilled /></el-icon>
      </div>
      
      <!-- 搜索功能 -->
      <div class="search-container">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索您需要的内容..."
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button 
              type="primary" 
              icon="Search" 
              @click="handleSearch"
            >
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 快速链接 -->
      <div class="quick-links">
        <h3>热门导航</h3>
        <div class="links-grid">
          <el-link 
            v-for="link in quickLinks" 
            :key="link.path"
            :to="link.path"
            class="link-item"
            @click="handleLinkClick(link)"
          >
            <div class="link-icon">
              <el-icon :size="20"><component :is="link.icon" /></el-icon>
            </div>
            <span class="link-text">{{ link.title }}</span>
          </el-link>
        </div>
      </div>
      
      <!-- 返回首页按钮 -->
      <el-button 
        type="primary" 
        class="back-home-btn"
        @click="goBackHome"
        :icon="HomeFilled"
      >
        返回首页
      </el-button>
      
      <!-- 尝试返回上一页 -->
      <el-button 
        class="back-prev-btn"
        @click="goBackPrevious"
        :icon="ArrowLeft"
      >
        返回上一页
      </el-button>
    </div>
    
    <!-- 装饰元素 -->
    <div class="not-found-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="decoration-circle circle-4"></div>
    </div>
    
    <!-- 错误报告对话框 -->
    <el-dialog
      v-model="showErrorReport"
      title="报告错误"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="errorFormRef" 
        :model="errorForm" 
        :rules="errorRules" 
        class="error-form"
        label-position="top"
      >
        <el-form-item prop="description">
          <el-input 
            v-model="errorForm.description" 
            type="textarea" 
            rows="4"
            placeholder="请描述您遇到的问题..."
          />
        </el-form-item>
        
        <el-form-item prop="contact">
          <el-input 
            v-model="errorForm.contact" 
            placeholder="留下您的邮箱或联系方式（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showErrorReport = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitErrorReport"
          :loading="submittingReport"
        >
          {{ submittingReport ? '提交中...' : '提交报告' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 路由
const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const showErrorReport = ref(false)
const errorFormRef = ref(null)
const submittingReport = ref(false)

// 错误报告表单
const errorForm = reactive({
  description: '',
  contact: ''
})

// 表单验证规则
const errorRules = reactive({
  description: [
    { required: true, message: '请描述您遇到的问题', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度为10-500个字符', trigger: 'blur' }
  ],
  contact: [
    { pattern: /^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
})

// 快速链接数据
const quickLinks = [
  { title: '仪表盘', path: '/dashboard', icon: '📊' },
  { title: '个人中心', path: '/user/profile', icon: '👤' },
  { title: '训练数据', path: '/training-data', icon: '📈' },
  { title: '历史统计', path: '/history-statistics', icon: '📅' },
  { title: '训练计划', path: '/training-plan', icon: '⭐' },
  { title: '系统设置', path: '/settings', icon: '⚙️' }
]

// 处理搜索
const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  
  // 这里可以实现搜索逻辑
  // 例如跳转到搜索结果页面或在当前页面显示搜索结果
  ElMessage.info(`搜索: ${searchQuery.value}`)
  
  // 模拟搜索延迟
  setTimeout(() => {
    // 这里可以根据实际搜索结果进行处理
    // 例如：router.push({ path: '/search', query: { q: searchQuery.value } })
    ElMessage.warning('暂未找到相关内容，请尝试其他关键词')
  }, 800)
}

// 处理链接点击
const handleLinkClick = (link) => {
  console.log('点击了链接:', link.title)
  // 可以在这里添加链接点击的统计或其他逻辑
}

// 返回首页
const goBackHome = () => {
  router.push('/dashboard')
}

// 返回上一页
const goBackPrevious = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    // 如果没有上一页历史记录，返回首页
    router.push('/dashboard')
  }
}

// 提交错误报告
const submitErrorReport = async () => {
  try {
    await errorFormRef.value.validate()
    submittingReport.value = true
    
    // 模拟提交错误报告
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    console.log('提交错误报告:', errorForm)
    
    // 显示成功消息
    ElMessage.success('感谢您的反馈，我们会尽快处理！')
    
    // 关闭对话框并重置表单
    showErrorReport.value = false
    resetErrorForm()
  } catch (error) {
    console.error('提交错误报告失败:', error)
  } finally {
    submittingReport.value = false
  }
}

// 重置错误报告表单
const resetErrorForm = () => {
  if (errorFormRef.value) {
    errorFormRef.value.resetFields()
  }
  Object.assign(errorForm, {
    description: '',
    contact: ''
  })
}

// 生命周期
onMounted(() => {
  // 记录404错误
  console.warn('404错误: 用户访问了不存在的页面')
  
  // 可以在这里添加错误日志记录或分析
  
  // 添加页面动画效果
  setTimeout(() => {
    const notFoundContent = document.querySelector('.not-found-content')
    if (notFoundContent) {
      notFoundContent.classList.add('animate-in')
    }
  }, 100)
  
  // 随机生成一些漂浮动画的延迟
  const circles = document.querySelectorAll('.decoration-circle')
  circles.forEach((circle, index) => {
    circle.style.animationDelay = `${index * 0.5}s`
  })
})
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color-light) 0%, var(--primary-color) 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
  font-family: var(--font-family);
}

.not-found-content {
  text-align: center;
  max-width: 600px;
  width: 100%;
  position: relative;
  z-index: 10;
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}

.not-found-content.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.error-code {
  display: flex;
  justify-content: center;
  align-items: baseline;
  margin-bottom: 20px;
  perspective: 1000px;
}

.digit {
  font-size: 120px;
  font-weight: 800;
  color: var(--primary-dark);
  line-height: 1;
  display: inline-block;
  margin: 0 10px;
  text-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  transform-style: preserve-3d;
  transition: transform 0.3s ease;
}

.digit:hover {
  transform: translateY(-10px) rotateY(15deg);
}

.digit.oops {
  color: var(--danger-color);
  animation: pulse 2s infinite;
  position: relative;
}

.digit.oops::after {
  content: '!';
  position: absolute;
  font-size: 30px;
  top: 10px;
  right: -20px;
  color: var(--danger-color);
  transform: rotateZ(30deg);
  animation: bounce 2s infinite;
}

.error-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--primary-text-color);
  margin: 0 0 15px;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.error-description {
  font-size: 18px;
  color: var(--secondary-text-color);
  margin: 0 0 30px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.error-icon {
  margin-bottom: 30px;
}

.icon-404 {
  font-size: 80px;
  color: var(--warning-color);
  opacity: 0.9;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
  animation: float 3s ease-in-out infinite;
}

.search-container {
  max-width: 400px;
  margin: 0 auto 40px;
}

.search-input {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow-md);
  transition: all var(--transition-time);
}

.search-input:focus-within {
  box-shadow: var(--box-shadow-lg);
}

.quick-links {
  background: var(--white);
  border-radius: var(--border-radius-lg);
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: var(--box-shadow-lg);
  transition: transform var(--transition-time), box-shadow var(--transition-time);
}

.quick-links:hover {
  transform: translateY(-5px);
  box-shadow: var(--box-shadow-xl);
}

.quick-links h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-text-color);
  margin: 0 0 20px;
  position: relative;
}

.quick-links h3::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: 3px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  border-radius: var(--border-radius);
  transition: all var(--transition-time);
  background: var(--input-bg-color);
  color: var(--secondary-text-color);
  text-decoration: none;
  border: 2px solid transparent;
}

.link-item:hover {
  background: var(--primary-color-light);
  color: var(--primary-color);
  transform: translateY(-3px);
  border-color: var(--primary-color);
  box-shadow: var(--box-shadow-sm);
}

.link-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--white);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  box-shadow: var(--box-shadow-sm);
  transition: all var(--transition-time);
}

.link-item:hover .link-icon {
  background: var(--primary-color);
  color: var(--white);
  transform: scale(1.1);
}

.link-text {
  font-size: 14px;
  font-weight: 500;
  transition: color var(--transition-time);
}

.back-home-btn,
.back-prev-btn {
  margin: 0 10px 10px;
  padding: 10px 25px;
  font-size: 16px;
  transition: all var(--transition-time);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-md);
}

.back-home-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border: none;
}

.back-home-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--box-shadow-lg);
}

.back-prev-btn {
  background: var(--white);
  border: 1px solid var(--border-color);
}

.back-prev-btn:hover:not(:disabled) {
  background: var(--input-bg-color);
  transform: translateY(-2px);
  box-shadow: var(--box-shadow-lg);
}

.not-found-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -200px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  left: -100px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 30%;
  left: 10%;
  background: rgba(255, 255, 255, 0.15);
}

.circle-4 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  right: 15%;
  background: rgba(255, 255, 255, 0.12);
}

.error-form {
  margin-top: 10px;
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0) rotateZ(30deg);
  }
  50% {
    transform: translateY(-10px) rotateZ(30deg);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-15px) translateX(10px);
  }
  50% {
    transform: translateY(0) translateX(20px);
  }
  75% {
    transform: translateY(15px) translateX(10px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .digit {
    font-size: 80px;
    margin: 0 5px;
  }
  
  .error-title {
    font-size: 28px;
  }
  
  .error-description {
    font-size: 16px;
  }
  
  .icon-404 {
    font-size: 60px;
  }
  
  .links-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .quick-links {
    padding: 20px;
  }
  
  .back-home-btn,
  .back-prev-btn {
    margin: 10px;
    width: calc(100% - 20px);
  }
}

@media (max-width: 480px) {
  .not-found-container {
    padding: 10px;
  }
  
  .digit {
    font-size: 60px;
    margin: 0 2px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-description {
    font-size: 14px;
  }
  
  .icon-404 {
    font-size: 50px;
  }
  
  .links-grid {
    grid-template-columns: 1fr;
  }
  
  .circle-1 {
    width: 250px;
    height: 250px;
    top: -125px;
    right: -75px;
  }
  
  .circle-2 {
    width: 200px;
    height: 200px;
    bottom: -100px;
    left: -75px;
  }
  
  .circle-3,
  .circle-4 {
    display: none;
  }
}

/* 加载动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.not-found-content > * {
  animation: fadeIn 0.6s ease-out forwards;
  opacity: 0;
}

.error-code {
  animation-delay: 0.2s;
}

.error-title {
  animation-delay: 0.4s;
}

.error-description {
  animation-delay: 0.6s;
}

.error-icon {
  animation-delay: 0.8s;
}

.search-container {
  animation-delay: 1s;
}

.quick-links {
  animation-delay: 1.2s;
}

.back-home-btn,
.back-prev-btn {
  animation-delay: 1.4s;
}
</style>