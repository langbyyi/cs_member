<template>
  <el-dialog
    class="custom-rounded-dialog employee-dialog"
    :model-value="visible"
    :title="isEdit ? '编辑员工' : '新增员工'"
    width="600px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <template #header>
      <div class="dialog-header-content">
        <el-icon class="header-icon">
          <User />
        </el-icon>
        <span class="header-title">{{ isEdit ? '编辑员工' : '新增员工' }}</span>
      </div>
    </template>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="员工姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入员工姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12" v-if="!isEdit">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="默认密码: 123456"
              show-password
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="form.birthday"
              type="date"
              placeholder="请选择生日"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
              :disabled-date="disabledDate"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色" prop="roleId">
            <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
              <el-option label="店员" :value="3" />
              <el-option label="店长" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属门店" prop="storeId">
            <el-select v-model="form.storeId" placeholder="请选择门店" style="width: 100%">
              <el-option
                v-for="store in storeOptions"
                :key="store.id"
                :label="store.storeName"
                :value="store.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <!-- Status field removed -->
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { User } from '@element-plus/icons-vue'
import type { CreateUserRequest } from '@/types'

const props = defineProps<{
  visible: boolean
  data?: any
  storeOptions: any[]
  submitting: boolean
}>()

const emit = defineEmits(['update:visible', 'submit'])

const formRef = ref()
const isEdit = computed(() => !!props.data?.id)

const form = ref<CreateUserRequest & { id?: number; birthday?: string }>({
  name: '',
  phone: '',
  email: '',
  gender: 1,
  roleId: 3,
  storeId: undefined,
  status: 1,
  password: '',
  birthday: undefined
})

watch(() => props.visible, (val) => {
  if (val) {
    if (props.data) {
      form.value = { ...props.data, password: '' }
    } else {
      form.value = {
        name: '',
        phone: '',
        email: '',
        gender: 1,
        roleId: 3,
        storeId: undefined,
        status: 1,
        password: '',
        id: undefined,
        birthday: undefined
      }
    }
  }
})

// 限制生日选择范围(不能选择未来日期)
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

const rules = {
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  storeId: [
    { required: true, message: '请选择门店', trigger: 'change' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('submit', form.value)
    }
  })
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
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

/* 自定义头部内容 */
.dialog-header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 关闭按钮美化 */
:deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

:deep(.el-dialog__headerbtn:hover) {
  background: rgba(0, 0, 0, 0.08);
  transform: rotate(90deg);
}

:deep(.el-dialog__close) {
  font-size: 18px;
  color: #909399;
  transition: color 0.3s ease;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #606266;
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
  max-height: 70vh;
  overflow-y: auto;
}

/* 滚动条样式 */
:deep(.el-dialog__body)::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-dialog__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

:deep(.el-dialog__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

:deep(.el-dialog__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
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
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
  transition: all 0.3s ease;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: #fff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
  background: #fff;
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #303133;
}

:deep(.el-input__inner::placeholder) {
  color: #c0c4cc;
}

/* Radio Group */
:deep(.el-radio-group) {
  background: rgba(0, 0, 0, 0.03);
  padding: 6px;
  border-radius: 12px;
  display: inline-flex;
  gap: 8px;
}

:deep(.el-radio) {
  margin-right: 0;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-radio:hover) {
  background: rgba(102, 126, 234, 0.05);
}

:deep(.el-radio.is-checked) {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-radio__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-radio.is-checked .el-radio__label) {
  color: #667eea;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto;
  }

  :deep(.el-dialog__body) {
    padding: 24px 20px;
    max-height: 60vh;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
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
