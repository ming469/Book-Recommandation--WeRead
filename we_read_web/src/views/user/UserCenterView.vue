<template>
  <div class="user-center-container">
    <!-- 移动端顶部导航栏 -->
    <div class="mobile-header" v-if="isMobile">
      <div class="mobile-title">
        <span>{{ getBreadcrumbTitle }}</span>
      </div>
      <el-button class="mobile-menu-btn" @click="toggleMobileMenu" text>
        <el-icon><Menu /></el-icon>
      </el-button>
    </div>

    <!-- 用户中心布局 -->
    <div class="user-center-layout">
      <!-- 侧边栏导航 -->
      <UserSidebar
        :class="{
          'mobile-sidebar': isMobile,
          'mobile-sidebar-visible': mobileMenuVisible,
        }"
        :userInfo="userInfo"
        :activeMenu="activeMenu"
        :defaultAvatar="defaultAvatar"
        @logout="handleLogoutSuccess"
        @close="mobileMenuVisible = false"
      />

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 移动端菜单切换按钮 - 仅保留右上角的菜单按钮 -->
        <div class="content-header" v-if="isMobile">
          <UserBreadcrumb :title="getBreadcrumbTitle" />
        </div>
        <UserBreadcrumb v-else :title="getBreadcrumbTitle" />

        <!-- 路由视图 -->
        <div class="content-view">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <keep-alive>
                <component :is="Component" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </div>
    </div>

    <!-- 移动端遮罩层 -->
    <div
      v-if="isMobile && mobileMenuVisible"
      class="mobile-overlay"
      @click="mobileMenuVisible = false"
    ></div>
  </div>
</template>

<script>
/**
 * UserCenterView.vue - 用户中心主页面
 *
 * @description 用户中心的主布局组件，包含侧边栏导航和内容区域，作为子路由的容器
 */
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElButton, ElIcon } from "element-plus";
import { Menu, Close } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
import UserSidebar from "@/components/user/UserSidebar.vue";
import UserBreadcrumb from "@/components/user/UserBreadcrumb.vue";

export default {
  name: "UserCenterView",
  components: {
    UserSidebar,
    UserBreadcrumb,
    ElButton,
    ElIcon,
    Menu,
    Close,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const userStore = useUserStore();

    // 默认头像
    const defaultAvatar = ref(
      "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
    );

    // 用户信息
    const userInfo = computed(() => userStore.userInfo || {});

    // 当前激活的菜单项
    const activeMenu = computed(() => route.path);

    // 面包屑标题
    const getBreadcrumbTitle = computed(() => {
      const path = route.path;
      if (path.includes("/profile")) return "个人资料";
      if (path.includes("/favorites")) return "我的收藏";
      if (path.includes("/history")) return "阅读历史";
      if (path.includes("/settings")) return "账号设置";
      if (path.includes("/help")) return "帮助中心";
      return "个人中心";
    });

    // 移动端相关状态
    const isMobile = ref(window.innerWidth <= 768);
    const mobileMenuVisible = ref(false);

    /**
     * 切换移动端菜单显示状态
     */
    const toggleMobileMenu = () => {
      mobileMenuVisible.value = !mobileMenuVisible.value;
    };

    /**
     * 处理窗口大小变化
     */
    const handleResize = () => {
      isMobile.value = window.innerWidth <= 768;
      if (!isMobile.value) {
        mobileMenuVisible.value = false;
      }
    };

    /**
     * 处理退出登录成功
     */
    const handleLogoutSuccess = () => {
      userStore.logout();
      router.push("/");
      ElMessage.success("已成功退出登录");
    };

    onMounted(async () => {
      // 监听窗口大小变化
      window.addEventListener("resize", handleResize);

      // 如果没有用户信息，尝试获取
      if (!userStore.hasUserInfo) {
        try {
          await userStore.getUserInfo();
        } catch (error) {
          console.error("获取用户信息失败", error);
          ElMessage.error("获取用户信息失败，请重新登录");
          userStore.resetState();
          router.push("/login");
        }
      }
    });

    onBeforeUnmount(() => {
      // 移除事件监听
      window.removeEventListener("resize", handleResize);
    });

    return {
      defaultAvatar,
      userInfo,
      activeMenu,
      getBreadcrumbTitle,
      isMobile,
      mobileMenuVisible,
      toggleMobileMenu,
      handleLogoutSuccess,
    };
  },
};
</script>

<style scoped>
/**
 * 用户中心样式
 */
.user-center-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  animation: fadeInUp 0.5s ease;
}

/* 移动端顶部导航栏 */
.mobile-header {
  display: none;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  margin-bottom: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.mobile-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.mobile-menu-btn {
  font-size: 20px;
}

.user-center-layout {
  display: flex;
  gap: 24px;
  min-height: calc(100vh - 200px);
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  padding: 24px;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 内容区头部样式 */
.content-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

/* 移除内嵌菜单按钮相关样式 */
/* .inline-menu-toggle 样式已移除 */

.content-view {
  min-height: 500px;
}

/* 移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
  backdrop-filter: blur(2px);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .user-center-container {
    padding: 16px;
  }

  .mobile-header {
    display: flex;
  }

  .user-center-layout {
    flex-direction: column;
  }

  .main-content {
    padding: 16px;
    border-radius: 10px;
  }

  .mobile-sidebar {
    position: fixed;
    top: 0;
    left: -280px;
    height: 100vh;
    width: 260px;
    z-index: 1000;
    border-radius: 0;
    transition: left 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    overflow-y: auto;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  }

  .mobile-sidebar-visible {
    left: 0;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .main-content {
    background-color: var(--el-bg-color-overlay);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }

  .mobile-overlay {
    background-color: rgba(0, 0, 0, 0.7);
  }
}
</style>