<template>
  <div class="create-order-page">
    <div class="container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <router-link :to="`/product/${route.params.productId}`">商品详情</router-link>
        <span>/</span>
        <span>创建订单</span>
      </div>

      <div v-if="product" class="order-content">
        <!-- 收货地址 -->
        <div class="card section-card">
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
            <button class="btn btn-primary" @click="goToAddAddress">添加收货地址</button>
          </div>
        </div>

        <!-- 商品卡 -->
        <div class="card product-card">
          <img :src="getFirstImage(product.images)" class="product-image" />
          <div class="product-info">
            <h3 class="product-title">{{ product.title }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="price">¥{{ product.price }}</span>
            </div>
          </div>
        </div>

        <!-- 购买数量 -->
        <div class="card section-card">
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

        <!-- 买家留言 -->
        <div class="card section-card">
          <div class="section-title">买家留言</div>
          <textarea
            v-model="buyerMessage"
            class="form-textarea message-textarea"
            placeholder="请输入留言，选填"
          ></textarea>
        </div>

        <!-- 金额明细 -->
        <div class="card section-card">
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

        <!-- 提交 -->
        <div class="card submit-card">
          <div class="total-section">
            <span class="total-label">合计:</span>
            <span class="total-price">¥{{ totalAmount }}</span>
          </div>
          <button class="btn btn-primary btn-lg submit-btn" :disabled="loading" @click="submitOrder">
            {{ loading ? '提交中...' : '立即购买' }}
          </button>
        </div>
      </div>

      <div v-else class="loading-state">
        <div class="loading-icon">⏳</div>
        <p>加载中...</p>
      </div>
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
  min-height: 60vh;
  padding-bottom: 40px;
}

.order-content {
  max-width: 860px;
  margin: 0 auto;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  padding: 18px 22px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--c-border);
}

/* 地址 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}

.address-item:hover {
  border-color: var(--c-border-strong);
}

.address-item.active {
  border-color: var(--c-primary);
  background: var(--c-primary-light);
}

.address-radio {
  width: 20px;
  height: 20px;
  border: 2px solid var(--c-border-strong);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  background: #fff;
}

.address-item.active .address-radio {
  border-color: var(--c-primary);
  background: var(--c-primary);
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
  color: var(--c-text);
}

.phone {
  font-size: 13px;
  color: var(--c-text-2);
}

.default-tag {
  font-size: 11px;
  color: var(--c-danger);
  background: var(--c-danger-light);
  padding: 1px 6px;
  border-radius: 4px;
}

.address-detail {
  font-size: 13px;
  color: var(--c-text-2);
  line-height: 1.5;
}

.no-address {
  text-align: center;
  padding: 30px 0;
}

.no-address p {
  font-size: 14px;
  color: var(--c-text-3);
  margin-bottom: 16px;
}

/* 商品卡 */
.product-card {
  display: flex;
  gap: 16px;
  padding: 16px 18px;
}

.product-image {
  width: 96px;
  height: 96px;
  border-radius: var(--radius);
  object-fit: cover;
  flex-shrink: 0;
  background: #f0f1f3;
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: var(--c-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 4px 0;
}

.product-price {
  margin-top: 6px;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-danger);
}

/* 数量 */
.quantity-control {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 260px;
}

.qty-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 18px;
  color: var(--c-text-2);
  background: var(--c-card);
  cursor: pointer;
  transition: all 0.15s;
}

.qty-btn:hover:not(:disabled) {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  flex: 1;
  text-align: center;
  padding: 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 16px;
  color: var(--c-text);
  background: #fafbfc;
}

.qty-input:focus {
  outline: none;
  border-color: var(--c-primary);
  background: #fff;
}

.message-textarea {
  width: 100%;
  min-height: 90px;
}

/* 金额 */
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
  color: var(--c-text-2);
}

.amount-row .value {
  font-size: 14px;
  color: var(--c-text);
}

.amount-row.total {
  border-top: 1px solid var(--c-border);
  padding-top: 12px;
  margin-top: 4px;
}

.amount-row.total .label {
  font-weight: 600;
  color: var(--c-text);
}

.amount-row.total .value {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-danger);
}

/* 提交 */
.submit-card {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding: 16px 22px;
}

.total-section {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.total-label {
  font-size: 14px;
  color: var(--c-text-2);
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-danger);
}

.submit-btn {
  min-width: 180px;
}

.submit-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--c-text-3);
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.loading-state p {
  font-size: 15px;
}
</style>
