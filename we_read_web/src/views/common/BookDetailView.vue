<template>
  <div class="book-detail" v-loading="loading">
    <div v-if="book" class="book-detail__content">
      <!-- 顶部信息区 -->
      <div class="book-header">
        <div class="book-cover">
          <img :src="displayCover" :alt="book.title" @error="handleCoverError" referrerpolicy="no-referrer" />
        </div>
        <div class="book-info">
          <h1 class="book-title">{{ book.title }}</h1>
          <div class="book-meta">
            <p><strong>作者：</strong>{{ book.author }}</p>
            <p><strong>出版社：</strong>{{ book.publisher }}</p>
            <p><strong>出版时间：</strong>{{ book.publishDate }}</p>
            <p><strong>ISBN：</strong>{{ book.isbn }}</p>
            <p><strong>分类：</strong><span class="category-link" @click="handleCategoryClick">{{ book.category }}</span></p>
          </div>
          <div class="book-rating">
            <el-rate
              :model-value="book.rating > 5 ? book.rating / 2 : book.rating"
              disabled
              allow-half
              :max="5"
              text-color="#ff9900"
              :void-icon="Star"
              :icon="StarFilled"
            >
            </el-rate>
            <span class="rating-score">{{ book.rating }}</span>
            <span class="review-count">({{ book.reviewCount }}人评价)</span>
          </div>
          <div class="user-rating">
            <div class="user-rating__label">我的评分</div>
            <el-rate
              v-model="userRating"
              allow-half
              :max="5"
              :void-icon="Star"
              :icon="StarFilled"
              @change="submitUserRating"
            />
            <span v-if="userRating > 0" class="user-rating__value">{{ userRating }}</span>
          </div>
          <div class="book-actions">
            <el-button type="primary" size="large" @click="goRead">立即阅读</el-button>
            <el-button size="large" @click="addToShelf">加入书架</el-button>
            <el-button size="large" :type="isFavorited ? 'danger' : 'success'" @click="toggleFavorite">
              {{ isFavorited ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 内容简介 -->
      <section class="section">
        <h2 class="section-title">内容简介</h2>
        <div class="book-description">
          <p>{{ displayDescription }}</p>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">发表评论</h2>
        <div class="comment-box">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="4"
            placeholder="写下你的看法…"
          />
          <div class="comment-actions">
            <el-button type="primary" @click="sendComment">发送</el-button>
          </div>
        </div>
        <div class="comment-list" v-loading="commentsLoading">
          <div v-if="comments.length === 0" class="comment-empty">暂无评论</div>
          <div v-else class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-meta">
              <span class="comment-user">用户 {{ c.userId }}</span>
              <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
        </div>
      </section>

      <section class="section" v-if="categoryBooks.length">
        <h2 class="section-title">同类图书</h2>
        <div class="category-books" v-loading="categoryLoading">
          <div
            v-for="item in categoryBooks"
            :key="item.id"
            class="category-book-card"
            @click="router.push(`/book/${item.id}`)"
          >
            <BookCard :book="item" />
          </div>
        </div>
      </section>

      <!-- 相似推荐 (ItemCF算法展示) -->
      <section class="section" v-if="book.similarBooks && book.similarBooks.length">
        <h2 class="section-title">喜欢这本书的人也喜欢 (ItemCF推荐)</h2>
        <div class="similar-books">
          <div 
            v-for="similar in book.similarBooks" 
            :key="similar.id" 
            class="similar-book-card"
            @click="router.push(`/book/${similar.id}`)"
          >
            <div class="similar-cover">
              <img :src="similar.cover" :alt="similar.title" referrerpolicy="no-referrer" loading="lazy">
              <div class="similarity-badge">相似度 {{ (similar.similarity * 100).toFixed(0) }}%</div>
            </div>
            <p class="similar-title">{{ similar.title }}</p>
          </div>
        </div>
      </section>
    </div>
    
    <!-- 加载失败或未找到 -->
    <div v-else-if="!loading" class="not-found">
      <el-empty description="未找到该图书信息" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import { userApi } from '@/api/user'
import { normalizeBook, isSuccessResponse } from '@/utils/api-helper'
import { getBookDefaultCover } from '@/utils/cover-helper'
import BookCard from '@/components/book/BookCard.vue'

export default {
  name: 'BookDetailView',
  components: {
    BookCard
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const book = ref(null)
    const loading = ref(false)
    const coverError = ref(false)
    const categoryBooks = ref([])
    const categoryLoading = ref(false)
    const currentCategory = ref('')
    const isFavorited = ref(false)
    const userRating = ref(0)
    const commentText = ref('')
    const comments = ref([])
    const commentsLoading = ref(false)

    const displayCover = computed(() => {
      if (!book.value) return getBookDefaultCover()
      return getBookDefaultCover(book.value.id)
    })

    const handleCoverError = (event) => {
      event.target.src = getBookDefaultCover(book.value?.id)
    }

    const displayDescription = computed(() => {
      return book.value?.description || '暂无简介'
    })

    const fetchCategoryBooks = async (category) => {
      if (!category || category === currentCategory.value) {
        return
      }
      categoryLoading.value = true
      currentCategory.value = category
      try {
        const res = await bookApi.getBooksByCategory(category, { page: 0, size: 8 })
        if (isSuccessResponse(res)) {
          const content = res.data?.content || []
          categoryBooks.value = content
            .map((item) => normalizeBook(item))
            .filter((item) => item.id !== book.value?.id)
        } else {
          categoryBooks.value = []
        }
      } catch (error) {
        categoryBooks.value = []
      } finally {
        categoryLoading.value = false
      }
    }

    const handleCategoryClick = () => {
      fetchCategoryBooks(book.value?.category)
    }

    const fetchBookDetail = async (id) => {
      if (!id) return
      loading.value = true
      try {
        const res = await bookApi.getBookById(id)
        if (isSuccessResponse(res)) {
          book.value = normalizeBook(res.data)
          currentCategory.value = ''
          fetchCategoryBooks(book.value.category)
          try {
            const fav = await userApi.checkFavorite(book.value.id)
            if (fav?.meta?.code === 200) {
              isFavorited.value = !!fav.data?.isFavorite
            }
          } catch (e) {
            isFavorited.value = false
          }
          try {
            const my = await userApi.getMyRate(book.value.id)
            if (my?.meta?.code === 200 && my.data && my.data.rating) {
              userRating.value = Number(my.data.rating)
            } else {
              userRating.value = 0
            }
          } catch (e) {
            userRating.value = 0
          }
        }
      } catch (error) {
        console.error('获取图书详情失败:', error)
      } finally {
        loading.value = false
      }
    }

    const toggleFavorite = async () => {
      if (!book.value) return
      try {
        if (isFavorited.value) {
          await userApi.removeFavorite(book.value.id)
          isFavorited.value = false
        } else {
          await userApi.addFavorite({ bookId: book.value.id })
          isFavorited.value = true
        }
      } catch (e) {}
    }

    const addToShelf = async () => {
      if (!book.value) return
      try {
        await userApi.addToShelf({ bookId: book.value.id, categoryId: book.value.categoryId })
        ElMessage.success('已加入书架，可在“我的书架”查看')
      } catch (e) {}
    }
    
    const goRead = () => {
      if (!book.value) return
      router.push(`/book/${book.value.id}/read`)
    }

    const submitUserRating = async (val) => {
      if (!book.value || !val) return
      try {
        const data = { bookId: book.value.id, categoryId: book.value.categoryId, rate: val }
        const res = await userApi.rateBook(data)
        if (isSuccessResponse(res)) {
          ElMessage.success('评分已提交')
        }
      } catch (e) {}
    }

    const sendComment = async () => {
      if (!commentText.value || !commentText.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      try {
        const data = {
          bookId: book.value.id,
          categoryId: book.value.categoryId,
          title: `评论`,
          content: commentText.value.trim()
        }
        const res = await userApi.addComment(data)
        if (isSuccessResponse(res)) {
          ElMessage.success('评论已发送')
          commentText.value = ''
          loadComments()
        }
      } catch (e) {}
    }
    
    const loadComments = async () => {
      if (!book.value) return
      commentsLoading.value = true
      try {
        const res = await bookApi.getBookComments(book.value.id, { page: 1, size: 10 })
        if (isSuccessResponse(res)) {
          comments.value = res.data?.list || []
        } else {
          comments.value = []
        }
      } catch (e) {
        comments.value = []
      } finally {
        commentsLoading.value = false
      }
    }

    onMounted(() => {
      fetchBookDetail(route.params.id)
    })

    watch(() => route.params.id, (newId) => {
      fetchBookDetail(newId)
      loadComments()
    })

    watch(book, () => {
      coverError.value = false
    })

    // 监听路由参数变化（例如点击相似图书时）
    watch(() => route.params.id, (newId) => {
      fetchBookDetail(newId)
    })

    return {
      book,
      loading,
      router,
      displayCover,
      handleCoverError,
      displayDescription,
      categoryBooks,
      categoryLoading,
      handleCategoryClick,
      Star,
      StarFilled,
      isFavorited,
      toggleFavorite,
      addToShelf,
      goRead,
      userRating,
      submitUserRating,
      commentText,
      sendComment,
      comments,
      commentsLoading,
      loadComments,
      formatTime: (iso) => (iso ? new Date(iso).toLocaleString() : '')
    }
  }
}
</script>

<style scoped>
.book-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.book-header {
  display: flex;
  gap: 40px;
  margin-bottom: 60px;
}

.book-cover {
  width: 260px;
  flex-shrink: 0;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-radius: 8px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #333;
}

.book-meta {
  color: #666;
  line-height: 1.8;
  margin-bottom: 20px;
}

.category-link {
  color: #409eff;
  cursor: pointer;
}

.category-link:hover {
  text-decoration: underline;
}

.book-rating {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.comment-box {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 12px;
}
.comment-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.comment-list{margin-top:12px}
.comment-empty{color:#909399}
.comment-item{padding:10px 0;border-top:1px solid #ebeef5}
.comment-item:first-child{border-top:none}
.comment-meta{display:flex;justify-content:space-between;color:#909399;font-size:12px;margin-bottom:4px}
.comment-content{white-space:pre-wrap;line-height:1.7}

.rating-score {
  margin-left: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #ff9900;
}

:deep(.el-rate__icon) {
  font-size: 20px;
  margin-right: 4px;
}

.review-count {
  margin-left: 10px;
  color: #999;
  font-size: 14px;
}

.book-actions {
  display: flex;
  gap: 20px;
}

.section {
  margin-bottom: 50px;
}

.section-title {
  font-size: 20px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
  margin-bottom: 24px;
}

.book-description {
  line-height: 1.8;
  color: #444;
  font-size: 16px;
  text-align: justify;
}

.book-catalog {
  line-height: 1.9;
  color: #555;
  font-size: 15px;
}

.category-books {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.category-book-card {
  cursor: pointer;
}

.similar-books {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 24px;
}

.similar-book-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.similar-book-card:hover {
  transform: translateY(-5px);
}

.similar-cover {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.similar-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.similarity-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 12px;
  text-align: center;
  padding: 4px 0;
}

.similar-title {
  font-size: 14px;
  color: #333;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
  
