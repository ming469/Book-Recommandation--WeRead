/**
 * @file search.js
 * @description 搜索状态管理模块，管理搜索历史、热门搜索和搜索结果
 * @module stores/modules/search
 * @created 2025-04-10
 */

import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { searchApi } from "@/api/search";
import { ElMessage } from "element-plus";
import {
  extractResponseData,
  isSuccessResponse,
  normalizeBook,
} from "@/utils/api-helper";

/**
 * 搜索相关状态管理
 */
export const useSearchStore = defineStore("search", () => {
  // 搜索历史 (从本地存储中获取)
  const searchHistory = ref(
    JSON.parse(localStorage.getItem("searchHistory") || "[]")
  );

  // 热门搜索
  const hotSearches = ref([]);

  // 搜索建议
  const searchSuggestions = ref([]);

  // 搜索结果
  const searchResults = ref([]);

  // 分页信息
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
    pages: 0,
  });

  // 加载状态
  const loading = ref(false);

  // 搜索错误信息
  const error = ref(null);

  /**
   * 计算属性：搜索结果总数
   */
  const totalResults = computed(() => pagination.value.total || 0);

  /**
   * 添加搜索历史
   * @param {string} keyword - 搜索关键字
   */
  function addSearchHistory(keyword) {
    if (!keyword.trim()) return;

    // 如果已存在该关键字，先移除
    const index = searchHistory.value.findIndex((item) => item === keyword);
    if (index !== -1) {
      searchHistory.value.splice(index, 1);
    }

    // 添加到历史记录开头
    searchHistory.value.unshift(keyword);

    // 限制历史记录数量
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10);
    }

    // 保存到本地存储
    localStorage.setItem("searchHistory", JSON.stringify(searchHistory.value));
  }

  /**
   * 清空搜索历史
   */
  function clearSearchHistory() {
    searchHistory.value = [];
    localStorage.removeItem("searchHistory");
  }

  /**
   * 获取热门搜索
   * @returns {Promise<Array>} 热门搜索列表
   */
  async function fetchHotSearches() {
    try {
      const response = await searchApi.getHotSearches();

      if (response.meta && response.meta.code === 200) {
        hotSearches.value = response.data || [];
        return hotSearches.value;
      } else {
        console.warn("获取热门搜索响应异常:", response);
        // 使用模拟数据作为后备
        hotSearches.value = [
          "人工智能基础",
          "计算机视觉",
          "深度学习实战",
          "互联网思维",
          "高效能人士的七个习惯",
          "数据结构与算法",
          "软件工程方法",
          "云计算架构设计",
        ];
        return hotSearches.value;
      }
    } catch (error) {
      console.error("获取热门搜索失败:", error);
      // 使用模拟数据作为后备
      hotSearches.value = [
        "人工智能基础",
        "计算机视觉",
        "深度学习实战",
        "互联网思维",
        "高效能人士的七个习惯",
        "数据结构与算法",
        "软件工程方法",
        "云计算架构设计",
      ];
      return hotSearches.value;
    }
  }

  /**
   * 获取搜索建议
   * @param {string} keyword - 搜索关键字
   * @returns {Promise<Array>} 搜索建议列表
   */
  async function fetchSearchSuggestions(keyword) {
    if (!keyword || keyword.length < 1) {
      searchSuggestions.value = [];
      return [];
    }

    try {
      const response = await searchApi.getSearchSuggestions({ keyword });

      if (response.meta && response.meta.code === 200) {
        searchSuggestions.value = response.data || [];
        return searchSuggestions.value;
      } else {
        console.warn("获取搜索建议响应异常:", response);
        // 基于关键字过滤热门搜索作为后备
        searchSuggestions.value = hotSearches.value.filter((item) =>
          item.toLowerCase().includes(keyword.toLowerCase())
        );
        return searchSuggestions.value;
      }
    } catch (error) {
      console.error("获取搜索建议失败:", error);
      searchSuggestions.value = [];
      return [];
    }
  }

  /**
   * 执行搜索
   * @param {Object} params - 搜索参数
   * @param {string} params.keyword - 搜索关键字
   * @param {number} [params.page=1] - 页码
   * @param {number} [params.size=10] - 每页数量
   * @param {string} [params.category] - 分类
   * @param {string} [params.sort] - 排序方式
   * @returns {Promise<Object>} 搜索结果
   */
  async function search(params) {
    const { keyword, page = 1, size = 10, category, sort } = params;

    // 重置错误状态
    error.value = null;

    // 如果没有关键字，则返回空结果
    if (!keyword) {
      searchResults.value = [];
      pagination.value = { page, size, total: 0, pages: 0 };
      return { list: [], pagination: pagination.value };
    }

    loading.value = true;

    try {
      // 构建API请求参数
      const requestParams = {
        keyword,
        page,
        size,
      };

      // 添加可选参数
      if (category && category !== "all") {
        requestParams.category = category;
      }

      if (sort && sort !== "comprehensive") {
        requestParams.sort = sort;
      }

      const response = await searchApi.searchBooks(requestParams);

    if (isSuccessResponse(response)) {
      const data = extractResponseData(response);
      const list = data.list || data.content || [];
      const total = data.total || data.totalElements || 0;
      const pages = data.pages || data.totalPages || 0;
      const currentPage = data.page || data.currentPage || page;
      const pageSize = data.size || size;

        // 更新搜索结果
      searchResults.value = list.map((item) => normalizeBook(item));

        // 更新分页信息
        pagination.value = {
          page: currentPage,
          size: pageSize,
          total,
          pages,
        };

        // 添加到搜索历史
        if (keyword) {
          addSearchHistory(keyword);
        }

        return { list: searchResults.value, pagination: pagination.value };
      } else {
        const errorMessage = response.meta?.message || "搜索失败";
        error.value = errorMessage;
        console.error("搜索响应异常:", response);
        ElMessage.error(errorMessage);
        return { list: [], pagination: { page, size, total: 0, pages: 0 } };
      }
    } catch (err) {
      const errorMessage = err.message || "搜索失败，请稍后再试";
      error.value = errorMessage;
      console.error("搜索失败:", err);
      ElMessage.error(errorMessage);
      return { list: [], pagination: { page, size, total: 0, pages: 0 } };
    } finally {
      loading.value = false;
    }
  }

  return {
    // 状态
    searchHistory,
    hotSearches,
    searchSuggestions,
    searchResults,
    pagination,
    loading,
    error,

    // 计算属性
    totalResults,

    // 方法
    addSearchHistory,
    clearSearchHistory,
    fetchHotSearches,
    fetchSearchSuggestions,
    search,
  };
});
