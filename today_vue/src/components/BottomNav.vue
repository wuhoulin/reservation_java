<template>
  <div class="bottom-nav">
    <div class="nav-container">

      <router-link
          to="/community-list"
          class="nav-item"
          :class="{ active: $route.path.startsWith('/community-list') }"
      >
        <div class="nav-icon">🏠</div>
        <div class="nav-text">社区</div>
      </router-link>

      <router-link
          to="/reservations"
          class="nav-item"
          :class="{ active: $route.path.startsWith('/reservations') }"
      >
        <div class="nav-icon">📋</div>
        <div class="nav-text">我的预约</div>
      </router-link>

      <router-link
          to="/my"
          class="nav-item"
          :class="{ active: $route.path.startsWith('/my') }"
      >
        <div class="nav-icon">👤</div>
        <div class="nav-text">个人中心</div>
      </router-link>

    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e0e0e0;
  z-index: 1000;
  padding: 8px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  max-width: 500px;
  margin: 0 auto;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #666;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  min-width: 60px;
}

.nav-item:hover {
  background-color: #f5f5f5;
}

.nav-item.active {
  color: #07c160; /* 保持激活颜色不变 */
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}
</style>
