<template>
  <div class="enhanced-stat-card" :class="[`stat-card--${type}`, { 'stat-card--loading': loading }]">
    <!-- 卡片顶部装饰 -->
    <div class="stat-card__decoration">
      <div class="stat-card__glow"></div>
      <div class="stat-card__dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="stat-card__content">
      <!-- 图标区域 -->
      <div class="stat-card__icon">
        <el-icon :size="32">
          <component :is="iconComponent" />
        </el-icon>
        <div class="stat-card__icon-bg"></div>
      </div>

      <!-- 数据区域 -->
      <div class="stat-card__data">
        <div class="stat-card__value">
          <span v-if="loading" class="loading-placeholder">---</span>
          <span v-else class="value-number">{{ formattedValue }}</span>
          <span v-if="unit" class="value-unit">{{ unit }}</span>
        </div>

        <div class="stat-card__title">{{ title }}</div>

        <div v-if="subtitle" class="stat-card__subtitle">{{ subtitle }}</div>

        <!-- 趋势指示器 -->
        <div v-if="trend !== undefined" class="stat-card__trend" :class="trendClass">
          <el-icon :size="14">
            <ArrowUp v-if="trend > 0" />
            <ArrowDown v-if="trend < 0" />
            <Minus v-if="trend === 0" />
          </el-icon>
          <span class="trend-text">{{ Math.abs(trend) }}%</span>
        </div>
      </div>
    </div>

    <!-- 悬浮效果 -->
    <div class="stat-card__hover-effect"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ArrowUp,
  ArrowDown,
  Minus,
  UserFilled,
  Van,
  Coin,
  Star,
  ShoppingCart,
  TrendCharts,
  PieChart,
  DataBoard,
  Avatar,
  Trophy,
  Medal
} from '@element-plus/icons-vue'

interface Props {
  title: string
  value: number | string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  icon?: string
  unit?: string
  trend?: number
  subtitle?: string
  precision?: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  precision: 0,
  loading: false
})

// 图标组件映射
const iconMap: Record<string, any> = {
  UserFilled,
  Van,
  Coin,
  Star,
  ShoppingCart,
  TrendCharts,
  PieChart,
  DataBoard,
  Avatar,
  Trophy,
  Medal
}

// 获取图标组件
const iconComponent = computed(() => {
  return props.icon ? iconMap[props.icon] || TrendCharts : TrendCharts
})

// 格式化数值
const formattedValue = computed(() => {
  if (props.loading) return '---'

  if (typeof props.value === 'string') return props.value

  return props.value.toLocaleString('zh-CN', {
    minimumFractionDigits: props.precision,
    maximumFractionDigits: props.precision
  })
})

// 趋势样式类
const trendClass = computed(() => {
  if (props.trend === undefined) return ''
  if (props.trend > 0) return 'trend--up'
  if (props.trend < 0) return 'trend--down'
  return 'trend--flat'
})

// 类型对应的颜色配置
const typeColors = {
  primary: {
    primary: '#6366f1',
    secondary: '#8b5cf6',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  },
  success: {
    primary: '#22c55e',
    secondary: '#16a34a',
    gradient: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)'
  },
  warning: {
    primary: '#f59e0b',
    secondary: '#d97706',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
  },
  danger: {
    primary: '#ef4444',
    secondary: '#dc2626',
    gradient: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
  },
  info: {
    primary: '#64748b',
    secondary: '#475569',
    gradient: 'linear-gradient(135deg, #64748b 0%, #475569 100%)'
  }
}
</script>

<style scoped>
.enhanced-stat-card {
  position: relative;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 24px;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 2px 4px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  cursor: pointer;
  min-height: 140px;
}

/* 卡片类型颜色 */
.stat-card--primary {
  border-color: rgba(99, 102, 241, 0.2);
}

.stat-card--success {
  border-color: rgba(34, 197, 94, 0.2);
}

.stat-card--warning {
  border-color: rgba(245, 158, 11, 0.2);
}

.stat-card--danger {
  border-color: rgba(239, 68, 68, 0.2);
}

.stat-card--info {
  border-color: rgba(100, 116, 139, 0.2);
}

/* 卡片装饰 */
.stat-card__decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  overflow: hidden;
}

.stat-card--primary .stat-card__decoration {
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
}

.stat-card--success .stat-card__decoration {
  background: linear-gradient(90deg, #22c55e 0%, #16a34a 50%, #84cc16 100%);
}

.stat-card--warning .stat-card__decoration {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 50%, #fbbf24 100%);
}

.stat-card--danger .stat-card__decoration {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 50%, #f87171 100%);
}

.stat-card--info .stat-card__decoration {
  background: linear-gradient(90deg, #64748b 0%, #475569 50%, #94a3b8 100%);
}

/* 发光效果 */
.stat-card__glow {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 20px;
  background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.8) 0%, transparent 70%);
  filter: blur(8px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.enhanced-stat-card:hover .stat-card__glow {
  opacity: 1;
}

/* 装饰点 */
.stat-card__dots {
  position: absolute;
  top: 8px;
  right: 12px;
  display: flex;
  gap: 4px;
}

.stat-card__dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  animation: dotPulse 2s ease-in-out infinite;
}

.stat-card__dots span:nth-child(1) {
  animation-delay: 0s;
}

.stat-card__dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.stat-card__dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.2); }
}

/* 卡片内容 */
.stat-card__content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  height: 100%;
}

/* 图标区域 */
.stat-card__icon {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.stat-card--primary .stat-card__icon {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.stat-card--success .stat-card__icon {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.stat-card--warning .stat-card__icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-card--danger .stat-card__icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.stat-card--info .stat-card__icon {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
}

/* 图标背景动画 */
.stat-card__icon-bg {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 70%);
  animation: iconShine 3s linear infinite;
}

@keyframes iconShine {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 数据区域 */
.stat-card__data {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 92px;
}

/* 数值显示 */
.stat-card__value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #1e293b 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value-number {
  color: inherit;
}

.value-unit {
  font-size: 18px;
  font-weight: 600;
  opacity: 0.7;
}

.loading-placeholder {
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s ease-in-out infinite;
  border-radius: 4px;
  color: transparent;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 标题 */
.stat-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

/* 副标题 */
.stat-card__subtitle {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

/* 趋势指示器 */
.stat-card__trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 12px;
  margin-top: 8px;
  align-self: flex-start;
}

.trend--up {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #166534;
}

.trend--down {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  color: #991b1b;
}

.trend--flat {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #475569;
}

/* 悬浮效果 */
.stat-card__hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 20px;
  pointer-events: none;
}

/* 悬浮状态 */
.enhanced-stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.enhanced-stat-card:hover .stat-card__hover-effect {
  opacity: 1;
}

.enhanced-stat-card:hover .stat-card__icon {
  transform: scale(1.1);
}

.enhanced-stat-card:hover .stat-card__value {
  transform: scale(1.05);
}

/* 加载状态 */
.stat-card--loading {
  pointer-events: none;
}

.stat-card--loading .stat-card__icon {
  opacity: 0.5;
}

.stat-card--loading .stat-card__value {
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .enhanced-stat-card {
    padding: 20px;
    min-height: 120px;
  }

  .stat-card__icon {
    width: 50px;
    height: 50px;
  }

  .stat-card__value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .enhanced-stat-card {
    padding: 16px;
    min-height: 100px;
  }

  .stat-card__content {
    gap: 12px;
  }

  .stat-card__icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }

  .stat-card__value {
    font-size: 24px;
  }

  .stat-card__title {
    font-size: 12px;
  }

  .stat-card__subtitle {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .enhanced-stat-card {
    padding: 12px;
    min-height: 80px;
  }

  .stat-card__content {
    gap: 8px;
  }

  .stat-card__icon {
    width: 36px;
    height: 36px;
  }

  .stat-card__value {
    font-size: 20px;
  }

  .stat-card__title {
    font-size: 11px;
  }

  .stat-card__trend {
    font-size: 10px;
    padding: 2px 6px;
  }
}


/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .enhanced-stat-card,
  .stat-card__icon-bg,
  .stat-card__dots span {
    animation: none !important;
    transition: none !important;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .enhanced-stat-card {
    border: 2px solid #000;
  }

  .stat-card__value {
    color: #000 !important;
    -webkit-text-fill-color: #000 !important;
  }
}
</style>