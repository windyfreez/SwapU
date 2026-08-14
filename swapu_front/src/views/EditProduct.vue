<template>
  <div class="container edit-product-page">
    <h1 class="page-title">修改商品</h1>

    <form class="edit-form" @submit.prevent="handleSave">
      <div class="form-group">
        <label class="form-label">商品标题 <span class="required">*</span></label>
        <input 
          type="text" 
          v-model="form.title" 
          placeholder="请输入商品标题"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label class="form-label">商品描述</label>
        <textarea 
          v-model="form.description" 
          placeholder="请输入商品描述"
          class="form-textarea"
        ></textarea>
      </div>

      <div class="form-group">
        <label class="form-label">商品分类 <span class="required">*</span></label>
        <div class="option-list">
          <span
            v-for="cat in categoryOptions"
            :key="cat.value"
            class="option-item"
            :class="{ active: form.categoryId === cat.value }"
            @click="form.categoryId = cat.value"
          >
            {{ cat.label }}
          </span>
        </div>
      </div>

      <div class="price-row">
        <div class="form-group">
          <label class="form-label">售价 <span class="required">*</span></label>
          <div class="price-line">
            <span class="price-symbol">¥</span>
            <input 
              type="number" 
              v-model="form.price" 
              placeholder="请输入售价"
              class="price-field"
              step="0.01"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">原价</label>
          <div class="price-line">
            <span class="price-symbol">¥</span>
            <input 
              type="number" 
              v-model="form.originalPrice" 
              placeholder="请输入原价"
              class="price-field"
              step="0.01"
            />
          </div>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品成色 <span class="required">*</span></label>
        <div class="option-list">
          <span
            v-for="item in conditionOptions"
            :key="item.value"
            class="option-item"
            :class="{ active: form.productCondition === item.value }"
            @click="form.productCondition = item.value"
          >
            {{ item.label }}
          </span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品数量</label>
        <div class="quantity-input">
          <button type="button" class="quantity-btn" @click="decreaseQty" :disabled="form.quantity <= 1">−</button>
          <input 
            type="number" 
            v-model="form.quantity" 
            class="quantity-field"
            min="1"
          />
          <button type="button" class="quantity-btn" @click="increaseQty" :disabled="form.quantity >= 999">＋</button>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品图片 <span class="required">*</span></label>
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
              <span class="add-icon">＋</span>
              <span class="add-text">{{ isUploading ? '上传中...' : '添加图片' }}</span>
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

      <div class="form-actions">
        <button type="submit" class="btn btn-primary btn-lg" :disabled="isUploading">
          保存修改
        </button>
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

const categoryOptions = [
  { value: '1', label: '数码产品' },
  { value: '2', label: '电脑配件' },
  { value: '3', label: '学习资料' },
  { value: '4', label: '生活用品' },
  { value: '5', label: '服饰鞋包' },
  { value: '6', label: '运动户外' },
  { value: '7', label: '美妆护肤' },
  { value: '8', label: '其他' }
]

const conditionOptions = [
  { value: '1', label: '全新' },
  { value: '2', label: '几乎全新' },
  { value: '3', label: '有使用痕迹' },
  { value: '4', label: '有明显磨损' }
]

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
      const response = await fetch('/user/common/upload', {
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
  padding-top: 24px;
  padding-bottom: 60px;
}

/* 标题与表单同一列对齐 */
.edit-product-page .page-title {
  max-width: 820px;
  margin: 0 auto 24px;
}

.edit-form {
  max-width: 820px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 30px;
}

/* 输入框:白底 + 浅边框,与灰色页面背景区分 */
.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.price-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.price-line {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: #fff;
  padding: 0 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.price-line:focus-within {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.price-symbol {
  font-size: 16px;
  color: var(--c-primary);
  font-weight: 600;
}

.price-field {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

/* 选项按钮:白底圆角,横向拉长等宽分布;选中变蓝 */
.option-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.option-item {
  flex: 1;
  min-width: 140px;
  padding: 11px 16px;
  text-align: center;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.06);
  font-size: 14px;
  color: var(--c-text-2);
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.option-item:hover {
  background: #f0f3fa;
  color: var(--c-text);
}

.option-item.active {
  background: var(--c-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}

/* 数量 */
.quantity-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: var(--radius);
  background: #fff;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.06);
  font-size: 20px;
  color: var(--c-primary);
  font-weight: 600;
  transition: background 0.15s;
}

.quantity-btn:hover:not(:disabled) {
  background: var(--c-primary-light);
}

.quantity-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quantity-field {
  width: 72px;
  height: 40px;
  text-align: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  background: #fff;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.quantity-field:focus {
  outline: none;
  border-color: var(--c-primary);
}

/* 图片 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--c-border);
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
  width: 22px;
  height: 22px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}

.remove-img:hover {
  background: var(--c-danger);
}

.add-image {
  width: 88px;
  height: 88px;
  border: 1.5px dashed var(--c-border-strong);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--c-text-3);
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
}

.add-image:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.add-icon {
  font-size: 22px;
  line-height: 1;
  margin-bottom: 4px;
}

.add-text {
  font-size: 12px;
}

.file-input-hidden {
  display: none;
}

/* 提交按钮靠右 */
.form-actions {
  margin-top: 40px;
  text-align: right;
  border-top: 1px solid var(--c-border);
  padding-top: 24px;
}
</style>
