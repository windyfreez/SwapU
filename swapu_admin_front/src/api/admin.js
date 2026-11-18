import { request } from './request'

/** 管理员分页（pageNum、pageSize 必传） */
export const pageAdmins = (query) => request('/admin/page', { query })

/** 新增管理员（仅超级管理员） */
export const createAdmin = (body) => request('/admin', { method: 'POST', body })

/** 修改管理员或重置密码（仅超级管理员） */
export const updateAdmin = (body) => request('/admin', { method: 'PUT', body })

/** 启用/禁用管理员（仅超级管理员） */
export const updateAdminStatus = (id, status) =>
  request(`/admin/${id}/status`, { method: 'PUT', body: { status } })

/** 删除管理员（仅超级管理员） */
export const deleteAdmin = (id) => request(`/admin/${id}`, { method: 'DELETE' })
