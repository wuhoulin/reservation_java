<template>
  <div class="room-detail-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="back-button" @click="goBack">
        <i class="icon-back">&#10094;</i>
      </div>
      <div class="title">教室详情</div>
      <div class="placeholder"></div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else class="room-detail-content">
      <!-- 教室图片 -->
      <div class="room-image" :style="{ backgroundImage: `url(${room.imageUrl || 'https://img95.699pic.com/photo/60003/0000.jpg_wh860.jpg'})` }">
        <div class="room-status-badge" :class="{ available: room.available === 1, unavailable: room.available === 0 }">
          {{ room.status === true ? '可预约' : '已被预约' }}
        </div>
      </div>

      <!-- 教室信息 -->
      <div class="room-info-card">
        <h1 class="room-name">{{ room.name }}</h1>
        <div class="room-meta">
          <div class="meta-item">
            <i class="icon-location"></i>
            <span>{{ room.communityName }}</span>
          </div>
          <div class="meta-item">
            <i class="icon-people"></i>
            <span>可容纳 {{ room.capacity }} 人</span>
          </div>
        </div>

        <!-- 教室描述 -->
        <div class="room-description">
          <h2 class="section-title">教室介绍</h2>
          <p>{{ room.description || '暂无介绍' }}</p>
        </div>

        <!-- 预约时间选择 -->
        <div class="booking-section">
          <h2 class="section-title">选择预约时间</h2>

          <!-- 日期选择 -->
          <div class="date-selector-container">
            <div class="date-selector" ref="dateSelector">
              <div
                  v-for="(date, index) in availableDates"
                  :key="index"
                  class="date-item"
                  :class="{ active: selectedDateIndex === index }"
                  @click="selectDate(index)"
              >
                <div class="date-weekday">{{ formatWeekday(date) }}</div>
                <div class="date-day">{{ formatDay(date) }}</div>
                <div class="date-month">{{ formatMonth(date) }}</div>
              </div>
            </div>
          </div>

          <!-- 时间段选择 -->
          <div class="time-slots">
            <div
                v-for="tp in availableTimePoints"
                :key="tp.id"
                class="time-slot"
                :class="{
                active: selectedTimePoints.some(s => s.id === tp.id),
                disabled: !tp.available
              }"
                @click="tp.available && selectTimePoint(tp)"
            >
              {{ formatTimePoint(tp.point) }}
              <span v-if="!tp.available" class="reserved-badge">已预约</span>
            </div>

            <div v-if="availableTimePoints.length === 0" class="no-time-slots">
              当前日期没有可用时间段
            </div>
          </div>
        </div>

        <!-- 预约表单组件 -->
        <BookingForm
            v-model="bookingForm"
            @form-validity-change="updateFormValidity"
        />

        <!-- 预约按钮 -->
        <div class="booking-actions">
          <button class="book-button" :disabled="!canBook" @click="showTermsModal">
            立即预约
          </button>
          <button v-if="isReservationOwner" class="cancel-button" @click="showCancelModal">
            取消预约
          </button>
        </div>
      </div>
    </div>

    <!-- 使用条款弹窗 -->
    <RulesModal
        v-model:show="termsModalVisible"
        @agree="proceedWithBooking"
    />

    <!-- 取消预约确认弹窗 -->
    <el-dialog
        v-model="cancelModalVisible"
        title="取消预约"
        width="90%"
        :close-on-click-modal="false"
    >
      <div class="cancel-dialog-content">
        <p>确定要取消该预约吗？</p>
        <p class="warning-text">注意：一个月内取消预约超过2次将被禁止预约3个月</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="cancel-dialog-button cancel" @click="cancelModalVisible = false">取消</button>
          <button class="cancel-dialog-button confirm" @click="confirmCancel">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getRoomDetail } from "@/api/home.js";
import {getAllTimePoints, getAvailableTimePointsForRoom} from "@/api/timePoint.js";
import RulesModal from "@/components/RulesModal.vue";
import BookingForm from "@/components/booking-form.vue";
import { createReservation, cancelReservation } from '@/api/roomDetail.js';
import { getRoomReservationStatus } from '@/api/roomDetail.js';
import { ElMessage } from 'element-plus'
// 添加已预约时间段状态
const reservedTimeRanges = ref([]);
const router = useRouter();
const route = useRoute();
const roomId = ref(route.params.roomId);
const dateSelector = ref(null);

const room = ref({});
const loading = ref(true);
const selectedDateIndex = ref(0);
const selectedTimePoints = ref([]);
const availableTimePoints = ref([]);
const timePointsLoading = ref(false);
const termsModalVisible = ref(false);
const isFormValid = ref(false);
const cancelModalVisible = ref(false);
const isReservationOwner = computed(() => {
  return room.value.status === false && room.value.userId === currentUser.value?.id;
});

// 预约表单数据
const bookingForm = reactive({
  activityName: '',
  department: '',
  needProjection: false,
  userName: '',
  college: '',
  major: '',
  contact: '',
  teacherName: '',
  teacherContact: '',
  otherRequirements: '',
  attendees:1
});

// 监听表单数据变化
watch(bookingForm, (newValue) => {
  console.log('Booking form changed:', newValue);
}, { deep: true });

// 更新表单有效性
const updateFormValidity = (isValid) => {
  console.log('Form validity changed:', isValid);
  isFormValid.value = isValid;
};

// 未来七天
const availableDates = computed(() => {
  const dates = [];
  const today = new Date();
  for (let i = 0; i < 7; i++) {
    // 用 new Date(年,月,日) 构造 —— 时分秒自动清零
    const date = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() + i
    );
    dates.push(date);
  }
  return dates;
});


const selectedFormattedDate = computed(() => {
  const d = availableDates.value[selectedDateIndex.value];
  const Y = d.getFullYear();
  const M = String(d.getMonth() + 1).padStart(2, '0');
  const D = String(d.getDate()).padStart(2, '0');
  return `${Y}-${M}-${D}`;
});


const formatTimePoint = (point) => {
  return point ? point.slice(0, 5) : '';
};

const canBook = computed(() => {
  return room.value.status === true &&
      selectedTimePoints.value.length > 0 &&
      isFormValid.value;
});

// 初始化加载
onMounted(async () => {
  try {
    await loadRoomDetail();
    await loadTimePoints();

    if (dateSelector.value) {
      const selectedElement = dateSelector.value.children[selectedDateIndex.value];
      if (selectedElement) {
        dateSelector.value.scrollLeft = selectedElement.offsetLeft - 20;
      }
    }
  } catch (error) {
    console.error('Failed to load room detail:', error);
  }
});

watch(selectedFormattedDate, async () => {
  await loadTimePoints();
});

watch(selectedDateIndex, () => {
  if (dateSelector.value) {
    const selectedElement = dateSelector.value.children[selectedDateIndex.value];
    if (selectedElement) {
      dateSelector.value.scrollLeft = selectedElement.offsetLeft - 20;
    }
  }
});

// 加载教室信息
const loadRoomDetail = async () => {
  try {
    loading.value = true;
    const response = await getRoomDetail(roomId.value);
    console.log('room detail', response);
    room.value = response.data || {};
  } catch (error) {
    console.error(`Failed to load room detail for ${roomId.value}:`, error);
  } finally {
    loading.value = false;
  }
};

// 加载时间段
const loadTimePoints = async () => {
  try {
    timePointsLoading.value = true;
    selectedTimePoints.value = [];
    reservedTimeRanges.value = [];

    // 并行获取所有时间段和预约状态
    const [allTimePointsRes, statusRes] = await Promise.all([
      getAllTimePoints(),  // 获取所有时间段
      getRoomReservationStatus(roomId.value, selectedFormattedDate.value)  // 获取预约状态
    ]);

    // 获取所有时间段数据
    const allTimePoints = allTimePointsRes.data || [];

    // 获取已预约时间段
    reservedTimeRanges.value = statusRes.data?.reservedTimeRanges || [];
    console.log("已预约时间段:", reservedTimeRanges.value);

    // 标记每个时间段是否可用
    availableTimePoints.value = allTimePoints.map(point => {
      // 是否在任一已预约区间内
      const hitRange = reservedTimeRanges.value.find(r => {
        const toMin = t => {
          const [h,m] = t.split(':').map(Number)
          return h*60 + m
        }
        const cur = toMin(point.point)
        return cur >= toMin(r.start) && cur < toMin(r.end)
      })

      return {
        ...point,
        available: !hitRange,
        reservedByMe: hitRange?.userId === currentUser.value.id,
        reservationNo: hitRange?.reservationNo
      }
    })

  } catch (error) {
    console.error('Failed to load time points:', error);
  } finally {
    timePointsLoading.value = false;
  }
};

// 判断时间点是否在任一已预约区间内
const isTimePointReserved = (timeStr) => {
  const toMin = t => {
    const [h, m] = t.split(':').map(Number);
    return h * 60 + m;
  };

  const timeStrWithoutSeconds = timeStr.slice(0, 5); // 去掉秒部分
  const cur = toMin(timeStrWithoutSeconds);

  return reservedTimeRanges.value.some(({ start, end }) => {
    const s = toMin(start);
    const e = toMin(end);
    // 当前时间点在预约区间内即为已预约
    return cur >= s && cur <= e;
  });
};



// 日期选择
const selectDate = (index) => {
  selectedDateIndex.value = index;
};

// 时间段选择（多选）
const selectTimePoint = (timePoint) => {
  if (!timePoint.available) return;

  // 如果已经选中了，就取消
  const existIdx = selectedTimePoints.value.findIndex(t => t.id === timePoint.id);
  if (existIdx >= 0) {
    selectedTimePoints.value.splice(existIdx, 1);
    return;
  }

  // 如果已经有两个，点第三个就重置成只选这个
  if (selectedTimePoints.value.length === 2) {
    selectedTimePoints.value = [timePoint];
    return;
  }

  // 否则直接加入
  selectedTimePoints.value.push(timePoint);

  // 如果正好选了两个，就按时间先后排一下
  if (selectedTimePoints.value.length === 2) {
    selectedTimePoints.value.sort((a, b) => {
      const idxA = availableTimePoints.value.findIndex(t => t.id === a.id);
      const idxB = availableTimePoints.value.findIndex(t => t.id === b.id);
      return idxA - idxB;
    });
  }
};


// 时间格式化
const formatDay = (date) => date.getDate();
const formatWeekday = (date) => ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
const formatMonth = (date) => `${date.getMonth() + 1}月`;

// 显示条款弹窗
const showTermsModal = () => {
  if (!canBook.value) return;
  termsModalVisible.value = true;
};

// 用户同意条款后继续预约
const proceedWithBooking = async () => {
  if (!canBook.value) return;

  try {
    const formattedDate = selectedFormattedDate.value;
    // 创建表单数据的深拷贝
    const formDataCopy = JSON.parse(JSON.stringify(bookingForm));
    console.log('表单数据拷贝:', formDataCopy); // 调试用

    // 验证表单数据
    if (!isFormValid.value) {
      ElMessage.success('请填写完整的预约信息')
      return;
    }

    // 准备提交数据 - 使用深拷贝后的数据
    const reservationData = {
      roomId: roomId.value,
      reservationDate: formattedDate,
      timePointIds: selectedTimePoints.value.map(tp => tp.id),
      ...formDataCopy // 使用解构展开深拷贝的表单数据
    };

    console.log('提交的预约数据:', reservationData); // 调试用

    // 调用API
    const response = await createReservation(reservationData);
    console.log("response",response)
    // 处理响应
    if (response.code === 200) {
      ElMessage.success('预约已提交，请耐心等待审核');
      resetForm();
      router.push('/');
    } else {
      throw new Error(response.message || '预约失败');
    }
  } catch (error) {
    console.error('预约失败:', error);
    ElMessage.error(`预约失败: ${error.message}`);
  } finally {
    termsModalVisible.value = false;
  }
};

// 重置表单
const resetForm = () => {
  selectedTimePoints.value = [];
  Object.keys(bookingForm).forEach(key => {
    if (key === 'needProjection') {
      bookingForm[key] = false;
    } else if (key === 'attendees') {
      bookingForm[key] = 1; // 重置为默认值1
    } else {
      bookingForm[key] = '';
    }
  });
};

// 返回
const goBack = () => {
  router.back();
};

// 显示取消预约弹窗
const showCancelModal = () => {
  cancelModalVisible.value = true;
};

// 确认取消预约
const confirmCancel = async () => {
  try {
    await cancelReservation(room.value.reservationNo, currentUser.value.id);
    ElMessage.success('预约已取消');
    cancelModalVisible.value = false;
    await loadRoomDetail(); // 重新加载教室信息
  } catch (error) {
    console.error('取消预约失败:', error);
    ElMessage.error(`取消预约失败: ${error.message}`);
  }
};

const goToStudentReservations = () => {
  router.push('/student-reservations')
}
</script>

<style scoped>
.room-detail-container {
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

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1677ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #666;
  font-size: 14px;
}

/* 教室详情内容 */
.room-detail-content {
  padding-bottom: 24px;
}

/* 教室图片 */
.room-image {
  height: 240px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.room-status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.room-status-badge.available {
  background-color: #52c41a;
  color: white;
}

.room-status-badge.unavailable {
  background-color: #f5222d;
  color: white;
}

/* 教室信息卡片 */
.room-info-card {
  margin-top: -20px;
  margin-left: 16px;
  margin-right: 16px;
  padding: 24px;
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.room-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.room-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.meta-item i {
  margin-right: 8px;
}

.icon-location::before {
  content: '📍';
}

.icon-people::before {
  content: '👥';
}

/* 教室描述 */
.room-description {
  margin-bottom: 28px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.room-description p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 预约部分 */
.booking-section {
  margin-bottom: 28px;
}

/* 日期选择器容器 */
.date-selector-container {
  position: relative;
  margin-bottom: 20px;
  overflow: hidden;
}

/* 日期选择器 */
.date-selector {
  display: flex;
  overflow-x: auto;
  gap: 12px;
  padding: 8px 4px;
  margin: 0 -4px;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none; /* Firefox */
}

.date-selector::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 100px;
  border-radius: 12px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #eaeaea;
}

.date-item.active {
  background-color: #1677ff;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.date-weekday {
  font-size: 13px;
  margin-bottom: 6px;
  font-weight: 500;
}

.date-day {
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.date-month {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 4px;
}

/* 时间段选择 */
.time-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.no-time-slots {
  grid-column: span 3;
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.time-slot {
  padding: 14px 0;
  text-align: center;
  background-color: #f5f7fa;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
  border: 1px solid #eaeaea;
}

.time-slot.active {
  background-color: #1677ff;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.time-slot.disabled {
  background-color: #f0f0f0;
  color: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

/* 预约按钮 */
.book-button {
  width: 100%;
  padding: 14px 0;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.book-button:hover {
  background-color: #0e5edb;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.3);
}

.book-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transform: none;
}

.time-slot.disabled {
  position: relative;
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.reserved-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 10px;
  color: #f5222d;
  background-color: rgba(245, 34, 45, 0.1);
  padding: 0 4px;
  border-radius: 4px;
}

.booking-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.cancel-button {
  width: 100%;
  padding: 14px 0;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.2);
}

.cancel-button:hover {
  background-color: #ff7875;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.3);
}

.cancel-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transform: none;
}

.cancel-dialog-content {
  padding: 20px;
  text-align: center;
}

.warning-text {
  color: #ff4d4f;
  margin-top: 10px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px;
}

.cancel-dialog-button {
  padding: 10px 30px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-dialog-button.cancel {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.cancel-dialog-button.confirm {
  background-color: #ff4d4f;
  color: white;
  border: none;
}

.cancel-dialog-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}


</style>
