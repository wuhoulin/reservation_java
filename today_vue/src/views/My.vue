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

    <!-- 快捷入口 -->
    <div class="quick-card">
      <div class="quick-stats">
        <div class="quick-stat-item">
          <!-- 动态绑定 loading 类，控制加载动画显示 -->
          <span class="quick-count" :class="{ loading: loading }">
            {{ loading ? '加载中' : totalReservations }}
          </span>
          <span class="quick-label">预约记录</span>
        </div>
        <div class="quick-stat-item">
          <span class="quick-count" :class="{ loading: loadingFavorites }">
            {{ loadingFavorites ? '加载中' : favoriteCount }}
          </span>
          <span class="quick-label">收藏房间</span>
        </div>
        <div class="quick-stat-item">
          <span class="quick-count" :class="{ loading: loadingMessages }">
            {{ loadingMessages ? '加载中' : messageCount }}
          </span>
          <span class="quick-label">消息通知</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单网格 -->
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
        <!-- 移除图标部分 -->
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
  </div> <!-- 补全根容器闭合标签 -->
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 导入预约相关接口
import { getMyReservations } from '@/api/reservations.js'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const reservations = ref([])
// 加载状态（区分不同数据加载）
const loading = ref(true)
const loadingFavorites = ref(true)
const loadingMessages = ref(true)

// 消息提示相关状态
const showMessage = ref(false)
const messageText = ref('')
const messageType = ref('success')

// 清除缓存弹窗状态
const showClearCacheDialog = ref(false)

// 默认头像
const defaultAvatar = 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132'

// 初始化数据（后续通过接口获取）
const favoriteCount = ref(0)
const messageCount = ref(0)
const pointsBalance = ref(0)

// 计算属性 - 基于真实预约数据计算统计
const totalReservations = computed(() => reservations.value.length)
const activeReservations = computed(() =>
    reservations.value.filter(r => r.status === 0 || r.status === 1).length) // 待审核+已通过=进行中
const completedReservations = computed(() =>
    reservations.value.filter(r => r.status === 3 || r.status === 4).length) // 已取消+已完成=已完成
const rejectedReservations = computed(() =>
    reservations.value.filter(r => r.status === 2).length) // 已拒绝=被退回

// 新增积分页面跳转方法
const goToPoints = () => {
  router.push('/points-balance')
}

// 处理清除缓存逻辑
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
      'community_data'
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

// 加载用户信息
const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    userInfo.value = JSON.parse(userInfoStr)
  }
}

// 加载预约信息（通过接口获取真实数据）
const loadReservations = async () => {
  try {
    loading.value = true
    // 调用接口获取当前用户所有预约记录（status=null表示查询所有状态）
    const response = await getMyReservations(null)
    if (response.code === 200 && response.data) {
      reservations.value = response.data || []
    } else {
      throw new Error(response.message || '获取预约记录失败')
    }
  } catch (error) {
    console.error('加载预约信息失败:', error)
    ElMessage.error('加载预约记录失败，请稍后重试')
    reservations.value = [] // 异常时置空
  } finally {
    loading.value = false
  }
}

// 加载收藏数量（示例：假设后续有收藏接口，这里先模拟）
const loadFavoritesCount = async () => {
  try {
    loadingFavorites.value = true
    // 后续替换为真实的收藏接口：
    // const response = await getFavoritesCount()
    // favoriteCount.value = response.data || 0

    // 模拟：如果没有接口，暂时默认0（或从本地存储读取）
    const favorites = localStorage.getItem('favorites')
    favoriteCount.value = favorites ? JSON.parse(favorites).length : 0
  } catch (error) {
    console.error('加载收藏数量失败:', error)
    favoriteCount.value = 0
  } finally {
    loadingFavorites.value = false
  }
}

// 加载消息数量（示例：假设后续有消息接口，这里先模拟）
const loadMessageCount = async () => {
  try {
    loadingMessages.value = true
    // 后续替换为真实的消息接口：
    // const response = await getUnreadMessageCount()
    // messageCount.value = response.data || 0

    // 模拟：如果没有接口，暂时默认0（或从本地存储读取）
    const messages = localStorage.getItem('unread_messages')
    messageCount.value = messages ? JSON.parse(messages).length : 0
  } catch (error) {
    console.error('加载消息数量失败:', error)
    messageCount.value = 0
  } finally {
    loadingMessages.value = false
  }
}

// 查看所有预约
const viewAllReservations = (filterType = 'all') => {
  router.push({
    path: '/reservation-list',
    query: { filter: filterType }
  })
}

// 跳转个人资料
const goToUserProfile = () => {
  router.push('/user-profile')
}

// 跳转收藏
const goToFavorites = () => {
  router.push('/favorites')
}

// 跳转消息
const goToMessages = () => {
  router.push('/messages')
}

// 跳转意见反馈
const goToFeedback = () => {
  router.push('/feedback')
}

// 跳转帮助中心
const goToHelp = () => {
  router.push('/help')
}

// 跳转关于我们
const goToAbout = () => {
  // router.push('/about')
}

// 生命周期：挂载时加载所有数据
onMounted(() => {
  loadUserInfo()
  loadReservations() // 加载真实预约数据
  loadFavoritesCount() // 加载收藏数量
  loadMessageCount() // 加载消息数量
})
</script>

<style scoped>
/* 背景改为白色 */
.student-reservations {
  min-height: calc(100vh - 70px);
  padding: 16px;
  background: #ffffff;
}

/* 用户头部信息 */
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

/* 快捷入口 */
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
  cursor: pointer;
  transition: color 0.2s;
}

.quick-stat-item:hover .quick-count {
  color: #1565c0;
}

.quick-count {
  font-size: 23px;
  font-weight: 700;
  color: #1e88e5;
  line-height: 1.2;
  margin-bottom: 4px;
}

/* 加载中样式：使用动态 class 替代 :contains 伪类 */
.quick-count.loading::after {
  content: '';
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #ccc;
  border-top-color: #1e88e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  vertical-align: middle;
  margin-left: 4px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.quick-label {
  font-size: 14px;
  color: #888;
}

/* 功能菜单网格 */
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

/* 消息提示 */
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

/* 清除缓存弹窗样式（去掉图标后调整） */
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
  padding: 24px 24px; /* 减少顶部内边距，去掉图标占用的空间 */
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  animation: dialogPop 0.3s ease-out;
  text-align: center; /* 让标题和描述居中 */
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
  margin: 0 0 20px 0; /* 调整底部间距，保持整体协调 */
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
