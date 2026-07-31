<template>
  <div class="chat-room">
    <header class="chat-header">
      <span class="back-btn" @click="goBack">‹</span>
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
      <span class="more-btn">⋯</span>
    </header>

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

    <div class="chat-input">
      <input
        type="text"
        v-model="inputMessage"
        placeholder="输入消息..."
        class="input-field"
        @keyup.enter="sendMessage"
      />
      <button class="send-btn" @click="sendMessage">发送</button>
    </div>

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
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'

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
let ws = null

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

onMounted(async () => {
  const userId = route.query.userId
  if (!userId) {
    router.back()
    return
  }

  targetUser.value.userId = userId

  const savedTarget = sessionStorage.getItem('chatTargetUser')
  if (savedTarget) {
    try {
      const user = JSON.parse(savedTarget)
      targetUser.value.productId = user.productId || null
      targetUser.value.productTitle = user.productTitle || ''
      targetUser.value.productImage = user.productImage || ''
      const userInfo = await fetchUserInfoById(userId)
      targetUser.value.nickname = userInfo.nickname
      targetUser.value.avatar = userInfo.avatar
    } catch (e) {
      const userInfo = await fetchUserInfoById(userId)
      targetUser.value.nickname = userInfo.nickname
      targetUser.value.avatar = userInfo.avatar
    }
  } else {
    const userInfo = await fetchUserInfoById(userId)
    targetUser.value.nickname = userInfo.nickname
    targetUser.value.avatar = userInfo.avatar
  }

  markAsRead(userId)
  fetchHistory()
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
.chat-room {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: white;
  padding: 16px 15px;
  padding-top: calc(16px + env(safe-area-inset-top));
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 24px;
  color: #333;
  margin-right: 12px;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-right: 10px;
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.nickname {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

.product-context {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.more-btn {
  font-size: 18px;
  color: #666;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.message-wrapper {
  display: flex;
  margin-bottom: 15px;
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
}

.message-wrapper.is-mine .message-content {
  order: 1;
  margin-right: 0;
}

.message-wrapper:not(.is-mine) .avatar-small {
  margin-right: 10px;
}

.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.avatar-small img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  max-width: 70%;
}

.sender-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  display: block;
}

.message-bubble {
  background: white;
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 14px;
  color: #333;
  word-break: break-all;
  border: 1px solid #e8ecf0;
}

.is-mine .message-bubble {
  background: #2563eb;
  color: white;
  border: none;
}

.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  display: block;
}

.is-mine .message-time {
  text-align: right;
}

.chat-input {
  background: white;
  padding: 12px 15px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  display: flex;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
}

.input-field {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e8ecf0;
  border-radius: 24px;
  font-size: 14px;
  background: #fafafa;
}

.send-btn {
  padding: 12px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.user-info-modal {
  background: white;
  width: 85%;
  max-width: 320px;
  border-radius: 16px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.modal-close {
  font-size: 24px;
  color: #999;
  line-height: 1;
}

.modal-content {
  padding: 20px;
  text-align: center;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 16px;
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
  color: #1a1a1a;
  margin-bottom: 12px;
}

.user-detail p {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
</style>
