import request from '@/utils/request'

/**
 * 获取当前需要签到的预约任务
 * @returns {Promise}
 */
export function getCurrentCheckInTask() {
    return request({
        url: '/api/reservations/current-check-in',
        method: 'get'
    })
}

/**
 * 提交现场签到
 * @param {Object} data
 * @param {number} data.reservationId 预约ID
 * @param {number} data.longitude 经度
 * @param {number} data.latitude 纬度
 * @returns {Promise}
 */
export function submitCheckIn(data) {
    return request({
        url: '/api/reservations/check-in',
        method: 'post',
        data
    })
}

/**
 * 获取当前签到状态（包含可签到任务或未来任务）
 * @returns {Promise} 返回 { state, taskInfo, countdownMs }
 */
export function getCheckInState() {
    return request({
        url: '/api/reservations/check-in-state', // 🟢 修改此处
        method: 'get'
    })
}
