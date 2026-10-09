<template>
  <!-- AI 客服：全局悬浮入口，任何页面都能呼出，不打断用户当前浏览 -->
  <div v-if="visible" class="ai-widget">
    <!-- 聊天面板 -->
    <transition name="ai-panel">
      <div v-if="panelOpen" class="ai-panel">
        <div class="ai-header">
          <div class="ai-header-title">
            <span class="ai-dot"></span>
            <span>AI 客服</span>
          </div>
          <span class="ai-close" @click="closePanel">×</span>
        </div>

        <div ref="bodyRef" class="ai-body">
          <!-- 未登录：引导去登录 -->
          <div v-if="!token" class="ai-login-tip">
            <span class="ai-login-icon">🔒</span>
            <p>登录后即可咨询 AI 客服</p>
            <button class="btn btn-primary btn-sm" @click="goLogin">去登录</button>
          </div>

          <template v-else>
            <div class="ai-msg is-bot">
              <div class="ai-bubble">
                你好，我是 SwapU 智能客服 👋 商品、下单、支付、发货、退货退款都可以问我。
              </div>
            </div>

            <!-- 快捷问题：只在还没聊过时显示 -->
            <div v-if="messages.length === 0 && !loading" class="ai-quick">
              <span
                v-for="question in quickQuestions"
                :key="question"
                class="ai-chip"
                @click="ask(question)"
              >{{ question }}</span>
            </div>

            <div
              v-for="(message, index) in messages"
              :key="index"
              class="ai-msg"
              :class="message.role === 'user' ? 'is-me' : 'is-bot'"
            >
              <div class="ai-bubble">{{ message.content }}</div>
            </div>

            <div v-if="loading" class="ai-msg is-bot">
              <div class="ai-bubble ai-typing">
                <span></span><span></span><span></span>
              </div>
            </div>

            <div v-if="errorMsg" class="ai-error">{{ errorMsg }}</div>
          </template>
        </div>

        <div v-if="token" class="ai-input">
          <input
            v-model="input"
            class="form-input"
            type="text"
            placeholder="输入你的问题…"
            :disabled="loading"
            @keyup.enter="send"
          />
          <button
            class="btn btn-primary ai-send"
            :disabled="loading || !input.trim()"
            @click="send"
          >发送</button>
        </div>
      </div>
    </transition>

    <!-- 悬浮按钮 -->
    <button class="ai-fab" :class="{ 'is-open': panelOpen }" @click="togglePanel">
      <span v-if="!panelOpen">🤖</span>
      <span v-else>×</span>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 登录/注册页不显示客服入口
const HIDE_PATHS = ['/login', '/register']
const visible = computed(() => !HIDE_PATHS.includes(route.path))

// 后端最多接收 10 条历史，这里保持一致
const MAX_HISTORY = 10

const quickQuestions = [
  '怎么申请退货退款？',
  '订单多久能发货？',
  '怎么发布二手商品？',
  '怎么修改收货地址？'
]

const panelOpen = ref(false)
const token = ref('')
const input = ref('')
const loading = ref(false)
const errorMsg = ref('')
const messages = ref([])
const bodyRef = ref(null)

const scrollToBottom = () => {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}

const openPanel = () => {
  // 每次打开都重新读一次登录态，避免登录后组件不会重新挂载导致一直是旧值
  token.value = localStorage.getItem('token') || ''
  panelOpen.value = true
  errorMsg.value = ''
  scrollToBottom()
}

const closePanel = () => {
  panelOpen.value = false
}

const togglePanel = () => {
  if (panelOpen.value) {
    closePanel()
  } else {
    openPanel()
  }
}

const goLogin = () => {
  closePanel()
  router.push('/login')
}

// 对话结束后立刻把提问回填输入框，方便直接重试
const restorePrompt = (prompt) => {
  input.value = prompt
}

const ask = (question) => {
  if (loading.value) return
  input.value = question
  send()
}

const send = async () => {
  const prompt = input.value.trim()
  if (!prompt || loading.value) return

  messages.value.push({ role: 'user', content: prompt })
  input.value = ''
  errorMsg.value = ''
  loading.value = true
  scrollToBottom()

  // 历史只带本次提问之前的最近若干条
  const history = messages.value
    .slice(0, -1)
    .slice(-MAX_HISTORY)
    .map((message) => ({ role: message.role, content: message.content }))

  let failReason = 'AI 客服暂时联系不上，请稍后再试'
  try {
    const response = await fetch('/user/ai/chat', {
      method: 'POST',
      headers: {
        'token': localStorage.getItem('token') || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ prompt, history })
    })

    // 未登录时拦截器返回 401，业务异常返回 200 + code=401，两种都要解析 body
    let data = null
    try {
      data = await response.json()
    } catch (e) {
      data = null
    }

    if (response.ok && data && data.code === 200 && data.data && data.data.content) {
      messages.value.push({ role: 'assistant', content: data.data.content })
      return
    }

    if (response.status === 401) {
      token.value = ''
      failReason = '登录状态已过期，请重新登录后再咨询'
    } else if (data && data.msg) {
      failReason = data.msg
    }
  } catch (e) {
    console.warn('AI 客服请求失败:', e)
  } finally {
    loading.value = false
  }

  // 失败时把这条提问撤回，让用户可以直接改一改重发
  messages.value.pop()
  restorePrompt(prompt)
  errorMsg.value = failReason
}
</script>

<style scoped>
/* 悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1710;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: var(--c-primary);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: transform 0.15s, background 0.15s;
}

.ai-fab:hover {
  background: var(--c-primary-hover);
  transform: translateY(-2px);
}

.ai-fab.is-open {
  font-size: 26px;
  background: var(--c-text-2);
}

/* 聊天面板 */
.ai-panel {
  position: fixed;
  right: 24px;
  bottom: 92px;
  z-index: 1700;
  display: flex;
  flex-direction: column;
  width: 380px;
  height: min(560px, calc(100vh - 140px));
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--c-border);
  flex-shrink: 0;
}

.ai-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.ai-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.ai-close {
  font-size: 20px;
  line-height: 1;
  color: var(--c-text-3);
  cursor: pointer;
  padding: 0 4px;
}

.ai-close:hover {
  color: var(--c-text);
}

.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-msg {
  display: flex;
}

.ai-msg.is-me {
  justify-content: flex-end;
}

.ai-bubble {
  max-width: 82%;
  padding: 9px 13px;
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--c-hover);
  color: var(--c-text);
}

.is-me .ai-bubble {
  background: var(--c-primary);
  color: #fff;
}

/* 打字中动画 */
.ai-typing {
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 20px;
}

.ai-typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-text-3);
  animation: ai-blink 1.2s infinite ease-in-out;
}

.ai-typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes ai-blink {
  0%, 80%, 100% { opacity: 0.3; }
  40% { opacity: 1; }
}

/* 快捷问题 */
.ai-quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.ai-chip {
  padding: 6px 12px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  font-size: 13px;
  color: var(--c-text-2);
  background: var(--c-input-bg);
  cursor: pointer;
  transition: all 0.15s;
}

.ai-chip:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: var(--c-primary-light);
}

.ai-error {
  align-self: center;
  padding: 8px 12px;
  border-radius: var(--radius);
  background: rgba(220, 38, 38, 0.08);
  color: var(--c-danger);
  font-size: 13px;
  text-align: center;
}

/* 未登录提示 */
.ai-login-tip {
  margin: auto;
  text-align: center;
  color: var(--c-text-3);
}

.ai-login-icon {
  font-size: 32px;
}

.ai-login-tip p {
  margin: 10px 0 16px;
  font-size: 14px;
}

/* 输入区 */
.ai-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--c-border);
  flex-shrink: 0;
}

.ai-input .form-input {
  flex: 1;
  min-width: 0;
}

.ai-send {
  flex-shrink: 0;
}

/* 面板出现动画 */
.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

/* 手机版：避开底部导航，面板铺满可用宽度 */
[data-layout='mobile'] .ai-fab {
  right: 12px;
  bottom: calc(72px + env(safe-area-inset-bottom));
}

[data-layout='mobile'] .ai-panel {
  left: 12px;
  right: 12px;
  bottom: calc(72px + env(safe-area-inset-bottom));
  width: auto;
  height: min(70vh, 560px);
}
</style>
