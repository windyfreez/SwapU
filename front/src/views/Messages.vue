<template>
  <div class="messages-page">
    <header class="messages-header">
      <h1>消息</h1>
      <span class="edit-btn">编辑</span>
    </header>

    <div class="message-list">
      <div
        v-for="session in sessions"
        :key="session.userId"
        class="message-item"
        @click="openChat(session)"
      >
        <div class="avatar">
          <img v-if="session.avatar" :src="session.avatar" alt="头像" />
          <span v-else>👤</span>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="nickname">{{ session.nickname || session.username }}</span>
            <span class="time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <p class="message-text">{{ session.lastMessage }}</p>
          <div v-if="session.productTitle" class="product-info">
            <img v-if="session.productImage" :src="getFirstImage(session.productImage)" class="product-thumb" />
            <span class="product-title">{{ session.productTitle }}</span>
          </div>
          <span v-if="session.unreadCount > 0" class="unread-badge">{{ session.unreadCount }}</span>
        </div>
      </div>
    </div>

    <div v-if="sessions.length === 0" class="empty-state">
      <span class="empty-icon">💬</span>
      <p>暂无消息</p>
      <p class="empty-hint">去逛逛，发现心仪的商品吧</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const sessions = ref([])

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const now = new Date()
  const date = new Date(dateStr.replace(/-/g, '/'))
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getFirstImage = (imageField) => {
  if (!imageField) return ''
  
  if (typeof imageField === 'string') {
    imageField = imageField.trim()
    if (imageField.startsWith('[') || imageField.startsWith('{')) {
      try {
        const parsed = JSON.parse(imageField)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return getFirstImage(parsed[0])
        }
        return parsed
      } catch {
        return imageField
      }
    }
    return imageField
  }
  
  if (Array.isArray(imageField)) {
    return imageField.length > 0 ? getFirstImage(imageField[0]) : ''
  }
  
  return String(imageField)
}

const mockSessions = [
  {
    userId: 1,
    nickname: '小美同学',
    avatar: '',
    productId: 101,
    productTitle: '二手自行车',
    productImage: '',
    lastMessage: '你好，这个商品还在吗？',
    lastMessageTime: new Date().toISOString().replace('T', ' ').substr(0, 19),
    unreadCount: 2
  },
  {
    userId: 2,
    nickname: '数码小王子',
    avatar: '',
    productId: 102,
    productTitle: 'iPad Pro 2023',
    productImage: '',
    lastMessage: '好的，明天下午可以面交',
    lastMessageTime: new Date(Date.now() - 7200000).toISOString().replace('T', ' ').substr(0, 19),
    unreadCount: 0
  },
  {
    userId: 3,
    nickname: '系统通知',
    avatar: '',
    lastMessage: '您的商品已通过审核',
    lastMessageTime: new Date(Date.now() - 86400000).toISOString().replace('T', ' ').substr(0, 19),
    unreadCount: 1
  },
  {
    userId: 4,
    nickname: '萌萌很忙',
    avatar: '',
    productId: 103,
    productTitle: '电动牙刷',
    productImage: '',
    lastMessage: '价格可以再便宜点吗？',
    lastMessageTime: new Date(Date.now() - 259200000).toISOString().replace('T', ' ').substr(0, 19),
    unreadCount: 0
  },
  {
    userId: 5,
    nickname: '客服小助手',
    avatar: '',
    lastMessage: '请问有什么可以帮您？',
    lastMessageTime: new Date(Date.now() - 604800000).toISOString().replace('T', ' ').substr(0, 19),
    unreadCount: 0
  }
]

const deduplicateSessions = (sessionList) => {
  const seen = new Set()
  return sessionList.filter(session => {
    if (seen.has(session.userId)) {
      return false
    }
    seen.add(session.userId)
    return true
  })
}

const fetchUserInfo = async (userId) => {
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

const fetchSessions = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/ws/chat/sessions', {
      headers: {
        'token': token || ''
      }
    })
    if (!response.ok) {
      console.warn('接口未就绪，使用mock数据')
      sessions.value = deduplicateSessions(mockSessions)
      return
    }
    const data = await response.json()
    if (data.code === 200 && data.data && data.data.list) {
      const list = data.data.list
      for (const session of list) {
        const userInfo = await fetchUserInfo(session.userId)
        session.nickname = userInfo.nickname
        session.avatar = userInfo.avatar
      }
      sessions.value = deduplicateSessions(list)
    } else {
      sessions.value = deduplicateSessions(mockSessions)
    }
  } catch (error) {
    console.warn('网络请求失败，使用mock数据:', error)
    sessions.value = deduplicateSessions(mockSessions)
  }
}

const openChat = (session) => {
  const nickname = session.nickname || (session.user && session.user.nickname) || session.username || '对方'
  const avatar = session.avatar || (session.user && session.user.avatar) || ''
  sessionStorage.setItem('chatTargetUser', JSON.stringify({
    userId: session.userId,
    nickname: nickname,
    avatar: avatar,
    productId: session.productId || null,
    productTitle: session.productTitle || '',
    productImage: session.productImage || ''
  }))
  router.push({
    path: '/messages/chat',
    query: { userId: session.userId }
  })
}

onMounted(() => {
  fetchSessions()
})

onActivated(() => {
  fetchSessions()
})
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  background: #fafafa;
}

.messages-header {
  background: white;
  padding: 16px 15px;
  padding-top: calc(16px + env(safe-area-inset-top));
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.messages-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.edit-btn {
  font-size: 14px;
  color: #2563eb;
}

.message-list {
  padding: 12px 15px;
}

.message-item {
  display: flex;
  background: white;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 10px;
  border: 1px solid #f0f0f0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  flex: 1;
  position: relative;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.nickname {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.time {
  font-size: 12px;
  color: #bbb;
}

.message-text {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
}

.product-info {
  display: flex;
  align-items: center;
  margin-top: 6px;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.product-thumb {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  margin-right: 6px;
  object-fit: cover;
}

.product-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  position: absolute;
  right: 0;
  top: 18px;
  background: #2563eb;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 14px !important;
  color: #999 !important;
}
</style>
