<template>
  <div class="wechat-auth-container">
    <div class="auth-card">
      <h2>预约系统</h2>
      <p>正在为您进行微信授权...</p>

      <!-- 添加调试信息显示 -->
      <div v-if="showDebug" class="debug-info">
        <h3>调试信息</h3>
        <pre>{{ debugInfo }}</pre>
      </div>

      <div v-if="loading" class="loading-spinner">
        <div class="spinner"></div>
        <p>授权处理中</p>
      </div>

      <div v-else-if="error" class="error-message">
        <h3>授权失败</h3>
        <p>{{ error }}</p>
        <div class="action-buttons">
          <button @click="retryAuth" class="retry-btn">重新授权</button>
          <button @click="showDebug = !showDebug" class="debug-btn">
            {{ showDebug ? '隐藏' : '显示' }}调试信息
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { generateAuthUrl } from '@/api/wechat'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const error = ref('')
const debugInfo = ref('')
const showDebug = ref(false)

// 添加调试日志函数
const addDebugLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  debugInfo.value += `[${timestamp}] ${message}\n`
  console.log(`[WeChatAuth] ${message}`)
}

const isWeChatBrowser = () => {
  const ua = navigator.userAgent.toLowerCase()
  const isWeChat = ua.indexOf('micromessenger') > -1

  addDebugLog(`UserAgent: ${navigator.userAgent}`)
  addDebugLog(`是否微信浏览器: ${isWeChat}`)
  return isWeChat
}

const startWeChatAuth = async () => {
  addDebugLog('开始微信授权流程')
  addDebugLog(`当前路由: ${route.path}`)
  addDebugLog(`路由参数: ${JSON.stringify(route.query)}`)

  // 再次确认清除旧缓存（双重保障）
  localStorage.removeItem('wechat_auth_state')
  localStorage.removeItem('wechat_auth_scope')

  if (!isWeChatBrowser()) {
    error.value = '请在微信中打开此链接'
    loading.value = false
    addDebugLog('非微信浏览器，停止授权流程')
    return
  }

  try {
    addDebugLog('请求后端生成授权URL...')

    // 使用snsapi_userinfo范围获取用户信息
    const response = await generateAuthUrl('/auth-callback', 'snsapi_userinfo')

    addDebugLog(`后端响应状态: ${response.status}`)
    addDebugLog(`后端响应数据: ${JSON.stringify(response.data)}`)

    const authUrl = response.data?.authUrl || response.authUrl
    const state = response.data?.state || response.state
    const scope = response.data?.scope || response.scope

    if (!authUrl) {
      throw new Error('未获取到授权URL')
    }

    // 关键校验：确保是微信官方授权链接（防止后端返回错误URL）
    if (!authUrl.includes('open.weixin.qq.com/connect/oauth2')) {
      throw new Error(`授权URL非法，必须是微信官方链接：${authUrl}`)
    }

    // 存储授权状态（用于回调页校验）
    localStorage.setItem('wechat_auth_state', state)
    localStorage.setItem('wechat_auth_scope', scope)

    addDebugLog(`保存state到本地: ${state}`)
    addDebugLog(`授权范围: ${scope}`)
    addDebugLog(`即将跳转到微信授权页面: ${authUrl}`)

    // 延迟跳转（避免浏览器拦截，同时让用户看到授权页提示）
    setTimeout(() => {
      window.location.href = authUrl // 跳转到微信官方授权页
    }, 800) // 800ms延迟，足够用户看到"授权处理中"提示

  } catch (err) {
    addDebugLog(`获取授权URL失败: ${err.message}`)
    addDebugLog(`错误详情: ${JSON.stringify(err.response?.data || err.stack)}`)
    error.value = err.message || '授权失败，请重试'
    loading.value = false
  }
}

const retryAuth = () => {
  addDebugLog('重新尝试授权')
  loading.value = true
  error.value = ''
  debugInfo.value = ''
  startWeChatAuth()
}

onMounted(() => {
  // 初始化时清除所有相关旧缓存（核心修复：避免残留参数干扰流程）
  localStorage.removeItem('wechat_auth_state')
  localStorage.removeItem('wechat_auth_scope')
  localStorage.removeItem('wechat_openid')
  localStorage.removeItem('jwt_token')
  localStorage.removeItem('user_info')
  localStorage.removeItem('token_expire_time')

  addDebugLog('页面加载完成，开始授权流程')
  startWeChatAuth()
})
</script>

<style scoped>
.wechat-auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.auth-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.debug-info {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 5px;
  padding: 15px;
  margin: 15px 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
  text-align: left;
}

.loading-spinner {
  margin: 20px 0;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #07c160;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #e74c3c;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}

.retry-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.retry-btn:hover {
  background: #2980b9;
}

.debug-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.debug-btn:hover {
  background: #545b62;
}

h2 {
  color: #333;
  margin-bottom: 10px;
}

p {
  color: #666;
  margin-bottom: 20px;
}
</style>
