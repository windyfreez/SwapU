<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">管理员管理</h1>
        <p class="page-desc">仅超级管理员可新增、修改、启停和删除管理员账号</p>
      </div>
      <div v-if="canManage" class="page-actions">
        <button class="btn" type="button" :disabled="loading" @click="load">刷新</button>
        <button class="btn btn-primary" type="button" @click="openCreate">新增管理员</button>
      </div>
    </div>

    <div v-if="!canManage" class="notice">当前账号没有管理员管理权限，无法查看或管理管理员账号。</div>

    <template v-else>
      <div class="panel">
        <div class="filters">
          <div class="field grow">
            <label>账号</label>
            <input
              v-model.trim="filters.username"
              class="input"
              type="text"
              placeholder="按账号模糊搜索"
              @keyup.enter="search"
            />
          </div>
          <div class="field">
            <label>角色</label>
            <select v-model="filters.role" class="select">
              <option value="">全部角色</option>
              <option :value="1">超级管理员</option>
              <option :value="2">普通管理员</option>
              <option :value="3">运营管理员</option>
            </select>
          </div>
          <div class="field">
            <label>状态</label>
            <select v-model="filters.status" class="select">
              <option value="">全部状态</option>
              <option :value="1">启用</option>
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
                <th>ID</th>
                <th>账号</th>
                <th>姓名</th>
                <th>角色</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7"><div class="loading">加载中…</div></td>
              </tr>
              <tr v-else-if="rows.length === 0">
                <td colspan="7">
                  <div class="empty"><div class="empty-icon">🛡️</div>没有符合条件的管理员</div>
                </td>
              </tr>
              <tr v-for="row in rows" :key="row.id">
                <td class="num">{{ row.id }}</td>
                <td>
                  <div class="cell-title">{{ row.username }}</div>
                  <div v-if="row.id === currentId" class="cell-sub">当前登录账号</div>
                </td>
                <td>{{ row.name || '-' }}</td>
                <td>
                  <span class="tag" :class="`tag-${enumOf(ADMIN_ROLE, row.role).tag}`">
                    {{ enumOf(ADMIN_ROLE, row.role).label }}
                  </span>
                </td>
                <td>
                  <span class="tag" :class="`tag-${enumOf(ADMIN_STATUS, row.status).tag}`">
                    {{ enumOf(ADMIN_STATUS, row.status).label }}
                  </span>
                </td>
                <td class="num">{{ row.createTime || '-' }}</td>
                <td>
                  <div class="table-actions">
                    <button class="btn-link" type="button" @click="openEdit(row)">编辑</button>
                    <template v-if="row.id !== currentId">
                      <button
                        class="btn-link"
                        :class="{ danger: row.status === 1 }"
                        type="button"
                        @click="toggleStatus(row)"
                      >
                        {{ row.status === 1 ? '禁用' : '启用' }}
                      </button>
                      <button class="btn-link danger" type="button" @click="remove(row)">删除</button>
                    </template>
                    <span v-else class="hint">不可禁用/删除自己</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <AdminPager v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" />
      </div>
    </template>

    <AdminModal
      v-model="modalOpen"
      :title="editing ? '编辑管理员' : '新增管理员'"
      width="560px"
    >
      <div class="form-grid">
        <div class="field">
          <label>账号 <span style="color: var(--c-danger)">*</span></label>
          <input
            v-model.trim="form.username"
            class="input"
            type="text"
            maxlength="20"
            :disabled="Boolean(editing)"
            placeholder="2-20 个字符"
          />
          <div v-if="editing" class="field-hint">账号创建后不可修改</div>
        </div>

        <div class="field">
          <label>
            {{ editing ? '重置密码' : '密码' }}
            <span v-if="!editing" style="color: var(--c-danger)">*</span>
          </label>
          <input
            v-model="form.password"
            class="input"
            type="password"
            maxlength="20"
            autocomplete="new-password"
            :placeholder="editing ? '留空则不修改' : '6-20 位'"
          />
        </div>

        <div class="field">
          <label>姓名</label>
          <input v-model.trim="form.name" class="input" type="text" maxlength="50" placeholder="选填" />
        </div>

        <div class="field">
          <label>角色</label>
          <select v-model="form.role" class="select">
            <option :value="2">普通管理员</option>
            <option :value="3">运营管理员</option>
            <option :value="1">超级管理员</option>
          </select>
        </div>

        <div class="field span2">
          <label>头像 URL</label>
          <input v-model.trim="form.avatar" class="input" type="text" placeholder="选填，http(s) 地址" />
        </div>
      </div>

      <div v-if="formError" class="error-box mt-16">{{ formError }}</div>

      <template #footer>
        <button class="btn" type="button" @click="modalOpen = false">取消</button>
        <button class="btn btn-primary" type="button" :disabled="saving" @click="save">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </template>
    </AdminModal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AdminModal from '../components/AdminModal.vue'
import AdminPager from '../components/AdminPager.vue'
import {
  createAdmin,
  deleteAdmin,
  pageAdmins,
  updateAdmin,
  updateAdminStatus
} from '../api/admin'
import { getAdminById } from '../api/auth'
import { getAdminInfo, updateAdminInfo } from '../api/request'
import { ADMIN_ROLE, ADMIN_STATUS, enumOf } from '../utils/enums'
import { PERMISSION, hasPermission } from '../utils/permission'
import { notify } from '../utils/notify'

const info = getAdminInfo()
const currentId = info ? info.id : null
// 管理员管理=ADMIN_MANAGE，目前仅超级管理员拥有；用权限点判断而不是硬编码 role === 1
const canManage = computed(() => hasPermission(PERMISSION.ADMIN_MANAGE))

const filters = reactive({ username: '', role: '', status: '' })
const query = reactive({ pageNum: 1, pageSize: 10 })

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

const modalOpen = ref(false)
const editing = ref(null)
const form = reactive({ username: '', password: '', name: '', avatar: '', role: 2 })
const formError = ref('')
const saving = ref(false)

async function load() {
  if (!canManage.value) return
  loading.value = true
  error.value = ''
  try {
    const data = await pageAdmins({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      username: filters.username,
      role: filters.role,
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
  filters.username = ''
  filters.role = ''
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

function openCreate() {
  editing.value = null
  form.username = ''
  form.password = ''
  form.name = ''
  form.avatar = ''
  form.role = 2
  formError.value = ''
  modalOpen.value = true
}

function openEdit(row) {
  editing.value = row
  form.username = row.username || ''
  form.password = ''
  form.name = row.name || ''
  form.avatar = row.avatar || ''
  form.role = row.role || 2
  formError.value = ''
  modalOpen.value = true
}

function validate() {
  if (!editing.value) {
    if (form.username.length < 2 || form.username.length > 20) {
      return '账号长度需为 2-20 个字符'
    }
    if (form.password.length < 6 || form.password.length > 20) {
      return '密码长度需为 6-20 位'
    }
  } else if (form.password && (form.password.length < 6 || form.password.length > 20)) {
    return '密码长度需为 6-20 位'
  }
  return ''
}

async function save() {
  const message = validate()
  if (message) {
    formError.value = message
    return
  }

  saving.value = true
  formError.value = ''
  try {
    if (editing.value) {
      const payload = {
        id: editing.value.id,
        name: form.name,
        avatar: form.avatar,
        role: form.role
      }
      if (form.password) payload.password = form.password
      await updateAdmin(payload)
      notify.success('管理员已更新')
    } else {
      await createAdmin({
        username: form.username,
        password: form.password,
        name: form.name,
        avatar: form.avatar,
        role: form.role
      })
      notify.success('管理员已新增')
    }

    modalOpen.value = false
    await load()

    // 修改的是自己时同步本地缓存的账号信息（姓名/头像/角色）
    if (editing.value && editing.value.id === currentId) {
      try {
        const fresh = await getAdminById(currentId)
        if (fresh) {
          updateAdminInfo(fresh)
          window.location.reload()
        }
      } catch (err) {
        // 刷新资料失败不影响已保存的修改
      }
    }
  } catch (err) {
    formError.value = err.message || '保存失败'
    if (err.code === 4105) load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  if (!window.confirm(`确定${action}管理员「${row.username}」吗？`)) return
  try {
    await updateAdminStatus(row.id, next)
    notify.success(`管理员已${action}`)
    await load()
  } catch (err) {
    notify.error(err.message || `${action}失败`)
  }
}

async function remove(row) {
  if (!window.confirm(`确定删除管理员「${row.username}」吗？该操作不可恢复。`)) return
  try {
    await deleteAdmin(row.id)
    notify.success('管理员已删除')
    await load()
  } catch (err) {
    notify.error(err.message || '删除失败')
  }
}

onMounted(() => {
  if (canManage.value) load()
})
</script>
