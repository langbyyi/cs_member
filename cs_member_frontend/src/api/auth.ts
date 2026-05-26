import request from '@/utils/request'
import type {
  LoginRequest,
  LoginResponse,
  PhoneCheckRequest,
  EmailVerifyRequest,
  PhoneVerifyRequest,
  PasswordResetRequest,
  SecurePasswordResetResponse
} from '@/types'

// 会员登录
export function memberLogin(data: LoginRequest) {
  return request.post<LoginResponse>('/member/login', data)
}

// 管理员登录
export function adminLogin(data: LoginRequest) {
  return request.post<LoginResponse>('/admin/login', data)
}


// 会员注册
export function memberRegister(data: {
  name: string
  phone: string
  email: string
  password: string
  gender: number
}) {
  return request.post('/member/register', data)
}

// 发送邮箱验证码
export function sendEmailCode(data: {
  userType: string
  email: string
  verificationType: string
}) {
  return request.post(`/${data.userType}/email-verification/send-code`, data)
}

// 验证邮箱验证码
export function verifyEmailCode(data: {
  userType: string
  email: string
  verificationType: string
  verificationCode: string
}) {
  return request.post(`/${data.userType}/email-verification/verify-code`, data)
}

// 刷新Token
export function refreshToken(refreshToken: string) {
  return request.post<{ token: string; expiresIn: number }>('/member/refresh', {
    refreshToken
  })
}

// 用户登出（保留兼容性，根据用户类型自动选择）
export function logout(userType: 'member' | 'admin' = 'member') {
  return request.post(`/${userType}/logout`)
}

// 会员登出
export function memberLogout() {
  return request.post('/member/logout')
}

// 管理员登出
export function adminLogout() {
  return request.post('/admin/logout')
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get<{
    id: number
    username: string
    realName: string
    roles: string[]
    permissions: string[]
  }>('/member/current-user')
}

// 获取会员当前详细信息（个人信息页面使用）
export function getCurrentMemberInfo() {
  return request.get('/member/profile/current')
}

// 获取会员当前用户详细信息
export function getMemberCurrentInfo() {
  return request.get<{
    id: number
    memberNo: string
    name: string
    phone: string
    email: string
    gender: number
    birthday: string
    address: string
    memberLevel: number
    status: number
    currentPoints: number
    totalPoints: number
    balance: number
    totalConsumption: number
    registerStoreId: number
    createdTime: string
    lastConsumptionTime: string
  }>('/member/profile/current')
}

// 获取会员等级配置列表
export function getMemberGradeList() {
  return request.get<Array<{
    id: number
    name: string
    level: number
    minPoints: number
    maxPoints: number
    discount: number
    icon: string
    color: string
    description: string
  }>>('/member/grade-config/list')
}

// 发送密码重置验证码
export function sendResetCode(identifier: string) {
  return request.post('/member/forgot-password/send-code', {
    identifier
  })
}

// 验证密码重置验证码
export function verifyResetCode(phone: string, verifyCode: string) {
  return request.post('/member/forgot-password/verify-code', {
    phone,
    verifyCode
  })
}

// 重置密码
export function resetPassword(phone: string, newPassword: string, verifyCode: string) {
  return request.post('/member/forgot-password/reset', {
    phone,
    newPassword,
    verifyCode
  })
}


// === 分步骤密码重置流程 ===

// 步骤1：校验手机号格式
export function checkPhoneForReset(phone: string) {
  return request.post<SecurePasswordResetResponse>('/member/forgot-password/validate-phone', {
    phone
  })
}

// 步骤2：发送重置密码验证码
export function verifyEmailAndSendCode(phone: string, email: string, resetToken: string, ip: string = '127.0.0.1') {
  return request.post<SecurePasswordResetResponse>('/member/forgot-password/send-code', {
    phone,
    email,
    ip
  })
}

// 步骤3：验证验证码并生成重置令牌
export function verifyResetCodeStep3(phone: string, verifyCode: string, resetToken: string) {
  return request.post<SecurePasswordResetResponse>('/member/forgot-password/verify-code', {
    phone,
    email: '', // 需要从form中获取
    verificationCode: verifyCode,
    resetToken
  })
}

// 步骤4：重置密码
export function resetPasswordWithCode(phone: string, newPassword: string, verifyCode: string, resetToken: string) {
  return request.post<SecurePasswordResetResponse>('/member/forgot-password/reset', {
    resetToken,
    newPassword,
    confirmPassword: newPassword
  })
}

// === 新增API接口 ===

// 会员校验手机号格式
export function validateMemberPhoneFormat(data: { phone: string }) {
  return request.post<{ success: boolean }>('/member/forgot-password/validate-phone', data)
}

// 管理员校验手机号格式
export function validateAdminPhoneFormat(data: { phone: string }) {
  return request.post<{ success: boolean }>('/admin/forgot-password/validate-phone', data)
}

// 会员发送重置密码验证码
export function sendMemberResetPasswordCode(data: {
  phone: string
  email: string
}) {
  return request.post<string>('/member/forgot-password/send-code', data)
}

// 管理员发送重置密码验证码
export function sendAdminResetPasswordCode(data: {
  phone: string
  email: string
}) {
  return request.post<string>('/admin/forgot-password/send-code', data)
}

// 会员验证验证码并生成重置令牌
export function verifyMemberResetCodeAndGenerateToken(data: {
  phone: string
  email: string
  emailCode: string
}) {
  return request.post<{ resetToken: string; expiresAt: string }>('/member/forgot-password/verify-code', data)
}

// 管理员验证验证码并生成重置令牌
export function verifyAdminResetCodeAndGenerateToken(data: {
  phone: string
  email: string
  emailCode: string
}) {
  return request.post<{ resetToken: string; expiresAt: string }>('/admin/forgot-password/verify-code', data)
}

// 重置密码（通用）
export function resetPasswordFinal(data: {
  resetToken: string
  newPassword: string
  confirmPassword: string
}) {
  return request.post<string>('/member/forgot-password/reset', data)
}

// 验证重置令牌（通用）
export function validateResetToken(resetToken: string) {
  return request.post<{ valid: boolean }>('/member/forgot-password/validate-token', {
    resetToken
  })
}

// === 新增：验证手机号绑定邮箱状态 ===

// 检查手机号是否已绑定邮箱（用于用户注册后绑定邮箱或忘记密码）
export function checkPhoneEmailBinding(phone: string) {
  return request.post('/member/check-phone-email-binding', {
    phone
  })
}

// 发送邮箱绑定验证码
export function sendEmailBindingCode(phone: string, email: string) {
  return request.post('/member/send-email-binding-code', {
    phone,
    email
  })
}

// 验证邮箱绑定验证码
export function verifyEmailBindingCode(phone: string, email: string, verifyCode: string) {
  return request.post('/member/verify-email-binding-code', {
    phone,
    email,
    verifyCode
  })
}

