import axios from 'axios';
import { ElMessage } from "element-plus";
import { getCurrentInstance } from 'vue';

// 创建一个axios实例
const request = axios.create({
    // ✅ 方式一：使用相对路径 (推荐)
    // 浏览器会自动使用当前域名(https://ndnu-yuyue.xyz)拼接
    // 配合 Nginx 的 location /wechat/ 代理规则
    baseURL: '/wechat',

    // ✅ 方式二：写死 HTTPS 域名
    // baseURL: 'https://ndnu-yuyue.xyz/wechat',

    timeout: 5000,
    withCredentials: true,
});
// 请求拦截器
request.interceptors.request.use((config) => {
    const token = localStorage.getItem("jwt_token");
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token;
    }

    if (config.data instanceof FormData) {
        config.headers['Content-Type'] = 'multipart/form-data';
    }

    if (token) {
        console.log(`🔐 携带Token: ${token.substring(0, 20)}...`);
    }

    return config;
}, (error) => {
    return Promise.reject(error);
});

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        // 根据 Content-Type 判断是否解析为 JSON
        if (response.headers['content-type'] && response.headers['content-type'].includes('application/json')) {
            let res = response.data;
            // 兼容服务端返回的字符串数据
            if (typeof res === 'string') {
                res = res ? JSON.parse(res) : res;
            }


            return res;
        } else {
            // 返回原始响应数据
            console.log(`✅ 请求成功: ${response.config.url}`, response.data);
            return response;
        }
    },
    async (error) => {

        // 检查是否为 401 状态码（未授权/Token失效）
        if (error.response && error.response.status === 401) {
            console.log('🔐 Token已失效，跳转到登录页面');

            // 清除本地存储的token和用户信息
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_info');
            localStorage.removeItem('token_expire_time');
            localStorage.removeItem('wechat_openid');

            const appContext = getCurrentInstance();
            if (appContext) {
                const router = appContext.appContext.config.globalProperties.$router;
                try {
                    // 直接跳转到微信授权页面
                    await router.push('/wechat-auth');
                    ElMessage.error('登录已过期，请重新授权');
                } catch (err) {
                    console.error('页面跳转失败:', err);
                    // 如果路由跳转失败，使用原生跳转
                    window.location.href = '/wechat-auth';
                }
            } else {
                // 如果没有Vue实例，使用原生跳转
                window.location.href = '/wechat-auth';
            }
        } else if (error.response && error.response.status >= 500) {
            ElMessage.error('服务器错误，请稍后重试');
        } else if (error.code === 'ECONNABORTED') {
            ElMessage.error('请求超时，请检查网络连接');
        } else if (error.response) {
            // 其他错误状态码
            const message = error.response.data?.message || error.response.data?.msg || '请求失败';
            ElMessage.error(message);
        } else {
            ElMessage.error('网络错误，请检查网络连接');
        }

        return Promise.reject(error);
    }
);

export default request;
