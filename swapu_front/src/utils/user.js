import { reactive } from 'vue'

/**
 * 全局用户信息
 *
 * 背景：登录接口只返回 id/userName/token，没有头像等字段；
 * 而导航栏是常驻组件（挂在 App.vue 上），只在 mounted 时读一次 localStorage，
 * 于是"刚登录 → 还没进过个人页"这段时间头像一直是空的。
 *
 * 这里统一维护一份响应式数据：写入时同步 localStorage，
 * 导航栏、个人页、编辑资料页都读它，任何一处更新后全局立即生效。
 */
export const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  creditScore: null
})

/**
 * 覆盖式写入用户信息
 * 先清空已有字段，避免切换账号后残留上一个账号的头像、昵称
 * @param {object} info 用户信息
 */
export function setUserInfo(info) {
  const next = info && typeof info === 'object' ? info : {}
  for (const key of Object.keys(userInfo)) {
    delete userInfo[key]
  }
  Object.assign(userInfo, next)
  localStorage.setItem('userInfo', JSON.stringify(next))
}

/**
 * 从 localStorage 读取缓存，用于页面刷新后的首屏渲染
 * 只赋值给内存，不回写 localStorage，避免脏数据来回覆盖
 */
export function readCachedUserInfo() {
  const saved = localStorage.getItem('userInfo')
  if (!saved || saved === 'undefined' || saved === 'null') {
    return
  }
  try {
    const parsed = JSON.parse(saved)
    for (const key of Object.keys(userInfo)) {
      delete userInfo[key]
    }
    Object.assign(userInfo, parsed)
  } catch (e) {
    console.warn('本地用户信息解析失败，已忽略：', e)
  }
}

/**
 * 清空用户信息（退出登录时调用）
 */
export function clearUserInfo() {
  for (const key of Object.keys(userInfo)) {
    delete userInfo[key]
  }
  localStorage.removeItem('userInfo')
}

/**
 * 从后端拉取完整用户资料（含头像），成功后同步到全局与 localStorage
 * @returns {Promise<object|null>} 完整用户信息，未登录或失败时返回 null
 */
export async function refreshUserInfo() {
  const token = localStorage.getItem('token')
  if (!token) {
    return null
  }

  try {
    const response = await fetch('/user/info', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) {
      return null
    }

    const data = await response.json()
    if (data.code === 200 && data.data) {
      setUserInfo(data.data)
      return data.data
    }
  } catch (e) {
    // 拉取失败不阻断页面，继续用缓存里的信息渲染
    console.warn('刷新用户信息失败：', e)
  }
  return null
}
