import request from '@/utils/request'

// 获取用户个人信息
export function getUserProfile() {
    return request({
        url: '/api/user/profile',
        method: 'get'
    })
}

// 更新用户个人信息
export function updateUserProfile(data) {
    return request({
        url: '/api/user/profile',
        method: 'put',
        data
    })
}
