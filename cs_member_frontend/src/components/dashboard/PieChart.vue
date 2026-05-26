<template>
  <div class="pie-chart">
    <div class="pie-chart__header">
      <h3 class="pie-chart__title">{{ title }}</h3>
      <div class="pie-chart__controls">
        <el-radio-group
          v-if="showChartType"
          v-model="chartType"
          size="small"
          @change="handleChartTypeChange"
        >
          <el-radio-button value="pie">饼图</el-radio-button>
          <el-radio-button value="doughnut">环形图</el-radio-button>
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

    <div class="pie-chart__content" v-loading="loading">
      <v-chart
        ref="chartRef"
        class="pie-chart__chart"
        :option="chartOption"
        :loading="loading"
        :loading-options="loadingOptions"
        :style="{ width: '100%', height: '300px' }"
        autoresize
        @click="handleChartClick"
      />
    </div>

    <div v-if="showLegend && legendData.length > 0" class="pie-chart__legend">
      <div class="pie-chart__legend-grid">
        <div
          v-for="(item, index) in legendData"
          :key="index"
          class="pie-chart__legend-item"
          @click="handleLegendClick(item, index)"
        >
          <div
            class="pie-chart__legend-color"
            :style="{ backgroundColor: item.color }"
          />
          <div class="pie-chart__legend-info">
            <div class="pie-chart__legend-name">{{ item.name }}</div>
            <div class="pie-chart__legend-value">{{ formatValue(item.value) }}</div>
            <div class="pie-chart__legend-percentage">{{ formatPercentage(item.percentage) }}</div>
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
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

interface ChartData {
  name: string
  value: number
  color?: string
}

interface LegendData extends ChartData {
  percentage: number
}

interface Props {
  title: string
  data: ChartData[]
  showLegend?: boolean
  showExport?: boolean
  showChartType?: boolean
  loading?: boolean
  height?: number
  radius?: string | [string, string]
  center?: [string, string]
  format?: 'number' | 'currency' | 'percentage'
  precision?: number
  defaultChartType?: 'pie' | 'doughnut'
  showPercentage?: boolean
  minShowPercentage?: number
}

const props = withDefaults(defineProps<Props>(), {
  showLegend: true,
  showExport: false,
  showChartType: true,
  loading: false,
  height: 300,
  radius: () => ['40%', '70%'],
  center: () => ['50%', '50%'],
  format: 'number',
  precision: 0,
  defaultChartType: 'pie',
  showPercentage: true,
  minShowPercentage: 1
})

const emit = defineEmits<{
  chartClick: [params: any]
  legendClick: [item: LegendData, index: number]
  export: [chartType: string]
}>()

const chartRef = ref()
const chartType = ref(props.defaultChartType)

// 图表加载配置
const loadingOptions = {
  text: '加载中...',
  color: '#409eff',
  textColor: '#000',
  maskColor: 'rgba(255, 255, 255, 0.8)',
  zlevel: 0
}

// 处理数据，计算百分比和颜色
const processedData = computed(() => {
  if (!props.data.length) return []

  const total = props.data.reduce((sum, item) => sum + item.value, 0)
  const defaultColors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
    '#b37feb', '#ff7875', '#ff85c0', '#ffc53d', '#95de64',
    '#69c0ff', '#40a9ff', '#36cfc9', '#73d13d', '#ffc069'
  ]

  return props.data
    .filter((item, index) => {
      const percentage = (item.value / total) * 100
      return percentage >= props.minShowPercentage || index < 8 // 最多显示8个，或大于最小百分比的
    })
    .map((item, index) => ({
      name: item.name,
      value: item.value,
      color: item.color || defaultColors[index % defaultColors.length]
    }))
})

// 图例数据
const legendData = computed((): LegendData[] => {
  if (!processedData.value.length) return []

  const total = processedData.value.reduce((sum, item) => sum + item.value, 0)

  return processedData.value.map(item => ({
    ...item,
    percentage: (item.value / total) * 100
  }))
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

// 格式化百分比
const formatPercentage = (percentage: number): string => {
  return `${percentage.toFixed(1)}%`
}

// 获取半径配置
const getRadius = (): string | [string, string] => {
  if (chartType.value === 'doughnut') {
    return Array.isArray(props.radius) ? props.radius : ['50%', '70%']
  } else {
    return Array.isArray(props.radius) ? ['0%', props.radius[1]] : '70%'
  }
}

// 图表配置
const chartOption = computed((): EChartsOption => {
  if (!processedData.value.length) {
    return {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 14
        }
      }
    }
  }

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const percentage = params.percent
        let result = `${params.name}<br/>`
        result += `${params.marker} 数值: ${formatValue(params.value)}<br/>`
        if (props.showPercentage) {
          result += `占比: ${percentage.toFixed(1)}%`
        }
        return result
      }
    },
    legend: {
      show: false // 使用自定义图例
    },
    series: [
      {
        name: props.title,
        type: 'pie',
        radius: getRadius(),
        center: props.center,
        data: processedData.value.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: item.color
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          },
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        label: {
          show: chartType.value === 'pie' && props.showPercentage,
          position: 'outside',
          formatter: (params: any) => {
            return `${params.name}\n${formatPercentage(params.percent)}`
          }
        },
        labelLine: {
          show: chartType.value === 'pie' && props.showPercentage,
          length: 15,
          length2: 10
        }
      }
    ]
  }
})

// 处理图表类型变化
const handleChartTypeChange = (type: 'pie' | 'doughnut') => {
  chartType.value = type
}

// 处理图表点击
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}

// 处理图例点击
const handleLegendClick = (item: LegendData, index: number) => {
  emit('legendClick', item, index)

  // 切换对应系列的显示状态
  if (chartRef.value) {
    const option = chartRef.value.getOption()
    if (option && option.series && option.series[0]) {
      const series = option.series[0] as any
      series.data[index].itemStyle = {
        ...series.data[index].itemStyle,
        opacity: series.data[index].itemStyle?.opacity === 0.2 ? 1 : 0.2
      }
      chartRef.value.setOption(option)
    }
  }
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
      link.download = `${props.title}_${chartType.value}.png`
      link.click()

      ElMessage.success('图表导出成功')
    }
  } catch (error) {
    ElMessage.error('图表导出失败')
  }

  emit('export', chartType.value)
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
      // 初始化图表类型
      if (props.defaultChartType) {
        chartType.value = props.defaultChartType
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
.pie-chart {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.pie-chart__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0 20px;
}

.pie-chart__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.pie-chart__controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pie-chart__content {
  padding: 0 20px 20px 20px;
}

.pie-chart__chart {
  height: 300px;
  width: 100%;
  min-height: 200px;
}

.pie-chart__legend {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.pie-chart__legend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.pie-chart__legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #f0f0f0;
}

.pie-chart__legend-item:hover {
  background: #f8f9fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pie-chart__legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.pie-chart__legend-info {
  flex: 1;
  min-width: 0;
}

.pie-chart__legend-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pie-chart__legend-value {
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
}

.pie-chart__legend-percentage {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pie-chart__header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .pie-chart__controls {
    width: 100%;
    justify-content: space-between;
  }

  .pie-chart__chart {
    height: 250px;
  }

  .pie-chart__legend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .pie-chart__legend-grid {
    grid-template-columns: 1fr;
  }

  .pie-chart__controls {
    flex-direction: column;
    gap: 12px;
  }
}
</style>