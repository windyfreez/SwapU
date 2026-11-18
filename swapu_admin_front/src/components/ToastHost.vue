<template>
  <Teleport to="body">
    <div class="toast-host">
      <TransitionGroup name="toast">
        <div
          v-for="item in toasts"
          :key="item.id"
          class="toast"
          :class="`toast-${item.type}`"
        >
          <span class="toast-dot"></span>
          <span>{{ item.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { toasts } from '../utils/notify'
</script>
