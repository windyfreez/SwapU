const TOKEN_KEY = 'adminToken'
const INFO_KEY = 'adminInfo'

/**
 * 管理端接口统一错误。
 * code 为后端业务码；网络异常时为 -1；HTTP 401 时为 401。
 */
export class ApiError extends Error {
  constructor(code, message) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getAdminInfo() {
  try {
    return JSON.parse(localStorage.getItem(INFO_KEY) || 'null')
  } catch (error) {
    return null
  }
}

export function saveSession(data) {
  if (!data || !data.token) return
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(INFO_KEY, JSON.stringify(data.adminInfo || null))
}

export function updateAdminInfo(info) {
  try {
    localStorage.setItem(INFO_KEY, JSON.stringify(info || null))
  } catch (error) {
    // 忽略序列化异常，登录态仍可用
  }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(INFO_KEY)
}

function buildQuery(query) {
  if (!query) return ''
  const params = new URLSearchParams()
  Object.keys(query).forEach((key) => {
    const value = query[key]
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, value)
    }
  })
  const serialized = params.toString()
  return serialized ? `?${serialized}` : ''
}

/**
 * 发起管理端请求。
 * - 自动携带请求头 token
 * - HTTP 401：清除登录态并跳转登录页
 * - HTTP 200 但 code !== 200：抛业务错误，由页面提示（不登出）
 */
export async function request(path, { method = 'GET', query, body } = {}) {
  const headers = {}
  const token = getToken()
  if (token) {
    headers.token = token
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  let response
  try {
    response = await fetch(path + buildQuery(query), {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch (error) {
    throw new ApiError(-1, '网络异常，请确认管理端后端（8081）已启动')
  }

  if (response.status === 401) {
    clearSession()
    if (!window.location.pathname.startsWith('/admin/login')) {
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      window.location.replace(`/admin/login?redirect=${redirect}`)
    }
    throw new ApiError(401, '登录已过期，请重新登录')
  }

  let payload
  try {
    payload = await response.json()
  } catch (error) {
    const message =
      response.status >= 500
        ? `后端服务不可用（HTTP ${response.status}），请确认 8081 已启动`
        : `服务返回格式异常（HTTP ${response.status}）`
    throw new ApiError(response.status, message)
  }

  if (!response.ok) {
    throw new ApiError(payload.code || response.status, payload.msg || `请求失败（HTTP ${response.status}）`)
  }

  if (payload.code !== 200) {
    throw new ApiError(payload.code, payload.msg || '请求失败')
  }

  return payload.data
}
