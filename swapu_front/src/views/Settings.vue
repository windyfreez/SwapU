<template>
  <AccountLayout active="settings">
    <div class="menu-section card">
      <div class="menu-item" @click="goToEditProfile">
        <span class="menu-icon">✏️</span>
        <span class="menu-text">修改资料</span>
        <span class="menu-arrow">›</span>
      </div>
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

    <button v-if="isLoggedIn" class="sign-out-btn" @click="handleLogout">退出登录</button>
  </AccountLayout>
</template>

<script setup>
import AccountLayout from '../components/AccountLayout.vue'
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
.menu-section {
  padding: 8px 4px;
  margin-bottom: 20px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.15s;
}

.menu-item:hover {
  background: #f5f6f8;
}

.menu-item + .menu-item {
  border-top: 1px solid var(--c-border);
  border-radius: 0;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: var(--c-text);
}

.menu-arrow {
  font-size: 18px;
  color: var(--c-text-3);
}

.sign-out-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 46px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  color: var(--c-danger);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.sign-out-btn:hover {
  background: var(--c-danger-light);
  border-color: var(--c-danger);
}
</style>
