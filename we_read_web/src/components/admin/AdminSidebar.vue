<template>
  <div class="admin-sidebar">
    <!-- 移动端关闭按钮 -->
    <div class="mobile-close" v-if="isMobile">
      <el-button @click="$emit('close')" text class="close-btn">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <!-- 系统标志和名称 -->
    <div class="system-logo">
      <img src="@/assets/images/logo.png" alt="微读管理系统" class="logo-img" />
      <h1 class="system-name">微读管理系统</h1>
    </div>

    <!-- 管理员信息 -->
    <div class="admin-info">
      <el-avatar
        :size="60"
        :src="adminInfo.avatar || defaultAvatar"
        class="admin-avatar"
      >
        {{
          adminInfo.name ? adminInfo.name.substring(0, 1).toUpperCase() : "A"
        }}
      </el-avatar>
      <div class="admin-details">
        <h3 class="admin-name">
          {{ adminInfo.name || "管理员" }}
          <el-tag
            size="small"
            v-if="adminInfo.role === 'super_admin'"
            type="danger"
            effect="dark"
            class="role-tag"
            >超级管理员</el-tag
          >
          <el-tag
            size="small"
            v-else
            type="info"
            effect="plain"
            class="role-tag"
            >{{ adminInfo.role || "管理员" }}</el-tag
          >
        </h3>
        <p class="admin-id">ID: {{ adminInfo.id || "未获取" }}</p>
      </div>
    </div>

    <!-- 导航菜单 -->
    <el-scrollbar height="calc(100vh - 240px)">
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        :router="true"
        :collapse="false"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>控制面板</span>
        </el-menu-item>

        <el-sub-menu index="user-manage">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><List /></el-icon>
            <span>用户列表</span>
          </el-menu-item>
          <el-menu-item index="/admin/user-groups">
            <el-icon><UserFilled /></el-icon>
            <span>用户分组</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="book-manage">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>图书管理</span>
          </template>
          <el-menu-item index="/admin/books">
            <el-icon><List /></el-icon>
            <span>图书列表</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Files /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-sub-menu>

        

        <el-sub-menu index="system-manage">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/admins">
            <el-icon><Avatar /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/roles">
            <el-icon><Lock /></el-icon>
            <span>角色权限</span>
          </el-menu-item>
          <el-menu-item index="/admin/system-config">
            <el-icon><Tools /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Histogram /></el-icon>
            <span>系统日志</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/profile">
          <el-icon><UserFilled /></el-icon>
          <span>个人资料</span>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>

    <!-- 退出登录按钮 -->
    <div class="logout-container">
      <el-button type="danger" class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出系统
      </el-button>
    </div>
  </div>
</template>
  
  <script>
import { computed } from "vue";
import {
  Close,
  DataBoard,
  User,
  UserFilled,
  Reading,
  Setting,
  List,
  Files,
  Avatar,
  Lock,
  Tools,
  Histogram,
  SwitchButton,
} from "@element-plus/icons-vue";

export default {
  name: "AdminSidebar",
  components: {
    Close,
    DataBoard,
    User,
    UserFilled,
    Reading,
    Setting,
    List,
    Files,
    Avatar,
    Lock,
    Tools,
    Histogram,
    SwitchButton,
  },
  props: {
    adminInfo: {
      type: Object,
      default: () => ({}),
    },
    activeMenu: {
      type: String,
      required: true,
    },
    defaultAvatar: {
      type: String,
      default: "/default-admin-avatar.png",
    },
  },
  emits: ["logout", "close"],
  setup(props, { emit }) {
    // 判断是否为移动端
    const isMobile = computed(() => window.innerWidth <= 991);

    /**
     * 处理退出登录
     */
    const handleLogout = () => {
      emit("logout");
    };

    return {
      isMobile,
      handleLogout,
    };
  },
};
</script>
  
  <style scoped>
/* 侧边栏样式 */
.admin-sidebar {
  width: 100%;
  height: 100%;
  background-color: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 移动端关闭按钮 */
.mobile-close {
  display: none;
  text-align: right;
  padding: 10px;
}

.close-btn {
  color: #fff;
  font-size: 18px;
}

/* 系统标志和名称 */
.system-logo {
  padding: 20px 0;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-img {
  width: 40px;
  height: 40px;
  margin-bottom: 8px;
}

.system-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #fff;
}

/* 管理员信息 */
.admin-info {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-avatar {
  margin-bottom: 10px;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.admin-details {
  margin-top: 10px;
}

.admin-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 5px;
}

.role-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 18px;
  line-height: 16px;
}

.admin-id {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 导航菜单 */
.admin-menu {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #0c2135;
}

:deep(.el-menu-item:hover) {
  color: #fff;
  background-color: #0c2135;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-sub-menu__title:hover) {
  color: #fff;
  background-color: #0c2135;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #fff;
}

:deep(.el-menu-item .el-icon, .el-sub-menu__title .el-icon) {
  color: inherit;
}

/* 退出登录按钮 */
.logout-container {
  margin-top: auto;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 响应式布局 */
@media screen and (max-width: 991px) {
  .mobile-close {
    display: block;
  }
}
</style>
