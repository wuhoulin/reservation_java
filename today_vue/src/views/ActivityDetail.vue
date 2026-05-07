<template>
  <div class="detail-page">
    <div class="nav-header">
      <div class="back-btn" @click="router.back()">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </div>
      <div class="nav-title">活动详情</div>
      <div class="share-btn" @click="handleShareClick">
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

        <div class="list-item" v-if="activity.signupDeadline">
          <div class="icon-box orange">⏳</div>
          <div class="content">
            <div class="label">报名截止</div>
            <div class="value" :class="{'text-danger': isDeadlineOver}">
              {{ formatTime(activity.signupDeadline) }}
            </div>
          </div>
        </div>
        <div class="list-item" @click="handleNavigate">
          <div class="icon-box green">📍</div>
          <div class="content">
            <div class="label">活动地点</div>
            <div class="value">{{ activity.location }} <span class="nav-hint">(点击智能规划路线)</span></div>
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

    <transition name="slide-up">
      <div v-if="routeModal.show" class="route-sheet-mask" @click="closeRouteModal">
        <div class="route-sheet" @click.stop>
          <div class="sheet-header">
            <h3>🚗 AI 智能行程规划</h3>
            <div class="close-icon" @click="closeRouteModal">×</div>
          </div>

          <div v-if="routeLoading" class="loading-box">
            <div class="spinner"></div>
            <p>正在定位并咨询 AI 助手...</p>
            <p class="sub-text">实时分析天气与路况中</p>
          </div>

          <div v-else class="sheet-content">
            <div class="ai-weather-card" v-if="routeData.weather">
              <div class="w-top">
                <span class="w-icon">⛅</span>
                <span class="w-summary">{{ routeData.weather.summary }}</span>
              </div>
              <div class="w-line"></div>
              <div class="w-tips">💡 出行贴士：{{ routeData.weather.tips }}</div>
            </div>

            <div class="route-list" v-if="routeData.routes && routeData.routes.length > 0">
              <div
                  v-for="(item, index) in routeData.routes"
                  :key="index"
                  class="route-item"
                  :class="getRouteClass(item.type)"
                  @click="openAmap(item.link)"
              >
                <div class="route-left">
                  <div class="route-type-row">
                    <span class="route-type-text">{{ item.type }}</span>
                    <span class="recommend-star" v-if="item.recommendation_score >= 5">推荐</span>
                  </div>
                  <div class="route-detail">{{ item.details }}</div>
                  <div class="route-tags">
                    <span class="tag-time">🕒 {{ item.duration }}</span>
                    <span class="tag-cost" v-if="item.cost && item.cost !== '未知'">💰 {{ item.cost }}</span>
                  </div>
                </div>
                <div class="route-right">
                  <div class="go-icon-btn">GO</div>
                </div>
              </div>
            </div>

            <div v-else class="empty-route">
              暂无路线方案，请尝试直接打开地图
            </div>
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
import { initWxShare } from '@/utils/wxShare';

const route = useRoute();
const router = useRouter();

// --- 数据状态 ---
const loading = ref(true);
const activity = ref(null);
const isJoined = ref(false);
const defaultImg = 'https://via.placeholder.com/800x400?text=Activity';

// 弹窗与提示状态
const modal = reactive({ show: false, title: '', content: '', confirmText: '确定', isDanger: false, actionType: '' });
const toast = reactive({ show: false, message: '', type: 'success', icon: '✅' });

// --- AI 路线规划状态 ---
const routeModal = reactive({ show: false });
const routeLoading = ref(false);
const routeData = ref({});

// --- 初始化 ---
const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  loading.value = true;
  try {
    const res = await getActivityDetail(id);
    activity.value = res.data || res;

    // 分享配置
    if (activity.value) {
      initWxShare({
        title: activity.value.title,
        desc: `时间：${formatTime(activity.value.startTime)}\n地点：${activity.value.location}`,
        imgUrl: activity.value.coverImage || 'https://ndnu-yuyue.xyz/logo.png',
      });
    }

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

const handleShareClick = () => {
  showToast('请点击右上角 "..." 发送给朋友', 'success');
};

// --- 🔥🔥🔥 修改点 2：计算属性判断截止时间 🔥🔥🔥 ---
const isDeadlineOver = computed(() => {
  if (!activity.value || !activity.value.signupDeadline) return false;
  return new Date() > new Date(activity.value.signupDeadline);
});

// 计算按钮状态
const isButtonDisabled = computed(() => {
  if (!activity.value) return true;
  if (isJoined.value) return false; // 已报名状态下，按钮不禁用（因为要显示取消报名）

  // 1. 状态不是报名中
  // 2. 人数已满
  // 3. 已过截止时间
  const isFull = activity.value.currentPeople >= activity.value.maxPeople;
  return activity.value.status !== '0' || isFull || isDeadlineOver.value;
});

// 计算按钮文字
const buttonText = computed(() => {
  if (!activity.value) return '加载中...';
  if (isJoined.value) return '取消报名';
  if (activity.value.status === '2') return '已结束';
  if (activity.value.status === '3') return '已取消';
  if (activity.value.currentPeople >= activity.value.maxPeople) return '名额已满';
  if (isDeadlineOver.value) return '报名已截止'; // 新增提示
  return '立即报名';
});

// --- 核心交互逻辑 ---

const handleNavigate = () => {
  if (!activity.value) return;

  if (!activity.value.longitude || !activity.value.latitude) {
    showToast('该地点暂无精确坐标，将为您搜索地名', 'warning');
    setTimeout(() => {
      window.location.href = `https://uri.amap.com/search?keyword=${activity.value.location}&src=mypage`;
    }, 1500);
    return;
  }

  const endPoint = `${activity.value.longitude},${activity.value.latitude}`;
  routeModal.show = true;
  routeLoading.value = true;
  routeData.value = {};

  if (!navigator.geolocation) {
    showToast('您的浏览器不支持定位', 'error');
    routeLoading.value = false;
    return;
  }

  navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords;
        const startPoint = `${longitude},${latitude}`;
        fetchAiRoute(startPoint, endPoint);
      },
      (err) => {
        console.error(err);
        showToast('无法获取您的位置，请检查权限', 'error');
        routeLoading.value = false;
      },
      { enableHighAccuracy: true, timeout: 6000 }
  );
};

const fetchAiRoute = async (from, to) => {
  try {
    const apiUrl = `http://43.139.169.190:9072/ai/route?from=${from}&to=${to}`;

    const res = await fetch(apiUrl);
    const text = await res.text();
    const jsonStr = text.replace(/```json/g, '').replace(/```/g, '').trim();
    const data = JSON.parse(jsonStr);

    routeData.value = data;
  } catch (e) {
    console.error("AI Parse Error", e);
    showToast('AI 规划服务繁忙，为您直接打开地图', 'warning');
    routeData.value = {
      weather: null,
      routes: [{
        type: '直接导航',
        duration: '未知',
        details: '点击直接跳转高德地图APP',
        link: `https://uri.amap.com/navigation?to=${to}&mode=car&src=mypage`
      }]
    };
  } finally {
    routeLoading.value = false;
  }
};

const closeRouteModal = () => { routeModal.show = false; };
const openAmap = (link) => { window.location.href = link; };
const getRouteClass = (type) => {
  if (!type) return 'type-car';
  return type.includes('公交') || type.includes('地铁') ? 'type-bus' : 'type-car';
};

// --- 🔥🔥🔥 修改点 3：核心报名/取消逻辑 (含3小时限制) 🔥🔥🔥 ---
const handleBtnClick = () => {
  if (isJoined.value) {
    // ---- 核心校验开始 ----
    if (activity.value && activity.value.startTime) {
      const now = new Date().getTime();
      const startTime = new Date(activity.value.startTime).getTime();
      // 3小时的毫秒数
      const threeHours = 3 * 60 * 60 * 1000;

      // 如果当前时间 + 3小时 > 开始时间，说明距离开始不到3小时了（或者已经开始了）
      if (now + threeHours > startTime) {
        showToast('活动开始前3小时内及活动期间，无法取消报名', 'warning'); // 使用 warning 样式提示
        return; // ⛔ 直接返回，不弹出确认框
      }
    }
    // ---- 核心校验结束 ----

    openModal({ title: '取消报名', content: '确定要取消参加该活动吗？', confirmText: '确定取消', isDanger: true, actionType: 'cancel' });
  } else {
    // 报名时也检查一下截止时间
    if (isDeadlineOver.value) {
      showToast('很遗憾，报名时间已截止', 'warning');
      return;
    }
    openModal({ title: '确认报名', content: `您即将报名参加“${activity.value.title}”，是否确认？`, confirmText: '立即报名', isDanger: false, actionType: 'join' });
  }
};

const confirmAction = async () => {
  closeModal();
  const id = activity.value.activityId;
  if (modal.actionType === 'cancel') {
    try { await cancelActivity(id); showToast('已成功取消报名', 'success'); fetchDetail(); } catch (e) { showToast(e.msg || '取消失败', 'error'); }
  } else if (modal.actionType === 'join') {
    try { await joinActivity(id); showToast('报名成功！请准时参加', 'success'); fetchDetail(); } catch (e) { if(e.code === 401) showToast('请先登录', 'warning'); else showToast(e.msg || '报名失败', 'error'); }
  }
};

// 工具函数
const openModal = (config) => { Object.assign(modal, config); modal.show = true; };
const closeModal = () => { modal.show = false; };
const showToast = (msg, type = 'success') => {
  toast.message = msg; toast.type = type; toast.icon = type==='success'?'✅':(type==='error'?'❌':'⚠️'); toast.show = true;
  setTimeout(() => { toast.show = false; }, 2000);
};
const getStatusText = (s) => ({'0':'报名中','1':'进行中','2':'已结束','3':'已取消'})[String(s)] || '未知';
const getStatusClass = (s) => ({'0':'tag-green','1':'tag-blue','2':'tag-gray'})[String(s)] || 'tag-gray';
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : '';
</script>

<style scoped>
/* 保持原有样式 */
/* ... 省略重复的布局代码，保持不变 ... */

/* 🔥🔥🔥 修改点 4：新增红色警告字体样式 🔥🔥🔥 */
.text-danger {
  color: #ef4444 !important;
  font-weight: 600;
}

/* ================== 页面基础布局 ================== */
.detail-page {
  background: #f4f5f9;
  min-height: 100vh;
  /* 基础内边距 + 底部安全区，防止被导航栏遮挡 */
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
  box-sizing: border-box;
}

.state-box { padding: 120px 0; text-align: center; color: #999; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.nav-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); position: sticky; top: 0; z-index: 100; box-shadow: 0 1px 0 rgba(0,0,0,0.05); }
.back-btn, .share-btn { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; color: #333; cursor: pointer; }
.nav-title { font-size: 16px; font-weight: 600; color: #111; }
.content-wrapper { animation: fadeIn 0.4s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.banner-image { width: 100%; height: 240px; position: relative; }
.banner-image img { width: 100%; height: 100%; object-fit: cover; }
.banner-mask { position: absolute; bottom: 0; left: 0; right: 0; height: 60px; background: linear-gradient(to top, rgba(0,0,0,0.05), transparent); }
.info-card { background: #fff; margin: 12px 16px; padding: 20px; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.header-card { margin-top: -30px; position: relative; z-index: 2; padding-bottom: 24px; }
.activity-title { font-size: 20px; font-weight: 700; margin-bottom: 12px; color: #1f2937; line-height: 1.4; }
.tags-row { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; }
.status-tag { padding: 4px 10px; border-radius: 20px; color: #fff; font-size: 12px; font-weight: 500; }
.tag-green { background: linear-gradient(135deg, #34d399, #059669); }
.tag-blue { background: linear-gradient(135deg, #60a5fa, #2563eb); }
.tag-gray { background: #9ca3af; }
.score-tag { background: #fffbeb; color: #d97706; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 4px; }
.data-row { display: flex; justify-content: space-between; align-items: center; padding: 0 10px; }
.data-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
.num { font-size: 18px; font-weight: 700; color: #111; font-family: 'DIN Alternate', sans-serif; }
.label { font-size: 12px; color: #9ca3af; margin-top: 4px; }
.divider-v { width: 1px; height: 20px; background: #f3f4f6; }
.list-card { padding: 8px 20px; }
.list-item { display: flex; padding: 16px 0; border-bottom: 1px solid #f9fafb; align-items: center; }
.list-item:last-child { border-bottom: none; }
.icon-box { width: 36px; height: 36px; border-radius: 10px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; margin-right: 14px; font-size: 18px; }
.icon-box.blue { background: #eff6ff; }
.icon-box.green { background: #ecfdf5; }
.icon-box.orange { background: #fff7ed; }
.content { flex: 1; }
.content .label { font-size: 12px; color: #9ca3af; margin-bottom: 4px; }
.content .value { font-size: 14px; color: #374151; line-height: 1.5; font-weight: 500; }
.nav-hint { font-size: 12px; color: #3b82f6; margin-left: 4px; }
.arrow-right { color: #ccc; font-size: 20px; margin-left: 10px; }
.detail-content { padding: 24px 20px; }
.section-header { display: flex; align-items: center; margin-bottom: 16px; gap: 10px; }
.section-title { font-size: 16px; font-weight: 700; color: #111; }
.section-line { flex: 1; height: 1px; background: #f3f4f6; }
.text-body { font-size: 14px; color: #4b5563; line-height: 1.7; text-align: justify; }
.text-body.empty { color: #d1d5db; text-align: center; padding: 20px 0; }
.remark-box { margin-top: 20px; background: #fff8f1; padding: 12px; border-radius: 8px; display: flex; gap: 8px; font-size: 13px; color: #c2410c; }

/* 底部占位块 (双重保险) */
.bottom-spacer { height: 120px; }

/* ================== 底部操作栏 ================== */
.bottom-action-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  padding: 10px 20px;
  /* 适配底部安全区 */
  padding-bottom: calc(15px + env(safe-area-inset-bottom));
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
  z-index: 9999;
  box-sizing: border-box;
}

.left-actions { display: flex; gap: 24px; margin-right: 20px; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 2px; cursor: pointer; }
.action-icon { font-size: 20px; }
.action-text { font-size: 10px; color: #6b7280; font-weight: 500; }
.main-btn { flex: 1; border: none; outline: none; height: 44px; border-radius: 22px; font-size: 16px; font-weight: 600; letter-spacing: 1px; transition: all 0.2s; box-shadow: 0 4px 12px rgba(0,0,0,0.1); cursor: pointer; }
.btn-primary { background: linear-gradient(135deg, #07c160, #047857); color: #fff; }
.btn-cancel { background: #fff; border: 1px solid #ef4444; color: #ef4444; box-shadow: none; }
.disabled { background: #e5e7eb; color: #9ca3af; cursor: not-allowed; box-shadow: none; }

/* 弹窗样式 */
.custom-modal-mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 10000; backdrop-filter: blur(4px); }
.custom-modal-box { width: 280px; background: #fff; border-radius: 16px; padding: 24px; text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.15); }
.modal-header h3 { font-size: 18px; color: #111; margin-bottom: 12px; }
.modal-content { font-size: 14px; color: #666; line-height: 1.5; margin-bottom: 24px; }
.modal-footer { display: flex; gap: 12px; }
.modal-btn { flex: 1; padding: 10px 0; border-radius: 20px; font-size: 14px; font-weight: 600; border: none; cursor: pointer; }
.modal-btn.cancel { background: #f3f4f6; color: #374151; }
.modal-btn.confirm { background: #07c160; color: #fff; }
.modal-btn.confirm.danger { background: #ef4444; }
.custom-toast { position: fixed; top: 100px; left: 50%; transform: translateX(-50%); padding: 10px 20px; border-radius: 30px; display: flex; align-items: center; gap: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 11000; font-size: 14px; font-weight: 500; }
.custom-toast.success { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }
.custom-toast.error { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
.custom-toast.warning { background: #fffbeb; color: #b45309; border: 1px solid #fde68a; }
.spinner { width: 32px; height: 32px; border: 3px solid #e5e7eb; border-top-color: #07c160; border-radius: 50%; animation: spin 0.8s linear infinite; margin: 0 auto 12px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ================== AI 路线规划弹窗 (已修复遮挡和滚动) ================== */
.route-sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  /* ⚠️ 核心修复：层级必须高于底部导航栏(9999) */
  z-index: 10000;
  display: flex;
  align-items: flex-end;
  backdrop-filter: blur(2px);
  touch-action: none;
}

.route-sheet {
  width: 100%;
  background: #fff;
  border-radius: 24px 24px 0 0;
  /* ⚠️ 核心修复：适配安全区，防止贴底 */
  padding: 24px 20px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));

  /* ⚠️ 核心修复：限制高度并允许内部滚动 */
  max-height: 85vh; /* 最高占屏幕 85% */
  min-height: 40vh; /* 最小高度 */

  box-shadow: 0 -10px 40px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column; /* 垂直布局 */
  animation: slideUp 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}

.slide-up-enter-active, .slide-up-leave-active { transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.5, 1); }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100%); }

.sheet-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0; /* 头部不压缩 */
}
.sheet-header h3 { font-size: 20px; font-weight: 700; color: #111; margin: 0; }
.close-icon { font-size: 28px; color: #999; padding: 0 10px; cursor: pointer; line-height: 1; }

/* ⚠️ 核心修复：内容区域可滚动 */
.sheet-content {
  flex: 1; /* 占满剩余高度 */
  overflow-y: auto; /* 开启垂直滚动 */
  -webkit-overflow-scrolling: touch; /* iOS 惯性滚动 */
  padding-bottom: 20px; /* 底部留白 */
}

.loading-box { padding: 40px 0; text-align: center; color: #666; }
.sub-text { font-size: 12px; color: #999; margin-top: 8px; }
.ai-weather-card { background: linear-gradient(135deg, #3b82f6, #2563eb); color: #fff; padding: 16px 20px; border-radius: 16px; margin-bottom: 20px; box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25); }
.w-top { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 600; }
.w-line { width: 100%; height: 1px; background: rgba(255,255,255,0.2); margin: 12px 0; }
.w-tips { font-size: 13px; line-height: 1.5; opacity: 0.95; }
.route-list { display: flex; flex-direction: column; gap: 12px; }
.route-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #f9fafb; border-radius: 16px; border: 1px solid #eee; cursor: pointer; transition: all 0.2s; position: relative; }
.route-item:active { transform: scale(0.98); background: #f3f4f6; }
.type-bus { border-left: 5px solid #10b981; }
.type-car { border-left: 5px solid #f59e0b; }
.route-left { flex: 1; margin-right: 12px; }
.route-type-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.route-type-text { font-weight: 700; font-size: 16px; color: #1f2937; }
.recommend-star { background: #fffbeb; color: #d97706; border: 1px solid #fcd34d; font-size: 10px; padding: 1px 6px; border-radius: 4px; font-weight: bold; }
.route-detail { font-size: 13px; color: #6b7280; margin-bottom: 8px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.route-tags { display: flex; gap: 12px; font-size: 13px; font-weight: 600; color: #374151; }
.go-icon-btn { background: #2563eb; color: #fff; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3); }
</style>
