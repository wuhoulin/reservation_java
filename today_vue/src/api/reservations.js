import request from '@/utils/request'
/**
 * 根据用户ID查询预约记录
 * @param {string} userId - 用户ID
 * @param {number} [status] - 可选状态筛选
 * @returns {Promise}
 */
export function getReservationsByUserId(studentId, status = null) {
    return request({
        url: `/api/reservations/user/${studentId}`,
        method: 'get',
        params: {
            status: status || undefined // 如果status为null则不发这个参数
        }
    })
}

/**
 * 查询用户最新的3条预约记录
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function getLatestReservations() {
    return request({
        url: '/api/reservations/latest',
        method: 'get'
    })
}


/**
 * 获取用户的所有预约记录
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getUserReservations(params = {}) {
    return request({
        url: '/api/reservations/user/reservations',
        method: 'get',
        params
    })
}

/**
 * 取消预约
 * @param {String} reservationNo - 预约编号
 * @param {String} userId - 用户ID
 * @returns {Promise}
 */
export function cancelUserReservation(reservationNo, userId) {
    return request({
        url: `/api/reservations/${reservationNo}/cancel`,
        method: 'patch',
        params: { userId }
    })
}

/**
 * 重新提交预约
 * @param {Number} reservationId - 预约ID
 * @param {String} userId - 用户ID
 * @returns {Promise}
 */
export function resubmitUserReservation(reservationId, userId) {
    return request({
        url: `/api/reservations/${reservationId}/resubmit`,
        method: 'patch',
        params: { userId }
    })
}

/**
 * 获取预约详情
 * @param {String} reservationId - 预约ID
 * @returns {Promise}
 */
export function getReservationDetail(reservationId) {
    return request({
        url: `/api/reservations/${reservationId}`,
        method: 'get'
    })
}
