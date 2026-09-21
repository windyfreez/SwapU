<template>
  <div class="chat-page">
    <div class="container">
      <div class="card chat-panel">
        <!-- 顶部栏 -->
        <div class="chat-header">
          <router-link to="/messages" class="back-link">← 返回消息</router-link>
          <div class="user-info" @click="showOtherUserInfo">
            <div class="avatar">
              <img v-if="targetUser.avatar" :src="targetUser.avatar" alt="头像" />
              <span v-else>👤</span>
            </div>
            <div class="user-detail">
              <span class="nickname">{{ targetUser.nickname }}</span>
              <span v-if="targetUser.productTitle" class="product-context">咨询: {{ targetUser.productTitle }}</span>
            </div>
          </div>
        </div>

        <!-- 消息滚动区 -->
        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="msg in messages"
            :key="msg.messageId"
            class="message-wrapper"
            :class="{ 'is-mine': isMyMessage(msg) }"
          >
            <div class="avatar-small" @click="isMyMessage(msg) ? showMyInfo() : showOtherUserInfo()">
              <img v-if="getAvatar(msg)" :src="getAvatar(msg)" alt="头像" />
              <span v-else>👤</span>
            </div>
            <div class="message-content">
              <span v-if="!isMyMessage(msg)" class="sender-name">{{ msg.fromUserNickname }}</span>
              <div class="message-bubble">
                {{ msg.content }}
              </div>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 输入栏 -->
        <div class="chat-input">
          <input
            type="text"
            v-model="inputMessage"
            placeholder="输入消息..."
            class="input-field"
            @keyup.enter="sendMessage"
          />
          <button class="btn btn-primary send-btn" @click="sendMessage">发送</button>
        </div>
      </div>
    </div>

    <!-- 用户信息弹窗 -->
    <div v-if="showUserInfoModal" class="modal-overlay" @click="closeModal">
      <div class="user-info-modal" @click.stop>
        <div class="modal-header">
          <span class="modal-title">用户信息</span>
          <span class="modal-close" @click="closeModal">×</span>
        </div>
        <div class="modal-content">
          <div class="user-avatar-large">
            <img v-if="otherUserInfo.avatar" :src="otherUserInfo.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="user-detail">
            <h2>{{ otherUserInfo.nickname }}</h2>
            <p>学号: {{ otherUserInfo.studentId }}</p>
            <p>学院: {{ otherUserInfo.college }}</p>
            <p>信用分: {{ otherUserInfo.creditScore }}</p>
            <p>发布商品: {{ otherUserInfo.publishCount }}件</p>
            <p>已售出: {{ otherUserInfo.soldCount }}件</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { connectChatSocket, onChatMessage } from '@/utils/chatSocket'

const router = useRouter()
const route = useRoute()
const messagesContainer = ref(null)

const targetUser = ref({
  userId: '',
  nickname: '',
  avatar: '',
  productId: null,
  productTitle: '',
  productImage: ''
})

const showUserInfoModal = ref(false)
const otherUserInfo = ref({})

const messages = ref([])
const inputMessage = ref('')
// WebSocket 消息订阅的取消函数，页面卸载时解除订阅
let unsubscribeSocket = null

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(/-/g, '/'))
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const isMyMessage = (msg) => {
  const userId = localStorage.getItem('userId')
  if (userId) {
    return msg.fromUserId === parseInt(userId) || msg.fromUserId === 999
  }
  return msg.fromUserId === 999
}

const getAvatar = (msg) => {
  if (isMyMessage(msg)) {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        return JSON.parse(userInfo).avatar
      } catch (e) {
        return null
      }
    }
    return null
  }
  return msg.fromUserAvatar
}

const mockMessages = [
  {
    messageId: 1,
    fromUserId: parseInt(route.query.userId) || 1,
    fromUserNickname: targetUser.value.nickname || '小美同学',
    fromUserAvatar: '',
    toUserId: 999,
    messageType: 1,
    content: '你好，这个商品还在吗？',
    isRead: true,
    createTime: new Date(Date.now() - 3600000).toISOString().replace('T', ' ').substr(0, 19)
  },
  {
    messageId: 2,
    fromUserId: 999,
    fromUserNickname: '我',
    fromUserAvatar: '',
    toUserId: parseInt(route.query.userId) || 1,
    messageType: 1,
    content: '在的，成色很新',
    isRead: true,
    createTime: new Date(Date.now() - 3500000).toISOString().replace('T', ' ').substr(0, 19)
  },
  {
    messageId: 3,
    fromUserId: parseInt(route.query.userId) || 1,
    fromUserNickname: targetUser.value.nickname || '小美同学',
    fromUserAvatar: '',
    toUserId: 999,
    messageType: 1,
    content: '价格可以便宜点吗？',
    isRead: true,
    createTime: new Date(Date.now() - 3400000).toISOString().replace('T', ' ').substr(0, 19)
  },
  {
    messageId: 4,
    fromUserId: 999,
    fromUserNickname: '我',
    fromUserAvatar: '',
    toUserId: parseInt(route.query.userId) || 1,
    messageType: 1,
    content: '最低900，已经很便宜了',
    isRead: true,
    createTime: new Date(Date.now() - 3300000).toISOString().replace('T', ' ').substr(0, 19)
  },
  {
    messageId: 5,
    fromUserId: parseInt(route.query.userId) || 1,
    fromUserNickname: targetUser.value.nickname || '小美同学',
    fromUserAvatar: '',
    toUserId: 999,
    messageType: 1,
    content: '好的，什么时候可以取货？',
    isRead: true,
    createTime: new Date(Date.now() - 3200000).toISOString().replace('T', ' ').substr(0, 19)
  }
]

const fetchHistory = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/ws/chat/history?toUserId=${targetUser.value.userId}`, {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      const text = await response.text()
      console.warn('接口返回错误状态:', response.status, response.statusText)
      console.warn('响应内容:', text)
      messages.value = mockMessages
      nextTick(() => {
        scrollToBottom()
      })
      return
    }
    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      const text = await response.text()
      console.warn('响应不是JSON格式:', contentType)
      console.warn('响应内容:', text)
      messages.value = mockMessages
      nextTick(() => {
        scrollToBottom()
      })
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data && data.data.list) {
      messages.value = data.data.list.reverse()
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      messages.value = mockMessages
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.warn('网络请求失败，使用mock数据:', error)
    messages.value = mockMessages
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/ws/chat/send', {
      method: 'POST',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        toUserId: parseInt(targetUser.value.userId),
        productId: targetUser.value.productId,
        messageType: 1,
        content: content
      })
    })
    if (!response.ok) {
      console.warn('发送接口未就绪，本地模拟发送')
      const newMessage = {
        messageId: Date.now(),
        fromUserId: 999,
        fromUserNickname: '我',
        fromUserAvatar: '',
        toUserId: parseInt(targetUser.value.userId),
        messageType: 1,
        content: content,
        isRead: true,
        createTime: new Date().toISOString().replace('T', ' ').substr(0, 19)
      }
      messages.value.push(newMessage)
      inputMessage.value = ''
      nextTick(() => {
        scrollToBottom()
      })
      return
    }
    const data = await response.json()
    if (data.code === 200) {
      inputMessage.value = ''
      fetchHistory()
    } else {
      alert(data.message || '发送失败')
    }
  } catch (error) {
    console.warn('发送失败，本地模拟发送:', error)
    const newMessage = {
      messageId: Date.now(),
      fromUserId: 999,
      fromUserNickname: '我',
      fromUserAvatar: '',
      toUserId: parseInt(targetUser.value.userId),
      messageType: 1,
      content: content,
      isRead: true,
      createTime: new Date().toISOString().replace('T', ' ').substr(0, 19)
    }
    messages.value.push(newMessage)
    inputMessage.value = ''
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const goBack = () => {
  router.back()
}

const showOtherUserInfo = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/user/${targetUser.value.userId}`, {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.warn('获取用户信息失败，使用本地数据')
      otherUserInfo.value = {
        nickname: targetUser.value.nickname,
        avatar: targetUser.value.avatar,
        studentId: '学号: ' + targetUser.value.userId,
        college: '计算机学院',
        creditScore: Math.floor(Math.random() * 30) + 80,
        publishCount: Math.floor(Math.random() * 10) + 1,
        soldCount: Math.floor(Math.random() * 5)
      }
      showUserInfoModal.value = true
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data) {
      otherUserInfo.value = data.data
    } else {
      otherUserInfo.value = {
        nickname: targetUser.value.nickname,
        avatar: targetUser.value.avatar,
        studentId: '学号: ' + targetUser.value.userId,
        college: '计算机学院',
        creditScore: Math.floor(Math.random() * 30) + 80,
        publishCount: Math.floor(Math.random() * 10) + 1,
        soldCount: Math.floor(Math.random() * 5)
      }
    }
    showUserInfoModal.value = true
  } catch (error) {
    console.warn('获取用户信息失败:', error)
    otherUserInfo.value = {
      nickname: targetUser.value.nickname,
      avatar: targetUser.value.avatar,
      studentId: '学号: ' + targetUser.value.userId,
      college: '计算机学院',
      creditScore: Math.floor(Math.random() * 30) + 80,
      publishCount: Math.floor(Math.random() * 10) + 1,
      soldCount: Math.floor(Math.random() * 5)
    }
    showUserInfoModal.value = true
  }
}

const showMyInfo = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/user/info', {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.warn('获取我的信息失败，使用本地数据')
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const info = JSON.parse(userInfo)
          otherUserInfo.value = {
            nickname: info.nickname || info.username,
            avatar: info.avatar,
            studentId: '学号: ' + info.studentId,
            college: '计算机学院',
            creditScore: info.creditScore || 100,
            publishCount: info.publishCount || 0,
            soldCount: info.soldCount || 0
          }
        } catch (e) {
          otherUserInfo.value = {
            nickname: '我',
            studentId: '未知',
            college: '未知',
            creditScore: 100,
            publishCount: 0,
            soldCount: 0
          }
        }
      } else {
        otherUserInfo.value = {
          nickname: '我',
          studentId: '未知',
          college: '未知',
          creditScore: 100,
          publishCount: 0,
          soldCount: 0
        }
      }
      showUserInfoModal.value = true
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data) {
      otherUserInfo.value = data.data
    } else {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const info = JSON.parse(userInfo)
          otherUserInfo.value = {
            nickname: info.nickname || info.username,
            avatar: info.avatar,
            studentId: '学号: ' + info.studentId,
            college: '计算机学院',
            creditScore: info.creditScore || 100,
            publishCount: info.publishCount || 0,
            soldCount: info.soldCount || 0
          }
        } catch (e) {
          otherUserInfo.value = {
            nickname: '我', 
            studentId: '未知',
            college: '未知',
            creditScore: 100,
            publishCount: 0,
            soldCount: 0
          }
        }
      } else {
        otherUserInfo.value = {
          nickname: '我',
          studentId: '未知',
          college: '未知',
          creditScore: 100,
          publishCount: 0,
          soldCount: 0
        }
      }
    }
    showUserInfoModal.value = true
  } catch (error) {
    console.warn('获取我的信息失败:', error)
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const info = JSON.parse(userInfo)
        otherUserInfo.value = {
          nickname: info.nickname || info.username,
          avatar: info.avatar,
          studentId: '学号: ' + info.studentId,
          college: '计算机学院',
          creditScore: info.creditScore || 100,
          publishCount: info.publishCount || 0,
          soldCount: info.soldCount || 0
        }
      } catch (e) {
        otherUserInfo.value = {
          nickname: '我',
          studentId: '未知',
          college: '未知',
          creditScore: 100,
          publishCount: 0,
          soldCount: 0
        }
      }
    } else {
      otherUserInfo.value = {
        nickname: '我',
        studentId: '未知',
        college: '未知',
        creditScore: 100,
        publishCount: 0,
        soldCount: 0
      }
    }
    showUserInfoModal.value = true
  }
}

const closeModal = () => {
  showUserInfoModal.value = false
}

const markAsRead = async (fromUserId) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/ws/chat/read', {
      method: 'POST',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        fromUserId: parseInt(fromUserId)
      })
    })
    if (!response.ok) {
      console.warn('标记已读接口未就绪')
      return
    }
    const data = await response.json()
    if (data.code === 200) {
      console.log('标记已读成功')
    }
  } catch (error) {
    console.warn('标记已读失败:', error)
  }
}

// 加载指定会话：清空当前消息、恢复对方资料与商品上下文，再拉取历史记录
const initChat = async (userId) => {
  messages.value = []
  targetUser.value.userId = userId

  // sessionStorage 里可能残留其他会话的上下文，必须校验 userId 一致后才使用
  let savedTarget = null
  const cachedTarget = sessionStorage.getItem('chatTargetUser')
  if (cachedTarget) {
    try {
      const parsed = JSON.parse(cachedTarget)
      if (String(parsed.userId) === String(userId)) {
        savedTarget = parsed
      }
    } catch (e) {
      savedTarget = null
    }
  }

  targetUser.value.productId = savedTarget ? (savedTarget.productId || null) : null
  targetUser.value.productTitle = savedTarget ? (savedTarget.productTitle || '') : ''
  targetUser.value.productImage = savedTarget ? (savedTarget.productImage || '') : ''

  const userInfo = await fetchUserInfoById(userId)
  targetUser.value.nickname = userInfo.nickname
  targetUser.value.avatar = userInfo.avatar

  markAsRead(userId)
  fetchHistory()
}

onMounted(async () => {
  const userId = route.query.userId
  if (!userId) {
    router.back()
    return
  }

  // 先注册订阅再建连，避免刚建连时推送的消息被漏掉
  unsubscribeSocket = onChatMessage(handleSocketMessage)
  connectChatSocket()

  await initChat(userId)
})

// 点击新消息弹窗切换到别的会话时路由参数会变，但组件被复用不会重新挂载，需要手动重载
watch(() => route.query.userId, (userId) => {
  if (userId) {
    initChat(userId)
  }
})

// 处理服务端通过 WebSocket 实时推送过来的消息（不再需要手动刷新页面）
const handleSocketMessage = (data) => {
  if (!data || !data.messageId) return

  const targetUserId = parseInt(targetUser.value.userId)
  // 只处理当前会话里对方发来的消息：自己发的由发送接口同步返回，其他会话交给会话列表页
  if (data.fromUserId !== targetUserId) return

  // 历史记录与实时推送可能包含同一条消息，按 messageId 去重
  if (messages.value.some((msg) => msg.messageId === data.messageId)) return

  messages.value.push({
    messageId: data.messageId,
    fromUserId: data.fromUserId,
    fromUserNickname: targetUser.value.nickname || '对方',
    fromUserAvatar: targetUser.value.avatar || '',
    toUserId: data.toUserId,
    productId: data.productId,
    messageType: data.messageType,
    content: data.content,
    isRead: false,
    createTime: data.createTime
  })
  nextTick(() => {
    scrollToBottom()
  })

  // 人正停留在当前会话页，收到的消息直接标记已读
  markAsRead(targetUserId)
}

onUnmounted(() => {
  // 只解除订阅，不断开连接：连接是全局单例，消息列表页等仍在复用
  if (unsubscribeSocket) {
    unsubscribeSocket()
    unsubscribeSocket = null
  }
})

const fetchUserInfoById = async (userId) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/user/${userId}`, {
      headers: {
        'token': token || ''
      }
    })
    if (response.ok) {
      const data = await response.json()
      if (data.code === 200 && data.data) {
        return {
          nickname: data.data.nickname || data.data.username || '对方',
          avatar: data.data.avatar || ''
        }
      }
    }
  } catch (error) {
    console.warn('获取用户信息失败:', error)
  }
  return { nickname: '对方', avatar: '' }
}
</script>

<style scoped>
.chat-page {
  padding: 20px 0 40px;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 260px);
  min-height: 480px;
  overflow: hidden;
}

/* 顶部栏 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border);
  flex-shrink: 0;
}

.back-link {
  font-size: 14px;
  color: var(--c-text-2);
  white-space: nowrap;
  flex-shrink: 0;
}

.back-link:hover {
  color: var(--c-primary);
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-right: 10px;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.user-detail {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.product-context {
  font-size: 12px;
  color: var(--c-text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 消息区 */
.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: var(--c-input-bg);
}

.message-wrapper {
  display: flex;
  margin-bottom: 16px;
}

.message-wrapper:not(.is-mine) {
  justify-content: flex-start;
}

.message-wrapper.is-mine {
  justify-content: flex-end;
}

.message-wrapper.is-mine .avatar-small {
  order: 2;
  margin-left: 10px;
  margin-right: 0;
}

.message-wrapper.is-mine .message-content {
  order: 1;
  align-items: flex-end;
}

.message-wrapper:not(.is-mine) .avatar-small {
  margin-right: 10px;
}

.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  overflow: hidden;
  cursor: pointer;
}

.avatar-small img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.sender-name {
  font-size: 12px;
  color: var(--c-text-3);
  margin-bottom: 4px;
}

.message-bubble {
  background: var(--c-card);
  padding: 10px 14px;
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text);
  word-break: break-all;
  border: 1px solid var(--c-border);
  border-top-left-radius: 4px;
}

.is-mine .message-bubble {
  background: var(--c-primary);
  color: white;
  border: none;
  border-top-right-radius: 4px;
  border-top-left-radius: var(--radius);
}

.message-time {
  font-size: 11px;
  color: var(--c-text-3);
  margin-top: 4px;
}

.is-mine .message-time {
  text-align: right;
}

/* 输入栏 */
.chat-input {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid var(--c-border);
  flex-shrink: 0;
  background: var(--c-card);
}

.input-field {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: var(--c-input-bg);
  color: var(--c-text);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-field:focus {
  outline: none;
  border-color: var(--c-primary);
  background: var(--c-card);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.send-btn {
  flex-shrink: 0;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.user-info-modal {
  background: var(--c-card);
  width: 380px;
  max-width: 90vw;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.modal-close {
  font-size: 24px;
  color: var(--c-text-3);
  line-height: 1;
  cursor: pointer;
}

.modal-content {
  padding: 24px 20px;
  text-align: center;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--c-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 16px;
  overflow: hidden;
}

.user-avatar-large img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.user-detail h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 12px;
}

.user-detail p {
  font-size: 14px;
  color: var(--c-text-2);
  margin-bottom: 8px;
}
</style>
