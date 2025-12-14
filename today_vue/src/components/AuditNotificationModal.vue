<!-- components/AuditNotificationModal.vue -->
<template>
  <div v-if="visible" class="audit-notification-modal">
    <!-- 遮罩层 -->
    <div class="modal-overlay" @click="handleClose"></div>

    <!-- 弹窗内容 -->
    <div class="modal-content">
      <div class="modal-header">
        <div class="header-left">
          <span class="notification-icon">
            <span v-if="notification.type === 2">✅</span>
            <span v-else>❌</span>
          </span>
          <div>
            <h3>{{ notification.type === 2 ? '审核通过' : '审核拒绝' }}</h3>
            <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
          </div>
        </div>
        <div class="header-right">
          <button
              v-if="notificationCount > 1"
              class="mark-all-btn"
              @click="handleMarkAll"
              :disabled="isProcessing"
          >
            全部已读({{ notificationCount }})
          </button>
          <button class="close-btn" @click="handleClose">×</button>
        </div>
      </div>

      <div class="modal-body">
        <div class="notification-content">
          <p>{{ notification.content }}</p>

          <div v-if="notification.reservationNo" class="reservation-info">
            <span class="info-label">预约编号：</span>
            <span class="info-value">{{ notification.reservationNo }}</span>
          </div>

          <div v-if="rejectReason" class="reject-reason">
            <span class="reason-label">拒绝原因：</span>
            <span class="reason-content">{{ rejectReason }}</span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <div class="footer-buttons">
          <button
              v-if="notification.reservationNo"
              class="btn detail-btn"
              @click="handleViewDetail"
          >
            查看详情
          </button>
          <button
              class="btn read-btn"
              @click="handleMarkRead"
              :disabled="isProcessing"
          >
            {{ isProcessing ? '处理中...' : '知道了' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  notification: {
    type: Object,
    required: true
  },
  notificationCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['close', 'mark-read', 'mark-all', 'view-detail'])

const router = useRouter()
const isProcessing = ref(false)

// 解析拒绝原因
const rejectReason = computed(() => {
  try {
    if (props.notification.extraData) {
      const extraData = JSON.parse(props.notification.extraData)
      return extraData.rejectReason || ''
    }
  } catch {
    return ''
  }
  return ''
})

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const notificationDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (notificationDay.getTime() === today.getTime()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  if (notificationDay.getTime() === today.getTime() - 86400000) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const handleMarkRead = async () => {
  isProcessing.value = true
  try {
    emit('mark-read', props.notification.id)
  } finally {
    isProcessing.value = false
  }
}

const handleMarkAll = () => {
  emit('mark-all')
}

const handleViewDetail = () => {
  if (props.notification.reservationNo) {
    emit('view-detail', props.notification.reservationNo)
  }
}

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.audit-notification-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  animation: fadeIn 0.3s ease;
}

.modal-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 450px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translate(-50%, 30px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  font-size: 24px;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  display: block;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mark-all-btn {
  padding: 6px 12px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.mark-all-btn:hover:not(:disabled) {
  background: #79bbff;
}

.mark-all-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.close-btn {
  width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.notification-content p {
  margin: 0 0 12px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #555;
}

.reservation-info {
  background: #f0f9ff;
  border: 1px solid #d1e9ff;
  border-radius: 4px;
  padding: 8px 12px;
  margin-bottom: 12px;
}

.info-label {
  font-size: 12px;
  color: #409eff;
  margin-right: 4px;
}

.info-value {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  font-family: 'Courier New', monospace;
}

.reject-reason {
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  padding: 8px 12px;
}

.reason-label {
  font-size: 12px;
  color: #f56c6c;
  margin-right: 4px;
}

.reason-content {
  font-size: 13px;
  color: #f56c6c;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.footer-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.detail-btn {
  background: white;
  color: #409eff;
  border: 1px solid #409eff;
}

.detail-btn:hover {
  background: #409eff;
  color: white;
}

.read-btn {
  background: #409eff;
  color: white;
}

.read-btn:hover:not(:disabled) {
  background: #79bbff;
}

.read-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
