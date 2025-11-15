<template>
  <div class="student-reservations">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="user-avatar">
        <img :src="userInfo.headimgurl || defaultAvatar" alt="头像" />
      </div>
      <div class="user-info">
        <h2 class="user-nickname">{{ userInfo.nickname || '微信用户' }}</h2>
        <p class="user-openid">ID: {{ userInfo.openid || '未知' }}</p>
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-number">{{ totalReservations }}</div>
        <div class="stat-label">总预约</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ activeReservations }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ completedReservations }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="reservations-section">
      <div class="section-header">
        <h3>我的预约</h3>
        <span class="section-badge">{{ reservations.length }} 条记录</span>
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
              <span class="value">{{ formatDate(reservation.reservationTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">时长：</span>
              <span class="value">{{ reservation.duration }} 小时</span>
            </div>
            <div class="detail-item">
              <span class="label">预约人：</span>
              <span class="value">{{ reservation.studentName }}</span>
            </div>
            <div class="detail-item" v-if="reservation.purpose">
              <span class="label">用途：</span>
              <span class="value">{{ reservation.purpose }}</span>
            </div>
          </div>

          <div class="reservation-actions" v-if="reservation.status === 'PENDING'">
            <button @click="cancelReservation(reservation.id)" class="cancel-btn">
              取消预约
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const reservations = ref([])
const loading = ref(true)

// 默认头像
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 计算属性
const totalReservations = computed(() => reservations.value.length)
const activeReservations = computed(() =>
    reservations.value.filter(r => r.status === 'PENDING' || r.status === 'CONFIRMED').length
)
const completedReservations = computed(() =>
    reservations.value.filter(r => r.status === 'COMPLETED' || r.status === 'CANCELLED').length
)

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
    // 模拟API调用 - 实际项目中替换为真实的API调用
    const mockReservations = [
      {
        id: 1,
        roomName: '社区活动室 A',
        reservationTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), // 2小时后
        duration: 2,
        studentName: userInfo.value.nickname || '学生',
        purpose: '小组讨论',
        status: 'PENDING'
      },
      {
        id: 2,
        roomName: '会议室 B',
        reservationTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // 1天前
        duration: 1,
        studentName: userInfo.value.nickname || '学生',
        purpose: '个人学习',
        status: 'COMPLETED'
      },
      {
        id: 3,
        roomName: '研讨室 C',
        reservationTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 1天后
        duration: 3,
        studentName: userInfo.value.nickname || '学生',
        purpose: '项目会议',
        status: 'CONFIRMED'
      }
    ]

    // 模拟网络延迟
    setTimeout(() => {
      reservations.value = mockReservations
      loading.value = false
    }, 1000)

  } catch (error) {
    console.error('加载预约信息失败:', error)
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const statusMap = {
    'PENDING': 'pending',
    'CONFIRMED': 'confirmed',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled'
  }
  return statusMap[status] || 'pending'
}

const getStatusText = (status) => {
  const statusTextMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusTextMap[status] || '未知状态'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const cancelReservation = (reservationId) => {
  if (confirm('确定要取消这个预约吗？')) {
    // 模拟取消预约
    const index = reservations.value.findIndex(r => r.id === reservationId)
    if (index !== -1) {
      reservations.value[index].status = 'CANCELLED'
      // 实际项目中这里应该调用取消预约的API
    }
  }
}

const goToCommunityList = () => {
  router.push('/community-list')
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #07c160;
  margin-bottom: 4px;
}

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
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.section-badge {
  background: #07c160;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
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
}

.primary-btn:hover {
  background: #06a050;
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
}

.cancel-btn:hover {
  background: #d9363e;
}
</style>
