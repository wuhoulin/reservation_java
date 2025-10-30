import axios from 'axios';
import { ElMessage } from "element-plus";
import { getCurrentInstance } from 'vue';

// 创建一个axios实例
const request = axios.create({
    baseURL: 'http://localhost:9072',
    timeout: 5000,
    withCredentials: true,
});

// 请求拦截器
request.interceptors.request.use((config) => {
    // 获取用户信息并设置token到请求头
    const user = localStorage.getItem("userInfo");
    if (user) {
        config.headers['token'] = JSON.parse(user).token;
    }

    // 如果需要发送表单数据或文件，则设置Content-Type为multipart/form-data
    if (config.data instanceof FormData) {
        config.headers['Content-Type'] = 'multipart/form-data';
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
            return response;
        }
    },
    async (error) => {
        console.log('err' + error); // for debug

        // 检查是否为 401 状态码
        if (error.response && error.response.status === 401) {
            const appContext = getCurrentInstance();
            if (appContext) {
                const router = appContext.appContext.config.globalProperties.$router;
                try {
                    // 直接跳转到登录页面
                    await router.push('/');
                    ElMessage.error('未登录，请先登录');
                } catch (err) {
                    console.error('页面跳转失败:', err);
                }
            }
        }

        return Promise.reject(error);
    }
);


export default request;
