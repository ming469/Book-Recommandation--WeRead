<template>
  <div class="categories-view">
    <div class="container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="separator">/</span>
        <span v-if="!categoryId">全部分类</span>
        <template v-else>
          <router-link to="/categories">全部分类</router-link>
          <span class="separator">/</span>
          <span>{{ categoryName }}</span>
        </template>
      </div>

      <!-- 分类列表 (当没有选定具体分类时显示) -->
      <div v-if="!categoryId" class="all-categories">
        <h1 class="page-title">全部分类</h1>
        <category-list :categories="categories" :totalCategories="categories.length" />
      </div>

      <!-- 具体分类的书籍列表 -->
      <div v-else class="category-books">
        <h1 class="page-title">{{ categoryName }}</h1>
        
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="books.length > 0" class="book-grid">
          <book-card 
            v-for="book in books" 
            :key="book.id" 
            :book="book" 
          />
        </div>
        
        <div v-else class="empty-state">
          该分类下暂无书籍
        </div>

        <div class="pager" v-if="total > pageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { searchApi } from '@/api/search'
import { bookApi } from '@/api/book'
import { normalizeBook, isSuccessResponse } from '@/utils/api-helper'
import { getCategoryDefaultCover } from '@/utils/cover-helper'
import CategoryList from '@/components/category/CategoryList.vue'
import BookCard from '@/components/book/BookCard.vue'

export default {
  name: 'CategoriesView',
  components: {
    CategoryList,
    BookCard
  },
  setup() {
    const route = useRoute()
    const loading = ref(false)
    const categories = ref([])
    const books = ref([])
    const categoryName = ref('分类详情')
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    
    const categoryId = computed(() => route.params.id)

    // 获取所有分类
    const fetchCategories = async () => {
      loading.value = true
      try {
        const res = await searchApi.getCategories()
        if (isSuccessResponse(res)) {
          const rawCategories = Array.isArray(res.data) ? res.data : []
          const processed = []
          const stack = [...rawCategories]
          
          while (stack.length) {
            const current = stack.pop()
            if (!current) continue
            
            const name = current.name ?? current.Name ?? current.name
            const id = current.categoryID ?? current.Category_ID ?? current.id
            
            if (name && id) {
              const specialCover = "https://cdn.weread.qq.com/weread/cover/21/cpplatform_m8zhpfmfxbdummla1qx3cr1768480023.jpg";
              processed.push({
                id: id,
                name: name,
                bookCount: current.bookCount || current.BookCount || 0,
                coverUrl: name === "文学" ? specialCover : getCategoryDefaultCover(id)
              })
            }
            
            const children = current.children || current.Children
            if (Array.isArray(children)) {
              stack.push(...children)
            }
          }
          categories.value = processed
          
          // 如果当前有categoryId，尝试找到对应的名字
          if (categoryId.value) {
            const currentCat = processed.find(c => c.id == categoryId.value)
            if (currentCat) {
              categoryName.value = currentCat.name
            }
          }
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error)
      } finally {
        loading.value = false
      }
    }

    // 获取分类下的书籍
    const fetchCategoryBooks = async (id) => {
      if (!id) return
      loading.value = true
      try {
        // 使用 bookApi.getBooksByCategory
        const res = await bookApi.getBooksByCategory(id, { 
          page: Math.max(0, currentPage.value - 1),
          size: pageSize.value
        })
        
        if (isSuccessResponse(res)) {
          const list = res.data?.content || res.data || []
          books.value = list.map(normalizeBook)
          total.value = res.data?.totalElements ?? 0
          pageSize.value = res.data?.size ?? pageSize.value
          // currentPage 从响应取值（后端可能返回1基），否则保持现状
          if (typeof res.data?.currentPage === 'number' && res.data.currentPage > 0) {
            currentPage.value = res.data.currentPage
          }
        }
      } catch (error) {
        console.error('Failed to fetch books:', error)
      } finally {
        loading.value = false
      }
    }
    
    const handlePageChange = (p) => {
      currentPage.value = p
      fetchCategoryBooks(categoryId.value)
    }

    // 初始化逻辑
    const init = () => {
      if (categoryId.value) {
        fetchCategories() // 获取分类列表以查找名字
        currentPage.value = 1
        fetchCategoryBooks(categoryId.value)
      } else {
        fetchCategories()
      }
    }

    onMounted(init)

    // 监听路由变化
    watch(() => route.params.id, (newId) => {
      if (newId) {
        currentPage.value = 1
        fetchCategoryBooks(newId)
        // 更新名字
        const currentCat = categories.value.find(c => c.id == newId)
        if (currentCat) {
          categoryName.value = currentCat.name
        }
      } else {
        books.value = []
        if (categories.value.length === 0) {
            fetchCategories()
        }
      }
    })

    return {
      loading,
      categories,
      books,
      categoryId,
      categoryName,
      currentPage,
      pageSize,
      total,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.categories-view {
  padding: 20px 0;
  min-height: 60vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  margin-bottom: 24px;
  color: #606266;
  font-size: 14px;
}

.breadcrumb a {
  color: #606266;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: #409EFF;
}

.separator {
  margin: 0 8px;
  color: #C0C4CC;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 16px;
}
.pager {
  display: flex;
  justify-content: center;
  margin: 20px 0 40px;
}

@media screen and (max-width: 1200px) {
  .book-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media screen and (max-width: 992px) {
  .book-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
