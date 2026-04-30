<template>
  <div class="favorites" v-loading="loading">
    <div class="header">
      <h2>我的书架</h2>
      <div class="count">共 {{ total }} 本</div>
    </div>

    <el-empty v-if="!loading && books.length === 0" description="书架还是空的，去添加几本吧～" />

    <div v-else class="grid">
      <div v-for="item in books" :key="item.id" class="card">
        <img :src="item.cover || defaultCover(item.id)" :alt="item.title" class="cover" @click="goDetail(item.id)" />
        <div class="info">
          <div class="title" @click="goDetail(item.id)">{{ item.title }}</div>
          <div class="meta">
            <span class="author">{{ item.author || "佚名" }}</span>
            <span class="time">{{ formatTime(item.addedAt) }}</span>
          </div>
        </div>
        <div class="actions">
          <el-button size="small" type="danger" plain @click="confirmRemove(item.id)">移出书架</el-button>
        </div>
      </div>
    </div>

    <div class="pager" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        :current-page="page"
        :total="total"
        @current-change="handlePage"
      />
    </div>
  </div>
  </template>

  <script setup>
  import { ref, onMounted } from "vue";
  import { useRouter } from "vue-router";
  import { ElMessageBox, ElMessage } from "element-plus";
  import { userApi } from "@/api/user";
  import { bookApi } from "@/api/book";
  import { normalizeBook } from "@/utils/api-helper";
  import { getBookDefaultCover } from "@/utils/cover-helper";

  const router = useRouter();

  const loading = ref(false);
  const page = ref(1);
  const pageSize = ref(12);
  const total = ref(0);
  const books = ref([]);

  const defaultCover = (id) => getBookDefaultCover(id);

  const fetchShelf = async () => {
    loading.value = true;
    try {
      const res = await userApi.getShelf({ page: page.value, size: pageSize.value });
      if (res?.meta?.code === 200) {
        total.value = res.data?.totalItems || 0;
        const content = res.data?.content || [];
        const full = await Promise.all(
          content.map(async (row) => {
            try {
              const d = await bookApi.getBookById(row.bookId);
              if (d?.meta?.code === 200 && d.data) {
                const n = normalizeBook(d.data);
                return {
                  id: n.id,
                  title: n.title,
                  author: n.author,
                  cover: n.cover || defaultCover(n.id),
                  addedAt: row.addedAt,
                };
              }
            } catch (e) {}
            return null;
          })
        );
        books.value = full.filter(Boolean);
      } else {
        books.value = [];
        total.value = 0;
      }
    } catch (e) {
      books.value = [];
      total.value = 0;
    } finally {
      loading.value = false;
    }
  };

  const handlePage = (p) => {
    page.value = p;
    fetchShelf();
  };

  const confirmRemove = (bookId) => {
    ElMessageBox.confirm("确定将该图书移出书架吗？", "移出书架", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    })
      .then(async () => {
        try {
          await userApi.removeFromShelf(bookId);
          ElMessage.success("已移出书架");
          fetchShelf();
        } catch (e) {
          ElMessage.error("操作失败，请重试");
        }
      })
      .catch(() => {});
  };

  const goDetail = (id) => router.push(`/book/${id}`);
  const formatTime = (iso) => (iso ? new Date(iso).toLocaleDateString() : "");

  onMounted(fetchShelf);
  </script>

  <style scoped>
  .favorites { padding: 20px; }
  .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
  .count { color: #909399; }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
  .card { background: var(--el-bg-color); border-radius: 8px; padding: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
  .cover { width: 100%; border-radius: 6px; cursor: pointer; }
  .info { padding: 6px 4px; }
  .title { font-size: 14px; font-weight: 600; margin: 6px 0; cursor: pointer; }
  .meta { font-size: 12px; color: #909399; display: flex; justify-content: space-between; }
  .actions { display: flex; justify-content: flex-end; margin-top: 6px; }
  .pager { display: flex; justify-content: center; margin-top: 16px; }
  </style>
