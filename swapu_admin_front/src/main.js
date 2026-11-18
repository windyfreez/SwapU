import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/base.css'
import './assets/admin.css'

createApp(App).use(router).mount('#app')
