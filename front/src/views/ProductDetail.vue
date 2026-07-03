<template>
  <div class="detail-page">
    <header class="detail-header">
      <span class="back-btn" @click="goBack">←</span>
      <span class="header-title">商品详情</span>
      <span class="placeholder"></span>
    </header>

    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="product" class="product-content">
      <div class="image-slider">
        <div class="image-wrapper">
          <img 
            :src="cleanImageUrl(product.images[currentImageIndex])" 
            :alt="product.title"
            class="product-image"
          />
        </div>
        <div class="image-indicators" v-if="product.images.length > 1">
          <span 
            v-for="(_, index) in product.images" 
            :key="index"
            class="indicator"
            :class="{ active: currentImageIndex === index }"
            @click="currentImageIndex = index"
          ></span>
        </div>
      </div>

      <div class="product-info">
        <div class="price-row">
          <span class="price">¥{{ product.price }}</span>
          <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
        </div>
        <h1 class="title">{{ product.title }}</h1>
        <p class="description">{{ product.description }}</p>

        <div class="condition-row">
          <span class="condition-badge" :class="getConditionClass(product.productCondition)">
            {{ product.productConditionDesc || getConditionDesc(product.productCondition) }}
          </span>
          <span class="view-count">浏览 {{ product.viewCount }} 次</span>
        </div>

        <div class="status-row" :class="{ sold: product.status === 2 }">
          <span>{{ product.statusDesc || (product.status === 1 ? '在售' : '已售出') }}</span>
        </div>
      </div>

      <div class="seller-section">
        <div class="seller-header">
          <span class="section-title">👤 卖家信息</span>
        </div>
        <div class="seller-info" @click="showSellerDetail = true">
          <img 
            :src="cleanImageUrl(product.sellerInfo.avatar)"
            :alt="product.sellerInfo.username"
            class="seller-avatar"
          />
          <div class="seller-detail">
            <span class="seller-name">{{ product.sellerInfo.username }}</span>
            <span class="seller-score">信誉分: {{ product.sellerInfo.creditScore }}</span>
          </div>
          <span class="arrow">→</span>
        </div>
      </div>

      <div class="other-info">
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
    </div>

    <div v-else class="empty-state">
      <span>商品不存在</span>
    </div>

    <footer v-if="product" class="detail-footer">
      <div class="footer-left">
        <span class="footer-item" @click="toggleFavorite">
          <span class="icon">{{ isFavorite ? '❤️' : '🤍' }}</span>
          <span class="text">收藏</span>
        </span>
      </div>
      <div class="footer-right">
        <button class="btn-secondary" @click="contactSeller">联系卖家</button>
        <button 
          class="btn-primary" 
          :class="{ disabled: product.status === 2 }"
          @click="buyProduct"
        >
          {{ product.status === 2 ? '已售出' : '立即购买' }}
        </button>
      </div>
    </footer>

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
        <button class="modal-contact-btn" @click="contactSeller(); showSellerDetail = false">
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
      console.log('收藏列表响应:', data)
      if (data.code === 200 && data.data && data.data.records) {
        const productIdStr = String(productId)
        const isFavorited = data.data.records.some(item => {
          console.log('比较: item.id=', item.id, 'vs productId=', productIdStr)
          return String(item.id) === productIdStr
        })
        isFavorite.value = isFavorited
        console.log('收藏状态:', isFavorited)
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

const goBack = () => {
  router.back()
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
        console.log('取消收藏成功')
      } else {
        alert('取消收藏失败')
      }
    } else {
      console.log('收藏商品ID:', product.value?.id)
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
        console.log('收藏成功')
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
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 24px;
  color: #333;
  width: 32px;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

.placeholder {
  width: 32px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
}

.image-slider {
  background: white;
  padding-bottom: 10px;
}

.image-wrapper {
  width: 100%;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.image-indicators {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
}

.indicator.active {
  background: #2563eb;
}

.product-info {
  background: white;
  padding: 16px;
  margin-top: 10px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  font-size: 28px;
  font-weight: 600;
  color: #ef4444;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  margin-top: 12px;
  margin-bottom: 8px;
}

.description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.condition-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.condition-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.condition-new {
  background: #dbeafe;
  color: #1d4ed8;
}

.condition-like-new {
  background: #dcfce7;
  color: #16a34a;
}

.condition-used {
  background: #fef3c7;
  color: #d97706;
}

.condition-worn {
  background: #fee2e2;
  color: #dc2626;
}

.condition-damaged {
  background: #f3f4f6;
  color: #6b7280;
}

.view-count {
  font-size: 12px;
  color: #999;
}

.status-row {
  margin-top: 12px;
  padding: 8px;
  background: #dcfce7;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  color: #16a34a;
}

.status-row.sold {
  background: #f3f4f6;
  color: #6b7280;
}

.seller-section {
  background: white;
  padding: 16px;
  margin-top: 10px;
}

.seller-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.seller-info {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f9fafb;
  border-radius: 10px;
}

.seller-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-detail {
  flex: 1;
  margin-left: 12px;
}

.seller-name {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.seller-score {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.arrow {
  font-size: 16px;
  color: #bbb;
}

.other-info {
  background: white;
  padding: 16px;
  margin-top: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 14px;
  color: #999;
}

.value {
  font-size: 14px;
  color: #333;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
}

.detail-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: white;
  border-top: 1px solid #f0f0f0;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
}

.footer-left {
  display: flex;
  gap: 20px;
}

.footer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.icon {
  font-size: 20px;
}

.text {
  font-size: 11px;
  color: #666;
}

.footer-right {
  display: flex;
  gap: 10px;
}

.btn-secondary {
  padding: 10px 20px;
  border: 1px solid #2563eb;
  border-radius: 25px;
  font-size: 14px;
  color: #2563eb;
  background: white;
}

.btn-primary {
  padding: 10px 25px;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  color: white;
  background: #2563eb;
}

.btn-primary.disabled {
  background: #999;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-width: 320px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 16px;
  font-weight: 500;
}

.modal-close {
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.seller-modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
}

.modal-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 16px;
}

.modal-seller-info {
  text-align: center;
}

.modal-seller-info h3 {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.modal-seller-info p {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.modal-contact-btn {
  width: calc(100% - 32px);
  margin: 0 16px 16px;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  color: white;
  background: #2563eb;
}
</style>
