import { request } from './request'

/**
 * 管理员操作日志分页（仅超级管理员）
 * @param {{ pageNum?: number, pageSize?: number, action?: string, adminUsername?: string, startTime?: string, endTime?: string }} query
 */
export const pageOperationLogs = (query) => request('/admin/operation-log/page', { query })
