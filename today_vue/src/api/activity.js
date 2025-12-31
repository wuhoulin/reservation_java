import request from '@/utils/request'

// 获取活动列表
export function getActivityPage(params) {
    return request({
        url: '/api/activities/page',
        method: 'get',
        params: params
    })
}

// 获取活动详情
export function getActivityDetail(id) {
    return request({
        url: '/api/activities/' + id,
        method: 'get'
    })
}

// 报名活动 (无需传 userId，后端自动获取)
export function joinActivity(activityId) {
    return request({
        url: '/api/activity-signups/join',
        method: 'post',
        params: { activityId }
    })
}

// 取消报名 (无需传 userId)
export function cancelActivity(activityId) {
    return request({
        url: '/api/activity-signups/cancel',
        method: 'post',
        params: { activityId }
    })
}

// 检查是否已报名 (无需传 userId)
export function checkIsJoined(activityId) {
    return request({
        url: '/api/activity-signups/is-joined',
        method: 'get',
        params: { activityId }
    })
}

// 获取我的活动列表
export function getMyActivityPage(params) {
    return request({
        url: '/api/activity-signups/my-page',
        method: 'get',
        params: params
    })
}
