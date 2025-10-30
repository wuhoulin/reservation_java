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
export function getLatestReservations(userId) {
    return request({
        url: '/api/reservations/latest',
        method: 'get',
        params: { userId }
    })
}
