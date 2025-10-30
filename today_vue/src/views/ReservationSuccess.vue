<template>
  <div class="success-container">
    <div class="success-card">
      <div class="success-icon">✓</div>
      <h2 class="success-title">预约成功</h2>
      <p class="success-message">您的教室预约已成功提交</p>
      <div class="reservation-details">
        <div class="detail-item">
          <span class="detail-label">预约教室:</span>
          <span class="detail-value">{{ reservationDetails.roomName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">预约日期:</span>
          <span class="detail-value">{{ reservationDetails.date }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">预约时间:</span>
          <span class="detail-value">{{ reservationDetails.timeSlot }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">预约编号:</span>
          <span class="detail-value">{{ reservationDetails.reservationId }}</span>
        </div>
      </div>
      <div class="action-buttons">
        <button class="primary-button" @click="goToHome">返回首页</button>
        <button class="secondary-button" @click="viewMyReservations">查看我的预约</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 模拟预约详情数据
const reservationDetails = ref({
  roomName: '多功能研讨室1',
  date: '2023年5月15日',
  timeSlot: '10:30 - 12:30',
  reservationId: 'R' + Math.floor(Math.random() * 1000000).toString().padStart(6, '0')
});

// 返回首页
const goToHome = () => {
  router.push('/');
};

// 查看我的预约
const viewMyReservations = () => {
  router.push('/my-reservations');
};
</script>

<style scoped>
.success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.success-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 32px;
  width: 100%;
  max-width: 480px;
  text-align: center;
}

.success-icon {
  width: 64px;
  height: 64px;
  background-color: #52c41a;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 24px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.success-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.reservation-details {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  text-align: left;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-weight: 500;
  color: #666;
  width: 80px;
  flex-shrink: 0;
}

.detail-value {
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.primary-button, .secondary-button {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.primary-button {
  background-color: #1677ff;
  color: white;
}

.secondary-button {
  background-color: #f0f0f0;
  color: #333;
}
</style>
