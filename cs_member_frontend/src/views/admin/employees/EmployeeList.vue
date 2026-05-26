<template>
  <div class="employee-management">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="page-content">
      <!-- Stats Section -->
      <EmployeeStats :stats="stats" />

      <!-- Filter Section -->
      <EmployeeFilter
        v-model:searchForm="searchForm"
        v-model:quickFilter="quickFilterActive"
        v-model:viewMode="viewMode"
        :total="pagination.total"
        :store-options="storeOptions"
        :can-manage="canManage"
        :has-selection="selectedEmployees.length > 0"
        @search="handleSearch"
        @reset="handleReset"
        @add="handleAddEmployee"
        @export="handleExport"
        @batch-operation="handleBatchOperation"
      />

      <!-- List View -->
      <EmployeeTable
        v-if="viewMode === 'list'"
        :data="employeeList"
        :loading="loading"
        :pagination="pagination"
        :can-manage="canManage"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @view="handleViewEmployee"
        @edit="handleEditEmployee"
        @reset-password="handleResetPassword"
        @row-action="handleRowAction"
        @update:pageNum="val => pagination.pageNum = val"
        @update:pageSize="val => pagination.pageSize = val"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

      <!-- Grid View -->
      <EmployeeGrid
        v-else
        :data="employeeList"
        :loading="loading"
        :pagination="pagination"
        :can-manage="canManage"
        @view="handleViewEmployee"
        @edit="handleEditEmployee"
        @row-action="handleRowAction"
        @update:pageNum="val => pagination.pageNum = val"
        @update:pageSize="val => pagination.pageSize = val"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Modals -->
    <EmployeeUpsertModal
      v-model:visible="dialogVisible"
      :data="currentEmployee"
      :store-options="storeOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <PasswordResetModal
      v-model:visible="resetPasswordVisible"
      :user-id="currentEmployee?.id || 0"
      @submit="confirmResetPassword"
    />

    <EmployeeDetailModal
      v-model:visible="detailDialogVisible"
      :employee-id="viewEmployeeId"
      :can-edit="canManage"
      @edit="handleEditFromDetail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEmployeeList,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  updateEmployeeStatus,
  resetEmployeePassword,
  batchUpdateEmployeeStatus,
  batchResetEmployeePassword,
  getEmployeeStatistics,
  getEmployeeDetail
} from '@/api/employee'
import type { User, CreateUserRequest } from '@/types'
import { getStoreList } from '@/api/store'
import { useUserStore } from '@/stores/user'

// Components
import EmployeeStats from './components/EmployeeStats.vue'
import EmployeeFilter from './components/EmployeeFilter.vue'
import EmployeeTable from './components/EmployeeTable.vue'
import EmployeeGrid from './components/EmployeeGrid.vue'
import EmployeeUpsertModal from './components/EmployeeUpsertModal.vue'
import PasswordResetModal from './components/PasswordResetModal.vue'
import EmployeeDetailModal from './components/EmployeeDetailModal.vue'

// Utils
import { exportEmployeesToExcel } from '@/utils/export'

const userStore = useUserStore()
const canManage = computed(() => userStore.isStoreAdmin || userStore.isSystemAdmin)

// State
const loading = ref(false)
const submitting = ref(false)
const viewMode = ref('list')
const dialogVisible = ref(false)
const resetPasswordVisible = ref(false)
const detailDialogVisible = ref(false)
const currentEmployee = ref<any>(null)
const viewEmployeeId = ref<number | null>(null)

// Stats
const stats = reactive({
  total: 0,
  activeEmployees: 0,
  newEmployees: 0,
  storeCount: 0
})

// Search & Filter
const searchForm = reactive({
  name: '',
  phone: '',
  storeId: undefined,
  roleName: undefined,
  status: undefined
})
const quickFilterActive = ref('')

// Pagination
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})
const sortParams = reactive({ prop: '', order: '' })

// Data
const employeeList = ref<User[]>([])
const selectedEmployees = ref<User[]>([])
const storeOptions = ref([])

// Methods
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
      sortProp: sortParams.prop,
      sortOrder: sortParams.order
    }

    if (quickFilterActive.value) {
      if (['0', '1'].includes(quickFilterActive.value)) {
        params.status = parseInt(quickFilterActive.value)
      } else if (['店长', '店员'].includes(quickFilterActive.value)) {
        params.roleName = quickFilterActive.value
      }
    }

    const response = await getEmployeeList(params)
    const employees = response.data.list

    // 为每个员工添加门店名称
    employeeList.value = employees.map(employee => {
      const store = storeOptions.value.find(s => s.id === employee.storeId)
      return {
        ...employee,
        storeName: store ? store.storeName : '未知门店'
      }
    })

    pagination.total = response.data.total
    
    // Update total stats
    stats.total = response.data.total
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const response = await getEmployeeStatistics()
    Object.assign(stats, response.data)
  } catch (error) {
  }
}

const fetchStoreOptions = async () => {
  try {
    const response = await getStoreList({ pageNum: 1, pageSize: 1000 })
    storeOptions.value = response.data.list
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  }
}

// Handlers
const handleSearch = () => {
  pagination.pageNum = 1
  fetchEmployeeList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    phone: '',
    storeId: undefined,
    roleName: undefined,
    status: undefined
  })
  quickFilterActive.value = ''
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchEmployeeList()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchEmployeeList()
}

const handleSortChange = ({ prop, order }: any) => {
  sortParams.prop = prop
  sortParams.order = order
  fetchEmployeeList()
}

const handleSelectionChange = (selection: User[]) => {
  selectedEmployees.value = selection
}

const handleAddEmployee = () => {
  currentEmployee.value = null
  dialogVisible.value = true
}

const transformEmployeeForEdit = (employee: any) => {
  // Transform backend EmployeeDTO to frontend form structure
  return {
    id: employee.id,
    name: employee.name,
    phone: employee.phone,
    email: employee.email || '',
    gender: employee.gender || 1, // 添加性别字段，默认为男
    birthday: employee.birthday || undefined, // 添加生日字段
    roleId: employee.roleCode === 'STORE_ADMIN' ? 2 : 3, // Map roleCode to roleId
    storeId: employee.storeId,
    status: employee.status,
    password: '' // Don't populate password for security
  }
}

const handleEditEmployee = async (employee: User) => {
  try {
    loading.value = true
    // 获取完整的员工详情(包含生日等信息)
    const response = await getEmployeeDetail(employee.id)
    currentEmployee.value = transformEmployeeForEdit(response.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取员工详情失败')
  } finally {
    loading.value = false
  }
}

const handleViewEmployee = (employee: User) => {
  viewEmployeeId.value = employee.id
  detailDialogVisible.value = true
}

const handleEditFromDetail = (employee: any) => {
  currentEmployee.value = transformEmployeeForEdit(employee)
  dialogVisible.value = true
}

const handleResetPassword = (employee: User) => {
  currentEmployee.value = employee
  resetPasswordVisible.value = true
}

const transformFormDataForBackend = (formData: CreateUserRequest & { id?: number; birthday?: string }) => {
  // Transform frontend form to backend EmployeeDTO format
  const backendData: any = {
    name: formData.name,
    phone: formData.phone,
    email: formData.email,
    gender: formData.gender, // 添加性别字段
    storeId: formData.storeId,
    roleCode: formData.roleId === 2 ? 'STORE_ADMIN' : 'CLERK'
  }

  // 添加生日字段(如果有)
  if (formData.birthday) {
    backendData.birthday = formData.birthday
  }

  // Only include password for new employees
  if (!formData.id && formData.password) {
    backendData.password = formData.password
  }

  return backendData
}

const handleSubmit = async (formData: CreateUserRequest & { id?: number }) => {
  try {
    submitting.value = true
    const backendData = transformFormDataForBackend(formData)

    if (formData.id) {
      const response = await updateEmployee(formData.id, backendData)
      ElMessage.success(response.message || '员工更新成功')
    } else {
      const response = await createEmployee(backendData)
      ElMessage.success(response.message || '员工创建成功')
    }
    dialogVisible.value = false
    fetchEmployeeList()
    fetchStatistics()
  } catch (error) {
    // Error handled by interceptor or component
  } finally {
    submitting.value = false
  }
}

const confirmResetPassword = async ({ userId, password }: any) => {
  try {
    const response = await resetEmployeePassword(userId, password)
    ElMessage.success(response.message || '密码重置成功')
    resetPasswordVisible.value = false
  } catch (error) {
    ElMessage.error('密码重置失败')
  }
}

const handleRowAction = async (command: string, employee: User) => {
  switch (command) {
    case 'view':
      handleViewEmployee(employee)
      break
    case 'edit':
      handleEditEmployee(employee)
      break
    case 'reset-password':
      handleResetPassword(employee)
      break
    case 'enable':
      const enableResponse = await updateEmployeeStatus(employee.id, 1)
      ElMessage.success(enableResponse.message || '员工已启用')
      fetchEmployeeList()
      break
    case 'disable':
      const disableResponse = await updateEmployeeStatus(employee.id, 0)
      ElMessage.success(disableResponse.message || '员工已停用')
      fetchEmployeeList()
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除该员工吗？', '提示', { type: 'warning' })
        const deleteResponse = await deleteEmployee(employee.id)
        ElMessage.success(deleteResponse.message || '员工已删除')
        fetchEmployeeList()
      } catch (error) {
        // Cancelled
      }
      break
  }
}

const handleBatchOperation = async (command: string) => {
  if (selectedEmployees.value.length === 0) return
  const ids = selectedEmployees.value.map(e => e.id)

  switch (command) {
    case 'enable':
      const enableResults = await batchUpdateEmployeeStatus(ids, 1)
      // 批量操作使用最后一个响应的消息，或者默认消息
      const enableMsg = enableResults[enableResults.length - 1]?.message || '批量启用成功'
      ElMessage.success(enableMsg)
      fetchEmployeeList()
      break
    case 'disable':
      const disableResults = await batchUpdateEmployeeStatus(ids, 0)
      const disableMsg = disableResults[disableResults.length - 1]?.message || '批量停用成功'
      ElMessage.success(disableMsg)
      fetchEmployeeList()
      break
    case 'resetPassword':
      try {
        const { value: newPassword } = await ElMessageBox.prompt('请输入新密码', '批量重置密码', {
          inputType: 'password',
          inputValidator: (val) => (!val || val.length < 6) ? '密码长度不能少于6位' : true
        })
        const resetResults = await batchResetEmployeePassword(ids, newPassword)
        const resetMsg = resetResults[resetResults.length - 1]?.message || '批量重置密码成功'
        ElMessage.success(resetMsg)
      } catch (error) {
        // Cancelled
      }
      break
  }
}

const handleExport = () => {
  if (employeeList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  try {
    exportEmployeesToExcel(employeeList.value, '员工列表')
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败,请重试')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    // 先加载门店数据，再加载员工列表
    await fetchStoreOptions()
    await fetchEmployeeList()
    fetchStatistics()
  } catch (error) {
    loading.value = false
  }
})
</script>

<style scoped>
.employee-management {
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

.page-content {
  position: relative;
  z-index: 1;
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>