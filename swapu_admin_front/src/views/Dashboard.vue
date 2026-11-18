<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">管理端运营概览，仅展示后端当前已提供的数据</p>
      </div>
      <div class="page-actions">
        <button class="btn" type="button" :disabled="loading" @click="load">
          {{ loading ? '加载中…' : '刷新数据' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="error-box">{{ error }}</div>

    <div class="stat-grid">
      <div class="stat-card accent">
        <div class="stat-label">待审核商品</div>
        <div class="stat-value">{{ stats.pendingProducts }}</div>
        <div class="stat-foot">
          <router-link to="/admin/audit/products">去商品审核 →</router-link>
        </div>
      </div>

      <div class="stat-card accent">
        <div class="stat-label">待审核评论</div>
        <div class="stat-value">{{ stats.pendingComments }}</div>
        <div class="stat-foot">
          <router-link to="/admin/audit/comments">去评论审核 →</router-link>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-label">注册用户</div>
        <div class="stat-value">{{ stats.users }}</div>
        <div class="stat-foot">
          <router-link to="/admin/users">查看用户 →</router-link>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-label">商品分类</div>
        <div class="stat-value">{{ stats.categories }}</div>
        <div class="stat-foot">启用 {{ stats.enabledCategories }} 个</div>
      </div>
    </div>

    <div class="panel mt-16">
      <div class="panel-head">
        <h2 class="panel-title">快捷入口</h2>
      </div>
      <div class="panel-body">
        <div class="quick-grid">
          <router-link to="/admin/audit/products" class="quick-card">
            <div class="quick-title">商品审核</div>
            <div class="quick-desc">审核用户新发布的商品，通过后进入在售状态</div>
          </router-link>
          <router-link to="/admin/audit/comments" class="quick-card">
            <div class="quick-title">评论审核</div>
            <div class="quick-desc">处理用户评论，驳回时需填写原因</div>
          </router-link>
          <router-link to="/admin/categories" class="quick-card">
            <div class="quick-title">分类管理</div>
            <div class="quick-desc">维护商品分类的名称、排序与启停状态</div>
          </router-link>
        </div>
      </div>
    </div>

    <div class="notice mt-16">
      平台统计接口（成交额、今日订单、流量趋势、商品状态分布等）尚未在 swapu_admin 中实现，
      因此工作台暂不展示图表；需要这些指标时请先补后端接口。
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageProductAudits, pageCommentAudits } from '../api/audit'
import { pageUsers } from '../api/user'
import { listCategories } from '../api/category'

const loading = ref(false)
const error = ref('')

const stats = reactive({
  pendingProducts: '—',
  pendingComments: '—',
  users: '—',
  categories: '—',
  enabledCategories: '—'
})

async function load() {
  loading.value = true
  error.value = ''

  const [products, comments, users, categories] = await Promise.allSettled([
    pageProductAudits({ status: 0, pageNum: 1, pageSize: 1 }),
    pageCommentAudits({ status: 0, pageNum: 1, pageSize: 1 }),
    pageUsers({ pageNum: 1, pageSize: 1 }),
    listCategories()
  ])

  stats.pendingProducts = products.status === 'fulfilled' ? products.value.total : '—'
  stats.pendingComments = comments.status === 'fulfilled' ? comments.value.total : '—'
  stats.users = users.status === 'fulfilled' ? users.value.total : '—'

  if (categories.status === 'fulfilled') {
    const list = categories.value || []
    stats.categories = list.length
    stats.enabledCategories = list.filter((item) => item.status === 1).length
  } else {
    stats.categories = '—'
    stats.enabledCategories = '—'
  }

  const failed = [products, comments, users, categories]
    .filter((item) => item.status === 'rejected')
    .map((item) => item.reason && item.reason.message)
    .filter(Boolean)

  if (failed.length > 0) {
    error.value = `部分数据加载失败：${failed.join('；')}`
  }

  loading.value = false
}

onMounted(load)
</script>
