import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import './assets/main.css'

// 应用挂载前先恢复主题,避免首帧闪烁(浅色/深色由 localStorage('theme') 决定)
document.documentElement.dataset.theme = localStorage.getItem('theme') === 'dark' ? 'dark' : 'light'

const app = createApp(App)
app.use(router)
app.mount('#app')