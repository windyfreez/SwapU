// 聊天 WebSocket 连接（全局单例）
// 说明：发送消息仍走 HTTP 接口 /ws/chat/send，WebSocket 只负责接收服务端的实时推送，
// 这样发送方拿到的是接口的同步返回，接收方的消息则由服务端主动推送，无需刷新页面。
// 连接会一直保持到页面卸载，ChatRoom 与 Messages 等页面共用同一条连接。

const RECONNECT_BASE_DELAY = 1000
const RECONNECT_MAX_DELAY = 30000

let socket = null
// 记录当前连接对应的用户，重新登录（身份变化）时用于重建连接
let connectedUserId = null
let reconnectTimer = null
let reconnectAttempts = 0
// 主动关闭标记：页面卸载或身份变化时不再自动重连
let closedByUser = false

const messageHandlers = new Set()

const getToken = () => localStorage.getItem('token') || ''

const getUserId = () => localStorage.getItem('userId') || ''

const clearReconnectTimer = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

const notifyMessage = (data) => {
  messageHandlers.forEach((handler) => {
    try {
      handler(data)
    } catch (e) {
      console.warn('聊天消息处理失败:', e)
    }
  })
}

// 指数退避重连：后端没启动时不至于高频重试刷爆控制台
const scheduleReconnect = () => {
  if (closedByUser || reconnectTimer || messageHandlers.size === 0) {
    return
  }
  reconnectAttempts += 1
  const delay = Math.min(RECONNECT_BASE_DELAY * 2 ** (reconnectAttempts - 1), RECONNECT_MAX_DELAY)
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    openSocket()
  }, delay)
}

const closeSocket = () => {
  closedByUser = true
  clearReconnectTimer()
  if (socket) {
    // 主动关闭不触发重连，先摘掉回调
    socket.onclose = null
    socket.onerror = null
    socket.onmessage = null
    socket.close()
    socket = null
  }
  connectedUserId = null
}

const openSocket = () => {
  const token = getToken()
  const userId = getUserId()
  // 未登录没有 userId/token，谈不上建立连接
  if (!token || !userId) {
    return
  }

  const isAlive = socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)
  if (isAlive && connectedUserId === userId) {
    return
  }
  // 身份变化或旧连接已断开时先清理，避免残留连接
  if (socket) {
    closeSocket()
  }

  closedByUser = false
  connectedUserId = userId

  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${protocol}://${window.location.host}/ws/chat/${userId}?token=${encodeURIComponent(token)}`

  try {
    socket = new WebSocket(url)
  } catch (e) {
    console.warn('聊天WebSocket建立失败:', e)
    socket = null
    scheduleReconnect()
    return
  }

  socket.onopen = () => {
    reconnectAttempts = 0
    console.log('聊天WebSocket已连接')
  }

  socket.onmessage = (event) => {
    let data
    try {
      data = JSON.parse(event.data)
    } catch (e) {
      console.warn('聊天WebSocket消息解析失败:', event.data)
      return
    }
    // 服务端建连成功时会先回一条“连接成功”，没有 messageId，不需要当成聊天消息
    if (data && !data.messageId) {
      return
    }
    notifyMessage(data)
  }

  socket.onclose = () => {
    socket = null
    if (!closedByUser) {
      scheduleReconnect()
    }
  }

  socket.onerror = () => {
    // onerror 之后一定会触发 onclose，重连统一交给 onclose 处理
  }
}

/**
 * 建立聊天 WebSocket 连接（已连接或正在连接时直接复用）
 */
export const connectChatSocket = () => {
  openSocket()
}

/**
 * 主动断开聊天 WebSocket 连接
 */
export const disconnectChatSocket = () => {
  closeSocket()
}

/**
 * 订阅服务端推送的聊天消息
 * @param {Function} handler 收到消息时回调，参数为服务端推送的数据
 * @returns {Function} 取消订阅函数
 */
export const onChatMessage = (handler) => {
  messageHandlers.add(handler)
  connectChatSocket()
  return () => {
    messageHandlers.delete(handler)
  }
}

/**
 * 当前 WebSocket 是否处于已连接状态
 * @returns {Boolean}
 */
export const isChatSocketOpen = () => !!socket && socket.readyState === WebSocket.OPEN
