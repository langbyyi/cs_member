<template>
  <div class="member-grade-badge">
    <!-- 等级徽章 -->
    <div
      v-if="gradeInfo"
      class="grade-badge"
      :class="getGradeClass(gradeInfo.gradeCode)"
      :style="{ backgroundColor: displayColor }"
    >
      <span class="grade-name">{{ displayGradeName }}</span>
    </div>

    <!-- 等级进度条（可选） -->
    <div v-if="showProgress && gradeInfo" class="grade-progress">
      <div class="progress-info">
        <span class="current-grade">{{ displayGradeName }}</span>
        <span class="points">{{ currentPoints }}积分</span>
      </div>

      <div v-if="nextGrade" class="progress-bar">
        <div
          class="progress-fill"
          :style="{
            width: `${progressPercentage}%`,
            backgroundColor: nextGrade.color
          }"
        ></div>
      </div>

      <div v-if="nextGrade" class="progress-text">
        距离{{ nextGrade.gradeName }}还需{{ pointsToNextLevel }}积分
      </div>
      <div v-else class="progress-text">
        已达到最高等级！
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { MemberGradeUtil, getMemberGrade, getGradeClass } from '@/utils/memberGrade'
import type { MemberGradeConfig, MemberGradeInfo } from '@/types'

interface Props {
  memberPoints?: number // 会员累计积分（可选）
  gradeInfo?: MemberGradeInfo // 等级信息（直接传入）
  gradeConfigs?: MemberGradeConfig[] // 等级配置表（从后端传入）
  showProgress?: boolean // 是否显示进度条
  autoLoad?: boolean // 是否自动加载等级配置
}

const props = withDefaults(defineProps<Props>(), {
  showProgress: true,
  autoLoad: true
})

// 等级信息
const gradeInfo = ref<MemberGradeConfig | MemberGradeInfo | null>(null)
const nextGrade = ref<MemberGradeConfig | null>(null)
const loading = ref(false)

// 计算属性
const currentPoints = computed(() => {
  return props.memberPoints || props.gradeInfo?.totalPoints || 0
})

const progressPercentage = computed(() => {
  if (!gradeInfo.value || !nextGrade.value) return 0

  // 处理两种不同的数据结构
  const currentGrade = 'gradeCode' in gradeInfo.value ?
    MemberGradeUtil.convertToMemberGradeConfig(gradeInfo.value) :
    gradeInfo.value as MemberGradeConfig

  return MemberGradeUtil.getGradeProgress(currentPoints.value, currentGrade, nextGrade.value)
})

const pointsToNextLevel = computed(() => {
  if (!gradeInfo.value || !nextGrade.value) return 0

  // 处理两种不同的数据结构
  const currentGrade = 'gradeCode' in gradeInfo.value ?
    MemberGradeUtil.convertToMemberGradeConfig(gradeInfo.value) :
    gradeInfo.value as MemberGradeConfig

  return MemberGradeUtil.getPointsToNextLevel(currentPoints.value, currentGrade, nextGrade.value)
})

const displayGradeName = computed(() => {
  return gradeInfo.value?.gradeName || ''
})

const displayColor = computed(() => {
  return gradeInfo.value?.color || '#C0C0C0'
})

// 方法
const loadGradeInfo = async () => {
  if (loading.value) return

  loading.value = true
  try {
    let grade = null

    // 优先级：gradeConfigs > gradeInfo > 自动获取
    if (props.gradeConfigs && props.gradeConfigs.length > 0) {
      // 使用传入的等级配置表计算等级
      grade = MemberGradeUtil.calculateGradeFromConfigs(currentPoints.value, props.gradeConfigs)

      // 设置下一等级
      const currentIndex = props.gradeConfigs.findIndex(g => g.id === grade?.id)
      nextGrade.value = currentIndex < props.gradeConfigs.length - 1 ? props.gradeConfigs[currentIndex + 1] : null
    } else if (props.gradeInfo) {
      // 直接使用传入的等级信息
      grade = MemberGradeUtil.convertToMemberGradeConfig(props.gradeInfo)
    } else {
      // 自动获取等级配置并计算
      grade = await getMemberGrade(currentPoints.value)

      if (grade) {
        const allGrades = await MemberGradeUtil.getGradeConfigs()
        const currentIndex = allGrades.findIndex(g => g.id === grade.id)
        nextGrade.value = currentIndex < allGrades.length - 1 ? allGrades[currentIndex + 1] : null
      }
    }

    gradeInfo.value = grade
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const getGradeClass = (gradeCode: string) => {
  return MemberGradeUtil.getGradeClass(gradeCode)
}

// 监听积分变化
watch(() => currentPoints.value, () => {
  if (!props.gradeInfo) {
    loadGradeInfo()
  }
})

// 组件挂载时处理等级信息
onMounted(() => {
  if (props.gradeConfigs && props.gradeConfigs.length > 0) {
    // 直接使用传入的等级配置表计算等级
    const grade = MemberGradeUtil.calculateGradeFromConfigs(currentPoints.value, props.gradeConfigs)
    gradeInfo.value = grade

    // 设置下一等级
    const currentIndex = props.gradeConfigs.findIndex(g => g.id === grade?.id)
    nextGrade.value = currentIndex < props.gradeConfigs.length - 1 ? props.gradeConfigs[currentIndex + 1] : null
  } else if (props.gradeInfo) {
    // 直接使用传入的等级信息
    gradeInfo.value = props.gradeInfo
  } else if (props.autoLoad) {
    // 预加载等级配置并计算等级
    MemberGradeUtil.preloadGradeConfigs()
    loadGradeInfo()
  }
})

// 暴露方法给父组件
defineExpose({
  loadGradeInfo,
  gradeInfo,
  nextGrade,
  loading
})
</script>

<style scoped>
.member-grade-badge {
  display: inline-block;
}

.grade-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.grade-badge:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.grade-name {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.grade-progress {
  margin-top: 8px;
  min-width: 200px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
}

.current-grade {
  font-weight: bold;
  color: #333;
}

.points {
  color: #666;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: #666;
  text-align: center;
}

/* 不同等级的特殊样式 */
.grade-vip1 {
  background: linear-gradient(45deg, #C0C0C0, #B8B8B8);
}

.grade-vip2 {
  background: linear-gradient(45deg, #87CEEB, #6BB6D6);
}

.grade-vip3 {
  background: linear-gradient(45deg, #FFD700, #FFC700);
}

.grade-vip4 {
  background: linear-gradient(45deg, #FF8C00, #FF7700);
}

.grade-vip5 {
  background: linear-gradient(45deg, #FF6347, #FF5733);
}

.grade-vip6 {
  background: linear-gradient(45deg, #9370DB, #8A2BE2);
}

.grade-vip7 {
  background: linear-gradient(45deg, #FF1493, #FF69B4);
}
</style>