/**
 * @file admin.js
 * @description 微读平台管理员状态管理模块
 * @created 2025-03-09
 * @updated 2025-03-21
 * @module stores/modules/admin
 */

import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import router from "@/router";
import { adminApi } from "@/api/admin";
import {
  handleApiError,
  isSuccessResponse,
  extractResponseData,
  extractResponseMessage,
} from "@/utils/api-helper";
import {
  getToken,
  setToken,
  getUserInfo,
  setUserInfo,
  clearAuthData,
  isTokenExpired,
  setRememberedAccount,
  removeRememberedAccount,
} from "@/utils/auth";

/**
 * 管理员状态管理
 * @returns {Object} 管理员状态和方法
 */
export const useAdminStore = defineStore("admin", () => {
  /**
   * 管理员信息
   * @type {Object|null}
   */
  const adminInfo = ref(getUserInfo(true) || null);

  /**
   * 管理员令牌
   * @type {string|null}
   */
  const token = ref(getToken(true) || null);

  /**
   * 管理员权限列表
   * @type {Array}
   */
  const permissions = ref([]);

  /**
   * 管理员角色
   * @type {string|null}
   */
  const role = ref(null);

  /**
   * 权限映射表 - 用于快速查找权限
   * @type {Object}
   */
  const permissionMap = ref({});

  /**
   * 加载状态
   * @type {boolean}
   */
  const loading = ref(false);

  /**
   * 是否已登录
   * @type {boolean}
   */
  const isLoggedIn = computed(() => !!token.value && !!adminInfo.value);

  /**
   * 管理员名称
   * @type {string}
   */
  const adminName = computed(() => adminInfo.value?.name || "管理员");

  /**
   * 管理员头像
   * @type {string}
   */
  const avatar = computed(
    () => adminInfo.value?.avatar || "/default-admin-avatar.png"
  );

  /**
   * 是否为超级管理员
   * @type {boolean}
   */
  const isSuperAdmin = computed(() => role.value === "super_admin");

  /**
   * 管理员登录
   * @param {Object} loginData - 登录信息
   * @param {string} loginData.account - 管理员账号
   * @param {string} loginData.password - 管理员密码
   * @param {boolean} loginData.rememberMe - 是否记住登录状态
   * @returns {Promise<void>}
   */
  const login = async (loginData) => {
    loading.value = true;
    try {
      const response = await adminApi.login(loginData);

      if (isSuccessResponse(response)) {
        const responseData = extractResponseData(response);

        // 保存令牌到本地存储
        setToken(responseData.token, true);
        token.value = responseData.token;

        // 保存管理员信息
        adminInfo.value = responseData.admin;
        setUserInfo(responseData.admin, true);

        // 保存角色信息
        role.value = responseData.admin.role;

        // 保存权限信息
        permissions.value = responseData.admin.permissions || [];

        // 构建权限映射表，用于快速查找
        buildPermissionMap(responseData.admin.permissions || []);

        // 处理"记住我"功能
        if (loginData.rememberMe) {
          setRememberedAccount(loginData.account, true);
        } else {
          removeRememberedAccount(true);
        }

        // 记录登录日志
        logAdminOperation("login", "管理员登录系统");

        ElMessage.success(extractResponseMessage(response) || "登录成功");
        return Promise.resolve(response);
      } else {
        throw new Error(extractResponseMessage(response) || "登录失败");
      }
    } catch (error) {
      handleApiError(error, { defaultMessage: "登录失败" });
      return Promise.reject(error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 获取管理员信息
   * @returns {Promise<void>}
   */
  const getAdminInfo = async () => {
    if (!token.value) {
      return Promise.reject(new Error("未登录"));
    }

    loading.value = true;
    try {
      const response = await adminApi.getInfo();

      if (isSuccessResponse(response)) {
        const responseData = extractResponseData(response);

        // 保存管理员信息
        adminInfo.value = responseData;
        setUserInfo(responseData, true);

        // 保存角色信息
        role.value = responseData.role;

        // 保存权限信息
        permissions.value = responseData.permissions || [];

        // 构建权限映射表，用于快速查找
        buildPermissionMap(responseData.permissions || []);

        return Promise.resolve(responseData);
      } else {
        throw new Error(
          extractResponseMessage(response) || "获取管理员信息失败"
        );
      }
    } catch (error) {
      // 如果是401错误，清除登录状态
      if (error.response?.status === 401) {
        resetState();
        router.push("/login");
      }

      handleApiError(error, { defaultMessage: "获取管理员信息失败" });
      return Promise.reject(error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 管理员登出
   * @returns {Promise<void>}
   */
  const logout = async () => {
    loading.value = true;
    try {
      // 确保在发送请求前token仍然存在
      if (!token.value) {
        throw new Error("未登录状态");
      }

      const response = await adminApi.logout();

      if (isSuccessResponse(response)) {
        ElMessage.success(extractResponseMessage(response) || "已安全退出系统");
      }

      return Promise.resolve(response);
    } catch (error) {
      handleApiError(error, { defaultMessage: "登出失败" });
      return Promise.reject(error);
    } finally {
      // 无论成功失败，最后才清除状态
      resetState();
      router.push("/login");
      loading.value = false;
    }
  };

  /**
   * 检查是否有指定权限
   * @param {string|Array} permissionName - 权限名称或权限名称数组
   * @param {boolean} requireAll - 当传入数组时，是否要求拥有所有权限，默认为false(拥有任一权限即可)
   * @returns {boolean} 是否有权限
   */
  const hasPermission = (permissionName, requireAll = false) => {
    // 超级管理员拥有所有权限
    if (isSuperAdmin.value) {
      return true;
    }

    // 如果未传入权限名，则默认有权限
    if (!permissionName) {
      return true;
    }

    // 处理权限数组
    if (Array.isArray(permissionName)) {
      if (requireAll) {
        // 要求拥有所有权限
        return permissionName.every((name) => permissionMap.value[name]);
      } else {
        // 拥有任一权限即可
        return permissionName.some((name) => permissionMap.value[name]);
      }
    }

    // 单个权限检查
    return !!permissionMap.value[permissionName];
  };

  /**
   * 检查是否有指定角色
   * @param {string|Array} roleName - 角色名称或角色名称数组
   * @returns {boolean} 是否有角色
   */
  const hasRole = (roleName) => {
    // 如果未传入角色名，则默认有权限
    if (!roleName) {
      return true;
    }

    // 处理角色数组
    if (Array.isArray(roleName)) {
      return roleName.includes(role.value);
    }

    // 单个角色检查
    return role.value === roleName;
  };

  /**
   * 更新管理员信息
   * @param {Object} info - 新的管理员信息
   * @returns {Promise<void>}
   */
  const updateAdminInfo = async (info) => {
    loading.value = true;
    try {
      const response = await adminApi.updateInfo(info);

      // 使用工具类处理响应
      if (isSuccessResponse(response)) {
        const responseData = extractResponseData(response);

        // 记录操作日志
        logAdminOperation("update_profile", "更新管理员个人信息");

        // 更新管理员信息
        adminInfo.value = {
          ...adminInfo.value,
          ...responseData,
        };
        setUserInfo(adminInfo.value, true);

        ElMessage.success(extractResponseMessage(response) || "信息更新成功");
        return Promise.resolve(response);
      } else {
        throw new Error(
          extractResponseMessage(response) || "更新管理员信息失败"
        );
      }
    } catch (error) {
      handleApiError(error, { defaultMessage: "更新管理员信息失败" });
      return Promise.reject(error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 修改管理员密码
   * @param {Object} passwordData - 密码数据
   * @param {string} passwordData.oldPassword - 旧密码
   * @param {string} passwordData.newPassword - 新密码
   * @returns {Promise<void>}
   */
  const changePassword = async (passwordData) => {
    loading.value = true;
    try {
      const response = await adminApi.changePassword(passwordData);

      // 使用工具类处理响应
      if (isSuccessResponse(response)) {
        // 记录操作日志
        logAdminOperation("change_password", "修改管理员密码");

        ElMessage.success(
          extractResponseMessage(response) || "密码修改成功，请重新登录"
        );

        // 修改密码后需要重新登录
        await logout();
        return Promise.resolve(response);
      } else {
        throw new Error(extractResponseMessage(response) || "修改密码失败");
      }
    } catch (error) {
      handleApiError(error, { defaultMessage: "修改密码失败" });
      return Promise.reject(error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 构建权限映射表，用于快速查找
   * @param {Array} permissionList - 权限列表
   * @private
   */
  const buildPermissionMap = (permissionList) => {
    const map = {};
    permissionList.forEach((permission) => {
      map[permission] = true;
    });
    permissionMap.value = map;
  };

  /**
   * 记录管理员操作日志
   * @param {string} action - 操作类型
   * @param {string} description - 操作描述
   * @param {Object} details - 操作详情
   * @private
   */
  const logAdminOperation = (action, description, details = {}) => {
    console.log(`管理员操作: ${description}`, {
      adminId: adminInfo.value?.id,
      adminName: adminInfo.value?.name,
      time: new Date().toISOString(),
      action,
      description,
      details,
    });
  };

  /**
   * 检查并执行权限验证
   * @param {string|Array} requiredPermission - 所需权限
   * @param {Function} callback - 有权限时执行的回调
   * @param {Function} [failCallback] - 无权限时执行的回调
   * @returns {*} 回调执行结果
   */
  const checkAndExecute = (requiredPermission, callback, failCallback) => {
    if (hasPermission(requiredPermission)) {
      return callback();
    } else {
      ElMessage.error("您没有执行此操作的权限");
      if (failCallback) {
        return failCallback();
      }
      return false;
    }
  };

  /**
   * 重置状态
   * @private
   */
  const resetState = () => {
    // 清除认证数据
    clearAuthData(true);

    // 重置状态
    token.value = null;
    adminInfo.value = null;
    role.value = null;
    permissions.value = [];
    permissionMap.value = {};
    loading.value = false;
  };

  /**
   * 获取管理员 token
   * @returns {string|null} 管理员 token
   */
  const getAdminToken = () => {
    return token.value;
  };

  /**
   * 检查管理员 token 是否过期
   * @returns {boolean} 是否已过期
   */
  const isAdminTokenExpired = () => {
    return isTokenExpired(token.value);
  };

  return {
    adminInfo,
    token,
    permissions,
    role,
    loading,
    isLoggedIn,
    adminName,
    avatar,
    isSuperAdmin,
    login,
    getAdminInfo,
    logout,
    hasPermission,
    hasRole,
    updateAdminInfo,
    changePassword,
    checkAndExecute,
    logAdminOperation,
    getAdminToken,
    isAdminTokenExpired,
    resetState,
  };
});
