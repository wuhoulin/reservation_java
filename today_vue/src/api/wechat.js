import request from '@/utils/request'

// 生成微信授权URL
export function generateAuthUrl(redirectPath) {
    return request({
        url: '/api/wechat/generate-auth-url',
        method: 'get',
        params: { redirectPath }
    })
}

// 使用code换取openid - 修改为接收对象
export function exchangeCode(data) {
    return request({
        url: '/api/wechat/exchange-code',
        method: 'post',
        data: data  // 直接传递整个对象
    })
}

// 获取用户信息（如果需要）
export function getUserInfo(openid) {
    return request({
        url: '/api/wechat/user-info',
        method: 'get',
        params: { openid }
    })
}
