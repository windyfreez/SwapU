import { request } from './request'

/**
 * 获取管理端登录图形验证码
 * 返回 { captchaKey, captchaImage, expireSeconds }
 */
export const getCaptcha = () => request('/admin/captcha/image')
