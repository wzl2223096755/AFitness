import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

console.log('🚀 开始初始化Vue应用...')

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

console.log('📦 插件安装完成，开始挂载应用...')

try {
  app.mount('#app')
  console.log('✅ Vue应用挂载成功!')
} catch (error) {
  console.error('❌ Vue应用挂载失败:', error)
  
  // 显示错误信息到页面
  const appElement = document.getElementById('app')
  if (appElement) {
    appElement.innerHTML = `
      <div style="padding: 20px; background: #fee; border: 1px solid #fcc; border-radius: 8px; margin: 20px;">
        <h2 style="color: #c33;">❌ 应用初始化失败</h2>
        <p style="color: #666;">错误信息: ${error.message}</p>
        <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow: auto;">${error.stack}</pre>
      </div>
    `
  }
}

// 添加全局错误处理
window.addEventListener('error', (event) => {
  console.error('🔥 全局错误:', event.error)
})

window.addEventListener('unhandledrejection', (event) => {
  console.error('⚠️ 未处理的Promise错误:', event.reason)
})
