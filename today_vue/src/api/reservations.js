import request from '@/utils/request'

/**
 * 获取我的预约列表
 * @param {number} status - 状态筛选（可选）
 * @returns {Promise}
 */
export function getMyReservations(status = null) {
    return request({
        url: '/api/reservations/user/reservations',
        method: 'get',
        params: { status }
    })
}

/**
 * 取消预约
 * @param {string} reservationNo - 预约编号
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function cancelReservation(reservationNo) {
    return request({
        url: `/api/reservations/${reservationNo}/cancel`,
        method: 'patch'
    })
}

/**
 * 重新提交被退回的预约
 * @param {number} reservationId - 预约ID
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function resubmitReservation(reservationId, userId) {
    return request({
        url: `/api/reservations/${reservationId}/resubmit`,
        method: 'patch',
        params: { userId }
    })
}


export const getReservationDetail = (reservationId) => {
    return request({
        url: `/api/reservations/${reservationId}`,
        method: 'get'
    })
}
