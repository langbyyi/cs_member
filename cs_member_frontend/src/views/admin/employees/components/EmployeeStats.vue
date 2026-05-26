<template>
  <div class="overview-section">
    <div class="overview-cards">
      <div class="overview-card total">
        <div class="card-icon">
          <el-icon><UserIcon /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ formatNumber(stats.total || 0) }}</div>
          <div class="card-label">总员工数</div>
        </div>
        <div class="card-trend up">
          <el-icon><TrendChartIcon /></el-icon>
          <span>8.5%</span>
        </div>
      </div>

      <div class="overview-card active">
        <div class="card-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ formatNumber(stats.activeEmployees || 0) }}</div>
          <div class="card-label">在职员工</div>
        </div>
        <div class="card-trend up">
          <el-icon><TrendChartIcon /></el-icon>
          <span>5.2%</span>
        </div>
      </div>

      <div class="overview-card new">
        <div class="card-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ formatNumber(stats.newEmployees || 0) }}</div>
          <div class="card-label">本月新增</div>
        </div>
        <div class="card-trend up">
          <el-icon><TrendChartIcon /></el-icon>
          <span>12.3%</span>
        </div>
      </div>

      <div class="overview-card stores">
        <div class="card-icon">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ formatNumber(stats.storeCount || 0) }}</div>
          <div class="card-label">覆盖门店</div>
        </div>
        <div class="card-trend stable">
          <el-icon><Minus /></el-icon>
          <span>0%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  User as UserIcon,
  UserFilled,
  Star,
  OfficeBuilding,
  TrendCharts as TrendChartIcon,
  Minus
} from '@element-plus/icons-vue'

defineProps<{
  stats: {
    total?: number
    activeEmployees?: number
    newEmployees?: number
    storeCount?: number
  }
}>()

const formatNumber = (num: number) => {
  return num.toLocaleString()
}
</script>

<style scoped>
.overview-section {
  margin-bottom: 24px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.overview-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 28px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(255, 255, 255, 0.9);
  animation: cardSlideIn 0.6s ease-out backwards;
}

.overview-card:nth-child(1) { animation-delay: 0.1s; }
.overview-card:nth-child(2) { animation-delay: 0.2s; }
.overview-card:nth-child(3) { animation-delay: 0.3s; }
.overview-card:nth-child(4) { animation-delay: 0.4s; }

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.overview-card::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 20px;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  opacity: 0;
  transition: opacity 0.4s;
  z-index: -1;
}

.overview-card:hover::before {
  opacity: 1;
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

.overview-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-icon::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 20px;
  opacity: 0;
  transition: opacity 0.4s;
  filter: blur(12px);
}

.overview-card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
}

.overview-card:hover .card-icon::after {
  opacity: 0.6;
}

.overview-card.total .card-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.overview-card.total .card-icon::after {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.overview-card.active .card-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.overview-card.active .card-icon::after {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.overview-card.new .card-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.overview-card.new .card-icon::after {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.overview-card.stores .card-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #722ed1;
}

.overview-card.stores .card-icon::after {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #1f2d3d 0%, #3c4858 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
  margin-bottom: 6px;
  animation: countUp 1s ease-out;
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.card-trend {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 12px;
  border-radius: 16px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.card-trend.up {
  color: #f56c6c;
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.05) 100%);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.card-trend.stable {
  color: #909399;
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.15) 0%, rgba(144, 147, 153, 0.05) 100%);
  box-shadow: 0 2px 8px rgba(144, 147, 153, 0.1);
}

.card-trend .el-icon {
  margin-right: 4px;
  animation: bounce 1.5s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

/* Responsive */
@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .card-icon {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }
  
  .card-value {
    font-size: 28px;
  }
}
</style>
