<template>
  <div class="register-page">
    <div class="register-container">
      <div class="header">
        <span class="back-btn" @click="goBack">‹</span>
        <h1>注册账号</h1>
        <span class="placeholder"></span>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label>学号 <span class="required">*</span></label>
          <input 
            type="text" 
            v-model="form.studentId" 
            placeholder="请输入学号"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>用户名 <span class="required">*</span></label>
          <input 
            type="text" 
            v-model="form.username" 
            placeholder="2-20个字符"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>昵称</label>
          <input 
            type="text" 
            v-model="form.nickname" 
            placeholder="请输入昵称"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>密码 <span class="required">*</span></label>
          <input 
            type="password" 
            v-model="form.password" 
            placeholder="6-20位"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>确认密码 <span class="required">*</span></label>
          <input 
            type="password" 
            v-model="form.confirmPassword" 
            placeholder="再次输入密码"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>手机号 <span class="required">*</span></label>
          <input 
            type="tel" 
            v-model="form.phone" 
            placeholder="请输入手机号"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>邮箱</label>
          <input 
            type="email" 
            v-model="form.email" 
            placeholder="选填"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>学院 <span class="required">*</span></label>
          <select v-model="form.college" class="form-select">
            <option value="">请选择学院</option>
            <option value="计算机学院">计算机学院</option>
            <option value="软件学院">软件学院</option>
            <option value="电子信息学院">电子信息学院</option>
            <option value="经济管理学院">经济管理学院</option>
            <option value="外国语学院">外国语学院</option>
            <option value="文学院">文学院</option>
            <option value="理学院">理学院</option>
            <option value="工学院">工学院</option>
            <option value="艺术学院">艺术学院</option>
            <option value="其他">其他</option>
          </select>
        </div>

        <label class="agree-checkbox">
          <input type="checkbox" v-model="agree" />
          <span>我已阅读并同意</span>
          <span class="link">《用户协议》</span>
          <span>和</span>
          <span class="link">《隐私政策》</span>
        </label>

        <button type="submit" class="register-btn" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoading = ref(false)
const agree = ref(false)

const form = reactive({
  studentId: '',
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  college: ''
})

const handleRegister = async () => {
  if (!form.studentId) {
    alert('请输入学号')
    return
  }
  if (!form.username || form.username.length < 2 || form.username.length > 20) {
    alert('用户名需要2-20个字符')
    return
  }
  if (!form.password || form.password.length < 6 || form.password.length > 20) {
    alert('密码需要6-20位')
    return
  }
  if (form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    alert('请输入正确的手机号')
    return
  }
  if (!form.college) {
    alert('请选择学院')
    return
  }
  if (!agree.value) {
    alert('请同意用户协议和隐私政策')
    return
  }

  isLoading.value = true

  try {
    const response = await fetch('/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        studentId: form.studentId,
        username: form.username,
        nickname: form.nickname,
        password: form.password,
        phone: form.phone,
        email: form.email,
        college: form.college
      })
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
      alert('注册成功，请登录')
      router.push('/login')
    } else {
      alert(data.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    alert('注册失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #fafafa;
}

.register-container {
  background: white;
  padding-bottom: 30px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 15px;
  padding-top: calc(16px + env(safe-area-inset-top));
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 24px;
  color: #333;
}

.header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.placeholder {
  width: 24px;
}

.register-form {
  padding: 20px 15px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.required {
  color: #ff6b6b;
}

.form-input {
  width: 100%;
  padding: 14px;
  border: 1px solid #e8ecf0;
  border-radius: 10px;
  font-size: 14px;
  background: #fafafa;
}

.form-select {
  width: 100%;
  padding: 14px;
  border: 1px solid #e8ecf0;
  border-radius: 10px;
  font-size: 14px;
  background: #fafafa;
}

.agree-checkbox {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
  color: #666;
  margin-bottom: 20px;
}

.agree-checkbox input {
  margin-right: 6px;
}

.link {
  color: #2563eb;
}

.register-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
}

.register-btn:disabled {
  opacity: 0.7;
}
</style>