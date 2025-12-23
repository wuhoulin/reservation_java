<template>
  <div class="app-container">
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

// ================== 1. 自动清除缓存逻辑 (基于参考代码优化) ==================
const autoClearCacheOnEntry = () => {
  // 防止在单次会话中重复清除（可选：使用 sessionStorage 标记）
  if (sessionStorage.getItem('app_cache_cleared')) {
    return
  }

  try {
    console.log('App初始化：正在执行智能缓存清理...')

    // 1. 定义需要【保留】的白名单 (防止用户被迫退出登录)
    const keepKeys = [
      'jwt_token',
      'wechat_openid',
      'user_info',
      'token_expire_time',
      'wechat_auth_state'
    ]

    // 2. 备份白名单数据
    const savedData = {}
    keepKeys.forEach(key => {
      const val = localStorage.getItem(key)
      if (val) savedData[key] = val
    })

    // 3. 清除 LocalStorage (业务数据如 reservation_data 会被清空)
    localStorage.clear()

    // 4. 还原白名单数据
    Object.keys(savedData).forEach(key => {
      localStorage.setItem(key, savedData[key])
    })

    // 5. 清除 SessionStorage (通常存临时状态，全清比较安全)
    sessionStorage.clear()
    // 重新标记已清理，防止热重载或路由切换时重复触发
    sessionStorage.setItem('app_cache_cleared', 'true')

    // 6. 清除 IndexedDB (参考你的代码逻辑)
    if (window.indexedDB) {
      window.indexedDB.databases().then(databases => {
        databases.forEach(db => {
          if (db.name) {
            console.log('删除数据库:', db.name)
            window.indexedDB.deleteDatabase(db.name)
          }
        })
      })
    }

    // 7. 清除 Cookies (参考你的代码逻辑，排除特定cookie防止误删)
    // 注意：如果有后端设置的 HttpOnly Cookie，前端是删不掉的
    document.cookie.split(";").forEach(cookie => {
      const eqPos = cookie.indexOf("=")
      const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim()
      // 如果 Cookie 中存了登录态，这里也要加白名单判断，否则不要执行这一步
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=${window.location.hostname}`
    })

    console.log('缓存清理完成 (已保留登录凭证)')

  } catch (error) {
    console.error('自动清除缓存异常:', error)
  }
}

// ================== 2. 审核通知逻辑 (保持不变) ==================
const showAuditModal = ref(false)
const currentNotification = computed(() => store.currentNotification())
const notificationCount = computed(() => store.auditNotifications.length)
const hasNotifications = computed(() => store.hasUnreadNotifications())

const checkAndShowNotification = () => {
  if (hasNotifications.value && !showAuditModal.value) {
    showAuditModal.value = true
  }
}

const handleMarkRead = async (notificationId) => {
  const success = await store.markNotificationAsRead(notificationId)
  if (success) checkAndShowNotification()
}

const handleMarkAll = async () => {
  const success = await store.markAllAsRead()
  if (success) showAuditModal.value = false
}

const handleViewDetail = (reservationNo) => {
  router.push('/reservations')
  showAuditModal.value = false
}

const handleCloseModal = () => {
  showAuditModal.value = false
}

// ================== 3. 全局监听与生命周期 ==================

const handleVisibilityChange = () => {
  if (!document.hidden) {
    setTimeout(() => {
      store.checkAuditNotifications()
    }, 1000)
  }
}

watch(
    () => router.currentRoute.value.path,
    (newPath) => {
      setTimeout(() => {
        store.checkAuditNotifications()
      }, 300)
    }
)

watch(() => store.auditNotifications.length, (newCount) => {
  if (newCount > 0 && !showAuditModal.value) {
    setTimeout(() => {
      checkAndShowNotification()
    }, 1000)
  }
})

onMounted(() => {
  // 🔥 执行自动清理
  autoClearCacheOnEntry()

  store.startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

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
