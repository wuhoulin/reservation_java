<template>
  <div class="reservations-container">
    <div class="header">
      <div class="title">我的预约 & 报名</div>
      <div class="header-right">
        <div class="record-count" :class="{ zero: filteredList.length === 0 }">
          {{ filteredList.length }}条记录
        </div>
        <div class="filter-toggle" @click="showFilter = !showFilter" :class="{ active: showFilter }">
          <span class="filter-text">筛选</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card total" @click="setFilterStatus('all')" :class="{ active: filterStatus === 'all' }">
        <div class="stat-icon-bg">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.total }}</span>
          <span class="stat-label">全部</span>
        </div>
      </div>
      <div class="stat-card ongoing" @click="setFilterStatus('ongoing')" :class="{ active: filterStatus === 'ongoing' }">
        <div class="stat-icon-bg">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.ongoing }}</span>
          <span class="stat-label">进行中</span>
        </div>
      </div>
      <div class="stat-card completed" @click="setFilterStatus('completed')" :class="{ active: filterStatus === 'completed' }">
        <div class="stat-icon-bg">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.completed }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
      <div class="stat-card cancelled" @click="setFilterStatus('cancelled')" :class="{ active: filterStatus === 'cancelled' }">
        <div class="stat-icon-bg">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.cancelled }}</span>
          <span class="stat-label">已取消</span>
        </div>
      </div>
    </div>

    <transition name="expand">
      <div v-if="showFilter" class="filter-panel">
        <div class="filter-row">
          <div class="f-label">类型</div>
          <div class="f-options">
            <div v-for="opt in typeOptions" :key="opt.value"
                 class="f-pill" :class="{ active: filterType === opt.value }"
                 @click="filterType = opt.value">
              {{ opt.label }}
            </div>
          </div>
        </div>
        <div class="filter-row time-row">
          <div class="f-label">时间</div>
          <div class="date-range-box">
            <div class="date-input-wrapper">
              <input type="date" v-model="startDate" class="date-input" placeholder="开始日期">
              <span class="date-placeholder" v-if="!startDate">开始日期</span>
            </div>
            <span class="range-separator">至</span>
            <div class="date-input-wrapper">
              <input type="date" v-model="endDate" class="date-input" placeholder="结束日期">
              <span class="date-placeholder" v-if="!endDate">结束日期</span>
            </div>
            <div class="reset-date-btn" @click="resetDateFilter" v-if="startDate || endDate">
              重置
            </div>
          </div>
        </div>
      </div>
    </transition>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在加载记录...</p>
    </div>

    <div v-else-if="filteredList.length === 0" class="empty-state">
      <div class="empty-icon-bg">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
      </div>
      <h3>没有找到相关记录</h3>
      <p>去预约一个教室或参加一场活动吧</p>
      <div class="empty-actions">
        <button class="btn-primary" @click="router.push('/community-list')">去预约</button>
        <button class="btn-outline" @click="router.push('/activity-list')">找活动</button>
      </div>
    </div>

    <div v-else class="record-list">
      <div
          v-for="item in filteredList"
          :key="item.uniqueId"
          class="record-card"
          :class="getStatusClass(item.status, item.type)"
          @click="goDetail(item)"
      >
        <div class="card-header">
          <div class="header-left">
            <div class="icon-box" :class="item.type">
              <svg v-if="item.type === 'room'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path><line x1="4" y1="22" x2="4" y2="15"></line></svg>
            </div>

            <div class="header-content">
              <div class="type-badge" :class="item.type">
                {{ item.type === 'room' ? '教室预约' : '活动报名' }}
              </div>
              <h3 class="item-title">{{ item.title }}</h3>
            </div>
          </div>

          <div class="status-badge" :class="getStatusClass(item.status, item.type)">
            {{ getStatusText(item.status, item.type) }}
          </div>
        </div>

        <div class="card-body">
          <div class="info-row time-row-container">
            <span class="icon">📅</span>
            <div class="time-range-inline">
              <span class="time-val">{{ item.startTimeStr }}</span>
              <span class="time-sep">~</span>
              <span class="time-val">{{ item.endTimeStr || '待定' }}</span>
            </div>
          </div>

          <div class="info-row">
            <span class="icon">🗺️</span>
            <span class="text">{{ item.location }}</span>
          </div>
          <div class="info-row sub-info">
            <span class="icon">🏢</span>
            <span class="text">{{ item.subTitle }}</span>
          </div>
        </div>

        <div class="card-footer">
          <button
              v-if="canCancel(item)"
              class="btn-action cancel"
              @click.stop="confirmCancel(item)"
          >
            取消
          </button>

          <button class="btn-action detail">
            查看详情
          </button>
        </div>
      </div>
    </div>

    <div style="height: 40px;"></div>

    <div v-if="showCancelModal" class="modal-mask" @click="showCancelModal = false">
      <div class="modal-box" @click.stop>
        <div class="modal-icon warning">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
        </div>
        <h3>确认取消?</h3>
        <p>您确定要取消「{{ selectedItem?.title }}」吗？<br>取消后可能无法恢复。</p>
        <div class="modal-btns">
          <button class="btn-gray" @click="showCancelModal = false">再想想</button>
          <button class="btn-red" @click="executeCancel">确定取消</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyReservations, cancelReservation } from '@/api/reservations.js'
import { getMyActivityPage, cancelActivity } from '@/api/activity.js'

const router = useRouter()
const loading = ref(true)
const showFilter = ref(false)
const showCancelModal = ref(false)
const selectedItem = ref(null)

// 筛选状态: all, ongoing(进行中), completed(已完成), cancelled(已取消)
const filterStatus = ref('all')
const filterType = ref('all')
const startDate = ref('')
const endDate = ref('')

const allRecords = ref([])

const typeOptions = [
  { value: 'all', label: '全部' },
  { value: 'room', label: '教室' },
  { value: 'activity', label: '活动' }
]

const loadData = async () => {
  loading.value = true
  try {
    const [roomRes, actRes] = await Promise.all([
      getMyReservations(null).catch(() => ({ code: 500, data: [] })),
      getMyActivityPage({ current: 1, size: 100 }).catch(() => ({ code: 500, data: { records: [] } }))
    ])

    const merged = []

    // 1. 处理教室数据 (Room)
    // 假设状态: 0=待审核, 1=已预约, 2=已拒绝, 3=已取消, 4=已完成, 5=已过期
    if (roomRes.code === 200 && roomRes.data) {
      roomRes.data.forEach(item => {
        const dateStr = item.reservationDate || '';
        const startStr = dateStr + ' ' + (item.startTime ? item.startTime.substring(0, 5) : '');
        const endStr = dateStr + ' ' + (item.endTime ? item.endTime.substring(0, 5) : '');

        merged.push({
          uniqueId: `room_${item.id}`,
          type: 'room',
          id: item.id,
          cancelId: item.reservationNo,
          title: item.roomName,
          subTitle: item.communityName || '教学区',
          location: item.roomName,
          date: dateStr,
          startTimeStr: startStr,
          endTimeStr: endStr,
          status: String(item.status), // 强转字符串统一处理
          raw: item
        })
      })
    }

    // 2. 处理活动数据 (Activity)
    // 对应数据库: 0=已报名, 1=已签到, 2=已取消, 3=已完成
    const actList = actRes.data?.records || []
    actList.forEach(item => {
      // 日期提取
      const datePart = item.activityStartTime ? item.activityStartTime.split('T')[0] : '';

      merged.push({
        uniqueId: `act_${item.signupId}`,
        type: 'activity',
        id: item.activityId,
        signupId: item.signupId,
        title: item.activityTitle,
        subTitle: '校园活动',
        location: item.activityLocation,
        date: datePart,
        startTimeStr: formatFullTime(item.activityStartTime),
        endTimeStr: formatFullTime(item.activityEndTime),
        status: String(item.status), // 强转字符串
        raw: item
      })
    })

    allRecords.value = merged.sort((a, b) => new Date(b.date) - new Date(a.date))
  } catch (err) {
    console.error(err)
    ElMessage.error('数据加载异常')
  } finally {
    loading.value = false
  }
}

// 🔥 核心修正：筛选与统计逻辑 🔥
const getStatType = (item) => {
  const s = item.status;
  if (item.type === 'activity') {
    // 活动: 0,1=进行中; 3=完成; 2=取消
    if (['0', '1'].includes(s)) return 'ongoing';
    if (s === '3') return 'completed';
    if (s === '2') return 'cancelled';
  } else {
    // 教室: 0,1=进行中; 4=完成; 2,3,5=取消
    if (['0', '1'].includes(s)) return 'ongoing';
    if (s === '4') return 'completed';
    if (['2', '3', '5'].includes(s)) return 'cancelled';
  }
  return 'other';
}

const stats = computed(() => {
  const list = allRecords.value;
  return {
    total: list.length,
    ongoing: list.filter(i => getStatType(i) === 'ongoing').length,
    completed: list.filter(i => getStatType(i) === 'completed').length,
    cancelled: list.filter(i => getStatType(i) === 'cancelled').length
  }
})

const filteredList = computed(() => {
  let list = allRecords.value;

  // 1. 状态筛选
  if (filterStatus.value !== 'all') {
    list = list.filter(i => getStatType(i) === filterStatus.value);
  }

  // 2. 类型筛选
  if (filterType.value !== 'all') {
    list = list.filter(item => item.type === filterType.value);
  }

  // 3. 时间筛选
  if (startDate.value) {
    list = list.filter(item => item.date >= startDate.value);
  }
  if (endDate.value) {
    list = list.filter(item => item.date <= endDate.value);
  }
  return list;
})

// 🔥 核心修正：显示文本与颜色 🔥
const getStatusText = (s, type) => {
  if (type === 'activity') {
    const map = { '0': '已报名', '1': '已签到', '2': '已取消', '3': '已完成' };
    return map[s] || '未知';
  } else {
    const map = { '0': '待审核', '1': '已预约', '2': '已拒绝', '3': '已取消', '4': '已完成', '5': '已过期' };
    return map[s] || '未知';
  }
}

const getStatusClass = (s, type) => {
  if (type === 'activity') {
    if (['0', '1'].includes(s)) return 'st-blue';
    if (s === '3') return 'st-green';
    return 'st-gray';
  } else {
    if (['0', '1'].includes(s)) return 'st-blue';
    if (s === '4') return 'st-green';
    return 'st-gray';
  }
}

const canCancel = (item) => {
  if (item.type === 'activity') {
    return item.status === '0'; // 只有"已报名"未签到前可取消
  } else {
    return item.status === '0' || item.status === '1'; // 教室待审或预约成功可取消
  }
}

const resetDateFilter = () => {
  startDate.value = ''
  endDate.value = ''
}

const setFilterStatus = (s) => filterStatus.value = s

const confirmCancel = (item) => {
  selectedItem.value = item
  showCancelModal.value = true
}

const executeCancel = async () => {
  if (!selectedItem.value) return
  const item = selectedItem.value
  try {
    let res
    if (item.type === 'room') {
      res = await cancelReservation(item.cancelId)
    } else {
      res = await cancelActivity(item.id)
    }

    if (res.code === 200 || res.success) {
      ElMessage.success('已取消')
      showCancelModal.value = false
      const target = allRecords.value.find(r => r.uniqueId === item.uniqueId)
      if(target) {
        // 取消后，活动变为'2'，教室变为'3'
        target.status = (item.type === 'activity') ? '2' : '3';
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goDetail = (item) => {
  if (item.type === 'room') {
    router.push(`/reservation-detail/${item.id}`)
  } else {
    router.push({ name: 'ActivityDetail', params: { id: item.id } })
  }
}

const formatFullTime = (isoStr) => {
  if (!isoStr) return ''
  return String(isoStr).replace('T', ' ').substring(0, 16)
}

onMounted(() => loadData())
</script>

<style scoped>
/* 样式定义 */
.reservations-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif;
}

.header {
  background: #fff; padding: 16px 20px;
  display: flex; justify-content: space-between; align-items: center;
  position: sticky; top: 0; z-index: 99;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.title { font-size: 18px; font-weight: 700; color: #1f2937; }
.header-right { display: flex; align-items: center; gap: 12px; }
.record-count { background: #f3f4f6; padding: 4px 10px; border-radius: 12px; font-size: 12px; color: #6b7280; font-weight: 500; }
.filter-toggle { display: flex; align-items: center; gap: 4px; font-size: 14px; color: #3b82f6; cursor: pointer; transition: opacity 0.2s; }
.filter-toggle:active { opacity: 0.7; }

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px;
  padding: 12px 16px; margin-bottom: 8px;
}
.stat-card {
  background: #fff; border-radius: 12px; padding: 12px 4px;
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02); transition: all 0.2s; border: 1px solid transparent;
}
.stat-card.active { border-color: #3b82f6; background: #eff6ff; }
.stat-icon-bg { width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.stat-info { display: flex; flex-direction: column; align-items: center; }
.stat-number { font-size: 18px; font-weight: 700; line-height: 1; margin-bottom: 4px; }
.stat-label { font-size: 11px; color: #9ca3af; }

.total .stat-icon-bg { background: #f3f4f6; color: #6b7280; } .total .stat-number { color: #374151; }
.ongoing .stat-icon-bg { background: #dbeafe; color: #2563eb; } .ongoing .stat-number { color: #2563eb; }
.completed .stat-icon-bg { background: #d1fae5; color: #059669; } .completed .stat-number { color: #059669; }
.cancelled .stat-icon-bg { background: #fee2e2; color: #dc2626; } .cancelled .stat-number { color: #dc2626; }

.filter-panel {
  background: #fff; padding: 16px 20px; border-radius: 0 0 16px 16px;
  margin-top: -10px; margin-bottom: 12px; position: relative; z-index: 10;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.filter-row { margin-bottom: 16px; display: flex; align-items: center; }
.f-label { font-size: 13px; color: #6b7280; width: 40px; font-weight: 500; flex-shrink: 0; }
.f-options { display: flex; gap: 8px; overflow-x: auto; flex: 1; }
.f-pill {
  padding: 4px 12px; background: #f9fafb; border: 1px solid #e5e7eb;
  border-radius: 14px; font-size: 12px; color: #4b5563; white-space: nowrap; transition: all 0.2s;
}
.f-pill.active { background: #eff6ff; border-color: #3b82f6; color: #3b82f6; font-weight: 500; }

.date-range-box { display: flex; align-items: center; flex: 1; gap: 8px; }
.date-input-wrapper {
  flex: 1; position: relative; height: 32px; background: #f9fafb;
  border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.date-input-wrapper input[type="date"] {
  opacity: 1; border: none; background: transparent; width: 100%; height: 100%;
  padding: 0 8px; font-size: 12px; color: #374151; font-family: inherit;
  text-align: center; outline: none;
}
.date-placeholder { position: absolute; font-size: 12px; color: #9ca3af; pointer-events: none; }
.range-separator { font-size: 12px; color: #9ca3af; }
.reset-date-btn { font-size: 12px; color: #3b82f6; padding: 4px 8px; cursor: pointer; }

.record-list { padding: 0 16px; display: flex; flex-direction: column; gap: 14px; }
.record-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: transform 0.1s;
}
.record-card:active { transform: scale(0.99); }

.card-header {
  padding: 14px; display: flex; justify-content: space-between; align-items: flex-start;
  border-bottom: 1px solid #f3f4f6;
}
.header-left { display: flex; gap: 12px; align-items: center; }

.icon-box {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.icon-box.room { background: #eff6ff; color: #3b82f6; }
.icon-box.activity { background: #fff7ed; color: #f97316; }

.header-content { display: flex; flex-direction: column; justify-content: center; gap: 4px; }
.type-badge {
  font-size: 10px; padding: 1px 6px; border-radius: 4px; width: fit-content; font-weight: 600;
}
.type-badge.room { background: #e0f2fe; color: #0284c7; }
.type-badge.activity { background: #ffedd5; color: #c2410c; }

.item-title { font-size: 15px; font-weight: 700; color: #111827; line-height: 1.3; }

.status-badge {
  font-size: 12px; font-weight: 500; padding: 2px 8px; border-radius: 6px;
  white-space: nowrap; flex-shrink: 0;
}
.st-blue { color: #2563eb; background: #eff6ff; }
.st-green { color: #059669; background: #ecfdf5; }
.st-gray { color: #6b7280; background: #f3f4f6; }

.card-body { padding: 12px 14px; }
.info-row { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #4b5563; margin-bottom: 6px; }
.info-row.sub-info { color: #9ca3af; margin-top: 8px; font-size: 12px; }
.icon { font-size: 14px; }

/* 强制时间不换行 */
.time-range-inline {
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}
.time-val { font-weight: 600; color: #374151; font-size: 12px; font-family: 'DIN Alternate', sans-serif; }
.time-sep { font-size: 12px; color: #9ca3af; margin: 0 4px; }

.card-footer { padding: 10px 14px; display: flex; justify-content: flex-end; gap: 10px; background: #fafafa; }
.btn-action {
  padding: 6px 14px; border-radius: 14px; font-size: 12px; font-weight: 500; border: none; cursor: pointer;
}
.btn-action.cancel { background: #fff; border: 1px solid #ef4444; color: #ef4444; }
.btn-action.detail { background: #f3f4f6; color: #4b5563; }

.loading-state, .empty-state { text-align: center; padding: 60px 0; color: #9ca3af; }
.spinner {
  width: 24px; height: 24px; border: 3px solid #e5e7eb; border-top-color: #3b82f6;
  border-radius: 50%; margin: 0 auto 10px; animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000;
  display: flex; align-items: center; justify-content: center; backdrop-filter: blur(2px);
}
.modal-box {
  background: #fff; width: 80%; max-width: 320px; border-radius: 16px; padding: 24px;
  text-align: center;
}
.modal-icon { width: 48px; height: 48px; border-radius: 50%; margin: 0 auto 16px; display: flex; align-items: center; justify-content: center; }
.modal-icon.warning { background: #fef2f2; color: #ef4444; }
.modal-box h3 { font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #111827; }
.modal-box p { font-size: 14px; color: #6b7280; line-height: 1.5; margin-bottom: 20px; }
.modal-btns { display: flex; gap: 12px; }
.modal-btns button { flex: 1; padding: 10px; border-radius: 10px; border: none; font-size: 14px; font-weight: 600; }
.btn-gray { background: #f3f4f6; color: #4b5563; }
.btn-red { background: #ef4444; color: #fff; }

.expand-enter-active, .expand-leave-active { transition: all 0.3s ease; max-height: 200px; opacity: 1; overflow: hidden; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; padding-top: 0; padding-bottom: 0; }
</style>
