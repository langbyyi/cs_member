import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { logout, getCurrentUser, memberLogin, adminLogin, getMemberCurrentInfo, getMemberGradeList } from '@/api/auth'
import type { LoginRequest, LoginResponse } from '@/types'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 双JWT系统：分别存储会员和管理员的Token
  const memberToken = ref<string>(localStorage.getItem('memberToken') || '')
  const adminToken = ref<string>(localStorage.getItem('adminToken') || '')
  const memberRefreshToken = ref<string>(localStorage.getItem('memberRefreshToken') || '')
  const adminRefreshToken = ref<string>(localStorage.getItem('adminRefreshToken') || '')

  // 当前活动的Token和用户信息
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<any>(null)
  const permissions = ref<string[]>([])
  const gradeConfigs = ref<any[]>([])

  // 当前登录的角色类型
  const currentLoginType = ref<'member' | 'admin' | null>(null)

  // 双JWT系统的登录状态判断
  const isLogin = computed(() => {
    return !!token.value && !!currentLoginType.value
  })
  const hasPermission = computed(() => (permission: string) => {
    return permissions.value.includes(permission) || permissions.value.includes('*')
  })

  // 用户角色判断
  const userRole = computed(() => {
    if (!userInfo.value) return 'guest'

    const roleName = userInfo.value.roleName || ''
    const roles = userInfo.value.roles || []

    // 管理端用户：所有从管理端登录的sys_user表用户都应该返回admin角色（用于路由权限）
    // 具体的细粒度权限通过权限列表控制
    if (roleName === '系统管理员' || roleName === '门店管理员' || roleName === '员工') {
      return 'admin'
    }

    // 优先使用 roleName 判断，如果没有则使用 roles 数组判断
    const roleText = roleName.toLowerCase() + ' ' + (Array.isArray(roles) ? roles.join(' ').toLowerCase() : roles.toString().toLowerCase())

    // 根据后端角色代码判断（sys_user表的角色）
    if (roleText.includes('system_admin') || roleText.includes('系统管理员')) {
      return 'admin'
    }
    if (roleText.includes('store_admin') || roleText.includes('门店管理员')) {
      return 'admin'
    }
    if (roleText.includes('clerk') || roleText.includes('员工')) {
      return 'admin'
    }
    if (roleText.includes('会员') || roleText.includes('member')) {
      return 'member'
    }

    // 如果用户有token但没有详细角色信息，默认为admin（管理端用户）
    if (token.value && !roleName && (!roles || roles.length === 0)) {
      return 'admin'
    }

    return 'guest'
  })

  // 判断是否为管理员角色
  const isAdmin = computed(() => userRole.value === 'admin')
  // 修复类型错误：userRole只返回 admin/member/guest，具体角色需要通过 isStoreAdmin/isClerk 判断
  const isStoreManager = computed(() => isStoreAdmin.value)
  const isStaff = computed(() => isClerk.value)
  const isMember = computed(() => userRole.value === 'member')
  const isGuest = computed(() => userRole.value === 'guest')

  // 具体角色权限判断（sys_user表的三种角色）
  const isSystemAdmin = computed(() => {
    const roleName = userInfo.value?.roleName || ''
    const roles = userInfo.value?.roles || []
    const roleText = roleName.toLowerCase() + ' ' + (Array.isArray(roles) ? roles.join(' ').toLowerCase() : roles.toString().toLowerCase())
    return roleText.includes('system_admin') || roleName === '系统管理员'
  })

  const isStoreAdmin = computed(() => {
    const roleName = userInfo.value?.roleName || ''
    const roles = userInfo.value?.roles || []
    const roleText = roleName.toLowerCase() + ' ' + (Array.isArray(roles) ? roles.join(' ').toLowerCase() : roles.toString().toLowerCase())
    return roleText.includes('store_admin') || roleName === '门店管理员'
  })

  const isClerk = computed(() => {
    const roleName = userInfo.value?.roleName || ''
    const roles = userInfo.value?.roles || []
    const roleText = roleName.toLowerCase() + ' ' + (Array.isArray(roles) ? roles.join(' ').toLowerCase() : roles.toString().toLowerCase())
    return roleText.includes('clerk') || roleName === '员工'
  })

  // 判断是否有管理权限
  const hasAdminPermission = computed(() => {
    return isAdmin.value || isStoreManager.value
  })

  // 判断是否可以访问财务数据
  const canAccessFinancialData = computed(() => {
    return isAdmin.value || isStoreManager.value
  })

  // 用户登录
  const loginAction = async (loginForm: LoginRequest) => {
    try {
      // 根据当前路由判断登录类型
      const currentPath = router.currentRoute.value.path
      const isAdminLogin = currentPath.includes('/admin')

      // 使用纯对象传递，避免直接传递 Vue reactive 对象导致序列化为空
      const requestData = {
        phone: (loginForm.phone || '').trim(),
        password: loginForm.password
      }

      // 根据路由选择不同的登录API
      const response = isAdminLogin
        ? await adminLogin(requestData)
        : await memberLogin(requestData)

      // 响应拦截器已经处理了错误情况，这里只处理成功的情况
      // 对于成功的登录响应，响应拦截器返回完整的响应对象
      const responseData = response as any

      // 实际的登录数据在 data 字段中
      const loginResponse = responseData.data || {}

      token.value = loginResponse.accessToken || ''
      refreshToken.value = loginResponse.refreshToken || ''

      // 根据登录类型设置不同的用户信息
      let finalRoleName = '员工' // 默认为员工

      if (isAdminLogin) {
        // 管理员登录的用户信息
        const backendRole = loginResponse.role || loginResponse.roles || 'CLERK'

        // 根据后端返回的角色设置对应的中文角色名
        if (backendRole === 'CLERK' || backendRole.includes('CLERK')) {
          finalRoleName = '员工'
        } else if (backendRole === 'STORE_ADMIN' || backendRole.includes('STORE_ADMIN')) {
          finalRoleName = '门店管理员'
        } else if (backendRole === 'SYSTEM_ADMIN' || backendRole.includes('SYSTEM_ADMIN')) {
          finalRoleName = '系统管理员'
        }

        userInfo.value = {
          id: loginResponse.userId || 0,
          username: loginResponse.username || '',
          realName: loginResponse.name || '', // 后端返回的是 name 字段
          storeId: loginResponse.storeId || null, // 门店ID
          storeName: loginResponse.storeName || null, // 门店名称
          roleId: loginResponse.userId || 0, // 临时使用userId作为roleId
          roleName: finalRoleName, // 使用转换后的中文角色名
          // 确保管理员有正确的角色标识
          roles: Array.isArray(loginResponse.roles) ? loginResponse.roles : [loginResponse.role],
          permissions: loginResponse.permissions || []
        }
      } else {
        // 会员登录
        finalRoleName = '会员' // 设置会员角色名

        // 会员登录的用户信息 - 直接使用登录响应的完整信息
        userInfo.value = {
          id: loginResponse.userId || 0,
          username: loginResponse.memberNo || '', // 使用会员编号作为用户名（兼容性字段）
          realName: loginResponse.name || '',
          storeId: loginResponse.registerStoreId || null,
          storeName: null,
          roleId: 1, // 会员角色ID
          roleName: finalRoleName, // 明确设置为会员
          // 会员特有字段
          phone: loginResponse.phone,
          memberNo: loginResponse.memberNo, // 直接使用memberNo字段
          memberLevel: loginResponse.memberLevel || 1,
          currentPoints: loginResponse.currentPoints || 0,
          totalPoints: loginResponse.totalPoints || 0,
          balance: loginResponse.balance || 0,
          totalConsumption: loginResponse.totalConsumption || 0,
          email: loginResponse.email,
          gender: loginResponse.gender,
          birthday: loginResponse.birthday,
          address: loginResponse.address,
          isFirstLogin: loginResponse.isFirstLogin || false,
          // 兼容字段
          points: loginResponse.currentPoints || 0
        }
      }

      // 双JWT系统：分别保存不同角色的Token
      if (isAdminLogin) {
        // 管理员登录
        adminToken.value = loginResponse.accessToken
        adminRefreshToken.value = loginResponse.refreshToken
        token.value = loginResponse.accessToken
        refreshToken.value = loginResponse.refreshToken
        currentLoginType.value = 'admin'

        localStorage.setItem('adminToken', loginResponse.accessToken)
        localStorage.setItem('adminRefreshToken', loginResponse.refreshToken)
        localStorage.setItem('adminUserInfo', JSON.stringify(userInfo.value))

        // 从登录响应中获取权限
        const permissionList = loginResponse.permissions || []
        permissions.value = Array.isArray(permissionList) ? permissionList : []
        localStorage.setItem('adminPermissions', JSON.stringify(permissions.value))

        // 构建管理员登录成功消息
        let adminLoginMessage = '管理员登录成功！'
        if (loginResponse.lastLoginTime && loginResponse.lastLoginIp) {
          const lastLoginTime = new Date(loginResponse.lastLoginTime).toLocaleString('zh-CN')
          const lastLoginIp = loginResponse.lastLoginIp === '0:0:0:0:0:0:0:1' ? '本地' : loginResponse.lastLoginIp
          adminLoginMessage = `欢迎回来，${finalRoleName}！上次登录时间：${lastLoginTime}，登录IP：${lastLoginIp}`
        } else {
          adminLoginMessage = `欢迎回来，${finalRoleName}！`
        }

        ElMessage.success(adminLoginMessage)
        router.push('/admin/dashboard')
      } else {
        // 会员登录
        memberToken.value = loginResponse.accessToken
        memberRefreshToken.value = loginResponse.refreshToken
        token.value = loginResponse.accessToken
        refreshToken.value = loginResponse.refreshToken
        currentLoginType.value = 'member'

        localStorage.setItem('memberToken', loginResponse.accessToken)
        localStorage.setItem('memberRefreshToken', loginResponse.refreshToken)

        // 获取最新会员信息和等级配置，计算实时等级
        try {
          const [memberInfoRes, gradesRes] = await Promise.all([
            getMemberCurrentInfo(),
            getMemberGradeList()
          ])

          const memberData = (memberInfoRes as any).data || memberInfoRes
          const grades = (gradesRes as any).data || gradesRes

          // 存储等级配置
          gradeConfigs.value = Array.isArray(grades) ? grades : []

          // 计算等级
          let calculatedLevel = memberData.memberLevel || 1
          if (Array.isArray(grades) && grades.length > 0) {
            const totalPoints = memberData.totalPoints || 0
            // 找到满足积分要求的最高等级
            const validGrades = grades.filter((g: any) => totalPoints >= g.minPoints)
            if (validGrades.length > 0) {
              // 按 minPoints 降序排序，取第一个
              validGrades.sort((a: any, b: any) => b.minPoints - a.minPoints)
              // 使用 sortOrder 字段作为等级
              calculatedLevel = validGrades[0].sortOrder

              // 添加调试日志
            }
          }

          // 更新 userInfo
          userInfo.value = {
            ...userInfo.value,
            ...memberData,
            memberLevel: calculatedLevel,
            // 确保关键字段正确
            id: memberData.id,
            totalPoints: memberData.totalPoints,
            currentPoints: memberData.currentPoints
          }
        } catch (error) {
        }

        localStorage.setItem('memberUserInfo', JSON.stringify(userInfo.value))

        // 会员登录，不需要权限验证，直接跳转
        permissions.value = [] // 会员用户不需要权限列表

        // 构建友好的登录成功消息，包含上次登录信息
        let loginMessage = '登录成功！'
        if (loginResponse.lastLoginTime && loginResponse.lastLoginIp) {
          const lastLoginTime = new Date(loginResponse.lastLoginTime).toLocaleString('zh-CN')
          const lastLoginIp = loginResponse.lastLoginIp === '0:0:0:0:0:0:0:1' ? '本地' : loginResponse.lastLoginIp
          loginMessage = `欢迎回来！上次登录时间：${lastLoginTime}，登录IP：${lastLoginIp}`
        }

        ElMessage.success(loginMessage)
        router.push('/member/profile')
      }

      return loginResponse
    } catch (error) {
      throw error
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const response = await getCurrentUser()

      // 检查 response 是否存在
      if (!response) {
        throw new Error('获取用户信息失败：响应数据为空')
      }

      // 响应拦截器已经提取了data字段，所以response就是用户信息
      const userData = response as any

      userInfo.value = userData
      permissions.value = userData.permissions || []

      localStorage.setItem('userInfo', JSON.stringify(userData))
      localStorage.setItem('permissions', JSON.stringify(userData.permissions || []))

      return userData
    } catch (error) {
      userInfo.value = null
      permissions.value = []
      throw error
    }
  }

  // 刷新会员信息（页面刷新时使用，获取最新数据）
  const refreshMemberInfo = async () => {
    try {
      // 只有会员用户才需要刷新信息
      if (currentLoginType.value !== 'member') {
        return
      }

      const [memberInfoRes, gradesRes] = await Promise.all([
        getMemberCurrentInfo(),
        getMemberGradeList()
      ])

      const memberData = (memberInfoRes as any).data || memberInfoRes
      const grades = (gradesRes as any).data || gradesRes

      // 存储等级配置
      gradeConfigs.value = Array.isArray(grades) ? grades : []

      // 计算等级
      let calculatedLevel = memberData.memberLevel || 1
      if (Array.isArray(grades) && grades.length > 0) {
        const totalPoints = memberData.totalPoints || 0
        // 找到满足积分要求的最高等级
        const validGrades = grades.filter((g: any) => totalPoints >= g.minPoints)
        if (validGrades.length > 0) {
          // 按 minPoints 降序排序，取第一个
          validGrades.sort((a: any, b: any) => b.minPoints - a.minPoints)
          // 使用 sortOrder 字段作为等级
          calculatedLevel = validGrades[0].sortOrder

          // 添加调试日志
        }
      }

      // 更新用户信息，保持原有结构
      const updatedUserInfo = {
        ...userInfo.value,
        ...memberData,
        memberLevel: calculatedLevel,
        // 确保关键字段不会被覆盖
        id: userInfo.value?.id || memberData.id,
        username: userInfo.value?.username || memberData.memberNo,
        realName: memberData.name || userInfo.value?.realName,
        memberNo: memberData.memberNo,
        // 会员特有字段
        phone: memberData.phone,
        email: memberData.email,
        gender: memberData.gender,
        birthday: memberData.birthday,
        address: memberData.address,
        // memberLevel: memberData.memberLevel, // 使用计算出的等级
        status: memberData.status,
        currentPoints: memberData.currentPoints,
        totalPoints: memberData.totalPoints,
        balance: memberData.balance,
        totalConsumption: memberData.totalConsumption,
        registerStoreId: memberData.registerStoreId,
        createdTime: memberData.createdTime,
        lastConsumptionTime: memberData.lastConsumptionTime,
        // 兼容字段
        points: memberData.currentPoints
      }

      userInfo.value = updatedUserInfo

      // 更新localStorage中的数据
      localStorage.setItem('memberUserInfo', JSON.stringify(updatedUserInfo))

      return updatedUserInfo
    } catch (error) {
      // 刷新失败时保持原有信息，不抛出错误
      return userInfo.value
    }
  }

  // 用户登出 - 支持双JWT系统
  // 核心逻辑：只登出当前选定的角色，保留另一个角色的登录状态
  const logoutAction = async () => {
    try {
      // 根据当前登录类型调用对应的登出接口
      if (currentLoginType.value === 'admin') {
        await logout('admin')
      } else {
        await logout('member')
      }

      // 只有API调用成功才显示成功消息
      ElMessage.success('退出登录成功')
    } catch (error) {
      // 即使API调用失败，也要清除本地数据
      ElMessage.warning('退出登录')
    } finally {
      // 双JWT系统：根据当前登录类型清除对应的数据
      if (currentLoginType.value === 'admin') {
        // 清除管理员数据
        adminToken.value = ''
        adminRefreshToken.value = ''
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminRefreshToken')
        localStorage.removeItem('adminUserInfo')
        localStorage.removeItem('adminPermissions')
      } else if (currentLoginType.value === 'member') {
        // 清除会员数据
        memberToken.value = ''
        memberRefreshToken.value = ''
        localStorage.removeItem('memberToken')
        localStorage.removeItem('memberRefreshToken')
        localStorage.removeItem('memberUserInfo')
      }

      // 根据当前登录类型跳转到对应的登录页面
      const wasMember = currentLoginType.value === 'member'

      // 清除当前活动的用户数据
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      permissions.value = []
      currentLoginType.value = null

      // 清除旧的兼容性数据
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')

      // 根据之前的登录类型跳转到对应的登录页面
      if (wasMember) {
        router.push('/member/login')
      } else {
        router.push('/admin/login')
      }
    }
  }

  // 切换到指定角色的Token
  // 核心逻辑：在路由切换时调用，确保当前活动的 token 和 userInfo 与访问的页面类型（admin/member）一致
  const switchToRole = (role: 'member' | 'admin') => {
    // 如果已经是当前角色，且用户信息已存在，则不需要切换
    if (currentLoginType.value === role && userInfo.value) {
      return
    }

    const localAdminToken = localStorage.getItem('adminToken')
    const localMemberToken = localStorage.getItem('memberToken')

    if (role === 'admin' && (adminToken.value || localAdminToken)) {
      // 优先使用响应式引用，如果没有则使用localStorage的值
      if (adminToken.value) {
        token.value = adminToken.value
        refreshToken.value = adminRefreshToken.value
      } else {
        token.value = localAdminToken || ''
        refreshToken.value = localStorage.getItem('adminRefreshToken') || ''
        // 更新响应式引用
        adminToken.value = localAdminToken || ''
        adminRefreshToken.value = localStorage.getItem('adminRefreshToken') || ''
      }
      currentLoginType.value = 'admin'

      // 只有在没有用户信息时才从localStorage恢复
      if (!userInfo.value) {
        const savedAdminUserInfo = localStorage.getItem('adminUserInfo')
        const savedAdminPermissions = localStorage.getItem('adminPermissions')

        if (savedAdminUserInfo) {
          try {
            const parsedUserInfo = JSON.parse(savedAdminUserInfo)
            userInfo.value = parsedUserInfo
          } catch (error) {
            userInfo.value = null
            localStorage.removeItem('adminUserInfo')
          }
        }

        if (savedAdminPermissions) {
          try {
            const parsedPermissions = JSON.parse(savedAdminPermissions)
            permissions.value = Array.isArray(parsedPermissions) ? parsedPermissions : []
          } catch (error) {
            permissions.value = []
            localStorage.removeItem('adminPermissions')
          }
        }
      }
    } else if (role === 'member' && (memberToken.value || localMemberToken)) {
      // 优先使用响应式引用，如果没有则使用localStorage的值
      if (memberToken.value) {
        token.value = memberToken.value
        refreshToken.value = memberRefreshToken.value
      } else {
        token.value = localMemberToken || ''
        refreshToken.value = localStorage.getItem('memberRefreshToken') || ''
        // 更新响应式引用
        memberToken.value = localMemberToken || ''
        memberRefreshToken.value = localStorage.getItem('memberRefreshToken') || ''
      }
      currentLoginType.value = 'member'

      // 只有在没有用户信息时才从localStorage恢复
      if (!userInfo.value) {
        const savedMemberUserInfo = localStorage.getItem('memberUserInfo')

        if (savedMemberUserInfo) {
          try {
            const parsedUserInfo = JSON.parse(savedMemberUserInfo)
            userInfo.value = parsedUserInfo
          } catch (error) {
            userInfo.value = null
            localStorage.removeItem('memberUserInfo')
          }
        }

        permissions.value = []
      }
    } else {
      // 没有对应角色的Token，清空当前状态
      // 这会导致 isLogin 为 false，从而触发路由守卫的重定向
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      permissions.value = []
      currentLoginType.value = null
    }
  }

  // 初始化用户信息 - 支持双JWT系统
  const initUserInfo = () => {
    // 根据当前路径判断应该使用哪个Token
    const currentPath = router.currentRoute.value.path
    const isAdminPath = currentPath.startsWith('/admin')

    switchToRole(isAdminPath ? 'admin' : 'member')
  }

  // 更新用户信息
  const updateUserInfo = async (userData: any) => {
    try {
      if (currentLoginType.value === 'member') {
        const { updateCurrentMemberProfile } = await import('@/api/member-profile')
        await updateCurrentMemberProfile(userData)
      }
      const updatedUserInfo = { ...userInfo.value, ...userData }
      userInfo.value = updatedUserInfo
      localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
      return updatedUserInfo
    } catch (error) {
      ElMessage.error('更新用户信息失败')
      throw error
    }
  }

  // 修改密码
  const changePassword = async (passwordData: { currentPassword: string; newPassword: string }) => {
    try {
      if (currentLoginType.value === 'member') {
        const { changePassword: changePasswordApi } = await import('@/api/member-profile')
        await changePasswordApi({
          oldPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.newPassword
        })
      }
      ElMessage.success('密码修改成功')
      return true
    } catch (error) {
      ElMessage.error('修改密码失败')
      throw error
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    permissions,
    gradeConfigs: readonly(gradeConfigs),
    isLogin,
    hasPermission,
    userRole,
    isAdmin,
    isStoreManager,
    isStaff,
    isMember,
    isGuest,
    // 具体角色权限判断
    isSystemAdmin,
    isStoreAdmin,
    isClerk,
    hasAdminPermission,
    canAccessFinancialData,
    loginAction,
    fetchUserInfo,
    refreshMemberInfo,
    logoutAction,
    initUserInfo,
    updateUserInfo,
    changePassword,
    // 双JWT系统相关
    switchToRole,
    currentLoginType: readonly(currentLoginType),
    memberToken: readonly(memberToken),
    adminToken: readonly(adminToken),
    // 兼容性别名
    user: userInfo,
    logout: logoutAction,
    points: computed(() => userInfo.value?.points || 0),
    id: computed(() => userInfo.value?.id || 0)
  }
})
