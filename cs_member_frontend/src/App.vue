<template>
  <div class="app-wrapper">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// App.vue不再需要初始化用户信息
// 路由守卫已经处理了角色切换和用户信息恢复
</script>

/* 导入主题系统样式 */
@import '@/styles/themes/index.css';
/* 导入管理员界面增强样式 */
@import '@/styles/admin-enhancement.css';
/* 导入管理员界面优化样式 */
@import '@/styles/admin-optimization.css';

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji',
    'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  background-color: #f5f5f5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

.app-wrapper {
  height: 100%;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus 样式覆盖 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #337ecc 0%, #2d6db5 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  background-color: #fafafa;
  font-weight: 600;
  color: #333;
}

/* 动画类 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

/* 工具类 */
.text-center {
  text-align: center;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mb-16 {
  margin-bottom: 16px;
}

.mb-20 {
  margin-bottom: 20px;
}

.mr-8 {
  margin-right: 8px;
}

.mr-16 {
  margin-right: 16px;
}

/* 文本颜色类 */
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }
.text-info { color: #909399; }

/* 页面容器样式 */
.page-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f8f9fa;
}

.card-body {
  padding: 20px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-active {
  color: #67c23a;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
}

.status-inactive {
  color: #909399;
  background: #f4f4f5;
  border: 1px solid #d3d4d6;
}

.status-danger {
  color: #f56c6c;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }

  .table-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .toolbar-left,
  .toolbar-right {
    flex-direction: column;
    gap: 8px;
  }
}

/* 全局隐藏Element Plus按钮的快捷键提示 */
.el-button .shortcut-key,
.el-button .el-button__shortcut-key,
.el-button [class*="shortcut"],
.el-button [data-shortcut] {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
}

/* 隐藏特定的快捷键提示元素 */
.quick-actions__shortcut,
[class*="__shortcut"],
[data-v-c5bb3a9a] .quick-actions__shortcut {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
  font-size: 0 !important;
  line-height: 0 !important;
}

/* 隐藏Element Plus按钮的所有快捷键相关元素 */
.el-button::after {
  display: none !important;
}

/* 隐藏所有包含shortcut的元素 */
*[class*="shortcut"] {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
  font-size: 0 !important;
  line-height: 0 !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
}

/* 隐藏所有data-v-*属性的快捷键元素 */
[data-v-c5bb3a9a] [class*="shortcut"],
[data-v-c5bb3a9a] .quick-actions__shortcut {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
}

/* 隐藏快捷操作徽章 */
.quick-actions__badge,
[data-v-c5bb3a9a] .quick-actions__badge,
.el-badge,
.el-badge__content,
.el-badge__content--danger,
.el-badge__content--primary,
.el-badge__content--success,
.el-badge__content--warning,
.el-badge__content--info {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
  font-size: 0 !important;
  line-height: 0 !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
}

/* 隐藏所有徽章相关元素 */
*[class*="badge"] {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
}

/* 隐藏sup元素中的徽章内容 */
sup[class*="badge"],
sup.el-badge__content {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
}

/* 导航组件禁止选中 */
.sidebar,
.logo-container,
.sidebar-menu,
.header,
.header-left,
.header-right,
.user-info,
.collapse-btn,
.fullscreen-btn,
.notification-btn,
.el-breadcrumb,
.el-breadcrumb__item,
.el-breadcrumb__inner,
.el-menu,
.el-menu-item,
.el-sub-menu__title {
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
}
</style>