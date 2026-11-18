<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">审核流水</h1>
        <p class="page-desc">商品、评论与头像审核的历史记录，按审核时间倒序</p>
      </div>
    </div>

    <div class="panel">
      <div class="filters">
        <div class="field">
          <label>审核类型</label>
          <select v-model="filters.type" class="select">
            <option value="">全部类型</option>
            <option v-for="item in typeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>审核结论</label>
          <select v-model="filters.result" class="select">
            <option value="">全部结论</option>
            <option v-for="item in resultOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>审核人 ID</label>
          <input
            v-model.trim="filters.reviewerId"
            class="input"
            type="text"
            placeholder="精确匹配管理员 ID"
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
              <th>流水 ID</th>
              <th>类型</th>
              <th>对象 ID</th>
              <th>结论</th>
              <th>驳回原因</th>
              <th>AI 结论</th>
              <th>审核人 ID</th>
              <th>审核时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="8">
                <div class="empty"><div class="empty-icon">🗂️</div>暂无审核记录</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td class="num">{{ row.id }}</td>
              <td>
                <span class="tag" :class="`tag-${enumOf(AUDIT_TYPE, row.type).tag}`">
                  {{ enumOf(AUDIT_TYPE, row.type).label }}
                </span>
              </td>
              <td class="num">{{ row.targetId }}</td>
              <td>
                <span class="tag" :class="`tag-${enumOf(AUDIT_RESULT, row.result).tag}`">
                  {{ enumOf(AUDIT_RESULT, row.result).label }}
                </span>
              </td>
              <td>
                <span class="ellipsis" style="display: inline-block; max-width: 240px" :title="row.reason || ''">
                  {{ row.reason || '-' }}
                </span>
              </td>
              <td>
                <span class="hint">{{ row.aiResult ? '已记录' : '无（AI 审核未接入）' }}</span>
              </td>
              <td class="num">{{ row.reviewerId ?? '-' }}</td>
              <td class="num">{{ row.reviewTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminPager v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import AdminPager from '../components/AdminPager.vue'
import { pageAuditRecords } from '../api/audit'
import { AUDIT_RESULT, AUDIT_TYPE, enumOf, enumOptions } from '../utils/enums'

const typeOptions = enumOptions(AUDIT_TYPE)
const resultOptions = enumOptions(AUDIT_RESULT)

const filters = reactive({ type: '', result: '', reviewerId: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageAuditRecords({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      type: filters.type,
      result: filters.result,
      reviewerId: filters.reviewerId
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
  filters.type = ''
  filters.result = ''
  filters.reviewerId = ''
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

onMounted(load)
</script>
