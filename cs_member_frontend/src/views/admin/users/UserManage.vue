<template>
  <div class="user-manage">
    <el-card>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h2 style="margin:0">用户管理</h2>
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>

      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="手机号">
          <el-input v-model="searchPhone" clearable placeholder="搜索手机号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchName" clearable placeholder="搜索姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">搜索</el-button>
          <el-button @click="searchPhone='';searchName='';fetchUsers()">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column prop="storeName" label="所属门店" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @current-change="fetchUsers"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" show-password placeholder="默认123456" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.roleId" style="width:100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser as deleteUserApi, updateUserStatus, resetUserPassword } from '@/api/user'
import { getAllRoles } from '@/api/role'

const loading = ref(false)
const userList = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchPhone = ref('')
const searchName = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = reactive({ phone: '', realName: '', password: '', roleId: undefined as number | undefined, email: '' })
let editingId = 0

const roleOptions = ref<any[]>([])

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, phone: searchPhone.value || undefined, realName: searchName.value || undefined })
    const data = (res as any).data || res
    userList.value = data.list || data.records || []
    total.value = data.total || 0
  } catch { ElMessage.error('获取用户列表失败') }
  loading.value = false
}

async function fetchRoles() {
  try {
    const res = await getAllRoles()
    roleOptions.value = ((res as any).data || res) as any[]
  } catch { /* ignore */ }
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { phone: '', realName: '', password: '', roleId: undefined, email: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editingId = row.id
  Object.assign(formData, { phone: row.phone, realName: row.realName, password: '', roleId: row.roleId, email: row.email || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.phone || !formData.realName) { ElMessage.warning('请填写必填项'); return }
  try {
    if (isEdit.value) {
      await updateUser(editingId, formData)
      ElMessage.success('更新成功')
    } else {
      await createUser(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch { ElMessage.error('操作失败') }
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchUsers()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除用户 ${row.realName} 吗？`, '删除确认', { type: 'warning' })
  try {
    await deleteUserApi(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch { ElMessage.error('删除失败') }
}

async function handleResetPwd(row: any) {
  await ElMessageBox.confirm(`确定重置用户 ${row.realName} 的密码为 123456 吗？`, '重置密码', { type: 'warning' })
  try {
    await resetUserPassword(row.id, '123456')
    ElMessage.success('密码已重置为 123456')
  } catch { ElMessage.error('重置失败') }
}

onMounted(() => { fetchUsers(); fetchRoles() })
</script>
