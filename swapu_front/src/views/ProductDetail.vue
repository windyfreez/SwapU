<template>
  <div class="detail-page">
    <div class="container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>商品详情</span>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="product" class="detail-main">
        <!-- 左:图片画廊 -->
        <div class="gallery card">
          <div class="main-image">
            <img
              :src="cleanImageUrl(product.images[currentImageIndex])"
              :alt="product.title"
            />
          </div>
          <div v-if="product.images.length > 1" class="thumb-list">
            <div
              v-for="(img, index) in product.images"
              :key="index"
              class="thumb-item"
              :class="{ active: currentImageIndex === index }"
              @click="currentImageIndex = index"
            >
              <img :src="cleanImageUrl(img)" alt="" />
            </div>
          </div>
        </div>

        <!-- 右:信息面板 -->
        <div class="info-panel card">
          <div class="price-row">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
            <span class="status-tag" :class="product.status === 2 ? 'sold' : 'on-sale'">
              {{ product.statusDesc || (product.status === 1 ? '在售' : '已售出') }}
            </span>
          </div>

          <h1 class="title">{{ product.title }}</h1>
          <p class="description">{{ product.description }}</p>

          <div class="meta-row">
            <span class="condition-badge" :class="getConditionClass(product.productCondition)">
              {{ product.productConditionDesc || getConditionDesc(product.productCondition) }}
            </span>
            <span class="view-count">浏览 {{ product.viewCount }} 次</span>
            <span v-if="product.quantity" class="view-count">库存 {{ product.quantity }} 件</span>
          </div>

          <hr class="divider" />

          <!-- 卖家卡片 -->
          <div class="seller-block" @click="showSellerDetail = true">
            <img
              :src="cleanImageUrl(product.sellerInfo.avatar)"
              :alt="product.sellerInfo.username"
              class="seller-avatar"
            />
            <div class="seller-detail">
              <span class="seller-name">{{ product.sellerInfo.username }}</span>
              <span class="seller-score">信誉分 {{ product.sellerInfo.creditScore }}</span>
            </div>
            <span class="seller-arrow">›</span>
          </div>

          <hr class="divider" />

          <div class="info-rows">
            <div class="info-row">
              <span class="label">商品编号</span>
              <span class="value">{{ product.id }}</span>
            </div>
            <div class="info-row">
              <span class="label">发布时间</span>
              <span class="value">{{ formatTime(product.createTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">更新时间</span>
              <span class="value">{{ formatTime(product.updateTime) }}</span>
            </div>
          </div>

          <hr class="divider" />

          <div class="action-row">
            <button class="btn btn-outline action-fav" :class="{ favorited: isFavorite }" @click="toggleFavorite">
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
            <button class="btn btn-outline" @click="contactSeller">联系卖家</button>
            <button
              class="btn btn-primary btn-lg buy-btn"
              :class="{ disabled: product.status === 2 }"
              @click="buyProduct"
            >
              {{ product.status === 2 ? '已售出' : '立即购买' }}
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">📦</span>
        <p>商品不存在</p>
        <router-link to="/" class="btn btn-outline">返回首页</router-link>
      </div>
    </div>

    <!-- 卖家信息弹窗 -->
    <div v-if="showSellerDetail" class="modal-mask" @click="showSellerDetail = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">卖家信息</span>
          <span class="modal-close" @click="showSellerDetail = false">×</span>
        </div>
        <div class="seller-modal-body">
          <img
            :src="cleanImageUrl(product?.sellerInfo?.avatar)"
            :alt="product?.sellerInfo.username"
            class="modal-avatar"
          />
          <div class="modal-seller-info">
            <h3>{{ product?.sellerInfo.username }}</h3>
            <p>用户ID: {{ product?.sellerInfo.id }}</p>
            <p>信誉分: {{ product?.sellerInfo.creditScore }}</p>
          </div>
        </div>
        <button class="btn btn-primary btn-block modal-contact-btn" @click="contactSeller(); showSellerDetail = false">
          联系卖家
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const product = ref(null)
const currentImageIndex = ref(0)
const showSellerDetail = ref(false)
const isFavorite = ref(false)

const fetchProductDetail = async () => {
  const productId = route.params.id
  if (!productId) {
    loading.value = false
    return
  }

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/product/detail/${productId}`, {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.error('获取商品详情失败:', response.status)
      loading.value = false
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data) {
      product.value = data.data
      checkFavoriteStatus(productId, token)
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

const checkFavoriteStatus = async (productId, token) => {
  if (!token) {
    isFavorite.value = false
    return
  }

  try {
    const response = await fetch(`/favorite/list?page=1&pageSize=100`, {
      headers: {
        'token': token
      }
    })
    if (response.ok) {
      const data = await response.json()
      if (data.code === 200 && data.data && data.data.records) {
        const productIdStr = String(productId)
        isFavorite.value = data.data.records.some(item => String(item.id) === productIdStr)
      }
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const getConditionDesc = (condition) => {
  const descMap = {
    1: '全新未拆封',
    2: '几乎全新',
    3: '轻微使用痕迹',
    4: '明显使用痕迹',
    5: '有损坏'
  }
  return descMap[condition] || '未知'
}

const getConditionClass = (condition) => {
  const classMap = {
    1: 'condition-new',
    2: 'condition-like-new',
    3: 'condition-used',
    4: 'condition-worn',
    5: 'condition-damaged'
  }
  return classMap[condition] || 'condition-used'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

const cleanImageUrl = (url) => {
  if (!url) return ''

  if (typeof url === 'string') {
    url = url.trim()
    if (url.startsWith('[') || url.startsWith('{')) {
      try {
        const parsed = JSON.parse(url)
        if (Array.isArray(parsed) && parsed.length > 0) {
          url = parsed[0]
        } else if (typeof parsed === 'string') {
          url = parsed
        }
      } catch {
        return url.replace(/[`'""]/g, '')
      }
    }
    return url.replace(/[`'""]/g, '')
  }

  if (Array.isArray(url)) {
    return url.length > 0 ? cleanImageUrl(url[0]) : ''
  }

  return String(url)
}

const toggleFavorite = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录')
    return
  }

  try {
    if (isFavorite.value) {
      const response = await fetch(`/favorite/cancel?productId=${product.value.id}`, {
        method: 'DELETE',
        headers: {
          'token': token
        }
      })

      if (response.ok || response.status === 204) {
        isFavorite.value = false
      } else {
        alert('取消收藏失败')
      }
    } else {
      const response = await fetch('/favorite/add', {
        method: 'POST',
        headers: {
          'token': token,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          productId: product.value?.id
        })
      })

      if (response.ok) {
        isFavorite.value = true
      } else {
        alert('收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    alert('操作失败，请稍后重试')
  }
}

const contactSeller = () => {
  if (!product.value?.sellerInfo) return

  sessionStorage.setItem('chatTargetUser', JSON.stringify({
    userId: product.value.sellerInfo.id,
    username: product.value.sellerInfo.username,
    avatar: product.value.sellerInfo.avatar,
    productId: product.value.id,
    productTitle: product.value.title,
    productImage: product.value.images?.[0] || ''
  }))

  router.push({
    path: '/messages/chat',
    query: { userId: product.value.sellerInfo.id }
  })
}

const buyProduct = () => {
  if (!product.value) return

  if (product.value.status === 2) {
    alert('该商品已售出')
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录')
    router.push('/login')
    return
  }

  router.push(`/order/create/${product.value.id}`)
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.detail-page {
  min-height: 60vh;
}

.detail-main {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 24px;
  padding-top: 20px;
  align-items: start;
}

/* 画廊 */
.gallery {
  padding: 16px;
}

.main-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--c-disabled-bg);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.thumb-item {
  width: 76px;
  height: 76px;
  border-radius: var(--radius);
  overflow: hidden;
  border: 2px solid var(--c-border);
  cursor: pointer;
  transition: border-color 0.2s;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-item.active {
  border-color: var(--c-primary);
}

/* 信息面板 */
.info-panel {
  padding: 24px 26px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.price {
  font-size: 30px;
  font-weight: 700;
  color: var(--c-danger);
}

.original-price {
  font-size: 14px;
  color: var(--c-text-3);
  text-decoration: line-through;
}

.status-tag {
  margin-left: auto;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.status-tag.on-sale {
  background: var(--c-success-light);
  color: var(--c-success);
}

.status-tag.sold {
  background: var(--c-hover);
  color: var(--c-text-3);
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 10px;
  line-height: 1.45;
}

.description {
  font-size: 14px;
  color: var(--c-text-2);
  line-height: 1.7;
  margin-bottom: 16px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.condition-badge {
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.condition-new {
  background: var(--c-primary-light);
  color: var(--c-primary);
}

.condition-like-new {
  background: var(--c-success-light);
  color: var(--c-success);
}

.condition-used {
  background: var(--c-warning-light);
  color: var(--c-warning);
}

.condition-worn {
  background: var(--c-danger-light);
  color: var(--c-danger);
}

.condition-damaged {
  background: var(--c-hover);
  color: var(--c-text-2);
}

.view-count {
  font-size: 12px;
  color: var(--c-text-3);
}

/* 卖家 */
.seller-block {
  display: flex;
  align-items: center;
  padding: 14px;
  background: var(--c-input-bg);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}

.seller-block:hover {
  border-color: var(--c-border-strong);
  background: var(--c-hover);
}

.seller-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.seller-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.seller-score {
  font-size: 12px;
  color: var(--c-warning);
  margin-top: 2px;
}

.seller-arrow {
  font-size: 20px;
  color: var(--c-text-3);
}

/* 信息行 */
.info-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.info-row .label {
  color: var(--c-text-3);
}

.info-row .value {
  color: var(--c-text);
}

/* 操作区 */
.action-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-fav.favorited {
  border-color: var(--c-danger);
  color: var(--c-danger);
  background: var(--c-danger-light);
}

.buy-btn {
  flex: 1;
}

.buy-btn.disabled {
  background: var(--c-text-3);
  border-color: var(--c-text-3);
  cursor: not-allowed;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 380px;
  max-width: 90vw;
  background: var(--c-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  font-size: 22px;
  color: var(--c-text-3);
  cursor: pointer;
  line-height: 1;
}

.seller-modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 24px 20px;
}

.modal-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 14px;
}

.modal-seller-info {
  text-align: center;
}

.modal-seller-info h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
}

.modal-seller-info p {
  font-size: 13px;
  color: var(--c-text-2);
  margin-bottom: 4px;
}

.modal-contact-btn {
  margin: 0 20px 20px;
  width: calc(100% - 40px);
}

/* 窄屏降级 */
@media (max-width: 900px) {
  .detail-main {
    grid-template-columns: 1fr;
  }
}
</style>
