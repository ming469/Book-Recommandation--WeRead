/**
 * @file user.js
 * @description 微读平台用户相关API接口
 * @created 2025-03-21
 * @module api/user
 */

import request from "@/utils/request";

/**
 * 用户API模块 - 提供用户相关API接口
 */
export const userApi = {
    /**
     * 用户登录
     * @param {Object} data - 登录信息
     * @param {string} data.account - 用户名或邮箱
     * @param {string} data.password - 密码
     * @param {boolean} data.rememberMe - 是否记住我
     * @returns {Promise<Object>} 用户信息及token
     */
    login(data) {
        return request({
            url: "/user/auth/login",
            method: "post",
            data,
        });
    },

    /** 获取阅读时长（秒） */
    getReadingTime(bookId) {
        return request({
            url: `/user/behavior/reading-time/${bookId}`,
            method: "get",
        });
    },

    /** 累加阅读时长（秒） */
    addReadingTime(data) {
        return request({
            url: "/user/behavior/reading-time",
            method: "post",
            data,
        });
    },

    /**
     * 用户注册
     * @param {Object} data - 注册信息
     * @param {string} data.username - 用户名
     * @param {string} data.email - 邮箱
     * @param {string} data.password - 密码
     * @returns {Promise<Object>} 注册结果
     */
    register(data) {
        return request({
            url: "/user/auth/register",
            method: "post",
            data,
        });
    },

    /**
     * 退出登录
     * @returns {Promise<Object>} 登出结果
     */
    logout() {
        return request({
            url: "/user/auth/logout",
            method: "post",
        });
    },

    /**
     * 获取当前登录用户信息
     * @returns {Promise<Object>} 用户信息
     */
    getInfo() {
        return request({
            url: "/user/profile",
            method: "get",
        });
    },

    /**
     * 刷新Token
     * @returns {Promise<Object>} 刷新结果，包含新token
     */
    refreshToken() {
        return request({
            url: "/user/auth/refresh-token",
            method: "post",
        });
    },

    /**
     * 修改用户密码
     * @param {Object} data - 密码修改信息
     * @param {string} data.oldPassword - 旧密码
     * @param {string} data.newPassword - 新密码
     * @returns {Promise<Object>} 修改结果
     */
    changePassword(data) {
        return request({
            url: "/user/change-password",
            method: "post",
            data,
        });
    },

    /**
     * 重置密码
     * @param {Object} data - 重置密码信息
     * @param {string} data.email - 用户邮箱
     * @returns {Promise<Object>} 重置结果
     */
    resetPassword(data) {
        return request({
            url: "/user/auth/reset-password",
            method: "post",
            data,
        });
    },

    /**
     * 更新用户信息
     * @param {Object} data - 用户资料
     * @returns {Promise<Object>} 更新结果
     */
    updateInfo(data) {
        return request({
            url: "/user/profile",
            method: "put",
            data,
        });
    },

    /**
     * 上传用户头像
     * @param {FormData} data - 包含头像文件的FormData
     * @returns {Promise<Object>} 上传结果，包含新头像URL
     */
    uploadAvatar(data) {
        return request({
            url: "/user/profile/avatar",
            method: "post",
            data,
            headers: {
                "Content-Type": "multipart/form-data",
            },
        });
    },

    /**
     * 验证邮箱
     * @param {Object} data - 验证信息
     * @param {string} data.token - 验证令牌
     * @returns {Promise<Object>} 验证结果
     */
    verifyEmail(data) {
        return request({
            url: "/user/auth/verify-email",
            method: "post",
            data,
        });
    },

    /**
     * 重新发送验证邮件
     * @param {Object} data - 邮箱信息
     * @param {string} data.email - 用户邮箱
     * @returns {Promise<Object>} 发送结果
     */
    resendVerificationEmail(data) {
        return request({
            url: "/user/auth/resend-verification",
            method: "post",
            data,
        });
    },

    /**
     * 获取用户收藏列表
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码，默认1
     * @param {number} params.size - 每页数量，默认10
     * @param {string} params.sort - 排序字段
     * @returns {Promise<Object>} 收藏列表
     */
    getFavorites(params) {
        return request({
            url: "/user/favorites",
            method: "get",
            params,
        });
    },

    /**
     * 添加收藏
     * @param {Object} data - 收藏信息
     * @param {string} data.bookId - 图书ID
     * @returns {Promise<Object>} 添加结果
     */
    addFavorite(data) {
        return request({
            url: "/user/favorites",
            method: "post",
            data,
        });
    },

    /**
     * 取消收藏
     * @param {string} bookId - 图书ID
     * @returns {Promise<Object>} 取消结果
     */
    removeFavorite(bookId) {
        return request({
            url: `/user/favorites/${bookId}`,
            method: "delete",
        });
    },

    /**
     * 检查图书是否已收藏
     * @param {string} bookId - 图书ID
     * @returns {Promise<Object>} 检查结果
     */
    checkFavorite(bookId) {
        return request({
            url: `/user/favorites/check/${bookId}`,
            method: "get",
        });
    },

    /** 书架：获取分页列表 */
    getShelf(params) {
        return request({
            url: "/user/shelf",
            method: "get",
            params,
        });
    },

    /** 书架：移除 */
    removeFromShelf(bookId) {
        return request({
            url: `/user/shelf/${bookId}`,
            method: "delete",
        });
    },

    /** 书架：加入（行为接口） */
    addToShelf(data) {
        return request({
            url: "/user/behavior/shelf",
            method: "post",
            data,
        });
    },
    /**
     * 获取用户阅读历史
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码，默认1
     * @param {number} params.size - 每页数量，默认10
     * @returns {Promise<Object>} 阅读历史列表
     */
    getReadHistory(params) {
        return request({
            url: "/user/history",
            method: "get",
            params,
        });
    },

    /**
     * 添加阅读历史
     * @param {Object} data - 阅读历史信息
     * @param {string} data.bookId - 图书ID
     * @param {number} data.progress - 阅读进度（百分比）
     * @returns {Promise<Object>} 添加结果
     */
    addReadHistory(data) {
        return request({
            url: "/user/history",
            method: "post",
            data,
        });
    },

    /**
     * 清空阅读历史
     * @returns {Promise<Object>} 清空结果
     */
    clearReadHistory() {
        return request({
            url: "/user/history",
            method: "delete",
        });
    },

    /**
     * 删除单条阅读历史
     * @param {string} historyId - 历史记录ID
     * @returns {Promise<Object>} 删除结果
     */
    deleteReadHistory(historyId) {
        return request({
            url: `/user/history/${historyId}`,
            method: "delete",
        });
    },

    /**
     * 获取用户阅读偏好
     * @returns {Promise<Object>} 阅读偏好
     */
    getReadingPreferences() {
        return request({
            url: "/user/behavior/preferences",
            method: "get",
        });
    },

    /**
     * 更新阅读偏好
     * @param {Object} data - 阅读偏好
     * @param {Array<string>} data.favoriteCategories - 喜欢的图书分类ID列表
     * @param {Array<string>} data.favoriteAuthors - 喜欢的作者ID列表
     * @param {Object} data.readingSettings - 阅读设置（字体大小、背景色等）
     * @returns {Promise<Object>} 更新结果
     */
    updateReadingPreferences(data) {
        return request({
            url: "/user/preferences/reading",
            method: "put",
            data,
        });
    },

    /**
     * 获取用户推荐图书
     * @param {Object} params - 查询参数
     * @param {number} params.page - 页码，默认1
     * @param {number} params.size - 每页数量，默认10
     * @returns {Promise<Object>} 推荐图书列表
     */
    getRecommendedBooks(params) {
        return request({
            url: "/user/behavior/recommendations",
            method: "get",
            params,
        });
    },

    /** 提交用户评分（0.5~5） */
    rateBook(data) {
        return request({
            url: "/user/behavior/rate",
            method: "post",
            data,
        });
    },

    /** 获取当前用户对某本书的个人评分 */
    getMyRate(bookId) {
        return request({
            url: `/user/behavior/my-rate/${bookId}`,
            method: "get",
        });
    },

    /** 发表评论 */
    addComment(data) {
        return request({
            url: "/user/behavior/comment",
            method: "post",
            data,
        });
    },
};