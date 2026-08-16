<template>
  <AccountLayout active="footprints">
    <h1 class="page-title">我的足迹</h1>

    <div v-if="loading && records.length === 0" class="loading-state">
      <p>加载中...</p>
    </div>

    <div v-else-if="records.length === 0" class="empty-state">
      <span class="empty-icon">👣</span>
      <p>暂无浏览记录</p>
      <router-link to="/" class="btn btn-outline">去逛逛</router-link>
    </div>

    <template v-else>
      <div class="footprint-grid">
        <div
          v-for="item in records"
          :key="item.id"
          class="footprint-card"
          @click="goToDetail(item.id)"
        >
          <div class="card-image">
            <img :src="getFirstImage(item.images)" :alt="item.title" />
            <span v-if="item.status === 2" class="status-badge badge-gray">已售</span>
            <span v-else-if="item.status === 3" class="status-badge badge-gray">已下架</span>
          </div>
          <div class="card-info">
            <h3 class="card-title">{{ item.title || '商品' + item.id }}</h3>
            <div class="card-footer">
              <span class="card-price">¥{{ item.price }}</span>
              <span v-if="item.viewCount" class="card-meta">{{ item.viewCount }}次浏览</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more" @click="loadMore">
        <span v-if="loading">加载中...</span>
        <span v-else>加载更多</span>
      </div>
      <div v-else class="no-more">没有更多了</div>
    </template>
  </AccountLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AccountLayout from '../components/AccountLayout.vue'

const router = useRouter()
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

const getFirstImage = (images) => {
  if (!images) return ''

  if (typeof images === 'string') {
    images = images.trim()
    if (images.startsWith('[') || images.startsWith('{')) {
      try {
        const parsed = JSON.parse(images)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return getFirstImage(parsed[0])
        }
        return parsed
      } catch {
        return images.replace(/[`'""]/g, '')
      }
    }
    return images.replace(/[`'""]/g, '')
  }

  if (Array.isArray(images) && images.length > 0) {
    return getFirstImage(images[0])
  }

  return String(images || '')
}

const fetchFootprints = async (reset = false) => {
  if (loading.value) return

  if (reset) {
    pageNum.value = 1
    records.value = []
    hasMore.value = true
  }

  const token = localStorage.getItem('token')
  if (!token) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const params = new URLSearchParams({
      pageNum: pageNum.value.toString(),
      pageSize: pageSize.value.toString()
    })
    const response = await fetch(`/view-history/list?${params}`, {
      headers: {
        'token': token
      }
    })

    if (!response.ok) {
      console.error('获取浏览记录失败:', response.status)
      return
    }

    const data = await response.json()
    if (data.code === 200 && data.data) {
      const list = data.data.records || []
      total.value = data.data.total || 0
      if (reset) {
        records.value = list
      } else {
        records.value = [...records.value, ...list]
      }
      hasMore.value = records.value.length < total.value
    }
  } catch (error) {
    console.error('获取浏览记录失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (!hasMore.value || loading.value) return
  pageNum.value++
  fetchFootprints()
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  fetchFootprints(true)
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
}

.footprint-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.footprint-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.footprint-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-border-strong);
}

.card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--c-disabled-bg);
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.footprint-card:hover .card-image img {
  transform: scale(1.04);
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
}

.status-badge.badge-gray {
  background: rgba(107, 114, 128, 0.85);
}

.card-info {
  padding: 12px 14px 14px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.card-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-danger);
}

.card-meta {
  font-size: 12px;
  color: var(--c-text-3);
}

.load-more {
  text-align: center;
  padding: 18px;
  color: var(--c-primary);
  font-size: 14px;
  cursor: pointer;
}

.load-more:hover {
  color: var(--c-primary-hover);
}

.no-more {
  text-align: center;
  padding: 18px;
  color: var(--c-text-3);
  font-size: 13px;
}
</style>
