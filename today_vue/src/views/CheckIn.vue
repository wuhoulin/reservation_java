<template>
  <div class="check-in-page">
    <!-- 全屏地图容器 -->
    <div id="map-container"></div>

    <!-- 加载状态 -->
    <div v-if="mapLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>{{ loadingText }}</p>
      </div>
    </div>

    <!-- 位置信息面板 -->
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
          <span class="coordinate-label">经度：</span>
          <span class="coordinate-value">{{ userLng.toFixed(6) }}</span>
        </div>
        <div class="coordinate-item">
          <span class="coordinate-label">纬度：</span>
          <span class="coordinate-value">{{ userLat.toFixed(6) }}</span>
        </div>
        <div class="coordinate-item">
          <span class="coordinate-label">精度：</span>
          <span class="coordinate-value">{{ accuracy }}米</span>
        </div>
        <div class="coordinate-item" v-if="distance !== null">
          <span class="coordinate-label">距离签到点：</span>
          <span class="coordinate-value" :class="{ 'in-range': isInRange, 'out-range': !isInRange }">
            {{ distance }}米
            <span class="range-status">{{ isInRange ? '✓' : '✗' }}</span>
          </span>
        </div>
      </div>

      <div class="action-area">
        <button
            class="refresh-btn"
            @click="refreshLocation"
            :disabled="loading"
        >
          <span v-if="loading">
            <svg class="loading-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 18V22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M4.93 4.93L7.76 7.76" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M16.24 16.24L19.07 19.07" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M2 12H6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M18 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M4.93 19.07L7.76 16.24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M16.24 7.76L19.07 4.93" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            定位中...
          </span>
          <span v-else>
            <svg class="refresh-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M23 4V10H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M1 20V14H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M3.51 9C4.15841 7.54386 5.15418 6.26643 6.40591 5.28351C7.65764 4.30059 9.12665 3.64155 10.6851 3.36399C12.2436 3.08644 13.845 3.19924 15.3493 3.69204C16.8536 4.18484 18.215 5.04371 19.32 6.2L23 10M1 14L4.68 17.8C5.785 18.9563 7.1464 19.8152 8.65072 20.308C10.155 20.8008 11.7564 20.9136 13.3149 20.636C14.8733 20.3585 16.3424 19.6994 17.5941 18.7165C18.8458 17.7336 19.8416 16.4561 20.49 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            刷新位置
          </span>
        </button>

        <button class="checkin-btn" @click="showCheckInDialog" :disabled="!isInRange || loading">
          <svg class="checkin-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          签到
        </button>
      </div>
    </div>

    <!-- 签到对话框 -->
    <el-dialog
        v-model="showCheckInDialogVisible"
        title="现场签到"
        width="90%"
        max-width="400px"
        center
        :close-on-click-modal="false"
        :close-on-press-escape="false"
    >
      <div class="checkin-dialog-content">
        <div class="room-info-card">
          <div class="room-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 21V5C19 3.89543 18.1046 3 17 3H7C5.89543 3 5 3.89543 5 5V21M19 21L21 21M19 21H14M5 21L3 21M5 21H10M9 6.99998H10M9 11H10M14 6.99998H15M14 11H15M10 21V16C10 15.4477 10.4477 15 11 15H13C13.5523 15 14 15.4477 14 16V21M10 21H14" stroke="#409EFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="room-info">
            <h3>{{ reservation.roomName }}</h3>
            <p>{{ reservation.buildingLocation }}</p>
            <div class="distance-info">
              <span class="distance-label">当前距离：</span>
              <span class="distance-value">{{ distance }}米</span>
            </div>
          </div>
        </div>

        <div class="checkin-status">
          <div class="status-icon" :class="{ 'success': isInRange, 'error': !isInRange }">
            <svg v-if="isInRange" width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17L4 12" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <p class="status-text">{{ isInRange ? '在签到范围内' : '不在签到范围内' }}</p>
          <p class="status-tip">{{ isInRange ? '您可以进行签到' : `需在${reservation.radius}米范围内签到` }}</p>
        </div>

        <div class="checkin-actions">
          <button
              class="confirm-checkin-btn"
              :class="{ 'disabled': !isInRange }"
              @click="handleCheckIn"
              :disabled="!isInRange || checkingIn"
          >
            <span v-if="checkingIn">
              <svg class="loading-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M12 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M12 18V22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M4.93 4.93L7.76 7.76" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16.24 16.24L19.07 19.07" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M2 12H6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M18 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M4.93 19.07L7.76 16.24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16.24 7.76L19.07 4.93" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              签到中...
            </span>
            <span v-else>
              立即签到
            </span>
          </button>
          <button class="cancel-btn" @click="showCheckInDialogVisible = false" :disabled="checkingIn">
            取消
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// ==========================================================
// 【高德地图配置】
const MAP_KEY = 'f239feb56fe63ea40fc1fa48146420cb'
const SECURITY_CODE = '8d1a57ba88fb091269930b809bba6c48'

// 安全配置（必须）
window._AMapSecurityConfig = {
  securityJsCode: SECURITY_CODE,
}
// ==========================================================

// 状态管理
const mapLoading = ref(true)
const loading = ref(false)
const locationLoaded = ref(false)
const userLat = ref(0)
const userLng = ref(0)
const accuracy = ref(0)
const distance = ref(null)
const map = ref(null)
const AMap = ref(null)
const userMarker = ref(null)
const targetMarker = ref(null)
const rangeCircle = ref(null)
const showCheckInDialogVisible = ref(false)
const checkingIn = ref(false)

// 加载文本
const loadingText = ref('正在获取您的位置...')

// 硬编码的预约数据
const reservation = ref({
  roomName: '第一教学楼 101室',
  buildingLocation: '南区教学楼群',
  targetLat: 26.654436,
  targetLng: 119.596169,
  radius: 100
})

// 计算属性
const isInRange = computed(() => {
  return distance.value !== null && distance.value <= reservation.value.radius
})

// 加载脚本
const loadScript = (src) => {
  return new Promise((resolve, reject) => {
    const existingScript = document.querySelector(`script[src*="amap.com"]`)
    if (existingScript && window.AMap) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = src
    script.type = 'text/javascript'

    const timeout = setTimeout(() => {
      reject(new Error('脚本加载超时'))
    }, 15000)

    script.onload = () => {
      clearTimeout(timeout)
      setTimeout(() => {
        if (window.AMap) {
          resolve()
        } else {
          reject(new Error('AMap对象未定义'))
        }
      }, 100)
    }

    script.onerror = () => {
      clearTimeout(timeout)
      reject(new Error(`脚本加载失败: ${src}`))
    }

    document.head.appendChild(script)
  })
}

// 加载地图SDK
const loadAMapSDK = async () => {
  try {
    console.log('开始加载高德地图SDK...')
    await loadScript(`https://webapi.amap.com/maps?v=2.0&key=${MAP_KEY}`)

    if (!window.AMap) {
      throw new Error('高德地图SDK加载失败')
    }

    console.log('高德地图SDK加载成功')
    return window.AMap

  } catch (error) {
    console.error('地图SDK加载失败:', error)
    throw error
  }
}

// 获取用户位置
const getUserLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持定位功能'))
      return
    }

    loadingText.value = '正在获取您的位置...'

    navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
            accuracy: Math.round(position.coords.accuracy)
          })
        },
        (error) => {
          let errorMessage = '定位失败'
          switch(error.code) {
            case error.PERMISSION_DENIED:
              errorMessage = '定位权限被拒绝，请在浏览器设置中允许定位'
              break
            case error.POSITION_UNAVAILABLE:
              errorMessage = '位置信息不可用'
              break
            case error.TIMEOUT:
              errorMessage = '定位请求超时'
              break
          }
          reject(new Error(errorMessage))
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
    )
  })
}

// 计算两点之间的距离（球面距离公式）
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const toRadians = (degrees) => degrees * Math.PI / 180
  const R = 6371000 // 地球半径（米）

  const φ1 = toRadians(lat1)
  const φ2 = toRadians(lat2)
  const Δφ = toRadians(lat2 - lat1)
  const Δλ = toRadians(lng2 - lng1)

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
      Math.cos(φ1) * Math.cos(φ2) *
      Math.sin(Δλ/2) * Math.sin(Δλ/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

  return Math.round(R * c)
}

// 更新距离计算
const updateDistance = () => {
  if (userLat.value && userLng.value) {
    const dist = calculateDistance(
        userLat.value,
        userLng.value,
        reservation.value.targetLat,
        reservation.value.targetLng
    )
    distance.value = dist
    console.log('距离计算结果:', distance.value, '米')
  }
}

// 初始化地图（显示用户位置和签到点）
const initUserLocationMap = async () => {
  mapLoading.value = true

  try {
    // 1. 获取用户位置
    loadingText.value = '正在获取您的位置...'
    const userLocation = await getUserLocation()

    userLat.value = userLocation.lat
    userLng.value = userLocation.lng
    accuracy.value = userLocation.accuracy
    locationLoaded.value = true

    console.log('用户位置获取成功:', userLocation)

    // 2. 计算距离
    updateDistance()

    // 3. 加载地图SDK
    loadingText.value = '正在加载地图...'
    AMap.value = await loadAMapSDK()

    // 4. 检查地图容器
    const container = document.getElementById('map-container')
    if (!container) {
      throw new Error('地图容器不存在')
    }

    // 5. 以用户位置和签到点中心创建地图
    const centerLng = (userLng.value + reservation.value.targetLng) / 2
    const centerLat = (userLat.value + reservation.value.targetLat) / 2

    map.value = new AMap.value.Map('map-container', {
      zoom: 15,
      center: [centerLng, centerLat],
      resizeEnable: true,
      viewMode: '2D'
    })

    console.log('地图实例创建成功')

    // 6. 标记用户位置（蓝色）
    userMarker.value = new AMap.value.Marker({
      position: [userLng.value, userLat.value],
      map: map.value,
      title: '我的位置',
      icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
      zIndex: 100,
      animation: 'AMAP_ANIMATION_DROP'
    })

    // 7. 标记签到点位置（红色）
    targetMarker.value = new AMap.value.Marker({
      position: [reservation.value.targetLng, reservation.value.targetLat],
      map: map.value,
      title: reservation.value.roomName,
      icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
      zIndex: 50
    })

    // 8. 绘制签到范围圈
    rangeCircle.value = new AMap.value.Circle({
      center: [reservation.value.targetLng, reservation.value.targetLat],
      radius: reservation.value.radius,
      strokeColor: isInRange.value ? '#67c23a' : '#f56c6c',
      strokeOpacity: 0.8,
      strokeWeight: 2,
      fillColor: isInRange.value ? '#67c23a' : '#f56c6c',
      fillOpacity: 0.15,
      map: map.value
    })

    // 9. 绘制用户精度圈
    const accuracyCircle = new AMap.value.Circle({
      center: [userLng.value, userLat.value],
      radius: accuracy.value,
      strokeColor: '#409eff',
      strokeOpacity: 0.6,
      strokeWeight: 1,
      fillColor: '#409eff',
      fillOpacity: 0.1,
      map: map.value
    })

    // 10. 调整地图视野，同时显示用户位置和签到点
    const bounds = new AMap.value.Bounds(
        [userLng.value, userLat.value],
        [reservation.value.targetLng, reservation.value.targetLat]
    )
    map.value.setBounds(bounds, true, [80, 80, 80, 80])

    // 11. 监听地图加载完成
    map.value.on('complete', () => {
      console.log('地图加载完成')
      mapLoading.value = false
      ElMessage.success('位置获取成功！' + (isInRange.value ? ' 已在签到范围内' : ' 请在100米内签到'))
    })

    // 设置加载超时
    setTimeout(() => {
      if (mapLoading.value) {
        mapLoading.value = false
        ElMessage.warning('地图加载超时')
      }
    }, 8000)

  } catch (error) {
    console.error('初始化失败:', error)
    mapLoading.value = false

    if (error.message.includes('权限')) {
      ElMessageBox.alert(error.message, '定位权限问题', {
        confirmButtonText: '我知道了',
        type: 'warning'
      })
    } else {
      ElMessage.error(`初始化失败: ${error.message}`)
    }
  }
}

// 刷新位置
const refreshLocation = async () => {
  if (loading.value) return
  loading.value = true

  try {
    // 获取新的位置
    const newLocation = await getUserLocation()

    userLat.value = newLocation.lat
    userLng.value = newLocation.lng
    accuracy.value = newLocation.accuracy

    console.log('位置刷新成功:', newLocation)

    // 更新距离
    updateDistance()

    if (map.value && AMap.value) {
      // 清除之前的标记和精度圈
      if (userMarker.value) {
        userMarker.value.setMap(null)
      }

      // 更新范围圈颜色
      if (rangeCircle.value) {
        rangeCircle.value.setOptions({
          strokeColor: isInRange.value ? '#67c23a' : '#f56c6c',
          fillColor: isInRange.value ? '#67c23a' : '#f56c6c'
        })
      }

      // 添加新标记
      userMarker.value = new AMap.value.Marker({
        position: [newLocation.lng, newLocation.lat],
        map: map.value,
        title: '我的位置',
        icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        zIndex: 100,
        animation: 'AMAP_ANIMATION_DROP'
      })

      // 添加新的精度圈
      new AMap.value.Circle({
        center: [newLocation.lng, newLocation.lat],
        radius: newLocation.accuracy,
        strokeColor: '#409eff',
        strokeOpacity: 0.6,
        strokeWeight: 1,
        fillColor: '#409eff',
        fillOpacity: 0.1,
        map: map.value
      })

      // 重新调整视野
      const bounds = new AMap.value.Bounds(
          [newLocation.lng, newLocation.lat],
          [reservation.value.targetLng, reservation.value.targetLat]
      )
      map.value.setBounds(bounds, true, [80, 80, 80, 80])

      ElMessage.success(`位置已更新！距离签到点 ${distance.value}米`)
    }

  } catch (error) {
    console.error('刷新位置失败:', error)
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

// 显示签到对话框
const showCheckInDialog = () => {
  if (!isInRange.value) {
    ElMessage.warning(`您距离签到点还有 ${distance.value}米，需要在${reservation.value.radius}米范围内签到`)
    return
  }
  showCheckInDialogVisible.value = true
}

// 处理签到
const handleCheckIn = async () => {
  if (!isInRange.value) {
    ElMessage.warning('您不在签到范围内')
    return
  }

  checkingIn.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 签到成功
    ElMessageBox.alert('签到成功！', '签到成功', {
      confirmButtonText: '确定',
      type: 'success',
      callback: () => {
        showCheckInDialogVisible.value = false
        // 这里可以添加跳转逻辑，比如返回上一页
        // router.back()
      }
    })

  } catch (error) {
    ElMessage.error('签到失败，请重试')
  } finally {
    checkingIn.value = false
  }
}

// 生命周期
onMounted(() => {
  setTimeout(() => {
    initUserLocationMap()
  }, 300)
})

onUnmounted(() => {
  if (userMarker.value) {
    userMarker.value.setMap(null)
  }
  if (targetMarker.value) {
    targetMarker.value.setMap(null)
  }
  if (rangeCircle.value) {
    rangeCircle.value.setMap(null)
  }
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<style scoped>
.check-in-page {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #f5f7fa;
}

/* 全屏地图 */
#map-container {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* 加载遮罩 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-content {
  text-align: center;
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-content p {
  color: #333;
  font-size: 18px;
  font-weight: 500;
}

/* 位置信息面板 */
.location-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  z-index: 100;
  animation: slideDown 0.4s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.location-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.location-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.location-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.location-details {
  margin-bottom: 20px;
}

.coordinate-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 10px;
  transition: background-color 0.3s;
}

.coordinate-item:hover {
  background: rgba(64, 158, 255, 0.1);
}

.coordinate-label {
  font-weight: 500;
  color: #666;
  min-width: 100px;
  font-size: 14px;
}

.coordinate-value {
  flex: 1;
  color: #333;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.coordinate-value.in-range {
  color: #67c23a;
  font-weight: 600;
}

.coordinate-value.out-range {
  color: #f56c6c;
  font-weight: 600;
}

.range-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 12px;
  margin-left: 8px;
}

.coordinate-value.in-range .range-status {
  background: #67c23a;
  color: white;
}

.coordinate-value.out-range .range-status {
  background: #f56c6c;
  color: white;
}

.action-area {
  display: flex;
  gap: 12px;
}

.refresh-btn, .checkin-btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.refresh-btn {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: white;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.refresh-btn:disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
  box-shadow: none;
}

.checkin-btn {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.checkin-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.checkin-btn:disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
  box-shadow: none;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

.refresh-icon, .checkin-icon {
  transition: transform 0.3s;
}

.refresh-btn:hover .refresh-icon {
  transform: rotate(180deg);
}

.checkin-btn:hover .checkin-icon {
  transform: scale(1.1);
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 签到对话框样式 */
.checkin-dialog-content {
  padding: 10px 0;
}

.room-info-card {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
}

.room-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.room-info h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.room-info p {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

.distance-info {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.distance-label {
  color: #666;
}

.distance-value {
  color: #409eff;
  font-weight: 600;
  margin-left: 5px;
}

.checkin-status {
  text-align: center;
  margin-bottom: 25px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.status-icon.success {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.status-icon.error {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.status-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 5px 0;
}

.status-tip {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.checkin-actions {
  display: flex;
  gap: 12px;
}

.confirm-checkin-btn, .cancel-btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.confirm-checkin-btn {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.confirm-checkin-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.confirm-checkin-btn.disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
  box-shadow: none;
}

.cancel-btn {
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
}

.cancel-btn:hover:not(:disabled) {
  background: #f5f5f5;
  transform: translateY(-2px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .location-panel {
    top: 10px;
    left: 10px;
    right: 10px;
    padding: 16px;
  }

  .location-icon {
    width: 40px;
    height: 40px;
  }

  .location-title {
    font-size: 18px;
  }

  .coordinate-label {
    min-width: 90px;
    font-size: 13px;
  }

  .coordinate-value {
    font-size: 13px;
  }

  .refresh-btn, .checkin-btn {
    height: 45px;
    font-size: 15px;
  }

  .room-info-card {
    padding: 15px;
  }

  .room-info h3 {
    font-size: 16px;
  }
}

@media (max-height: 600px) {
  .location-panel {
    top: 10px;
    padding: 12px;
  }

  .location-details {
    margin-bottom: 15px;
  }

  .coordinate-item {
    margin-bottom: 8px;
    padding: 8px;
  }
}
</style>
