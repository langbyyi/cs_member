<template>
  <el-card class="quick-actions">
    <template #header>
      <div class="quick-actions__header">
        <h3 class="quick-actions__title">
          <el-icon><Operation /></el-icon>
          快捷操作
        </h3>
        <el-button
          text
          size="small"
          @click="handleCustomize"
        >
          <el-icon><Setting /></el-icon>
          自定义
        </el-button>
      </div>
    </template>

    <div class="quick-actions__content">
      <div class="quick-actions__grid">
        <div
          v-for="action in displayActions"
          :key="action.key"
          class="quick-actions__item"
          :class="[`quick-actions__item--${action.type}`, { 'quick-actions__item--disabled': action.disabled }]"
          @click="handleActionClick(action)"
        >
          <div class="quick-actions__icon">
            <el-icon :size="24">
              <component :is="action.icon" />
            </el-icon>
          </div>

          <div class="quick-actions__info">
            <div class="quick-actions__label">{{ action.label }}</div>
            <div class="quick-actions__description">{{ action.description }}</div>
          </div>

          <div class="quick-actions__badge" v-if="action.badge">
            <el-badge :value="action.badge" :type="action.badgeType" />
          </div>

          <div v-if="action.shortcut" class="quick-actions__shortcut">
            {{ formatShortcut(action.shortcut) }}
          </div>
        </div>
      </div>

      <!-- 常用功能区域 -->
      <div class="quick-actions__frequent" v-if="frequentActions.length > 0">
        <h4 class="quick-actions__subtitle">常用功能</h4>
        <div class="quick-actions__frequent-list">
          <el-button
            v-for="action in frequentActions.slice(0, 4)"
            :key="action.key"
            :type="action.type"
            :icon="action.icon"
            size="small"
            plain
            @click="handleActionClick(action)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="quick-actions__search">
        <el-input
          v-model="searchQuery"
          placeholder="搜索功能..."
          :prefix-icon="Search"
          clearable
          size="small"
          @input="handleSearch"
        />
        <div v-if="searchResults.length > 0 && searchQuery" class="quick-actions__search-results">
          <div
            v-for="result in searchResults.slice(0, 5)"
            :key="result.key"
            class="quick-actions__search-result"
            @click="handleActionClick(result)"
          >
            <el-icon>
              <component :is="result.icon" />
            </el-icon>
            <span>{{ result.label }}</span>
            <span class="quick-actions__search-path">{{ result.path }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 自定义对话框 -->
    <el-dialog
      v-model="customizeVisible"
      title="自定义快捷操作"
      width="600px"
    >
      <div class="quick-actions__customize">
        <p class="quick-actions__customize-tip">
          拖拽调整顺序，点击眼睛图标显示/隐藏操作
        </p>
        <div class="quick-actions__customize-list">
          <div
            v-for="action in allActions"
            :key="action.key"
            class="quick-actions__customize-item"
            :class="{ 'quick-actions__customize-item--hidden': !action.visible }"
            draggable
            @dragstart="handleDragStart(action, $event)"
            @dragover="handleDragOver($event)"
            @drop="handleDrop(action, $event)"
          >
            <el-icon class="quick-actions__drag-handle"><Rank /></el-icon>
            <el-icon class="quick-actions__action-icon">
              <component :is="action.icon" />
            </el-icon>
            <span class="quick-actions__action-label">{{ action.label }}</span>
            <div class="quick-actions__action-controls">
              <el-button
                text
                :icon="action.visible ? View : Hide"
                size="small"
                @click="toggleActionVisibility(action)"
              />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="customizeVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCustomization">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Operation,
  Setting,
  Search,
  UserFilled,
  Van,
  Ticket,
  Coin,
  Plus,
  Upload,
  Download,
  View,
  Hide,
  Rank
} from '@element-plus/icons-vue'

interface QuickAction {
  key: string
  label: string
  description: string
  icon: any
  type: 'primary' | 'success' | 'warning' | 'info' | 'danger'
  path?: string
  shortcut?: string
  badge?: number
  badgeType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  disabled?: boolean
  visible?: boolean
  category?: string
}

const router = useRouter()

// 所有可用的快捷操作
const allActions = ref<QuickAction[]>([
  {
    key: 'add-member',
    label: '新增会员',
    description: '快速添加新会员',
    icon: markRaw(UserFilled),
    type: 'primary',
    path: '/admin/members/create',
    shortcut: 'Ctrl+N',
    category: 'member',
    visible: true
  },
  {
    key: 'add-consumption',
    label: '记录消费',
    description: '录入会员消费记录',
    icon: markRaw(Van),
    type: 'success',
    path: '/admin/consumption/create',
    shortcut: 'Ctrl+Shift+C',
    category: 'consumption',
    visible: true
  },
  {
    key: 'create-coupon',
    label: '创建优惠券',
    description: '批量生成优惠券',
    icon: markRaw(Ticket),
    type: 'warning',
    path: '/admin/coupons/create',
    category: 'coupon',
    visible: true
  },
  {
    key: 'adjust-points',
    label: '积分调整',
    description: '批量调整会员积分',
    icon: markRaw(Coin),
    type: 'info',
    path: '/admin/points',
    category: 'points',
    visible: true
  },
  {
    key: 'import-members',
    label: '批量导入',
    description: 'Excel批量导入会员',
    icon: markRaw(Upload),
    type: 'primary',
    path: '/admin/members',
    shortcut: 'Ctrl+I',
    category: 'member',
    visible: false
  },
  {
    key: 'export-data',
    label: '数据导出',
    description: '导出各类统计数据',
    icon: markRaw(Download),
    type: 'info',
    path: '/admin/statistics',
    category: 'report',
    visible: false
  },
  {
    key: 'member-analysis',
    label: '会员分析',
    description: '查看会员行为分析',
    icon: markRaw(UserFilled),
    type: 'success',
    path: '/admin/statistics',
    category: 'report',
    visible: false
  },
  {
    key: 'sales-report',
    label: '销售报表',
    description: '生成销售数据报表',
    icon: markRaw(Van),
    type: 'warning',
    path: '/admin/statistics',
    category: 'report',
    visible: false
  }
])

// 显示的操作（可自定义）
const displayActions = ref<QuickAction[]>([])

// 常用功能（基于使用频率）
const frequentActions = ref<QuickAction[]>([])

// 搜索相关
const searchQuery = ref('')
const searchResults = ref<QuickAction[]>([])

// 自定义对话框
const customizeVisible = ref(false)

// 拖拽相关
const draggedItem = ref<QuickAction | null>(null)

// 计算显示的操作
const visibleActions = computed(() => {
  return allActions.value.filter(action => action.visible !== false)
})

// 处理搜索
const handleSearch = (query: string) => {
  if (!query) {
    searchResults.value = []
    return
  }

  const lowerQuery = query.toLowerCase()
  searchResults.value = allActions.value.filter(action =>
    action.label.toLowerCase().includes(lowerQuery) ||
    action.description.toLowerCase().includes(lowerQuery)
  )
}

// 处理操作点击
const handleActionClick = (action: QuickAction) => {
  if (action.disabled) {
    ElMessage.warning('该功能暂时不可用')
    return
  }

  if (action.path) {
    router.push(action.path)
  }

  // 记录使用频率
  updateActionFrequency(action.key)

  ElMessage.success(`执行操作: ${action.label}`)
}

// 更新操作使用频率
const updateActionFrequency = (actionKey: string) => {
  const storageKey = 'dashboard_action_frequency'
  const frequency = JSON.parse(localStorage.getItem(storageKey) || '{}')
  frequency[actionKey] = (frequency[actionKey] || 0) + 1
  localStorage.setItem(storageKey, JSON.stringify(frequency))

  updateFrequentActions()
}

// 更新常用功能
const updateFrequentActions = () => {
  const storageKey = 'dashboard_action_frequency'
  const frequency = JSON.parse(localStorage.getItem(storageKey) || '{}')

  const sorted = Object.entries(frequency)
    .sort(([, a], [, b]) => (b as number) - (a as number))
    .slice(0, 4)
    .map(([key]) => key)

  frequentActions.value = sorted
    .map(key => allActions.value.find(action => action.key === key))
    .filter(Boolean) as QuickAction[]
}

// 处理自定义
const handleCustomize = () => {
  customizeVisible.value = true
}

// 切换操作可见性
const toggleActionVisibility = (action: QuickAction) => {
  action.visible = !action.visible
}

// 拖拽开始
const handleDragStart = (action: QuickAction, event: DragEvent) => {
  draggedItem.value = action
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/html', '')
  }
}

// 拖拽经过
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 拖拽放下
const handleDrop = (targetAction: QuickAction, event: DragEvent) => {
  event.preventDefault()
  if (!draggedItem.value || draggedItem.value.key === targetAction.key) {
    return
  }

  const draggedIndex = allActions.value.findIndex(action => action.key === draggedItem.value!.key)
  const targetIndex = allActions.value.findIndex(action => action.key === targetAction.key)

  if (draggedIndex !== -1 && targetIndex !== -1) {
    const [removed] = allActions.value.splice(draggedIndex, 1)
    allActions.value.splice(targetIndex, 0, removed)
  }

  draggedItem.value = null
}

// 保存自定义配置
const saveCustomization = () => {
  const config = {
    visibleActions: allActions.value.filter(action => action.visible !== false).map(action => action.key),
    actionOrder: allActions.value.map(action => action.key)
  }

  localStorage.setItem('dashboard_quick_actions_config', JSON.stringify(config))
  updateDisplayActions()
  customizeVisible.value = false
  ElMessage.success('配置已保存')
}

// 更新显示的操作
const updateDisplayActions = () => {
  const config = JSON.parse(localStorage.getItem('dashboard_quick_actions_config') || '{}')

  if (config.actionOrder) {
    allActions.value.sort((a, b) => {
      const aIndex = config.actionOrder.indexOf(a.key)
      const bIndex = config.actionOrder.indexOf(b.key)
      return aIndex - bIndex
    })
  }

  if (config.visibleActions) {
    allActions.value.forEach(action => {
      action.visible = config.visibleActions.includes(action.key)
    })
  }

  displayActions.value = allActions.value.filter(action => action.visible !== false).slice(0, 6)
}

// 格式化快捷键显示
const formatShortcut = (shortcut: string): string => {
  return shortcut.replace('Ctrl', '⌃').replace('Shift', '⇧')
}

// 加载徽章数据（模拟）
const loadBadgeData = () => {
  // 这里可以调用API获取实际数据
  allActions.value.find(action => action.key === 'add-consumption')!.badge = 3
  allActions.value.find(action => action.key === 'create-coupon')!.badge = 12
  allActions.value.find(action => action.key === 'adjust-points')!.badge = 5
}

// 监听显示操作变化
watch(visibleActions, (newActions) => {
  displayActions.value = newActions.slice(0, 6)
}, { immediate: true })

onMounted(() => {
  updateDisplayActions()
  updateFrequentActions()
  loadBadgeData()
})
</script>

<style scoped>
.quick-actions {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.quick-actions__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.quick-actions__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-actions__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.quick-actions__item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-actions__item:hover {
  background: #f0f9ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.quick-actions__item--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-actions__item--disabled:hover {
  transform: none;
  background: #fafafa;
  border-color: #e4e7ed;
  box-shadow: none;
}

/* 不同类型的操作样式 */
.quick-actions__item--primary {
  border-left: 3px solid #409eff;
}

.quick-actions__item--success {
  border-left: 3px solid #67c23a;
}

.quick-actions__item--warning {
  border-left: 3px solid #e6a23c;
}

.quick-actions__item--info {
  border-left: 3px solid #909399;
}

.quick-actions__item--danger {
  border-left: 3px solid #f56c6c;
}

.quick-actions__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: white;
  color: #606266;
  font-size: 24px;
  flex-shrink: 0;
}

.quick-actions__item--primary .quick-actions__icon {
  background: #e6f7ff;
  color: #1890ff;
}

.quick-actions__item--success .quick-actions__icon {
  background: #f6ffed;
  color: #52c41a;
}

.quick-actions__item--warning .quick-actions__icon {
  background: #fff7e6;
  color: #fa8c16;
}

.quick-actions__item--info .quick-actions__icon {
  background: #f0f9ff;
  color: #1890ff;
}

.quick-actions__item--danger .quick-actions__icon {
  background: #fff1f0;
  color: #f5222d;
}

.quick-actions__info {
  text-align: center;
  flex: 1;
  min-width: 0;
}

.quick-actions__label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  line-height: 1.2;
}

.quick-actions__description {
  font-size: 12px;
  color: #909399;
  line-height: 1.3;
}

.quick-actions__badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.quick-actions__shortcut {
  position: absolute;
  bottom: 4px;
  right: 8px;
  font-size: 10px;
  color: #c0c4cc;
  background: white;
  padding: 2px 4px;
  border-radius: 3px;
  border: 1px solid #e4e7ed;
}

.quick-actions__frequent {
  border-top: 1px solid var(--border-color-lighter);
  padding-top: 16px;
}

.quick-actions__subtitle {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 12px 0;
}


.quick-actions__search {
  position: relative;
}

.quick-actions__search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-color);
  border: 1px solid var(--border-color-light);
  border-radius: 6px;
  box-shadow: 0 4px 12px var(--shadow-color-light);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.quick-actions__search-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.quick-actions__search-result:hover {
  background: var(--fill-color-light);
}

.quick-actions__search-path {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-color-tertiary);
}

/* 自定义对话框样式 */
.quick-actions__customize {
  max-height: 400px;
  overflow-y: auto;
}

.quick-actions__customize-tip {
  color: var(--text-color-secondary);
  font-size: 13px;
  margin-bottom: 16px;
}

.quick-actions__customize-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-actions__customize-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--fill-color-lighter);
  border-radius: 6px;
  cursor: move;
  transition: all 0.2s ease;
}

.quick-actions__customize-item:hover {
  background: var(--fill-color-light);
}

.quick-actions__customize-item--hidden {
  opacity: 0.5;
}

.quick-actions__drag-handle {
  color: var(--text-color-tertiary);
  cursor: move;
}

.quick-actions__action-icon {
  color: var(--text-color-secondary);
}

.quick-actions__action-label {
  flex: 1;
  font-size: 14px;
  color: var(--text-color);
}

.quick-actions__action-controls {
  display: flex;
  gap: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .quick-actions__grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .quick-actions__item {
    padding: 12px 8px;
  }

  .quick-actions__icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .quick-actions__label {
    font-size: 13px;
  }

  .quick-actions__description {
    font-size: 11px;
  }

  .quick-actions__frequent-list {
    gap: 8px;
    max-width: 280px;
  }

  .quick-actions__frequent-list {
    gap: 10px !important;
    max-width: 280px !important;
  }

  .quick-actions__frequent-list :deep(.el-button) {
    height: 48px !important;
    padding: 6px 4px !important;
  }

  .quick-actions__frequent-list :deep(.el-button .el-icon) {
    width: 18px !important;
    height: 18px !important;
  }

  .quick-actions__frequent-list :deep(.el-button span) {
    font-size: 10px !important;
    max-height: 12px !important;
  }

  .quick-actions__customize-item {
    padding: 10px;
  }
}

/* 按钮统一样式 - 2x2网格布局 - 深度选择器强制覆盖 */
.quick-actions__frequent-list {
  display: grid !important;
  grid-template-columns: repeat(2, 1fr) !important;
  gap: 12px !important;
  justify-content: center !important;
  max-width: 320px !important;
  margin: 0 auto !important;
}

.quick-actions__frequent-list :deep(.el-button) {
  width: 100% !important;
  height: 56px !important;
  max-width: none !important;
  min-width: 0 !important;
  font-weight: 500 !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 4px !important;
  transition: all 0.2s ease !important;
  padding: 8px 6px !important;
  box-sizing: border-box !important;
  margin: 0 !important;
  border-radius: 6px !important;
}

.quick-actions__frequent-list :deep(.el-button .el-icon) {
  width: 20px !important;
  height: 20px !important;
  flex-shrink: 0 !important;
  margin: 0 0 2px 0 !important;
  display: block !important;
}

.quick-actions__frequent-list :deep(.el-button span) {
  font-size: 11px !important;
  line-height: 1.2 !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  width: 100% !important;
  text-align: center !important;
  display: block !important;
  margin: 0 !important;
  padding: 0 !important;
  max-height: 14px !important;
}

.quick-actions__frequent-list .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-color-light);
}

@media (max-width: 480px) {
  .quick-actions__grid {
    grid-template-columns: 1fr;
  }

  .quick-actions__item {
    flex-direction: row;
    align-items: center;
    padding: 12px;
  }

  .quick-actions__info {
    text-align: left;
  }

  .quick-actions__shortcut {
    position: static;
    margin-top: 4px;
  }

  .quick-actions__frequent-list {
    gap: 6px;
    max-width: 260px;
  }

  .quick-actions__frequent-list {
    gap: 8px !important;
    max-width: 240px !important;
  }

  .quick-actions__frequent-list :deep(.el-button) {
    height: 44px !important;
    padding: 4px 2px !important;
  }

  .quick-actions__frequent-list :deep(.el-button .el-icon) {
    width: 16px !important;
    height: 16px !important;
  }

  .quick-actions__frequent-list :deep(.el-button span) {
    font-size: 9px !important;
    max-height: 11px !important;
  }
}
</style>