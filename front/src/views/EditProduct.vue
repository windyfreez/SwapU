<template>
  <div class="edit-product-page">
    <header class="page-header">
      <span class="back-btn" @click="goBack">←</span>
      <h1>修改商品</h1>
      <span class="save-btn" @click="handleSave">保存</span>
    </header>

    <form class="edit-form" @submit.prevent="handleSave">
      <div class="form-section">
        <label>商品标题</label>
        <input 
          type="text" 
          v-model="form.title" 
          placeholder="请输入商品标题"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>商品描述</label>
        <textarea 
          v-model="form.description" 
          placeholder="请输入商品描述"
          class="form-textarea"
        ></textarea>
      </div>

      <div class="form-section">
        <label>分类</label>
        <select v-model="form.categoryId" class="form-select">
          <option value="">请选择分类</option>
          <option value="1">数码产品</option>
          <option value="2">电脑配件</option>
          <option value="3">学习资料</option>
          <option value="4">生活用品</option>
          <option value="5">服饰鞋包</option>
          <option value="6">运动户外</option>
          <option value="7">美妆护肤</option>
          <option value="8">其他</option>
        </select>
      </div>

      <div class="form-section">
        <label>售价</label>
        <div class="price-input-group">
          <span class="price-symbol">¥</span>
          <input 
            type="number" 
            v-model="form.price" 
            placeholder="请输入售价"
            class="form-input price-input"
            step="0.01"
          />
        </div>
      </div>

      <div class="form-section">
        <label>原价</label>
        <div class="price-input-group">
          <span class="price-symbol">¥</span>
          <input 
            type="number" 
            v-model="form.originalPrice" 
            placeholder="请输入原价"
            class="form-input price-input"
            step="0.01"
          />
        </div>
      </div>

      <div class="form-section">
        <label>商品成色</label>
        <select v-model="form.productCondition" class="form-select">
          <option value="">请选择成色</option>
          <option value="1">全新</option>
          <option value="2">几乎全新</option>
          <option value="3">有使用痕迹</option>
          <option value="4">有明显磨损</option>
        </select>
      </div>

      <div class="form-section">
        <label>商品数量</label>
        <div class="quantity-control">
          <button type="button" class="qty-btn" @click="decreaseQty">-</button>
          <input 
            type="number" 
            v-model="form.quantity" 
            class="form-input qty-input"
            min="1"
          />
          <button type="button" class="qty-btn" @click="increaseQty">+</button>
        </div>
      </div>

      <div class="form-section">
        <label>商品图片</label>
        <div class="image-upload-section">
          <div class="image-list">
            <div 
              v-for="(img, index) in form.images" 
              :key="index" 
              class="image-item"
            >
              <img :src="cleanImageUrl(img)" alt="" />
              <span class="remove-img" @click="removeImage(index)">×</span>
            </div>
            <div 
              v-if="form.images.length < 9" 
              class="add-image"
              @click="triggerUpload"
            >
              <span class="add-icon">+</span>
              <span class="add-text">添加图片</span>
            </div>
          </div>
          <input 
            ref="fileInput"
            type="file" 
            accept="image/jpeg,image/png" 
            multiple
            class="file-input-hidden"
            @change="handleFileUpload"
          />
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const fileInput = ref(null)
const isUploading = ref(false)

const form = reactive({
  id: '',
  title: '',
  description: '',
  categoryId: '',
  price: '',
  originalPrice: '',
  productCondition: '',
  quantity: 1,
  images: []
})

const cleanImageUrl = (url) => {
  if (!url) return ''
  const regex = /[`'"]/g
  return String(url).trim().replace(regex, '')
}

const goBack = () => {
  router.back()
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileUpload = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return
  
  isUploading.value = true
  const token = localStorage.getItem('token')
  
  for (let i = 0; i < files.length; i++) {
    if (form.images.length >= 9) break
    
    const file = files[i]
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await fetch('/api/user/common/upload', {
        method: 'POST',
        headers: {
          'token': token || ''
        },
        body: formData
      })
      
      if (!response.ok) {
        alert('图片上传失败')
        continue
      }
      
      const data = await response.json()
      if (data.code === 200) {
        form.images.push(data.data)
      }
    } catch (error) {
      console.error('上传失败:', error)
    }
  }
  
  isUploading.value = false
  event.target.value = ''
}

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const increaseQty = () => {
  if (form.quantity < 999) {
    form.quantity++
  }
}

const decreaseQty = () => {
  if (form.quantity > 1) {
    form.quantity--
  }
}

const fetchProductDetail = async () => {
  const id = route.params.id
  if (!id) {
    alert('商品ID不存在')
    router.back()
    return
  }

  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/product/detail/${id}`, {
      headers: {
        'token': token || ''
      }
    })

    if (!response.ok) {
      alert('获取商品信息失败')
      router.back()
      return
    }

    const data = await response.json()
    if (data.code === 200 && data.data) {
      const product = data.data
      form.id = product.id
      form.title = product.title || ''
      form.description = product.description || ''
      form.categoryId = product.categoryId ? product.categoryId.toString() : ''
      form.price = product.price || ''
      form.originalPrice = product.originalPrice || ''
      form.productCondition = product.productCondition ? product.productCondition.toString() : ''
      form.quantity = product.quantity || 1
      form.images = product.images || []
    }
  } catch (error) {
    console.error('获取商品信息失败:', error)
    alert('获取商品信息失败')
    router.back()
  }
}

const handleSave = async () => {
  if (!form.title) {
    alert('请输入商品标题')
    return
  }
  if (!form.categoryId) {
    alert('请选择分类')
    return
  }
  if (!form.price || parseFloat(form.price) <= 0) {
    alert('请输入有效的售价')
    return
  }
  if (!form.productCondition) {
    alert('请选择商品成色')
    return
  }
  if (form.images.length === 0) {
    alert('请至少上传一张商品图片')
    return
  }

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/product', {
      method: 'PUT',
      headers: {
        'token': token || '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: parseInt(form.id),
        title: form.title,
        description: form.description,
        categoryId: parseInt(form.categoryId),
        price: parseFloat(form.price),
        originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
        productCondition: parseInt(form.productCondition),
        quantity: parseInt(form.quantity),
        images: form.images
      })
    })

    if (!response.ok) {
      alert(`修改失败: ${response.status}`)
      return
    }

    const data = await response.json()
    if (data.code === 200 || data.code === 0) {
      alert('修改成功')
      router.push('/my-products')
    } else {
      alert(data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    alert('修改失败，请稍后重试')
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.edit-product-page {
  min-height: 100vh;
  background: #fafafa;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 12px 16px;
  padding-top: calc(12px + env(safe-area-inset-top));
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  font-size: 24px;
  color: #333;
  width: 32px;
}

.page-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.save-btn {
  color: #2563eb;
  font-size: 16px;
  font-weight: 500;
  width: 40px;
  text-align: right;
}

.edit-form {
  padding: 15px;
}

.form-section {
  background: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 10px;
}

.form-section label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  height: 100px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
}

.form-select {
  width: 100%;
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  background: white;
}

.price-input-group {
  display: flex;
  align-items: center;
}

.price-symbol {
  font-size: 16px;
  color: #333;
  margin-right: 8px;
}

.price-input {
  flex: 1;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  background: white;
  font-size: 18px;
  color: #333;
}

.qty-input {
  width: 60px;
  text-align: center;
}

.image-upload-section {
  margin-top: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: calc(33.33% - 7px);
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.add-image {
  width: calc(33.33% - 7px);
  aspect-ratio: 1;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.add-icon {
  font-size: 28px;
  margin-bottom: 4px;
}

.add-text {
  font-size: 12px;
}

.file-input-hidden {
  display: none;
}
</style>