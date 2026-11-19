<template>
  <AuthShell>
    <h2 class="auth-title">登录</h2>
    <p class="auth-subtitle">欢迎回来，继续逛市集。</p>

    <form @submit.prevent="handleLogin" class="auth-form">
      <div class="form-group">
        <label class="form-label">用户名</label>
        <input
          type="text"
          v-model="form.username"
          placeholder="请输入用户名"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label class="form-label">密码</label>
        <input
          type="password"
          v-model="form.password"
          placeholder="请输入密码"
          class="form-input"
        />
      </div>

      <div class="form-options">
        <label class="checkbox">
          <input type="checkbox" v-model="rememberMe" />
          <span>记住我</span>
        </label>
        <span class="forgot-pwd text-link">忘记密码？</span>
      </div>

      <button type="submit" class="btn btn-primary btn-lg btn-block" :disabled="isLoading">
        {{ isLoading ? '登录中...' : '登 录' }}
      </button>
    </form>

    <div class="auth-footer">
      <span>还没有账号？</span>
      <span class="text-link" @click="goToRegister">立即注册</span>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AuthShell from '../components/AuthShell.vue'

const router = useRouter()
const isLoading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username) {
    alert('请输入用户名')
    return
  }
  if (!form.password) {
    alert('请输入密码')
    return
  }

  isLoading.value = true

  try {
    const response = await fetch('/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form)
    })

    if (!response.ok) {
      const text = await response.text()
      console.error('HTTP error:', response.status, text)
      alert(`请求失败: ${response.status}`)
      return
    }

    const contentType = response.headers.get('content-type')
    if (!contentType || !contentType.includes('application/json')) {
      const text = await response.text()
      console.error('非JSON响应:', text)
      alert('服务器返回格式错误')
      return
    }

    const data = await response.json()

    if (data.code === 200) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.data))
      localStorage.setItem('userId', data.data.id.toString())
      alert('登录成功')
      router.push('/')
    } else {
      alert(data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    alert('登录失败，请检查网络连接或稍后重试')
  } finally {
    isLoading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.auth-title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--c-text);
}

.auth-subtitle {
  margin: 6px 0 24px;
  font-size: 14px;
  color: var(--c-text-2);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--c-text-2);
  cursor: pointer;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--c-text-2);
  margin-top: 20px;
}
</style>
