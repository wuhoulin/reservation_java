<template>
  <div class="feedback-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </div>
      <div class="title">意见反馈</div>
      <div class="history-button" @click="showHistory">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
      </div>
    </div>

    <!-- 反馈表单 -->
    <div class="feedback-form">
      <div class="form-section">
        <label class="form-label">反馈类型</label>
        <div class="type-selector">
          <div
              v-for="type in feedbackTypes"
              :key="type.value"
              class="type-item"
              :class="{ active: form.type === type.value }"
              @click="form.type = type.value"
          >
            <span class="type-name">{{ type.name }}</span>
          </div>
        </div>
      </div>

      <div class="form-section">
        <label class="form-label">反馈标题</label>
        <input
            v-model="form.title"
            type="text"
            class="form-input"
            placeholder="请简要描述您的问题"
            maxlength="50"
        >
        <div class="char-count">{{ form.title.length }}/50</div>
      </div>

      <div class="form-section">
        <label class="form-label">详细描述</label>
        <textarea
            v-model="form.content"
            class="form-textarea"
            placeholder="请详细描述您遇到的问题或建议..."
            rows="5"
            maxlength="500"
        ></textarea>
        <div class="char-count">{{ form.content.length }}/500</div>
      </div>

      <div class="form-section">
        <label class="form-label">联系方式 <span class="optional">（选填）</span></label>
        <input
            v-model="form.contact"
            type="text"
            class="form-input"
            placeholder="邮箱或手机号，方便我们联系您"
        >
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section">
      <button
          class="submit-button"
          :class="{ disabled: !canSubmit }"
          :disabled="!canSubmit || submitting"
          @click="submitFeedback"
      >
        <span v-if="submitting">提交中...</span>
        <span v-else>提交反馈</span>
      </button>
    </div>

    <!-- 历史记录侧边栏 -->
    <div class="history-sidebar" :class="{ active: showHistoryPanel }">
      <div class="sidebar-header">
        <h3>反馈历史</h3>
        <button class="close-sidebar" @click="showHistoryPanel = false">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      <div class="history-list">
        <div
            v-for="item in historyList"
            :key="item.id"
            class="history-item"
            @click="viewFeedbackDetail(item)"
        >
          <div class="history-header">
            <span class="history-title">{{ item.title }}</span>
            <span class="history-status" :class="getStatusClass(item.status)">
              {{ item.statusName }}
            </span>
          </div>
          <div class="history-content">{{ item.content }}</div>
          <div class="history-meta">
            <span class="history-type">{{ item.typeName }}</span>
            <span class="history-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div v-if="item.replyContent" class="history-reply">
            <div class="reply-label">管理员回复：</div>
            <div class="reply-content">{{ item.replyContent }}</div>
          </div>
        </div>
        <div v-if="historyList.length === 0" class="empty-history">
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
          </svg>
          <p>暂无反馈记录</p>
        </div>
      </div>
    </div>

    <!-- 遮罩层 -->
    <div
        v-if="showHistoryPanel"
        class="sidebar-overlay"
        @click="showHistoryPanel = false"
    ></div>

    <!-- 提交成功弹窗 -->
    <div v-if="showSuccessModal" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <div class="success-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <h3>感谢您的反馈！</h3>
        <p>您的意见对我们非常重要，我们会认真阅读并尽快处理。</p>
        <p class="sub-text">如有需要，我们会通过您留下的联系方式与您沟通。</p>
        <button class="modal-button" @click="closeSuccessModal">我知道了</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitFeedback as submitFeedbackApi, getMyFeedback } from '@/api/feedback.js'

const router = useRouter()

// 表单数据
const form = reactive({
  type: 1,
  title: '',
  content: '',
  contact: ''
})

// 状态
const submitting = ref(false)
const showHistoryPanel = ref(false)
const showSuccessModal = ref(false)
const historyList = ref([])

// 反馈类型选项
const feedbackTypes = [
  { value: 1, name: '功能建议' },
  { value: 2, name: '界面问题' },
  { value: 3, name: '性能问题' },
  { value: 4, name: '内容错误' },
  { value: 5, name: '其他问题' }
]

// 计算属性
const canSubmit = computed(() => {
  return form.title.trim() && form.content.trim() && form.type
})

// 方法
const goBack = () => {
  router.back()
}

const showHistory = async () => {
  await loadHistory()
  showHistoryPanel.value = true
}

const loadHistory = async () => {
  try {
    const response = await getMyFeedback()
    if (response.code === 200) {
      historyList.value = response.data || []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败')
  }
}

const submitFeedback = async () => {
  if (!canSubmit.value || submitting.value) return

  try {
    submitting.value = true
    const response = await submitFeedbackApi(form)
    if (response.code === 200) {
      showSuccessModal.value = true
      resetForm()
      await loadHistory()
    } else {
      throw new Error(response.message || '提交失败')
    }
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.content = ''
  form.contact = ''
  form.type = 1
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
}

const getStatusClass = (status) => {
  const classes = {
    1: 'pending',
    2: 'processing',
    3: 'resolved',
    4: 'closed'
  }
  return classes[status] || 'pending'
}

const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleDateString()
}

const viewFeedbackDetail = (item) => {
  ElMessage.info(`查看反馈详情: ${item.title}`)
}

// 初始化
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.feedback-container {
  min-height: 100vh;
  background: #f4f5f7;
  padding-bottom: 24px;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e8e8e8;
}

.back-button,
.history-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
  color: #333;
}

.back-button:hover,
.history-button:hover {
  background: #f0f0f0;
}

.title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

/* 反馈表单 */
.feedback-form {
  background: #fff;
  margin: 16px;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.form-section {
  margin-bottom: 24px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.optional {
  color: #999;
  font-weight: 400;
}

/* 类型选择器 */
.type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.type-item {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  font-size: 14px;
  color: #666;
}

.type-item:hover {
  border-color: #1677ff;
  color: #1677ff;
}

.type-item.active {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
}

.type-name {
  font-size: 14px;
}

/* 表单输入 */
.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
  background: #fff;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #1677ff;
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #bfbfbf;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
  line-height: 1.5;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

/* 提交按钮 */
.submit-section {
  padding: 0 16px;
}

.submit-button {
  width: 100%;
  padding: 12px 20px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-button:hover:not(.disabled) {
  background: #4096ff;
}

.submit-button.disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* 历史记录侧边栏 */
.history-sidebar {
  position: fixed;
  top: 0;
  right: -100%;
  width: 100%;
  max-width: 360px;
  height: 100vh;
  background: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  transition: right 0.25s ease;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.history-sidebar.active {
  right: 0;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.close-sidebar {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.close-sidebar:hover {
  background: #f0f0f0;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.history-item {
  padding: 14px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.history-item:hover {
  border-color: #1677ff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.history-title {
  font-weight: 500;
  color: #333;
  flex: 1;
  margin-right: 10px;
  font-size: 14px;
}

.history-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;
}

.history-status.pending {
  background: #fffbe6;
  color: #d48806;
}

.history-status.processing {
  background: #e6f7ff;
  color: #1677ff;
}

.history-status.resolved {
  background: #f6ffed;
  color: #52c41a;
}

.history-status.closed {
  background: #f5f5f5;
  color: #999;
}

.history-content {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.history-reply {
  margin-top: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #1677ff;
}

.reply-label {
  font-size: 12px;
  font-weight: 500;
  color: #1677ff;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.empty-history {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-history svg {
  margin-bottom: 12px;
  color: #d9d9d9;
}

.empty-history p {
  margin: 0;
  font-size: 14px;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
}

/* 弹窗样式 */
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
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  padding: 32px 24px;
  text-align: center;
  max-width: 320px;
  width: 100%;
}

.success-icon {
  margin-bottom: 16px;
  color: #52c41a;
}

.modal-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.modal-content p {
  color: #666;
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.6;
}

.modal-content .sub-text {
  color: #999;
  font-size: 13px;
  margin-bottom: 20px;
}

.modal-button {
  width: 100%;
  padding: 10px 20px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.modal-button:hover {
  background: #4096ff;
}

/* 响应式适配 */
@media (min-width: 769px) {
  .history-sidebar {
    right: -360px;
  }
}

@media (max-width: 768px) {
  .feedback-form {
    margin: 12px;
    padding: 16px;
  }

  .type-selector {
    gap: 8px;
  }

  .type-item {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>
