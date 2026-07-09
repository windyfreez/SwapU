<template>
  <div class="app-container">
    <div class="page-wrapper">
      <router-view />
    </div>
    <div v-if="showTabBar" class="tab-bar-wrapper">
      <TabBar />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from './components/TabBar.vue'

const router = useRouter()

const showTabBar = computed(() => {
  const noTabBarRoutes = ['/login', '/register', '/profile/edit', '/product/:id', '/product/edit/:id', '/order/create/:productId', '/order-detail/:orderNo', '/my-wallet', '/settings', '/messages/chat']
  const currentPath = router.currentRoute.value.path
  return !noTabBarRoutes.some(route => {
    if (route.includes(':')) {
      const routePrefix = route.split('/:')[0]
      return currentPath.startsWith(routePrefix + '/')
    }
    return currentPath === route
  })
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
  background-color: #f5f5f5;
}

.page-wrapper {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f5f5f5;
  position: relative;
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
