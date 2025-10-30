<template>
  <div class="recent-reservations">
    <div class="section-title">最近预约</div>
    <div class="reservation-list">
      <div class="reservation-item" v-for="(reservation, index) in displayedReservations" :key="index">
        <div class="reservation-location">
          <i class="icon-location"></i>
          <span>{{ reservation.roomName }}</span>
        </div>
        <div class="reservation-time">
          <i class="icon-time"></i>
          <span>{{ reservation.date }} {{ reservation.timeRange }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  reservations: {
    type: Array,
    default: () => []
  }
});

// 示例数据
const sampleReservations = [
  {
    roomName: "会议室A",
    date: "2023-05-15",
    timeRange: "09:00-11:00"
  },
  {
    roomName: "培训室B",
    date: "2023-05-16",
    timeRange: "14:00-16:00"
  },
  {
    roomName: "讨论室3",
    date: "2023-05-17",
    timeRange: "10:30-12:00"
  }
];

// 合并传入的reservations和示例数据，优先显示传入的数据
const displayedReservations = computed(() => {
  return props.reservations.length > 0 ? props.reservations : sampleReservations;
});
</script>

<style scoped>
.recent-reservations {
  background-color: #fff;
  margin: 15px;
  padding: 15px;
  border-radius: 8px;
}

.section-title {
  font-weight: 500;
  margin-bottom: 10px;
}

.reservation-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reservation-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 6px;
  font-size: 13px;
}

.reservation-location, .reservation-time {
  display: flex;
  align-items: center;
}

.icon-location::before {
  content: "📍";
  margin-right: 5px;
}

.icon-time::before {
  content: "🕒";
  margin-right: 5px;
}
</style>
