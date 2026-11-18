/** 金额展示：保留两位小数 */
export function money(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

/** 空值占位 */
export function dash(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

/** 截断长文本 */
export function ellipsis(value, max = 60) {
  if (value === null || value === undefined) return '-'
  const text = String(value)
  return text.length > max ? `${text.slice(0, max)}…` : text
}

/** 取名称首字符，用于头像占位 */
export function initialOf(...candidates) {
  const text = candidates.find((item) => item && String(item).trim()) || 'A'
  return String(text).trim().charAt(0).toUpperCase()
}
