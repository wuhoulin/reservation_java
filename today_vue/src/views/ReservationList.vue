<template>
  <div class="all-reservations">
    <!-- 头部 -->
    <div class="page-header">
      <el-page-header @back="goBack" class="header-back">
        <template #content>
          <span class="header-title">{{ pageTitle }}</span>
        </template>
        <template #extra>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="goToCommunityList">
              去预约
            </el-button>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-card shadow="never" class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">状态筛选：</span>
            <el-select
                v-model="filterStatus"
                placeholder="请选择状态"
                size="small"
                style="width: 150px;"
                @change="handleStatusChange"
            >
              <el-option
                  v-for="option in statusOptions"
                  :key="option.value"
                  :label="option.text"
                  :value="option.value"
              />
            </el-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">时间范围：</span>
            <el-select
                v-model="filterDate"
                placeholder="请选择时间"
                size="small"
                style="width: 150px;"
            >
              <el-option
                  v-for="option in dateOptions"
                  :key="option.value"
                  :label="option.text"
                  :value="option.value"
              />
            </el-select>
          </div>

          <div class="filter-item">
            <el-button
                type="primary"
                size="small"
                @click="handleSearch"
                :loading="loading"
            >
              搜索
            </el-button>
            <el-button
                size="small"
                @click="handleReset"
            >
              重置
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计信息 -->
    <div class="stats-overview">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ filteredReservations.length }}</div>
              <div class="stats-label">当前筛选</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ totalReservations }}</div>
              <div class="stats-label">总记录</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预约列表 -->
    <div class="reservations-container">
      <el-card shadow="never">
        <div v-if="loading" class="loading-state">
          <el-empty description="加载中...">
            <el-icon class="is-loading" :size="32">
              <Loading />
            </el-icon>
          </el-empty>
        </div>

        <div v-else-if="filteredReservations.length === 0" class="empty-state">
          <el-empty :description="emptyDescription" :image-size="200">
            <el-button type="primary" @click="goToCommunityList">
              去预约
            </el-button>
          </el-empty>
        </div>

        <div v-else class="reservations-list">
          <el-scrollbar>
            <div
                v-for="reservation in filteredReservations"
                :key="reservation.id"
                class="reservation-item"
                :class="getStatusClass(reservation.status)"
            >
              <div class="reservation-header">
                <div class="room-info">
                  <h3 class="room-name">{{ reservation.roomName }}</h3>
                  <div class="reservation-meta">
                    <span class="reservation-no">编号: {{ reservation.reservationNo }}</span>
                    <el-tag
                        :type="getStatusTagType(reservation.status)"
                        size="small"
                        class="status-tag"
                    >
                      {{ getStatusText(reservation.status) }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="reservation-content">
                <el-descriptions :column="1" size="small" border>
                  <el-descriptions-item label="预约日期">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(reservation.reservationDate) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="预约时间">
                    <el-icon><Clock /></el-icon>
                    {{ reservation.startTime }} - {{ reservation.endTime }}
                  </el-descriptions-item>
                  <el-descriptions-item label="预约人">
                    <el-icon><User /></el-icon>
                    {{ reservation.userName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="用途">
                    <el-icon><Document /></el-icon>
                    {{ reservation.activityName || '未填写用途' }}
                  </el-descriptions-item>
                  <el-descriptions-item
                      v-if="reservation.status === 2 && reservation.auditReason"
                      label="退回原因"
                  >
                    <el-icon><Warning /></el-icon>
                    <span class="reject-reason">{{ reservation.auditReason }}</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="reservation-actions">
                <el-button-group>
                  <el-button
                      v-if="reservation.status === 0"
                      type="danger"
                      size="small"
                      @click="handleCancelReservation(reservation)"
                  >
                    取消预约
                  </el-button>
                  <el-button
                      v-if="reservation.status === 2"
                      type="primary"
                      size="small"
                      @click="handleResubmitReservation(reservation)"
                  >
                    重新提交
                  </el-button>
                  <el-button
                      type="default"
                      size="small"
                      @click="viewReservationDetail(reservation)"
                  >
                    查看详情
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, User, Document, Warning, Loading, Calendar } from '@element-plus/icons-vue'
import { getUserReservations, cancelUserReservation, resubmitUserReservation } from '@/api/reservations'

const router = useRouter()
const route = useRoute()

// 响应式数据
const reservations = ref([])
const loading = ref(false)
const filterStatus = ref('all')
const filterDate = ref('all')

// 状态映射
const statusMap = {
  0: 'PENDING',      // 待审核
  1: 'CONFIRMED',    // 已通过
  2: 'REJECTED',     // 已拒绝
  3: 'CANCELLED',    // 已取消
  4: 'COMPLETED'     // 已完成
}

// 页面标题映射
const pageTitleMap = {
  'all': '全部预约记录',
  'active': '进行中的预约',
  'completed': '已完成的预约',
  'rejected': '被退回的预约'
}

// 空状态描述映射
const emptyDescriptionMap = {
  'all': '暂无预约记录',
  'active': '暂无进行中的预约',
  'completed': '暂无已完成的预约',
  'rejected': '暂无被退回的预约'
}

// 状态筛选映射
const statusFilterMap = {
  'all': 'all',
  'active': [0, 1],           // 待审核 + 已通过
  'completed': [3, 4],        // 已取消 + 已完成
  'rejected': 2               // 已拒绝
}

// 计算属性
const pageTitle = computed(() => {
  const filter = route.query.filter || 'all'
  return pageTitleMap[filter] || '全部预约记录'
})

const emptyDescription = computed(() => {
  const filter = route.query.filter || 'all'
  return emptyDescriptionMap[filter] || '暂无预约记录'
})

const totalReservations = computed(() => reservations.value.length)

const filteredReservations = computed(() => {
  let filtered = reservations.value

  // 根据路由参数进行初始筛选
  const routeFilter = route.query.filter || 'all'
  const statusFilter = statusFilterMap[routeFilter]

  if (statusFilter !== 'all') {
    if (Array.isArray(statusFilter)) {
      filtered = filtered.filter(item => statusFilter.includes(item.status))
    } else {
      filtered = filtered.filter(item => item.status === statusFilter)
    }
  }

  // 再根据用户选择的筛选条件进行二次筛选
  if (filterStatus.value !== 'all') {
    const selectedStatus = statusFilterMap[filterStatus.value]
    if (selectedStatus !== 'all') {
      if (Array.isArray(selectedStatus)) {
        filtered = filtered.filter(item => selectedStatus.includes(item.status))
      } else {
        filtered = filtered.filter(item => item.status === selectedStatus)
      }
    }
  }

  // 时间筛选
  if (filterDate.value !== 'all') {
    const now = new Date()
    filtered = filtered.filter(item => {
      const itemDate = new Date(item.createdAt)
      switch (filterDate.value) {
        case 'today':
          return itemDate.toDateString() === now.toDateString()
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          return itemDate >= weekAgo
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
          return itemDate >= monthAgo
        default:
          return true
      }
    })
  }

  return filtered
})

// 筛选选项
const statusOptions = [
  { text: '全部状态', value: 'all' },
  { text: '待确认', value: 'active' },
  { text: '已完成', value: 'completed' },
  { text: '被退回', value: 'rejected' }
]

const dateOptions = [
  { text: '全部时间', value: 'all' },
  { text: '今天', value: 'today' },
  { text: '本周', value: 'week' },
  { text: '本月', value: 'month' }
]

// 方法
const loadReservations = async () => {
  try {
    loading.value = true

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
    const openid = userInfo.openid

    if (!openid) {
      ElMessage.error('用户未登录')
      return
    }

    // 调用真实API
    const response = await getUserReservations()

    if (response.code === 200) {
      reservations.value = response.data || []
      ElMessage.success('加载成功')
    } else {
      throw new Error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载预约记录失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  ElMessage.info('状态筛选已更新')
}

const handleSearch = () => {
  ElMessage.info('已应用筛选条件')
}

const handleReset = () => {
  filterStatus.value = 'all'
  filterDate.value = 'all'
  ElMessage.info('已重置筛选条件')
}

const getStatusClass = (status) => {
  const statusClassMap = {
    0: 'pending',    // 待审核
    1: 'confirmed',  // 已通过
    2: 'rejected',   // 已拒绝
    3: 'cancelled',  // 已取消
    4: 'completed'   // 已完成
  }
  return statusClassMap[status] || 'pending'
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',   // 待审核
    1: 'success',   // 已通过
    2: 'danger',    // 已拒绝
    3: 'info',      // 已取消
    4: 'info'       // 已完成
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusTextMap = {
    0: '待确认',
    1: '已确认',
    2: '被退回',
    3: '已取消',
    4: '已完成'
  }
  return statusTextMap[status] || '未知状态'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const handleCancelReservation = async (reservation) => {
  try {
    await ElMessageBox.confirm(
        '确定要取消这个预约吗？',
        '取消预约',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

    // 调用取消预约API
    const response = await cancelUserReservation(reservation.reservationNo, userInfo.openid)

    if (response.code === 200) {
      // 重新加载数据
      await loadReservations()
      ElMessage.success('取消成功')
    } else {
      throw new Error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const handleResubmitReservation = async (reservation) => {
  try {
    await ElMessageBox.confirm(
        '确定要重新提交这个预约吗？',
        '重新提交',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

    // 调用重新提交API
    const response = await resubmitUserReservation(reservation.id, userInfo.openid)

    if (response.code === 200) {
      // 重新加载数据
      await loadReservations()
      ElMessage.success('重新提交成功')
    } else {
      throw new Error(response.message || '重新提交失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重新提交失败')
    }
  }
}

const viewReservationDetail = (reservation) => {
  ElMessage.info(`查看预约详情: ${reservation.reservationNo}`)
  // 这里可以跳转到详情页面
  // router.push(`/reservation-detail/${reservation.id}`)
}

const goBack = () => {
  router.back()
}

const goToCommunityList = () => {
  router.push('/community-list')
}

// 监听路由参数变化
watch(
    () => route.query.filter,
    (newFilter) => {
      if (newFilter) {
        // 根据路由参数更新筛选状态
        filterStatus.value = newFilter
      }
    },
    { immediate: true }
)

// 生命周期
onMounted(() => {
  loadReservations()
})
</script>


<style scoped>
.all-reservations {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 20px;
}

/* 头部样式 */
.page-header {
  margin-bottom: 20px;
}

.header-back {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 16px;
}

.filter-card {
  border: none;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

/* 统计概览 */
.stats-overview {
  margin-bottom: 16px;
}

.stats-card {
  border: none;
  text-align: center;
}

.stats-content {
  padding: 20px;
}

.stats-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

/* 预约列表 */
.reservations-container {
  min-height: 400px;
}

.loading-state,
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 预约项样式 */
.reservations-list {
  max-height: 600px;
}

.reservation-item {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s ease;
  background: white;
}

.reservation-item:last-child {
  border-bottom: none;
}

.reservation-item:hover {
  background: #f5f7fa;
}

.reservation-item.pending {
  border-left: 4px solid #e6a23c;
}

.reservation-item.confirmed {
  border-left: 4px solid #67c23a;
}

.reservation-item.completed {
  border-left: 4px solid #409eff;
}

.reservation-item.cancelled {
  border-left: 4px solid #909399;
  opacity: 0.7;
}

.reservation-item.rejected {
  border-left: 4px solid #f56c6c;
  background: #fef0f0;
}

.reservation-header {
  margin-bottom: 16px;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.room-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.reservation-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.reservation-no {
  font-size: 12px;
  color: #909399;
}

.status-tag {
  margin-left: 8px;
}

.reservation-content {
  margin-bottom: 16px;
}

.reject-reason {
  color: #f56c6c;
  font-weight: 500;
}

.reservation-actions {
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .all-reservations {
    padding: 12px;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    justify-content: space-between;
  }

  .room-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .reservation-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .reservation-actions {
    justify-content: flex-start;
  }

  .reservation-actions .el-button-group {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
