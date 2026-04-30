<template>
  <div class="admin-book-list">
    <!-- 搜索和操作区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="书名/作者/ISBN" clearable @input="debouncedSearch"
            @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader v-model="searchForm.categoryId" :options="bookAdminStore.categoryOptions" :props="{
            checkStrictly: true,
            value: 'Category_ID',
            label: 'Name',
            children: 'children',
          }" placeholder="选择分类" clearable style="width: 220px" @change="debouncedSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px"
            @change="debouncedSearch">
            <el-option :label="'上架'" :value="1" />
            <el-option :label="'下架'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="电子书">
          <el-select v-model="searchForm.hasEbook" placeholder="电子书" clearable style="width: 120px"
            @change="debouncedSearch">
            <el-option :label="'有'" :value="1" />
            <el-option :label="'无'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="searchLoading">
            <el-icon>
              <Search />
            </el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 搜索历史记录 -->
      <div v-if="searchHistory.length > 0" class="search-history">
        <span class="history-label">搜索历史：</span>
        <el-tag v-for="(item, index) in searchHistory.slice(0, 5)" :key="index" size="small" closable
          @click="applySearchHistory(item)" @close="removeSearchHistory(index)" class="history-tag">
          {{ formatHistoryLabel(item) }}
        </el-tag>
        <el-button v-if="searchHistory.length > 5" type="text" size="small" @click="showAllHistory = !showAllHistory">
          {{ showAllHistory ? '收起' : '更多...' }}
        </el-button>
        <el-button type="text" size="small" @click="clearSearchHistory" style="margin-left: 10px">
          清空历史
        </el-button>

        <!-- 所有历史记录 -->
        <div v-if="showAllHistory" class="all-history">
          <el-tag v-for="(item, index) in searchHistory.slice(5)" :key="index + 5" size="small" closable
            @click="applySearchHistory(item)" @close="removeSearchHistory(index + 5)" class="history-tag">
            {{ formatHistoryLabel(item) }}
          </el-tag>
        </div>
      </div>

      <div class="operation-bar">
        <el-button type="primary" @click="handleAddBook">
          <el-icon>
            <Plus />
          </el-icon>
          添加图书
        </el-button>
        <el-button type="success" @click="handleImportBooks">
          <el-icon>
            <Upload />
          </el-icon>
          批量导入
        </el-button>
        <el-button type="danger" :disabled="!selectedBooks.length" @click="handleBatchDelete">
          <el-icon>
            <Delete />
          </el-icon>
          批量删除
        </el-button>
        <el-button @click="refreshTable">
          <el-icon>
            <Refresh />
          </el-icon>
          刷新
        </el-button>
        <el-button type="info" @click="exportBooks">
          <el-icon>
            <Download />
          </el-icon>
          导出数据
        </el-button>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table v-loading="bookAdminStore.loading" :data="bookAdminStore.bookList" style="width: 100%"
        @selection-change="handleSelectionChange" @sort-change="handleSortChange" border stripe highlight-current-row>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="cover" label="封面" width="80" align="center">
          <template #default="scope">
            <el-image :src="getBookDefaultCover(scope.row.Book_ID)"
              :preview-src-list="[getBookDefaultCover(scope.row.Book_ID)]" fit="cover"
              style="width: 50px; height: 70px; border-radius: 4px"
              referrer-policy="no-referrer"
              loading="lazy">
              <template #error>
                <div class="image-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="Title" label="书名" min-width="180" sortable="custom" show-overflow-tooltip>
          <template #default="scope">
            <div class="book-title">
              <span>{{ scope.row.Title }}</span>
              <el-tag v-if="scope.row.isRecommended" size="small" type="warning">推荐</el-tag>
              <el-tag v-if="scope.row.Has_Ebook === 1" size="small" type="success">电子书</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" min-width="120" sortable="custom" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" sortable="custom" show-overflow-tooltip />
        <el-table-column prop="ISBN" label="ISBN" width="130" show-overflow-tooltip />
        <el-table-column prop="Status" label="状态" width="80" sortable="custom" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.Status === 1 ? 'success' : 'info'" effect="light">
              {{ scope.row.Status === 1 ? "上架" : "下架" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="View_Count" label="浏览量" width="90" sortable="custom" align="center" />
        <el-table-column prop="Collection_Count" label="收藏数" width="90" sortable="custom" align="center" />
        <el-table-column prop="avgRating" label="评分" width="90" sortable="custom" align="center">
          <template #default="scope">
            <div class="rating-display">
              <el-rate v-model="scope.row.avgRating" disabled text-color="#ff9900" score-template="{value}"
                :show-score="true" :max="5" :allow-half="true" />
              <span class="rating-count">({{ scope.row.Rating_Count || 0 }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="Created_At" label="创建时间" width="160" sortable="custom" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.Created_At) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditBook(scope.row)" link>
              编辑
            </el-button>
            <el-button type="success" size="small" @click="handlePreviewBook(scope.row)" link>
              预览
            </el-button>
            <el-button :type="scope.row.Status === 1 ? 'warning' : 'success'" size="small"
              @click="handleToggleStatus(scope.row)" link>
              {{ scope.row.Status === 1 ? "下架" : "上架" }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteBook(scope.row)" link>
              删除
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="searchActive ? '没有找到匹配的图书数据' : '暂无图书数据'" :image-size="200">
            <template #default>
              <div v-if="searchActive" class="empty-search-actions">
                <el-button @click="resetSearch">清除搜索条件</el-button>
                <el-button type="primary" @click="handleAddBook">添加新图书</el-button>
              </div>
              <div v-else class="empty-actions">
                <el-button type="primary" @click="handleAddBook">添加第一本图书</el-button>
                <el-button type="success" @click="handleImportBooks">批量导入图书</el-button>
              </div>
            </template>
          </el-empty>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" background />
      </div>
    </el-card>

    <!-- 图书表单对话框 -->
    <el-dialog v-model="bookDialogVisible" :title="isEdit ? '编辑图书' : '添加图书'" width="70%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="100px" class="book-form">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="16">
                <el-form-item label="书名" prop="Title">
                  <el-input v-model="bookForm.Title" placeholder="请输入书名" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="ISBN" prop="ISBN">
                  <el-input v-model="bookForm.ISBN" placeholder="请输入ISBN" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="作者" prop="author">
                  <el-input v-model="bookForm.author" placeholder="请输入作者名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出版社" prop="Publisher">
                  <el-input v-model="bookForm.Publisher" placeholder="请输入出版社" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="分类" prop="Category_ID">
                  <el-cascader v-model="bookForm.Category_ID" :options="bookAdminStore.categoryOptions" :props="{
                    checkStrictly: true,
                    value: 'Category_ID',
                    label: 'Name',
                    children: 'children',
                  }" placeholder="请选择分类" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="出版日期" prop="Publication_Date">
                  <el-date-picker v-model="bookForm.Publication_Date" type="date" placeholder="选择出版日期"
                    style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="状态" prop="Status">
                  <el-select v-model="bookForm.Status" placeholder="请选择状态" style="width: 100%">
                    <el-option :label="'上架'" :value="1" />
                    <el-option :label="'下架'" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="页数" prop="Pages">
                  <el-input-number v-model="bookForm.Pages" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="语言" prop="Language">
                  <el-select v-model="bookForm.Language" placeholder="请选择语言" style="width: 100%">
                    <el-option label="中文" value="中文" />
                    <el-option label="英文" value="英文" />
                    <el-option label="日文" value="日文" />
                    <el-option label="法文" value="法文" />
                    <el-option label="德文" value="德文" />
                    <el-option label="西班牙文" value="西班牙文" />
                    <el-option label="俄文" value="俄文" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="电子书" prop="Has_Ebook">
                  <el-switch v-model="bookForm.Has_Ebook" :active-value="1" :inactive-value="0" active-text="有"
                    inactive-text="无" />
                </el-form-item>
              </el-col>
            </el-row>



            <el-form-item label="简介" prop="Description">
              <el-input v-model="bookForm.Description" type="textarea" :rows="4" placeholder="请输入图书简介" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="文件管理" name="media">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="电子书文件" prop="File_Path" v-if="bookForm.Has_Ebook === 1">
                  <el-upload class="ebook-uploader" action="/api/admin/upload/ebook" :headers="uploadHeaders"
                    :on-success="handleEbookSuccess" :before-upload="beforeEbookUpload" :limit="1">
                    <el-button type="primary">
                      <el-icon>
                        <Upload />
                      </el-icon>
                      上传电子书
                    </el-button>
                    <template #tip>
                      <div class="el-upload__tip">
                        支持 PDF, EPUB 格式，不超过50MB
                      </div>
                    </template>
                  </el-upload>
                  <div v-if="bookForm.File_Path" class="file-info">
                    <el-icon>
                      <Document />
                    </el-icon>
                    <span>{{ getFileName(bookForm.File_Path) }}</span>
                    <el-button type="danger" size="small" @click="removeEbook" link>
                      移除
                    </el-button>
                  </div>
                </el-form-item>
                <el-alert v-else title="非电子书类型无需上传文件" type="info" :closable="false" show-icon />
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBookForm" :loading="submitLoading">
            {{ isEdit ? "保存修改" : "添加图书" }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入图书" width="500px">
      <el-upload class="import-uploader" action="/api/admin/books/import" :headers="uploadHeaders"
        :on-success="handleImportSuccess" :on-error="handleImportError" :before-upload="beforeImportUpload"
        accept=".xlsx,.xls,.csv" drag>
        <el-icon class="el-icon--upload">
          <Upload />
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .xlsx, .xls, .csv 格式文件，<el-link type="primary" :underline="false"
              @click="downloadTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, toRefs, watch, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Refresh,
  Plus,
  Upload,
  Delete,
  Download,
  Picture,
  Document,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
import { useBookAdminStore } from "@/stores/modules/book-admin";
import { formatDate as formatDateUtil } from "@/utils/filters";
import { useRoute, useRouter } from "vue-router";
import { getBookDefaultCover } from "@/utils/cover-helper";

const route = useRoute();
const router = useRouter();

// 用户状态
const userStore = useUserStore();

// 图书管理状态
const bookAdminStore = useBookAdminStore();

// 上传请求头（包含认证信息）
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`,
  };
});

// 表格数据
const selectedBooks = ref([]);
// 搜索加载状态
const searchLoading = ref(false);
// 搜索历史记录
const searchHistory = ref(JSON.parse(localStorage.getItem('bookSearchHistory') || '[]'));
const showAllHistory = ref(false);
// 表格排序
const sortParams = ref({
  prop: '',
  order: ''
});

// 判断是否有活跃的搜索条件
const searchActive = computed(() => {
  return Object.values(searchForm).some(value => {
    if (value === undefined || value === null) return false;
    if (typeof value === 'string') return value.trim() !== '';
    return true;
  });
});

// 搜索表单
const searchForm = reactive({
  keyword: "",
  categoryId: null,
  status: "",
  hasEbook: "",
});

// 防抖定时器
let searchDebounceTimer = null;

// 防抖搜索函数
const debouncedSearch = () => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
  searchDebounceTimer = setTimeout(() => {
    handleSearch();
  }, 500); // 500ms的防抖延迟
};

// 分页
const { pagination } = toRefs(bookAdminStore);

// 对话框控制
const bookDialogVisible = ref(false);
const importDialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const activeTab = ref("basic");

// 表单引用
const bookFormRef = ref(null);

// 图书表单
const bookForm = reactive({
  Book_ID: null,
  Title: "",
  ISBN: "",
  author: "",
  Publisher: "",
  Category_ID: null,
  Publication_Date: "",
  Status: 1,
  Pages: 0,
  Language: "中文",
  Has_Ebook: 0,
  File_Path: "",
  Description: "",
});

// 表单验证规则
const bookRules = {
  Title: [
    { required: true, message: "请输入书名", trigger: "blur" },
    { min: 1, max: 200, message: "长度在 1 到 200 个字符", trigger: "blur" },
  ],
  ISBN: [
    { required: true, message: "请输入ISBN", trigger: "blur" },
    { pattern: /^[0-9-]{10,17}$/, message: "ISBN格式不正确", trigger: "blur" },
  ],
  author: [{ required: true, message: "请输入作者名称", trigger: "blur" }],
  Publisher: [{ required: true, message: "请输入出版社", trigger: "blur" }],
  Category_ID: [{ required: true, message: "请选择分类", trigger: "change" }],
  Status: [{ required: true, message: "请选择状态", trigger: "change" }],
  Description: [
    { required: true, message: "请输入图书简介", trigger: "blur" },
    {
      min: 10,
      max: 2000,
      message: "长度在 10 到 2000 个字符",
      trigger: "blur",
    },
  ],
};

// 格式化日期
const formatDate = (dateString) => {
  return formatDateUtil(dateString, "YYYY-MM-DD HH:mm");
};

// 获取文件名
const getFileName = (path) => {
  if (!path) return "";
  return path.split("/").pop();
};

// 统一过滤和处理搜索参数
const getFilteredParams = () => {
  // 创建一个新对象来存储过滤后的参数
  const filteredParams = {};

  // 只添加非空值（排除null, undefined, 空字符串）
  if (searchForm.keyword?.trim()) filteredParams.keyword = searchForm.keyword.trim();
  if (searchForm.categoryId) filteredParams.categoryId = searchForm.categoryId;
  if (searchForm.status !== "" && searchForm.status !== null && searchForm.status !== undefined)
    filteredParams.status = searchForm.status;
  if (searchForm.hasEbook !== "" && searchForm.hasEbook !== null && searchForm.hasEbook !== undefined)
    filteredParams.hasEbook = searchForm.hasEbook;

  // 添加排序参数
  if (sortParams.value.prop && sortParams.value.order) {
    filteredParams.sortField = sortParams.value.prop;
    filteredParams.sortOrder = sortParams.value.order === 'ascending' ? 'asc' : 'desc';
  }

  return filteredParams;
};

// 将URL参数转换为搜索表单值
const paramsToSearchForm = (params) => {
  const result = {};

  // 处理关键词
  if (params.keyword) result.keyword = params.keyword;

  // 处理分类ID
  if (params.categoryId) {
    // 如果是数字字符串，转为数字
    result.categoryId = !isNaN(params.categoryId) ? Number(params.categoryId) : params.categoryId;
  }

  // 处理状态和电子书参数 - 明确转换为数字类型
  if (params.status !== undefined) {
    result.status = params.status === '' ? '' : Number(params.status);
  }

  if (params.hasEbook !== undefined) {
    result.hasEbook = params.hasEbook === '' ? '' : Number(params.hasEbook);
  }

  // 处理排序参数
  if (params.sortField && params.sortOrder) {
    sortParams.value = {
      prop: params.sortField,
      order: params.sortOrder === 'asc' ? 'ascending' : 'descending'
    };
  }

  return result;
};

// 更新URL参数
const updateUrlParams = () => {
  const params = getFilteredParams();

  // 更新路由，保持当前路径但更新查询参数
  router.replace({
    path: route.path,
    query: { ...params }
  });
};

// 从URL加载搜索参数
const loadSearchParamsFromUrl = () => {
  if (Object.keys(route.query).length > 0) {
    const urlParams = route.query;
    const searchFormValues = paramsToSearchForm(urlParams);

    // 将查询参数设置到搜索表单
    Object.keys(searchFormValues).forEach(key => {
      if (key in searchForm) {
        searchForm[key] = searchFormValues[key];
      }
    });

    return true; // 表示已加载URL参数
  }
  return false; // 表示没有URL参数
};

// 保存搜索历史记录
const saveSearchHistory = () => {
  const searchParams = getFilteredParams();

  // 如果没有实际的搜索条件，则不保存
  if (Object.keys(searchParams).length === 0) return;

  // 检查是否和已有的历史记录重复
  const historyString = JSON.stringify(searchParams);
  const existingIndex = searchHistory.value.findIndex(item =>
    JSON.stringify(item) === historyString
  );

  // 如果存在，先删除旧的记录
  if (existingIndex !== -1) {
    searchHistory.value.splice(existingIndex, 1);
  }

  // 添加到历史记录开头
  searchHistory.value.unshift(searchParams);

  // 限制历史记录数量为10条
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10);
  }

  // 保存到localStorage
  localStorage.setItem('bookSearchHistory', JSON.stringify(searchHistory.value));
};

// 格式化历史记录显示标签
const formatHistoryLabel = (historyItem) => {
  const labels = [];

  if (historyItem.keyword) labels.push(`关键词: ${historyItem.keyword}`);
  if (historyItem.categoryId) {
    const category = findCategoryName(historyItem.categoryId);
    labels.push(`分类: ${category || historyItem.categoryId}`);
  }
  if (historyItem.status !== undefined && historyItem.status !== null && historyItem.status !== '') {
    labels.push(`状态: ${historyItem.status === 1 ? '上架' : '下架'}`);
  }
  if (historyItem.hasEbook !== undefined && historyItem.hasEbook !== null && historyItem.hasEbook !== '') {
    labels.push(`电子书: ${historyItem.hasEbook === 1 ? '有' : '无'}`);
  }

  return labels.join(', ') || '空查询';
};

// 根据分类ID查找分类名称
const findCategoryName = (categoryId) => {
  if (!bookAdminStore.categoryOptions || bookAdminStore.categoryOptions.length === 0) return null;

  // 递归查找分类名称
  const findName = (categories, id) => {
    for (const category of categories) {
      if (category.Category_ID === id) return category.Name;
      if (category.children && category.children.length > 0) {
        const childResult = findName(category.children, id);
        if (childResult) return childResult;
      }
    }
    return null;
  };

  return findName(bookAdminStore.categoryOptions, categoryId);
};

// 应用历史搜索记录
const applySearchHistory = (historyItem) => {
  // 清空当前搜索表单
  resetSearchFormWithoutFetch();

  // 设置新的搜索条件
  for (const key in historyItem) {
    if (key in searchForm) {
      searchForm[key] = historyItem[key];
    }
  }

  // 设置排序参数
  if (historyItem.sortField && historyItem.sortOrder) {
    sortParams.value = {
      prop: historyItem.sortField,
      order: historyItem.sortOrder === 'asc' ? 'ascending' : 'descending'
    };
  }

  // 执行搜索
  handleSearch();
};

// 删除指定的搜索历史
const removeSearchHistory = (index) => {
  searchHistory.value.splice(index, 1);
  localStorage.setItem('bookSearchHistory', JSON.stringify(searchHistory.value));
};

// 清空所有搜索历史
const clearSearchHistory = () => {
  ElMessageBox.confirm('确定要清空所有搜索历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    searchHistory.value = [];
    localStorage.removeItem('bookSearchHistory');
    ElMessage.success('搜索历史已清空');
  }).catch(() => { });
};

// 搜索图书
const handleSearch = async () => {
  searchLoading.value = true;
  try {
    pagination.value.page = 1;
    // 更新URL参数
    updateUrlParams();
    // 保存搜索历史
    saveSearchHistory();
    // 获取数据
    await fetchBookList();
  } catch (error) {
    ElMessage.error(`搜索失败: ${error.message || '未知错误'}`);
    console.error('搜索图书失败:', error);
  } finally {
    searchLoading.value = false;
  }
};

// 仅重置搜索表单而不触发查询
const resetSearchFormWithoutFetch = () => {
  searchForm.keyword = "";
  searchForm.categoryId = null;
  searchForm.status = "";
  searchForm.hasEbook = "";
  sortParams.value = { prop: '', order: '' };
};

// 重置搜索
const resetSearch = () => {
  resetSearchFormWithoutFetch();
  pagination.value.page = 1;
  // 清空URL参数
  router.replace({ path: route.path });
  // 执行查询
  fetchBookList();
};

// 处理表格排序变化
const handleSortChange = ({ prop, order }) => {
  sortParams.value = { prop, order };
  debouncedSearch();
};

// 刷新表格
const refreshTable = () => {
  fetchBookList();
};

// 处理选择变更
const handleSelectionChange = (selection) => {
  selectedBooks.value = selection;
};

// 处理页码变更
const handleCurrentChange = (page) => {
  pagination.value.page = page;
  fetchBookList();
  // 更新URL中的页码参数
  router.replace({
    path: route.path,
    query: { ...route.query, page }
  });
};

// 处理每页数量变更
const handleSizeChange = (size) => {
  pagination.value.size = size;
  pagination.value.page = 1;
  fetchBookList();
  // 更新URL中的页面大小参数
  router.replace({
    path: route.path,
    query: { ...route.query, size, page: 1 }
  });
};

// 添加图书
const handleAddBook = () => {
  isEdit.value = false;
  resetBookForm();
  bookDialogVisible.value = true;
  activeTab.value = "basic";
};

// 编辑图书
const handleEditBook = async (row) => {
  isEdit.value = true;
  resetBookForm();
  activeTab.value = "basic";

  try {
    // 获取图书详情
    const bookDetail = await bookAdminStore.getBookById(row.Book_ID);

    if (bookDetail) {
      // 填充表单数据
      Object.keys(bookForm).forEach((key) => {
        if (key in bookDetail) {
          bookForm[key] = bookDetail[key];
        }
      });

      bookDialogVisible.value = true;
    }
  } catch (error) {
    console.error("获取图书详情失败:", error);
    ElMessage.error("获取图书详情失败: " + (error.message || "未知错误"));
  }
};

// 预览图书
const handlePreviewBook = (row) => {
  // 在新窗口打开图书详情页
  window.open(`/book/${row.Book_ID}`, "_blank");
};

// 切换图书状态
const handleToggleStatus = async (row) => {
  const newStatus = row.Status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? "上架" : "下架";

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}《${row.Title}》吗？`,
      `图书${statusText}`,
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );

    // 调用API更新状态
    const success = await bookAdminStore.updateBookStatus(
      row.Book_ID,
      newStatus
    );

    if (success) {
      // 更新本地数据
      const index = bookAdminStore.bookList.findIndex(
        (item) => item.Book_ID === row.Book_ID
      );
      if (index !== -1) {
        bookAdminStore.bookList[index].Status = newStatus;
      }
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error(`图书${statusText}失败:`, error);
      ElMessage.error(`图书${statusText}失败: ${error.message || "未知错误"}`);
    }
  }
};

// 删除图书
const handleDeleteBook = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除《${row.Title}》吗？此操作不可恢复！`,
      "删除图书",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "danger",
      }
    );

    // 调用API删除图书
    const success = await bookAdminStore.deleteBook(row.Book_ID);

    if (success) {
      // 更新本地数据
      fetchBookList();
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除图书失败:", error);
      ElMessage.error(`删除图书失败: ${error.message || "未知错误"}`);
    }
  }
};

// 批量删除图书
const handleBatchDelete = async () => {
  if (selectedBooks.value.length === 0) {
    ElMessage.warning("请至少选择一项");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedBooks.value.length} 本图书吗？此操作不可恢复！`,
      "批量删除",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "danger",
      }
    );

    // 获取选中项的ID
    const ids = selectedBooks.value.map((item) => item.Book_ID);

    // 调用API批量删除
    const success = await bookAdminStore.batchDeleteBooks(ids);

    if (success) {
      // 更新本地数据
      fetchBookList();
      selectedBooks.value = [];
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("批量删除失败:", error);
      ElMessage.error(`批量删除失败: ${error.message || "未知错误"}`);
    }
  }
};

// 批量导入图书
const handleImportBooks = () => {
  importDialogVisible.value = true;
};

// 导入成功处理
const handleImportSuccess = (response) => {
  ElMessage.success(`成功导入 ${response.data.successCount} 本图书`);
  importDialogVisible.value = false;
  fetchBookList();
};

// 导入失败处理
const handleImportError = (error) => {
  console.error("导入失败:", error);
  if (error.response && error.response.data && error.response.data.message) {
    ElMessage.error(`导入失败: ${error.response.data.message}`);
  } else {
    ElMessage.error("导入失败，请检查文件格式");
  }
};

// 导入前检查
const beforeImportUpload = (file) => {
  const isExcel =
    file.type === "application/vnd.ms-excel" ||
    file.type ===
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
    file.type === "text/csv";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isExcel) {
    ElMessage.error("只能上传Excel或CSV文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("文件大小不能超过2MB!");
    return false;
  }
  return true;
};

// 下载模板
const downloadTemplate = () => {
  window.open("/api/admin/books/template", "_blank");
};

// 电子书上传成功
const handleEbookSuccess = (response) => {
  bookForm.File_Path = response.data.url;
  ElMessage.success("电子书上传成功");
};

// 电子书上传前检查
const beforeEbookUpload = (file) => {
  const isValidType =
    file.type === "application/pdf" ||
    file.type === "application/epub+zip" ||
    file.name.endsWith(".epub");
  const isLt50M = file.size / 1024 / 1024 < 50;

  if (!isValidType) {
    ElMessage.error("只能上传PDF或EPUB文件!");
    return false;
  }
  if (!isLt50M) {
    ElMessage.error("文件大小不能超过50MB!");
    return false;
  }
  return true;
};

// 移除电子书
const removeEbook = () => {
  bookForm.File_Path = "";
  ElMessage.success("已移除电子书文件");
};

// 导出图书数据
const exportBooks = async () => {
  try {
    const params = new URLSearchParams();
    const filteredParams = getFilteredParams();

    // 将过滤后的参数添加到URLSearchParams
    Object.entries(filteredParams).forEach(([key, value]) => {
      params.append(key, value);
    });

    const url = `/api/admin/books/export?${params.toString()}`;
    window.open(url, "_blank");
  } catch (error) {
    console.error("导出失败:", error);
    ElMessage.error(`导出失败: ${error.message || "未知错误"}`);
  }
};

// 提交图书表单
const submitBookForm = async () => {
  if (!bookFormRef.value) return;

  try {
    await bookFormRef.value.validate();

    submitLoading.value = true;

    // 准备提交的数据
    const book = { ...bookForm };

    // 处理日期格式
    if (book.Publication_Date instanceof Date) {
      book.Publication_Date =
        book.Publication_Date.toISOString().split("T")[0];
    }

    // 构造 Category 对象 (只传 ID)
    const category = {
      Category_ID: Array.isArray(book.Category_ID)
        ? book.Category_ID[book.Category_ID.length - 1]
        : book.Category_ID
    };

    // 构造后端期望的 BookRequest 结构
    const bookRequest = {
      book: book,
      category: category
    };

    let success;
    if (isEdit.value) {
      // 更新图书
      success = await bookAdminStore.updateBook(book.Book_ID, bookRequest);
    } else {
      // 添加图书
      success = await bookAdminStore.addBook(bookRequest);
    }

    if (success) {
      bookDialogVisible.value = false;
      fetchBookList(); // 刷新列表
      if (!isEdit.value) {
        try {
          window.dispatchEvent(new CustomEvent('admin-book-added'));
        } catch (_) {}
      }
    }
  } catch (error) {
    if (error.name === "ValidationError") {
      // 表单验证错误
      return;
    }
    console.error("保存图书失败:", error);
    ElMessage.error(`保存图书失败: ${error.message || "未知错误"}`);
  } finally {
    submitLoading.value = false;
  }
};

// 重置图书表单
const resetBookForm = () => {
  Object.keys(bookForm).forEach((key) => {
    if (key === "Status") {
      bookForm[key] = 1;
    } else if (key === "Has_Ebook") {
      bookForm[key] = 0;
    } else if (key === "Pages") {
      bookForm[key] = 0;
    } else if (key === "Language") {
      bookForm[key] = "中文";
    } else {
      bookForm[key] = "";
    }
  });
};

// 获取图书列表
const fetchBookList = async () => {
  try {
    // 使用过滤处理后的参数
    const params = getFilteredParams();

    // 调用store方法获取图书列表
    await bookAdminStore.getBooks({
      ...params,
      page: pagination.value.page,
      size: pagination.value.size
    });

    // 如果结果为空，且不是第一页，返回第一页
    if (bookAdminStore.bookList.length === 0 && pagination.value.page > 1 && pagination.value.total > 0) {
      pagination.value.page = 1;
      await fetchBookList();
    }
  } catch (error) {
    console.error("获取图书列表失败:", error);
    ElMessage.error(`获取图书列表失败: ${error.message || "未知错误"}`);
  }
};

// 监听搜索表单变化，应用防抖
watch(
  () => searchForm.keyword,
  () => {
    debouncedSearch();
  }
);

watch(
  () => searchForm.categoryId,
  () => {
    debouncedSearch();
  }
);

watch(
  () => searchForm.status,
  () => {
    debouncedSearch();
  }
);

watch(
  () => searchForm.hasEbook,
  () => {
    debouncedSearch();
  }
);

// 在路由变化时重新加载URL参数
watch(
  () => route.query,
  (newQuery) => {
    // 只有当URL查询参数发生实质性变化时才重新加载
    const currentParams = getFilteredParams();
    const hasCurrentParams = Object.keys(currentParams).length > 0;
    const hasNewQuery = Object.keys(newQuery).length > 0;

    // 如果两者不一致，才需要重新加载
    if (hasCurrentParams !== hasNewQuery ||
      (hasCurrentParams && JSON.stringify(currentParams) !== JSON.stringify(newQuery))) {
      loadSearchParamsFromUrl();
      fetchBookList();
    }
  }
);

// 生命周期钩子
onMounted(async () => {
  // 加载数据
  try {
    await bookAdminStore.getCategories();

    // 尝试从URL加载搜索参数
    const hasUrlParams = loadSearchParamsFromUrl();

    // 获取图书列表
    fetchBookList();

    // 如果从URL加载了参数，添加到搜索历史
    if (hasUrlParams) {
      saveSearchHistory();
    }
  } catch (error) {
    console.error("初始化页面失败:", error);
    ElMessage.error(`加载页面数据失败: ${error.message || "未知错误"}`);
  }
});

// 组件销毁前清理定时器
onBeforeUnmount(() => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
});
</script>

<style scoped>
.admin-book-list {
  padding-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.search-form {
  margin-bottom: 16px;
  padding-top: 6px;
}

.search-history {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 0 4px 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.history-label {
  font-size: 13px;
  color: #606266;
  margin-right: 10px;
  white-space: nowrap;
}

.history-tag {
  margin-right: 8px;
  margin-bottom: 6px;
  cursor: pointer;
}

.all-history {
  width: 100%;
  margin-top: 8px;
  line-height: 2;
}

.operation-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: 4px 0;
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
  margin-top: 8px;
}

.table-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

.book-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 50px;
  height: 70px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
  border-radius: 4px;
}

.empty-search-actions,
.empty-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.cover-uploader {
  width: 150px;
  height: 210px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cover-uploader:hover {
  border-color: #409eff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.cover-uploader-icon {
  font-size: 32px;
  color: #8c939d;
  width: 150px;
  height: 210px;
  line-height: 210px;
  text-align: center;
  background-color: #f8f9fa;
}

.cover-image {
  width: 150px;
  height: 210px;
  display: block;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  border-radius: 6px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.import-uploader {
  width: 100%;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: border-color 0.3s;
}

.import-uploader:hover {
  border-color: #409eff;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 10px 14px;
  background-color: #f5f7fa;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.rating-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.rating-count {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.book-form .el-form-item {
  margin-bottom: 20px;
}

.book-form .el-input-number,
.book-form .el-date-picker,
.book-form .el-select {
  width: 100%;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 20px;
}

:deep(.el-tabs__nav) {
  padding-left: 10px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  text-align: right;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .search-form {
    display: flex;
    flex-direction: column;
  }

  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }

  .operation-bar {
    justify-content: space-between;
    padding-top: 12px;
    gap: 8px;
  }

  .operation-bar .el-button {
    margin-bottom: 8px;
    padding: 8px 12px;
    font-size: 12px;
  }

  .el-dialog {
    width: 95% !important;
  }

  .cover-uploader {
    width: 120px;
    height: 168px;
  }

  .cover-uploader-icon {
    width: 120px;
    height: 168px;
    line-height: 168px;
    font-size: 24px;
  }

  .cover-image {
    width: 120px;
    height: 168px;
  }

  .pagination-container {
    margin-top: 12px;
    flex-wrap: wrap;
    justify-content: center;
  }

  .search-history {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-label {
    margin-bottom: 8px;
  }
}

@media screen and (max-width: 576px) {
  .operation-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .operation-bar .el-button {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style>
