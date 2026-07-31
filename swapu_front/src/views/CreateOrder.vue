<template>
  <div class="create-order-page">
    <header class="order-header">
      <button class="back-btn" @click="goBack">‹</button>
      <span class="header-title">创建订单</span>
      <span class="placeholder"></span>
    </header>

    <div v-if="product" class="order-content">
      <div class="section-card">
        <div class="section-title">收货地址</div>
        <div v-if="addresses.length > 0" class="address-list">
          <div 
            v-for="address in addresses" 
            :key="address.id" 
            class="address-item"
            :class="{ active: selectedAddressId === address.id }"
            @click="selectedAddressId = address.id"
          >
            <div class="address-radio">
              <span v-if="selectedAddressId === address.id" class="radio-check">✓</span>
            </div>
            <div class="address-info">
              <div class="address-header">
                <span class="consignee">{{ address.consignee }}</span>
                <span class="phone">{{ address.phone }}</span>
                <span v-if="address.isDefault === 1" class="default-tag">默认</span>
              </div>
              <div class="address-detail">
                {{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-address">
          <p>暂无收货地址，请先添加</p>
          <button class="add-address-btn" @click="goToAddAddress">添加收货地址</button>
        </div>
      </div>

      <div class="product-card">
        <img :src="getFirstImage(product.images)" class="product-image" />
        <div class="product-info">
          <h3 class="product-title">{{ product.title }}</h3>
          <p class="product-desc">{{ product.description }}</p>
          <div class="product-price">
            <span class="price">¥{{ product.price }}</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">购买数量</div>
        <div class="quantity-control">
          <button class="qty-btn" @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
          <input 
            v-model="quantity" 
            type="number" 
            class="qty-input" 
            min="1"
          />
          <button class="qty-btn" @click="increaseQuantity">+</button>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">买家留言</div>
        <textarea 
          v-model="buyerMessage" 
          class="message-textarea" 
          placeholder="请输入留言，选填"
        ></textarea>
      </div>

      <div class="section-card">
        <div class="section-title">订单金额</div>
        <div class="amount-info">
          <div class="amount-row">
            <span class="label">商品金额</span>
            <span class="value">¥{{ product.price }}</span>
          </div>
          <div class="amount-row">
            <span class="label">数量</span>
            <span class="value">×{{ quantity }}</span>
          </div>
          <div class="amount-row total">
            <span class="label">订单总额</span>
            <span class="value">¥{{ totalAmount }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="loading-state">
      <div class="loading-icon">⏳</div>
      <p>加载中...</p>
    </div>

    <div class="bottom-bar">
      <div class="total-section">
        <span class="total-label">合计:</span>
        <span class="total-price">¥{{ totalAmount }}</span>
      </div>
      <button class="submit-btn" :disabled="loading" @click="submitOrder">
        {{ loading ? '提交中...' : '立即购买' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const product = ref(null)
const quantity = ref(1)
const buyerMessage = ref('')
const loading = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)

const totalAmount = computed(() => {
  if (!product.value) return 0
  return (product.value.price * quantity.value).toFixed(2)
})

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

const fetchProduct = async () => {
  const productId = route.params.productId
  if (!productId) return

  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/product/detail/${productId}`, {
      headers: {
        'token': token || ''
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      product.value = data.data
    } else {
      alert(data.msg || '获取商品信息失败')
    }
  } catch (error) {
    console.error('获取商品信息失败:', error)
    alert('获取商品信息失败')
  }
}

const fetchAddresses = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const response = await fetch('/address/list', {
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      addresses.value = data.data || []
      if (addresses.value.length > 0) {
        const defaultAddress = addresses.value.find(a => a.isDefault === 1)
        selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
      }
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const goToAddAddress = () => {
  router.push('/my-address')
}

const increaseQuantity = () => {
  quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const submitOrder = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录')
    router.push('/login')
    return
  }

  if (!product.value) {
    alert('商品信息异常')
    return
  }

  if (!selectedAddressId.value) {
    alert('请选择收货地址')
    return
  }

  loading.value = true

  try {
    const response = await fetch('/order/create', {
      method: 'POST',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        productId: product.value.id,
        quantity: quantity.value,
        buyerMessage: buyerMessage.value,
        addressId: selectedAddressId.value
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('下单成功')
      router.push(`/order-detail/${data.data.orderNo}`)
    } else {
      alert(data.msg || '下单失败')
    }
  } catch (error) {
    console.error('下单失败:', error)
    alert('下单失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchProduct()
  fetchAddresses()
})
</script>

<style scoped>
.create-order-page {
  min-height: 100vh;
  background: #fafafa;
  padding-bottom: 140px;
}

.order-header {
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

.order-content {
  padding: 15px;
}

.product-card {
  display: flex;
  gap: 12px;
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  border: 1px solid #f0f0f0;
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
  margin-top: 10px;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ef4444;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  border: 1px solid #f0f0f0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  transition: all 0.3s;
}

.address-item.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.address-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #e8ecf0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.address-item.active .address-radio {
  border-color: #2563eb;
  background: #2563eb;
}

.radio-check {
  color: white;
  font-size: 12px;
}

.address-info {
  flex: 1;
  min-width: 0;
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

.address-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.no-address {
  text-align: center;
  padding: 30px 0;
}

.no-address p {
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.add-address-btn {
  padding: 10px 25px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 15px;
}

.qty-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 18px;
  color: #666;
  background: white;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  flex: 1;
  text-align: center;
  padding: 10px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 16px;
  color: #1a1a1a;
}

.message-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  min-height: 80px;
  resize: none;
  color: #1a1a1a;
}

.message-textarea::placeholder {
  color: #999;
}

.amount-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-row .label {
  font-size: 14px;
  color: #666;
}

.amount-row .value {
  font-size: 14px;
  color: #1a1a1a;
}

.amount-row.total {
  border-top: 1px solid #f5f5f5;
  padding-top: 10px;
  margin-top: 5px;
}

.amount-row.total .label {
  font-weight: 600;
}

.amount-row.total .value {
  font-size: 18px;
  font-weight: 600;
  color: #ef4444;
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

.bottom-bar {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.total-section {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 20px;
  font-weight: 600;
  color: #ef4444;
}

.submit-btn {
  flex: 1;
  padding: 15px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  color: white;
  background: #2563eb;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
