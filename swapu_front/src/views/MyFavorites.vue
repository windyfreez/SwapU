<template>
  <div class="favorites-page">
    <header class="page-header">
      <span class="back-btn" @click="goBack">‹</span>
      <h1>我的收藏</h1>
      <span class="placeholder"></span>
    </header>

    <div class="favorites-content">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="favorites.length === 0" class="empty">
        <div class="empty-icon">❤️</div>
        <p>暂无收藏商品</p>
      </div>
      
      <div v-else class="favorites-list">
        <div 
          v-for="item in favorites" 
          :key="item.id" 
          class="favorite-item"
          @click="goToDetail(item.id)"
        >
          <div class="item-image">
            <img :src="getFirstImage(item.images)" alt="" />
          </div>
          <div class="item-info">
            <h3 class="item-title">{{ item.title || '商品' + item.id }}</h3>
            <p class="item-desc">{{ item.description || '' }}</p>
            <div class="item-meta">
              <span class="item-price">¥{{ item.price || 0 }}</span>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <button class="cancel-btn" @click.stop="cancelFavorite(item.id)">取消收藏</button>
        </div>
      </div>

      <div v-if="hasMore" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const favorites = ref([])
const page = ref(1)
const pageSize = 10
const hasMore = ref(false)

const goBack = () => {
  router.back()
}

const getFirstImage = (imageField) => {
  if (!imageField) return ''
  
  if (typeof imageField === 'string') {
    imageField = imageField.trim()
    if (imageField.startsWith('[') || imageField.startsWith('{')) {
      try {
        const parsed = JSON.parse(imageField)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return getFirstImage(parsed[0])
        }
        return parsed
      } catch {
        return imageField
      }
    }
    return imageField
  }
  
  if (Array.isArray(imageField)) {
    return imageField.length > 0 ? getFirstImage(imageField[0]) : ''
  }
  
  return String(imageField)
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(/-/g, '/'))
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/favorite/list?page=${page.value}&pageSize=${pageSize}`, {
      headers: {
        'token': token || ''
      }
    })
    
    if (!response.ok) {
      console.error('获取收藏列表失败:', response.status)
      return
    }
    
    const data = await response.json()
    console.log('收藏列表数据:', data)
    
    if (data.code === 200 && data.data) {
      const records = data.data.records || []
      total.value = data.data.total || 0
      
      if (page.value === 1) {
        favorites.value = records
      } else {
        favorites.value = [...favorites.value, ...records]
      }
      
      hasMore.value = favorites.value.length < total.value
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

const total = ref(0)

const loadMore = () => {
  if (hasMore.value) {
    page.value++
    fetchFavorites()
  }
}

const cancelFavorite = async (productId) => {
  if (!confirm('确定要取消收藏吗？')) return
  
  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/favorite/cancel?productId=${productId}`, {
      method: 'DELETE',
      headers: {
        'token': token || ''
      }
    })
    
    if (response.ok || response.status === 204) {
      favorites.value = favorites.value.filter(item => item.id !== productId)
      alert('取消收藏成功')
    } else {
      alert('取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    alert('取消收藏失败，请稍后重试')
  }
}

const goToDetail = (productId) => {
  router.push(`/product/${productId}`)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: #fafafa;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 12px 16px;
  padding-top: calc(12px + env(safe-area-inset-top));
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.page-header h1 {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}

.back-btn {
  font-size: 28px;
  color: #1a1a1a;
  cursor: pointer;
}

.placeholder {
  width: 28px;
}

.favorites-content {
  padding: 12px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty p {
  color: #999;
  font-size: 14px;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.favorite-item {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.cancel-btn {
  align-self: center;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: white;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
</style>
