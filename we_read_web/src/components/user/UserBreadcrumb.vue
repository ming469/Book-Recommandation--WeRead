<!--
  文件名称：UserBreadcrumb.vue
  文件描述：用户面包屑导航组件，展示首页、我的页面以及当前页面标题。
  创建日期：2025-03-11
-->

<template>
  <!-- 面包屑容器 -->
  <div class="breadcrumb-container">
    <!-- Element Plus 面包屑组件，分隔符设为 "/" -->
    <el-breadcrumb separator="/">
      <!-- 首页面包屑项，点击跳转至首页 -->
      <el-breadcrumb-item :to="{ path: '/' }">
        <!-- 首页图标 -->
        <el-icon>
          <HomeFilled />
        </el-icon>
        <!-- 首页文字 -->
        <span>首页</span>
      </el-breadcrumb-item>

      <!-- 我的面包屑项，点击跳转至用户中心 -->
      <el-breadcrumb-item :to="{ path: '/user' }">
        <!-- 用户图标 -->
        <el-icon>
          <User />
        </el-icon>
        <!-- 我的文字 -->
        <span>我的</span>
      </el-breadcrumb-item>

      <!-- 当前页面面包屑项，仅展示，不支持跳转 -->
      <el-breadcrumb-item>
        <span class="current-page">{{ title }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>
/*
  组件名称：UserBreadcrumb
  功能描述：展示面包屑导航，指示用户当前所在位置。
  依赖：Element Plus 图标组件 HomeFilled、User
*/
import { HomeFilled, User } from "@element-plus/icons-vue";

export default {
  name: "UserBreadcrumb",
  // 注册子组件
  components: {
    HomeFilled,
    User,
  },
  // 属性定义
  props: {
    // 当前页面标题，必传参数
    title: {
      type: String,
      required: true,
    },
  },
};
</script>

<style scoped>
/* 面包屑容器样式 */
.breadcrumb-container {
  padding: 12px 0;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-breadcrumb__inner:hover) {
  color: var(--el-color-primary);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  font-weight: 600;
}

:deep(.el-breadcrumb__item .el-icon) {
  margin-right: 4px;
  font-size: 16px;
}

.current-page {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .breadcrumb-container {
    border-color: var(--el-border-color-darker);
  }

  .current-page {
    color: var(--el-color-primary-light-3);
  }
}

/* 移动端优化 */
@media screen and (max-width: 768px) {
  .breadcrumb-container {
    padding: 8px 0;
    margin-bottom: 16px;
  }

  :deep(.el-breadcrumb__inner) {
    font-size: 13px;
  }

  :deep(.el-breadcrumb__item .el-icon) {
    font-size: 14px;
  }
}
</style>
