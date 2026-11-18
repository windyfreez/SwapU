import { reactive } from 'vue'

let seed = 0

/** 全局轻提示队列，由 ToastHost 渲染 */
export const toasts = reactive([])

function push(type, message, duration = 3000) {
  const id = ++seed
  toasts.push({ id, type, message })
  window.setTimeout(() => {
    const index = toasts.findIndex((item) => item.id === id)
    if (index > -1) toasts.splice(index, 1)
  }, duration)
}

export const notify = {
  success(message) {
    push('success', message, 2600)
  },
  error(message) {
    push('error', message, 4200)
  }
}
