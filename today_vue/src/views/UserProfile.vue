<template>
  <div class="user-profile">
    <!-- 头部 -->
    <div class="profile-header">
      <div class="back-btn" @click="goBack">
        返回
      </div>
      <h2>个人信息</h2>
    </div>

    <div class="profile-content">
      <!-- 微信信息卡片（只读） -->
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

      <!-- 个人信息表单 -->
      <div class="info-section">
        <h3 class="section-title">个人信息</h3>
        <div class="form-container">
          <div class="form-item">
            <label class="form-label">姓名</label>
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
            <label class="form-label">学号</label>
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
            <label class="form-label">学院</label>
            <select v-model="formData.college" class="form-select" @change="checkChanges">
              <option value="">请选择学院</option>
              <option v-for="college in collegeOptions" :key="college.value" :value="college.value">
                {{ college.label }}
              </option>
            </select>
          </div>

          <div class="form-item">
            <label class="form-label">年级专业</label>
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
            <label class="form-label">联系方式</label>
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
        </div>
      </div>

      <!-- 操作按钮 - 只在有修改时显示 -->
      <div v-if="hasChanges" class="action-buttons">
        <button @click="saveProfile" class="save-btn" :disabled="saving">
          {{ saving ? '保存中...' : '保存信息' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile, updateUserProfile } from '@/api/user'
// 引入 Element Plus 的 Message 组件
import { ElMessage } from 'element-plus'
// 引入 Element Plus 样式（如果项目已全局引入可省略）
import 'element-plus/dist/index.css'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const originalData = reactive({})
const formData = reactive({
  userName: '',
  studentId: '',
  college: '',
  major: '',
  phonenumber: ''
})

const saving = ref(false)
const hasChanges = ref(false)
const showPhoneError = ref(false)

// 默认头像
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 学院选项
const collegeOptions = ref([
  { value: '信息工程学院', label: '信息工程学院' },
  { value: '机电学院', label: '机电学院' },
  { value: '经济管理学院', label: '经济管理学院' },
  { value: '体育学院', label: '体育学院' },
  { value: '医学院', label: '医学院' }
])

// 计算属性 - 验证手机号格式
const isPhoneValid = computed(() => {
  if (!formData.phonenumber) return true
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(formData.phonenumber)
})

// 方法
const goBack = () => {
  router.back()
}

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
      // 填充表单数据
      Object.assign(formData, {
        userName: response.data.userName || '',
        studentId: response.data.studentId || '',
        college: response.data.college || '',
        major: response.data.major || '',
        phonenumber: response.data.phonenumber || ''
      })

      // 保存原始数据用于比较
      Object.assign(originalData, { ...formData })
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 替换 alert 为 ElMessage
    ElMessage.error({
      message: '加载用户信息失败，请重试',
      duration: 3000,
      showClose: true
    })
  }
}

const checkChanges = () => {
  // 检查手机号格式
  showPhoneError.value = formData.phonenumber && !isPhoneValid.value

  // 检查是否有任何字段发生变化
  hasChanges.value = Object.keys(formData).some(key => {
    return formData[key] !== originalData[key]
  })
}

const saveProfile = async () => {
  // 手机号格式验证（如果有输入的话）
  if (formData.phonenumber && !isPhoneValid.value) {
    // 替换 alert 为 ElMessage
    ElMessage.warning({
      message: '请输入正确的手机号码',
      duration: 3000,
      showClose: true
    })
    return
  }

  try {
    saving.value = true

    // 构建提交数据 - 只提交有值的字段
    const submitData = {}
    Object.keys(formData).forEach(key => {
      if (formData[key] !== originalData[key]) {
        submitData[key] = formData[key]
      }
    })

    await updateUserProfile(submitData)

    // 更新原始数据
    Object.assign(originalData, { ...formData })
    hasChanges.value = false

    // 替换 alert 为 ElMessage
    ElMessage.success({
      message: '个人信息保存成功！',
      duration: 3000,
      showClose: true
    })
  } catch (error) {
    console.error('保存个人信息失败:', error)
    // 替换 alert 为 ElMessage
    ElMessage.error({
      message: '保存失败，请重试',
      duration: 3000,
      showClose: true
    })
  } finally {
    saving.value = false
  }
}

// 生命周期
onMounted(() => {
  loadUserInfo()
  loadUserProfile()
})
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 头部 */
.profile-header {
  background: white;
  padding: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  display: flex;
  align-items: center;
  color: #07c160;
  font-size: 14px;
  cursor: pointer;
  margin-right: 16px;
}

.profile-header h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
  flex: 1;
  text-align: center;
}

/* 内容区域 */
.profile-content {
  padding: 16px;
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  padding-bottom: 8px;
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
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
  border: 2px solid #07c160;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-info .nickname {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.avatar-info .openid {
  margin: 0;
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

/* 表单样式 */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-input,
.form-select {
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #07c160;
  box-shadow: 0 0 0 2px rgba(7, 193, 96, 0.1);
}

.form-input::placeholder {
  color: #999;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23666'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px;
}

/* 错误提示 */
.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* 操作按钮 */
.action-buttons {
  margin-top: 24px;
  animation: fadeIn 0.3s ease;
}

.save-btn {
  width: 100%;
  padding: 14px;
  background: #07c160;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn:hover:not(:disabled) {
  background: #06a050;
  transform: translateY(-1px);
}

.save-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
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
    padding: 12px;
  }

  .wechat-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
