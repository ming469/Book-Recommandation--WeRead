/**
 * @file request.js
 * @description 封装的axios请求工具，负责处理请求拦截、响应拦截以及错误统一处理。
 * @module utils/request
 * @updated 2025-03-21
 */

import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/modules/user";
import { useAdminStore } from "@/stores/modules/admin";
import { getToken, isTokenExpired } from "@/utils/auth";

/**
 * 获取API基础URL
 *
 * 优先从环境变量中获取，如果不存在则采用默认值
 *
 * @function getBaseURL
 * @returns {string} 返回API基础URL
 */
const getBaseURL = () => {
    return "/api";
};

/**
 * 创建axios实例，并配置基础URL、超时时间和默认请求头
 *
 * @constant {AxiosInstance} service - axios实例
 */
const service = axios.create({
    baseURL: getBaseURL(),
    timeout: 30000,
    headers: {
        "Content-Type": "application/json;charset=utf-8",
    },
});

// 标识是否正在刷新token
let isRefreshing = false;
// 存放等待刷新token完成后重新发起的请求回调队列
let requests = [];

/**
 * 判断请求是否为管理员API请求
 * @param {string} url - 请求URL
 * @returns {boolean} 是否为管理员API请求
 */
const isAdminRequest = (url) => {
    return url.startsWith("/admin/");
};

// 特殊：热搜接口允许匿名，避免401弹窗打断管理端
const isHotSearchRequest = (url) => url && url.includes("/user/behavior/hot-searches");

/**
 * 请求拦截器
 *
 * 每次请求前自动添加token至请求头，并在token即将过期时尝试刷新token。
 *
 * @function requestInterceptor
 * @param {object} config - axios请求配置对象
 * @returns {Promise<object>|object} 返回更新后的请求配置对象或等待token刷新后的Promise配置对象
 * @throws {Error} 请求出错时返回错误信息
 */
service.interceptors.request.use(
    (config) => {
        const url = config.url || "";
        if (url.includes("/admin/recommend/recompute") ||
            url.includes("/admin/users/batch-password") ||
            url.includes("/admin/users/batch-password/test")) {
            config.timeout = 60000;
        }
        // 判断是否为管理员API请求
        const isAdmin = isAdminRequest(url);

        // 获取对应的token
        const token = getToken(isAdmin);

        if (token) {
            // 检查token是否即将过期或格式错误
            if (isTokenExpired(token)) {
                // 如果是格式错误的 Token（非 JWT 格式），直接清理并退出
                if (!token.includes(".")) {
                    const userStore = useUserStore();
                    const adminStore = useAdminStore();
                    if (isAdmin) {
                        adminStore.logout();
                    } else {
                        userStore.logout();
                    }
                    return config;
                }

                // 如果是刷新token的请求，直接放行，带上旧token
                if (config.url.includes("/user/auth/refresh-token") ||
                    config.url.includes("/admin/auth/refresh-token")) {
                    config.headers.Authorization = `Bearer ${token}`;
                    return config;
                }

                // 如果token即将过期，则尝试刷新token
                if (!isRefreshing) {
                    isRefreshing = true;

                    // 根据请求类型选择对应的store
                    if (isAdmin) {
                        const adminStore = useAdminStore();
                        // 管理员token刷新逻辑（如果有的话）
                        // 这里假设管理员没有刷新token的API，直接退出登录
                        adminStore.logout().finally(() => {
                            isRefreshing = false;
                        });
                    } else {
                        const userStore = useUserStore();
                        userStore
                            .refreshToken()
                            .then(() => {
                                // 刷新成功后，执行所有等待的请求
                                requests.forEach((cb) => cb());
                                requests = [];
                            })
                            .catch(() => {
                                // 刷新失败，则退出登录
                                userStore.logout();
                            })
                            .finally(() => {
                                isRefreshing = false;
                            });
                    }
                }

                // 非刷新token请求加入等待队列
                if (!config.url.includes("/auth/refresh-token")) {
                    return new Promise((resolve) => {
                        requests.push(() => {
                            config.headers.Authorization = `Bearer ${getToken(isAdmin)}`;
                            resolve(config);
                        });
                    });
                }
            }

            // 正常情况下，将token添加到请求头
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        console.error("请求错误:", error);
        return Promise.reject(error);
    }
);

/**
 * 响应拦截器
 *
 * 统一处理响应数据格式、错误提示以及特定业务错误码的处理逻辑。
 *
 * @function responseInterceptor
 * @param {object} response - axios响应对象
 * @returns {Promise<object>} 返回标准格式的响应数据，或在错误情况下返回拒绝的Promise
 */
service.interceptors.response.use(
    (response) => {
        const res = response.data;
        const isAdmin = isAdminRequest(response.config.url);

        // 检查响应是否符合预期的标准结构
        if (res) {
            // 优先检查meta.code结构
            if (res.meta && res.meta.code !== undefined) {
                if (res.meta.code !== 200) {
                    if (isHotSearchRequest(response.config.url) && res.meta.code === 401) {
                        return { meta: { code: 200, message: "success" }, data: [] };
                    }
                    const message = res.meta.message || "请求错误";
                    if (!response.config._errorShown) {
                        ElMessage({ message, type: "error", duration: 5000 });
                        response.config._errorShown = true;
                    }
                    switch (res.meta.code) {
                        case 401:
                            handleUnauthorized(isAdmin);
                            break;
                        case 403: // 权限不足
                            ElMessage({
                                message: "没有权限访问该资源",
                                type: "error",
                                duration: 5000,
                            });
                            break;
                        case 429: // 请求过于频繁
                            ElMessage({
                                message: "请求过于频繁，请稍后再试",
                                type: "warning",
                                duration: 5000,
                            });
                            break;
                    }
                    return Promise.reject(new Error(message));
                }
                return res;
            }
            // 若响应结构为直接code格式
            else if (res.code !== undefined) {
                if (res.code !== 200) {
                    if (isHotSearchRequest(response.config.url) && res.code === 401) {
                        return { meta: { code: 200, message: "success" }, data: [] };
                    }
                    const message = res.message || "请求错误";
                    if (!response.config._errorShown) {
                        ElMessage({ message, type: "error", duration: 5000 });
                        response.config._errorShown = true;
                    }
                    switch (res.code) {
                        case 401:
                            handleUnauthorized(isAdmin);
                            break;
                        case 403:
                            ElMessage({
                                message: "没有权限访问该资源",
                                type: "error",
                                duration: 5000,
                            });
                            break;
                        case 429:
                            ElMessage({
                                message: "请求过于频繁，请稍后再试",
                                type: "warning",
                                duration: 5000,
                            });
                            break;
                    }
                    return Promise.reject(new Error(message));
                }
                return res;
            }
        }
        return res;
    },
    (error) => {
        if (error && error.config && isHotSearchRequest(error.config.url)) {
            return Promise.reject(error);
        }
        console.error("响应错误:", error);
        // 判断是否为管理员API请求
        const isAdmin = error.config && isAdminRequest(error.config.url);

        // 避免重复弹出错误提示
        if (!error._handled) {
            let message = "服务器错误";
            if (error.response) {
                if (error.response.data && error.response.data.message) {
                    message = error.response.data.message;
                } else if (
                    error.response.data &&
                    error.response.data.meta &&
                    error.response.data.meta.message
                ) {
                    message = error.response.data.meta.message;
                } else {
                    switch (error.response.status) {
                        case 400:
                            message = "请求参数错误";
                            break;
                        case 401:
                            message = "未授权，请重新登录";
                            handleUnauthorized(isAdmin);
                            break;
                        case 403:
                            message = "拒绝访问";
                            break;
                        case 404:
                            message = "请求的资源不存在";
                            break;
                        case 500:
                            message = "服务器内部错误";
                            break;
                        default:
                            message = `请求失败(${error.response.status})`;
                            break;
                    }
                }
            } else if (error.request) {
                if (error.message.includes("timeout")) {
                    message = "请求超时，请检查网络连接";
                } else {
                    message = "网络异常，无法连接到服务器";
                }
            } else {
                message = error.message || "请求配置错误";
            }
            ElMessage({
                message,
                type: "error",
                duration: 5000,
            });
            // 标记错误已处理，避免重复提示
            error._handled = true;
        }
        return Promise.reject(error);
    }
);

/**
 * 处理未授权（401）情况
 *
 * 当检测到401错误时，提示用户重新登录并执行相应的退出登录及重定向操作。
 *
 * @function handleUnauthorized
 * @param {boolean} isAdmin - 是否为管理员请求
 * @returns {void}
 */
function handleUnauthorized(isAdmin = false) {
    if (isAdmin) {
        const adminStore = useAdminStore();
        if (adminStore.isLoggedIn) {
            ElMessageBox.confirm("您的管理员登录状态已过期，请重新登录", "登录过期", {
                    confirmButtonText: "重新登录",
                    cancelButtonText: "取消",
                    type: "warning",
                })
                .then(() => {
                    adminStore.resetState();
                    window.location.href = "/login";
                })
                .catch(() => {
                    adminStore.resetState();
                });
        }
    } else {
        const userStore = useUserStore();
        if (userStore.isLoggedIn) {
            ElMessageBox.confirm("您的登录状态已过期，请重新登录", "登录过期", {
                    confirmButtonText: "重新登录",
                    cancelButtonText: "取消",
                    type: "warning",
                })
                .then(() => {
                    userStore.logout().then(() => {
                        const currentPath =
                            window.location.pathname + window.location.search;
                        window.location.href = `/login?redirect=${encodeURIComponent(
              currentPath
            )}`;
                    });
                })
                .catch(() => {
                    userStore.resetState();
                });
        }
    }
}

export default service;