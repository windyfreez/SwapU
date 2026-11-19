<template>
  <div class="auth-page">
    <aside class="auth-brand-panel">
      <div class="auth-brand-inner">
        <p class="auth-kicker">校园二手交易平台</p>
        <h1>SwapU 云市集</h1>
        <p class="auth-tagline">同校闲置交易，见面验货更放心。</p>
        <ul class="auth-points">
          <li>校内认证，只在同学之间</li>
          <li>课本、数码、生活用品都能流转</li>
          <li>发布闲置，找到下一位使用者</li>
        </ul>
      </div>
    </aside>

    <main class="auth-form-panel">
      <div class="auth-form-inner" :class="{ 'is-wide': wide }">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
defineProps({
  wide: { type: Boolean, default: false }
})
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  background: var(--c-bg);
}

/* 左侧深蓝品牌区，与首页 banner 同色 */
.auth-brand-panel {
  display: flex;
  align-items: center;
  flex: 0 0 44%;
  max-width: 620px;
  padding: 64px 56px;
  background: var(--c-primary-dark);
  color: #fff;
}

.auth-brand-inner {
  width: 100%;
  max-width: 380px;
  animation: auth-panel 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) both;
}

.auth-kicker {
  font-size: 13px;
  color: #bfdbfe;
}

.auth-brand-inner h1 {
  margin: 12px 0 10px;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.2;
  color: #fff;
}

.auth-tagline {
  font-size: 16px;
  line-height: 1.8;
  color: #dbe7ff;
}

.auth-points {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 28px;
}

.auth-points li {
  position: relative;
  padding-left: 14px;
  font-size: 14px;
  line-height: 1.7;
  color: #dbe7ff;
}

.auth-points li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 5px;
  height: 5px;
  margin-top: -2.5px;
  border-radius: 50%;
  background: #93c5fd;
}

/* 右侧表单区 */
.auth-form-panel {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: var(--c-card);
}

.auth-form-inner {
  width: 100%;
  max-width: 400px;
  animation: auth-form 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) 0.1s both;
}

.auth-form-inner.is-wide {
  max-width: 560px;
}

@keyframes auth-panel {
  from {
    opacity: 0;
    transform: translateX(-16px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes auth-form {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 900px) {
  .auth-page {
    flex-direction: column;
  }

  .auth-brand-panel {
    flex: none;
    max-width: none;
    padding: 32px 24px;
  }

  .auth-brand-inner {
    max-width: none;
  }

  .auth-brand-inner h1 {
    font-size: 30px;
  }

  .auth-points {
    display: none;
  }

  .auth-form-panel {
    align-items: flex-start;
    padding: 32px 20px 48px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-brand-inner,
  .auth-form-inner {
    animation: none;
  }
}
</style>
