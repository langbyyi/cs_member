<template>
  <el-card
    class="stat-card"
    :class="[`stat-card--${type}`, { 'stat-card--loading': loading, 'stat-card--clickable': clickable }]"
    :loading="loading"
    @click="handleClick"
  >
    <div class="stat-card__content">
      <div class="stat-card__icon" :style="iconStyle">
        <el-icon :size="iconSize">
          <component :is="icon" />
        </el-icon>
      </div>

      <div class="stat-card__info">
        <div class="stat-card__value">
          <span class="stat-card__number">{{ formattedValue }}</span>
          <span v-if="unit" class="stat-card__unit">{{ unit }}</span>
        </div>

        <div class="stat-card__label">
          <span>{{ title }}</span>
          <el-tooltip v-if="tooltip" :content="tooltip" placement="top">
            <el-icon class="stat-card__help-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>

        <div v-if="trend || subtitle" class="stat-card__extra">
          <div v-if="trend" class="stat-card__trend" :class="trendClass">
            <el-icon size="12">
              <component :is="trendIcon" />
            </el-icon>
            <span>{{ formattedTrend }}</span>
          </div>
          <div v-if="subtitle" class="stat-card__subtitle">
            {{ subtitle }}
          </div>
        </div>
      </div>

      <div v-if="showProgress && progress !== undefined" class="stat-card__progress">
        <el-progress
          :percentage="progress"
          :status="progressStatus"
          :show-text="false"
          :stroke-width="4"
        />
      </div>
    </div>

    <div v-if="extraInfo" class="stat-card__extra-info">
      {{ extraInfo }}
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, markRaw } from 'vue'
import {
  ArrowUp,
  ArrowDown,
  Minus,
  InfoFilled
} from '@element-plus/icons-vue'

interface Props {
  title: string
  value: number | string
  type?: 'primary' | 'success' | 'warning' | 'info' | 'danger'
  icon?: string
  unit?: string
  trend?: number
  trendType?: 'percentage' | 'absolute'
  subtitle?: string
  tooltip?: string
  loading?: boolean
  clickable?: boolean
  showProgress?: boolean
  progress?: number
  progressStatus?: 'success' | 'warning' | 'exception'
  extraInfo?: string
  iconSize?: number
  iconColor?: string
  precision?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  trendType: 'percentage',
  loading: false,
  clickable: false,
  showProgress: false,
  iconSize: 24,
  precision: 0
})

const emit = defineEmits<{
  click: [event: Event]
}>()

// 格式化数值
const formattedValue = computed(() => {
  if (typeof props.value === 'string') {
    return props.value
  }

  if (props.value >= 1000000) {
    return `${(props.value / 1000000).toFixed(1)}M`
  } else if (props.value >= 1000) {
    return `${(props.value / 1000).toFixed(1)}K`
  } else {
    return props.value.toFixed(props.precision)
  }
})

// 格式化趋势
const formattedTrend = computed(() => {
  if (props.trend === undefined) return ''

  if (props.trendType === 'percentage') {
    return `${props.trend > 0 ? '+' : ''}${props.trend.toFixed(1)}%`
  } else {
    const prefix = props.trend > 0 ? '+' : ''
    return `${prefix}${props.trend.toFixed(props.precision)}`
  }
})

// 趋势样式类
const trendClass = computed(() => {
  if (!props.trend) return 'stat-card__trend--neutral'
  return props.trend > 0 ? 'stat-card__trend--up' : 'stat-card__trend--down'
})

// 趋势图标 (使用markRaw避免响应式包装)
const trendIcon = computed(() => {
  if (!props.trend) return markRaw(Minus)
  return markRaw(props.trend > 0 ? ArrowUp : ArrowDown)
})

// 图标样式
const iconStyle = computed(() => {
  if (props.iconColor) {
    return { color: props.iconColor }
  }
  return {}
})

// 点击处理
const handleClick = (event: Event) => {
  if (props.clickable) {
    emit('click', event)
  }
}
</script>

<style scoped>
.stat-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: default;
}

.stat-card--clickable {
  cursor: pointer;
}

.stat-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card--loading {
  pointer-events: none;
}

/* 不同类型的卡片背景 */
.stat-card--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card--success {
  background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
  color: white;
}

.stat-card--warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card--info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card--danger {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  color: white;
}

.stat-card__content {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
}

.stat-card__info {
  flex: 1;
  min-width: 0;
}

.stat-card__value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 4px;
}

.stat-card__number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-card__unit {
  font-size: 14px;
  opacity: 0.8;
  font-weight: 500;
}

.stat-card__label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-card__help-icon {
  font-size: 12px;
  opacity: 0.6;
  cursor: help;
  transition: opacity 0.2s;
}

.stat-card__help-icon:hover {
  opacity: 1;
}

.stat-card__extra {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.stat-card__trend--up {
  color: rgba(255, 255, 255, 0.9);
}

.stat-card__trend--down {
  color: rgba(255, 255, 255, 0.7);
}

.stat-card__trend--neutral {
  color: rgba(255, 255, 255, 0.6);
}

.stat-card__subtitle {
  font-size: 12px;
  opacity: 0.8;
}

.stat-card__progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0 16px 8px;
}

.stat-card__progress :deep(.el-progress-bar__outer) {
  background-color: rgba(255, 255, 255, 0.3);
}

.stat-card__extra-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 12px;
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-card__icon {
    width: 40px;
    height: 40px;
  }

  .stat-card__number {
    font-size: 24px;
  }

  .stat-card__label {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .stat-card__content {
    gap: 12px;
  }

  .stat-card__icon {
    width: 36px;
    height: 36px;
  }

  .stat-card__number {
    font-size: 20px;
  }
}
</style>