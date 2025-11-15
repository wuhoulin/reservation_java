
<template>
  <div class="reservation-container">

    <!-- 社区选择标签 -->
    <div class="community-tabs">
      <div
          v-for="(community, index) in communities"
          :key="community.id"
          class="tab"
          :class="{ active: activeCommunity === index }"
          @click="activeCommunity = index"
      >
        {{ community.name }}
      </div>
    </div>

    <!-- 位置和时间信息 -->
    <div class="info-section" v-if="communities.length > 0">
      <div class="info-item">
        <i class="icon-location"></i>
        <span>{{ communities[activeCommunity]?.location }}</span>
        <i class="icon-arrow-right"></i>
      </div>
      <div class="info-item">
        <i class="icon-time"></i>
        <span>开放时间 08:30 - 22:00</span>
        <i class="icon-arrow-right"></i>
      </div>
    </div>


    <!-- 搜索栏 -->
    <div class="search-bar">
      <i class="icon-search"></i>
      <input type="text" placeholder="搜索房间" v-model="searchQuery">
      <i class="icon-filter"></i>
    </div>

    <!-- 房间列表 -->
    <div class="room-list" v-if="currentCommunityWithRooms">
      <div
          v-for="(room, index) in filteredRooms"
          :key="room.id"
          class="room-card"
          @click="goToRoomDetail(room)"
      >
        <div class="room-image" :style="{ backgroundImage: `url(${room.imageUrl || '/placeholder.svg?height=200&width=400'})` }"></div>
        <div class="room-info">
          <div class="room-name">{{ room.name }}</div>
          <div class="room-capacity">可容纳 {{ room.capacity }}人</div>
          <div class="room-status" :class="{ available: room.available, unavailable: !room.available }">
            {{ room.available ? '可预约' : '已被预约' }}
          </div>
        </div>
      </div>
    </div>



    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div>加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {getAllCommunities, getAllCommunitiesWithRooms, getCommunityWithRooms} from "@/api/community.js";
import RecentReservations from "@/components/RecentReservations.vue";
import AppointmentQueryCard from "@/components/AppointmentQueryCard.vue";


const router = useRouter();

// 状态变量
const communities = ref([]);
const communitiesWithRooms = ref([]);
const activeCommunity = ref(0);
const searchQuery = ref('');
const loading = ref(true);

// 获取所有社区信息
const fetchCommunities = async () => {
  try {
    loading.value = true;
    const response = await getAllCommunities();
    communities.value = response.data || [];

    if (communities.value.length > 0) {
      // 获取所有社区及其房间信息
      await fetchAllCommunitiesWithRooms();
    }
  } catch (error) {
    console.error('获取社区列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取所有社区及其房间信息
const fetchAllCommunitiesWithRooms = async () => {
  try {
    const response = await getAllCommunitiesWithRooms();
    console.log("responese",response)
    communitiesWithRooms.value = response.data || [];
  } catch (error) {
    console.error('获取社区及房间信息失败:', error);
  }
};

// 获取特定社区及其房间信息
const fetchCommunityWithRooms = async (communityId) => {
  try {
    loading.value = true;
    const response = await getCommunityWithRooms(communityId);

    // 更新特定社区的房间信息
    const index = communitiesWithRooms.value.findIndex(c => c.id === communityId);
    if (index !== -1) {
      communitiesWithRooms.value[index] = response.data;
    } else {
      communitiesWithRooms.value.push(response.data);
    }
  } catch (error) {
    console.error(`获取社区 ${communityId} 的房间信息失败:`, error);
  } finally {
    loading.value = false;
  }
};

// 当前选中社区的房间信息
const currentCommunityWithRooms = computed(() => {
  if (communities.value.length === 0 || communitiesWithRooms.value.length === 0) return null;

  const currentCommunityId = communities.value[activeCommunity.value]?.id;
  return communitiesWithRooms.value.find(c => c.id === currentCommunityId);
});

// 过滤后的房间列表
const filteredRooms = computed(() => {
  const rooms = currentCommunityWithRooms.value?.rooms || [];

  // 为每个房间添加默认属性
  const processedRooms = rooms.map(room => ({
    ...room,
    available: room.available !== undefined ? room.available : true,
    image: room.imageUrl || '/placeholder.svg',  // 👈 修复字段名
    capacity: room.capacity || 20
  }));


  if (!searchQuery.value) return processedRooms;

  return processedRooms.filter(room =>
      room.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

// 监听当前选中的社区变化
watch(activeCommunity, (newIndex) => {
  const communityId = communities.value[newIndex]?.id;
  if (communityId) {
    fetchCommunityWithRooms(communityId);
  }
});

// 跳转到房间详情页
const goToRoomDetail = (room) => {
  // 如果房间不可预约，可以提示用户
  if (room.available === false) {
    alert('该房间已被预约，请选择其他房间');
    return;
  }

  // 跳转到房间详情页，并传递房间信息
  router.push({
    name: 'RoomDetail',
    params: { roomId: room.id },
    // 也可以通过query或state传递更多数据
    state: { room }
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchCommunities();
});
</script>

<style scoped>
.reservation-container {
  max-width: 100%;
  background-color: #f5f7fa;
  min-height: 100vh;
  position: relative;
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
}

.back-button {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 24px;
}

/* 社区选择标签 */
.community-tabs {
  display: flex;
  background-color: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #eee;
  overflow-x: auto;
}

.tab {
  padding: 12px 16px;
  font-size: 15px;
  color: #666;
  position: relative;
  white-space: nowrap;
  cursor: pointer;
}

.tab.active {
  color: #1677ff;
  font-weight: 500;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background-color: #1677ff;
  border-radius: 2px;
}

/* 位置和时间信息 */
.info-section {
  background-color: #fff;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item i {
  margin-right: 8px;
  color: #999;
}

.icon-location::before {
  content: '📍';
}

.icon-time::before {
  content: '🕒';
}

.icon-arrow-right {
  margin-left: auto;
}

.icon-arrow-right::before {
  content: '›';
  font-size: 20px;
  color: #ccc;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background-color: #fff;
  margin: 10px 16px;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.icon-search::before {
  content: '🔍';
  font-size: 16px;
  color: #999;
}

.search-bar input {
  flex: 1;
  border: none;
  padding: 8px;
  font-size: 14px;
  outline: none;
}

.icon-filter::before {
  content: '≡';
  font-size: 20px;
  color: #999;
}

/* 房间列表 */
.room-list {
  padding: 0 16px 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.room-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s;
}

.room-card:active {
  transform: scale(0.98);
}

.room-image {
  height: 120px;
  background-size: cover;
  background-position: center;
}

.room-info {
  padding: 12px;
}

.room-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
}

.room-capacity {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.room-status {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  padding: 2px 0;
}

.room-status::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
}

.room-status.available {
  color: #52c41a;
}

.room-status.available::before {
  background-color: #52c41a;
}

.room-status.unavailable {
  color: #f5222d;
}

.room-status.unavailable::before {
  background-color: #f5222d;
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
