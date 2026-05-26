<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :before-close="handleClose"
    class="store-dialog-enhanced"
    :close-on-click-modal="false"
  >
    <div class="dialog-content">
      <!-- 基础信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Shop /></el-icon>
            <span>基础信息</span>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          class="store-form"
          v-loading="loading"
          label-position="top"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="门店名称" prop="storeName">
                <el-input
                  v-model="formData.storeName"
                  placeholder="请输入门店名称"
                  maxlength="100"
                  show-word-limit
                  clearable
                >
                  <template #prefix>
                    <el-icon><Shop /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="门店类型" prop="storeType">
                <el-select v-model="formData.storeType" placeholder="请选择门店类型" style="width: 100%">
                  <el-option label="直营店" value="direct">
                    <el-tag type="success" size="small">直营店</el-tag>
                  </el-option>
                  <el-option label="加盟店" value="franchise">
                    <el-tag type="warning" size="small">加盟店</el-tag>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="门店状态" prop="status">
                <el-radio-group v-model="formData.status">
                  <el-radio :value="1">
                    <el-tag type="success">营业中</el-tag>
                  </el-radio>
                  <el-radio :value="0">
                    <el-tag type="info">停业</el-tag>
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="营业时间" prop="businessHours">
                <el-input
                  v-model="formData.businessHours"
                  placeholder="例如：9:00-21:00"
                  maxlength="100"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Clock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 地址信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Location /></el-icon>
            <span>地址信息</span>
          </div>
        </template>

        <el-form
          ref="addressFormRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          label-position="top"
        >
          <!-- 省市区选择器 -->
          <RegionSelector
            v-model="regionData"
            province-label="省份"
            city-label="城市"
            district-label="区县"
            :show-popular="true"
            :disabled="loading"
            @change="handleRegionChange"
          />

          <el-form-item label="详细地址" prop="address">
            <el-input
              v-model="formData.address"
              type="textarea"
              :rows="2"
              placeholder="请输入详细地址（街道、门牌号等）"
              maxlength="255"
              show-word-limit
            />
          </el-form-item>

          <!-- 地址预览 -->
          <el-form-item v-if="fullAddress" label="完整地址">
            <el-alert type="info" :closable="false" show-icon>
              {{ fullAddress }}
            </el-alert>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 联系信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Phone /></el-icon>
            <span>联系信息</span>
          </div>
        </template>

        <el-form
          ref="contactFormRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          label-position="top"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input
                  v-model="formData.contactPhone"
                  placeholder="请输入联系电话"
                  maxlength="30"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input
                  v-model="formData.contactEmail"
                  placeholder="请输入联系邮箱"
                  maxlength="100"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 管理信息卡片 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>管理信息</span>
          </div>
        </template>

        <el-form
          ref="managementFormRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          label-position="top"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="店长ID" prop="managerId">
                <el-input-number
                  v-model="formData.managerId"
                  :min="1"
                  placeholder="请输入店长用户ID"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="店长姓名" prop="managerName">
                <el-input
                  v-model="formData.managerName"
                  placeholder="请输入店长姓名"
                  maxlength="50"
                  clearable
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="门店面积(㎡)" prop="area">
                <el-input-number
                  v-model="formData.area"
                  :min="0"
                  :precision="2"
                  placeholder="请输入门店面积"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="员工数量" prop="employeeCount">
                <el-input-number
                  v-model="formData.employeeCount"
                  :min="0"
                  placeholder="请输入员工数量"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="开业时间" prop="openTime">
                <el-date-picker
                  v-model="formData.openTime"
                  type="datetime"
                  placeholder="选择开业时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button @click="resetForm" size="large">重置</el-button>
        <el-button
          type="primary"
          @click="submitForm"
          :loading="submitting"
          size="large"
        >
          {{ isEdit ? '更新门店' : '创建门店' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Shop, Location, Phone, Message, User, Clock } from '@element-plus/icons-vue'
import { storeApi, type Store } from '@/api/store'
import RegionSelector from '@/components/RegionSelector.vue'
import { formatFullAddress } from '@/utils/regionUtils'

// Props
interface Props {
  modelValue: boolean
  store?: Store | null
}

const props = withDefaults(defineProps<Props>(), {
  store: null
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式变量
const formRef = ref<FormInstance>()
const addressFormRef = ref<FormInstance>()
const contactFormRef = ref<FormInstance>()
const managementFormRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val: boolean) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.store?.id)
const dialogTitle = computed(() => isEdit.value ? '编辑门店' : '新增门店')

// 省市区数据
const regionData = ref({
  province: '',
  city: '',
  district: ''
})

// 计算完整地址
const fullAddress = computed(() => {
  if (regionData.value.province && regionData.value.city && regionData.value.district && formData.address) {
    return formatFullAddress(
      regionData.value.province,
      regionData.value.city,
      regionData.value.district,
      formData.address
    )
  }
  return ''
})

// 表单数据
const formData = reactive({
  id: null as number | null,
  storeName: '',
  storeType: 'direct',
  status: 1,
  province: '',
  city: '',
  district: '',
  address: '',
  contactPhone: '',
  contactEmail: '',
  businessHours: '',
  managerId: null as number | null,
  managerName: '',
  area: null as number | null,
  employeeCount: 0,
  openTime: ''
})

// 表单验证规则
const formRules: FormRules = {
  storeName: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 100, message: '门店名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  storeType: [
    { required: true, message: '请选择门店类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择门店状态', trigger: 'change' }
  ],
  province: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  city: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  district: [
    { required: true, message: '请选择区县', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 255, message: '详细地址长度在 5 到 255 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  businessHours: [
    { pattern: /^(\d{1,2}:\d{2})-(\d{1,2}:\d{2})$/, message: '营业时间格式不正确，例如：9:00-21:00', trigger: 'blur' }
  ],
  area: [
    { type: 'number', min: 0, message: '门店面积必须大于等于0', trigger: 'blur' }
  ],
  employeeCount: [
    { type: 'number', min: 0, message: '员工数量必须大于等于0', trigger: 'blur' }
  ]
}

// 省市区变化处理
const handleRegionChange = (value: { province?: string; city?: string; district?: string }) => {
  formData.province = value.province || ''
  formData.city = value.city || ''
  formData.district = value.district || ''
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    storeName: '',
    storeType: 'direct',
    status: 1,
    province: '',
    city: '',
    district: '',
    address: '',
    contactPhone: '',
    contactEmail: '',
    businessHours: '',
    managerId: null,
    managerName: '',
    area: null,
    employeeCount: 0,
    openTime: ''
  })

  // 重置地址数据
  regionData.value = {
    province: '',
    city: '',
    district: ''
  }
}

// 验证所有表单
const validateAllForms = async () => {
  const formRefs = [formRef.value, addressFormRef.value, contactFormRef.value, managementFormRef.value]

  for (const ref of formRefs) {
    if (ref) {
      await ref.validate()
    }
  }
}

// 提交表单
const submitForm = async () => {
  try {
    // 验证所有表单
    await validateAllForms()

    // 二次确认
    await ElMessageBox.confirm(
      `确定要${isEdit.value ? '更新' : '创建'}这个门店吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    // 准备提交数据
    const submitData = {
      ...formData,
      id: undefined, // 不提交id字段
      area: formData.area || 0,
      employeeCount: formData.employeeCount || 0,
      managerId: formData.managerId || undefined
    }

    let response
    if (isEdit.value) {
      response = await storeApi.updateStore(formData.id!, submitData)
    } else {
      response = await storeApi.createStore(submitData)
    }

    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '门店信息更新成功' : '门店创建成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有表单数据吗？', '确认重置', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    resetFormData()
    // 清除所有表单的验证状态
    const formRefs = [formRef.value, addressFormRef.value, contactFormRef.value, managementFormRef.value]
    formRefs.forEach(ref => {
      ref?.clearValidate()
    })

    ElMessage.success('表单已重置')
  } catch (error) {
    // 用户取消操作
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 监听门店数据变化，更新表单
watch(() => props.store, (store) => {
  if (store && store.id) {
    // 编辑模式：填充表单数据
    Object.assign(formData, {
      id: store.id,
      storeName: store.storeName || '',
      storeType: store.storeType || 'direct',
      status: store.status || 1,
      province: store.province || '',
      city: store.city || '',
      district: store.district || '',
      address: store.address || '',
      contactPhone: store.contactPhone || '',
      contactEmail: store.contactEmail || '',
      businessHours: store.businessHours || '',
      managerId: store.managerId || null,
      managerName: store.managerName || '',
      area: store.area || null,
      employeeCount: store.employeeCount || 0,
      openTime: store.openTime || ''
    })

    // 设置地址数据
    regionData.value = {
      province: store.province || '',
      city: store.city || '',
      district: store.district || ''
    }
  } else {
    // 新增模式：重置表单
    resetFormData()
  }
}, { immediate: true })
</script>

<style scoped>
.store-dialog-enhanced :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.store-dialog-enhanced :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  border-bottom: 1px solid #f0f0f0;
  margin: 0 24px 20px;
}

.store-dialog-enhanced :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.store-dialog-enhanced :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 32px;
  height: 32px;
}

.store-dialog-enhanced :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 18px;
  color: #999;
  transition: color 0.3s;
}

.store-dialog-enhanced :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #666;
}

.store-dialog-enhanced :deep(.el-dialog__body) {
  padding: 0 24px 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.store-dialog-enhanced :deep(.el-dialog__footer) {
  padding: 20px 24px 24px;
  border-top: 1px solid #f0f0f0;
  margin-top: 20px;
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.info-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

.info-card :deep(.el-card__body) {
  padding: 20px;
}

.store-form {
  margin: 0;
}

.store-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.store-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-bottom: 8px;
}

.store-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.store-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.store-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.store-form :deep(.el-select) {
  width: 100%;
}

.store-form :deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

.store-form :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 24px;
  transition: all 0.3s;
}

.dialog-footer .el-button:hover {
  transform: translateY(-1px);
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-dialog-enhanced :deep(.el-dialog) {
    width: 95% !important;
    margin: 2.5vh auto;
  }

  .store-dialog-enhanced :deep(.el-dialog__header) {
    padding: 20px 20px 0;
    margin: 0 20px 16px;
  }

  .store-dialog-enhanced :deep(.el-dialog__title) {
    font-size: 18px;
  }

  .store-dialog-enhanced :deep(.el-dialog__headerbtn) {
    top: 20px;
    right: 20px;
  }

  .store-dialog-enhanced :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }

  .store-dialog-enhanced :deep(.el-dialog__footer) {
    padding: 16px 20px 20px;
  }

  .dialog-content {
    gap: 16px;
  }

  .info-card :deep(.el-card__header) {
    padding: 12px 16px;
  }

  .info-card :deep(.el-card__body) {
    padding: 16px;
  }

  .card-header {
    font-size: 14px;
  }

  .store-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  .dialog-footer {
    flex-wrap: wrap;
    gap: 8px;
  }

  .dialog-footer .el-button {
    flex: 1;
    min-width: 100px;
  }
}

/* 滚动条样式 */
.store-dialog-enhanced :deep(.el-dialog__body)::-webkit-scrollbar {
  width: 6px;
}

.store-dialog-enhanced :deep(.el-dialog__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.store-dialog-enhanced :deep(.el-dialog__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.store-dialog-enhanced :deep(.el-dialog__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 动画效果 */
.dialog-content {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表单行样式 */
.store-form :deep(.el-row) {
  margin: 0;
}

.store-form :deep(.el-col) {
  padding: 0;
}
</style>