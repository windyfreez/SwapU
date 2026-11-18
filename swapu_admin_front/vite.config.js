import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 管理端页面路由同样位于 /admin/*，与接口前缀相同。
// 浏览器直接访问或刷新页面时 Accept 带 text/html，说明要的是页面而不是接口，
// 交给前端路由处理；fetch 请求的 Accept 是 */*，仍按代理转发到 8081。
const spaBypass = (req) => {
  if (req.headers.accept && req.headers.accept.includes('text/html')) {
    return '/index.html'
  }
}

const proxy = {
  '/admin': {
    target: 'http://localhost:8081',
    changeOrigin: true,
    bypass: spaBypass
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5174,
    proxy
  },
  preview: {
    proxy
  }
})
