import { request } from './request'

/** 商品审核列表（status 只支持 0/1/4，默认 0） */
export const pageProductAudits = (query) => request('/admin/audit/product/page', { query })

/** 商品审核详情 */
export const getProductAuditDetail = (id) => request(`/admin/audit/product/${id}`)

/** 商品审核通过/驳回 */
export const reviewProduct = (body) => request('/admin/audit/product/review', { method: 'POST', body })

/** 评论审核列表（status 只支持 0/1/2，默认 0） */
export const pageCommentAudits = (query) => request('/admin/audit/comment/page', { query })

/** 评论审核通过/驳回 */
export const reviewComment = (body) => request('/admin/audit/comment/review', { method: 'POST', body })

/** 审核流水分页 */
export const pageAuditRecords = (query) => request('/admin/audit/record/page', { query })
