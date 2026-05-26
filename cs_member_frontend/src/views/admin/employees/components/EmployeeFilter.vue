<template>
  <div class="search-section">
    <div class="search-header">
      <div class="search-title">
        <h2>员工列表</h2>
        <span class="total-count">共 {{ total }} 条记录</span>
      </div>
      <div class="search-actions">
        <el-button
          v-if="canManage"
          type="primary"
          :icon="Plus"
          @click="$emit('add')"
        >
          新增员工
        </el-button>
        <el-button
          :icon="Download"
          @click="$emit('export')"
        >
          导出数据
        </el-button>
        <el-dropdown
          v-if="canManage"
          @command="(cmd: string) => $emit('batch-operation', cmd)"
          trigger="click"
        >
          <el-button :icon="Operation" :disabled="!hasSelection">
            批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <!-- Status batch actions removed -->
              <el-dropdown-item command="resetPassword" divided>
                <el-icon><Key /></el-icon>重置密码
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- View Toggle -->
        <el-radio-group v-model="viewModeModel" size="small">
          <el-radio-button value="list">
            <el-icon><Menu /></el-icon>
          </el-radio-button>
          <el-radio-button value="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- Quick Filters -->
    <div class="quick-filters">
      <el-radio-group v-model="quickFilterModel" @change="$emit('search')">
        <el-radio-button value="">全部</el-radio-button>
        <!-- Status filters removed -->
        <el-radio-button value="店员">店员</el-radio-button>
        <el-radio-button value="店长">店长</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Search Form -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="员工姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入员工姓名"
            clearable
            style="width: 200px"
            @keyup.enter="$emit('search')"
          />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号码"
            clearable
            style="width: 200px"
            @keyup.enter="$emit('search')"
          />
        </el-form-item>
        <el-form-item label="所属门店">
          <el-select
            v-model="searchForm.storeId"
            placeholder="请选择门店"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.roleName"
            placeholder="请选择角色"
            clearable
            style="width: 150px"
          >
            <el-option label="店员" value="店员" />
            <el-option label="店长" value="店长" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="$emit('search')">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="$emit('reset')">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Plus,
  Download,
  Operation,
  Search,
  Refresh,
  ArrowDown,
  Check,
  Close,
  Key,
  Menu,
  Grid
} from '@element-plus/icons-vue'

const props = defineProps<{
  searchForm: any
  quickFilter: string
  viewMode: string
  total: number
  storeOptions: any[]
  canManage: boolean
  hasSelection: boolean
}>()

const emit = defineEmits([
  'update:searchForm',
  'update:quickFilter',
  'update:viewMode',
  'search',
  'reset',
  'add',
  'export',
  'batch-operation'
])

const searchForm = computed({
  get: () => props.searchForm,
  set: (val) => emit('update:searchForm', val)
})

const quickFilterModel = computed({
  get: () => props.quickFilter,
  set: (val) => emit('update:quickFilter', val)
})

const viewModeModel = computed({
  get: () => props.viewMode,
  set: (val) => emit('update:viewMode', val)
})
</script>

<style scoped>
.search-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.9);
  animation: slideDown 0.5s ease-out;
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

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.search-title h2 {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.total-count {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.search-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.search-actions :deep(.el-button--primary::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.search-actions :deep(.el-button--primary:active::before) {
  width: 300px;
  height: 300px;
}

.search-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.search-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 10px 20px;
  transition: all 0.3s ease;
}

.search-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
}

.quick-filters {
  margin-bottom: 24px;
}

.quick-filters :deep(.el-radio-group) {
  display: flex;
  gap: 12px;
}

.quick-filters :deep(.el-radio-button__inner) {
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.6);
  padding: 10px 24px;
  border-radius: 24px;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.quick-filters :deep(.el-radio-button__inner::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.quick-filters :deep(.el-radio-button__inner:hover::before) {
  left: 100%;
}

.quick-filters :deep(.el-radio-button__inner:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-filters :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px) scale(1.05);
}

.quick-filters :deep(.el-radio-button:first-child .el-radio-button__inner),
.quick-filters :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 24px;
}

.search-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.search-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-form :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.search-form :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

.search-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.search-form :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.search-form :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
}

/* Radio group for view mode */
.search-actions :deep(.el-radio-group) {
  background: rgba(255, 255, 255, 0.6);
  padding: 4px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.search-actions :deep(.el-radio-button__inner) {
  border: none;
  background: transparent;
  border-radius: 8px;
  transition: all 0.3s ease;
  padding: 8px 12px;
}

.search-actions :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-actions {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }
  
  .quick-filters :deep(.el-radio-group) {
    flex-wrap: wrap;
  }
}
</style>
