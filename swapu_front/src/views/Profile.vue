<template>
  <AccountLayout active="home">
    <template v-if="!isLoggedIn">
      <div class="card login-prompt">
        <div class="prompt-icon">👤</div>
        <p>登录后享受更多服务</p>
        <div class="prompt-buttons">
          <button class="btn btn-outline" @click="goToRegister">注册</button>
          <button class="btn btn-primary" @click="goToLogin">登录</button>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- 用户信息卡 -->
      <div class="card profile-card">
        <div class="profile-main">
          <div class="avatar-large">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="user-detail">
            <h2>{{ userInfo.nickname || userInfo.username }}</h2>
            <p class="user-meta">学号 {{ userInfo.studentId || '—' }}</p>
            <p class="user-meta" v-if="userInfo.college">{{ userInfo.college }}</p>
          </div>
          <button class="btn btn-outline btn-sm edit-btn" @click="goToEditProfile">编辑资料</button>
        </div>

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
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value credit">{{ userInfo.creditScore || 100 }}</span>
            <span class="stat-label">信用分</span>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="quick-grid">
        <div class="card quick-item" @click="goToMyProducts">
          <span class="quick-icon">📦</span>
          <span class="quick-text">我的发布</span>
          <span class="quick-arrow">›</span>
        </div>
        <div class="card quick-item" @click="goToMyFavorites">
          <span class="quick-icon">❤️</span>
          <span class="quick-text">我的收藏</span>
          <span class="quick-arrow">›</span>
        </div>
        <div class="card quick-item" @click="goToMyOrders">
          <span class="quick-icon">🛒</span>
          <span class="quick-text">我的订单</span>
          <span class="quick-arrow">›</span>
        </div>
        <div class="card quick-item" @click="goToMyWallet">
          <span class="quick-icon">💰</span>
          <span class="quick-text">我的钱包</span>
          <span class="quick-arrow">›</span>
        </div>
        <div class="card quick-item" @click="goToMyAddress">
          <span class="quick-icon">📍</span>
          <span class="quick-text">收货地址</span>
          <span class="quick-arrow">›</span>
        </div>
        <div class="card quick-item" @click="goToSettings">
          <span class="quick-icon">⚙️</span>
          <span class="quick-text">设置</span>
          <span class="quick-arrow">›</span>
        </div>
      </div>
    </template>
  </AccountLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AccountLayout from '../components/AccountLayout.vue'

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

const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/register')
const goToEditProfile = () => router.push('/profile/edit')
const goToMyProducts = () => router.push('/my-products')
const goToMyFavorites = () => router.push('/my-favorites')
const goToMyOrders = () => router.push('/my-orders')
const goToMyWallet = () => router.push('/my-wallet')
const goToMyAddress = () => router.push('/my-address')
const goToSettings = () => router.push('/settings')

const loadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    isLoggedIn.value = false
    return
  }

  try {
    const response = await fetch('/user/info', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      if (response.status === 401) {
        alert('登录状态已过期，请重新登录')
      }
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      isLoggedIn.value = false
      return
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
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
.login-prompt {
  padding: 60px 20px;
  text-align: center;
}

.prompt-icon {
  font-size: 56px;
  margin-bottom: 14px;
}

.login-prompt p {
  font-size: 16px;
  color: var(--c-text-2);
  margin-bottom: 24px;
}

.prompt-buttons {
  display: inline-flex;
  gap: 14px;
}

.profile-card {
  padding: 28px 30px 0;
}

.profile-main {
  display: flex;
  align-items: center;
  padding-bottom: 24px;
}

.avatar-large {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  margin-right: 18px;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.user-detail h2 {
  font-size: 21px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 4px;
}

.user-meta {
  font-size: 13px;
  color: var(--c-text-3);
}

.stats-row {
  display: flex;
  align-items: center;
  border-top: 1px solid var(--c-border);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-text);
}

.stat-value.credit {
  color: var(--c-warning);
}

.stat-label {
  font-size: 12px;
  color: var(--c-text-3);
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--c-border);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.quick-item {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-border-strong);
}

.quick-icon {
  font-size: 22px;
  margin-right: 14px;
}

.quick-text {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--c-text);
}

.quick-arrow {
  font-size: 18px;
  color: var(--c-text-3);
}
</style>
