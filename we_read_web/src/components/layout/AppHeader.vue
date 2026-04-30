<template>
  <header class="app-header">
    <div class="header-container">
      <!-- 网站 Logo 及标题 -->
      <div class="logo" @click="goHome">
        <el-image
          class="logo-image"
          :src="require('@/assets/images/logo.png')"
          alt="微读"
          fit="contain"
        />
        <h1 class="logo-title">微读</h1>
      </div>

      <!-- 搜索框 -->
      <div class="search-container">
        <SearchBar />
      </div>

      <!-- 导航菜单 -->
      <nav class="main-nav">
        <div
          v-for="(item, index) in navItems"
          :key="index"
          class="nav-item"
          :class="{ active: activeNav === item.key }"
          @click="setActiveNav(item.key)"
        >
          {{ item.label }}
        </div>
      </nav>

      <!-- 用户区域：判断是否登录 -->
      <div class="user-area">
        <template v-if="isLoggedIn">
          <!-- 使用提取的子组件 -->
          <UserDropdown />
        </template>
        <template v-else>
          <!-- 未登录状态显示登录按钮 -->
          <div class="auth-buttons" v-if="showLoginButton">
            <button class="gradient-btn login-btn" @click="goToLogin">
              <span class="btn-content">
                <el-icon class="btn-icon"><User /></el-icon>
                登录
              </span>
            </button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
/**
 * AppHeader.vue - 网页头部组件
 *
 * @description 提供网站头部导航、搜索和用户登录入口。若已登录，则展示用户中心下拉菜单。
 */

import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { User } from "@element-plus/icons-vue";
import SearchBar from "@/components/common/SearchBar.vue";
import UserDropdown from "@/components/user/UserDropdown.vue";
import { useUserStore } from "@/stores/modules/user";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

/** 导航菜单列表 */
const navItems = [
  { key: "back", label: "回到首页" },
  { key: "explore", label: "探索更多" },
  { key: "find", label: "发现我们" },
];

/** 当前激活的导航项 */
const activeNav = ref("back");

/** 是否登录 */
const isLoggedIn = computed(() => userStore.isLoggedIn);

/** 不显示登录按钮的页面路径 */
const hiddenAuthPaths = ["/login", "/register"];

/** 是否显示登录按钮 */
const showLoginButton = computed(() => !hiddenAuthPaths.includes(route.path));

/**
 * 根据路由初始化激活的导航项
 */
function initializeActiveNav() {
  const currentPath = route.path;
  if (currentPath.includes("/back")) {
    setActiveNav("back");
  } else if (currentPath.includes("/recent")) {
    setActiveNav("recent");
  } else if (currentPath.includes("/find")) {
    setActiveNav("find");
  }
}

/**
 * 设置激活的导航项并跳转
 * @param {string} nav - 导航项 key
 */
function setActiveNav(nav) {
  activeNav.value = nav;
  switch (nav) {
    case "back":
      router.push("/");
      break;
    case "explore":
      router.push("/explore");
      break;
    case "find":
      router.push("/find");
      break;
  }
}

/** 回到首页 */
function goHome() {
  router.push("/");
  setActiveNav("back");
}

/**
 * 跳转到登录页
 * 若当前路由不是登录/注册页，则带上重定向参数
 */
function goToLogin() {
  const currentPath = router.currentRoute.value.fullPath;
  if (currentPath !== "/login" && currentPath !== "/register") {
    router.push({
      path: "/login",
      query: { redirect: currentPath },
    });
  } else {
    router.push("/login");
  }
}

onMounted(async () => {
  // 初始化激活导航
  initializeActiveNav();

  // 若已登录但缺少用户信息，则尝试获取
  if (userStore.isLoggedIn && !userStore.hasUserInfo) {
    try {
      await userStore.getUserInfo();
    } catch (error) {
      console.error("获取用户信息失败，将重置登录状态", error);
      userStore.resetState();
    }
  }
});
</script>

<style scoped>
/**
 * 微读平台 - 网页头部样式
 */

/* 头部容器 */
.app-header {
  background-color: #edeef0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

/* 内容容器 */
.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

/* Logo 区域 */
.logo {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.logo:hover {
  transform: scale(1.02);
}

.logo-image {
  width: 48px;
  height: 48px;
  margin-right: 12px;
  color: #409eff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-title {
  font-size: 24px;
  font-weight: 600;
  color: #000000;
  margin: 0;
  line-height: 1.2;
  font-family: "SimSun", "宋体", sans-serif;
  background: linear-gradient(135deg, #333333, #000000);
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 搜索框区域 */
.search-container {
  width: 100%;
  max-width: 600px;
  margin-bottom: 20px;
}

/* 导航菜单 */
.main-nav {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 28px;
  width: 100%;
  padding: 5px 0;
}

.nav-item {
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  padding: 6px 2px;
  position: relative;
  transition: all 0.3s ease;
}

.nav-item:hover {
  color: var(--el-color-primary);
  transform: translateY(-2px);
}

.nav-item.active {
  color: var(--el-color-primary);
  font-weight: 500;
}

.nav-item.active::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 3px;
  background-color: var(--el-color-primary);
  border-radius: 1.5px;
  transition: width 0.3s ease;
}

.nav-item:hover::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background-color: var(--el-color-primary);
  border-radius: 1px;
  opacity: 0.7;
}

/* 用户区域 */
.user-area {
  position: absolute;
  top: 20px;
  right: 24px;
  display: flex;
  align-items: center;
}

/* 登录/注册按钮区域 */
.auth-buttons {
  display: flex;
  align-items: center;
}

/* 渐变按钮 */
.gradient-btn {
  border: none;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  text-align: center;
  min-width: 90px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  outline: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  background: linear-gradient(to right, #409eff, #79bbff);
  color: white;
}

.gradient-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  filter: brightness(1.05);
}

.gradient-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  filter: brightness(0.95);
}

.login-btn {
  padding: 0 14px;
  letter-spacing: 0.5px;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 0;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

/* 波纹效果 */
.gradient-btn::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  opacity: 0;
  transform: scale(0);
  border-radius: 8px;
  transition: all 0.4s ease;
}

.gradient-btn:hover::after {
  opacity: 1;
  transform: scale(1);
}

.btn-icon {
  margin-right: 6px;
  font-size: 14px;
}

/* 响应式布局：大于768px时横向排版，小于768px时堆叠 */
@media screen and (min-width: 768px) {
  .header-container {
    padding: 22px 24px;
    flex-direction: row;
    flex-wrap: wrap;
  }
  .logo {
    margin-bottom: 0;
    margin-right: 20px;
  }
  .search-container {
    flex: 1;
    margin-bottom: 0;
    margin-right: 20px;
    max-width: 400px;
  }
  .main-nav {
    width: auto;
    margin-left: auto;
  }
  .user-area {
    position: static;
    margin-left: 20px;
  }
}

@media screen and (max-width: 767px) {
  .main-nav {
    gap: 16px;
    overflow-x: auto;
    justify-content: center;
    padding: 5px 0;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }
  .nav-item {
    font-size: 14px;
    white-space: nowrap;
  }
  .logo-title {
    font-size: 20px;
  }
  .logo-image {
    width: 40px;
    height: 40px;
  }
  .user-area {
    top: 15px;
  }
  .gradient-btn {
    min-width: 70px;
    height: 32px;
  }
  .btn-content {
    font-size: 13px;
    padding: 0;
  }
  .btn-icon {
    margin-right: 4px;
    font-size: 14px;
  }
}

@media screen and (max-width: 480px) {
  .main-nav {
    gap: 12px;
  }
  .nav-item {
    font-size: 13px;
    padding: 4px 0;
  }
  .header-container {
    padding: 16px;
  }
  .logo-image {
    width: 36px;
    height: 36px;
  }
  .gradient-btn {
    min-width: 64px;
    height: 30px;
  }
  .btn-content {
    font-size: 12px;
    padding: 0;
  }
  .btn-icon {
    margin-right: 3px;
    font-size: 13px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .app-header {
    background-color: #1e1e1e;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
  }
  .logo-title {
    background: linear-gradient(135deg, #e5e7eb, #ffffff);
    background-clip: text;
  }
  .nav-item {
    color: #a0a0a0;
  }
  .gradient-btn {
    background: linear-gradient(to right, #3a7be0, #6ca6f5);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  }
  .gradient-btn:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  }
}
</style>
