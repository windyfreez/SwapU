<template>
  <div class="wallet-page">
    <header class="wallet-header">
      <button class="back-btn" @click="goBack">‹</button>
      <span class="header-title">我的钱包</span>
      <span class="placeholder"></span>
    </header>

    <div class="balance-card">
      <div class="balance-label">账户余额</div>
      <div class="balance-value">¥{{ userInfo.balance || '0.00' }}</div>
      <div class="balance-actions">
        <button class="action-btn primary" @click="showRechargeModal = true">充值</button>
      </div>
    </div>

    <div class="menu-section">
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
  </div>
</template>

<script setup>
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
    const response = await fetch('/api/user/info', {
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
.wallet-page {
  min-height: 100vh;
  background: #fafafa;
}

.wallet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-top: calc(15px + env(safe-area-inset-top));
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #1a1a1a;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.placeholder {
  width: 36px;
}

.balance-card {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  margin: 15px;
  padding: 30px;
  border-radius: 16px;
  text-align: center;
  color: white;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.balance-value {
  font-size: 40px;
  font-weight: 600;
  margin-bottom: 20px;
}

.balance-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.action-btn {
  padding: 12px 30px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 25px;
  font-size: 15px;
  color: white;
  background: transparent;
}

.action-btn.primary {
  background: white;
  color: #2563eb;
}

.menu-section {
  background: white;
  margin: 0 15px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #1a1a1a;
}

.menu-arrow {
  font-size: 18px;
  color: #ddd;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
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
  color: #999;
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
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  background: white;
}

.amount-option.active {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.custom-amount {
  margin-bottom: 10px;
}

.amount-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  color: #1a1a1a;
}

.form-group {
  margin-bottom: 15px;
}

.form-label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  color: #1a1a1a;
}

.form-input::placeholder {
  color: #999;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  color: white;
  background: #2563eb;
}
</style>