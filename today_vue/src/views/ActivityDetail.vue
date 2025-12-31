<template>
  <div class="detail-page">
    <div class="nav-header">
      <div class="back-btn" @click="router.back()">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </div>
      <div class="nav-title">活动详情</div>
      <div class="share-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
      </div>
    </div>

    <div v-if="loading" class="state-box">
      <div class="spinner"></div>
      <p>正在加载精彩内容...</p>
    </div>

    <div v-else-if="!activity" class="state-box error">
      <div class="error-icon">😕</div>
      <p>未找到该活动信息</p>
    </div>

    <div v-else class="content-wrapper">
      <div class="banner-image">
        <img
            :src="activity.coverImage || defaultImg"
            alt="banner"
            @error="e => e.target.src = defaultImg"
        />
        <div class="banner-mask"></div>
      </div>

      <div class="info-card header-card">
        <h1 class="activity-title">{{ activity.title }}</h1>
        <div class="tags-row">
          <span class="status-tag" :class="getStatusClass(activity.status)">
            {{ getStatusText(activity.status) }}
          </span>
          <span class="score-tag">
            <span class="icon">🏆</span> 综合素质分 +0.5
          </span>
        </div>

        <div class="data-row">
          <div class="data-item">
            <span class="num">{{ activity.currentPeople || 0 }}</span>
            <span class="label">已报名</span>
          </div>
          <div class="divider-v"></div>
          <div class="data-item">
            <span class="num">{{ activity.maxPeople }}</span>
            <span class="label">名额</span>
          </div>
          <div class="divider-v"></div>
          <div class="data-item">
            <span class="num">{{ activity.viewCount || 0 }}</span>
            <span class="label">浏览</span>
          </div>
        </div>
      </div>

      <div class="info-card list-card">
        <div class="list-item">
          <div class="icon-box blue">🕒</div>
          <div class="content">
            <div class="label">活动时间</div>
            <div class="value">{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</div>
          </div>
        </div>
        <div class="list-item" @click="handleNavigate">
          <div class="icon-box green">📍</div>
          <div class="content">
            <div class="label">活动地点</div>
            <div class="value">{{ activity.location }} <span class="nav-hint">(点击导航)</span></div>
          </div>
          <div class="arrow-right">›</div>
        </div>
        <div class="list-item">
          <div class="icon-box orange">👤</div>
          <div class="content">
            <div class="label">主办方</div>
            <div class="value">{{ activity.organizer || '校团委' }}</div>
          </div>
        </div>
      </div>

      <div class="info-card detail-content">
        <div class="section-header">
          <div class="section-title">活动介绍</div>
          <div class="section-line"></div>
        </div>
        <div class="text-body" v-if="activity.description" v-html="activity.description"></div>
        <div class="text-body empty" v-else>暂无详细介绍</div>

        <div class="remark-box" v-if="activity.remark">
          <span class="remark-icon">💡</span>
          <span class="remark-text">注：{{ activity.remark }}</span>
        </div>
      </div>

      <div class="bottom-spacer"></div>

      <div class="bottom-action-bar">
        <div class="left-actions">
          <div class="action-btn" @click="handleNavigate">
            <span class="action-icon">🗺️</span>
            <span class="action-text">导航</span>
          </div>
          <div class="action-btn">
            <span class="action-icon">💬</span>
            <span class="action-text">咨询</span>
          </div>
        </div>

        <button
            class="main-btn"
            :class="{
              'disabled': isButtonDisabled && !isJoined,
              'btn-cancel': isJoined,
              'btn-primary': !isJoined && !isButtonDisabled
            }"
            @click="handleBtnClick"
            :disabled="isButtonDisabled && !isJoined"
        >
          {{ buttonText }}
        </button>
      </div>
    </div>

    <transition name="fade">
      <div v-if="modal.show" class="custom-modal-mask" @click="closeModal">
        <div class="custom-modal-box" @click.stop>
          <div class="modal-header">
            <h3>{{ modal.title }}</h3>
          </div>
          <div class="modal-content">
            {{ modal.content }}
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="closeModal">取消</button>
            <button class="modal-btn confirm" :class="{ 'danger': modal.isDanger }" @click="confirmAction">
              {{ modal.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="slide-top">
      <div v-if="toast.show" class="custom-toast" :class="toast.type">
        <span class="toast-icon">{{ toast.icon }}</span>
        <span class="toast-text">{{ toast.message }}</span>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getActivityDetail, joinActivity, cancelActivity, checkIsJoined } from '@/api/activity';

const route = useRoute();
const router = useRouter();

// --- 数据状态 ---
const loading = ref(true);
const activity = ref(null);
const isJoined = ref(false);
const defaultImg = 'https://via.placeholder.com/800x400?text=Activity';

const modal = reactive({
  show: false, title: '', content: '', confirmText: '确定', isDanger: false, actionType: ''
});

const toast = reactive({
  show: false, message: '', type: 'success', icon: '✅'
});

// --- 初始化 ---
const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;

  loading.value = true;
  try {
    const res = await getActivityDetail(id);
    activity.value = res.data || res;
    try {
      const joinRes = await checkIsJoined(id);
      isJoined.value = joinRes.data;
    } catch (e) { isJoined.value = false; }
  } catch (error) {
    showToast('加载失败，请重试', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchDetail());

// --- 计算属性 ---
const isButtonDisabled = computed(() => {
  if (!activity.value) return true;
  if (isJoined.value) return false;
  return activity.value.status !== '0' ||
      (activity.value.currentPeople >= activity.value.maxPeople);
});

const buttonText = computed(() => {
  if (!activity.value) return '加载中...';
  if (isJoined.value) return '取消报名';
  if (activity.value.status === '2') return '已结束';
  if (activity.value.status === '3') return '已取消';
  if (activity.value.currentPeople >= activity.value.maxPeople) return '名额已满';
  return '立即报名';
});

// --- 交互逻辑 ---

// 导航功能
const handleNavigate = () => {
  if (!activity.value || !activity.value.location) return;
  // 这里可以调用微信 JSSDK 打开地图，或者跳转到百度/高德地图网页版
  // 暂时用 Toast 模拟
  showToast(`正在为您规划前往“${activity.value.location}”的路线...`, 'success');

  // 示例：跳转百度地图网页版
  // window.location.href = `http://api.map.baidu.com/geocoder?address=${activity.value.location}&output=html&src=webapp`;
};

const handleBtnClick = () => {
  if (isJoined.value) {
    openModal({
      title: '取消报名',
      content: '确定要取消参加该活动吗？取消后可能无法再次报名。',
      confirmText: '确定取消',
      isDanger: true,
      actionType: 'cancel'
    });
  } else {
    openModal({
      title: '确认报名',
      content: `您即将报名参加“${activity.value.title}”，是否确认？`,
      confirmText: '立即报名',
      isDanger: false,
      actionType: 'join'
    });
  }
};

const confirmAction = async () => {
  closeModal();
  const id = activity.value.activityId;

  if (modal.actionType === 'cancel') {
    try {
      await cancelActivity(id);
      showToast('已成功取消报名', 'success');
      fetchDetail();
    } catch (e) {
      showToast(e.msg || '取消失败', 'error');
    }
  } else if (modal.actionType === 'join') {
    try {
      await joinActivity(id);
      showToast('报名成功！请准时参加', 'success');
      fetchDetail();
    } catch (e) {
      if(e.code === 401) showToast('请先登录', 'warning');
      else showToast(e.msg || '报名失败，名额可能已满', 'error');
    }
  }
};

// --- 工具函数 ---
const openModal = (config) => {
  modal.title = config.title;
  modal.content = config.content;
  modal.confirmText = config.confirmText;
  modal.isDanger = config.isDanger;
  modal.actionType = config.actionType;
  modal.show = true;
};
const closeModal = () => { modal.show = false; };
const showToast = (msg, type = 'success') => {
  toast.message = msg;
  toast.type = type;
  toast.icon = type === 'success' ? '✅' : (type === 'error' ? '❌' : '⚠️');
  toast.show = true;
  setTimeout(() => { toast.show = false; }, 2000);
};
const getStatusText = (s) => ({'0':'报名中','1':'进行中','2':'已结束','3':'已取消'})[String(s)] || '未知';
const getStatusClass = (s) => ({'0':'tag-green','1':'tag-blue','2':'tag-gray'})[String(s)] || 'tag-gray';
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : '';
</script>

<style scoped>
/* ================== 基础布局 ================== */
.detail-page {
  background: #f4f5f9;
  min-height: 100vh;
  padding-bottom: 1px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
}

.state-box {
  padding: 120px 0; text-align: center; color: #999;
  display: flex; flex-direction: column; align-items: center; gap: 10px;
}
.error-icon { font-size: 40px; margin-bottom: 10px; }

/* 导航栏 */
.nav-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: sticky; top: 0; z-index: 100;
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}
.back-btn, .share-btn {
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  color: #333; cursor: pointer; transition: opacity 0.2s;
}
.back-btn:active, .share-btn:active { opacity: 0.6; }
.nav-title { font-size: 16px; font-weight: 600; color: #111; }

/* ================== 内容样式 ================== */
.content-wrapper { animation: fadeIn 0.4s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* Banner */
.banner-image {
  width: 100%; height: 240px; position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.banner-image img { width: 100%; height: 100%; object-fit: cover; }
.banner-mask {
  position: absolute; bottom: 0; left: 0; right: 0; height: 60px;
  background: linear-gradient(to top, rgba(0,0,0,0.05), transparent);
}

/* 通用卡片 */
.info-card {
  background: #fff; margin: 12px 16px; padding: 20px;
  border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.header-card { margin-top: -30px; position: relative; z-index: 2; padding-bottom: 24px; }

/* 头部卡片 */
.activity-title { font-size: 20px; font-weight: 700; margin-bottom: 12px; color: #1f2937; line-height: 1.4; }
.tags-row { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; }

.status-tag { padding: 4px 10px; border-radius: 20px; color: #fff; font-size: 12px; font-weight: 500; }
.tag-green { background: linear-gradient(135deg, #34d399, #059669); box-shadow: 0 2px 6px rgba(5, 150, 105, 0.2); }
.tag-blue { background: linear-gradient(135deg, #60a5fa, #2563eb); }
.tag-gray { background: #9ca3af; }

.score-tag {
  background: #fffbeb; color: #d97706; padding: 4px 10px; border-radius: 20px;
  font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 4px;
}

.data-row { display: flex; justify-content: space-between; align-items: center; padding: 0 10px; }
.data-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
.num { font-size: 18px; font-weight: 700; color: #111; font-family: 'DIN Alternate', sans-serif; }
.label { font-size: 12px; color: #9ca3af; margin-top: 4px; }
.divider-v { width: 1px; height: 20px; background: #f3f4f6; }

/* 列表卡片 */
.list-card { padding: 8px 20px; }
.list-item { display: flex; padding: 16px 0; border-bottom: 1px solid #f9fafb; align-items: center; }
.list-item:active { background-color: #f9f9f9; } /* 点击反馈 */
.list-item:last-child { border-bottom: none; }
.icon-box {
  width: 36px; height: 36px; border-radius: 10px; background: #f3f4f6;
  display: flex; align-items: center; justify-content: center; margin-right: 14px; font-size: 18px;
}
.icon-box.blue { background: #eff6ff; }
.icon-box.green { background: #ecfdf5; }
.icon-box.orange { background: #fff7ed; }
.content { flex: 1; }
.content .label { font-size: 12px; color: #9ca3af; margin-bottom: 4px; }
.content .value { font-size: 14px; color: #374151; line-height: 1.5; font-weight: 500; }
.nav-hint { font-size: 12px; color: #3b82f6; margin-left: 4px; }
.arrow-right { color: #ccc; font-size: 20px; margin-left: 10px; }

/* 详情内容 */
.detail-content { padding: 24px 20px; }
.section-header { display: flex; align-items: center; margin-bottom: 16px; gap: 10px; }
.section-title { font-size: 16px; font-weight: 700; color: #111; }
.section-line { flex: 1; height: 1px; background: #f3f4f6; }
.text-body { font-size: 14px; color: #4b5563; line-height: 1.7; text-align: justify; }
.text-body :deep(img) { max-width: 100%; border-radius: 8px; margin: 10px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.text-body.empty { color: #d1d5db; text-align: center; padding: 20px 0; }
.remark-box {
  margin-top: 20px; background: #fff8f1; padding: 12px; border-radius: 8px;
  display: flex; gap: 8px; font-size: 13px; color: #c2410c;
}

/* ================== 底部操作栏 ================== */
.bottom-spacer { height: 100px; }

.bottom-action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 10px 20px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
  z-index: 9999;
  box-sizing: border-box;
}

.left-actions { display: flex; gap: 24px; margin-right: 20px; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 2px; cursor: pointer; transition: transform 0.1s; }
.action-btn:active { transform: scale(0.9); }
.action-icon { font-size: 20px; }
.action-text { font-size: 10px; color: #6b7280; font-weight: 500; }

.main-btn {
  flex: 1; border: none; outline: none; height: 44px;
  border-radius: 22px; font-size: 16px; font-weight: 600; letter-spacing: 1px;
  transition: all 0.2s; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  cursor: pointer;
}
.main-btn:active { transform: scale(0.98); box-shadow: 0 2px 6px rgba(0,0,0,0.1); }

.btn-primary { background: linear-gradient(135deg, #07c160, #047857); color: #fff; box-shadow: 0 4px 15px rgba(7, 193, 96, 0.3); }
.btn-cancel { background: #fff; border: 1px solid #ef4444; color: #ef4444; box-shadow: none; }
.btn-cancel:active { background: #fef2f2; }
.disabled { background: #e5e7eb; color: #9ca3af; cursor: not-allowed; box-shadow: none; }

/* ================== 自定义弹窗 (Modal) ================== */
.custom-modal-mask {
  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 10000; backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}
.custom-modal-box {
  width: 280px; background: #fff; border-radius: 16px; padding: 24px;
  text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  animation: zoomIn 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes zoomIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.modal-header h3 { font-size: 18px; color: #111; margin-bottom: 12px; }
.modal-content { font-size: 14px; color: #666; line-height: 1.5; margin-bottom: 24px; }
.modal-footer { display: flex; gap: 12px; }
.modal-btn {
  flex: 1; padding: 10px 0; border-radius: 20px; font-size: 14px; font-weight: 600; border: none; cursor: pointer;
}
.modal-btn.cancel { background: #f3f4f6; color: #374151; }
.modal-btn.confirm { background: #07c160; color: #fff; }
.modal-btn.confirm.danger { background: #ef4444; }

/* ================== 自定义轻提示 (Toast) ================== */
.custom-toast {
  position: fixed; top: 100px; left: 50%; transform: translateX(-50%);
  padding: 10px 20px; border-radius: 30px;
  display: flex; align-items: center; gap: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 11000;
  font-size: 14px; font-weight: 500;
}
.custom-toast.success { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }
.custom-toast.error { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
.custom-toast.warning { background: #fffbeb; color: #b45309; border: 1px solid #fde68a; }

/* 动画 */
.slide-top-enter-active, .slide-top-leave-active { transition: all 0.3s ease; }
.slide-top-enter-from, .slide-top-leave-to { opacity: 0; transform: translate(-50%, -20px); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 加载动画 */
.spinner {
  width: 32px; height: 32px; border: 3px solid #e5e7eb;
  border-top-color: #07c160; border-radius: 50%;
  animation: spin 0.8s linear infinite; margin: 0 auto 12px;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
