<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-mask" @mousedown.self="close">
      <div class="modal" :style="{ maxWidth: width }" role="dialog" aria-modal="true">
        <header class="modal-header">
          <h3 class="modal-title">{{ title }}</h3>
          <button class="modal-close" type="button" aria-label="关闭" @click="close">×</button>
        </header>
        <div class="modal-body">
          <slot />
        </div>
        <footer v-if="$slots.footer" class="modal-footer">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '560px' }
})

const emit = defineEmits(['update:modelValue'])

function close() {
  emit('update:modelValue', false)
}

function onKeydown(event) {
  if (event.key === 'Escape') close()
}

// 打开时锁定背景滚动并监听 Esc，关闭时恢复
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', onKeydown)
    } else {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKeydown)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeydown)
})
</script>
