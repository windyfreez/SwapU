<template>
  <div class="user-home-page">
    <div class="container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>个人主页</span>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <template v-else-if="profile">
        <!-- 用户信息卡 -->
        <div class="card profile-card">
          <div class="profile-avatar">
            <img v-if="profile.avatar" :src="cleanImageUrl(profile.avatar)" :alt="displayName" />
            <span v-else>👤</span>
          </div>

          <div class="profile-main">
            <h1 class="profile-name">{{ displayName }}</h1>
            <p class="profile-college">{{ profile.college || '暂未填写学院' }}</p>

            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-value">{{ profile.sellingCount || 0 }}</span>
                <span class="stat-label">在售商品</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profile.soldCount || 0 }}</span>
                <span class="stat-label">已售出</span>
              </div>
              <div class="stat-item">
                <span class="stat-value credit-value">{{ profile.creditScore ?? '—' }}</span>
                <span class="stat-label">信用分</span>
              </div>
            </div>
          </div>

          <div class="profile-actions">
            <template v-if="isSelf">
              <span class="self-tip">这是你自己的主页</span>
            </template>
            <template v-else>
              <button class="btn btn-primary" @click="openCommentModal">评价 TA</button>
            </template>
          </div>
        </div>

        <!-- TA的商品 -->
        <section class="card section-card">
          <div class="section-head">
            <h2 class="section-title">TA的商品</h2>
            <div class="tab-bar">
              <button
                v-for="tab in productTabs"
                :key="tab.label"
                class="tab-btn"
                :class="{ active: activeProductTab === tab.value }"
                @click="switchProductTab(tab.value)"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div v-if="productLoading" class="loading-state">
            <p>加载中...</p>
          </div>

          <template v-else>
            <div v-if="products.length > 0" class="goods-grid">
              <div v-for="item in products" :key="item.id" class="goods-card">
                <div class="goods-image" @click="goProductDetail(item.id)">
                  <img :src="cleanImageUrl(item.images)" :alt="item.title" />
                  <span class="goods-status" :class="item.status === 2 ? 'sold' : 'selling'">
                    {{ item.status === 2 ? '已售出' : '在售' }}
                  </span>
                </div>
                <div class="goods-info">
                  <h3 class="goods-title" @click="goProductDetail(item.id)">{{ item.title }}</h3>
                  <div class="goods-footer">
                    <span class="goods-price">¥{{ item.price }}</span>
                    <span v-if="item.originalPrice" class="goods-original-price">¥{{ item.originalPrice }}</span>
                  </div>
                  <div v-if="!isSelf" class="goods-actions">
                    <button class="btn btn-sm btn-outline" @click="consult(item)">咨询</button>
                    <button
                      class="btn btn-sm btn-primary"
                      :disabled="item.status === 2"
                      @click="buy(item)"
                    >
                      {{ item.status === 2 ? '已售出' : '下单' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <span class="empty-icon">📦</span>
              <p>暂无商品</p>
            </div>

            <div v-if="productTotal > productPageSize" class="pagination">
              <span class="pagination-info">共 {{ productTotal }} 件商品</span>
              <div class="page-nav">
                <button
                  class="page-btn"
                  :class="{ disabled: productPage === 1 }"
                  @click="changeProductPage(productPage - 1)"
                >
                  ‹
                </button>
                <span class="current-page">{{ productPage }}</span>
                <button
                  class="page-btn"
                  :class="{ disabled: productPage >= productTotalPages }"
                  @click="changeProductPage(productPage + 1)"
                >
                  ›
                </button>
              </div>
            </div>
          </template>
        </section>

        <!-- TA收到的评价 -->
        <section class="card section-card">
          <div class="section-head">
            <h2 class="section-title">TA收到的评价</h2>
            <span class="section-sub">仅展示已通过审核的评价</span>
          </div>

          <div v-if="commentLoading" class="loading-state">
            <p>加载中...</p>
          </div>

          <template v-else>
            <div v-if="comments.length > 0" class="comment-list">
              <div v-for="item in comments" :key="item.id" class="comment-item">
                <div class="comment-avatar" @click="goUserHome(item.senderId)">
                  <img v-if="item.senderAvatar" :src="cleanImageUrl(item.senderAvatar)" :alt="item.senderName" />
                  <span v-else>👤</span>
                </div>
                <div class="comment-body">
                  <div class="comment-head">
                    <span class="comment-name" @click="goUserHome(item.senderId)">
                      {{ item.senderName || '匿名用户' }}
                    </span>
                    <span class="badge" :class="item.commentType === 0 ? 'badge-red' : 'badge-green'">
                      {{ item.commentTypeDesc || (item.commentType === 0 ? '差评' : '好评') }}
                    </span>
                    <span class="comment-time">{{ formatTime(item.sendTime) }}</span>
                  </div>
                  <p class="comment-content">{{ item.content }}</p>
                  <img v-if="item.image" class="comment-image" :src="cleanImageUrl(item.image)" alt="评价图片" />
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <span class="empty-icon">💬</span>
              <p>TA还没有收到评价</p>
            </div>

            <div v-if="commentTotal > commentPageSize" class="pagination">
              <span class="pagination-info">共 {{ commentTotal }} 条评价</span>
              <div class="page-nav">
                <button
                  class="page-btn"
                  :class="{ disabled: commentPage === 1 }"
                  @click="changeCommentPage(commentPage - 1)"
                >
                  ‹
                </button>
                <span class="current-page">{{ commentPage }}</span>
                <button
                  class="page-btn"
                  :class="{ disabled: commentPage >= commentTotalPages }"
                  @click="changeCommentPage(commentPage + 1)"
                >
                  ›
                </button>
              </div>
            </div>
          </template>
        </section>
      </template>

      <div v-else class="empty-state">
        <span class="empty-icon">👤</span>
        <p>用户不存在</p>
        <router-link to="/" class="btn btn-outline">返回首页</router-link>
      </div>
    </div>

    <!-- 评价弹窗 -->
    <div v-if="showCommentModal" class="modal-mask" @click="closeCommentModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">评价 {{ displayName }}</span>
          <span class="modal-close" @click="closeCommentModal">×</span>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">评价类型</label>
            <div class="type-options">
              <label class="type-option" :class="{ active: commentForm.commentType === 1 }">
                <input v-model.number="commentForm.commentType" type="radio" :value="1" />
                👍 好评
              </label>
              <label class="type-option" :class="{ active: commentForm.commentType === 0 }">
                <input v-model.number="commentForm.commentType" type="radio" :value="0" />
                👎 差评
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">评价内容 <span class="required">*</span></label>
            <textarea
              v-model="commentForm.content"
              class="form-textarea"
              maxlength="1000"
              placeholder="说说这次交易的体验吧..."
            ></textarea>
            <p class="form-hint">{{ commentForm.content.length }}/1000</p>
          </div>

          <div class="form-group">
            <label class="form-label">插入图片</label>
            <div class="image-upload">
              <div v-if="commentForm.image" class="image-preview">
                <img :src="cleanImageUrl(commentForm.image)" alt="评价图片" />
                <span class="image-remove" title="移除图片" @click="removeImage">×</span>
              </div>
              <template v-else>
                <button
                  type="button"
                  class="btn btn-outline btn-sm"
                  :disabled="uploadingImage"
                  @click="triggerImageUpload"
                >
                  {{ uploadingImage ? '上传中...' : '＋ 选择图片' }}
                </button>
                <span class="upload-tip">仅支持一张，JPG / PNG</span>
              </template>
              <input
                ref="fileInput"
                type="file"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleImageUpload"
              />
            </div>
          </div>

          <p v-if="commentError" class="form-error">{{ commentError }}</p>
        </div>

        <div class="modal-footer">
          <button class="btn" @click="closeCommentModal">取消</button>
          <button class="btn btn-primary" :disabled="submittingComment" @click="submitComment">
            {{ submittingComment ? '提交中...' : '提交评价' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const productPageSize = 8
const commentPageSize = 5

const loading = ref(true)
const profile = ref(null)

const products = ref([])
const productPage = ref(1)
const productTotal = ref(0)
const activeProductTab = ref('')
const productLoading = ref(false)

const comments = ref([])
const commentPage = ref(1)
const commentTotal = ref(0)
const commentLoading = ref(false)

const showCommentModal = ref(false)
const submittingComment = ref(false)
const commentError = ref('')
const uploadingImage = ref(false)
const fileInput = ref(null)
const commentForm = ref({
  commentType: 1,
  content: '',
  image: ''
})

const productTabs = [
  { label: '全部', value: '' },
  { label: '在售中', value: '1' },
  { label: '已售出', value: '2' }
]

const userId = computed(() => route.params.id)

const isSelf = computed(() => {
  const currentUserId = localStorage.getItem('userId')
  return !!currentUserId && String(currentUserId) === String(userId.value)
})

const displayName = computed(() => {
  if (!profile.value) return ''
  return profile.value.nickname || profile.value.username || '用户'
})

const productTotalPages = computed(() =>
  Math.max(1, Math.ceil(productTotal.value / productPageSize))
)

const commentTotalPages = computed(() =>
  Math.max(1, Math.ceil(commentTotal.value / commentPageSize))
)

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

const fetchProfile = async () => {
  if (!userId.value) {
    loading.value = false
    return
  }

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/user/${userId.value}/profile`, {
      headers: {
        'token': token || ''
      }
    })
    const result = await parseResult(response)
    if (result.ok) {
      profile.value = result.data
    }
  } catch (error) {
    console.error('获取用户主页信息失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  productLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      page: productPage.value.toString(),
      pageSize: productPageSize.toString()
    })
    if (activeProductTab.value) {
      params.append('status', activeProductTab.value)
    }

    const response = await fetch(`/product/user/${userId.value}?${params}`, {
      headers: {
        'token': token || ''
      }
    })
    const result = await parseResult(response)
    if (result.ok && result.data) {
      products.value = result.data.records || []
      productTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('获取用户商品失败:', error)
  } finally {
    productLoading.value = false
  }
}

const fetchComments = async () => {
  commentLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({
      page: commentPage.value.toString(),
      pageSize: commentPageSize.toString()
    })

    const response = await fetch(`/comment/received/${userId.value}?${params}`, {
      headers: {
        'token': token || ''
      }
    })
    const result = await parseResult(response)
    if (result.ok && result.data) {
      comments.value = result.data.records || []
      commentTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('获取用户评价失败:', error)
  } finally {
    commentLoading.value = false
  }
}

const switchProductTab = (value) => {
  if (activeProductTab.value === value) return
  activeProductTab.value = value
  productPage.value = 1
  fetchProducts()
}

const changeProductPage = (newPage) => {
  if (newPage < 1 || newPage > productTotalPages.value || productLoading.value) return
  productPage.value = newPage
  fetchProducts()
}

const changeCommentPage = (newPage) => {
  if (newPage < 1 || newPage > commentTotalPages.value || commentLoading.value) return
  commentPage.value = newPage
  fetchComments()
}

const goProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const goUserHome = (targetUserId) => {
  if (!targetUserId) return
  router.push(`/user-home/${targetUserId}`)
}

const requireLogin = () => {
  if (localStorage.getItem('token')) return true
  alert('请先登录')
  router.push('/login')
  return false
}

const consult = (item) => {
  if (!requireLogin()) return

  sessionStorage.setItem('chatTargetUser', JSON.stringify({
    userId: profile.value.id,
    username: displayName.value,
    avatar: profile.value.avatar,
    productId: item.id,
    productTitle: item.title,
    productImage: item.images?.[0] || ''
  }))

  router.push({
    path: '/messages/chat',
    query: { userId: profile.value.id }
  })
}

const buy = (item) => {
  if (item.status === 2) {
    alert('该商品已售出')
    return
  }
  if (!requireLogin()) return
  router.push(`/order/create/${item.id}`)
}

const openCommentModal = () => {
  if (!requireLogin()) return
  commentError.value = ''
  commentForm.value = { commentType: 1, content: '', image: '' }
  showCommentModal.value = true
}

const closeCommentModal = () => {
  showCommentModal.value = false
}

// 触发选择图片
const triggerImageUpload = () => {
  fileInput.value?.click()
}

// 评价图片上传：只支持一张，复用后端通用上传接口，拿回 OSS 地址后存进表单
const handleImageUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  uploadingImage.value = true
  commentError.value = ''
  try {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    const response = await fetch('/user/common/upload', {
      method: 'POST',
      headers: {
        'token': token || ''
      },
      body: formData
    })

    const result = await parseResult(response)
    if (!result.ok) {
      commentError.value = result.msg
      return
    }
    commentForm.value.image = result.data
  } catch (error) {
    console.error('评价图片上传失败:', error)
    commentError.value = '图片上传失败，请稍后重试'
  } finally {
    uploadingImage.value = false
    // 清空 input 的值，保证同一张图片删掉之后还能再次选中
    event.target.value = ''
  }
}

// 移除已选图片
const removeImage = () => {
  commentForm.value.image = ''
}

const submitComment = async () => {
  const content = commentForm.value.content.trim()
  if (!content) {
    commentError.value = '评价内容不能为空'
    return
  }

  submittingComment.value = true
  commentError.value = ''

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/comment/add', {
      method: 'POST',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        receiverId: profile.value.id,
        content,
        commentType: commentForm.value.commentType,
        image: commentForm.value.image
      })
    })

    const result = await parseResult(response)
    if (!result.ok) {
      commentError.value = result.msg
      return
    }

    showCommentModal.value = false
    // 新评价处于审核中，不会出现在对方公开的评价列表里，无需刷新列表
    alert('评价提交成功，等待审核')
  } catch (error) {
    console.error('提交评价失败:', error)
    commentError.value = '提交失败，请稍后重试'
  } finally {
    submittingComment.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    if (!route.params.id) return
    loading.value = true
    profile.value = null
    products.value = []
    comments.value = []
    productPage.value = 1
    commentPage.value = 1
    activeProductTab.value = ''
    fetchProfile()
    fetchProducts()
    fetchComments()
  }
)

onMounted(() => {
  fetchProfile()
  fetchProducts()
  fetchComments()
})
</script>

<style scoped>
.user-home-page {
  padding-top: 20px;
  min-height: 60vh;
}

/* 用户信息卡 */
.profile-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 26px 28px;
  margin-bottom: 20px;
}

.profile-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 6px;
}

.profile-college {
  font-size: 13px;
  color: var(--c-text-3);
  margin-bottom: 16px;
}

.profile-stats {
  display: flex;
  gap: 34px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--c-text);
}

.stat-value.credit-value {
  color: var(--c-warning);
}

.stat-label {
  font-size: 12px;
  color: var(--c-text-3);
}

.profile-actions {
  flex-shrink: 0;
}

.self-tip {
  font-size: 13px;
  color: var(--c-text-3);
}

/* 区块 */
.section-card {
  padding: 20px 22px 22px;
  margin-bottom: 20px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--c-text);
}

.section-sub {
  font-size: 12px;
  color: var(--c-text-3);
}

/* 商品分类切换 */
.tab-bar {
  display: flex;
  gap: 6px;
}

.tab-btn {
  padding: 6px 16px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: var(--c-card);
  font-size: 13px;
  color: var(--c-text-2);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.tab-btn.active {
  background: var(--c-primary-light);
  border-color: var(--c-primary);
  color: var(--c-primary);
  font-weight: 600;
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
}

.goods-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.2s ease;
}

.goods-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-border-strong);
}

.goods-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--c-disabled-bg);
  overflow: hidden;
  cursor: pointer;
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

.goods-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.6;
}

.goods-status.selling {
  background: var(--c-success-light);
  color: var(--c-success);
}

.goods-status.sold {
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}

.goods-info {
  padding: 12px 14px 14px;
}

.goods-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.goods-title:hover {
  color: var(--c-primary);
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

.goods-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.goods-actions .btn {
  flex: 1;
}

/* 评价列表 */
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--c-border);
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 4px;
}

.comment-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  cursor: pointer;
}

.comment-name:hover {
  color: var(--c-primary);
}

.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--c-text-3);
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

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 2px 0;
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

/* 评价弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 440px;
  max-width: 92vw;
  background: var(--c-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  font-size: 22px;
  color: var(--c-text-3);
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
}

.type-options {
  display: flex;
  gap: 12px;
}

.type-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text-2);
  cursor: pointer;
  transition: all 0.2s;
}

.type-option input {
  display: none;
}

.type-option.active {
  border-color: var(--c-primary);
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}

.form-error {
  font-size: 13px;
  color: var(--c-danger);
}

/* 评价图片上传:只允许一张 */
.image-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-input {
  display: none;
}

.upload-tip {
  font-size: 12px;
  color: var(--c-text-3);
}

.image-preview {
  position: relative;
  width: 84px;
  height: 84px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
}

.image-remove:hover {
  background: var(--c-danger);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 20px 20px;
}

/* 窄屏降级 */
@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
    text-align: center;
  }

  .profile-stats {
    justify-content: center;
  }

  .comment-time {
    margin-left: 0;
    width: 100%;
  }
}
</style>
