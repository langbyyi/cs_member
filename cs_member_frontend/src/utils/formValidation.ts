/**
 * 表单验证工具类
 * 提供实时验证、错误提示、智能建议等功能
 */

// 验证结果接口
export interface ValidationResult {
  isValid: boolean
  message: string
  suggestion?: string
  strength?: number
  level?: 'weak' | 'medium' | 'strong'
}

// 字段类型
export type FieldType = 'phone' | 'email' | 'password' | 'verifyCode' | 'confirmPassword'

// 验证规则配置
export interface ValidationRule {
  required?: boolean
  minLength?: number
  maxLength?: number
  pattern?: RegExp
  customValidator?: (value: string) => ValidationResult
  relatedField?: string
}

// 表单验证状态管理
export interface ValidationState {
  [fieldName: string]: {
    isValid: boolean
    isDirty: boolean
    errors: string[]
    suggestions: string[]
    lastValidated: number
  }
}

export class FormValidator {
  private validationState: ValidationState = {}
  private fieldRules: Record<string, ValidationRule[]> = {}
  private formData: Record<string, any> = {}

  constructor() {
    this.initializeDefaultRules()
  }

  /**
   * 初始化默认验证规则
   */
  private initializeDefaultRules(): void {
    this.fieldRules = {
      phone: [
        {
          required: true,
          customValidator: this.validatePhone.bind(this)
        }
      ],
      email: [
        {
          required: true,
          customValidator: this.validateEmail.bind(this)
        }
      ],
      verifyCode: [
        {
          required: true,
          minLength: 6,
          maxLength: 6,
          pattern: /^\d{6}$/,
          customValidator: this.validateVerifyCode.bind(this)
        }
      ],
      newPassword: [
        {
          required: true,
          minLength: 6,
          maxLength: 20,
          customValidator: this.validatePasswordStrength.bind(this)
        }
      ],
      confirmPassword: [
        {
          required: true,
          customValidator: this.validatePasswordConfirm.bind(this),
          relatedField: 'newPassword'
        }
      ]
    }
  }

  /**
   * 更新表单数据
   */
  updateFormData(fieldName: string, value: any): void {
    this.formData[fieldName] = value

    // 初始化验证状态
    if (!this.validationState[fieldName]) {
      this.validationState[fieldName] = {
        isValid: false,
        isDirty: false,
        errors: [],
        suggestions: [],
        lastValidated: Date.now()
      }
    }

    // 标记字段已修改
    this.validationState[fieldName].isDirty = true
  }

  /**
   * 验证单个字段
   */
  validateField(fieldName: string, value: string, relatedData?: Record<string, any>): ValidationResult {
    const rules = this.fieldRules[fieldName]
    if (!rules) {
      return { isValid: true, message: '' }
    }

    // 更新表单数据
    this.updateFormData(fieldName, value)

    // 合并相关数据
    const validationData = { ...this.formData, ...relatedData }

    for (const rule of rules) {
      // 必填验证
      if (rule.required && !value?.trim()) {
        return {
          isValid: false,
          message: this.getRequiredMessage(fieldName),
          suggestion: this.getFieldSuggestion(fieldName)
        }
      }

      // 如果为空且非必填，跳过其他验证
      if (!value?.trim()) {
        return { isValid: true, message: '' }
      }

      // 长度验证
      if (rule.minLength && value.length < rule.minLength) {
        return {
          isValid: false,
          message: `${this.getFieldLabel(fieldName)}长度不能少于${rule.minLength}位`,
          suggestion: `请至少输入${rule.minLength}位字符`
        }
      }

      if (rule.maxLength && value.length > rule.maxLength) {
        return {
          isValid: false,
          message: `${this.getFieldLabel(fieldName)}长度不能超过${rule.maxLength}位`,
          suggestion: `请控制在${rule.maxLength}位字符以内`
        }
      }

      // 正则验证
      if (rule.pattern && !rule.pattern.test(value)) {
        return {
          isValid: false,
          message: this.getPatternMessage(fieldName),
          suggestion: this.getFieldSuggestion(fieldName)
        }
      }

      // 自定义验证器
      if (rule.customValidator) {
        let validatorValue = value
        if (rule.relatedField) {
          validatorValue = {
            value,
            relatedValue: validationData[rule.relatedField],
            allData: validationData
          }
        }
        const result = rule.customValidator(validatorValue as any)
        if (!result.isValid) {
          return result
        }
      }
    }

    // 验证通过
    this.validationState[fieldName].isValid = true
    this.validationState[fieldName].errors = []

    return { isValid: true, message: '验证通过' }
  }

  /**
   * 手机号验证
   */
  private validatePhone(phone: string): ValidationResult {
    const phoneRegex = /^1[3-9]\d{9}$/

    if (!phoneRegex.test(phone)) {
      return {
        isValid: false,
        message: '请输入正确的11位手机号码',
        suggestion: '手机号应以1开头，第二位为3-9，共11位数字'
      }
    }

    // 运营商检测
    const carrier = this.detectPhoneCarrier(phone)
    return {
      isValid: true,
      message: '手机号格式正确',
      suggestion: carrier ? `检测到${carrier}运营商` : undefined
    }
  }

  /**
   * 邮箱验证
   */
  private validateEmail(email: string): ValidationResult {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

    if (!emailRegex.test(email)) {
      return {
        isValid: false,
        message: '请输入正确的邮箱地址',
        suggestion: '邮箱格式示例：user@example.com'
      }
    }

    // 邮箱域名常见错误检测
    const commonMistakes = [
      { pattern: /@gmail\.cm/, suggestion: '应该是@gmail.com' },
      { pattern: /@163\.cm/, suggestion: '应该是@163.com' },
      { pattern: /@qq\.cm/, suggestion: '应该是@qq.com' },
      { pattern: /@126\.cm/, suggestion: '应该是@126.com' }
    ]

    for (const mistake of commonMistakes) {
      if (mistake.pattern.test(email)) {
        return {
          isValid: false,
          message: '邮箱地址可能有误',
          suggestion: mistake.suggestion
        }
      }
    }

    return { isValid: true, message: '邮箱格式正确' }
  }

  /**
   * 验证码验证
   */
  private validateVerifyCode(code: string): ValidationResult {
    const codeRegex = /^\d{6}$/

    if (!codeRegex.test(code)) {
      return {
        isValid: false,
        message: '验证码应为6位数字',
        suggestion: '请输入邮箱收到的6位数字验证码'
      }
    }

    return { isValid: true, message: '验证码格式正确' }
  }

  /**
   * 密码强度验证
   */
  private validatePasswordStrength(password: string): ValidationResult {
    if (password.length < 6) {
      return {
        isValid: false,
        message: '密码长度不能少于6位',
        suggestion: '建议使用8位以上的密码'
      }
    }

    const strength = this.calculatePasswordStrength(password)
    const { score, level, feedback } = strength

    if (score < 2) {
      return {
        isValid: true, // 允许弱密码，但给出建议
        message: '密码强度较弱',
        suggestion: feedback,
        strength: score,
        level
      }
    }

    return {
      isValid: true,
      message: level === 'strong' ? '密码强度很好' : '密码强度适中',
      suggestion: score < 4 ? feedback : undefined,
      strength: score,
      level
    }
  }

  /**
   * 确认密码验证
   */
  private validatePasswordConfirm(data: { value: string; relatedValue: string }): ValidationResult {
    const { value, relatedValue } = data

    if (value !== relatedValue) {
      return {
        isValid: false,
        message: '两次输入的密码不一致',
        suggestion: '请确保两次输入的密码完全相同'
      }
    }

    return { isValid: true, message: '密码确认一致' }
  }

  /**
   * 计算密码强度
   */
  private calculatePasswordStrength(password: string): {
    score: number
    level: 'weak' | 'medium' | 'strong'
    feedback: string
  } {
    let score = 0
    const feedback: string[] = []

    // 长度评分
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.length < 6) feedback.push('密码过短')

    // 字符类型评分
    if (/[a-z]/.test(password)) score++
    else feedback.push('缺少小写字母')

    if (/[A-Z]/.test(password)) score++
    else feedback.push('缺少大写字母')

    if (/\d/.test(password)) score++
    else feedback.push('缺少数字')

    if (/[^a-zA-Z0-9]/.test(password)) score++
    else feedback.push('缺少特殊字符')

    // 常见密码检测
    if (this.isCommonPassword(password)) {
      score = Math.max(1, score - 2)
      feedback.push('避免使用常见密码')
    }

    let level: 'weak' | 'medium' | 'strong'
    if (score <= 2) level = 'weak'
    else if (score <= 4) level = 'medium'
    else level = 'strong'

    return {
      score,
      level,
      feedback: feedback.slice(0, 2).join('，') || '密码强度良好'
    }
  }

  /**
   * 检测常见密码
   */
  private isCommonPassword(password: string): boolean {
    const commonPasswords = [
      '123456', '123456789', 'password', '12345678', 'qwerty',
      '123123', '111111', '1234567890', '1234567', 'sunshine',
      'qwerty123', 'iloveyou', 'princess', 'admin', 'welcome',
      '666666', 'abc123', 'football', '123123123', 'monkey',
      '654321', '!@#$%^&*'
    ]

    return commonPasswords.includes(password.toLowerCase())
  }

  /**
   * 检测手机号运营商
   */
  private detectPhoneCarrier(phone: string): string | null {
    const prefix = phone.substring(0, 3)

    const carriers: Record<string, string> = {
      '134': '中国移动', '135': '中国移动', '136': '中国移动', '137': '中国移动',
      '138': '中国移动', '139': '中国移动', '147': '中国移动', '150': '中国移动',
      '151': '中国移动', '152': '中国移动', '157': '中国移动', '158': '中国移动',
      '159': '中国移动', '172': '中国移动', '178': '中国移动', '182': '中国移动',
      '183': '中国移动', '184': '中国移动', '187': '中国移动', '188': '中国移动',
      '198': '中国移动',

      '130': '中国联通', '131': '中国联通', '132': '中国联通', '145': '中国联通',
      '155': '中国联通', '156': '中国联通', '166': '中国联通', '171': '中国联通',
      '175': '中国联通', '176': '中国联通', '185': '中国联通', '186': '中国联通',

      '133': '中国电信', '149': '中国电信', '153': '中国电信', '173': '中国电信',
      '177': '中国电信', '180': '中国电信', '181': '中国电信', '189': '中国电信',
      '191': '中国电信', '193': '中国电信', '199': '中国电信'
    }

    return carriers[prefix] || null
  }

  /**
   * 获取字段标签
   */
  private getFieldLabel(fieldName: string): string {
    const labels: Record<string, string> = {
      phone: '手机号',
      email: '邮箱地址',
      verifyCode: '验证码',
      newPassword: '密码',
      confirmPassword: '确认密码'
    }

    return labels[fieldName] || fieldName
  }

  /**
   * 获取必填提示信息
   */
  private getRequiredMessage(fieldName: string): string {
    return `请输入${this.getFieldLabel(fieldName)}`
  }

  /**
   * 获取正则验证提示信息
   */
  private getPatternMessage(fieldName: string): string {
    const messages: Record<string, string> = {
      phone: '请输入正确的11位手机号码',
      email: '请输入正确的邮箱地址',
      verifyCode: '验证码应为6位数字',
      newPassword: '密码格式不正确',
      confirmPassword: '密码确认不正确'
    }

    return messages[fieldName] || '格式不正确'
  }

  /**
   * 获取字段输入建议
   */
  private getFieldSuggestion(fieldName: string): string {
    const suggestions: Record<string, string> = {
      phone: '请输入以1开头的11位手机号码',
      email: '请输入类似 user@example.com 的邮箱地址',
      verifyCode: '请检查邮箱收到的6位数字验证码',
      newPassword: '建议使用字母、数字和特殊字符的组合',
      confirmPassword: '请再次输入密码确保一致'
    }

    return suggestions[fieldName] || '请检查输入内容'
  }

  /**
   * 获取字段验证状态
   */
  getFieldValidationState(fieldName: string): any {
    return this.validationState[fieldName] || {
      isValid: false,
      isDirty: false,
      errors: [],
      suggestions: [],
      lastValidated: 0
    }
  }

  /**
   * 获取表单整体验证状态
   */
  getFormValidationState(): {
    isValid: boolean
    isDirty: boolean
    errors: string[]
    completionRate: number
  } {
    const fields = Object.keys(this.validationState)
    if (fields.length === 0) {
      return { isValid: true, isDirty: false, errors: [], completionRate: 0 }
    }

    const validFields = fields.filter(field => this.validationState[field].isValid)
    const dirtyFields = fields.filter(field => this.validationState[field].isDirty)
    const allErrors = fields.flatMap(field => this.validationState[field].errors)

    return {
      isValid: validFields.length === fields.length,
      isDirty: dirtyFields.length > 0,
      errors: allErrors,
      completionRate: (validFields.length / fields.length) * 100
    }
  }

  /**
   * 清空验证状态
   */
  clearValidationState(fieldName?: string): void {
    if (fieldName) {
      delete this.validationState[fieldName]
    } else {
      this.validationState = {}
      this.formData = {}
    }
  }

  /**
   * 批量验证表单
   */
  validateForm(formData: Record<string, any>): {
    isValid: boolean
    errors: Record<string, string>
    suggestions: Record<string, string>
  } {
    const errors: Record<string, string> = {}
    const suggestions: Record<string, string> = {}
    let isValid = true

    for (const [fieldName, value] of Object.entries(formData)) {
      const result = this.validateField(fieldName, value as string, formData)

      if (!result.isValid) {
        errors[fieldName] = result.message
        isValid = false
      }

      if (result.suggestion) {
        suggestions[fieldName] = result.suggestion
      }
    }

    return { isValid, errors, suggestions }
  }
}

// 创建默认实例
export const formValidator = new FormValidator()

// 导出便捷方法
export const validatePhone = (phone: string) => formValidator.validateField('phone', phone)
export const validateEmail = (email: string) => formValidator.validateField('email', email)
export const validatePassword = (password: string) => formValidator.validateField('newPassword', password)
export const validateVerifyCode = (code: string) => formValidator.validateField('verifyCode', code)
export const validatePasswordConfirm = (password: string, confirmPassword: string) =>
  formValidator.validateField('confirmPassword', confirmPassword, { newPassword: password })

// 密码强度级别颜色
export const getPasswordStrengthColor = (level?: 'weak' | 'medium' | 'strong'): string => {
  switch (level) {
    case 'weak': return '#ef4444'
    case 'medium': return '#f59e0b'
    case 'strong': return '#10b981'
    default: return '#6b7280'
  }
}

// 密码强度文本
export const getPasswordStrengthText = (level?: 'weak' | 'medium' | 'strong'): string => {
  switch (level) {
    case 'weak': return '弱'
    case 'medium': return '中'
    case 'strong': return '强'
    default: return ''
  }
}