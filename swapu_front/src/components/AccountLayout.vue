<template>
  <div class="container account-layout">
    <aside class="account-sidebar">
      <div class="user-card">
        <template v-if="loggedIn">
          <div class="user-avatar">
            <img v-if="user.avatar" :src="user.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="user-name">{{ user.nickname || user.username || '用户' }}</div>
          <div class="user-meta">学号 {{ user.studentId || '—' }}</div>
          <div class="user-credit"><span class="credit-label">信用分</span><span class="credit-value">{{ user.creditScore || 100 }}</span></div>
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

      <nav class="side-menu">
        <div v-for="group in menuGroups" :key="group.label" class="menu-group">
          <span class="menu-group-label">{{ group.label }}</span>
          <router-link
            v-for="item in group.items"
            :key="item.key"
            :to="item.to"
            class="side-link"
            :class="{ active: item.key === active }"
          >{{ item.label }}</router-link>
        </div>
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

const menuGroups = [
  { label: '交易', items: [
    { key: 'products', label: '我的发布', to: '/my-products' },
    { key: 'orders', label: '我的订单', to: '/my-orders' }
  ] },
  { label: '收藏', items: [
    { key: 'favorites', label: '我的收藏', to: '/my-favorites' },
    { key: 'footprints', label: '我的足迹', to: '/my-footprints' },
    { key: 'comments', label: '我的评价', to: '/my-comments' }
  ] },
  { label: '账户', items: [
    { key: 'wallet', label: '我的钱包', to: '/my-wallet' },
    { key: 'address', label: '收货地址', to: '/my-address' },
    { key: 'profile', label: '编辑资料', to: '/profile/edit' },
    { key: 'settings', label: '设置', to: '/settings' }
  ] }
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
  gap: 56px;
  padding-top: 42px;
}

.account-sidebar {
  width: 190px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--nav-height) + 24px);
}

.user-card {
  padding: 0 0 28px;
  text-align: left;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--c-border);
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
  margin: 0 0 12px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 17px;
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
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
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
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.menu-group { display: flex; flex-direction: column; gap: 3px; }
.menu-group-label { color: var(--c-text-3); font-size: 11px; font-weight: 700; letter-spacing: 1.5px; margin-bottom: 4px; }

.side-link {
  display: flex;
  align-items: center;
  padding: 8px 0 8px 12px;
  border-left: 2px solid transparent;
  font-size: 14px;
  color: var(--c-text-2);
  transition: all 0.15s;
}

.side-link:hover {
  color: var(--c-primary);
}

.side-link.active {
  color: var(--c-primary);
  font-weight: 600;
  border-left-color: var(--c-primary);
}

.account-content {
  flex: 1;
  min-width: 0;
}

/* 窄屏降级:侧边栏变顶部横向菜单 */
@media (max-width: 860px) {
  .account-layout {
    flex-direction: column;
    gap: 20px;
  }

  .account-sidebar {
    width: 100%;
    position: static;
  }

  .user-card {
    display: flex;
    align-items: center;
    text-align: left;
    padding: 0 0 16px;
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
    flex-direction: row;
    gap: 20px;
    width: 100%;
  }

  .menu-group { flex-direction: row; gap: 2px; }
  .menu-group-label { display: none; }

  .side-link {
    white-space: nowrap;
    flex-shrink: 0;
    padding: 7px 10px;
    border-left: 0;
    border-bottom: 2px solid transparent;
  }

  .side-link.active {
    border-left-color: transparent;
    border-bottom-color: var(--c-primary);
  }
}
</style>
