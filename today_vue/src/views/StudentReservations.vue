<template>
  <div class="student-reservations-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="back-button" @click="goBack">
        <i class="icon-back">&#10094;</i>
      </div>
      <div class="title">学号查询预约</div>
      <div class="placeholder"></div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-input">
        <i class="icon-search"></i>
        <input
          type="text"
          v-model="studentId"
          placeholder="请输入学号"
          @keyup.enter="searchReservations"
        >
      </div>
      <button class="search-button" @click="searchReservations">查询</button>
    </div>

    <!-- 预约列表 -->
    <div class="reservations-list" v-if="reservations.length > 0">
      <div
        v-for="reservation in reservations"
        :key="reservation.id"
        class="reservation-card"
      >
        <div class="reservation-header">
          <span class="room-name">{{ reservation.roomName }}</span>
          <span class="status" :class="reservation.statusClass">{{ reservation.statusText }}</span>

        </div>
        <div class="reservation-info">
          <div class="info-item">
            <i class="icon-activity"></i>
            <span>活动名称：{{ reservation.activityName }}</span>
          </div>
          <div class="info-item">
            <i class="icon-date"></i>
            <span>预约日期：{{ reservation.reservationDate }}</span>
          </div>

          <div class="info-item">
            <i class="icon-time"></i>
            <span>预约时间：{{ reservation.timeRange }}</span>
          </div>


        </div>
        <div class="reservation-footer">
          <div class="department">{{ reservation.department }}</div>
          <div class="attendees">参与人数: {{ reservation.attendees }}</div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="hasSearched">
      <i class="icon-empty"></i>
      <p>暂无预约记录</p>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <div class="loading-spinner"></div>
      <div>加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getReservationsByUserId } from "@/api/reservations.js";

const router = useRouter();
const studentId = ref('');
const reservations = ref([]);
const loading = ref(false);
const hasSearched = ref(false);

const goBack = () => {
  router.back();
};

const searchReservations = async () => {
  if (!studentId.value) {
    ElMessage({
      message: '请输入学号',
      type: 'warning',
      duration: 2000,
      offset: 80
    });
    return;
  }

  try {
    loading.value = true;
    const response = await getReservationsByUserId(studentId.value);
    console.log('response', response)

    // 处理每条记录，增加 timeRange 字段
    reservations.value = (response.data || []).map(item => ({
      ...item,
      timeRange: `${item.startTime} - ${item.endTime}`,
      // 这里也可以顺便处理 status 显示文本，比如：
      statusText: getStatusText(item.status),
      // 如果需要状态 class 样式，也可以加上
      statusClass: getStatusClass(item.status)
    }));

    hasSearched.value = true;

    if (reservations.value.length === 0) {
      ElMessage.info({
        message: '暂无预约记录',
        duration: 1500
      });
    }
  } catch (error) {
    console.error('查询预约记录失败:', error);
    ElMessage.error({
      message: '查询失败，请稍后重试',
      duration: 3000
    });
  } finally {
    loading.value = false;
  }
};

// 辅助函数：状态转文字
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核';
    case 1: return '已通过';
    case 2: return '已拒绝';
    default: return '未知状态';
  }
};

// 辅助函数：状态转 class 名字
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'pending';
    case 1: return 'approved';
    case 2: return 'rejected';
    default: return '';
  }
};
</script>

<style scoped>
.student-reservations-container {
  max-width: 100%;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.back-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
}

.back-button:hover {
  background-color: #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 32px;
}

/* 搜索区域 */
.search-section {
  padding: 16px;
  background-color: #fff;
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 0 12px;
}

.icon-search::before {
  content: '🔍';
  font-size: 16px;
  color: #999;
  margin-right: 8px;
}

.search-input input {
  flex: 1;
  border: none;
  background: none;
  padding: 12px 0;
  font-size: 16px;
  outline: none;
}

.search-button {
  padding: 0 24px;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.search-button:hover {
  background-color: #0e5edb;
}

/* 预约列表 */
.reservations-list {
  padding: 0 16px;
}

.reservation-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.room-name {
  font-size: 18px;
  font-weight: 600;
}

.status {
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
}
.status {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
  border: 1px solid transparent;
  display: inline-block;
}

/* 不同状态的颜色 */
.status.pending {
  color: #fa8c16;
  background-color: #fff7e6;
  border-color: #ffd591;
}

.status.approved {
  color: #52c41a;
  background-color: #f6ffed;
  border-color: #b7eb8f;
}

.status.rejected {
  color: #f5222d;
  background-color: #fff1f0;
  border-color: #ffa39e;
}


.reservation-info {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #666;
}

.info-item i {
  margin-right: 8px;
  font-size: 16px;
}

.icon-date::before {
  content: '📅';
}

.icon-time::before {
  content: '🕒';
}

.icon-activity::before {
  content: '📝';
}

.reservation-footer {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
}

.icon-empty::before {
  content: '📋';
  font-size: 48px;
  margin-bottom: 16px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #666;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1677ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
