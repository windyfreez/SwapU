<template>
  <AccountLayout active="comments">
    <div class="card comments-card">
      <div class="card-title">
        我发出的评价
        <span class="title-sub">审核中、过审、违禁的评价都会展示在这里</span>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <template v-else>
        <div v-if="comments.length > 0" class="comment-list">
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <div class="comment-head">
              <span class="comment-target">
                评价对象：{{ item.receiverName || ('用户' + item.receiverId) }}
              </span>
              <span class="badge" :class="statusClass(item.status)">
                {{ item.statusDesc || statusText(item.status) }}
              </span>
              <span class="badge" :class="item.commentType === 0 ? 'badge-red' : 'badge-green'">
                {{ item.commentTypeDesc || (item.commentType === 0 ? '差评' : '好评') }}
              </span>
            </div>

            <p class="comment-content">{{ item.content }}</p>
            <img v-if="item.image" class="comment-image" :src="cleanImageUrl(item.image)" alt="评价图片" />

            <div class="comment-foot">
              <span class="comment-time">{{ formatTime(item.sendTime) }}</span>
              <button class="btn btn-sm btn-danger" @click="deleteComment(item)">删除</button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <span class="empty-icon">💬</span>
          <p>你还没有发出过评价</p>
          <router-link to="/" class="btn btn-outline">去逛逛</router-link>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <span class="pagination-info">共 {{ total }} 条评价</span>
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
              :class="{ disabled: page >= totalPages }"
              @click="changePage(page + 1)"
            >
              ›
            </button>
          </div>
        </div>
      </template>
    </div>
  </AccountLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AccountLayout from '../components/AccountLayout.vue'

const pageSize = 5

const comments = ref([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

// 统一解析后端 Result 结构：code 为 200 才算成功
const parseResult = async (response) => {
  if (!response.ok) {
    return { ok: false, msg: response.status === 401 ? '请先登录' : '请求失败，请稍后重试' }
  }
  const result = await response.json()
  if (result.code !== 200) {
    return { ok: false, msg: result.msg || '请求失败' }
  }
  return { ok: true, data: result.data }
}

const cleanImageUrl = (url) => {
  if (!url) return ''

  if (typeof url === 'string') {
    url = url.trim()
    if (url.startsWith('[') || url.startsWith('{')) {
      try {
        const parsed = JSON.parse(url)
        if (Array.isArray(parsed) && parsed.length > 0) {
          url = parsed[0]
        } else if (typeof parsed === 'string') {
          url = parsed
        }
      } catch {
        return url.replace(/[`'""]/g, '')
      }
    }
    return url.replace(/[`'""]/g, '')
  }

  if (Array.isArray(url)) {
    return url.length > 0 ? cleanImageUrl(url[0]) : ''
  }

  return String(url)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

// 审核状态：0审核中 1过审 2违禁
const statusText = (status) => {
  const textMap = {
    0: '审核中',
    1: '过审',
    2: '违禁'
  }
  return textMap[status] || '未知状态'
}

const statusClass = (status) => {
  const classMap = {
    0: 'badge-orange',
    1: 'badge-green',
    2: 'badge-red'
  }
  return classMap[status] || 'badge-gray'
}

const fetchComments = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/comment/my?page=${page.value}&pageSize=${pageSize}`, {
      headers: {
        'token': token || ''
      }
    })
    const result = await parseResult(response)
    if (result.ok && result.data) {
      comments.value = result.data.records || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('获取我发出的评价失败:', error)
  } finally {
    loading.value = false
  }
}

const changePage = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value || loading.value) return
  page.value = newPage
  fetchComments()
}

const deleteComment = async (item) => {
  if (!confirm('确定要删除这条评价吗？')) return

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/comment/${item.id}`, {
      method: 'DELETE',
      headers: {
        'token': token || ''
      }
    })
    const result = await parseResult(response)
    if (!result.ok) {
      alert(result.msg)
      return
    }

    // 删除当前页最后一条时回退一页，避免停留在空页
    if (comments.value.length === 1 && page.value > 1) {
      page.value = page.value - 1
    }
    await fetchComments()
  } catch (error) {
    console.error('删除评价失败:', error)
    alert('删除失败，请稍后重试')
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comments-card {
  padding-bottom: 8px;
}

.card-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.title-sub {
  font-size: 12px;
  font-weight: 400;
  color: var(--c-text-3);
}

.comment-list {
  display: flex;
  flex-direction: column;
  padding: 0 20px;
}

.comment-item {
  padding: 18px 0;
  border-bottom: 1px solid var(--c-border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.comment-target {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.comment-content {
  font-size: 14px;
  color: var(--c-text-2);
  line-height: 1.7;
  word-break: break-word;
}

.comment-image {
  max-width: 180px;
  margin-top: 10px;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
}

.comment-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.comment-time {
  font-size: 12px;
  color: var(--c-text-3);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
}

.pagination-info {
  font-size: 13px;
  color: var(--c-text-2);
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
  cursor: pointer;
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
