<template>
  <div class="home">
    <!-- 大家都在看 -->
    <recommend-section :books="popularBooks" v-loading="loading" @refresh="refreshPopularBooks" />

    <!-- 榜单 -->
    <section class="section">
      <div class="section-title">
        <span>榜单</span>
      </div>

      <!-- 推荐榜 (原好评榜) -->
      <ranking-list 
        :books="recommendedBooks" 
        badge-text="推荐榜" 
        badge-class="recommend"
        subtitle="个性化精选 猜你喜欢TOP作品"
        more-link="/ranking/recommended"
        v-loading="loading"
      />
      
      <!-- 畅销榜 -->
      <ranking-list 
        :books="bestsellerBooks" 
        badge-text="TOP 50 / 畅销榜" 
        badge-class="bestseller"
        subtitle="畅销书排行TOP50，每日更新"
        more-link="/ranking/bestseller"
        v-loading="loading"
      />
      
      <!-- 新书榜 -->
      <ranking-list 
        :books="newBooks" 
        badge-text="TOP 50 / 新书榜" 
        badge-class="new-book"
        subtitle="本周新书推荐TOP50，每周更新"
        more-link="/ranking/new"
        v-loading="loading"
      />

      <!-- 总榜 -->
      <ranking-list 
        :books="allTimeBooks" 
        badge-text="TOP 50 / 总榜" 
        badge-class="all-time"
        subtitle="总榜TOP50，看看什么书最值得读"
        more-link="/ranking/all-time"
        v-loading="loading"
      />
    </section>

    <!-- 分类 -->
    <category-list :categories="categories" :totalCategories="totalCategories" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { bookApi } from '@/api/book'
import { searchApi } from '@/api/search'
import { normalizeBook, isSuccessResponse } from '@/utils/api-helper'
import { getCategoryDefaultCover } from '@/utils/cover-helper'
import RecommendSection from '@/components/recommendation/RecommendSection.vue'
import RankingList from '@/components/ranking/RankingList.vue'
import CategoryList from '@/components/category/CategoryList.vue'

export default {
  name: 'HomeView',
  components: {
    RecommendSection,
    RankingList,
    CategoryList
  },
  setup() {
    const loading = ref(false)
    const popularBooks = ref([])
    const bestsellerBooks = ref([])
    const newBooks = ref([])
    const allTimeBooks = ref([])
    const recommendedBooks = ref([])
    const categories = ref([])
    const totalCategories = ref(22)

    const getListFromResponse = (res) => {
      if (!res) return []
      const data = res.data
      if (Array.isArray(data)) return data
      if (data && Array.isArray(data.list)) return data.list
      if (data && Array.isArray(data.content)) return data.content
      return []
    }

    const adaptBookData = (book) => {
      const normalized = normalizeBook(book)
      return {
        ...normalized,
        popularity: normalized.rating ? `评分 ${normalized.rating}` : '暂无评分'
      }
    }

    const fetchAllData = async () => {
      loading.value = true
      try {
        // 并行请求所有数据
        const [
          popularRes, 
          latestRes, 
          bestsellerRes,
          allTimeRes,
          recRes,
          catRes
        ] = await Promise.all([
          bookApi.getPopularBooks({ size: 4 }), // 大家都在看
          bookApi.getLatestBooks({ size: 5 }),      // 新书榜
          bookApi.getMostCollectedBooks({ size: 5 }), // 畅销榜 (按收藏量)
          bookApi.getPopularBooks({ size: 5, page: 1 }), // 总榜 (按浏览量，取第2页以区分内容)
          bookApi.getRecommendedBooks({ size: 5 }), // 好评榜
          searchApi.getCategories()
        ])

        // 处理大家都在看
        if (isSuccessResponse(popularRes)) {
          const list = getListFromResponse(popularRes)
          popularBooks.value = list.map(adaptBookData)
        }

        // 处理新书榜
        if (isSuccessResponse(latestRes)) {
          const list = getListFromResponse(latestRes)
          newBooks.value = list.map(adaptBookData)
        }

        // 处理畅销榜
        if (isSuccessResponse(bestsellerRes)) {
          bestsellerBooks.value = getListFromResponse(bestsellerRes).map(adaptBookData)
        }

        // 处理总榜
        if (isSuccessResponse(allTimeRes)) {
          allTimeBooks.value = getListFromResponse(allTimeRes).map(adaptBookData)
        }

        // 处理好评榜
        if (isSuccessResponse(recRes)) {
          recommendedBooks.value = getListFromResponse(recRes).map(adaptBookData)
        }

        // 处理分类
        if (isSuccessResponse(catRes)) {
          const rawCategories = Array.isArray(catRes.data) ? catRes.data : []
          categories.value = rawCategories.map(cat => {
            const catId = cat.Category_ID || cat.id;
            const name = cat.Name || cat.name;
            const specialCover = "https://img1.doubanio.com/view/subject/l/public/s35402390.jpg";
            return {
              id: catId,
              name,
              bookCount: cat.bookCount || 0,
              coverUrl: name === "文学" ? specialCover : getCategoryDefaultCover(catId)
            }
          })
          totalCategories.value = categories.value.length
        }

      } catch (error) {
        console.error('Failed to fetch home data:', error)
      } finally {
        loading.value = false
      }
    }

    const refreshPopularBooks = async () => {
      try {
        // 随机获取一页热门书籍来模拟“换一批”
        const randomPage = Math.floor(Math.random() * 5) + 1
        const res = await bookApi.getPopularBooks({ size: 4, page: randomPage })
        if (isSuccessResponse(res)) {
          const list = getListFromResponse(res)
          if (list.length > 0) {
            popularBooks.value = list.map(adaptBookData)
          }
        }
      } catch (error) {
        console.error('Failed to refresh popular books:', error)
      }
    }

    onMounted(() => {
      fetchAllData()
    })

    return {
      loading,
      popularBooks,
      bestsellerBooks,
      newBooks,
      allTimeBooks,
      recommendedBooks,
      categories,
      totalCategories,
      refreshPopularBooks
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: bold;
  padding: 0 16px;
}
</style>
