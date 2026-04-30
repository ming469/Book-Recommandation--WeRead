<template>
  <div class="register-page">
    <!-- 左侧装饰区 -->
    <auth-decoration
      v-show="!isMobileView"
      title="加入微读，畅享阅读"
      subtitle="和我们一起探索吧，就现在"
    />

    <!-- 右侧注册区域 -->
    <div class="register-container">
      <!-- 头部 LOGO 及导航 -->
      <div class="register-header">
        <div class="register-logo">
          <img src="@/assets/images/logo.png" alt="微读" />
          <span class="logo-text">微读</span>
        </div>
        <div class="header-action">
          <span class="action-text">已有账号？</span>
          <el-button type="success" plain round size="small" @click="goToLogin">
            立即登录
          </el-button>
        </div>
      </div>

      <!-- 注册表单区域 -->
      <div class="register-form-wrapper">
        <div class="form-header">
          <h1 class="welcome-title">创建账号</h1>
          <p class="welcome-subtitle">注册成为微读用户，开启您的阅读之旅</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="formRules"
          class="register-form"
          size="large"
          @submit.prevent="handleRegister"
        >
          <!-- 用户名输入 -->
          <el-form-item prop="username">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><User /></el-icon>
              </div>
              <el-input
                v-model="registerForm.username"
                placeholder="用户名（3-20个字符）"
                class="custom-input"
                clearable
              />
            </div>
          </el-form-item>

          <!-- 邮箱输入 -->
          <el-form-item prop="email">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><Message /></el-icon>
              </div>
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱地址"
                class="custom-input"
                clearable
              />
            </div>
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（6-20个字符）"
                class="custom-input"
                show-password
                clearable
              />
            </div>
          </el-form-item>

          <!-- 确认密码输入 -->
          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <div class="input-icon">
                <el-icon><Check /></el-icon>
              </div>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                class="custom-input"
                show-password
                clearable
              />
            </div>
          </el-form-item>

          <!-- 用户协议 -->
          <el-form-item prop="agreement" class="agreement-item">
            <el-checkbox v-model="registerForm.agreement">
              <span class="checkbox-text">
                我已阅读并同意
                <el-link type="success" :underline="false">《用户协议》</el-link
                >和
                <el-link type="success" :underline="false"
                  >《隐私政策》</el-link
                >
              </span>
            </el-checkbox>
          </el-form-item>

          <el-form-item class="form-buttons">
            <el-button
              type="success"
              class="register-btn"
              :loading="isSubmitting"
              native-type="submit"
            >
              <span class="btn-text">{{
                isSubmitting ? "注册中..." : "立即注册"
              }}</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 页脚 -->
      <div class="register-footer">
        <p class="footer-text">
          注册微读代表您已同意
          <el-link type="success" :underline="false">服务条款</el-link> 和
          <el-link type="success" :underline="false">隐私政策</el-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * @file RegisterView.vue
 * @description 微读平台注册页面组件，提供用户注册功能及UI界面
 * @updated 2025-03-14
 * @module views/common/RegisterView
 */

import { ref, reactive, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Message, Lock, Check } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
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
 * 表单引用，用于表单验证和操作
 * @type {Object|null}
 */
const registerFormRef = ref(null);

/**
 * 是否为移动视图标志
 * @type {boolean}
 */
const isMobileView = ref(window.innerWidth < 768);

/**
 * 窗口大小变化处理函数
 * @function
 */
const handleResize = () => {
  isMobileView.value = window.innerWidth < 768;
};

/**
 * 组件挂载时的初始化操作
 * 设置窗口大小监听
 */
onMounted(() => {
  // 监听窗口大小变化，更新移动视图状态
  window.addEventListener("resize", handleResize);
});

/**
 * 组件销毁前清理事件监听
 */
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});

/**
 * 表单提交状态标志
 * @type {boolean}
 */
const isSubmitting = ref(false);

/**
 * 注册表单数据对象
 * @type {Object}
 */
const registerForm = reactive({
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
  agreement: false,
});

/**
 * 自定义验证函数：确认密码是否与密码一致
 * @param {Object} rule - 验证规则
 * @param {string} value - 表单项的值
 * @param {Function} callback - 回调函数
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

/**
 * 自定义验证函数：用户协议是否勾选
 * @param {Object} rule - 验证规则
 * @param {boolean} value - 表单项的值
 * @param {Function} callback - 回调函数
 */
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请阅读并同意用户协议和隐私政策"));
  } else {
    callback();
  }
};

/**
 * 表单验证规则
 * @type {Object}
 */
const formRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 3,
      max: 20,
      message: "用户名长度应在3到20个字符之间",
      trigger: "blur",
    },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: "用户名只能包含字母、数字、下划线和中文",
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    {
      min: 6,
      max: 20,
      message: "密码长度应在6到20个字符之间",
      trigger: "blur",
    },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
      message: "密码必须包含大小写字母和数字",
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
  agreement: [{ validator: validateAgreement, trigger: "change" }],
};

/**
 * 处理注册逻辑
 * 验证表单，调用API，处理结果
 * @async
 * @function
 * @returns {Promise<void>}
 */
const handleRegister = async () => {
  // 表单验证
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    try {
      // 设置提交状态
      isSubmitting.value = true;

      // 准备注册数据
      const registerData = {
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
      };

      // 调用store中的注册方法
      await userStore.register(registerData);

      // 注册成功提示
      ElMessage({
        type: "success",
        message: "注册成功，即将跳转到登录页面",
        duration: 2000,
      });

      // 重置表单
      registerFormRef.value.resetFields();

      // 延迟跳转到登录页
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } catch (error) {
      // 注册失败提示
      ElMessage.error(error.message || "注册失败，请稍后重试");
    } finally {
      // 重置提交状态
      isSubmitting.value = false;
    }
  });
};

/**
 * 跳转到登录页
 * @function
 */
const goToLogin = () => {
  router.push("/login");
};
</script>

<style scoped>
/**
 * 微读平台 - 注册页面样式
 */

/* ===== 1. 全局布局 ===== */
.register-page {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Helvetica,
    Arial, sans-serif;
}

/* ===== 2. 右侧注册表单区域 ===== */
.register-container {
  flex: 1;
  padding: 40px 10%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

/* 注册头部 */
.register-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 60px;
  width: 100%;
}

.register-logo {
  display: flex;
  align-items: center;
}

.register-logo img {
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
.register-form-wrapper {
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

/* 协议选项 */
.agreement-item {
  margin-top: 8px;
  margin-bottom: 20px;
  width: 100%;
}

.checkbox-text {
  font-size: 14px;
  color: #606266;
}

/* 注册按钮 */
.form-buttons {
  margin-top: 24px;
  width: 100%;
}

.register-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(90deg, #43a047, #2e7d32);
  border: none;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
}

.register-btn:active {
  transform: translateY(0);
}

.btn-text {
  font-size: 16px;
}

/* 页脚 */
.register-footer {
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
  .register-container {
    padding: 40px 5%;
  }
}

@media (max-width: 768px) {
  .register-page {
    flex-direction: column;
  }

  .register-container {
    padding: 20px;
  }

  .register-header {
    margin-bottom: 40px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .register-form-wrapper {
    padding: 0;
  }

  .register-footer {
    padding-top: 30px;
  }
}

/* ===== 4. 深色模式支持 ===== */
@media (prefers-color-scheme: dark) {
  .register-page {
    background-color: #1a1a1a;
  }

  .register-container {
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

  .footer-text {
    color: #a0a0a0;
  }
}
</style>