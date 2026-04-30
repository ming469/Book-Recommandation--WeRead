/**
 * @file permission.js
 * @description 路由权限控制模块，主要负责全局路由前置、后置守卫以及错误处理。
 * @module router/permission
 * @created 2025-03-08
 * @updated 2025-03-15
 */

import router from "./index";
import { useUserStore } from "@/stores/modules/user";
import { useAdminStore } from "@/stores/modules/admin";
import { getToken } from "@/utils/auth";
import { ElMessage } from "element-plus";

// 路由白名单（无需登录即可访问的路径）
const WHITE_LIST = [
  "/",
  "/login",
  "/register",
  "/reset-password",
  "/verify-email",
  "/search",
  "/find",
  "/about",
  "/404",
  "/403",
];

// 前缀白名单（允许以这些路径开头的路由无需登录）
const PREFIX_WHITE_LIST = ["/book/", "/category/", "/categories/", "/ranking/"];

// 管理员路径前缀
const ADMIN_PATH_PREFIX = "/admin";

/**
 * 判断指定路径是否属于白名单
 * @param {string} path - 当前访问路径
 * @returns {boolean} 若路径在白名单中返回 true，否则返回 false
 */
function isInWhiteList(path) {
  // 精确匹配检查
  if (WHITE_LIST.includes(path)) {
    console.log(`路径 ${path} 在白名单中（精确匹配）`);
    return true;
  }

  // 前缀匹配检查
  for (const prefix of PREFIX_WHITE_LIST) {
    if (path.startsWith(prefix)) {
      console.log(`路径 ${path} 在白名单中（前缀匹配: ${prefix}）`);
      return true;
    }
  }

  console.log(`路径 ${path} 不在白名单中`);
  return false;
}

/**
 * 判断是否为管理员路径
 * @param {string} path - 当前访问路径
 * @returns {boolean} 若是管理员路径返回 true，否则返回 false
 */
function isAdminPath(path) {
  return path.startsWith(ADMIN_PATH_PREFIX);
}

/**
 * 全局前置路由守卫
 * 根据用户 token 和路由 meta 信息判断是否允许访问目标路由
 */
router.beforeEach(async (to, from, next) => {
  // 设置页面标题（优先使用路由 meta 中定义的 title）
  document.title = to.meta.title || "微读 - 找到感兴趣的书";

  // 打印导航详细信息
  console.log("=====================================================");
  console.log(`导航: 从 ${from.path} 到 ${to.path}`);
  console.log("目标路由 meta:", to.meta);
  console.log("目标路由需要认证:", !!to.meta.requiresAuth);
  console.log("目标路由需要管理员认证:", !!to.meta.requiresAdminAuth);

  // 获取 token 和用户状态
  const token = getToken();
  const userStore = useUserStore();
  const adminStore = useAdminStore();
  console.log("当前 token 状态:", !!token);

  // 检查是否为管理员路径
  const isAdmin = isAdminPath(to.path);
  console.log("是否为管理员路径:", isAdmin);

  // 管理员路径处理逻辑
  if (isAdmin) {
    // 如果是管理员登录页，允许直接访问
    if (to.path === "/admin/login") {
      next();
      return;
    }

    // 检查管理员是否已登录
    if (adminStore.isLoggedIn) {
      console.log("管理员已登录，允许访问管理员路径");

      // 如果没有管理员信息，尝试获取
      if (!adminStore.adminInfo) {
        try {
          console.log("尝试获取管理员信息");
          await adminStore.getAdminInfo();
          next();
        } catch (error) {
          console.error("获取管理员信息失败，重定向到登录页", error);
          adminStore.logout();
          next({
            path: "/login",
            query: { redirect: to.fullPath },
            replace: true,
          });
        }
      } else {
        next();
      }
    } else {
      console.log("管理员未登录，重定向到登录页");
      next({
        path: "/login",
        query: { redirect: to.fullPath },
        replace: true,
      });
      ElMessage.warning("请先登录管理员账号");
    }
    return;
  }

  // 普通用户路径处理逻辑
  if (token) {
    console.log("用户已登录（存在 token）");

    // 已登录用户访问登录页时，直接重定向到首页
    if (to.path === "/login") {
      console.log("已登录用户访问登录页，重定向到首页");
      next("/");
      return;
    }

    // 检查是否已获取用户信息
    if (!userStore.hasUserInfo) {
      console.log("用户有 token 但缺少用户信息，尝试获取用户信息");
      try {
        await userStore.getUserInfo();
        console.log("成功获取用户信息，继续导航");
        next();
      } catch (error) {
        console.error("获取用户信息失败，清除登录状态并重定向到登录页", error);
        userStore.resetState();
        next({
          path: "/login",
          query: { redirect: to.fullPath },
          replace: true,
        });
      }
    } else {
      // 用户信息已存在，根据路由是否需要权限打印相应日志
      if (to.meta.requiresAuth) {
        console.log("目标路径需要权限，用户已登录，允许访问");
      } else {
        console.log("目标路径不需要权限，允许访问");
      }
      next();
    }
  } else {
    // 用户未登录逻辑
    console.log("用户未登录");

    // 针对需要认证路径进行拦截
    if (to.meta.requiresAuth) {
      console.log("目标路径需要权限但用户未登录，重定向到登录页");
      next({
        path: "/login",
        query: { redirect: to.fullPath },
        replace: true,
      });
      ElMessage.warning("请先登录后再访问此页面");
    } else if (isInWhiteList(to.path)) {
      // 白名单内路径允许直接访问
      console.log("目标路径在白名单中，允许访问");
      next();
    } else {
      // 其他情况均要求登录
      console.log("目标路径不在白名单中且需要认证，重定向到登录页");
      next({
        path: "/login",
        query: { redirect: to.fullPath },
        replace: true,
      });
    }
  }
});

/**
 * 全局后置路由守卫
 * 用于在路由跳转完成后结束加载进度条并打印导航结束日志
 */
router.afterEach((to, from) => {
  console.log(`导航完成: 从 ${from.path} 到 ${to.path}`);
  console.log("=====================================================");
});

/**
 * 路由错误统一处理
 */
router.onError((error) => {
  console.error("路由错误:", error);
});

export default router;
