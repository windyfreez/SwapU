<template>
  <div class="my-products-page">
    <header class="page-header">
      <span class="back-btn" @click="goBack">←</span>
      <h1>我的发布</h1>
      <span class="placeholder"></span>
    </header>

    <div class="filter-bar">
      <div 
        class="filter-item" 
        :class="{ active: statusFilter === '' }"
        @click="statusFilter = ''; page = 1; fetchMyProducts()"
      >
        全部
      </div>
      <div 
        class="filter-item" 
        :class="{ active: statusFilter === '1' }"
        @click="statusFilter = '1'; page = 1; fetchMyProducts()"
      >
        在售
      </div>
      <div 
        class="filter-item" 
        :class="{ active: statusFilter === '2' }"
        @click="statusFilter = '2'; page = 1; fetchMyProducts()"
      >
        已售
      </div>
    </div>

    <div class="products-list">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="products.length === 0" class="empty">
        <div class="empty-icon">📦</div>
        <p>暂无发布的商品</p>
        <button class="publish-btn" @click="goToPublish">去发布</button>
      </div>

      <div v-else>
        <div 
          v-for="product in products" 
          :key="product.id" 
          class="product-item"
        >
          <div class="product-content" @click="goToDetail(product.id)">
            <div class="product-image">
              <img :src="getProductImage(product)" alt="" />
              <div v-if="product.status === 2" class="sold-badge">已售</div>
              <div v-if="product.status === 3" class="offline-badge">已下架</div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-meta">
                <span class="product-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="product-original-price">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
              <div class="product-stats">
                <span>浏览 {{ product.viewCount || 0 }}</span>
                <span>发布于 {{ formatDate(product.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="product-actions">
            <button class="action-btn edit-btn" @click.stop="goToEdit(product.id)">
              修改
            </button>
            <button 
              v-if="product.status === 1" 
              class="action-btn offline-btn" 
              @click.stop="handleOffline(product)"
            >
              下架
            </button>
            <button 
              v-if="product.status === 3" 
              class="action-btn online-btn" 
              @click.stop="handleOnline(product)"
            >
              上架
            </button>
            <button class="action-btn delete-btn" @click.stop="handleDelete(product)">
              删除
            </button>
          </div>
        </div>

        <div v-if="hasMore" class="load-more" @click="loadMore">
          加载更多
        </div>
        <div v-else class="no-more">没有更多了</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true)
const statusFilter = ref('')

const goBack = () => {
  router.back()
}

const goToPublish = () => {
  router.push('/sell')
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const goToEdit = (id) => {
  router.push(`/product/edit/${id}`)
}

const handleOffline = async (product) => {
  if (!confirm('确定要下架该商品吗？')) return
  
  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/product/${product.id}/off`, {
      method: 'PUT',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: 3
      })
    })
    
    if (!response.ok) {
      alert('下架失败')
      return
    }
    
    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('下架成功')
      product.status = 3
    } else {
      alert(data.message || '下架失败')
    }
  } catch (error) {
    console.error('下架失败:', error)
    alert('下架失败，请稍后重试')
  }
}

const handleOnline = async (product) => {
  if (!confirm('确定要上架该商品吗？')) return
  
  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/product/${product.id}/on`, {
      method: 'PUT',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: 1
      })
    })
    
    if (!response.ok) {
      alert('上架失败')
      return
    }
    
    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('上架成功')
      product.status = 1
    } else {
      alert(data.message || '上架失败')
    }
  } catch (error) {
    console.error('上架失败:', error)
    alert('上架失败，请稍后重试')
  }
}

const handleDelete = async (product) => {
  if (!confirm('确定要删除该商品吗？此操作不可撤销！')) return
  
  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/product/${product.id}`, {
      method: 'DELETE',
      headers: {
        'token': token || ''
      }
    })
    
    if (!response.ok) {
      alert('删除失败')
      return
    }
    
    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('删除成功')
      const index = products.value.findIndex(p => p.id === product.id)
      if (index > -1) {
        products.value.splice(index, 1)
        total.value--
      }
    } else {
      alert(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

const getProductImage = (product) => {
  if (!product.images) return ''
  
  let images = product.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      return images.trim().replace(/[`'""]/g, '')
    }
  }
  
  if (Array.isArray(images) && images.length > 0) {
    let img = images[0]
    if (typeof img === 'string') {
      return img.trim().replace(/[`'""]/g, '')
    }
  }
  return ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const fetchMyProducts = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      page: page.value.toString(),
      pageSize: pageSize.value.toString()
    })
    
    if (statusFilter.value) {
      params.append('status', statusFilter.value)
    }

    const response = await fetch(`/product/my-products?${params}`, {
      headers: {
        'token': token || ''
      }
    })

    if (!response.ok) {
      console.error('获取商品失败:', response.status)
      return
    }

    const data = await response.json()
    console.log('我的商品数据:', data)

    if (data.code === 200 && data.data) {
      const records = data.data.records || []
      total.value = data.data.total || 0

      if (page.value === 1) {
        products.value = records
      } else {
        products.value = [...products.value, ...records]
      }

      hasMore.value = products.value.length < total.value
    }
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  fetchMyProducts()
}

onMounted(() => {
  fetchMyProducts()
})
</script>

<style scoped>
.my-products-page {
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

.back-btn {
  font-size: 24px;
  color: #333;
  width: 32px;
}

.page-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.placeholder {
  width: 32px;
}

.filter-bar {
  display: flex;
  background: white;
  padding: 10px 15px;
  gap: 10px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 56px;
  z-index: 99;
}

.filter-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  background: #f5f7fa;
  transition: all 0.2s ease;
}

.filter-item.active {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 500;
}

.products-list {
  padding: 10px 15px;
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
  font-size: 64px;
  margin-bottom: 16px;
}

.empty p {
  font-size: 16px;
  color: #999;
  margin-bottom: 20px;
}

.publish-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
}

.product-item {
  background: white;
  border-radius: 10px;
  margin-bottom: 10px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.product-content {
  display: flex;
  padding: 12px;
}

.product-actions {
  display: flex;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  background: white;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.action-btn:not(:last-child) {
  border-right: 1px solid #f0f0f0;
}

.action-btn:active {
  background: #f5f5f5;
}

.edit-btn {
  color: #2563eb;
}

.offline-btn {
  color: #f59e0b;
}

.online-btn {
  color: #10b981;
}

.delete-btn {
  color: #ef4444;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.offline-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(107, 114, 128, 0.8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.product-info {
  flex: 1;
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #ef4444;
}

.product-original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.product-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
  margin-top: auto;
}

.load-more {
  text-align: center;
  padding: 15px;
  color: #2563eb;
  font-size: 14px;
}

.no-more {
  text-align: center;
  padding: 15px;
  color: #999;
  font-size: 14px;
}
</style>
