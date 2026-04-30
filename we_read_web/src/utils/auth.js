/**
 * @file auth.js
 * @description 统一的认证相关工具函数，同时处理用户和管理员的认证
 * @created 2025-03-08
 * @updated 2025-03-21
 * @module utils/auth
 */

// Token 在 localStorage 中的键名
const USER_TOKEN_KEY = "weread_token";
const ADMIN_TOKEN_KEY = "admin_token";
const USER_INFO_KEY = "weread_user";
const ADMIN_INFO_KEY = "admin_info";
const USER_ACCOUNT_KEY = "weread_account";
const ADMIN_ACCOUNT_KEY = "admin_account";

/**
 * 获取存储的 token
 * @param {boolean} [isAdmin=false] - 是否为管理员 token
 * @returns {string|null} 存储的 token 或 null
 */
export function getToken(isAdmin = false) {
  const token = localStorage.getItem(isAdmin ? ADMIN_TOKEN_KEY : USER_TOKEN_KEY);
  // 增加对 "undefined" 或 "null" 字符串的检查，这些可能是由于错误的存储导致的
  if (!token || token === "undefined" || token === "null") {
    return null;
  }
  return token;
}

/**
 * 设置 token 到 localStorage
 * @param {string} token - 认证令牌
 * @param {boolean} [isAdmin=false] - 是否为管理员 token
 */
export function setToken(token, isAdmin = false) {
  const key = isAdmin ? ADMIN_TOKEN_KEY : USER_TOKEN_KEY;
  if (token) {
    localStorage.setItem(key, token);
  } else {
    localStorage.removeItem(key);
  }
}

/**
 * 移除存储的 token
 * @param {boolean} [isAdmin=false] - 是否为管理员 token
 */
export function removeToken(isAdmin = false) {
  localStorage.removeItem(isAdmin ? ADMIN_TOKEN_KEY : USER_TOKEN_KEY);
}

/**
 * 获取存储的用户或管理员信息
 * @param {boolean} [isAdmin=false] - 是否为管理员信息
 * @returns {Object|null} 用户或管理员信息对象或 null
 */
export function getUserInfo(isAdmin = false) {
  const key = isAdmin ? ADMIN_INFO_KEY : USER_INFO_KEY;
  const info = localStorage.getItem(key);
  return info ? JSON.parse(info) : null;
}

/**
 * 设置用户或管理员信息到 localStorage
 * @param {Object} userInfo - 用户或管理员信息对象
 * @param {boolean} [isAdmin=false] - 是否为管理员信息
 */
export function setUserInfo(userInfo, isAdmin = false) {
  const key = isAdmin ? ADMIN_INFO_KEY : USER_INFO_KEY;
  if (userInfo && Object.keys(userInfo).length > 0) {
    // 确保所有字段都被正确存储
    localStorage.setItem(key, JSON.stringify(userInfo));
  } else {
    localStorage.removeItem(key);
  }
}

/**
 * 移除存储的用户或管理员信息
 * @param {boolean} [isAdmin=false] - 是否为管理员信息
 */
export function removeUserInfo(isAdmin = false) {
  localStorage.removeItem(isAdmin ? ADMIN_INFO_KEY : USER_INFO_KEY);
}

/**
 * 获取记住的账号
 * @param {boolean} [isAdmin=false] - 是否为管理员账号
 * @returns {string|null} 记住的账号或 null
 */
export function getRememberedAccount(isAdmin = false) {
  return localStorage.getItem(isAdmin ? ADMIN_ACCOUNT_KEY : USER_ACCOUNT_KEY);
}

/**
 * 设置记住的账号
 * @param {string} account - 用户或管理员账号
 * @param {boolean} [isAdmin=false] - 是否为管理员账号
 */
export function setRememberedAccount(account, isAdmin = false) {
  const key = isAdmin ? ADMIN_ACCOUNT_KEY : USER_ACCOUNT_KEY;
  if (account) {
    localStorage.setItem(key, account);
  } else {
    localStorage.removeItem(key);
  }
}

/**
 * 移除记住的账号
 * @param {boolean} [isAdmin=false] - 是否为管理员账号
 */
export function removeRememberedAccount(isAdmin = false) {
  localStorage.removeItem(isAdmin ? ADMIN_ACCOUNT_KEY : USER_ACCOUNT_KEY);
}

/**
 * 清除所有认证相关的存储数据
 * @param {boolean} [isAdmin=false] - 是否为管理员数据
 * @param {boolean} [keepRememberedAccount=true] - 是否保留记住的账号
 */
export function clearAuthData(isAdmin = false, keepRememberedAccount = true) {
  removeToken(isAdmin);
  removeUserInfo(isAdmin);

  // 根据参数决定是否清除记住的账号
  if (!keepRememberedAccount) {
    removeRememberedAccount(isAdmin);
  }
}

/**
 * 检查用户或管理员是否已登录
 * @param {boolean} [isAdmin=false] - 是否为管理员
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn(isAdmin = false) {
  return !!getToken(isAdmin) && !!getUserInfo(isAdmin);
}

/**
 * 解析JWT Token（不验证签名）
 * @param {string} token - JWT Token字符串
 * @returns {Object|null} 解析后的payload部分或null
 */
export function parseJwt(token) {
  try {
    if (!token || typeof token !== "string" || !token.includes("."))
      return null;
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64Url = parts[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error("解析JWT Token失败:", e);
    return null;
  }
}

/**
 * 检查Token是否过期
 * @param {string} token - JWT Token字符串
 * @returns {boolean} 是否已过期
 */
export function isTokenExpired(token) {
  // 如果是 Mock Token，格式明显不对（不含点号），直接认为过期
  if (!token || !token.includes(".")) return true;

  const payload = parseJwt(token);
  if (!payload || !payload.exp) return true;

  // exp是Unix时间戳（秒），需要转换为毫秒
  const expDate = new Date(payload.exp * 1000);
  const now = new Date();

  // 提前5分钟判断过期，避免边界情况
  const earlyExpire = 5 * 60 * 1000; // 5分钟（毫秒）
  return now.getTime() > expDate.getTime() - earlyExpire;
}

// 导出常量，便于其他模块使用
export const AUTH_KEYS = {
  USER_TOKEN_KEY,
  ADMIN_TOKEN_KEY,
  USER_INFO_KEY,
  ADMIN_INFO_KEY,
  USER_ACCOUNT_KEY,
  ADMIN_ACCOUNT_KEY,
};
