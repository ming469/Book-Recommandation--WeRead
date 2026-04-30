<template>
  <div class="admin-page" v-loading="loading">
    <div class="header">
      <h2>分类管理</h2>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="6">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>全部分类</span>
            </div>
          </template>
          <el-tree
            :data="categoryTree"
            :props="treeProps"
            node-key="Category_ID"
            highlight-current
            @node-click="handleCategorySelect"
            :expand-on-click-node="false"
            :default-expand-all="false"
          >
            <template #default="{ data }">
              <span>{{ data.Name || data.name || data.title }}</span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="18">
        <el-card>
          <template #header>
            <div class="card-header">
              <div class="title">
                <span>分类下图书</span>
                <el-tag v-if="currentCategoryName" size="small" type="info" effect="plain" style="margin-left:8px;">
                  {{ currentCategoryName }}
                </el-tag>
              </div>
              <div class="actions">
                <el-button type="primary" :disabled="!currentCategoryId" @click="openAdd">添加图书</el-button>
                <el-button type="danger" plain :disabled="!multipleSelected.length" @click="batchDelete">删除选中</el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!currentCategoryId" description="请选择左侧分类查看图书" />

          <template v-else>
            <el-table
              :data="books"
              border
              style="width: 100%"
              @selection-change="onSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="序号" width="80">
                <template #default="scope">{{ (page - 1) * size + scope.$index + 1 }}</template>
              </el-table-column>
              <el-table-column label="书名" min-width="180">
                <template #default="{ row }">{{ row.Title || row.title }}</template>
              </el-table-column>
              <el-table-column label="ISBN" min-width="140">
                <template #default="{ row }">{{ row.ISBN || row.Isbn || row.isbn }}</template>
              </el-table-column>
              <el-table-column label="作者" min-width="140">
                <template #default="{ row }">{{ authorText(row) }}</template>
              </el-table-column>
              <el-table-column label="出版社" min-width="140">
                <template #default="{ row }">{{ row.Publisher || row.publisher }}</template>
              </el-table-column>
              <el-table-column prop="Status" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.Status === 1 ? 'success' : 'info'">{{ row.Status === 1 ? '上架' : '下架' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>

            <div class="pager">
              <el-pagination
                v-model:current-page="page"
                v-model:page-size="size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :page-sizes="[10, 20, 50, 100]"
                background
                @size-change="fetchBooks"
                @current-change="fetchBooks"
              />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialog.visible" title="添加图书" width="600px" :close-on-click-modal="false">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="书名">
          <el-input v-model="dialog.form.Title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="dialog.form.ISBN" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="dialog.form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="dialog.form.Publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="dialog.form.Status" placeholder="请选择">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { adminApi } from "@/api/admin";

const loading = ref(false);

const categoryTree = ref([]);
const treeProps = { children: "children", label: "Name" };

const currentCategoryId = ref(null);
const currentCategoryName = ref("");

const books = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const multipleSelected = ref([]);
const authorCache = ref({});

const dialog = ref({
  visible: false,
  submitting: false,
  form: {
    Title: "",
    ISBN: "",
    author: "",
    Publisher: "",
    Status: 1,
  },
});

const findFirstLeafId = (node) => {
  if (!node) return null;
  const hasChildren = Array.isArray(node.children) && node.children.length > 0;
  if (!hasChildren) {
    return node.Category_ID || node.CategoryId || node.id || null;
  }
  const stack = [...node.children];
  while (stack.length) {
    const n = stack.shift();
    const c = Array.isArray(n.children) && n.children.length > 0;
    if (!c) {
      return n.Category_ID || n.CategoryId || n.id || null;
    }
    stack.unshift(...n.children);
  }
  return node.Category_ID || node.CategoryId || node.id || null;
};

const handleCategorySelect = async (node) => {
  const rawId = node.Category_ID || node.CategoryId || node.id;
  const effId = Array.isArray(node.children) && node.children.length ? findFirstLeafId(node) : rawId;
  currentCategoryId.value = !isNaN(effId) ? Number(effId) : effId;
  currentCategoryName.value = node.Name || node.name || node.title || "";
  page.value = 1;
  await fetchBooks();
};

const fetchCategories = async () => {
  loading.value = true;
  try {
    const res = await adminApi.getCategoryList();
    const data = res?.data || [];
    const normalize = (list) =>
      (list || []).map((it) => ({
        ...it,
        Category_ID: it.Category_ID ?? it.CategoryId ?? it.id,
        Name: it.Name ?? it.name ?? it.title,
        children: it.children ?? it.Childrens ?? it.childrens ?? [],
      }));
    categoryTree.value = normalize(data);
  } catch (e) {
    ElMessage.error("加载分类失败");
    categoryTree.value = [];
  } finally {
    loading.value = false;
  }
};

const fetchBooks = async () => {
  if (!currentCategoryId.value) return;
  loading.value = true;
  try {
    const res = await adminApi.getBookList({
      page: page.value,
      size: size.value,
      categoryId: !isNaN(currentCategoryId.value) ? Number(currentCategoryId.value) : currentCategoryId.value,
    });
    if (res?.meta?.code === 200) {
      const data = res.data || {};
      const list = data.content || data.list || data.records || data.items || [];
      books.value = Array.isArray(list) ? list : [];
      total.value = data.totalElements ?? data.total ?? 0;
      size.value = data.size || size.value;
      page.value = data.currentPage || page.value;
      await resolveAuthorsForPage();
    } else {
      ElMessage.error("获取图书失败");
    }
  } catch (e) {
    ElMessage.error("获取图书失败");
  } finally {
    loading.value = false;
  }
};

const authorText = (row) => {
  const id = row.Book_ID || row.id;
  const title = row.Title || row.title || '';
  const key = id || `t:${title}`;
  if (authorCache.value[key]) return authorCache.value[key];
  return (row.authorName || row.AuthorName || row.Author_Name || row.author || row.Author) || '未知作者';
};

const resolveAuthorsForPage = async () => {
  const items = Array.isArray(books.value) ? books.value : [];
  for (const row of items) {
    const id = row.Book_ID || row.id;
    const title = row.Title || row.title || '';
    const key = id || `t:${title}`;
    if (authorCache.value[key]) continue;
    try {
      let authorValue = '';
      if (id) {
        const detail = await adminApi.getBookDetail(id);
        const d = detail?.data || {};
        authorValue = d.authorName || d.AuthorName || d.Author || d.author || '';
      } else if (title) {
        const resp = await adminApi.getBookList({ page: 1, size: 1, keyword: title });
        const item = (resp?.data?.content || [])[0] || {};
        authorValue = item.authorName || item.AuthorName || item.Author || item.author || '';
      }
      if (authorValue) {
        authorCache.value[key] = authorValue;
      }
    } catch (_) {}
  }
};

const onSelectionChange = (val) => {
  multipleSelected.value = val || [];
};

const openAdd = () => {
  dialog.value.form = {
    Title: "",
    ISBN: "",
    author: "",
    Publisher: "",
    Status: 1,
  };
  dialog.value.visible = true;
};

const submitAdd = async () => {
  const f = dialog.value.form;
  if (!f.Title?.trim() || !f.ISBN?.trim() || !currentCategoryId.value) {
    ElMessage.warning("请完整填写书名、ISBN，并选择分类");
    return;
  }
  dialog.value.submitting = true;
  try {
    const book = {
      Title: f.Title?.trim(),
      ISBN: f.ISBN?.trim(),
      author: f.author?.trim() || "",
      Publisher: f.Publisher?.trim() || "",
      Category_ID: currentCategoryId.value,
      Status: Number.isInteger(f.Status) ? f.Status : 1,
      Language: "中文",
      Pages: 0,
      Has_Ebook: 0,
      Description: "",
    };
    const category = { Category_ID: currentCategoryId.value };
    const payload = { book, category };
    const res = await adminApi.addBook(payload);
    if (res?.meta?.code === 200) {
      ElMessage.success("图书已添加");
      dialog.value.visible = false;
      fetchBooks();
      window.dispatchEvent(new CustomEvent("admin-book-added"));
    } else {
      ElMessage.error("添加失败");
    }
  } catch (e) {
    ElMessage.error("添加失败");
  } finally {
    dialog.value.submitting = false;
  }
};

const deleteOne = (row) => {
  ElMessageBox.confirm(`确定删除《${row.Title}》吗？`, "删除图书", {
    type: "warning",
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(async () => {
      try {
        await adminApi.deleteBook(row.Book_ID || row.id);
        ElMessage.success("已删除");
        fetchBooks();
      } catch (e) {
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

const batchDelete = () => {
  if (!multipleSelected.value.length) return;
  ElMessageBox.confirm(
    `确定删除选中的 ${multipleSelected.value.length} 本图书吗？`,
    "批量删除",
    {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    }
  )
    .then(async () => {
      const ids = multipleSelected.value.map((it) => it.Book_ID || it.id);
      try {
        // 后端若支持批量删除接口可替换为 batchDeleteBooks
        for (const id of ids) {
          await adminApi.deleteBook(id);
        }
        ElMessage.success("已删除");
        multipleSelected.value = [];
        fetchBooks();
      } catch (e) {
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

fetchCategories();
</script>

<style scoped>
.admin-page { padding: 16px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 8px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
