import { useRouter } from 'vue-router'


// 获取token
export function getToken() {
    return localStorage.getItem('jwt_token')
}

// 设置token
export function setToken(token) {
    localStorage.setItem('jwt_token', token)
}

// 移除token
export function removeToken() {
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('user_info')
    localStorage.removeItem('token_expire_time')
    localStorage.removeItem('wechat_openid')
}

// 检查token是否有效
export function isTokenValid() {
    const token = getToken()
    if (!token) return false

    const expireTime = localStorage.getItem('token_expire_time')
    if (expireTime && Date.now() > parseInt(expireTime)) {
        removeToken()
        return false
    }

    return true
}

// 获取用户信息
export function getUserInfo() {
    const userInfoStr = localStorage.getItem('user_info')
    try {
        return userInfoStr ? JSON.parse(userInfoStr) : null
    } catch {
        return null
    }
}

// 获取openid
export function getOpenid() {
    return localStorage.getItem('wechat_openid')
}
