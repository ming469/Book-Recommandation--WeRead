<template>
  <div class="ranking-section">
    <div class="ranking-header">
      <div class="ranking-title">
        <span class="ranking-badge" :class="badgeClass">{{ badgeText }}</span>
        <span class="ranking-subtitle">{{ subtitle }}</span>
      </div>
      <router-link :to="moreLink" class="section-more">查看全部 ></router-link>
    </div>
    <div class="ranking-list">
      <div v-for="(book, index) in books" :key="book.id" class="ranking-item">
        <div class="ranking-number">{{ index + 1 }}</div>
        <book-card :book="book" :ranking="true" />
      </div>
    </div>
  </div>
</template>
  
  <script>
import BookCard from "@/components/book/BookCard.vue";

export default {
  name: "RankingList",
  components: {
    BookCard,
  },
  props: {
    books: {
      type: Array,
      required: true,
      default: () => [],
    },
    badgeText: {
      type: String,
      required: true,
    },
    badgeClass: {
      type: String,
      required: true,
    },
    subtitle: {
      type: String,
      required: true,
    },
    moreLink: {
      type: String,
      required: true,
    },
  },
};
</script>
  
  <style scoped>
.ranking-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.ranking-title {
  display: flex;
  flex-direction: column;
}

.ranking-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}

.bestseller {
  background-color: #ffecec;
  color: #ff5353;
}

.new-book {
  background-color: #ecf5ff;
  color: #1989fa;
}

.all-time {
  background-color: #f0f9eb;
  color: #67c23a;
}

.recommend {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.ranking-subtitle {
  font-size: 12px;
  color: #999;
}

.ranking-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.ranking-item {
  position: relative;
}

.ranking-number {
  position: absolute;
  top: 0;
  left: 0;
  width: 24px;
  height: 24px;
  background-color: #1989fa;
  color: white;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 0 4px 0;
  z-index: 1;
}

.ranking-item:nth-child(1) .ranking-number,
.ranking-item:nth-child(2) .ranking-number,
.ranking-item:nth-child(3) .ranking-number {
  background-color: #ff5353;
}

.section-more {
  font-size: 14px;
  color: #1989fa;
  font-weight: normal;
}

@media screen and (max-width: 768px) {
  .ranking-list {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style>