import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 根路径重定向到会员登录
    {
      path: '/',
      redirect: '/member/login'
    },
    // 会员相关路由
    {
      path: '/member/login',
      name: 'MemberLogin',
      component: () => import('@/views/member/Login.vue'),
      meta: { title: '会员登录', requiresAuth: false }
    },
    {
      path: '/member/register',
      name: 'Register',
      component: () => import('@/views/member/Register.vue'),
      meta: { title: '会员注册', requiresAuth: false }
    },
    {
      path: '/terms',
      name: 'Terms',
      component: () => import('@/views/member/Terms.vue'),
      meta: { title: '用户服务协议', requiresAuth: false }
    },
    {
      path: '/privacy',
      name: 'Privacy',
      component: () => import('@/views/member/Privacy.vue'),
      meta: { title: '隐私政策', requiresAuth: false }
    },

    // 会员端路由
    {
      path: '/member',
      component: () => import('@/layouts/member/MemberLayout.vue'),
      redirect: '/member/profile',
      meta: { requiresAuth: true, userRole: 'member' },
      children: [
        {
          path: '/member/profile',
          name: 'MemberProfile',
          component: () => import('@/views/member/Profile.vue'),
          meta: { title: '个人中心' }
        },
        {
          path: '/member/consumption',
          name: 'MemberConsumption',
          component: () => import('@/views/member/Consumption.vue'),
          meta: { title: '消费记录' }
        },
        {
          path: '/member/points',
          name: 'MemberPoints',
          component: () => import('@/views/member/Points.vue'),
          meta: { title: '我的积分' }
        },
        {
          path: '/member/coupons',
          name: 'MemberCoupons',
          component: () => import('@/views/member/Coupons.vue'),
          meta: { title: '优惠券' }
        },
        {
          path: '/member/settings',
          name: 'MemberSettings',
          component: () => import('@/views/member/Settings.vue'),
          meta: { title: '账户设置' }
        }
      ]
    },

    // 管理员相关路由
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
      meta: { title: '管理员登录', requiresAuth: false }
    },

    // 管理员后台
    {
      path: '/admin',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, userRole: 'admin' },
      children: [
        {
          path: '/admin/dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '仪表盘', icon: 'House' }
        },
        {
          path: '/admin/members',
          name: 'Members',
          component: () => import('@/views/admin/members/MemberList.vue'),
          meta: { title: '会员管理', icon: 'User' }
        },
        {
          path: '/admin/members/create',
          name: 'CreateMember',
          component: () => import('@/views/admin/members/MemberForm.vue'),
          meta: { title: '新增会员', activeMenu: '/admin/members' }
        },
        {
          path: '/admin/members/:id',
          name: 'EditMember',
          component: () => import('@/views/admin/members/MemberForm.vue'),
          meta: { title: '编辑会员', activeMenu: '/admin/members' }
        },
        {
          path: '/admin/members/:id/edit',
          name: 'EditMemberDetail',
          component: () => import('@/views/admin/members/MemberForm.vue'),
          meta: { title: '编辑会员', activeMenu: '/admin/members' }
        },
        {
          path: '/admin/members/:id/detail',
          name: 'MemberDetail',
          component: () => import('@/views/admin/members/MemberDetail.vue'),
          meta: { title: '会员详情', activeMenu: '/admin/members' }
        },
        {
          path: '/admin/consumption',
          name: 'Consumption',
          component: () => import('@/views/admin/consumption/ConsumptionList.vue'),
          meta: { title: '消费记录', icon: 'ShoppingCart' }
        },
        {
          path: '/admin/consumption/create',
          name: 'CreateConsumption',
          component: () => import('@/views/admin/consumption/ConsumptionForm.vue'),
          meta: { title: '新增消费', activeMenu: '/admin/consumption' }
        },
        {
          path: '/admin/points',
          name: 'Points',
          component: () => import('@/views/admin/points/PointsList.vue'),
          meta: { title: '积分管理', icon: 'Medal' }
        },
        {
          path: '/admin/coupons',
          name: 'Coupons',
          component: () => import('@/views/admin/coupons/CouponList.vue'),
          meta: { title: '优惠券管理', icon: 'Ticket' }
        },
        {
          path: '/admin/coupons/create',
          name: 'CreateCoupon',
          component: () => import('@/views/admin/coupons/CouponForm.vue'),
          meta: { title: '新增优惠券', activeMenu: '/admin/coupons' }
        },
        {
          path: '/admin/stores',
          name: 'Stores',
          component: () => import('@/views/admin/stores/StoreList.vue'),
          meta: { title: '门店管理', icon: 'Shop' }
        },
        {
          path: '/admin/stores/create',
          name: 'CreateStore',
          component: () => import('@/views/admin/stores/StoreManage.vue'),
          meta: { title: '新增门店', activeMenu: '/admin/stores' }
        },
        {
          path: '/admin/stores/:id',
          name: 'EditStore',
          component: () => import('@/views/admin/stores/StoreManage.vue'),
          meta: { title: '编辑门店', activeMenu: '/admin/stores' }
        },
        {
          path: '/admin/statistics',
          name: 'Statistics',
          component: () => import('@/views/admin/statistics/Statistics.vue'),
          meta: { title: '统计分析', icon: 'DataAnalysis' }
        },
        {
          path: '/admin/settings',
          name: 'Settings',
          component: () => import('@/views/admin/settings/Settings.vue'),
          meta: { title: '系统设置', icon: 'Setting' }
        },
        {
          path: '/admin/profile',
          name: 'AdminProfile',
          component: () => import('@/views/admin/profile/ProfileView.vue'),
          meta: { title: '个人中心', hidden: true }
        },
        {
          path: '/admin/change-password',
          name: 'ChangePassword',
          component: () => import('@/views/admin/profile/ChangePassword.vue'),
          meta: { title: '修改密码', hidden: true }
        },
        {
          path: '/admin/employees',
          name: 'Employees',
          component: () => import('@/views/admin/employees/EmployeeList.vue'),
          meta: { title: '员工管理', icon: 'Avatar', roles: ['SYSTEM_ADMIN', 'STORE_ADMIN'] }
        },
        {
          path: '/admin/roles',
          name: 'RoleManage',
          component: () => import('@/views/admin/roles/RoleManage.vue'),
          meta: { title: '角色管理', icon: 'Lock', roles: ['SYSTEM_ADMIN'] }
        },
        {
          path: '/admin/users',
          name: 'UserManage',
          component: () => import('@/views/admin/users/UserManage.vue'),
          meta: { title: '用户管理', icon: 'User', roles: ['SYSTEM_ADMIN'] }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { title: '页面不存在' }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 便利店会员管理系统` : '便利店会员管理系统'

  // 双JWT系统：根据路径切换到对应的Token
  // 核心修复逻辑：确保访问admin页面时使用admin token，访问member页面时使用member token
  // 这样可以防止 member 登录后访问 admin 页面时，因为携带了 member token 而被误认为是未登录的 admin
  const targetRole = to.path.startsWith('/admin') ? 'admin' : 'member'

  // 只有在角色需要切换时才调用switchToRole
  // 这会自动更新 userStore.token, userStore.userInfo 等状态为对应角色的数据
  if (userStore.currentLoginType !== targetRole) {
    userStore.switchToRole(targetRole)
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth !== false && !userStore.isLogin) {
    ElMessage.warning('请先登录')

    // 根据访问路径决定重定向到哪个登录页面
    if (to.path.startsWith('/admin')) {
      next('/admin/login')
    } else {
      next('/member/login')
    }
    return
  }

  // 双JWT系统：智能处理多账号登录
  // 场景1：Member已登录，访问Member登录页 -> 跳转Member首页
  // 场景2：Admin已登录，访问Admin登录页 -> 跳转Admin首页
  // 场景3：Member已登录，访问Admin登录页 -> 保持在Admin登录页（因为switchToRole已切换为admin角色，此时isLogin为false）
  if (userStore.isLogin && (to.path === '/member/login' || to.path === '/admin/login' || to.path === '/member/register')) {
    const currentRole = userStore.userRole
    const targetLogin = to.path

    // 如果访问同类型的登录页面，直接重定向到对应首页
    if ((targetLogin === '/member/login' || targetLogin === '/member/register') && currentRole === 'member') {
      ElMessage.success('欢迎回来！')
      next('/member/profile')
      return
    }

    if (targetLogin === '/admin/login' && currentRole === 'admin') {
      next('/admin/dashboard')
      return
    }

    // 如果访问不同类型的登录页面，提示用户可以选择切换角色
    const currentRoleName = currentRole === 'member' ? '会员' : '管理员'
    const targetRoleName = targetLogin.includes('admin') ? '管理员' : '会员'

    ElMessage.info({
      message: `您当前已登录${currentRoleName}账号，可以继续登录${targetRoleName}账号进行多账号操作`,
      duration: 3000,
      showClose: true
    })

    // 允许继续访问登录页面
  }

  // 检查用户角色权限（只对管理员页面进行权限检查）
  if (to.meta.userRole && to.path.startsWith('/admin') && userStore.userRole !== to.meta.userRole) {
    ElMessage.warning('您没有权限访问该页面')
    if (userStore.userRole === 'admin') {
      next('/admin/dashboard')
    } else {
      next('/member/profile')
    }
    return
  }

  // 检查基于角色的路由权限
  if (to.meta.roles && to.path.startsWith('/admin')) {
    const userRoleName = userStore.userInfo?.roleName || ''
    const userRoles = (userStore.userInfo?.roles || []) as any[]

    // 将后端角色名称转换为前端角色标识
    let userRoleType = ''
    if (userRoleName === '系统管理员' || userRoles.includes('system_admin')) {
      userRoleType = 'SYSTEM_ADMIN'
    } else if (userRoleName === '门店管理员' || userRoles.includes('store_admin')) {
      userRoleType = 'STORE_ADMIN'
    } else if (userRoleName === '员工' || userRoles.includes('clerk')) {
      userRoleType = 'CLERK'
    }

    // 检查用户角色是否在允许的角色列表中
    if (!to.meta.roles.includes(userRoleType)) {
      ElMessage.warning('您没有权限访问该页面')
      next('/admin/dashboard')
      return
    }
  }

  // 添加路由错误处理
  if (to.matched.length === 0) {
    // 路由不存在，重定向到404页面
    next({ name: 'NotFound' })
    return
  }

  next()
})

// 路由错误处理
router.onError((error) => {
  ElMessage.error('页面加载失败，请重试')
})

export default router