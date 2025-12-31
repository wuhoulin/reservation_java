<template>
  <div class="activity-page">
    <div class="header-container">
      <div class="search-wrapper">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input
              type="text"
              v-model="searchKeyword"
              @keyup.enter="handleSearch"
              placeholder="搜索活动、讲座、比赛..."
          />
        </div>
      </div>

      <div class="tabs-scroll-area">
        <div class="status-tabs">
          <div
              v-for="tab in tabs" :key="tab.value"
              class="tab-item"
              :class="{ active: currentTab === tab.value }"
              @click="currentTab = tab.value"
          >
            {{ tab.label }}
          </div>
        </div>
      </div>
    </div>

    <div class="activity-list" v-loading="loading">
      <div
          v-for="item in activityList"
          :key="item.activityId"
          class="activity-card"
          @click="goToDetail(item.activityId)"
      >
        <div class="card-cover">
          <img
              :src="item.coverImage || defaultImg"
              loading="lazy"
              alt="cover"
              @error="e => e.target.src = defaultImg"
          />
          <div class="status-badge" :class="getStatusClass(item.status)">
            <span class="dot"></span>
            {{ getStatusText(item.status) }}
          </div>

          <div v-if="item.isJoined" class="joined-badge">
            ✅ 已报名
          </div>
        </div>

        <div class="card-content">
          <div class="content-top">
            <h3 class="title">{{ item.title }}</h3>

            <div class="meta-info">
              <div class="meta-item">
                <span class="icon">📅</span>
                <span>{{ formatTime(item.startTime) }}</span>
              </div>
              <div class="meta-item">
                <span class="icon">📍</span>
                <span class="loc-text">{{ item.location }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="progress-box">
              <div class="progress-text">
                <span class="label">已报 {{ item.currentPeople || 0 }}/{{ item.maxPeople }}</span>
                <span class="percent">{{ calculatePercent(item) }}%</span>
              </div>
              <div class="progress-track">
                <div
                    class="progress-fill"
                    :style="{ width: calculatePercent(item) + '%' }"
                    :class="getProgressColorClass(item)"
                ></div>
              </div>
            </div>

            <div class="btn-wrapper">
              <div v-if="item.isJoined" class="go-btn joined">
                <span>✓ 已报名</span>
              </div>
              <div v-else class="go-btn normal">
                <span>报名</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && activityList.length === 0" class="empty-state">
        <img src="https://img.icons8.com/clouds/200/000000/box.png" alt="empty" class="empty-img"/>
        <p>暂时没有相关活动哦~</p>
      </div>
    </div>

    <div class="bottom-spacer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
// 确保你的 api/activity.js 路径正确
import { getActivityPage } from '@/api/activity';

const router = useRouter();
const loading = ref(false);
const activityList = ref([]);
const defaultImg = 'https://via.placeholder.com/200x200?text=Activity';

const searchKeyword = ref('');
const currentTab = ref('all');

// 状态 Tabs
const tabs = [
  { label: '全部', value: 'all' },
  { label: '🔥 报名中', value: '0' },
  { label: '✅ 已报名', value: 'joined' },
  { label: '🏁 已结束', value: '2' }
];

// 获取数据逻辑
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      current: 1,  // 暂时只拉取第一页
      size: 50,
      keyword: searchKeyword.value
    };

    // 只有当 Tab 不为 'all' 时才传 status 参数
    if (currentTab.value !== 'all') {
      params.status = currentTab.value;
    }

    // --- 调用真实后端接口 ---
    const response = await getActivityPage(params);

    if (response.data && response.data.records) {
      activityList.value = response.data.records;
    } else if (response.records) {
      activityList.value = response.records;
    } else if (response.rows) {
      // 兼容 RuoYi pageHelper
      activityList.value = response.rows;
    } else {
      activityList.value = [];
    }

  } catch (error) {
    console.error("获取活动列表失败:", error);
    activityList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => fetchList();

// 监听 Tab 切换
watch(currentTab, () => fetchList());

// 挂载时加载
onMounted(() => fetchList());

// --- 辅助方法 ---
const goToDetail = (id) => router.push({ name: 'ActivityDetail', params: { id } });

const getStatusText = (status) => {
  const map = { '0': '报名中', '1': '进行中', '2': '已结束', '3': '已取消' };
  return map[String(status)] || '未知';
};

const getStatusClass = (status) => {
  const map = { '0': 'status-blue', '1': 'status-cyan', '2': 'status-gray' };
  return map[String(status)] || 'status-gray';
};

const formatTime = (time) => {
  if (!time) return '';
  // 例如 "2025-12-30 14:00:00" -> "12-30 14:00"
  return time.replace('T', ' ').substring(5, 16);
};

const calculatePercent = (item) => {
  const max = item.maxPeople || 0;
  const current = item.currentPeople || 0;
  if (max === 0) return 0;
  return Math.min(100, Math.floor((current / max) * 100));
};

const getProgressColorClass = (item) => {
  if (String(item.status) === '2') return 'fill-gray';
  const max = item.maxPeople || 0;
  const current = item.currentPeople || 0;
  if (current >= max) return 'fill-red';
  return 'fill-blue';
};
</script>

<style scoped>
/* 基础设置 */
.activity-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Arial, sans-serif;
  color: #333;
}

/* 头部区域 */
.header-container {
  background: #fff;
  padding: 12px 0 0 0;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.search-wrapper { padding: 0 16px 8px; }
.search-bar {
  background: #f2f4f8;
  border-radius: 20px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}
.search-bar:focus-within {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
}
.search-icon { margin-right: 8px; font-size: 14px; color: #999; }
.search-bar input {
  border: none;
  background: transparent;
  width: 100%;
  font-size: 14px;
  color: #333;
  outline: none;
}

/* Tabs */
.tabs-scroll-area {
  overflow-x: auto;
  padding: 0 16px 12px;
  scrollbar-width: none;
}
.tabs-scroll-area::-webkit-scrollbar { display: none; }
.status-tabs { display: flex; gap: 10px; }
.tab-item {
  padding: 6px 16px;
  font-size: 13px;
  color: #666;
  background: #f7f8fa;
  border-radius: 16px;
  white-space: nowrap;
  transition: all 0.3s;
  border: 1px solid transparent;
}
.tab-item.active {
  background: #e6f4ff;
  color: #1677ff;
  border-color: #1677ff;
  font-weight: 600;
}

/* 列表卡片 */
.activity-list { padding: 16px; }

.activity-card {
  background: #fff;
  border-radius: 12px;
  display: flex;
  margin-bottom: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.02);
}
.activity-card:active {
  background-color: #fafafa;
  transform: scale(0.99);
}

/* 左侧图片 */
.card-cover {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  position: relative;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px 0 0 12px;
}

/* 状态角标 */
.status-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 3px;
  backdrop-filter: blur(4px);
}

/* 【新增】已报名角标样式 */
.joined-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(82, 196, 26, 0.95); /* 鲜艳的绿色 */
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 2;
  display: flex;
  align-items: center;
}

.dot { width: 4px; height: 4px; border-radius: 50%; background: #fff; }
.status-blue { background: rgba(22, 119, 255, 0.85); }
.status-cyan { background: rgba(19, 194, 194, 0.85); }
.status-gray { background: rgba(0, 0, 0, 0.5); }

/* 右侧内容 */
.card-content {
  flex: 1;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-info { display: flex; flex-direction: column; gap: 4px; }
.meta-item { display: flex; align-items: center; font-size: 12px; color: #888; }
.meta-item .icon { margin-right: 4px; font-size: 12px; }
.loc-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

/* 底部功能区 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.progress-box { flex: 1; margin-right: 12px; }
.progress-text {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #999;
  margin-bottom: 3px;
}
.progress-text .percent { color: #1677ff; font-weight: bold; }
.progress-track {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}
.progress-fill { height: 100%; border-radius: 2px; transition: width 0.5s ease; }
.fill-blue { background: linear-gradient(90deg, #69c0ff, #1677ff); }
.fill-red { background: linear-gradient(90deg, #ff7875, #ff4d4f); }
.fill-gray { background: #d9d9d9; }

/* 按钮样式 */
.go-btn {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 60px;
}

/* 普通状态 */
.go-btn.normal {
  background: #e6f4ff;
  color: #1677ff;
}
.activity-card:hover .go-btn.normal {
  background: #1677ff;
  color: #fff;
}

/* 已报名状态 */
.go-btn.joined {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid rgba(82, 196, 26, 0.2);
}

.empty-state { text-align: center; padding: 60px 0; color: #999; font-size: 13px; }
.empty-img { width: 100px; margin-bottom: 10px; opacity: 0.6; }
.bottom-spacer { height: 60px; }
</style>
