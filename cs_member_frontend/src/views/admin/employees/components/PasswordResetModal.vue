<template>
  <el-dialog
    class="custom-rounded-dialog"
    :model-value="visible"
    title="重置密码"
    width="400px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="新密码">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请确认新密码"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
  userId: number
}>()

const emit = defineEmits(['update:visible', 'submit'])

const form = reactive({
  password: '',
  confirmPassword: ''
})

watch(() => props.visible, (val) => {
  if (val) {
    form.password = ''
    form.confirmPassword = ''
  }
})

const handleSubmit = () => {
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (form.password.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }
  emit('submit', { userId: props.userId, password: form.password })
}
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 24px 28px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.05) 0%, rgba(245, 108, 108, 0.05) 100%);
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

:deep(.el-dialog__footer) {
  padding: 20px 28px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.5);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Form Styling */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
  transition: all 0.3s ease;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: #fff;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.15);
  border-color: #e6a23c;
  background: #fff;
}

/* Footer Buttons */
:deep(.el-button) {
  border-radius: 12px;
  padding: 10px 24px;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--default) {
  background: #f5f7fa;
  color: #606266;
}

:deep(.el-button--default:hover) {
  background: #e4e7ed;
  transform: translateY(-2px);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(230, 162, 60, 0.4);
}
</style>

<style>
.custom-rounded-dialog {
  border-radius: 20px !important;
  overflow: hidden !important;
}

.custom-rounded-dialog .el-dialog__header {
  margin-right: 0 !important;
  border-top-left-radius: 20px !important;
  border-top-right-radius: 20px !important;
}
</style>
