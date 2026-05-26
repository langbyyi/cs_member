<template>
  <div class="role-manage">
    <el-card>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h2 style="margin:0">角色管理</h2>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="roleType" label="角色类型" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="handleAssignPermissions(row)">分配权限</el-button>
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
        @current-change="fetchRoles"
      />
    </el-card>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="formData.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="formData.roleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色类型">
          <el-select v-model="formData.roleType" style="width:100%">
            <el-option label="系统" value="system" />
            <el-option label="门店" value="store" />
            <el-option label="员工" value="clerk" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px">
      <p style="margin-bottom:12px;color:#666">为角色 <b>{{ currentRole?.roleName }}</b> 分配权限：</p>
      <el-checkbox-group v-model="selectedPermissions" v-loading="permLoading">
        <div v-for="perm in allPermissions" :key="perm.id" style="margin-bottom:6px">
          <el-checkbox :value="perm.id">{{ perm.permissionName }} ({{ perm.permissionCode }})</el-checkbox>
        </div>
      </el-checkbox-group>
      <el-empty v-if="!permLoading && allPermissions.length === 0" description="暂无权限数据" />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRoleList, createRole, updateRole, deleteRole as deleteRoleApi,
  updateRoleStatus, getRolePermissions, assignRolePermissions, getAllPermissions
} from '@/api/role'

const loading = ref(false)
const roleList = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = reactive({ roleName: '', roleCode: '', roleType: 'system', description: '' })
let editingId = 0

const permDialogVisible = ref(false)
const permLoading = ref(false)
const currentRole = ref<any>(null)
const allPermissions = ref<any[]>([])
const selectedPermissions = ref<number[]>([])

async function fetchRoles() {
  loading.value = true
  try {
    const res = await getRoleList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const data = (res as any).data || res
    roleList.value = data.list || data.records || []
    total.value = data.total || 0
  } catch { ElMessage.error('获取角色列表失败') }
  loading.value = false
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { roleName: '', roleCode: '', roleType: 'system', description: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editingId = row.id
  Object.assign(formData, { roleName: row.roleName, roleCode: row.roleCode, roleType: row.roleType, description: row.description || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.roleName || !formData.roleCode) { ElMessage.warning('请填写必填项'); return }
  try {
    if (isEdit.value) {
      await updateRole(editingId, formData)
      ElMessage.success('更新成功')
    } else {
      await createRole(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchRoles()
  } catch { ElMessage.error('操作失败') }
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateRoleStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchRoles()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除角色 ${row.roleName} 吗？`, '删除确认', { type: 'warning' })
  try {
    await deleteRoleApi(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch { ElMessage.error('删除失败') }
}

async function handleAssignPermissions(row: any) {
  currentRole.value = row
  permDialogVisible.value = true
  permLoading.value = true
  try {
    const [permRes, rolePermRes] = await Promise.all([
      getAllPermissions(),
      getRolePermissions(row.id)
    ])
    allPermissions.value = ((permRes as any).data || permRes) as any[]
    selectedPermissions.value = ((rolePermRes as any).data || rolePermRes) as number[]
  } catch { ElMessage.error('获取权限数据失败') }
  permLoading.value = false
}

async function handleSubmitPermissions() {
  if (!currentRole.value) return
  try {
    await assignRolePermissions(currentRole.value.id, selectedPermissions.value)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch { ElMessage.error('权限分配失败') }
}

onMounted(fetchRoles)
</script>
