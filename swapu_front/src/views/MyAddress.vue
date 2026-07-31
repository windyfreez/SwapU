<template>
  <div class="address-page">
    <header class="address-header">
      <button class="back-btn" @click="goBack">‹</button>
      <span class="header-title">我的地址</span>
      <button class="add-btn" @click="openAddModal">+</button>
    </header>

    <div class="address-list">
      <div 
        v-for="address in addresses" 
        :key="address.id" 
        class="address-card"
      >
        <div class="address-main" @click="selectAddress(address)">
          <div class="address-header">
            <span class="consignee">{{ address.consignee }}</span>
            <span class="phone">{{ address.phone }}</span>
            <span v-if="address.sex" class="sex">{{ address.sex === '1' ? '男' : '女' }}</span>
          </div>
          <div class="address-detail">
            {{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}
          </div>
          <div class="address-footer">
            <span v-if="address.label" class="address-label">{{ address.label }}</span>
            <span v-if="address.isDefault === 1" class="default-tag">默认</span>
          </div>
        </div>
        <div class="address-actions">
          <button class="action-btn" @click="openEditModal(address)">编辑</button>
          <button class="action-btn delete" @click="handleDelete(address.id)">删除</button>
          <button v-if="address.isDefault !== 1" class="action-btn" @click="setDefault(address)">设为默认</button>
        </div>
      </div>

      <div v-if="addresses.length === 0" class="empty-state">
        <span class="empty-icon">📍</span>
        <p>暂无收货地址</p>
        <button class="add-empty-btn" @click="openAddModal">添加收货地址</button>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <span class="modal-title">{{ isEdit ? '编辑地址' : '添加地址' }}</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <label class="modal-label">收货人</label>
          <input 
            v-model="form.consignee" 
            class="modal-input" 
            placeholder="请输入收货人姓名"
          />
          <label class="modal-label">手机号</label>
          <input 
            v-model="form.phone" 
            class="modal-input" 
            type="tel"
            placeholder="请输入手机号"
          />
          <label class="modal-label">性别</label>
          <div class="sex-options">
            <button 
              class="sex-option"
              :class="{ active: form.sex === '1' }"
              @click="form.sex = '1'"
            >男</button>
            <button 
              class="sex-option"
              :class="{ active: form.sex === '0' }"
              @click="form.sex = '0'"
            >女</button>
          </div>
          <label class="modal-label">省市区</label>
          <div class="region-inputs">
            <input 
              v-model="form.provinceName" 
              class="region-input" 
              placeholder="省份"
            />
            <input 
              v-model="form.cityName" 
              class="region-input" 
              placeholder="城市"
            />
            <input 
              v-model="form.districtName" 
              class="region-input" 
              placeholder="区县"
            />
          </div>
          <label class="modal-label">详细地址</label>
          <textarea 
            v-model="form.detail" 
            class="modal-textarea" 
            placeholder="请输入详细地址"
          ></textarea>
          <label class="modal-label">标签</label>
          <input 
            v-model="form.label" 
            class="modal-input" 
            placeholder="例如：家、公司"
          />
          <label class="checkbox-label">
            <input 
              v-model="form.isDefault" 
              type="checkbox" 
              :value="1"
            />
            <span>设为默认地址</span>
          </label>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="handleSubmit">{{ isEdit ? '保存修改' : '添加地址' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const addresses = ref([])
const showModal = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  userId: null,
  consignee: '',
  phone: '',
  sex: '',
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  detail: '',
  label: '',
  isDefault: 0
})

const fetchAddresses = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

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
    } else {
      alert(data.msg || '获取地址列表失败')
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    alert('获取地址列表失败，请稍后重试')
  }
}

const openAddModal = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    userId: null,
    consignee: '',
    phone: '',
    sex: '',
    provinceCode: '',
    provinceName: '',
    cityCode: '',
    cityName: '',
    districtCode: '',
    districtName: '',
    detail: '',
    label: '',
    isDefault: 0
  })
  showModal.value = true
}

const openEditModal = (address) => {
  isEdit.value = true
  Object.assign(form, address)
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const handleSubmit = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  if (!form.consignee || !form.phone || !form.detail) {
    alert('请填写必填项')
    return
  }

  const url = isEdit.value ? '/address/update' : '/address/add'
  const method = isEdit.value ? 'PUT' : 'POST'

  const submitData = {
    ...form,
    isDefault: form.isDefault ? 1 : 0
  }

  try {
    const response = await fetch(url, {
      method: method,
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(submitData)
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert(isEdit.value ? '修改成功' : '添加成功')
      closeModal()
      fetchAddresses()
    } else {
      alert(data.msg || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败，请稍后重试')
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该地址吗？')) return

  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  try {
    const response = await fetch(`/address/delete/${id}`, {
      method: 'DELETE',
      headers: {
        'token': token
      }
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('删除成功')
      fetchAddresses()
    } else {
      alert(data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

const setDefault = async (address) => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  try {
    const response = await fetch('/address/default', {
      method: 'PUT',
      headers: {
        'token': token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ id: address.id, isDefault: 1 })
    })

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('设置成功')
      fetchAddresses()
    } else {
      alert(data.msg || '设置失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    alert('设置失败，请稍后重试')
  }
}

const selectAddress = (address) => {
  router.back()
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.address-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #1a1a1a;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}

.add-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #2563eb;
  color: white;
  border-radius: 50%;
  font-size: 24px;
}

.address-list {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.address-main {
  padding: 15px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.consignee {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.phone {
  font-size: 14px;
  color: #666;
}

.sex {
  font-size: 12px;
  color: #999;
}

.address-detail {
  font-size: 14px;
  color: #1a1a1a;
  line-height: 1.6;
  margin-bottom: 10px;
}

.address-footer {
  display: flex;
  gap: 10px;
}

.address-label {
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.default-tag {
  font-size: 12px;
  color: #ef4444;
  background: #fef2f2;
  padding: 2px 8px;
  border-radius: 4px;
}

.address-actions {
  display: flex;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: white;
  font-size: 14px;
  color: #666;
  border-right: 1px solid #f5f5f5;
}

.action-btn:last-child {
  border-right: none;
}

.action-btn.delete {
  color: #ef4444;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 15px;
  color: #999;
  margin-bottom: 20px;
}

.add-empty-btn {
  padding: 12px 30px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
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
  margin-bottom: 15px;
}

.modal-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  color: #1a1a1a;
  margin-bottom: 15px;
  min-height: 80px;
  resize: none;
}

.sex-options {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.sex-option {
  padding: 10px 30px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  background: white;
}

.sex-option.active {
  border-color: #2563eb;
  background: #eff6ff;
  color: #2563eb;
}

.region-inputs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.region-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  color: #1a1a1a;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
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
</style>