<template>
  <div class="user-history">
    <div class="history-header">
      <h2 class="section-title">阅读历史</h2>
      <div class="filter-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          size="default"
          @change="handleDateChange"
        />

        <el-button
          type="danger"
          @click="clearHistory"
          :disabled="historyList.length === 0"
        >
          清空历史
        </el-button>
      </div>
    </div>

    <!-- 历史记录列表 -->
    <div class="history-content">
      <el-empty
        v-if="historyList.length === 0"
        description="暂无阅读历史"
        :image-size="200"
      >
        <el-button type="primary" @click="goToHome">去发现好书</el-button>
      </el-empty>

      <div v-else>
        <!-- 按日期分组显示 -->
        <div
          v-for="(group, date) in groupedHistory"
          :key="date"
          class="history-group"
        >
          <div class="date-header">
            <h3 class="date-title">{{ formatGroupDate(date) }}</h3>
            <div class="date-count">{{ group.length }} 本书</div>
          </div>

          <div class="history-list">
            <div
              v-for="item in group"
              :key="`${item.id}-${item.timestamp}`"
              class="history-item"
            >
              <div class="book-cover" @click="viewBookDetail(item.id)">
                <el-image :src="item.cover" fit="cover" :lazy="true">
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>

              <div class="book-info">
                <h3 class="book-title" @click="viewBookDetail(item.id)">
                  {{ item.title }}
                </h3>
                <p class="book-author">{{ item.author }}</p>
                <div class="book-meta">
                  <span class="book-category">{{ item.category }}</span>
                  <span class="read-progress">
                    阅读至: {{ item.progress }}%
                  </span>
                </div>
                <div class="read-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(item.timestamp) }}
                </div>
              </div>

              <div class="item-actions">
                <el-tooltip content="继续阅读" placement="top">
                  <el-button
                    type="primary"
                    circle
                    @click="continueReading(item.id)"
                  >
                    <el-icon><Reading /></el-icon>
                  </el-button>
                </el-tooltip>

                <el-tooltip content="删除记录" placement="top">
                  <el-button
                    type="danger"
                    circle
                    @click="removeHistoryItem(item.id, item.timestamp)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMoreHistory">
          <el-button :loading="loading" @click="loadMoreHistory">
            加载更多
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { Picture, Clock, Reading, Delete } from "@element-plus/icons-vue";
import {
  format,
  parseISO,
  isToday,
  isYesterday,
  isThisWeek,
  isThisMonth,
} from "date-fns";
import zhCN from "date-fns/locale/zh-CN";
import { getBookDefaultCover } from "@/utils/cover-helper";

const router = useRouter();

// 日期范围
const dateRange = ref([]);

// 日期快捷选项
const dateShortcuts = [
  {
    text: "最近一周",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    },
  },
  {
    text: "最近一个月",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      return [start, end];
    },
  },
  {
    text: "最近三个月",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      return [start, end];
    },
  },
];

// 历史记录列表
const historyList = ref([]);
const loading = ref(false);
const hasMoreHistory = ref(true);
const currentPage = ref(1);
const pageSize = ref(20);

// 按日期分组的历史记录
const groupedHistory = computed(() => {
  const groups = {};

  historyList.value.forEach((item) => {
    const date = format(new Date(item.timestamp), "yyyy-MM-dd");
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(item);
  });

  // 按日期降序排序
  return Object.fromEntries(
    Object.entries(groups).sort((a, b) => {
      return new Date(b[0]) - new Date(a[0]);
    })
  );
});

/**
 * 格式化分组日期显示
 * @param {string} dateStr - 日期字符串 (yyyy-MM-dd)
 * @returns {string} 格式化后的日期字符串
 */
const formatGroupDate = (dateStr) => {
  const date = parseISO(dateStr);

  if (isToday(date)) {
    return "今天";
  } else if (isYesterday(date)) {
    return "昨天";
  } else if (isThisWeek(date)) {
    return `本周 ${format(date, "EEEE", { locale: zhCN })}`;
  } else if (isThisMonth(date)) {
    return format(date, "MM月dd日", { locale: zhCN });
  } else {
    return format(date, "yyyy年MM月dd日", { locale: zhCN });
  }
};

/**
 * 格式化时间显示
 * @param {string} timestamp - 时间戳
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (timestamp) => {
  return format(new Date(timestamp), "HH:mm:ss");
};

/**
 * 获取阅读历史记录
 * @param {boolean} loadMore - 是否加载更多
 */
const fetchHistoryList = async (loadMore = false) => {
  if (loading.value) return;

  loading.value = true;

  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 800));

    // 模拟数据
    const startDate =
      dateRange.value && dateRange.value[0]
        ? new Date(dateRange.value[0])
        : null;
    const endDate =
      dateRange.value && dateRange.value[1]
        ? new Date(dateRange.value[1])
        : null;

    // 生成过去30天的随机历史记录
    const now = new Date();
    const mockHistory = [];

    for (let i = 0; i < 50; i++) {
      const randomDaysAgo = Math.floor(Math.random() * 30);
      const randomHours = Math.floor(Math.random() * 24);
      const randomMinutes = Math.floor(Math.random() * 60);
      const randomSeconds = Math.floor(Math.random() * 60);

      const timestamp = new Date(now);
      timestamp.setDate(timestamp.getDate() - randomDaysAgo);
      timestamp.setHours(randomHours, randomMinutes, randomSeconds);

      // 如果设置了日期范围，则过滤
      if (startDate && timestamp < startDate) continue;
      if (endDate && timestamp > endDate) continue;

      const id = 1000 + i;
      mockHistory.push({
        id: id,
        title: `阅读书籍 ${i + 1}`,
        author: `作者 ${(i % 10) + 1}`,
        cover: getBookDefaultCover(id),
        category: ["小说", "历史", "科技", "文学", "艺术"][i % 5],
        progress: Math.floor(Math.random() * 100),
        timestamp: timestamp.toISOString(),
      });
    }

    // 按时间戳降序排序
    mockHistory.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    // 分页
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    const pageData = mockHistory.slice(start, end);

    if (loadMore) {
      historyList.value = [...historyList.value, ...pageData];
    } else {
      historyList.value = pageData;
    }

    // 判断是否还有更多数据
    hasMoreHistory.value = end < mockHistory.length;
  } catch (error) {
    console.error("获取阅读历史失败", error);
    ElMessage.error("获取阅读历史失败，请刷新重试");
  } finally {
    loading.value = false;
  }
};

/**
 * 加载更多历史记录
 */
const loadMoreHistory = () => {
  currentPage.value++;
  fetchHistoryList(true);
};

/**
 * 处理日期范围变化
 */
const handleDateChange = () => {
  currentPage.value = 1;
  fetchHistoryList();
};

/**
 * 查看书籍详情
 * @param {number} bookId - 书籍ID
 */
const viewBookDetail = (bookId) => {
  router.push(`/book/${bookId}`);
};

/**
 * 继续阅读
 * @param {number} bookId - 书籍ID
 */
const continueReading = (bookId) => {
  router.push(`/book/${bookId}?action=read`);
};

/**
 * 删除单条历史记录
 * @param {number} bookId - 书籍ID
 * @param {string} timestamp - 时间戳
 */
const removeHistoryItem = (bookId, timestamp) => {
  ElMessageBox.confirm("确定要删除该条阅读记录吗？", "删除记录", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      // 模拟API调用
      historyList.value = historyList.value.filter(
        (item) => !(item.id === bookId && item.timestamp === timestamp)
      );

      ElMessage.success("已删除阅读记录");
    })
    .catch(() => {
      // 取消操作
    });
};

/**
 * 清空所有历史记录
 */
const clearHistory = () => {
  if (historyList.value.length === 0) {
    ElMessage.warning("暂无阅读历史");
    return;
  }

  ElMessageBox.confirm(
    "确定要清空所有阅读历史吗？此操作不可恢复。",
    "清空历史",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      // 模拟API调用
      historyList.value = [];
      hasMoreHistory.value = false;

      ElMessage.success("已清空所有阅读历史");
    })
    .catch(() => {
      // 取消操作
    });
};

/**
 * 跳转到首页
 */
const goToHome = () => {
  router.push("/");
};

onMounted(() => {
  // 获取阅读历史记录
  fetchHistoryList();
});
</script>

<style scoped>
.user-history {
  position: relative;
}

.history-header {
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
  align-items: center;
}

.history-content {
  min-height: 400px;
}

.history-group {
  margin-bottom: 30px;
}

.date-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.date-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.date-count {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
  background-color: #f0f2f5;
  padding: 2px 8px;
  border-radius: 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.history-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 100px;
  height: 140px;
  flex-shrink: 0;
  cursor: pointer;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
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

.book-info {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.book-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
}

.book-title:hover {
  color: #409eff;
}

.book-author {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}

.book-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.book-category {
  font-size: 13px;
  color: #909399;
  padding: 2px 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
}

.read-progress {
  font-size: 13px;
  color: #409eff;
}

.read-time {
  margin-top: auto;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 20px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .history-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .history-item {
    flex-direction: column;
  }

  .book-cover {
    width: 100%;
    height: 180px;
  }

  .item-actions {
    flex-direction: row;
    padding: 12px 16px;
    justify-content: flex-end;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .section-title,
  .date-title {
    color: #e5e7eb;
  }

  .history-item {
    background-color: #1e1e1e;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
  }

  .book-title {
    color: #e5e7eb;
  }

  .book-author {
    color: #a0a0a0;
  }

  .book-category,
  .date-count {
    background-color: #2c2c2c;
    color: #a0a0a0;
  }

  .image-error {
    background-color: #2c2c2c;
    color: #a0a0a0;
  }

  .date-header {
    border-color: #333;
  }
}
</style>