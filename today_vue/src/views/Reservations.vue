<template>
  <div class="community-reservation">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="nav-bar">
        <div class="back-button">
          <i class="icon-back"></i>
        </div>
        <div class="title">社区预约</div>
        <div class="settings">
          <i class="icon-settings"></i>
        </div>
      </div>
    </div>

    <!-- 房间选择区域 -->
    <div class="room-selection">
      <div
          v-for="(room, index) in rooms"
          :key="index"
          class="room-card"
          :class="{ active: selectedRoomIndex === index }"
          @click="selectRoom(index)"
      >
        <div class="room-image">
          <img :src="room.image" alt="房间图片">
        </div>
        <div class="room-info">
          <div class="room-name">{{ room.name }}</div>
          <div class="room-capacity">
            <span class="status" :class="{ available: room.available }"></span>
            可容纳 {{ room.capacity }} 人
          </div>
        </div>
      </div>
    </div>

    <!-- 日期选择 -->
    <div class="date-selection">
      <div
          v-for="(date, index) in dates"
          :key="index"
          class="date-item"
          :class="{ active: selectedDateIndex === index }"
          @click="selectDate(index)"
      >
        <div class="day-name">{{ date.dayName }}</div>
        <div class="date-number">{{ date.dateNumber }}</div>
      </div>
    </div>

    <!-- 时间段选择 -->
    <div class="time-slots">
      <div class="time-grid">
        <div
            v-for="(timeSlot, index) in timeSlots"
            :key="index"
            class="time-slot"
            :class="{
              active: selectedTimeSlot === index,
              disabled: unavailableTimeSlots.includes(index)
            }"
            @click="selectTimeSlot(index)"
        >
          {{ timeSlot.start }} - {{ timeSlot.end }}
        </div>
      </div>
    </div>

    <!-- 预约规则 -->
    <div class="reservation-rules">
      <div class="rules-title">
        <i class="icon-info"></i>
        预约须知
      </div>
      <div class="rules-content">
        <div class="rule-item">• 每次预约时长固定为 2 小时</div>
        <div class="rule-item">• 可预约时段：8:00 - 22:00</div>
        <div class="rule-item">• 如临时请假请提前操作</div>
      </div>
    </div>

    <!-- 最近预约组件 -->
    <RecentReservations :reservations="recentReservations" />

    <!-- 预约按钮 -->
    <div class="action-button">
      <button class="reserve-button" @click="makeReservation">立即预约</button>
    </div>

    <!-- 场地借用注意事项弹窗 -->
    <RulesModal
        v-model:show="showRulesModal"
        @agree="handleAgreeRules"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import RulesModal from "@/components/RulesModal.vue";
import RecentReservations from "@/components/RecentReservations.vue";

// 动态生成未来一周的日期
const generateWeekDates = () => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const today = new Date();
  const weekDates = [];

  for (let i = 0; i < 7; i++) {
    const date = new Date();
    date.setDate(today.getDate() + i);

    weekDates.push({
      dayName: days[date.getDay()],
      dateNumber: `${date.getMonth() + 1}/${date.getDate()}`
    });
  }

  return weekDates;
};

// 房间数据
const roomsData = [
  // 学生第一社区
  {
    id: 1,
    name: '宿舍一号楼',
    type: '多功能研讨室1',
    capacity: 20,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 2,
    name: '宿舍二号楼',
    type: '多功能研讨室2',
    capacity: 15,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 3,
    name: '宿舍一号楼',
    type: '多功能研讨室3',
    capacity: 15,
    available: false,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 4,
    name: '宿舍一号楼',
    type: '体育健身室',
    capacity: 30,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 5,
    name: '宿舍一号楼',
    type: '品名室',
    capacity: 10,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 6,
    name: '宿舍一号楼',
    type: '心理咨询室',
    capacity: 10,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 7,
    name: '宿舍一号楼',
    type: '美术室',
    capacity: 20,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  // 学生第二社区
  {
    id: 8,
    name: '宿舍七号楼',
    type: '多功能活动室1',
    capacity: 30,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 9,
    name: '宿舍七号楼',
    type: '多功能活动室2',
    capacity: 30,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  // 学生第三社区
  {
    id: 10,
    name: '宿舍九号楼',
    type: '党团活动室',
    capacity: 30,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
  {
    id: 11,
    name: '宿舍九号楼',
    type: '创新创业室',
    capacity: 20,
    available: true,
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-SjI9IXyfPj6f8CvGf5n3c4RXGhovqo.png'
  },
];

// 显示的房间列表
const rooms = ref(roomsData.slice(0, 2));
const selectedRoomIndex = ref(0);

// 日期数据（动态生成）
const dates = ref(generateWeekDates());
const selectedDateIndex = ref(0);

// 时间段数据（8:00-22:00，每2小时为一个时段）
const generateTimeSlots = () => {
  const slots = [];
  for (let hour = 8; hour <= 20; hour += 2) {
    slots.push({
      start: `${hour}:00`,
      end: `${hour + 2}:00`
    });
  }
  return slots;
};

const timeSlots = ref(generateTimeSlots());
const selectedTimeSlot = ref(null);
const unavailableTimeSlots = ref([2, 3]); // 示例：禁用第3、4个时段（12:00-14:00和14:00-16:00）

// 最近预约数据
const recentReservations = ref([
  { roomName: '宿舍一号楼', date: '12/16日', timeRange: '14:00-16:00' },
  { roomName: '宿舍二号楼', date: '12/17日', timeRange: '10:00-12:00' },
]);

// 规则弹窗控制
const showRulesModal = ref(false);
const rulesAgreed = ref(false);

// 选择房间
const selectRoom = (index) => {
  selectedRoomIndex.value = index;
};

// 选择日期
const selectDate = (index) => {
  selectedDateIndex.value = index;
};

// 选择时间段
const selectTimeSlot = (index) => {
  if (unavailableTimeSlots.value.includes(index)) return;
  selectedTimeSlot.value = index;
};

// 处理同意规则
const handleAgreeRules = () => {
  rulesAgreed.value = true;
  proceedWithReservation();
};

// 继续预约流程
const proceedWithReservation = () => {
  if (selectedTimeSlot.value === null) {
    alert('请选择预约时间段');
    return;
  }

  const room = rooms.value[selectedRoomIndex.value];
  const date = dates.value[selectedDateIndex.value];
  const timeSlot = timeSlots.value[selectedTimeSlot.value];

  alert(`预约成功！\n房间：${room.name} (${room.type})\n日期：${date.dateNumber}\n时间：${timeSlot.start}-${timeSlot.end}`);

  // 在实际应用中，这里应该调用API进行预约
};

// 预约操作
const makeReservation = () => {
  if (selectedTimeSlot.value === null) {
    alert('请选择预约时间段');
    return;
  }

  // 如果还没有同意规则，显示规则弹窗
  if (!rulesAgreed.value) {
    showRulesModal.value = true;
  } else {
    // 已经同意过规则，直接进行预约
    proceedWithReservation();
  }
};
</script>

<style scoped>
.community-reservation {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  max-width: 500px;
  margin: 0 auto;
  background-color: white;
  min-height: 100vh;
  position: relative;
  padding-bottom: 80px;
}

.header {
  background-color: #fff;
  padding: 10px 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  height: 44px;
}

.back-button, .settings {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-back::before {
  content: "←";
  font-size: 20px;
}

.icon-settings::before {
  content: "⚙️";
  font-size: 20px;
}

.title {
  font-size: 18px;
  font-weight: 500;
}

.room-selection {
  display: flex;
  padding: 15px;
  overflow-x: auto;
  gap: 15px;
  background-color: #fff;
  margin-top: 10px;
}

.room-card {
  min-width: 150px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
}

.room-card.active {
  border-color: #4080ff;
}

.room-image {
  height: 100px;
  overflow: hidden;
}

.room-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-info {
  padding: 10px;
}

.room-name {
  font-weight: 500;
  margin-bottom: 5px;
}

.room-capacity {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
}

.status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 5px;
  background-color: #ccc;
}

.status.available {
  background-color: #4cd964;
}

.date-selection {
  display: flex;
  background-color: #4080ff;
  color: white;
  padding: 10px 0;
  margin-top: 15px;
}

.date-item {
  flex: 1;
  text-align: center;
  padding: 5px 0;
  cursor: pointer;
}

.date-item.active {
  background-color: #2060dd;
  border-radius: 4px;
}

.day-name {
  font-size: 12px;
  margin-bottom: 2px;
}

.date-number {
  font-size: 14px;
  font-weight: 500;
}

.time-slots {
  background-color: #fff;
  padding: 15px;
  margin-top: 10px;
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.time-slot {
  text-align: center;
  padding: 12px 0;
  border-radius: 4px;
  background-color: #f5f5f5;
  font-size: 14px;
  cursor: pointer;
}

.time-slot.active {
  background-color: #4080ff;
  color: white;
}

.time-slot.disabled {
  background-color: #e0e0e0;
  color: #999;
  cursor: not-allowed;
}

.reservation-rules {
  background-color: #f0f8ff;
  margin: 15px;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
}

.rules-title {
  display: flex;
  align-items: center;
  color: #4080ff;
  margin-bottom: 8px;
  font-weight: 500;
}

.icon-info::before {
  content: "ℹ️";
  margin-right: 5px;
}

.rule-item {
  margin-bottom: 5px;
  color: #666;
}

.action-button {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  margin: 0 auto;
}

.reserve-button {
  width: 100%;
  background-color: #4080ff;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

.reserve-button:active {
  background-color: #2060dd;
}
</style>
