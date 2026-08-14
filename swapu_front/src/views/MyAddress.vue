<template>
  <AccountLayout active="address">
    <div class="address-header">
      <h1 class="page-title">我的地址</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 添加地址</button>
    </div>

    <div v-if="addresses.length > 0" class="address-grid">
      <div 
        v-for="address in addresses" 
        :key="address.id" 
        class="address-card card"
      >
        <div class="address-main" @click="selectAddress(address)">
          <div class="address-head">
            <span class="consignee">{{ address.consignee }}</span>
            <span class="phone">{{ address.phone }}</span>
            <span v-if="address.sex" class="sex">{{ address.sex === '1' ? '男' : '女' }}</span>
          </div>
          <div class="address-detail">
            {{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}
          </div>
          <div class="address-footer">
            <span v-if="address.label" class="badge badge-blue">{{ address.label }}</span>
            <span v-if="address.isDefault === 1" class="badge badge-red">默认</span>
          </div>
        </div>
        <div class="address-actions">
          <button class="btn btn-outline btn-sm" @click="openEditModal(address)">编辑</button>
          <button class="btn btn-danger btn-sm" @click="handleDelete(address.id)">删除</button>
          <button v-if="address.isDefault !== 1" class="btn btn-sm" @click="setDefault(address)">设为默认</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state card">
      <span class="empty-icon">📍</span>
      <p>暂无收货地址</p>
      <button class="btn btn-primary" @click="openAddModal">添加收货地址</button>
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
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="handleSubmit">{{ isEdit ? '保存修改' : '添加地址' }}</button>
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
.address-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.address-header .page-title {
  margin-bottom: 0;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.address-card {
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.address-main {
  flex: 1;
  cursor: pointer;
}

.address-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.consignee {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.phone {
  font-size: 14px;
  color: var(--c-text-2);
}

.sex {
  font-size: 12px;
  color: var(--c-text-3);
}

.address-detail {
  font-size: 14px;
  color: var(--c-text);
  line-height: 1.6;
  margin-bottom: 12px;
  word-break: break-all;
}

.address-footer {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.address-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--c-border);
  justify-content: flex-end;
}

.empty-state {
  margin-top: 0;
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
  width: 480px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: var(--radius-lg);
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

.modal-close {
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

.modal-label {
  display: block;
  font-size: 13px;
  color: var(--c-text-2);
  margin-bottom: 8px;
}

.modal-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: #fafbfc;
  box-sizing: border-box;
  color: var(--c-text);
  margin-bottom: 16px;
}

.modal-input:focus,
.modal-textarea:focus,
.region-input:focus {
  outline: none;
  border-color: var(--c-primary);
  background: #fff;
}

.modal-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: #fafbfc;
  box-sizing: border-box;
  color: var(--c-text);
  margin-bottom: 16px;
  min-height: 80px;
  resize: none;
}

.sex-options {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.sex-option {
  padding: 8px 28px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--c-text-2);
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
}

.sex-option.active {
  border-color: var(--c-primary);
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 600;
}

.region-inputs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.region-input {
  flex: 1;
  min-width: 0;
  padding: 10px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: #fafbfc;
  box-sizing: border-box;
  color: var(--c-text);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--c-text);
}

.checkbox-label input {
  width: 18px;
  height: 18px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--c-border);
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  height: 40px;
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
}

.btn-cancel {
  background: #fff;
  border: 1px solid var(--c-border);
  color: var(--c-text-2);
}

.btn-cancel:hover {
  border-color: var(--c-border-strong);
}

.btn-confirm {
  background: var(--c-primary);
  border: 1px solid var(--c-primary);
  color: #fff;
}

.btn-confirm:hover {
  background: var(--c-primary-hover);
}
</style>
