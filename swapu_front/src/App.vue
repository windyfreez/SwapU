<template>
  <div class="app-shell" :class="{ 'app-shell-mobile': showTabBar }">
    <NavBar v-if="showChrome" />
    <main class="app-main">
      <router-view />
    </main>
    <footer v-if="showChrome" class="app-footer">
      <div class="container footer-inner">
        <span>© {{ year }} SwapU 云市集</span>
        <span>云端集市 · 让好物触手可及</span>
      </div>
    </footer>
    <!-- 手机版底部导航 -->
    <MobileTabBar v-if="showChrome && showTabBar" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import MobileTabBar from './components/MobileTabBar.vue'
import { getCurrentLayout, LAYOUT_MOBILE } from './utils/layout'

const route = useRoute()
const year = new Date().getFullYear()

// 登录/注册页使用独立的全屏布局,不显示导航与页脚
const showChrome = computed(() => !['/login', '/register'].includes(route.path))

// 当前生效的布局(电脑版/手机版),由 utils/layout 写入 html 属性并广播变化
const layout = ref(getCurrentLayout())
const isMobile = computed(() => layout.value === LAYOUT_MOBILE)
const syncLayout = () => {
  layout.value = getCurrentLayout()
}

// 聊天室在手机版下占满屏幕,隐藏底部导航并把留位去掉
const showTabBar = computed(() => isMobile.value && route.path !== '/messages/chat')

onMounted(() => window.addEventListener('layout-change', syncLayout))
onUnmounted(() => window.removeEventListener('layout-change', syncLayout))
</script>

<style>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
}

.app-footer {
  margin-top: 48px;
  border-top: 1px solid var(--c-border);
  background: var(--c-card);
  padding: 22px 0;
}

/* 手机版：底部导航固定占位，页面留出高度；页脚在手机版下隐藏 */
.app-shell-mobile {
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
}

.app-shell-mobile .app-footer {
  display: none;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--c-text-3);
}

@media (max-width: 768px) {
  .footer-inner {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
