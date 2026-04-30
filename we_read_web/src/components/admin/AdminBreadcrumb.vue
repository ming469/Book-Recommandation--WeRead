<template>
  <div class="admin-breadcrumb">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-breadcrumb-item>

      <template v-if="parentRoute">
        <el-breadcrumb-item :to="{ path: parentRoute.path }">
          <el-icon v-if="parentRoute.icon">
            <component :is="parentRoute.icon" />
          </el-icon>
          <span>{{ parentRoute.title }}</span>
        </el-breadcrumb-item>
      </template>

      <el-breadcrumb-item>
        <span class="current-page">{{ title }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>
  
  <script>
import { computed } from "vue";
import { useRoute } from "vue-router";
import {
  HomeFilled,
  Reading,
  User,
  Setting,
  DataBoard,
} from "@element-plus/icons-vue";

export default {
  name: "AdminBreadcrumb",
  components: {
    HomeFilled,
    Reading,
    User,
    Setting,
    DataBoard,
  },
  props: {
    title: {
      type: String,
      required: true,
    },
  },
  setup() {
    const route = useRoute();

    // 路由映射表，用于确定父级路由和图标
    const routeMap = {
      "/admin/dashboard": {
        title: "控制面板",
        icon: "DataBoard",
        parent: null,
      },
      "/admin/users": {
        title: "用户列表",
        icon: "List",
        parent: { path: "/admin/dashboard", title: "用户管理", icon: "User" },
      },
      "/admin/user-groups": {
        title: "用户分组",
        icon: "UserFilled",
        parent: { path: "/admin/dashboard", title: "用户管理", icon: "User" },
      },
      "/admin/books": {
        title: "图书列表",
        icon: "List",
        parent: {
          path: "/admin/dashboard",
          title: "图书管理",
          icon: "Reading",
        },
      },
      "/admin/categories": {
        title: "分类管理",
        icon: "Files",
        parent: {
          path: "/admin/dashboard",
          title: "图书管理",
          icon: "Reading",
        },
      },
      "/admin/admins": {
        title: "管理员管理",
        icon: "Avatar",
        parent: {
          path: "/admin/dashboard",
          title: "系统管理",
          icon: "Setting",
        },
      },
      "/admin/roles": {
        title: "角色权限",
        icon: "Lock",
        parent: {
          path: "/admin/dashboard",
          title: "系统管理",
          icon: "Setting",
        },
      },
      "/admin/system-config": {
        title: "系统配置",
        icon: "Tools",
        parent: {
          path: "/admin/dashboard",
          title: "系统管理",
          icon: "Setting",
        },
      },
      "/admin/logs": {
        title: "系统日志",
        icon: "Histogram",
        parent: {
          path: "/admin/dashboard",
          title: "系统管理",
          icon: "Setting",
        },
      },
      "/admin/profile": { title: "个人资料", icon: "UserFilled", parent: null },
      "/admin/change-password": {
        title: "修改密码",
        icon: "Lock",
        parent: null,
      },
    };

    // 获取当前路由的父级路由信息
    const parentRoute = computed(() => {
      const currentPath = route.path;
      const routeInfo = routeMap[currentPath];
      return routeInfo ? routeInfo.parent : null;
    });

    return {
      parentRoute,
    };
  },
};
</script>
  
  <style scoped>
.admin-breadcrumb {
  flex: 1;
  padding: 0 15px;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #409eff;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133;
  font-weight: 600;
}

:deep(.el-breadcrumb__item .el-icon) {
  margin-right: 4px;
  font-size: 16px;
}

.current-page {
  color: #303133;
  font-weight: 600;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  :deep(.el-breadcrumb__inner) {
    color: #a6a6a6;
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: #e0e0e0;
  }

  .current-page {
    color: #e0e0e0;
  }
}
</style>
