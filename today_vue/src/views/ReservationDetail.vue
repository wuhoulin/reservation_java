<template>
  <div class="reservation-detail-container">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <div class="header-title">预约详情</div>
      <div class="header-actions">
        <button
            v-if="canCancelReservation(reservation)"
            class="action-btn cancel-btn"
            @click="handleCancel"
        >
          取消预约
        </button>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="detail-content" v-if="reservation.id">
      <!-- 状态卡片 -->
      <div class="status-card" :class="getStatusClass(reservation.status)">
        <div class="status-icon">
          <div v-if="reservation.status === 0" class="icon pending">⏳</div>
          <div v-else-if="reservation.status === 1" class="icon approved">✅</div>
          <div v-else-if="reservation.status === 2" class="icon rejected">❌</div>
          <div v-else-if="reservation.status === 3" class="icon cancelled">🚫</div>
          <div v-else-if="reservation.status === 4" class="icon completed">🎉</div>
        </div>
        <div class="status-info">
          <div class="status-text">{{ getStatusText(reservation.status) }}</div>
          <div class="status-desc">{{ getStatusDescription(reservation.status) }}</div>
        </div>
        <div class="reservation-no">#{{ reservation.reservationNo }}</div>
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <div class="info-label">活动名称</div>
            <div class="info-value">{{ reservation.activityName }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">预约场地</div>
            <div class="info-value">{{ reservation.roomName }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">所属社区</div>
            <div class="info-value">{{ reservation.communityName }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">预约日期</div>
            <div class="info-value">{{ formatDate(reservation.reservationDate) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">使用时间</div>
            <div class="info-value">{{ reservation.startTime }} - {{ reservation.endTime }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">参与人数</div>
            <div class="info-value">{{ reservation.attendees }}人</div>
          </div>
          <div class="info-item">
            <div class="info-label">预计完成</div>
            <div class="info-value">{{ getActivityEndDisplay }}</div>
          </div>
        </div>
      </div>

      <!-- 申请信息 -->
      <div class="info-section">
        <h3 class="section-title">申请信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <div class="info-label">申请部门</div>
            <div class="info-value">{{ reservation.department }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">申请人</div>
            <div class="info-value">{{ reservation.userName }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">学院专业</div>
            <div class="info-value">{{ reservation.college }} - {{ reservation.major }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">联系方式</div>
            <div class="info-value">{{ reservation.contact }}</div>
          </div>
          <div class="info-item" v-if="reservation.teacherName">
            <div class="info-label">指导老师</div>
            <div class="info-value">{{ reservation.teacherName }}</div>
          </div>
          <div class="info-item" v-if="reservation.teacherContact">
            <div class="info-label">老师联系方式</div>
            <div class="info-value">{{ reservation.teacherContact }}</div>
          </div>
        </div>
      </div>

      <!-- 设备需求 -->
      <div class="info-section" v-if="reservation.needProjection || reservation.otherRequirements">
        <h3 class="section-title">设备需求</h3>
        <div class="requirements-list">
          <div class="requirement-item" v-if="reservation.needProjection">
            <div class="requirement-icon">📽️</div>
            <div class="requirement-text">需要投影设备</div>
          </div>
          <div class="requirement-item" v-if="reservation.otherRequirements">
            <div class="requirement-icon">📝</div>
            <div class="requirement-text">{{ reservation.otherRequirements }}</div>
          </div>
        </div>
      </div>

      <!-- 审核信息 -->
      <div class="info-section" v-if="reservation.status === 2 || reservation.auditReason">
        <h3 class="section-title">审核信息</h3>
        <div class="audit-info" :class="{ rejected: reservation.status === 2 }">
          <div class="audit-reason">
            {{ reservation.auditReason || (reservation.status === 2 ? '预约申请未通过审核' : '') }}
          </div>
          <div class="audit-time" v-if="reservation.auditTime">
            审核时间：{{ formatDateTime(reservation.auditTime) }}
          </div>
        </div>
        <div class="resubmit-action" v-if="reservation.status === 2">
          <button class="resubmit-btn" @click="handleResubmit">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
            重新提交申请
          </button>
        </div>
      </div>

      <!-- 时间线 -->
      <div class="info-section">
        <h3 class="section-title">时间记录</h3>
        <div class="timeline">
          <div class="timeline-item" :class="{ active: true }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">预约创建</div>
              <div class="timeline-time">{{ formatDateTime(reservation.createdAt) }}</div>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: reservation.status !== 0 }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">审核处理</div>
              <div class="timeline-time" v-if="reservation.status !== 0">
                {{ reservation.auditTime ? formatDateTime(reservation.auditTime) : '系统自动处理' }}
              </div>
              <div class="timeline-time" v-else>等待审核中...</div>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: reservation.status === 4 }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">活动完成</div>
              <div class="timeline-time">
                {{ reservation.status === 4 ? getActivityEndDisplay : `预计完成：${getActivityEndDisplay}` }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <div class="error-icon">😕</div>
      <div class="error-title">加载失败</div>
      <div class="error-desc">无法获取预约详情信息</div>
      <button class="retry-btn" @click="loadReservationDetail">重试</button>
    </div>

    <!-- 取消预约确认弹窗 -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click="showCancelConfirm = false">
      <div class="modal-content confirm-modal" @click.stop>
        <div class="modal-icon">⚠️</div>
        <h3>取消预约</h3>
        <p>确定要取消预约「{{ reservation.roomName }}」吗？</p>
        <p class="warning-text">注意：频繁取消可能会影响您的预约权限</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showCancelConfirm = false">再想想</button>
          <button class="modal-btn confirm" @click="confirmCancel">确定取消</button>
        </div>
      </div>
    </div>

    <!-- 重新提交确认弹窗 -->
    <div v-if="showResubmitConfirm" class="modal-overlay" @click="showResubmitConfirm = false">
      <div class="modal-content confirm-modal" @click.stop>
        <div class="modal-icon">🔄</div>
        <h3>重新提交</h3>
        <p>确定要重新提交预约申请吗？</p>
        <p class="info-text">系统将重新审核您的预约申请</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showResubmitConfirm = false">取消</button>
          <button class="modal-btn confirm" @click="confirmResubmit">确定提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReservationDetail, cancelReservation, resubmitReservation } from '@/api/reservations.js'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const showCancelConfirm = ref(false)
const showResubmitConfirm = ref(false)

// 预约数据
const reservation = ref({})

// 计算属性：活动结束时间
const activityEndDateTime = computed(() => {
  if (!reservation.value.reservationDate || !reservation.value.endTime) {
    return null
  }

  const date = new Date(reservation.value.reservationDate)
  const [hours, minutes] = reservation.value.endTime.split(':').map(Number)

  date.setHours(hours, minutes, 0, 0)
  return date
})

// 计算属性：活动完成时间显示
const getActivityEndDisplay = computed(() => {
  if (reservation.value.status === 4) {
    // 如果活动已完成，使用updatedAt时间
    return reservation.value.updatedAt ? formatDateTime(reservation.value.updatedAt) : ''
  } else if (activityEndDateTime.value) {
    // 否则显示预计完成时间（拼接日期和时间）
    return formatDateTimeDisplay(activityEndDateTime.value)
  }
  return ''
})

// 方法
const loadReservationDetail = async () => {
  try {
    loading.value = true
    const reservationId = route.params.id
    const response = await getReservationDetail(reservationId)

    if (response.code === 200) {
      reservation.value = response.data
    } else {
      throw new Error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
    ElMessage.error('加载预约详情失败')
  } finally {
    loading.value = false
  }
}

// 新增：检查是否可以取消预约
const canCancelReservation = (reservation) => {
  // 只有待审核(0)和已通过(1)状态的预约可以取消
  if (reservation.status !== 0 && reservation.status !== 1) {
    return false
  }

  // 获取当前时间
  const now = new Date()

  // 构建预约开始时间（预约日期 + 开始时间）
  const reservationDate = new Date(reservation.reservationDate)
  const [startHour, startMinute] = reservation.startTime.split(':').map(Number)
  const startDateTime = new Date(reservationDate)
  startDateTime.setHours(startHour, startMinute, 0, 0)

  // 计算距离预约开始时间还有多少分钟
  const timeDiff = startDateTime.getTime() - now.getTime()
  const minutesDiff = Math.floor(timeDiff / (1000 * 60))

  // 如果距离开始时间小于等于5分钟，不能取消
  return minutesDiff > 5
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'pending',    // 待审核
    1: 'approved',   // 已通过
    2: 'rejected',   // 已拒绝
    3: 'cancelled',  // 已取消
    4: 'completed'   // 已完成
  }
  return classMap[status] || 'pending'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已取消',
    4: '已完成'
  }
  return textMap[status] || '未知状态'
}

const getStatusDescription = (status) => {
  const descMap = {
    0: '您的预约正在等待管理员审核',
    1: '预约已通过，请按时使用场地',
    2: '预约申请未通过审核',
    3: '预约已被取消',
    4: '预约已完成'
  }
  return descMap[status] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''

  const date = new Date(dateStr)
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)

  if (date.toDateString() === today.toDateString()) {
    return `今天（${date.getMonth() + 1}月${date.getDate()}日）`
  } else if (date.toDateString() === tomorrow.toDateString()) {
    return `明天（${date.getMonth() + 1}月${date.getDate()}日）`
  } else {
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
  }
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''

  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 新增：格式化时间显示（用于预计完成时间）
const formatDateTimeDisplay = (date) => {
  if (!date) return ''

  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)
  const activityDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  let dateStr = ''

  if (activityDate.getTime() === today.getTime()) {
    dateStr = '今天'
  } else if (activityDate.getTime() === tomorrow.getTime()) {
    dateStr = '明天'
  } else {
    dateStr = `${date.getMonth() + 1}月${date.getDate()}日`
  }

  const timeStr = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`

  return `${dateStr} ${timeStr}`
}

const goBack = () => {
  router.back()
}

const handleCancel = () => {
  // 再次检查是否可以取消
  if (!canCancelReservation(reservation.value)) {
    ElMessage.warning('距离预约开始时间不足5分钟，无法取消')
    return
  }

  showCancelConfirm.value = true
}

const confirmCancel = async () => {
  try {
    // 修改：只传递预约编号，不再传递userId
    const response = await cancelReservation(
        reservation.value.reservationNo
    )

    if (response.code === 200) {
      ElMessage.success('取消预约成功')
      showCancelConfirm.value = false
      // 重新加载数据
      loadReservationDetail()
    } else {
      throw new Error(response.message || '取消预约失败')
    }
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error('取消预约失败')
  }
}

const handleResubmit = () => {
  showResubmitConfirm.value = true
}

const confirmResubmit = async () => {
  try {
    // 修改：只传递预约ID，不再传递userId
    const response = await resubmitReservation(
        reservation.value.id
    )

    if (response.code === 200) {
      ElMessage.success('重新提交成功')
      showResubmitConfirm.value = false
      // 重新加载数据
      loadReservationDetail()
    } else {
      throw new Error(response.message || '重新提交失败')
    }
  } catch (error) {
    console.error('重新提交失败:', error)
    ElMessage.error('重新提交失败')
  }
}

// 初始化
onMounted(() => {
  loadReservationDetail()
})
</script>

<style scoped>
.reservation-detail-container {
  min-height: 100vh;
  background: #f8fafc;
}

/* 顶部导航 */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #4a5568;
}

.back-button:hover {
  background: #f7fafc;
  transform: scale(1.05);
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #e53e3e;
  border-radius: 8px;
  background: white;
  color: #e53e3e;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #fed7d7;
}

/* 主要内容 */
.detail-content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

/* 状态卡片 */
.status-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.status-card.pending {
  border-left: 6px solid #f59e0b;
}

.status-card.approved {
  border-left: 6px solid #10b981;
}

.status-card.rejected {
  border-left: 6px solid #ef4444;
}

.status-card.cancelled {
  border-left: 6px solid #6b7280;
}

.status-card.completed {
  border-left: 6px solid #3b82f6;
}

.status-icon {
  font-size: 48px;
}

.status-info {
  flex: 1;
}

.status-text {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.status-card.pending .status-text { color: #d97706; }
.status-card.approved .status-text { color: #065f46; }
.status-card.rejected .status-text { color: #dc2626; }
.status-card.cancelled .status-text { color: #6b7280; }
.status-card.completed .status-text { color: #1e40af; }

.status-desc {
  font-size: 14px;
  color: #718096;
}

.reservation-no {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 12px;
  color: #a0aec0;
  font-family: 'Courier New', monospace;
}

/* 信息区块 */
.info-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f7fafc;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  color: #2d3748;
  font-weight: 600;
}

/* 设备需求 */
.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7fafc;
  border-radius: 8px;
}

.requirement-icon {
  font-size: 20px;
}

.requirement-text {
  font-size: 14px;
  color: #4a5568;
}

/* 审核信息 */
.audit-info {
  padding: 16px;
  background: #f0fff4;
  border-radius: 8px;
  border-left: 4px solid #48bb78;
}

.audit-info.rejected {
  background: #fef2f2;
  border-left-color: #f56565;
}

.audit-reason {
  font-size: 14px;
  color: #2d3748;
  margin-bottom: 8px;
  line-height: 1.5;
}

.audit-time {
  font-size: 12px;
  color: #718096;
}

.resubmit-action {
  margin-top: 16px;
}

.resubmit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #1e88e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.resubmit-btn:hover {
  background: #1976d2;
  transform: translateY(-1px);
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 11px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e2e8f0;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #cbd5e0;
  border: 2px solid white;
  z-index: 1;
}

.timeline-item.active .timeline-dot {
  background: #1e88e5;
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.2);
}

.timeline-content {
  padding-bottom: 8px;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 14px;
  color: #718096;
}

.timeline-item:not(.active) .timeline-title {
  color: #a0aec0;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #1e88e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #6b7280;
  font-size: 16px;
  font-weight: 500;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.error-title {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.error-desc {
  color: #718096;
  margin-bottom: 24px;
}

.retry-btn {
  padding: 12px 24px;
  background: #1e88e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #1976d2;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.confirm-modal {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  text-align: center;
  max-width: 320px;
  width: 100%;
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.confirm-modal h3 {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.confirm-modal p {
  color: #718096;
  margin-bottom: 16px;
  line-height: 1.5;
}

.warning-text {
  font-size: 14px;
  color: #e53e3e;
  background: #fef2f2;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #fecaca;
}

.info-text {
  font-size: 14px;
  color: #1e88e5;
  background: #e3f2fd;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #90caf9;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.modal-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-btn.cancel {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.modal-btn.cancel:hover {
  background: #edf2f7;
}

.modal-btn.confirm {
  background: #ef4444;
  color: white;
}

.modal-btn.confirm:hover {
  background: #dc2626;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .detail-content {
    padding: 16px;
  }

  .status-card {
    padding: 20px;
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .reservation-no {
    position: static;
    margin-top: 8px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style>
