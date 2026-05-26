<template>
  <div class="table-section">
    <el-table
      v-loading="loading"
      :data="data"
      stripe
      @selection-change="$emit('selection-change', $event)"
      @sort-change="$emit('sort-change', $event)"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column prop="name" label="姓名" width="120" sortable="custom">
        <template #default="{ row }">
          <el-text type="primary" style="cursor: pointer" @click="$emit('view', row)">
            {{ row.name }}
          </el-text>
        </template>
      </el-table-column>
      <el-table-column prop="employeeNo" label="员工编号" width="150" />
      <el-table-column prop="phone" label="手机号码" width="130" />
      <el-table-column prop="roleName" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.roleCode === 'STORE_ADMIN' ? 'warning' : 'info'" size="small">
            {{ row.roleName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="storeName" label="所属门店" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : 'info'"
            size="small"
          >
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="180" sortable="custom">
        <template #default="{ row }">
          {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '从未登录' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" sortable="custom">
        <template #default="{ row }">
          {{ formatDateTime(row.createdTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center" class-name="operation-column" label-class-name="operation-column">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              v-if="canManage"
              type="primary"
              size="small"
              :icon="Edit"
              @click="$emit('edit', row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canManage"
              type="warning"
              size="small"
              :icon="Key"
              @click="$emit('reset-password', row)"
            >
              重置密码
            </el-button>
            <el-dropdown @command="(cmd: string) => $emit('row-action', cmd, row)" trigger="click">
              <el-button type="info" size="small" :icon="MoreFilled">
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">
                    <el-icon><View /></el-icon>查看详情
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status !== 1" command="enable">
                    <el-icon><Check /></el-icon>启用
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 1" command="disable">
                    <el-icon><Close /></el-icon>禁用
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-section">
      <el-pagination
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @update:current-page="$emit('update:pageNum', $event)"
        @update:page-size="$emit('update:pageSize', $event)"
        @size-change="$emit('size-change', $event)"
        @current-change="$emit('current-change', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Edit,
  Key,
  MoreFilled,
  View,
  Close,
  Check,
  Delete
} from '@element-plus/icons-vue'

defineProps<{
  data: any[]
  loading: boolean
  pagination: {
    pageNum: number
    pageSize: number
    total: number
  }
  canManage: boolean
}>()

defineEmits([
  'selection-change',
  'sort-change',
  'view',
  'edit',
  'reset-password',
  'row-action',
  'update:pageNum',
  'update:pageSize',
  'size-change',
  'current-change'
])

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '启用'
    case 0: return '禁用'
    default: return '未知'
  }
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return '-'
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  } catch (error) {
    return '-'
  }
}
</script>

<style scoped>
.table-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.9);
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

:deep(.el-table) {
  background: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(102, 126, 234, 0.08);
}

:deep(.el-table th.el-table__cell) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  font-weight: 700;
  color: #303133;
  border: none;
  font-size: 14px;
  letter-spacing: 0.5px;
}

:deep(.el-table__body tr) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-table__body tr:hover) {
  transform: translateX(4px);
  box-shadow: -4px 0 0 0 #667eea;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

:deep(.el-tag) {
  border-radius: 16px;
  padding: 4px 14px;
  border: none;
  font-weight: 600;
  font-size: 12px;
  transition: all 0.3s ease;
}

:deep(.el-tag:hover) {
  transform: scale(1.05);
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.05) 100%);
  color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15) 0%, rgba(230, 162, 60, 0.05) 100%);
  color: #e6a23c;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.05) 100%);
  color: #f56c6c;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.15) 0%, rgba(144, 147, 153, 0.05) 100%);
  color: #909399;
  box-shadow: 0 2px 8px rgba(144, 147, 153, 0.1);
}

.pagination-section {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.6);
  --el-pagination-hover-color: #667eea;
}

:deep(.el-pagination button),
:deep(.el-pager li) {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  transition: all 0.3s ease;
  font-weight: 600;
}

:deep(.el-pagination button:hover),
:deep(.el-pager li:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

:deep(.el-table .operation-column) {
  background: #ffffff !important;
  border-left: 1px solid rgba(0, 0, 0, 0.03);
}

:deep(.el-table th.operation-column) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%), #ffffff !important;
}

/* Enhanced button styles */
:deep(.action-buttons .el-button) {
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

:deep(.action-buttons .el-button::before) {
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

:deep(.action-buttons .el-button:active::before) {
  width: 200px;
  height: 200px;
}

:deep(.action-buttons .el-button:hover) {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

:deep(.action-buttons .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  border: none !important;
  color: #fff !important;
}

:deep(.action-buttons .el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%) !important;
}

:deep(.action-buttons .el-button--warning) {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%) !important;
  border: none !important;
  color: #fff !important;
}

:deep(.action-buttons .el-button--warning:hover) {
  background: linear-gradient(135deg, #ebb563 0%, #e6a23c 100%) !important;
}

:deep(.action-buttons .el-button--info) {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%) !important;
  border: none !important;
  color: #fff !important;
}

:deep(.action-buttons .el-button--info:hover) {
  background: linear-gradient(135deg, #a6a9ad 0%, #909399 100%) !important;
}

/* Dropdown menu styling */
:deep(.el-dropdown-menu) {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 8px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  transform: translateX(4px);
}

/* Loading animation */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
}

/* Text link styling */
:deep(.el-text) {
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.el-text:hover) {
  transform: translateX(2px);
}
</style>
