<template>
  <div class="trend-chart">
    <div class="trend-chart__header">
      <h3 class="trend-chart__title">{{ title }}</h3>
      <div class="trend-chart__controls">
        <el-radio-group v-model="selectedPeriod" size="small" @change="handlePeriodChange">
          <el-radio-button
            v-for="period in periods"
            :key="period.value"
            :value="period.value"
          >
            {{ period.label }}
          </el-radio-button>
        </el-radio-group>
        <el-button
          v-if="showExport"
          link
          size="small"
          @click="handleExport"
        >
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <div class="trend-chart__content" v-loading="loading">
      <v-chart
        ref="chartRef"
        class="trend-chart__chart"
        :option="chartOption"
        :loading="loading"
        :loading-options="loadingOptions"
        @click="handleChartClick"
      />
    </div>

    <div v-if="showSummary && summaryData" class="trend-chart__summary">
      <div class="trend-chart__summary-grid">
        <div
          v-for="(item, index) in summaryData"
          :key="index"
          class="trend-chart__summary-item"
        >
          <div class="trend-chart__summary-label">{{ item.label }}</div>
          <div class="trend-chart__summary-value">{{ item.value }}</div>
          <div
            v-if="item.trend"
            class="trend-chart__summary-trend"
            :class="getTrendClass(item.trend)"
          >
            <el-icon size="12">
              <component :is="getTrendIcon(item.trend)" />
            </el-icon>
            <span>{{ formatTrend(item.trend) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkPointComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { Download, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  MarkLineComponent,
  MarkPointComponent
])

interface PeriodOption {
  label: string
  value: string
}

interface ChartSeries {
  name: string
  type: 'line' | 'bar'
  data: number[]
  color?: string
  smooth?: boolean
  areaStyle?: boolean
  yAxisIndex?: number
}

interface SummaryItem {
  label: string
  value: string
  trend?: number
}

interface Props {
  title: string
  data: {
    dates: string[]
    series: ChartSeries[]
  }
  periods?: PeriodOption[]
  defaultPeriod?: string
  showSummary?: boolean
  showExport?: boolean
  loading?: boolean
  height?: number
  chartType?: 'line' | 'bar' | 'mixed'
  showZoom?: boolean
  showMarkLine?: boolean
  format?: 'number' | 'currency' | 'percentage'
  precision?: number
}

const props = withDefaults(defineProps<Props>(), {
  periods: () => [
    { label: '7天', value: '7d' },
    { label: '30天', value: '30d' },
    { label: '90天', value: '90d' }
  ],
  defaultPeriod: '7d',
  showSummary: true,
  showExport: false,
  loading: false,
  height: 300,
  chartType: 'line',
  showZoom: true,
  showMarkLine: false,
  format: 'number',
  precision: 0
})

const emit = defineEmits<{
  periodChange: [period: string]
  chartClick: [params: any]
  export: [period: string]
}>()

const chartRef = ref()
const selectedPeriod = ref(props.defaultPeriod)

// 图表加载配置
const loadingOptions = {
  text: '加载中...',
  color: '#409eff',
  textColor: '#000',
  maskColor: 'rgba(255, 255, 255, 0.8)',
  zlevel: 0
}

// 摘要数据
const summaryData = computed(() => {
  if (!props.data.series.length) return null

  const summary: SummaryItem[] = []
  const currentSeries = props.data.series[0]

  if (currentSeries.data.length > 0) {
    // 当前值（最新数据）
    const currentValue = currentSeries.data[currentSeries.data.length - 1]
    const previousValue = currentSeries.data[currentSeries.data.length - 2] || 0
    const trend = currentValue - previousValue

    summary.push({
      label: '当前值',
      value: formatValue(currentValue),
      trend: previousValue ? (trend / previousValue) * 100 : 0
    })

    // 平均值
    const avgValue = currentSeries.data.reduce((sum, val) => sum + val, 0) / currentSeries.data.length
    summary.push({
      label: '平均值',
      value: formatValue(avgValue)
    })

    // 最大值
    const maxValue = Math.max(...currentSeries.data)
    summary.push({
      label: '最大值',
      value: formatValue(maxValue)
    })

    // 最小值
    const minValue = Math.min(...currentSeries.data)
    summary.push({
      label: '最小值',
      value: formatValue(minValue)
    })
  }

  return summary
})

// 格式化数值
const formatValue = (value: number): string => {
  if (props.format === 'currency') {
    return `¥${value.toLocaleString('zh-CN', { minimumFractionDigits: props.precision })}`
  } else if (props.format === 'percentage') {
    return `${value.toFixed(props.precision)}%`
  } else {
    return value.toLocaleString('zh-CN', { minimumFractionDigits: props.precision })
  }
}

// 格式化趋势
const formatTrend = (trend: number): string => {
  const sign = trend > 0 ? '+' : ''
  return `${sign}${trend.toFixed(1)}%`
}

// 获取趋势样式类
const getTrendClass = (trend: number): string => {
  if (trend > 0) return 'trend-up'
  if (trend < 0) return 'trend-down'
  return 'trend-neutral'
}

// 获取趋势图标 (使用markRaw避免响应式包装)
const getTrendIcon = (trend: number) => {
  if (trend > 0) return markRaw(ArrowUp)
  if (trend < 0) return markRaw(ArrowDown)
  return markRaw(Minus)
}

// 图表配置
const chartOption = computed((): EChartsOption => {
  const series = props.data.series.map((s, index) => {
    const baseSeries: any = {
      name: s.name,
      type: s.type || props.chartType,
      data: s.data,
      smooth: s.smooth !== false,
      itemStyle: {
        color: s.color || getDefaultColor(index)
      }
    }

    // 区域样式
    if (s.areaStyle && s.type === 'line') {
      baseSeries.areaStyle = {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: `${s.color || getDefaultColor(index)}20` },
            { offset: 1, color: `${s.color || getDefaultColor(index)}05` }
          ]
        }
      }
    }

    // 标记线
    if (props.showMarkLine && index === 0) {
      baseSeries.markLine = {
        data: [{ type: 'average', name: '平均值' }],
        label: { formatter: '{b}: {c}' }
      }
    }

    // 标记点
    if (s.data.length > 0) {
      const maxValue = Math.max(...s.data)
      const maxIndex = s.data.indexOf(maxValue)

      baseSeries.markPoint = {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    }

    return baseSeries
  })

  // Y轴配置
  const yAxis = series.map((s, index) => ({
    type: 'value' as const,
    position: (index === 0 ? 'left' : 'right') as 'left' | 'right',
    name: s.name,
    axisLabel: {
      formatter: (value: number) => {
        if (props.format === 'currency') {
          return `¥${value}`
        } else if (props.format === 'percentage') {
          return `${value}%`
        }
        return value.toString()
      }
    }
  }))

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: (params: any) => {
        let result = `${params[0].name}<br/>`
        params.forEach((param: any) => {
          result += `${param.marker} ${param.seriesName}: ${formatValue(param.value)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: series.map(s => s.name),
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: props.showZoom ? '15%' : '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.data.dates,
      boundaryGap: props.chartType === 'bar'
    },
    yAxis: yAxis.length > 1 ? yAxis : yAxis[0],
    series: series
  }

  // 数据缩放
  if (props.showZoom && props.data.dates.length > 30) {
    option.dataZoom = [
      {
        type: 'slider',
        show: true,
        start: Math.max(0, 100 - (30 / props.data.dates.length) * 100),
        end: 100,
        bottom: 10
      }
    ]
  }

  return option
})

// 获取默认颜色
const getDefaultColor = (index: number): string => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb']
  return colors[index % colors.length]
}

// 处理周期变化
const handlePeriodChange = (period: string) => {
  selectedPeriod.value = period
  emit('periodChange', period)
}

// 处理图表点击
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}

// 处理导出
const handleExport = () => {
  try {
    if (chartRef.value) {
      const url = chartRef.value.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      })

      const link = document.createElement('a')
      link.href = url
      link.download = `${props.title}_${selectedPeriod.value}.png`
      link.click()

      ElMessage.success('图表导出成功')
    }
  } catch (error) {
    ElMessage.error('图表导出失败')
  }

  emit('export', selectedPeriod.value)
}

// 监听数据变化，重新渲染图表
watch(() => props.data, () => {
  nextTick(() => {
    if (chartRef.value) {
      chartRef.value.resize()
    }
  })
}, { deep: true })

// 窗口大小变化监听
let resizeObserver: ResizeObserver | null = null

const setupResizeObserver = () => {
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
  // 延迟初始化以确保DOM完全渲染
  nextTick(() => {
    setTimeout(() => {
      // 初始化周期
      if (props.defaultPeriod) {
        selectedPeriod.value = props.defaultPeriod
      }

      // 设置resize监听
      setupResizeObserver()

      // 添加窗口大小变化监听作为备选方案
      window.addEventListener('resize', () => {
        if (chartRef.value) {
          chartRef.value.resize()
        }
      })
    }, 200) // 200ms延迟确保DOM完全渲染
  })
})

onUnmounted(() => {
  // 清理ResizeObserver
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  // 移除事件监听器
  window.removeEventListener('resize', () => {
    if (chartRef.value) {
      chartRef.value.resize()
    }
  })
})
</script>

<style scoped>
.trend-chart {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.trend-chart__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0 20px;
}

.trend-chart__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.trend-chart__controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trend-chart__content {
  padding: 0 20px 20px 20px;
}

.trend-chart__chart {
  height: 300px;
  width: 100%;
  min-height: 200px;
}

.trend-chart__summary {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.trend-chart__summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.trend-chart__summary-item {
  text-align: center;
}

.trend-chart__summary-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.trend-chart__summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.trend-chart__summary-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-neutral {
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .trend-chart__header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .trend-chart__controls {
    width: 100%;
    justify-content: space-between;
  }

  .trend-chart__chart {
    height: 250px;
  }

  .trend-chart__summary-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .trend-chart__summary-grid {
    grid-template-columns: 1fr;
  }

  .trend-chart__controls {
    flex-direction: column;
    gap: 12px;
  }
}
</style>