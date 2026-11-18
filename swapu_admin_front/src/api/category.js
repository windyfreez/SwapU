import { request } from './request'

/** 分类列表（含停用，按 sort 升序） */
export const listCategories = () => request('/admin/category/list')

/** 新增分类 */
export const createCategory = (body) => request('/admin/category', { method: 'POST', body })

/** 修改分类名称或排序 */
export const updateCategory = (body) => request('/admin/category', { method: 'PUT', body })

/** 启用/禁用分类 */
export const updateCategoryStatus = (id, status) =>
  request(`/admin/category/${id}/status`, { method: 'PUT', body: { status } })

/** 删除分类 */
export const deleteCategory = (id) => request(`/admin/category/${id}`, { method: 'DELETE' })
