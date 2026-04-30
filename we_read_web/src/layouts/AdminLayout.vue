<template>
  <div class="admin-layout">
    <!-- 侧边栏遮罩层（移动端） -->
    <div
      class="sidebar-overlay"
      v-if="showSidebar && isMobile"
      @click="toggleSidebar"
    ></div>

    <!-- 侧边栏 -->
    <div
      class="sidebar-container"
      :class="{ 'sidebar-show': showSidebar, 'sidebar-hide': !showSidebar }"
    >
      <AdminSidebar
        :adminInfo="adminInfo"
        :activeMenu="activeMenu"
        :defaultAvatar="defaultAvatar"
        @logout="handleLogout"
        @close="toggleSidebar"
      />
    </div>

    <!-- 主内容区 -->
    <div class="main-container" :class="{ 'main-expanded': !showSidebar }">
      <!-- 顶部导航栏 -->
      <div class="admin-header">
        <!-- 侧边栏切换按钮 -->
        <div class="sidebar-toggle" @click="toggleSidebar">
          <el-icon v-if="showSidebar"><Fold /></el-icon>
          <el-icon v-else><Expand /></el-icon>
        </div>

        <!-- 面包屑导航 -->
        <AdminBreadcrumb :title="pageTitle" />

        <!-- 右侧工具栏 -->
        <div class="header-tools">
          <!-- 全屏按钮 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-button
              text
              circle
              class="tool-btn"
              @click="toggleFullScreen"
              :icon="isFullScreen ? 'FullScreen' : 'FullScreen'"
            >
              <el-icon v-if="isFullScreen"><FullScreen /></el-icon>
              <el-icon v-else><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 刷新按钮 -->
          <el-tooltip content="刷新页面" placement="bottom">
            <el-button
              text
              circle
              class="tool-btn"
              @click="refreshPage"
              :loading="isRefreshing"
            >
              <el-icon><RefreshRight /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 管理员信息下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="admin-dropdown-link">
              <el-avatar
                :size="32"
                :src="adminInfo.avatar || defaultAvatar"
                class="admin-avatar"
              >
                {{
                  adminInfo.name
                    ? adminInfo.name.substring(0, 1).toUpperCase()
                    : "A"
                }}
              </el-avatar>
              <span class="admin-name">{{ adminInfo.name || "管理员" }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出系统
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="admin-content">
        <!-- 路由视图 -->
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedViews">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>

      <!-- 页脚 -->
      <div class="admin-footer">
        <p>© {{ currentYear }} ming469</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminStore } from "@/stores/modules/admin";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Fold,
  Expand,
  FullScreen,
  RefreshRight,
  ArrowDown,
  UserFilled,
  Lock,
  SwitchButton,
  Setting,
  Bell,
  Warning,
} from "@element-plus/icons-vue";
import AdminSidebar from "@/components/admin/AdminSidebar.vue";
import AdminBreadcrumb from "@/components/admin/AdminBreadcrumb.vue";
import { formatDate } from "@/utils/filters";

export default {
  name: "AdminLayout",
  components: {
    AdminSidebar,
    AdminBreadcrumb,
    Fold,
    Expand,
    FullScreen,
    RefreshRight,
    Bell,
    ArrowDown,
    UserFilled,
    Lock,
    SwitchButton,
    Setting,
    Warning,
  },
  setup() {
    // 路由和状态管理
    const route = useRoute();
    const router = useRouter();
    const adminStore = useAdminStore();

    // 页面状态
    const showSidebar = ref(true);
    const isFullScreen = ref(false);
    const isRefreshing = ref(false);
    const isMobile = ref(window.innerWidth <= 991);
    const defaultAvatar = "/default-admin-avatar.png";
    const currentYear = new Date().getFullYear();

    // 缓存的视图
    const cachedViews = computed(() => {
      const views = [];
      if (route.meta && route.meta.keepAlive) {
        views.push(route.name);
      }
      return views;
    });

    // 当前页面标题
    const pageTitle = computed(() => {
      return route.meta.title?.replace(" - 微读管理系统", "") || "管理系统";
    });

    // 当前活动菜单
    const activeMenu = computed(() => {
      return route.path;
    });

    // 管理员信息
    const adminInfo = computed(() => {
      return adminStore.adminInfo || {};
    });

    // 监听窗口大小变化
    const handleResize = () => {
      isMobile.value = window.innerWidth <= 991;
      if (isMobile.value) {
        showSidebar.value = false;
      }
    };

    // 切换侧边栏
    const toggleSidebar = () => {
      showSidebar.value = !showSidebar.value;
    };

    // 切换全屏
    const toggleFullScreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen();
        isFullScreen.value = true;
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
          isFullScreen.value = false;
        }
      }
    };

    // 刷新页面
    const refreshPage = () => {
      isRefreshing.value = true;
      router.push("/admin-refresh").then(() => {
        setTimeout(() => {
          router.go(-1);
          isRefreshing.value = false;
        }, 100);
      });
    };

    // 处理指令
    const handleCommand = (command) => {
      switch (command) {
        case "profile":
          router.push("/admin/profile");
          break;
        case "password":
          router.push("/admin/change-password");
          break;
        case "logout":
          handleLogout();
          break;
      }
    };

    // 处理登出
    const handleLogout = async () => {
      try {
        ElMessageBox.confirm("确定要退出系统吗？", "退出提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(async () => {
            try {
              await adminStore.logout();
              ElMessage.success("已安全退出系统");
              router.push("/login");
            } catch (error) {
              console.error("登出失败:", error);
              // 即使API调用失败，也清除本地状态并重定向
              adminStore.resetState();
              router.push("/login");
            }
          })
          .catch(() => {
            // 用户取消操作
          });
      } catch (error) {
        console.error("登出确认框错误:", error);
      }
    };

    // 获取管理员信息
    const fetchAdminInfo = async () => {
      if (!adminStore.adminInfo && adminStore.token) {
        try {
          await adminStore.getAdminInfo();
        } catch (error) {
          console.error("获取管理员信息失败:", error);
          // 如果获取信息失败，可能是token无效，尝试登出
          adminStore.resetState();
          router.push("/login");
        }
      }
    };

    // 生命周期钩子
    onMounted(() => {
      window.addEventListener("resize", handleResize);
      fetchAdminInfo();

      // 监听全屏变化事件
      document.addEventListener("fullscreenchange", () => {
        isFullScreen.value = !!document.fullscreenElement;
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener("resize", handleResize);
      document.removeEventListener("fullscreenchange", () => {});
    });

    // 监听路由变化，在移动端自动关闭侧边栏
    watch(
      () => route.path,
      () => {
        if (isMobile.value) {
          showSidebar.value = false;
        }
      }
    );

    return {
      showSidebar,
      isFullScreen,
      isRefreshing,
      isMobile,
      defaultAvatar,
      currentYear,
      cachedViews,
      pageTitle,
      activeMenu,
      adminInfo,
      adminStore,
      toggleSidebar,
      toggleFullScreen,
      refreshPage,
      handleCommand,
      handleLogout,
    };
  },
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar-container {
  width: 240px;
  height: 100%;
  transition: all 0.3s;
  z-index: 1000;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-hide {
  margin-left: -240px;
}

/* 主内容区样式 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s;
}

.main-expanded {
  margin-left: 0;
}

/* 顶部导航栏 */
.admin-header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  padding: 0 15px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.sidebar-toggle {
  font-size: 20px;
  cursor: pointer;
  padding: 0 15px;
  color: #606266;
}

.sidebar-toggle:hover {
  color: #409eff;
}

/* 右侧工具栏 */
.header-tools {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.tool-btn {
  font-size: 18px;
  margin: 0 5px;
}

.admin-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 50px;
  border-radius: 4px;
  transition: all 0.3s;
}

.admin-dropdown-link:hover {
  background-color: #f5f7fa;
}

.admin-avatar {
  margin-right: 8px;
}

.admin-name {
  font-size: 14px;
  margin-right: 5px;
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 主内容区 */
.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

/* 页脚 */
.admin-footer {
  padding: 15px 0;
  text-align: center;
  font-size: 12px;
  color: #909399;
  background-color: #fff;
  border-top: 1px solid #eee;
}

/* 移动端样式 */
@media screen and (max-width: 991px) {
  .sidebar-container {
    position: fixed;
    top: 0;
    left: 0;
    height: 100%;
    z-index: 1001;
  }

  .sidebar-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1000;
  }

  .admin-name {
    display: none;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .admin-header,
  .admin-footer {
    background-color: #1e1e1e;
    border-color: #333;
  }

  .admin-content {
    background-color: #121212;
  }

  .sidebar-toggle,
  .admin-name {
    color: #e0e0e0;
  }

  .admin-dropdown-link:hover {
    background-color: #2c2c2c;
  }

}
</style>
