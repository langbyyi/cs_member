<template>
  <el-card class="real-time-stats" :loading="loading">
    <template #header>
      <div class="real-time-stats__header">
        <div class="real-time-stats__title">
          <el-icon><Timer /></el-icon>
          <span>关键指标</span>
          <div class="real-time-stats__status" :class="statusClass">
            <div class="real-time-stats__pulse" />
            {{ statusText }}
          </div>
        </div>
        <div class="real-time-stats__controls">
          <el-switch
            v-model="autoRefresh"
            inline-prompt
            :active-text="`刷新间隔: ${refreshInterval}s`"
            inactive-text="自动刷新"
            @change="handleAutoRefreshChange"
          />
          <el-button
            link
            size="small"
            @click="handleManualRefresh"
            :loading="loading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </template>

    <div class="real-time-stats__content">
      <div class="real-time-stats__grid">
        <div
          v-for="(item, index) in statsData"
          :key="index"
          class="real-time-stats__item"
          :class="{ 'real-time-stats__item--changed': item.changed }"
        >
          <div class="real-time-stats__icon" :style="{ color: item.color }">
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="real-time-stats__info">
            <div class="real-time-stats__value">
              {{ formatValue(item.value, item.format) }}
              <span v-if="item.unit" class="real-time-stats__unit">{{ item.unit }}</span>
            </div>
            <div class="real-time-stats__label">{{ item.label }}</div>
            <div v-if="item.change !== undefined" class="real-time-stats__change" :class="getChangeClass(item.change)">
              <el-icon size="12">
                <component :is="getChangeIcon(item.change)" />
              </el-icon>
              <span>{{ formatChange(item.change) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-show="showChart && timeSeriesData.length > 0" class="real-time-stats__chart">
        <v-chart
          ref="chartRef"
          class="real-time-stats__chart-canvas"
          :option="chartOption"
          :loading="false"
          :style="{ width: '100%', height: '118px' }"
          autoresize
        />
      </div>

      <div class="real-time-stats__alerts" v-if="alerts.length > 0">
        <div class="real-time-stats__alerts-header">
          <span>系统告警</span>
          <el-badge :value="alerts.length" type="danger" />
        </div>
        <div class="real-time-stats__alerts-list">
          <div
            v-for="(alert, index) in alerts.slice(0, 3)"
            :key="index"
            class="real-time-stats__alert"
            :class="`real-time-stats__alert--${alert.level}`"
          >
            <el-icon>
              <component :is="getAlertIcon(alert.level)" />
            </el-icon>
            <span>{{ alert.message }}</span>
            <span class="real-time-stats__alert-time">{{ formatTime(alert.time) }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick, markRaw } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { getDashboardStats } from '@/api/dashboard'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import {
  Timer,
  Refresh,
  ArrowUp,
  ArrowDown,
  Minus,
  Warning,
  InfoFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent
])

interface RealTimeItem {
  label: string
  value: number
  icon: string
  color: string
  unit?: string
  format?: 'number' | 'currency' | 'percentage'
  change?: number
  changed?: boolean
}

interface Alert {
  level: 'warning' | 'error' | 'info'
  message: string
  time: Date
}

interface TimeSeriesData {
  time: string
  value: number
}

interface Props {
  refreshInterval?: number
  showChart?: boolean
  maxDataPoints?: number
  enableAlerts?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  refreshInterval: 30,
  showChart: true,
  maxDataPoints: 20,
  enableAlerts: true
})

const emit = defineEmits<{
  refresh: [data: any]
  alert: [alert: Alert]
  error: [error: any]
}>()

// ECharts 引用
const chartRef = ref()

// 响应式数据
const loading = ref(false)
const autoRefresh = ref(true)
const lastUpdateTime = ref(new Date())
const connectionStatus = ref<'connected' | 'disconnected' | 'error'>('connected')

// 统计数据
const statsData = ref<RealTimeItem[]>([
  {
    label: '在线会员',
    value: 0,
    icon: 'User',
    color: '#409eff',
    unit: '人'
  },
  {
    label: '今日订单',
    value: 0,
    icon: 'ShoppingCart',
    color: '#67c23a',
    unit: '单'
  },
  {
    label: '今日营收',
    value: 0,
    icon: 'Coin',
    color: '#e6a23c',
    format: 'currency',
    unit: '¥'
  },
  {
    label: '活跃门店',
    value: 0,
    icon: 'Shop',
    color: '#909399',
    unit: '家'
  }
])

// 时间序列数据（用于图表）
const timeSeriesData = ref<TimeSeriesData[]>([])

// 告警信息
const alerts = ref<Alert[]>([])

// 定时器
let refreshTimer: NodeJS.Timeout | null = null

// 状态显示
const statusClass = computed(() => {
  return {
    'real-time-stats__status--connected': connectionStatus.value === 'connected',
    'real-time-stats__status--disconnected': connectionStatus.value === 'disconnected',
    'real-time-stats__status--error': connectionStatus.value === 'error'
  }
})

const statusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected':
      return '实时连接'
    case 'disconnected':
      return '连接断开'
    case 'error':
      return '连接错误'
    default:
      return '未知状态'
  }
})

// 图表配置
const chartOption = computed(() => {
  if (!timeSeriesData.value.length) {
    return {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 12
        }
      }
    }
  }

  return {
    grid: {
      top: 10,
      left: 10,
      right: 10,
      bottom: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: timeSeriesData.value.map(item => item.time),
      axisLabel: {
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 10
      }
    },
    series: [
      {
        type: 'line',
        data: timeSeriesData.value.map(item => item.value),
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: {
          color: '#409eff',
          width: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        }
      }
    ],
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.name}<br/>订单数: ${data.value}`
      }
    }
  }
})

// 格式化数值
const formatValue = (value: number, format?: string): string => {
  if (format === 'currency') {
    return value.toLocaleString('zh-CN')
  } else if (format === 'percentage') {
    return `${value.toFixed(1)}%`
  } else {
    return value.toLocaleString()
  }
}

// 格式化变化值
const formatChange = (change: number): string => {
  const prefix = change > 0 ? '+' : ''
  return `${prefix}${change}`
}

// 获取变化样式类
const getChangeClass = (change: number): string => {
  if (change > 0) return 'real-time-stats__change--up'
  if (change < 0) return 'real-time-stats__change--down'
  return 'real-time-stats__change--neutral'
}

// 获取变化图标
const getChangeIcon = (change: number) => {
  if (change > 0) return ArrowUp
  if (change < 0) return ArrowDown
  return Minus
}

// 获取告警图标
const getAlertIcon = (level: string) => {
  switch (level) {
    case 'error':
      return CircleCloseFilled
    case 'warning':
      return Warning
    default:
      return InfoFilled
  }
}

// 格式化时间
const formatTime = (time: Date): string => {
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / (1000 * 60))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`

  return `${Math.floor(hours / 24)}天前`
}

// 获取实时数据
const fetchRealTimeData = async () => {
  try {
    loading.value = true

    // 调用仪表盘统计数据API（使用已存在的接口）
    const response = await getDashboardStats()
    const dashboardData = response.data || {}

    // 更新统计数据，记录变化
    const prevData = [...statsData.value]

    // 使用仪表盘数据映射到实时统计
    // 在线会员数：使用活跃会员数作为近似值
    const newOnlineMembers = dashboardData.activeMembers || 0
    statsData.value[0].value = newOnlineMembers
    statsData.value[0].change = newOnlineMembers - prevData[0].value
    statsData.value[0].changed = statsData.value[0].change !== 0

    // 今日订单：直接使用
    const newTodayOrders = dashboardData.todayOrders || 0
    statsData.value[1].value = newTodayOrders
    statsData.value[1].change = newTodayOrders - prevData[1].value
    statsData.value[1].changed = statsData.value[1].change !== 0

    // 今日营收：根据角色权限处理
    const newTodayRevenue = dashboardData.todayRevenue || 0
    statsData.value[2].value = newTodayRevenue
    statsData.value[2].change = newTodayRevenue - prevData[2].value
    statsData.value[2].changed = statsData.value[2].change !== 0

    // 活跃门店：管理员显示总数，其他角色显示1（当前门店）
    const newActiveStores = dashboardData.totalStores || 1
    statsData.value[3].value = newActiveStores
    statsData.value[3].change = newActiveStores - prevData[3].value
    statsData.value[3].changed = statsData.value[3].change !== 0

    // 更新时间序列数据
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    timeSeriesData.value.push({
      time: timeStr,
      value: newTodayOrders
    })

    // 限制数据点数量
    if (timeSeriesData.value.length > props.maxDataPoints) {
      timeSeriesData.value.shift()
    }

    // 检查告警条件
    if (props.enableAlerts) {
      checkAlerts(dashboardData)
    }

    connectionStatus.value = 'connected'
    lastUpdateTime.value = now

    emit('refresh', dashboardData)

    // 清除变化标记
    setTimeout(() => {
      statsData.value.forEach(item => {
        item.changed = false
      })
    }, 2000)

  } catch (error) {
    // API失败时，将所有数据重置为0，避免误导性显示
    statsData.value.forEach((item, index) => {
      const prevValue = item.value
      item.value = 0
      item.change = 0 - prevValue
      item.changed = prevValue !== 0
    })

    // 清空时间序列数据，显示加载失败状态
    timeSeriesData.value = []

    connectionStatus.value = 'error'
    lastUpdateTime.value = new Date()

    // 发送错误事件
    emit('error', error)
  } finally {
    loading.value = false
  }
}

// 检查告警
const checkAlerts = (data: any) => {
  const newAlerts: Alert[] = []

  // 检查各种告警条件
  if (data.pendingOrders > 5) {
    newAlerts.push({
      level: 'warning',
      message: `待处理订单过多: ${data.pendingOrders} 单`,
      time: new Date()
    })
  }

  if (data.lowStockProducts > 0) {
    newAlerts.push({
      level: 'warning',
      message: `库存不足商品: ${data.lowStockProducts} 种`,
      time: new Date()
    })
  }

  if (data.currentHourOrders < 10) {
    newAlerts.push({
      level: 'info',
      message: '当前时段订单量较低',
      time: new Date()
    })
  }

  // 添加新告警（保持最新的5条）
  if (newAlerts.length > 0) {
    alerts.value = [...newAlerts, ...alerts.value].slice(0, 5)
    newAlerts.forEach(alert => emit('alert', alert))
  }
}

// 处理自动刷新变化
const handleAutoRefreshChange = (enabled: boolean) => {
  if (enabled) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 手动刷新
const handleManualRefresh = () => {
  fetchRealTimeData()
}

// 开始自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh()
  fetchRealTimeData() // 立即获取一次数据
  refreshTimer = setInterval(fetchRealTimeData, props.refreshInterval * 1000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 监听刷新间隔变化
watch(() => props.refreshInterval, (newInterval) => {
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

// 监听时间序列数据变化，手动触发图表重绘
watch(() => timeSeriesData.value.length, (newLength) => {
  if (newLength > 0) {
    nextTick(() => {
      if (chartRef.value) {
        setTimeout(() => {
          chartRef.value.resize()
        }, 100)
      }
    })
  }
})

// 设置ResizeObserver来确保图表尺寸正确
let resizeObserver: ResizeObserver | null = null

const setupChartResize = () => {
  nextTick(() => {
    if (chartRef.value && chartRef.value.$el) {
      // 清理旧的观察器
      if (resizeObserver) {
        resizeObserver.disconnect()
      }

      // 创建新的观察器
      resizeObserver = new ResizeObserver(() => {
        if (chartRef.value) {
          chartRef.value.resize()
        }
      })

      resizeObserver.observe(chartRef.value.$el)
    }
  })
}

onMounted(() => {
  // 延迟启动以确保DOM完全渲染和CSS应用完成
  nextTick(() => {
    setTimeout(() => {
      //设置图表resize监听
      setupChartResize()
      
      // 延迟启动数据获取，确保DOM完全准备好
      setTimeout(() => {
        if (autoRefresh.value) {
          startAutoRefresh()
        }
      }, 800) // 使用更长的延迟确保万无一失
    }, 200)
  })
})

onUnmounted(() => {
  // 清理ResizeObserver
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  // 停止自动刷新
  stopAutoRefresh()
})
</script>

<style scoped>
.real-time-stats {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.real-time-stats__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.real-time-stats__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.real-time-stats__status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.real-time-stats__status--connected {
  background: #f0f9ff;
  color: #1890ff;
}

.real-time-stats__status--disconnected {
  background: #fff7e6;
  color: #fa8c16;
}

.real-time-stats__status--error {
  background: #fff1f0;
  color: #f5222d;
}

.real-time-stats__pulse {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.real-time-stats__controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.real-time-stats__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.real-time-stats__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.real-time-stats__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.real-time-stats__item--changed {
  background: #e6f7ff;
  animation: highlight 2s ease;
}

@keyframes highlight {
  0% {
    background: #bae7ff;
    transform: scale(1.02);
  }
  100% {
    background: #e6f7ff;
    transform: scale(1);
  }
}

.real-time-stats__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  flex-shrink: 0;
}

.real-time-stats__info {
  flex: 1;
  min-width: 0;
}

.real-time-stats__value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.real-time-stats__unit {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.7;
}

.real-time-stats__label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.real-time-stats__change {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 500;
}

.real-time-stats__change--up {
  color: #67c23a;
}

.real-time-stats__change--down {
  color: #f56c6c;
}

.real-time-stats__change--neutral {
  color: #909399;
}

.real-time-stats__chart {
  height: 150px;
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  box-sizing: border-box;
}

.real-time-stats__chart-canvas {
  height: 118px;
  width: 100%;
  min-height: 118px;
  display: block;
}

.real-time-stats__alerts {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.real-time-stats__alerts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.real-time-stats__alerts-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.real-time-stats__alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
}

.real-time-stats__alert--warning {
  background: #fff7e6;
  color: #fa8c16;
}

.real-time-stats__alert--error {
  background: #fff1f0;
  color: #f5222d;
}

.real-time-stats__alert--info {
  background: #e6f7ff;
  color: #1890ff;
}

.real-time-stats__alert-time {
  margin-left: auto;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .real-time-stats__header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .real-time-stats__controls {
    width: 100%;
    justify-content: space-between;
  }

  .real-time-stats__grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .real-time-stats__item {
    padding: 12px;
  }

  .real-time-stats__value {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .real-time-stats__grid {
    grid-template-columns: 1fr;
  }

  .real-time-stats__controls {
    flex-direction: column;
    gap: 12px;
  }
}
</style>