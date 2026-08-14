<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <div class="auth-logo">🔄</div>
        <h1>注册账号</h1>
        <p>加入 SwapU 云市集,开启云端集市之旅</p>
      </div>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">学号 <span class="required">*</span></label>
            <input
              type="text"
              v-model="form.studentId"
              placeholder="请输入学号"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">用户名 <span class="required">*</span></label>
            <input
              type="text"
              v-model="form.username"
              placeholder="2-20个字符"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">昵称</label>
            <input
              type="text"
              v-model="form.nickname"
              placeholder="请输入昵称"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">手机号 <span class="required">*</span></label>
            <input
              type="tel"
              v-model="form.phone"
              placeholder="请输入手机号"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">密码 <span class="required">*</span></label>
            <input
              type="password"
              v-model="form.password"
              placeholder="6-20位"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">确认密码 <span class="required">*</span></label>
            <input
              type="password"
              v-model="form.confirmPassword"
              placeholder="再次输入密码"
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input
            type="email"
            v-model="form.email"
            placeholder="选填"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label class="form-label">学院 <span class="required">*</span></label>
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
          <span class="text-link">《用户协议》</span>
          <span>和</span>
          <span class="text-link">《隐私政策》</span>
        </label>

        <button type="submit" class="btn btn-primary btn-lg btn-block" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <span class="text-link" @click="goToLogin">去登录</span>
      </div>
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
    const response = await fetch('/user/register', {
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

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #f5f6f8 0%, #e8eefb 100%);
  padding: 24px 20px;
}

.auth-card {
  width: 100%;
  max-width: 560px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 44px 32px;
  box-shadow: 0 8px 40px rgba(16, 24, 40, 0.1);
}

.auth-brand {
  text-align: center;
  margin-bottom: 28px;
}

.auth-logo {
  font-size: 44px;
  margin-bottom: 10px;
}

.auth-brand h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 6px;
}

.auth-brand p {
  font-size: 13px;
  color: var(--c-text-3);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.required {
  color: var(--c-danger);
}

.agree-checkbox {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 13px;
  color: var(--c-text-2);
  margin-bottom: 20px;
  cursor: pointer;
}

.agree-checkbox input {
  margin-right: 4px;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--c-text-2);
  margin-top: 20px;
}

@media (max-width: 560px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .auth-card {
    padding: 28px 22px 24px;
  }
}
</style>
