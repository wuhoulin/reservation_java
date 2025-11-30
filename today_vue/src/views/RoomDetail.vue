<template>
  <div class="room-detail-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </div>
      <div class="title">教室详情</div>
      <div class="favorite-button" @click="toggleFavorite">
        <svg v-if="isFavorited" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
        </svg>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 教室详情内容 -->
    <div v-else class="room-detail-content">
      <!-- 教室图片 -->
      <div class="room-image" :style="{ backgroundImage: `url(${room.imageUrl || '/placeholder.svg?height=240&width=400'})` }">
        <div class="room-status-badge" :class="{
          available: room.status === true || room.status === 1,
          unavailable: room.status === false || room.status === 0
        }">
          <span class="status-dot"></span>
          {{ (room.status === true || room.status === 1) ? '可预约' : '不可预约' }}
        </div>
      </div>

      <!-- 教室信息卡片 -->
      <div class="room-info-card">
        <div class="room-header">
          <h1 class="room-name">{{ room.name }}</h1>
          <div class="favorite-indicator" :class="{ favorited: isFavorited }" @click="toggleFavorite">
            <svg v-if="isFavorited" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
          </div>
        </div>

        <div class="room-meta">
          <div class="meta-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span>{{ room.communityName }}</span>
          </div>
          <div class="meta-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>可容纳 {{ room.capacity }} 人</span>
          </div>
        </div>

        <!-- 教室描述 -->
        <div class="room-description">
          <h2 class="section-title">
            <span class="title-icon">📝</span>
            教室介绍
          </h2>
          <p class="description-text">{{ room.description || '暂无介绍' }}</p>
        </div>

        <!-- 预约时间选择 -->
        <div class="booking-section">
          <h2 class="section-title">
            <span class="title-icon">📅</span>
            选择预约时间
          </h2>

          <!-- 日期选择器 -->
          <div class="date-selector-wrapper">
            <div class="date-selector" ref="dateSelector">
              <div
                  v-for="(date, index) in availableDates"
                  :key="index"
                  class="date-item"
                  :class="{
                    active: selectedDateIndex === index,
                    disabled: isDateDisabled(date) // 新增：禁用过去的日期
                  }"
                  @click="selectDate(index)"
                  :style="{ cursor: isDateDisabled(date) ? 'not-allowed' : 'pointer' }"
              >
                <div class="date-weekday">{{ formatWeekday(date) }}</div>
                <div class="date-day">{{ formatDay(date) }}</div>
                <div class="date-month">{{ formatMonth(date) }}</div>
              </div>
            </div>
          </div>

          <!-- 时间段选择 -->
          <div class="time-slots-container">
            <div v-if="timePointsLoading" class="time-loading">
              <div class="mini-spinner"></div>
              <span>加载时间段...</span>
            </div>
            <div v-else-if="allTimePoints.length === 0" class="no-time-slots">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <p>该日期暂无可用时间段</p>
            </div>
            <div v-else class="time-slots">
              <div
                  v-for="timePoint in allTimePoints"
                  :key="timePoint.id"
                  class="time-slot"
                  :class="{
                  'start-point': timePoint.id === selectedStartTimeId,
                  'end-point': timePoint.id === selectedEndTimeId,
                  'middle-point': isMiddlePoint(timePoint.id),
                  'disabled': !(room.status === true || room.status === 1) || !timePoint.available || isIntervalContainsReserved(timePoint.id) || isTimePointDisabled(timePoint)
                }"
                  @click="handleTimePointClick(timePoint.id)"
              >
                <span class="time-text">{{ formatTimePoint(timePoint.point) }}</span>
                <span v-if="!timePoint.available" class="time-badge reserved">已预约</span>
                <span v-else-if="isTimePointDisabled(timePoint)" class="time-badge reserved">已过期</span> <!-- 新增：已过期标签 -->
                <span v-else-if="timePoint.id === selectedStartTimeId" class="time-badge start">开始</span>
                <span v-else-if="timePoint.id === selectedEndTimeId" class="time-badge end">结束</span>
                <span v-else-if="isMiddlePoint(timePoint.id)" class="time-badge middle">选中</span>
              </div>
            </div>
          </div>

          <!-- 区间提示 -->
          <transition name="slide-fade">
            <div v-if="selectedStartTimeId && selectedEndTimeId" class="interval-tip success">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span>已选择区间：{{ getTimePointLabel(selectedStartTimeId) }} - {{ getTimePointLabel(selectedEndTimeId) }} （共 {{ getSelectedIntervalCount() }} 个时间段）</span>
            </div>
          </transition>
          <transition name="slide-fade">
            <div v-if="selectedStartTimeId && !selectedEndTimeId" class="interval-tip info">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span>已选择开始时间：{{ getTimePointLabel(selectedStartTimeId) }}，请选择结束时间</span>
            </div>
          </transition>
        </div>
      </div>

      <!-- 预约表单组件 -->
      <BookingForm
          ref="bookingFormRef"
          v-model="bookingForm"
          @form-validity-change="updateFormValidity"
      />

      <!-- 预约按钮 -->
      <div class="booking-actions">
        <button
            class="book-button"
            :class="{ disabled: !canBook }"
            :disabled="!canBook"
            @click="showTermsModal"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>立即预约</span>
        </button>
        <button v-if="isReservationOwner" class="cancel-button" @click="showCancelModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
          <span>取消预约</span>
        </button>
      </div>
    </div>

    <!-- 使用条款弹窗 -->
    <RulesModal
        v-model:show="termsModalVisible"
        @agree="proceedWithBooking"
    />

    <!-- 取消预约确认弹窗 -->
    <div v-if="cancelModalVisible" class="modal-overlay" @click="cancelModalVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>取消预约</h3>
          <button class="close-btn" @click="cancelModalVisible = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="warning-icon">⚠️</div>
          <p class="confirm-text">确定要取消该预约吗？</p>
          <p class="warning-text">注意：一个月内取消预约超过2次将被禁止预约3个月</p>
        </div>
        <div class="modal-footer">
          <button class="modal-btn secondary" @click="cancelModalVisible = false">取消</button>
          <button class="modal-btn primary" @click="confirmCancel">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getRoomDetail } from "@/api/home.js";
import { getAllTimePoints, getAvailableTimePointsForRoom } from "@/api/timePoint.js";
import { getUserProfile } from "@/api/user.js";
import RulesModal from "@/components/RulesModal.vue";
import BookingForm from "@/components/booking-form.vue";
import { createReservation, cancelReservation } from '@/api/roomDetail.js';
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite.js';
import { ElMessage } from 'element-plus';

// 从localStorage读取用户信息
const currentUser = ref({
  id: localStorage.getItem('wechat_openid') || '',
  info: JSON.parse(localStorage.getItem('user_info')) || {}
});

const router = useRouter();
const route = useRoute();
const roomId = ref(route.params.roomId);
const dateSelector = ref(null);
const bookingFormRef = ref(null);

// 加载状态
const loadingProfile = ref(false);
const room = ref({});
const loading = ref(true);
const timePointsLoading = ref(false);
const selectedDateIndex = ref(0);

// 区间选择相关状态
const selectedStartTimeId = ref(null);
const selectedEndTimeId = ref(null);
const availableTimePointsForRoom = ref([]);

// 收藏状态
const isFavorited = ref(false);
const favoriteLoading = ref(false);

// 其他状态
const termsModalVisible = ref(false);
const isFormValid = ref(false);
const cancelModalVisible = ref(false);

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
  attendees: 1,
  userId: '',
  studentId: ''
});

// 所有时间点（不再过滤available: false）
const allTimePoints = computed(() => {
  return availableTimePointsForRoom.value
      .sort((a, b) => a.point.localeCompare(b.point));
});

// 未来七天日期（过滤掉过去的日期）
const availableDates = computed(() => {
  const dates = [];
  const today = new Date();
  // 只显示今天及未来6天（共7天）
  for (let i = 0; i < 7; i++) {
    const date = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() + i
    );
    dates.push(date);
  }
  return dates;
});

// 选中日期格式化
const selectedFormattedDate = computed(() => {
  const d = availableDates.value[selectedDateIndex.value];
  const Y = d.getFullYear();
  const M = String(d.getMonth() + 1).padStart(2, '0');
  const D = String(d.getDate()).padStart(2, '0');
  return `${Y}-${M}-${D}`;
});

// 时间点格式化
const formatTimePoint = (timePoint) => {
  return timePoint ? timePoint.slice(0, 5) : '';
};

// 是否为预约所有者
const isReservationOwner = computed(() => {
  return room.value.reservationNo && currentUser.value.id === room.value.userId;
});

// 按钮可点击条件
const canBook = computed(() => {
  const isLogin = !!currentUser.value.id;
  const isRoomAvailable = room.value.status === true || room.value.status === 1;
  const hasCompleteInterval = !!selectedStartTimeId.value && !!selectedEndTimeId.value;
  const isFormValidated = isFormValid.value;
  const isTimeValid = isSelectedTimeValid(); // 新增：校验选中的时间是否有效

  return isLogin && isRoomAvailable && hasCompleteInterval && isFormValidated && isTimeValid;
});

// 初始化加载
onMounted(async () => {
  await fetchUserProfile();
  await loadRoomDetail();
  // 初始化时自动选中第一个可用日期（跳过过去的日期）
  initSelectedDate();
  await loadAvailableTimePointsForRoom();
  await checkFavoriteStatus();

  if (dateSelector.value) {
    const selectedElement = dateSelector.value.children[selectedDateIndex.value];
    if (selectedElement) {
      dateSelector.value.scrollLeft = selectedElement.offsetLeft - 20;
    }
  }

  await nextTick();
  const isValid = await bookingFormRef.value?.checkFormValidity();
  isFormValid.value = isValid || false;
});

// 切换日期时重置区间选择
watch(selectedDateIndex, async () => {
  resetTimePointSelection();
  await loadAvailableTimePointsForRoom();
  if (dateSelector.value) {
    const selectedElement = dateSelector.value.children[selectedDateIndex.value];
    if (selectedElement) {
      dateSelector.value.scrollLeft = selectedElement.offsetLeft - 20;
    }
  }
});

// 监听时间区间变化，触发表单校验
watch([selectedStartTimeId, selectedEndTimeId], () => {
  bookingFormRef.value?.checkFormValidity();
});

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!currentUser.value.id) return;

  try {
    const response = await checkFavorite(roomId.value);
    if (response.code === 200) {
      isFavorited.value = response.data;
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error);
  }
};

// 切换收藏状态
const toggleFavorite = async () => {
  if (!currentUser.value.id) {
    ElMessage.warning('请先登录后再收藏');
    router.push('/wechat-auth');
    return;
  }

  if (favoriteLoading.value) return;

  try {
    favoriteLoading.value = true;

    if (isFavorited.value) {
      // 取消收藏
      await removeFavorite(roomId.value);
      isFavorited.value = false;
      ElMessage.success('已取消收藏');
    } else {
      // 添加收藏
      await addFavorite({ roomId: Number(roomId.value) });
      isFavorited.value = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('收藏操作失败:', error);
    ElMessage.error(isFavorited.value ? '取消收藏失败' : '收藏失败');
  } finally {
    favoriteLoading.value = false;
  }
};

// 获取用户信息填充表单
const fetchUserProfile = async () => {
  try {
    loadingProfile.value = true;
    const response = await getUserProfile();
    if (response.code === 200 && response.data) {
      const userProfile = response.data;
      bookingForm.userName = userProfile.userName || '';
      bookingForm.college = userProfile.college || '';
      bookingForm.major = userProfile.major || '';
      bookingForm.contact = userProfile.phonenumber || '';
      bookingForm.userId = userProfile.userId || '';
      bookingForm.studentId = userProfile.studentId || '';
      currentUser.value.id = userProfile.openid || '';
      localStorage.setItem('user_info', JSON.stringify(userProfile));

      await nextTick();
      bookingFormRef.value?.checkFormValidity();
    } else {
      ElMessage.warning(response.message || '用户信息加载失败，表单需手动填写');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.warning('用户信息加载失败，表单需手动填写');
  } finally {
    loadingProfile.value = false;
  }
};

// 加载教室详情
const loadRoomDetail = async () => {
  try {
    loading.value = true;
    const response = await getRoomDetail(roomId.value);
    room.value = response.data || {};
  } catch (error) {
    console.error(`Failed to load room detail for ${roomId.value}:`, error);
    room.value = { status: false };
    ElMessage.error('加载教室信息失败');
  } finally {
    loading.value = false;
  }
};

// 加载可用时间点
const loadAvailableTimePointsForRoom = async () => {
  try {
    timePointsLoading.value = true;
    const response = await getAvailableTimePointsForRoom({
      roomId: Number(roomId.value),
      date: selectedFormattedDate.value
    });
    availableTimePointsForRoom.value = response.data.sort((a, b) => a.point.localeCompare(b.point));
  } catch (error) {
    console.error('Failed to load available time points:', error);
    availableTimePointsForRoom.value = [];
    if (error.response?.status === 400) {
      ElMessage.error('参数错误，请刷新页面重试');
    } else if (error.response?.status === 404) {
      ElMessage.error('时间段接口不存在');
    } else {
      ElMessage.error('加载时间段失败，请稍后重试');
    }
  } finally {
    timePointsLoading.value = false;
  }
};

// 初始化选中日期（跳过过去的日期）
const initSelectedDate = () => {
  const today = new Date();
  today.setHours(0, 0, 0, 0); // 清除时分秒，只比较日期

  // 找到第一个不是过去的日期（默认是今天）
  for (let i = 0; i < availableDates.value.length; i++) {
    const date = new Date(availableDates.value[i]);
    date.setHours(0, 0, 0, 0);
    if (date >= today) {
      selectedDateIndex.value = i;
      break;
    }
  }
};

// 日期选择（过滤掉过去的日期）
const selectDate = (index) => {
  const selectedDate = availableDates.value[index];
  if (isDateDisabled(selectedDate)) {
    ElMessage.warning('不能选择过去的日期');
    return;
  }
  selectedDateIndex.value = index;
};

// 判断日期是否已过期（过去的日期）
const isDateDisabled = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0); // 清除时分秒，只比较日期
  const targetDate = new Date(date);
  targetDate.setHours(0, 0, 0, 0);
  return targetDate < today;
};

// 判断时间点是否已过期
const isTimePointDisabled = (timePoint) => {
  // 如果选中的是今天，才需要判断时间点是否已过
  const selectedDate = availableDates.value[selectedDateIndex.value];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const targetDate = new Date(selectedDate);
  targetDate.setHours(0, 0, 0, 0);

  // 不是今天，直接返回false（不过期）
  if (targetDate > today) return false;

  // 是今天，判断时间点是否已过当前时间
  const [hours, minutes] = timePoint.point.split(':').map(Number);
  const currentTime = new Date();
  const timePointTime = new Date();
  timePointTime.setHours(hours, minutes, 0, 0);

  // 时间点已过当前时间，返回true（过期）
  return timePointTime < currentTime;
};

// 时间点点击处理（修复核心逻辑）
const handleTimePointClick = (timePointId) => {
  const id = Number(timePointId);
  const timePoint = allTimePoints.value.find(tp => tp.id === id);

  // 修复：从room.value中获取status，判断教室是否可预约
  const isRoomAvailable = room.value.status === true || room.value.status === 1;

  // 不可用（已预约/已过期）或教室不可预约时，不允许点击
  if (!timePoint || !timePoint.available || !isRoomAvailable || isTimePointDisabled(timePoint)) return;

  if (selectedStartTimeId.value === id && !selectedEndTimeId.value) {
    selectedStartTimeId.value = null;
    return;
  }

  if (selectedStartTimeId.value && selectedEndTimeId.value) {
    resetTimePointSelection();
    selectedStartTimeId.value = id;
    return;
  }

  if (!selectedStartTimeId.value && !selectedEndTimeId.value) {
    selectedStartTimeId.value = id;
    return;
  }

  if (selectedStartTimeId.value && !selectedEndTimeId.value) {
    const startIndex = allTimePoints.value.findIndex(tp => tp.id === selectedStartTimeId.value);
    const currentIndex = allTimePoints.value.findIndex(tp => tp.id === id);

    if (currentIndex > startIndex) {
      selectedEndTimeId.value = id;
    } else {
      ElMessage.warning('结束时间必须晚于开始时间');
    }
    return;
  }
};

// 校验选中的时间区间是否有效（不早于当前时间）
const isSelectedTimeValid = () => {
  if (!selectedStartTimeId.value || !selectedEndTimeId.value) return false;

  const selectedDate = availableDates.value[selectedDateIndex.value];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const targetDate = new Date(selectedDate);
  targetDate.setHours(0, 0, 0, 0);

  // 选中的是未来日期，直接有效
  if (targetDate > today) return true;

  // 选中的是今天，需要判断开始时间是否晚于当前时间
  const startTimePoint = allTimePoints.value.find(tp => tp.id === selectedStartTimeId.value);
  if (!startTimePoint) return false;

  const [hours, minutes] = startTimePoint.point.split(':').map(Number);
  const currentTime = new Date();
  const startTime = new Date();
  startTime.setHours(hours, minutes, 0, 0);

  // 开始时间必须晚于当前时间（至少当前时间+30分钟，可根据需求调整）
  const minTime = new Date(currentTime.getTime() + 30 * 60 * 1000); // 30分钟后
  return startTime >= minTime;
};

// 重置时间点选择
const resetTimePointSelection = () => {
  selectedStartTimeId.value = null;
  selectedEndTimeId.value = null;
};

// 判断是否为中间时间点
const isMiddlePoint = (timePointId) => {
  if (!selectedStartTimeId.value || !selectedEndTimeId.value) return false;

  const startIndex = allTimePoints.value.findIndex(tp => tp.id === selectedStartTimeId.value);
  const endIndex = allTimePoints.value.findIndex(tp => tp.id === selectedEndTimeId.value);
  const currentIndex = allTimePoints.value.findIndex(tp => tp.id === timePointId);

  return currentIndex > startIndex && currentIndex < endIndex;
};

// 检查区间是否包含已预约时间点
const isIntervalContainsReserved = (timePointId) => {
  if (!selectedStartTimeId.value && !selectedEndTimeId.value) return false;

  const startIndex = selectedStartTimeId.value
      ? allTimePoints.value.findIndex(tp => tp.id === selectedStartTimeId.value)
      : allTimePoints.value.findIndex(tp => tp.id === timePointId);

  const endIndex = selectedEndTimeId.value
      ? allTimePoints.value.findIndex(tp => tp.id === selectedEndTimeId.value)
      : allTimePoints.value.findIndex(tp => tp.id === timePointId);

  for (let i = Math.min(startIndex, endIndex); i <= Math.max(startIndex, endIndex); i++) {
    if (!allTimePoints.value[i].available) {
      return true;
    }
  }
  return false;
};

// 获取时间点显示标签
const getTimePointLabel = (timePointId) => {
  const timePoint = allTimePoints.value.find(tp => tp.id === timePointId);
  return timePoint ? formatTimePoint(timePoint.point) : '';
};

// 获取选中区间的时间段数量
const getSelectedIntervalCount = () => {
  if (!selectedStartTimeId.value || !selectedEndTimeId.value) return 0;

  const startIndex = allTimePoints.value.findIndex(tp => tp.id === selectedStartTimeId.value);
  const endIndex = allTimePoints.value.findIndex(tp => tp.id === selectedEndTimeId.value);

  return endIndex - startIndex + 1;
};

// 日期格式化工具
const formatDay = (date) => date.getDate();
const formatWeekday = (date) => ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
const formatMonth = (date) => `${date.getMonth() + 1}月`;

// 显示条款弹窗
const showTermsModal = () => {
  if (canBook.value) {
    termsModalVisible.value = true;
  } else if (!isSelectedTimeValid()) {
    ElMessage.warning('预约时间必须晚于当前时间30分钟以上');
  }
};

// 提交预约
const proceedWithBooking = async () => {
  try {
    if (!currentUser.value.id) {
      ElMessage.error('请先登录');
      router.push('/wechat-auth');
      return;
    }

    // 再次校验时间有效性（防止绕过前端限制）
    if (!isSelectedTimeValid()) {
      ElMessage.error('预约时间必须晚于当前时间30分钟以上');
      termsModalVisible.value = false;
      return;
    }

    const isFormValid = await bookingFormRef.value?.checkFormValidity();
    if (!isFormValid) {
      ElMessage.error('表单存在未填写或错误项，请检查后重试');
      termsModalVisible.value = false;
      return;
    }

    const timePointIds = [selectedStartTimeId.value, selectedEndTimeId.value];

    const formDataCopy = JSON.parse(JSON.stringify(bookingForm));
    const reservationData = {
      roomId: Number(roomId.value),
      reservationDate: selectedFormattedDate.value,
      timePointIds: timePointIds,
      activityName: formDataCopy.activityName,
      department: formDataCopy.department,
      needProjection: formDataCopy.needProjection,
      userName: formDataCopy.userName,
      college: formDataCopy.college,
      major: formDataCopy.major,
      contact: formDataCopy.contact,
      teacherName: formDataCopy.teacherName,
      teacherContact: formDataCopy.teacherContact,
      otherRequirements: formDataCopy.otherRequirements,
      attendees: formDataCopy.attendees,
      userId: formDataCopy.userId,
      studentId: formDataCopy.studentId
    };

    const response = await createReservation(reservationData);
    if (response.code === 200) {
      ElMessage.success('预约已提交，请耐心等待审核');
      resetTimePointSelection();
      resetForm();
      await loadAvailableTimePointsForRoom();
    } else {
      throw new Error(response.message || '预约失败');
    }
  } catch (error) {
    console.error('预约失败:', error);
    const errorMsg = error.response?.data?.message || `预约失败：${error.message}`;
    ElMessage.error(errorMsg);
  } finally {
    termsModalVisible.value = false;
  }
};

// 重置表单
const resetForm = () => {
  Object.keys(bookingForm).forEach(key => {
    if (key === 'needProjection') {
      bookingForm[key] = false;
    } else if (key === 'attendees') {
      bookingForm[key] = 1;
    } else {
      bookingForm[key] = '';
    }
  });
  isFormValid.value = false;
  if (bookingFormRef.value) {
    Object.keys(bookingFormRef.value.touched).forEach(key => {
      bookingFormRef.value.touched[key] = false;
    });
    Object.keys(bookingFormRef.value.errors).forEach(key => {
      bookingFormRef.value.errors[key] = '';
    });
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 显示取消预约弹窗
const showCancelModal = () => {
  if (currentUser.value.id) {
    cancelModalVisible.value = true;
  } else {
    ElMessage.error('请先登录');
    router.push('/wechat-auth');
  }
};

// 确认取消预约
const confirmCancel = async () => {
  try {
    await cancelReservation(room.value.reservationNo, currentUser.value.id);
    ElMessage.success('预约已取消');
    cancelModalVisible.value = false;
    await loadRoomDetail();
    await loadAvailableTimePointsForRoom();
  } catch (error) {
    console.error('取消预约失败:', error);
    ElMessage.error(`取消预约失败: ${error.message}`);
  }
};

// 更新表单有效性
const updateFormValidity = (isValid) => {
  isFormValid.value = isValid;
};
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.room-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 24px;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.back-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #667eea;
}

.back-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: scale(1.05);
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  letter-spacing: 0.5px;
}

.favorite-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #f56565;
}

.favorite-button:hover {
  background: rgba(245, 101, 101, 0.1);
  transform: scale(1.05);
}

/* 教室信息卡片头部 */
.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.room-name {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1.3;
  flex: 1;
  margin-right: 16px;
}

.favorite-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  font-size: 14px;
  color: #718096;
  background: white;
  white-space: nowrap;
}

.favorite-indicator:hover {
  border-color: #f56565;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.15);
}

.favorite-indicator.favorited {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border-color: #fc8181;
  color: #c53030;
}

.favorite-indicator.favorited:hover {
  background: linear-gradient(135deg, #feb2b2 0%, #fc8181 100%);
  color: white;
}

.favorite-text {
  font-weight: 600;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #fff;
  font-size: 16px;
  font-weight: 500;
}

/* 教室详情内容 */
.room-detail-content {
  max-width: 100%;
}

/* 教室图片 */
.room-image {
  height: 280px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.room-image::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.3) 100%);
}

.room-status-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 8px 18px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.room-status-badge.available {
  background: rgba(16, 185, 129, 0.95);
  color: white;
}

.room-status-badge.unavailable {
  background: rgba(239, 68, 68, 0.95);
  color: white;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 教室信息卡片 */
.room-info-card {
  background: white;
  border-radius: 30px 30px 0 0;
  margin-top: -30px;
  padding: 32px 24px;
  position: relative;
  box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.1);
}

.room-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 14px;
  font-weight: 500;
}

.meta-item svg {
  color: #667eea;
}

/* 教室描述 */
.room-description {
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #f6f8fb 0%, #f1f5f9 100%);
  border-radius: 20px;
  border-left: 4px solid #667eea;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 22px;
}

.description-text {
  color: #4a5568;
  line-height: 1.7;
  font-size: 15px;
}

/* 预约时间选择 */
.booking-section {
  margin-bottom: 32px;
}

/* 日期选择器 */
.date-selector-wrapper {
  margin: 20px -24px 24px;
  padding: 0 24px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.date-selector-wrapper::-webkit-scrollbar {
  display: none;
}

.date-selector {
  display: flex;
  gap: 12px;
  padding: 4px 0;
}

.date-item {
  flex-shrink: 0;
  width: 80px;
  padding: 16px 12px;
  text-align: center;
  background: #f7fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.date-item:hover {
  background: #edf2f7;
  transform: translateY(-2px);
}

.date-item.active {
  background: linear-gradient(135deg, #1677ff 0%, #40a9ff 100%);
  color: white;
  border-color: #1677ff;
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.4);
  transform: translateY(-2px);
}

/* 新增：过期日期样式 */
.date-item.disabled {
  background: #f1f5f9;
  color: #cbd5e0;
  opacity: 0.6;
  cursor: not-allowed;
}

.date-item.disabled:hover {
  background: #f1f5f9;
  transform: none;
  box-shadow: none;
}

.date-weekday {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.8;
  margin-bottom: 4px;
}

.date-day {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 2px;
}

.date-month {
  font-size: 12px;
  opacity: 0.8;
}

/* 时间段选择 */
.time-slots-container {
  min-height: 200px;
}

.time-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 20px;
  color: #718096;
}

.mini-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.no-time-slots {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.no-time-slots svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-time-slots p {
  font-size: 15px;
  font-weight: 500;
}

.time-slots {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.time-slot {
  position: relative;
  padding: 16px 12px;
  text-align: center;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  color: #2d3748;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.time-slot:hover:not(.disabled) {
  background: #edf2f7;
  border-color: #cbd5e0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 开始时间节点样式 */
.time-slot.start-point {
  background: linear-gradient(135deg, #1677ff 0%, #40a9ff 100%);
  color: white;
  border-color: #1677ff;
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.4);
  transform: scale(1.05);
}

/* 结束时间节点样式（改为和开始时间完全一致） */
.time-slot.end-point {
  background: linear-gradient(135deg, #1677ff 0%, #40a9ff 100%);
  color: white;
  border-color: #1677ff;
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.4);
  transform: scale(1.05);
}

.time-slot.middle-point {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #1677ff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.time-slot.disabled {
  background: #f1f5f9;
  color: #cbd5e0;
  cursor: not-allowed;
  opacity: 0.6;
}

.time-text {
  font-size: 15px;
}

.time-badge {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.time-badge.reserved {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

/* 开始/结束标签样式保持一致（白色背景+白色文字） */
.time-badge.start,
.time-badge.end {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.time-badge.middle {
  background: rgba(45, 55, 72, 0.1);
  color: #2d3748;
}

/* 区间提示 */
.interval-tip {
  margin-top: 20px;
  padding: 16px 20px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.6;
}

.interval-tip.success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
  border: 2px solid #10b981;
}

.interval-tip.info {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border: 2px solid #3b82f6;
}

.interval-tip svg {
  flex-shrink: 0;
}

/* 预约按钮 */
.booking-actions {
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.book-button,
.cancel-button {
  width: 100%;
  padding: 18px 24px;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  letter-spacing: 0.5px;
}

.book-button {
  background: linear-gradient(135deg, #1677ff 0%, #40a9ff 100%);
  color: white;
  box-shadow: 0 10px 30px rgba(22, 119, 255, 0.4);
}

.book-button:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(22, 119, 255, 0.5);
}

.book-button.disabled {
  background: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
  box-shadow: none;
  opacity: 0.6;
}

.cancel-button {
  background: linear-gradient(135deg, #fc8181 0%, #f56565 100%);
  color: white;
  box-shadow: 0 10px 30px rgba(245, 101, 101, 0.3);
}

.cancel-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(245, 101, 101, 0.4);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 440px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 24px 24px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f7fafc;
  border-radius: 8px;
  cursor: pointer;
  color: #718096;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #edf2f7;
  color: #2d3748;
}

.modal-body {
  padding: 32px 24px;
  text-align: center;
}

.warning-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.confirm-text {
  font-size: 17px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 16px;
}

.warning-text {
  font-size: 14px;
  color: #e53e3e;
  line-height: 1.6;
  padding: 16px;
  background: #fff5f5;
  border-radius: 12px;
  border-left: 4px solid #fc8181;
}

.modal-footer {
  padding: 20px 24px;
  display: flex;
  gap: 12px;
  background: #f7fafc;
}

.modal-btn {
  flex: 1;
  padding: 14px 20px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-btn.secondary {
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
}

.modal-btn.secondary:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.modal-btn.primary {
  background: linear-gradient(135deg, #fc8181 0%, #f56565 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.3);
}

.modal-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 101, 101, 0.4);
}

/* 响应式适配 */
@media (max-width: 480px) {
  .room-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .room-name {
    font-size: 24px;
    margin-right: 0;
  }

  .favorite-indicator {
    align-self: stretch;
    justify-content: center;
  }

  .time-slots {
    grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
    gap: 10px;
  }

  .date-item {
    width: 70px;
    padding: 14px 10px;
  }

  .date-day {
    font-size: 20px;
  }
}
</style>
