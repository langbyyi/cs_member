<template>
  <div class="not-found">
    <div class="not-found-container">
      <div class="error-code">404</div>
      <div class="error-message">页面不存在</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被删除
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="handleGoHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>

      <div class="quick-links">
        <p class="links-title">快速访问：</p>
        <div class="links-container">
          <router-link to="/login" class="quick-link">会员登录</router-link>
          <router-link to="/admin/login" class="quick-link">管理员登录</router-link>
          <router-link to="/register" class="quick-link">会员注册</router-link>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleGoHome = () => {
  if (userStore.isLogin) {
    // 根据用户角色重定向到对应的首页
    if (userStore.userRole === 'admin') {
      router.push('/admin/dashboard')
    } else {
      router.push('/member/profile')
    }
  } else {
    // 未登录则重定向到登录页面
    router.push('/login')
  }
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.not-found-container {
  text-align: center;
  color: white;
  z-index: 2;
  position: relative;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: float 3s ease-in-out infinite;
}

.error-message {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.error-description {
  font-size: 16px;
  margin-bottom: 40px;
  opacity: 0.9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 32px;
}

.quick-links {
  text-align: center;
}

.links-title {
  font-size: 16px;
  margin-bottom: 12px;
  opacity: 0.9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.links-container {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-link {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.quick-link:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
  text-decoration: none;
  color: white;
}

.error-actions .el-button {
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.error-actions .el-button--primary {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

.error-actions .el-button--primary:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.error-actions .el-button:not(.el-button--primary) {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: white;
}

.error-actions .el-button:not(.el-button--primary):hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }

  .error-message {
    font-size: 24px;
  }

  .error-description {
    font-size: 14px;
    padding: 0 20px;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;
  }

  .error-actions .el-button {
    width: 200px;
  }

  .shape-1 {
    width: 200px;
    height: 200px;
  }

  .shape-2 {
    width: 150px;
    height: 150px;
  }

  .shape-3 {
    width: 100px;
    height: 100px;
  }
}

@media (max-width: 480px) {
  .error-code {
    font-size: 60px;
  }

  .error-message {
    font-size: 20px;
  }

  .error-actions .el-button {
    width: 160px;
    font-size: 14px;
    padding: 10px 20px;
  }
}
</style>