/**
 * @file user.js
 * @description 用户状态管理模块，管理用户登录状态、个人信息、权限等
 * @module stores/modules/user
 * @updated 2025-03-25
 */

import router from "@/router";
import { defineStore } from "pinia";
import { userApi } from "@/api/user";
import { ElMessage } from "element-plus";
import {
  getToken,
  setToken,
  getUserInfo,
  setUserInfo,
  clearAuthData,
  setRememberedAccount,
  removeRememberedAccount,
} from "@/utils/auth";
import {
  handleApiResponse,
  handleApiError,
  isSuccessResponse,
  extractResponseData,
  extractResponseMessage,
} from "@/utils/api-helper";

/**
 * 用户状态管理 Store
 */
export const useUserStore = defineStore("user", {
  /**
   * 状态定义
   */
  state: () => ({
    /** 用户令牌 */
    token: getToken() || "",
    /** 用户基本信息 */
    userInfo: getUserInfo() || {},
    /** 用户角色列表 */
    roles: [],
    /** 用户权限列表 */
    permissions: [],
    /** 登录加载状态 */
    loading: false,
  }),

  /**
   * 计算属性（Getters）
   */
  getters: {
    /**
     * 判断用户是否已登录：依赖 token 状态
     * @param {Object} state - 当前状态
     * @returns {boolean}
     */
    isLoggedIn: (state) => !!state.token,

    /**
     * 判断是否存在用户信息
     * @param {Object} state - 当前状态
     * @returns {boolean}
     */
    hasUserInfo: (state) =>
      !!state.userInfo && Object.keys(state.userInfo).length > 0,

    /**
     * 获取用户名，如不存在则返回空字符串
     * @param {Object} state - 当前状态
     * @returns {string}
     */
    username: (state) => state.userInfo?.username || "",

    /**
     * 获取昵称，如不存在则返回空字符串
     * @param {Object} state - 当前状态
     * @returns {string}
     */
    nickname: (state) => state.userInfo?.nickname || "",

    /**
     * 获取用户头像，如不存在则返回默认头像
     * @param {Object} state - 当前状态
     * @returns {string}
     */
    avatar: (state) => state.userInfo?.avatar || "default-avatar.png",

    /**
     * 获取用户邮箱，如不存在则返回空字符串
     * @param {Object} state - 当前状态
     * @returns {string}
     */
    email: (state) => state.userInfo?.email || "",

    /**
     * 判断用户是否拥有指定权限
     * @param {Object} state - 当前状态
     * @returns {function(string): boolean}
     */
    hasPermission: (state) => (permission) => {
      if (!permission) return true;
      return state.permissions.includes(permission);
    },

    /**
     * 判断用户是否拥有指定角色
     * @param {Object} state - 当前状态
     * @returns {function(string): boolean}
     */
    hasRole: (state) => (role) => {
      if (!role) return true;
      return state.roles.includes(role);
    },
  },

  /**
   * 操作方法（Actions）
   */
  actions: {
    /**
     * 用户登录
     * @param {Object} loginData - 登录数据，包括账号、密码及记住我选项
     * @returns {Promise<Object>} 登录接口响应
     */
    async login(loginData) {
      this.loading = true;
      try {
        const response = await userApi.login(loginData);

        // 使用工具类处理响应
        if (isSuccessResponse(response)) {
          const responseData = extractResponseData(response);

          // 提取token和用户信息
          const token = responseData.token || responseData.accessToken;
          const user =
            responseData.user || responseData.userInfo || responseData;

          if (!token) {
            throw new Error("登录响应中缺少token");
          }

          // 保存token
          this.token = token;
          setToken(token);

          // 保存基本用户信息
          if (user) {
            this.userInfo = { ...user };
            setUserInfo({ ...user });
          }

          // 处理"记住我"功能
          if (loginData.rememberMe) {
            setRememberedAccount(loginData.account);
          } else {
            removeRememberedAccount();
          }

          // 获取用户详细信息（包括角色和权限）
          try {
            await this.getUserInfo();
          } catch (infoError) {
            console.warn("获取用户详情失败，但登录已成功:", infoError);
            // 即使获取详情失败，也不影响登录成功
          }

          ElMessage.success(
            `欢迎回来，${user?.nickname || user?.username || "用户"}！`
          );
          return response;
        } else {
          throw new Error(extractResponseMessage(response) || "登录失败");
        }
      } catch (error) {
        console.error("登录失败:", error);
        this.resetState();
        handleApiError(error, { defaultMessage: "登录失败，请检查账号和密码" });
      } finally {
        this.loading = false;
      }
    },

    /**
     * 用户注册
     * @param {Object} registerData - 注册所需数据
     * @returns {Promise<Object>} 注册接口响应
     */
    async register(registerData) {
      this.loading = true;
      try {
        const response = await userApi.register(registerData);

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "注册成功，请登录",
          errorMessage: "注册失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "注册失败，请稍后再试" });
      } finally {
        this.loading = false;
      }
    },

    /**
     * 用户登出
     * @returns {Promise<void>}
     */
    async logout() {
      try {
        const token = this.token;
        if (!token) {
          this.resetState();
          router.push("/login");
          return;
        }

        const response = await userApi.logout({
          headers: { Authorization: `Bearer ${token}` },
        });

        // 使用工具类检查响应是否成功
        if (isSuccessResponse(response)) {
          ElMessage.success("您已安全退出登录。");
        } else {
          console.warn("登出响应异常:", response);
          ElMessage.warning("登出可能未完全成功，但已清除本地登录状态");
        }
      } catch (error) {
        console.error("登出时发生错误：", error);
        ElMessage.warning("登出失败，但已清除本地登录状态");
      } finally {
        // 清除本地存储的 Token 和用户信息
        this.resetState();
        router.push("/login");
      }
    },

    /**
     * 获取用户详细信息（包含角色和权限）
     * @returns {Promise<Object>} 用户信息数据
     */
    async getUserInfo() {
      try {
        const response = await userApi.getInfo();

        // 使用工具类处理响应
        if (isSuccessResponse(response)) {
          const responseData = extractResponseData(response);
          const userData = responseData.user || responseData;

          // 提取角色和权限，兼容不同的数据结构
          const roles = userData.roles || responseData.roles || [];
          const permissions =
            userData.permissions || responseData.permissions || [];

          // 更新状态，确保保留所有用户信息字段
          this.userInfo = { ...this.userInfo, ...userData };
          setUserInfo({ ...this.userInfo });
          this.roles = roles;
          this.permissions = permissions;

          return userData;
        } else {
          console.warn("获取用户信息响应异常:", response);
          throw new Error(
            extractResponseMessage(response) || "获取用户信息失败"
          );
        }
      } catch (error) {
        console.error("获取用户信息失败：", error);

        // 只在非401错误时显示错误消息，避免重复提示
        if (!error.response || error.response.status !== 401) {
          handleApiError(error, { defaultMessage: "获取用户信息失败" });
        }

        // token 可能已过期，若401则清除状态
        if (error.response?.status === 401) {
          this.resetState();
        }
        throw error;
      }
    },

    /**
     * 刷新用户 token
     * @returns {Promise<Object>} 刷新 token 接口响应
     */
    async refreshToken() {
      try {
        const response = await userApi.refreshToken();

        // 使用工具类处理响应
        if (isSuccessResponse(response)) {
          const responseData = extractResponseData(response);
          const token = responseData.token || responseData.accessToken;

          if (!token) {
            throw new Error("刷新Token响应中缺少token");
          }

          // 更新 token
          this.token = token;
          setToken(token);
          return response;
        } else {
          throw new Error(extractResponseMessage(response) || "刷新Token失败");
        }
      } catch (error) {
        console.error("刷新Token失败：", error);
        this.resetState();
        throw error;
      }
    },

    /**
     * 上传用户头像
     * @param {FormData} formData - 包含头像文件的表单数据
     * @returns {Promise<Object>} 上传头像接口响应
     */
    async uploadAvatar(formData) {
      try {
        const response = await userApi.uploadAvatar(formData);

        // 使用工具类处理响应
        if (isSuccessResponse(response)) {
          const responseData = extractResponseData(response);
          const avatarUrl = responseData.avatarUrl || responseData.avatar;

          if (!avatarUrl) {
            throw new Error("上传头像响应中缺少avatarUrl");
          }

          // 更新用户头像
          const updatedUser = { ...this.userInfo, avatar: avatarUrl };
          this.userInfo = updatedUser;
          setUserInfo(updatedUser);

          ElMessage.success("头像上传成功");
          return response;
        } else {
          throw new Error(extractResponseMessage(response) || "头像上传失败");
        }
      } catch (error) {
        handleApiError(error, { defaultMessage: "头像上传失败" });
      }
    },

    /**
     * 修改用户密码
     * @param {Object} passwordData - 包含原密码和新密码的数据
     * @returns {Promise<Object>} 修改密码接口响应
     */
    async changePassword(passwordData) {
      try {
        const response = await userApi.changePassword(passwordData);

        // 使用工具类处理响应
        if (isSuccessResponse(response)) {
          ElMessage.success(
            extractResponseMessage(response) || "密码修改成功，请重新登录"
          );

          // 密码修改成功后退出登录
          await this.logout();
          return response;
        } else {
          throw new Error(extractResponseMessage(response) || "密码修改失败");
        }
      } catch (error) {
        handleApiError(error, { defaultMessage: "密码修改失败" });
      }
    },

    /**
     * 获取未读通知数量（占位实现，避免运行时报错）
     * @returns {number} 未读数量
     */
    getUnreadNotificationCount() {
      return 0;
    },

    /**
     * 重置密码：发送重置密码邮件
     * @param {Object} resetData - 包含用户邮箱等信息
     * @returns {Promise<Object>} 重置密码接口响应
     */
    async resetPassword(resetData) {
      try {
        const response = await userApi.resetPassword(resetData);

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "密码重置邮件已发送，请查收邮箱",
          errorMessage: "密码重置失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "密码重置失败" });
      }
    },

    /**
     * 邮箱验证
     * @param {Object} verifyData - 验证所需数据
     * @returns {Promise<Object>} 邮箱验证接口响应
     */
    async verifyEmail(verifyData) {
      try {
        const response = await userApi.verifyEmail(verifyData);

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "邮箱验证成功",
          errorMessage: "邮箱验证失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "邮箱验证失败" });
      }
    },

    /**
     * 重新发送验证邮件
     * @param {Object} emailData - 包含用户邮箱的数据
     * @returns {Promise<Object>} 重新发送验证邮件接口响应
     */
    async resendVerificationEmail(emailData) {
      try {
        const response = await userApi.resendVerificationEmail(emailData);

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "验证邮件已重新发送，请查收邮箱",
          errorMessage: "发送验证邮件失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "发送验证邮件失败" });
      }
    },

    /**
     * 添加收藏
     * @param {string|number} bookId - 书籍 ID
     * @returns {Promise<Object>} 添加收藏接口响应
     */
    async addFavorite(bookId) {
      try {
        const response = await userApi.addFavorite({ bookId });

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "已添加到收藏",
          errorMessage: "添加收藏失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "添加收藏失败" });
      }
    },

    /**
     * 取消收藏
     * @param {string|number} bookId - 书籍 ID
     * @returns {Promise<Object>} 取消收藏接口响应
     */
    async removeFavorite(bookId) {
      try {
        const response = await userApi.removeFavorite(bookId);

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "已取消收藏",
          errorMessage: "取消收藏失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "取消收藏失败" });
      }
    },

    /**
     * 清空阅读历史
     * @returns {Promise<Object>} 清空阅读历史接口响应
     */
    async clearReadHistory() {
      try {
        const response = await userApi.clearReadHistory();

        // 使用工具类处理响应
        handleApiResponse(response, {
          successMessage: "阅读历史已清空",
          errorMessage: "清空阅读历史失败",
        });

        return response;
      } catch (error) {
        handleApiError(error, { defaultMessage: "清空阅读历史失败" });
      }
    },

    /**
     * 重置用户状态，清除所有登录及个人信息数据
     */
    resetState() {
      this.token = "";
      this.userInfo = {};
      this.roles = [];
      this.permissions = [];
      this.loading = false;
      // 清除本地存储的认证数据
      clearAuthData();
    },
  },
});
