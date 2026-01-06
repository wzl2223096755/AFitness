import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 导入共享样式
import '@shared/styles/index.scss'
// 导入管理端样式
import './assets/styles/admin.scss'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)

app.mount('#app')
