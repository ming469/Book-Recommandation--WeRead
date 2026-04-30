<template>
  <el-dropdown trigger="click" @command="handleCommand" placement="bottom-end">
    <div class="avatar-container">
      <el-avatar :size="40" :src="userInfo.avatar || defaultAvatar">
        {{ userInfo.nickname ? userInfo.nickname.substring(0, 1) : "U" }}
      </el-avatar>
      <span class="username-display" v-if="!isMobileView">
        {{ userInfo.nickname || userInfo.username }}
      </span>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="user-dropdown-menu">
        <el-dropdown-item command="profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-dropdown-item>
        <el-dropdown-item command="favorites">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-dropdown-item>
        <el-dropdown-item command="shelf">
          <el-icon><User /></el-icon>
          <span>我的书架</span>
        </el-dropdown-item>
        <el-dropdown-item command="history">
          <el-icon><Clock /></el-icon>
          <span>阅读历史</span>
        </el-dropdown-item>
        <el-dropdown-item command="settings">
          <el-icon><Setting /></el-icon>
          <span>账号设置</span>
        </el-dropdown-item>
        <el-dropdown-item command="help">
          <el-icon><QuestionFilled /></el-icon>
          <span>帮助中心</span>
        </el-dropdown-item>
        <el-dropdown-item divided command="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
/**
 * UserDropdown - 用户中心下拉菜单
 *
 * @author
 * @created 2025-03-08
 * @updated 2025-03-11
 */

import { computed, ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import {
  User,
  Star,
  QuestionFilled,
  SwitchButton,
  Clock,
  Setting,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";

/** 路由实例 */
const router = useRouter();
/** 用户状态管理 */
const userStore = useUserStore();

/** 默认头像 */
const defaultAvatar = "default-avatar.png";

/** 用户信息 */
const userInfo = computed(() => userStore.userInfo);

/** 是否为移动视图 */
const isMobileView = ref(false);

/** 检查是否为移动视图 */
const checkMobileView = () => {
  isMobileView.value = window.innerWidth < 768;
};

/** 生命周期钩子 - 组件挂载时 */
onMounted(async () => {
  checkMobileView();
  window.addEventListener("resize", checkMobileView);

  // 确保用户信息已加载
  if (!userInfo.value.username && !userInfo.value.nickname) {
    await userStore.getUserInfo();
  }

  // 获取未读通知数量
  try {
    await userStore.getUnreadNotificationCount();
  } catch (error) {
    console.error("获取未读通知数量失败:", error);
  }
});

/** 生命周期钩子 - 组件卸载时 */
onUnmounted(() => {
  window.removeEventListener("resize", checkMobileView);
});

/**
 * 下拉菜单的命令处理
 * @param {string} command - 下拉菜单传递的命令
 */
function handleCommand(command) {
  switch (command) {
    case "profile":
      router.push("/user/profile");
      break;
    case "favorites":
      router.push("/user/favorites");
      break;
    case "shelf":
      router.push("/user/shelf");
      break;
    case "history":
      router.push("/user/history");
      break;
    case "settings":
      router.push("/user/settings");
      break;
    case "help":
      router.push("/user/help");
      break;
    case "logout":
      confirmLogout();
      break;
    default:
      break;
  }
}

/**
 * 确认并执行退出登录
 */
function confirmLogout() {
  ElMessageBox.confirm(
    "您确定要退出当前账号吗？退出后需要重新登录才能访问个人内容。",
    "退出登录",
    {
      confirmButtonText: "确认退出",
      cancelButtonText: "取消",
      type: "warning",
      distinguishCancelAndClose: true,
      customClass: "logout-confirm-box",
      showClose: true,
      center: false,
      roundButton: true,
      closeOnClickModal: false,
      closeOnPressEscape: true,
      iconClass: "el-icon-warning",
    }
  )
    .then(async () => {
      try {
        await userStore.logout();
        router.push("/");
      } catch (error) {
        console.error("退出登录失败:", error);
      }
    })
    .catch(() => {
      // 用户取消操作，无需额外处理
    });
}
</script>

<style scoped>
/**
 * 用户中心下拉菜单 - 样式
 */

.avatar-container {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 4px 12px 4px 4px;
  background-color: #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
}

.avatar-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #e6f1ff;
}

.username-display {
  margin-left: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 头像上的未读通知徽章 */
.avatar-badge {
  position: absolute;
  top: 0;
  right: 4px;
}

/* 通知菜单项上的未读通知徽章 */


/* 自定义下拉菜单样式 */
:deep(.user-dropdown-menu) {
  min-width: 140px;
  padding: 8px 0;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 10px;
  font-size: 16px;
  color: #409eff;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f0f7ff;
  color: #409eff;
}

:deep(.el-dropdown-menu__item.is-divided) {
  border-top: 1px solid #ebeef5;
  margin-top: 6px;
  padding-top: 12px;
}

:deep(.el-dropdown-menu__item.is-divided:hover) {
  background-color: #fff0f0;
  color: #f56c6c;
}

:deep(.el-dropdown-menu__item.is-divided .el-icon) {
  color: #f56c6c;
}

:deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
}

/* 响应式适配:小屏上只显示头像 */
@media screen and (max-width: 767px) {
  .avatar-container {
    padding: 4px;
    border-radius: 50%;
    background-color: #f0f2f5;
  }

  .avatar-badge {
    top: -2px;
    right: -2px;
  }
}
</style>

<style>
/* 全局样式 - 退出登录确认框 */
.logout-confirm-box {
  border-radius: 16px;
  padding: 24px;
  width: 380px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
  background: linear-gradient(to bottom right, #ffffff, #f9fbff);
}

.logout-confirm-box .el-message-box__header {
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(235, 238, 245, 0.6);
  position: relative;
}

.logout-confirm-box .el-message-box__title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.logout-confirm-box .el-message-box__title::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 18px;
  background: #409eff;
  border-radius: 2px;
  margin-right: 10px;
}

.logout-confirm-box .el-message-box__content {
  padding: 24px 0;
  display: flex;
  align-items: center;
}

.logout-confirm-box .el-message-box__message {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  position: relative;
  padding-left: 30px;
}

.logout-confirm-box .el-message-box__status {
  display: none !important;
}

.logout-confirm-box .el-message-box__message::before {
  content: "!";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.logout-confirm-box .el-message-box__btns {
  padding-top: 16px;
  border-top: 1px solid rgba(235, 238, 245, 0.6);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.logout-confirm-box .el-button {
  border-radius: 24px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none;
  height: 40px;
  min-width: 90px;
}

.logout-confirm-box .el-button--default {
  border: 1px solid #e4e7ed;
  background-color: #ffffff;
  color: #606266;
}

.logout-confirm-box .el-button--default:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
  color: #303133;
  transform: translateY(-1px);
}

.logout-confirm-box .el-button--primary {
  background: linear-gradient(135deg, #ff7676, #f56c6c);
  border: none;
  color: white;
}

.logout-confirm-box .el-button--primary:hover {
  background: linear-gradient(135deg, #ff8a8a, #f78989);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transform: translateY(-1px);
}

.logout-confirm-box .el-message-box__close {
  color: #909399;
  font-size: 18px;
  top: 16px;
  right: 16px;
  transition: all 0.2s ease;
}

.logout-confirm-box .el-message-box__close:hover {
  color: #f56c6c;
  transform: rotate(90deg);
}

/* 添加响应式适配 */
@media screen and (max-width: 480px) {
  .logout-confirm-box {
    width: 90%;
    padding: 20px;
  }

  .logout-confirm-box .el-message-box__title {
    font-size: 18px;
  }

  .logout-confirm-box .el-message-box__message {
    font-size: 14px;
  }

  .logout-confirm-box .el-button {
    padding: 8px 16px;
    min-width: 80px;
  }
}
</style>
