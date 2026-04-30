<template>
  <router-link :to="`/book/${book.id}`" class="book-card" :class="{ 'book-card--ranking': ranking }">
    <!-- 封面区域 -->
    <div class="book-card__cover">
      <img :src="book.cover" :alt="book.title" @error="handleCoverError" referrerpolicy="no-referrer" loading="lazy" />
      <div class="book-card__overlay"></div>
    </div>

    <!-- 书籍信息区域 -->
    <div class="book-card__info">
      <h3 class="book-card__title">{{ book.title }}</h3>
      <p class="book-card__author">{{ book.author }}</p>
      <p class="book-card__publisher" v-if="book.publisher">
        {{ book.publisher }}
      </p>
    </div>
  </router-link>
</template>

<script>
import { getBookDefaultCover } from "@/utils/cover-helper";
/**
 * 组件名称：BookCard
 * 组件描述：用于展示单本书籍的信息，包括封面、书名、作者、出版社，并支持排行榜样式。
 *
 * @prop {Object} book - 书籍信息对象
 * @prop {String} book.cover - 书籍封面图片的 URL
 * @prop {String} book.title - 书籍标题
 * @prop {String} book.author - 书籍作者
 * @prop {String} [book.publisher] - 书籍出版社（可选）
 * @prop {Boolean} [ranking=false] - 是否启用排行榜样式
 */
export default {
  name: "BookCard",
  props: {
    book: {
      type: Object,
      required: true,
    },
    ranking: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    handleCoverError(event) {
      event.target.src = getBookDefaultCover(this.book.id);
    },
  },
};
</script>

<style scoped>
/* 
  导入 Google Fonts，提供更优雅的中文与英文字体支持 
*/
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&family=Noto+Serif+SC:wght@400;500;600;700&display=swap");

/* 
  书籍卡片基础样式 
  - 采用白色背景，增加立体感
  - 适用于常规书籍展示
*/
.book-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  font-family: "Noto Sans SC", sans-serif;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid rgba(240, 240, 240, 0.8);
  overflow: hidden;
  text-decoration: none; /* 去掉链接下划线 */
  color: inherit;        /* 继承父元素颜色 */
}

/* 
  书籍卡片
*/
.book-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08), 0 6px 6px rgba(0, 0, 0, 0.06);
}

/* 
  书籍封面区域 
  - 采用相对定位，确保封面图片可自适应填充
*/
.book-card__cover {
  position: relative;
  width: 100%;
  padding-top: 140%;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
}

/* 
  书籍封面图片 - 覆盖整个容器 
*/
.book-card__cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

/* 
  悬停时封面图片放大，增强动态感 
*/
.book-card:hover .book-card__cover img {
  transform: scale(1.05);
}

/* 
  书籍封面
*/
.book-card__overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 30%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-card:hover .book-card__overlay {
  opacity: 1;
}

/* 
  书籍信息区域 
  - 包括书名、作者、出版社信息
*/
.book-card__info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
  background: linear-gradient(to bottom, #ffffff, #f8f9fa);
}

/* 
  书名样式 
  - 采用衬线字体，提升阅读体验
  - 超长文本省略
*/
.book-card__title {
  font-family: "Noto Serif SC", "Times New Roman", serif;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
  color: #333;
  letter-spacing: 0.01em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s ease;
}

.book-card:hover .book-card__title {
  color: #1890ff;
}

/* 
  作者信息样式 
  - 保持单行显示，超出省略
*/
.book-card__author {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 
  出版社信息样式 
  - 字号较小，适用于辅助信息
*/
.book-card__publisher {
  font-size: 12px;
  color: #999;
  margin-top: auto;
  font-weight: 400;
}

/* 
  排行榜样式（不同于常规书籍卡片）
  - 采用横向布局
*/
.book-card--ranking {
  flex-direction: row;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
}

.book-card--ranking .book-card__cover {
  width: 70px;
  height: 90px;
  padding-top: 0;
  margin-right: 15px;
  border-radius: 5px;
  flex-shrink: 0;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
}

/* 
  排行榜书籍信息区域
  - 取消背景色，适应紧凑排版
*/
.book-card--ranking .book-card__info {
  padding: 0;
  justify-content: center;
  background: none;
}

/* 
  排行榜样式
*/
.book-card--ranking:hover {
  transform: translateY(-3px) scale(1.01);
}

/* 
  媒体查询（适配移动端）
*/
@media (max-width: 768px) {
  .book-card__title {
    font-size: 14px;
  }

  .book-card__author,
  .book-card__publisher {
    font-size: 12px;
  }

  .book-card--ranking .book-card__cover {
    width: 60px;
    height: 80px;
  }
}
</style>
