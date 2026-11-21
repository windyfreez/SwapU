<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">操作日志</h1>
        <p class="page-desc">管理员管理相关的写操作留痕：新增、修改、启停、删除、修改密码</p>
      </div>
    </div>

    <div class="panel">
      <div class="filters">
        <div class="field">
          <label>动作</label>
          <select v-model="filters.action" class="select">
            <option value="">全部动作</option>
            <option v-for="item in actionOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>操作人账号</label>
          <input
            v-model.trim="filters.adminUsername"
            class="input"
            type="text"
            placeholder="按账号模糊搜索"
            @keyup.enter="search"
          />
        </div>
        <div class="field">
          <label>开始时间</label>
          <input v-model="filters.startTime" class="input" type="datetime-local" />
        </div>
        <div class="field">
          <label>结束时间</label>
          <input v-model="filters.endTime" class="input" type="datetime-local" />
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
              <th>操作时间</th>
              <th>操作人</th>
              <th>动作</th>
              <th>对象</th>
              <th>变更摘要</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="6">
                <div class="empty"><div class="empty-icon">📝</div>暂无操作日志</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td class="num">{{ row.createTime }}</td>
              <td>{{ row.adminUsername || row.adminId }}</td>
              <td>
                <span class="tag" :class="`tag-${actionMeta(row).tag}`">
                  {{ actionMeta(row).label }}
                </span>
              </td>
              <td>{{ row.targetType || '-' }}{{ row.targetId != null ? `#${row.targetId}` : '' }}</td>
              <td>
                <span
                  class="ellipsis"
                  style="display: inline-block; max-width: 320px"
                  :title="row.detail || ''"
                >
                  {{ row.detail || '-' }}
                </span>
              </td>
              <td class="num">{{ row.ip || '-' }}</td>
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
import { pageOperationLogs } from '../api/operationLog'
import { ADMIN_LOG_ACTION, ADMIN_LOG_ACTION_OPTIONS } from '../utils/enums'

const actionOptions = ADMIN_LOG_ACTION_OPTIONS

const filters = reactive({ action: '', adminUsername: '', startTime: '', endTime: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

/**
 * 动作码 → 文案与标签颜色；后端也会返回 actionDesc，这里优先用本地映射以拿到颜色
 * @param {object} row 日志行
 */
function actionMeta(row) {
  return ADMIN_LOG_ACTION[row.action] || { label: row.actionDesc || row.action || '-', tag: 'muted' }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    // 空字符串会被 request 的 buildQuery 过滤掉，不会传给后端
    const data = await pageOperationLogs({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      action: filters.action,
      adminUsername: filters.adminUsername,
      startTime: filters.startTime,
      endTime: filters.endTime
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
  filters.action = ''
  filters.adminUsername = ''
  filters.startTime = ''
  filters.endTime = ''
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
