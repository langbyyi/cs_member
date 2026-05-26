<template>
  <div class="settings">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>


    <el-card class="settings-card" :body-style="{ padding: '0px' }">
      <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
        <!-- 动态渲染配置标签页 -->
        <el-tab-pane v-for="(configs, type) in configGroups" :key="type" :label="getTabLabel(type)" :name="type">
          <div class="tab-content">
            <div class="tab-header">
              <h3>{{ getTabLabel(type) }}</h3>
              <el-button type="primary" @click="handleSave(type)" :loading="saving[type]">保存配置</el-button>
            </div>
            
            <el-form label-width="180px" label-position="left" class="config-form">
              <div v-for="config in configs" :key="config.configKey" class="config-item">
                <el-form-item :label="config.configName">
                  <template #label>
                    <div class="config-label">
                      <span>{{ config.configName }}</span>
                      <el-tooltip v-if="config.description" :content="config.description" placement="top">
                        <el-icon class="help-icon"><QuestionFilled /></el-icon>
                      </el-tooltip>
                    </div>
                  </template>

                  <!-- 字符串类型 -->
                  <template v-if="config.dataType === 'string'">
                    <!-- 图片上传 (Logo) -->
                    <div v-if="config.configKey === 'site.logo'" class="logo-upload-container">
                      <el-upload
                        class="logo-uploader"
                        action="/api/v1/common/upload"
                        :show-file-list="false"
                        :on-success="(res) => handleLogoSuccess(res, config)"
                        :before-upload="beforeLogoUpload"
                        :headers="uploadHeaders"
                      >
                        <img v-if="config.configValue" :src="config.configValue" class="logo-image" />
                        <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                      </el-upload>
                      <div class="form-tip">点击图片更换，支持 JPG/PNG，不超过 2MB</div>
                    </div>
                    
                    <!-- 普通文本输入 -->
                    <el-input
                      v-else
                      v-model="config.configValue"
                      :placeholder="config.description"
                      class="config-input"
                    />
                  </template>
                  
                  <!-- 数字类型 -->
                  <el-input-number
                    v-else-if="config.dataType === 'number'"
                    v-model="config.configValue"
                    :min="0"
                    class="config-input-number"
                  />
                  
                  <!-- 布尔类型 -->
                  <el-switch
                    v-else-if="config.dataType === 'boolean'"
                    v-model="config.configValue"
                    active-text="开启"
                    inactive-text="关闭"
                    inline-prompt
                  />
                  
                  <!-- JSON类型 -->
                  <el-input
                    v-else-if="config.dataType === 'json'"
                    v-model="config.configValue"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入JSON格式配置"
                    class="config-input"
                  />
                </el-form-item>
              </div>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 会员等级配置单独处理 -->
        <el-tab-pane label="会员等级" name="member-level">
          <div class="tab-content">
            <div class="tab-header">
              <h3>会员等级配置</h3>
              <el-button type="primary" @click="handleCreateGrade">
                <el-icon><Plus /></el-icon>新增等级
              </el-button>
            </div>
            
            <el-table :data="memberLevels" style="width: 100%" v-loading="memberLevelsLoading" row-key="id">
              <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
              <el-table-column prop="gradeName" label="等级名称" width="120">
                <template #default="{ row }">
                  <div class="grade-name-cell">
                    <div class="grade-color-dot" :style="{ backgroundColor: row.color || '#ccc' }"></div>
                    {{ row.gradeName }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="gradeCode" label="编码" width="120" />
              <el-table-column label="积分范围" width="180">
                <template #default="{ row }">
                  {{ row.minPoints }} - {{ row.maxPoints || '∞' }}
                </template>
              </el-table-column>
              <el-table-column prop="pointsMultiplier" label="积分倍率" width="100">
                <template #default="{ row }">{{ row.pointsMultiplier }}x</template>
              </el-table-column>
              <el-table-column prop="discountRate" label="折扣率" width="100">
                <template #default="{ row }">{{ row.discountRate / 10 }}折</template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="handleEditGrade(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteGrade(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 会员等级编辑对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      :title="gradeForm.id ? '编辑会员等级' : '新增会员等级'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="gradeFormRef" :model="gradeForm" :rules="gradeRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级名称" prop="gradeName">
              <el-input v-model="gradeForm.gradeName" placeholder="如：普通会员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级编码" prop="gradeCode">
              <el-input v-model="gradeForm.gradeCode" placeholder="如：NORMAL" :disabled="!!gradeForm.id" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低积分" prop="minPoints">
              <el-input-number v-model="gradeForm.minPoints" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高积分" prop="maxPoints">
              <el-input-number v-model="gradeForm.maxPoints" :min="0" placeholder="留空表示无上限" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="积分倍率" prop="pointsMultiplier">
              <el-input-number v-model="gradeForm.pointsMultiplier" :precision="2" :step="0.1" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折扣率(%)" prop="discountRate">
              <el-input-number v-model="gradeForm.discountRate" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级颜色" prop="color">
              <el-color-picker v-model="gradeForm.color" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="gradeForm.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-switch v-model="gradeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGradeForm" :loading="gradeSubmitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, QuestionFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getAllConfigs,
  getSortedMemberGrades,
  batchSaveConfigs,
  saveConfig,
  createMemberGrade,
  updateMemberGrade,
  deleteMemberGrade,
  type SystemConfig,
  type MemberGradeConfig
} from '@/api/system-config'

const userStore = useUserStore()
const activeTab = ref('system')
const configGroups = ref<Record<string, SystemConfig[]>>({})
const memberLevels = ref<MemberGradeConfig[]>([])
const memberLevelsLoading = ref(false)
const saving = ref<Record<string, boolean>>({})

// 会员等级表单相关
const gradeDialogVisible = ref(false)
const gradeSubmitting = ref(false)
const gradeFormRef = ref<FormInstance>()
const gradeForm = reactive<Partial<MemberGradeConfig>>({
  status: 1,
  pointsMultiplier: 1.0,
  discountRate: 100,
  sortOrder: 0
})

const gradeRules: FormRules = {
  gradeName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  gradeCode: [{ required: true, message: '请输入等级编码', trigger: 'blur' }],
  minPoints: [{ required: true, message: '请输入最低积分', trigger: 'blur' }],
  pointsMultiplier: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }]
}

// 上传请求头
const uploadHeaders = computed(() => {
  const token = userStore.token
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
})

const tabLabels: Record<string, string> = {
  system: '系统设置',
  business: '业务配置',
  security: '安全设置',
  notification: '通知设置'
}

const getTabLabel = (type: string) => tabLabels[type] || type

// Logo上传前校验
const beforeLogoUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('Logo图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Logo图片大小不能超过 2MB!')
    return false
  }
  return true
}

// Logo上传成功回调
const handleLogoSuccess = async (response: any, config: SystemConfig) => {
  if (response.code === 200) {
    config.configValue = response.data.url
    
    // 立即保存配置
    try {
      const { data: success } = await saveConfig(config.configKey, config.configValue)
      if (success) {
        ElMessage.success('Logo上传并保存成功')
      } else {
        ElMessage.warning('Logo上传成功但保存配置失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '保存Logo配置失败')
    }
  } else {
    ElMessage.error('Logo上传失败: ' + response.message)
  }
}

// 加载所有配置
const loadAllConfigs = async () => {
  const loading = ElLoading.service({ text: '加载配置中...', target: '.settings-card' })
  try {
    const { data } = await getAllConfigs()

    const groups: Record<string, SystemConfig[]> = {}
    
    data.forEach(config => {
      // 确保类型转换
      if (config.dataType === 'boolean') {
        config.configValue = config.configValue === 'true' || config.configValue === '1'
      } else if (config.dataType === 'number') {
        config.configValue = Number(config.configValue)
      }

      const type = config.configType || 'other'
      if (!groups[type]) {
        groups[type] = []
      }
      groups[type].push(config)
    })
    
    // 组内排序
    for (const key in groups) {
      groups[key].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    }

    configGroups.value = groups

  } catch (error: any) {
    ElMessage.error(error.message || '加载配置失败')
  } finally {
    loading.close()
  }
}

// 加载会员等级
const loadMemberGrades = async () => {
  memberLevelsLoading.value = true
  try {
    const { data } = await getSortedMemberGrades()
    memberLevels.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载会员等级失败')
  } finally {
    memberLevelsLoading.value = false
  }
}

// 保存配置
const handleSave = async (type: string) => {
  const configs = configGroups.value[type]
  if (!configs) return

  saving.value[type] = true
  try {
    const configMap: Record<string, string> = {}
    configs.forEach(c => {
      configMap[c.configKey] = String(c.configValue)
    })

    const { data: success } = await batchSaveConfigs(configMap)
    if (success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value[type] = false
  }
}

// 会员等级操作
const handleCreateGrade = () => {
  Object.assign(gradeForm, {
    id: undefined,
    gradeName: '',
    gradeCode: '',
    minPoints: 0,
    maxPoints: undefined,
    pointsMultiplier: 1.0,
    discountRate: 100,
    color: '#409EFF',
    sortOrder: 0,
    status: 1
  })
  gradeDialogVisible.value = true
}

const handleEditGrade = (row: MemberGradeConfig) => {
  Object.assign(gradeForm, { ...row })
  // 处理maxPoints为null的情况
  if (gradeForm.maxPoints === null) {
    gradeForm.maxPoints = undefined
  }
  gradeDialogVisible.value = true
}

const handleDeleteGrade = (row: MemberGradeConfig) => {
  ElMessageBox.confirm(
    `确定要删除会员等级"${row.gradeName}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const { data: success } = await deleteMemberGrade(row.id!)
      if (success) {
        ElMessage.success('删除成功')
        loadMemberGrades()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

const submitGradeForm = async () => {
  if (!gradeFormRef.value) return
  
  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      gradeSubmitting.value = true
      try {
        const isEdit = !!gradeForm.id
        const api = isEdit ? updateMemberGrade : createMemberGrade
        
        // 处理maxPoints，如果未定义或为0，设为null（后端可能需要处理）
        // 这里根据后端逻辑，如果后端支持null，则传null
        const submitData = { ...gradeForm }
        if (!submitData.maxPoints) {
          submitData.maxPoints = undefined // 或者 null，视后端接口而定
        }

        const { data: success } = await api(submitData)
        
        if (success) {
          ElMessage.success(isEdit ? '更新成功' : '创建成功')
          gradeDialogVisible.value = false
          loadMemberGrades()
        } else {
          ElMessage.error(isEdit ? '更新失败' : '创建失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '提交失败')
      } finally {
        gradeSubmitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadAllConfigs()
  loadMemberGrades()
})
</script>

<style scoped>
.settings {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f2f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 24px;
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

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2f3d;
  margin: 0;
}

.settings-card {
  min-height: 600px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.settings-tabs :deep(.el-tabs__header) {
  margin-right: 0;
  background-color: transparent;
  border-right: 1px solid #e4e7ed;
  padding-top: 20px;
  width: 200px;
}

.settings-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
  color: #606266;
  padding: 0 24px !important;
  text-align: left;
  justify-content: flex-start;
  margin-right: -1px;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  background-color: #e6f7ff;
  color: var(--el-color-primary);
  border-right: 2px solid var(--el-color-primary);
  font-weight: 500;
}

.tab-content {
  padding: 24px 40px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.config-form {
  max-width: 800px;
}

.config-item {
  margin-bottom: 24px;
}

.config-label {
  display: flex;
  align-items: center;
  color: #606266;
}

.help-icon {
  margin-left: 6px;
  color: #909399;
  cursor: help;
  font-size: 14px;
}

.config-input {
  width: 400px;
}

.config-input-number {
  width: 200px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 6px;
}

/* Logo上传样式 */
.logo-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  background-color: #fafafa;
}

.logo-uploader:hover {
  border-color: var(--el-color-primary);
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-image {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: contain;
  background-color: #fff;
}

/* 会员等级样式 */
.grade-name-cell {
  display: flex;
  align-items: center;
}

.grade-color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}
</style>