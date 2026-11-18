<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">分类管理</h1>
        <p class="page-desc">维护商品分类的名称、排序与启停状态；列表包含已停用分类</p>
      </div>
      <div class="page-actions">
        <button class="btn" type="button" :disabled="loading" @click="load">刷新</button>
        <button class="btn btn-primary" type="button" @click="openCreate">新增分类</button>
      </div>
    </div>

    <div class="panel">
      <div v-if="error" class="error-box" style="margin: 14px 18px 0">{{ error }}</div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>分类名称</th>
              <th>排序</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7"><div class="loading">加载中…</div></td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="7">
                <div class="empty"><div class="empty-icon">🏷️</div>还没有分类，先新增一个吧</div>
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td class="num">{{ row.id }}</td>
              <td>
                <span class="cell-title">{{ row.name }}</span>
              </td>
              <td class="num">{{ row.sort ?? 0 }}</td>
              <td>
                <span class="tag" :class="`tag-${enumOf(CATEGORY_STATUS, row.status).tag}`">
                  {{ enumOf(CATEGORY_STATUS, row.status).label }}
                </span>
              </td>
              <td class="num">{{ row.createTime || '-' }}</td>
              <td class="num">{{ row.updateTime || '-' }}</td>
              <td>
                <div class="table-actions">
                  <button class="btn-link" type="button" @click="openEdit(row)">编辑</button>
                  <button class="btn-link" type="button" @click="toggleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button class="btn-link danger" type="button" @click="remove(row)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AdminModal v-model="modalOpen" :title="editing ? '编辑分类' : '新增分类'" width="460px">
      <div class="form-grid">
        <div class="field span2">
          <label>分类名称 <span style="color: var(--c-danger)">*</span></label>
          <input
            v-model="form.name"
            class="input"
            type="text"
            maxlength="50"
            placeholder="请输入分类名称（最多 50 字）"
          />
        </div>
        <div class="field span2">
          <label>排序值</label>
          <input v-model.number="form.sort" class="input" type="number" placeholder="越小越靠前，默认 0" />
          <div class="field-hint">数值越小越靠前，默认 0</div>
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
import { onMounted, reactive, ref } from 'vue'
import AdminModal from '../components/AdminModal.vue'
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  updateCategoryStatus
} from '../api/category'
import { CATEGORY_STATUS, enumOf } from '../utils/enums'
import { notify } from '../utils/notify'

const rows = ref([])
const loading = ref(false)
const error = ref('')

const modalOpen = ref(false)
const editing = ref(null)
const form = reactive({ name: '', sort: 0 })
const formError = ref('')
const saving = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = (await listCategories()) || []
  } catch (err) {
    error.value = err.message || '加载失败'
    rows.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.name = ''
  form.sort = 0
  formError.value = ''
  modalOpen.value = true
}

function openEdit(row) {
  editing.value = row
  form.name = row.name || ''
  form.sort = row.sort ?? 0
  formError.value = ''
  modalOpen.value = true
}

async function save() {
  const name = String(form.name || '').trim()
  if (!name) {
    formError.value = '请输入分类名称'
    return
  }
  if (name.length > 50) {
    formError.value = '分类名称不能超过 50 个字符'
    return
  }

  const sort = Number(form.sort)
  if (!Number.isInteger(sort)) {
    formError.value = '排序值必须是整数'
    return
  }

  saving.value = true
  formError.value = ''
  try {
    if (editing.value) {
      await updateCategory({ id: editing.value.id, name, sort })
      notify.success('分类已更新')
    } else {
      await createCategory({ name, sort })
      notify.success('分类已新增')
    }
    modalOpen.value = false
    await load()
  } catch (err) {
    formError.value = err.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  if (!window.confirm(`确定${action}分类「${row.name}」吗？`)) return
  try {
    await updateCategoryStatus(row.id, next)
    notify.success(`分类已${action}`)
    await load()
  } catch (err) {
    notify.error(err.message || `${action}失败`)
  }
}

async function remove(row) {
  if (!window.confirm(`确定删除分类「${row.name}」吗？该操作不可恢复。`)) return
  try {
    await deleteCategory(row.id)
    notify.success('分类已删除')
    await load()
  } catch (err) {
    notify.error(err.message || '删除失败')
  }
}

onMounted(load)
</script>
