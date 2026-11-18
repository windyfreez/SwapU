import { createRouter, createWebHistory } from 'vue-router'
import { getAdminInfo, getToken } from '../api/request'
import AdminLayout from '../components/AdminLayout.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import ProductAudit from '../views/ProductAudit.vue'
import CommentAudit from '../views/CommentAudit.vue'
import AuditRecords from '../views/AuditRecords.vue'
import CategoryManage from '../views/CategoryManage.vue'
import UserManage from '../views/UserManage.vue'
import AdminManage from '../views/AdminManage.vue'
import AdminProfile from '../views/AdminProfile.vue'

const routes = [
  { path: '/', redirect: '/admin/dashboard' },
  {
    path: '/admin/login',
    component: Login,
    meta: { title: '登录', guest: true }
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', component: Dashboard, meta: { title: '工作台' } },
      { path: 'audit/products', component: ProductAudit, meta: { title: '商品审核' } },
      { path: 'audit/comments', component: CommentAudit, meta: { title: '评论审核' } },
      { path: 'audit/records', component: AuditRecords, meta: { title: '审核流水' } },
      { path: 'categories', component: CategoryManage, meta: { title: '分类管理' } },
      { path: 'users', component: UserManage, meta: { title: '用户管理' } },
      { path: 'admins', component: AdminManage, meta: { title: '管理员管理', requiresSuper: true } },
      { path: 'profile', component: AdminProfile, meta: { title: '个人设置' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const token = getToken()

  // 未登录只能访问登录页
  if (!to.meta.guest && !token) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }

  // 已登录不再回到登录页
  if (to.meta.guest && token) {
    return '/admin/dashboard'
  }

  // 管理员管理仅超级管理员可见，直接输入地址也拦住
  if (to.meta.requiresSuper) {
    const info = getAdminInfo()
    if (!info || info.role !== 1) {
      return '/admin/dashboard'
    }
  }

  document.title = `${to.meta.title || '管理后台'} - SwapU 管理后台`
  return true
})

export default router
