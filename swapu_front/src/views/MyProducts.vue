<template>
  <AccountLayout active="products">
    <div class="my-products-page">
      <h1 class="page-title">我的发布</h1>

      <!-- 状态筛选 tabs -->
      <div class="card filter-tabs">
        <button
          class="filter-tab"
          :class="{ active: statusFilter === '' }"
          @click="statusFilter = ''; page = 1; fetchMyProducts()"
        >
          全部
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === '1' }"
          @click="statusFilter = '1'; page = 1; fetchMyProducts()"
        >
          在售
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === '2' }"
          @click="statusFilter = '2'; page = 1; fetchMyProducts()"
        >
          已售
        </button>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="products.length === 0" class="card empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无发布的商品</p>
        <button class="btn btn-primary" @click="goToPublish">去发布</button>
      </div>

      <div v-else class="products-list">
        <div
          v-for="product in products"
          :key="product.id"
          class="card product-item"
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
            <button class="btn btn-outline btn-sm" @click.stop="goToEdit(product.id)">
              修改
            </button>
            <button
              v-if="product.status === 1"
              class="btn btn-outline btn-sm"
              @click.stop="handleOffline(product)"
            >
              下架
            </button>
            <button
              v-if="product.status === 3"
              class="btn btn-outline btn-sm"
              @click.stop="handleOnline(product)"
            >
              上架
            </button>
            <button class="btn btn-danger btn-sm" @click.stop="handleDelete(product)">
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
  </AccountLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AccountLayout from '../components/AccountLayout.vue'

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
  min-height: 60vh;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  padding: 6px;
  margin-bottom: 16px;
}

.filter-tab {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text-2);
  cursor: pointer;
  transition: all 0.15s;
}

.filter-tab:hover {
  background: var(--c-hover);
  color: var(--c-text);
}

.filter-tab.active {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.product-item:hover {
  border-color: var(--c-border-strong);
}

.product-content {
  flex: 1;
  min-width: 0;
  display: flex;
  gap: 16px;
  cursor: pointer;
}

.product-image {
  width: 120px;
  height: 120px;
  border-radius: var(--radius);
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  background: var(--c-disabled-bg);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-badge,
.offline-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.sold-badge {
  background: rgba(0, 0, 0, 0.5);
}

.offline-badge {
  background: rgba(107, 114, 128, 0.8);
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: var(--c-text-2);
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
  font-weight: 700;
  color: var(--c-danger);
}

.product-original-price {
  font-size: 12px;
  color: var(--c-text-3);
  text-decoration: line-through;
}

.product-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--c-text-3);
  margin-top: auto;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  width: 76px;
}

.product-actions .btn {
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

.no-more {
  text-align: center;
  padding: 16px;
  color: var(--c-text-3);
  font-size: 14px;
}
</style>
