<template>
  <div class="student-reservations">

    <div class="user-header">
      <div class="user-avatar" @click="goToUserProfile" style="cursor: pointer;">
        <img :src="userInfo.headimgurl || defaultAvatar" alt="头像" />
      </div>
      <div class="user-info">
        <h2 class="user-nickname">{{ userInfo.nickname || '微信用户' }}</h2>
        <p class="user-openid">ID: {{ userInfo.openid || '未知' }}</p>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card" @click="viewAllReservations('all')" style="cursor: pointer;">
        <div class="stat-number">{{ totalReservations }}</div>
        <div class="stat-label">总预约</div>
      </div>
      <div class="stat-card" @click="viewAllReservations('active')" style="cursor: pointer;">
        <div class="stat-number">{{ activeReservations }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card" @click="viewAllReservations('completed')" style="cursor: pointer;">
        <div class="stat-number">{{ completedReservations }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card" @click="viewAllReservations('rejected')" style="cursor: pointer;">
        <div class="stat-number">{{ rejectedReservations }}</div>
        <div class="stat-label">被退回</div>
      </div>
    </div>

    <div class="reservations-section">
      <div class="section-header" @click="viewAllReservations('all')">
        <h3>我的预约</h3>
        <div class="header-right">
          <span class="section-badge">{{ reservations.length }} 条记录</span>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="reservations.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无预约记录</p>
        <button @click="goToCommunityList" class="primary-btn">去预约</button>
      </div>

      <div v-else class="reservations-list">
        <div
            v-for="reservation in reservations"
            :key="reservation.id"
            class="reservation-card"
            :class="getStatusClass(reservation.status)"
        >
          <div class="reservation-header">
            <span class="room-name">{{ reservation.roomName }}</span>
            <span class="status-badge" :class="getStatusClass(reservation.status)">
              {{ getStatusText(reservation.status) }}
            </span>
          </div>

          <div class="reservation-details">
            <div class="detail-item">
              <span class="label">预约时间：</span>
              <span class="value">{{ formatDate(reservation.reservationDate, reservation.startTime, reservation.endTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">时长：</span>
              <span class="value">{{ calculateDuration(reservation.startTime, reservation.endTime) }} 小时</span>
            </div>
            <div class="detail-item" v-if="reservation.activityName">
              <span class="label">用途：</span>
              <span class="value">{{ reservation.activityName }}</span>
            </div>
            <div class="detail-item" v-if="reservation.status === 2 && reservation.auditReason">
              <span class="label">退回原因：</span>
              <span class="value reject-reason">{{ reservation.auditReason }}</span>
            </div>
          </div>

          <div class="reservation-actions" v-if="reservation.status === 0">
            <button @click="cancelReservation(reservation.reservationNo)" class="cancel-btn">
              取消预约
            </button>
          </div>

        </div>
      </div>
    </div>
    <div class="cache-control-section" @click="clearAllCache">
      清除缓存
    </div>
    <div v-if="showMessage" class="message-toast" :class="messageType">
      {{ messageText }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getLatestReservations, cancelUserReservation, resubmitUserReservation } from '@/api/reservations'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const reservations = ref([])
const loading = ref(true)

// 消息提示相关状态
const showMessage = ref(false)
const messageText = ref('')
const messageType = ref('success') // 'success' 或 'error'

// 默认头像
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 计算属性
const totalReservations = computed(() => reservations.value.length)
const activeReservations = computed(() =>
    reservations.value.filter(r => r.status === 0 || r.status === 1).length
)
const completedReservations = computed(() =>
    reservations.value.filter(r => r.status === 3 || r.status === 4).length
)

const rejectedReservations = computed(() =>
    reservations.value.filter(r => r.status === 2).length
)

// 清除缓存方法
const clearAllCache = () => {
  if (!confirm('确定要清除所有本地缓存并重新加载页面吗？这可能有助于解决登录或数据异常问题。')) {
    return
  }

  try {
    console.log('🧹 开始清除所有缓存...')

    // 清除 localStorage 中的所有相关数据
    const itemsToRemove = [
      'wechat_openid',
      'jwt_token',
      'user_info',
      'token_expire_time',
      'wechat_auth_state',
      'wechat_auth_scope',
      'reservation_data',
      'community_data'
    ]

    itemsToRemove.forEach(item => {
      localStorage.removeItem(item)
      console.log(`✅ 已清除: ${item}`)
    })

    // 清除 sessionStorage
    sessionStorage.clear()

    // 清除 IndexedDB 等其他存储（如果有的话）
    if (window.indexedDB) {
      window.indexedDB.databases().then(databases => {
        databases.forEach(db => {
          if (db.name) {
            window.indexedDB.deleteDatabase(db.name)
          }
        })
      })
    }

    // 清除 Cookie（针对特定域名）
    document.cookie.split(";").forEach(cookie => {
      const eqPos = cookie.indexOf("=")
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=" + window.location.hostname
    })

    console.log('✅ 所有缓存清除完成')

    // 显示成功消息
    showMessage.value = true
    messageText.value = '✅ 缓存清除成功，即将重新加载...'
    messageType.value = 'success'

    // 1.5秒后重新加载页面
    setTimeout(() => {
      window.location.href = '/?clear=all&t=' + Date.now()
    }, 1500)

  } catch (error) {
    console.error('❌ 清除缓存失败:', error)

    // 显示错误消息
    showMessage.value = true
    messageText.value = '❌ 清除失败，请重试'
    messageType.value = 'error'

    setTimeout(() => {
      showMessage.value = false
    }, 3000)
  }
}

// 方法
const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    userInfo.value = JSON.parse(userInfoStr)
  }
}

const loadReservations = async () => {
  try {
    loading.value = true
    const response = await getLatestReservations()
    reservations.value = response.data || []
    loading.value = false
  } catch (error) {
    console.error('加载预约信息失败:', error)
    loading.value = false
    // 可以添加错误提示
  }
}

const getStatusClass = (status) => {
  const statusMap = {
    0: 'pending',      // 待审核
    1: 'confirmed',    // 已通过
    2: 'rejected',     // 被拒绝
    3: 'cancelled',    // 已取消
    4: 'completed'     // 已完成
  }
  return statusMap[status] || 'pending'
}

const getStatusText = (status) => {
  const statusTextMap = {
    0: '待审核',
    1: '已通过',
    2: '被退回',
    3: '已取消',
    4: '已完成'
  }
  return statusTextMap[status] || '未知状态'
}

// 查看全部预约
const viewAllReservations = (filterType = 'all') => {
  router.push({
    path: '/reservation-list',
    query: { filter: filterType }
  })
}

// 格式化日期和时间
const formatDate = (reservationDate, startTime, endTime) => {
  const date = new Date(reservationDate)
  const formattedDate = date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
  return `${formattedDate} ${startTime} - ${endTime}`
}

// 计算时长
const calculateDuration = (startTime, endTime) => {
  const start = new Date(`2000-01-01 ${startTime}`)
  const end = new Date(`2000-01-01 ${endTime}`)
  const duration = (end - start) / (1000 * 60 * 60) // 转换为小时
  return duration.toFixed(1)
}

const cancelReservation = async (reservationNo) => {
  if (confirm('确定要取消这个预约吗？')) {
    try {
      await cancelUserReservation(reservationNo)
      // 重新加载数据
      await loadReservations()
      // 可以添加成功提示
    } catch (error) {
      console.error('取消预约失败:', error)
      // 可以添加错误提示
    }
  }
}

// 重新提交预约 (函数保留，但模板中已无调用)
const resubmitReservation = async (reservationId) => {
  if (confirm('确定要重新提交这个预约吗？')) {
    try {
      // 这里的逻辑仍然保留，以防后端 API 依赖
      await resubmitUserReservation(reservationId)
      // 重新加载数据
      await loadReservations()
      // 可以添加成功提示
    } catch (error) {
      console.error('重新提交预约失败:', error)
      // 可以添加错误提示
    }
  }
}

const goToCommunityList = () => {
  router.push('/community-list')
}

const goToUserProfile = () => {
  router.push('/user-profile')
}

// 生命周期
onMounted(() => {
  loadUserInfo()
  loadReservations()
})
</script>

<style scoped>
.student-reservations {
  min-height: calc(100vh - 70px);
  padding: 16px;
  background: #f5f5f5;
}

/* 优化后的清除缓存控制区域 */
.cache-control-section {
  /* 位置和颜色保持不变 */
  margin-top: 10px;
  background: white;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  /* 优化样式 */
  font-size: 16px;
  font-weight: 600; /* 加粗 */
  color: #ff4d4f; /* 使用取消按钮的红色，醒目 */
  cursor: pointer; /* 鼠标指针变化 */
  transition: all 0.3s ease;
  border: 1px solid transparent; /* 边框透明 */
}

/* 鼠标悬停效果 */
.cache-control-section:hover {
  background: #fff2f0; /* 悬停时背景变浅红 */
  color: #d9363e; /* 悬停时字体颜色变深 */
  transform: translateY(-2px); /* 轻微上浮 */
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.2); /* 悬停时阴影增强 */
  border-color: #ffccc7; /* 悬停时显示边框 */
}


/* 用户头部信息 */
.user-header {
  background: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 3px solid #07c160;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-nickname {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.user-openid {
  margin: 0;
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

/* 统计卡片 - 修改为4列 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

/* 不同状态的数字颜色 */
.stat-card:nth-child(1) .stat-number { color: #07c160; } /* 总预约 */
.stat-card:nth-child(2) .stat-number { color: #1890ff; } /* 进行中 */
.stat-card:nth-child(3) .stat-number { color: #52c41a; } /* 已完成 */
.stat-card:nth-child(4) .stat-number { color: #ff4d4f; } /* 被退回 */

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 预约列表区域 */
.reservations-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 6px;
  padding: 12px;
  margin: -12px -12px 16px -12px;
}

.section-header:hover {
  background: #f8f9fa;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-badge {
  background: #07c160;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.view-all {
  color: #07c160;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.section-header:hover .view-all {
  transform: translateX(2px);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px 0;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #07c160;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.primary-btn {
  background: #07c160;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 12px;
  transition: all 0.3s ease;
}

.primary-btn:hover {
  background: #06a050;
  transform: translateY(-1px);
}

/* 预约卡片 */
.reservations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reservation-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.reservation-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reservation-card.pending {
  border-left: 4px solid #ffa500;
}

.reservation-card.confirmed {
  border-left: 4px solid #07c160;
}

.reservation-card.completed {
  border-left: 4px solid #1890ff;
}

.reservation-card.cancelled {
  border-left: 4px solid #999;
  opacity: 0.7;
}

/* 新增：被退回状态样式 */
.reservation-card.rejected {
  border-left: 4px solid #ff4d4f;
  background: #fff2f0;
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.room-name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.confirmed {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.completed {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.cancelled {
  background: #f5f5f5;
  color: #666;
}

/* 新增：被退回状态徽章 */
.status-badge.rejected {
  background: #fff2f0;
  color: #ff4d4f;
}

.reservation-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  font-size: 14px;
}

.label {
  color: #666;
  min-width: 70px;
}

.value {
  color: #333;
  flex: 1;
}

/* 新增：退回原因样式 */
.reject-reason {
  color: #ff4d4f;
  font-weight: 500;
}

.reservation-actions {
  text-align: right;
}

.cancel-btn {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #d9363e;
  transform: translateY(-1px);
}

/* 新增：重新提交按钮 (样式保留，但模板中已无调用) */
.resubmit-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.resubmit-btn:hover {
  background: #096dd9;
  transform: translateY(-1px);
}

/* 消息提示 */
.message-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 16px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  z-index: 1001;
  animation: fadeInOut 3s ease-in-out;
}

.message-toast.success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.message-toast.error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
}

@keyframes fadeInOut {
  0%, 100% { opacity: 0; transform: translate(-50%, -60%); }
  10%, 90% { opacity: 1; transform: translate(-50%, -50%); }
}
</style>
