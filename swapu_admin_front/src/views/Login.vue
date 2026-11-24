<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">S</div>
        <h1>SwapU 管理后台</h1>
        <p>云端市集 · 运营管理中心</p>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <div v-if="error" class="error-box">{{ error }}</div>

        <div class="field">
          <label for="admin-username">管理员账号</label>
          <input
            id="admin-username"
            v-model.trim="form.username"
            class="input"
            type="text"
            placeholder="请输入管理员账号"
            autocomplete="username"
          />
        </div>

        <div class="field">
          <label for="admin-password">密码</label>
          <input
            id="admin-password"
            v-model="form.password"
            class="input"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>

        <div class="field">
          <label>验证码</label>
          <CaptchaField
            ref="captchaRef"
            v-model:code="form.captchaCode"
            v-model:captchaKey="form.captchaKey"
            :disabled="loading"
          />
        </div>

        <button class="btn btn-primary login-submit" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>

      <div v-if="isDev" class="login-hint">
        初始账号 root / 123456<br />
        首次部署后请及时修改密码
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CaptchaField from '../components/CaptchaField.vue'
import { login, refreshAdminMe } from '../api/auth'
import { saveSession } from '../api/request'
import { notify } from '../utils/notify'

const route = useRoute()
const router = useRouter()

const form = reactive({ username: '', password: '', captchaKey: '', captchaCode: '' })
const captchaRef = ref(null)
const error = ref('')
const loading = ref(false)
const isDev = import.meta.env.DEV

async function submit() {
  if (!form.username || !form.password) {
    error.value = '请输入管理员账号和密码'
    return
  }
  if (!form.captchaCode) {
    error.value = '请输入图形验证码'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const data = await login({
      username: form.username,
      password: form.password,
      captchaKey: form.captchaKey,
      captchaCode: form.captchaCode
    })
    saveSession(data)

    // 登录响应只带基础信息，权限点要从 /admin/me 取；失败不阻断登录，路由守卫会再补一次
    try {
      await refreshAdminMe()
    } catch (meError) {
      // 忽略：菜单会退化为仅有通用项，受限页面由守卫与后端兜底
    }

    notify.success('登录成功')

    const redirect = route.query.redirect
    if (
      typeof redirect === 'string' &&
      redirect.startsWith('/admin') &&
      !redirect.startsWith('/admin/login')
    ) {
      router.replace(redirect)
    } else {
      router.replace('/admin/dashboard')
    }
  } catch (err) {
    // 4101 账号或密码错误、4102 账号已被禁用，直接展示后端 msg
    error.value = err.message || '登录失败，请稍后重试'
    // 验证码是一次性的：无论失败原因是什么，都必须换一张再试
    if (captchaRef.value) {
      captchaRef.value.refresh()
    }
  } finally {
    loading.value = false
  }
}
</script>
