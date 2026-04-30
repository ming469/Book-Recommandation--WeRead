/**
 * @file book-admin.js
 * @description 图书管理状态管理模块，管理后台图书列表、编辑、添加等
 * @module stores/modules/book-admin
 * @created 2025-03-21
 */

import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import { adminApi } from "@/api/admin";
import {
    handleApiError,
    isSuccessResponse,
    extractResponseData,
} from "@/utils/api-helper";

/**
 * 图书管理状态管理 Store
 */
export const useBookAdminStore = defineStore("bookAdmin", () => {
    // 状态
    const bookList = ref([]);
    const currentBook = ref(null);
    const loading = ref(false);
    const pagination = reactive({
        page: 1,
        size: 10,
        total: 0,
    });
    const categoryOptions = ref([]);

    /**
     * 获取图书列表
     * @param {Object} params - 查询参数
     * @returns {Promise<Array>} 图书列表
     */
    const getBooks = async(params = {}) => {
        loading.value = true;
        try {
            const response = await adminApi.getBookList({
                page: pagination.page,
                size: pagination.size,
                ...params,
            });

            if (isSuccessResponse(response)) {
                const data = extractResponseData(response);
                bookList.value = data.list.map((book) => ({
                    ...book,
                    // 计算平均评分
                    avgRating: book.Rating_Count > 0 ? book.Rating_Sum / book.Rating_Count : 0,
                }));
                pagination.total = data.total;
                return bookList.value;
            } else {
                throw new Error("获取图书列表失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "获取图书列表失败" });
            return [];
        } finally {
            loading.value = false;
        }
    };

    /**
     * 获取图书详情
     * @param {number} id - 图书ID
     * @returns {Promise<Object>} 图书详情
     */
    const getBookById = async(id) => {
        loading.value = true;
        try {
            const response = await adminApi.getBookDetail(id);

            if (isSuccessResponse(response)) {
                currentBook.value = extractResponseData(response);
                return currentBook.value;
            } else {
                throw new Error("获取图书详情失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "获取图书详情失败" });
            return null;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 添加图书
     * @param {Object} bookData - 图书数据
     * @returns {Promise<Object>} 添加结果
     */
    const addBook = async(bookData) => {
        loading.value = true;
        try {
            const response = await adminApi.addBook(bookData);

            if (isSuccessResponse(response)) {
                ElMessage.success("图书添加成功");
                return extractResponseData(response);
            } else {
                throw new Error("添加图书失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "添加图书失败" });
            return null;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 更新图书
     * @param {number} id - 图书ID
     * @param {Object} bookData - 图书数据
     * @returns {Promise<Object>} 更新结果
     */
    const updateBook = async(id, bookData) => {
        loading.value = true;
        try {
            const response = await adminApi.updateBook(id, bookData);

            if (isSuccessResponse(response)) {
                ElMessage.success("图书更新成功");
                return extractResponseData(response);
            } else {
                throw new Error("更新图书失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "更新图书失败" });
            return null;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 删除图书
     * @param {number} id - 图书ID
     * @returns {Promise<boolean>} 删除结果
     */
    const deleteBook = async(id) => {
        loading.value = true;
        try {
            const response = await adminApi.deleteBook(id);

            if (isSuccessResponse(response)) {
                ElMessage.success("图书删除成功");
                return true;
            } else {
                throw new Error("删除图书失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "删除图书失败" });
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 批量删除图书
     * @param {Array<number>} ids - 图书ID数组
     * @returns {Promise<boolean>} 删除结果
     */
    const batchDeleteBooks = async(ids) => {
        loading.value = true;
        try {
            const response = await adminApi.batchDeleteBooks(ids);

            if (isSuccessResponse(response)) {
                ElMessage.success("批量删除成功");
                return true;
            } else {
                throw new Error("批量删除失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "批量删除失败" });
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 更新图书状态
     * @param {number} id - 图书ID
     * @param {number} status - 图书状态：1-上架，0-下架
     * @returns {Promise<boolean>} 更新结果
     */
    const updateBookStatus = async(id, status) => {
        loading.value = true;
        try {
            const response = await adminApi.updateBookStatus(id, status);

            if (isSuccessResponse(response)) {
                const statusText = status === 1 ? "上架" : "下架";
                ElMessage.success(`图书${statusText}成功`);
                return true;
            } else {
                throw new Error("更新图书状态失败");
            }
        } catch (error) {
            handleApiError(error, { defaultMessage: "更新图书状态失败" });
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 获取分类列表
     * @returns {Promise<Array>} 分类列表
     */
    const getCategories = async() => {
        try {
            const response = await adminApi.getCategoryList();

            if (isSuccessResponse(response)) {
                categoryOptions.value = extractResponseData(response);
                return categoryOptions.value;
            } else {
                throw new Error("获取分类列表失败");
            }
        } catch (error) {
            handleApiError(error, {
                defaultMessage: "获取分类列表失败",
                showErrorMessage: false,
            });
            return [];
        }
    };

    /**
     * 设置分页信息
     * @param {Object} paginationInfo - 分页信息
     */
    const setPagination = (paginationInfo) => {
        Object.assign(pagination, paginationInfo);
    };

    /**
     * 重置状态
     */
    const resetState = () => {
        bookList.value = [];
        currentBook.value = null;
        pagination.page = 1;
        pagination.size = 10;
        pagination.total = 0;
    };

    return {
        // 状态
        bookList,
        currentBook,
        loading,
        pagination,
        categoryOptions,

        // 方法
        getBooks,
        getBookById,
        addBook,
        updateBook,
        deleteBook,
        batchDeleteBooks,
        updateBookStatus,
        getCategories,
        setPagination,
        resetState,
    };
});