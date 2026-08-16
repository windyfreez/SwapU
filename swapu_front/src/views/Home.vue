<template>
  <div class="home-page">
    <div class="container">
      <!-- 分类导航 -->
      <div class="category-bar card">
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

      <!-- 热门商品 -->
      <section class="section">
        <div class="section-header">
          <h2>🔥 热门商品</h2>
        </div>
        <div v-if="hotGoods.length > 0" class="goods-grid">
          <div
            v-for="item in hotGoods"
            :key="'hot-' + item.id"
            class="goods-card"
            @click="goToDetail(item)"
          >
            <div class="goods-image">
              <img :src="item.image" :alt="item.title" />
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.title }}</h3>
              <div class="goods-footer">
                <span class="goods-price">¥{{ item.price }}</span>
                <span v-if="item.originalPrice" class="goods-original-price">¥{{ item.originalPrice }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 精选推荐 -->
      <section class="section">
        <div class="section-header">
          <h2>精选推荐</h2>
        </div>

        <div class="filter-bar card">
          <div class="filter-item">
            <span class="filter-label">价格区间</span>
            <input v-model="minPrice" type="number" placeholder="最低价" class="filter-input" />
            <span class="filter-sep">—</span>
            <input v-model="maxPrice" type="number" placeholder="最高价" class="filter-input" />
          </div>
          <div class="filter-item">
            <span class="filter-label">成色</span>
            <select v-model="condition" class="filter-select">
              <option value="">全部</option>
              <option value="1">全新</option>
              <option value="2">几乎全新</option>
              <option value="3">有使用痕迹</option>
            </select>
          </div>
          <div class="filter-item">
            <span class="filter-label">排序</span>
            <select v-model="sort" class="filter-select">
              <option value="time_desc">最新发布</option>
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
              <option value="view_desc">最热</option>
            </select>
          </div>
          <button class="btn btn-primary" @click="handleSearch">搜索</button>
          <button class="btn" @click="handleReset">重置</button>
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
          <span class="empty-icon">📦</span>
          <p>暂无商品</p>
        </div>

        <div class="pagination" v-if="total > 0 && goods.length > 0">
          <div class="pagination-info">
            <span>共 {{ total }} 件商品</span>
            <select v-model="pageSize" class="page-size-select" @change="handlePageSizeChange">
              <option :value="10">10条/页</option>
              <option :value="20">20条/页</option>
              <option :value="30">30条/页</option>
            </select>
          </div>
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
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

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
    const response = await fetch('/category/list', {
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

    if (data.code === 200 && data.data) {
      let goodsArray = data.data

      if (Array.isArray(goodsArray) && goodsArray.length > 0 && Array.isArray(goodsArray[0])) {
        goodsArray = goodsArray[0]
      }

      if (!Array.isArray(goodsArray)) {
        console.error('热门商品数据不是数组:', typeof goodsArray, goodsArray)
        return
      }

      const goodsList = goodsArray.map(item => {
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
          seller: item.seller,
          description: item.description || ''
        }
      })
      if (reset) {
        goods.value = newGoods
      } else {
        goods.value = [...goods.value, ...newGoods]
      }
      total.value = data.data.total || data.total || 0
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
  page.value = 1
  fetchGoods(true)
}

const handleReset = () => {
  minPrice.value = ''
  maxPrice.value = ''
  condition.value = ''
  sort.value = 'time_desc'
  keyword.value = ''
  page.value = 1
  fetchGoods(true)
  router.replace({ path: '/', query: {} })
}

const handlePageSizeChange = () => {
  page.value = 1
  fetchGoods(true)
}

const changePage = (newPage) => {
  const totalPages = Math.ceil(total.value / pageSize.value)
  if (newPage < 1 || newPage > totalPages || loading.value) return
  page.value = newPage
  fetchGoods(true)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(activeCategory, () => {
  page.value = 1
  fetchGoods(true)
})

watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = val || ''
    page.value = 1
    fetchGoods(true)
  }
)

onMounted(() => {
  keyword.value = route.query.keyword || ''
  fetchCategories()
  fetchHotGoods()
  fetchGoods()
})

const goToDetail = (item) => {
  router.push(`/product/${item.id}`)
}
</script>

<style scoped>
.home-page {
  padding-top: 20px;
}

/* 分类栏 */
.category-bar {
  display: flex;
  flex-wrap: wrap;
  padding: 14px 10px;
  margin-bottom: 28px;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--c-text-2);
}

.category-item:hover {
  background: var(--c-hover);
}

.category-icon {
  font-size: 17px;
}

.category-name {
  font-size: 14px;
}

.category-item.active {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}

/* 区块 */
.section {
  margin-bottom: 36px;
}

.section-header {
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--c-text);
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(215px, 1fr));
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 14px 18px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: var(--c-text-2);
  white-space: nowrap;
}

.filter-input {
  width: 90px;
  padding: 7px 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-input-bg);
}

.filter-input:focus {
  outline: none;
  border-color: var(--c-primary);
  background: var(--c-card);
}

.filter-sep {
  color: var(--c-text-3);
}

.filter-select {
  padding: 7px 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-input-bg);
  min-width: 130px;
}

.filter-select:focus {
  outline: none;
  border-color: var(--c-primary);
}

/* 分页 */
.loading-more,
.no-more {
  text-align: center;
  padding: 20px;
  color: var(--c-text-3);
  font-size: 13px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 4px;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: var(--c-text-2);
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-card);
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: var(--c-card);
  font-size: 16px;
  color: var(--c-text-2);
  transition: all 0.2s;
}

.page-btn:hover:not(.disabled) {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.page-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.current-page {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  min-width: 28px;
  text-align: center;
}
</style>
