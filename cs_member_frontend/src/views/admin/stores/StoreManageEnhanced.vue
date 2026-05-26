<template>
  <div class="store-manage-enhanced">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>门店管理</el-breadcrumb-item>
            <el-breadcrumb-item>门店列表</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title">
            <el-icon><Shop /></el-icon>
            门店管理
          </h1>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleAdd" size="large">
            <el-icon><Plus /></el-icon>
            新增门店
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和操作栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="门店名称">
          <el-input
            v-model="searchForm.storeName"
            placeholder="请输入门店名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="门店类型">
          <el-select
            v-model="searchForm.storeType"
            placeholder="请选择门店类型"
            clearable
            style="width: 150px"
          >
            <el-option label="直营店" value="direct" />
            <el-option label="加盟店" value="franchise" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择门店状态"
            clearable
            style="width: 150px"
          >
            <el-option label="营业中" :value="1" />
            <el-option label="停业" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份">
          <el-select
            v-model="searchForm.province"
            placeholder="请选择省份"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="province in provinces"
              :key="province.value"
              :label="province.label"
              :value="province.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="searchLoading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="storeName" label="门店名称" min-width="150" />
        <el-table-column prop="storeType" label="门店类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.storeType === 'direct' ? 'success' : 'warning'" size="small">
              {{ row.storeType === 'direct' ? '直营店' : '加盟店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="地址" min-width="200">
          <template #default="{ row }">
            {{ formatAddress(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="managerName" label="店长" width="100" />
        <el-table-column prop="employeeCount" label="员工数" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 批量操作栏 -->
    <div v-if="selectedRows.length > 0" class="batch-actions">
      <el-card shadow="never">
        <div class="batch-content">
          <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
          <div class="batch-buttons">
            <el-button type="primary" @click="handleBatchEdit">批量编辑</el-button>
            <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
            <el-button @click="handleClearSelection">取消选择</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 门店对话框（使用增强版） -->
    <StoreDialogEnhanced
      v-model="dialogVisible"
      :store="currentStore"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, Plus, Search, Refresh } from '@element-plus/icons-vue'
import StoreDialogEnhanced from './StoreDialogEnhanced.vue'
import { storeApi, type Store } from '@/api/store'
import { provinces } from '@/data/regionDataFull'
import { formatFullAddress } from '@/utils/regionUtils'

// 响应式数据
const tableLoading = ref(false)
const searchLoading = ref(false)
const tableData = ref<Store[]>([])
const selectedRows = ref<Store[]>([])
const dialogVisible = ref(false)
const currentStore = ref<Store | null>(null)

// 搜索表单
const searchForm = reactive({
  storeName: '',
  storeType: '',
  status: null as number | null,
  province: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 方法
// 获取门店列表
const fetchStoreList = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...searchForm
    }

    const response = await storeApi.getStoreList(params)
    if (response.data.code === 200) {
      tableData.value = response.data.data.records || []
      pagination.total = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.message || '获取门店列表失败')
    }
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  searchLoading.value = true
  pagination.currentPage = 1
  await fetchStoreList()
  searchLoading.value = false
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    storeName: '',
    storeType: '',
    status: null,
    province: ''
  })
  pagination.currentPage = 1
  fetchStoreList()
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchStoreList()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  fetchStoreList()
}

// 表格选择变化
const handleSelectionChange = (rows: Store[]) => {
  selectedRows.value = rows
}

// 清除选择
const handleClearSelection = () => {
  selectedRows.value = []
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap = {
    1: 'success',
    0: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap = {
    1: '营业中',
    0: '停业'
  }
  return statusMap[status] || '未知'
}

// 格式化地址
const formatAddress = (row: Store) => {
  return formatFullAddress(row.province, row.city, row.district, row.address)
}

// 新增门店
const handleAdd = () => {
  currentStore.value = null
  dialogVisible.value = true
}

// 查看门店
const handleView = (row: Store) => {
  // 这里可以实现查看详情功能
  ElMessage.info('查看门店详情功能待实现')
}

// 编辑门店
const handleEdit = (row: Store) => {
  currentStore.value = { ...row }
  dialogVisible.value = true
}

// 删除门店
const handleDelete = async (row: Store) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除门店"${row.storeName}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await storeApi.deleteStore(row.id!)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      fetchStoreList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量编辑
const handleBatchEdit = () => {
  ElMessage.info('批量编辑功能待实现')
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个门店吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const ids = selectedRows.value.map(row => row.id!)
    const response = await storeApi.batchDeleteStores(ids)
    if (response.data.code === 200) {
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      fetchStoreList()
    } else {
      ElMessage.error(response.data.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 对话框成功回调
const handleDialogSuccess = () => {
  fetchStoreList()
}

// 页面挂载
onMounted(() => {
  fetchStoreList()
})
</script>

<style scoped>
.store-manage-enhanced {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title .el-icon {
  font-size: 32px;
  color: #409eff;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.search-card :deep(.el-card__body) {
  padding: 20px;
}

.search-form {
  margin: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.table-card {
  border-radius: 12px;
}

.table-card :deep(.el-card__body) {
  padding: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.batch-actions {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
}

.batch-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.batch-info {
  font-size: 14px;
  color: #606266;
}

.batch-buttons {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-manage-enhanced {
    padding: 12px;
  }

  .page-header {
    padding: 16px;
    margin-bottom: 12px;
  }

  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .search-card {
    margin-bottom: 12px;
  }

  .search-card :deep(.el-card__body) {
    padding: 16px;
  }

  .search-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .table-card :deep(.el-card__body) {
    padding: 12px;
  }

  .batch-actions {
    bottom: 10px;
    width: calc(100% - 24px);
    left: 12px;
    transform: none;
  }

  .batch-content {
    flex-direction: column;
    gap: 12px;
  }

  .batch-buttons {
    width: 100%;
    justify-content: space-between;
  }

  .batch-buttons .el-button {
    flex: 1;
  }
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-table .el-button + .el-button) {
  margin-left: 8px;
}

/* 动画效果 */
.page-header {
  animation: slideDown 0.3s ease-in-out;
}

.search-card {
  animation: slideUp 0.3s ease-in-out 0.1s both;
}

.table-card {
  animation: slideUp 0.3s ease-in-out 0.2s both;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>