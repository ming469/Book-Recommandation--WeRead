<template>
  <div class="user-favorites">
    <div class="favorites-header">
      <h2 class="section-title">我的收藏</h2>
      <div class="filter-actions">
        <el-select v-model="filterType" placeholder="筛选类型" size="default">
          <el-option label="全部" value="all" />
          <el-option label="小说" value="fiction" />
          <el-option label="历史" value="history" />
          <el-option label="科技" value="technology" />
          <el-option label="文学" value="literature" />
          <el-option label="艺术" value="art" />
        </el-select>

        <el-select v-model="sortBy" placeholder="排序方式" size="default">
          <el-option label="收藏时间 - 最新" value="time-desc" />
          <el-option label="收藏时间 - 最早" value="time-asc" />
          <el-option label="书名 A-Z" value="title-asc" />
          <el-option label="书名 Z-A" value="title-desc" />
          <el-option label="评分 - 高到低" value="rating-desc" />
        </el-select>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="favorites-content">
      <el-empty
        v-if="favoriteBooks.length === 0"
        description="暂无收藏书籍"
        :image-size="200"
      >
        <el-button type="primary" @click="goToHome">去发现好书</el-button>
      </el-empty>

      <div v-else class="book-list">
        <div v-for="book in displayBooks" :key="book.id" class="book-card">
          <div class="book-cover">
            <el-image
              :src="book.cover"
              fit="cover"
              :lazy="true"
              @click="viewBookDetail(book.id)"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="book-actions">
              <el-tooltip content="取消收藏" placement="top">
                <el-button
                  type="danger"
                  circle
                  size="small"
                  @click="removeFavorite(book.id)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>

          <div class="book-info">
            <h3 class="book-title" @click="viewBookDetail(book.id)">
              {{ book.title }}
            </h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-rating">
              <el-rate
                v-model="book.rating"
                disabled
                text-color="#ff9900"
                :score-template="book.rating.toFixed(1)"
                :show-score="true"
              />
            </div>
            <p class="book-category">{{ book.category }}</p>
            <p class="favorite-time">
              收藏于: {{ formatDate(book.favoriteTime) }}
            </p>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="favoriteBooks.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalBooks"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 批量操作工具栏 -->
    <div class="batch-toolbar" v-if="favoriteBooks.length > 0">
      <el-button type="primary" @click="showBatchActions = !showBatchActions">
        {{ showBatchActions ? "取消批量操作" : "批量操作" }}
      </el-button>

      <div class="batch-actions" v-if="showBatchActions">
        <el-button
          type="danger"
          :disabled="selectedBooks.length === 0"
          @click="batchRemove"
        >
          批量取消收藏
        </el-button>
        <el-button type="info" @click="selectAll">全选</el-button>
        <el-button @click="clearSelection">清除选择</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { Picture, Delete } from "@element-plus/icons-vue";
import { format } from "date-fns";
import { getBookDefaultCover } from "@/utils/cover-helper";
import { userApi } from "@/api/user";
import { bookApi } from "@/api/book";
import { normalizeBook } from "@/utils/api-helper";

const router = useRouter();

// 筛选和排序
const filterType = ref("all");
const sortBy = ref("time-desc");

// 分页
const currentPage = ref(1);
const pageSize = ref(12);
const totalBooks = ref(0);

// 批量操作
const showBatchActions = ref(false);
const selectedBooks = ref([]);

// 收藏书籍列表
const favoriteBooks = ref([]);

// 根据筛选和排序条件计算显示的书籍
const displayBooks = computed(() => {
  let result = [...favoriteBooks.value];

  // 应用筛选
  if (filterType.value !== "all") {
    result = result.filter((book) => book.categoryKey === filterType.value);
  }

  // 应用排序
  switch (sortBy.value) {
    case "time-desc":
      result.sort(
        (a, b) => new Date(b.favoriteTime) - new Date(a.favoriteTime)
      );
      break;
    case "time-asc":
      result.sort(
        (a, b) => new Date(a.favoriteTime) - new Date(b.favoriteTime)
      );
      break;
    case "title-asc":
      result.sort((a, b) => a.title.localeCompare(b.title));
      break;
    case "title-desc":
      result.sort((a, b) => b.title.localeCompare(a.title));
      break;
    case "rating-desc":
      result.sort((a, b) => b.rating - a.rating);
      break;
  }

  // 应用分页
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;

  return result.slice(startIndex, endIndex);
});

/**
 * 格式化日期
 * @param {string|Date} date - 日期字符串或日期对象
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (date) => {
  try {
    return format(new Date(date), "yyyy-MM-dd HH:mm");
  } catch (error) {
    return "未知时间";
  }
};

/**
 * 获取收藏书籍列表
 */
const fetchFavoriteBooks = async () => {
  try {
    const res = await userApi.getFavorites();
    if (res?.meta?.code === 200 && Array.isArray(res.data?.list)) {
      const list = res.data.list;
      const books = await Promise.all(
        list.map(async (item) => {
          try {
            const detail = await bookApi.getBookById(item.bookId);
            if (detail?.meta?.code === 200 && detail.data) {
              const n = normalizeBook(detail.data);
              return {
                id: n.id,
                title: n.title,
                author: n.author,
                cover: n.cover || getBookDefaultCover(n.id),
                rating: n.rating || 0,
                category: n.category || "",
                categoryKey: n.categoryKey || "",
                favoriteTime: item.favoriteTime || new Date().toISOString(),
              };
            }
          } catch (e) {}
          return null;
        })
      );
      const valid = books.filter(Boolean);
      favoriteBooks.value = valid;
      totalBooks.value = valid.length;
    } else {
      favoriteBooks.value = [];
      totalBooks.value = 0;
    }
  } catch (error) {
    console.error("获取收藏书籍失败", error);
    ElMessage.error("获取收藏书籍失败，请刷新重试");
  }
};

/**
 * 查看书籍详情
 * @param {number} bookId - 书籍ID
 */
const viewBookDetail = (bookId) => {
  router.push(`/book/${bookId}`);
};

/**
 * 移除收藏
 * @param {number} bookId - 书籍ID
 */
const removeFavorite = (bookId) => {
  ElMessageBox.confirm("确定要取消收藏该书籍吗？", "取消收藏", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await userApi.removeFavorite(bookId);
        favoriteBooks.value = favoriteBooks.value.filter((book) => book.id !== bookId);
        totalBooks.value = favoriteBooks.value.length;
        if (displayBooks.value.length === 0 && currentPage.value > 1) {
          currentPage.value--;
        }
        ElMessage.success("已取消收藏");
      } catch (e) {
        ElMessage.error("取消收藏失败，请重试");
      }
    })
    .catch(() => {
      // 取消操作
    });
};

/**
 * 批量移除收藏
 */
const batchRemove = () => {
  if (selectedBooks.value.length === 0) {
    ElMessage.warning("请先选择要取消收藏的书籍");
    return;
  }

  ElMessageBox.confirm(
    `确定要取消收藏选中的 ${selectedBooks.value.length} 本书籍吗？`,
    "批量取消收藏",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      // 模拟API调用
      favoriteBooks.value = favoriteBooks.value.filter(
        (book) => !selectedBooks.value.includes(book.id)
      );
      totalBooks.value = favoriteBooks.value.length;

      // 更新分页
      if (displayBooks.value.length === 0 && currentPage.value > 1) {
        currentPage.value--;
      }

      selectedBooks.value = [];
      ElMessage.success("已批量取消收藏");
    })
    .catch(() => {
      // 取消操作
    });
};

/**
 * 全选
 */
const selectAll = () => {
  selectedBooks.value = displayBooks.value.map((book) => book.id);
};

/**
 * 清除选择
 */
const clearSelection = () => {
  selectedBooks.value = [];
};

/**
 * 处理每页显示数量变化
 */
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1; // 重置到第一页
};

/**
 * 处理页码变化
 */
const handleCurrentChange = (page) => {
  currentPage.value = page;
};

/**
 * 跳转到首页
 */
const goToHome = () => {
  router.push("/");
};

// 监听筛选和排序变化，重置页码
watch([filterType, sortBy], () => {
  currentPage.value = 1;
});

onMounted(() => {
  // 获取收藏书籍列表
  fetchFavoriteBooks();
});
</script>

<style scoped>
.user-favorites {
  position: relative;
}

.favorites-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.favorites-content {
  min-height: 400px;
}

.book-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.book-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.book-cover {
  position: relative;
  height: 280px;
  overflow: hidden;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.book-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-cover:hover .book-actions {
  opacity: 1;
}

.book-info {
  padding: 16px;
}

.book-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 2.8em;
}

.book-title:hover {
  color: #409eff;
}

.book-author {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}

.book-rating {
  margin-bottom: 8px;
}

.book-category {
  margin: 8px 0;
  font-size: 13px;
  color: #909399;
  padding: 2px 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  display: inline-block;
}

.favorite-time {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.batch-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  padding: 12px 20px;
  display: flex;
  align-items: center;
  z-index: 10;
}

.batch-actions {
  display: flex;
  gap: 10px;
  margin-left: 20px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .favorites-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-actions .el-select {
    width: 100%;
  }

  .book-list {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .book-cover {
    height: 220px;
  }

  .book-title {
    font-size: 14px;
  }

  .batch-toolbar {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }

  .batch-actions {
    margin-left: 0;
    width: 100%;
    justify-content: space-between;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .section-title {
    color: #e5e7eb;
  }

  .book-card {
    background-color: #1e1e1e;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
  }

  .book-title {
    color: #e5e7eb;
  }

  .book-author {
    color: #a0a0a0;
  }

  .book-category {
    background-color: #2c2c2c;
    color: #a0a0a0;
  }

  .image-error {
    background-color: #2c2c2c;
    color: #a0a0a0;
  }

  .batch-toolbar {
    background-color: #1e1e1e;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.3);
  }
}
</style>
