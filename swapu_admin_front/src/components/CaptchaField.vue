<template>
  <div class="captcha-field">
    <input
      :value="code"
      class="input captcha-input"
      type="text"
      maxlength="6"
      placeholder="请输入右侧验证码"
      autocomplete="off"
      :disabled="disabled"
      @input="$emit('update:code', $event.target.value)"
    />
    <button
      type="button"
      class="captcha-image"
      :disabled="loading || disabled"
      title="看不清？点击换一张"
      @click="refresh"
    >
      <img v-if="image" :src="image" alt="图形验证码" />
      <span v-else class="captcha-tip">{{ loading ? '加载中…' : '点击加载' }}</span>
    </button>
  </div>
  <p v-if="errorMsg" class="captcha-error">{{ errorMsg }}</p>
</template>

<script setup>
/**
 * 管理端图形验证码输入组件
 *
 * 用法：
 *   <CaptchaField v-model:code="form.captchaCode" v-model:captchaKey="form.captchaKey" ref="captchaRef" />
 *
 * 挂载时自动拉取一张；登录失败后父组件调用 captchaRef.value.refresh() 换一张。
 */
import { onMounted, ref } from 'vue'
import { getCaptcha } from '../api/captcha'

defineProps({
  // 管理员输入的字符
  code: { type: String, default: '' },
  // 本次验证码的标识，由组件内部写回父组件
  captchaKey: { type: String, default: '' },
  // 表单提交中时禁用
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:code', 'update:captchaKey'])

const image = ref('')
const loading = ref(false)
const errorMsg = ref('')

const refresh = async () => {
  if (loading.value) return
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await getCaptcha()
    if (data && data.captchaImage) {
      image.value = data.captchaImage
      emit('update:captchaKey', data.captchaKey)
      // 换了新图，之前输入的字符已经无效
      emit('update:code', '')
    } else {
      image.value = ''
      errorMsg.value = '验证码加载失败，点击图片重试'
    }
  } catch (e) {
    image.value = ''
    errorMsg.value = e.message || '验证码加载失败，点击图片重试'
  } finally {
    loading.value = false
  }
}

onMounted(refresh)

defineExpose({ refresh })
</script>

<style scoped>
.captcha-field {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-input {
  flex: 1;
  min-width: 0;
}

.captcha-image {
  flex: 0 0 130px;
  width: 130px;
  height: 40px;
  padding: 0;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.15s;
}

.captcha-image:hover:not(:disabled) {
  border-color: var(--c-primary);
}

.captcha-image:disabled {
  cursor: default;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-tip {
  font-size: 12px;
  color: var(--c-text-3);
}

.captcha-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--c-danger, #dc2626);
}
</style>
