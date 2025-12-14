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
 * 获取教室在指定日期的可用时间点（修复参数传递格式）
 * @param {Object} params - 平级参数对象
 * @param {Number} params.roomId - 教室ID（必须为数字类型）
 * @param {String} params.date - 日期（格式：yyyy-MM-dd）
 * @returns {Promise}
 */
export function getAvailableTimePointsForRoom(params) {
    return request({
        url: '/api/time-points/available-for-room',
        method: 'get',
        params: params // 直接传递平级参数，不嵌套（关键修复）
    })
}

export const getAvailableTimePoints = (params) => {
    return request({
        url: '/api/roomReserveDate/available',
        method: 'get',
        params
    });
};
