<template>
  <div class="container sell-page">
    <h1 class="page-title">发布商品</h1>

    <form @submit.prevent="submitForm" class="sell-form">
      <div class="form-group">
        <label class="form-label">添加图片 <span class="required">*</span></label>
        <div class="upload-area" @click="triggerFileInput">
          <span class="upload-icon">＋</span>
          <span>点击添加图片</span>
        </div>
        <input 
          type="file" 
          id="file-input" 
          class="file-input-hidden" 
          multiple 
          accept="image/*"
          @change="handleFileSelect"
        />
        <div class="preview-images">
          <div v-for="(img, index) in images" :key="index" class="preview-item">
            <img :src="img.url" alt="" />
            <span class="remove-btn" @click="removeImage(index)">×</span>
          </div>
        </div>
        <p class="image-hint">最多上传9张图片</p>
      </div>

      <div class="form-group">
        <label class="form-label">商品标题 <span class="required">*</span></label>
        <input 
          type="text" 
          v-model="form.title" 
          placeholder="请输入商品标题（2-50字符）"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label class="form-label">商品描述</label>
        <textarea 
          v-model="form.description" 
          placeholder="请描述商品的成色、使用情况等"
          class="form-textarea"
          rows="4"
        ></textarea>
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
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">原价（可选）</label>
          <div class="price-line">
            <span class="price-symbol">¥</span>
            <input 
              type="number" 
              v-model="form.originalPrice" 
              placeholder="请输入原价"
              class="price-field"
            />
          </div>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品分类 <span class="required">*</span></label>
        <div class="option-list">
          <span
            v-for="cat in categories" 
            :key="cat.id"
            class="option-item"
            :class="{ active: form.categoryId === cat.id }"
            @click="form.categoryId = cat.id"
          >
            {{ cat.icon }} {{ cat.name }}
          </span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品成色 <span class="required">*</span></label>
        <div class="option-list">
          <span 
            v-for="item in conditionOptions" 
            :key="item.value"
            class="option-item"
            :class="{ active: form.condition === item.value }"
            @click="form.condition = item.value"
          >
            {{ item.label }}
          </span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">商品数量</label>
        <div class="quantity-input">
          <button 
            type="button"
            class="quantity-btn" 
            @click="decreaseQuantity"
            :disabled="parseInt(form.quantity) <= 1"
          >−</button>
          <input 
            type="number" 
            v-model="form.quantity" 
            class="quantity-field"
            min="1"
            max="99"
          />
          <button 
            type="button"
            class="quantity-btn" 
            @click="increaseQuantity"
            :disabled="parseInt(form.quantity) >= 99"
          >＋</button>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary btn-lg" :disabled="loading">
          {{ loading ? '发布中...' : '发布商品' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const images = ref([])
const categories = ref([
  { id: 1, icon: '📱', name: '手机数码' },
  { id: 2, icon: '💻', name: '电脑办公' },
  { id: 3, icon: '📚', name: '图书教材' },
  { id: 4, icon: '🏠', name: '生活用品' },
  { id: 5, icon: '👕', name: '服饰鞋包' },
  { id: 6, icon: '⚽', name: '运动户外' },
  { id: 7, icon: '💄', name: '美妆个护' },
  { id: 8, icon: '📦', name: '其他' }
])

const conditionOptions = [
  { value: 1, label: '全新' },
  { value: 2, label: '几乎全新' },
  { value: 3, label: '有使用痕迹' }
]

const form = reactive({
  title: '',
  description: '',
  price: '',
  originalPrice: '',
  categoryId: '',
  condition: '',
  quantity: '1'
})

const fetchCategories = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/category/list', {
      headers: {
        'token': token || ''
      }
    })
    if (response.ok) {
      const data = await response.json()
      if (data.code === 200 && data.data) {
        categories.value = data.data.map(cat => ({
          id: cat.id,
          icon: getCategoryIcon(cat.name),
          name: cat.name
        }))
      }
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const getCategoryIcon = (name) => {
  const iconMap = {
    '手机数码': '📱',
    '电脑办公': '💻',
    '图书教材': '📚',
    '生活用品': '🏠',
    '服饰鞋包': '👕',
    '运动户外': '⚽',
    '美妆个护': '💄',
    '其他': '📦'
  }
  return iconMap[name] || '📦'
}

const triggerFileInput = () => {
  document.getElementById('file-input')?.click()
}

const handleFileSelect = (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return
  
  const remainingSlots = 9 - images.value.length
  const filesToAdd = Array.from(files).slice(0, remainingSlots)
  
  filesToAdd.forEach(file => {
    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件')
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      images.value.push({
        url: e.target.result,
        file: file
      })
    }
    reader.readAsDataURL(file)
  })
  
  event.target.value = ''
}

const removeImage = (index) => {
  images.value.splice(index, 1)
}

const increaseQuantity = () => {
  if (parseInt(form.quantity) < 99) {
    form.quantity = (parseInt(form.quantity) + 1).toString()
  }
}

const decreaseQuantity = () => {
  if (parseInt(form.quantity) > 1) {
    form.quantity = (parseInt(form.quantity) - 1).toString()
  }
}

const submitForm = async () => {
  if (!form.title.trim()) {
    alert('请输入商品标题')
    return
  }
  if (form.title.length < 2 || form.title.length > 50) {
    alert('商品标题长度必须在2-50字符之间')
    return
  }
  if (!form.price) {
    alert('请输入售价')
    return
  }
  if (parseFloat(form.price) < 0.01 || parseFloat(form.price) > 99999.99) {
    alert('售价必须在0.01-99999.99之间')
    return
  }
  if (!form.categoryId) {
    alert('请选择商品分类')
    return
  }
  if (!form.condition) {
    alert('请选择商品成色')
    return
  }
  if (images.value.length === 0) {
    alert('请至少上传一张商品图片')
    return
  }

  loading.value = true
  
  try {
    const token = localStorage.getItem('token')
    
    const imageUrls = []
    for (const img of images.value) {
      const formData = new FormData()
      formData.append('file', img.file)
      const uploadResponse = await fetch('/user/common/upload', {
        method: 'POST',
        headers: {
          'token': token || ''
        },
        body: formData
      })
      const uploadData = await uploadResponse.json()
      if (uploadData.code === 200) {
        imageUrls.push(uploadData.data)
      } else {
        throw new Error('图片上传失败')
      }
    }
    
    const requestBody = {
        title: form.title.trim(),
        description: form.description.trim(),
        categoryId: parseInt(form.categoryId),
        price: parseFloat(form.price),
        originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
        productCondition: parseInt(form.condition),
        quantity: parseInt(form.quantity || 1),
        images: imageUrls
      }
      console.log('提交数据:', requestBody)
      
    const response = await fetch('/product', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': token || ''
      },
      body: JSON.stringify(requestBody)
    })

    const data = await response.json()
    if (data.code === 200) {
      alert('发布成功！')
      router.push('/')
    } else {
      alert(data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    alert('发布失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.sell-page {
  padding-top: 24px;
  padding-bottom: 60px;
}

/* 标题与表单同一列对齐 */
.sell-page .page-title {
  max-width: 820px;
  margin: 0 auto 24px;
}

.sell-form {
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

/* 上传区:白底虚线,轻量 */
.upload-area {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 26px;
  border: 1.5px dashed var(--c-border-strong);
  border-radius: var(--radius);
  color: var(--c-text-2);
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
}

.upload-area:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: #fbfcff;
}

.upload-icon {
  font-size: 17px;
  font-weight: 600;
}

.upload-area span {
  font-size: 14px;
}

.preview-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.preview-item {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--c-border);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
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
  transition: background 0.15s;
}

.remove-btn:hover {
  background: var(--c-danger);
}

.image-hint {
  font-size: 12px;
  color: var(--c-text-3);
  margin-top: 8px;
}

/* 提交按钮靠右 */
.form-actions {
  margin-top: 40px;
  text-align: right;
  border-top: 1px solid var(--c-border);
  padding-top: 24px;
}

.file-input-hidden {
  display: none;
}
</style>
