import request from '@/utils/request'

// 生成微信授权URL
export function generateAuthUrl(redirectPath, scope = 'snsapi_userinfo') {
    return request({
        url: '/api/wechat/generate-auth-url',
        method: 'get',
        params: {
            redirectPath,
            scope
        }
    })
}

// 使用code换取openid和用户信息
export function exchangeCode(data) {
    return request({
        url: '/api/wechat/exchange-code',
        method: 'post',
        data: data
    })
}

// 获取用户详细信息（如果需要单独获取）
export function getUserInfo(openid) {
    return request({
        url: '/api/wechat/user-info',
        method: 'get',
        params: { openid }
    })
}

// 刷新token
export function refreshToken() {
    return request({
        url: '/api/wechat/refresh-token',
        method: 'post'
    })
}
