<template>
  <div class="home-page">
    <header class="header">
      <div class="search-box" @click="showSearch = true">
        <span class="search-icon">🔍</span>
        <span class="search-placeholder">{{ keyword || '搜索二手商品' }}</span>
      </div>
    </header>

    <div v-if="showSearch" class="search-modal" @click="showSearch = false">
      <div class="search-content" @click.stop>
        <div class="search-input-wrapper">
          <input 
            v-model="keyword" 
            type="text" 
            placeholder="输入商品关键词"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <div class="search-filters">
          <div class="filter-row">
            <span class="filter-label">价格区间</span>
            <input v-model="minPrice" type="number" placeholder="最低价" class="filter-input" />
            <span class="filter-separator">-</span>
            <input v-model="maxPrice" type="number" placeholder="最高价" class="filter-input" />
          </div>
          <div class="filter-row">
            <span class="filter-label">成色</span>
            <select v-model="condition" class="filter-select">
              <option value="">全部</option>
              <option value="1">全新</option>
              <option value="2">几乎全新</option>
              <option value="3">有使用痕迹</option>
            </select>
          </div>
          <div class="filter-row">
            <span class="filter-label">排序</span>
            <select v-model="sort" class="filter-select">
              <option value="time_desc">最新发布</option>
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
              <option value="view_desc">最热</option>
            </select>
          </div>
        </div>
        <button class="search-submit" @click="handleSearch">确认搜索</button>
      </div>
    </div>

    <div class="category-scroll">
      <div 
        v-for="cat in categories" 
        :key="cat.id"
        class="category-item"
        :class="{ active: activeCategory === cat.id }"
        @click="activeCategory = cat.id"
      >
        <span class="category-icon">{{ cat.icon }}</span>
        <span class="category-name">{{ cat.name }}</span>
      </div>
    </div>

    <section class="banner-section">
      <div class="banner">
        <div class="banner-content">
          <h2>发现好物</h2>
          <p>品质二手 · 环保生活</p>
        </div>
      </div>
    </section>

    <section class="hot-section">
      <div class="section-header">
        <h2>🔥 热门商品</h2>
        <span class="more">查看更多</span>
      </div>
      <div class="hot-scroll">
        <div
          v-for="item in hotGoods"
          :key="item.id"
          class="hot-card"
          @click="goToDetail(item)"
        >
          <div class="hot-image">
            <img :src="item.image" :alt="item.title" />
          </div>
          <div class="hot-info">
            <h3 class="hot-title">{{ item.title }}</h3>
            <div class="hot-footer">
              <span class="hot-price">¥{{ item.price }}</span>
              <span v-if="item.originalPrice" class="hot-original-price">¥{{ item.originalPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="goods-section">
      <div class="section-header">
        <h2>精选推荐</h2>
        <span class="more">查看更多</span>
      </div>
      <div class="goods-grid">
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
          </div>
        </div>
      </div>
      <div class="loading-more" v-if="loading">
        <span>加载中...</span>
      </div>
      <div class="no-more" v-if="noMore && goods.length > 0">
        <span>没有更多了</span>
      </div>
      <div class="empty-state" v-if="!loading && goods.length === 0">
        <span>暂无商品</span>
      </div>
      
      <div class="pagination" v-if="total > 0 && goods.length > 0">
        <div class="pagination-info">
          <span>共 {{ total }} 件商品</span>
          <span style="margin-left: 10px; font-size: 12px; color: #999;">当前页: {{ page }}</span>
        </div>
        <div class="pagination-controls">
          <select v-model="pageSize" class="page-size-select" @change="handlePageSizeChange">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="30">30条/页</option>
          </select>
          <div class="page-nav">
            <button 
              class="page-btn" 
              :class="{ disabled: page === 1 }" 
              @click="changePage(page - 1)"
            >
              ‹
            </button>
            <span class="current-page">{{ page }}</span>
            <button 
              class="page-btn" 
              :class="{ disabled: page >= Math.ceil(total / pageSize) }" 
              @click="changePage(page + 1)"
            >
              ›
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="quick-actions">
      <div class="action-item">
        <span class="action-icon">🎁</span>
        <span class="action-text">新人礼包</span>
      </div>
      <div class="action-item">
        <span class="action-icon">🚚</span>
        <span class="action-text">同城配送</span>
      </div>
      <div class="action-item">
        <span class="action-icon">🛡️</span>
        <span class="action-text">品质保障</span>
      </div>
      <div class="action-item">
        <span class="action-icon">💯</span>
        <span class="action-text">正品保证</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const showSearch = ref(false)
const activeCategory = ref('all')
const keyword = ref('')
const minPrice = ref('')
const maxPrice = ref('')
const condition = ref('')
const sort = ref('time_desc')

const categories = ref([
  { id: 'all', icon: '📱', name: '全部' }
])

const goods = ref([])
const hotGoods = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)

const getCategoryIcon = (name) => {
  const iconMap = {
    '全部': '📦',
    '手机数码': '📱',
    '电脑办公': '💻',
    '图书教材': '📚',
    '生活用品': '🏠',
    '服饰鞋包': '👕',
    '运动户外': '⚽',
    '美妆个护': '💄',
    '其他': '📦'
  }
  return iconMap[name] || '📦'
}

const fetchCategories = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/category/list', {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.error('获取分类失败:', response.status)
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data) {
      const categoryList = data.data.map(cat => ({
        id: cat.id,
        icon: getCategoryIcon(cat.name),
        name: cat.name
      }))
      categories.value = [{ id: 'all', icon: '📱', name: '全部' }, ...categoryList]
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchHotGoods = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/product/hot', {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.error('获取热门商品失败, HTTP状态:', response.status)
      return
    }
    const data = await response.json()
    console.log('热门商品接口返回:', data)
    
    if (data.code === 200 && data.data) {
      let goodsArray = data.data
      
      // 处理二维数组情况：data: [[...]]
      if (Array.isArray(goodsArray) && goodsArray.length > 0 && Array.isArray(goodsArray[0])) {
        goodsArray = goodsArray[0]
      }
      
      if (!Array.isArray(goodsArray)) {
        console.error('热门商品数据不是数组:', typeof goodsArray, goodsArray)
        return
      }
      
      const goodsList = goodsArray.map(item => {
        // 处理Redis中可能存储为字符串的情况
        if (typeof item === 'string') {
          try {
            item = JSON.parse(item)
          } catch (e) {
            console.warn('解析商品数据失败:', item)
            return null
          }
        }
        return {
          id: item.id,
          title: item.title,
          description: item.description,
          price: item.price,
          originalPrice: item.originalPrice,
          image: item.images && item.images.length > 0 ? item.images[0] : '',
          categoryId: item.categoryId,
          productCondition: item.productCondition,
          viewCount: item.viewCount,
          createTime: item.createTime
        }
      }).filter(item => item !== null)
      
      hotGoods.value = goodsList
      console.log('最终热门商品列表:', hotGoods.value)
    } else {
      console.error('热门商品数据格式错误:', data)
    }
  } catch (error) {
    console.error('获取热门商品失败:', error)
  }
}

const fetchGoods = async (reset = false) => {
  if (loading.value || (reset === false && noMore.value)) return

  if (reset) {
    noMore.value = false
    goods.value = []
  }

  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      page: page.value.toString(),
      pageSize: pageSize.value.toString(),
      sort: sort.value
    })
    if (activeCategory.value !== 'all') {
      params.append('categoryId', activeCategory.value.toString())
    }
    if (keyword.value) {
      params.append('keyword', keyword.value)
    }
    if (minPrice.value) {
      params.append('minPrice', minPrice.value)
    }
    if (maxPrice.value) {
      params.append('maxPrice', maxPrice.value)
    }
    if (condition.value) {
      params.append('condition', condition.value)
    }
    const response = await fetch(`/product/list?${params}`, {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.error('获取商品失败:', response.status)
      loading.value = false
      return
    }
    const data = await response.json()
    console.log('商品列表接口返回:', data)
    if (data.code === 200 && data.data) {
      const goodsArray = data.data.list || data.data.records || []
      const newGoods = goodsArray.map(item => {
        let images = []
        if (item.images) {
          if (typeof item.images === 'string') {
            try {
              images = JSON.parse(item.images)
            } catch (e) {
              console.warn('解析images失败:', item.images)
            }
          } else if (Array.isArray(item.images)) {
            images = item.images
          }
        }
        const firstImage = images.length > 0 ? images[0] : ''
        return {
          id: item.id,
          title: item.title,
          price: item.price || item.price_,
          originalPrice: item.originalPrice || item.original_price,
          image: item.image || firstImage,
          condition: item.condition || item.productCondition || item.product_condition,
          conditionDesc: item.conditionDesc || item.condition_desc,
          viewCount: item.viewCount || item.view_count,
          favoriteCount: item.favoriteCount || item.favorite_count,
          createTime: item.createTime || item.create_time,
          seller: item.seller
        }
      })
      if (reset) {
        goods.value = newGoods
      } else {
        goods.value = [...goods.value, ...newGoods]
      }
      total.value = data.data.total || data.total || 0
      console.log('total updated:', total.value)
      if (goods.value.length >= total.value) {
        noMore.value = true
      }
    }
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  showSearch.value = false
  page.value = 1
  fetchGoods(true)
}

const handlePageSizeChange = () => {
  page.value = 1
  fetchGoods(true)
}

const changePage = (newPage) => {
  const totalPages = Math.ceil(total.value / pageSize.value)
  console.log('changePage called:', { currentPage: page.value, newPage, totalPages, total: total.value, pageSize: pageSize.value })
  if (newPage < 1 || newPage > totalPages || loading.value) return
  page.value = newPage
  fetchGoods(true)
}

watch(activeCategory, () => {
  page.value = 1
  fetchGoods(true)
})

onMounted(() => {
  fetchCategories()
  fetchHotGoods()
  fetchGoods()
})

const goToDetail = (item) => {
  router.push(`/product/${item.id}`)
}

const loadMore = () => {
  if (!noMore.value && !loading.value) {
    page.value++
    fetchGoods()
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #fafafa;
}

.header {
  background: #ffffff;
  padding: 16px 15px;
  padding-top: calc(16px + env(safe-area-inset-top));
  border-bottom: 1px solid #f0f0f0;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 14px;
  border: 1px solid #e8ecf0;
}

.search-icon {
  font-size: 14px;
  margin-right: 8px;
}

.search-placeholder {
  color: #999;
  font-size: 14px;
}

.search-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 100px;
  z-index: 1000;
}

.search-content {
  background: white;
  width: 100%;
  border-radius: 12px;
  padding: 20px;
}

.search-input-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
}

.search-btn {
  padding: 10px 20px;
  background: #1e88e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
}

.search-filters {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  width: 60px;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  font-size: 14px;
  width: 80px;
}

.filter-separator {
  color: #999;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  font-size: 14px;
  flex: 1;
}

.search-submit {
  width: 100%;
  padding: 12px;
  background: #1e88e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
}

.category-scroll {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  background: white;
  padding: 12px 0;
}

.category-scroll::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 18px;
  min-width: 56px;
}

.category-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.category-name {
  font-size: 12px;
  color: #666;
}

.category-item.active .category-name {
  color: #2563eb;
  font-weight: 500;
}

.category-item.active .category-icon {
  transform: scale(1.05);
}

.banner-section {
  padding: 12px 15px;
}

.banner {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border-radius: 12px;
  padding: 24px 20px;
}

.banner-content h2 {
  font-size: 20px;
  font-weight: 600;
  color: white;
  margin-bottom: 6px;
}

.banner-content p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.goods-section {
  padding: 0 15px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-top: 8px;
}

.section-header h2 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.more {
  font-size: 12px;
  color: #2563eb;
}

.hot-section {
  padding: 10px;
}

.hot-scroll {
  display: flex;
  overflow-x: auto;
  gap: 10px;
  padding: 5px 0;
}

.hot-card {
  flex-shrink: 0;
  width: 130px;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.hot-image {
  width: 100%;
  height: 100px;
}

.hot-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hot-info {
  padding: 8px;
}

.hot-title {
  font-size: 12px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-footer {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.hot-price {
  font-size: 14px;
  font-weight: 600;
  color: #ef4444;
}

.hot-original-price {
  font-size: 10px;
  color: #bbb;
  text-decoration: line-through;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.goods-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.goods-image {
  width: 100%;
  height: 130px;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-info {
  padding: 10px;
}

.goods-title {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-desc {
  font-size: 11px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 15px;
  font-weight: 600;
  color: #2563eb;
}

.goods-location {
  font-size: 10px;
  color: #bbb;
}

.goods-original-price {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}

.loading-more, .no-more, .empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  background: white;
  margin: 15px;
  padding: 20px 0;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.action-text {
  font-size: 12px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: white;
  margin-top: 10px;
  border-radius: 8px;
}

.pagination-info {
  font-size: 13px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  font-size: 13px;
  background: white;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  border: none;
  padding: 0;
  outline: none;
  appearance: none;
}

.page-btn:hover:not(.disabled) {
  background: #e8ecf0;
}

.page-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.current-page {
  font-size: 14px;
  font-weight: 500;
  color: #1e88e5;
  min-width: 32px;
  text-align: center;
}
</style>