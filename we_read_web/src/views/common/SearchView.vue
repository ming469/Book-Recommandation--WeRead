<template>
  <div class="search-page">
    <!-- 页面标题及搜索信息 -->
    <div class="search-header">
      <h1 class="search-title">搜索结果</h1>
      <p class="search-info">
        搜索"<span class="search-keyword">{{ searchQuery }}</span
        >"， 共找到 {{ totalResults }} 条相关结果
      </p>
    </div>

    <!-- 分类标签页 -->
    <transition name="tab-switch-fade">
      <el-tabs
        v-model="activeTab"
        class="search-tabs"
        @tab-change="handleTabChange"
        key="tab-list"
      >
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane 
          v-for="category in categoryOptions" 
          :key="category.value" 
          :label="category.label" 
          :name="category.value"
        ></el-tab-pane>
      </el-tabs>
    </transition>

    <div class="search-content">
      <!-- 筛选区域 -->
      <div class="search-filter">
        <div class="filter-item">
          <span class="filter-label">分类:</span>
          <div class="filter-options">
            <span
              v-for="category in categories"
              :key="category.value"
              :class="[
                'filter-option',
                { 'filter-active': selectedCategory === category.value },
              ]"
              @click="handleCategoryChange(category.value)"
            >
              {{ category.label }}
            </span>
          </div>
        </div>

        <div class="filter-item">
          <span class="filter-label">排序:</span>
          <div class="filter-options">
            <span
              v-for="sort in sortOptions"
              :key="sort.value"
              :class="[
                'filter-option',
                { 'filter-active': selectedSort === sort.value },
              ]"
              @click="handleSortChange(sort.value)"
            >
              {{ sort.label }}
            </span>
          </div>
        </div>

        <!-- 移动端筛选抽屉触发按钮 -->
        <div class="filter-mobile-trigger">
          <el-button
            type="primary"
            size="small"
            @click="showFilterDrawer = true"
          >
            <el-icon class="filter-icon"><Filter /></el-icon>筛选
          </el-button>
        </div>
      </div>

      <!-- 移动端筛选抽屉 -->
      <transition name="drawer-slide-fade">
        <el-drawer
          v-model="showFilterDrawer"
          title="筛选选项"
          direction="rtl"
          size="80%"
          custom-class="mobile-drawer"
        >
          <div class="drawer-content">
            <div class="drawer-section">
              <h3 class="drawer-title">分类</h3>
              <div class="drawer-options">
                <el-radio-group
                  v-model="selectedCategory"
                  @change="applyFilter"
                >
                  <el-radio
                    v-for="category in categories"
                    :key="category.value"
                    :label="category.value"
                    border
                  >
                    {{ category.label }}
                  </el-radio>
                </el-radio-group>
              </div>
            </div>

            <div class="drawer-section">
              <h3 class="drawer-title">排序方式</h3>
              <div class="drawer-options">
                <el-radio-group v-model="selectedSort" @change="applyFilter">
                  <el-radio
                    v-for="sort in sortOptions"
                    :key="sort.value"
                    :label="sort.value"
                    border
                  >
                    {{ sort.label }}
                  </el-radio>
                </el-radio-group>
              </div>
            </div>

            <div class="drawer-buttons">
              <el-button @click="resetFilter">重置</el-button>
              <el-button type="primary" @click="applyFilter"
                >应用筛选</el-button
              >
            </div>
          </div>
        </el-drawer>
      </transition>

      <transition name="fade">
        <div>
          <!-- 加载状态 -->
          <div v-if="loading" class="loading">
            <el-skeleton :rows="10" animated />
          </div>

          <!-- 无结果状态 -->
          <div v-else-if="books.length === 0" class="no-result">
            <el-empty
              description="没有找到符合条件的图书，请尝试其他关键词或筛选条件"
            >
              <el-button type="primary" @click="goHome">返回首页</el-button>
            </el-empty>
          </div>

          <!-- 搜索结果列表 -->
          <div v-else class="book-results">
            <transition-group name="list-fade" tag="div">
              <div
                v-for="book in books"
                :key="book.id"
                class="book-result-item"
                @click="navigateToDetail(book.id)"
              >
                <div class="book-cover">
                  <img :src="book.cover" :alt="book.title" @error="handleCoverError($event, book.id)" referrerpolicy="no-referrer" loading="lazy" />
                  <div v-if="isNewBook(book.publishDate)" class="book-badge new">新书</div>
                  <div v-if="book.hasEbook === 1 || book.hasEbook === true" class="book-badge free">电子书</div>
                </div>

                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <div class="book-meta">
                    <span class="book-author">{{ book.author || '未知作者' }}</span>
                    <span class="book-publisher">{{ book.publisher }}</span>
                    <span class="book-date">{{
                      formatDate(book.publishDate)
                    }}</span>
                  </div>
                  <div class="book-rating">
                    <el-rate
                      v-model="book.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value}"
                    />
                    <span class="book-popularity">浏览 {{ book.viewCount }}</span>
                  </div>
                  <div class="book-intro">
                    {{ book.description }}
                  </div>
                  <div class="book-tags">
                    <span class="book-tag">{{ book.category }}</span>
                    <span v-if="book.language" class="book-tag">{{ book.language }}</span>
                    <span v-if="book.isRecommended" class="book-tag">推荐</span>
                  </div>
                </div>
              </div>
            </transition-group>
          </div>
        </div>
      </transition>

      <!-- 分页控件 -->
      <web-pagination
        v-if="books.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalResults"
        :hide-on-single-page="true"
        @change="handlePaginationChange"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * @file SearchView.vue
 * @description 搜索结果页面组件，支持分类、排序、分页，以及PC和移动端的UI适配。
 * @created 2025-04-10
 * @updated 2025-04-15
 */

import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Filter } from "@element-plus/icons-vue";
import { useSearchStore } from "@/stores/modules/search";
import { useBookStore } from "@/stores/modules/book";
import { getBookDefaultCover } from "@/utils/cover-helper";
import { formatDate } from "@/utils/filters";
import WebPagination from "@/components/common/WebPagination.vue";
const route = useRoute();
const router = useRouter();

/** 搜索状态管理 */
const searchStore = useSearchStore();
const bookStore = useBookStore();

/** 关键字、图书列表、加载状态 */
const searchQuery = ref("");
const books = computed(() => searchStore.searchResults);
const loading = computed(() => searchStore.loading);
const totalResults = computed(() => searchStore.totalResults);

/** 当前激活的标签页、分页信息 */
const activeTab = ref("all");
const currentPage = ref(1);
const pageSize = ref(10);

/** 移动端筛选抽屉可见性 */
const showFilterDrawer = ref(false);

/** 筛选和排序 */
const selectedCategory = ref("all");
const selectedSort = ref("comprehensive");

/** 分类选项 - 从bookStore获取 */
const categoryOptions = computed(() => {
  if (bookStore.categoryOptions && bookStore.categoryOptions.length > 0) {
    return [...bookStore.categoryOptions];
  }
  return [
    { label: "小说", value: "fiction" },
    { label: "文学", value: "literature" },
    { label: "历史", value: "history" },
    { label: "科技", value: "tech" },
    { label: "教育", value: "education" },
  ];
});

/**
 * 分类选项 - 用于筛选区域显示
 * @type {Array<{label: string, value: string}>}
 */
const categories = computed(() => [{ label: "全部", value: "all" }, ...categoryOptions.value]);

/**
 * 排序选项
 * @type {Array<{label: string, value: string}>}
 */
const sortOptions = [
  { label: "综合排序", value: "comprehensive" },
  { label: "热度", value: "popularity" },
  { label: "评分", value: "rating" },
  { label: "最新", value: "latest" },
];

/**
 * 判断是否为新书（出版日期在6个月内）
 * @param {string} publishDate - 出版日期
 * @returns {boolean} 是否为新书
 */
const isNewBook = (publishDate) => {
  if (!publishDate) return false;
  const sixMonthsAgo = new Date();
  sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
  return new Date(publishDate) >= sixMonthsAgo;
};

/**
 * 执行搜索
 * 根据当前的搜索条件从API获取图书数据
 */
const fetchSearchResults = async () => {
  // 构建搜索参数并更新URL（不触发路由跳转）
  const params = {
    q: searchQuery.value,
    page: currentPage.value,
    size: pageSize.value,
    category: selectedCategory.value !== "all" ? selectedCategory.value : undefined,
    sort: selectedSort.value !== "comprehensive" ? selectedSort.value : undefined,
  };
  
  // 更新URL，但不触发路由变化
  router.replace({ path: route.path, query: params });

  // 调用搜索API
  await searchStore.search({
    keyword: searchQuery.value,
    page: currentPage.value,
    size: pageSize.value,
    category: selectedCategory.value !== "all" ? selectedCategory.value : undefined,
    sort: selectedSort.value !== "comprehensive" ? selectedSort.value : undefined,
  });
};

/**
 * 处理分页变化
 * @param {Object} pagination - 分页信息
 * @param {number} pagination.page - 新的页码
 * @param {number} pagination.size - 新的每页数量
 */
const handlePaginationChange = ({ page, size }) => {
  currentPage.value = page;
  pageSize.value = size;
  fetchSearchResults();
};

/**
 * 处理标签页切换
 * @param {string} tab - 新的标签页名称
 */
const handleTabChange = (tab) => {
  selectedCategory.value = tab;
  currentPage.value = 1;
  fetchSearchResults();
};

/**
 * 处理分类切换
 * @param {string} category - 新的分类值
 */
const handleCategoryChange = (category) => {
  selectedCategory.value = category;
  activeTab.value = category; // 同步更新标签页
  currentPage.value = 1;
  fetchSearchResults();
};

/**
 * 处理排序方式切换
 * @param {string} sort - 新的排序方式
 */
const handleSortChange = (sort) => {
  selectedSort.value = sort;
  fetchSearchResults();
};

/**
 * 应用筛选（用于移动端抽屉）
 */
const applyFilter = () => {
  showFilterDrawer.value = false;
  activeTab.value = selectedCategory.value; // 同步更新标签页
  fetchSearchResults();
};

/**
 * 重置筛选条件
 */
const resetFilter = () => {
  selectedCategory.value = "all";
  activeTab.value = "all";
  selectedSort.value = "comprehensive";
};

/**
 * 返回首页
 */
const goHome = () => {
  router.push("/");
};

/**
 * 跳转到图书详情页
 * @param {number} bookId - 图书ID
 */
const navigateToDetail = (bookId) => {
  router.push({
    path: `/book/${bookId}`,
    query: { from: "search" },
  });
};

/**
 * 处理封面加载错误
 */
const handleCoverError = (event, bookId) => {
  event.target.src = getBookDefaultCover(bookId);
};

/**
 * 监听路由参数变化，实时同步搜索条件
 */
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.q !== searchQuery.value) {
      searchQuery.value = newQuery.q || "";
      currentPage.value = parseInt(newQuery.page) || 1;
      pageSize.value = parseInt(newQuery.size) || 10;
      activeTab.value = newQuery.category || "all";
      selectedCategory.value = newQuery.category || "all";
      selectedSort.value = newQuery.sort || "comprehensive";
      fetchSearchResults();
    }
  },
  { immediate: true, deep: true }
);

/**
 * 组件挂载时执行初始化
 */
onMounted(async () => {
  // 从URL获取初始搜索条件
  searchQuery.value = route.query.q || "";
  currentPage.value = parseInt(route.query.page) || 1;
  pageSize.value = parseInt(route.query.size) || 10;
  selectedCategory.value = route.query.category || "all";
  selectedSort.value = route.query.sort || "comprehensive";
  activeTab.value = selectedCategory.value;

  // 若未输入搜索关键词则提示并跳转首页
  if (!searchQuery.value) {
    ElMessage.warning("请输入搜索关键词");
    router.push("/");
    return;
  }
  
  // 获取分类数据
  if (!bookStore.categories.length) {
    try {
      await bookStore.getCategories();
    } catch (error) {
      console.error("获取分类数据失败:", error);
    }
  }
  
  // 拉取搜索结果
  fetchSearchResults();
});
</script>

<style scoped>
/* 
  ========== 基础通用样式 ========== 
  采用简洁、时尚的设计语言，注重留白和必要的信息呈现。
*/

.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
  background-color: #f5f7fa;
}

/* 标题区域 */
.search-header {
  margin-bottom: 20px;
}

.search-title {
  font-size: 24px;
  font-weight: 500;
  margin: 0 0 10px;
}

.search-info {
  color: #666;
  font-size: 14px;
}

.search-keyword {
  color: #1989fa;
  font-weight: 500;
}

/* 标签页样式 */
:deep(.search-tabs .el-tabs__header) {
  margin-bottom: 20px;
}
:deep(.search-tabs .el-tabs__active-bar) {
  background-color: #1989fa;
}
:deep(.search-tabs .el-tabs__item.is-active) {
  color: #1989fa;
}

/* 内容外层卡片 */
.search-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* ========== 筛选区域 ========== */
.search-filter {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.filter-label {
  color: #666;
  font-size: 14px;
  margin-right: 10px;
  width: 40px;
  flex-shrink: 0;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

/* 筛选选项样式 */
.filter-option {
  margin-right: 12px;
  margin-bottom: 8px;
  padding: 4px 10px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
}
.filter-option:hover {
  color: #1989fa;
}

/* 选中状态 */
.filter-active {
  background-color: #ecf5ff;
  color: #1989fa;
}

/* 移动端筛选按钮 */
.filter-mobile-trigger {
  display: none;
  position: absolute;
  top: 10px;
  right: 0;
}
.filter-icon {
  margin-right: 4px;
}

/* ========== 移动端抽屉 ========== */
.drawer-content {
  padding: 20px;
}

.drawer-section {
  margin-bottom: 30px;
}

.drawer-title {
  font-size: 16px;
  margin-bottom: 15px;
  font-weight: 500;
}

.drawer-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.drawer-options .el-radio) {
  margin: 8px 0;
  width: 100%;
}

.drawer-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

/* ========== 加载 & 无结果 ========== */
.loading,
.no-result {
  padding: 40px 0;
  text-align: center;
  color: #999;
}

/* ========== 图书结果列表 ========== */
.book-results {
  display: flex;
  flex-direction: column;
}

.book-result-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.book-result-item:hover {
  background-color: #f9fafc;
}

.book-result-item:last-child {
  border-bottom: none;
}

/* 图书封面 */
.book-cover {
  flex-shrink: 0;
  width: 80px;
  height: 120px;
  margin-right: 20px;
  position: relative;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.book-result-item:hover .book-cover img {
  transform: scale(1.05);
}

/* 徽章 */
.book-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 2px 6px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}
.book-badge.new {
  background-color: #1989fa;
}
.book-badge.free {
  background-color: #67c23a;
}

/* 图书信息块 */
.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 16px;
  margin: 0 0 8px;
  font-weight: 500;
  color: #303133;
  transition: color 0.2s ease;
}
.book-result-item:hover .book-title {
  color: #1989fa;
}

/* 元信息（作者、出版社、日期等） */
.book-meta {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.book-author,
.book-publisher,
.book-date {
  position: relative;
}

.book-author::after,
.book-publisher::after {
  content: "";
  position: absolute;
  right: -6px;
  top: 50%;
  width: 1px;
  height: 12px;
  background-color: #dcdfe6;
  transform: translateY(-50%);
}

/* 评分 */
.book-rating {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
:deep(.book-rating .el-rate__icon) {
  font-size: 16px;
  margin-right: 2px;
}
:deep(.book-rating .el-rate__text) {
  font-size: 14px;
}
.book-popularity {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

/* 简介 */
.book-intro {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 标签 */
.book-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.book-tag {
  font-size: 12px;
  padding: 2px 8px;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 2px;
  transition: all 0.2s ease;
}
.book-result-item:hover .book-tag {
  background-color: #ecf5ff;
  color: #1989fa;
}

/* ========== 响应式 - 中等屏幕 ========== */
@media screen and (max-width: 992px) {
  .search-page {
    padding: 15px;
  }
  .search-content {
    padding: 15px;
  }
}

/* ========== 响应式 - 小屏幕和移动设备 ========== */
@media screen and (max-width: 768px) {
  .search-title {
    font-size: 20px;
  }
  .search-info {
    font-size: 13px;
  }
  .filter-label {
    width: 36px;
  }

  /* 使筛选选项可水平滚动，避免换行 */
  .filter-options {
    flex: 1;
    overflow-x: auto;
    padding-bottom: 5px;
    -ms-overflow-style: none; /* IE and Edge */
    scrollbar-width: none; /* Firefox */
  }
  .filter-options::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }

  .filter-item {
    margin-bottom: 12px;
  }

  /* 移动端显示抽屉按钮，隐藏第二个筛选项（演示示例） */
  .filter-mobile-trigger {
    display: block;
  }
  .filter-item:nth-child(2) {
    display: none;
  }

  /* 列表改为竖排 */
  .book-result-item {
    flex-direction: column;
    padding: 15px 0;
  }
  .book-cover {
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 15px;
  }
  .book-info {
    width: 100%;
  }
  .book-intro {
    -webkit-line-clamp: 3;
  }
}

/* 超小屏适配 */
@media screen and (max-width: 480px) {
  .search-page {
    padding: 10px;
  }
  .search-content {
    padding: 12px;
  }
  .search-title {
    font-size: 18px;
  }
  .search-info {
    font-size: 12px;
  }
  .book-title {
    font-size: 15px;
  }
  .book-cover {
    height: 180px;
  }
  .book-meta {
    flex-direction: column;
    gap: 4px;
  }
  .book-author::after,
  .book-publisher::after {
    display: none;
  }
}

/* ========== 过渡动画 ========== */

/* 通用淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 列表渲染过渡 */
.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.3s ease;
}
.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 标签页切换过渡 */
.tab-switch-fade-enter-active,
.tab-switch-fade-leave-active {
  transition: opacity 0.2s ease;
}
.tab-switch-fade-enter-from,
.tab-switch-fade-leave-to {
  opacity: 0;
}

/* 抽屉过渡 */
.drawer-slide-fade-enter-active,
.drawer-slide-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.drawer-slide-fade-enter-from,
.drawer-slide-fade-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
