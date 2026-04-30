/**
 * @file api-helper.js
 * @description API响应处理工具函数，提供统一的API响应及错误处理逻辑。
 * @module utils/api-helper
 * @version 1.0.1
 * @created 2025-03-20
 */

import { ElMessage } from "element-plus";
import { getBookDefaultCover } from "./cover-helper";

/**
 * 处理API响应数据，统一成功与错误处理逻辑。
 *
 * @function handleApiResponse
 * @param {Object} response - API响应对象，包含状态码、消息和数据等字段。
 * @param {Object} [options={}] - 可选参数配置项。
 * @param {string} [options.successMessage="操作成功"] - 成功时的默认提示消息。
 * @param {string} [options.errorMessage="操作失败"] - 失败时的默认错误提示消息。
 * @param {boolean} [options.showSuccessMessage=true] - 是否展示成功提示消息。
 * @param {boolean} [options.showErrorMessage=true] - 是否展示错误提示消息。
 * @returns {Object} 返回API响应中的数据部分。
 * @throws {Error} 当响应状态码不为200时，抛出错误并显示错误提示。
 */
export function handleApiResponse(response, options = {}) {
    const {
        successMessage = "操作成功",
            errorMessage = "操作失败",
            showSuccessMessage = true,
            showErrorMessage = true,
    } = options;

    // 提取响应中的状态码、消息和数据
    const code = extractResponseCode(response);
    const message = extractResponseMessage(response);
    const data = extractResponseData(response);

    // 响应成功处理
    if (code === 200) {
        if (showSuccessMessage) {
            ElMessage.success(message || successMessage);
        }
        return data;
    } else {
        // 处理响应错误情况
        const errorMsg = message || errorMessage;
        if (showErrorMessage) {
            ElMessage.error(errorMsg);
        }
        const err = new Error(errorMsg);
        // 标记错误已处理，避免重复弹出提示
        err._handled = true;
        throw err;
    }
}

/**
 * 统一处理API调用时捕获的异常，记录错误信息并展示提示。
 *
 * @function handleApiError
 * @param {Error} error - 捕获的异常对象，可能包含响应数据或其他错误信息。
 * @param {Object} [options={}] - 可选参数配置项。
 * @param {string} [options.defaultMessage="操作失败，请稍后再试"] - 默认错误提示信息。
 * @param {boolean} [options.showErrorMessage=true] - 是否展示错误提示消息。
 * @throws {Error} 重新抛出错误以便上层调用捕获。
 */
export function handleApiError(error, options = {}) {
    if (error._handled) {
        throw error;
    }

    const { defaultMessage = "操作失败，请稍后再试", showErrorMessage = true } =
    options;

    const message =
        (error.response && error.response.data && error.response.data.message) ||
        (error.response && error.response.data && error.response.data.meta && error.response.data.meta.message) ||
        error.message ||
        defaultMessage;

    console.error("API错误:", error);

    if (showErrorMessage) {
        ElMessage.error(message);
    }

    error._handled = true;
    throw error;
}

/**
 * 检查API响应是否表示成功。
 *
 * @function isSuccessResponse
 * @param {Object} response - API响应对象，可能包含meta或直接code字段。
 * @returns {boolean} 返回true表示响应成功（状态码200），否则返回false。
 */
export function isSuccessResponse(response) {
    if (response.meta && response.meta.code !== undefined) {
        return response.meta.code === 200;
    }
    const code = response.code || 500;
    return code === 200;
}

/**
 * 从API响应中提取数据部分。
 *
 * @function extractResponseData
 * @param {Object} response - API响应对象，期望包含data字段。
 * @returns {Object} 返回响应中的数据，若不存在data字段则返回整个响应对象。
 */
export function extractResponseData(response) {
    if (response && response.data !== undefined) {
        return response.data;
    }
    return response || {};
}

/**
 * 从API响应中提取提示消息。
 *
 * @function extractResponseMessage
 * @param {Object} response - API响应对象，可能包含meta或直接message字段。
 * @returns {string} 返回响应中的消息文本，如无消息则返回空字符串。
 */
export function extractResponseMessage(response) {
    if (response.meta && response.meta.message !== undefined) {
        return response.meta.message;
    }
    return response.message || "";
}

/**
 * 从API响应中提取状态码。
 *
 * @function extractResponseCode
 * @param {Object} response - API响应对象，期望包含meta或直接code字段。
 * @returns {number} 返回提取的状态码，默认值为500表示服务器错误。
 */
export function extractResponseCode(response) {
    if (response.meta && response.meta.code !== undefined) {
        return response.meta.code;
    }
    return response.code || 500;
}

export function normalizeBook(rawBook = {}) {
    const id = rawBook.id || rawBook.bookID || rawBook.Book_ID || rawBook.bookId;
    const title = rawBook.title || rawBook.Title || "";
    const author = rawBook.author || rawBook.authorName || "未知作者";
    const publisher = rawBook.publisher || rawBook.Publisher || "";
    const publishDate =
        rawBook.publishDate ||
        rawBook.publicationDate ||
        rawBook.Publication_Date ||
        "";
    const isbn = rawBook.isbn || rawBook.ISBN || "";
    const categoryId =
        rawBook.categoryId ||
        rawBook.Category_ID ||
        (rawBook.category && typeof rawBook.category === 'object' && rawBook.category.id ? rawBook.category.id : undefined);
    const category =
        rawBook.categoryName ||
        (rawBook.category && typeof rawBook.category === 'string' ? rawBook.category :
            (rawBook.category && rawBook.category.name ? rawBook.category.name : ""));
    const description = rawBook.description || rawBook.Description || "";
    const resolvedCover = getBookDefaultCover(id);
    const rating =
        rawBook.rating ||
        rawBook.avgRating ||
        rawBook.Rating_Sum ||
        rawBook.Rating ||
        0;
    const reviewCount =
        rawBook.reviewCount ||
        rawBook.Rating_Count ||
        rawBook.ratingCount ||
        0;
    const price = rawBook.price || rawBook.Price || 0;
    const language = rawBook.language || rawBook.Language || "";
    const viewCount =
        rawBook.viewCount || rawBook.View_Count || rawBook.views || 0;
    const isRecommended =
        rawBook.isRecommended || rawBook.recommended || false;
    const hasEbook =
        rawBook.hasEbook || rawBook.Has_Ebook || rawBook.has_ebook || 0;

    // 处理相似书籍
    const similarBooks = Array.isArray(rawBook.similarBooks) ?
        rawBook.similarBooks.map(item => normalizeBook(item)) : [];

    return {
        id,
        title,
        author,
        publisher,
        publishDate,
        isbn,
        categoryId,
        category,
        description: description || "暂无简介",
        cover: resolvedCover,
        rating,
        reviewCount,
        price,
        language,
        viewCount,
        isRecommended,
        hasEbook,
        similarBooks,
    };
}
