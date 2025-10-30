import { useRouter } from 'vue-router'

// 配置常量（直接硬编码）
const CAS_CONFIG = {
    CAS_SERVER_URL: 'http://authserver.ndnu.edu.cn/authserver',  // CAS 服务器地址
    API_BASE_URL: 'http://210.34.24.34:40080',                  // 后端接口基础地址
    CALLBACK_PATH: '/comprehensiveQualityApi/user/loginByCAS',  // 回调接口路径
    FRONTEND_URL: 'http://localhost:9072'                       // 前端地址（用于重定向）
}

// 跳转到 CAS 登录页
export const redirectToCAS = () => {
    // 拼接完整的回调地址（后端接口）
    const serviceUrl = encodeURIComponent(
        `${CAS_CONFIG.API_BASE_URL}${CAS_CONFIG.CALLBACK_PATH}`
    )
    window.location.href = `${CAS_CONFIG.CAS_SERVER_URL}/login?service=${serviceUrl}`
}

// 处理登录成功后的 Token（从 URL 获取）
export const handleCASCallback = () => {
    const router = useRouter()
    const urlParams = new URLSearchParams(window.location.search)
    const token = urlParams.get('token')

    if (token) {
        // 存储 Token（示例用 localStorage）
        localStorage.setItem('auth_token', token)

        // 清除 URL 中的 token
        window.history.replaceState({}, '', window.location.pathname)

        // 跳转到首页
        router.push('/')
    }
}
