import request from '@/utils/request'

/**
 * 提交反馈
 * @param {Object} data - 反馈数据
 * @returns {Promise}
 */
export function submitFeedback(data) {
    return request({
        url: '/api/feedback',
        method: 'post',
        data
    })
}

/**
 * 获取我的反馈列表
 * @returns {Promise}
 */
export function getMyFeedback() {
    return request({
        url: '/api/feedback',
        method: 'get'
    })
}

/**
 * 获取反馈详情
 * @param {number} id - 反馈ID
 * @returns {Promise}
 */
export function getFeedbackDetail(id) {
    return request({
        url: `/api/feedback/${id}`,
        method: 'get'
    })
}
