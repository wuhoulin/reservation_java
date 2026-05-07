<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

const nuclearCleanCheck = () => {
  // 检查是否是"AuthCallback"回调页面（url里有code参数）
  // 如果是回调页面，说明正在登录中，不能清缓存
  const isCallback = window.location.href.includes('code=') || window.location.pathname.includes('/auth-callback');

  // 检查当前会话是否活跃
  const isSessionActive = sessionStorage.getItem('session_active');

  // 🔥 兜底逻辑：
  // 如果当前【没有活跃会话】 且 【不是正在进行微信回调】
  // 说明用户是新进来的（或者刷新了页面但Session丢了）
  // 直接执行核弹清理，防止残留的 LocalStorage 导致串号
  if (!isSessionActive && !isCallback) {
    console.log('☢️ 检测到非活跃会话，执行核弹级清理...');

    // 1. 只有当 localStorage 里真的有脏数据时才执行，避免死循环
    if (localStorage.getItem('jwt_token') || localStorage.getItem('user_info')) {
      localStorage.clear(); // 杀全家
      sessionStorage.clear();
      console.log('✅ 残留数据已清除');

      // 2. 如果当前不在授权页，强制去授权页
      if (!window.location.pathname.includes('/wechat-auth')) {
        window.location.replace('/wechat-auth');
      }
    }
  }
}

onMounted(() => {
  nuclearCleanCheck();
})
</script>

<style>
.app-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  max-width: 100%;
  background-color: #f5f5f5;
  min-height: 100vh;
}
</style>
