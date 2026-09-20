import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 浏览器在地址栏访问或刷新前端路由(如 /user-home/1、/product/5)时，Accept 头带 text/html，
// 说明要的是页面而不是接口数据，直接交给前端路由处理；
// 否则会被下面的代理按前缀(如 /user、/product)转发到后端，导致刷新页面 404。
// fetch 请求的 Accept 是 */*，不受影响，仍照常代理到后端。
const spaBypass = (req) => {
  if (req.headers.accept && req.headers.accept.includes('text/html')) {
    return '/index.html'
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
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/product': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: spaBypass
      },
      '/category': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/favorite': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/comment': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/order': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: spaBypass
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: spaBypass
      },
      '/address': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/view-history': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 聊天消息实时推送走 WebSocket，代理必须开启 ws 才会转发 Upgrade 请求
        ws: true
      }
    }
  }
})
