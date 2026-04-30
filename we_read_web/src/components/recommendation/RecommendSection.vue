<template>
  <section class="section">
    <div class="section-title">
      <span>大家都在看 <small @click="$emit('refresh')">换一批</small></span>
    </div>
    <div class="book-list">
      <book-card v-for="book in books" :key="book.id" :book="book" />
    </div>
  </section>
</template>
  
<script>
import BookCard from "@/components/book/BookCard.vue";

export default {
  name: "RecommendSection",
  components: {
    BookCard,
  },
  props: {
    books: {
      type: Array,
      required: true,
      default: () => [],
    },
  },
};
</script>
  
<style scoped>
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
}

.section-title small {
  font-size: 12px;
  color: #1989fa;
  margin-left: 10px;
  font-weight: normal;
  cursor: pointer;
}

.section-more {
  font-size: 14px;
  color: #1989fa;
  font-weight: normal;
}

.book-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

@media screen and (max-width: 768px) {
  .book-list {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style>