<template>
  <nav class="mobile-tabbar">
    <router-link
      v-for="tab in tabs"
      :key="tab.to"
      :to="tab.to"
      class="tab-item"
      :class="{ active: isActive(tab) }"
    >
      <svg
        class="tab-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path :d="tab.icon" />
      </svg>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { to: '/recommend', icon: 'M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7z', label: '推荐' },
  { to: '/', icon: 'M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-4v-6H9v6H5a1 1 0 0 1-1-1z', label: '全部', exact: true },
  { to: '/sell', icon: 'M12 5v14M5 12h14', label: '发布' },
  { to: '/messages', icon: 'M5 4h14a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H9l-4 4z', label: '消息' },
  { to: '/profile', icon: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21c0-3.5 3.6-6 8-6s8 2.5 8 6', label: '我的' }
]

// 全部(首页路由)要精确匹配，其余按前缀匹配（/messages/chat 也算“消息”选中）
// 账户相关页面（我的发布/收藏/订单/设置等）统一高亮「我的」
const isActive = (tab) => {
  if (tab.exact) return route.path === tab.to
  if (tab.to === '/profile') {
    return (
      route.path.startsWith('/profile') ||
      route.path.startsWith('/my-') ||
      route.path === '/settings'
    )
  }
  return route.path.startsWith(tab.to)
}
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
  width: 22px;
  height: 22px;
}

.tab-label {
  font-size: 11px;
  line-height: 1.2;
}

.tab-item.active .tab-label {
  font-weight: 600;
}
</style>
