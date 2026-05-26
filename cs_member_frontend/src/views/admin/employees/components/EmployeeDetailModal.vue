<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(val) => $emit('update:visible', val)"
    title="员工详情"
    width="800px"
    :close-on-click-modal="false"
    class="employee-detail-dialog"
  >
    <div v-loading="loading" class="detail-container">
      <div v-if="employeeDetail" class="detail-content">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>姓名</label>
                <span>{{ employeeDetail.name }}</span>
              </div>
              <div class="info-item">
                <label>工号</label>
                <span>{{ employeeDetail.employeeNo || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>手机号</label>
                <span>{{ employeeDetail.phone }}</span>
              </div>
              <div class="info-item">
                <label>邮箱</label>
                <span>{{ employeeDetail.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>性别</label>
                <span>{{ genderText }}</span>
              </div>
              <div class="info-item">
                <label>生日</label>
                <span>{{ employeeDetail.birthday || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 工作信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <el-icon><Briefcase /></el-icon>
            <span>工作信息</span>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>所属门店</label>
                <span>{{ storeName }}</span>
              </div>
              <div class="info-item">
                <label>职位</label>
                <el-tag :type="roleTagType">{{ employeeDetail.roleName }}</el-tag>
              </div>
              <div class="info-item">
                <label>状态</label>
                <el-tag :type="statusTagType">{{ statusText }}</el-tag>
              </div>
              <div class="info-item">
                <label>入职时间</label>
                <span>{{ formatDateTime(employeeDetail.createdTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 登录信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>登录信息</span>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>登录次数</label>
                <span>{{ employeeDetail.loginCount || 0 }} 次</span>
              </div>
              <div class="info-item">
                <label>最后登录时间</label>
                <span>{{ employeeDetail.lastLoginTime ? formatDateTime(employeeDetail.lastLoginTime) : '从未登录' }}</span>
              </div>
              <div class="info-item">
                <label>最后登录IP</label>
                <span>{{ employeeDetail.lastLoginIp || '-' }}</span>
              </div>
              <div class="info-item">
                <label>最后更新时间</label>
                <span>{{ formatDateTime(employeeDetail.updatedTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
        <el-button v-if="canEdit" type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { User, Briefcase, Clock, Edit } from '@element-plus/icons-vue'
import { getEmployeeDetail } from '@/api/employee'
import type { EmployeeDTO } from '@/api/employee'
import { getStoreList } from '@/api/store'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

interface Props {
  visible: boolean
  employeeId: number | null
  canEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  canEdit: true
})

const storeMap = ref<Map<number, string>>(new Map())

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'edit': [employee: EmployeeDTO]
}>()

const loading = ref(false)
const employeeDetail = ref<EmployeeDTO | null>(null)

// 计算属性
const genderText = computed(() => {
  if (!employeeDetail.value?.gender) return '未设置'
  return employeeDetail.value.gender === 1 ? '男' : '女'
})

const storeName = computed(() => {
  if (!employeeDetail.value) return '未分配'
  // 优先使用API返回的storeName
  if (employeeDetail.value.storeName) return employeeDetail.value.storeName
  // 如果没有storeName但有storeId,从storeMap中查找
  if (employeeDetail.value.storeId) {
    return storeMap.value.get(employeeDetail.value.storeId) || '未分配'
  }
  return '未分配'
})

const statusText = computed(() => {
  if (!employeeDetail.value) return ''
  switch (employeeDetail.value.status) {
    case 0: return '已停用'
    case 1: return '正常'
    case 2: return '已锁定'
    default: return '未知'
  }
})

const statusTagType = computed(() => {
  if (!employeeDetail.value) return ''
  switch (employeeDetail.value.status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
})

const roleTagType = computed(() => {
  if (!employeeDetail.value) return ''
  return employeeDetail.value.roleCode === 'STORE_ADMIN' ? 'warning' : 'primary'
})

// 方法
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 加载门店列表
const fetchStores = async () => {
  try {
    const response = await getStoreList({ pageNum: 1, pageSize: 1000 })
    const stores = response.data.list
    // 构建门店ID到名称的映射
    storeMap.value = new Map(stores.map(store => [store.id, store.storeName]))
  } catch (error) {
  }
}

const fetchEmployeeDetail = async () => {
  if (!props.employeeId) return
  
  loading.value = true
  try {
    const response = await getEmployeeDetail(props.employeeId)
    employeeDetail.value = response.data
  } catch (error) {
    ElMessage.error('获取员工详情失败')
    emit('update:visible', false)
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  if (employeeDetail.value) {
    emit('edit', employeeDetail.value)
    emit('update:visible', false)
  }
}

// 组件挂载时加载门店列表
onMounted(() => {
  fetchStores()
})

// 监听对话框打开
watch(() => props.visible, (newVal) => {
  if (newVal && props.employeeId) {
    fetchEmployeeDetail()
  } else if (!newVal) {
    // 关闭时清空数据
    employeeDetail.value = null
  }
})
</script>

<style scoped>
.employee-detail-dialog :deep(.el-dialog) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.employee-detail-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.employee-detail-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.employee-detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.detail-container {
  min-height: 300px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.card-header .el-icon {
  font-size: 20px;
}

.card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.info-item span {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  min-width: 100px;
  border-radius: 8px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
