<template>
  <div class="pager">
    <span class="pager-info">
      共 {{ total }} 条 · 第 {{ page }} / {{ pages }} 页
    </span>

    <div class="pager-nav">
      <button class="pager-btn" type="button" :disabled="page <= 1" @click="go(page - 1)">
        上一页
      </button>

      <template v-for="(item, index) in pageItems" :key="`${item}-${index}`">
        <span v-if="item === '...'" class="pager-btn" style="border: none; cursor: default">…</span>
        <button
          v-else
          class="pager-btn"
          :class="{ active: item === page }"
          type="button"
          @click="go(item)"
        >
          {{ item }}
        </button>
      </template>

      <button class="pager-btn" type="button" :disabled="page >= pages" @click="go(page + 1)">
        下一页
      </button>

      <select class="pager-size" :value="size" @change="changeSize">
        <option v-for="option in sizes" :key="option" :value="option">{{ option }} 条/页</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  sizes: { type: Array, default: () => [10, 20, 50] }
})

const emit = defineEmits(['update:page', 'update:size'])

const pages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

const pageItems = computed(() => {
  const totalPages = pages.value
  const current = Math.min(Math.max(1, props.page), totalPages)
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }
  const items = [1]
  const start = Math.max(2, current - 2)
  const end = Math.min(totalPages - 1, current + 2)
  if (start > 2) items.push('...')
  for (let index = start; index <= end; index += 1) items.push(index)
  if (end < totalPages - 1) items.push('...')
  items.push(totalPages)
  return items
})

function go(target) {
  if (target < 1 || target > pages.value || target === props.page) return
  emit('update:page', target)
}

function changeSize(event) {
  emit('update:size', Number(event.target.value))
  emit('update:page', 1)
}
</script>
