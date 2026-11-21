import { createRouter, createWebHistory } from 'vue-router'
import { getAdminInfo, getToken } from '../api/request'
import { refreshAdminMe } from '../api/auth'
import { hasPermission, PERMISSION } from '../utils/permission'
import AdminLayout from '../components/AdminLayout.vue'
import Login from '../views/Login.vue'
// 工作台是唯一依赖 ECharts 的页面，改为路由级懒加载，
// 这样登录页与其它页面不必下载图表库（ECharts 约 190KB gzip）
const Dashboard = () => import('../views/Dashboard.vue')
import ProductAudit from '../views/ProductAudit.vue'
import CommentAudit from '../views/CommentAudit.vue'
import AuditRecords from '../views/AuditRecords.vue'
import CategoryManage from '../views/CategoryManage.vue'
import UserManage from '../views/UserManage.vue'
import AdminManage from '../views/AdminManage.vue'
import OperationLog from '../views/OperationLog.vue'
import AdminProfile from '../views/AdminProfile.vue'

/**
 * 路由的 meta.permission 与后端权限点一一对应：
 * 进入页面前先确认当前管理员拥有该权限，否则回落到工作台。
 * 注意：前端的守卫只做体验层拦截，真正的鉴权在服务端。
 */
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
      {
        path: 'audit/products',
        component: ProductAudit,
        meta: { title: '商品审核', permission: PERMISSION.AUDIT }
      },
      {
        path: 'audit/comments',
        component: CommentAudit,
        meta: { title: '评论审核', permission: PERMISSION.AUDIT }
      },
      {
        path: 'audit/records',
        component: AuditRecords,
        meta: { title: '审核流水', permission: PERMISSION.AUDIT }
      },
      {
        path: 'categories',
        component: CategoryManage,
        meta: { title: '分类管理', permission: PERMISSION.CATEGORY_MANAGE }
      },
      {
        path: 'users',
        component: UserManage,
        meta: { title: '用户管理', permission: PERMISSION.USER_MANAGE }
      },
      {
        path: 'admins',
        component: AdminManage,
        meta: { title: '管理员管理', permission: PERMISSION.ADMIN_MANAGE }
      },
      {
        path: 'operation-log',
        component: OperationLog,
        meta: { title: '操作日志', permission: PERMISSION.ADMIN_MANAGE }
      },
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

router.beforeEach(async (to) => {
  const token = getToken()

  // 未登录只能访问登录页
  if (!to.meta.guest && !token) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }

  // 已登录不再回到登录页
  if (to.meta.guest && token) {
    return '/admin/dashboard'
  }

  const required = to.meta.permission
  if (required) {
    let info = getAdminInfo()

    // 旧会话（登录时还没有写入 permissions）先向后端刷新一次
    if (!info || !Array.isArray(info.permissions)) {
      try {
        info = await refreshAdminMe()
      } catch (error) {
        // 取不到权限（会话失效/后端异常）时不让进入受限页面
        return '/admin/dashboard'
      }
    }

    if (!hasPermission(required)) {
      return '/admin/dashboard'
    }
  }

  document.title = `${to.meta.title || '管理后台'} - SwapU 管理后台`
  return true
})

export default router
