<template>
  <!-- 新消息提醒：右下角浮层。挂在 App 下全局生效，任何页面收到 WebSocket 推送都会弹 -->
  <div class="toast-stack">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="message-toast"
        @click="openChat(toast)"
      >
        <div class="toast-avatar">
          <img v-if="toast.avatar" :src="toast.avatar" alt="头像" />
          <span v-else>👤</span>
        </div>
        <div class="toast-body">
          <div class="toast-head">
            <span class="toast-name">{{ toast.nickname }}</span>
            <span class="toast-close" @click.stop="closeToast(toast.id)">×</span>
          </div>
          <p class="toast-title">您有一条新消息</p>
          <p class="toast-preview">{{ toast.content }}</p>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { connectChatSocket, onChatMessage } from '@/utils/chatSocket'

const route = useRoute()
const router = useRouter()

// 单条提醒停留时长与最多同时显示的条数
const TOAST_DURATION = 5000
const MAX_TOASTS = 3

const toasts = ref([])

// WebSocket 消息订阅的取消函数
let unsubscribeSocket = null
// 每条提醒的自动关闭定时器
const timers = new Map()

const getMyUserId = () => parseInt(localStorage.getItem('userId'))

// 当前页面是否已经在看这条消息，是的话就不打扰
const isViewingSender = (fromUserId) => {
  // 正开着对方的会话窗口
  if (route.path === '/messages/chat') {
    return String(route.query.userId) === String(fromUserId)
  }
  // 消息列表页自身会实时刷新会话与未读数，不需要弹窗
  return route.path === '/messages'
}

const closeToast = (id) => {
  const timer = timers.get(id)
  if (timer) {
    clearTimeout(timer)
    timers.delete(id)
  }
  toasts.value = toasts.value.filter((toast) => toast.id !== id)
}

const showToast = (message) => {
  const id = `${message.messageId}-${Date.now()}`
  toasts.value.push({
    id,
    fromUserId: message.fromUserId,
    nickname: message.fromUserNickname || '对方',
    avatar: message.fromUserAvatar || '',
    productId: message.productId || null,
    content: message.content || ''
  })

  // 超出上限时挤掉最早的一条
  while (toasts.value.length > MAX_TOASTS) {
    closeToast(toasts.value[0].id)
  }

  timers.set(id, setTimeout(() => closeToast(id), TOAST_DURATION))
}

// 收到服务端推送的新消息
const handleSocketMessage = (data) => {
  if (!data || !data.messageId) return
  // 自己发的消息（多标签页互发）不提醒
  if (data.fromUserId === getMyUserId()) return
  if (isViewingSender(data.fromUserId)) return

  showToast(data)
}

// 点击提醒直接进入与该用户的会话
const openChat = (toast) => {
  sessionStorage.setItem('chatTargetUser', JSON.stringify({
    userId: toast.fromUserId,
    nickname: toast.nickname,
    avatar: toast.avatar,
    productId: toast.productId,
    productTitle: '',
    productImage: ''
  }))
  closeToast(toast.id)
  router.push({
    path: '/messages/chat',
    query: { userId: toast.fromUserId }
  })
}

onMounted(() => {
  // 订阅的同时建立 WebSocket 连接，这样停留在首页等任何页面都能收到提醒
  unsubscribeSocket = onChatMessage(handleSocketMessage)
  connectChatSocket()
})

onUnmounted(() => {
  if (unsubscribeSocket) {
    unsubscribeSocket()
    unsubscribeSocket = null
  }
  timers.forEach((timer) => clearTimeout(timer))
  timers.clear()
})
</script>

<style scoped>
.toast-stack {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 320px;
  pointer-events: none;
}

/* 手机版：底部导航会挡住右下角，整体上移并铺满宽度 */
[data-layout='mobile'] .toast-stack {
  right: 12px;
  left: 12px;
  bottom: calc(72px + env(safe-area-inset-bottom));
  width: auto;
}

.message-toast {
  pointer-events: auto;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.message-toast:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(16, 24, 40, 0.16);
}

.toast-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  overflow: hidden;
}

.toast-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.toast-body {
  flex: 1;
  min-width: 0;
}

.toast-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.toast-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toast-close {
  font-size: 18px;
  line-height: 1;
  color: var(--c-text-3);
  flex-shrink: 0;
  padding: 0 2px;
}

.toast-close:hover {
  color: var(--c-text);
}

.toast-title {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--c-primary);
}

.toast-preview {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--c-text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 滑入滑出动画 */
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(24px);
}

.toast-leave-active {
  position: absolute;
}
</style>
