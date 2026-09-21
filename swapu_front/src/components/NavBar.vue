<template>
  <header class="navbar">
    <div class="navbar-inner container">
      <router-link to="/" class="brand">
        <span class="brand-name">SwapU<em>云市集</em></span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <router-link to="/recommend" class="nav-link" active-class="active">推荐</router-link>
        <router-link to="/sell" class="nav-link" active-class="active">发布</router-link>
        <router-link to="/messages" class="nav-link" active-class="active">消息</router-link>
        <router-link to="/profile" class="nav-link" active-class="active">我的</router-link>
      </nav>

      <div class="nav-search">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索好物..."
          enterkeyhint="search"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <div class="nav-user">
        <!-- 显示模式:自动 / 电脑版 / 手机版 -->
        <button
          class="btn layout-toggle"
          :title="'当前：' + layoutLabel + '，点击切换 自动 → 电脑版 → 手机版'"
          @click="cycleLayout"
        >
          {{ layoutLabel }}
        </button>
        <!-- 主题切换:浅色 / 深色,按钮与导航栏同色,只保留边框 -->
        <button
          class="btn theme-toggle"
          :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
          @click="toggleTheme"
        >
          <span class="theme-text">{{ isDark ? '深色模式' : '浅色模式' }}</span>
        </button>
        <template v-if="loggedIn">
          <div class="user-menu" ref="menuRef" @click="menuOpen = !menuOpen">
            <div class="user-avatar">
              <img v-if="user.avatar" :src="user.avatar" alt="头像" />
              <span v-else>👤</span>
            </div>
            <span class="user-name">{{ user.nickname || user.username || '用户' }}</span>
            <span class="caret" :class="{ up: menuOpen }">▾</span>
            <div v-if="menuOpen" class="dropdown">
              <router-link v-for="item in menuItems" :key="item.to" :to="item.to" class="dropdown-item" @click="menuOpen = false">
                <span class="dropdown-icon">{{ item.icon }}</span>
                {{ item.label }}
              </router-link>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item" @click="handleLogout">
                <span class="dropdown-icon">🚪</span>
                退出登录
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="login-link">登录</router-link>
          <router-link to="/register" class="btn btn-register btn-sm">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  LAYOUT_ORDER,
  LAYOUT_LABELS,
  getLayoutPreference,
  setLayoutPreference
} from '../utils/layout'

const router = useRouter()
const keyword = ref('')
const loggedIn = ref(false)
const user = ref({})
const menuOpen = ref(false)
const menuRef = ref(null)

// 主题:浅色 / 深色,偏好持久化到 localStorage('theme')
const isDark = ref(localStorage.getItem('theme') === 'dark')

const applyTheme = (dark) => {
  isDark.value = dark
  document.documentElement.dataset.theme = dark ? 'dark' : 'light'
  localStorage.setItem('theme', dark ? 'dark' : 'light')
}

const toggleTheme = () => applyTheme(!isDark.value)

// 显示模式:自动 / 电脑版 / 手机版,偏好持久化到 localStorage('layout')
const layoutPref = ref(getLayoutPreference())
const layoutLabel = computed(() => LAYOUT_LABELS[layoutPref.value] || '自动')

const cycleLayout = () => {
  const next = LAYOUT_ORDER[(LAYOUT_ORDER.indexOf(layoutPref.value) + 1) % LAYOUT_ORDER.length]
  setLayoutPreference(next)
}

// 显示模式可能在别处变化(自动模式跟随屏宽),同步按钮文案
const syncLayout = () => {
  layoutPref.value = getLayoutPreference()
}

const menuItems = [
  { to: '/my-products', icon: '📦', label: '我的发布' },
  { to: '/my-favorites', icon: '❤️', label: '我的收藏' },
  { to: '/my-footprints', icon: '👣', label: '我的足迹' },
  { to: '/my-orders', icon: '🛒', label: '我的订单' },
  { to: '/my-comments', icon: '💬', label: '我的评价' },
  { to: '/my-wallet', icon: '💰', label: '我的钱包' },
  { to: '/my-address', icon: '📍', label: '收货地址' },
  { to: '/profile/edit', icon: '✏️', label: '编辑资料' },
  { to: '/settings', icon: '⚙️', label: '设置' }
]

const loadUser = () => {
  loggedIn.value = !!localStorage.getItem('token')
  const saved = localStorage.getItem('userInfo')
  if (saved && saved !== 'undefined' && saved !== 'null') {
    try {
      user.value = JSON.parse(saved)
    } catch (e) {
      user.value = {}
    }
  } else {
    user.value = {}
  }
}

const handleSearch = () => {
  router.push({
    path: '/',
    query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {}
  })
}

const handleLogout = () => {
  if (!confirm('确定要退出登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
  loggedIn.value = false
  menuOpen.value = false
  router.push('/')
}

const handleClickOutside = (event) => {
  if (menuRef.value && !menuRef.value.contains(event.target)) {
    menuOpen.value = false
  }
}

onMounted(() => {
  // 刷新后恢复已保存的主题
  applyTheme(isDark.value)
  loadUser()
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('layout-change', syncLayout)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('layout-change', syncLayout)
})
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 900;
  height: var(--nav-height);
  background: linear-gradient(120deg, #2563eb 0%, #3b82f6 100%);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.navbar-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* Logo */
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.brand-logo {
  font-size: 26px;
}

.brand-name {
  font-size: 19px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.brand-name em {
  font-style: normal;
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin-left: 6px;
  letter-spacing: 0.5px;
}

/* 导航链接 */
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nav-link {
  padding: 8px 16px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  border-radius: var(--radius);
  transition: all 0.2s;
}

.nav-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.nav-link.active {
  color: #fff;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.22);
}

/* 搜索 */
.nav-search {
  flex: 1;
  max-width: 360px;
  display: flex;
  align-items: center;
  height: 38px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.nav-search:focus-within {
  border-color: #fff;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.25);
}

.nav-search input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  padding: 0 14px;
  font-size: 14px;
  color: var(--c-text);
}

.nav-search .search-btn {
  height: 100%;
  padding: 0 18px;
  border: none;
  background: transparent;
  color: var(--c-primary);
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

.nav-search .search-btn:hover {
  background: var(--c-primary-light);
}

/* 深色主题下搜索框随主题变深,保证输入文字可读 */
[data-theme='dark'] .nav-search {
  background: var(--c-input-bg);
  border-color: var(--c-border-strong);
}

[data-theme='dark'] .nav-search:focus-within {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.35);
}

/* 用户区 */
.nav-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  flex-shrink: 0;
}

/* 显示模式 / 主题切换按钮:与导航栏同色——背景透明,直接露出导航栏渐变,只保留一圈边框
   描边、文字、悬停配色对齐导航栏既有风格(.nav-link / .user-menu / .nav-search) */
.navbar .theme-toggle,
.navbar .layout-toggle {
  height: 34px;
  padding: 0 14px;
  font-size: 13px;
  flex-shrink: 0;
  background: transparent;
  border-color: rgba(255, 255, 255, 0.45);
  color: #fff;
}

/* 覆盖全局 .btn:hover 的浅色底,避免悬停时变回白底卡片 */
.navbar .theme-toggle:hover,
.navbar .layout-toggle:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.85);
}

/* 手机版:顶栏分两行——第一行品牌 + 搜索框,第二行显示模式/主题/登录注册,
   页面导航交给底部 tab 栏。搜索框在手机版必须保留:
   首页筛选栏里没有关键词输入框,顶栏搜索框是手机端唯一能输入关键词的入口 */
html[data-layout='mobile'] .navbar {
  height: auto;
}

/* 权重写成 .navbar .navbar-inner,避免与 main.css 里 .container 的内边距简写打平手 */
html[data-layout='mobile'] .navbar .navbar-inner {
  height: auto;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px 12px;
}

html[data-layout='mobile'] .nav-links {
  display: none;
}

html[data-layout='mobile'] .brand {
  order: 1;
}

/* 搜索框与品牌同处第一行,占满右侧剩余空间;
   flex-basis 写死 140px,换行位置才可预测(不被输入框固有宽度带偏) */
html[data-layout='mobile'] .nav-search {
  order: 2;
  display: flex;
  flex: 1 1 140px;
  min-width: 0;
  max-width: none;
}

/* 显示模式/主题/登录注册整体换到第二行并靠右,与电脑版的位置习惯一致 */
html[data-layout='mobile'] .nav-user {
  order: 3;
  justify-content: flex-end;
}

/* iOS 上字号小于 16px 的输入框聚焦时会自动放大整个页面 */
html[data-layout='mobile'] .nav-search input {
  font-size: 16px;
}

.login-link {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.login-link:hover {
  color: #fff;
  text-decoration: underline;
}

.btn-register {
  background: #fff;
  border-color: #fff;
  color: var(--c-primary);
  font-weight: 600;
}

.btn-register:hover {
  background: var(--c-primary-light);
  border-color: #fff;
  color: var(--c-primary-hover);
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s;
  user-select: none;
}

.user-menu:hover {
  background: rgba(255, 255, 255, 0.15);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  color: #fff;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  transition: transform 0.2s;
}

.caret.up {
  transform: rotate(180deg);
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 180px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 6px;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text);
  cursor: pointer;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: var(--c-hover);
  color: var(--c-primary);
}

.dropdown-icon {
  font-size: 15px;
}

.dropdown-divider {
  height: 1px;
  background: var(--c-border);
  margin: 6px 4px;
}

/* 窄屏降级 */
@media (max-width: 900px) {
  .nav-search {
    display: none;
  }

  /* 按钮只有文字,窄屏不再隐藏(隐藏会变成空边框),只压缩内边距 */
  .navbar .theme-toggle {
    padding: 0 10px;
  }

  .nav-link {
    padding: 8px 10px;
    font-size: 14px;
  }

  .user-name,
  .caret {
    display: none;
  }
}
</style>
