<template>
  <div class="callback-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <transition name="fade-scale" mode="out-in">
      <div v-if="loading" key="loading" class="auth-card loading-card">
        <div class="loader-ring">
          <div></div><div></div><div></div><div></div>
        </div>
        <p class="status-text">正在安全连接...</p>
        <p class="sub-text">获取用户信息中</p>
      </div>

      <div v-else-if="success" key="success" class="auth-card success-card">
        <div class="avatar-glow">
          <div class="success-icon-wrapper">
            <span class="success-icon">👋</span>
            <div class="success-particles">✨</div>
          </div>
        </div>

        <div class="text-content">
          <h3 class="welcome-title">欢迎回来</h3>
          <h2 class="user-nickname">{{ userInfo.nickname || '尊敬的用户' }}</h2>
          <p class="redirect-text">身份验证通过，正在进入系统</p>
        </div>

        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-glow"></div>
          </div>
        </div>
      </div>

      <div v-else key="error" class="auth-card error-card">
        <div class="error-icon-wrapper">
          <span class="error-icon">✕</span>
        </div>
        <h3>授权中断</h3>
        <p class="error-desc">{{ error }}</p>

        <div class="action-buttons">
          <button @click="retryAuth" class="btn-primary">
            <span>↺ 重新授权</span>
          </button>
          <button @click="showDebug = !showDebug" class="btn-secondary">
            {{ showDebug ? '收起' : '查看' }}详情
          </button>
        </div>

        <transition name="slide-down">
          <div v-if="showDebug" class="debug-terminal">
            <div class="terminal-header">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
              <span class="terminal-title">Debug Console</span>
            </div>
            <div class="terminal-body">
              <div class="log-line"><span class="label">URL:</span> {{ currentUrl }}</div>
              <div class="log-line"><span class="label">Code:</span> {{ debugCode || 'N/A' }}</div>
              <div class="log-line"><span class="label">State:</span> {{ debugState || 'N/A' }}</div>
              <div class="log-divider">Log Output:</div>
              <pre>{{ debugLog }}</pre>
            </div>
          </div>
        </transition>
      </div>
    </transition>
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
const userInfo = ref({})

const addDebugLog = (message) => {
  const timestamp = new Date().toLocaleTimeString()
  debugLog.value += `[${timestamp}] ${message}\n`
  console.log(`[AuthCallback] ${message}`)
}

const handleAuthCallback = async () => {
  currentUrl.value = window.location.href
  urlParams.value = window.location.search
  storedState.value = localStorage.getItem('wechat_auth_state')

  const urlParamsObj = new URLSearchParams(window.location.search)
  const code = urlParamsObj.get('code')
  const state = urlParamsObj.get('state')

  debugCode.value = code
  debugState.value = state

  addDebugLog('=== 前端回调调试信息 ===')
  addDebugLog(`Code: ${code}`)

  if (!code) {
    error.value = '未获取到授权码，请重新尝试'
    loading.value = false
    return
  }

  try {
    const requestData = { code, state }
    const response = await exchangeCode(requestData)
    const responseData = response?.data || response

    if (responseData && responseData.success) {
      const { openid, token, userInfo: fetchedUserInfo } = responseData
      userInfo.value = fetchedUserInfo || {}

      localStorage.setItem('jwt_token', token)
      localStorage.setItem('wechat_openid', openid)
      localStorage.setItem('user_info', JSON.stringify(userInfo.value))

      if (responseData.expiresIn) {
        localStorage.setItem('token_expire_time', (Date.now() + (responseData.expiresIn * 1000)).toString())
      }

      // 🔥 关键新增：标记当前 Session 为已激活
      // 这行代码配合 router/index.js 使用，防止刷新页面时被强制跳回授权页
      sessionStorage.setItem('session_active', 'true')

      localStorage.removeItem('wechat_auth_state')
      success.value = true
      loading.value = false

      let redirectPath = route.query.redirect || '/community-list'
      setTimeout(() => {
        router.push(redirectPath).catch(() => router.push('/community-list'))
      }, 1500)
    } else {
      throw new Error(responseData?.message || '获取用户信息失败')
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '处理授权信息失败'
    loading.value = false
    addDebugLog(`Error: ${err.message}`)
  }
}

const retryAuth = () => {
  router.push('/wechat-auth')
}

onMounted(() => {
  handleAuthCallback()
})
</script>

<style scoped>
/* ---------------- 全局容器与背景 ---------------- */
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #eef1f5 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 装饰性背景图形 */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
}
.shape-1 {
  width: 300px;
  height: 300px;
  background: #dcfce7; /* 浅绿 */
  top: -50px;
  left: -50px;
}
.shape-2 {
  width: 250px;
  height: 250px;
  background: #dbeafe; /* 浅蓝 */
  bottom: -50px;
  right: -50px;
}

/* ---------------- 卡片通用样式 ---------------- */
.auth-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 40px 30px;
  border-radius: 24px;
  box-shadow:
      0 20px 40px -10px rgba(0, 0, 0, 0.08),
      0 0 0 1px rgba(0, 0, 0, 0.02);
  width: 100%;
  max-width: 380px;
  text-align: center;
  /* 确保切换时布局稳定 */
  min-height: 320px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* ---------------- 1. 加载状态 ---------------- */
.loader-ring {
  display: inline-block;
  position: relative;
  width: 64px;
  height: 64px;
  margin-bottom: 24px;
}
.loader-ring div {
  box-sizing: border-box;
  display: block;
  position: absolute;
  width: 50px;
  height: 50px;
  margin: 8px;
  border: 4px solid #07c160;
  border-radius: 50%;
  animation: loader-ring 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  border-color: #07c160 transparent transparent transparent;
}
.loader-ring div:nth-child(1) { animation-delay: -0.45s; }
.loader-ring div:nth-child(2) { animation-delay: -0.3s; }
.loader-ring div:nth-child(3) { animation-delay: -0.15s; }

.status-text {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}
.sub-text {
  font-size: 14px;
  color: #94a3b8;
  margin-top: 8px;
}

/* ---------------- 2. 成功状态 ---------------- */
.success-card {
  padding: 50px 30px;
  overflow: hidden;
}

.success-icon-wrapper {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 20px rgba(16, 185, 129, 0.15);
  position: relative;
}

.success-icon {
  font-size: 40px;
  animation: wave 2.5s infinite;
  transform-origin: 70% 70%;
}

.success-particles {
  position: absolute;
  top: -10px;
  right: -10px;
  font-size: 24px;
  animation: float 3s ease-in-out infinite;
}

.welcome-title {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 500;
}

.user-nickname {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 16px;
  background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.redirect-text {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 30px;
}

.progress-container {
  width: 100%;
  height: 6px;
  background: #f1f5f9;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  width: 100%;
  background: #10b981;
  border-radius: 10px;
  animation: progressFill 1.5s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  position: relative;
  transform-origin: left;
}

.progress-glow {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 20px;
  background: rgba(255,255,255,0.5);
  box-shadow: 0 0 10px rgba(255,255,255,0.8);
  filter: blur(3px);
}

/* ---------------- 3. 失败状态 ---------------- */
.error-card {
  border-top: 4px solid #ef4444;
}

.error-icon-wrapper {
  width: 72px;
  height: 72px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  color: #ef4444;
  font-size: 32px;
  font-weight: bold;
}

.error-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
  padding: 0 10px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  width: 100%;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  border: none;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
}
.btn-primary:hover {
  background: #2563eb;
  transform: translateY(-1px);
}
.btn-primary:active { transform: translateY(0); }

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
}
.btn-secondary:hover {
  background: #e2e8f0;
  color: #334155;
}

/* ---------------- 调试终端样式 ---------------- */
.debug-terminal {
  margin-top: 24px;
  width: 100%;
  background: #1e293b;
  border-radius: 12px;
  overflow: hidden;
  text-align: left;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.terminal-header {
  background: #334155;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.red { background: #ef4444; }
.yellow { background: #f59e0b; }
.green { background: #10b981; }

.terminal-title {
  margin-left: auto;
  font-size: 10px;
  color: #94a3b8;
  font-family: monospace;
}

.terminal-body {
  padding: 12px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 11px;
  color: #e2e8f0;
  max-height: 200px;
  overflow-y: auto;
}

.log-line {
  margin-bottom: 4px;
  word-break: break-all;
}
.log-line .label {
  color: #60a5fa;
  font-weight: bold;
}
.log-divider {
  color: #94a3b8;
  margin: 10px 0 5px;
  border-bottom: 1px dashed #475569;
}
pre {
  margin: 0;
  white-space: pre-wrap;
  color: #a5b4fc;
}

/* ---------------- 动画定义 ---------------- */
/* 路由切换过渡 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 调试面板下拉 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  opacity: 1;
}
.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
}

/* Loading 旋转 */
@keyframes loader-ring {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 挥手动画 */
@keyframes wave {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(14deg); }
  20% { transform: rotate(-8deg); }
  30% { transform: rotate(14deg); }
  40% { transform: rotate(-4deg); }
  50% { transform: rotate(10deg); }
  60% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}

/* 悬浮动画 */
@keyframes float {
  0% { transform: translateY(0px) rotate(0deg); opacity: 0.6; }
  50% { transform: translateY(-10px) rotate(10deg); opacity: 1; }
  100% { transform: translateY(0px) rotate(0deg); opacity: 0.6; }
}

/* 进度条填充 */
@keyframes progressFill {
  0% { transform: scaleX(0); }
  100% { transform: scaleX(1); }
}
</style>
