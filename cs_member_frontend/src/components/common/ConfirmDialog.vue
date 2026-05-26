<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="400px"
    :before-close="handleClose"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="confirm-content">
      <div class="confirm-icon" :class="type">
        <el-icon size="48">
          <component :is="getIcon()" />
        </el-icon>
      </div>
      <div class="confirm-text">
        <h3 v-if="title">{{ title }}</h3>
        <p>{{ message }}</p>
      </div>
    </div>

    <template #footer>
      <div class="confirm-actions">
        <el-button @click="handleCancel" :disabled="loading">
          {{ cancelText }}
        </el-button>
        <el-button
          :type="buttonType"
          :loading="loading"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  modelValue: boolean
  title?: string
  message: string
  type?: 'warning' | 'danger' | 'info' | 'success'
  confirmText?: string
  cancelText?: string
  loading?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'warning',
  confirmText: '确定',
  cancelText: '取消',
  loading: false
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const buttonType = computed(() => {
  const typeMap = {
    warning: 'warning',
    danger: 'danger',
    info: 'primary',
    success: 'success'
  }
  return typeMap[props.type]
})

function getIcon(): string {
  const iconMap = {
    warning: 'WarningFilled',
    danger: 'CircleCloseFilled',
    info: 'InfoFilled',
    success: 'CircleCheckFilled'
  }
  return iconMap[props.type]
}

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
  visible.value = false
}

function handleClose() {
  if (!props.loading) {
    emit('cancel')
    visible.value = false
  }
}
</script>

<style scoped>
.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 0;
}

.confirm-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.confirm-icon.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.confirm-icon.danger {
  background: #fef0f0;
  color: #f56c6c;
}

.confirm-icon.info {
  background: #f4f4f5;
  color: #909399;
}

.confirm-icon.success {
  background: #f0f9ff;
  color: #67c23a;
}

.confirm-text {
  flex: 1;
}

.confirm-text h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.confirm-text p {
  font-size: 14px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 0 0;
  border-top: 1px solid #f0f0f0;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .confirm-text h3 {
    color: #e5eaf3;
  }

  .confirm-text p {
    color: #cfd3dc;
  }

  .confirm-actions {
    border-top-color: #4c4d4f;
  }
}
</style>