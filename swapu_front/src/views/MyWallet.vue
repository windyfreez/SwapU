<template>
  <AccountLayout active="wallet">
    <div class="balance-card card">
      <div class="balance-label">账户余额（元）</div>
      <div class="balance-value">¥{{ userInfo.balance || '0.00' }}</div>
      <div class="balance-actions">
        <button class="recharge-btn" @click="showRechargeModal = true">充 值</button>
      </div>
    </div>

    <div class="menu-section card">
      <div class="menu-item">
        <span class="menu-icon">📊</span>
        <span class="menu-text">收支明细</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">🔄</span>
        <span class="menu-text">提现</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div v-if="showRechargeModal" class="modal-overlay" @click="showRechargeModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">充值</span>
          <button class="close-btn" @click="showRechargeModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="amount-options">
            <button 
              v-for="amount in rechargeOptions" 
              :key="amount"
              class="amount-option"
              :class="{ active: rechargeAmount === amount }"
              @click="rechargeAmount = amount"
            >
              ¥{{ amount }}
            </button>
          </div>
          <div class="custom-amount">
            <input 
              v-model="customAmount" 
              type="number" 
              class="amount-input" 
              placeholder="输入自定义金额"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-submit" @click="handleRecharge">确认充值</button>
        </div>
      </div>
    </div>
  </AccountLayout>
</template>

<script setup>
import AccountLayout from '../components/AccountLayout.vue'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showRechargeModal = ref(false)
const rechargeAmount = ref(100)
const customAmount = ref('')

const rechargeOptions = [10, 50, 100, 200, 500]

const userInfo = reactive({
  id: '',
  balance: '0.00'
})

const goBack = () => {
  router.back()
}

const loadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/user/info', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200) {
      userInfo.id = data.data.id
      userInfo.balance = data.data.balance || '0.00'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleRecharge = async () => {
  const amount = customAmount.value ? parseFloat(customAmount.value) : rechargeAmount.value
  
  if (!amount || amount <= 0) {
    alert('请输入有效金额')
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await fetch('/wallet/recharge', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ amount })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('充值成功')
      showRechargeModal.value = false
      customAmount.value = ''
      loadUserInfo()
    } else {
      alert(data.msg || '充值失败')
    }
  } catch (error) {
    console.error('充值失败:', error)
    alert('充值失败，请稍后重试')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.balance-card {
  padding: 32px 36px;
  background: linear-gradient(120deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  color: #fff;
  margin-bottom: 20px;
}

.balance-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 12px;
}

.balance-value {
  font-size: 40px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 24px;
}

.recharge-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 36px;
  border: none;
  border-radius: var(--radius);
  background: #fff;
  color: var(--c-primary);
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s;
}

.recharge-btn:hover {
  background: #f0f4ff;
  color: var(--c-primary-hover);
}

.menu-section {
  padding: 8px 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: var(--radius);
  transition: background 0.15s;
}

.menu-item + .menu-item {
  border-top: 1px solid var(--c-border);
  border-radius: 0;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: var(--c-text);
}

.menu-arrow {
  font-size: 18px;
  color: var(--c-text-3);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 420px;
  max-width: 90vw;
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.close-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  font-size: 24px;
  color: var(--c-text-3);
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.amount-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.amount-option {
  flex: 1;
  min-width: calc(33.33% - 7px);
  padding: 12px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text-2);
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
}

.amount-option:hover {
  border-color: var(--c-border-strong);
}

.amount-option.active {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: var(--c-primary-light);
  font-weight: 600;
}

.amount-input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: #fafbfc;
  color: var(--c-text);
  box-sizing: border-box;
}

.amount-input:focus {
  outline: none;
  border-color: var(--c-primary);
  background: #fff;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--c-border);
}

.btn-submit {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  color: #fff;
  background: var(--c-primary);
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover {
  background: var(--c-primary-hover);
}
</style>
