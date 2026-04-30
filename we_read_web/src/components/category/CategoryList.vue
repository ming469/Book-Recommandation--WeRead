<template>
  <section class="section">
    <div class="section-title">
      <span>分类</span>
      <router-link to="/categories" class="section-more"
        >查看全部 · {{ totalCategories }}个 ></router-link
      >
    </div>
    <div class="category-grid">
      <router-link
        v-for="category in categories"
        :key="category.id"
        :to="`/category/${category.id}`"
        class="category-card"
      >
        <div class="category-cover">
          <img
            :src="getCoverSrc(category)"
            :alt="category.name"
            class="cover-image"
            @error="handleCoverError($event, category.id)"
            referrerpolicy="no-referrer"
            loading="lazy"
          />
        </div>
        <div class="category-info">
          <div class="category-name">{{ category.name }}</div>
          <div class="book-count">{{ category.bookCount }}本书籍</div>
        </div>
      </router-link>
    </div>
  </section>
</template>

<script>
import { getCategoryDefaultCover, getCategoryCoverByName } from "@/utils/cover-helper";

export default {
  name: "CategoryList",
  props: {
    categories: {
      type: Array,
      required: true,
      default: () => [],
    },
    totalCategories: {
      type: Number,
      default: 22,
    },
  },
  methods: {
    getCoverSrc(category) {
      const byName = getCategoryCoverByName(category?.name);
      return byName || category?.coverUrl || getCategoryDefaultCover(category?.id);
    },
    handleCoverError(e, id) {
      const name = e?.target?.alt || "";
      const byName = getCategoryCoverByName(name);
      e.target.src = byName || getCategoryDefaultCover(id);
    },
  },
};

</script>

<style scoped>
.section {
  margin-bottom: 40px;
  padding: 0 16px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: bold;
}

.section-more {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  text-decoration: none;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.category-cover {
  width: 100%;
  height: 120px;
  margin-bottom: 12px;
  border-radius: 4px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.category-card:hover .cover-image {
  transform: scale(1.05);
}

.category-info {
  flex: 1;
}

.category-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 6px;
  color: #303133;
}

.book-count {
  font-size: 14px;
  color: #909399;
}

@media screen and (max-width: 1200px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 992px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 576px) {
  .category-grid {
    grid-template-columns: 1fr;
  }

  .category-cover {
    height: 100px;
  }
}
</style>
