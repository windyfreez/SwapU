<template>
  <div class="settings-page">
    <header class="settings-header">
      <button class="back-btn" @click="goBack">‹</button>
      <span class="header-title">设置</span>
      <span class="placeholder"></span>
    </header>

    <div class="menu-section">
      <div class="menu-item" @click="goToEditProfile">
        <span class="menu-icon">✏️</span>
        <span class="menu-text">修改资料</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item">
        <span class="menu-icon">❓</span>
        <span class="menu-text">帮助中心</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">👤</span>
        <span class="menu-text">关于我们</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div v-if="isLoggedIn" class="sign-out-btn" @click="handleLogout">
      退出登录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(false)

const goBack = () => {
  router.back()
}

const goToEditProfile = () => {
  router.push('/profile/edit')
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    isLoggedIn.value = false
    alert('已退出登录')
    router.push('/login')
  }
}

onMounted(() => {
  isLoggedIn.value = !!localStorage.getItem('token')
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #fafafa;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-top: calc(15px + env(safe-area-inset-top));
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #1a1a1a;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.placeholder {
  width: 36px;
}

.menu-section {
  background: white;
  margin: 0 15px 10px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #1a1a1a;
}

.menu-arrow {
  font-size: 18px;
  color: #ddd;
}

.sign-out-btn {
  margin: 20px 15px;
  padding: 15px;
  text-align: center;
  background: white;
  border-radius: 10px;
  color: #999;
  font-size: 15px;
  border: 1px solid #f0f0f0;
}
</style>