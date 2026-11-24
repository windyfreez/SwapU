<template>
  <AccountLayout active="home" :show-user="false">
    <template v-if="!isLoggedIn">
      <div class="card login-prompt">
        <span class="login-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 20c0-4 3.6-6 8-6s8 2 8 6" />
          </svg>
        </span>
        <h2>登录后查看个人中心</h2>
        <p>登录后可查看发布、订单、收藏与足迹。</p>
        <div class="prompt-buttons">
          <button class="btn btn-primary" @click="goToLogin">登录</button>
          <button class="btn" @click="goToRegister">注册</button>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- 账户头图：左侧身份 + 右侧速览，和首页 banner 同一套 -->
      <section class="account-hero">
        <div class="hero-identity">
          <div class="hero-avatar">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
            <span v-else>{{ (userInfo.nickname || userInfo.username || '我').slice(0, 1) }}</span>
          </div>
          <div class="hero-copy">
            <p class="hero-kicker">个人中心</p>
            <h1>你好，{{ userInfo.nickname || userInfo.username || '同学' }}。</h1>
            <p class="hero-meta">
              {{ userInfo.college || '校园生活，从发现一件好物开始。' }}<template v-if="userInfo.studentId"> · 学号 {{ userInfo.studentId }}</template>
            </p>
            <div class="hero-actions">
              <span class="credit-chip">信用分 {{ userInfo.creditScore || 100 }}</span>
              <button type="button" class="hero-edit" @click="goToEditProfile">编辑资料</button>
            </div>
          </div>
        </div>

        <aside class="hero-stats">
          <p class="hero-stats-title">账户速览</p>
          <div class="hero-stats-grid">
            <button type="button" class="stat-cell" @click="goToMyProducts">
              <strong>{{ displayStats.publishCount }}</strong>
              <span>在售</span>
            </button>
            <button type="button" class="stat-cell" @click="goToMyProducts">
              <strong>{{ displayStats.soldCount }}</strong>
              <span>已售</span>
            </button>
            <button type="button" class="stat-cell" @click="goToMyFavorites">
              <strong>{{ displayStats.favoriteCount }}</strong>
              <span>收藏</span>
            </button>
            <button type="button" class="stat-cell" @click="goToMyFootprints">
              <strong>{{ displayStats.footprintCount }}</strong>
              <span>足迹</span>
            </button>
          </div>
        </aside>
      </section>

      <!-- 最近足迹 / 在售商品：两张白卡 -->
      <div class="preview-grid">
        <section class="preview-panel card">
          <div class="panel-head">
            <h2>最近足迹</h2>
            <router-link to="/my-footprints" class="panel-more">查看更多 <span class="arrow" aria-hidden="true">›</span></router-link>
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
        </section>

        <section class="preview-panel card">
          <div class="panel-head">
            <h2>在售商品</h2>
            <router-link to="/my-products" class="panel-more">查看更多 <span class="arrow" aria-hidden="true">›</span></router-link>
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
        </section>
      </div>
    </template>
  </AccountLayout>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AccountLayout from '../components/AccountLayout.vue'
import { setUserInfo } from '../utils/user'

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

// 展示用数字：数据到达后做一次 700ms 的递增动画
const displayStats = reactive({
  publishCount: 0,
  soldCount: 0,
  favoriteCount: 0,
  footprintCount: 0
})

const prefersReducedMotion =
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

let statsFrame = null

const animateStats = () => {
  if (typeof window === 'undefined') return

  if (prefersReducedMotion) {
    Object.assign(displayStats, orderStats)
    return
  }

  if (statsFrame) cancelAnimationFrame(statsFrame)

  const from = { ...displayStats }
  const startedAt = performance.now()
  const duration = 700

  const tick = (now) => {
    const progress = Math.min((now - startedAt) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)

    displayStats.publishCount = Math.round(from.publishCount + (orderStats.publishCount - from.publishCount) * eased)
    displayStats.soldCount = Math.round(from.soldCount + (orderStats.soldCount - from.soldCount) * eased)
    displayStats.favoriteCount = Math.round(from.favoriteCount + (orderStats.favoriteCount - from.favoriteCount) * eased)
    displayStats.footprintCount = Math.round(from.footprintCount + (orderStats.footprintCount - from.footprintCount) * eased)

    if (progress < 1) statsFrame = requestAnimationFrame(tick)
  }

  statsFrame = requestAnimationFrame(tick)
}

watch(orderStats, animateStats, { deep: true })

onUnmounted(() => {
  if (statsFrame) cancelAnimationFrame(statsFrame)
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
      setUserInfo(data.data)
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
/* ============================================
   「我的」：沿用首页的深蓝头、白卡和蓝色强调
   ============================================ */

/* 入场动画：头图先出现，预览卡延迟 120ms 跟上；只播一次 */
@keyframes account-rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 未登录 */
.login-prompt {
  max-width: 560px;
  margin: 8px auto;
  padding: 56px 24px;
  text-align: center;
  animation: account-rise 0.45s cubic-bezier(0.2, 0.7, 0.3, 1) both;
}

.login-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: var(--c-primary-light);
  color: var(--c-primary);
}

.login-icon svg {
  width: 30px;
  height: 30px;
}

.login-prompt h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--c-text);
}

.login-prompt p {
  margin-top: 8px;
  font-size: 14px;
  color: var(--c-text-2);
}

.prompt-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.prompt-buttons .btn {
  min-width: 120px;
}

/* 蓝色账户头图 */
.account-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 32px;
  align-items: center;
  padding: 28px 32px;
  border-radius: 12px;
  background: var(--c-primary-dark);
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  animation: account-rise 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) both;
}

.hero-identity {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.hero-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  overflow: hidden;
}

.hero-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-copy {
  min-width: 0;
}

.hero-kicker {
  font-size: 13px;
  color: #bfdbfe;
}

.hero-copy h1 {
  margin: 6px 0 4px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  color: #fff;
}

.hero-meta {
  font-size: 14px;
  line-height: 1.6;
  color: #dbe7ff;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.credit-chip {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #dbe7ff;
  font-size: 12px;
}

.hero-edit {
  height: 34px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 6px;
  background: transparent;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.hero-edit:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.6);
}

/* 右侧白色速览卡 */
.hero-stats {
  padding: 16px 18px;
  border-radius: 8px;
  background: var(--c-card);
  color: var(--c-text);
  box-shadow: 0 18px 40px rgba(2, 12, 40, 0.25);
}

.hero-stats-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--c-text-2);
}

.hero-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 10px 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--c-text);
  text-align: left;
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.stat-cell:hover {
  background: var(--c-hover);
}

.stat-cell:active {
  background: var(--c-primary-light);
}

.stat-cell strong {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
  transition: color 0.18s ease;
}

.stat-cell:hover strong {
  color: var(--c-primary);
}

.stat-cell span {
  font-size: 12px;
  color: var(--c-text-2);
}

/* 预览卡片 */
.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
  animation: account-rise 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) 0.12s both;
}

.preview-panel {
  padding: 16px 18px;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.panel-head h2 {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.panel-more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  color: var(--c-primary);
  transition: color 0.15s;
}

.panel-more .arrow {
  display: inline-block;
  transition: transform 0.18s ease;
}

.panel-more:hover {
  color: var(--c-primary-hover);
}

.panel-more:hover .arrow {
  transform: translateX(2px);
}

.preview-list {
  display: flex;
  flex-direction: column;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.preview-item:hover {
  background: var(--c-hover);
}

.preview-thumb {
  flex-shrink: 0;
  width: 64px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--c-disabled-bg);
}

.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s cubic-bezier(0.2, 0.7, 0.3, 1);
}

.preview-item:hover .preview-thumb img {
  transform: scale(1.06);
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.preview-title {
  font-size: 14px;
  color: var(--c-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--c-danger);
  font-variant-numeric: tabular-nums;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 36px 12px;
  font-size: 13px;
  color: var(--c-text-2);
}

/* 深蓝头图内白色焦点环，白卡里用主色 */
.account-hero :focus-visible {
  outline: 2px solid #fff;
  outline-offset: 2px;
}

.preview-panel :focus-visible,
.login-prompt :focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

@media (max-width: 900px) {
  .account-hero {
    grid-template-columns: minmax(0, 1fr);
    gap: 24px;
    padding: 24px;
  }

  .preview-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 620px) {
  .account-hero {
    padding: 20px;
    border-radius: 10px;
  }

  .hero-identity {
    gap: 14px;
  }

  .hero-avatar {
    width: 52px;
    height: 52px;
    font-size: 20px;
  }

  .hero-copy h1 {
    font-size: 22px;
  }

  .hero-meta {
    font-size: 13px;
  }

  .hero-stats {
    padding: 14px;
  }

  .stat-cell strong {
    font-size: 22px;
  }

  .login-prompt {
    padding: 40px 20px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .account-hero,
  .preview-grid,
  .login-prompt {
    animation: none;
  }

  .stat-cell,
  .stat-cell strong,
  .preview-item,
  .preview-thumb img,
  .panel-more,
  .panel-more .arrow,
  .hero-edit {
    transition: none;
  }

  .preview-item:hover .preview-thumb img {
    transform: none;
  }

  .panel-more:hover .arrow {
    transform: none;
  }
}
</style>
