<template>
  <div class="member-profile">
    <!-- 会员基本信息 -->
    <el-card v-if="memberInfo" class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>会员信息</h3>
          <!-- 使用等级徽章组件，传入从后端获取的等级配置表和会员积分 -->
          <MemberGradeBadge
            :grade-configs="gradeConfigs"
            :member-points="memberInfo.totalPoints"
            :show-progress="true"
          />
        </div>
      </template>

      <div class="profile-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="avatar-section">
              <el-avatar
                :size="100"
                :src="memberInfo.avatar"
                :alt="memberInfo.name"
              >
                {{ memberInfo.name?.charAt(0) || '会' }}
              </el-avatar>
              <h3>{{ memberInfo.name || '会员' }}</h3>
              <p class="member-no">{{ memberInfo.memberNo }}</p>
            </div>
          </el-col>

          <el-col :span="16">
            <div class="info-section">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="手机号">
                  {{ memberInfo.phone }}
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  {{ formatDate(memberInfo.createdTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="当前积分">
                  <el-tag type="success">{{ memberInfo.currentPoints || 0 }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="累计积分">
                  <el-tag type="primary">{{ memberInfo.totalPoints || 0 }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="账户余额">
                  <span class="balance">¥{{ memberInfo.balance || 0 }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="消费金额">
                  <span>¥{{ memberInfo.totalConsumption || 0 }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>

        <!-- 等级详细信息 -->
        <div class="grade-details" v-if="currentGradeInfo">
          <h4>等级详情</h4>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="grade-stat">
                <div class="stat-label">等级名称</div>
                <div class="stat-value" :style="{ color: currentGradeInfo.color }">
                  {{ currentGradeInfo.gradeName }}
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="grade-stat">
                <div class="stat-label">积分倍率</div>
                <div class="stat-value">{{ (currentGradeInfo.pointsMultiplier / 100).toFixed(1) }}x</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="grade-stat">
                <div class="stat-label">会员折扣</div>
                <div class="stat-value">{{ currentGradeInfo.discountRate }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="grade-stat">
                <div class="stat-label">累计积分</div>
                <div class="stat-value">{{ memberInfo.totalPoints }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-empty description="加载会员信息失败">
        <el-button type="primary" @click="loadMemberInfo">重新加载</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import MemberGradeBadge from '@/components/MemberGradeBadge.vue'
import { getCurrentMemberInfo } from '@/api/auth'
import { MemberGradeUtil } from '@/utils/memberGrade'
import type { MemberGradeConfig } from '@/types'

// 响应式数据
const memberInfo = ref<any>(null)
const gradeConfigs = ref<any[]>([])
const loading = ref(false)
const error = ref(false)

// 计算属性
const currentGradeInfo = computed(() => {
  if (!memberInfo.value || gradeConfigs.value.length === 0) return null

  // 使用会员积分和等级配置计算等级
  return MemberGradeUtil.calculateGradeFromConfigs(memberInfo.value.totalPoints || 0, gradeConfigs.value)
})

// 方法
const loadMemberInfo = async () => {
  if (loading.value) return

  loading.value = true
  error.value = false

  try {
    // 并行请求会员信息和等级配置
    const [memberResponse, gradeConfigsResponse] = await Promise.all([
      getCurrentMemberInfo(),
      MemberGradeUtil.getGradeConfigs()
    ])

    // 处理会员信息响应
    if ((memberResponse as any).code === 200 && (memberResponse as any).data) {
      memberInfo.value = (memberResponse as any).data
    } else {
      error.value = true
      ElMessage.error((memberResponse as any).message || '获取会员信息失败')
      return
    }

    // 处理等级配置响应
    if (gradeConfigsResponse && gradeConfigsResponse.length > 0) {
      gradeConfigs.value = gradeConfigsResponse
    } else {
    }

  } catch (err) {
    error.value = true
    ElMessage.error('加载会员信息失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadMemberInfo()
})
</script>

<style scoped>
.member-profile {
  padding: 20px;
}

.profile-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.profile-content {
  margin-top: 20px;
}

.avatar-section {
  text-align: center;
}

.avatar-section h3 {
  margin: 15px 0 5px 0;
  color: #303133;
}

.member-no {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.info-section {
  padding: 0 20px;
}

.balance {
  color: #67C23A;
  font-weight: bold;
}

.grade-details {
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.grade-details h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
}

.grade-stat {
  text-align: center;
  padding: 15px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>