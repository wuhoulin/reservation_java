<template>
  <div class="reservations-container">
    <div class="header">
      <div class="title">我的预约</div>
      <div class="header-right">
        <div class="record-count" :class="{ zero: filteredReservations.length === 0 }">
          {{ filteredReservations.length }}条记录
        </div>
        <div class="filter-button" @click="showFilter = !showFilter">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
          </svg>
        </div>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card" @click="goToFilteredReservations('all')">
        <span class="stat-number">{{ totalCount }}</span>
        <span class="stat-label">总预约</span>
      </div>
      <div class="stat-card" @click="goToFilteredReservations('0')">
        <span class="stat-number">{{ pendingAuditCount }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-card" @click="goToFilteredReservations('1')">
        <span class="stat-number">{{ ongoingCount }}</span>
        <span class="stat-label">进行中</span>
      </div>
      <div class="stat-card" @click="goToFilteredReservations('4')">
        <span class="stat-number">{{ approvedCount }}</span>
        <span class="stat-label">已完成</span>
      </div>
      <div class="stat-card" @click="goToFilteredReservations('2')">
        <span class="stat-number">{{ rejectedCount }}</span>
        <span class="stat-label">被退回</span>
      </div>
    </div>

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
                @click="handleFilterClick(status.value)"
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

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else-if="filteredReservations.length === 0" class="empty-state">
      <h3 class="empty-title">暂无预约记录</h3>
      <p class="empty-desc">快去预约您心仪的教室吧</p>
      <button class="explore-button" @click="goToCommunity">
        去预约
      </button>
    </div>

    <div v-else class="reservations-list">
      <div
          v-for="reservation in filteredReservations"
          :key="reservation.id"
          class="reservation-card"
          :class="getStatusClass(reservation.status)"
          @click="viewReservationDetail(reservation)"
      >
        <div class="card-header">
          <div class="header-left">
            <svg class="room-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <h3 class="room-name">{{ reservation.roomName }}</h3>
          </div>
          <div class="status-tag" :class="getStatusClass(reservation.status)">
            {{ getStatusText(reservation.status) }}
          </div>
        </div>

        <div class="card-body">
          <div class="time-box">
            <div class="date-row">
              <svg class="mini-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
              <span class="date">{{ formatDate(reservation.reservationDate) }}</span>
            </div>
            <div class="time-row">
              <svg class="mini-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
              <span class="time">{{ reservation.startTime }} - {{ reservation.endTime }}</span>
            </div>
          </div>

          <div class="community-tag">
            <svg class="tag-icon" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4 8 4v14"/><path d="M8 21v-2a2 2 0 0 1 4 0v2"/></svg>
            {{ reservation.communityName }}
          </div>
        </div>

        <div class="divider"></div>

        <div class="card-footer">
          <div class="activity-summary">
            <svg class="label-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path><line x1="4" y1="22" x2="4" y2="15"></line></svg>
            <span class="value">{{ reservation.activityName }}</span>
            <span class="dept-badge">{{ reservation.department }}</span>
          </div>

          <div class="action-buttons">
            <button
                v-if="canCancelReservation(reservation)"
                class="btn-mini cancel"
                @click.stop="handleCancelReservation(reservation)"
            >
              取消
            </button>
            <button
                class="btn-mini detail"
                @click.stop="viewReservationDetail(reservation)"
            >
              详情
            </button>
          </div>
        </div>
      </div>
    </div>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyReservations, cancelReservation } from '@/api/reservations.js'

const router = useRouter()

// 状态
const loading = ref(true)
const showFilter = ref(false)
const showCancelConfirm = ref(false)
const selectedReservation = ref(null)
const filterStatus = ref('all') // 默认为全部
const filterTime = ref('all')

// 预约数据
const reservations = ref([])

// 筛选选项
const statusOptions = [
  { value: 'all', label: '全部' },
  { value: '0', label: '待审核' },
  { value: '1', label: '进行中' },
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

// 核心修复：筛选计算属性
const filteredReservations = computed(() => {
  let list = reservations.value

  // 状态筛选
  if (filterStatus.value !== 'all') {
    // 🟢 修复点：强制转换为 String 进行比较，防止类型不匹配 (number vs string)
    list = list.filter(item => String(item.status) === String(filterStatus.value))
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

  // 按日期倒序排列
  return list.sort((a, b) => new Date(b.reservationDate) - new Date(a.reservationDate))
})

// 统计数据 (基于原始数据计算)
const totalCount = computed(() => reservations.value.length)
const pendingAuditCount = computed(() => reservations.value.filter(item => item.status === 0).length)
const ongoingCount = computed(() => reservations.value.filter(item => item.status === 1).length)
const approvedCount = computed(() => reservations.value.filter(item => item.status === 4).length)
const rejectedCount = computed(() => reservations.value.filter(item => item.status === 2).length)

// 加载数据 (只调用一次，获取全部数据)
const loadReservations = async () => {
  try {
    loading.value = true
    // 传入 null，让后端返回所有数据，我们在前端筛选
    const response = await getMyReservations(null)
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

// 核心修复：点击统计卡片时的处理逻辑
const goToFilteredReservations = (status) => {
  console.log('切换筛选状态:', status)
  // 直接修改前端变量，触发 computed 重新计算
  filterStatus.value = String(status)
  // 如果筛选面板是打开的，顺便关掉它（可选）
  showFilter.value = false
}

// 筛选面板点击处理
const handleFilterClick = (statusValue) => {
  filterStatus.value = String(statusValue)
}

// 状态样式映射
const getStatusClass = (status) => {
  const classMap = {
    0: 'pending-audit', // 待审核
    1: 'ongoing',       // 进行中
    2: 'rejected',      // 已拒绝
    3: 'cancelled',     // 已取消
    4: 'completed'      // 已完成
  }
  return classMap[status] || 'pending-audit'
}

// 状态文本映射
const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '进行中',
    2: '已拒绝',
    3: '已取消',
    4: '已完成'
  }
  return textMap[status] || '未知状态'
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return ''
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

// 检查是否可以取消
const canCancelReservation = (reservation) => {
  if (reservation.status !== 0 && reservation.status !== 1) {
    return false
  }
  const now = new Date()
  const reservationDate = new Date(reservation.reservationDate)
  // 兼容 startTime 为空的情况
  const [startHour, startMinute] = reservation.startTime ? reservation.startTime.split(':').map(Number) : [0, 0]
  const startDateTime = new Date(reservationDate)
  startDateTime.setHours(startHour, startMinute, 0, 0)

  const timeDiff = startDateTime.getTime() - now.getTime()
  const minutesDiff = Math.floor(timeDiff / (1000 * 60))

  return minutesDiff > 5
}

const viewReservationDetail = (reservation) => {
  router.push(`/reservation-detail/${reservation.id}`)
}

const handleCancelReservation = (reservation) => {
  if (!canCancelReservation(reservation)) {
    ElMessage.warning('距离预约开始时间不足5分钟，无法取消')
    return
  }
  selectedReservation.value = reservation
  showCancelConfirm.value = true
}

const confirmCancel = async () => {
  if (!selectedReservation.value) return

  try {
    const response = await cancelReservation(selectedReservation.value.reservationNo)
    if (response.code === 200) {
      ElMessage.success('取消预约成功')
      const index = reservations.value.findIndex(item => item.id === selectedReservation.value.id)
      if (index > -1) {
        reservations.value[index].status = 3 // 更新为已取消
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

const goToCommunity = () => {
  router.push('/community-list')
}

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

/* --- 头部样式 --- */
.header {
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  box-sizing: border-box;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #333333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-count {
  padding: 4px 12px;
  background: #f0fff4;
  border-radius: 12px;
  color: #2f855a;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
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

/* --- 统计卡片样式 --- */
.stats-cards {
  display: flex;
  justify-content: space-around;
  padding: 20px 10px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 8px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 12px;
  flex: 1;
  min-width: 60px;
}

.stat-card:hover {
  background: rgba(30, 136, 229, 0.05);
  transform: translateY(-2px);
}

.stat-number {
  font-size: 23px;
  font-weight: 700;
  color: #1e88e5;
}

.stats-cards .stat-card:nth-child(1) .stat-number { color: #43a047; }
.stats-cards .stat-card:nth-child(2) .stat-number { color: #f59e0b; }
.stats-cards .stat-card:nth-child(3) .stat-number { color: #1e88e5; }
.stats-cards .stat-card:nth-child(4) .stat-number { color: #43a047; }
.stats-cards .stat-card:nth-child(5) .stat-number { color: #e53935; }

.stat-label {
  font-size: 14px;
  color: #888888;
}

/* --- 筛选面板 --- */
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
.filter-option.active {
  background: #e3f2fd;
  border-color: #1e88e5;
  color: #1e88e5;
}

.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-10px); }

/* --- 列表布局 (新 - 矢量图标优化版) --- */
.reservations-list {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片基础样式 */
.reservation-card {
  background: white;
  border-radius: 16px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.reservation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

/* 左侧彩色装饰条 */
.reservation-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: #e2e8f0;
}

/* 头部布局 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 头部图标 - 蓝色主题 */
.room-icon {
  color: #1e88e5;
  margin-right: 2px;
}

.room-name {
  font-size: 17px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

/* 状态标签 */
.status-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 内容主体 */
.card-body {
  padding: 0 20px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.time-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.date-row, .time-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-icon {
  color: #a0aec0; /* 灰色图标 */
  flex-shrink: 0;
}

.time-box .date {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
}

.time-box .time {
  font-size: 13px;
  color: #718096;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 社区标签 */
.community-tag {
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-icon {
  color: #94a3b8;
}

/* 虚线分割 */
.divider {
  height: 1px;
  background-image: linear-gradient(to right, #e2e8f0 50%, rgba(255, 255, 255, 0) 0%);
  background-position: bottom;
  background-size: 8px 1px;
  background-repeat: repeat-x;
  margin: 0 20px;
}

/* 底部区域 */
.card-footer {
  padding: 12px 20px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.activity-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4a5568;
  flex: 1;
  min-width: 150px;
}

.label-icon {
  color: #cbd5e0;
  flex-shrink: 0;
}

.activity-summary .value {
  font-weight: 500;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #2d3748;
}

.dept-badge {
  font-size: 11px;
  background: #edf2f7;
  color: #718096;
  padding: 1px 6px;
  border-radius: 4px;
}

/* 按钮组 */
.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-mini {
  border: none;
  padding: 6px 14px;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-mini.cancel {
  background: #fff;
  border: 1px solid #fed7d7;
  color: #e53e3e;
}
.btn-mini.cancel:hover { background: #fff5f5; }

.btn-mini.detail {
  background: #edf2f7;
  color: #4a5568;
}
.btn-mini.detail:hover {
  background: #e2e8f0;
  color: #2d3748;
}

/* 状态配色方案 (侧边条 + 标签) */
.reservation-card.pending-audit::before { background: #f59e0b; }
.status-tag.pending-audit { background: #fef3c7; color: #d97706; }

.reservation-card.ongoing::before { background: #10b981; }
.status-tag.ongoing { background: #d1fae5; color: #065f46; }

.reservation-card.rejected::before { background: #ef4444; }
.status-tag.rejected { background: #fee2e2; color: #dc2626; }

.reservation-card.cancelled::before { background: #94a3b8; }
.status-tag.cancelled { background: #f1f5f9; color: #64748b; }

.reservation-card.completed::before { background: #3b82f6; }
.status-tag.completed { background: #dbeafe; color: #1e40af; }

/* --- 空状态 & 加载状态 --- */
.loading-container, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
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

@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.loading-text, .empty-desc { color: #6b7280; font-size: 16px; }
.empty-title { font-size: 20px; font-weight: 700; color: #2d3748; margin-bottom: 8px; }

.explore-button {
  padding: 12px 24px;
  background: #43a047;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
}
.explore-button:hover { background: #388e3c; }

/* --- 弹窗样式 --- */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
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

.modal-icon { font-size: 48px; margin-bottom: 16px; }
.confirm-modal h3 { font-size: 20px; font-weight: 700; color: #2d3748; margin-bottom: 8px; }
.confirm-modal p { color: #718096; margin-bottom: 16px; line-height: 1.5; }

.warning-text {
  font-size: 14px;
  color: #e53e3e;
  background: #fef2f2;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #fecaca;
}

.modal-actions { display: flex; gap: 12px; margin-top: 24px; }
.modal-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
}
.modal-btn.cancel { background: #f7fafc; color: #4a5568; }
.modal-btn.confirm { background: #ef4444; color: white; }

/* 响应式 */
@media (max-width: 480px) {
  .card-footer {
    flex-direction: column;
    align-items: flex-start;
  }
  .action-buttons {
    width: 100%;
    justify-content: flex-end;
    margin-top: 4px;
  }
}
</style>
