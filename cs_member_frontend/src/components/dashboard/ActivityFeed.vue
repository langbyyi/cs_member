<template>
  <el-card class="activity-feed" :loading="loading">
    <template #header>
      <div class="activity-feed__header">
        <h3 class="activity-feed__title">
          <el-icon><Bell /></el-icon>
          最新动态
        </h3>
        <div class="activity-feed__controls">
          <el-select
            v-model="selectedType"
            size="small"
            placeholder="活动类型"
            clearable
            @change="handleTypeChange"
          >
            <el-option label="全部" value="" />
            <el-option label="会员" value="member" />
            <el-option label="消费" value="consumption" />
            <el-option label="积分" value="points" />
            <el-option label="优惠券" value="coupon" />
          </el-select>
          <el-button
            link
            size="small"
            @click="handleRefresh"
            :loading="loading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </template>

    <div class="activity-feed__content">
      <div v-if="filteredActivities.length === 0" class="activity-feed__empty">
        <el-empty
          description="暂无活动记录"
          :image-size="80"
        />
      </div>

      <div v-else class="activity-feed__list">
        <TransitionGroup name="activity" tag="div">
          <div
            v-for="activity in paginatedActivities"
            :key="activity.id"
            class="activity-feed__item"
            :class="[`activity-feed__item--${activity.type}`]"
            @click="handleActivityClick(activity)"
          >
            <div class="activity-feed__icon">
              <el-icon :size="20">
                <component :is="getActivityIcon(activity.type)" />
              </el-icon>
            </div>

            <div class="activity-feed__body">
              <div class="activity-feed__main">
                <div class="activity-feed__description">
                  {{ activity.description }}
                </div>
                <div class="activity-feed__meta">
                  <span class="activity-feed__time">
                    {{ formatRelativeTime(activity.time) }}
                  </span>
                  <span v-if="activity.storeName" class="activity-feed__store">
                    {{ activity.storeName }}
                  </span>
                  <span v-if="activity.amount" class="activity-feed__amount">
                    ¥{{ activity.amount.toLocaleString() }}
                  </span>
                </div>
              </div>

              <div v-if="activity.userName" class="activity-feed__user">
                <el-avatar :size="24" class="activity-feed__avatar">
                  {{ getUserInitial(activity.userName) }}
                </el-avatar>
                <span>{{ activity.userName }}</span>
              </div>
            </div>

            <div class="activity-feed__actions">
              <el-dropdown trigger="click" @command="handleAction">
                <el-button text :icon="MoreFilled" size="small" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'view', activity }">
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'related', activity }">
                      相关记录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </TransitionGroup>
      </div>

      <div v-if="totalCount > pageSize" class="activity-feed__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalCount"
          :small="true"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Refresh, MoreFilled, User, ShoppingCart, Medal, Ticket, InfoFilled } from '@element-plus/icons-vue'
import type { RecentActivity } from '@/types/dashboard'

interface Props {
  activities?: RecentActivity[]
  loading?: boolean
  pageSize?: number
  showPagination?: boolean
  autoRefresh?: boolean
  refreshInterval?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  pageSize: 20,
  showPagination: true,
  autoRefresh: false,
  refreshInterval: 30000 // 30秒
})

const emit = defineEmits<{
  refresh: []
  activityClick: [activity: RecentActivity]
  action: [action: { action: string; activity: RecentActivity }]
}>()

// 响应式数据
const selectedType = ref('')
const currentPage = ref(1)
const localActivities = ref<RecentActivity[]>(props.activities || [])

// 过滤后的活动
const filteredActivities = computed(() => {
  if (!selectedType.value) {
    return localActivities.value
  }
  return localActivities.value.filter(activity => activity.type === selectedType.value)
})

// 分页后的活动
const paginatedActivities = computed(() => {
  const start = (currentPage.value - 1) * props.pageSize
  const end = start + props.pageSize
  return filteredActivities.value.slice(start, end)
})

// 总数
const totalCount = computed(() => filteredActivities.value.length)

// 获取活动图标
const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    member: User,
    consumption: ShoppingCart,
    points: Medal,
    coupon: Ticket
  }
  return iconMap[type] || InfoFilled
}

// 获取用户姓名首字母
const getUserInitial = (name: string): string => {
  return name.charAt(0).toUpperCase()
}

// 格式化相对时间
const formatRelativeTime = (timeStr: string): string => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - time.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return time.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理类型变化
const handleTypeChange = () => {
  currentPage.value = 1
}

// 处理页面变化
const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 处理刷新
const handleRefresh = () => {
  emit('refresh')
}

// 处理活动点击
const handleActivityClick = (activity: RecentActivity) => {
  emit('activityClick', activity)
}

// 处理操作
const handleAction = (command: { action: string; activity: RecentActivity }) => {
  emit('action', command)

  switch (command.action) {
    case 'view':
      ElMessage.info(`查看活动详情: ${command.activity.description}`)
      break
    case 'related':
      ElMessage.info(`查看相关记录: ${command.activity.description}`)
      break
  }
}

// 监听外部传入的活动数据变化
watch(() => props.activities, (newActivities) => {
  if (newActivities) {
    localActivities.value = [...newActivities]
    currentPage.value = 1
  }
}, { deep: true })

// 自动刷新
let refreshTimer: NodeJS.Timeout | null = null

const startAutoRefresh = () => {
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(() => {
      handleRefresh()
    }, props.refreshInterval)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  if (props.activities) {
    localActivities.value = [...props.activities]
  }
  startAutoRefresh()
})

// 组件卸载时清理定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.activity-feed {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.activity-feed__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-feed__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.activity-feed__controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-feed__content {
  min-height: 200px;
  position: relative;
}

.activity-feed__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.activity-feed__list {
  display: flex;
  flex-direction: column;
}

.activity-feed__item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.activity-feed__item:hover {
  background: #f8f9fa;
  border-color: #e4e7ed;
  transform: translateX(2px);
}

/* 不同类型的活动样式 */
.activity-feed__item--member {
  border-left: 3px solid #409eff;
}

.activity-feed__item--consumption {
  border-left: 3px solid #67c23a;
}

.activity-feed__item--points {
  border-left: 3px solid #e6a23c;
}

.activity-feed__item--coupon {
  border-left: 3px solid #f56c6c;
}

.activity-feed__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  color: #606266;
  flex-shrink: 0;
}

.activity-feed__item--member .activity-feed__icon {
  background: #e6f7ff;
  color: #1890ff;
}

.activity-feed__item--consumption .activity-feed__icon {
  background: #f6ffed;
  color: #52c41a;
}

.activity-feed__item--points .activity-feed__icon {
  background: #fff7e6;
  color: #fa8c16;
}

.activity-feed__item--coupon .activity-feed__icon {
  background: #fff1f0;
  color: #f5222d;
}

.activity-feed__body {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.activity-feed__main {
  flex: 1;
  min-width: 0;
}

.activity-feed__description {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 4px;
  word-break: break-word;
}

.activity-feed__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.activity-feed__time {
  color: #909399;
}

.activity-feed__store {
  color: #606266;
  font-weight: 500;
}

.activity-feed__amount {
  color: #67c23a;
  font-weight: 600;
}

.activity-feed__user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  flex-shrink: 0;
}

.activity-feed__avatar {
  background: #f5f7fa;
  color: #606266;
  font-size: 12px;
  font-weight: 500;
}

.activity-feed__actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.activity-feed__item:hover .activity-feed__actions {
  opacity: 1;
}

.activity-feed__pagination {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* 动画效果 */
.activity-enter-active,
.activity-leave-active {
  transition: all 0.3s ease;
}

.activity-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.activity-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.activity-move {
  transition: transform 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-feed__header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .activity-feed__controls {
    width: 100%;
    justify-content: space-between;
  }

  .activity-feed__item {
    padding: 12px;
    gap: 10px;
  }

  .activity-feed__icon {
    width: 36px;
    height: 36px;
  }

  .activity-feed__body {
    flex-direction: column;
    gap: 8px;
  }

  .activity-feed__user {
    align-self: flex-start;
  }

  .activity-feed__meta {
    gap: 6px;
  }
}

@media (max-width: 480px) {
  .activity-feed__controls {
    flex-direction: column;
    gap: 8px;
  }

  .activity-feed__item {
    padding: 10px;
  }

  .activity-feed__description {
    font-size: 13px;
  }

  .activity-feed__meta {
    font-size: 11px;
  }
}
</style>