// api/notification.js
import request from '@/utils/request'

// 获取通知列表
export function getNotifications(pageNum = 1, pageSize = 20) {
    return request({
        url: '/user/notification/list',
        method: 'get',
        params: {
            pageNum,
            pageSize
        }
    })
}

// 获取未读数量
export function getUnreadCount() {
    return request({
        url: '/user/notification/unread/count',
        method: 'get'
    })
}

// 标记为已读
export function markAsRead(id) {
    return request({
        url: `/user/notification/mark/${id}`,
        method: 'post'
    })
}

// 标记全部为已读
export function markAllAsRead() {
    return request({
        url: '/user/notification/mark/all',
        method: 'post'
    })
}

// 删除通知
export function deleteNotification(id) {
    return request({
        url: `/user/notification/${id}`,
        method: 'delete'
    })
}

// 清空所有通知
export function clearAllNotifications() {
    return request({
        url: '/user/notification/clear/all',
        method: 'delete'
    })
}

export function getUnreadAuditNotifications() {
    return request({
        url: '/user/notification/unread/audit',
        method: 'get'
    })
}
