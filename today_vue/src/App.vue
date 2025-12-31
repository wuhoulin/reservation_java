<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

// 只要这个值变了，用户浏览器就会强制清除缓存
const CURRENT_APP_VERSION = '20231224_v1.6'

const autoClearCacheOnEntry = () => {
  const localVersion = localStorage.getItem('app_version')

  if (localVersion === CURRENT_APP_VERSION) {
    console.log('当前版本已是最新的，无需清理缓存')
    return
  }

  try {
    console.log(`检测到新版本 (旧: ${localVersion} -> 新: ${CURRENT_APP_VERSION})，正在执行智能清理...`)

    const keepKeys = [
      'jwt_token',
      'wechat_openid',
      'user_info',
      'token_expire_time',
      'wechat_auth_state'
    ]
    const savedData = {}
    keepKeys.forEach(key => {
      const val = localStorage.getItem(key)
      if (val) savedData[key] = val
    })

    // --- B. 执行清除 ---

    // 清除 LocalStorage
    localStorage.clear()

    // 清除 SessionStorage
    sessionStorage.clear()

    // 清除 Cookies (尝试清除根路径下的所有 Cookie)
    document.cookie.split(";").forEach(function(c) {
      document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });

    // 清除 IndexedDB
    if (window.indexedDB) {
      window.indexedDB.databases().then(databases => {
        databases.forEach(db => {
          if (db.name) window.indexedDB.deleteDatabase(db.name)
        })
      })
    }

    // --- C. 还原白名单数据 ---
    Object.keys(savedData).forEach(key => {
      localStorage.setItem(key, savedData[key])
    })

    // --- D. 写入新版本号 ---
    localStorage.setItem('app_version', CURRENT_APP_VERSION)

    console.log('缓存清理完成，正在重新加载...')

    // --- E. 强制刷新页面 ---
    // 这一步很重要，确保内存中的旧状态也被清除
    window.location.reload()

  } catch (error) {
    console.error('自动清除缓存异常:', error)
  }
}

// ================== 生命周期 ==================

onMounted(() => {
  // 执行自动检测与清理
  autoClearCacheOnEntry()
})
</script>

<style>
.app-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  max-width: 100%;
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>
