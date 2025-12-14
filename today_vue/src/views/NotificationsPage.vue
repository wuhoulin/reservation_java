<template>
  <div class="notifications-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="back-button" @click="router.back()">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <div class="header-title">消息通知</div>
      <div class="header-actions">
        <button
            v-if="unreadCount > 0"
            class="action-btn"
            @click="handleMarkAllAsRead"
            :disabled="markingAll"
        >
          <span v-if="markingAll">处理中...</span>
          <span v-else>全部已读</span>
        </button>
        <button
            v-if="notifications.length > 0"
            class="action-btn delete-btn"
            @click="handleClearAll"
            :disabled="clearing"
        >
          <span v-if="clearing">处理中...</span>
          <span v-else>清空</span>
        </button>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notifications-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <div class="loading-text">加载中...</div>
      </div>

      <div v-else-if="notifications.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
        </div>
        <div class="empty-title">暂无通知</div>
        <div class="empty-desc">当有新的预约状态变更时，系统会及时通知您</div>
      </div>

      <div v-else class="notifications-list">
        <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-card"
            :class="{ unread: item.isRead === 0 }"
            @click="handleNotificationClick(item)"
        >
          <div class="notification-header">
            <div class="notification-type" :class="getTypeClass(item.type)">
              {{ getTypeText(item.type) }}
            </div>
            <div class="notification-time">
              {{ formatTime(item.createdAt) }}
            </div>
          </div>

          <div class="notification-content">
            <h4 class="notification-title">{{ item.title }}</h4>
            <p class="notification-desc">{{ item.content }}</p>
          </div>

          <div v-if="item.reservationNo" class="notification-footer">
            <span class="reservation-info">
              预约号：{{ item.reservationNo }}
            </span>

          </div>

          <div v-if="item.isRead === 0" class="unread-dot"></div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore && !loadingMore" class="load-more">
          <button @click="handleLoadMore" class="load-more-btn">
            加载更多
          </button>
        </div>

        <div v-if="loadingMore" class="loading-more">
          <div class="mini-spinner"></div>
          <span>加载中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getNotifications,
  markAsRead,
  markAllAsRead as markAllAsReadApi,  // 重命名导入的API函数
  clearAllNotifications
} from '@/api/notification.js'

const router = useRouter()

const loading = ref(true)
const loadingMore = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const markingAll = ref(false)
const clearing = ref(false)

const pageNum = ref(1)
const pageSize = 20
const hasMore = ref(true)

// 加载通知
const loadNotifications = async (isLoadMore = false) => {
  try {
    if (!isLoadMore) {
      loading.value = true
    } else {
      loadingMore.value = true
    }

    const response = await getNotifications(pageNum.value, pageSize)
    if (response.code === 200 && response.data) {
      const data = response.data

      if (isLoadMore) {
        notifications.value = [...notifications.value, ...(data.notifications || [])]
      } else {
        notifications.value = data.notifications || []
      }

      unreadCount.value = data.unreadCount || 0

      // 判断是否还有更多
      hasMore.value = (data.notifications?.length || 0) >= pageSize

      if (!isLoadMore) {
        pageNum.value = 1
      }
    } else {
      throw new Error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
const handleLoadMore = async () => {
  pageNum.value += 1
  await loadNotifications(true)
}

// 处理通知点击
const handleNotificationClick = async (notification) => {
  if (notification.isRead === 0) {
    try {
      await markAsRead(notification.id)
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  if (notification.reservationNo) {
    viewReservationDetail(notification.reservationNo)
  }
}

// 标记全部为已读（重命名以避免冲突）
const handleMarkAllAsRead = async () => {
  try {
    markingAll.value = true
    await markAllAsReadApi() // 使用重命名后的API函数

    // 更新本地状态
    notifications.value.forEach(item => item.isRead = 1)
    unreadCount.value = 0

    ElMessage.success('已标记全部为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('操作失败')
  } finally {
    markingAll.value = false
  }
}

// 清空所有通知（重命名以避免冲突）
const handleClearAll = async () => {
  try {
    clearing.value = true
    await clearAllNotifications()

    notifications.value = []
    unreadCount.value = 0

    ElMessage.success('已清空所有通知')
  } catch (error) {
    console.error('清空通知失败:', error)
    ElMessage.error('操作失败')
  } finally {
    clearing.value = false
  }
}

// 查看预约详情
const viewReservationDetail = (reservationNo) => {
  router.push(`/reservations/detail/${reservationNo}`)
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 一周内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[date.getDay()]
  }

  // 更早
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 获取类型样式
const getTypeClass = (type) => {
  const classMap = {
    1: 'type-success',
    2: 'type-approved',
    3: 'type-rejected',
    4: 'type-cancelled',
    5: 'type-completed',
    6: 'type-system'
  }
  return classMap[type] || 'type-system'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    1: '预约成功',
    2: '审核通过',
    3: '审核拒绝',
    4: '预约取消',
    5: '预约完成',
    6: '系统通知'
  }
  return textMap[type] || '系统通知'
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #4a5568;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover:not(:disabled) {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.action-btn.delete-btn:hover:not(:disabled) {
  background: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.notifications-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #6b7280;
  font-size: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 20px;
  color: #d1d5db;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.5;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  position: relative;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  cursor: pointer;
}

.notification-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.notification-card.unread {
  background: #f0f9ff;
  border-color: #bae6fd;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.notification-type {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}

.type-success {
  background: #d1fae5;
  color: #065f46;
}

.type-approved {
  background: #dbeafe;
  color: #1e40af;
}

.type-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.type-cancelled {
  background: #f3f4f6;
  color: #374151;
}

.type-completed {
  background: #f0fdf4;
  color: #166534;
}

.type-system {
  background: #fef3c7;
  color: #92400e;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
}

.notification-content {
  margin-bottom: 12px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.notification-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.reservation-info {
  font-size: 12px;
  color: #6b7280;
  font-family: 'Courier New', monospace;
  background: #f9fafb;
  padding: 4px 8px;
  border-radius: 4px;
}

.view-detail-btn {
  padding: 6px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.view-detail-btn:hover {
  background: #2563eb;
}

.unread-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.load-more-btn {
  padding: 10px 24px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.load-more-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  color: #6b7280;
  font-size: 14px;
}

.mini-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #f3f4f6;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
</style>
