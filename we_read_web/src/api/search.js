/**
 * @file search.js
 * @description 微读平台搜索相关API接口
 * @created 2025-04-10
 * @module api/search
 */

import request from "@/utils/request";

const fallbackHotSearches = [
  "活着",
  "围城",
  "红楼梦",
  "人间失格",
  "悉达多",
  "小王子",
  "麦田里的守望者",
  "雪国",
];

/**
 * 搜索API模块 - 提供搜索相关API接口
 */
export const searchApi = {
  /**
   * 搜索图书
   * @param {Object} params - 搜索参数
   * @param {string} params.keyword - 搜索关键词
   * @param {number} [params.page=1] - 页码
   * @param {number} [params.size=10] - 每页数量
   * @param {string} [params.category] - 图书分类
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
   * 获取热门搜索关键词
   * @returns {Promise<Object>} 热门搜索关键词列表
   */
  getHotSearches() {
    return request({
      url: "/user/behavior/hot-searches",
      method: "get",
      params: { limit: 8 },
    }).catch(() => {
      return {
        meta: { code: 200, message: "success" },
        data: fallbackHotSearches,
      };
    });
  },

  /**
   * 获取搜索建议
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @returns {Promise<Object>} 搜索建议列表
   */
  getSearchSuggestions(params) {
    const keyword = (params?.keyword || "").trim();
    // 无关键词时：返回“真正的推荐”——调用个性化热搜（登录）或回退热词（未登录）
    if (!keyword) {
      return this.getHotSearches();
    }
    // 有关键词时：联动后端实时搜索，提取Top N书名作为建议
    const size = 8;
    return request({
      url: "/books/search",
      method: "get",
      params: { keyword, page: 0, size },
    })
      .then((res) => {
        if (res?.meta?.code === 200) {
          const data = Array.isArray(res.data?.list)
            ? res.data.list
            : Array.isArray(res.data?.content)
            ? res.data.content
            : [];
          const suggestions = data
            .map((b) => b?.title || b?.Title || b?.name || "")
            .filter((t) => typeof t === "string" && t.trim().length > 0);
          return { meta: { code: 200, message: "success" }, data: suggestions };
        }
        // 失败回退到本地热词过滤
        const fallback = fallbackHotSearches.filter((item) =>
          item.toLowerCase().includes(keyword.toLowerCase())
        );
        return { meta: { code: 200, message: "success" }, data: fallback };
      })
      .catch(() => {
        const fallback = fallbackHotSearches.filter((item) =>
          item.toLowerCase().includes(keyword.toLowerCase())
        );
        return { meta: { code: 200, message: "success" }, data: fallback };
      });
  },

  /**
   * 获取分类列表
   * @returns {Promise<Object>} 分类列表
   */
  getCategories() {
    return request({
      url: "/categories",
      method: "get",
    });
  },
};
