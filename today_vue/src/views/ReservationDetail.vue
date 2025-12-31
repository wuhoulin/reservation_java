<template>
  <div class="reservation-detail-container">
    <div class="detail-header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
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

    <div class="detail-content" v-if="reservation.id">

      <div class="status-card" :class="getStatusClass(reservation.status)">
        <div class="status-icon">
          <div v-if="reservation.status === 1" class="icon approved">✅</div>
          <div v-else-if="reservation.status === 3" class="icon cancelled">🚫</div>
          <div v-else-if="reservation.status === 4" class="icon completed">🎉</div>
          <div v-else-if="reservation.status === 5" class="icon expired">⏰</div>
          <div v-else class="icon pending">ℹ️</div>
        </div>
        <div class="status-info">
          <div class="status-text">{{ getStatusText(reservation.status) }}</div>
          <div class="status-desc">{{ getStatusDescription(reservation.status) }}</div>

          <div v-if="reservation.status === 4" class="checkin-badge-container">
            <div v-if="reservation.checkInStatus === 1" class="checkin-badge success">
              <span class="badge-icon">📍</span>
              <span>已于 {{ formatTimeOnly(reservation.checkInTime) }} 签到</span>
            </div>
            <div v-else-if="reservation.checkInStatus === 2" class="checkin-badge error">
              <span class="badge-icon">⚠️</span>
              <span>未签到 (记违约)</span>
            </div>
          </div>

        </div>
        <div class="reservation-no">#{{ reservation.reservationNo }}</div>
      </div>

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

        </div>
      </div>

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

      <div class="info-section">
        <h3 class="section-title">时间记录</h3>
        <div class="timeline">
          <div class="timeline-item active">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">预约成功</div>
              <div class="timeline-time">{{ formatDateTime(reservation.createdAt) }}</div>
            </div>
          </div>

          <div class="timeline-item active" v-if="reservation.checkInStatus === 1">
            <div class="timeline-dot dot-success"></div>
            <div class="timeline-content">
              <div class="timeline-title">现场签到</div>
              <div class="timeline-time">{{ formatDateTime(reservation.checkInTime) }}</div>
            </div>
          </div>

          <div class="timeline-item" :class="{ active: reservation.status === 4 || reservation.status === 3 || reservation.status === 5 }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">
                {{ getEndStatusTitle(reservation.status) }}
              </div>
              <div class="timeline-time">
                {{ getEndStatusTime(reservation) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else class="error-container">
      <div class="error-icon">😕</div>
      <div class="error-title">加载失败</div>
      <div class="error-desc">无法获取预约详情信息</div>
      <button class="retry-btn" @click="loadReservationDetail">重试</button>
    </div>

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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReservationDetail, cancelReservation } from '@/api/reservations.js'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const showCancelConfirm = ref(false)

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
    return reservation.value.updatedAt ? formatDateTime(reservation.value.updatedAt) : ''
  } else if (activityEndDateTime.value) {
    return formatDateTimeDisplay(activityEndDateTime.value)
  }
  return ''
})

// 加载详情
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

// 检查是否可以取消
const canCancelReservation = (reservation) => {
  if (reservation.status !== 1 && reservation.status !== 0) {
    return false
  }

  const now = new Date()
  const reservationDate = new Date(reservation.reservationDate)
  const [startHour, startMinute] = reservation.startTime.split(':').map(Number)
  const startDateTime = new Date(reservationDate)
  startDateTime.setHours(startHour, startMinute, 0, 0)

  const timeDiff = startDateTime.getTime() - now.getTime()
  const threeHoursInMs = 3 * 60 * 60 * 1000

  return timeDiff > threeHoursInMs
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'pending',
    1: 'approved',
    2: 'rejected',
    3: 'cancelled',
    4: 'completed',
    5: 'expired'
  }
  return classMap[status] || 'pending'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '进行中',
    2: '已拒绝',
    3: '已取消',
    4: '已完成',
    5: '已过期'
  }
  return textMap[status] || '未知状态'
}

const getStatusDescription = (status) => {
  const descMap = {
    0: '数据迁移中...',
    1: '预约成功，请按时使用场地',
    2: '预约未生效',
    3: '预约已被取消',
    4: '活动已圆满结束',
    5: '预约已过期'
  }
  return descMap[status] || ''
}

const getEndStatusTitle = (status) => {
  if (status === 3) return '已取消'
  if (status === 4) return '活动完成'
  if (status === 5) return '已过期'
  return '预计完成'
}

const getEndStatusTime = (res) => {
  if (res.status === 4 || res.status === 3 || res.status === 5) {
    return res.updatedAt ? formatDateTime(res.updatedAt) : ''
  }
  return getActivityEndDisplay.value
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 🔥 新增：只提取时分，用于徽标显示
const formatTimeOnly = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatDateTimeDisplay = (date) => {
  if (!date) return ''
  const dateStr = `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
  const timeStr = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  return `${dateStr} ${timeStr}`
}

const goBack = () => {
  router.back()
}

const handleCancel = () => {
  if (!canCancelReservation(reservation.value)) {
    ElMessage.warning('距离预约开始时间不足3小时，无法取消')
    return
  }
  showCancelConfirm.value = true
}

const confirmCancel = async () => {
  try {
    const response = await cancelReservation(
        reservation.value.reservationNo
    )
    if (response.code === 200) {
      ElMessage.success('取消预约成功')
      showCancelConfirm.value = false
      loadReservationDetail()
    } else {
      throw new Error(response.message || '取消预约失败')
    }
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error('取消预约失败')
  }
}

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

.status-card.approved { border-left: 6px solid #10b981; }
.status-card.cancelled { border-left: 6px solid #6b7280; }
.status-card.completed { border-left: 6px solid #3b82f6; }
.status-card.expired { border-left: 6px solid #9ca3af; }
.status-card.pending { border-left: 6px solid #f59e0b; }

.status-icon { font-size: 48px; }

.status-info { flex: 1; }

.status-text {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.status-card.approved .status-text { color: #065f46; }
.status-card.cancelled .status-text { color: #6b7280; }
.status-card.completed .status-text { color: #1e40af; }
.status-card.expired .status-text { color: #6b7280; }
.status-card.pending .status-text { color: #d97706; }

.status-desc { font-size: 14px; color: #718096; }

/* 🔥 新增：签到徽标容器 */
.checkin-badge-container {
  margin-top: 8px;
}

/* 🔥 新增：签到徽标样式 */
.checkin-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

/* 成功签到：绿色背景 */
.checkin-badge.success {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

/* 违约：红色背景 */
.checkin-badge.error {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.badge-icon {
  font-size: 14px;
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

.info-label { font-size: 14px; color: #718096; font-weight: 500; }
.info-value { font-size: 16px; color: #2d3748; font-weight: 600; }

/* 设备需求 */
.requirements-list { display: flex; flex-direction: column; gap: 12px; }
.requirement-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; background: #f7fafc; border-radius: 8px;
}
.requirement-icon { font-size: 20px; }
.requirement-text { font-size: 14px; color: #4a5568; }

/* 时间线 */
.timeline { position: relative; padding-left: 24px; }
.timeline::before {
  content: ''; position: absolute; left: 11px; top: 0; bottom: 0;
  width: 2px; background: #e2e8f0;
}

.timeline-item { position: relative; margin-bottom: 24px; }
.timeline-item:last-child { margin-bottom: 0; }

.timeline-dot {
  position: absolute; left: -24px; top: 4px;
  width: 12px; height: 12px; border-radius: 50%;
  background: #cbd5e0; border: 2px solid white; z-index: 1;
}

/* 🔥 新增：时间轴绿色节点 */
.timeline-dot.dot-success {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.timeline-item.active .timeline-dot {
  background: #1e88e5;
  box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.2);
}

.timeline-item.active .timeline-dot.dot-success {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.timeline-content { padding-bottom: 8px; }
.timeline-title { font-size: 16px; font-weight: 600; color: #2d3748; margin-bottom: 4px; }
.timeline-time { font-size: 14px; color: #718096; }
.timeline-item:not(.active) .timeline-title { color: #a0aec0; }

/* 加载/错误状态 */
.loading-container, .error-container {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 80px 20px; text-align: center;
}
.loading-spinner {
  width: 50px; height: 50px; border: 4px solid #f3f4f6;
  border-top: 4px solid #1e88e5; border-radius: 50%;
  animation: spin 1s linear infinite; margin-bottom: 20px;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.loading-text { color: #6b7280; font-size: 16px; font-weight: 500; }

.error-icon { font-size: 64px; margin-bottom: 20px; }
.error-title { font-size: 20px; font-weight: 700; color: #2d3748; margin-bottom: 8px; }
.error-desc { color: #718096; margin-bottom: 24px; }
.retry-btn {
  padding: 12px 24px; background: #1e88e5; color: white;
  border: none; border-radius: 8px; font-weight: 600; cursor: pointer;
  transition: all 0.3s ease;
}
.retry-btn:hover { background: #1976d2; }

/* 弹窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(5px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px;
}
.confirm-modal {
  background: white; border-radius: 20px; padding: 32px 24px;
  text-align: center; max-width: 320px; width: 100%;
}
.modal-icon { font-size: 48px; margin-bottom: 16px; }
.confirm-modal h3 { font-size: 20px; font-weight: 700; color: #2d3748; margin-bottom: 8px; }
.confirm-modal p { color: #718096; margin-bottom: 16px; line-height: 1.5; }
.warning-text {
  font-size: 14px; color: #e53e3e; background: #fef2f2;
  padding: 12px; border-radius: 8px; border-left: 4px solid #fecaca;
}
.modal-actions { display: flex; gap: 12px; margin-top: 24px; }
.modal-btn {
  flex: 1; padding: 12px 20px; border: none; border-radius: 12px;
  font-weight: 600; cursor: pointer; transition: all 0.3s ease;
}
.modal-btn.cancel { background: #f7fafc; color: #4a5568; border: 1px solid #e2e8f0; }
.modal-btn.cancel:hover { background: #edf2f7; }
.modal-btn.confirm { background: #ef4444; color: white; }
.modal-btn.confirm:hover { background: #dc2626; }

@media (max-width: 768px) {
  .detail-content { padding: 16px; }
  .status-card { padding: 20px; flex-direction: column; text-align: center; gap: 12px; }
  .reservation-no { position: static; margin-top: 8px; }
  .info-grid { grid-template-columns: 1fr; }
  .modal-actions { flex-direction: column; }
}
</style>
