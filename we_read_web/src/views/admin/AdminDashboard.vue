<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
        v-for="(card, index) in statCards"
        :key="index"
      >
        <el-card class="stat-card" :body-style="{ padding: '0px' }">
          <div class="card-content" :class="`card-${card.type}`">
            <div class="card-icon">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ formatNumber(card.value) }}</div>
              <div class="card-title">{{ card.title }}</div>
            </div>
          </div>
          <div class="card-footer">
            <span
              >{{ card.change >= 0 ? "↑" : "↓" }}
              {{ Math.abs(card.change) }}%</span
            >
            <span>较上周</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 图书分类统计 -->
      <el-col :xs="24" :lg="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>图书分类统计</span>
              <el-switch
                v-model="showTopCategories"
                active-text="仅显示前10项"
                inactive-text="显示全部"
                size="small"
              />
            </div>
          </template>
          <div class="chart-container">
            <div>
              <el-empty v-if="displayedCategories.length === 0" description="暂无分类数据" />
              <div v-else style="display:flex; flex-wrap: wrap;">
                <el-tag
                  v-for="(item, index) in displayedCategories"
                  :key="index"
                  type="info"
                  effect="plain"
                  style="margin: 4px"
                >
                  {{ item }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动和待办事项 -->
    <el-row :gutter="20" class="activity-row">
      <!-- 最近活动 -->
      <el-col :xs="24" :lg="16">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近系统活动</span>
              <el-button text @click="refreshActivities">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="activitiesLoading">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentActivities"
                :key="index"
                :type="activity.type"
                :color="getActivityColor(activity.type)"
                :timestamp="activity.time"
                :hollow="activity.hollow"
              >
                {{ activity.content }}
                <div class="activity-detail">{{ activity.detail }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>

      <!-- 待办事项 -->
      <el-col :xs="24" :lg="8">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button text @click="addTodoItem">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="todosLoading">
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" />
            <el-checkbox-group
              v-model="checkedTodos"
              @change="handleTodoChange"
            >
              <div
                v-for="(todo, index) in todoList"
                :key="index"
                class="todo-item"
              >
                <el-checkbox :value="todo.id" :disabled="todo.completed">
                  <span :class="{ 'todo-completed': todo.completed }">{{
                    todo.content
                  }}</span>
                </el-checkbox>
                <div class="todo-actions">
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="removeTodoItem(todo.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-checkbox-group>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="system-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="服务器状态">
              <el-tag type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="数据库状态">
              <el-tag type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="缓存状态">
              <el-tag type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="上次更新"
              >2025-03-20 15:30:00</el-descriptions-item
            >
            <el-descriptions-item label="运行时间"
              >30天12小时</el-descriptions-item
            >
            <el-descriptions-item label="CPU使用率">32%</el-descriptions-item>
            <el-descriptions-item label="内存使用率">45%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加待办事项对话框 -->
    <el-dialog
      v-model="todoDialogVisible"
      title="添加待办事项"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="newTodo" ref="todoForm" :rules="todoRules">
        <el-form-item prop="content">
          <el-input
            v-model="newTodo.content"
            placeholder="请输入待办事项内容"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="todoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTodoForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
  
  <script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useAdminStore } from "@/stores/modules/admin";
import { adminApi } from "@/api/admin";
import { Refresh, Plus, Delete } from "@element-plus/icons-vue";

// 统计卡片数据
const statCards = ref([
  { title: "总用户数", value: 0, change: 0, icon: "User", type: "primary" },
  { title: "图书总数", value: 0, change: 0, icon: "Reading", type: "success" },
  { title: "收藏总数", value: 0, change: 0, icon: "Star", type: "warning" },
  { title: "本月新增", value: 0, change: 0, icon: "Coin", type: "danger" },
]);

// 分类数据
const categories = ref([]);
const showTopCategories = ref(true);
const displayedCategories = computed(() => {
  const list = categories.value || [];
  return showTopCategories.value ? list.slice(0, 10) : list;
});

// 最近活动
const recentActivities = ref([]);
const activitiesLoading = ref(false);

// 待办事项
const todoList = ref([]);
const checkedTodos = ref([]);
const todosLoading = ref(false);
const todoDialogVisible = ref(false);
const newTodo = reactive({
  content: "",
});
const todoRules = {
  content: [
    { required: true, message: "请输入待办事项内容", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
};

// 管理员状态
const adminStore = useAdminStore();

// 格式化数字
const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

// 获取活动类型对应的颜色
const getActivityColor = (type) => {
  const colorMap = {
    primary: "#409EFF",
    success: "#67C23A",
    warning: "#E6A23C",
    danger: "#F56C6C",
    info: "#909399",
  };
  return colorMap[type] || colorMap.primary;
};

// 刷新活动列表
const refreshActivities = async () => {
  activitiesLoading.value = true;
  try {
    const res = await adminApi.getActivities();
    if (res?.meta?.code === 200) {
      recentActivities.value = (res.data || []).map(item => ({
        ...item,
        time: item.time ? new Date(item.time).toLocaleString() : ''
      }));
    }
  } catch (error) {
    console.error("刷新活动列表失败:", error);
    ElMessage.error("刷新活动列表失败");
  } finally {
    activitiesLoading.value = false;
  }
};

// 加载待办事项
const loadTodoList = async () => {
  todosLoading.value = true;
  try {
    const res = await adminApi.getTodos();
    if (res?.meta?.code === 200) {
      todoList.value = res.data || [];
      // 设置已完成的待办项
      checkedTodos.value = todoList.value
        .filter((item) => item.completed)
        .map((item) => item.id);
    }
  } catch (error) {
    console.error("加载待办事项失败:", error);
    ElMessage.error("加载待办事项失败");
  } finally {
    todosLoading.value = false;
  }
};

// 处理待办事项状态变更
const handleTodoChange = async (value) => {
  // 找出刚刚改变状态的项
  for (const todo of todoList.value) {
    const isNowCompleted = value.includes(todo.id);
    if (todo.completed !== isNowCompleted) {
      try {
        await adminApi.updateTodo(todo.id, { completed: isNowCompleted });
        todo.completed = isNowCompleted;
      } catch (e) {
        ElMessage.error("更新状态失败");
        // 恢复勾选状态
        if (isNowCompleted) {
          checkedTodos.value = checkedTodos.value.filter(id => id !== todo.id);
        } else {
          checkedTodos.value.push(todo.id);
        }
      }
    }
  }
};

// 添加待办事项
const addTodoItem = () => {
  newTodo.content = "";
  todoDialogVisible.value = true;
};

// 提交待办事项表单
const submitTodoForm = async () => {
  if (!newTodo.content.trim()) {
    ElMessage.warning("请输入待办事项内容");
    return;
  }

  try {
    const res = await adminApi.addTodo({ content: newTodo.content, completed: false });
    if (res?.meta?.code === 200) {
      todoList.value.unshift(res.data);
      todoDialogVisible.value = false;
      ElMessage.success("添加待办事项成功");
    }
  } catch (error) {
    console.error("添加待办事项失败:", error);
    ElMessage.error("添加待办事项失败");
  }
};

// 删除待办事项
const removeTodoItem = (id) => {
  ElMessageBox.confirm("确定要删除这个待办事项吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await adminApi.deleteTodo(id);
        todoList.value = todoList.value.filter((item) => item.id !== id);
        checkedTodos.value = checkedTodos.value.filter((itemId) => itemId !== id);
        ElMessage.success("删除成功");
      } catch (e) {
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

// 获取系统统计数据
const getStatistics = async () => {
  try {
    const response = await adminApi.getStatistics();
    if (response?.meta?.code === 200) {
      const data = response.data || {};
      const userCount =
        data.userCount ?? data.users ?? data.totalUsers ?? 0;
      const bookCount =
        data.bookCount ?? data.books ?? data.totalBooks ?? 0;
      const favoriteCount =
        data.favoriteCount ?? data.favorites ?? data.totalFavorites ?? 0;
      const newCount =
        data.newBookCount ?? data.newCount ?? data.addedBooks ?? data.monthNewBooks ?? 0;

      statCards.value[0].value = userCount;
      statCards.value[1].value = bookCount;
      statCards.value[2].value = favoriteCount;
      statCards.value[3].value = newCount;

      statCards.value.forEach((c) => {
        if (typeof c.change !== "number") c.change = 0;
      });
    } else {
      await loadStatisticsFallback();
    }
  } catch (error) {
    await loadStatisticsFallback();
  }
};

// 生命周期钩子
onMounted(async () => {
  // 加载数据
  getStatistics();
  refreshActivities();
  loadTodoList();
  fetchCategories();

  window.addEventListener("admin-book-added", () => {
    statCards.value[1].value = Number(statCards.value[1].value || 0) + 1;
    statCards.value[3].value = Number(statCards.value[3].value || 0) + 1;
  });

});

async function loadStatisticsFallback() {
  try {
    const [userRes, bookRes] = await Promise.all([
      adminApi.getUserList({ page: 1, size: 1 }),
      adminApi.getBookList({ page: 1, size: 1 }),
    ]);
    const userTotal =
      userRes?.data?.totalElements ??
      userRes?.data?.total ??
      0;
    const bookTotal =
      bookRes?.data?.totalElements ??
      bookRes?.data?.total ??
      0;
    statCards.value[0].value = Number(userTotal) || 0;
    statCards.value[1].value = Number(bookTotal) || 0;
    // 计算收藏总数：遍历图书分页累加 Collection_Count 字段
    try {
      let page = 1;
      const size = 100;
      let totalPages = Math.ceil((bookTotal || 0) / size) || 1;
      let sum = 0;
      // 安全上限，防止意外大数据导致阻塞
      const maxPages = Math.min(totalPages, 50);
      for (; page <= maxPages; page++) {
        const resp = await adminApi.getBookList({ page, size });
        const list = resp?.data?.content || [];
        for (const item of list) {
          const c = Number(item?.Collection_Count || item?.collectionCount || 0);
          if (!Number.isNaN(c)) sum += c;
        }
        // 提前结束：最后一页不足 size
        if (list.length < size) break;
      }
      statCards.value[2].value = sum;
    } catch (_) {
      if (!statCards.value[2].value) statCards.value[2].value = 0;
    }
    if (!statCards.value[3].value) statCards.value[3].value = 0;
  } catch (e) {
    ElMessage.error("获取统计数据失败");
  }
}

const fetchCategories = async () => {
  try {
    const res = await adminApi.getCategoryList();
    const data = res?.data || [];
    const flat = [];
    const stack = Array.isArray(data) ? [...data] : [];
    while (stack.length) {
      const item = stack.shift();
      if (!item) continue;
      const name = item.Name || item.name || item.title || "";
      if (name) flat.push(name);
      const children = item.children || item.Childrens || item.childrens || [];
      if (Array.isArray(children) && children.length) {
        for (const c of children) stack.push(c);
      }
    }
    categories.value = flat;
  } catch (e) {
    categories.value = [];
  }
};
</script>
  
  <style scoped>
.admin-dashboard {
  padding-bottom: 20px;
}

/* 统计卡片样式 */
.stat-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
  color: #fff;
}

.card-primary {
  background: linear-gradient(135deg, #409eff, #53a8ff);
}

.card-success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.card-danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.card-icon {
  font-size: 48px;
  margin-right: 20px;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}

.card-title {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 5px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 12px;
}

/* 图表卡片样式 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 100%;
}

.chart-container {
  height: 300px;
}

/* 活动和待办事项样式 */
.activity-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-completed {
  text-decoration: line-through;
  color: #909399;
}

.todo-actions {
  display: flex;
  gap: 5px;
}

/* 系统信息卡片 */
.system-card {
  margin-bottom: 20px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .card-icon {
    font-size: 36px;
    margin-right: 15px;
  }

  .card-value {
    font-size: 20px;
  }

  .chart-container {
    height: 250px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .todo-item {
    border-bottom-color: #363636;
  }
}
</style>
