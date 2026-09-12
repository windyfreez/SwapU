// 电脑版 / 手机版显示模式
// 偏好存 localStorage('layout')，取值 auto(跟随屏宽) / desktop(电脑版) / mobile(手机版)
// 最终生效的布局写到 <html data-layout="desktop|mobile">，页面样式与外壳按它分支

export const LAYOUT_AUTO = 'auto'
export const LAYOUT_DESKTOP = 'desktop'
export const LAYOUT_MOBILE = 'mobile'

// 三种模式的展示文案
export const LAYOUT_LABELS = {
  [LAYOUT_AUTO]: '自动',
  [LAYOUT_DESKTOP]: '电脑版',
  [LAYOUT_MOBILE]: '手机版'
}

// 切换顺序：自动 -> 电脑版 -> 手机版 -> 自动
export const LAYOUT_ORDER = [LAYOUT_AUTO, LAYOUT_DESKTOP, LAYOUT_MOBILE]

const STORAGE_KEY = 'layout'
const MOBILE_MAX_WIDTH = 768

// 当前屏幕是否属于手机尺寸
const isNarrowScreen = () =>
  window.matchMedia(`(max-width: ${MOBILE_MAX_WIDTH}px)`).matches

// 读取偏好，非法值一律按“自动”处理
export const getLayoutPreference = () => {
  const saved = localStorage.getItem(STORAGE_KEY)
  return LAYOUT_ORDER.includes(saved) ? saved : LAYOUT_AUTO
}

// 把偏好解析成最终生效的布局
export const resolveLayout = (preference) => {
  if (preference === LAYOUT_MOBILE) return LAYOUT_MOBILE
  if (preference === LAYOUT_DESKTOP) return LAYOUT_DESKTOP
  return isNarrowScreen() ? LAYOUT_MOBILE : LAYOUT_DESKTOP
}

// 写入 <html> 属性，供 CSS 与根组件分支使用
export const applyLayout = (preference) => {
  const layout = resolveLayout(preference)
  document.documentElement.dataset.layout = layout
  document.documentElement.dataset.layoutPref = preference
  return layout
}

// 当前生效的布局
export const getCurrentLayout = () =>
  document.documentElement.dataset.layout === LAYOUT_MOBILE ? LAYOUT_MOBILE : LAYOUT_DESKTOP

// 切换偏好：持久化 + 立即生效 + 广播，让根组件与导航栏同步
export const setLayoutPreference = (preference) => {
  localStorage.setItem(STORAGE_KEY, preference)
  applyLayout(preference)
  window.dispatchEvent(new Event('layout-change'))
}

// 应用启动时调用：恢复偏好，并在“自动”模式下跟随窗口尺寸变化
export const initLayout = () => {
  applyLayout(getLayoutPreference())

  const media = window.matchMedia(`(max-width: ${MOBILE_MAX_WIDTH}px)`)
  const onChange = () => {
    // 只有“自动”模式才跟随屏宽，手动选定的模式不覆盖用户意愿
    if (getLayoutPreference() !== LAYOUT_AUTO) return
    applyLayout(LAYOUT_AUTO)
    window.dispatchEvent(new Event('layout-change'))
  }

  if (media.addEventListener) {
    media.addEventListener('change', onChange)
  } else {
    media.addListener(onChange)
  }
}
