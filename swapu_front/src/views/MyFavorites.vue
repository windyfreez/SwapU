<template>
  <AccountLayout active="favorites">
    <div class="favorites-page">
      <h1 class="page-title">我的收藏</h1>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="favorites.length === 0" class="card empty-state">
        <div class="empty-icon">❤️</div>
        <p>暂无收藏商品</p>
      </div>

      <div v-else>
        <div class="favorites-grid">
          <div
            v-for="item in favorites"
            :key="item.id"
            class="card favorite-card"
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
              <button class="btn btn-outline btn-sm cancel-btn" @click.stop="cancelFavorite(item.id)">
                取消收藏
              </button>
            </div>
          </div>
        </div>

        <div v-if="hasMore" class="load-more" @click="loadMore">
          加载更多
        </div>
      </div>
    </div>
  </AccountLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AccountLayout from '../components/AccountLayout.vue'

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
  min-height: 60vh;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.favorite-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.favorite-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-border-strong);
}

.item-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--c-disabled-bg);
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-card:hover .item-image img {
  transform: scale(1.04);
}

.item-info {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  font-size: 13px;
  color: var(--c-text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 4px 0 10px;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-price {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-danger);
}

.item-time {
  font-size: 12px;
  color: var(--c-text-3);
}

.cancel-btn {
  width: 100%;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: var(--c-primary);
  font-size: 14px;
  cursor: pointer;
  border-radius: var(--radius);
  transition: background 0.15s;
}

.load-more:hover {
  background: var(--c-primary-light);
}
</style>
