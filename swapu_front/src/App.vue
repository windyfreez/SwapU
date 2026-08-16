<template>
  <div class="app-shell">
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'

const route = useRoute()
const year = new Date().getFullYear()

// 登录/注册页使用独立的全屏布局,不显示导航与页脚
const showChrome = computed(() => !['/login', '/register'].includes(route.path))
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
