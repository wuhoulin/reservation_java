// stores/notification.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadAuditNotifications, markAsRead } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
    // 状态
    const auditNotifications = ref([]) // 未读审核通知
    const checkedIds = ref(new Set()) // 已经检查过的通知ID（避免重复弹窗）
    const isPolling = ref(false)
    const pollTimer = ref(null)

    // 动作
    const checkAuditNotifications = async () => {
        if (isPolling.value) return

        isPolling.value = true
        try {
            const response = await getUnreadAuditNotifications()

            if (response.code === 200 && response.data) {
                const notifications = response.data.notifications || []

                // 过滤掉已经检查过的通知
                const newNotifications = notifications.filter(
                    item => !checkedIds.value.has(item.id)
                )

                // 记录已经检查过的通知ID
                newNotifications.forEach(item => {
                    checkedIds.value.add(item.id)
                })

                // 更新通知列表
                if (newNotifications.length > 0) {
                    auditNotifications.value = [...newNotifications, ...auditNotifications.value]
                }
            }
        } catch (error) {
            console.error('检查审核通知失败:', error)
        } finally {
            isPolling.value = false
        }
    }

    const markNotificationAsRead = async (id) => {
        try {
            await markAsRead(id)

            // 从列表中移除
            auditNotifications.value = auditNotifications.value.filter(
                item => item.id !== id
            )

            return true
        } catch (error) {
            console.error('标记已读失败:', error)
            return false
        }
    }

    const markAllAsRead = async () => {
        try {
            // 这里需要调用批量标记的接口，暂时先逐个标记
            const promises = auditNotifications.value.map(item =>
                markAsRead(item.id)
            )
            await Promise.all(promises)

            // 清空列表
            auditNotifications.value = []
            return true
        } catch (error) {
            console.error('标记全部已读失败:', error)
            return false
        }
    }

    const startPolling = () => {
        stopPolling()

        // 立即检查一次
        checkAuditNotifications()

        // 每30秒检查一次
        pollTimer.value = setInterval(() => {
            checkAuditNotifications()
        }, 30000) // 30秒
    }

    const stopPolling = () => {
        if (pollTimer.value) {
            clearInterval(pollTimer.value)
            pollTimer.value = null
        }
    }

    const clearCheckedIds = () => {
        checkedIds.value.clear()
    }

    return {
        // 状态
        auditNotifications,
        isPolling,

        // 计算属性
        hasUnreadNotifications: () => auditNotifications.value.length > 0,
        currentNotification: () => auditNotifications.value[0] || null,

        // 动作
        checkAuditNotifications,
        markNotificationAsRead,
        markAllAsRead,
        startPolling,
        stopPolling,
        clearCheckedIds
    }
})
