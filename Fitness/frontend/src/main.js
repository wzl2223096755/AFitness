import { createApp } from 'vue'
import { createPinia } from 'pinia'
// 按需导入 - 不再全量导入Element Plus和Vant
// Element Plus和Vant组件会通过unplugin-vue-components自动按需导入
import App from './App.vue'
import router from './router'
// 导入自定义样式系统
import './style.css'
// 导入资源优化工具
import { initResourceOptimization } from './utils/resourceOptimization'
import { registerLazyDirectives } from './directives/lazyLoad'
// 导入PWA注册
import { registerSW } from 'virtual:pwa-register'
// 导入离线同步管理器
import { initSyncManager } from './utils/syncManager'
// 导入离线存储初始化
import { openDatabase } from './utils/offlineStorage'
// 导入错误监控服务
import { initErrorMonitoring } from './utils/errorMonitoring'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 初始化错误监控服务（在其他初始化之前）
initErrorMonitoring(app, router)

// 注册懒加载指令
registerLazyDirectives(app)

// 注册Service Worker
const updateSW = registerSW({
  onNeedRefresh() {
    // 当有新版本可用时提示用户
    if (confirm('发现新版本，是否立即更新？')) {
      updateSW(true)
    }
  },
  onOfflineReady() {
    console.log('应用已准备好离线使用')
  },
  onRegistered(registration) {
    console.log('Service Worker 注册成功:', registration)
  },
  onRegisterError(error) {
    console.error('Service Worker 注册失败:', error)
  }
})

// 初始化资源优化
initResourceOptimization({
  lazyLoadSelector: '[data-src], [data-bg-src], .lazy-image',
  preconnectOrigins: [
    'https://fonts.googleapis.com',
    'https://fonts.gstatic.com'
  ],
  preloadFonts: [
    {
      url: 'https://fonts.gstatic.com/s/inter/v13/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiJ-Ek-_EeA.woff2',
      type: 'font/woff2'
    }
  ]
})

// 初始化离线存储和同步管理器
openDatabase()
  .then(() => {
    console.log('离线存储数据库已初始化')
    initSyncManager()
  })
  .catch(error => {
    console.error('离线存储初始化失败:', error)
  })

app.mount('#app')
