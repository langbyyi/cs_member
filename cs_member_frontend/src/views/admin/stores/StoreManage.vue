<template>
  <div class="store-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-card class="header-card" shadow="never">
        <div class="header-content">
          <div class="header-info">
            <h1 class="page-title">
              <el-icon class="title-icon"><Shop /></el-icon>
              {{ isEdit ? '编辑门店' : '新增门店' }}
            </h1>
            <p class="page-description">
              {{ isEdit ? '修改门店基本信息' : '创建新的门店分店' }}
            </p>
          </div>
          <div class="header-actions">
            <el-button @click="handleCancel">
              <el-icon><ArrowLeft /></el-icon>
              返回列表
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 表单内容 -->
    <div class="form-container">
      <el-card class="form-card" shadow="hover">
        <StoreDialog
          v-model:visible="dialogVisible"
          :store-data="storeData"
          :is-edit="isEdit"
          @success="handleSuccess"
          @cancel="handleCancel"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, ArrowLeft } from '@element-plus/icons-vue'
import StoreDialog from './StoreDialog.vue'
import { storeApi, type Store } from '@/api/store'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const dialogVisible = ref(true)
const storeData = ref<Partial<Store>>({})
const loading = ref(false)

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 页面加载时的处理
onMounted(async () => {
  if (isEdit.value) {
    await loadStoreData()
  }
})

// 加载门店数据
const loadStoreData = async () => {
  try {
    loading.value = true
    const storeId = Number(route.params.id)
    const response = await storeApi.getStoreById(storeId)

    if (response.data.code === 200) {
      storeData.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载门店信息失败')
    }
  } catch (error) {
    ElMessage.error('加载门店信息失败')
  } finally {
    loading.value = false
  }
}

// 操作成功处理
const handleSuccess = () => {
  ElMessage.success(isEdit.value ? '门店信息更新成功' : '门店创建成功')
  router.push('/admin/stores')
}

// 取消操作
const handleCancel = () => {
  router.push('/admin/stores')
}
</script>

<style scoped>
.store-manage {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  margin-bottom: 24px;
}

.header-card {
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.header-info {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.title-icon {
  margin-right: 12px;
  color: #409eff;
  font-size: 32px;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
  opacity: 0.8;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
}

.form-container {
  animation: fadeInUp 0.6s ease-out;
}

.form-card {
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: visible;
}

.form-card :deep(.el-card__body) {
  padding: 0;
  overflow: visible;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-manage {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .title-icon {
    font-size: 28px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>