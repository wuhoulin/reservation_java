<template>
  <div class="wechat-auth-container">
    <div class="auth-card">
      <h2>预约系统</h2>
      <p>正在为您进行微信授权...</p>

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

// 添加调试日志
const addDebugLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  debugInfo.value += `[${timestamp}] ${message}\n`
  console.log(`[WeChatAuth] ${message}`)
}

// 🌟 核心新增：彻底清理缓存的方法
// 这与你个人中心页的 handleClearCache 逻辑保持一致
const clearAllCache = () => {
  addDebugLog('开始执行前置缓存清理...')

  try {
    // 1. 定义需要清除的 localStorage 键名列表
    // 包含认证信息、用户信息、以及业务数据缓存
    const itemsToRemove = [
      // 认证核心数据
      'wechat_openid',
      'jwt_token',
      'user_info',
      'token_expire_time',
      'wechat_auth_state',
      'wechat_auth_scope',

      // 业务数据缓存 (对应你个人中心页清理的内容)
      'reservation_data',
      'community_data',
      'unread_messages'
    ]

    // 2. 循环清除
    itemsToRemove.forEach(item => {
      const exists = localStorage.getItem(item)
      if (exists) {
        localStorage.removeItem(item)
        addDebugLog(`已清除旧缓存: ${item}`)
      }
    })

    // 3. 清除 SessionStorage (防止会话残留)
    sessionStorage.clear()
    addDebugLog('SessionStorage 已清空')

  } catch (err) {
    addDebugLog(`缓存清理过程出现警告: ${err.message}`)
  }
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

    const authUrl = response.data?.authUrl || response.authUrl
    const state = response.data?.state || response.state
    const scope = response.data?.scope || response.scope

    if (!authUrl) {
      throw new Error('未获取到授权URL')
    }

    if (!authUrl.includes('open.weixin.qq.com/connect/oauth2')) {
      throw new Error(`授权URL非法：${authUrl}`)
    }

    // 存储新的授权状态
    localStorage.setItem('wechat_auth_state', state)
    localStorage.setItem('wechat_auth_scope', scope)

    addDebugLog(`保存新state: ${state}`)
    addDebugLog(`即将跳转: ${authUrl}`)

    setTimeout(() => {
      window.location.href = authUrl
    }, 800)

  } catch (err) {
    addDebugLog(`获取授权URL失败: ${err.message}`)
    error.value = err.message || '授权失败，请重试'
    loading.value = false
  }
}

const retryAuth = () => {
  addDebugLog('用户点击重试')
  loading.value = true
  error.value = ''

  // 重试时也先清理缓存
  clearAllCache()
  startWeChatAuth()
}

onMounted(() => {
  // 🌟 页面加载时，第一步就是调用清理函数
  clearAllCache()

  addDebugLog('环境清理完毕，启动授权...')
  startWeChatAuth()
})
</script>

<style scoped>
/* 样式保持不变 */
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
.loading-spinner { margin: 20px 0; }
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
.error-message { color: #e74c3c; }
.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}
.retry-btn, .debug-btn {
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}
.retry-btn { background: #3498db; }
.debug-btn { background: #6c757d; }
</style>
