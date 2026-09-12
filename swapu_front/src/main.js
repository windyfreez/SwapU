import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import './assets/main.css'
import { initLayout } from './utils/layout'

// 应用挂载前先恢复主题与显示模式,避免首帧闪烁
// 浅色/深色由 localStorage('theme') 决定
document.documentElement.dataset.theme = localStorage.getItem('theme') === 'dark' ? 'dark' : 'light'
// 电脑版/手机版由 localStorage('layout') 决定(auto 时跟随屏宽)
initLayout()

const app = createApp(App)
app.use(router)
app.mount('#app')