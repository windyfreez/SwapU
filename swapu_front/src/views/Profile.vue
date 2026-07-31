<template>
  <div class="profile-page">
    <template v-if="!isLoggedIn">
      <div class="login-prompt">
        <div class="login-card">
          <div class="login-icon">👤</div>
          <p>登录后享受更多服务</p>
          <div class="login-buttons">
            <button class="btn-secondary" @click="goToRegister">注册</button>
            <button class="btn-primary" @click="goToLogin">登录</button>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <header class="profile-header">
        <div class="user-info">
          <div class="avatar-large">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="user-detail">
            <h2>{{ userInfo.nickname || userInfo.username }}</h2>
            <p>学号: {{ userInfo.studentId }}</p>
          </div>
          <button class="edit-profile-btn" @click="goToEditProfile">编辑资料</button>
        </div>
      </header>

      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.publishCount || 0 }}</span>
          <span class="stat-label">在售</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.soldCount || 0 }}</span>
          <span class="stat-label">已售</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.favoriteCount || 0 }}</span>
          <span class="stat-label">收藏</span>
        </div>
      </div>

      <div class="credit-section">
        <div class="credit-info">
          <span class="credit-icon">⭐</span>
          <div>
            <span class="credit-label">信用分</span>
            <span class="credit-value">{{ userInfo.creditScore || 100 }}</span>
          </div>
        </div>
      </div>
    </template>

    <div class="menu-section">
      <div class="menu-item" @click="goToMyProducts">
        <span class="menu-icon">📦</span>
        <span class="menu-text">我的发布</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="goToMyFavorites">
        <span class="menu-icon">❤️</span>
        <span class="menu-text">我的收藏</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="goToMyOrders">
        <span class="menu-icon">🛒</span>
        <span class="menu-text">我的订单</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item" @click="goToMyWallet">
        <span class="menu-icon">💰</span>
        <span class="menu-text">我的钱包</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="goToMyAddress">
        <span class="menu-icon">📍</span>
        <span class="menu-text">收货地址</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item" @click="goToSettings">
        <span class="menu-icon">⚙️</span>
        <span class="menu-text">设置</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(false)
const userInfo = reactive({
  id: '',
  username: '',
  studentId: '',
  phone: '',
  email: '',
  avatar: '',
  college: '',
  balance: 0,
  creditScore: 100,
  publishCount: 0,
  soldCount: 0,
  favoriteCount: 0,
  createTime: ''
})


const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const goToEditProfile = () => {
  router.push('/profile/edit')
}

const goToMyProducts = () => {
  router.push('/my-products')
}

const goToMyFavorites = () => {
  router.push('/my-favorites')
}

const goToMyOrders = () => {
  router.push('/my-orders')
}

const goToMyWallet = () => {
  router.push('/my-wallet')
}

const goToMyAddress = () => {
  router.push('/my-address')
}

const goToSettings = () => {
  router.push('/settings')
}

const loadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    isLoggedIn.value = false
    return
  }

  console.log('Token found:', token ? 'Yes' : 'No')
  console.log('Token length:', token ? token.length : 0)

  try {
    const response = await fetch('/user/info', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      console.error('HTTP error:', response.status)
      if (response.status === 401) {
        console.warn('Token expired or invalid, clearing cache')
        alert('登录状态已过期，请重新登录')
      }
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      isLoggedIn.value = false
      return
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      const text = await response.text()
      console.error('非JSON响应:', text)
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      isLoggedIn.value = false
      return
    }

    const data = await response.json()
    
    if (data.code === 200) {
      Object.assign(userInfo, data.data)
      localStorage.setItem('userInfo', JSON.stringify(data.data))
      isLoggedIn.value = true
    } else {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      isLoggedIn.value = false
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    isLoggedIn.value = false
  }
}

onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo && savedUserInfo !== 'undefined' && savedUserInfo !== 'null') {
    try {
      const parsed = JSON.parse(savedUserInfo)
      if (parsed && typeof parsed === 'object') {
        Object.assign(userInfo, parsed)
        isLoggedIn.value = true
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    }
  }
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #fafafa;
}

.login-prompt {
  padding: 30px 15px;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  text-align: center;
  border: 1px solid #f0f0f0;
}

.login-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.login-card p {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.login-buttons {
  display: flex;
  gap: 12px;
}

.btn-secondary {
  flex: 1;
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.btn-primary {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: white;
}

.profile-header {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  padding: 30px 20px 40px;
  padding-top: calc(30px + env(safe-area-inset-top));
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar-large {
  width: 75px;
  height: 75px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-right: 15px;
}

.avatar-large img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.user-detail {
  flex: 1;
}

.user-detail h2 {
  font-size: 19px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.user-detail p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.edit-profile-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  padding: 7px 14px;
  font-size: 13px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  background: white;
  margin: -25px 15px 15px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  background: #f0f0f0;
}

.credit-section {
  background: white;
  margin: 0 15px 15px;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.credit-info {
  display: flex;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 15px;
}

.credit-icon {
  font-size: 28px;
  margin-right: 12px;
}

.credit-label {
  font-size: 13px;
  color: #666;
  display: block;
}

.credit-value {
  font-size: 24px;
  font-weight: 600;
  color: #f59e0b;
}

.balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-label {
  font-size: 14px;
  color: #666;
}

.balance-value {
  font-size: 20px;
  font-weight: 600;
  color: #2563eb;
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

.menu-badge {
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  padding: 3px 8px;
  border-radius: 10px;
  margin-right: 8px;
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