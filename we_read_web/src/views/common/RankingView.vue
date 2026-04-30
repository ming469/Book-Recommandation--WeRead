<template>
  <div class="ranking-view">
    <div class="container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="separator">/</span>
        <span>{{ pageTitle }}</span>
      </div>

      <div class="ranking-header">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <p class="page-subtitle">{{ pageSubtitle }}</p>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="books.length > 0" class="book-list">
        <div v-for="(book, index) in books" :key="book.id" class="ranking-item">
          <div class="ranking-index" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</div>
          <book-card :book="book" :ranking="true" />
        </div>
      </div>
      
      <div v-else class="empty-state">
        暂无书籍数据
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { bookApi } from '@/api/book'
import { normalizeBook, isSuccessResponse } from '@/utils/api-helper'
import BookCard from '@/components/book/BookCard.vue'

export default {
  name: 'RankingView',
  components: {
    BookCard
  },
  setup() {
    const route = useRoute()
    const loading = ref(false)
    const books = ref([])
    
    const rankingType = computed(() => route.params.type)
    
    const pageTitle = computed(() => {
      const map = {
        'bestseller': '畅销榜 TOP 50',
        'new': '新书榜 TOP 50',
        'all-time': '总榜 TOP 50',
        'recommended': '好评推荐榜'
      }
      return map[rankingType.value] || '图书榜单'
    })

    const pageSubtitle = computed(() => {
      const map = {
        'bestseller': '每日更新 · 畅销好书推荐',
        'new': '每周更新 · 发现最新上架好书',
        'all-time': '综合评选 · 最值得阅读的经典之作',
        'recommended': '读者精选 · 收获最多好评的佳作'
      }
      return map[rankingType.value] || ''
    })

    const fetchBooks = async () => {
      loading.value = true
      books.value = []
      try {
        let res
        const type = rankingType.value
        const params = { page: 0, size: 50 } // 榜单通常显示较多

        if (type === 'new') {
          res = await bookApi.getLatestBooks(params)
        } else if (type === 'recommended') {
          res = await bookApi.getRecommendedBooks(params)
        } else if (type === 'bestseller') {
          // 畅销榜 -> 收藏榜
          res = await bookApi.getMostCollectedBooks(params)
        } else if (type === 'all-time') {
          // 总榜 -> 热门榜
          res = await bookApi.getPopularBooks(params)
        } else {
          // 默认
          res = await bookApi.getBooks(params)
        }

        if (isSuccessResponse(res)) {
          const list = res.data.content || res.data || []
          books.value = list.map(normalizeBook)
        }
      } catch (error) {
        console.error('Failed to fetch ranking books:', error)
      } finally {
        loading.value = false
      }
    }

    onMounted(fetchBooks)

    watch(() => route.params.type, (newType) => {
      if (newType) {
        fetchBooks()
      }
    })

    return {
      loading,
      books,
      pageTitle,
      pageSubtitle
    }
  }
}
</script>

<style scoped>
.ranking-view {
  padding: 20px 0;
  min-height: 60vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1000px;
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

.separator {
  margin: 0 8px;
  color: #C0C4CC;
}

.ranking-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-subtitle {
  color: #909399;
  font-size: 14px;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: flex-start;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.ranking-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.ranking-index {
  font-size: 24px;
  font-weight: bold;
  color: #909399;
  margin-right: 20px;
  min-width: 40px;
  text-align: center;
  font-family: Impact, sans-serif;
  line-height: 1.5;
}

.ranking-index.top-three {
  color: #ff4d4f;
}

/* 复用 BookCard 样式但做一些调整以适应列表布局 */
:deep(.book-card) {
  display: flex;
  flex: 1;
  border: none;
  box-shadow: none;
  height: auto;
}

:deep(.book-card__cover) {
  width: 100px;
  height: 140px;
  flex-shrink: 0;
  margin-right: 20px;
}

:deep(.book-card__info) {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 0;
}

:deep(.book-card__title) {
  font-size: 18px;
  margin-bottom: 8px;
}

:deep(.book-card__author) {
  font-size: 14px;
  margin-bottom: 8px;
}

:deep(.book-card__publisher) {
  font-size: 13px;
  color: #909399;
}

.loading, .empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 16px;
}
</style>
