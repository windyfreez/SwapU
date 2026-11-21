import { getAdminInfo } from '../api/request'

/**
 * 管理端权限点，与后端 com.itsean.swapu_admin.constant.AdminPermission 保持一致。
 * 前端只做「显示/隐藏」层面的控制，真正的拦截在后端。
 */
export const PERMISSION = {
  AUDIT: 'AUDIT',
  CATEGORY_MANAGE: 'CATEGORY_MANAGE',
  USER_MANAGE: 'USER_MANAGE',
  ADMIN_MANAGE: 'ADMIN_MANAGE'
}

/**
 * 读取当前登录管理员的权限点列表。
 * 数据来源是 /admin/me，登录成功与进入后台时各刷新一次。
 *
 * @returns {string[]} 权限点数组，未登录或旧会话时返回空数组
 */
export function currentPermissions() {
  const info = getAdminInfo()
  return info && Array.isArray(info.permissions) ? info.permissions : []
}

/**
 * 判断当前管理员是否拥有某个权限点
 *
 * @param {string} code 权限点
 * @returns {boolean}
 */
export function hasPermission(code) {
  return currentPermissions().includes(code)
}

/**
 * 判断当前管理员是否拥有其中任意一个权限点
 *
 * @param {string[]} codes 权限点列表
 * @returns {boolean}
 */
export function hasAnyPermission(codes) {
  return codes.some((code) => hasPermission(code))
}
