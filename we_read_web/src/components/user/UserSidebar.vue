<template>
  <div class="sidebar">
    <!-- 移动端关闭按钮 -->
    <div class="mobile-close" v-if="isMobile">
      <el-button @click="$emit('close')" text class="close-btn">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div class="user-info">
      <div class="avatar-container">
        <el-avatar
          :size="isMobile ? 60 : 70"
          :src="userInfo.avatar || defaultAvatar"
          @error="handleAvatarError"
          class="user-avatar"
        >
          {{
            userInfo.nickname
              ? userInfo.nickname.substring(0, 1).toUpperCase()
              : "U"
          }}
        </el-avatar>
        <div class="avatar-status" v-if="userInfo.vip"></div>
      </div>
      <div class="user-details">
        <h3 class="user-name">
          {{ userInfo.nickname || "微读用户" }}
          <el-tag
            size="small"
            v-if="userInfo.vip"
            type="warning"
            effect="plain"
            class="vip-tag"
            >VIP</el-tag
          >
        </h3>
        <p class="user-id">ID: {{ userInfo.id || "未获取" }}</p>
      </div>
    </div>

    <!-- 用户统计信息 -->
    <div class="user-stats">
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.readCount || 0 }}</div>
        <div class="stat-label">阅读</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.favoriteCount || 0 }}</div>
        <div class="stat-label">收藏</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.followCount || 0 }}</div>
        <div class="stat-label">关注</div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <el-menu
      :default-active="activeMenu"
      class="user-menu"
      :router="true"
      @select="handleSelect"
    >
      <el-menu-item index="/user/profile">
        <el-icon><User /></el-icon>
        <span>个人资料</span>
      </el-menu-item>
      <el-menu-item index="/user/favorites">
        <el-icon><Star /></el-icon>
        <span>我的收藏</span>
      </el-menu-item>
      <el-menu-item index="/user/shelf">
        <el-icon><User /></el-icon>
        <span>我的书架</span>
      </el-menu-item>
      <el-menu-item index="/user/history">
        <el-icon><Clock /></el-icon>
        <span>阅读历史</span>
      </el-menu-item>
      <el-menu-item index="/user/settings">
        <el-icon><Setting /></el-icon>
        <span>账号设置</span>
      </el-menu-item>
      <el-menu-item index="/user/help">
        <el-icon><QuestionFilled /></el-icon>
        <span>帮助中心</span>
      </el-menu-item>
    </el-menu>

    <!-- 退出登录按钮 -->
    <div class="logout-container">
      <el-button type="danger" plain class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import {
  User,
  Star,
  Clock,
  Setting,
  QuestionFilled,
  SwitchButton,
  Close,
  Bell,
} from "@element-plus/icons-vue";

export default {
  name: "UserSidebar",
  components: {
    User,
    Star,
    Clock,
    Setting,
    QuestionFilled,
    SwitchButton,
    Close,
    Bell,
  },
  props: {
    userInfo: {
      type: Object,
      default: () => ({}),
    },
    activeMenu: {
      type: String,
      required: true,
    },
    defaultAvatar: {
      type: String,
      default:
        "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
    },
  },
  emits: ["logout", "close"],
  setup(props, { emit }) {
    const router = useRouter();

    // 判断是否为移动端
    const isMobile = computed(() => window.innerWidth <= 768);

    /**
     * 处理头像加载错误
     */
    const handleAvatarError = () => {
      ElMessage.warning("头像加载失败，已使用默认头像");
    };

    /**
     * 处理菜单选择
     * @param {string} key - 选中的菜单项路径
     */
    const handleSelect = (key) => {
      router.push(key);
      // 在移动端选择菜单项后自动关闭侧边栏
      if (isMobile.value) {
        emit("close");
      }
    };

    /**
     * 处理退出登录
     */
    const handleLogout = () => {
      ElMessageBox.confirm("确定要退出登录吗？", "退出提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          emit("logout");
        })
        .catch(() => {
          // 取消退出
        });
    };

    return {
      isMobile,
      handleAvatarError,
      handleSelect,
      handleLogout,
    };
  },
};
</script>

<style scoped>
/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 移动端关闭按钮 */
.mobile-close {
  display: none;
  text-align: right;
  padding: 0 10px 10px;
}

.close-btn {
  font-size: 18px;
  transition: transform 0.2s ease;
}

.close-btn:hover {
  transform: rotate(90deg);
}

.user-info {
  padding: 15px 20px;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
}

.user-avatar {
  border: 3px solid var(--el-color-primary-light-8);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background-color: #67c23a;
  border-radius: 50%;
  border: 2px solid var(--el-bg-color);
}

.user-details {
  margin-top: 12px;
  transition: margin 0.3s ease;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0 4px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.vip-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 18px;
  line-height: 16px;
}

.user-id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

/* 用户统计信息 */
.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
  margin-bottom: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.user-menu {
  border-right: none;
  flex: 1;
  padding: 5px 0;
}

.user-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 2px 0;
  border-radius: 0 25px 25px 0;
  margin-right: 16px;
  padding: 0 20px;
}

.user-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}

.user-menu :deep(.el-menu-item:hover) {
  background-color: var(--el-color-primary-light-9);
}

.user-menu :deep(.el-menu-item .el-icon) {
  margin-right: 10px;
  font-size: 18px;
}

.logout-container {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #f56c6c;
  color: white;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: 100%;
    border-radius: 0;
    padding: 10px 0;
    background-color: var(--el-bg-color);
  }

  .mobile-close {
    display: block;
  }

  .user-info {
    display: flex;
    align-items: center;
    text-align: left;
    padding: 15px 20px;
  }

  .avatar-container {
    margin-bottom: 0;
  }

  .user-details {
    margin-left: 15px;
    margin-top: 0;
  }

  .user-name {
    justify-content: flex-start;
  }

  .user-menu :deep(.el-menu-item) {
    padding: 0 16px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .sidebar {
    background-color: var(--el-bg-color-overlay);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }

  .user-info,
  .user-stats,
  .logout-container {
    border-color: var(--el-border-color-darker);
  }

  .user-avatar {
    border-color: var(--el-color-primary-light-5);
  }

  .user-menu :deep(.el-menu-item.is-active) {
    background-color: rgba(64, 158, 255, 0.2);
  }

  .user-menu :deep(.el-menu-item:hover) {
    background-color: rgba(64, 158, 255, 0.1);
  }

  .avatar-status {
    border-color: var(--el-bg-color-overlay);
  }
}
.user-menu :deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
}
</style>
