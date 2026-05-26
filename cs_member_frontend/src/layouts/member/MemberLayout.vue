<template>
  <div class="member-layout">
    <!-- 会员顶部导航 -->
    <header class="member-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo">
            <div class="logo-icon">
              <el-icon><Shop /></el-icon>
            </div>
            <span class="logo-text">便利店会员服务</span>
          </div>
        </div>

        <nav class="header-nav">
          <router-link to="/member/profile" class="nav-link" :class="{ active: $route.path === '/member/profile' }">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </router-link>
          <router-link to="/member/consumption" class="nav-link" :class="{ active: $route.path === '/member/consumption' }">
            <el-icon><ShoppingCart /></el-icon>
            <span>消费记录</span>
          </router-link>
          <router-link to="/member/points" class="nav-link" :class="{ active: $route.path === '/member/points' }">
            <el-icon><Medal /></el-icon>
            <span>我的积分</span>
          </router-link>
          <router-link to="/member/coupons" class="nav-link" :class="{ active: $route.path === '/member/coupons' }">
            <el-icon><Ticket /></el-icon>
            <span>优惠券</span>
          </router-link>
        </nav>

        <div class="header-right">
          <el-dropdown @command="handleUserMenu" trigger="click">
            <span class="user-info">
              <el-avatar :size="36" :icon="UserFilled" class="header-avatar" />
              <div class="user-details">
                <span class="username">{{ userStore.user?.name || '会员' }}</span>
                <span class="user-role" :style="{ color: getMemberLevelColor() }">{{ getMemberLevelText() }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 会员主要内容区域 -->
    <main class="member-main">
      <div class="main-container">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 会员底部信息 -->
    <footer class="member-footer">
      <div class="footer-container">
        <p class="footer-text">© 2025 便利店会员管理系统. All rights reserved.</p>
        <div class="footer-links">
          <a href="#" @click.prevent="showAbout">关于我们</a>
          <a href="#" @click.prevent="showContact">联系方式</a>
          <a href="#" @click.prevent="showPrivacy">隐私政策</a>
          <a href="#" @click.prevent="showTerms">服务条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  ShoppingCart,
  Medal,
  Ticket,
  Setting,
  SwitchButton,
  ArrowDown,
  Shop
} from '@element-plus/icons-vue'

import { onMounted } from 'vue'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.isMember) {
    userStore.refreshMemberInfo()
  }
})

// 处理用户菜单
const handleUserMenu = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/member/profile')
      break
    case 'settings':
      router.push('/member/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    })

    await userStore.logoutAction()
  } catch (error) {
  }
}

// 显示关于信息
const showAbout = () => {
  ElMessageBox.alert('便利店会员管理系统 v1.0', '关于我们', {
    confirmButtonText: '确定'
  })
}

// 显示联系方式
const showContact = () => {
  ElMessageBox.alert('客服热线：400-123-4567<br>邮箱：service@convenience-store.com', '联系方式', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定'
  })
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert('我们重视您的隐私保护，所有数据都将被严格保密。', '隐私政策', {
    confirmButtonText: '确定'
  })
}

// 显示服务条款
const showTerms = () => {
  ElMessageBox.alert('使用本服务即表示您同意我们的服务条款和隐私政策。', '服务条款', {
    confirmButtonText: '确定'
  })
}
// 获取会员等级文本
const getMemberLevelText = () => {
  if (!userStore.user || !userStore.isMember) return '访客'
  
  const level = userStore.user.memberLevel || 1
  const gradeConfig = userStore.gradeConfigs.find((g: any) => g.sortOrder === level)
  return gradeConfig ? gradeConfig.gradeName : '普通会员'
}
// 获取会员等级颜色
const getMemberLevelColor = () => {
  if (!userStore.user || !userStore.isMember) return '#909399'
  
  const level = userStore.user.memberLevel || 1
  const gradeConfig = userStore.gradeConfigs.find((g: any) => g.sortOrder === level)
  return gradeConfig ? gradeConfig.color : '#909399'
}
</script>

<style scoped>
.member-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 顶部导航 */
.member-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(235, 238, 245, 0.8);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 72px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: -0.5px;
}

.header-nav {
  display: flex;
  gap: 8px;
  background: rgba(240, 242, 245, 0.5);
  padding: 4px;
  border-radius: 12px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: 8px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.nav-link:hover {
  color: #409eff;
  background: rgba(255, 255, 255, 0.8);
}

.nav-link.active {
  background: #fff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 8px 6px 6px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: #ebeef5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.header-avatar {
  background: linear-gradient(135deg, #a0cfff 0%, #409eff 100%);
  color: white;
  border: 2px solid #fff;
}

.user-details {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.user-role {
  font-size: 11px;
  color: #909399;
}

.dropdown-icon {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

/* 主要内容区域 */
.member-main {
  flex: 1;
  padding: 32px 24px;
  position: relative;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 底部 */
.member-footer {
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 32px 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
}

.footer-text {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-links a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #409eff;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
    height: 64px;
  }

  .logo-text {
    display: none;
  }

  .header-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(12px);
    padding: 12px;
    border-radius: 0;
    border-top: 1px solid #ebeef5;
    justify-content: space-around;
    z-index: 999;
  }

  .nav-link {
    flex-direction: column;
    gap: 4px;
    padding: 4px;
    font-size: 10px;
    background: transparent !important;
    box-shadow: none !important;
  }

  .nav-link .el-icon {
    font-size: 20px;
    margin-bottom: 2px;
  }

  .nav-link span {
    font-weight: 500;
  }

  .member-main {
    padding: 20px 16px 80px; /* 底部留出导航栏空间 */
  }

  .user-details {
    display: none;
  }
}
</style>