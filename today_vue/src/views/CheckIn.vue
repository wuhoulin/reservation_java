<template>
  <div class="check-in-page">

    <template v-if="checkInState === 1 && taskInfo">
      <div id="map-container"></div>

      <div v-if="mapLoading" class="loading-overlay">
        <div class="loading-content"><div class="spinner"></div><p>{{ loadingText }}</p></div>
      </div>

      <div class="location-panel" v-if="!mapLoading && locationLoaded">
        <div class="location-header">
          <div class="location-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2C8.13 2 5 5.13 5 9C5 13.17 9.42 18.92 11.24 21.11C11.64 21.59 12.37 21.59 12.77 21.11C14.58 18.92 19 13.17 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="#409EFF"/>
            </svg>
          </div>
          <div class="location-title">我的位置</div>
        </div>

        <div class="location-details">
          <div class="coordinate-item">
            <span class="coordinate-label">定位精度：</span>
            <span class="coordinate-value">{{ accuracy ? `±${accuracy}米` : '定位中...' }}</span>
          </div>
          <div class="coordinate-item" v-if="distance !== null">
            <span class="coordinate-label">距离教室：</span>
            <span class="coordinate-value" :class="{ 'in-range': isInRange, 'out-range': !isInRange }">
              {{ distance }}米
              <span class="range-status">{{ isInRange ? '✓' : '✗' }}</span>
            </span>
          </div>
        </div>

        <div class="action-area">
          <button class="refresh-btn" @click="refreshLocation" :disabled="loading">刷新定位</button>
          <button class="checkin-btn" @click="showCheckInDialog" :disabled="!isInRange || loading">立即签到</button>
        </div>
      </div>
    </template>

    <div v-else-if="checkInState === 2 && taskInfo" class="upcoming-container">
      <div class="upcoming-card">
        <div class="icon-wrapper">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#409eff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
        </div>
        <h3>下一个活动即将开始</h3>

        <div class="info-group">
          <div class="info-row">
            <span class="label">活动：</span>
            <span class="val">{{ taskInfo.activityName }}</span>
          </div>
          <div class="info-row">
            <span class="label">日期：</span>
            <span class="val">{{ taskInfo.reservationDate }}</span>
          </div>
          <div class="info-row">
            <span class="label">地点：</span>
            <span class="val">{{ taskInfo.roomName }}</span>
          </div>
          <div class="info-row">
            <span class="label">开始时间：</span>
            <span class="val highlight">{{ formatTime(taskInfo.startTimeId) }}</span>
          </div>
        </div>

        <div class="countdown-box">
          <p>距离签到开始还有</p>
          <div class="timer">{{ countdownText }}</div>
        </div>

        <button class="back-btn" @click="$router.push('/my-reservations')">查看预约详情</button>
      </div>
    </div>

    <div v-else class="no-task-container">
      <div v-if="pageInitializing" class="init-loading">
        <div class="spinner"></div>
        <p>正在查询签到任务...</p>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">📅</div>
        <h3>当前暂无需要签到的活动</h3>
        <p class="empty-desc">请在活动开始前 30 分钟内进行签到</p>
        <button class="back-btn" @click="$router.push('/my-reservations')">查看我的预约</button>
      </div>
    </div>

    <el-dialog v-model="showCheckInDialogVisible" title="现场签到" width="90%" max-width="400px" center>
      <div class="checkin-dialog-content">
        <div class="room-info-card">
          <div class="room-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 21V5C19 3.89543 18.1046 3 17 3H7C5.89543 3 5 3.89543 5 5V21M19 21L21 21M19 21H14M5 21L3 21M5 21H10M9 6.99998H10M9 11H10M14 6.99998H15M14 11H15M10 21V16C10 15.4477 10.4477 15 11 15H13C13.5523 15 14 15.4477 14 16V21M10 21H14" stroke="#409EFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="room-info">
            <h3>{{ taskInfo?.roomName }}</h3>
            <p>{{ taskInfo?.activityName }}</p>
            <div class="distance-info">
              <span class="distance-label">当前距离：</span>
              <span class="distance-value">{{ distance }}米</span>
            </div>
          </div>
        </div>

        <div class="checkin-status">
          <div class="status-icon" :class="{ 'success': isInRange, 'error': !isInRange }">
            <svg v-if="isInRange" width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17L4 12" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M18 6L6 18M6 6L18 18" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <p class="status-text">{{ isInRange ? '在签到范围内' : '不在签到范围内' }}</p>
          <p class="status-tip">{{ isInRange ? '您可以进行签到' : `需在${ALLOWED_DISTANCE}米范围内签到` }}</p>
        </div>

        <div class="checkin-actions">
          <button class="confirm-checkin-btn" @click="handleCheckIn" :disabled="!isInRange || checkingIn">
            {{ checkingIn ? '签到中...' : '确认签到' }}
          </button>
          <button class="cancel-btn" @click="showCheckInDialogVisible = false">取消</button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckInState, submitCheckIn } from '@/api/checkin'

const router = useRouter()

// ================= 地图配置 =================
const MAP_KEY = 'f239feb56fe63ea40fc1fa48146420cb'
const SECURITY_CODE = '8d1a57ba88fb091269930b809bba6c48'
window._AMapSecurityConfig = { securityJsCode: SECURITY_CODE }
const ALLOWED_DISTANCE = 200 // 签到范围半径

// ================= 状态变量 =================
const pageInitializing = ref(true)
const checkInState = ref(0) // 0:无, 1:可签, 2:等待
const taskInfo = ref(null)
const countdownText = ref('-- : -- : --')
let timerInterval = null

// 地图相关
const mapLoading = ref(true)
const loading = ref(false)
const locationLoaded = ref(false)
const loadingText = ref('正在加载地图...')
const userLat = ref(0)
const userLng = ref(0)
const accuracy = ref(0)
const distance = ref(null)

const map = ref(null)
const AMap = ref(null)
const geolocation = ref(null)

// 覆盖物
const targetMarker = ref(null) // 目标点图标
const rangeCircle = ref(null)  // 🟢 签到范围圈

const showCheckInDialogVisible = ref(false)
const checkingIn = ref(false)

const isInRange = computed(() => distance.value !== null && distance.value <= ALLOWED_DISTANCE)

// ================= 初始化逻辑 =================
onMounted(async () => {
  try {
    const res = await getCheckInState()
    const data = res.data

    if (data) {
      checkInState.value = data.state
      taskInfo.value = data.taskInfo

      if (checkInState.value === 1) {
        initUserLocationMap()
      } else if (checkInState.value === 2) {
        startCountdown(data.countdownMs)
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取任务失败')
  } finally {
    pageInitializing.value = false
  }
})

// ================= 倒计时逻辑 =================
const startCountdown = (ms) => {
  let remaining = ms
  updateTimerText(remaining)
  timerInterval = setInterval(() => {
    remaining -= 1000
    if (remaining <= 0) {
      clearInterval(timerInterval)
      window.location.reload()
    } else {
      updateTimerText(remaining)
    }
  }, 1000)
}

const updateTimerText = (ms) => {
  if (ms < 0) ms = 0
  const hours = Math.floor(ms / (1000 * 60 * 60))
  const minutes = Math.floor((ms % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((ms % (1000 * 60)) / 1000)
  countdownText.value = `${pad(hours)}小时 ${pad(minutes)}分 ${pad(seconds)}秒`
}

const pad = (n) => n < 10 ? '0' + n : n

const formatTime = (id) => {
  const map = { 1: '08:00', 2: '10:00', 3: '14:00', 4: '16:00', 5: '19:00' }
  return map[id] || '即将开始'
}

// ================= 地图 & 签到逻辑 =================

const loadScript = (src) => {
  return new Promise((resolve, reject) => {
    const existingScript = document.querySelector(`script[src*="plugin=AMap.Geolocation"]`)
    if (existingScript && window.AMap) { resolve(); return }
    const script = document.createElement('script')
    script.src = src
    script.type = 'text/javascript'
    script.onload = () => setTimeout(() => window.AMap ? resolve() : reject(new Error('AMap undefined')), 100)
    script.onerror = () => reject(new Error('脚本加载失败'))
    document.head.appendChild(script)
  })
}

const loadAMapSDK = async () => {
  await loadScript(`https://webapi.amap.com/maps?v=2.0&key=${MAP_KEY}&plugin=AMap.Geolocation`)
  if (!window.AMap) throw new Error('高德SDK加载失败')
  return window.AMap
}

const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const radLat1 = lat1 * Math.PI / 180.0
  const radLat2 = lat2 * Math.PI / 180.0
  const a = radLat1 - radLat2
  const b = (lng1 * Math.PI / 180.0) - (lng2 * Math.PI / 180.0)
  const s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)))
  return Math.round(s * 6378137)
}

const updateDistance = () => {
  if (userLat.value && userLng.value && taskInfo.value) {
    distance.value = calculateDistance(
        userLat.value, userLng.value,
        taskInfo.value.latitude, taskInfo.value.longitude
    )
  }
}

const initUserLocationMap = async () => {
  mapLoading.value = true
  try {
    AMap.value = await loadAMapSDK()

    map.value = new AMap.value.Map('map-container', {
      zoom: 16,
      center: [taskInfo.value.longitude, taskInfo.value.latitude],
      resizeEnable: true,
      viewMode: '2D'
    })

    // 🟢 绘制图标和圆圈
    drawTargetOverlays()

    geolocation.value = new AMap.value.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0,
      convert: true,
      showButton: false,
      panToLocation: false,
      zoomToAccuracy: false
    })
    map.value.addControl(geolocation.value)

    await refreshLocation()

    mapLoading.value = false
    locationLoaded.value = true

  } catch (error) {
    console.error('地图初始化失败:', error)
    mapLoading.value = false
    ElMessage.error('地图加载失败')
  }
}

// 🟢 核心修改：绘制目标红点 + 范围红圈
const drawTargetOverlays = () => {
  if (!map.value || !AMap.value || !taskInfo.value) return

  // 1. 清理旧覆盖物
  if (targetMarker.value) targetMarker.value.setMap(null)
  if (rangeCircle.value) rangeCircle.value.setMap(null)

  // 2. 绘制目标图标 (红点)
  targetMarker.value = new AMap.value.Marker({
    position: [taskInfo.value.longitude, taskInfo.value.latitude],
    map: map.value,
    title: taskInfo.value.roomName,
    icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
    offset: new AMap.value.Pixel(-10, -32),
    zIndex: 50
  })

  // 3. 🟢 绘制范围圆圈 (红色)
  rangeCircle.value = new AMap.value.Circle({
    center: [taskInfo.value.longitude, taskInfo.value.latitude],
    radius: ALLOWED_DISTANCE, // 使用配置的半径 (200米)
    strokeColor: '#f56c6c',   // 边框红色
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#f56c6c',     // 填充红色
    fillOpacity: 0.15,
    map: map.value
  })

  // 自动缩放地图以适应圆圈范围
  map.value.setFitView([rangeCircle.value])
}

const refreshLocation = async () => {
  if (loading.value || !geolocation.value) return

  loading.value = true
  if (!mapLoading.value) ElMessage.info('正在定位...')

  geolocation.value.getCurrentPosition((status, result) => {
    loading.value = false

    if (status === 'complete') {
      userLat.value = result.position.lat
      userLng.value = result.position.lng
      accuracy.value = result.accuracy

      updateDistance()

      // 🟢 动态更新圆圈颜色：在范围内变绿，不在变红
      if (rangeCircle.value) {
        const color = isInRange.value ? '#67c23a' : '#f56c6c'
        rangeCircle.value.setOptions({
          strokeColor: color,
          fillColor: color
        })
      }

      if (!mapLoading.value) ElMessage.success('定位成功')
    } else {
      ElMessage.warning('定位失败，请检查GPS权限')
    }
  })
}

// ================= 签到交互 =================
const showCheckInDialog = () => {
  if (!isInRange.value) {
    ElMessage.warning(`距离太远，请在${ALLOWED_DISTANCE}米范围内签到`)
    return
  }
  showCheckInDialogVisible.value = true
}

const handleCheckIn = async () => {
  if (!isInRange.value) return

  checkingIn.value = true
  try {
    const params = {
      reservationId: taskInfo.value.id,
      longitude: userLng.value,
      latitude: userLat.value
    }
    await submitCheckIn(params)
    ElMessageBox.alert('签到成功！', '提示', {
      confirmButtonText: '确定',
      type: 'success',
      callback: () => {
        showCheckInDialogVisible.value = false
        router.push('/my-reservations')
      }
    })
  } catch (error) {
    const msg = error.response?.data?.message || '签到失败，请重试'
    ElMessage.error(msg)
  } finally {
    checkingIn.value = false
  }
}

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  if (targetMarker.value) targetMarker.value.setMap(null)
  if (rangeCircle.value) rangeCircle.value.setMap(null)
  if (map.value) map.value.destroy()
})
</script>

<style scoped>
.check-in-page { width: 100%; height: 100vh; position: relative; overflow: hidden; background: #f5f7fa; }
#map-container { width: 100%; height: 100%; position: absolute; top: 0; left: 0; z-index: 1; }
.loading-overlay, .no-task-container { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: #f5f7fa; display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 1000; }
.loading-overlay { background: rgba(255,255,255,0.9); }
.loading-content, .init-loading { text-align: center; }
.spinner { width: 50px; height: 50px; border: 4px solid #e0e0e0; border-top: 4px solid #409eff; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 40px; }
.empty-icon { font-size: 60px; margin-bottom: 20px; }
.empty-desc { color: #999; margin: 10px 0 30px; font-size: 14px; }
.back-btn { padding: 10px 25px; background: #409eff; color: white; border: none; border-radius: 20px; font-size: 15px; cursor: pointer; }
.location-panel { position: absolute; bottom: 30px; left: 20px; right: 20px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 16px; padding: 20px; box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15); z-index: 100; animation: slideUp 0.4s ease; }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.location-header { display: flex; align-items: center; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
.location-icon { width: 40px; height: 40px; background: #ecf5ff; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.location-title { font-size: 18px; font-weight: 600; color: #333; }
.location-details { margin-bottom: 20px; }
.coordinate-item { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; color: #666; }
.coordinate-value { font-weight: 500; color: #333; display: flex; align-items: center; }
.coordinate-value.in-range { color: #67c23a; }
.coordinate-value.out-range { color: #f56c6c; }
.range-status { margin-left: 5px; font-size: 12px; padding: 2px 6px; border-radius: 10px; color: white; }
.range-status.in-range { background: #67c23a; }
.range-status.out-range { background: #f56c6c; }
.action-area { display: flex; gap: 12px; }
.refresh-btn, .checkin-btn { flex: 1; height: 44px; border: none; border-radius: 8px; font-size: 15px; font-weight: 500; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; transition: all 0.2s; }
.refresh-btn { background: #f0f2f5; color: #606266; }
.refresh-btn:active { background: #e6e8eb; }
.checkin-btn { background: #409eff; color: white; box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3); }
.checkin-btn:disabled { background: #a0cfff; cursor: not-allowed; box-shadow: none; }
.loading-icon { animation: rotate 1s linear infinite; }
.upcoming-container { height: 100vh; background: #f5f7fa; display: flex; justify-content: center; align-items: center; padding: 20px; }
.upcoming-card { background: white; width: 100%; max-width: 340px; padding: 40px 30px; border-radius: 20px; box-shadow: 0 15px 35px rgba(0,0,0,0.08); text-align: center; animation: fadeIn 0.5s ease; }
.icon-wrapper { margin-bottom: 20px; }
.upcoming-card h3 { margin: 0 0 30px; color: #333; font-size: 20px; font-weight: 600; }
.info-group { text-align: left; background: #f8fafc; padding: 20px; border-radius: 12px; margin-bottom: 25px; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 15px; color: #666; border-bottom: 1px dashed #eee; padding-bottom: 8px; }
.info-row:last-child { margin-bottom: 0; border-bottom: none; padding-bottom: 0; }
.info-row .val { color: #333; font-weight: 500; }
.info-row .highlight { color: #409eff; font-weight: bold; }
.countdown-box { margin: 30px 0; background: linear-gradient(135deg, #ecf5ff, #e6f1fc); padding: 20px; border-radius: 12px; border: 1px solid #d9ecff; }
.countdown-box p { margin: 0 0 10px; font-size: 13px; color: #79bbff; font-weight: 500; }
.timer { font-size: 24px; font-weight: bold; color: #409eff; font-family: 'Monaco', monospace; letter-spacing: 1px; }
.room-info-card { display: flex; align-items: center; background: #f8fafc; border-radius: 12px; padding: 15px; margin-bottom: 20px; }
.room-icon { width: 48px; height: 48px; background: #ecf5ff; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 15px; }
.room-info h3 { margin: 0 0 5px; font-size: 16px; color: #333; }
.room-info p { margin: 0 0 5px; font-size: 13px; color: #666; }
.distance-info { font-size: 13px; color: #999; }
.distance-value { color: #409eff; font-weight: 600; margin-left: 4px; }
.checkin-status { text-align: center; margin-bottom: 25px; }
.status-icon { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; }
.status-icon.success { background: linear-gradient(135deg, #67c23a, #95d475); }
.status-icon.error { background: linear-gradient(135deg, #f56c6c, #f78989); }
.status-text { font-size: 16px; font-weight: 600; margin: 0 0 5px; }
.status-tip { font-size: 12px; color: #999; margin: 0; }
.checkin-actions { display: flex; gap: 10px; }
.confirm-checkin-btn, .cancel-btn { flex: 1; height: 40px; border: none; border-radius: 8px; font-size: 14px; cursor: pointer; }
.confirm-checkin-btn { background: #409eff; color: white; }
.confirm-checkin-btn.disabled { background: #a0cfff; }
.cancel-btn { background: #f5f7fa; color: #606266; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
