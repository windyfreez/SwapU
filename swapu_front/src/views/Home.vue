<template>
  <div class="home-page">
    <div class="container">
      <!-- 报头：左标题 + 右市集台账 -->
      <section class="home-hero">
        <div class="hero-copy">
          <p class="hero-kicker">校园二手交易，只在同学之间</p>
          <h1>好物在校园里，<br /><strong>换第二任主人。</strong></h1>
          <p class="hero-intro">课本、耳机、自行车……发布闲置，<br class="hero-break" />交给正需要的同学。</p>
          <div class="hero-actions">
            <button class="btn hero-primary" @click="document.querySelector('.section')?.scrollIntoView({ behavior: 'smooth' })">开始逛 <span aria-hidden="true">→</span></button>
            <router-link to="/sell" class="btn hero-secondary">发布闲置</router-link>
          </div>
        </div>

        <aside class="hero-ledger">
          <span class="ledger-title">市集台账</span>
          <dl class="ledger">
            <div class="ledger-row"><dt>在售好物</dt><dd>1,000+</dd></div>
            <div class="ledger-row"><dt>校园用户</dt><dd>5,000+</dd></div>
            <div class="ledger-row"><dt>交易方式</dt><dd class="ledger-text">校园认证</dd></div>
          </dl>
          <div class="ledger-foot">
            <span class="seal">校内认证</span>
            <p class="ledger-note">见面验货，放心交易</p>
          </div>
        </aside>
      </section>

      <section v-if="hotGoods.length" class="hot-section">
        <div class="section-heading">
          <h2>热门商品</h2>
          <span class="heading-note">同学们最近在看</span>
        </div>
        <div class="hot-grid">
          <article v-for="item in hotGoods" :key="item.id" class="hot-card" @click="goToDetail(item)">
            <div class="hot-image">
              <img :src="item.image" :alt="item.title" />
              <span class="price-tag">¥{{ item.price }}</span>
            </div>
            <h3 class="hot-title">{{ item.title }}</h3>
          </article>
        </div>
      </section>

      <!-- 全部商品:搜索/筛选栏 + 商品列表 -->
      <section class="section">
        <div class="section-header">
          <h2>全部商品</h2>
          <span v-if="total" class="section-count">共 {{ total }} 件</span>
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

        <!-- 分类导航:紧贴它实际过滤的商品列表 -->
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
          <span>正在加载……</span>
        </div>
        <div class="no-more" v-if="noMore && goods.length > 0">
          <span>没有更多了</span>
        </div>
        <div class="empty-state" v-if="!loading && goods.length === 0">
          <span class="empty-stamp" aria-hidden="true">空</span>
          <p>暂无在售物品</p>
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
    description: item.description || ''
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
   首页设计方向：校园公告栏 / 市集台账
   纸面底、墨色字、朱砂强调，细线分隔，不用卡片阴影。
   所有颜色、间距、字体走 token；深色主题与手机版单独覆盖。
   ============================================ */
.home-page {
  --paper: #F4EEE2;
  --surface: #FBF7ED;
  --ink: #1C1A15;
  --ink-2: #57503F;
  --ink-3: #6E6554;
  --line: #D8CEBB;
  --line-strong: #BDB199;
  --accent: #B23A1E;
  --tag: #E8D9B9;
  --tag-line: #C4B08C;

  --font-sans: "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", "Microsoft YaHei", sans-serif;
  --font-mono: "JetBrains Mono", "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  --radius: 2px;

  position: relative;
  min-height: calc(100vh - var(--nav-height));
  min-height: calc(100dvh - var(--nav-height));
  padding: 40px 0 64px;
  background: var(--paper);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.7;
}

/* 页脚在 App.vue 里带 48px 外边距，用纸面盖住这段底色，避免出现一条蓝灰色横带 */
.home-page::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -48px;
  height: 48px;
  background: var(--paper);
}

.home-page ::selection {
  background: var(--ink);
  color: var(--paper);
}

/* 键盘焦点：统一 2px 朱砂描边 */
.home-page :focus-visible {
  outline: 2px solid var(--accent);
  outline-offset: 2px;
}

/* ---------- 报头 ---------- */
.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 64px;
  align-items: start;
  padding: 36px 0 48px;
  border-top: 3px solid var(--ink);
  border-bottom: 1px solid var(--line);
}

.hero-kicker {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-2);
}

.hero-kicker::before {
  content: '';
  flex: none;
  width: 28px;
  height: 1px;
  background: var(--ink-3);
}

.hero-copy h1 {
  margin: 20px 0 16px;
  font-size: clamp(36px, 4.4vw, 56px);
  font-weight: 700;
  line-height: 1.14;
  color: var(--ink);
}

.hero-copy h1 strong {
  font-weight: 700;
  color: var(--accent);
}

.hero-intro {
  max-width: 36em;
  font-size: 16px;
  line-height: 1.9;
  color: var(--ink-2);
  text-wrap: pretty;
}

.hero-break {
  display: none;
}

@media (max-width: 480px) {
  .hero-break {
    display: inline;
  }
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 28px;
}

.hero-primary,
.hero-secondary {
  height: 44px;
  padding: 0 24px;
  border-radius: var(--radius);
  font-size: 14px;
  font-weight: 600;
}

.hero-primary {
  border: 1px solid var(--ink);
  background: var(--ink);
  color: var(--paper);
}

.hero-primary:hover {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

.hero-primary span {
  margin-left: 8px;
  transition: transform 0.2s ease;
}

.hero-primary:hover span {
  transform: translateX(4px);
}

.hero-secondary {
  border: 1px solid var(--line-strong);
  background: transparent;
  color: var(--ink);
}

.hero-secondary:hover {
  border-color: var(--ink);
  background: var(--surface);
  color: var(--ink);
}

/* 右侧台账：像一张贴出来的市集告示 */
.hero-ledger {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-self: stretch;
  padding-left: 32px;
  border-left: 1px solid var(--line);
}

.ledger-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-2);
}

.ledger-title::before {
  content: '';
  width: 6px;
  height: 6px;
  background: var(--accent);
}

.ledger {
  display: flex;
  flex-direction: column;
}

.ledger-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--line);
}

.ledger-row:first-child {
  border-top: 1px solid var(--line);
}

.ledger-row dt {
  font-size: 14px;
  color: var(--ink-2);
}

.ledger-row dd {
  margin: 0;
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.ledger-row dd.ledger-text {
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 600;
}

.ledger-foot {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: auto;
  padding-top: 8px;
}

.seal {
  flex: none;
  padding: 4px 8px;
  border: 2px solid var(--accent);
  border-radius: var(--radius);
  color: var(--accent);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.4;
  transform: rotate(-6deg);
}

.ledger-note {
  font-size: 13px;
  line-height: 1.7;
  color: var(--ink-3);
}

/* ---------- 热门商品：钉在公告栏上的一排热帖 ---------- */
.hot-section {
  padding-top: 44px;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 24px;
}

.section-heading h2 {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--ink);
}

.heading-note {
  font-size: 13px;
  color: var(--ink-3);
}

.hot-grid {
  display: flex;
  gap: 24px;
  overflow-x: auto;
  padding: 4px 0 28px;
  scrollbar-width: none;
}

.hot-grid::-webkit-scrollbar {
  display: none;
}

.hot-card {
  flex: 0 0 160px;
  width: 160px;
  cursor: pointer;
}

.hot-image {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.hot-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.2, 0.7, 0.3, 1);
}

.hot-card:hover .hot-image img {
  transform: scale(1.04);
}

/* 记忆点：牛皮纸价签，打孔、微转，悬停回正 */
.price-tag {
  position: absolute;
  left: 12px;
  bottom: 12px;
  display: inline-flex;
  align-items: center;
  padding: 4px 8px 4px 16px;
  border: 1px solid var(--tag-line);
  border-radius: var(--radius);
  background: var(--tag);
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
  transform: rotate(-3deg);
  transition: transform 0.25s ease;
  box-shadow: 0 1px 0 rgba(28, 26, 21, 0.08);
}

.price-tag::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 50%;
  width: 5px;
  height: 5px;
  margin-top: -2.5px;
  border-radius: 50%;
  background: var(--paper);
  box-shadow: inset 0 0 0 1px var(--tag-line);
}

.hot-card:hover .price-tag {
  transform: rotate(0deg);
}

.hot-title {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ---------- 全部商品：分类广告式列表 ---------- */
.section {
  margin-top: 56px;
  padding-top: 28px;
  border-top: 3px solid var(--ink);
  scroll-margin-top: calc(var(--nav-height) + 24px);
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--ink);
}

.section-count {
  font-size: 13px;
  color: var(--ink-3);
  font-variant-numeric: tabular-nums;
}

/* 筛选栏：一条纸面上的检索台，不加卡片底 */
.filter-bar {
  margin-bottom: 4px;
}

.filter-bar.card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 28px;
  padding: 0 0 20px;
  margin-bottom: 4px;
  border: 0;
  border-bottom: 1px solid var(--line);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: var(--ink-2);
  white-space: nowrap;
}

.filter-input {
  width: 92px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.filter-input::placeholder {
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--ink-3);
}

.filter-input:focus,
.filter-select:focus {
  border-color: var(--accent);
}

.filter-sep {
  color: var(--ink-3);
}

.filter-select {
  min-width: 128px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink);
  font-size: 14px;
}

.filter-bar .btn {
  height: 40px;
  padding: 0 20px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: transparent;
  color: var(--ink);
  font-size: 14px;
}

.filter-bar .btn:hover {
  border-color: var(--ink);
  background: var(--surface);
}

.filter-bar .btn-primary,
.filter-bar .btn-primary:hover {
  color: var(--paper);
}

.filter-bar .btn-primary {
  border-color: var(--ink);
  background: var(--ink);
}

.filter-bar .btn-primary:hover {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

/* 分类导航：文字页签，去掉 emoji 图标，选中项压一道朱砂线 */
.category-bar {
  margin-bottom: 32px;
}

.category-bar.card {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 28px;
  padding: 16px 0;
  margin-bottom: 32px;
  border: 0;
  border-bottom: 1px solid var(--line);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.category-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 4px 0 8px;
  color: var(--ink-2);
  font-size: 16px;
  cursor: pointer;
}

.category-icon {
  display: none;
}

.category-item:hover {
  color: var(--ink);
}

.category-item.active {
  color: var(--ink);
  font-weight: 600;
}

.category-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: var(--accent);
}

/* 商品网格：目录式排布，图片上留白，文字在纸上 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 40px 24px;
}

.goods-card {
  /* min-width:0 是必须的：全局手机版把网格写成 repeat(2,1fr)，
     1fr 的自动最小尺寸会被卡片里 nowrap 的标题/描述撑开，导致横向溢出 */
  min-width: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  overflow: visible;
  cursor: pointer;
}

.goods-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.2, 0.7, 0.3, 1);
}

.goods-card:hover .goods-image img {
  transform: scale(1.04);
}

.goods-info {
  min-width: 0;
  padding: 12px 0 0;
}

.goods-title {
  margin-bottom: 4px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-card:hover .goods-title {
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 4px;
}

.goods-desc {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-footer {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.goods-price {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.goods-original-price {
  font-size: 13px;
  color: var(--ink-3);
  text-decoration: line-through;
  text-decoration-thickness: 1px;
}

/* 状态 */
.loading-more,
.no-more {
  padding: 32px 0 8px;
  text-align: center;
  font-size: 14px;
  color: var(--ink-3);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 72px 0;
  text-align: center;
}

.empty-stamp {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border: 2px solid var(--accent);
  border-radius: 50%;
  color: var(--accent);
  font-size: 20px;
  font-weight: 700;
  transform: rotate(-8deg);
  opacity: 0.85;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
  color: var(--ink-2);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: var(--ink-2);
  font-variant-numeric: tabular-nums;
}

.page-size-select {
  height: 36px;
  padding: 0 8px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink);
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
  width: 36px;
  height: 36px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: transparent;
  color: var(--ink);
  font-size: 20px;
  line-height: 1;
  transition: border-color 0.15s, color 0.15s;
}

.page-btn:hover:not(.disabled) {
  border-color: var(--ink);
}

.page-btn.disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.current-page {
  min-width: 28px;
  text-align: center;
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

/* ---------- 响应式：先保 375 / 768，再回桌面 ---------- */
@media (max-width: 960px) {
  .home-hero {
    grid-template-columns: minmax(0, 1fr);
    gap: 32px;
    padding-top: 28px;
  }

  .hero-ledger {
    padding-left: 0;
    border-left: 0;
    border-top: 1px solid var(--line);
    padding-top: 24px;
  }

  .ledger {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0 24px;
  }

  .ledger-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    padding: 0;
    border-bottom: 0;
  }

  .ledger-row:first-child {
    border-top: 0;
  }

  .ledger-foot {
    margin-top: 4px;
  }
}

@media (max-width: 620px) {
  .home-page {
    padding-top: 24px;
  }

  .home-hero {
    padding: 24px 0 32px;
  }

  .hero-copy h1 {
    margin: 16px 0 16px;
    font-size: 36px;
  }

  .hero-intro {
    font-size: 16px;
    line-height: 1.8;
  }

  .hero-actions {
    flex-wrap: wrap;
  }

  .hero-actions .btn {
    flex: 1 1 auto;
    justify-content: center;
  }

  .ledger {
    gap: 0 12px;
  }

  .ledger-row dd {
    font-size: 16px;
  }

  .hot-grid {
    gap: 16px;
  }

  .hot-card {
    flex: 0 0 140px;
    width: 140px;
  }

  .section-heading h2,
  .section-header h2 {
    font-size: 24px;
  }

  .section-heading {
    flex-wrap: wrap;
    gap: 4px 16px;
  }

  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px 12px;
  }

  .pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .page-nav {
    justify-content: flex-end;
  }
}

/* ---------- 手动「手机版」模式：在桌面浏览器上预览手机布局 ---------- */
html[data-layout='mobile'] .home-page {
  padding: 24px 0 48px;
}

/* 手机版筛选栏由全局样式改为竖排，这里收回成可换行的横排：
   一整列铺满的输入框和按钮在 720px 宽的手机版里会显得像设置页，横排更紧凑 */
html[data-layout='mobile'] .filter-bar.card {
  flex-direction: row;
  align-items: center;
  gap: 12px 16px;
}

/* 手机版触控目标至少 44px 高 */
html[data-layout='mobile'] .filter-input,
html[data-layout='mobile'] .filter-select {
  height: 44px;
}

html[data-layout='mobile'] .filter-bar .btn {
  height: 44px;
}

html[data-layout='mobile'] .category-item {
  min-height: 44px;
  padding: 0 12px;
}

html[data-layout='mobile'] .page-btn {
  width: 44px;
  height: 44px;
}

html[data-layout='mobile'] .page-size-select {
  height: 44px;
}

html[data-layout='mobile'] .home-hero {
  grid-template-columns: minmax(0, 1fr);
  gap: 28px;
  padding: 20px 0 28px;
}

html[data-layout='mobile'] .hero-ledger {
  padding-left: 0;
  border-left: 0;
  border-top: 1px solid var(--line);
  padding-top: 20px;
}

html[data-layout='mobile'] .ledger {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
}

html[data-layout='mobile'] .ledger-row {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 0;
  border-bottom: 0;
}

html[data-layout='mobile'] .ledger-row:first-child {
  border-top: 0;
}

html[data-layout='mobile'] .ledger-row dd {
  font-size: 20px;
}

html[data-layout='mobile'] .hot-card {
  flex: 0 0 148px;
  width: 148px;
}

html[data-layout='mobile'] .section-heading h2,
html[data-layout='mobile'] .section-header h2 {
  font-size: 24px;
}

html[data-layout='mobile'] .home-page::after {
  display: none;
}

/* ---------- 深色主题 ---------- */
[data-theme='dark'] .home-page {
  --paper: #17140F;
  --surface: #201C14;
  --ink: #EFE6D4;
  --ink-2: #B7AB92;
  --ink-3: #8E8370;
  --line: #3B3527;
  --line-strong: #554C3A;
  --accent: #E76E4E;
  --tag: #2A2418;
  --tag-line: #4A402C;
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
