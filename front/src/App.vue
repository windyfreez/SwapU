<template>
  <div class="app-container">
    <template v-if="isFullPage">
      <router-view />
    </template>
    <template v-else>
      <div class="content-wrapper">
        <router-view />
        <div class="tab-bar-wrapper">
          <TabBar />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from './components/TabBar.vue'

const router = useRouter()

const isFullPage = computed(() => {
  const fullPageRoutes = ['/login', '/register', '/profile/edit', '/product/:id']
  const currentPath = router.currentRoute.value.path
  return fullPageRoutes.includes(currentPath) || currentPath.startsWith('/product/')
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
}

.content-wrapper {
  max-width: 480px;
  margin: 0 auto;
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.tab-bar-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
}
</style>
