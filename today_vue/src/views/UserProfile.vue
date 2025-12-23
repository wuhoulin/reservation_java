<template>
  <div class="user-profile">
    <div class="profile-header">
      <div class="back-btn" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        返回
      </div>

      <h2>个人信息</h2>

      <div class="header-right-placeholder"></div>
    </div>

    <div class="profile-content">
      <div class="info-section">
        <h3 class="section-title">微信信息</h3>
        <div class="wechat-info">
          <div class="avatar-section">
            <div class="avatar">
              <img :src="userInfo.headimgurl || defaultAvatar" alt="微信头像" />
            </div>
            <div class="avatar-info">
              <p class="nickname">{{ userInfo.nickname || '微信用户' }}</p>
              <p class="openid">ID: {{ userInfo.openid || '未知' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title">个人信息</h3>
        <div class="form-container">
          <div class="form-item">
            <label class="form-label">姓名 <span class="required-star">*</span></label>
            <input
                v-model="formData.userName"
                type="text"
                class="form-input"
                placeholder="请输入您的真实姓名"
                maxlength="30"
                @input="checkChanges"
            />
          </div>

          <div class="form-item">
            <label class="form-label">学号 <span class="required-star">*</span></label>
            <input
                v-model="formData.studentId"
                type="text"
                class="form-input"
                placeholder="请输入学号"
                maxlength="20"
                @input="checkChanges"
            />
          </div>

          <div class="form-item">
            <label class="form-label">学院 <span class="required-star">*</span></label>
            <select v-model="formData.college" class="form-select" @change="checkChanges">
              <option value="">请选择学院</option>
              <option v-for="college in collegeOptions" :key="college.value" :value="college.value">
                {{ college.label }}
              </option>
            </select>
          </div>

          <div class="form-item">
            <label class="form-label">年级专业 <span class="required-star">*</span></label>
            <input
                v-model="formData.major"
                type="text"
                class="form-input"
                placeholder="请输入年级专业，如：2023级计算机科学与技术"
                maxlength="50"
                @input="checkChanges"
            />
          </div>

          <div class="form-item">
            <label class="form-label">联系方式 <span class="required-star">*</span></label>
            <input
                v-model="formData.phonenumber"
                type="tel"
                class="form-input"
                placeholder="请输入手机号码"
                maxlength="11"
                @input="checkChanges"
            />
            <div v-if="showPhoneError" class="error-message">
              请输入正确的手机号码
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">邮箱 <span class="required-star">*</span></label>
            <input
                v-model="formData.email"
                type="email"
                class="form-input"
                placeholder="请输入邮箱地址"
                maxlength="50"
                @input="checkChanges"
            />
            <div v-if="showEmailError" class="error-message">
              请输入正确的邮箱格式
            </div>
          </div>
        </div>
      </div>

      <div v-if="hasChanges || isForcedMode" class="action-buttons">
        <button @click="saveProfile" class="save-btn" :disabled="saving">
          {{ saving ? '保存中...' : '保存信息' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserProfile, updateUserProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'

const router = useRouter()
const route = useRoute()

// 1. 判断是否处于强制模式 (仅用于控制按钮显示逻辑，不再控制拦截)
const isForcedMode = computed(() => route.query.mode === 'force')

// 用户信息
const userInfo = ref({})
const originalData = reactive({})
const formData = reactive({
  userName: '',
  studentId: '',
  college: '',
  major: '',
  phonenumber: '',
  email: ''
})

const saving = ref(false)
const hasChanges = ref(false)
const showPhoneError = ref(false)
const showEmailError = ref(false)

// 默认头像
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 修改：更新后的学院选项
const collegeOptions = ref([
  { value: '信息工程学院', label: '信息工程学院' },
  { value: '教育学院', label: '教育学院' },
  { value: '数理学院', label: '数理学院' },
  { value: '生物科学与工程学院', label: '生物科学与工程学院' },
  { value: '海洋学院', label: '海洋学院' },
  { value: '新能源与材料学院', label: '新能源与材料学院' },
  { value: '机电工程学院', label: '机电工程学院' },
  { value: '经济管理学院', label: '经济管理学院' },
  { value: '旅游管理学院', label: '旅游管理学院' },
  { value: '语言文化学院', label: '语言文化学院' },
  { value: '马克思主义学院', label: '马克思主义学院' },
  { value: '体育学院', label: '体育学院' },
  { value: '医学院', label: '医学院' }
])

// ================== 校验逻辑 ==================

const isPhoneValid = computed(() => {
  if (!formData.phonenumber) return true
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(formData.phonenumber)
})

const isEmailValid = computed(() => {
  if (!formData.email) return true
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(formData.email)
})

// ================== 导航 ==================

// 🟢 修改：无条件允许返回
const goBack = () => {
  router.back()
}

// 🟢 移除：onBeforeRouteLeave 路由守卫

// ================== 数据加载与保存 ==================

const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    userInfo.value = JSON.parse(userInfoStr)
  }
}

const loadUserProfile = async () => {
  try {
    const response = await getUserProfile()
    if (response.data) {
      Object.assign(formData, {
        userName: response.data.userName || '',
        studentId: response.data.studentId || '',
        college: response.data.college || '',
        major: response.data.major || '',
        phonenumber: response.data.phonenumber || '',
        email: response.data.email || ''
      })
      Object.assign(originalData, { ...formData })
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败，请重试')
  }
}

const checkChanges = () => {
  showPhoneError.value = formData.phonenumber && !isPhoneValid.value
  showEmailError.value = formData.email && !isEmailValid.value

  hasChanges.value = Object.keys(formData).some(key => {
    return formData[key] !== originalData[key]
  })
}

const saveProfile = async () => {
  // 1. 基础格式校验
  if (formData.phonenumber && !isPhoneValid.value) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }
  if (formData.email && !isEmailValid.value) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  // 2. 强制模式下的必填校验 (依然保留，确保用户在点击保存时必须填完)
  if (isForcedMode.value) {
    // 修改：将 'email' 添加到必填字段检查列表
    const requiredFields = ['userName', 'studentId', 'college', 'major', 'phonenumber', 'email']
    const missing = requiredFields.filter(key => !formData[key] || String(formData[key]).trim() === '')

    if (missing.length > 0) {
      ElMessage.warning('请填写所有带 * 号的必填信息')
      return
    }
  }

  try {
    saving.value = true

    // 构建提交数据
    const submitData = {}
    Object.keys(formData).forEach(key => {
      // 强制模式下提交所有字段，确保完整性
      if (isForcedMode.value || formData[key] !== originalData[key]) {
        submitData[key] = formData[key]
      }
    })

    await updateUserProfile(submitData)

    // 更新本地状态
    Object.assign(originalData, { ...formData })
    hasChanges.value = false

    ElMessage.success('个人信息保存成功！')

    // 4. 如果是强制模式，自动跳转到首页
    if (isForcedMode.value) {
      setTimeout(() => {
        router.replace('/') // 使用 replace 避免返回历史堆栈问题
      }, 1000)
    }

  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadUserProfile()
})
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

/* 头部样式优化：支持 flex 布局平衡 */
.profile-header {
  background: white;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between; /* 关键：两端对齐 */
  border-bottom: 1px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* 返回按钮 */
.back-btn {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
  width: 60px; /* 固定宽度 */
}

.back-btn:hover {
  color: #07c160;
}

.back-btn svg {
  margin-right: 4px;
}

/* 右占位符，宽度与返回按钮一致，保证标题居中 */
.header-right-placeholder {
  width: 60px;
}

.profile-header h2 {
  margin: 0;
  font-size: 17px;
  color: #333;
  flex: 1; /* 占据剩余空间 */
  text-align: center;
  font-weight: 600;
}

/* 内容区域 */
.profile-content {
  padding: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 微信信息 */
.wechat-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  align-items: center;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  margin-right: 16px;
  border: 1px solid #eee;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-info .nickname {
  margin: 0 0 6px 0;
  font-size: 17px;
  color: #333;
  font-weight: 600;
}

.avatar-info .openid {
  margin: 0;
  font-size: 13px;
  color: #999;
  font-family: monospace;
}

/* 表单样式 */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 必填星号 */
.required-star {
  color: #ff4d4f;
  margin-left: 4px;
  font-weight: bold;
}

.form-input,
.form-select {
  padding: 12px 14px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: #fcfcfc;
  color: #333;
  width: 100%;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #07c160;
  background: white;
  box-shadow: 0 0 0 3px rgba(7, 193, 96, 0.1);
}

.form-input::placeholder {
  color: #ccc;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23999'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 20px;
}

.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  align-items: center;
}

.error-message::before {
  content: '!';
  display: inline-block;
  width: 14px;
  height: 14px;
  background: #ff4d4f;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 14px;
  font-size: 10px;
  margin-right: 4px;
}

/* 操作按钮 */
.action-buttons {
  margin-top: 32px;
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: sticky;
  bottom: 20px;
}

.save-btn {
  width: 100%;
  padding: 14px;
  background: #07c160;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.2);
}

.save-btn:hover:not(:disabled) {
  background: #06ad56;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(7, 193, 96, 0.3);
}

.save-btn:active:not(:disabled) {
  transform: translateY(0);
}

.save-btn:disabled {
  background: #a0eac6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 375px) {
  .profile-content {
    padding: 12px;
  }

  .info-section {
    padding: 16px;
  }

  .wechat-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
