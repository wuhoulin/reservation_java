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

  if (!isWeChatBrowser()) {
    error.value = '请在微信中打开此链接'
    loading.value = false
    addDebugLog('非微信浏览器，停止授权流程')
    return
  }

  try {
    addDebugLog('请求后端生成授权URL...')
    const response = await generateAuthUrl('/auth-callback')

    addDebugLog(`后端响应状态: ${response.status}`)
    addDebugLog(`后端响应数据: ${JSON.stringify(response.data)}`)

    const authUrl = response.data?.authUrl || response.authUrl
    const state = response.data?.state || response.state

    if (!authUrl) {
      throw new Error('未获取到授权URL')
    }

    localStorage.setItem('wechat_auth_state', state)
    addDebugLog(`保存state到本地: ${state}`)
    addDebugLog(`即将跳转到微信授权页面: ${authUrl}`)

    // 立即跳转，不延迟
    window.location.href = authUrl

  } catch (err) {
    addDebugLog(`获取授权URL失败: ${err.message}`)
    addDebugLog(`错误详情: ${JSON.stringify(err.response?.data)}`)
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
