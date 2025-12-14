<!-- App.vue -->
<template>
  <div class="app-container">
    <!-- 审核通知弹窗 -->
    <AuditNotificationModal
        v-if="showAuditModal && currentNotification"
        v-model:visible="showAuditModal"
        :notification="currentNotification"
        :notification-count="notificationCount"
        @mark-read="handleMarkRead"
        @mark-all="handleMarkAll"
        @view-detail="handleViewDetail"
        @close="handleCloseModal"
    />

    <router-view></router-view>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AuditNotificationModal from '@/components/AuditNotificationModal.vue'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const store = useNotificationStore()

const showAuditModal = ref(false)

// 计算属性
const currentNotification = computed(() => store.currentNotification())
const notificationCount = computed(() => store.auditNotifications.length)
const hasNotifications = computed(() => store.hasUnreadNotifications())

// 检查并显示通知
const checkAndShowNotification = () => {
  if (hasNotifications.value && !showAuditModal.value) {
    showAuditModal.value = true
  }
}

// 处理标记已读
const handleMarkRead = async (notificationId) => {
  const success = await store.markNotificationAsRead(notificationId)
  if (success) {
    // 如果还有通知，继续显示下一个
    checkAndShowNotification()
  }
}

// 处理全部标记已读
const handleMarkAll = async () => {
  const success = await store.markAllAsRead()
  if (success) {
    showAuditModal.value = false
  }
}

// 处理查看详情
const handleViewDetail = (reservationNo) => {
  router.push(`/reservations/detail/${reservationNo}`)
  showAuditModal.value = false
}

// 处理关闭弹窗
const handleCloseModal = () => {
  showAuditModal.value = false
}

// 处理页面可见性变化
const handleVisibilityChange = () => {
  if (!document.hidden) {
    // 页面变得可见时，延迟1秒检查通知
    setTimeout(() => {
      store.checkAuditNotifications()
    }, 1000)
  }
}

// 处理路由变化
const handleRouteChange = () => {
  // 路由变化时检查通知
  setTimeout(() => {
    store.checkAuditNotifications()
  }, 300)
}

// 监听通知变化
watch(() => store.auditNotifications.length, (newCount) => {
  if (newCount > 0 && !showAuditModal.value) {
    // 延迟显示，避免在页面加载时立即弹窗
    setTimeout(() => {
      checkAndShowNotification()
    }, 1000)
  }
})

// 组件挂载时
onMounted(() => {
  // 开始轮询
  store.startPolling()

  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)

  // 监听路由变化
  router.afterEach(handleRouteChange)
})

// 组件卸载时
onUnmounted(() => {
  store.stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style>
.app-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  max-width: 100%;
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>
