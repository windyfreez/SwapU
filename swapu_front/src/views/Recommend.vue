<template>
  <div class="recommend-page">
    <div class="container">
      <div class="recommend-header">
        <h1 class="page-title">✨ 推荐商品</h1>
        <p class="recommend-subtitle">
          {{ loggedIn ? '根据你的浏览与收藏，为你精选以下好物' : '为你精选高人气、刚上架的校园好物，登录后可获得更精准推荐' }}
        </p>
      </div>

      <div v-if="loading && goods.length === 0" class="loading-state">
        <p>推荐加载中...</p>
      </div>

      <div v-else-if="goods.length === 0" class="card empty-state">
        <span class="empty-icon">🔍</span>
        <p>暂时没有可推荐的商品</p>
        <router-link to="/" class="btn btn-outline">去逛逛全部商品</router-link>
      </div>

      <template v-else>
        <div class="recommend-grid">
          <div
            v-for="item in goods"
            :key="item.id"
            class="goods-card"
            @click="goToDetail(item)"
          >
            <div class="goods-image">
              <img :src="item.image" :alt="item.title" />
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.title }}</h3>
              <p class="goods-desc">{{ item.description }}</p>
              <div class="goods-footer">
                <span class="goods-price">¥{{ item.price }}</span>
                <span v-if="item.originalPrice" class="goods-original-price">¥{{ item.originalPrice }}</span>
              </div>
              <div class="goods-meta">
                <span v-if="item.viewCount">{{ item.viewCount }}次浏览</span>
                <span>{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="recommend-tip">
          <span>💡 推荐内容由系统根据浏览、收藏和热门商品综合生成</span>
          <button class="btn btn-outline btn-sm" :disabled="loading" @click="fetchRecommend">
            {{ loading ? '刷新中...' : '刷新' }}
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const goods = ref([])
const loading = ref(false)
const loggedIn = ref(false)

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

  return String(imageField || '')
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(/-/g, '/'))
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

const normalizeProduct = (item) => {
  let images = item.images
  let image = item.image || getFirstImage(images)

  if (!image && typeof images === 'string') {
    image = images
  }

  return {
    id: item.id,
    title: item.title || `商品${item.id}`,
    description: item.description || '',
    price: item.price || 0,
    originalPrice: item.originalPrice || item.original_price || 0,
    image,
    categoryId: item.categoryId,
    productCondition: item.productCondition,
    viewCount: item.viewCount || 0,
    createTime: item.createTime || ''
  }
}

const fetchRecommend = async () => {
  if (loading.value) return

  loading.value = true
  try {
    const token = localStorage.getItem('token')
    loggedIn.value = !!token
    const response = await fetch('/product/recommend?limit=20', {
      headers: {
        'token': token || ''
      }
    })

    if (!response.ok) {
      console.error('获取推荐商品失败:', response.status)
      return
    }

    const data = await response.json()
    if (data.code === 200) {
      const goodsArray = data.data
      if (!Array.isArray(goodsArray)) {
        console.error('推荐商品数据不是数组:', goodsArray)
        return
      }
      goods.value = goodsArray.map(normalizeProduct)
    } else {
      console.error('获取推荐商品失败:', data.msg || data)
    }
  } catch (error) {
    console.error('获取推荐商品失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (item) => {
  router.push(`/product/${item.id}`)
}

onMounted(() => {
  loggedIn.value = !!localStorage.getItem('token')
  fetchRecommend()
})
</script>

<style scoped>
.recommend-page {
  padding-top: 20px;
  min-height: 60vh;
}

.recommend-header {
  margin-bottom: 24px;
}

.recommend-header .page-title {
  margin-bottom: 6px;
}

.recommend-subtitle {
  color: var(--c-text-2);
  font-size: 14px;
}

/* 商品网格 */
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}

.goods-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.goods-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-border-strong);
}

.goods-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--c-disabled-bg);
  overflow: hidden;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.goods-card:hover .goods-image img {
  transform: scale(1.04);
}

.goods-info {
  padding: 12px 14px 14px;
}

.goods-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-desc {
  font-size: 12px;
  color: var(--c-text-3);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-footer {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.goods-price {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-danger);
}

.goods-original-price {
  font-size: 12px;
  color: var(--c-text-3);
  text-decoration: line-through;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--c-text-3);
}

/* 底部刷新提示 */
.recommend-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding: 16px 20px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  font-size: 13px;
  color: var(--c-text-2);
}

@media (max-width: 768px) {
  .recommend-tip {
    flex-direction: column;
    text-align: center;
  }
}
</style>
