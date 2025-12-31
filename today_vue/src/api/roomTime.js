import request from '@/utils/request'

/**
 * 获取所有时间点（调用后端 /api/time-points）
 * @returns {Promise}
 */
export function getAllTimePoints() {
    return request({
        url: '/api/time-points',
        method: 'get'
    })
}

/**
 * 获取教室在指定日期的可用时间点（调用后端 /api/time-points/available-for-room）
 * @param {Object} params - { roomId: 教室ID, date: 日期（yyyy-MM-dd） }
 * @returns {Promise}
 */
export function getAvailableTimePointsForRoom(params) {
    return request({
        url: '/api/time-points/available-for-room',
        method: 'get',
        params
    })
}
