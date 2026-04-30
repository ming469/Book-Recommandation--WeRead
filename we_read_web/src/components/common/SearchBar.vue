<template>
  <div class="search-bar">
    <!-- 搜索输入框组件 -->
    <el-input
      v-model="keyword"
      placeholder="搜索图书、作者、分类..."
      class="search-input"
      :prefix-icon="Search"
      clearable
      @keyup.enter="handleSearch"
      @focus="showSuggestions = true"
    />

    <!-- 搜索历史和热门搜索下拉面板 -->
    <div
      v-if="
        showSuggestions && (searchHistory.length > 0 || hotSearches.length > 0)
      "
      class="search-suggestions"
    >
      <!-- 搜索历史部分 -->
      <template v-if="searchHistory.length > 0">
        <div class="suggestion-section">
          <div class="suggestion-header">
            <span>搜索历史</span>
            <el-button
              type="text"
              @click.stop="clearSearchHistory"
              class="clear-button"
              aria-label="清除搜索历史"
            >
              <el-icon><Delete /></el-icon>
              <span>清除</span>
            </el-button>
          </div>
          <div class="suggestion-list">
            <div
              v-for="(item, index) in searchHistory"
              :key="`history-${index}`"
              class="suggestion-item"
              @click="selectSuggestion(item)"
            >
              <el-icon><Timer /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 热门搜索部分 -->
      <template v-if="hotSearches.length > 0">
        <div class="suggestion-section">
          <div class="suggestion-header">
            <span>热门搜索</span>
          </div>
          <div class="suggestion-list hot-search-list">
            <div
              v-for="(item, index) in hotSearches"
              :key="`hot-${index}`"
              class="suggestion-item"
              @click="selectSuggestion(item)"
            >
              <span class="rank-number" :class="{ 'top-rank': index < 3 }">{{
                index + 1
              }}</span>
              <span class="item-text">{{ item }}</span>
              <span v-if="index < 3" class="hot-tag">热</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
/**
 * @file SearchBar.vue
 * @description 微读平台搜索框组件，支持关键词搜索、搜索历史和热门搜索推荐
 * @author WeRead Team
 * @created 2025-03-1
 * @updated 2025-03-1
 */

import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { Search, Timer, Delete } from "@element-plus/icons-vue";
import { useSearchStore } from "@/stores/modules/search";

/**
 * 搜索关键字
 * @type {import('vue').Ref<string>}
 */
const keyword = ref("");

/**
 * 是否显示搜索建议
 * @type {import('vue').Ref<boolean>}
 */
const showSuggestions = ref(false);

/**
 * 路由实例
 * @type {import('vue-router').Router}
 */
const router = useRouter();

/**
 * 搜索状态管理
 * @type {ReturnType<typeof useSearchStore>}
 */
const searchStore = useSearchStore();

/**
 * 搜索历史
 * @type {import('vue').ComputedRef<string[]>}
 */
const searchHistory = computed(() => searchStore.searchHistory);

/**
 * 热门搜索
 * @type {import('vue').ComputedRef<string[]>}
 */
const hotSearches = computed(() => searchStore.hotSearches);

/**
 * 处理搜索操作
 * 将搜索关键词保存到历史记录并跳转到搜索结果页
 * @returns {void}
 */
const handleSearch = () => {
  if (keyword.value.trim()) {
    // 保存到搜索历史
    searchStore.addSearchHistory(keyword.value.trim());

    // 跳转到搜索结果页
    router.push({
      path: "/search",
      query: { q: keyword.value.trim() },
    });

    // 隐藏搜索建议
    showSuggestions.value = false;
  }
};

/**
 * 选择搜索建议
 * 将选中的建议设置为搜索关键词并执行搜索
 * @param {string} suggestion - 搜索建议内容
 * @returns {void}
 */
const selectSuggestion = (suggestion) => {
  keyword.value = suggestion;
  handleSearch();
};

/**
 * 清空搜索历史
 * 阻止事件冒泡并清除所有搜索历史记录
 * @param {Event} event - 点击事件对象
 * @returns {void}
 */
const clearSearchHistory = (event) => {
  event.stopPropagation();
  searchStore.clearSearchHistory();
};

/**
 * 处理点击外部元素
 * 当点击搜索框外部区域时隐藏搜索建议
 * @param {Event} event - 点击事件对象
 * @returns {void}
 */
const handleClickOutside = (event) => {
  const searchBarEl = document.querySelector(".search-bar");
  if (searchBarEl && !searchBarEl.contains(event.target)) {
    showSuggestions.value = false;
  }
};

/**
 * 监听关键字变化
 * 当输入框内容变化时显示搜索建议
 */
watch(keyword, (newValue) => {
  // 显示搜索建议，无论输入框是否有内容
  showSuggestions.value = true;

  // 如果有输入内容，可以在此处调用API获取搜索建议
  if (newValue.trim()) {
    // searchStore.fetchSearchSuggestions(newValue.trim());
  }
});

/**
 * 组件挂载时的生命周期钩子
 * 初始化热门搜索数据并添加点击事件监听
 */
onMounted(() => {
  // 加载热门搜索数据
  searchStore.fetchHotSearches();

  // 添加点击外部元素事件监听
  document.addEventListener("click", handleClickOutside);
});

/**
 * 组件卸载时的生命周期钩子
 * 移除事件监听，防止内存泄漏
 */
onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
/**
 * 搜索框容器样式
 */
.search-bar {
  width: 100%;
  position: relative;
  font-family: "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

/**
 * 搜索输入框样式
 */
.search-input {
  width: 100%;
}

/**
 * 输入框包装器样式
 */
.search-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding-left: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  height: 40px;
  border: 1px solid #ebeef5;
}

/**
 * 输入框悬停状态
 */
.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

/**
 * 输入框聚焦状态
 */
.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

/**
 * 输入框内部文本样式
 */
.search-input :deep(.el-input__inner) {
  font-size: 14px;
  color: #333;
}

/**
 * 输入框前缀图标样式
 */
.search-input :deep(.el-input__prefix) {
  font-size: 16px;
  margin-right: 8px;
  color: #909399;
}

/**
 * 输入框后缀图标样式
 */
.search-input :deep(.el-input__suffix) {
  color: #909399;
}

/**
 * 搜索建议下拉面板样式
 */
.search-suggestions {
  position: absolute;
  top: 46px;
  left: 0;
  width: 100%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  z-index: 999;
  padding: 12px 0;
  max-height: 400px;
  overflow-y: auto;
  animation: slideDown 0.2s ease-out;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

/**
 * 搜索建议下拉动画
 */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/**
 * 建议部分容器样式
 */
.suggestion-section {
  padding: 0 16px;
  margin-bottom: 16px;
}

/**
 * 建议部分标题样式
 */
.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
}

/**
 * 清除按钮样式
 */
.clear-button {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 15px;
}

/**
 * 清除按钮悬停状态
 */
.clear-button:hover {
  color: var(--el-color-primary);
  background-color: rgba(var(--el-color-primary-rgb), 0.1);
}

/**
 * 清除按钮图标样式
 */
.clear-button .el-icon {
  margin-right: 4px;
}

/**
 * 建议列表样式
 */
.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/**
 * 热门搜索列表网格布局
 */
.hot-search-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 8px;
}

/**
 * 建议项样式
 */
.suggestion-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  font-size: 14px;
}

/**
 * 建议项悬停状态
 */
.suggestion-item:hover {
  background-color: rgba(var(--el-color-primary-rgb), 0.08);
  transform: translateX(4px);
}

/**
 * 建议项图标样式
 */
.suggestion-item .el-icon {
  margin-right: 10px;
  font-size: 16px;
  color: #909399;
}

/**
 * 建议项文本样式
 */
.suggestion-item .item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/**
 * 排名数字样式
 */
.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  margin-right: 10px;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
}

/**
 * 前三名排名样式
 */
.rank-number.top-rank {
  color: white;
  font-weight: bold;
}

/**
 * 第一名排名样式
 */
.rank-number.top-rank:nth-child(1) {
  background: #f56c6c;
}

/**
 * 第二名排名样式
 */
.rank-number.top-rank:nth-of-type(2) {
  background: #e6a23c;
}

/**
 * 第三名排名样式
 */
.rank-number.top-rank:nth-of-type(3) {
  background: #409eff;
}

/**
 * 热门标签样式
 */
.hot-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  color: white;
  font-size: 12px;
  border-radius: 3px;
  padding: 1px 5px;
  margin-left: 8px;
  font-weight: bold;
}

/**
 * 响应式布局 - 平板设备
 */
@media screen and (max-width: 767px) {
  .search-input :deep(.el-input__wrapper) {
    height: 38px;
    padding-left: 12px;
  }

  .search-suggestions {
    top: 44px;
    border-radius: 8px;
  }

  .hot-search-list {
    grid-template-columns: 1fr;
  }

  .suggestion-section {
    padding: 0 12px;
    margin-bottom: 12px;
  }

  .suggestion-item {
    padding: 8px;
  }
}

/**
 * 响应式布局 - 移动设备
 */
@media screen and (max-width: 480px) {
  .search-input :deep(.el-input__wrapper) {
    height: 36px;
    border-radius: 18px;
    padding-left: 10px;
  }

  .search-suggestions {
    top: 42px;
    padding: 10px 0;
  }

  .suggestion-header {
    font-size: 13px;
    margin-bottom: 8px;
  }

  .suggestion-item {
    font-size: 13px;
  }

  /* 移动端优化：更紧凑的设计 */
  .suggestion-item .el-icon,
  .rank-number {
    margin-right: 8px;
  }

  .rank-number {
    width: 18px;
    height: 18px;
    font-size: 11px;
  }

  .hot-tag {
    font-size: 11px;
    padding: 0 4px;
  }

  /* 移动端触摸优化 */
  .suggestion-item {
    padding: 10px 8px;
  }

  .clear-button {
    padding: 6px 10px;
  }
}
</style>