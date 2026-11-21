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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, getAdminInfo } from '../api/request'
import { refreshAdminMe } from '../api/auth'
import { ADMIN_ROLE, enumOf } from '../utils/enums'
import { PERMISSION } from '../utils/permission'
import { initialOf } from '../utils/format'

const route = useRoute()
const router = useRouter()

const sidebarOpen = ref(false)
const info = ref(getAdminInfo())

/**
 * 菜单与路由守卫共用同一套权限点：item.permission 为空表示所有管理员可见。
 * 权限点与后端 AdminPermission 一一对应，前端只做显示层控制。
 */
const navGroups = [
  {
    title: '概览',
    items: [{ to: '/admin/dashboard', label: '工作台' }]
  },
  {
    title: '内容审核',
    items: [
      { to: '/admin/audit/products', label: '商品审核', permission: PERMISSION.AUDIT },
      { to: '/admin/audit/comments', label: '评论审核', permission: PERMISSION.AUDIT },
      { to: '/admin/audit/records', label: '审核流水', permission: PERMISSION.AUDIT }
    ]
  },
  {
    title: '运营管理',
    items: [
      { to: '/admin/categories', label: '分类管理', permission: PERMISSION.CATEGORY_MANAGE },
      { to: '/admin/users', label: '用户管理', permission: PERMISSION.USER_MANAGE }
    ]
  },
  {
    title: '系统管理',
    items: [
      { to: '/admin/admins', label: '管理员管理', permission: PERMISSION.ADMIN_MANAGE },
      { to: '/admin/operation-log', label: '操作日志', permission: PERMISSION.ADMIN_MANAGE }
    ]
  },
  {
    title: '账号',
    items: [{ to: '/admin/profile', label: '个人设置' }]
  }
]

const groups = computed(() => {
  // 依赖 info 而不是直接读 localStorage，权限刷新后菜单会自动重算
  const me = info.value
  const permissions = me && Array.isArray(me.permissions) ? me.permissions : []
  return navGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !item.permission || permissions.includes(item.permission))
    }))
    .filter((group) => group.items.length > 0)
})

const displayName = computed(() => {
  if (!info.value) return '管理员'
  return info.value.name || info.value.username || '管理员'
})

const avatarChar = computed(() => initialOf(info.value && info.value.name, info.value && info.value.username))

const roleLabel = computed(() => {
  // /admin/me 会返回后端维护的角色名称，优先使用；旧会话回落到枚举表
  if (info.value && info.value.roleLabel) {
    return info.value.roleLabel
  }
  return enumOf(ADMIN_ROLE, info.value && info.value.role).label
})

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
    info.value = getAdminInfo()
  }
)

/**
 * 进入后台时刷新一次当前管理员信息，角色/权限被调整后菜单能及时反映
 */
onMounted(async () => {
  try {
    const me = await refreshAdminMe()
    if (me) {
      info.value = me
    }
  } catch (error) {
    // 刷新失败沿用本地缓存；真正的鉴权始终在服务端
  }
})

function logout() {
  if (!window.confirm('确定退出登录吗？')) return
  clearSession()
  router.replace('/admin/login')
}
</script>
