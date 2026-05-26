<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :before-close="handleClose"
    class="store-dialog custom-rounded-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="store-form"
      v-loading="loading"
    >
      <el-form-item label="门店名称" prop="storeName">
        <el-input
          v-model="formData.storeName"
          placeholder="请输入门店名称"
          maxlength="100"
          :disabled="isReadOnly"
        />
      </el-form-item>

      <el-form-item label="门店类型" prop="storeType">
        <el-select v-model="formData.storeType" placeholder="请选择门店类型" :disabled="isReadOnly">
          <el-option label="直营店" value="direct" />
          <el-option label="加盟店" value="franchise" />
        </el-select>
      </el-form-item>

      <el-form-item label="门店状态" prop="status">
        <el-radio-group v-model="formData.status" :disabled="isReadOnly">
          <el-radio :value="1">营业中</el-radio>
          <el-radio :value="0">停业</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 地址信息分组 -->
      <el-divider content-position="left">
        <span class="section-title">📍 门店地址信息</span>
      </el-divider>

      <div class="address-section">
        <el-form-item label="省份" prop="province" class="address-item">
          <el-select
            v-model="formData.province"
            placeholder="请选择省份"
            filterable
            clearable
            @change="handleProvinceChange"
            class="address-select"
            :disabled="isReadOnly"
          >
            <el-option
              v-for="province in provinces"
              :key="province.value"
              :label="province.label"
              :value="province.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="城市" prop="city" class="address-item">
          <el-select
            v-model="formData.city"
            placeholder="请选择城市"
            filterable
            clearable
            @change="handleCityChange"
            :disabled="isReadOnly || !formData.province"
            class="address-select"
          >
            <el-option
              v-for="city in cities"
              :key="city.value"
              :label="city.label"
              :value="city.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="区县" prop="district" class="address-item">
          <el-select
            v-model="formData.district"
            placeholder="请选择区县"
            filterable
            clearable
            :disabled="isReadOnly || !formData.city"
            class="address-select"
          >
            <el-option
              v-for="district in districts"
              :key="district.value"
              :label="district.label"
              :value="district.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="详细地址" prop="address" class="address-item-full">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址，如街道、门牌号等"
            maxlength="255"
            show-word-limit
            resize="none"
            :disabled="isReadOnly"
          />
        </el-form-item>
      </div>

      <!-- 联系信息分组 -->
      <el-divider content-position="left">
        <span class="section-title">📞 联系信息</span>
      </el-divider>

      <div class="contact-section">
        <el-form-item label="联系电话" prop="contactPhone" class="contact-item">
          <el-input
            v-model="formData.contactPhone"
            placeholder="请输入手机号码或座机号（如：010-87654321）"
            maxlength="20"
            prefix-icon="Phone"
            :disabled="isReadOnly"
          />
        </el-form-item>

        <el-form-item label="联系邮箱" prop="contactEmail" class="contact-item">
          <el-input
            v-model="formData.contactEmail"
            placeholder="请输入联系邮箱"
            maxlength="100"
            prefix-icon="Message"
            :disabled="isReadOnly"
          />
        </el-form-item>

        <el-form-item label="营业时间" prop="businessHours" class="contact-item-full">
          <el-input
            v-model="formData.businessHours"
            placeholder="例如：9:00-21:00"
            maxlength="100"
            prefix-icon="Clock"
            :disabled="isReadOnly"
          />
        </el-form-item>
      </div>

      <!-- 经营信息分组 -->
      <el-divider content-position="left">
        <span class="section-title">💼 经营信息</span>
      </el-divider>

      <div class="business-section" :class="{ 'edit-mode': isEdit }">
        <el-form-item label="门店面积(㎡)" prop="area" class="business-item">
          <el-input-number
            v-model="formData.area"
            :min="0"
            :precision="2"
            placeholder="请输入门店面积"
            :controls="false"
            style="width: 100%"
            :disabled="isReadOnly"
          />
        </el-form-item>

        <el-form-item label="店长" prop="managerId" class="business-item" v-if="isEdit || isView">
          <el-select
            v-model="formData.managerId"
            placeholder="请选择店长"
            filterable
            :disabled="isReadOnly"
            clearable
            remote
            :remote-method="searchManagers"
            :loading="managerLoading"
            @change="handleManagerChange"
            style="width: 100%"
          >
            <el-option
              v-for="manager in managerOptions"
              :key="manager.id"
              :label="manager.name"
              :value="manager.id"
            >
              <div class="manager-option">
                <span class="manager-name">{{ manager.name }}</span>
                <span class="manager-phone">{{ manager.phone }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="开业时间" prop="openTime" class="business-item">
          <el-date-picker
            v-model="formData.openTime"
            type="datetime"
            placeholder="选择开业时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled="isReadOnly"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" @click="resetForm">重置</el-button>
        <el-button
          v-if="!isView"
          type="primary"
          @click="submitForm"
          :loading="submitting"
        >
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { storeApi, type Store, type UserInfo } from '@/api/store'
import { provinces, citiesData, districtsData, type RegionOption } from '@/data/regionDataNational'

// Props
interface Props {
  modelValue: boolean
  store?: Store | null
  mode?: 'add' | 'edit' | 'view'
}

const props = withDefaults(defineProps<Props>(), {
  store: null,
  mode: 'add'
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式变量
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

// 店长选择相关
const managerLoading = ref(false)
const managerOptions = ref<UserInfo[]>([])
const allManagers = ref<UserInfo[]>([])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val: boolean) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.store?.id)
const isView = computed(() => props.mode === 'view')
const isReadOnly = computed(() => props.mode === 'view')

// 对话框标题
const dialogTitle = computed(() => {
  switch (props.mode) {
    case 'add':
      return '新增门店'
    case 'edit':
      return '编辑门店'
    case 'view':
      return '查看门店详情'
    default:
      return '门店信息'
  }
})

// 省市区数据引用
const cities = ref<RegionOption[]>([])
const districts = ref<RegionOption[]>([])

// 表单数据
const formData = reactive({
  id: null as number | null,
  storeName: '',
  storeType: 'direct',
  province: '',
  city: '',
  district: '',
  address: '',
  contactPhone: '',
  contactEmail: '',
  businessHours: '',
  managerId: null as number | null,
  area: null as number | null,
  status: 1,
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
    {
      pattern: /^(\d{3,4}-)?\d{7,8}$|^1[3-9]\d{9}$/,
      message: '请输入正确的电话号码（手机号或座机号）',
      trigger: 'blur'
    }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  businessHours: [
    { required: true, message: '请输入营业时间', trigger: 'blur' },
    { pattern: /^([0-1]?[0-9]|2[0-3]):([0-5][0-9])-([0-1]?[0-9]|2[0-3]):([0-5][0-9])$/,
      message: '请输入正确的营业时间格式，如：9:00-21:00', trigger: 'blur' }
  ],
  managerId: [
    { required: false, message: '请选择店长', trigger: 'change' }
  ],
  area: [
    { type: 'number', min: 0, message: '门店面积必须大于等于0', trigger: 'blur' }
  ]
}

// 省份变化处理
const handleProvinceChange = () => {
  formData.city = ''
  formData.district = ''
  cities.value = citiesData[formData.province] || []
  districts.value = []
}

// 城市变化处理
const handleCityChange = () => {
  formData.district = ''
  districts.value = districtsData[formData.city] || []
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    storeName: '',
    storeType: 'direct',
    province: '',
    city: '',
    district: '',
    address: '',
    contactPhone: '',
    contactEmail: '',
    businessHours: '',
    managerId: null,
    area: null,
    status: 1,
    openTime: ''
  })

  // 重置地址数据
  cities.value = []
  districts.value = []
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 过滤空值，不提交员工数量（由系统自动计算）
    const submitData = {
      ...formData,
      id: undefined, // 不提交id字段
      area: formData.area || 0,
      managerId: formData.managerId || undefined
    }

    let response
    if (isEdit.value) {
      response = await storeApi.updateStore(formData.id!, submitData)
    } else {
      response = await storeApi.createStore(submitData)
    }

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '门店信息更新成功' : '门店创建成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  resetFormData()
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 加载员工列表（如果门店员工为空，则加载所有可用员工）
const loadEmployees = async () => {
  if (!props.store?.id) {
    allManagers.value = []
    managerOptions.value = []
    return
  }

  try {
    managerLoading.value = true
    const response = await storeApi.getStoreEmployees(props.store.id)

    // 处理不同的响应格式
    let employees = []
    if (Array.isArray(response.data)) {
      // 直接是数组格式
      employees = response.data
    } else if (response.data?.data && Array.isArray(response.data.data)) {
      // 包装格式
      employees = response.data.data
    }

    // 检查店长是否在员工列表中
    const managerExists = employees.some(emp => emp.id === props.store.managerId)

    // 如果店长不在员工列表中，但门店有店长，则添加店长到选项中
    if (!managerExists && props.store.managerId) {
      employees.push({
        id: props.store.managerId,
        name: props.store.managerName || '未知店长',
        phone: '',
        status: 1,
        storeId: props.store.id
      })
    }

    allManagers.value = employees
    managerOptions.value = allManagers.value
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  } finally {
    managerLoading.value = false
  }
}

// 搜索店长
const searchManagers = (query: string) => {
  if (query !== '') {
    managerLoading.value = true
    setTimeout(() => {
      managerLoading.value = false
      managerOptions.value = allManagers.value.filter(manager =>
        manager.name.toLowerCase().includes(query.toLowerCase()) ||
        manager.phone.includes(query)
      )
    }, 200)
  } else {
    managerOptions.value = allManagers.value
  }
}

// 店长变化处理
const handleManagerChange = (managerId: number | null) => {
  // 不需要设置managerName，因为现在通过关联查询获取
}

// 监听门店数据变化，更新表单
watch(() => props.store, async (store) => {
  if (store && store.id) {
    // 先填充表单数据（但不包含managerId）
    Object.assign(formData, {
      id: store.id,
      storeName: store.storeName || '',
      storeType: store.storeType || 'direct',
      province: store.province || '',
      city: store.city || '',
      district: store.district || '',
      address: store.address || '',
      contactPhone: store.contactPhone || '',
      contactEmail: store.contactEmail || '',
      businessHours: store.businessHours || '',
      managerId: null, // 暂时设为null
      area: store.area || null,
      status: store.status || 1,
      openTime: store.openTime || ''
    })

    // 编辑模式：先加载员工列表，再设置店长ID
    await loadEmployees()

    // 在员工列表加载完成后再设置managerId，但要验证店长是否属于当前门店
    if (store.managerId) {
      // 检查店长是否在员工列表中，或者是否属于当前门店
      const managerInCurrentStore = allManagers.value.some(emp =>
        emp.id === store.managerId && emp.storeId === store.id
      )

      // 只有当店长在当前门店的员工列表中，或者没有storeId限制时才设置
      if (managerInCurrentStore || allManagers.value.some(emp => emp.id === store.managerId && !emp.storeId)) {
        formData.managerId = store.managerId
      } else {
        // 如果店长不属于当前门店，清空managerId
        formData.managerId = null
      }
    }

    // 初始化地址数据
    if (store.province) {
      // 手动设置省份数据，不清空已有的城市和区县
      cities.value = citiesData[store.province] || []
      if (store.city) {
        // 手动设置城市数据，不清空已有的区县
        districts.value = districtsData[store.city] || []
      }
    }
  } else {
    // 新增模式：重置表单
    resetFormData()
    allManagers.value = []
    managerOptions.value = []
  }
}, { immediate: true })

// 组件挂载时仅在编辑模式下加载员工列表
onMounted(() => {
  if (props.store?.id) {
    loadEmployees()
  }
})
</script>

<style scoped>
/* 分组标题样式 */
.section-title {
  font-weight: 600;
  color: #2c3e50;
  font-size: 15px;
}

/* 地址分组布局 */
.address-section {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 8px;
}

.address-item {
  margin-bottom: 0;
}

.address-item-full {
  grid-column: 1 / -1;
  margin-bottom: 0;
}

.address-select {
  width: 100%;
  min-width: 120px;
}

/* 地址选择器下拉选项样式 */
.store-dialog :deep(.el-select .el-input__wrapper) {
  min-height: 40px;
}

.store-dialog :deep(.el-select .el-input__inner) {
  height: 40px;
  line-height: 40px;
}

.store-dialog :deep(.el-select-dropdown__item) {
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
}

/* 员工数量字段内容居中 */
.store-dialog :deep(.business-item .el-input__inner) {
  text-align: center;
  font-weight: 600;
  color: #2c3e50;
}

/* 联系信息分组布局 */
.contact-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 8px;
}

.contact-item {
  margin-bottom: 0;
}

.contact-item-full {
  grid-column: 1 / -1;
  margin-bottom: 0;
}

/* 经营信息分组布局 */
.business-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 8px;
}



.business-item {
  margin-bottom: 0;
}

/* 店长选择框样式 */
.manager-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.manager-name {
  font-weight: 500;
  color: #303133;
}

.manager-phone {
  color: #909399;
  font-size: 12px;
}

/* 对话框圆角设计 */
.store-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.store-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  border-bottom: 1px solid #f0f0f0;
  margin: 0 24px 20px;
}

.store-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.store-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 32px;
  height: 32px;
}

.store-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 18px;
  color: #999;
  transition: color 0.3s;
}

.store-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #666;
}

.store-dialog :deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

.store-dialog :deep(.el-dialog__footer) {
  padding: 20px 24px 24px;
  border-top: 1px solid #f0f0f0;
  margin-top: 20px;
}

.store-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表单项圆角设计 */
.dialog-footer .el-button {
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 20px;
  transition: all 0.3s;
}

.dialog-footer .el-button:hover {
  transform: translateY(-1px);
}

/* 简约的滚动条样式 */
.store-form::-webkit-scrollbar {
  width: 6px;
}

.store-form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.store-form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.store-form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .store-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }

  .address-section {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .address-select {
    min-width: unset;
  }

  .store-dialog :deep(.el-dialog__header) {
    padding: 20px 20px 0;
    margin: 0 20px 16px;
  }

  .store-dialog :deep(.el-dialog__title) {
    font-size: 16px;
  }

  .store-dialog :deep(.el-dialog__headerbtn) {
    top: 20px;
    right: 20px;
  }

  .store-dialog :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }

  .store-dialog :deep(.el-dialog__footer) {
    padding: 16px 20px 20px;
  }

  .store-form {
    max-height: 50vh;
  }

  .dialog-footer {
    justify-content: center;
  }

  .dialog-footer .el-button {
    flex: 1;
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