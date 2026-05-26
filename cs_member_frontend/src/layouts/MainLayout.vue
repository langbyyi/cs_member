<template>
  <div class="main-layout">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <div class="bg-grid"></div>
    </div>
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <div class="logo-container">
          <div v-if="!isCollapse" class="logo-placeholder">
            <el-icon size="28"><ShoppingCart /></el-icon>
          </div>
          <span v-if="!isCollapse" class="logo-text">会员管理系统</span>
          <div v-else class="logo-mini">
            <el-icon size="24"><ShoppingCart /></el-icon>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item
              v-if="!('children' in item)"
              :index="item.path"
              :route="item.path"
            >
              <el-icon v-if="item.meta?.icon">
                <component :is="item.meta.icon" />
              </el-icon>
              <template #title>{{ item.meta?.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-button
              text
              @click="toggleSidebar"
              class="collapse-btn"
            >
              <el-icon>
                <Fold v-if="!isCollapse" />
                <Expand v-else />
              </el-icon>
            </el-button>

            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="showMemberManagementBreadcrumb" :to="{ path: '/admin/members' }">会员管理</el-breadcrumb-item>
              <!-- 动态面包屑 -->
              <el-breadcrumb-item v-if="currentRoute.meta?.activeMenu" :to="currentRoute.meta.activeMenu">
                {{ getParentMenuTitle(currentRoute.meta.activeMenu) }}
              </el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute.meta?.title">
                {{ currentRoute.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <!-- 全屏切换 -->
            <el-button circle class="fullscreen-btn" @click="toggleFullscreen" title="全屏">
              <el-icon><Monitor /></el-icon>
            </el-button>

            <!-- 通知铃铛 -->
            <el-badge :value="unreadCount" :max="99" class="notification-badge" v-if="unreadCount > 0">
              <el-button circle class="notification-btn" @click="toggleNotifications">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
            <el-button v-else circle class="notification-btn" @click="toggleNotifications">
              <el-icon><Bell /></el-icon>
            </el-button>

            <!-- 用户头像下拉菜单 -->
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.realName?.charAt(0) || 'A' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName || '管理员' }}</span>
                <el-icon class="arrow-down"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item command="lock">
                    <el-icon><Lock /></el-icon>
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 通知面板 -->
            <div v-if="showNotifications" class="notification-panel">
              <div class="notification-header">
                <h3>通知中心</h3>
                <el-button text size="small" @click="clearAllNotifications">清空</el-button>
              </div>
              <div class="notification-list">
                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ 'is-read': notification.read }"
                  @click="markNotificationRead(notification.id)"
                >
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-desc">{{ notification.content }}</div>
                    <div class="notification-time">{{ notification.time }}</div>
                  </div>
                  <div v-if="!notification.read" class="notification-dot"></div>
                </div>
              </div>
              <div v-if="notifications.length === 0" class="notification-empty">
                <el-icon><InfoFilled /></el-icon>
                <p>暂无通知</p>
              </div>
            </div>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component, route }">
            <component :is="Component" :key="route.path" />
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <!-- 浮动操作按钮 -->
    <div class="floating-actions" v-if="showFloatingActions">
      <el-tooltip content="回到顶部" placement="left">
        <el-button
          circle
          :icon="ArrowUp"
          class="floating-btn up-button"
          @click="handleScrollToTop"
        />
      </el-tooltip>

      <el-tooltip content="全屏模式" placement="left">
        <el-button
          circle
          :icon="FullScreen"
          class="floating-btn fullscreen-toggle"
          @click="toggleFullscreen"
        />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  ShoppingCart,
  Fold,
  Expand,
  Bell,
  User,
  Setting,
  SwitchButton,
  ArrowDown,
  ArrowUp,
  FullScreen,
  House,
  Medal,
  Ticket,
  Shop,
  UserFilled,
  DataAnalysis,
  Avatar,
  Monitor,
  Lock,
  InfoFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const notifications = ref([
  { id: 1, title: '新会员注册', content: '张三刚刚注册成为会员', time: '5分钟前', read: false },
  { id: 2, title: '系统通知', content: '系统将于今晚22:00进行维护', time: '1小时前', read: false },
  { id: 3, title: '消费提醒', content: '会员李四今日消费超过500元', time: '2小时前', read: true }
])
const showNotifications = ref(false)

// 浮动按钮控制
const showFloatingActions = ref(true)

// 当前路由
const currentRoute = computed(() => route)

// 激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 是否显示会员管理面包屑
const showMemberManagementBreadcrumb = computed(() => {
  const path = route.path
  return path.includes('/admin/members') && !path.endsWith('/admin/members')
})

// 未读通知数量
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})


// 切换通知面板
const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

// 标记通知为已读
const markNotificationRead = (id: number) => {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    notification.read = true
  }
}

// 清空所有通知
const clearAllNotifications = () => {
  notifications.value = []
}

// 处理滚动到顶部点击
const handleScrollToTop = () => {
  // 添加点击反馈效果
  const button = document.querySelector('.up-button')
  if (button) {
    button.classList.add('clicked')
    setTimeout(() => {
      button.classList.remove('clicked')
    }, 300)
  }

  // 执行滚动操作
  scrollToTop()
}

// 滚动到顶部
const scrollToTop = () => {
  // 首先尝试主要的滚动容器
  const mainContent = document.querySelector('.main-content')
  if (mainContent && mainContent.scrollTop > 0) {
    mainContent.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
    return
  }

  // 尝试 Element Plus 的 el-main
  const elMain = document.querySelector('.el-main')
  if (elMain && elMain.scrollTop > 0) {
    elMain.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
    return
  }

  // 尝试所有可能的滚动容器
  const scrollableElements = [
    document.querySelector('.el-main'),
    document.querySelector('.main-container'),
    document.querySelector('main'),
    document.body,
    document.documentElement
  ]

  for (const element of scrollableElements) {
    if (element) {
      try {
        const currentScroll = element.scrollTop || element.pageYOffset || 0
        if (currentScroll > 0) {
          if (element.scrollTo) {
            element.scrollTo({
              top: 0,
              behavior: 'smooth'
            })
          } else if (element.scrollTop !== undefined) {
            element.scrollTop = 0
          }
          break
        }
      } catch (error) {
        // 忽略错误，尝试下一个元素
        continue
      }
    }
  }

  // 最后的备用方案：使用 window 对象
  try {
    const pageYOffset = window.pageYOffset || document.documentElement.scrollTop || 0
    if (pageYOffset > 0) {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    }
  } catch (error) {
    // 最简单的方式
    window.scrollTop = 0
    document.body.scrollTop = 0
    document.documentElement.scrollTop = 0
  }
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 菜单项
const menuItems = computed(() => {
  const items = [
    {
      path: '/admin/dashboard',
      meta: { title: '仪表盘', icon: 'House' }
    },
    {
      path: '/admin/members',
      meta: { title: '会员管理', icon: 'UserFilled' }
    },
    {
      path: '/admin/consumption',
      meta: { title: '消费记录', icon: 'ShoppingCart' }
    },
    {
      path: '/admin/points',
      meta: { title: '积分管理', icon: 'Medal' }
    },
    {
      path: '/admin/coupons',
      meta: { title: '优惠券管理', icon: 'Ticket' }
    }
  ]

  // 根据用户角色添加不同的菜单项
  if (userStore.isSystemAdmin) {
    // 系统管理员：全部功能
    items.push(
      {
        path: '/admin/stores',
        meta: { title: '门店管理', icon: 'Shop' }
      },
      {
        path: '/admin/employees',
        meta: { title: '员工管理', icon: 'Avatar' }
      },
      {
        path: '/admin/roles',
        meta: { title: '角色管理', icon: 'Lock' }
      },
      {
        path: '/admin/users',
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: '/admin/statistics',
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      },
      {
        path: '/admin/settings',
        meta: { title: '系统设置', icon: 'Setting' }
      }
    )
  } else if (userStore.isStoreAdmin) {
    // 门店管理员：会员/消费/积分/优惠券 + 员工 + 统计（无系统设置/角色/用户管理）
    items.push(
      {
        path: '/admin/employees',
        meta: { title: '员工管理', icon: 'Avatar' }
      },
      {
        path: '/admin/statistics',
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      }
    )
  }
  // CLERK（店员）：只有基础业务（会员/消费/积分/优惠券），无管理类菜单

  return items
})

// 获取父级菜单标题
const getParentMenuTitle = (activeMenu: string) => {
  const menuItem = menuItems.value.find(item => item.path === activeMenu)
  return menuItem?.meta?.title || ''
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'lock':
      router.push('/admin/change-password')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logoutAction()
  })
}

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }

  // 点击外部关闭通知面板
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement
    if (!target.closest('.header-right') && showNotifications.value) {
      showNotifications.value = false
    }
  })
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
}

/* 背景装饰元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.bg-orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  top: -200px;
  right: -100px;
  animation-delay: 0s;
}

.bg-orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1) 0%, rgba(16, 185, 129, 0.1) 100%);
  bottom: -150px;
  left: -100px;
  animation-delay: 2s;
}

.bg-orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(245, 158, 11, 0.08) 100%);
  top: 50%;
  left: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-20px) rotate(180deg) scale(1.1);
  }
}

/* 背景网格 */
.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.main-container {
  position: relative;
  z-index: 10;
}

.sidebar {
  background: var(--bg-color-secondary);
  transition: width 0.3s;
  overflow: hidden;
  border-right: 1px solid var(--border-color-light);
  position: relative;
  z-index: 10;
  height: 100vh;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: var(--bg-color);
  color: var(--text-color);
  border-bottom: 1px solid var(--border-color-light);
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.logo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-right: 12px;
  color: var(--primary-color);
  background: var(--primary-color);
  color: white;
  border-radius: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}

.logo-mini {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: var(--primary-color);
  background: var(--primary-color);
  color: white;
  border-radius: 8px;
}

.sidebar-menu {
  border: none;
  background: var(--bg-color-secondary);
  height: calc(100vh - 60px);
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(144, 147, 153, 0.3) transparent;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(144, 147, 153, 0.3);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(144, 147, 153, 0.5);
}

.sidebar-menu :deep(.el-menu-item) {
  color: var(--text-color-secondary);
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--fill-color-light) !important;
  color: var(--primary-color) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--primary-color) !important;
  color: white !important;
  border-left-color: var(--primary-color);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: white !important;
}

.header {
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px var(--shadow-color-light);
  position: relative;
  z-index: 10;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.collapse-btn {
  font-size: 18px;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.fullscreen-btn,
.notification-btn {
  border: none;
  background: none;
  color: #606266;
  padding: 8px;
  transition: all 0.3s;
}

.fullscreen-btn:hover,
.notification-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.notification-badge {
  margin-right: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.arrow-down {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s;
}

.main-content {
  background: var(--bg-color-page);
  height: calc(100vh - 60px);
  overflow-y: auto;
  position: relative;
  z-index: 5;
  scrollbar-width: thin;
  scrollbar-color: rgba(144, 147, 153, 0.3) transparent;
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(144, 147, 153, 0.3);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(144, 147, 153, 0.5);
}

/* 通知面板样式 */
.notification-panel {
  position: absolute;
  top: 100%;
  right: 0;
  width: 360px;
  max-height: 480px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid #e4e7ed;
  z-index: 2000;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  position: relative;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.is-read {
  opacity: 0.7;
}

.notification-item.is-read .notification-title {
  font-weight: 400;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-dot {
  position: absolute;
  top: 20px;
  right: 16px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
}

.notification-empty .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.notification-empty p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .sidebar.mobile-show {
    transform: translateX(0);
  }

  .header {
    padding: 0 16px;
  }

  .username {
    display: none;
  }

  .notification-panel {
    width: 320px;
    right: -20px;
  }

  .fullscreen-btn {
    padding: 6px;
  }
}


/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .bg-orb,
  .bg-grid {
    animation: none !important;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .bg-grid {
    opacity: 0.5;
  }
}

/* 浮动操作按钮 */
.floating-actions {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50% !important;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
  border: none !important;
  color: white !important;
  font-size: 20px;
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.floating-btn:hover::before {
  left: 100%;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.1) !important;
  box-shadow: 0 12px 35px rgba(99, 102, 241, 0.4) !important;
}

/* 确保Element Plus按钮不影响对齐 */
.floating-actions .el-button {
  margin: 0 !important;
  padding: 0 !important;
  line-height: 1 !important;
}

.up-button {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3) !important;
}

.up-button:hover {
  box-shadow: 0 12px 35px rgba(59, 130, 246, 0.4) !important;
}

.up-button.clicked {
  transform: translateY(-4px) scale(0.95) !important;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
  transition: all 0.15s ease !important;
}

.fullscreen-toggle {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3) !important;
}

.fullscreen-toggle:hover {
  box-shadow: 0 12px 35px rgba(239, 68, 68, 0.4) !important;
}

/* 按钮进入动画 */
.floating-btn {
  animation: slideInRight 0.5s ease-out forwards;
  opacity: 0;
}

.floating-btn:nth-child(1) {
  animation-delay: 0.1s;
}

.floating-btn:nth-child(2) {
  animation-delay: 0.2s;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 浮动按钮响应式设计 */
@media (max-width: 768px) {
  .floating-actions {
    bottom: 24px;
    right: 24px;
    gap: 10px;
  }

  .floating-btn {
    width: 48px;
    height: 48px;
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .floating-actions {
    bottom: 16px;
    right: 16px;
    gap: 8px;
  }

  .floating-btn {
    width: 44px;
    height: 44px;
    font-size: 16px;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .floating-btn {
    animation: none !important;
  }

  .floating-btn::before {
    display: none !important;
  }
}
</style>