import { request } from './request'

/** 用户分页（pageNum、pageSize 必传） */
export const pageUsers = (query) => request('/admin/user/page', { query })

/** 用户详情 */
export const getUser = (id) => request(`/admin/user/${id}`)

/** 启用/禁用用户 */
export const updateUserStatus = (id, status) =>
  request(`/admin/user/${id}/status`, { method: 'PUT', body: { status } })

/** 修改信用分（0-100） */
export const updateUserCredit = (id, creditScore) =>
  request(`/admin/user/${id}/credit-score`, { method: 'PUT', body: { creditScore } })

/** 用户交易统计 */
export const getUserTradeStats = (id) => request(`/admin/user/${id}/trade-statistics`)
