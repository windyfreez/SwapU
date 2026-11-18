<template>
  <div class="admin-shell">
    <aside class="admin-sidebar" :class="{ open: sidebarOpen }">
      <div class="admin-brand">
        <div class="admin-brand-mark">S</div>
        <div>
          <div class="admin-brand-name">SwapU</div>
          <div class="admin-brand-sub">管理后台</div>
        </div>
      </div>

      <nav class="admin-nav">
        <template v-for="group in groups" :key="group.title">
          <div class="admin-nav-group">{{ group.title }}</div>
          <router-link
            v-for="item in group.items"
            :key="item.to"
            class="admin-nav-link"
            :to="item.to"
            @click="sidebarOpen = false"
          >
            {{ item.label }}
          </router-link>
        </template>
      </nav>

      <div class="admin-sidebar-foot">
        <div>SwapU 管理后台 v0.1</div>
        <div>接口服务：localhost:8081</div>
      </div>
    </aside>

    <div v-if="sidebarOpen" class="admin-overlay" @click="sidebarOpen = false"></div>

    <div class="admin-main">
      <header class="admin-topbar">
        <button
          class="admin-menu-btn"
          type="button"
          aria-label="打开菜单"
          @click="sidebarOpen = true"
        >
          ☰
        </button>
        <span class="admin-topbar-title">{{ route.meta.title || '管理后台' }}</span>
        <div class="admin-topbar-spacer"></div>

        <div class="admin-user">
          <div class="admin-user-avatar">
            <img v-if="info && info.avatar" :src="info.avatar" alt="头像" />
            <span v-else>{{ avatarChar }}</span>
          </div>
          <div class="admin-user-meta">
            <div class="admin-user-name">{{ displayName }}</div>
            <div class="admin-user-role">{{ roleLabel }}</div>
          </div>
          <button class="btn btn-sm btn-ghost" type="button" @click="logout">退出</button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, getAdminInfo } from '../api/request'
import { ADMIN_ROLE, enumOf } from '../utils/enums'
import { initialOf } from '../utils/format'

const route = useRoute()
const router = useRouter()

const sidebarOpen = ref(false)
const info = ref(getAdminInfo())

const navGroups = [
  {
    title: '概览',
    items: [{ to: '/admin/dashboard', label: '工作台' }]
  },
  {
    title: '内容审核',
    items: [
      { to: '/admin/audit/products', label: '商品审核' },
      { to: '/admin/audit/comments', label: '评论审核' },
      { to: '/admin/audit/records', label: '审核流水' }
    ]
  },
  {
    title: '运营管理',
    items: [
      { to: '/admin/categories', label: '分类管理' },
      { to: '/admin/users', label: '用户管理' },
      { to: '/admin/admins', label: '管理员管理', superOnly: true }
    ]
  },
  {
    title: '账号',
    items: [{ to: '/admin/profile', label: '个人设置' }]
  }
]

const groups = computed(() => {
  const isSuper = info.value && info.value.role === 1
  return navGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !item.superOnly || isSuper)
    }))
    .filter((group) => group.items.length > 0)
})

const displayName = computed(() => {
  if (!info.value) return '管理员'
  return info.value.name || info.value.username || '管理员'
})

const avatarChar = computed(() => initialOf(info.value && info.value.name, info.value && info.value.username))

const roleLabel = computed(() => enumOf(ADMIN_ROLE, info.value && info.value.role).label)

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
    info.value = getAdminInfo()
  }
)

function logout() {
  if (!window.confirm('确定退出登录吗？')) return
  clearSession()
  router.replace('/admin/login')
}
</script>
