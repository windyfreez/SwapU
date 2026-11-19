<template>
  <header class="navbar">
    <div class="navbar-inner container">
      <router-link to="/" class="brand">
        <span class="brand-name">SwapU<em>云市集</em></span>
      </router-link>

      <nav class="nav-links">
        <!-- 推荐放在最前,首页(全部商品)紧随其后 -->
        <router-link to="/recommend" class="nav-link" active-class="active">推荐</router-link>
        <router-link to="/" class="nav-link" exact-active-class="active">全部</router-link>
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
        <template v-if="loggedIn">
          <div class="user-menu" ref="menuRef" @click="menuOpen = !menuOpen">
            <div class="user-avatar">
              <img v-if="user.avatar" :src="user.avatar" alt="头像" />
              <span v-else>{{ (user.nickname || user.username || '我').slice(0, 1) }}</span>
            </div>
            <span class="user-name">{{ user.nickname || user.username || '用户' }}</span>
            <span class="caret" :class="{ up: menuOpen }">▾</span>
            <transition name="dropdown">
              <div v-if="menuOpen" class="dropdown">
                <router-link v-for="item in menuItems" :key="item.to" :to="item.to" class="dropdown-item" @click="menuOpen = false">
                  {{ item.label }}
                </router-link>
                <div class="dropdown-divider"></div>
                <div class="dropdown-item" @click="handleLogout">
                  退出登录
                </div>
              </div>
            </transition>
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
  background: var(--c-primary-dark);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 16px rgba(15, 36, 76, 0.18);
}

/* 深蓝底上统一白色焦点环 */
.navbar :focus-visible {
  outline: 2px solid #fff;
  outline-offset: 2px;
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
  color: #bfdbfe;
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
  color: rgba(255, 255, 255, 0.78);
  border-radius: 6px;
  transition: background 0.2s, color 0.2s;
}

.nav-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.nav-link.active {
  color: var(--c-primary-dark);
  font-weight: 600;
  background: #fff;
}

/* 搜索 */
.nav-search {
  flex: 1;
  max-width: 360px;
  display: flex;
  align-items: center;
  height: 38px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.nav-search:focus-within {
  border-color: #fff;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.28);
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

.nav-search input::placeholder {
  color: var(--c-text-2);
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

/* 用户区 */
.nav-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  flex-shrink: 0;
}

/* 显示模式切换按钮：深蓝底上的透明描边按钮 */
.navbar .layout-toggle {
  height: 34px;
  padding: 0 14px;
  font-size: 13px;
  flex-shrink: 0;
  border-radius: 6px;
  background: transparent;
  border-color: rgba(255, 255, 255, 0.35);
  color: #fff;
}

/* 覆盖全局 .btn:hover 的浅色底,避免悬停时变回白底卡片 */
.navbar .layout-toggle:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.6);
  color: #fff;
}

/* 手机版:顶栏分两行——第一行品牌 + 搜索框,第二行显示模式/主题/登录注册,
   页面导航交给底部 tab 栏。搜索框在手机版必须保留:
   首页路由(导航栏显示为「全部」)的筛选栏里没有关键词输入框,
   顶栏搜索框是手机端唯一能输入关键词的入口 */
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

/* 显示模式/登录注册整体换到第二行并靠右,与电脑版的位置习惯一致 */
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
  color: rgba(255, 255, 255, 0.85);
}

.login-link:hover {
  color: #fff;
  text-decoration: underline;
}

.btn-register {
  border-radius: 6px;
  background: #fff;
  border-color: #fff;
  color: var(--c-primary-dark);
  font-weight: 600;
}

.btn-register:hover {
  background: var(--c-primary-light);
  border-color: var(--c-primary-light);
  color: var(--c-primary-dark);
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  user-select: none;
}

.user-menu:hover {
  background: rgba(255, 255, 255, 0.12);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
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
  color: rgba(255, 255, 255, 0.7);
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

/* 用户菜单展开/收起：160ms 位移淡入 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
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

  .nav-link {
    padding: 8px 10px;
    font-size: 14px;
  }

  .user-name,
  .caret {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .dropdown-enter-active,
  .dropdown-leave-active {
    transition-duration: 0.01ms;
  }
}
</style>
