<template>
  <div class="sell-page">
    <header class="sell-header">
      <span class="back-btn" @click="goBack">←</span>
      <h1>发布闲置</h1>
      <span class="placeholder"></span>
    </header>

    <form @submit.prevent="submitForm" class="sell-form">
      <div class="form-section">
        <h3>添加图片</h3>
        <div class="upload-area" @click="triggerFileInput">
          <div class="upload-icon">+</div>
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

      <div class="form-section">
        <label>商品标题</label>
        <input 
          type="text" 
          v-model="form.title" 
          placeholder="请输入商品标题（2-50字符）"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>商品描述</label>
        <textarea 
          v-model="form.description" 
          placeholder="请描述商品的成色、使用情况等"
          class="form-textarea"
          rows="4"
        ></textarea>
      </div>

      <div class="form-section">
        <label>售价</label>
        <div class="price-input">
          <span class="price-symbol">¥</span>
          <input 
            type="number" 
            v-model="form.price" 
            placeholder="请输入售价"
            class="form-input price-field"
          />
        </div>
      </div>

      <div class="form-section">
        <label>原价（可选）</label>
        <div class="price-input">
          <span class="price-symbol">¥</span>
          <input 
            type="number" 
            v-model="form.originalPrice" 
            placeholder="请输入原价"
            class="form-input price-field"
          />
        </div>
      </div>

      <div class="form-section">
        <label>商品分类</label>
        <div class="category-options">
          <div 
            v-for="cat in categories" 
            :key="cat.id"
            class="category-option"
            :class="{ active: form.categoryId === cat.id }"
            @click="form.categoryId = cat.id"
          >
            {{ cat.icon }} {{ cat.name }}
          </div>
        </div>
      </div>

      <div class="form-section">
        <label>商品成色</label>
        <div class="condition-options">
          <div 
            v-for="item in conditionOptions" 
            :key="item.value"
            class="condition-option"
            :class="{ active: form.condition === item.value }"
            @click="form.condition = item.value"
          >
            {{ item.label }}
          </div>
        </div>
      </div>

      <div class="form-section">
        <label>商品数量</label>
        <div class="quantity-input">
          <button 
            type="button"
            class="quantity-btn" 
            @click="decreaseQuantity"
            :disabled="parseInt(form.quantity) <= 1"
          >-</button>
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
          >+</button>
        </div>
      </div>

      <button type="submit" class="submit-btn" :disabled="loading">
        {{ loading ? '发布中...' : '发布商品' }}
      </button>
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
    const response = await fetch('/api/category/list', {
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
      const uploadResponse = await fetch('/api/user/common/upload', {
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
  min-height: 100vh;
  background: #fafafa;
}

.sell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 12px 16px;
  padding-top: calc(12px + env(safe-area-inset-top));
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 24px;
  color: #333;
  width: 32px;
}

.sell-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.placeholder {
  width: 32px;
}

.sell-form {
  padding: 15px;
}

.form-section {
  background: white;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 12px;
  border: 1px solid #f0f0f0;
}

.form-section h3 {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.form-section label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  background: #fafafa;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  background: #fafafa;
  resize: none;
}

.price-input {
  display: flex;
  align-items: center;
  background: #fafafa;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  padding: 0 12px;
}

.price-symbol {
  font-size: 16px;
  color: #2563eb;
  font-weight: 600;
}

.price-field {
  border: none;
  background: transparent;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
}

.category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-option {
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  border: 1px solid #e8ecf0;
}

.category-option.active {
  background: #eff6ff;
  border-color: #2563eb;
  color: #2563eb;
}

.condition-options {
  display: flex;
  gap: 12px;
}

.condition-option {
  flex: 1;
  padding: 10px;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  border: 2px solid #e8ecf0;
}

.condition-option.active {
  background: #eff6ff;
  border-color: #2563eb;
  color: #2563eb;
}

.quantity-input {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 2px solid #e8ecf0;
  background: white;
  font-size: 20px;
  color: #2563eb;
  font-weight: 600;
  transition: all 0.2s ease;
}

.quantity-btn:hover:not(:disabled) {
  background: #eff6ff;
  border-color: #2563eb;
}

.quantity-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quantity-field {
  width: 60px;
  height: 40px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  border: 2px solid #e8ecf0;
  border-radius: 10px;
  background: white;
}

.upload-area {
  border: 2px dashed #2563eb;
  border-radius: 12px;
  padding: 40px 30px;
  text-align: center;
  color: #2563eb;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f7ff 100%);
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #1d4ed8;
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  transform: scale(1.01);
}

.upload-icon {
  font-size: 48px;
  color: #2563eb;
  margin-bottom: 12px;
  transition: transform 0.3s ease;
}

.upload-area:hover .upload-icon {
  transform: scale(1.1);
}

.upload-area span {
  font-size: 15px;
  font-weight: 500;
}

.preview-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 15px;
}

.preview-item {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
  transition: all 0.2s ease;
}

.remove-btn:hover {
  background: #dc2626;
  transform: scale(1.1);
}

.submit-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 20px;
}

.submit-btn:disabled {
  opacity: 0.6;
}

.file-input-hidden {
  display: none;
}
</style>
