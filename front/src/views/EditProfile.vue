<template>
  <div class="edit-profile-page">
    <div class="header">
      <span class="back-btn" @click="goBack">‹</span>
      <h1>编辑资料</h1>
      <span class="save-btn" @click="handleSave">保存</span>
    </div>

    <form class="edit-form">
      <div class="form-section">
        <label>用户名</label>
        <input 
          type="text" 
          v-model="form.username" 
          placeholder="请输入用户名"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>昵称</label>
        <input 
          type="text" 
          v-model="form.nickname" 
          placeholder="请输入昵称"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>学号</label>
        <input 
          type="text" 
          v-model="form.studentId" 
          placeholder="请输入学号"
          class="form-input"
          disabled
        />
      </div>

      <div class="form-section">
        <label>手机号</label>
        <input 
          type="tel" 
          v-model="form.phone" 
          placeholder="请输入手机号"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>邮箱</label>
        <input 
          type="email" 
          v-model="form.email" 
          placeholder="请输入邮箱"
          class="form-input"
        />
      </div>

      <div class="form-section">
        <label>学院</label>
        <select v-model="form.college" class="form-select">
          <option value="">请选择学院</option>
          <option value="通信工程学院">通信工程学院</option>
          <option value="电子工程学院">电子工程学院</option>
          <option value="计算机科学与技术学院">计算机科学与技术学院</option>
          <option value="机电工程学院">机电工程学院</option>
          <option value="物理与光电工程学院">物理与光电工程学院</option>
          <option value="经济与管理学院">经济与管理学院</option>
          <option value="数学与统计学院">数学与统计学院</option>
          <option value="人文学院">人文学院</option>
          <option value="外国语学院">外国语学院</option>
          <option value="微电子学院">微电子学院</option>
          <option value="软件学院">软件学院</option>
          <option value="生命科学技术学院">生命科学技术学院</option>
          <option value="空间科学与技术学院">空间科学与技术学院</option>
          <option value="先进材料与纳米科技学院">先进材料与纳米科技学院</option>
          <option value="网络与信息安全学院">网络与信息安全学院</option>
          <option value="人工智能学院">人工智能学院</option>
          <option value="马克思主义学院">马克思主义学院</option>
          <option value="体育部">体育部</option>
        </select>
      </div>

      <div class="form-section">
        <label>头像</label>
        <div class="avatar-upload">
          <div class="avatar-preview" @click="triggerUpload">
            <img v-if="form.avatar" :src="form.avatar" alt="头像" />
            <span v-else>👤</span>
          </div>
          <div class="upload-info">
            <span class="upload-btn" @click="triggerUpload">
              {{ isUploading ? '上传中...' : '上传头像' }}
            </span>
            <span class="upload-tip">支持JPG、PNG格式</span>
          </div>
          <input 
            ref="fileInput"
            type="file" 
            accept="image/jpeg,image/png" 
            class="file-input"
            @change="handleFileUpload"
          />
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
const isLoading = ref(false);
const isUploading = ref(false);
const fileInput = ref(null);
const form = reactive({
 username: '',
 nickname: '',
 studentId: '',
 phone: '',
 email: '',
 college: '',
 avatar: ''
});
const goBack = () => {
 router.back();
};
const triggerUpload = () => {
 fileInput.value?.click();
};
const handleFileUpload = async (event) => {
 const file = event.target.files?.[0];
 if (!file)
 return;
 isUploading.value = true;
 try {
 const formData = new FormData();
 formData.append('file', file);
 const token = localStorage.getItem('token');
 const response = await fetch('/user/common/upload', {
 method: 'POST',
 headers: {
 'token': token || ''
 },
 body: formData
 });
 if (!response.ok) {
 alert('上传失败');
 return;
 }
 const data = await response.json();
 if (data.code === 200) {
 form.avatar = data.data;
 }
 else {
 alert(data.message || '上传失败');
 }
 }
 catch (error) {
 console.error('上传失败:', error);
 alert('上传失败，请稍后重试');
 }
 finally {
 isUploading.value = false;
 }
};
const handleSave = async () => {
 if (!form.username) {
 alert('请输入用户名');
 return;
 }
 isLoading.value = true;
 try {
 const token = localStorage.getItem('token');
 const response = await fetch('/user', {
 method: 'PUT',
 headers: {
 'token': token,
 'Content-Type': 'application/json'
 },
 body: JSON.stringify({
 username: form.username,
 nickname: form.nickname,
 phone: form.phone,
 email: form.email,
 college: form.college,
 avatar: form.avatar
 })
 });
 if (!response.ok) {
 alert(`请求失败: ${response.status}`);
 return;
 }
 const contentType = response.headers.get('content-type');
 if (!contentType || !contentType.includes('application/json')) {
 alert('服务器返回格式错误');
 return;
 }
 const data = await response.json();
 if (data.code === 200) {
 alert('修改成功');
 localStorage.removeItem('userInfo');
 router.push('/profile');
 }
 else {
 alert(data.message || '修改失败');
 }
 }
 catch (error) {
 console.error('修改失败:', error);
 alert('修改失败，请稍后重试');
 }
 finally {
 isLoading.value = false;
 }
};
const loadUserInfo = async () => {
 const savedUserInfo = localStorage.getItem('userInfo');
 if (savedUserInfo && savedUserInfo !== 'undefined' && savedUserInfo !== 'null') {
 try {
 const user = JSON.parse(savedUserInfo);
 Object.assign(form, user);
 }
 catch (e) {
 console.error('解析用户信息失败:', e);
 }
 }
 const token = localStorage.getItem('token');
 if (token) {
 try {
 const response = await fetch('/user/info', {
 headers: {
 'token': token,
 'Content-Type': 'application/json'
 }
 });
 if (response.ok) {
 const data = await response.json();
 if (data.code === 200) {
 Object.assign(form, data.data);
 localStorage.setItem('userInfo', JSON.stringify(data.data));
 }
 }
 }
 catch (e) {
 console.error('获取用户信息失败:', e);
 }
 }
};
onMounted(() => {
 loadUserInfo();
});
</script>

<style scoped>
.edit-profile-page {
  min-height: 100vh;
  background: #fafafa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 15px;
  padding-top: calc(16px + env(safe-area-inset-top));
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  font-size: 24px;
  color: #333;
}

.header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.save-btn {
  font-size: 16px;
  color: #2563eb;
  font-weight: 500;
}

.edit-form {
  padding: 15px;
}

.form-section {
  background: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
}

.form-section label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  background: #fafafa;
}

.form-input:disabled {
  background: #f0f0f0;
  color: #999;
}

.form-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
  background: #fafafa;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
  cursor: pointer;
  border: 2px dashed #ddd;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.upload-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.upload-btn {
  font-size: 14px;
  color: #2563eb;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

.file-input {
  display: none;
}
</style>