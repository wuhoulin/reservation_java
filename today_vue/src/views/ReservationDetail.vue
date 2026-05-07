<template>
  <div class="reservation-detail-container">
    <div class="detail-header">
      <div class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </div>
      <div class="header-title">预约详情</div>
      <div class="header-actions">
        <button v-if="canCancelReservation(reservation)" class="action-btn cancel-btn" @click="handleCancel">取消预约</button>
      </div>
    </div>

    <div class="detail-content" v-if="reservation.id">

      <div class="status-card" :class="getStatusClass(reservation.status)">
        <div class="status-icon">
          <div v-if="reservation.status === 1" class="icon approved">✅</div>
          <div v-else-if="reservation.status === 3" class="icon cancelled">🚫</div>
          <div v-else-if="reservation.status === 4" class="icon completed">🎉</div>
          <div v-else-if="reservation.status === 5" class="icon expired">⏰</div>
          <div v-else class="icon pending">ℹ️</div>
        </div>
        <div class="status-info">
          <div class="status-text">{{ getStatusText(reservation.status) }}</div>
          <div class="status-desc">{{ getStatusDescription(reservation.status) }}</div>
          <div v-if="reservation.status === 4" class="checkin-badge-container">
            <div v-if="reservation.checkInStatus === 1" class="checkin-badge success">
              <span>已于 {{ formatTimeOnly(reservation.checkInTime) }} 签到</span>
            </div>
            <div v-else-if="reservation.checkInStatus === 2" class="checkin-badge error">
              <span class="badge-icon">⚠️</span><span>未签到 (记违约)</span>
            </div>
          </div>
        </div>
        <div class="reservation-no">#{{ reservation.reservationNo }}</div>
      </div>

      <div class="info-section">
        <h3 class="section-title">
          <span class="title-icon">📝</span>
          预约信息详情
        </h3>

        <div class="merged-info-container">
          <div class="info-group">
            <h4 class="group-title">活动与场地</h4>
            <div class="info-grid">
              <div class="info-item full-width">
                <div class="info-label">活动名称</div>
                <div class="info-value highlight">{{ reservation.activityName }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">预约场地</div>
                <div class="info-value">{{ reservation.roomName }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">所属社区</div>
                <div class="info-value">{{ reservation.communityName }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">使用时间</div>
                <div class="info-value time-value">
                  {{ formatDate(reservation.reservationDate) }}
                  <span>{{ reservation.startTime }} - {{ reservation.endTime }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">参与人数</div>
                <div class="info-value">{{ reservation.attendees }}人</div>
              </div>
            </div>
          </div>

          <div class="info-divider"></div>

          <div class="info-group">
            <h4 class="group-title">申请人信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">申请人</div>
                <div class="info-value">{{ reservation.userName }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">联系方式</div>
                <div class="info-value">{{ reservation.contact }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">申请部门</div>
                <div class="info-value">{{ reservation.department }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">学院专业</div>
                <div class="info-value">{{ reservation.college }} - {{ reservation.major }}</div>
              </div>
              <div class="info-item" v-if="reservation.teacherName">
                <div class="info-label">指导老师</div>
                <div class="info-value">{{ reservation.teacherName }} ({{ reservation.teacherContact }})</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="info-section" v-if="reservation.needProjection || reservation.otherRequirements">
        <h3 class="section-title"><span class="title-icon">🛠️</span> 设备需求</h3>
        <div class="requirements-list">
          <div class="requirement-item" v-if="reservation.needProjection">
            <div class="requirement-icon">📽️</div>
            <div class="requirement-text">需要投影设备</div>
          </div>
          <div class="requirement-item" v-if="reservation.otherRequirements">
            <div class="requirement-icon">📝</div>
            <div class="requirement-text">{{ reservation.otherRequirements }}</div>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title"><span class="title-icon">⏱️</span> 状态记录</h3>
        <div class="timeline">
          <div class="timeline-item active">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">预约成功</div>
              <div class="timeline-time">{{ formatDateTime(reservation.createdAt) }}</div>
            </div>
          </div>
          <div class="timeline-item active" v-if="reservation.checkInStatus === 1">
            <div class="timeline-dot dot-success"></div>
            <div class="timeline-content">
              <div class="timeline-title">现场签到</div>
              <div class="timeline-time">{{ formatDateTime(reservation.checkInTime) }}</div>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: reservation.status === 4 || reservation.status === 3 || reservation.status === 5 }">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">{{ getEndStatusTitle(reservation.status) }}</div>
              <div class="timeline-time">{{ getEndStatusTime(reservation) }}</div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <div v-else-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-else class="error-container">
      <div class="error-icon">😕</div>
      <div class="error-title">加载失败</div>
      <button class="retry-btn" @click="loadReservationDetail">重试</button>
    </div>
    <div v-if="showCancelConfirm" class="modal-overlay" @click="showCancelConfirm = false">
      <div class="modal-content confirm-modal" @click.stop>
        <div class="modal-icon">⚠️</div>
        <h3>取消预约</h3>
        <p>确定要取消预约「{{ reservation.roomName }}」吗？</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showCancelConfirm = false">再想想</button>
          <button class="modal-btn confirm" @click="confirmCancel">确定取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script 逻辑完全不需要变动，直接复用之前的即可
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReservationDetail, cancelReservation } from '@/api/reservations.js'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const showCancelConfirm = ref(false)
const reservation = ref({})

const activityEndDateTime = computed(() => {
  if (!reservation.value.reservationDate || !reservation.value.endTime) return null
  const date = new Date(reservation.value.reservationDate)
  const [hours, minutes] = reservation.value.endTime.split(':').map(Number)
  date.setHours(hours, minutes, 0, 0)
  return date
})

const getActivityEndDisplay = computed(() => {
  if (reservation.value.status === 4) return reservation.value.updatedAt ? formatDateTime(reservation.value.updatedAt) : ''
  else if (activityEndDateTime.value) return formatDateTimeDisplay(activityEndDateTime.value)
  return ''
})

const loadReservationDetail = async () => {
  try {
    loading.value = true
    const response = await getReservationDetail(route.params.id)
    if (response.code === 200) reservation.value = response.data
    else throw new Error(response.message)
  } catch (error) {
    ElMessage.error('加载预约详情失败')
  } finally {
    loading.value = false
  }
}

const canCancelReservation = (res) => {
  if (res.status !== 1 && res.status !== 0) return false
  const now = new Date()
  const resDate = new Date(res.reservationDate)
  const [h, m] = res.startTime.split(':').map(Number)
  resDate.setHours(h, m, 0, 0)
  return (resDate.getTime() - now.getTime()) > (3 * 60 * 60 * 1000)
}

const getStatusClass = (s) => ({0:'pending',1:'approved',2:'rejected',3:'cancelled',4:'completed',5:'expired'}[s] || 'pending')
const getStatusText = (s) => ({0:'待审核',1:'进行中',2:'已拒绝',3:'已取消',4:'已完成',5:'已过期'}[s] || '未知')
const getStatusDescription = (s) => ({0:'等待管理员审核...',1:'预约成功，请按时使用',2:'预约未生效',3:'预约已被取消',4:'活动已结束',5:'预约已过期'}[s] || '')
const getEndStatusTitle = (s) => s===3?'已取消':s===4?'活动完成':s===5?'已过期':'预计完成'
const getEndStatusTime = (res) => (res.status===4||res.status===3||res.status===5) ? (res.updatedAt?formatDateTime(res.updatedAt):'') : getActivityEndDisplay.value
const formatDate = (s) => s ? `${new Date(s).getFullYear()}年${new Date(s).getMonth()+1}月${new Date(s).getDate()}日` : ''
const formatDateTime = (s) => s ? `${new Date(s).getFullYear()}-${(new Date(s).getMonth()+1).toString().padStart(2,'0')}-${new Date(s).getDate().toString().padStart(2,'0')} ${new Date(s).getHours().toString().padStart(2,'0')}:${new Date(s).getMinutes().toString().padStart(2,'0')}` : ''
const formatTimeOnly = (s) => s ? `${new Date(s).getHours().toString().padStart(2,'0')}:${new Date(s).getMinutes().toString().padStart(2,'0')}` : ''
const formatDateTimeDisplay = (d) => `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
const goBack = () => router.back()
const handleCancel = () => { if(!canCancelReservation(reservation.value)) return ElMessage.warning('距离开始不足3小时无法取消'); showCancelConfirm.value = true }
const confirmCancel = async () => {
  try {
    const res = await cancelReservation(reservation.value.reservationNo)
    if(res.code===200) { ElMessage.success('取消成功'); showCancelConfirm.value=false; loadReservationDetail() }
    else throw new Error(res.message)
  } catch(e) { ElMessage.error('取消失败') }
}

onMounted(loadReservationDetail)
</script>

<style scoped>
/* 保持原有的基础样式 */
.reservation-detail-container { min-height: 100vh; background: #f8fafc; }
.detail-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: white; border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; z-index: 100; }
.back-button { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; cursor: pointer; border-radius: 10px; color: #4a5568; }
.back-button:hover { background: #f7fafc; }
.header-title { font-size: 18px; font-weight: 700; color: #2d3748; }
.action-btn { padding: 8px 16px; border: 1px solid #e53e3e; border-radius: 8px; background: white; color: #e53e3e; font-size: 14px; font-weight: 500; cursor: pointer; }
.detail-content { padding: 20px; max-width: 800px; margin: 0 auto; }

/* 状态卡片样式保持不变 */
.status-card { background: white; border-radius: 16px; padding: 24px; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); display: flex; align-items: center; gap: 16px; position: relative; }
.status-card.approved { border-left: 6px solid #10b981; } .status-card.cancelled { border-left: 6px solid #6b7280; } .status-card.completed { border-left: 6px solid #3b82f6; } .status-card.expired { border-left: 6px solid #9ca3af; } .status-card.pending { border-left: 6px solid #f59e0b; }
.status-icon { font-size: 48px; } .status-info { flex: 1; }
.status-text { font-size: 20px; font-weight: 700; margin-bottom: 4px; } .status-desc { font-size: 14px; color: #718096; }
.checkin-badge-container { margin-top: 8px; } .checkin-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 600; }
.checkin-badge.success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; } .checkin-badge.error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.reservation-no { position: absolute; top: 20px; right: 20px; font-size: 12px; color: #a0aec0; font-family: 'Courier New', monospace; }

/* 🟢 合并后的信息区块样式 */
.info-section { background: white; border-radius: 16px; padding: 24px; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.section-title { font-size: 17px; font-weight: 700; color: #2d3748; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 2px solid #f7fafc; display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 20px; }

.merged-info-container { display: flex; flex-direction: column; gap: 20px; }
.group-title { font-size: 14px; font-weight: 600; color: #94a3b8; margin: 0 0 12px 0; text-transform: uppercase; letter-spacing: 0.5px; }

/* 虚线分割线 */
.info-divider { height: 1px; background-image: linear-gradient(to right, #e2e8f0 50%, rgba(255,255,255,0) 0%); background-position: bottom; background-size: 8px 1px; background-repeat: repeat-x; margin: 4px 0; }

.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

/* 单独占据一行的项 */
.info-item.full-width { grid-column: 1 / -1; }

.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 13px; color: #718096; font-weight: 500; }
.info-value { font-size: 15px; color: #2d3748; font-weight: 600; line-height: 1.5; }

/* 高亮样式 */
.info-value.highlight { font-size: 18px; color: #1e88e5; }
.time-value span { display: block; font-size: 14px; color: #4a5568; margin-top: 2px; }

/* 响应式调整：小屏幕变成单列 */
@media (max-width: 600px) {
  .info-grid { grid-template-columns: 1fr; }
}

/* 其他原有样式 (Timeline, Modal, Loading 等) */
.requirements-list { display: flex; flex-direction: column; gap: 12px; }
.requirement-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: #f7fafc; border-radius: 8px; }
.timeline { position: relative; padding-left: 24px; margin-top: 10px; }
.timeline::before { content: ''; position: absolute; left: 11px; top: 0; bottom: 0; width: 2px; background: #e2e8f0; }
.timeline-item { position: relative; margin-bottom: 24px; } .timeline-item:last-child { margin-bottom: 0; }
.timeline-dot { position: absolute; left: -24px; top: 4px; width: 12px; height: 12px; border-radius: 50%; background: #cbd5e0; border: 2px solid white; z-index: 1; }
.timeline-dot.dot-success { background: #10b981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); }
.timeline-item.active .timeline-dot { background: #1e88e5; box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.2); }
.timeline-title { font-size: 15px; font-weight: 600; color: #2d3748; margin-bottom: 2px; } .timeline-time { font-size: 13px; color: #718096; }
.loading-container, .error-container { display: flex; flex-direction: column; align-items: center; padding: 80px 20px; }
.loading-spinner { width: 40px; height: 40px; border: 3px solid #f3f4f6; border-top: 3px solid #1e88e5; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 16px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 999; }
.confirm-modal { background: white; border-radius: 16px; padding: 24px; width: 85%; max-width: 320px; text-align: center; }
.modal-actions { display: flex; gap: 12px; margin-top: 20px; } .modal-btn { flex: 1; padding: 10px; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; }
.modal-btn.cancel { background: #f1f5f9; color: #64748b; } .modal-btn.confirm { background: #ef4444; color: white; }
</style>
