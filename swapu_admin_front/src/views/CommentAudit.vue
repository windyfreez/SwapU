<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">评论审核</h1>
        <p class="page-desc">评论为用户对用户的评价；通过后对用户端可见，驳回后进入审核驳回状态</p>
      </div>
      <div class="page-actions">
        <div class="tabs">
          <button
            v-for="tab in COMMENT_AUDIT_TABS"
            :key="tab.value"
            class="tab"
            :class="{ active: status === tab.value }"
            type="button"
            @click="changeStatus(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="filters">
        <div class="field grow">
          <label>评论内容</label>
          <input
            v-model.trim="filters.keyword"
            class="input"
            type="text"
            placeholder="按评论内容模糊搜索"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>被评论人 ID</label>
          <input
            v-model.trim="filters.receiverId"
            class="input"
            type="text"
            placeholder="精确匹配用户 ID"
            @keyup.enter="search"
          />
        </div>
        <div class="filters-actions">
          <button class="btn" type="button" @click="reset">重置</button>
          <button class="btn btn-primary" type="button" @click="search">查询</button>
        </div>
      </div>

      <div v-if="error" class="error-box" style="margin: 14px 18px 0">{{ error }}</div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>评论内容</th>
              <th>评论人</th>
              <th>被评论人</th>
              <th>状态</th>
              <th>评论时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="6">
                <div class="empty"><div class="empty-icon">💬</div>暂无评论</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td>
                <div class="cell-flex">
                  <img
                    v-if="firstImage(row)"
                    class="thumb"
                    :src="firstImage(row)"
                    alt=""
                  />
                  <div style="min-width: 0">
                    <span class="cell-title" :title="row.content">{{ row.content }}</span>
                    <div class="cell-sub">ID {{ row.id }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div>{{ row.username || '-' }}</div>
                <div class="cell-sub">UID {{ row.userId }}</div>
              </td>
              <td>
                <div>{{ row.receiverNickname || row.receiverUsername || '-' }}</div>
                <div class="cell-sub">UID {{ row.receiverId }}</div>
              </td>
              <td>
                <span class="tag" :class="`tag-${enumOf(COMMENT_STATUS, row.status).tag}`">
                  {{ enumOf(COMMENT_STATUS, row.status).label }}
                </span>
              </td>
              <td class="num">{{ row.createTime }}</td>
              <td>
                <div class="table-actions">
                  <button class="btn-link" type="button" @click="openView(row)">查看</button>
                  <template v-if="row.status === 0">
                    <button class="btn-link" type="button" @click="openReview(row, 'pass')">通过</button>
                    <button class="btn-link danger" type="button" @click="openReview(row, 'reject')">
                      驳回
                    </button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminPager v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" />
    </div>

    <AdminModal v-model="viewOpen" title="评论详情" width="600px">
      <div v-if="current">
        <dl class="kv">
          <dt>评论人</dt>
          <dd>{{ current.username || '-' }}（UID {{ current.userId }}）</dd>
          <dt>被评论人</dt>
          <dd>
            {{ current.receiverNickname || current.receiverUsername || '-' }}
            （UID {{ current.receiverId }}）
          </dd>
          <dt>状态</dt>
          <dd>
            <span class="tag" :class="`tag-${enumOf(COMMENT_STATUS, current.status).tag}`">
              {{ enumOf(COMMENT_STATUS, current.status).label }}
            </span>
          </dd>
          <dt>评论时间</dt>
          <dd>{{ current.createTime }}</dd>
          <dt>评论内容</dt>
          <dd style="white-space: pre-wrap">{{ current.content }}</dd>
        </dl>

        <div class="section-title">评论图片</div>
        <div v-if="current.images && current.images.length" class="image-grid">
          <img
            v-for="(image, index) in current.images"
            :key="index"
            :src="image"
            :alt="`评论图 ${index + 1}`"
            @click="previewImage(image)"
          />
        </div>
        <div v-else class="hint">无图片</div>
      </div>

      <template #footer>
        <button class="btn" type="button" @click="viewOpen = false">关闭</button>
        <template v-if="current && current.status === 0">
          <button class="btn btn-danger" type="button" @click="openReview(current, 'reject')">
            驳回
          </button>
          <button class="btn btn-primary" type="button" @click="openReview(current, 'pass')">
            通过
          </button>
        </template>
      </template>
    </AdminModal>

    <AdminModal
      v-model="reviewOpen"
      :title="reviewMode === 'pass' ? '通过评论审核' : '驳回评论'"
      width="480px"
    >
      <div v-if="reviewTarget" class="notice">
        评论：{{ ellipsis(reviewTarget.content, 80) }}
      </div>

      <div v-if="reviewMode === 'reject'" class="field mt-16">
        <label>驳回原因 <span style="color: var(--c-danger)">*</span></label>
        <textarea
          v-model="reason"
          class="textarea"
          maxlength="500"
          placeholder="请填写驳回原因（最多 500 字）"
        ></textarea>
        <div class="field-hint">{{ reason.length }} / 500</div>
      </div>
      <p v-else class="hint mt-16">通过后评论将对用户端可见。</p>

      <div v-if="reviewError" class="error-box mt-16">{{ reviewError }}</div>

      <template #footer>
        <button class="btn" type="button" @click="reviewOpen = false">取消</button>
        <button
          class="btn"
          :class="reviewMode === 'pass' ? 'btn-primary' : 'btn-danger'"
          type="button"
          :disabled="reviewSubmitting"
          @click="submitReview"
        >
          {{ reviewSubmitting ? '提交中…' : '确认' }}
        </button>
      </template>
    </AdminModal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import AdminModal from '../components/AdminModal.vue'
import AdminPager from '../components/AdminPager.vue'
import { pageCommentAudits, reviewComment } from '../api/audit'
import { COMMENT_AUDIT_TABS, COMMENT_STATUS, enumOf } from '../utils/enums'
import { ellipsis } from '../utils/format'
import { notify } from '../utils/notify'

const status = ref(0)
const filters = reactive({ keyword: '', receiverId: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

const viewOpen = ref(false)
const current = ref(null)

const reviewOpen = ref(false)
const reviewMode = ref('pass')
const reviewTarget = ref(null)
const reason = ref('')
const reviewError = ref('')
const reviewSubmitting = ref(false)

function firstImage(row) {
  return Array.isArray(row.images) && row.images.length > 0 ? row.images[0] : ''
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageCommentAudits({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: status.value,
      keyword: filters.keyword,
      receiverId: filters.receiverId
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (err) {
    error.value = err.message || '加载失败'
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function changeStatus(value) {
  if (status.value === value) return
  status.value = value
  if (query.pageNum !== 1) {
    query.pageNum = 1
  } else {
    load()
  }
}

function search() {
  if (query.pageNum !== 1) {
    query.pageNum = 1
  } else {
    load()
  }
}

function reset() {
  filters.keyword = ''
  filters.receiverId = ''
  status.value = 0
  if (query.pageNum !== 1) {
    query.pageNum = 1
  } else {
    load()
  }
}

watch(
  () => [query.pageNum, query.pageSize],
  () => load()
)

function openView(row) {
  current.value = row
  viewOpen.value = true
}

function openReview(target, mode) {
  reviewTarget.value = target
  reviewMode.value = mode
  reason.value = ''
  reviewError.value = ''
  viewOpen.value = false
  reviewOpen.value = true
}

async function submitReview() {
  if (!reviewTarget.value) return
  const text = reason.value.trim()
  if (reviewMode.value === 'reject' && !text) {
    reviewError.value = '驳回时必须填写原因'
    return
  }

  reviewSubmitting.value = true
  reviewError.value = ''
  try {
    await reviewComment({
      commentId: reviewTarget.value.id,
      result: reviewMode.value === 'pass' ? 1 : 2,
      reason: reviewMode.value === 'reject' ? text : undefined
    })
    notify.success(reviewMode.value === 'pass' ? '审核已通过' : '已驳回该评论')
    reviewOpen.value = false
    await load()
  } catch (err) {
    reviewError.value = err.message || '提交失败'
    if (err.code === 4203) load()
  } finally {
    reviewSubmitting.value = false
  }
}

function previewImage(url) {
  window.open(url, '_blank')
}

onMounted(load)
</script>
