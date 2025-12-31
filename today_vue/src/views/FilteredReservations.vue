<!--<template>-->
<!--  <div class="filtered-reservations-container">-->
<!--    &lt;!&ndash; 顶部导航 &ndash;&gt;-->
<!--    <div class="detail-header">-->
<!--      <div class="back-button" @click="goBack">-->
<!--        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--          <path d="M19 12H5M12 19l-7-7 7-7"/>-->
<!--        </svg>-->
<!--      </div>-->
<!--      <div class="header-title">{{ pageTitle }}</div>-->
<!--      <div class="header-actions"></div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 筛选栏 &ndash;&gt;-->
<!--    <div class="filter-bar">-->
<!--      <div class="section-title">{{ pageTitle }}</div>-->
<!--      <div class="record-count" :class="{ zero: filteredReservations.length === 0 }">-->
<!--        {{ filteredReservations.length }}条记录-->
<!--      </div>-->
<!--    </div>-->


<!--    &lt;!&ndash; 加载状态 &ndash;&gt;-->
<!--    <div v-if="loading" class="loading-container">-->
<!--      <div class="loading-spinner"></div>-->
<!--      <div class="loading-text">加载中...</div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 空状态 &ndash;&gt;-->
<!--    <div v-else-if="filteredReservations.length === 0" class="empty-state">-->
<!--      <div class="empty-image">-->
<!--        <img src="https://via.placeholder.com/120x120?text=暂无预约" alt="暂无预约" />-->
<!--      </div>-->
<!--      <h3 class="empty-title">{{ emptyTitle }}</h3>-->
<!--      <p class="empty-desc">{{ emptyDescription }}</p>-->
<!--      <button class="explore-button" @click="goToCommunity">-->
<!--        去预约-->
<!--      </button>-->
<!--    </div>-->

<!--    &lt;!&ndash; 预约列表 - 新样式 &ndash;&gt;-->
<!--    <div v-else class="reservations-list">-->
<!--      <div-->
<!--          v-for="reservation in filteredReservations"-->
<!--          :key="reservation.id"-->
<!--          class="reservation-card"-->
<!--          :class="getStatusClass(reservation.status)"-->
<!--      >-->
<!--        &lt;!&ndash; 状态标签：悬浮在右上角 &ndash;&gt;-->
<!--        <div class="status-badge">-->
<!--          {{ getStatusText(reservation.status) }}-->
<!--        </div>-->

<!--        &lt;!&ndash; 卡片主要内容 &ndash;&gt;-->
<!--        <div class="card-main-content" @click="viewReservationDetail(reservation)">-->
<!--          &lt;!&ndash; 预约头部 &ndash;&gt;-->
<!--          <div class="reservation-header">-->
<!--            <h3 class="room-name">{{ reservation.roomName }}</h3>-->
<!--          </div>-->

<!--          &lt;!&ndash; 预约信息 &ndash;&gt;-->
<!--          <div class="reservation-info">-->
<!--            <div class="info-item">-->
<!--              <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>-->
<!--                <line x1="16" y1="2" x2="16" y2="6"></line>-->
<!--                <line x1="8" y1="2" x2="8" y2="6"></line>-->
<!--                <line x1="3" y1="10" x2="21" y2="10"></line>-->
<!--              </svg>-->
<!--              <span class="info-text">{{ formatDate(reservation.reservationDate) }}</span>-->
<!--            </div>-->
<!--            <div class="info-item">-->
<!--              <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--                <circle cx="12" cy="12" r="10"></circle>-->
<!--                <polyline points="12 6 12 12 16 14"></polyline>-->
<!--              </svg>-->
<!--              <span class="info-text">{{ reservation.startTime }} - {{ reservation.endTime }}</span>-->
<!--            </div>-->
<!--            <div class="info-item">-->
<!--              <span class="info-text community-text">{{ reservation.communityName }}</span>-->
<!--            </div>-->
<!--          </div>-->

<!--          &lt;!&ndash; 统一宽度容器：活动信息 + 按钮 &ndash;&gt;-->
<!--          <div class="content-action-wrapper">-->
<!--            &lt;!&ndash; 活动信息 &ndash;&gt;-->
<!--            <div class="activity-info">-->
<!--              <div class="activity-name">{{ reservation.activityName }}</div>-->
<!--              <div class="department">{{ reservation.department }}</div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->

<!--        &lt;!&ndash; 操作按钮：和活动信息同宽度容器 &ndash;&gt;-->
<!--        <div class="content-action-wrapper">-->
<!--          <div class="action-buttons">-->
<!--            <button-->
<!--                v-if="canCancelReservation(reservation)"-->
<!--                class="action-btn cancel-btn"-->
<!--                @click="handleCancelReservation(reservation)"-->
<!--            >-->
<!--              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--                <line x1="18" y1="6" x2="6" y2="18"></line>-->
<!--                <line x1="6" y1="6" x2="18" y2="18"></line>-->
<!--              </svg>-->
<!--              取消预约-->
<!--            </button>-->
<!--            <button-->
<!--                class="action-btn detail-btn"-->
<!--                @click="viewReservationDetail(reservation)"-->
<!--            >-->
<!--              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>-->
<!--                <circle cx="12" cy="12" r="3"></circle>-->
<!--              </svg>-->
<!--              详情-->
<!--            </button>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 取消预约确认弹窗 &ndash;&gt;-->
<!--    <div v-if="showCancelConfirm" class="modal-overlay" @click="showCancelConfirm = false">-->
<!--      <div class="modal-content confirm-modal" @click.stop>-->
<!--        <div class="modal-icon">⚠️</div>-->
<!--        <h3>取消预约</h3>-->
<!--        <p>确定要取消预约「{{ selectedReservation?.roomName }}」吗？</p>-->
<!--        <p class="warning-text">注意：频繁取消可能会影响您的预约权限</p>-->
<!--        <div class="modal-actions">-->
<!--          <button class="modal-btn cancel" @click="showCancelConfirm = false">再想想</button>-->
<!--          <button class="modal-btn confirm" @click="confirmCancel">确定取消</button>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed, onMounted, watch } from 'vue'-->
<!--import { useRouter, useRoute } from 'vue-router'-->
<!--import { ElMessage, ElDatePicker } from 'element-plus'-->
<!--import { getMyReservations, cancelReservation } from '@/api/reservations.js'-->
<!--// 引入Element Plus样式-->
<!--import 'element-plus/dist/index.css'-->

<!--const router = useRouter()-->
<!--const route = useRoute()-->

<!--// 状态-->
<!--const loading = ref(true)-->
<!--const showCancelConfirm = ref(false)-->
<!--const selectedReservation = ref(null)-->

<!--// 时间筛选相关状态-->
<!--const timeFilter = ref('all') // all/today/week/month/custom-->
<!--const showCustomPicker = ref(false)-->
<!--const dateRange = ref([]) // 存储日期范围 [开始日期, 结束日期]-->

<!--// 预约数据-->
<!--const reservations = ref([])-->

<!--// 页面标题和空状态配置-->
<!--const pageConfig = {-->
<!--  '0': { title: '待审核预约', emptyTitle: '暂无待审核预约', emptyDesc: '您当前没有待审核的预约记录' },-->
<!--  '1': { title: '已通过预约', emptyTitle: '暂无进行中预约', emptyDesc: '您当前没有进行中的预约记录' },-->
<!--  '2': { title: '已拒绝预约', emptyTitle: '暂无被退回预约', emptyDesc: '您当前没有被退回的预约记录' },-->
<!--  '3': { title: '已取消预约', emptyTitle: '暂无已取消预约', emptyDesc: '您当前没有已取消的预约记录' },-->
<!--  '4': { title: '已完成预约', emptyTitle: '暂无已完成预约', emptyDesc: '您当前没有已完成的预约记录' }-->
<!--}-->

<!--// 计算属性-->
<!--const pageTitle = computed(() => {-->
<!--  return pageConfig[route.params.status]?.title || '预约列表'-->
<!--})-->

<!--const emptyTitle = computed(() => {-->
<!--  return pageConfig[route.params.status]?.emptyTitle || '暂无预约记录'-->
<!--})-->

<!--const emptyDescription = computed(() => {-->
<!--  return pageConfig[route.params.status]?.emptyDesc || '快去预约您心仪的教室吧'-->
<!--})-->

<!--// 筛选后的预约列表-->
<!--const filteredReservations = computed(() => {-->
<!--  if (reservations.value.length === 0) return []-->

<!--  return reservations.value.filter(reservation => {-->
<!--    const reservationDate = new Date(reservation.reservationDate)-->

<!--    // 根据不同时间筛选条件过滤-->
<!--    switch (timeFilter.value) {-->
<!--      case 'all':-->
<!--        return true-->
<!--      case 'today':-->
<!--        return isSameDay(reservationDate, new Date())-->
<!--      case 'week':-->
<!--        return isSameWeek(reservationDate, new Date())-->
<!--      case 'month':-->
<!--        return isSameMonth(reservationDate, new Date())-->
<!--      case 'custom':-->
<!--        if (!dateRange.value || !dateRange.value.length) return true-->
<!--        const start = new Date(dateRange.value[0])-->
<!--        const end = new Date(dateRange.value[1])-->
<!--        // 设置结束日期为当天结束时间-->
<!--        end.setHours(23, 59, 59, 999)-->
<!--        return reservationDate >= start && reservationDate <= end-->
<!--      default:-->
<!--        return true-->
<!--    }-->
<!--  })-->
<!--})-->

<!--// 方法-->
<!--const loadReservations = async () => {-->
<!--  try {-->
<!--    loading.value = true-->
<!--    const status = route.params.status ? parseInt(route.params.status) : null-->
<!--    const response = await getMyReservations(status)-->

<!--    if (response.code === 200) {-->
<!--      reservations.value = response.data || []-->
<!--    } else {-->
<!--      throw new Error(response.message || '加载失败')-->
<!--    }-->
<!--  } catch (error) {-->
<!--    console.error('加载预约列表失败:', error)-->
<!--    ElMessage.error('加载预约列表失败')-->
<!--  } finally {-->
<!--    loading.value = false-->
<!--  }-->
<!--}-->

<!--// 设置时间筛选条件-->
<!--const setTimeFilter = (type) => {-->
<!--  timeFilter.value = type-->
<!--  // 清除自定义时间（非自定义筛选时）-->
<!--  if (type !== 'custom') {-->
<!--    dateRange.value = []-->
<!--    showCustomPicker.value = false-->
<!--  }-->
<!--}-->

<!--// 切换自定义日期选择器显示/隐藏-->
<!--const toggleCustomPicker = () => {-->
<!--  if (timeFilter.value === 'custom') {-->
<!--    showCustomPicker.value = !showCustomPicker.value-->
<!--  } else {-->
<!--    timeFilter.value = 'custom'-->
<!--    showCustomPicker.value = true-->
<!--  }-->
<!--}-->

<!--// 处理日期范围变化-->
<!--const handleDateRangeChange = (val) => {-->
<!--  if (val && val.length) {-->
<!--    dateRange.value = val-->
<!--    ElMessage.success(`已筛选 ${formatDateStr(val[0])} 至 ${formatDateStr(val[1])} 的预约`)-->
<!--  }-->
<!--}-->

<!--// 清除日期范围-->
<!--const clearDateRange = () => {-->
<!--  dateRange.value = []-->
<!--  ElMessage.info('已清除日期筛选条件')-->
<!--}-->

<!--// 禁用日期函数（这里不禁用任何日期，支持选择未来日期）-->
<!--const disabledDate = (date) => {-->
<!--  // 返回false表示不禁用任何日期，支持选择过去和未来的日期-->
<!--  // 如果需要限制只能选择未来日期，可以使用：return date < new Date(new Date().setHours(0, 0, 0, 0))-->
<!--  return false-->
<!--}-->

<!--// 日期工具函数-->
<!--const isSameDay = (date1, date2) => {-->
<!--  return (-->
<!--      date1.getFullYear() === date2.getFullYear() &&-->
<!--      date1.getMonth() === date2.getMonth() &&-->
<!--      date1.getDate() === date2.getDate()-->
<!--  )-->
<!--}-->

<!--const isSameWeek = (date1, date2) => {-->
<!--  // 计算本周第一天（周一）-->
<!--  const getWeekStart = (date) => {-->
<!--    const d = new Date(date)-->
<!--    const day = d.getDay() || 7 // 周日转为7-->
<!--    d.setDate(d.getDate() - day + 1)-->
<!--    d.setHours(0, 0, 0, 0)-->
<!--    return d-->
<!--  }-->

<!--  const weekStart1 = getWeekStart(date1)-->
<!--  const weekStart2 = getWeekStart(date2)-->

<!--  return isSameDay(weekStart1, weekStart2)-->
<!--}-->

<!--const isSameMonth = (date1, date2) => {-->
<!--  return (-->
<!--      date1.getFullYear() === date2.getFullYear() &&-->
<!--      date1.getMonth() === date2.getMonth()-->
<!--  )-->
<!--}-->

<!--// 格式化日期字符串（YYYY-MM-DD 转 YYYY年MM月DD日）-->
<!--const formatDateStr = (dateStr) => {-->
<!--  if (!dateStr) return ''-->
<!--  const date = new Date(dateStr)-->
<!--  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`-->
<!--}-->

<!--// 状态样式映射 - 新样式的状态类名-->
<!--const getStatusClass = (status) => {-->
<!--  const classMap = {-->
<!--    0: 'pending-audit', // 待审核-->
<!--    1: 'ongoing',       // 进行中/已通过-->
<!--    2: 'rejected',      // 已拒绝-->
<!--    3: 'cancelled',     // 已取消-->
<!--    4: 'completed'      // 已完成-->
<!--  }-->
<!--  return classMap[status] || 'pending-audit'-->
<!--}-->

<!--const getStatusText = (status) => {-->
<!--  const textMap = {-->
<!--    0: '待审核',-->
<!--    1: '进行中',-->
<!--    2: '已拒绝',-->
<!--    3: '已取消',-->
<!--    4: '已完成'-->
<!--  }-->
<!--  return textMap[status] || '未知状态'-->
<!--}-->

<!--const formatDate = (dateStr) => {-->
<!--  const date = new Date(dateStr)-->
<!--  const today = new Date()-->
<!--  const tomorrow = new Date(today)-->
<!--  tomorrow.setDate(today.getDate() + 1)-->

<!--  if (date.toDateString() === today.toDateString()) {-->
<!--    return '今天'-->
<!--  } else if (date.toDateString() === tomorrow.toDateString()) {-->
<!--    return '明天'-->
<!--  } else {-->
<!--    return `${date.getMonth() + 1}月${date.getDate()}日`-->
<!--  }-->
<!--}-->

<!--// 新增：检查是否可以取消预约-->
<!--const canCancelReservation = (reservation) => {-->
<!--  // 只有待审核(0)和已通过(1)状态的预约可以取消-->
<!--  if (reservation.status !== 0 && reservation.status !== 1) {-->
<!--    return false-->
<!--  }-->

<!--  // 获取当前时间-->
<!--  const now = new Date()-->

<!--  // 构建预约开始时间（预约日期 + 开始时间）-->
<!--  const reservationDate = new Date(reservation.reservationDate)-->
<!--  const [startHour, startMinute] = reservation.startTime.split(':').map(Number)-->
<!--  const startDateTime = new Date(reservationDate)-->
<!--  startDateTime.setHours(startHour, startMinute, 0, 0)-->

<!--  // 计算距离预约开始时间还有多少分钟-->
<!--  const timeDiff = startDateTime.getTime() - now.getTime()-->
<!--  const minutesDiff = Math.floor(timeDiff / (1000 * 60))-->

<!--  // 如果距离开始时间小于等于5分钟，不能取消-->
<!--  return minutesDiff > 5-->
<!--}-->

<!--const viewReservationDetail = (reservation) => {-->
<!--  router.push(`/reservation-detail/${reservation.id}`)-->
<!--}-->

<!--const handleCancelReservation = (reservation) => {-->
<!--  // 再次检查是否可以取消-->
<!--  if (!canCancelReservation(reservation)) {-->
<!--    ElMessage.warning('距离预约开始时间不足5分钟，无法取消')-->
<!--    return-->
<!--  }-->

<!--  selectedReservation.value = reservation-->
<!--  showCancelConfirm.value = true-->
<!--}-->

<!--const confirmCancel = async () => {-->
<!--  if (!selectedReservation.value) return-->

<!--  try {-->
<!--    const response = await cancelReservation(selectedReservation.value.reservationNo)-->
<!--    if (response.code === 200) {-->
<!--      ElMessage.success('取消预约成功')-->
<!--      // 重新加载数据-->
<!--      loadReservations()-->
<!--    } else {-->
<!--      throw new Error(response.message || '取消预约失败')-->
<!--    }-->
<!--  } catch (error) {-->
<!--    console.error('取消预约失败:', error)-->
<!--    ElMessage.error('取消预约失败')-->
<!--  } finally {-->
<!--    showCancelConfirm.value = false-->
<!--    selectedReservation.value = null-->
<!--  }-->
<!--}-->

<!--const goToCommunity = () => {-->
<!--  router.push('/community-list')-->
<!--}-->

<!--const goBack = () => {-->
<!--  router.back()-->
<!--}-->

<!--// 监听路由参数变化-->
<!--watch(-->
<!--    () => route.params.status,-->
<!--    () => {-->
<!--      loadReservations()-->
<!--    }-->
<!--)-->

<!--// 初始化-->
<!--onMounted(() => {-->
<!--  loadReservations()-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--.filtered-reservations-container {-->
<!--  min-height: 100vh;-->
<!--  background: #ffffff;-->
<!--  padding-bottom: 24px;-->
<!--}-->

<!--/* 顶部导航 */-->
<!--.detail-header {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: space-between;-->
<!--  padding: 16px 20px;-->
<!--  background: white;-->
<!--  border-bottom: 1px solid #e2e8f0;-->
<!--  position: sticky;-->
<!--  top: 0;-->
<!--  z-index: 100;-->
<!--}-->

<!--.back-button {-->
<!--  width: 40px;-->
<!--  height: 40px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  cursor: pointer;-->
<!--  border-radius: 10px;-->
<!--  transition: all 0.3s ease;-->
<!--  color: #4a5568;-->
<!--}-->

<!--.back-button:hover {-->
<!--  background: #f7fafc;-->
<!--  transform: scale(1.05);-->
<!--}-->

<!--.header-title {-->
<!--  font-size: 18px;-->
<!--  font-weight: 700;-->
<!--  color: #2d3748;-->
<!--}-->

<!--.header-actions {-->
<!--  width: 40px; /* 保持对称 */-->
<!--}-->

<!--/* 筛选栏 */-->
<!--.filter-bar {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  padding: 16px 20px;-->
<!--  background: #ffffff;-->
<!--  border-bottom: 1px solid #f0f0f0;-->
<!--}-->

<!--.section-title {-->
<!--  font-size: 18px;-->
<!--  font-weight: 700;-->
<!--  color: #333333;-->
<!--}-->

<!--.record-count {-->
<!--  padding: 4px 12px;-->
<!--  background: #f0fff4;-->
<!--  border-radius: 12px;-->
<!--  color: #2f855a;-->
<!--  font-size: 14px;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.record-count.zero {-->
<!--  background: #fef2f2;-->
<!--  color: #dc2626;-->
<!--}-->

<!--/* 时间筛选器样式 */-->
<!--.time-filter-container {-->
<!--  background: white;-->
<!--  padding: 16px 20px;-->
<!--  border-bottom: 1px solid #f0f0f0;-->
<!--}-->

<!--.time-filter-header {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 12px;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.filter-label {-->
<!--  font-size: 14px;-->
<!--  color: #4a5568;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.time-filter-buttons {-->
<!--  display: flex;-->
<!--  gap: 8px;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.time-filter-btn {-->
<!--  padding: 8px 16px;-->
<!--  background: #f8fafc;-->
<!--  border: 1px solid #e2e8f0;-->
<!--  border-radius: 8px;-->
<!--  font-size: 14px;-->
<!--  color: #4a5568;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s ease;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.time-filter-btn.active {-->
<!--  background: #1e88e5;-->
<!--  color: white;-->
<!--  border-color: #1e88e5;-->
<!--  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);-->
<!--}-->

<!--.time-filter-btn:hover:not(.active) {-->
<!--  background: #f1f5f9;-->
<!--  border-color: #cbd5e1;-->
<!--}-->

<!--/* 自定义日期选择器容器 */-->
<!--.custom-date-picker-container {-->
<!--  margin-top: 16px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 12px;-->
<!--}-->

<!--/* 清除日期按钮 */-->
<!--.clear-date-btn {-->
<!--  padding: 8px 16px;-->
<!--  background: #fef2f2;-->
<!--  color: #dc2626;-->
<!--  border: 1px solid #fecaca;-->
<!--  border-radius: 8px;-->
<!--  font-size: 14px;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s ease;-->
<!--}-->

<!--.clear-date-btn:hover {-->
<!--  background: #fee2e2;-->
<!--}-->

<!--/* 加载状态 */-->
<!--.loading-container {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  padding: 80px 20px;-->
<!--}-->

<!--.loading-spinner {-->
<!--  width: 50px;-->
<!--  height: 50px;-->
<!--  border: 4px solid #f3f4f6;-->
<!--  border-top: 4px solid #1e88e5;-->
<!--  border-radius: 50%;-->
<!--  animation: spin 1s linear infinite;-->
<!--  margin-bottom: 20px;-->
<!--}-->

<!--@keyframes spin {-->
<!--  0% { transform: rotate(0deg); }-->
<!--  100% { transform: rotate(360deg); }-->
<!--}-->

<!--.loading-text {-->
<!--  color: #6b7280;-->
<!--  font-size: 16px;-->
<!--  font-weight: 500;-->
<!--}-->

<!--/* 空状态 */-->
<!--.empty-state {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  padding: 80px 20px;-->
<!--  text-align: center;-->
<!--}-->

<!--.empty-image {-->
<!--  width: 120px;-->
<!--  height: 120px;-->
<!--  margin-bottom: 24px;-->
<!--}-->

<!--.empty-image img {-->
<!--  width: 100%;-->
<!--  height: 100%;-->
<!--  object-fit: contain;-->
<!--}-->

<!--.empty-title {-->
<!--  font-size: 20px;-->
<!--  font-weight: 700;-->
<!--  color: #2d3748;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.empty-desc {-->
<!--  color: #718096;-->
<!--  margin-bottom: 32px;-->
<!--  font-size: 15px;-->
<!--}-->

<!--.explore-button {-->
<!--  padding: 12px 24px;-->
<!--  background: #43a047;-->
<!--  color: white;-->
<!--  border: none;-->
<!--  border-radius: 8px;-->
<!--  font-weight: 600;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.explore-button:hover {-->
<!--  background: #388e3c;-->
<!--}-->

<!--/* 预约列表 - 新样式 */-->
<!--.reservations-list {-->
<!--  padding: 20px;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  gap: 16px;-->
<!--}-->

<!--.reservation-card {-->
<!--  background: white;-->
<!--  border-radius: 16px;-->
<!--  padding: 20px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);-->
<!--  transition: all 0.3s ease;-->
<!--  position: relative; /* 为状态标签提供定位参考 */-->
<!--}-->

<!--.reservation-card:hover {-->
<!--  transform: translateY(-2px);-->
<!--  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);-->
<!--}-->

<!--/* 状态标签样式 */-->
<!--.status-badge {-->
<!--  position: absolute;-->
<!--  top: 20px;-->
<!--  right: 20px;-->
<!--  padding: 4px 12px;-->
<!--  border-radius: 12px;-->
<!--  font-size: 12px;-->
<!--  font-weight: 600;-->
<!--  z-index: 2;-->
<!--  white-space: nowrap;-->
<!--  box-sizing: border-box;-->
<!--  backdrop-filter: blur(2px);-->
<!--  -webkit-backdrop-filter: blur(2px);-->
<!--}-->

<!--/* 卡片主要内容 */-->
<!--.card-main-content {-->
<!--  cursor: pointer;-->
<!--  width: 100%;-->
<!--  box-sizing: border-box;-->
<!--}-->

<!--/* 预约头部 */-->
<!--.reservation-header {-->
<!--  margin-bottom: 16px;-->
<!--}-->

<!--.room-name {-->
<!--  font-size: 18px;-->
<!--  font-weight: 700;-->
<!--  color: #2d3748;-->
<!--  margin: 0;-->
<!--}-->

<!--/* 预约信息 */-->
<!--.reservation-info {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  gap: 12px;-->
<!--  margin-bottom: 16px;-->
<!--  width: 100%;-->
<!--}-->

<!--.info-item {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  width: 100%;-->
<!--}-->

<!--.info-icon {-->
<!--  color: #1e88e5;-->
<!--  width: 16px;-->
<!--  height: 16px;-->
<!--  flex-shrink: 0;-->
<!--  margin-right: 8px;-->
<!--}-->

<!--.info-text {-->
<!--  font-size: 14px;-->
<!--  color: #4a5568;-->
<!--  line-height: 1.4;-->
<!--}-->

<!--.community-text {-->
<!--  display: block;-->
<!--  padding-left: 24px;-->
<!--  width: 100%;-->
<!--  box-sizing: border-box;-->
<!--}-->

<!--/* 统一宽度容器：核心！确保活动信息和按钮宽度一致 */-->
<!--.content-action-wrapper {-->
<!--  width: 100%;-->
<!--  box-sizing: border-box;-->
<!--  padding: 0;-->
<!--  margin: 0;-->
<!--}-->

<!--/* 活动信息：基于统一容器的100%宽度 */-->
<!--.activity-info {-->
<!--  padding: 12px;-->
<!--  background: #f7fafc;-->
<!--  border-radius: 8px;-->
<!--  width: 100%;-->
<!--  box-sizing: border-box;-->
<!--  margin: 0 0 16px 0; /* 与按钮保持间距 */-->
<!--}-->

<!--.activity-name {-->
<!--  font-size: 14px;-->
<!--  font-weight: 600;-->
<!--  color: #2d3748;-->
<!--  margin: 0 0 4px 0;-->
<!--}-->

<!--.department {-->
<!--  font-size: 12px;-->
<!--  color: #718096;-->
<!--  margin: 0;-->
<!--}-->

<!--/* 操作按钮容器：基于统一容器的100%宽度 */-->
<!--.action-buttons {-->
<!--  display: flex;-->
<!--  gap: 8px;-->
<!--  width: 100%;-->
<!--  box-sizing: border-box;-->
<!--}-->

<!--.action-btn {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 6px;-->
<!--  padding: 8px 16px;-->
<!--  border: none;-->
<!--  border-radius: 8px;-->
<!--  font-size: 13px;-->
<!--  font-weight: 500;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--  flex: 1;-->
<!--  justify-content: center;-->
<!--  min-width: 80px;-->
<!--}-->

<!--.cancel-btn {-->
<!--  background: #fef2f2;-->
<!--  color: #dc2626;-->
<!--  border: 1px solid #fecaca;-->
<!--}-->

<!--.cancel-btn:hover {-->
<!--  background: #fee2e2;-->
<!--  transform: translateY(-1px);-->
<!--}-->

<!--.detail-btn {-->
<!--  background: #f8fafc;-->
<!--  color: #475569;-->
<!--  border: 1px solid #e2e8f0;-->
<!--}-->

<!--.detail-btn:hover {-->
<!--  background: #f1f5f9;-->
<!--  transform: translateY(-1px);-->
<!--}-->

<!--/* 状态样式 */-->
<!--.reservation-card.pending-audit {-->
<!--  border-left: 4px solid #f59e0b;-->
<!--}-->
<!--.reservation-card.ongoing {-->
<!--  border-left: 4px solid #10b981;-->
<!--}-->
<!--.reservation-card.rejected {-->
<!--  border-left: 4px solid #ef4444;-->
<!--}-->
<!--.reservation-card.cancelled {-->
<!--  border-left: 4px solid #6b7280;-->
<!--}-->
<!--.reservation-card.completed {-->
<!--  border-left: 4px solid #3b82f6;-->
<!--}-->

<!--/* 状态标签背景色 */-->
<!--.reservation-card.pending-audit .status-badge {-->
<!--  background: #fef3c7;-->
<!--  color: #d97706;-->
<!--}-->
<!--.reservation-card.ongoing .status-badge {-->
<!--  background: #d1fae5;-->
<!--  color: #065f46;-->
<!--}-->
<!--.reservation-card.rejected .status-badge {-->
<!--  background: #fee2e2;-->
<!--  color: #dc2626;-->
<!--}-->
<!--.reservation-card.cancelled .status-badge {-->
<!--  background: #f3f4f6;-->
<!--  color: #6b7280;-->
<!--}-->
<!--.reservation-card.completed .status-badge {-->
<!--  background: #dbeafe;-->
<!--  color: #1e40af;-->
<!--}-->

<!--/* 弹窗样式 */-->
<!--.modal-overlay {-->
<!--  position: fixed;-->
<!--  top: 0;-->
<!--  left: 0;-->
<!--  right: 0;-->
<!--  bottom: 0;-->
<!--  background: rgba(0, 0, 0, 0.6);-->
<!--  backdrop-filter: blur(5px);-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  z-index: 1000;-->
<!--  padding: 20px;-->
<!--}-->

<!--.confirm-modal {-->
<!--  background: white;-->
<!--  border-radius: 20px;-->
<!--  padding: 32px 24px;-->
<!--  text-align: center;-->
<!--  max-width: 320px;-->
<!--  width: 100%;-->
<!--}-->

<!--.modal-icon {-->
<!--  font-size: 48px;-->
<!--  margin-bottom: 16px;-->
<!--}-->

<!--.confirm-modal h3 {-->
<!--  font-size: 20px;-->
<!--  font-weight: 700;-->
<!--  color: #2d3748;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.confirm-modal p {-->
<!--  color: #718096;-->
<!--  margin-bottom: 16px;-->
<!--  line-height: 1.5;-->
<!--}-->

<!--.warning-text {-->
<!--  font-size: 14px;-->
<!--  color: #e53e3e;-->
<!--  background: #fef2f2;-->
<!--  padding: 12px;-->
<!--  border-radius: 8px;-->
<!--  border-left: 4px solid #fecaca;-->
<!--}-->

<!--.modal-actions {-->
<!--  display: flex;-->
<!--  gap: 12px;-->
<!--  margin-top: 24px;-->
<!--}-->

<!--.modal-btn {-->
<!--  flex: 1;-->
<!--  padding: 12px 20px;-->
<!--  border: none;-->
<!--  border-radius: 12px;-->
<!--  font-weight: 600;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--}-->

<!--.modal-btn.cancel {-->
<!--  background: #f7fafc;-->
<!--  color: #4a5568;-->
<!--  border: 1px solid #e2e8f0;-->
<!--}-->

<!--.modal-btn.cancel:hover {-->
<!--  background: #edf2f7;-->
<!--}-->

<!--.modal-btn.confirm {-->
<!--  background: #ef4444;-->
<!--  color: white;-->
<!--}-->

<!--.modal-btn.confirm:hover {-->
<!--  background: #dc2626;-->
<!--}-->

<!--/* 自定义日期选择器样式 */-->
<!--:deep(.custom-date-picker) {-->
<!--  &#45;&#45;el-color-primary: #1e88e5;-->
<!--  &#45;&#45;el-color-primary-light-3: #64b5f6;-->
<!--  &#45;&#45;el-color-primary-light-5: #90caf9;-->
<!--  &#45;&#45;el-color-primary-light-7: #bbdefb;-->
<!--  &#45;&#45;el-color-primary-light-8: #e3f2fd;-->
<!--  &#45;&#45;el-color-primary-light-9: #f5fafe;-->
<!--}-->

<!--:deep(.el-date-picker) {-->
<!--  width: 100%;-->
<!--  max-width: 400px;-->
<!--}-->

<!--:deep(.el-input__wrapper) {-->
<!--  border-radius: 8px;-->
<!--  padding: 8px 12px;-->
<!--  border: 1px solid #e2e8f0;-->
<!--  box-shadow: none;-->
<!--}-->

<!--:deep(.el-input__wrapper:focus) {-->
<!--  border-color: #1e88e5;-->
<!--  box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.2);-->
<!--}-->

<!--:deep(.el-date-range-picker__header) {-->
<!--  background: #f8fafc;-->
<!--  border-bottom: 1px solid #e2e8f0;-->
<!--}-->

<!--:deep(.el-date-table td.today) {-->
<!--  color: #1e88e5;-->
<!--  font-weight: bold;-->
<!--}-->

<!--:deep(.el-date-table td.in-range) {-->
<!--  background: #e3f2fd;-->
<!--}-->

<!--:deep(.el-date-table td.start-date),-->
<!--:deep(.el-date-table td.end-date) {-->
<!--  background: #1e88e5;-->
<!--  color: white;-->
<!--}-->

<!--/* 响应式适配 */-->
<!--@media (max-width: 768px) {-->
<!--  .reservations-list {-->
<!--    padding: 12px;-->
<!--  }-->

<!--  .action-buttons {-->
<!--    flex-direction: column;-->
<!--  }-->

<!--  .info-item:nth-child(3) {-->
<!--    padding-left: 20px;-->
<!--  }-->

<!--  .status-badge {-->
<!--    top: 16px;-->
<!--    right: 16px;-->
<!--    max-width: 80px;-->
<!--  }-->

<!--  .time-filter-header {-->
<!--    flex-direction: column;-->
<!--    align-items: flex-start;-->
<!--  }-->

<!--  .custom-date-picker-container {-->
<!--    flex-direction: column;-->
<!--    align-items: stretch;-->
<!--  }-->

<!--  :deep(.el-date-picker) {-->
<!--    width: 100%;-->
<!--  }-->

<!--  .community-text {-->
<!--    padding-left: 20px;-->
<!--  }-->
<!--}-->
<!--</style>-->
