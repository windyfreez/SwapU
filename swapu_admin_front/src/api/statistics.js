import { request } from './request'

/** 平台总览（工作台卡片数据源） */
export const getStatisticsOverview = () => request('/admin/statistics/overview')

/**
 * 流量趋势（按天）
 * @param {{ startDate?: string, endDate?: string }} query yyyy-MM-dd，缺省最近 7 天
 */
export const getTrafficTrend = (query) => request('/admin/statistics/traffic-trend', { query })

/** 商品状态分布（饼图数据源） */
export const getProductStatusStats = () => request('/admin/statistics/product-status')

/**
 * 分类销售分布（成交额 Top N）
 * @param {number} limit 取前 N，缺省 10
 */
export const getCategorySales = (limit) =>
  request('/admin/statistics/category-sales', { query: { limit } })
