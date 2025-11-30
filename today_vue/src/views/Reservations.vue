<template>
  <div class="reservations-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="title">我的预约</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <span class="stat-number">{{ totalCount }}</span>
        <span class="stat-label">总预约</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ pendingCount }}</span>
        <span class="stat-label">进行中</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ approvedCount }}</span>
        <span class="stat-label">已完成</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ rejectedCount }}</span>
        <span class="stat-label">被退回</span>
      </div>
    </div>

    <!-- 筛选按钮 -->
    <div class="filter-bar">
      <div class="section-title">我的预约</div>
      <div class="record-count" :class="{ zero: filteredReservations.length === 0 }">
        {{ filteredReservations.length }}条记录
      </div>
      <div class="filter-button" @click="showFilter = !showFilter">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
        </svg>
      </div>
    </div>

    <!-- 筛选条件 -->
    <transition name="slide-down">
      <div v-if="showFilter" class="filter-panel">
        <div class="filter-group">
          <div class="filter-label">状态筛选</div>
          <div class="filter-options">
            <div
                v-for="status in statusOptions"
                :key="status.value"
                class="filter-option"
                :class="{ active: filterStatus === status.value }"
                @click="filterStatus = status.value"
            >
              {{ status.label }}
            </div>
          </div>
        </div>
        <div class="filter-group">
          <div class="filter-label">时间筛选</div>
          <div class="filter-options">
            <div
                v-for="time in timeOptions"
                :key="time.value"
                class="filter-option"
                :class="{ active: filterTime === time.value }"
                @click="filterTime = time.value"
            >
              {{ time.label }}
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredReservations.length === 0" class="empty-state">
      <div class="empty-image">
        <img src="https://via.placeholder.com/120x120?text=暂无预约" alt="暂无预约" />
      </div>
      <h3 class="empty-title">暂无预约记录</h3>
      <p class="empty-desc">快去预约您心仪的教室吧</p>
      <button class="explore-button" @click="goToCommunity">
        去预约
      </button>
    </div>

    <!-- 预约列表 -->
    <div v-else class="reservations-list">
      <div
          v-for="reservation in filteredReservations"
          :key="reservation.id"
          class="reservation-card"
          :class="getStatusClass(reservation.status)"
      >
        <!-- 状态标签 -->
        <div class="status-badge">
          {{ getStatusText(reservation.status) }}
        </div>

        <!-- 主要内容 -->
        <div class="card-content" @click="viewReservationDetail(reservation)">
          <div class="reservation-header">
            <h3 class="room-name">{{ reservation.roomName }}</h3>
            <div class="reservation-no">#{{ reservation.reservationNo }}</div>
          </div>

          <div class="reservation-info">
            <div class="info-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              <span>{{ formatDate(reservation.reservationDate) }}</span>
            </div>
            <div class="info-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ reservation.startTime }} - {{ reservation.endTime }}</span>
            </div>
            <div class="info-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
              <span>{{ reservation.attendees }}人</span>
            </div>
            <div class="info-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>{{ reservation.communityName }}</span>
            </div>
          </div>

          <div class="activity-info">
            <div class="activity-name">{{ reservation.activityName }}</div>
            <div class="department">{{ reservation.department }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button
              v-if="reservation.status === 0"
              class="action-btn cancel-btn"
              @click="handleCancelReservation(reservation)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
            取消预约
          </button>
          <button
              v-if="reservation.status === 2"
              class="action-btn resubmit-btn"
              @click="handleResubmitReservation(reservation)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 2v6h6"></path>
              <path d="M21 12A9 9 0 0 0 6 5.3L3 8"></path>
              <path d="M21 22v-6h-6"></path>
              <path d="M3 12a9 9 0 0 0 15 6.7l3-2.7"></path>
            </svg>
            重新提交
          </button>
          <button
              v-if="reservation.status === 1 && canCheckIn(reservation)"
              class="action-btn checkin-btn"
              @click="checkIn(reservation)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            签到
          </button>
          <button
              class="action-btn detail-btn"
              @click="viewReservationDetail(reservation)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            详情
          </button>
        </div>
      </div>
    </div>

    <!-- 取消预约确认弹窗 -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click="showCancelConfirm = false">
      <div class="modal-content confirm-modal" @click.stop>
        <div class="modal-icon">⚠️</div>
        <h3>取消预约</h3>
        <p>确定要取消预约「{{ selectedReservation?.roomName }}」吗？</p>
        <p class="warning-text">注意：频繁取消可能会影响您的预约权限</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showCancelConfirm = false">再想想</button>
          <button class="modal-btn confirm" @click="confirmCancel">确定取消</button>
        </div>
      </div>
    </div>

    <!-- 签到成功弹窗 -->
    <div v-if="showCheckInSuccess" class="modal-overlay" @click="showCheckInSuccess = false">
      <div class="modal-content success-modal" @click.stop>
        <div class="success-icon">✅</div>
        <h3>签到成功</h3>
        <p>祝您活动顺利！</p>
        <button class="modal-button" @click="showCheckInSuccess = false">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyReservations, cancelReservation, resubmitReservation, checkInReservation } from '@/api/reservations.js'

const router = useRouter()

// 状态
const loading = ref(true)
const showFilter = ref(false)
const showCancelConfirm = ref(false)
const showCheckInSuccess = ref(false)
const selectedReservation = ref(null)
const filterStatus = ref('all')
const filterTime = ref('all')

// 预约数据
const reservations = ref([])

// 筛选选项
const statusOptions = [
  { value: 'all', label: '全部' },
  { value: '0', label: '待审核' },
  { value: '1', label: '已通过' },
  { value: '2', label: '已拒绝' },
  { value: '3', label: '已取消' },
  { value: '4', label: '已完成' }
]

const timeOptions = [
  { value: 'all', label: '全部时间' },
  { value: 'today', label: '今天' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
  { value: 'future', label: '未来' },
  { value: 'past', label: '历史' }
]

// 计算属性
const filteredReservations = computed(() => {
  let list = reservations.value

  // 状态筛选
  if (filterStatus.value !== 'all') {
    list = list.filter(item => item.status.toString() === filterStatus.value)
  }

  // 时间筛选
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekStart = new Date(today)
  weekStart.setDate(today.getDate() - today.getDay())
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

  switch (filterTime.value) {
    case 'today':
      list = list.filter(item => new Date(item.reservationDate).getTime() === today.getTime())
      break
    case 'week':
      list = list.filter(item => new Date(item.reservationDate) >= weekStart)
      break
    case 'month':
      list = list.filter(item => new Date(item.reservationDate) >= monthStart)
      break
    case 'future':
      list = list.filter(item => new Date(item.reservationDate) >= today)
      break
    case 'past':
      list = list.filter(item => new Date(item.reservationDate) < today)
      break
  }

  return list.sort((a, b) => new Date(b.reservationDate) - new Date(a.reservationDate))
})

const totalCount = computed(() => reservations.value.length)
const pendingCount = computed(() => reservations.value.filter(item => item.status === 1).length) // 进行中对应已通过
const approvedCount = computed(() => reservations.value.filter(item => item.status === 4).length) // 已完成
const rejectedCount = computed(() => reservations.value.filter(item => item.status === 2).length) // 被退回对应已拒绝

// 方法
const loadReservations = async () => {
  try {
    loading.value = true
    const response = await getMyReservations()
    if (response.code === 200) {
      reservations.value = response.data || []
    } else {
      throw new Error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
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

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === tomorrow.toDateString()) {
    return '明天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

const canCheckIn = (reservation) => {
  const now = new Date()
  const reservationDate = new Date(reservation.reservationDate)
  const startTime = new Date(`${reservation.reservationDate} ${reservation.startTime}`)

  // 预约日期是今天，且当前时间在开始时间前后30分钟内
  return reservationDate.toDateString() === now.toDateString() &&
      Math.abs(now - startTime) <= 30 * 60 * 1000
}

const viewReservationDetail = (reservation) => {
  // 跳转到预约详情页
  router.push(`/reservation-detail/${reservation.id}`)
}

const handleCancelReservation = (reservation) => {
  selectedReservation.value = reservation
  showCancelConfirm.value = true
}

const confirmCancel = async () => {
  if (!selectedReservation.value) return

  try {
    const response = await cancelReservation(
        selectedReservation.value.reservationNo,
        selectedReservation.value.userId
    )
    if (response.code === 200) {
      ElMessage.success('取消预约成功')
      const index = reservations.value.findIndex(item => item.id === selectedReservation.value.id)
      if (index > -1) {
        reservations.value[index].status = 3 // 已取消
      }
    } else {
      throw new Error(response.message || '取消预约失败')
    }
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error('取消预约失败')
  } finally {
    showCancelConfirm.value = false
    selectedReservation.value = null
  }
}

const handleResubmitReservation = async (reservation) => {
  try {
    const response = await resubmitReservation(reservation.id, reservation.userId)
    if (response.code === 200) {
      ElMessage.success('重新提交成功')
      const index = reservations.value.findIndex(item => item.id === reservation.id)
      if (index > -1) {
        reservations.value[index].status = 0 // 待审核
      }
    } else {
      throw new Error(response.message || '重新提交失败')
    }
  } catch (error) {
    console.error('重新提交失败:', error)
    ElMessage.error('重新提交失败')
  }
}

const checkIn = async (reservation) => {
  try {
    const response = await checkInReservation(reservation.id)
    if (response.code === 200) {
      showCheckInSuccess.value = true
      ElMessage.success('签到成功')
    } else {
      throw new Error(response.message || '签到失败')
    }
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  }
}

const goToCommunity = () => {
  router.push('/community-list')
}

// 初始化
onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.reservations-container {
  min-height: 100vh;
  background: #ffffff;
  padding-bottom: 24px;
}

/* 顶部导航栏 */
.header {
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #333333;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  justify-content: space-around;
  padding: 20px 10px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-number {
  font-size: 23px;
  font-weight: 700;
  color: #1e88e5; /* 蓝色数字 */
}

/* 统计卡片数字颜色区分 */
.stats-cards .stat-card:nth-child(1) .stat-number {
  color: #43a047; /* 总预约-绿色 */
}
.stats-cards .stat-card:nth-child(2) .stat-number {
  color: #1e88e5; /* 进行中-蓝色 */
}
.stats-cards .stat-card:nth-child(3) .stat-number {
  color: #43a047; /* 已完成-绿色 */
}
.stats-cards .stat-card:nth-child(4) .stat-number {
  color: #e53935; /* 被退回-红色 */
}

.stat-label {
  font-size: 14px;
  color: #888888;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #333333;
}

.record-count {
  padding: 4px 12px;
  background: #f0fff4;
  border-radius: 12px;
  color: #2f855a;
  font-size: 14px;
  font-weight: 500;
}

.record-count.zero {
  background: #fef2f2;
  color: #dc2626;
}

.filter-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #1e88e5;
}

.filter-button:hover {
  background: rgba(30, 136, 229, 0.1);
  transform: scale(1.05);
}

/* 筛选面板 */
.filter-panel {
  background: white;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 8px 16px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 13px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-option:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.filter-option.active {
  background: #e3f2fd;
  border-color: #1e88e5;
  color: #1e88e5;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-image {
  width: 120px;
  height: 120px;
  margin-bottom: 24px;
}

.empty-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.empty-desc {
  color: #718096;
  margin-bottom: 32px;
  font-size: 15px;
}

.explore-button {
  padding: 12px 24px;
  background: #43a047;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.explore-button:hover {
  background: #388e3c;
}

/* 预约列表 */
.reservations-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reservation-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.reservation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 状态样式 */
.reservation-card.pending {
  border-left: 4px solid #f59e0b;
}

.reservation-card.approved {
  border-left: 4px solid #10b981;
}

.reservation-card.rejected {
  border-left: 4px solid #ef4444;
}

.reservation-card.cancelled {
  border-left: 4px solid #6b7280;
}

.reservation-card.completed {
  border-left: 4px solid #3b82f6;
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.reservation-card.pending .status-badge {
  background: #fef3c7;
  color: #d97706;
}

.reservation-card.approved .status-badge {
  background: #d1fae5;
  color: #065f46;
}

.reservation-card.rejected .status-badge {
  background: #fee2e2;
  color: #dc2626;
}

.reservation-card.cancelled .status-badge {
  background: #f3f4f6;
  color: #6b7280;
}

.reservation-card.completed .status-badge {
  background: #dbeafe;
  color: #1e40af;
}

/* 卡片内容 */
.card-content {
  cursor: pointer;
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.room-name {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.3;
  flex: 1;
  margin-right: 12px;
}

.reservation-no {
  font-size: 12px;
  color: #a0aec0;
  font-family: 'Courier New', monospace;
}

.reservation-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4a5568;
}

.info-item svg {
  color: #1e88e5;
  flex-shrink: 0;
}

.activity-info {
  padding: 12px;
  background: #f7fafc;
  border-radius: 8px;
}

.activity-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.department {
  font-size: 12px;
  color: #718096;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  justify-content: center;
  min-width: 80px;
}

.cancel-btn {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.cancel-btn:hover {
  background: #fee2e2;
  transform: translateY(-1px);
}

.resubmit-btn {
  background: #f0f9ff;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.resubmit-btn:hover {
  background: #e0f2fe;
  transform: translateY(-1px);
}

.checkin-btn {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.checkin-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.detail-btn {
  background: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.detail-btn:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
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

.confirm-modal, .success-modal {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  text-align: center;
  max-width: 320px;
  width: 100%;
}

.modal-icon, .success-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.confirm-modal h3, .success-modal h3 {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.confirm-modal p, .success-modal p {
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

.modal-button {
  width: 100%;
  padding: 14px 20px;
  background: #43a047;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-button:hover {
  background: #388e3c;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .reservations-list {
    padding: 12px;
  }

  .filter-options {
    justify-content: space-between;
  }

  .filter-option {
    flex: 1;
    text-align: center;
    min-width: 60px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
