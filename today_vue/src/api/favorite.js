import request from '@/utils/request'

/**
 * 添加收藏
 * @param {Object} data - 收藏数据
 * @param {number} data.roomId - 教室ID
 * @returns {Promise}
 */
export function addFavorite(data) {
    return request({
        url: '/api/favorites',
        method: 'post',
        data
    })
}

/**
 * 取消收藏
 * @param {number} roomId - 教室ID
 * @returns {Promise}
 */
export function removeFavorite(roomId) {
    return request({
        url: `/api/favorites/${roomId}`,
        method: 'delete'
    })
}

/**
 * 检查是否收藏
 * @param {number} roomId - 教室ID
 * @returns {Promise}
 */
export function checkFavorite(roomId) {
    return request({
        url: `/api/favorites/check/${roomId}`,
        method: 'get'
    })
}


/**
 * 获取收藏列表
 * @returns {Promise}
 */
export function getFavorites() {
    return request({
        url: '/api/favorites',
        method: 'get'
    })
}

