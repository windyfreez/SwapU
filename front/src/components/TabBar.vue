<template>
  <nav class="tab-bar">
    <div 
      v-for="item in tabs" 
      :key="item.path"
      class="tab-item"
      :class="{ active: currentPath === item.path }"
      @click="navigate(item.path)"
    >
      <span class="tab-icon">{{ item.icon }}</span>
      <span class="tab-text">{{ item.text }}</span>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentPath = ref('/')

const tabs = [
  { path: '/', icon: '🏠', text: '首页' },
  { path: '/sell', icon: '📦', text: '卖闲置' },
  { path: '/messages', icon: '💬', text: '消息' },
  { path: '/profile', icon: '👤', text: '我的' }
]

const navigate = (path) => {
  currentPath.value = path
  router.push(path)
}

const handleRouteChange = () => {
  currentPath.value = router.currentRoute.value.path
}

onMounted(() => {
  currentPath.value = router.currentRoute.value.path
  router.afterEach(handleRouteChange)
})

onUnmounted(() => {
  router.afterEach(() => {})
})
</script>

<style scoped>
.tab-bar {
  width: 100%;
  height: 60px;
  background: white;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.08);
  padding-bottom: env(safe-area-inset-bottom);
  border-radius: 16px 16px 0 0;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  transition: all 0.3s ease;
}

.tab-icon {
  font-size: 22px;
  margin-bottom: 4px;
}

.tab-text {
  font-size: 11px;
  color: #666;
}

.tab-item.active .tab-text {
  color: #2563eb;
  font-weight: 500;
}

.tab-item.active .tab-icon {
  transform: scale(1.1);
}
</style>