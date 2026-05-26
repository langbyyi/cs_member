// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
  errors?: Array<{
    field: string
    message: string
  }>
}

// 分页响应类型 (PageHelper 格式)
export interface PageResponse<T = any> {
  list: T[]  // PageHelper 返回的是 list
  total: number
  pageSize: number
  pageNum: number
  pages: number
}

// 分页请求参数
export interface PageParams {
  pageNum?: number
  pageSize?: number
}

// 用户相关类型
export interface User {
  id: number
  username: string
  realName: string
  phone: string
  email?: string
  roleId: number
  roleName: string
  storeId?: number
  storeName?: string
  status: number
  lastLoginTime?: string
  createTime: string
}

export interface LoginRequest {
  phone: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  username: string
  realName: string
  storeId?: number
  storeName?: string
  roleId: number
  roleName: string
}

export interface CreateUserRequest {
  username: string
  password: string
  realName: string
  phone: string
  email?: string
  roleId: number
  storeId?: number
}

export interface UpdateUserRequest {
  realName?: string
  email?: string
  phone?: string
  roleId?: number
  storeId?: number
}

// 角色相关类型
export interface Role {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime: string
  userCount?: number
}

export interface CreateRoleRequest {
  roleName: string
  roleCode: string
  description?: string
}

// 门店相关类型
export interface Store {
  id: number
  storeNo: string
  storeName: string
  storeType: string
  province: string
  city: string
  district: string
  address: string
  contactPhone: string
  managerId?: number | null
  employeeCount: number
  memberCount: number
  status: number
  createTime?: string
}

export interface CreateStoreRequest {
  storeNo: string
  storeName: string
  address: string
  phone: string
  managerId?: number | null
  businessHours?: string
}

export interface UpdateStoreRequest {
  storeName?: string
  address?: string
  phone?: string
  managerId?: number | null
  businessHours?: string
}

// 会员相关类型
export interface Member {
  id: number
  memberNo: string
  name: string
  phone: string
  gender: number
  birthday?: string
  email?: string
  address?: string
  memberLevel: number
  memberLevelName?: string
  currentPoints: number
  totalPoints: number
  balance: number
  totalConsumption: number
  consumptionCount?: number
  registerStoreId?: number
  registerStoreName?: string
  status: number
  createdTime: string
  updatedTime?: string
  lastConsumptionTime?: string
  lastLoginTime?: string
  lastLoginIp?: string
  loginCount?: number
}

export interface CreateMemberRequest {
  name: string
  phone: string
  gender: number
  birthday?: string
  email?: string
  address?: string
}

export interface UpdateMemberRequest {
  name?: string
  email?: string
  memberLevel?: number
  address?: string
}

export interface UpdateConsumptionRequest {
  amount?: number
  discountAmount?: number
  actualAmount?: number
  pointsUsed?: number
  couponId?: number
  paymentMethod?: number
  remark?: string
}

// 消费记录相关类型
export interface ConsumptionRecord {
  id: number
  consumptionNo: string
  memberId: number
  memberNo: string
  memberName: string
  memberPhone: string
  storeId: number
  storeName: string
  storeNo: string
  amount: number
  discountAmount: number
  actualAmount: number
  pointsEarned: number
  pointsUsed: number
  couponId?: number
  couponName?: string
  paymentMethod: number
  paymentMethodName: string
  status: number
  operatorId: number
  operatorName: string
  consumptionTime: string
  remark?: string
}

export interface CreateConsumptionRequest {
  memberId: number
  storeId: number
  amount: number
  discountAmount?: number
  actualAmount: number
  pointsUsed?: number
  couponId?: number
  paymentMethod: number
  operatorId: number
  remark?: string
}

// 优惠券相关类型
export interface Coupon {
  id: number
  couponName: string
  couponType: number
  couponTypeName: string
  discountAmount: number
  minAmount?: number
  totalQuantity: number
  issuedQuantity: number
  usedQuantity: number
  remainingQuantity: number
  validStartTime: string
  validEndTime: string
  description?: string
  status: number
  createTime: string
}

export interface CreateCouponRequest {
  couponName: string
  couponType: number
  discountAmount: number
  minAmount?: number
  totalQuantity: number
  validStartTime: string
  validEndTime: string
  description?: string
}

export interface UpdateCouponRequest {
  couponName?: string
  couponType?: number
  discountAmount?: number
  minAmount?: number
  totalQuantity?: number
  validStartTime?: string
  validEndTime?: string
  description?: string
}

// 会员优惠券相关类型
export interface MemberCoupon {
  id: number
  memberId: number
  couponId: number
  couponNo: string
  couponName: string
  couponType: number
  couponTypeName: string
  discountAmount: number
  minAmount?: number
  validStartTime: string
  validEndTime: string
  receiveTime: string
  useTime?: string
  orderNo?: string
  status: number
  statusName: string
  available: boolean
  remainingDays?: number
}

// 积分记录相关类型
export interface PointsRecord {
  id: number
  memberId: number
  memberNo: string
  memberName: string
  pointsType: number
  pointsTypeName: string
  points: number
  beforePoints: number
  afterPoints: number
  description: string
  orderId?: string
  createTime: string
}

export interface AdjustPointsRequest {
  memberId: number
  points: number
  pointsType: number
  description: string
}

export interface CreatePointsRequest {
  memberId: number
  points: number
  pointsType: number
  description: string
  orderId?: string
}

// 统计相关类型
export interface MemberStatistics {
  totalMembers: number
  newMembers: number
  activeMembers: number
  memberLevels: Record<string, number>
  trend: {
    dates: string[]
    newCounts: number[]
    activeCounts: number[]
  }
}

export interface SalesStatistics {
  totalAmount: number
  totalOrders: number
  avgOrderAmount: number
  growthRate: number
  trend: {
    dates: string[]
    amounts: number[]
    orders: number[]
  }
}

// 操作日志类型
export interface OperationLog {
  id: number
  userId: number
  username: string
  realName: string
  module: string
  operation: string
  description: string
  method: string
  url: string
  ip: string
  userAgent: string
  params: string
  result: string
  status: number
  createTime: string
}

// 系统配置类型
export interface SystemConfig {
  systemName: string
  version: string
  companyName: string
  logo?: string
  footer: string
  theme: string
  pageSize: number
  sessionTimeout: number
  pointsRate: number
  memberUpgradeRules: {
    silver: { minConsumption: number; minPoints: number }
    gold: { minConsumption: number; minPoints: number }
    diamond: { minConsumption: number; minPoints: number }
  }
}

// 数据字典类型
export interface DictItem {
  id: number
  dictLabel: string
  dictValue: string
  dictSort: number
  status: number
  remark?: string
}

export interface DictType {
  id: number
  dictName: string
  dictCode: string
  status: number
  remark?: string
  createTime: string
}

// 消息通知类型
export interface Notification {
  id: number
  title: string
  content: string
  type: number
  typeName: string
  status: number
  createTime: string
  readTime?: string
}

// 菜单相关类型
export interface MenuItem {
  id: number
  name: string
  path: string
  component?: string
  icon?: string
  sort: number
  parentId?: number
  children?: MenuItem[]
  permissions?: string[]
}

// 密码重置相关类型
export interface PhoneCheckRequest {
  phone: string
}

export interface EmailVerifyRequest {
  phone: string
  email: string
  ip: string
  resetToken: string
}

export interface PhoneVerifyRequest {
  phone: string
  verifyCode: string
  resetToken: string
}

export interface PasswordResetRequest {
  phone: string
  newPassword: string
  verifyCode: string
  resetToken: string
}

export interface SecurePasswordResetResponse {
  resetToken?: string
}

// 会员等级配置类型
export interface MemberGradeConfig {
  id: number
  gradeCode: string
  gradeName: string
  minPoints: number
  maxPoints: number | null
  pointsMultiplier: number
  discountRate: number
  color: string
  sortOrder: number
  status: number
  createdTime?: string
  updatedTime?: string
}

// 会员等级信息（从后端返回的简化版本）
export interface MemberGradeInfo {
  gradeCode: string
  gradeName: string
  color: string
  pointsMultiplier: number
  discountRate: number
  totalPoints: number
}

// 会员信息响应（包含等级信息）
export interface MemberInfoWithGrade {
  member: Member
  gradeInfo: MemberGradeInfo
}

// 会员信息响应（包含等级配置表）
export interface MemberInfoWithGradeConfigs {
  member: Member
  gradeConfigs: MemberGradeConfig[]
}
