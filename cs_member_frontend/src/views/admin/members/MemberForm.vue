<template>
  <div class="member-form" v-loading="loading">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <el-button
            link
            @click="$router.back()"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>{{ isEdit ? '编辑会员' : '新增会员' }}</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
        class="member-form"
        :disabled="submitting"
      >
        <!-- 编辑时显示会员编号和手机号 -->
        <el-row :gutter="20" v-if="isEdit">
            <el-col :span="12">
              <el-form-item label="会员编号" prop="memberNo">
                <el-input v-model="form.memberNo" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="form.phone" disabled />
              </el-form-item>
            </el-col>
          </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入姓名"
                clearable
              />
            </el-form-item>

            <el-form-item label="手机号码" prop="phone" v-if="!isEdit">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号码"
                clearable
                maxlength="11"
                show-word-limit
                @input="form.phone = form.phone.replace(/\D/g, '')"
              />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="请选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="邮箱地址" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱地址"
                clearable
              />
            </el-form-item>

            <el-form-item label="注册门店" prop="registerStoreId">
              <el-select
                v-model="form.registerStoreId"
                placeholder="请选择注册门店"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="store in storeList"
                  :key="store.id"
                  :label="store.storeName"
                  :value="store.id"
                >
                  <span style="float: left">{{ store.storeName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">{{ store.storeNo }}</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="会员等级" prop="memberLevel" v-if="isEdit">
              <el-select v-model="form.memberLevel" placeholder="请选择会员等级">
                <el-option label="普通会员" :value="1" />
                <el-option label="银卡会员" :value="2" />
                <el-option label="金卡会员" :value="3" />
                <el-option label="钻石会员" :value="4" />
              </el-select>
            </el-form-item>

            <el-form-item label="状态" prop="status" v-if="isEdit">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="禁用" :value="0" />
                <el-option label="正常" :value="1" />
                <el-option label="冻结" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 会员统计信息（仅编辑时显示） -->
        <el-divider v-if="isEdit && memberData" />
        <div v-if="isEdit && memberData" class="member-stats">
          <h3>会员统计信息</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">会员编号</div>
                <div class="stat-value member-no">{{ memberData.memberNo }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">手机号码</div>
                <div class="stat-value phone">{{ memberData.phone }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">当前积分</div>
                <div class="stat-value">{{ memberData.currentPoints }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">累计消费</div>
                <div class="stat-value money">¥{{ memberData.totalConsumption }}</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 16px;">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">累计积分</div>
                <div class="stat-value">{{ memberData.totalPoints }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">消费次数</div>
                <div class="stat-value">{{ memberData.consumptionCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">注册时间</div>
                <div class="stat-value">{{ formatDateTime(memberData.createdTime) }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">最后消费</div>
                <div class="stat-value">
                  {{ memberData.lastConsumptionTime ? formatDateTime(memberData.lastConsumptionTime) : '暂无' }}
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 16px;">
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-label">账户余额</div>
                <div class="stat-value money">¥{{ memberData.balance }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-label">注册门店</div>
                <div class="stat-value">{{ memberData.registerStoreName || '自主注册' }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 操作按钮 -->
        <el-divider />
        <div class="form-actions">
          <el-button size="large" @click="$router.back()">
            取消
          </el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '创建会员') }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createMember, updateMember, getMemberDetail, type Member } from '@/api/member'
import { getOpenStores, type Store } from '@/api/store'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)
const memberData = ref<Member | null>(null)
const storeList = ref<Store[]>([])

// 表单数据
const form = reactive({
  memberNo: '',
  name: '',
  phone: '',
  gender: 1, // 默认男
  birthday: '',
  email: '',
  registerStoreId: null,
  memberLevel: 1,
  status: 1
})

// 表单验证规则
const formRules = computed((): FormRules => {
  const rules: FormRules = {
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 50, message: '姓名在2-50个字符之间', trigger: 'blur' }
    ],
    gender: [],
    birthday: [],
    registerStoreId: []
  }

  // 根据模式设置不同的验证规则
  if (isEdit.value) {
    // 编辑模式：手机号只验证格式，邮箱可选
    rules.phone = [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号码', trigger: 'blur' }
    ]
    rules.email = [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  } else {
    // 新增模式：手机号和邮箱都必填
    rules.phone = [
      { required: true, message: '请输入手机号码', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号码', trigger: 'blur' }
    ]
    rules.email = [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }

  return rules
})



// 格式化日期时间
function formatDateTime(date: string) {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取门店列表
async function fetchStoreList() {
  try {
    const response = await getOpenStores()
    storeList.value = response.data || response
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  }
}

// 获取会员详情
async function fetchMemberDetail() {
  try {
    loading.value = true
    const memberId = Number(route.params.id)
    const response = await getMemberDetail(memberId)

    // 处理API响应数据
    memberData.value = response.data || response

    // 调试：打印API响应数据

    // 填充表单
    Object.assign(form, {
      memberNo: memberData.value.memberNo || '',
      name: memberData.value.name || '',
      phone: memberData.value.phone || '',
      gender: memberData.value.gender || 1,
      birthday: memberData.value.birthday || '',
      email: memberData.value.email || '',
      registerStoreId: memberData.value.registerStoreId || null,
      memberLevel: memberData.value.memberLevel || 1,
      status: memberData.value.status || 1
    })

    // 调试：打印表单数据
  } catch (error) {
    ElMessage.error('获取会员详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    if (isEdit.value) {
      // 编辑会员 - 邮箱和手机号不需要必填验证
      const memberId = Number(route.params.id)
      const updateData: any = {
        name: form.name,
        memberLevel: form.memberLevel,
        status: form.status,
        gender: form.gender,
        birthday: form.birthday
      }

      // 邮箱可选，如果填写了则包含在更新数据中
      if (form.email) {
        updateData.email = form.email
      }
      if (form.registerStoreId) {
        updateData.registerStoreId = form.registerStoreId
      }

      await updateMember(memberId, updateData)
      ElMessage.success('会员信息更新成功')
    } else {
      // 创建会员 - 只需要三个必要字段
      const createData: any = {
        name: form.name,
        phone: form.phone,
        email: form.email
      }

      // 可选字段：如果填写了则包含在创建数据中
      if (form.gender !== 0) { // 性别默认为1（男），只有当不为0时才提交
        createData.gender = form.gender
      }
      if (form.birthday) {
        createData.birthday = form.birthday
      }
      if (form.registerStoreId) {
        createData.registerStoreId = form.registerStoreId
      }

      await createMember(createData)
      ElMessage.success('会员创建成功')
    }

    router.back()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchStoreList()
  if (isEdit.value) {
    fetchMemberDetail()
  }
})
</script>

<style scoped>
.member-form {
  padding: 20px;
}

.form-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.back-btn {
  font-size: 16px;
  padding: 8px 12px;
  color: #606266;
}

.back-btn:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.member-form {
  max-width: 1000px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.member-stats {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.member-stats h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-value.money {
  color: #67c23a;
}

.stat-value.member-no {
  color: #409eff;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', monospace;
  font-size: 18px;
}

.stat-value.phone {
  color: #e6a23c;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', monospace;
  font-size: 16px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}

.form-actions .el-button {
  min-width: 120px;
  height: 40px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .member-form {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .member-form :deep(.el-col) {
    margin-bottom: 0;
  }

  .member-stats .el-col {
    margin-bottom: 16px;
  }

  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 18px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }
}

/* 表单样式增强 */
.member-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.member-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.member-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.member-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.member-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: vertical;
}

.member-form :deep(.el-radio) {
  margin-right: 20px;
}

.member-form :deep(.el-radio__label) {
  font-weight: 500;
}

.member-form :deep(.el-switch) {
  height: 22px;
}

.member-form :deep(.el-divider) {
  margin: 24px 0;
}

/* 加载状态 */
.member-form[disabled] {
  opacity: 0.6;
  pointer-events: none;
}

/* 动画效果 */
.member-form :deep(.el-form-item) {
  transition: all 0.3s ease;
}

.member-form :deep(.el-form-item:hover) {
  transform: translateY(-1px);
}
</style>