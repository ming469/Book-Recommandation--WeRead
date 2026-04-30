<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <auth-decoration v-show="!isMobileView" />

    <!-- 右侧登录区域 -->
    <div class="login-container">
      <!-- 头部 LOGO 及导航 -->
      <div class="login-header">
        <div class="login-logo">
          <img src="@/assets/images/logo.png" alt="微读" />
          <span class="logo-text">微读</span>
        </div>
        <div class="header-action">
          <span class="action-text">还没有账号？</span>
          <el-button
            type="success"
            plain
            round
            size="small"
            @click="goToRegister"
          >
            立即注册
          </el-button>
        </div>
      </div>

      <!-- 登录表单区域 -->
      <div class="login-form-wrapper">
        <div class="form-header">
          <h1 class="welcome-title">欢迎回来</h1>
          <p class="welcome-subtitle">登录后获取更多精彩推荐</p>
        </div>

        <!-- 用户类型选择 -->
        <div class="user-type-selector">
          <el-radio-group v-model="loginForm.userType" size="large">
            <el-radio-button value="user">普通用户</el-radio-button>
            <el-radio-button value="admin">管理员</el-radio-button>
          </el-radio-group>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="formRules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="account">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><User /></el-icon>
              </div>
              <el-input
                v-model="loginForm.account"
                :placeholder="
                  loginForm.userType === 'admin' ? '管理员账号' : '用户名/邮箱'
                "
                class="custom-input"
                clearable
                @keyup.enter="focusPassword"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <el-input
                ref="passwordInputRef"
                v-model="loginForm.password"
                type="password"
                :placeholder="
                  loginForm.userType === 'admin' ? '管理员密码' : '密码'
                "
                class="custom-input"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">
              <span class="checkbox-text">记住我</span>
            </el-checkbox>
            <el-button
              link
              type="success"
              class="forget-link"
              @click="forgotPassword"
              v-if="loginForm.userType === 'user'"
            >
              忘记密码?
            </el-button>
          </div>

          <el-form-item class="form-buttons">
            <el-button
              type="success"
              class="login-btn"
              :loading="isSubmitting"
              native-type="submit"
            >
              <span class="btn-text">{{
                isSubmitting ? "登录中..." : "登 录"
              }}</span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 分割线 -->
        <div class="divider" v-if="loginForm.userType === 'user'">
          <span class="divider-text">其他登录方式</span>
        </div>

        <!-- 第三方登录：使用本地 SVG 文件 -->
        <div class="social-login" v-if="loginForm.userType === 'user'">
          <div class="social-btn wechat" @click="socialLogin('微信')">
            <img
              src="@/assets/icons/WeChat.svg"
              alt="WeChat 图标"
              class="social-icon"
            />
          </div>
          <div class="social-btn qq" @click="socialLogin('QQ')">
            <img
              src="@/assets/icons/QQ.svg"
              alt="QQ 图标"
              class="social-icon"
            />
          </div>
          <div class="social-btn weibo" @click="socialLogin('微博')">
            <img
              src="@/assets/icons/WeiBo.svg"
              alt="微博图标"
              class="social-icon"
            />
          </div>
        </div>
      </div>

      <!-- 页脚 -->
      <div class="login-footer">
        <p class="footer-text">
          登录即表示您同意我们的
          <el-link type="success" :underline="false">服务条款</el-link>和
          <el-link type="success" :underline="false">隐私政策</el-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * @file LoginView.vue
 * @description 微读平台登录页面组件，提供用户和管理员登录功能及UI界面
 * @created 2025-03-03
 * @updated 2025-03-05
 * @module views/LoginView
 */

import { ref, reactive, onMounted, onBeforeUnmount, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
import { useAdminStore } from "@/stores/modules/admin";
import AuthDecoration from "@/components/auth/AuthDecoration.vue";

/**
 * 路由实例，用于页面跳转
 * @constant {VueRouter}
 */
const router = useRouter();

/**
 * 用户状态管理
 * @constant {UserStore}
 */
const userStore = useUserStore();

/**
 * 管理员状态管理
 * @constant {AdminStore}
 */
const adminStore = useAdminStore();

/**
 * 表单引用，用于表单验证和操作
 * @type {Object|null}
 */
const loginFormRef = ref(null);

/**
 * 密码输入框引用，用于聚焦操作
 * @type {Object|null}
 */
const passwordInputRef = ref(null);

/**
 * 是否为移动视图标志
 * @type {boolean}
 */
const isMobileView = ref(window.innerWidth < 768);

/**
 * 表单提交状态标志
 * @type {boolean}
 */
const isSubmitting = ref(false);

/**
 * 登录表单数据对象
 * @type {Object}
 * @property {string} account - 用户账号
 * @property {string} password - 用户密码
 * @property {boolean} rememberMe - 是否记住账号
 * @property {string} userType - 用户类型（普通用户/管理员）
 */
const loginForm = reactive({
  account: "",
  password: "",
  rememberMe: false,
  userType: "user", // 默认为普通用户登录
});

/**
 * 表单验证规则
 * @type {Object}
 */
const formRules = {
  account: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 3, max: 50, message: "长度应在3到50个字符之间", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    {
      min: 6,
      max: 20,
      message: "密码长度应在6到20个字符之间",
      trigger: "blur",
    },
  ],
};

/**
 * 监听用户类型变化，清空表单
 */
watch(
  () => loginForm.userType,
  (newType) => {
    // 切换用户类型时清空表单
    loginForm.account = "";
    loginForm.password = "";

    // 重置表单验证
    if (loginFormRef.value) {
      loginFormRef.value.resetFields();
    }

    // 根据用户类型从本地存储加载账号
    const storageKey =
      newType === "admin" ? "weread_admin_account" : "weread_account";
    const savedAccount = localStorage.getItem(storageKey);
    if (savedAccount) {
      loginForm.account = savedAccount;
      loginForm.rememberMe = true;
    }
  }
);

/**
 * 窗口大小变化处理函数
 * @function
 */
const handleResize = () => {
  isMobileView.value = window.innerWidth < 768;
};

/**
 * 组件挂载时的初始化操作
 * 监听窗口大小变化，加载用户记住的账号
 */
onMounted(() => {
  // 监听窗口大小变化，更新移动视图状态
  window.addEventListener("resize", handleResize);

  // 从本地存储加载用户账号（如果存在）
  const savedAccount = localStorage.getItem("weread_account");
  if (savedAccount) {
    loginForm.account = savedAccount;
    loginForm.rememberMe = true;
  }

  // 处理URL中的重定向参数
  const redirectUrl = new URLSearchParams(window.location.search).get(
    "redirect"
  );
  if (redirectUrl) {
    sessionStorage.setItem("redirectUrl", redirectUrl);
  }
});

/**
 * 组件销毁前清理
 */
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});

/**
 * 聚焦到密码输入框
 * 在用户名输入完成按回车时触发
 * @function
 */
const focusPassword = () => {
  passwordInputRef.value.focus();
};

/**
 * 处理登录逻辑
 * 验证表单，调用API，处理结果
 * @async
 * @function
 * @returns {Promise<void>}
 */
const handleLogin = async () => {
  try {
    // 防止重复提交
    if (isSubmitting.value) return;

    // 验证表单
    await loginFormRef.value.validate();

    // 设置提交状态
    isSubmitting.value = true;

    if (loginForm.userType === "admin") {
      // 管理员登录逻辑
      await adminStore.login({
        account: loginForm.account,
        password: loginForm.password,
        rememberMe: loginForm.rememberMe,
      });

      // 保存管理员账号到本地存储
      if (loginForm.rememberMe) {
        localStorage.setItem("weread_admin_account", loginForm.account);
      } else {
        localStorage.removeItem("weread_admin_account");
      }

      // 登录成功提示
      ElMessage.success("管理员登录成功！");

      // 跳转到管理后台
      router.push({
        path: "/admin/dashboard",
        replace: true,
      });
    } else {
      // 普通用户登录逻辑
      console.log("执行普通用户登录", loginForm);

      await userStore.login({
        account: loginForm.account,
        password: loginForm.password,
        rememberMe: loginForm.rememberMe,
      });

      // 保存用户账号到本地存储
      if (loginForm.rememberMe) {
        localStorage.setItem("weread_account", loginForm.account);
      } else {
        localStorage.removeItem("weread_account");
      }

      // 获取重定向URL
      const redirect = router.currentRoute.value.query.redirect || "/";
      console.log("登录成功，重定向到:", redirect);

      // 直接跳转，不使用setTimeout
      router.push({
        path: redirect,
        replace: true,
      });
    }
  } catch (error) {
    console.error("登录失败:", error);
    ElMessage.error(error.message || "登录失败，请检查账号和密码");
  } finally {
    isSubmitting.value = false;
  }
};

/**
 * 忘记密码处理
 * @function
 */
const forgotPassword = () => {
  router.push("/forgot-password");
};

/**
 * 跳转到注册页
 * @function
 */
const goToRegister = () => {
  router.push("/register");
};

/**
 * 社交媒体登录处理
 * @param {string} platform - 平台名称
 * @function
 */
const socialLogin = (platform) => {
  ElMessage({
    type: "info",
    message: `正在尝试使用${platform}登录，该功能正在开发中`,
    duration: 2000,
  });
};
</script>

<style scoped>
/**
 * 微读平台 - 登录页面样式
 * 
 * 主要样式区块：
 * 1. 全局布局
 * 2. 右侧登录表单区域
 * 3. 响应式适配
 * 4. 深色模式支持
 */

/* ===== 1. 全局布局 ===== */
.login-page {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Helvetica,
    Arial, sans-serif;
}

/* ===== 2. 右侧登录表单区域 ===== */
.login-container {
  flex: 1;
  padding: 40px 10%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

/* 登录头部 */
.login-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 60px;
  width: 100%;
}

.login-logo {
  display: flex;
  align-items: center;
}

.login-logo img {
  height: 36px;
  width: auto;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-left: 8px;
  letter-spacing: 0.5px;
}

.header-action {
  display: flex;
  align-items: center;
}

.action-text {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

/* 表单区域 */
.login-form-wrapper {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 20px 0;
}

.form-header {
  margin-bottom: 36px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

/* 用户类型选择器 */
.user-type-selector {
  margin-bottom: 32px;
  text-align: center;
  position: relative;
}

/* 自定义单选按钮组样式 */
:deep(.el-radio-group) {
  display: inline-flex;
  background: #f5f7fa;
  padding: 4px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 360px;
}

/* 单选按钮样式 */
:deep(.el-radio-button) {
  flex: 1;
}

/* 单选按钮内部样式 */
:deep(.el-radio-button__inner) {
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  border: none !important;
  background: transparent !important;
  color: #606266;
  transition: all 0.3s ease;
  border-radius: 12px !important;
  height: 48px;
  line-height: 24px;
  width: 100%;
  box-shadow: none !important;
}

/* 选中状态样式 */
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #ffffff !important;
  color: #43a047 !important;
  box-shadow: 0 4px 10px rgba(67, 160, 71, 0.2) !important;
  transform: translateY(-1px);
}

/* 鼠标悬停效果 */
:deep(.el-radio-button__inner:hover) {
  color: #43a047;
}

/* 选中前后按钮边框处理 */
:deep(.el-radio-button:first-child .el-radio-button__inner),
:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 12px !important;
}

/* 添加动画效果 */
:deep(
    .el-radio-button__original-radio:checked + .el-radio-button__inner
  )::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(67, 160, 71, 0.05);
  border-radius: 12px;
  z-index: -1;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(67, 160, 71, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(67, 160, 71, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(67, 160, 71, 0);
  }
}

/* 自定义输入框样式 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 12px;
  transition: all 0.3s;
  height: 52px;
  width: 100%;
}

.input-wrapper:focus-within {
  background-color: #edf7ed;
  box-shadow: 0 0 0 2px rgba(67, 160, 71, 0.2);
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  color: #909399;
  font-size: 18px;
  flex-shrink: 0;
}

.custom-input {
  flex: 1;
  width: 100%;
}

:deep(.custom-input .el-input__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
  padding-left: 0;
  height: 52px;
  width: 100%;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: none !important;
}

:deep(.custom-input .el-input__inner) {
  height: 52px;
  font-size: 16px;
  width: 100%;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 20px;
  width: 100%;
}

.checkbox-text {
  font-size: 14px;
  color: #606266;
}

.forget-link {
  font-size: 14px;
  padding: 0;
}

/* 登录按钮 */
.form-buttons {
  margin-top: 24px;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(90deg, #43a047, #2e7d32);
  border: none;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
}

.login-btn:active {
  transform: translateY(0);
}

.btn-text {
  font-size: 16px;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin: 30px 0;
  color: #909399;
  width: 100%;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background-color: #e4e7ed;
}

.divider-text {
  padding: 0 16px;
  font-size: 14px;
}

/* 第三方登录图标 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  width: 100%;
}

.social-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 20px;
  transition: all 0.3s ease;
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #e4e7ed;
}

.social-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

/* 如果需要在本地 SVG 上再加一些特定尺寸，可以添加 .social-icon */
.social-icon {
  width: 24px;
  height: 24px;
  display: block;
}

.wechat:hover {
  border-color: #07c160;
}

.qq:hover {
  border-color: #12b7f5;
}

.weibo:hover {
  border-color: #e6162d;
}

/* 页脚 */
.login-footer {
  margin-top: auto;
  text-align: center;
  padding-top: 40px;
  width: 100%;
}

.footer-text {
  font-size: 13px;
  color: #909399;
}

/* ===== 3. 响应式适配 ===== */
@media (max-width: 992px) {
  .login-container {
    padding: 40px 5%;
  }
}

@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }

  .login-container {
    padding: 20px;
  }

  .login-header {
    margin-bottom: 40px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .login-form-wrapper {
    padding: 0;
  }

  .login-footer {
    padding-top: 30px;
  }

  /* 用户类型选择器响应式 */
  .user-type-selector {
    margin-bottom: 24px;
  }

  :deep(.el-radio-button__inner) {
    padding: 10px 16px;
    font-size: 15px;
    height: 44px;
  }

  :deep(.el-radio-group) {
    max-width: 100%;
  }
}

/* ===== 4. 深色模式支持 ===== */
@media (prefers-color-scheme: dark) {
  .login-page {
    background-color: #1a1a1a;
  }

  .login-container {
    background-color: #1a1a1a;
  }

  .logo-text {
    color: #e0e0e0;
  }

  .action-text {
    color: #a0a0a0;
  }

  .welcome-title {
    color: #e0e0e0;
  }

  .welcome-subtitle {
    color: #a0a0a0;
  }

  /* 用户类型选择器深色模式 */
  :deep(.el-radio-group) {
    background: #2d2d2d;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  :deep(.el-radio-button__inner) {
    color: #a0a0a0;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: #333333 !important;
    color: #4caf50 !important;
    box-shadow: 0 4px 10px rgba(76, 175, 80, 0.3) !important;
  }

  :deep(.el-radio-button__inner:hover) {
    color: #4caf50;
  }

  :deep(
      .el-radio-button__original-radio:checked + .el-radio-button__inner
    )::before {
    background: rgba(76, 175, 80, 0.1);
  }

  .input-wrapper {
    background-color: #2d2d2d;
  }

  .input-wrapper:focus-within {
    background-color: #283228;
    box-shadow: 0 0 0 2px rgba(67, 160, 71, 0.3);
  }

  .checkbox-text {
    color: #a0a0a0;
  }

  .divider::before,
  .divider::after {
    background-color: #383838;
  }

  .divider-text {
    color: #a0a0a0;
  }

  .social-btn {
    background-color: #2d2d2d;
    border-color: #383838;
    color: #a0a0a0;
  }

  .footer-text {
    color: #a0a0a0;
  }
}
</style>