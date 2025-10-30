import request from '@/utils/request'

/**
 * 创建时间点
 * @param {Object} data 时间点请求数据
 * @returns {Promise}
 */
export function createTimePoint(data) {
    return request({
        url: '/api/time-points',
        method: 'post',
        data
    })
}

/**
 * 更新时间点
 * @param {Number} id 时间点ID
 * @param {Object} data 时间点请求数据
 * @returns {Promise}
 */
export function updateTimePoint(id, data) {
    return request({
        url: `/api/time-points/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除时间点
 * @param {Number} id 时间点ID
 * @returns {Promise}
 */
export function deleteTimePoint(id) {
    return request({
        url: `/api/time-points/${id}`,
        method: 'delete'
    })
}

/**
 * 获取时间点详情
 * @param {Number} id 时间点ID
 * @returns {Promise}
 */
export function getTimePoint(id) {
    return request({
        url: `/api/time-points/${id}`,
        method: 'get'
    })
}

/**
 * 获取所有时间点
 * @returns {Promise}
 */
export function getAllTimePoints() {
    return request({
        url: '/api/time-points',
        method: 'get'
    })
}

/**
 * 获取可用时间点
 * @returns {Promise}
 */
export function getAvailableTimePoints() {
    return request({
        url: '/api/time-points/available',
        method: 'get'
    })
}

/**
 * 获取教室在指定日期的可用时间点
 * @param {Number} roomId 教室ID
 * @param {String} date 日期 (格式: yyyy-MM-dd)
 * @returns {Promise}
 */
export function getAvailableTimePointsForRoom(roomId, date) {
    return request({
        url: '/api/time-points/available-for-room',
        method: 'get',
        params: { roomId, date }
    })
}
