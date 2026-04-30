<template>
  <div class="web-pagination">
    <el-pagination
      v-model:current-page="currentPageSync"
      v-model:page-size="pageSizeSync"
      :page-sizes="pageSizes"
      :layout="layout"
      :total="total"
      :background="background"
      :hide-on-single-page="hideOnSinglePage"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
/**
 * @file WebPagination.vue
 * @description 微读平台通用分页组件，支持页码切换、每页数量调整等功能
 * @author WeRead Team
 * @created 2025-04-15
 */

/* eslint-disable no-undef */
import { computed, watch } from 'vue';

// 在<script setup>中，defineProps和defineEmits是编译器宏，不需要导入
const props = defineProps({
  /**
   * 当前页码
   */
  currentPage: {
    type: Number,
    default: 1
  },
  /**
   * 每页显示条数
   */
  pageSize: {
    type: Number,
    default: 10
  },
  /**
   * 总条目数
   */
  total: {
    type: Number,
    default: 0
  },
  /**
   * 每页显示条数选项
   */
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  /**
   * 分页布局
   */
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  /**
   * 是否使用背景色
   */
  background: {
    type: Boolean,
    default: true
  },
  /**
   * 只有一页时是否隐藏分页
   */
  hideOnSinglePage: {
    type: Boolean,
    default: false
  },
  /**
   * 是否自动滚动到顶部
   */
  autoScroll: {
    type: Boolean,
    default: true
  },
  /**
   * 滚动到顶部的偏移量
   */
  scrollOffset: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change']);
/* eslint-enable no-undef */

/**
 * 内部当前页码，用于双向绑定
 */
const currentPageSync = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
});

/**
 * 内部每页显示条数，用于双向绑定
 */
const pageSizeSync = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
});

/**
 * 处理页码变化
 * @param {number} page - 新的页码
 */
const handleCurrentChange = (page) => {
  if (props.autoScroll) {
    scrollToTop();
  }
  emit('change', { page, size: pageSizeSync.value });
};

/**
 * 处理每页条数变化
 * @param {number} size - 新的每页条数
 */
const handleSizeChange = (size) => {
  // 当改变每页显示条数时，通常需要重置为第一页
  if (currentPageSync.value !== 1) {
    currentPageSync.value = 1;
  }
  emit('change', { page: 1, size });
};

/**
 * 滚动到页面顶部
 */
const scrollToTop = () => {
  window.scrollTo({
    top: props.scrollOffset,
    behavior: 'smooth'
  });
};

/**
 * 监听总数变化，当总数变化且当前页超出范围时，自动调整到最后一页
 */
watch(() => props.total, (newTotal) => {
  const maxPage = Math.ceil(newTotal / pageSizeSync.value) || 1;
  if (currentPageSync.value > maxPage) {
    currentPageSync.value = maxPage;
  }
});
</script>

<style scoped>
.web-pagination {
  margin: 30px 0;
  display: flex;
  justify-content: center;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .web-pagination {
    margin: 20px 0;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  :deep(.el-pagination .el-select) {
    margin-bottom: 8px;
  }
}

@media screen and (max-width: 480px) {
  .web-pagination {
    margin: 15px 0;
  }
  
  :deep(.el-pagination__jump) {
    margin-top: 8px;
  }
}
</style>