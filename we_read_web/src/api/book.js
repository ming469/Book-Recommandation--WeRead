/**
 * @file book.js
 * @description 微读平台图书相关API接口
 * @created 2025-04-10
 * @module api/book
 */

import request from "@/utils/request";

/**
 * 图书API模块 - 提供图书相关API接口
 */
export const bookApi = {
    /**
     * 获取图书列表
     * @param {Object} params - 查询参数
     * @param {number} [params.page=1] - 页码
     * @param {number} [params.size=10] - 每页数量
     * @param {string} [params.sort] - 排序方式
     * @returns {Promise<Object>} 图书列表
     */
    getBooks(params = {}) {
        return request({
            url: "/books",
            method: "get",
            params,
        });
    },

    /**
     * 获取图书详情
     * @param {string|number} id - 图书ID
     * @returns {Promise<Object>} 图书详情
     */
    getBookById(id) {
        return request({
            url: `/books/${id}`,
            method: "get",
        });
    },

    /**
     * 搜索图书
     * @param {Object} params - 搜索参数
     * @param {string} params.keyword - 搜索关键词
     * @param {number} [params.page=1] - 页码
     * @param {number} [params.size=10] - 每页数量
     * @param {string} [params.category] - 分类
     * @param {string} [params.sort] - 排序方式
     * @returns {Promise<Object>} 搜索结果
     */
    searchBooks(params) {
        return request({
            url: "/books/search",
            method: "get",
            params,
        });
    },

    /**
     * 获取推荐图书
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=10] - 返回数量
     * @returns {Promise<Object>} 推荐图书列表
     */
    getRecommendedBooks(params = {}) {
        return request({
            url: "/books/recommended",
            method: "get",
            params,
        });
    },

    /**
     * 获取最新图书
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=10] - 返回数量
     * @returns {Promise<Object>} 最新图书列表
     */
    getLatestBooks(params = {}) {
        return request({
            url: "/books/latest",
            method: "get",
            params,
        });
    },

    /**
     * 获取最受欢迎图书
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=10] - 返回数量
     * @returns {Promise<Object>} 最受欢迎图书列表
     */
    getPopularBooks(params = {}) {
        return request({
            url: "/books/popular",
            method: "get",
            params,
        });
    },

    /**
     * 获取收藏最多图书
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=10] - 返回数量
     * @returns {Promise<Object>} 收藏最多图书列表
     */
    getMostCollectedBooks(params = {}) {
        return request({
            url: "/books/collected",
            method: "get",
            params,
        });
    },

    /**
     * 根据分类获取图书
     * @param {string} category - 图书分类
     * @param {Object} params - 查询参数
     * @param {number} [params.page=1] - 页码
     * @param {number} [params.size=10] - 每页数量
     * @param {string} [params.sort] - 排序方式
     * @returns {Promise<Object>} 分类图书列表
     */
    getBooksByCategory(categoryOrId, params = {}) {
        const p = { ...params };
        const maybeNum = Number(categoryOrId);
        if (!Number.isNaN(maybeNum) && String(categoryOrId).trim() !== "") {
            p.categoryId = maybeNum;
        } else {
            p.category = categoryOrId;
        }
        return request({
            url: "/books/by-category",
            method: "get",
            params: p,
        });
    },

    /**
     * 获取所有图书分类
     * @returns {Promise<Object>} 分类列表
     */
    getCategories() {
        return request({
            url: "/categories",
            method: "get",
        });
    },

    /**
     * 获取图书评论
     * @param {string|number} bookId - 图书ID
     * @param {Object} params - 查询参数
     * @param {number} [params.page=1] - 页码
     * @param {number} [params.size=10] - 每页数量
     * @returns {Promise<Object>} 评论列表
     */
    getBookComments(bookId, params = {}) {
        return request({
            url: `/books/${bookId}/comments`,
            method: "get",
            params,
        });
    },

    /**
     * 添加图书评论
     * @param {string|number} bookId - 图书ID
     * @param {Object} data - 评论数据
     * @param {string} data.content - 评论内容
     * @param {number} data.rating - 评分（1-5）
     * @returns {Promise<Object>} 添加结果
     */
    addBookComment(bookId, data) {
        return request({
            url: `/books/${bookId}/comments`,
            method: "post",
            data,
        });
    },

    /**
     * 获取相关图书推荐
     * @param {string|number} bookId - 图书ID
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=6] - 返回数量
     * @returns {Promise<Object>} 相关图书列表
     */
    getRelatedBooks(bookId, params = {}) {
        return request({
            url: `/books/${bookId}/related`,
            method: "get",
            params,
        });
    },

    /**
     * 获取图书排行榜
     * @param {string} type - 排行榜类型（popular/rating/collection）
     * @param {Object} params - 查询参数
     * @param {number} [params.limit=10] - 返回数量
     * @returns {Promise<Object>} 排行榜图书列表
     */
    getBookRanking(type, params = {}) {
        return request({
            url: `/books/ranking/${type}`,
            method: "get",
            params,
        });
    },
};
