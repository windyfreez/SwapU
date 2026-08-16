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
      <!-- 用户信息卡 + 统计(可点击跳转) -->
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
          <div class="stat-item link" @click="goToMyProducts">
            <span class="stat-value">{{ orderStats.publishCount }}</span>
            <span class="stat-label">在售</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item link" @click="goToMyProducts">
            <span class="stat-value">{{ orderStats.soldCount }}</span>
            <span class="stat-label">已售</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item link" @click="goToMyFavorites">
            <span class="stat-value">{{ orderStats.favoriteCount }}</span>
            <span class="stat-label">收藏</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item link" @click="goToMyFootprints">
            <span class="stat-value">{{ orderStats.footprintCount }}</span>
            <span class="stat-label">足迹</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value credit">{{ userInfo.creditScore || 100 }}</span>
            <span class="stat-label">信用分</span>
          </div>
        </div>
      </div>

      <!-- 内容预览:最近足迹 / 在售商品 -->
      <div class="preview-grid">
        <div class="card preview-panel">
          <div class="preview-header">
            <h3>👣 最近足迹</h3>
            <router-link to="/my-footprints" class="more-link">查看更多 ›</router-link>
          </div>
          <div v-if="footprintPreview.length > 0" class="preview-list">
            <div
              v-for="item in footprintPreview"
              :key="'fp-' + item.id"
              class="preview-item"
              @click="goToDetail(item.id)"
            >
              <div class="preview-thumb">
                <img :src="getFirstImage(item.images)" :alt="item.title" />
              </div>
              <div class="preview-info">
                <span class="preview-title">{{ item.title || '商品' + item.id }}</span>
                <span class="preview-price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
          <div v-else class="preview-empty">
            <span>暂无浏览记录</span>
            <router-link to="/" class="text-link">去逛逛</router-link>
          </div>
        </div>

        <div class="card preview-panel">
          <div class="preview-header">
            <h3>📦 在售商品</h3>
            <router-link to="/my-products" class="more-link">查看更多 ›</router-link>
          </div>
          <div v-if="onSalePreview.length > 0" class="preview-list">
            <div
              v-for="item in onSalePreview"
              :key="'sale-' + item.id"
              class="preview-item"
              @click="goToDetail(item.id)"
            >
              <div class="preview-thumb">
                <img :src="getFirstImage(item.images)" :alt="item.title" />
              </div>
              <div class="preview-info">
                <span class="preview-title">{{ item.title || '商品' + item.id }}</span>
                <span class="preview-price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
          <div v-else class="preview-empty">
            <span>暂无在售商品</span>
            <router-link to="/sell" class="text-link">去发布</router-link>
          </div>
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
const footprintPreview = ref([])
const onSalePreview = ref([])

// 统计数字(来自后端 /order/statistics)
const orderStats = reactive({
  publishCount: 0,
  soldCount: 0,
  favoriteCount: 0,
  footprintCount: 0
})

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
const goToMyFootprints = () => router.push('/my-footprints')
const goToDetail = (id) => router.push(`/product/${id}`)

const getFirstImage = (images) => {
  if (!images) return ''

  if (typeof images === 'string') {
    images = images.trim()
    if (images.startsWith('[') || images.startsWith('{')) {
      try {
        const parsed = JSON.parse(images)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return getFirstImage(parsed[0])
        }
        return parsed
      } catch {
        return images.replace(/[`'""]/g, '')
      }
    }
    return images.replace(/[`'""]/g, '')
  }

  if (Array.isArray(images) && images.length > 0) {
    return getFirstImage(images[0])
  }

  return String(images || '')
}

// 最近足迹预览(取前4条;总数由 /order/statistics 提供)
const fetchFootprintPreview = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/view-history/list?pageNum=1&pageSize=4', {
      headers: {
        'token': token
      }
    })
    if (!response.ok) return
    const data = await response.json()
    if (data.code === 200 && data.data) {
      footprintPreview.value = data.data.records || []
    }
  } catch (error) {
    console.error('获取足迹预览失败:', error)
  }
}

// 统计:在售/已售/我的收藏/足迹(后端 OrderController /order/statistics)
const fetchOrderStatistics = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/order/statistics', {
      headers: {
        'token': token
      }
    })
    if (!response.ok) return
    const data = await response.json()
    if (data.code === 200 && data.data) {
      orderStats.publishCount = data.data.publishCount || 0
      orderStats.soldCount = data.data.soldCount || 0
      orderStats.favoriteCount = data.data.favoriteCount || 0
      orderStats.footprintCount = data.data.footprintCount || 0
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 在售商品预览(取前4条)
const fetchOnSalePreview = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/product/my-products?page=1&pageSize=4&status=1', {
      headers: {
        'token': token
      }
    })
    if (!response.ok) return
    const data = await response.json()
    if (data.code === 200 && data.data) {
      onSalePreview.value = data.data.records || []
    }
  } catch (error) {
    console.error('获取在售商品预览失败:', error)
  }
}

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
  fetchOrderStatistics()
  fetchFootprintPreview()
  fetchOnSalePreview()
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

/* 用户信息卡 */
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

.stat-item.link {
  cursor: pointer;
  transition: background 0.15s;
  border-radius: var(--radius);
}

.stat-item.link:hover {
  background: var(--c-hover);
}

.stat-item.link:hover .stat-value {
  color: var(--c-primary);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-text);
  transition: color 0.15s;
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

/* 内容预览 */
.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 20px;
}

.preview-panel {
  overflow: hidden;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.preview-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.more-link {
  font-size: 13px;
  color: var(--c-primary);
  cursor: pointer;
}

.more-link:hover {
  color: var(--c-primary-hover);
}

.preview-list {
  padding: 8px 10px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 10px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.15s;
}

.preview-item:hover {
  background: var(--c-hover);
}

.preview-thumb {
  width: 52px;
  height: 52px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--c-disabled-bg);
  flex-shrink: 0;
}

.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.preview-title {
  font-size: 13px;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-price {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-danger);
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 34px 16px;
  font-size: 13px;
  color: var(--c-text-3);
}

/* 窄屏降级 */
@media (max-width: 1100px) {
  .preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
