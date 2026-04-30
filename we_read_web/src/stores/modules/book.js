/**
 * @file book.js
 * @description 图书状态管理模块，管理图书列表、搜索、分类等
 * @module stores/modules/book
 * @created 2025-03-09
 */

import { defineStore } from "pinia";
import { bookApi } from "@/api/book";
import { ElMessage } from "element-plus";
import { extractResponseData, isSuccessResponse, normalizeBook } from "@/utils/api-helper";

/**
 * 图书状态管理 Store
 */
export const useBookStore = defineStore("book", {
  /**
   * 状态定义
   */
  state: () => ({
    /** 图书列表 */
    books: [],
    /** 推荐图书列表 */
    recommendedBooks: [],
    /** 最新图书列表 */
    latestBooks: [],
    /** 分页信息 */
    pagination: {
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      size: 10,
    },
    /** 加载状态 */
    loading: false,
    /** 当前图书详情 */
    currentBook: null,
    /** 搜索关键词 */
    searchKeyword: "",
    /** 当前分类 */
    currentCategory: "",
    /** 所有分类列表 */
    categories: [],
  }),

  /**
   * 计算属性（Getters）
   */
  getters: {
    /**
     * 判断是否有图书数据
     * @param {Object} state - 当前状态
     * @returns {boolean}
     */
    hasBooks: (state) => state.books.length > 0,

    /**
     * 获取当前图书总数
     * @param {Object} state - 当前状态
     * @returns {number}
     */
    totalBooks: (state) => state.pagination.totalElements,

    /**
     * 获取格式化后的分类列表（用于下拉菜单等）
     * @param {Object} state - 当前状态
     * @returns {Array}
     */
    categoryOptions: (state) =>
      state.categories.map((category) => ({
        label: category,
        value: category,
      })),
  },

  /**
   * 操作方法（Actions）
   */
  actions: {
    /**
     * 获取图书列表
     * @param {Object} params - 查询参数
     * @returns {Promise<Array>} 图书列表
     */
    async getBooks(params = {}) {
      this.loading = true;
      try {
        const response = await bookApi.getBooks(params);

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const { content, currentPage, totalPages, totalElements, size } = data;

          this.books = (content || []).map((item) => normalizeBook(item));
          this.pagination = {
            currentPage,
            totalPages,
            totalElements,
            size,
          };

          return this.books;
        } else {
          throw new Error(response.meta?.message || "获取图书列表失败");
        }
      } catch (error) {
        const message =
          error.response?.data?.meta?.message ||
          error.message ||
          "获取图书列表失败";
        ElMessage.error(message);
        this.books = [];
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 获取图书详情
     * @param {string|number} id - 图书ID
     * @returns {Promise<Object>} 图书详情
     */
    async getBookById(id) {
      this.loading = true;
      try {
        const response = await bookApi.getBookById(id);

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          this.currentBook = normalizeBook(data);
          return this.currentBook;
        } else {
          throw new Error(response.meta?.message || "获取图书详情失败");
        }
      } catch (error) {
        const message =
          error.response?.data?.meta?.message ||
          error.message ||
          "获取图书详情失败";
        ElMessage.error(message);
        this.currentBook = null;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 搜索图书
     * @param {string} keyword - 搜索关键词
     * @param {Object} params - 其他查询参数
     * @returns {Promise<Array>} 搜索结果
     */
    async searchBooks(keyword, params = {}) {
      if (!keyword) {
        return this.getBooks(params);
      }

      this.searchKeyword = keyword;
      this.loading = true;

      try {
        const response = await bookApi.searchBooks({
          keyword,
          ...params,
        });

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const { content, currentPage, totalPages, totalElements, size } = data;

          this.books = (content || []).map((item) => normalizeBook(item));
          this.pagination = {
            currentPage,
            totalPages,
            totalElements,
            size,
          };

          return this.books;
        } else {
          throw new Error(response.meta?.message || "搜索图书失败");
        }
      } catch (error) {
        const message =
          error.response?.data?.meta?.message ||
          error.message ||
          "搜索图书失败";
        ElMessage.error(message);
        this.books = [];
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 获取推荐图书
     * @param {Object} params - 查询参数
     * @returns {Promise<Array>} 推荐图书列表
     */
    async getRecommendedBooks(params = {}) {
      try {
        const response = await bookApi.getRecommendedBooks(params);

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const { content } = data;
          this.recommendedBooks = (content || []).map((item) => normalizeBook(item));
          return this.recommendedBooks;
        } else {
          throw new Error(response.meta?.message || "获取推荐图书失败");
        }
      } catch (error) {
        console.error("获取推荐图书失败:", error);
        this.recommendedBooks = [];
        return [];
      }
    },

    /**
     * 获取最新图书
     * @param {Object} params - 查询参数
     * @returns {Promise<Array>} 最新图书列表
     */
    async getLatestBooks(params = {}) {
      try {
        const response = await bookApi.getLatestBooks(params);

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const { content } = data;
          this.latestBooks = (content || []).map((item) => normalizeBook(item));
          return this.latestBooks;
        } else {
          throw new Error(response.meta?.message || "获取最新图书失败");
        }
      } catch (error) {
        console.error("获取最新图书失败:", error);
        this.latestBooks = [];
        return [];
      }
    },

    /**
     * 根据分类获取图书
     * @param {string} category - 图书分类
     * @param {Object} params - 其他查询参数
     * @returns {Promise<Array>} 分类图书列表
     */
    async getBooksByCategory(category, params = {}) {
      this.currentCategory = category;
      this.loading = true;

      try {
        const response = await bookApi.getBooksByCategory(category, params);

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const { content, currentPage, totalPages, totalElements, size } = data;

          this.books = (content || []).map((item) => normalizeBook(item));
          this.pagination = {
            currentPage,
            totalPages,
            totalElements,
            size,
          };

          return this.books;
        } else {
          throw new Error(response.meta?.message || "获取分类图书失败");
        }
      } catch (error) {
        const message =
          error.response?.data?.meta?.message ||
          error.message ||
          "获取分类图书失败";
        ElMessage.error(message);
        this.books = [];
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 获取所有图书分类
     * @returns {Promise<Array>} 分类列表
     */
    async getCategories() {
      try {
        const response = await bookApi.getCategories();

        if (isSuccessResponse(response)) {
          const data = extractResponseData(response);
          const rawCategories = Array.isArray(data) ? data : [];
          const categoryNames = [];
          const stack = [...rawCategories];
          while (stack.length) {
            const current = stack.pop();
            if (!current) continue;
            const name = current.name ?? current.Name ?? current;
            if (name) {
              categoryNames.push(name);
            }
            const children = current.children || current.Children;
            if (Array.isArray(children)) {
              stack.push(...children);
            }
          }
          this.categories = categoryNames;
          return this.categories;
        } else {
          throw new Error(response.meta?.message || "获取图书分类失败");
        }
      } catch (error) {
        console.error("获取图书分类失败:", error);
        return [];
      }
    },

    /**
     * 清除当前图书详情
     */
    clearCurrentBook() {
      this.currentBook = null;
    },

    /**
     * 重置搜索状态
     */
    resetSearch() {
      this.searchKeyword = "";
      this.currentCategory = "";
    },
  },
});
