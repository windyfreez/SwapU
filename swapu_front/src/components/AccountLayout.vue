<template>
  <div class="container account-layout">
    <aside class="account-sidebar">
      <div class="card user-card">
        <template v-if="loggedIn">
          <div class="user-avatar">
            <img v-if="user.avatar" :src="user.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="user-name">{{ user.nickname || user.username || '用户' }}</div>
          <div class="user-meta">学号 {{ user.studentId || '—' }}</div>
          <div class="user-credit">
            <span class="credit-label">信用分</span>
            <span class="credit-value">{{ user.creditScore || 100 }}</span>
          </div>
        </template>
        <template v-else>
          <div class="user-avatar"><span>👤</span></div>
          <div class="user-name">未登录</div>
          <div class="user-meta">登录后查看个人中心</div>
          <div class="login-btns">
            <router-link to="/login" class="btn btn-primary btn-block">登 录</router-link>
            <router-link to="/register" class="btn btn-outline btn-block">注 册</router-link>
          </div>
        </template>
      </div>

      <nav class="card side-menu">
        <router-link
          v-for="item in menu"
          :key="item.key"
          :to="item.to"
          class="side-link"
          :class="{ active: item.key === active }"
        >
          <span class="side-icon">{{ item.icon }}</span>
          <span class="side-text">{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>

    <div class="account-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

defineProps({
  active: { type: String, default: '' }
})

const loggedIn = ref(false)
const user = ref({})

const menu = [
  { key: 'products', label: '我的发布', icon: '📦', to: '/my-products' },
  { key: 'favorites', label: '我的收藏', icon: '❤️', to: '/my-favorites' },
  { key: 'footprints', label: '我的足迹', icon: '👣', to: '/my-footprints' },
  { key: 'orders', label: '我的订单', icon: '🛒', to: '/my-orders' },
  { key: 'wallet', label: '我的钱包', icon: '💰', to: '/my-wallet' },
  { key: 'address', label: '收货地址', icon: '📍', to: '/my-address' },
  { key: 'profile', label: '编辑资料', icon: '✏️', to: '/profile/edit' },
  { key: 'settings', label: '设置', icon: '⚙️', to: '/settings' }
]

onMounted(() => {
  loggedIn.value = !!localStorage.getItem('token')
  const saved = localStorage.getItem('userInfo')
  if (saved && saved !== 'undefined' && saved !== 'null') {
    try {
      user.value = JSON.parse(saved)
    } catch (e) {
      user.value = {}
    }
  }
})
</script>

<style scoped>
.account-layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding-top: 24px;
}

.account-sidebar {
  width: 230px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--nav-height) + 24px);
}

.user-card {
  padding: 24px 20px;
  text-align: center;
  margin-bottom: 16px;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 12px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-meta {
  font-size: 13px;
  color: var(--c-text-3);
  margin-bottom: 12px;
}

.user-credit {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  background: var(--c-warning-light);
  padding: 4px 14px;
  border-radius: 999px;
}

.credit-label {
  font-size: 12px;
  color: var(--c-warning);
}

.credit-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-warning);
}

.login-btns {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}

.side-menu {
  padding: 8px;
}

.side-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text-2);
  transition: all 0.15s;
}

.side-link:hover {
  background: var(--c-hover);
  color: var(--c-text);
}

.side-link.active {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}

.side-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.account-content {
  flex: 1;
  min-width: 0;
}

/* 窄屏降级:侧边栏变顶部横向菜单 */
@media (max-width: 860px) {
  .account-layout {
    flex-direction: column;
  }

  .account-sidebar {
    width: 100%;
    position: static;
  }

  .user-card {
    display: flex;
    align-items: center;
    text-align: left;
    padding: 16px;
  }

  .user-avatar {
    margin: 0 14px 0 0;
    width: 52px;
    height: 52px;
  }

  .user-meta {
    margin-bottom: 0;
  }

  .side-menu {
    display: flex;
    overflow-x: auto;
    padding: 6px;
  }

  .side-link {
    white-space: nowrap;
    flex-shrink: 0;
  }
}
</style>
