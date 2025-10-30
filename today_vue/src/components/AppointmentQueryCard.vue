<template>
  <!-- 查询预约卡片部分 -->
  <div class="query-card" @click="goToReservationQuery">
    <div class="query-icon">
      <i class="icon-search"></i>
    </div>
    <div class="query-content">
      <h3>查询预约</h3>
      <p>使用学号快速查询预约情况</p>
    </div>
    <div class="query-action">
      <span class="action-button">立即查询 <i class="icon-arrow-right"></i></span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goToReservationQuery = () => {

  router.push('/student-reservations');
};
</script>

<style scoped>
.icon-search::before {
  content: "🔍";
  font-style: normal;
}

.icon-arrow-right::before {
  content: "→";
  font-style: normal;
}
/* 查询预约卡片样式 */
.query-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e8e8e8;
}

.query-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.query-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  color: white;
  font-size: 20px;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

.query-content {
  flex: 1;
}

.query-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #333;
  letter-spacing: 0.5px;
}

.query-content p {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

.query-action {
  margin-left: 10px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 14px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

.action-button i {
  margin-left: 6px;
  transition: transform 0.3s ease;
}

.query-card:hover .action-button {
  background: linear-gradient(135deg, #1261d1, #4d9cff);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.query-card:hover .action-button i {
  transform: translateX(3px);
}

/* 响应式调整 */
@media (max-width: 480px) {
  .query-card {
    padding: 14px;
    margin: 14px 12px;
  }

  .query-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    margin-right: 12px;
  }

  .query-content h3 {
    font-size: 15px;
  }

  .query-content p {
    font-size: 12px;
  }

  .action-button {
    padding: 7px 12px;
    font-size: 13px;
  }
}
</style>
