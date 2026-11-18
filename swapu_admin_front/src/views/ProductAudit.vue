<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">商品审核</h1>
        <p class="page-desc">默认展示待审核商品；通过后进入在售，驳回后进入审核驳回状态</p>
      </div>
      <div class="page-actions">
        <div class="tabs">
          <button
            v-for="tab in PRODUCT_AUDIT_TABS"
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
          <label>商品标题</label>
          <input
            v-model.trim="filters.title"
            class="input"
            type="text"
            placeholder="按标题模糊搜索"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>分类</label>
          <select v-model="filters.categoryId" class="select">
            <option value="">全部分类</option>
            <option v-for="item in categories" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
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
              <th>商品</th>
              <th>发布人</th>
              <th>分类</th>
              <th>价格</th>
              <th>成色</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="8">
                <div class="empty"><div class="empty-icon">📦</div>暂无商品</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td>
                <div class="cell-flex">
                  <img v-if="firstImage(row)" class="thumb" :src="firstImage(row)" alt="" />
                  <div v-else class="thumb thumb-empty">无图</div>
                  <div style="min-width: 0">
                    <span class="cell-title" :title="row.title">{{ row.title }}</span>
                    <div class="cell-sub">ID {{ row.id }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div>{{ row.username || '-' }}</div>
                <div class="cell-sub">UID {{ row.userId }}</div>
              </td>
              <td>{{ row.categoryName || '-' }}</td>
              <td class="num">
                ¥{{ money(row.price) }}
                <div class="cell-sub">原价 ¥{{ money(row.originalPrice) }}</div>
              </td>
              <td>{{ conditionText(row.productCondition) }}</td>
              <td>
                <span class="tag" :class="`tag-${enumOf(PRODUCT_STATUS, row.status).tag}`">
                  {{ enumOf(PRODUCT_STATUS, row.status).label }}
                </span>
              </td>
              <td class="num">{{ row.createTime }}</td>
              <td>
                <div class="table-actions">
                  <button class="btn-link" type="button" @click="openDetail(row)">详情</button>
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

    <AdminModal v-model="detailOpen" title="商品审核详情" width="920px">
      <div v-if="detailLoading" class="loading">加载中…</div>
      <div v-else-if="detail">
        <div class="detail-grid">
          <div>
            <div class="section-title">商品信息</div>
            <dl class="kv">
              <dt>标题</dt>
              <dd>{{ detail.product.title }}</dd>
              <dt>状态</dt>
              <dd>
                <span
                  class="tag"
                  :class="`tag-${enumOf(PRODUCT_STATUS, detail.product.status).tag}`"
                >
                  {{ enumOf(PRODUCT_STATUS, detail.product.status).label }}
                </span>
              </dd>
              <dt>分类 ID</dt>
              <dd>{{ detail.product.categoryId ?? '-' }}</dd>
              <dt>价格</dt>
              <dd>
                ¥{{ money(detail.product.price) }}
                <span class="muted">（原价 ¥{{ money(detail.product.originalPrice) }}）</span>
              </dd>
              <dt>成色</dt>
              <dd>{{ conditionText(detail.product.productCondition) }}</dd>
              <dt>库存</dt>
              <dd>{{ detail.product.quantity ?? '-' }}</dd>
              <dt>浏览量</dt>
              <dd>{{ detail.product.viewCount ?? 0 }}</dd>
              <dt>发布时间</dt>
              <dd>{{ detail.product.createTime }}</dd>
              <dt>描述</dt>
              <dd style="white-space: pre-wrap">{{ detail.product.description || '-' }}</dd>
            </dl>

            <div class="section-title">商品图片</div>
            <div v-if="detail.product.images && detail.product.images.length" class="image-grid">
              <img
                v-for="(image, index) in detail.product.images"
                :key="index"
                :src="image"
                :alt="`商品图 ${index + 1}`"
                @click="previewImage(image)"
              />
            </div>
            <div v-else class="hint">暂无图片</div>
          </div>

          <div>
            <div class="section-title">发布人</div>
            <dl v-if="detail.seller" class="kv">
              <dt>账号</dt>
              <dd>{{ detail.seller.username || '-' }}</dd>
              <dt>昵称</dt>
              <dd>{{ detail.seller.nickname || '-' }}</dd>
              <dt>学院</dt>
              <dd>{{ detail.seller.college || '-' }}</dd>
              <dt>信用分</dt>
              <dd>{{ detail.seller.creditScore ?? '-' }}</dd>
              <dt>用户 ID</dt>
              <dd>{{ detail.seller.id }}</dd>
            </dl>
            <div v-else class="hint">发布人信息不可用</div>

            <div class="section-title">审核流水</div>
            <div v-if="!detail.auditRecords || detail.auditRecords.length === 0" class="hint">
              暂无审核记录
            </div>
            <div v-for="record in detail.auditRecords" :key="record.id" class="audit-record">
              <div class="audit-record-head">
                <span class="tag" :class="`tag-${enumOf(AUDIT_RESULT, record.result).tag}`">
                  {{ enumOf(AUDIT_RESULT, record.result).label }}
                </span>
                <span class="hint">{{ record.reviewTime }}</span>
              </div>
              <div v-if="record.reason" class="hint">原因：{{ record.reason }}</div>
              <div class="hint">审核人 ID：{{ record.reviewerId ?? '-' }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <button class="btn" type="button" @click="detailOpen = false">关闭</button>
        <template v-if="detail && detail.product.status === 0">
          <button class="btn btn-danger" type="button" @click="openReview(detail.product, 'reject')">
            驳回
          </button>
          <button class="btn btn-primary" type="button" @click="openReview(detail.product, 'pass')">
            通过
          </button>
        </template>
      </template>
    </AdminModal>

    <AdminModal
      v-model="reviewOpen"
      :title="reviewMode === 'pass' ? '通过商品审核' : '驳回商品'"
      width="480px"
    >
      <div v-if="reviewTarget" class="notice">商品：{{ reviewTarget.title }}</div>

      <div v-if="reviewMode === 'reject'" class="field mt-16">
        <label>驳回原因 <span style="color: var(--c-danger)">*</span></label>
        <textarea
          v-model="reason"
          class="textarea"
          maxlength="500"
          placeholder="请填写驳回原因，将展示给卖家（最多 500 字）"
        ></textarea>
        <div class="field-hint">{{ reason.length }} / 500</div>
      </div>
      <p v-else class="hint mt-16">通过后商品将进入“在售”状态，用户端可见。</p>

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
import { getProductAuditDetail, pageProductAudits, reviewProduct } from '../api/audit'
import { listCategories } from '../api/category'
import {
  AUDIT_RESULT,
  PRODUCT_AUDIT_TABS,
  PRODUCT_STATUS,
  conditionText,
  enumOf
} from '../utils/enums'
import { money } from '../utils/format'
import { notify } from '../utils/notify'

const status = ref(0)
const filters = reactive({ title: '', categoryId: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')
const categories = ref([])

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

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
    const data = await pageProductAudits({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: status.value,
      categoryId: filters.categoryId,
      title: filters.title
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

async function loadCategories() {
  try {
    categories.value = (await listCategories()) || []
  } catch (err) {
    categories.value = []
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
  filters.title = ''
  filters.categoryId = ''
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

async function openDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getProductAuditDetail(row.id)
  } catch (err) {
    notify.error(err.message || '详情加载失败')
    detailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

function openReview(target, mode) {
  reviewTarget.value = target
  reviewMode.value = mode
  reason.value = ''
  reviewError.value = ''
  // 详情与审核弹窗不同时打开，避免遮罩层叠加
  detailOpen.value = false
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
    await reviewProduct({
      productId: reviewTarget.value.id,
      result: reviewMode.value === 'pass' ? 1 : 2,
      reason: reviewMode.value === 'reject' ? text : undefined
    })
    notify.success(reviewMode.value === 'pass' ? '审核已通过' : '已驳回该商品')
    reviewOpen.value = false
    detailOpen.value = false
    await load()
  } catch (err) {
    reviewError.value = err.message || '提交失败'
    // 4203：商品状态已被其他管理员改变，刷新列表
    if (err.code === 4203) load()
  } finally {
    reviewSubmitting.value = false
  }
}

function previewImage(url) {
  window.open(url, '_blank')
}

onMounted(() => {
  loadCategories()
  load()
})
</script>
