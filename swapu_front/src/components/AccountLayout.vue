<template>
  <div class="container account-layout">
    <aside class="account-sidebar">
      <div v-if="showUser" class="user-card">
        <template v-if="loggedIn">
          <div class="user-avatar">
            <img v-if="user.avatar" :src="user.avatar" alt="头像" />
            <span v-else>{{ (user.nickname || user.username || '我').slice(0, 1) }}</span>
          </div>
          <div class="user-identity">
            <div class="user-name">{{ user.nickname || user.username || '用户' }}</div>
            <div class="user-meta">学号 {{ user.studentId || '—' }}</div>
          </div>
          <div class="user-credit"><span class="credit-label">信用分</span><span class="credit-value">{{ user.creditScore || 100 }}</span></div>
        </template>
        <template v-else>
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
              <circle cx="12" cy="8" r="4" />
              <path d="M4 20c0-4 3.6-6 8-6s8 2 8 6" />
            </svg>
          </div>
          <div class="user-identity">
            <div class="user-name">未登录</div>
            <div class="user-meta">登录后查看个人中心</div>
          </div>
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
  active: { type: String, default: '' },
  // 「我的」首页改用蓝色个人头图展示身份信息，侧栏不再重复用户卡
  showUser: { type: Boolean, default: true }
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
/* 账户中心外壳：与首页同一套白卡、8px 圆角和蓝色强调 */
.account-layout {
  display: flex;
  align-items: flex-start;
  gap: 32px;
  padding-top: 28px;
}

.account-sidebar {
  width: 200px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--nav-height) + 24px);
}

.account-sidebar :focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

/* 用户卡（其他账户页使用） */
.user-card {
  margin-bottom: 12px;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-card);
}

.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--c-primary-light);
  color: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 12px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar svg {
  width: 26px;
  height: 26px;
}

.user-identity {
  min-width: 0;
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
  color: var(--c-text-2);
  margin-bottom: 12px;
}

.user-credit {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
}

.credit-label {
  font-size: 12px;
  color: var(--c-text-2);
}

.credit-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-warning);
}

.login-btns {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

/* 侧栏菜单：一张白卡，选中项蓝底蓝字 */
.side-menu {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-card);
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-group-label {
  margin: 4px 8px;
  color: var(--c-text-2);
  font-size: 12px;
  font-weight: 600;
}

.side-link {
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 14px;
  color: var(--c-text-2);
  transition: background 0.15s, color 0.15s;
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

.account-content {
  flex: 1;
  min-width: 0;
}

/* 窄屏：侧栏变顶部横向菜单 */
@media (max-width: 860px) {
  .account-layout {
    flex-direction: column;
    gap: 16px;
    padding-top: 16px;
  }

  .account-sidebar {
    width: 100%;
    position: static;
  }

  .user-card {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px;
    margin-bottom: 12px;
  }

  .user-avatar {
    width: 48px;
    height: 48px;
    margin: 0;
    flex-shrink: 0;
  }

  .user-identity {
    flex: 1;
  }

  .user-name,
  .user-meta {
    margin-bottom: 0;
  }

  .user-credit {
    margin-left: auto;
    flex-direction: column;
    align-items: flex-end;
    gap: 2px;
    margin-top: 0;
    padding-top: 0;
    border: 0;
  }

  .login-btns {
    flex: 1 1 100%;
    margin-left: 0;
    margin-top: 4px;
    flex-direction: row;
  }

  .login-btns .btn {
    flex: 1;
  }

  .side-menu {
    flex-direction: row;
    gap: 8px;
    overflow-x: auto;
    padding: 8px;
  }

  .menu-group {
    flex-direction: row;
    gap: 4px;
  }

  .menu-group-label {
    display: none;
  }

  .side-link {
    white-space: nowrap;
    flex-shrink: 0;
  }
}
</style>
