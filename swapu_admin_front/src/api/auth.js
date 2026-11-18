import { request } from './request'

/** 管理员登录 */
export const login = (payload) => request('/admin/login', { method: 'POST', body: payload })

/** 管理员详情（password 不会返回；id 不存在时 data 为 null） */
export const getAdminById = (id) => request(`/admin/${id}`)

/** 修改自己的密码 */
export const updateOwnPassword = (payload) => request('/admin/password', { method: 'PUT', body: payload })
