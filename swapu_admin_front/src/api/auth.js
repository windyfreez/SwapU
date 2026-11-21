import { request, updateAdminInfo } from './request'

/** 管理员登录 */
export const login = (payload) => request('/admin/login', { method: 'POST', body: payload })

/** 管理员详情（password 不会返回；id 不存在时 data 为 null） */
export const getAdminById = (id) => request(`/admin/${id}`)

/** 修改自己的密码 */
export const updateOwnPassword = (payload) => request('/admin/password', { method: 'PUT', body: payload })

/** 当前登录管理员信息与权限（前端菜单/路由守卫的唯一数据源） */
export const getAdminMe = () => request('/admin/me')

/**
 * 拉取并缓存当前管理员信息（含 permissions）。
 * 登录成功后与每次进入后台布局时各调一次，保证角色/权限变更能及时生效。
 *
 * @returns {Promise<object|null>} 当前管理员信息
 */
export async function refreshAdminMe() {
  const me = await getAdminMe()
  if (me) {
    updateAdminInfo(me)
  }
  return me
}
