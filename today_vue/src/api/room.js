import request from '@/utils/request'

/**
 * 创建教室
 * @param {Object} data 教室请求数据
 * @returns {Promise}
 */
export function createRoom(data) {
    return request({
        url: '/api/rooms',
        method: 'post',
        data
    })
}

/**
 * 更新教室
 * @param {Number} id 教室ID
 * @param {Object} data 教室请求数据
 * @returns {Promise}
 */
export function updateRoom(id, data) {
    return request({
        url: `/api/rooms/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除教室
 * @param {Number} id 教室ID
 * @returns {Promise}
 */
export function deleteRoom(id) {
    return request({
        url: `/api/rooms/${id}`,
        method: 'delete'
    })
}

/**
 * 获取教室详情
 * @param {Number} id 教室ID
 * @returns {Promise}
 */
export function getRoom(id) {
    return request({
        url: `/api/rooms/${id}`,
        method: 'get'
    })
}

/**
 * 获取教室详细信息
 * @param {Number} id 教室ID
 * @returns {Promise}
 */
export function getRoomDetail(id) {
    return request({
        url: `/api/rooms/${id}/detail`,
        method: 'get'
    })
}

/**
 * 获取所有教室
 * @returns {Promise}
 */
export function getAllRooms() {
    return request({
        url: '/api/rooms',
        method: 'get'
    })
}

/**
 * 分页获取教室
 * @param {Number} current 当前页码，默认1
 * @param {Number} size 每页数量，默认10
 * @returns {Promise}
 */
export function getRoomsByPage(current = 1, size = 10) {
    return request({
        url: '/api/rooms/page',
        method: 'get',
        params: { current, size }
    })
}

/**
 * 获取社区下的所有教室
 * @param {Number} communityId 社区ID
 * @returns {Promise}
 */
export function getRoomsByCommunity(communityId) {
    return request({
        url: `/api/rooms/community/${communityId}`,
        method: 'get'
    })
}

/**
 * 搜索教室
 * @param {String} keyword 搜索关键字
 * @returns {Promise}
 */
export function searchRooms(keyword) {
    return request({
        url: '/api/rooms/search',
        method: 'get',
        params: { keyword }
    })
}

/**
 * 获取可用教室
 * @param {Number} communityId 社区ID
 * @param {String} date 日期 (格式: yyyy-MM-dd)
 * @param {Number} timeSlotId 时间段ID
 * @returns {Promise}
 */
export function getAvailableRooms(communityId, date, timeSlotId) {
    return request({
        url: '/api/rooms/available',
        method: 'get',
        params: { communityId, date, timeSlotId }
    })
}
