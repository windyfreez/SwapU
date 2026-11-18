<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">个人设置</h1>
        <p class="page-desc">查看当前管理员信息并修改登录密码</p>
      </div>
    </div>

    <div class="profile-grid">
      <div class="panel profile-card">
        <div class="profile-avatar">
          <img v-if="displayInfo && displayInfo.avatar" :src="displayInfo.avatar" alt="头像" />
          <span v-else>{{ avatarChar }}</span>
        </div>
        <div class="profile-name">{{ displayInfo ? displayInfo.name || displayInfo.username : '管理员' }}</div>
        <div class="profile-username">@{{ displayInfo ? displayInfo.username : '-' }}</div>
        <div class="mt-8">
          <span class="tag" :class="`tag-${enumOf(ADMIN_ROLE, displayInfo && displayInfo.role).tag}`">
            {{ enumOf(ADMIN_ROLE, displayInfo && displayInfo.role).label }}
          </span>
        </div>

        <div class="profile-meta">
          <dl class="kv">
            <dt>账号 ID</dt>
            <dd>{{ displayInfo ? displayInfo.id : '-' }}</dd>
            <dt>状态</dt>
            <dd>
              <span
                v-if="detail"
                class="tag"
                :class="`tag-${enumOf(ADMIN_STATUS, detail.status).tag}`"
              >
                {{ enumOf(ADMIN_STATUS, detail.status).label }}
              </span>
              <span v-else>-</span>
            </dd>
            <dt>创建时间</dt>
            <dd>{{ detail ? detail.createTime || '-' : '-' }}</dd>
            <dt>最近更新</dt>
            <dd>{{ detail ? detail.updateTime || '-' : '-' }}</dd>
          </dl>
        </div>

        <button class="btn btn-sm mt-16" type="button" :disabled="loadingProfile" @click="loadProfile">
          {{ loadingProfile ? '刷新中…' : '刷新资料' }}
        </button>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2 class="panel-title">修改密码</h2>
        </div>
        <div class="panel-body">
          <form style="max-width: 420px" @submit.prevent="submitPassword">
            <div class="field">
              <label for="old-password">原密码</label>
              <input
                id="old-password"
                v-model="passwordForm.oldPassword"
                class="input"
                type="password"
                autocomplete="current-password"
                placeholder="请输入原密码"
              />
            </div>

            <div class="field mt-16">
              <label for="new-password">新密码</label>
              <input
                id="new-password"
                v-model="passwordForm.newPassword"
                class="input"
                type="password"
                autocomplete="new-password"
                placeholder="6-20 位，且不能与原密码相同"
              />
            </div>

            <div class="field mt-16">
              <label for="confirm-password">确认新密码</label>
              <input
                id="confirm-password"
                v-model="passwordForm.confirmPassword"
                class="input"
                type="password"
                autocomplete="new-password"
                placeholder="请再次输入新密码"
              />
            </div>

            <div v-if="passwordError" class="error-box mt-16">{{ passwordError }}</div>

            <div class="form-actions">
              <button class="btn" type="button" :disabled="saving" @click="resetPasswordForm">
                清空
              </button>
              <button class="btn btn-primary" type="submit" :disabled="saving">
                {{ saving ? '提交中…' : '修改密码' }}
              </button>
            </div>
          </form>

          <div class="hint mt-16">
            修改成功后需要重新登录；已签发的旧令牌在有效期内仍可使用，建议尽快重新登录。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminById, updateOwnPassword } from '../api/auth'
import { clearSession, getAdminInfo, updateAdminInfo } from '../api/request'
import { ADMIN_ROLE, ADMIN_STATUS, enumOf } from '../utils/enums'
import { initialOf } from '../utils/format'
import { notify } from '../utils/notify'

const router = useRouter()

const info = ref(getAdminInfo())
const detail = ref(null)
const loadingProfile = ref(false)

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordError = ref('')
const saving = ref(false)

const displayInfo = computed(() => {
  if (!info.value) return null
  if (!detail.value) return info.value
  return { ...info.value, name: detail.value.name, avatar: detail.value.avatar }
})

const avatarChar = computed(() =>
  initialOf(
    detail.value && detail.value.name,
    info.value && info.value.name,
    info.value && info.value.username
  )
)

async function loadProfile() {
  if (!info.value || !info.value.id) return
  loadingProfile.value = true
  try {
    const data = await getAdminById(info.value.id)
    if (data) {
      detail.value = data
      updateAdminInfo({
        id: data.id,
        username: data.username,
        name: data.name,
        avatar: data.avatar,
        role: data.role
      })
      info.value = getAdminInfo()
    }
  } catch (err) {
    notify.error(err.message || '资料加载失败')
  } finally {
    loadingProfile.value = false
  }
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordError.value = ''
}

async function submitPassword() {
  const oldPassword = passwordForm.oldPassword
  const newPassword = passwordForm.newPassword
  const confirmPassword = passwordForm.confirmPassword

  if (!oldPassword || !newPassword) {
    passwordError.value = '请填写原密码和新密码'
    return
  }
  if (newPassword.length < 6 || newPassword.length > 20) {
    passwordError.value = '新密码长度需为 6-20 位'
    return
  }
  if (newPassword === oldPassword) {
    passwordError.value = '新密码不能与原密码相同'
    return
  }
  if (newPassword !== confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  saving.value = true
  passwordError.value = ''
  try {
    await updateOwnPassword({ oldPassword, newPassword })
    notify.success('密码已修改，请重新登录')
    clearSession()
    window.setTimeout(() => router.replace('/admin/login'), 600)
  } catch (err) {
    passwordError.value = err.message || '修改失败'
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>
