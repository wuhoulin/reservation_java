import request from '@/utils/request'

/**
 * 创建预约
 * @param data
 * @returns {*}
 */
export function createReservation(data) {
    return request({
        url: '/api/reservations',
        method: 'post',
        data
    })
}


/**
 * 查询教室预约状态
 * @param {number} roomId 教室ID
 * @param {string} [date] 可选日期，格式为YYYY-MM-DD
 * @returns {*}
 */
export function getRoomReservationStatus(roomId, date = null) {
    return request({
        url: `/api/reservations/room/${roomId}/status`,
        method: 'get',
        params: {
            date: date || undefined // 如果date为null则不发这个参数
        }
    })
}

/**
 * 取消预约
 * @param {string} reservationNo - 预约编号
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function cancelReservation(reservationNo, userId) {
    return request({
        url: `/api/reservations/${reservationNo}/cancel`,
        method: 'patch',
        params: { userId }
    });
}

