<template>
  <div class="callback-container">
    <div v-if="loading" class="loading-spinner">
      <div class="spinner"></div>
      <p>正在获取用户信息...</p>
    </div>

    <div v-else-if="success" class="success-message">
      <div class="success-icon">✓</div>
      <h3>授权成功！</h3>
      <p>正在跳转到预约系统...</p>
    </div>

    <div v-else class="error-message">
      <div class="error-icon">✗</div>
      <h3>授权失败</h3>
      <p>{{ error }}</p>

      <!-- 添加详细的调试信息 -->
      <div v-if="showDebug" class="debug-info">
        <h4>调试信息</h4>
        <p><strong>当前URL:</strong> {{ currentUrl }}</p>
        <p><strong>URL参数:</strong> {{ urlParams }}</p>
        <p><strong>解析的code:</strong> {{ debugCode }}</p>
        <p><strong>解析的state:</strong> {{ debugState }}</p>
        <p><strong>本地存储的state:</strong> {{ storedState }}</p>
        <pre>{{ debugLog }}</pre>
      </div>

      <div class="action-buttons">
        <button @click="retryAuth" class="retry-btn">重新授权</button>
        <button @click="showDebug = !showDebug" class="debug-btn">
          {{ showDebug ? '隐藏' : '显示' }}调试信息
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { exchangeCode } from '@/api/wechat'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const success = ref(false)
const error = ref('')
const showDebug = ref(false)
const currentUrl = ref('')
const urlParams = ref('')
const debugCode = ref('')
const debugState = ref('')
const storedState = ref('')
const debugLog = ref('')

// 添加调试日志
const addDebugLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  debugLog.value += `[${timestamp}] ${message}\n`
  console.log(`[AuthCallback] ${message}`)
}

const handleAuthCallback = async () => {
  // 记录调试信息
  currentUrl.value = window.location.href
  urlParams.value = window.location.search
  storedState.value = localStorage.getItem('wechat_auth_state')

  // 获取URL中的code和state参数
  const urlParamsObj = new URLSearchParams(window.location.search)
  const code = urlParamsObj.get('code')
  const state = urlParamsObj.get('state')

  debugCode.value = code
  debugState.value = state

  addDebugLog('=== 前端回调调试信息 ===')
  addDebugLog(`完整URL: ${window.location.href}`)
  addDebugLog(`URL参数: ${JSON.stringify(Object.fromEntries(urlParamsObj.entries()))}`)
  addDebugLog(`获取到的code: ${code}`)
  addDebugLog(`获取到的state: ${state}`)
  addDebugLog(`本地存储的state: ${storedState.value}`)
  addDebugLog('=======================')

  if (!code) {
    error.value = '未获取到授权码，请重新尝试'
    loading.value = false
    addDebugLog('错误: 未获取到授权码')
    return
  }

  try {
    // 确保正确传递参数
    const requestData = {
      code: code,
      state: state
    }

    addDebugLog(`发送到后端的参数: ${JSON.stringify(requestData)}`)
    addDebugLog('开始调用exchangeCode API...')

    const response = await exchangeCode(requestData)

    // 添加详细的响应日志
    addDebugLog('=== 后端响应详细信息 ===')
    addDebugLog(`响应对象类型: ${typeof response}`)
    addDebugLog(`响应对象: ${JSON.stringify(response)}`)

    // 关键修复：智能解析响应数据
    const responseData = response?.data || response

    addDebugLog(`解析后的响应数据: ${JSON.stringify(responseData)}`)
    addDebugLog(`响应数据success字段: ${responseData?.success}`)
    addDebugLog(`响应数据openid字段: ${responseData?.openid}`)
    addDebugLog(`响应数据token字段: ${responseData?.token}`)
    addDebugLog('========================')

    // 在控制台也输出详细日志
    console.log('🔍 完整响应对象:', response)
    console.log('🔍 解析后的数据:', responseData)
    console.log('🔍 token字段:', responseData?.token)

    // 检查响应数据
    if (responseData && responseData.success) {
      const openid = responseData.openid
      const token = responseData.token
      const userInfo = responseData.userInfo || {}

      addDebugLog(`成功获取openid: ${openid}`)
      addDebugLog(`成功获取token: ${token ? '有值' : '无值'}`)
      addDebugLog(`用户信息: ${JSON.stringify(userInfo)}`)

      if (!token) {
        throw new Error('未获取到认证token')
      }

      // 存储认证信息和用户信息
      localStorage.setItem('jwt_token', token)
      localStorage.setItem('wechat_openid', openid)
      localStorage.setItem('user_info', JSON.stringify(userInfo))

      // 设置token过期时间
      if (responseData.expiresIn) {
        const expireTime = Date.now() + (responseData.expiresIn * 1000)
        localStorage.setItem('token_expire_time', expireTime.toString())
      }

      localStorage.removeItem('wechat_auth_state')
      localStorage.removeItem('wechat_auth_scope')

      addDebugLog('用户信息和token已保存到本地存储')
      addDebugLog(`用户昵称: ${userInfo.nickname || '未获取'}`)
      addDebugLog(`用户头像: ${userInfo.headimgurl || '未获取'}`)

      success.value = true
      loading.value = false

      addDebugLog('授权成功，准备跳转...')

      // 修改跳转逻辑：优先使用redirect参数，如果没有则跳转到社区列表
      let redirectPath = '/community-list' // 默认跳转到社区列表

      // 检查是否有重定向参数
      if (route.query.redirect) {
        redirectPath = route.query.redirect
        addDebugLog(`使用重定向参数: ${redirectPath}`)
      } else {
        addDebugLog(`使用默认跳转路径: ${redirectPath}`)
      }

      addDebugLog(`最终跳转目标: ${redirectPath}`)

      // 添加更详细的跳转日志
      addDebugLog('开始执行路由跳转...')

      setTimeout(() => {
        addDebugLog(`正在跳转到: ${redirectPath}`)
        router.push(redirectPath).then(() => {
          addDebugLog('路由跳转成功')
        }).catch((err) => {
          addDebugLog(`路由跳转失败: ${err.message}`)
          console.error('路由跳转错误:', err)
          // 如果跳转失败，尝试跳转到社区列表
          router.push('/community-list')
        })
      }, 1500)
    }else {
      const errorMessage = responseData?.message || '获取用户信息失败'
      addDebugLog(`业务逻辑失败: ${errorMessage}`)
      throw new Error(errorMessage)
    }
  } catch (err) {
    // 添加详细的错误日志
    addDebugLog('=== 错误详细信息 ===')
    addDebugLog(`错误名称: ${err.name}`)
    addDebugLog(`错误消息: ${err.message}`)
    addDebugLog(`错误堆栈: ${err.stack}`)
    addDebugLog(`错误响应: ${JSON.stringify(err.response)}`)
    addDebugLog(`错误请求: ${JSON.stringify(err.request)}`)
    addDebugLog(`错误配置: ${JSON.stringify(err.config)}`)
    addDebugLog('===================')

    console.log('💥 完整错误对象:', err)
    console.log('💥 错误消息:', err.message)

    error.value = err.response?.data?.message || err.message || '处理授权信息失败'
    loading.value = false
  }
}

const retryAuth = () => {
  addDebugLog('重新开始授权流程')
  // 跳转回授权页面重新开始
  router.push('/wechat-auth')
}

onMounted(() => {
  addDebugLog('回调页面加载完成')
  handleAuthCallback()
})
</script>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.loading-spinner,
.success-message,
.error-message {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 500px;
  width: 100%;
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

.success-icon,
.error-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.success-icon {
  color: #07c160;
}

.error-icon {
  color: #e74c3c;
}

.debug-info {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 5px;
  padding: 15px;
  margin: 15px 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  text-align: left;
}

.debug-info h4 {
  margin-top: 0;
  color: #6c757d;
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 5px;
}

.debug-info pre {
  white-space: pre-wrap;
  word-break: break-all;
  background: #fff;
  padding: 10px;
  border-radius: 3px;
  border: 1px solid #e9ecef;
  margin-top: 10px;
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

h3 {
  color: #333;
  margin-bottom: 10px;
}

p {
  color: #666;
  margin-bottom: 10px;
}
</style>
