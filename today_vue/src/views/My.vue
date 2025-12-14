<template>
  <div class="student-reservations">
    <div class="user-header">
      <!-- 修复1：添加可选链 ?. 防止 userInfo 为 null/undefined -->
      <div class="user-avatar" @click="goToUserProfile" style="cursor: pointer;">
        <img :src="userInfo?.headimgurl || defaultAvatar" alt="头像" />
      </div>
      <div class="user-info">
        <h2 class="user-nickname">{{ userInfo?.nickname || '微信用户' }}</h2>
        <p class="user-openid">ID: {{ userInfo?.openid || '未知' }}</p>
      </div>
    </div>

    <!-- 快捷入口（样式不变） -->
    <div class="quick-card">
      <div class="quick-stats">
        <div class="quick-stat-item" @click="viewAllReservations('all')">
          <span class="quick-count" :class="{ loading: loading }">
            {{ loading ? '' : totalReservations }}
          </span>
          <span class="quick-label">预约记录</span>
        </div>
        <div class="quick-stat-item" @click="goToFavorites">
          <span class="quick-count" :class="{ loading: loadingFavorites }">
            {{ loadingFavorites ? '' : favoriteCount }}
          </span>
          <span class="quick-label">收藏房间</span>
        </div>
        <div class="quick-stat-item" @click="goToMessages">
          <span class="quick-count" :class="{ loading: loadingMessages }">
            {{ loadingMessages ? '' : messageCount }}
          </span>
          <span class="quick-label">消息通知</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单网格（不变） -->
    <div class="menu-grid">
      <div class="menu-item" @click="viewAllReservations('all')">
        <div class="menu-icon green">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
        </div>
        <span class="menu-text">我的预约</span>
        <span class="menu-arrow">›</span>
      </div>

      <div class="menu-item" @click="goToFavorites">
        <div class="menu-icon pink">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
          </svg>
        </div>
        <span class="menu-text">收藏房间</span>
        <span class="menu-arrow">›</span>
      </div>

      <div class="menu-item" @click="goToMessages">
        <div class="menu-icon blue">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
        </div>
        <span class="menu-text">消息通知</span>
        <span class="menu-arrow">›</span>
      </div>

      <div class="menu-item" @click="goToFeedback">
        <div class="menu-icon cyan">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
        </div>
        <span class="menu-text">意见反馈</span>
        <span class="menu-arrow">›</span>
      </div>

      <div class="menu-item" @click="showClearCacheDialog = true">
        <div class="menu-icon red">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 0-2 2H7a2 2 0 0 0-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </div>
        <span class="menu-text">清除缓存</span>
        <span class="menu-arrow">›</span>
      </div>

      <div class="menu-item" @click="goToAbout">
        <div class="menu-icon gray">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
        </div>
        <span class="menu-text">关于我们</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="showMessage" class="message-toast" :class="messageType">
      {{ messageText }}
    </div>

    <!-- 自定义清除缓存弹窗（去掉图标） -->
    <div v-if="showClearCacheDialog" class="cache-dialog-overlay">
      <div class="cache-dialog">
        <div class="dialog-content">
          <h3 class="dialog-title">清除本地缓存</h3>
          <p class="dialog-desc">确定要清除所有本地缓存并重新加载页面吗？<br>这可能有助于解决登录或数据异常问题。</p>
        </div>
        <div class="dialog-buttons">
          <button class="btn cancel-btn" @click="showClearCacheDialog = false">
            取消
          </button>
          <button class="btn confirm-btn" @click="handleClearCache">
            确认清除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入接口（不变）
import { getMyReservations } from '@/api/reservations.js'
import { getFavorites } from '@/api/favorite.js'
import { getUnreadCount, getNotifications } from '@/api/notification.js'

const router = useRouter()

// 修复2：初始化 userInfo 为非空对象，避免初始渲染报错
const userInfo = ref({
  headimgurl: '',
  nickname: '',
  openid: ''
})
const reservations = ref([])

// 加载状态（不变）
const loading = ref(true)
const loadingFavorites = ref(true)
const loadingMessages = ref(true)

// 消息提示相关状态（不变）
const showMessage = ref(false)
const messageText = ref('')
const messageType = ref('success')

// 清除缓存弹窗状态（不变）
const showClearCacheDialog = ref(false)

// 定时器引用（不变）
const refreshTimer = ref(null)

// 默认头像（不变）
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 初始化数据（不变）
const favoriteCount = ref(0)
const messageCount = ref(0)
const totalNotifications = ref(0)

// 计算属性（不变）
const totalReservations = computed(() => reservations.value.length)
const activeReservations = computed(() =>
    reservations.value.filter(r => r.status === 0 || r.status === 1).length)
const completedReservations = computed(() =>
    reservations.value.filter(r => r.status === 3 || r.status === 4).length)
const rejectedReservations = computed(() =>
    reservations.value.filter(r => r.status === 2).length)

// 处理清除缓存逻辑（不变）
const handleClearCache = () => {
  showClearCacheDialog.value = false
  try {
    console.log('开始清除所有缓存...')
    const itemsToRemove = [
      'wechat_openid',
      'jwt_token',
      'user_info',
      'token_expire_time',
      'wechat_auth_state',
      'wechat_auth_scope',
      'reservation_data',
      'community_data',
      'unread_messages'
    ]
    itemsToRemove.forEach(item => {
      localStorage.removeItem(item)
    })
    sessionStorage.clear()
    if (window.indexedDB) {
      window.indexedDB.databases().then(databases => {
        databases.forEach(db => {
          if (db.name) {
            window.indexedDB.deleteDatabase(db.name)
          }
        })
      })
    }
    document.cookie.split(";").forEach(cookie => {
      const eqPos = cookie.indexOf("=")
      const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim()
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=${window.location.hostname}`
    })
    showMessage.value = true
    messageText.value = '缓存清除成功，即将重新加载...'
    messageType.value = 'success'
    setTimeout(() => {
      window.location.href = '/?clear=all&t=' + Date.now()
    }, 1500)
  } catch (error) {
    console.error('清除缓存失败:', error)
    showMessage.value = true
    messageText.value = '清除失败，请重试'
    messageType.value = 'error'
    setTimeout(() => {
      showMessage.value = false
    }, 3000)
  }
}

// 修复3：加载用户信息时增加容错处理，避免 JSON 解析失败
const loadUserInfo = () => {
  try {
    const userInfoStr = localStorage.getItem('user_info')
    if (userInfoStr) {
      // 增加 JSON 解析容错
      const parsedInfo = JSON.parse(userInfoStr)
      // 只覆盖有值的属性，避免清空默认值
      userInfo.value = {
        ...userInfo.value,
        ...parsedInfo
      }
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
    // 解析失败时保留默认空对象，避免渲染报错
    userInfo.value = {
      headimgurl: '',
      nickname: '',
      openid: ''
    }
  }
}

// 加载预约信息（不变）
const loadReservations = async () => {
  try {
    loading.value = true
    const response = await getMyReservations(null)
    if (response.code === 200 && response.data) {
      reservations.value = response.data || []
    } else {
      throw new Error(response.message || '获取预约记录失败')
    }
  } catch (error) {
    console.error('加载预约信息失败:', error)
    ElMessage.error('加载预约记录失败，请稍后重试')
    reservations.value = []
  } finally {
    loading.value = false
  }
}

// 加载收藏数量（不变）
const loadFavoritesCount = async () => {
  try {
    loadingFavorites.value = true
    const response = await getFavorites()
    if (response.code === 200 && response.data) {
      favoriteCount.value = response.data.length || 0
    } else {
      throw new Error(response.message || '获取收藏数量失败')
    }
  } catch (error) {
    console.error('加载收藏数量失败:', error)
    ElMessage.error('加载收藏信息失败')
    favoriteCount.value = 0
  } finally {
    loadingFavorites.value = false
  }
}

// 加载消息数量（不变）
const loadMessageCount = async () => {
  try {
    loadingMessages.value = true

    const unreadResponse = await getUnreadCount()
    if (unreadResponse.code === 200) {
      messageCount.value = unreadResponse.data || 0
    } else {
      console.warn('获取未读数量失败:', unreadResponse.message)
      messageCount.value = 0
    }

    try {
      const listResponse = await getNotifications(1, 1)
      if (listResponse.code === 200 && listResponse.data) {
        totalNotifications.value = listResponse.data.total || 0
      }
    } catch (listError) {
      console.warn('获取总通知数量失败:', listError)
    }

    if (messageCount.value > 0) {
      localStorage.setItem('unread_messages', messageCount.value.toString())
    } else {
      localStorage.removeItem('unread_messages')
    }

  } catch (error) {
    console.error('加载消息数量失败:', error)

    const cachedCount = localStorage.getItem('unread_messages')
    messageCount.value = cachedCount ? parseInt(cachedCount) : 0

    if (error.message.includes('Network Error')) {
      console.warn('网络连接失败，使用缓存的未读消息数量')
    } else {
      ElMessage.warning('加载消息通知失败，请检查网络')
    }
  } finally {
    loadingMessages.value = false
  }
}

// 定时刷新消息数量（不变）
const startMessageRefresh = () => {
  refreshTimer.value = setInterval(() => {
    if (!document.hidden) {
      loadMessageCount()
    }
  }, 60000)
}

// 页面跳转方法（不变）
const viewAllReservations = (filterType = 'all') => {
  router.push({
    path: '/reservations',
    query: { filter: filterType }
  })
}

const goToUserProfile = () => {
  router.push('/user-profile')
}

const goToFavorites = () => {
  router.push('/favorites')
}

const goToMessages = () => {
  router.push('/notifications')
}

const goToFeedback = () => {
  router.push('/feedback')
}

const goToHelp = () => {
  router.push('/help')
}

const goToAbout = () => {
  // router.push('/about')
}

// 监听页面可见性变化（不变）
const handleVisibilityChange = () => {
  if (!document.hidden) {
    loadMessageCount()
  }
}

// 初始化数据（不变）
const initData = async () => {
  loadUserInfo()
  await Promise.all([
    loadReservations(),
    loadFavoritesCount(),
    loadMessageCount()
  ])
}

// 生命周期（不变）
onMounted(() => {
  initData()
  startMessageRefresh()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('focus', loadMessageCount)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('focus', loadMessageCount)
})
</script>

<style scoped>
/* 所有样式保持不变 */
.student-reservations {
  min-height: calc(100vh - 70px);
  padding: 16px;
  background: #ffffff;
}

.user-header {
  background: #ffffff;
  padding: 24px 20px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 3px solid #43a047;
  box-shadow: 0 2px 12px rgba(67, 160, 71, 0.2);
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
  margin: 0 0 6px 0;
  font-size: 20px;
  color: #333333;
  font-weight: 600;
}

.user-openid {
  margin: 0;
  font-size: 13px;
  color: #888888;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.quick-card {
  width: 100%;
  background: #fff;
  border-radius: 14px;
  padding: 20px 16px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.quick-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  width: 100%;
}

.quick-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 12px;
  width: 100%;
  border-radius: 8px;
}

.quick-count {
  font-size: 23px;
  font-weight: 700;
  color: #1e88e5;
  line-height: 1.2;
  margin-bottom: 6px;
  position: relative;
  min-width: 40px;
  text-align: center;
}

.quick-count.loading {
  color: transparent;
}

.quick-count.loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 24px;
  height: 24px;
  border: 2px solid #e2e8f0;
  border-top-color: #1e88e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

.quick-label {
  font-size: 14px;
  color: #888;
  text-align: center;
}

.quick-stat-item:hover {
  background-color: #f5f9ff;
  transform: translateY(-2px);
}

.quick-stat-item:hover .quick-count {
  color: #1565c0;
}

.quick-stat-item:hover .quick-label {
  color: #1976d2;
}

.quick-stat-item.has-unread .quick-count {
  color: #ff4757;
  position: relative;
}

.quick-stat-item.has-unread .quick-count::after {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #ff4757;
  border-radius: 50%;
  border: 2px solid white;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.menu-item {
  background: #fff;
  border-radius: 14px;
  padding: 18px 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.menu-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e8e8e8;
}

.menu-item:active {
  transform: scale(0.98);
}

.menu-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.menu-icon.green {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #43a047;
}

.menu-icon.pink {
  background: linear-gradient(135deg, #fce4ec 0%, #f8bbd9 100%);
  color: #e91e63;
}

.menu-icon.blue {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1e88e5;
}

.menu-icon.cyan {
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
  color: #00acc1;
}

.menu-icon.red {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  color: #e53935;
}

.menu-icon.orange {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #fb8c00;
}

.menu-icon.teal {
  background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);
  color: #00897b;
}

.menu-icon.gray {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
  color: #757575;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.menu-arrow {
  color: #c0c0c0;
  font-size: 20px;
  font-weight: 300;
  transition: transform 0.2s ease;
}

.menu-item:hover .menu-arrow {
  transform: translateX(3px);
  color: #999;
}

.message-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 16px 28px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  z-index: 1001;
  animation: fadeInOut 3s ease-in-out;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
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

.cache-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1002;
  backdrop-filter: blur(2px);
}

.cache-dialog {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 360px;
  padding: 24px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  animation: dialogPop 0.3s ease-out;
  text-align: center;
}

@keyframes dialogPop {
  0% { opacity: 0; transform: scale(0.9); }
  100% { opacity: 1; transform: scale(1); }
}

.dialog-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.dialog-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.dialog-buttons {
  display: flex;
  width: 100%;
  gap: 12px;
}

.btn {
  flex: 1;
  padding: 12px 0;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e9e9e9;
  color: #333;
}

.confirm-btn {
  background: linear-gradient(135deg, #e53935 0%, #d32f2f 100%);
  color: #fff;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #d32f2f 0%, #c62828 100%);
  transform: translateY(-1px);
}

.confirm-btn:active {
  transform: scale(0.98);
}
</style>
