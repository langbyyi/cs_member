<template>
  <div class="grid-section" v-loading="loading">
    <div class="employee-grid">
      <div
        v-for="employee in data"
        :key="employee.id"
        class="employee-card"
      >
        <div class="card-header">
          <div class="header-top">
            <el-tag
              :type="employee.status === 1 ? 'success' : 'info'"
              size="small"
              effect="light"
              class="status-tag"
            >
              {{ getStatusText(employee.status) }}
            </el-tag>
            <el-dropdown @command="(cmd: string) => $emit('row-action', cmd, employee)" trigger="click">
              <el-button link class="more-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="reset-password">
                    <el-icon><Key /></el-icon>重置密码
                  </el-dropdown-item>
                  <el-dropdown-item command="view" divided>
                    <el-icon><View /></el-icon>查看详情
                  </el-dropdown-item>
                  <el-dropdown-item v-if="employee.status !== 1" command="enable">
                    <el-icon><Check /></el-icon>启用
                  </el-dropdown-item>
                  <el-dropdown-item v-if="employee.status === 1" command="disable">
                    <el-icon><Close /></el-icon>禁用
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="avatar-section">
            <el-avatar :size="64" :src="employee.avatar" class="user-avatar">
              {{ employee.name?.charAt(0) }}
            </el-avatar>
          </div>
        </div>
        
        <div class="card-body">
          <h3 class="user-name" @click="$emit('view', employee)">{{ employee.name }}</h3>
          <p class="user-role">{{ employee.roleName }}</p>
          
          <div class="info-row">
            <el-icon><Phone /></el-icon>
            <span>{{ employee.phone }}</span>
          </div>
          <div class="info-row">
            <el-icon><Shop /></el-icon>
            <span>{{ employee.storeName }}</span>
          </div>
          <div class="info-row">
            <el-icon><Timer /></el-icon>
            <span>{{ formatDateTime(employee.lastLoginTime) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <el-button 
            v-if="canManage" 
            type="primary" 
            plain 
            size="small" 
            class="action-btn"
            @click="$emit('edit', employee)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="canManage" 
            size="small" 
            class="action-btn"
            @click="$emit('view', employee)"
          >
            详情
          </el-button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-section">
      <el-pagination
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[12, 24, 48, 96]"
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
  MoreFilled,
  Edit,
  Key,
  View,
  Close,
  Check,
  Delete,
  Phone,
  Shop,
  Timer
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
  'view',
  'edit',
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
  if (!dateTime) return '从未登录'
  return new Date(dateTime).toLocaleDateString()
}
</script>

<style scoped>
.grid-section {
  padding: 0 0 24px 0;
}

.employee-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 28px;
}

.employee-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.9);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  animation: cardFadeIn 0.6s ease-out backwards;
}

.employee-card:nth-child(1) { animation-delay: 0.05s; }
.employee-card:nth-child(2) { animation-delay: 0.1s; }
.employee-card:nth-child(3) { animation-delay: 0.15s; }
.employee-card:nth-child(4) { animation-delay: 0.2s; }
.employee-card:nth-child(5) { animation-delay: 0.25s; }
.employee-card:nth-child(6) { animation-delay: 0.3s; }
.employee-card:nth-child(7) { animation-delay: 0.35s; }
.employee-card:nth-child(8) { animation-delay: 0.4s; }

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.employee-card::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  opacity: 0;
  transition: opacity 0.4s;
  z-index: -1;
}

.employee-card:hover::before {
  opacity: 1;
  animation: borderRotate 3s linear infinite;
}

@keyframes borderRotate {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}

.employee-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.card-header {
  padding: 24px 24px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.header-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: -20px;
  z-index: 2;
}

.status-tag {
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.more-btn {
  transform: rotate(90deg);
  color: #909399;
  transition: all 0.3s ease;
}

.more-btn:hover {
  color: #667eea;
  transform: rotate(90deg) scale(1.2);
}

.avatar-section {
  position: relative;
  z-index: 1;
  margin-bottom: 16px;
  margin-top: 20px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 28px;
  font-weight: 700;
  border: 4px solid rgba(255, 255, 255, 1);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.user-avatar::after {
  content: '';
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  filter: blur(16px);
  transition: opacity 0.4s;
  z-index: -1;
}

.employee-card:hover .user-avatar {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.6);
}

.employee-card:hover .user-avatar::after {
  opacity: 0.6;
}

.card-body {
  padding: 0 28px 24px;
  text-align: center;
  flex: 1;
}

.user-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-name:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: scale(1.05);
}

.user-role {
  margin: 0 0 20px;
  font-size: 13px;
  color: #909399;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  display: inline-block;
  padding: 4px 16px;
  border-radius: 16px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  font-size: 13px;
  margin-bottom: 10px;
  gap: 8px;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 8px;
}

.info-row:hover {
  background: rgba(102, 126, 234, 0.05);
  transform: translateX(4px);
}

.info-row .el-icon {
  color: #667eea;
  font-size: 16px;
}

.card-footer {
  padding: 20px 28px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
  display: flex;
  gap: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.6) 100%);
}

.action-btn {
  flex: 1;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.action-btn:active::before {
  width: 200px;
  height: 200px;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.action-btn.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.action-btn.el-button--primary.is-plain {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 2px solid #667eea;
  color: #667eea;
}

.action-btn.el-button--primary.is-plain:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
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
  background: rgba(255, 255, 255, 0.8);
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

/* Dropdown styling */
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

/* Responsive */
@media (max-width: 768px) {
  .employee-grid {
    grid-template-columns: 1fr;
  }
}
</style>
