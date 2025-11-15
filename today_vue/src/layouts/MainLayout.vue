<template>
  <div class="main-layout">
    <!-- 主要内容区域 -->
    <main class="main-content" :class="{ 'with-nav': showNav }">
      <router-view />
    </main>

    <!-- 底部导航栏 -->
    <BottomNav v-if="showNav" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'

const route = useRoute()

// 在授权页面不显示导航栏
const showNav = computed(() => {
  const hideNavRoutes = ['/wechat-auth', '/auth-callback']
  return !hideNavRoutes.includes(route.path)
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-bottom: 0;
}

.main-content.with-nav {
  padding-bottom: 70px; /* 为底部导航栏留出空间 */
}
</style>
