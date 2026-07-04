<template>
  <div class="order-detail-page">
    <header class="detail-header">
      <button class="back-btn" @click="goBack">‹</button>
      <span class="header-title">订单详情</span>
      <span class="placeholder"></span>
    </header>

    <div v-if="order" class="detail-content">
      <div class="status-section">
        <div class="status-badge" :class="getStatusClass(order.status)">
          {{ order.statusDesc }}
        </div>
        <div class="order-no">订单号: {{ order.orderNo }}</div>
        <div class="create-time">下单时间: {{ formatTime(order.createTime) }}</div>
      </div>

      <div class="section-card">
        <div class="section-title">商品信息</div>
        <div class="product-card">
          <img :src="getFirstImage(order.productImage)" class="product-image" />
          <div class="product-detail">
            <h3 class="product-title">{{ order.productTitle }}</h3>
            <p class="product-desc">{{ order.productDescription }}</p>
            <div class="product-price-row">
              <span class="price">¥{{ order.unitPrice }}</span>
              <span class="quantity">数量: {{ order.quantity }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">买家信息</div>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">买家昵称</span>
            <span class="info-value">{{ buyerInfo?.nickname || buyerInfo?.username || order.buyerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">头像</span>
            <img v-if="buyerInfo?.avatar" :src="buyerInfo.avatar" class="info-avatar" />
            <span v-else class="info-value">👤</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">卖家信息</div>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">卖家昵称</span>
            <span class="info-value">{{ sellerInfo?.nickname || sellerInfo?.username || order.sellerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">头像</span>
            <img v-if="sellerInfo?.avatar" :src="sellerInfo.avatar" class="info-avatar" />
            <span v-else class="info-value">👤</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">收货地址</div>
        <div v-if="addressInfo" class="address-info">
          <div class="address-icon">📍</div>
          <div class="address-content">
            <div class="address-header">
              <span class="consignee">{{ addressInfo.consignee }}</span>
              <span class="phone">{{ addressInfo.phone }}</span>
              <span v-if="addressInfo.isDefault === 1" class="default-tag">默认</span>
            </div>
            <div class="address-text">{{ addressInfo.provinceName }}{{ addressInfo.cityName }}{{ addressInfo.districtName }}{{ addressInfo.detail }}</div>
          </div>
        </div>
        <div v-else-if="order.address" class="address-info">
          <div class="address-icon">📍</div>
          <div class="address-content">
            <div class="address-text">{{ order.address }}</div>
          </div>
        </div>
        <div v-else class="address-empty">
          <span class="empty-text">暂无收货地址</span>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">物流信息</div>
        <div v-if="order.logisticsNo" class="logistics-info">
          <div class="logistics-item">
            <span class="info-label">物流公司</span>
            <span class="info-value">{{ order.logisticsCompany }}</span>
          </div>
          <div class="logistics-item">
            <span class="info-label">物流单号</span>
            <span class="info-value">{{ order.logisticsNo }}</span>
          </div>
        </div>
        <div v-else class="no-logistics">暂无物流信息</div>
      </div>

      <div class="section-card">
        <div class="section-title">金额明细</div>
        <div class="amount-list">
          <div class="amount-item">
            <span class="amount-label">商品金额</span>
            <span class="amount-value">¥{{ order.amount }}</span>
          </div>
          <div class="amount-item">
            <span class="amount-label">运费</span>
            <span class="amount-value">¥{{ order.freight }}</span>
          </div>
          <div class="amount-item total">
            <span class="amount-label">订单总额</span>
            <span class="amount-value">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">交易时间线</div>
        <div class="timeline">
          <div class="timeline-item" :class="{ active: order.payTime }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-label">付款时间</div>
              <div class="timeline-value">{{ order.payTime ? formatTime(order.payTime) : '未付款' }}</div>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: order.deliverTime }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-label">发货时间</div>
              <div class="timeline-value">{{ order.deliverTime ? formatTime(order.deliverTime) : '未发货' }}</div>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: order.confirmTime }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-label">确认收货</div>
              <div class="timeline-value">{{ order.confirmTime ? formatTime(order.confirmTime) : '未确认' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="order.buyerMessage" class="section-card">
        <div class="section-title">买家留言</div>
        <div class="buyer-message">{{ order.buyerMessage }}</div>
      </div>
    </div>

    <div v-else class="loading-state">
      <div class="loading-icon">⏳</div>
      <p>加载中...</p>
    </div>

    <div v-if="showCancelModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">取消订单</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <label class="modal-label">取消原因</label>
          <textarea 
            v-model="cancelReason" 
            class="modal-textarea" 
            placeholder="请输入取消原因"
          ></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="confirmCancel">确认取消</button>
        </div>
      </div>
    </div>

    <div v-if="showDeliverModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">确认发货</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <label class="modal-label">物流公司</label>
          <input 
            v-model="logisticsCompany" 
            class="modal-input" 
            placeholder="请输入物流公司"
          />
          <label class="modal-label">物流单号</label>
          <input 
            v-model="logisticsNo" 
            class="modal-input" 
            placeholder="请输入物流单号"
          />
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="confirmDeliver">确认发货</button>
        </div>
      </div>
    </div>

    <div v-if="showConfirmModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">确认接单</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <label class="modal-label">运费</label>
          <input 
            v-model="freight" 
            type="number" 
            class="modal-input" 
            placeholder="请输入运费"
            step="0.01"
          />
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="confirmOrder">确认接单</button>
        </div>
      </div>
    </div>

    <div v-if="showPayModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">选择支付方式</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="pay-methods">
            <div 
              v-for="method in payMethods" 
              :key="method.value"
              class="pay-method"
              :class="{ active: selectedPayType === method.value }"
              @click="selectedPayType = method.value"
            >
              <span class="pay-icon">{{ method.icon }}</span>
              <span class="pay-label">{{ method.label }}</span>
              <span v-if="selectedPayType === method.value" class="pay-check">✓</span>
            </div>
          </div>
          <div v-if="selectedPayType === 3" class="pay-password-section">
            <label class="modal-label">支付密码</label>
            <input 
              v-model="payPassword" 
              type="password" 
              class="modal-input" 
              placeholder="请输入支付密码"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="confirmPay">确认支付</button>
        </div>
      </div>
    </div>
    <div v-if="order" class="bottom-actions">
      <button v-if="isSeller && order.status === 1" class="action-btn primary" @click="handleConfirm">确认接单</button>
      <button v-if="isBuyer && order.status === 2" class="action-btn primary" @click="handlePay">去支付</button>
      <button v-if="isBuyer && order.status === 2" class="action-btn" @click="handleCancel">取消订单</button>
      <button v-if="isSeller && order.status === 3" class="action-btn primary" @click="handleDeliver">确认发货</button>
      <button v-if="isBuyer && order.status === 4" class="action-btn primary" @click="handleConfirm">确认收货</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const order = ref(null)
const showCancelModal = ref(false)
const showDeliverModal = ref(false)
const showConfirmModal = ref(false)
const showPayModal = ref(false)
const cancelReason = ref('')
const logisticsCompany = ref('')
const logisticsNo = ref('')
const freight = ref('')
const selectedPayType = ref(3)
const payPassword = ref('')

const buyerInfo = ref(null)
const sellerInfo = ref(null)
const addressInfo = ref(null)

const payMethods = [
  { value: 1, label: '支付宝', icon: '💳' },
  { value: 2, label: '微信支付', icon: '💚' },
  { value: 3, label: '余额支付', icon: '💰' }
]

const currentUserId = ref(null)

const isBuyer = computed(() => {
  return order.value && currentUserId.value && order.value.buyerId === currentUserId.value
})

const isSeller = computed(() => {
  return order.value && currentUserId.value && order.value.sellerId === currentUserId.value
})

const getStatusClass = (status) => {
  const classes = {
    1: 'status-pending',
    2: 'status-wait-pay',
    3: 'status-wait-deliver',
    4: 'status-wait-receive',
    5: 'status-completed',
    6: 'status-cancelled'
  }
  return classes[status] || ''
}

const getFirstImage = (images) => {
  if (!images) return ''
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed[0]
      }
    } catch (e) {
      return images
    }
  }
  if (Array.isArray(images) && images.length > 0) {
    return images[0]
  }
  return ''
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchUserInfo = async (userId) => {
  const token = localStorage.getItem('token')
  if (!token || !userId) return null

  try {
    const response = await fetch(`/user/${userId}`, {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      return data.data
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

const fetchAddressInfo = async (addressId) => {
  const token = localStorage.getItem('token')
  if (!token || !addressId) return null

  try {
    const response = await fetch(`/address/${addressId}`, {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      return data.data
    }
    return null
  } catch (error) {
    console.error('获取地址信息失败:', error)
    return null
  }
}

const fetchOrderDetail = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  const orderNo = route.params.orderNo
  if (!orderNo) return

  try {
    const response = await fetch(`/order/detail/${orderNo}`, {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      order.value = data.data
      
      if (order.value.buyerId) {
        buyerInfo.value = await fetchUserInfo(order.value.buyerId)
      }
      if (order.value.sellerId) {
        sellerInfo.value = await fetchUserInfo(order.value.sellerId)
      }
      if (order.value.addressId) {
        addressInfo.value = await fetchAddressInfo(order.value.addressId)
      }
    } else {
      alert(data.msg || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    alert('获取订单详情失败，请稍后重试')
  }
}

const goBack = () => {
  router.back()
}

const handlePay = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  selectedPayType.value = 3
  payPassword.value = ''
  showPayModal.value = true
}

const confirmPay = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  if (selectedPayType.value === 3 && !payPassword.value) {
    alert('请输入支付密码')
    return
  }

  try {
    const response = await fetch('/order/pay', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderNo: order.value.orderNo,
        payPassword: payPassword.value,
        payType: selectedPayType.value
      })
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('支付成功')
      showPayModal.value = false
      fetchOrderDetail()
    } else {
      alert(data.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    alert('支付失败，请稍后重试')
  }
}

const handleDeliver = () => {
  logisticsCompany.value = ''
  logisticsNo.value = ''
  showDeliverModal.value = true
}

const handleConfirm = () => {
  if (order.value.status === 1) {
    freight.value = ''
    showConfirmModal.value = true
  } else if (order.value.status === 4) {
    handleReceive()
  }
}

const handleReceive = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  if (!confirm('确认已收到商品吗？')) {
    return
  }

  try {
    const response = await fetch('/order/receive', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderNo: order.value.orderNo
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('确认收货成功')
      fetchOrderDetail()
    } else {
      alert(data.msg || '确认收货失败')
    }
  } catch (error) {
    console.error('确认收货失败:', error)
    alert('确认收货失败，请稍后重试')
  }
}

const handleCancel = () => {
  cancelReason.value = ''
  showCancelModal.value = true
}

const closeModal = () => {
  showCancelModal.value = false
  showDeliverModal.value = false
  showConfirmModal.value = false
  cancelReason.value = ''
  logisticsCompany.value = ''
  logisticsNo.value = ''
  freight.value = ''
}

const confirmCancel = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  try {
    const response = await fetch('/order/cancel', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderNo: order.value.orderNo,
        cancelReason: cancelReason.value
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('订单取消成功')
      closeModal()
      fetchOrderDetail()
    } else {
      alert(data.msg || '取消订单失败')
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    alert('取消订单失败，请稍后重试')
  }
}

const confirmDeliver = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  if (!logisticsCompany.value || !logisticsNo.value) {
    alert('请填写物流公司和物流单号')
    return
  }

  try {
    const response = await fetch('/order/deliver', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderNo: order.value.orderNo,
        logisticsCompany: logisticsCompany.value,
        logisticsNo: logisticsNo.value
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('发货成功')
      closeModal()
      fetchOrderDetail()
    } else {
      alert(data.msg || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    alert('发货失败，请稍后重试')
  }
}

const confirmOrder = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  try {
    const response = await fetch('/order/confirm', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        orderNo: order.value.orderNo,
        freight: parseFloat(freight.value) || 0
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('接单成功')
      closeModal()
      fetchOrderDetail()
    } else {
      alert(data.msg || '接单失败')
    }
  } catch (error) {
    console.error('接单失败:', error)
    alert('接单失败，请稍后重试')
  }
}

onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    try {
      const userInfo = JSON.parse(savedUserInfo)
      if (userInfo.id) {
        currentUserId.value = typeof userInfo.id === 'string' ? parseInt(userInfo.id) : userInfo.id
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: #fafafa;
  padding-bottom: 140px;
}

.detail-header {
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

.status-section {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  padding: 20px 15px;
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-bottom: 10px;
}

.status-pending {
  background: rgba(245, 158, 11, 0.9);
}

.status-wait-pay {
  background: rgba(239, 68, 68, 0.9);
}

.status-wait-deliver {
  background: rgba(59, 130, 246, 0.9);
}

.status-wait-receive {
  background: rgba(16, 185, 129, 0.9);
}

.status-completed {
  background: rgba(102, 102, 102, 0.9);
}

.status-cancelled {
  background: rgba(153, 153, 153, 0.9);
}

.order-no {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 5px;
}

.create-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.detail-content {
  padding: 15px;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  border: 1px solid #f0f0f0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.product-card {
  display: flex;
  gap: 12px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.product-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ef4444;
}

.quantity {
  font-size: 13px;
  color: #999;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #1a1a1a;
}

.info-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.address-info {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
}

.address-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.consignee {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.phone {
  font-size: 13px;
  color: #666;
}

.default-tag {
  font-size: 11px;
  color: #ef4444;
  background: #fef2f2;
  padding: 1px 6px;
  border-radius: 4px;
}

.address-text {
  font-size: 14px;
  color: #1a1a1a;
  line-height: 1.6;
}

.address-empty {
  text-align: center;
  padding: 20px 0;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.logistics-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.logistics-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-logistics {
  font-size: 14px;
  color: #999;
  text-align: center;
  padding: 20px 0;
}

.amount-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-item.total {
  border-top: 1px solid #f5f5f5;
  padding-top: 10px;
  margin-top: 5px;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  font-size: 14px;
  color: #1a1a1a;
}

.amount-item.total .amount-value {
  font-size: 18px;
  font-weight: 600;
  color: #ef4444;
}

.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e8ecf0;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -17px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ddd;
}

.timeline-item.active .timeline-dot {
  background: #2563eb;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-label {
  font-size: 13px;
  color: #666;
}

.timeline-value {
  font-size: 14px;
  color: #1a1a1a;
}

.buyer-message {
  font-size: 14px;
  color: #1a1a1a;
  line-height: 1.6;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.loading-state p {
  font-size: 15px;
  color: #999;
}

.bottom-actions {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  display: flex;
  gap: 0;
  padding: 0;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  flex: 1;
  padding: 15px;
  border: none;
  border-right: 1px solid #e8ecf0;
  border-radius: 0;
  font-size: 15px;
  color: #666;
  background: white;
}

.action-btn:last-child {
  border-right: none;
}

.action-btn.primary {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
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

.modal-close {
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

.modal-label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.modal-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  color: #1a1a1a;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-secondary {
  flex: 1;
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.btn-primary {
  flex: 1;
  padding: 12px;
  background: #2563eb;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: white;
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.pay-method {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #e8ecf0;
  border-radius: 10px;
  background: white;
  transition: all 0.3s;
}

.pay-method.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.pay-icon {
  font-size: 20px;
  margin-right: 12px;
}

.pay-label {
  flex: 1;
  font-size: 15px;
  color: #1a1a1a;
}

.pay-check {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: white;
  border-radius: 50%;
  font-size: 12px;
}

.pay-password-section {
  margin-top: 10px;
}
</style>
