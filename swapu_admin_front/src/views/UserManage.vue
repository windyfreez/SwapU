<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">查询平台用户、启停账号、调整信用分，并查看买卖双方交易统计</p>
      </div>
    </div>

    <div class="panel">
      <div class="filters">
        <div class="field grow">
          <label>用户名 / 昵称</label>
          <input
            v-model.trim="filters.keyword"
            class="input"
            type="text"
            placeholder="模糊搜索用户名或昵称"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>学号</label>
          <input
            v-model.trim="filters.studentId"
            class="input"
            type="text"
            placeholder="模糊搜索学号"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>手机号</label>
          <input
            v-model.trim="filters.phone"
            class="input"
            type="text"
            placeholder="模糊搜索手机号"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>状态</label>
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option :value="1">正常</option>
            <option :value="0">禁用</option>
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
              <th>用户</th>
              <th>学号</th>
              <th>学院</th>
              <th>手机号</th>
              <th>余额</th>
              <th>信用分</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="9">
                <div class="empty"><div class="empty-icon">👤</div>没有符合条件的用户</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td>
                <div class="cell-title">{{ row.nickname || row.username || '-' }}</div>
                <div class="cell-sub">{{ row.username || '-' }} · UID {{ row.id }}</div>
              </td>
              <td>{{ row.studentId || '-' }}</td>
              <td>{{ row.college || '-' }}</td>
              <td class="num">{{ row.phone || '-' }}</td>
              <td class="num">¥{{ money(row.balance) }}</td>
              <td class="num">
                <span :class="{ 'credit-low': (row.creditScore ?? 0) < 60 }">
                  {{ row.creditScore ?? '-' }}
                </span>
              </td>
              <td>
                <span class="tag" :class="`tag-${enumOf(USER_STATUS, row.status).tag}`">
                  {{ enumOf(USER_STATUS, row.status).label }}
                </span>
              </td>
              <td class="num">{{ row.createTime || '-' }}</td>
              <td>
                <div class="table-actions">
                  <button class="btn-link" type="button" @click="openDetail(row)">详情</button>
                  <button class="btn-link" type="button" @click="openCredit(row)">信用分</button>
                  <button class="btn-link" :class="{ danger: row.status === 1 }" type="button" @click="toggleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminPager v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" />
    </div>

    <AdminModal v-model="detailOpen" title="用户详情" width="820px">
      <div v-if="detailLoading" class="loading">加载中…</div>
      <template v-else-if="detailUser">
        <div class="section-title">基本信息</div>
        <dl class="kv">
          <dt>用户 ID</dt>
          <dd>{{ detailUser.id }}</dd>
          <dt>用户名</dt>
          <dd>{{ detailUser.username || '-' }}</dd>
          <dt>昵称</dt>
          <dd>{{ detailUser.nickname || '-' }}</dd>
          <dt>学号</dt>
          <dd>{{ detailUser.studentId || '-' }}</dd>
          <dt>学院</dt>
          <dd>{{ detailUser.college || '-' }}</dd>
          <dt>手机号</dt>
          <dd>{{ detailUser.phone || '-' }}</dd>
          <dt>邮箱</dt>
          <dd>{{ detailUser.email || '-' }}</dd>
          <dt>余额</dt>
          <dd>¥{{ money(detailUser.balance) }}</dd>
          <dt>信用分</dt>
          <dd>{{ detailUser.creditScore ?? '-' }}</dd>
          <dt>状态</dt>
          <dd>
            <span class="tag" :class="`tag-${enumOf(USER_STATUS, detailUser.status).tag}`">
              {{ enumOf(USER_STATUS, detailUser.status).label }}
            </span>
          </dd>
          <dt>注册时间</dt>
          <dd>{{ detailUser.createTime || '-' }}</dd>
        </dl>

        <div class="section-title">交易统计</div>
        <div v-if="statsError" class="error-box">{{ statsError }}</div>
        <div v-else-if="detailStats" class="detail-grid">
          <div>
            <div class="hint" style="margin-bottom: 8px">作为买家</div>
            <dl class="kv">
              <dt>待支付</dt>
              <dd>{{ detailStats.buyWaitPay ?? 0 }} 单</dd>
              <dt>待收货</dt>
              <dd>{{ detailStats.buyWaitReceive ?? 0 }} 单</dd>
              <dt>已成交</dt>
              <dd>{{ detailStats.buyCompleted ?? 0 }} 单</dd>
              <dt>已取消</dt>
              <dd>{{ detailStats.buyCanceled ?? 0 }} 单</dd>
              <dt>累计买入</dt>
              <dd>¥{{ money(detailStats.buyTotalAmount) }}</dd>
            </dl>
          </div>
          <div>
            <div class="hint" style="margin-bottom: 8px">作为卖家</div>
            <dl class="kv">
              <dt>待发货</dt>
              <dd>{{ detailStats.sellWaitDeliver ?? 0 }} 单</dd>
              <dt>已成交</dt>
              <dd>{{ detailStats.sellCompleted ?? 0 }} 单</dd>
              <dt>已取消</dt>
              <dd>{{ detailStats.sellCanceled ?? 0 }} 单</dd>
              <dt>累计卖出</dt>
              <dd>¥{{ money(detailStats.sellTotalAmount) }}</dd>
            </dl>
          </div>
        </div>
      </template>

      <template #footer>
        <button class="btn" type="button" @click="detailOpen = false">关闭</button>
        <button
          v-if="detailUser"
          class="btn"
          :class="detailUser.status === 1 ? 'btn-danger' : 'btn-primary'"
          type="button"
          @click="toggleStatus(detailUser)"
        >
          {{ detailUser.status === 1 ? '禁用该用户' : '启用该用户' }}
        </button>
      </template>
    </AdminModal>

    <AdminModal v-model="creditOpen" title="修改信用分" width="420px">
      <div v-if="creditUser" class="notice">
        用户：{{ creditUser.nickname || creditUser.username }}（UID {{ creditUser.id }}）
      </div>
      <div class="field mt-16">
        <label>信用分（0-100）</label>
        <input v-model.number="creditValue" class="input" type="number" min="0" max="100" />
        <div class="field-hint">当前信用分：{{ creditUser ? creditUser.creditScore ?? '-' : '-' }}</div>
      </div>
      <div v-if="creditError" class="error-box mt-16">{{ creditError }}</div>

      <template #footer>
        <button class="btn" type="button" @click="creditOpen = false">取消</button>
        <button class="btn btn-primary" type="button" :disabled="creditSaving" @click="saveCredit">
          {{ creditSaving ? '保存中…' : '保存' }}
        </button>
      </template>
    </AdminModal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import AdminModal from '../components/AdminModal.vue'
import AdminPager from '../components/AdminPager.vue'
import { getUser, getUserTradeStats, pageUsers, updateUserCredit, updateUserStatus } from '../api/user'
import { USER_STATUS, enumOf } from '../utils/enums'
import { money } from '../utils/format'
import { notify } from '../utils/notify'

const filters = reactive({ keyword: '', studentId: '', phone: '', status: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

const detailOpen = ref(false)
const detailLoading = ref(false)
const detailUser = ref(null)
const detailStats = ref(null)
const statsError = ref('')

const creditOpen = ref(false)
const creditUser = ref(null)
const creditValue = ref(100)
const creditError = ref('')
const creditSaving = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageUsers({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: filters.keyword,
      studentId: filters.studentId,
      phone: filters.phone,
      status: filters.status
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

function search() {
  if (query.pageNum !== 1) {
    query.pageNum = 1
  } else {
    load()
  }
}

function reset() {
  filters.keyword = ''
  filters.studentId = ''
  filters.phone = ''
  filters.status = ''
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
  detailUser.value = null
  detailStats.value = null
  statsError.value = ''

  const [userResult, statsResult] = await Promise.allSettled([
    getUser(row.id),
    getUserTradeStats(row.id)
  ])

  if (userResult.status === 'fulfilled') {
    detailUser.value = userResult.value
  } else {
    notify.error(userResult.reason ? userResult.reason.message : '用户详情加载失败')
    detailOpen.value = false
  }

  if (statsResult.status === 'fulfilled') {
    detailStats.value = statsResult.value
  } else {
    statsError.value = statsResult.reason ? statsResult.reason.message : '交易统计加载失败'
  }

  detailLoading.value = false
}

function openCredit(row) {
  creditUser.value = row
  creditValue.value = row.creditScore ?? 100
  creditError.value = ''
  creditOpen.value = true
}

async function saveCredit() {
  if (!creditUser.value) return
  const value = Number(creditValue.value)
  if (!Number.isInteger(value) || value < 0 || value > 100) {
    creditError.value = '信用分需为 0-100 之间的整数'
    return
  }

  creditSaving.value = true
  creditError.value = ''
  try {
    await updateUserCredit(creditUser.value.id, value)
    notify.success('信用分已更新')
    creditOpen.value = false
    await load()
  } catch (err) {
    creditError.value = err.message || '保存失败'
  } finally {
    creditSaving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  if (!window.confirm(`确定${action}用户「${row.nickname || row.username}」吗？`)) return
  try {
    await updateUserStatus(row.id, next)
    notify.success(`用户已${action}`)
    detailOpen.value = false
    await load()
  } catch (err) {
    notify.error(err.message || `${action}失败`)
  }
}

onMounted(load)
</script>
