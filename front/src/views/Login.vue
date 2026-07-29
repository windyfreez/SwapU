<template>
  <div class="login-page">
    <div class="login-container">
      <div class="logo-section">
        <div class="logo">🔄</div>
        <h1>校园二手</h1>
        <p>让闲置物品焕发新生</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="form.username" 
            placeholder="请输入用户名"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
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
          <span class="forgot-pwd">忘记密码？</span>
        </div>

        <button type="submit" class="login-btn" :disabled="isLoading">
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="register-link">
        <span>还没有账号？</span>
        <span class="link" @click="goToRegister">立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

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
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 360px;
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.logo-section h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.logo-section p {
  font-size: 13px;
  color: #999;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 14px;
  border: 1px solid #e8ecf0;
  border-radius: 10px;
  font-size: 14px;
  background: #fafafa;
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
  font-size: 13px;
  color: #666;
}

.checkbox input {
  margin-right: 6px;
}

.forgot-pwd {
  font-size: 13px;
  color: #2563eb;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
}

.login-btn:disabled {
  opacity: 0.7;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.link {
  color: #2563eb;
  margin-left: 4px;
}
</style>