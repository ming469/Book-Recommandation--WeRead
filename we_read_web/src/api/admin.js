/**
 * @file admin.js
 * @description 微读平台管理员相关API接口
 * @created 2025-03-21
 * @module api/admin
 */

import request from "@/utils/request";

/**
 * 管理员API接口
 */
export const adminApi = {
  /**
   * 管理员登录
   * @param {Object} data - 登录数据
   * @param {string} data.account - 管理员账号
   * @param {string} data.password - 管理员密码
   * @returns {Promise<Object>} 登录结果
   */
  login(data) {
    return request({
      url: "/admin/auth/login",
      method: "post",
      data,
    });
  },

  /**
   * 获取管理员信息
   * @returns {Promise<Object>} 管理员信息
   */
  getInfo() {
    return request({
      url: "/admin/profile",
      method: "get",
    });
  },

  /**
   * 管理员登出
   * @returns {Promise<Object>} 登出结果
   */
  logout() {
    return request({
      url: "/admin/auth/logout",
      method: "post",
    });
  },

  /**
   * 更新管理员信息
   * @param {Object} data - 管理员信息
   * @returns {Promise<Object>} 更新结果
   */
  updateInfo(data) {
    return request({
      url: "/admin/profile",
      method: "put",
      data,
    });
  },

  /**
   * 修改管理员密码
   * @param {Object} data - 密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise<Object>} 修改结果
   */
  changePassword(data) {
    return request({
      url: "/admin/profile/password",
      method: "put",
      data,
    });
  },

  /**
   * 获取系统统计数据
   * @returns {Promise<Object>} 统计数据
   */
  getStatistics() {
    return request({
      url: "/admin/dashboard/statistics",
      method: "get",
    });
  },

  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} [params.keyword] - 搜索关键词
   * @returns {Promise<Object>} 用户列表
   */
  getUserList(params) {
    return request({
      url: "/admin/users",
      method: "get",
      params,
    });
  },

  /** 重置用户密码（管理员设置新密码） */
  resetUserPassword(id, newPassword) {
    return request({
      url: `/admin/users/${id}/password`,
      method: "put",
      data: { newPassword },
    });
  },

  /** 批量重置测试用户密码（user_ 前缀全体） */
  batchResetTestUserPasswords(newPassword) {
    return request({
      url: "/admin/users/batch-password/test",
      method: "post",
      data: { newPassword },
    });
  },

  /** 删除用户 */
  deleteUser(id) {
    return request({
      url: `/admin/users/${id}`,
      method: "delete",
    });
  },

  /**
   * 获取图书列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} [params.keyword] - 搜索关键词
   * @param {number} [params.categoryId] - 图书分类ID
   * @param {number} [params.status] - 图书状态
   * @param {number} [params.hasEbook] - 是否有电子书
   * @returns {Promise<Object>} 图书列表
   */
  getBookList(params) {
    return request({
      url: "/admin/books",
      method: "get",
      params,
    });
  },

  /**
   * 获取图书详情
   * @param {number} id - 图书ID
   * @returns {Promise<Object>} 图书详情
   */
  getBookDetail(id) {
    return request({
      url: `/admin/books/${id}`,
      method: "get",
    });
  },

  /**
   * 添加图书
   * @param {Object} data - 图书数据
   * @returns {Promise<Object>} 添加结果
   */
  addBook(data) {
    return request({
      url: "/admin/books",
      method: "post",
      data,
    });
  },

  /**
   * 更新图书
   * @param {number} id - 图书ID
   * @param {Object} data - 图书数据
   * @returns {Promise<Object>} 更新结果
   */
  updateBook(id, data) {
    return request({
      url: `/admin/books/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除图书
   * @param {number} id - 图书ID
   * @returns {Promise<Object>} 删除结果
   */
  deleteBook(id) {
    return request({
      url: `/admin/books/${id}`,
      method: "delete",
    });
  },

  /**
   * 获取最近活动日志
   */
  getActivities() {
    return request({
      url: "/admin/dashboard/activities",
      method: "get",
    });
  },

  /**
   * 获取待办事项
   */
  getTodos() {
    return request({
      url: "/admin/dashboard/todos",
      method: "get",
    });
  },

  /**
   * 添加待办事项
   */
  addTodo(data) {
    return request({
      url: "/admin/dashboard/todos",
      method: "post",
      data,
    });
  },

  /**
   * 更新待办事项状态
   */
  updateTodo(id, data) {
    return request({
      url: `/admin/dashboard/todos/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除待办事项
   */
  deleteTodo(id) {
    return request({
      url: `/admin/dashboard/todos/${id}`,
      method: "delete",
    });
  },

  /**
   * 批量删除图书
   * @param {Array<number>} ids - 图书ID数组
   * @returns {Promise<Object>} 删除结果
   */
  batchDeleteBooks(ids) {
    return request({
      url: "/admin/books/batch-delete",
      method: "post",
      data: { ids },
    });
  },

  /**
   * 更新图书状态
   * @param {number} id - 图书ID
   * @param {number} status - 图书状态：1-上架，0-下架
   * @returns {Promise<Object>} 更新结果
   */
  updateBookStatus(id, status) {
    return request({
      url: `/admin/books/${id}/status`,
      method: "put",
      data: { status },
    });
  },

  /**
   * 获取分类列表
   * @returns {Promise<Object>} 分类列表
   */
  getCategoryList() {
    return request({
      url: "/admin/categories",
      method: "get",
    });
  },

  /**
   * 上传文件
   * @param {FormData} formData - 包含文件的表单数据
   * @returns {Promise<Object>} 上传结果
   */
  uploadFile(formData) {
    return request({
      url: "/admin/upload",
      method: "post",
      data: formData,
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  /**
   * 上传电子书文件
   * @param {FormData} formData - 包含电子书文件的表单数据
   * @returns {Promise<Object>} 上传结果
   */
  uploadEbook(formData) {
    return request({
      url: "/admin/upload/ebook",
      method: "post",
      data: formData,
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  /**
   * 导出图书数据
   * @param {Object} params - 查询参数
   * @returns {Promise<Blob>} 导出的文件数据
   */
  exportBooks(params) {
    return request({
      url: "/admin/books/export",
      method: "get",
      params,
      responseType: "blob",
    });
  },

  /**
   * 导入图书数据
   * @param {FormData} formData - 包含导入文件的表单数据
   * @returns {Promise<Object>} 导入结果
   */
  importBooks(formData) {
    return request({
      url: "/admin/books/import",
      method: "post",
      data: formData,
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  /**
   * 获取系统配置
   * @returns {Promise<Object>} 系统配置
   */
  getSystemConfig() {
    return request({
      url: "/admin/system/config",
      method: "get",
    });
  },

  /**
   * 更新系统配置
   * @param {Object} data - 系统配置
   * @returns {Promise<Object>} 更新结果
   */
  updateSystemConfig(data) {
    return request({
      url: "/admin/system/config",
      method: "put",
      data,
    });
  },

  /** 触发推荐相似度重算 */
  triggerRecommendRecompute() {
    return request({
      url: "/admin/recommend/recompute",
      method: "post",
    });
  },

  /** 清理缓存（按 pattern） */
  evictCache(pattern) {
    return request({
      url: "/admin/system/cache/evict",
      method: "post",
      params: { pattern },
    });
  },

  /** 管理员列表 */
  getAdminList(params) {
    return request({
      url: "/admin/admins",
      method: "get",
      params,
    });
  },

  /** 新增管理员 */
  createAdmin(data) {
    return request({
      url: "/admin/admins",
      method: "post",
      data,
    });
  },

  /** 更新管理员 */
  updateAdmin(id, data) {
    return request({
      url: `/admin/admins/${id}`,
      method: "put",
      data,
    });
  },

  /** 删除管理员 */
  deleteAdmin(id) {
    return request({
      url: `/admin/admins/${id}`,
      method: "delete",
    });
  },

  /** 重置管理员密码 */
  resetAdminPassword(id, newPassword) {
    return request({
      url: `/admin/admins/${id}/password`,
      method: "put",
      data: { newPassword },
    });
  },
};
