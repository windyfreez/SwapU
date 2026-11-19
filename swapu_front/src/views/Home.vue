<template>
  <div class="home-page">
    <div class="container">
      <!-- 市集头部：左主张 + 右速览 -->
      <section class="market-hero">
        <div class="hero-copy">
          <p class="hero-kicker">校园二手交易平台</p>
          <h1>同校闲置交易，<br />见面验货更放心</h1>
          <p class="hero-intro">课本、数码、生活用品，交给需要的同学。</p>
          <div class="hero-actions">
            <button class="btn hero-primary" @click="document.querySelector('.section')?.scrollIntoView({ behavior: 'smooth' })">浏览商品</button>
            <router-link to="/sell" class="btn hero-secondary">发布闲置</router-link>
          </div>
        </div>
        <aside class="market-stats">
          <p class="stats-title">市集速览</p>
          <div class="stats-row"><span>在售好物</span><strong>1,000+</strong></div>
          <div class="stats-row"><span>校园用户</span><strong>5,000+</strong></div>
          <p class="stats-note">校内认证 · 见面验货</p>
        </aside>
      </section>

      <!-- 热门商品：横向商品卡 -->
      <section v-if="hotGoods.length" class="hot-section">
        <div class="section-heading">
          <h2>热门商品</h2>
          <span class="heading-note">同学们最近在看</span>
        </div>
        <div class="hot-grid">
          <article v-for="item in hotGoods" :key="item.id" class="hot-card" @click="goToDetail(item)">
            <div class="hot-image">
              <img :src="item.image" :alt="item.title" />
              <span v-if="conditionLabel(item.condition)" class="condition-badge">{{ conditionLabel(item.condition) }}</span>
            </div>
            <div class="hot-info">
              <h3 class="hot-title">{{ item.title }}</h3>
              <div class="hot-price-row">
                <span class="hot-price"><small>¥</small>{{ item.price }}</span>
                <span v-if="item.viewCount" class="hot-views">{{ item.viewCount }} 浏览</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 全部商品：分类 + 筛选工具栏，紧贴商品列表 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title">
            <h2>全部商品</h2>
            <span v-if="total" class="section-count">共 {{ total }} 件</span>
          </div>
        </div>

        <div class="market-toolbar card">
          <div class="category-bar">
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

          <div class="filter-bar">
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
              <span v-if="conditionLabel(item.condition)" class="condition-badge">{{ conditionLabel(item.condition) }}</span>
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.title }}</h3>
              <p class="goods-desc">{{ item.description }}</p>
              <div class="goods-footer">
                <div class="goods-price-line">
                  <span class="goods-price"><small>¥</small>{{ item.price }}</span>
                  <span v-if="item.originalPrice" class="goods-original-price">¥{{ item.originalPrice }}</span>
                </div>
                <span v-if="item.viewCount" class="goods-views">{{ item.viewCount }} 浏览</span>
              </div>
            </div>
          </div>
        </div>

        <div class="loading-more" v-if="loading">
          <span>正在加载……</span>
        </div>
        <div class="no-more" v-if="noMore && goods.length > 0">
          <span>没有更多了</span>
        </div>
        <div class="empty-state" v-if="!loading && goods.length === 0">
          <p class="empty-title">暂无在售物品</p>
          <p class="empty-hint">换个筛选条件，或去发布一件闲置</p>
        </div>

        <div class="pagination" v-if="total > 0 && goods.length > 0">
          <div class="pagination-info">
            <span>共 {{ total }} 件商品</span>
            <select v-model="pageSize" class="page-size-select" @change="handlePageSizeChange">
              <option :value="30">30个商品/页</option>
              <option :value="60">60个商品/页</option>
            </select>
          </div>
          <div class="page-nav">
            <button
              class="page-btn"
              :class="{ disabled: page === 1 }"
              aria-label="上一页"
              @click="changePage(page - 1)"
            >
              ‹
            </button>
            <span class="current-page">{{ page }}</span>
            <button
              class="page-btn"
              :class="{ disabled: page >= Math.ceil(total / pageSize) }"
              aria-label="下一页"
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
// 全部商品默认每页 30 个，可选 60 个
const pageSize = ref(30)
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)

// 成色文案：只用于商品卡角标，与筛选下拉保持一致
const conditionLabels = { 1: '全新', 2: '几乎全新', 3: '有使用痕迹' }
const conditionLabel = (value) => conditionLabels[value] || ''

const mapProduct = (item) => {
  let images = []
  if (item.images) {
    if (typeof item.images === 'string') {
      try { images = JSON.parse(item.images) } catch (e) { console.warn('解析热门商品图片失败:', item.images) }
    } else if (Array.isArray(item.images)) images = item.images
  }
  return {
    id: item.id,
    title: item.title,
    price: item.price || item.price_,
    originalPrice: item.originalPrice || item.original_price,
    image: item.image || (images.length > 0 ? images[0] : ''),
    description: item.description || '',
    condition: item.condition || item.productCondition || item.product_condition,
    viewCount: item.viewCount || item.view_count
  }
}

const fetchHotGoods = async () => {
  try {
    const response = await fetch('/product/hot', { headers: { token: localStorage.getItem('token') || '' } })
    if (!response.ok) return
    const data = await response.json()
    if (data.code === 200 && data.data) {
      const list = Array.isArray(data.data) ? data.data : (data.data.list || data.data.records || [])
      hotGoods.value = list.map(mapProduct)
    }
  } catch (error) {
    console.error('获取热门商品失败:', error)
  }
}

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
        token: token || ''
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
      const newGoods = goodsArray.map(item => ({
        ...mapProduct(item),
        condition: item.condition || item.productCondition || item.product_condition,
        conditionDesc: item.conditionDesc || item.condition_desc,
        viewCount: item.viewCount || item.view_count,
        favoriteCount: item.favoriteCount || item.favorite_count,
        createTime: item.createTime || item.create_time,
        seller: item.seller
      }))
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
/* ============================================
   首页：校园二手交易市场
   沿用项目原有蓝色系（--c-primary / --c-bg / 白卡 / 红色价格），
   结构改为：深蓝市集头 → 热门商品 → 分类+筛选工具栏 → 商品目录
   ============================================ */
.home-page {
  --hero-bg: #1e3a8a;
  --hero-muted: #dbe7ff;
  --price: var(--c-danger);
  --card-hover-border: #b9cdf4;

  padding: 24px 0 48px;
  color: var(--c-text);
}


/* ---------- 市集头部 ---------- */
.market-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 48px;
  align-items: center;
  padding: 40px 48px;
  border-radius: 12px;
  background: var(--hero-bg);
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.hero-kicker {
  font-size: 13px;
  line-height: 1.6;
  color: var(--hero-muted);
}

.hero-copy h1 {
  margin: 12px 0 12px;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.25;
  color: #fff;
}

.hero-intro {
  max-width: 34em;
  font-size: 16px;
  line-height: 1.8;
  color: var(--hero-muted);
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
}

.hero-primary,
.hero-secondary {
  height: 44px;
  padding: 0 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
}

.hero-primary {
  border: 1px solid #fff;
  background: #fff;
  color: var(--c-primary-dark);
}

.hero-primary:hover {
  border-color: #eaf2ff;
  background: #eaf2ff;
  color: var(--c-primary-dark);
}

.hero-secondary {
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: transparent;
  color: #fff;
}

.hero-secondary:hover {
  border-color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.hero-primary:focus-visible,
.hero-secondary:focus-visible {
  outline: 2px solid #fff;
  outline-offset: 3px;
}

/* 右侧速览卡 */
.market-stats {
  padding: 20px 24px;
  border-radius: 8px;
  background: var(--c-card);
  color: var(--c-text);
  box-shadow: 0 18px 40px rgba(2, 12, 40, 0.25);
}


.stats-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--c-text-2);
}

.stats-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--c-border);
}

.stats-row span {
  font-size: 13px;
  color: var(--c-text-2);
}

.stats-row strong {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--c-text);
  font-variant-numeric: tabular-nums;
}

.stats-note {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--c-text-2);
}

.stats-note::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-success);
}

/* ---------- 区块标题 ---------- */
.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin: 32px 0 16px;
}

.section-heading h2,
.section-title h2 {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--c-text);
}

.heading-note {
  font-size: 13px;
  color: var(--c-text-2);
}

/* ---------- 热门商品 ---------- */
.hot-grid {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}

.hot-grid::-webkit-scrollbar {
  display: none;
}

.hot-card {
  flex: 0 0 184px;
  width: 184px;
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-card);
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.hot-card:hover {
  border-color: var(--card-hover-border);
  box-shadow: var(--shadow-sm);
}

.hot-image {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--c-disabled-bg);
}

.hot-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.hot-card:hover .hot-image img {
  transform: scale(1.04);
}

.hot-info {
  padding: 8px 12px 12px;
}

.hot-title {
  overflow: hidden;
  color: var(--c-text);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
}

.hot-price {
  color: var(--price);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.hot-price small {
  margin-right: 1px;
  font-size: 12px;
  font-weight: 600;
}

.hot-views {
  font-size: 12px;
  color: var(--c-text-2);
  white-space: nowrap;
}

/* 成色角标：压在图片左上角，浅蓝底避免黑块压暗商品图 */
.condition-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 0 8px;
  border-radius: 4px;
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.8;
}


/* ---------- 全部商品 ---------- */
.section {
  margin-top: 36px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.section-count {
  font-size: 13px;
  color: var(--c-text-2);
  font-variant-numeric: tabular-nums;
}

/* 同一张工具栏卡内放分类与筛选 */
.market-toolbar {
  overflow: hidden;
  margin-bottom: 16px;
}

.category-bar {
  display: flex;
  gap: 4px;
  padding: 8px 12px;
  overflow-x: auto;
  border-bottom: 1px solid var(--c-border);
  scrollbar-width: none;
}

.category-bar::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding: 8px 12px;
  border-radius: 999px;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.5;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.category-item:hover {
  background: var(--c-hover);
  color: var(--c-text);
}

.category-item.active {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}


/* 分类图标不参与展示：纯文字页签更接近交易平台 */
.category-icon {
  display: none;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
  padding: 12px 16px;
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
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-input-bg);
  color: var(--c-text);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.filter-input::placeholder {
  color: var(--c-text-2);
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: var(--c-primary);
  background: var(--c-card);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.14);
}

.filter-sep {
  color: var(--c-text-2);
}

.filter-select {
  min-width: 120px;
  height: 36px;
  padding: 0 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-input-bg);
  color: var(--c-text);
  font-size: 13px;
}

.filter-bar .btn {
  height: 36px;
  padding: 0 20px;
  border-radius: 6px;
  font-size: 13px;
}

/* ---------- 商品目录 ---------- */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.goods-card {
  /* 防止手机版 1fr 轨道被 nowrap 文本撑开 */
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-card);
  cursor: pointer;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.goods-card:hover {
  border-color: var(--card-hover-border);
  box-shadow: var(--shadow-sm);
}

.goods-image {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--c-disabled-bg);
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.goods-card:hover .goods-image img {
  transform: scale(1.04);
}

.goods-info {
  padding: 12px;
}

.goods-title {
  display: -webkit-box;
  min-height: 44px;
  overflow: hidden;
  color: var(--c-text);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.goods-desc {
  margin-top: 4px;
  overflow: hidden;
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-footer {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-top: 12px;
}

.goods-price-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.goods-price {
  color: var(--price);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.goods-price small {
  margin-right: 1px;
  font-size: 12px;
  font-weight: 600;
}

.goods-original-price {
  overflow: hidden;
  color: var(--c-text-2);
  font-size: 12px;
  text-decoration: line-through;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-views {
  flex-shrink: 0;
  color: var(--c-text-2);
  font-size: 12px;
  white-space: nowrap;
}

/* ---------- 状态与分页 ---------- */
.loading-more,
.no-more {
  padding: 24px 0 8px;
  text-align: center;
  font-size: 13px;
  color: var(--c-text-2);
}

.empty-state {
  padding: 56px 20px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-card);
  text-align: center;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.empty-hint {
  margin-top: 8px;
  font-size: 13px;
  color: var(--c-text-2);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--c-border);
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--c-text-2);
  font-variant-numeric: tabular-nums;
}

.page-size-select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-card);
  color: var(--c-text);
  font-size: 13px;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-card);
  color: var(--c-text);
  font-size: 18px;
  line-height: 1;
  transition: border-color 0.15s, color 0.15s;
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
  min-width: 28px;
  text-align: center;
  color: var(--c-text);
  font-size: 14px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* ---------- 响应式 ---------- */
@media (max-width: 1024px) {
  .market-hero {
    grid-template-columns: minmax(0, 1fr);
    gap: 24px;
    padding: 32px;
  }

  .market-stats {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    gap: 12px 20px;
    max-width: none;
    padding: 16px 20px;
  }

  .stats-title {
    flex: 1 1 100%;
    margin-bottom: 0;
  }

  .stats-row {
    flex: 1 1 0;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    padding: 0;
    border-bottom: 0;
  }

  .stats-row span {
    font-size: 12px;
  }

  .stats-row strong {
    font-size: 22px;
  }

  .stats-note {
    flex: 1 1 100%;
    margin-top: 0;
  }
}

@media (max-width: 620px) {
  .home-page {
    padding-top: 16px;
  }

  .market-hero {
    padding: 24px 20px;
    border-radius: 10px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-intro {
    font-size: 14px;
    line-height: 1.7;
  }

  .hero-actions {
    flex-wrap: wrap;
  }

  .hero-actions .btn {
    flex: 1 1 auto;
    justify-content: center;
  }


  .section-heading h2,
  .section-title h2 {
    font-size: 20px;
  }

  .hot-card {
    flex: 0 0 152px;
    width: 152px;
  }

  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .section-header {
    flex-wrap: wrap;
    gap: 4px 12px;
  }

  .pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .page-nav {
    justify-content: flex-end;
  }
}

/* ---------- 手动「手机版」模式 ---------- */
html[data-layout='mobile'] .market-hero {
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
  padding: 28px 20px;
}

html[data-layout='mobile'] .market-stats {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px 20px;
  max-width: none;
  padding: 16px 20px;
}

html[data-layout='mobile'] .stats-title {
  flex: 1 1 100%;
  margin-bottom: 0;
}

html[data-layout='mobile'] .stats-row {
  flex: 1 1 0;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 0;
  border-bottom: 0;
}

html[data-layout='mobile'] .stats-row span {
  font-size: 12px;
}

html[data-layout='mobile'] .stats-row strong {
  font-size: 22px;
}

html[data-layout='mobile'] .stats-note {
  flex: 1 1 100%;
  margin-top: 0;
}

html[data-layout='mobile'] .market-toolbar .filter-bar {
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  padding: 12px;
}

html[data-layout='mobile'] .market-toolbar .category-bar {
  gap: 4px;
  padding: 8px 12px;
}

html[data-layout='mobile'] .category-item {
  min-height: 44px;
  padding: 8px 12px;
}

html[data-layout='mobile'] .filter-input,
html[data-layout='mobile'] .filter-select {
  height: 44px;
}

html[data-layout='mobile'] .filter-bar .btn {
  height: 44px;
}

html[data-layout='mobile'] .goods-title {
  font-size: 14px;
}

html[data-layout='mobile'] .goods-price {
  font-size: 18px;
}

html[data-layout='mobile'] .page-btn {
  width: 44px;
  height: 44px;
}

html[data-layout='mobile'] .page-size-select {
  height: 44px;
}

/* 键盘焦点：统一 2px 主色描边 */
.home-page :focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

/* 深蓝头图上的按钮用白色焦点环，蓝环在深蓝底上不可见 */
.home-page .hero-primary:focus-visible,
.home-page .hero-secondary:focus-visible {
  outline: 2px solid #fff;
  outline-offset: 3px;
}

/* ---------- 动效降级 ---------- */
@media (prefers-reduced-motion: reduce) {
  .home-page *,
  .home-page *::before,
  .home-page *::after {
    transition-duration: 0.01ms !important;
    animation-duration: 0.01ms !important;
  }
}
</style>
