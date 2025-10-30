import request from '@/utils/request'

/**
 * 创建社区
 * @param {Object} data 社区请求数据
 * @returns {Promise}
 */
export function createCommunity(data) {
    return request({
        url: '/api/communities',
        method: 'post',
        data
    })
}

/**
 * 更新社区
 * @param {Number} id 社区ID
 * @param {Object} data 社区请求数据
 * @returns {Promise}
 */
export function updateCommunity(id, data) {
    return request({
        url: `/api/communities/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除社区
 * @param {Number} id 社区ID
 * @returns {Promise}
 */
export function deleteCommunity(id) {
    return request({
        url: `/api/communities/${id}`,
        method: 'delete'
    })
}

/**
 * 获取社区详情
 * @param {Number} id 社区ID
 * @returns {Promise}
 */
export function getCommunity(id) {
    return request({
        url: `/api/communities/${id}`,
        method: 'get'
    })
}

/**
 * 获取所有社区
 * @returns {Promise}
 */
export function getAllCommunities() {
    return request({
        url: '/api/communities',
        method: 'get'
    })
}

/**
 * 分页获取社区
 * @param {Number} current 当前页码，默认1
 * @param {Number} size 每页数量，默认10
 * @returns {Promise}
 */
export function getCommunitiesByPage(current = 1, size = 10) {
    return request({
        url: '/api/communities/page',
        method: 'get',
        params: { current, size }
    })
}

/**
 * 搜索社区
 * @param {String} keyword 搜索关键字
 * @returns {Promise}
 */
export function searchCommunities(keyword) {
    return request({
        url: '/api/communities/search',
        method: 'get',
        params: { keyword }
    })
}

/**
 * 获取社区及其教室
 * @param {Number} id 社区ID
 * @returns {Promise}
 */
export function getCommunityWithRooms(id) {
    return request({
        url: `/api/communities/${id}/rooms`,
        method: 'get'
    })
}

/**
 * 获取所有社区及其教室
 * @returns {Promise}
 */
export function getAllCommunitiesWithRooms() {
    return request({
        url: '/api/communities/with-rooms',
        method: 'get'
    })
}
