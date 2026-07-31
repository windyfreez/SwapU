<template>
  <div class="my-orders-page">
    <header class="orders-header">
      <div class="header-title">我的订单</div>
      <div class="role-switch">
        <button 
          class="role-btn" 
          :class="{ active: currentRole === 'buyer' }"
          @click="switchRole('buyer')"
        >
          我买到的
        </button>
        <button 
          class="role-btn" 
          :class="{ active: currentRole === 'seller' }"
          @click="switchRole('seller')"
        >
          我卖出的
        </button>
      </div>
    </header>

    <div class="status-tabs">
      <button 
        v-for="tab in statusTabs" 
        :key="tab.value"
        class="status-tab"
        :class="{ active: currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        {{ tab.label }}
        <span v-if="getStatCount(tab.value) > 0" class="tab-badge">{{ getStatCount(tab.value) }}</span>
      </button>
    </div>

    <div class="orders-list">
      <div 
        v-for="order in orders" 
        :key="order.orderNo" 
        class="order-card"
        @click="goToOrderDetail(order.orderNo)"
      >
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <span class="order-status" :class="getStatusClass(order.status)">{{ order.statusDesc }}</span>
        </div>
        
        <div class="order-product">
          <img :src="getFirstImage(order.productImage)" class="product-image" />
          <div class="product-info">
            <h3 class="product-title">{{ order.productTitle }}</h3>
            <p class="product-desc">{{ order.productDescription }}</p>
            <div class="product-price">
              <span class="price">¥{{ order.unitPrice }}</span>
              <span class="quantity">x{{ order.quantity }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <span class="total-amount">合计: ¥{{ order.totalAmount }}</span>
          <div class="order-actions">
            <button v-if="currentRole === 'seller' && order.status === 1" class="action-btn primary" @click.stop="handleConfirm(order)">确认接单</button>
            <button v-if="currentRole === 'buyer' && order.status === 2" class="action-btn primary" @click.stop="handlePay(order)">去支付</button>
            <button v-if="currentRole === 'buyer' && order.status === 2" class="action-btn" @click.stop="handleCancel(order)">取消订单</button>
            <button v-if="currentRole === 'seller' && order.status === 3" class="action-btn primary" @click.stop="handleDeliver(order)">确认发货</button>
            <button v-if="currentRole === 'buyer' && order.status === 4" class="action-btn primary" @click.stop="handleConfirm(order)">确认收货</button>
            <button v-if="order.status === 6" class="action-btn" @click.stop="handleDelete(order)">删除订单</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="orders.length === 0" class="empty-state">
      <div class="empty-icon">📦</div>
      <p>暂无订单</p>
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

    <div class="pagination">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1" 
        @click="prevPage"
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button 
        class="page-btn" 
        :disabled="currentPage >= totalPages" 
        @click="nextPage"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentStatus = ref(-1)
const currentRole = ref('buyer')
const showCancelModal = ref(false)
const showDeliverModal = ref(false)
const showConfirmModal = ref(false)
const selectedOrder = ref(null)
const cancelReason = ref('')
const logisticsCompany = ref('')
const logisticsNo = ref('')
const freight = ref('')
const showPayModal = ref(false)
const selectedPayType = ref(3)
const payPassword = ref('')

const payMethods = [
  { value: 1, label: '支付宝', icon: '💳' },
  { value: 2, label: '微信支付', icon: '💚' },
  { value: 3, label: '余额支付', icon: '💰' }
]

const orderStatistics = reactive({
  buyCompleted: 0,
  sellCompleted: 0,
  totalCostAmount: 0,
  totalSellAmount: 0,
  waitDeliver: 0,
  waitPay: 0,
  waitReceive: 0
})

const statusTabs = [
  { label: '全部', value: -1 },
  { label: '待确认', value: 1 },
  { label: '待付款', value: 2 },
  { label: '待发货', value: 3 },
  { label: '待收货', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

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

const fetchOrders = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  const params = new URLSearchParams()
  params.append('page', currentPage.value)
  params.append('pageSize', pageSize.value)
  params.append('role', currentRole.value)
  if (currentStatus.value !== -1) {
    params.append('status', currentStatus.value)
  }

  try {
    const response = await fetch(`/order/pageQuery?${params.toString()}`, {
      method: 'POST',
      headers: {
        'token': token
      }
    })

    const data = await response.json()
    console.log('订单列表响应:', data)
    if (data.code === 200 || data.code === 0) {
      orders.value = data.data.records || []
      total.value = data.data.total || 0
    } else {
      alert(data.msg || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    alert('获取订单列表失败，请稍后重试')
  }
}

const switchRole = (role) => {
  currentRole.value = role
  currentPage.value = 1
  fetchOrders()
}

const switchStatus = (status) => {
  currentStatus.value = status
  currentPage.value = 1
  fetchOrders()
}

const getStatCount = (status) => {
  switch (status) {
    case 2:
      return orderStatistics.waitPay || 0
    case 3:
      return orderStatistics.waitDeliver || 0
    case 4:
      return orderStatistics.waitReceive || 0
    case 5:
      return currentRole.value === 'buyer' 
        ? (orderStatistics.buyCompleted || 0)
        : (orderStatistics.sellCompleted || 0)
    default:
      return 0
  }
}

const fetchOrderStatistics = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/order/statistics', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      Object.assign(orderStatistics, data.data)
    }
  } catch (error) {
    console.error('获取订单统计信息失败:', error)
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchOrders()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    fetchOrders()
  }
}

const goToOrderDetail = (orderNo) => {
  router.push(`/order-detail/${orderNo}`)
}

const handlePay = (order) => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  selectedOrder.value = order
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

  if (!selectedOrder.value) return

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
        orderNo: selectedOrder.value.orderNo,
        payPassword: payPassword.value,
        payType: selectedPayType.value
      })
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('支付成功')
      showPayModal.value = false
      fetchOrders()
      fetchOrderStatistics()
    } else {
      alert(data.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    alert('支付失败，请稍后重试')
  }
}

const handleDeliver = (order) => {
  selectedOrder.value = order
  logisticsCompany.value = ''
  logisticsNo.value = ''
  showDeliverModal.value = true
}

const handleConfirm = (order) => {
  if (currentRole.value === 'seller') {
    selectedOrder.value = order
    freight.value = ''
    showConfirmModal.value = true
  } else {
    handleReceive(order)
  }
}

const handleReceive = async (order) => {
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
        orderNo: order.orderNo
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('确认收货成功')
      fetchOrders()
    } else {
      alert(data.msg || '确认收货失败')
    }
  } catch (error) {
    console.error('确认收货失败:', error)
    alert('确认收货失败，请稍后重试')
  }
}

const handleCancel = (order) => {
  selectedOrder.value = order
  cancelReason.value = ''
  showCancelModal.value = true
}

const handleDelete = (order) => {
  alert('删除订单功能开发中')
}

const closeModal = () => {
  showCancelModal.value = false
  showDeliverModal.value = false
  showConfirmModal.value = false
  selectedOrder.value = null
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
        orderNo: selectedOrder.value.orderNo,
        cancelReason: cancelReason.value
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('订单取消成功')
      closeModal()
      fetchOrders()
      fetchOrderStatistics()
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
        orderNo: selectedOrder.value.orderNo,
        logisticsCompany: logisticsCompany.value,
        logisticsNo: logisticsNo.value
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('发货成功')
      closeModal()
      fetchOrders()
      fetchOrderStatistics()
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
        orderNo: selectedOrder.value.orderNo,
        freight: parseFloat(freight.value) || 0
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('接单成功')
      closeModal()
      fetchOrders()
      fetchOrderStatistics()
    } else {
      alert(data.msg || '接单失败')
    }
  } catch (error) {
    console.error('接单失败:', error)
    alert('接单失败，请稍后重试')
  }
}

onMounted(() => {
  fetchOrders()
  fetchOrderStatistics()
})
</script>

<style scoped>
.my-orders-page {
  min-height: 100vh;
  background: #fafafa;
  padding-bottom: 30px;
}

.orders-header {
  background: white;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  padding-top: calc(15px + env(safe-area-inset-top));
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.role-switch {
  display: flex;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 3px;
}

.role-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  border-radius: 17px;
  font-size: 13px;
  color: #666;
  transition: all 0.3s;
}

.role-btn.active {
  background: white;
  color: #2563eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.status-tabs {
  display: flex;
  background: white;
  padding: 10px 15px;
  gap: 15px;
  overflow-x: auto;
}

.status-tab {
  flex-shrink: 0;
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #666;
  border-radius: 20px;
  transition: all 0.3s;
}

.status-tab.active {
  background: #eff6ff;
  color: #2563eb;
}

.tab-badge {
  display: inline-block;
  background: #ef4444;
  color: white;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  margin-left: 5px;
  padding: 0 5px;
}

.orders-list {
  padding: 15px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  border: 1px solid #f0f0f0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.order-no {
  font-size: 13px;
  color: #999;
}

.order-status {
  font-size: 13px;
  font-weight: 500;
}

.status-pending {
  color: #f59e0b;
}

.status-wait-pay {
  color: #ef4444;
}

.status-wait-deliver {
  color: #3b82f6;
}

.status-wait-receive {
  color: #10b981;
}

.status-completed {
  color: #666;
}

.status-cancelled {
  color: #999;
}

.order-product {
  display: flex;
  gap: 12px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.product-info {
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

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
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

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.total-amount {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  background: white;
}

.action-btn.primary {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 15px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e8ecf0;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  background: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
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
  background: white;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 15px;
}

.modal-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.modal-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  margin-bottom: 15px;
}

.modal-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  min-height: 80px;
  resize: none;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 15px;
  border-top: 1px solid #f5f5f5;
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
