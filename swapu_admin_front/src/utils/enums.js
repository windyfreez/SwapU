/** 商品状态：与 product.status 一致 */
export const PRODUCT_STATUS = {
  0: { label: '待审核', tag: 'warning' },
  1: { label: '在售', tag: 'success' },
  2: { label: '已售出', tag: 'info' },
  3: { label: '已下架', tag: 'muted' },
  4: { label: '审核驳回', tag: 'danger' }
}

/** 评论状态：与 comment.status 一致 */
export const COMMENT_STATUS = {
  0: { label: '待审核', tag: 'warning' },
  1: { label: '已发布', tag: 'success' },
  2: { label: '审核驳回', tag: 'danger' }
}

/** 审核类型 */
export const AUDIT_TYPE = {
  1: { label: '商品', tag: 'info' },
  2: { label: '评论', tag: 'info' },
  3: { label: '头像', tag: 'muted' }
}

/** 审核结论 */
export const AUDIT_RESULT = {
  1: { label: '通过', tag: 'success' },
  2: { label: '驳回', tag: 'danger' }
}

/** 管理员角色 */
export const ADMIN_ROLE = {
  1: { label: '超级管理员', tag: 'info' },
  2: { label: '普通管理员', tag: 'muted' }
}

/** 管理员状态 */
export const ADMIN_STATUS = {
  1: { label: '启用', tag: 'success' },
  0: { label: '禁用', tag: 'muted' }
}

/** 用户状态 */
export const USER_STATUS = {
  1: { label: '正常', tag: 'success' },
  0: { label: '禁用', tag: 'muted' }
}

/** 分类状态 */
export const CATEGORY_STATUS = {
  1: { label: '启用', tag: 'success' },
  0: { label: '禁用', tag: 'muted' }
}

/** 商品成色：管理端只做展示 */
export const CONDITION_DESC = {
  1: '全新',
  2: '几乎全新',
  3: '轻微使用痕迹',
  4: '明显使用痕迹',
  5: '有损坏'
}

/** 商品审核状态页签（后端只允许 0/1/4） */
export const PRODUCT_AUDIT_TABS = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 4, label: '已驳回' }
]

/** 评论审核状态页签 */
export const COMMENT_AUDIT_TABS = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已驳回' }
]

/**
 * 取枚举展示项，未知值返回灰色占位。
 * @param {Record<string, {label: string, tag: string}>} map
 * @param {number|string|null|undefined} value
 */
export function enumOf(map, value) {
  const item = map[value]
  if (item) return item
  return {
    label: value === null || value === undefined || value === '' ? '-' : String(value),
    tag: 'muted'
  }
}

/**
 * 枚举转下拉选项。
 * @param {Record<string, {label: string, tag: string}>} map
 */
export function enumOptions(map) {
  return Object.keys(map)
    .map(Number)
    .sort((a, b) => a - b)
    .map((value) => ({ value, label: map[value].label }))
}

/** 成色文案 */
export function conditionText(value) {
  return CONDITION_DESC[value] || '未知成色'
}
