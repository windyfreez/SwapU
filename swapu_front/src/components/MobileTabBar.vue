<template>
  <nav class="mobile-tabbar">
    <router-link
      v-for="tab in tabs"
      :key="tab.to"
      :to="tab.to"
      class="tab-item"
      :class="{ active: isActive(tab) }"
    >
      <span class="tab-icon">{{ tab.icon }}</span>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { to: '/', icon: '🏠', label: '首页', exact: true },
  { to: '/recommend', icon: '✨', label: '推荐' },
  { to: '/sell', icon: '＋', label: '发布' },
  { to: '/messages', icon: '💬', label: '消息' },
  { to: '/profile', icon: '👤', label: '我的' }
]

// 首页要精确匹配，其余按前缀匹配（/messages/chat 也算“消息”选中）
const isActive = (tab) =>
  tab.exact ? route.path === tab.to : route.path.startsWith(tab.to)
</script>

<style scoped>
.mobile-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 900;
  display: flex;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--c-card);
  border-top: 1px solid var(--c-border);
  box-shadow: 0 -2px 8px rgba(16, 24, 40, 0.06);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--c-text-3);
  transition: color 0.15s;
}

.tab-item.active {
  color: var(--c-primary);
}

.tab-icon {
  font-size: 18px;
  line-height: 1;
}

.tab-label {
  font-size: 11px;
  line-height: 1.2;
}

.tab-item.active .tab-label {
  font-weight: 600;
}
</style>
