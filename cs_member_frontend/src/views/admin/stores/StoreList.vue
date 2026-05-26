<template>
  <div class="store-list-container">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="store-list-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-actions">
          <el-button type="primary" size="large" @click="handleAdd" class="add-button">
            <el-icon><Plus /></el-icon>
            <span>新增门店</span>
          </el-button>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="门店搜索">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入门店名称、编号或地址"
              clearable
              prefix-icon="Search"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              style="width: 250px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch" style="width: 120px">
              <el-option label="营业中" :value="1" />
              <el-option label="停业" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="search-btn">
              <el-icon><Search /></el-icon> 查询
            </el-button>
            <el-button @click="resetSearch" class="reset-btn">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格卡片 -->
      <div class="table-card">
        <el-table 
          :data="tableData" 
          v-loading="loading" 
          class="modern-table"
          :header-cell-style="{ background: 'rgba(240, 242, 245, 0.5)', color: '#1a1a1a', fontWeight: '600', height: '50px' }"
          :row-style="{ height: '65px' }"
        >
          <el-table-column prop="storeName" label="门店名称" min-width="200">
            <template #default="{ row }">
              <div class="store-name-cell">
                <div class="store-avatar">
                  {{ row.storeName.charAt(0) }}
                </div>
                <div class="store-info">
                  <span class="store-name">{{ row.storeName }}</span>
                  <span class="store-sub">{{ row.storeNo }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="storeType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.storeType === 'direct' ? 'warning' : 'info'" effect="light" round size="small">
                {{ row.storeType === 'direct' ? '直营' : '加盟' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="address" label="地址" min-width="280">
            <template #default="{ row }">
              <div class="address-cell">
                <el-icon class="cell-icon"><Location /></el-icon>
                <div class="address-content">
                  <div class="full-address">
                    {{ getFullAddress(row) }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="contactPhone" label="联系电话" width="160">
            <template #default="{ row }">
              <div class="phone-cell">
                <el-icon class="cell-icon"><Phone /></el-icon>
                <span>{{ row.contactPhone }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="managerName" label="店长" width="140">
            <template #default="{ row }">
              <div class="manager-cell">
                <el-avatar :size="28" class="manager-avatar" :style="{ backgroundColor: row.managerName ? '#409EFF' : '#C0C4CC' }">
                  {{ row.managerName?.charAt(0) || '无' }}
                </el-avatar>
                <span :class="{ 'text-placeholder': !row.managerName }">{{ row.managerName || '未分配' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="employeeCount" label="员工数" width="100" align="center">
            <template #default="{ row }">
              <div class="count-cell">
                <span class="count-text">{{ row.employeeCount || 0 }}</span>
                <span class="count-unit">人</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="memberCount" label="会员数" width="100" align="center">
            <template #default="{ row }">
              <span class="count-text">{{ row.memberCount || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="Number(row.status) === 1 ? 'success' : 'info'" 
                effect="light" 
                round 
                size="small"
              >
                <span class="flex-center">
                  <span class="dot" :class="Number(row.status) === 1 ? 'bg-success' : 'bg-info'"></span>
                  {{ Number(row.status) === 1 ? '营业中' : '停业' }}
                </span>
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-tooltip content="编辑" placement="top">
                  <el-button type="primary" plain circle size="small" @click="handleEdit(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                
                <el-tooltip :content="Number(row.status) === 1 ? '关闭门店' : '开启门店'" placement="top">
                  <el-button 
                    :type="Number(row.status) === 1 ? 'danger' : 'success'" 
                    plain 
                    circle 
                    size="small"
                    @click="handleToggleStatus(row)"
                  >
                    <el-icon v-if="Number(row.status) === 1"><SwitchButton /></el-icon>
                    <el-icon v-else><VideoPlay /></el-icon>
                  </el-button>
                </el-tooltip>

                <el-tooltip content="查看详情" placement="top">
                  <el-button type="info" plain circle size="small" @click="handleView(row)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页器 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 门店对话框 -->
    <StoreDialog
      v-model="dialogVisible"
      :store="currentStore"
      :mode="dialogMode"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Shop, Location, Phone, Clock, View, Edit,
  Search, Refresh, SwitchButton, VideoPlay
} from '@element-plus/icons-vue'
import { storeApi, type Store } from '@/api/store'
import StoreDialog from './StoreDialog.vue'

const loading = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})

// 格式化完整地址
const getFullAddress = (store: Store) => {
  const parts = []
  if (store.province) parts.push(store.province)
  // 避免直辖市重复显示(如:北京市北京市)
  if (store.city && store.city !== store.province) parts.push(store.city)
  if (store.district) parts.push(store.district)
  if (store.address) parts.push(store.address)
  return parts.join('')
}

const tableData = ref<Store[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'view'>('add')
const currentStore = ref<Store | null>(null)

// 获取门店列表
async function fetchStoreList() {
  try {
    loading.value = true
    const params: any = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status
    }
    
    // 关键词搜索：支持门店名称、编号、地址搜索
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }

    const res = await storeApi.getStoreList(params)

    // request.ts 响应拦截器已经返回了完整的响应对象
    if (res.code === 200) {
      // 后端返回的是PageInfo格式，需要适配
      const pageInfo = res.data
      tableData.value = pageInfo.list || []
      pagination.total = pageInfo.total || 0
    }
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchStoreList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = undefined
  handleSearch()
}

onMounted(() => {
  fetchStoreList()
})

function handleView(row: Store) {
  // 查看详情：只读模式
  currentStore.value = { ...row }
  dialogMode.value = 'view'
  dialogVisible.value = true
}

function handleEdit(row: Store) {
  // 编辑门店
  currentStore.value = { ...row }
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

function handleAdd() {
  // 新增门店
  currentStore.value = null
  dialogMode.value = 'add'
  dialogVisible.value = true
}

// 对话框成功回调
function handleDialogSuccess() {
  fetchStoreList()
}

async function handleToggleStatus(row: Store) {
  const newStatus = Number(row.status) === 1 ? 0 : 1
  const action = newStatus === 1 ? '开启' : '关闭'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}门店 "${row.storeName}" 吗？`,
      `${action}门店`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: newStatus === 1 ? 'success' : 'warning',
        icon: newStatus === 1 ? VideoPlay : SwitchButton
      }
    )

    const res = await storeApi.updateStoreStatus(row.id!, newStatus)
    if (res.code === 200) {
      ElMessage.success(`门店${action}成功`)
      row.status = newStatus
    } else {
      ElMessage.error(res.message || `${action}门店失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}门店失败`)
    }
  }
}


function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchStoreList()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  fetchStoreList()
}
</script>

<style scoped>
.store-list-container {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f2f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态背景图形 */
.background-shapes {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  top: -10%;
  right: -5%;
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #a0c4ff 0%, #c2e9fb 100%);
  animation-delay: 0s;
}

.shape-2 {
  bottom: -10%;
  left: -5%;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #ffc3a0 0%, #ffafbd 100%);
  animation-delay: -5s;
}

.shape-3 {
  top: 40%;
  left: 40%;
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%);
  animation-delay: -10s;
  opacity: 0.4;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(10deg); }
  66% { transform: translate(-20px, 20px) rotate(-5deg); }
}

.store-list-content {
  position: relative;
  z-index: 1;
  padding: 24px 32px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
  animation: slideDown 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.add-button {
  height: 44px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
}

/* 搜索卡片 */
.search-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  animation: fadeIn 0.6s ease 0.1s both;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}

.search-form :deep(.el-input__wrapper:hover),
.search-form :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  background: #fff;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 20px;
}

/* 表格卡片 */
.table-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.04);
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 表格样式优化 */
.modern-table {
  background: transparent;
  --el-table-border-color: rgba(0, 0, 0, 0.05);
  --el-table-row-hover-bg-color: rgba(64, 158, 255, 0.05);
  --el-table-text-color: #2c3e50;
  --el-table-header-text-color: #1a1a1a;
}

.modern-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.store-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.store-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e0eaff 0%, #f0f4ff 100%);
  color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.store-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.store-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

.store-sub {
  font-size: 12px;
  color: #606266;
  font-family: monospace;
  background: rgba(0, 0, 0, 0.04);
  padding: 2px 6px;
  border-radius: 4px;
  width: fit-content;
}

.address-cell, .phone-cell, .time-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #2c3e50;
  line-height: 1.4;
}

.cell-icon {
  color: #909399;
  margin-top: 2px;
  flex-shrink: 0;
}

.address-content {
  flex: 1;
  min-width: 0;
}

.full-address {
  font-size: 13px;
  line-height: 1.4;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manager-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.manager-avatar {
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.text-placeholder {
  color: #909399;
  font-style: italic;
  font-size: 12px;
}

.count-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.count-text {
  font-family: 'Roboto', sans-serif;
  font-weight: 600;
  color: #606266;
  font-size: 16px;
}

.count-unit {
  font-size: 11px;
  color: #909399;
}

.flex-center {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.bg-success {
  background-color: #67c23a;
}

.bg-info {
  background-color: #909399;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 分页器 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

@media (max-width: 768px) {
  .store-list-content {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .add-button {
    width: 100%;
  }
  
  .search-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
    width: 100%;
  }
  
  .search-form :deep(.el-input) {
    width: 100%;
  }
}
</style>
