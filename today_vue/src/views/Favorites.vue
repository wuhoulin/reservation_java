<template>
  <div class="favorites-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </div>
      <div class="title">我的收藏</div>
      <div class="header-actions">
        <div class="sort-button" @click="showSortOptions = !showSortOptions">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 6h18"></path>
            <path d="M7 12h10"></path>
            <path d="M10 18h4"></path>
          </svg>
          <span>{{ sortOptions.find(opt => opt.value === sortBy)?.name }}</span>
        </div>
      </div>
    </div>

    <!-- 排序选项 -->
    <transition name="slide-down">
      <div v-if="showSortOptions" class="sort-options">
        <div
            v-for="option in sortOptions"
            :key="option.value"
            class="sort-option"
            :class="{ active: sortBy === option.value }"
            @click="handleSortChange(option.value)"
        >
          <span class="option-icon">{{ option.icon }}</span>
          <span class="option-name">{{ option.name }}</span>
          <svg v-if="sortBy === option.value" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>
      </div>
    </transition>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="favorites.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
        </svg>
      </div>
      <h3 class="empty-title">暂无收藏的教室</h3>
      <p class="empty-desc">快去发现并收藏您喜欢的教室吧</p>
      <button class="explore-button" @click="goToExplore">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        探索教室
      </button>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="favorites-content">
      <div class="favorites-grid">
        <div
            v-for="favorite in sortedFavorites"
            :key="favorite.id"
            class="favorite-card"
            @click="goToRoomDetail(favorite.roomId)"
        >
          <!-- 教室图片 -->
          <div class="room-image" :style="{ backgroundImage: `url(${favorite.imageUrl || '/placeholder-room.jpg'})` }">
            <div class="favorite-badge">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
            </div>
            <button class="quick-action" @click.stop="handleQuickAction(favorite)">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="1"></circle>
                <circle cx="12" cy="5" r="1"></circle>
                <circle cx="12" cy="19" r="1"></circle>
              </svg>
            </button>
          </div>

          <!-- 教室信息 -->
          <div class="room-info">
            <div class="room-header">
              <h3 class="room-name">{{ favorite.roomName }}</h3>
              <div class="room-capacity">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                {{ favorite.capacity }}人
              </div>
            </div>

            <div class="room-location">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              {{ favorite.communityName || '未知区域' }}
            </div>

            <div class="room-features">
              <span v-if="favorite.hasProjection" class="feature-tag">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                  <line x1="8" y1="21" x2="16" y2="21"></line>
                  <line x1="12" y1="17" x2="12" y2="21"></line>
                </svg>
                投影仪
              </span>
              <span v-if="favorite.hasAirConditioner" class="feature-tag">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                空调
              </span>
            </div>

            <div class="room-meta">
              <span class="collect-time">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                收藏于 {{ formatTime(favorite.createdAt) }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button
                class="action-btn book-btn"
                @click.stop="quickBook(favorite)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              快速预约
            </button>
            <button class="action-btn unfavorite-btn" @click.stop="handleRemoveClick(favorite)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作菜单 -->
    <div v-if="showActionMenu" class="action-menu-overlay" @click="showActionMenu = false">
      <div class="action-menu" @click.stop>
        <div class="menu-header">
          <h4>{{ selectedFavorite?.roomName }}</h4>
          <button class="close-menu" @click="showActionMenu = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="menu-items">
          <button class="menu-item" @click="goToRoomDetail(selectedFavorite?.roomId)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            查看详情
          </button>
          <button
              class="menu-item"
              @click="quickBook(selectedFavorite)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            快速预约
          </button>
          <button class="menu-item share" @click="shareRoom(selectedFavorite)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path>
              <polyline points="16 6 12 2 8 6"></polyline>
              <line x1="12" y1="2" x2="12" y2="15"></line>
            </svg>
            分享教室
          </button>
          <button class="menu-item remove" @click="handleRemoveClick(selectedFavorite)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            取消收藏
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="showDeleteConfirm = false">
      <div class="modal-content confirm-modal" @click.stop>
        <div class="modal-icon">🗑️</div>
        <h3>取消收藏</h3>
        <p>确定要取消收藏「{{ selectedFavorite?.roomName }}」吗？</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showDeleteConfirm = false">取消</button>
          <button class="modal-btn confirm" @click="confirmRemove">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api/favorite.js'

const router = useRouter()

// 状态
const loading = ref(true)
const favorites = ref([])
const showSortOptions = ref(false)
const showActionMenu = ref(false)
const showDeleteConfirm = ref(false)
const selectedFavorite = ref(null)
const sortBy = ref('time') // time, name, capacity

// 排序选项
const sortOptions = [
  { value: 'time', name: '收藏时间', icon: '🕒' },
  { value: 'name', name: '名称', icon: '🔤' },
  { value: 'capacity', name: '容量', icon: '👥' }
]

// 计算属性
const sortedFavorites = computed(() => {
  const list = [...favorites.value]
  switch (sortBy.value) {
    case 'name':
      return list.sort((a, b) => a.roomName.localeCompare(b.roomName))
    case 'capacity':
      return list.sort((a, b) => b.capacity - a.capacity)
    case 'time':
    default:
      return list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  }
})

// 方法
const goBack = () => {
  router.back()
}

const goToExplore = () => {
  router.push('/community-list')
}

const loadFavorites = async () => {
  try {
    loading.value = true
    const response = await getFavorites()
    if (response.code === 200) {
      favorites.value = response.data || []
    } else {
      throw new Error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleSortChange = (value) => {
  sortBy.value = value
  showSortOptions.value = false
}

const handleQuickAction = (favorite) => {
  selectedFavorite.value = favorite
  showActionMenu.value = true
}

const goToRoomDetail = (roomId) => {
  if (roomId) {
    router.push(`/room/${roomId}`)
  }
}

const quickBook = (favorite) => {
  goToRoomDetail(favorite.roomId)
  showActionMenu.value = false
}

const handleRemoveClick = (favorite) => {
  selectedFavorite.value = favorite
  showDeleteConfirm.value = true
  showActionMenu.value = false
}

const confirmRemove = async () => {
  if (!selectedFavorite.value) return
  try {
    // 修复：将 cancelFavorite 改为正确的 removeFavorite 接口
    const response = await removeFavorite(selectedFavorite.value.roomId)
    if (response.code === 200) {
      ElMessage.success('取消收藏成功')
      const index = favorites.value.findIndex(fav => fav.id === selectedFavorite.value.id)
      if (index > -1) favorites.value.splice(index, 1)
    } else {
      throw new Error(response.message || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  } finally {
    showDeleteConfirm.value = false
    selectedFavorite.value = null
  }
}

const shareRoom = (favorite) => {
  if (!favorite) return
  const shareUrl = `${window.location.origin}/room/${favorite.roomId}`
  if (navigator.share) {
    navigator.share({
      title: `推荐教室：${favorite.roomName}`,
      text: `发现一个不错的教室：${favorite.roomName}，可容纳${favorite.capacity}人`,
      url: shareUrl
    })
  } else {
    navigator.clipboard.writeText(shareUrl)
    ElMessage.success('链接已复制到剪贴板')
  }
  showActionMenu.value = false
}

const formatTime = (timeStr) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 初始化
onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 24px;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.back-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #4299e1; /* 替换紫色为蓝色 */
}

.back-button:hover {
  background: rgba(66, 153, 225, 0.1); /* 替换紫色为蓝色 */
  transform: scale(1.05);
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  letter-spacing: 0.5px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.sort-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-button:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

/* 排序选项 */
.sort-options {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 99;
}

.sort-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.sort-option:hover {
  background: #f7fafc;
}

.sort-option.active {
  background: #f0fff4;
  border-left-color: #48bb78;
  color: #2f855a;
}

.option-icon {
  font-size: 16px;
}

.option-name {
  flex: 1;
  font-weight: 500;
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
  border-top: 4px solid #4299e1; /* 替换紫色为蓝色 */
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

.empty-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border-radius: 50%;
  margin-bottom: 24px;
  color: #e53e3e;
}

.empty-icon svg {
  opacity: 0.7;
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
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%); /* 替换紫色为蓝绿色渐变 */
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.explore-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(66, 153, 225, 0.4); /* 替换紫色为蓝色 */
}

/* 收藏网格 */
.favorites-content {
  padding: 0 20px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 教室图片 */
.room-image {
  position: relative;
  height: 180px;
  background-size: cover;
  background-position: center;
  background-color: #f7fafc;
}

.favorite-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(239, 68, 68, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.quick-action {
  position: absolute;
  bottom: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.quick-action:hover {
  background: white;
  transform: scale(1.1);
}

/* 教室信息 */
.room-info {
  padding: 16px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.room-name {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.3;
  flex: 1;
  margin-right: 12px;
}

.room-capacity {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #718096;
  white-space: nowrap;
}

.room-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #718096;
  margin-bottom: 12px;
}

.room-features {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.feature-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 11px;
  color: #4a5568;
}

.room-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.collect-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #a0aec0;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  padding: 0 16px 16px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.book-btn {
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%); /* 替换紫色为蓝绿色渐变 */
  color: white;
}

.book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.3); /* 替换紫色为蓝色 */
}

.unfavorite-btn {
  width: 44px;
  background: #fed7d7;
  color: #e53e3e;
  border: 1px solid #feb2b2;
}

.unfavorite-btn:hover {
  background: #feb2b2;
  transform: scale(1.05);
}

/* 操作菜单 */
.action-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.action-menu {
  background: white;
  border-radius: 20px 20px 0 0;
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.menu-header h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.close-menu {
  width: 32px;
  height: 32px;
  border: none;
  background: #f7fafc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #718096;
}

.menu-items {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 16px 24px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  color: #2d3748;
}

.menu-item:hover {
  background: #f7fafc;
}

.menu-item.remove {
  color: #e53e3e;
}

.menu-item.remove:hover {
  background: #fed7d7;
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
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
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
  background: linear-gradient(135deg, #fc8181 0%, #f56565 100%);
  color: white;
}

.modal-btn.confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.3);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }

  .favorites-content {
    padding: 0 12px;
  }
}
</style>
