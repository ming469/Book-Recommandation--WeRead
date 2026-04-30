<template>
  <div class="not-found-container">
    <div class="stars-container">
      <div
        v-for="n in 20"
        :key="n"
        class="star"
        :style="getRandomStarStyle()"
      ></div>
    </div>

    <div class="not-found-content">
      <div class="error-illustration">
        <div class="animation-container" v-if="hasImage">
          <div class="floating-elements">
            <div class="floating-book book-1"></div>
            <div class="floating-book book-2"></div>
            <div class="floating-book book-3"></div>
          </div>

          <img
            src="@/assets/images/404.svg"
            alt="页面未找到"
            class="error-image"
            ref="errorImage"
          />

          <div class="paper-plane">
            <div class="plane-icon"></div>
            <div class="plane-trail"></div>
          </div>

          <div class="magnifying-glass">
            <div class="glass"></div>
            <div class="handle"></div>
          </div>
        </div>

        <div class="error-code-container" v-else>
          <div class="error-digit">4</div>
          <div class="error-digit">0</div>
          <div class="error-digit">4</div>
        </div>
      </div>

      <h1 class="error-title" ref="errorTitle">{{ title }}</h1>
      <p class="error-message" ref="errorMessage">{{ message }}</p>

      <div class="action-buttons">
        <el-button type="primary" @click="goBack" class="action-btn back-btn">
          <el-icon><ArrowLeft /></el-icon>返回上一页
        </el-button>
        <el-button @click="goHome" class="action-btn home-btn">
          <el-icon><House /></el-icon>回到首页
        </el-button>
        <el-button @click="reportIssue" class="action-btn report-btn">
          <el-icon><Warning /></el-icon>报告问题
        </el-button>
      </div>

      <div class="suggestions" ref="suggestions">
        <h3>您可能想要尝试：</h3>
        <ul>
          <li class="suggestion-item">
            <el-icon><Check /></el-icon>检查输入的网址是否正确
          </li>
          <li class="suggestion-item">
            <el-icon><Back /></el-icon>返回
            <router-link to="/" class="link-text">首页</router-link>重新浏览
          </li>
          <li class="suggestion-item">
            <el-icon><Search /></el-icon>使用
            <router-link to="/search" class="link-text">搜索</router-link>
            查找您需要的内容
          </li>
          <li class="suggestion-item">
            <el-icon><Menu /></el-icon>查看
            <router-link to="/categories" class="link-text"
              >全部分类</router-link
            >
            浏览相关书籍
          </li>
        </ul>
      </div>

      <div class="related-books" ref="relatedBooks">
        <h3>您可能感兴趣的推荐</h3>
        <div class="book-recommendations">
          <div
            class="book-card"
            v-for="(book, index) in recommendedBooks"
            :key="index"
            :style="{ animationDelay: `${index * 0.15}s` }"
          >
            <div class="book-cover">
              <el-image :src="book.cover" fit="cover" lazy>
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="shine-effect"></div>
            </div>
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">{{ book.author }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * @file NotFoundView.vue
 * @description 404页面未找到组件
 * @created 2025-03-09
 * @updated 2025-03-09
 * @module views/NotFoundView
 */
import { ref, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getBookDefaultCover } from "@/utils/cover-helper";
import {
  ArrowLeft,
  House,
  Warning,
  Check,
  Back,
  Search,
  Menu,
  Picture,
} from "@element-plus/icons-vue";

export default {
  name: "NotFoundView",
  components: {
    ArrowLeft,
    House,
    Warning,
    Check,
    Back,
    Search,
    Menu,
    Picture,
  },
  setup() {
    const router = useRouter();
    const hasImage = ref(true);
    const title = ref("页面未找到");
    const message = ref("抱歉，您要访问的页面不存在或已被移除");

    // DOM引用
    const errorImage = ref(null);
    const errorTitle = ref(null);
    const errorMessage = ref(null);
    const suggestions = ref(null);
    const relatedBooks = ref(null);

    // 模拟推荐的书籍数据
    const recommendedBooks = reactive([
      {
        id: 1,
        title: "人类简史",
        author: "尤瓦尔·赫拉利",
        cover: getBookDefaultCover(1),
      },
      {
        id: 2,
        title: "解忧杂货店",
        author: "东野圭吾",
        cover: getBookDefaultCover(2),
      },
      {
        id: 3,
        title: "活着",
        author: "余华",
        cover: getBookDefaultCover(3),
      },
      {
        id: 4,
        title: "百年孤独",
        author: "加西亚·马尔克斯",
        cover: getBookDefaultCover(4),
      },
    ]);

    /**
     * 返回上一页
     * 如果没有历史记录则返回首页
     */
    const goBack = () => {
      if (window.history.length > 1) {
        router.go(-1);
      } else {
        goHome();
      }
    };

    /**
     * 跳转到首页
     */
    const goHome = () => {
      router.push({ name: "home" });
    };

    /**
     * 报告问题功能
     */
    const reportIssue = () => {
      ElMessage({
        message: "感谢您的反馈，我们会尽快处理",
        type: "success",
        duration: 3000,
        showClose: true,
      });

      // 点击报告问题按钮时添加额外动画效果
      const plane = document.querySelector(".paper-plane");
      if (plane) {
        plane.classList.add("report-flight");
        setTimeout(() => {
          plane.classList.remove("report-flight");
        }, 3000);
      }
    };

    /**
     * 为星星生成随机样式
     */
    const getRandomStarStyle = () => {
      const size = Math.random() * 4 + 1; // 1-5px
      const opacity = Math.random() * 0.7 + 0.3; // 0.3-1
      const left = Math.random() * 100; // 0-100%
      const top = Math.random() * 100; // 0-100%
      const animationDuration = Math.random() * 3 + 2; // 2-5s
      const animationDelay = Math.random() * 2; // 0-2s

      return {
        width: `${size}px`,
        height: `${size}px`,
        opacity: opacity,
        left: `${left}%`,
        top: `${top}%`,
        animationDuration: `${animationDuration}s`,
        animationDelay: `${animationDelay}s`,
      };
    };

    /**
     * 初始化并监控页面滚动以添加交互动画
     */
    const initScrollAnimations = () => {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              entry.target.classList.add("animate-in");
            }
          });
        },
        { threshold: 0.1 }
      );

      // 观察元素
      if (suggestions.value) observer.observe(suggestions.value);
      if (relatedBooks.value) observer.observe(relatedBooks.value);
    };

    /**
     * 初始化纸飞机动画
     */
    const animatePaperPlane = () => {
      const plane = document.querySelector(".paper-plane");
      const trail = document.querySelector(".plane-trail");

      if (plane && trail) {
        // 添加更复杂的飞行路径动画
        setTimeout(() => {
          plane.classList.add("flying");
        }, 500);
      }
    };

    /**
     * 初始化放大镜动画
     */
    const animateMagnifyingGlass = () => {
      const glass = document.querySelector(".magnifying-glass");
      if (glass) {
        setTimeout(() => {
          glass.classList.add("searching");
        }, 1000);
      }
    };

    /**
     * 初始化404数字动画，当没有SVG图片时使用
     */
    const animateErrorDigits = () => {
      if (!hasImage.value) {
        const digits = document.querySelectorAll(".error-digit");
        digits.forEach((digit, index) => {
          setTimeout(() => {
            digit.classList.add("animate");
          }, index * 200);
        });
      }
    };

    onMounted(() => {
      // 检查404图片是否存在
      try {
        const img = new Image();
        img.src = require("@/assets/images/404.svg");
        img.onerror = () => {
          hasImage.value = false;
        };
      } catch (error) {
        hasImage.value = false;
        console.warn("404图片资源加载失败，使用文字代替", error);
      }

      // 设置页面标题
      document.title = "页面未找到 - 微读";

      // 初始化所有动画
      setTimeout(() => {
        animatePaperPlane();
        animateMagnifyingGlass();
        animateErrorDigits();
        initScrollAnimations();

        // 为书籍推荐卡片添加依次出现的动画
        const bookCards = document.querySelectorAll(".book-card");
        bookCards.forEach((card, index) => {
          setTimeout(() => {
            card.classList.add("visible");
          }, 500 + index * 150);
        });
      }, 300);
    });

    return {
      hasImage,
      title,
      message,
      goBack,
      goHome,
      reportIssue,
      recommendedBooks,
      getRandomStarStyle,
      errorImage,
      errorTitle,
      errorMessage,
      suggestions,
      relatedBooks,
    };
  },
};
</script>

<style lang="scss" scoped>
// 背景与容器样式
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 20px;
  background-color: var(--el-bg-color);
  background-image: linear-gradient(to bottom, #f7f9fc 0%, #ffffff 100%);
  position: relative;
  overflow: hidden;
}

// 星星背景效果
.stars-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.star {
  position: absolute;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 0 10px 0 rgba(255, 255, 255, 0.7);
  animation: twinkle 5s infinite ease-in-out;
}

@keyframes twinkle {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

// 主要内容区样式
.not-found-content {
  max-width: 900px;
  width: 100%;
  text-align: center;
  padding: 40px 30px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  z-index: 1;
  animation: fadeIn 0.8s ease-out forwards;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 5px;
    background: linear-gradient(
      90deg,
      var(--el-color-primary),
      var(--el-color-success)
    );
  }

  @media (max-width: 768px) {
    padding: 30px 20px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 错误图示区域
.error-illustration {
  margin-bottom: 40px;
  position: relative;
  height: 240px;

  @media (max-width: 768px) {
    height: 180px;
  }
}

.animation-container {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 浮动元素容器
.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

// 浮动书籍样式
.floating-book {
  position: absolute;
  width: 40px;
  height: 55px;
  border-radius: 3px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  background-size: cover;
  z-index: 2;
}

.book-1 {
  top: 20%;
  left: 20%;
  background-color: var(--el-color-primary-light-9);
  transform: rotate(-15deg);
  animation: floatingBook 7s infinite ease-in-out;
}

.book-2 {
  top: 30%;
  right: 25%;
  background-color: var(--el-color-success-light-8);
  transform: rotate(10deg);
  animation: floatingBook 8s infinite ease-in-out 1s;
}

.book-3 {
  bottom: 25%;
  left: 30%;
  background-color: var(--el-color-warning-light-8);
  transform: rotate(5deg);
  animation: floatingBook 6s infinite ease-in-out 0.5s;
}

@keyframes floatingBook {
  0%,
  100% {
    transform: translateY(0) rotate(var(--rotation, 0deg));
  }
  50% {
    transform: translateY(-15px) rotate(var(--rotation, 0deg));
  }
}

// 404图片样式
.error-image {
  max-width: 320px;
  height: auto;
  transform-origin: center;
  animation: float 6s ease-in-out infinite;
  position: relative;
  z-index: 3;
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.12));

  @media (max-width: 768px) {
    max-width: 200px;
  }
}

// 纸飞机及其动画
.paper-plane {
  position: absolute;
  top: 40%;
  left: -50px;
  width: 30px;
  height: 30px;
  z-index: 4;
  opacity: 0;

  &.flying {
    animation: complexFly 15s infinite cubic-bezier(0.45, 0.05, 0.55, 0.95);
    opacity: 1;
  }

  &.report-flight {
    animation: reportFlight 3s forwards;
  }
}

.plane-icon {
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%230d6efd'%3E%3Cpath d='M21.71,3.29a1,1,0,0,0-1.11-.22l-18,7a1,1,0,0,0,0,1.86l6.76,2.92,2.92,6.76a1,1,0,0,0,.93.63,1,1,0,0,0,.93-.63l7-18A1,1,0,0,0,21.71,3.29Z'/%3E%3C/svg%3E");
  background-size: contain;
  transform: rotate(-30deg);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.plane-trail {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(
    to left,
    rgba(13, 110, 253, 0.5),
    rgba(13, 110, 253, 0)
  );
  transform-origin: left;
  transform: translateY(-50%);
}

@keyframes complexFly {
  0% {
    left: -50px;
    top: 40%;
    transform: rotate(-30deg) scale(0.8);
  }
  10% {
    left: 20%;
    top: 30%;
    transform: rotate(15deg) scale(1);
  }
  30% {
    left: 40%;
    top: 60%;
    transform: rotate(-10deg) scale(0.9);
  }
  50% {
    left: 60%;
    top: 30%;
    transform: rotate(5deg) scale(1.1);
  }
  70% {
    left: 75%;
    top: 50%;
    transform: rotate(-5deg) scale(1);
  }
  90% {
    left: 100%;
    top: 40%;
    transform: rotate(10deg) scale(0.9);
  }
  100% {
    left: 110%;
    top: 45%;
    transform: rotate(0deg) scale(0.8);
  }
}

@keyframes reportFlight {
  0% {
    transform: scale(1) rotate(0);
    opacity: 1;
  }
  20% {
    transform: scale(1.2) rotate(-10deg);
  }
  100% {
    transform: scale(0.1) rotate(720deg);
    opacity: 0;
    top: 100%;
    left: 50%;
  }
}

// 放大镜动画
.magnifying-glass {
  position: absolute;
  width: 40px;
  height: 40px;
  top: 60%;
  right: 20%;
  opacity: 0;
  transform: rotate(-20deg) scale(0.8);
  z-index: 4;
  filter: drop-shadow(0 3px 5px rgba(0, 0, 0, 0.2));

  &.searching {
    animation: searchAnimation 12s infinite ease-in-out 0.5s;
    opacity: 1;
  }
}

.glass {
  position: absolute;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  border: 3px solid var(--el-color-info-dark-2);
  top: 0;
  left: 0;

  &::after {
    content: "";
    position: absolute;
    top: 50%;
    left: 50%;
    width: 15px;
    height: 15px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 50%;
    transform: translate(-80%, -80%);
  }
}

.handle {
  position: absolute;
  width: 3px;
  height: 15px;
  background-color: var(--el-color-info-dark-2);
  bottom: 0;
  right: 5px;
  transform: rotate(45deg);
  transform-origin: top;
}

@keyframes searchAnimation {
  0%,
  100% {
    transform: rotate(-20deg) scale(0.8) translate(0, 0);
  }
  25% {
    transform: rotate(10deg) scale(1.1) translate(-30px, 10px);
  }
  50% {
    transform: rotate(-5deg) scale(0.9) translate(20px, -20px);
  }
  75% {
    transform: rotate(15deg) scale(1) translate(-10px, 15px);
  }
}

// 404数字动画（当无图片时显示）
.error-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  perspective: 800px;
}

.error-digit {
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  margin: 0 10px;
  opacity: 0;
  transform: rotateY(-90deg);

  &:nth-child(1) {
    color: var(--el-color-primary);
  }

  &:nth-child(2) {
    color: var(--el-color-danger);
  }

  &:nth-child(3) {
    color: var(--el-color-warning);
  }

  &.animate {
    animation: flipIn 1s forwards;
  }

  @media (max-width: 768px) {
    font-size: 80px;
  }
}

@keyframes flipIn {
  0% {
    opacity: 0;
    transform: rotateY(-90deg);
  }
  100% {
    opacity: 1;
    transform: rotateY(0);
  }
}

// 错误标题与消息
.error-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-color-info-dark-2);
  margin-bottom: 16px;
  letter-spacing: -0.5px;
  animation: slideUp 0.8s ease forwards 0.4s;
  opacity: 0;

  @media (max-width: 768px) {
    font-size: 26px;
  }
}

.error-message {
  font-size: 18px;
  color: var(--el-text-color-secondary);
  margin-bottom: 32px;
  line-height: 1.6;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  animation: slideUp 0.8s ease forwards 0.6s;
  opacity: 0;

  @media (max-width: 768px) {
    font-size: 16px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
  flex-wrap: wrap;

  @media (max-width: 576px) {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .action-btn {
    min-width: 140px;
    border-radius: 8px;
    font-weight: 500;
    padding: 12px 24px;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &:before {
      content: "";
      position: absolute;
      top: 50%;
      left: 50%;
      width: 0;
      height: 0;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      transform: translate(-50%, -50%);
      transition: width 0.6s ease, height 0.6s ease;
    }

    &:hover:before {
      width: 300px;
      height: 300px;
    }

    .el-icon {
      margin-right: 6px;
      font-size: 16px;
      position: relative;
      z-index: 2;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }

    &:active {
      transform: translateY(1px);
    }

    @media (max-width: 576px) {
      width: 100%;
      max-width: 240px;
    }
  }

  .back-btn {
    background: var(--el-color-primary);
    border-color: var(--el-color-primary);
    animation: fadeInButton 0.5s ease forwards 0.8s;
    opacity: 0;
  }

  .home-btn {
    color: var(--el-text-color-primary);
    background: var(--el-color-info-light-9);
    border-color: var(--el-color-info-light-5);
    animation: fadeInButton 0.5s ease forwards 1s;
    opacity: 0;
  }

  .report-btn {
    color: var(--el-color-info-dark-2);
    background: transparent;
    animation: fadeInButton 0.5s ease forwards 1.2s;
    opacity: 0;
  }
}

@keyframes fadeInButton {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 建议区域
.suggestions {
  text-align: left;
  background-color: var(--el-color-info-light-9);
  padding: 24px 30px;
  border-radius: 12px;
  margin-top: 30px;
  position: relative;
  transition: transform 0.6s ease, opacity 0.6s ease;
  opacity: 0;
  transform: translateY(30px);

  &.animate-in {
    opacity: 1;
    transform: translateY(0);
  }

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 4px;
    background: linear-gradient(
      to bottom,
      var(--el-color-primary-light-3),
      var(--el-color-primary-light-7)
    );
    border-radius: 4px 0 0 4px;
  }

  h3 {
    font-size: 18px;
    color: var(--el-text-color-primary);
    margin-bottom: 16px;
    font-weight: 600;
    position: relative;

    &::after {
      content: "";
      position: absolute;
      bottom: -8px;
      left: 0;
      width: 50px;
      height: 2px;
      background-color: var(--el-color-primary-light-5);
    }
  }

  ul {
    padding-left: 0;
    list-style: none;

    li.suggestion-item {
      margin-bottom: 14px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      transform: translateX(-20px);
      opacity: 0;
      animation: slideInSuggestion 0.5s forwards;

      @for $i from 1 through 4 {
        &:nth-child(#{$i}) {
          animation-delay: #{0.3 + $i * 0.15}s;
        }
      }

      .el-icon {
        margin-right: 10px;
        color: var(--el-color-success);
        font-size: 16px;
      }

      .link-text {
        color: var(--el-color-primary);
        text-decoration: none;
        font-weight: 500;
        margin: 0 4px;
        position: relative;
        transition: all 0.3s ease;

        &::after {
          content: "";
          position: absolute;
          width: 100%;
          height: 2px;
          bottom: -2px;
          left: 0;
          background-color: var(--el-color-primary);
          transform: scaleX(0);
          transform-origin: bottom left;
          transition: transform 0.3s ease-out;
        }

        &:hover {
          color: var(--el-color-primary-dark-2);
        }

        &:hover::after {
          transform: scaleX(1);
        }
      }
    }
  }
}

@keyframes slideInSuggestion {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 相关书籍区域
.related-books {
  margin-top: 50px;
  transition: transform 0.6s ease, opacity 0.6s ease;
  opacity: 0;
  transform: translateY(30px);

  &.animate-in {
    opacity: 1;
    transform: translateY(0);
  }

  h3 {
    font-size: 18px;
    color: var(--el-text-color-primary);
    margin-bottom: 24px;
    font-weight: 600;
    text-align: left;
    position: relative;
    padding-left: 16px;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 50%;
      height: 70%;
      width: 4px;
      transform: translateY(-50%);
      background: var(--el-color-primary);
      border-radius: 2px;
    }
  }

  .book-recommendations {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    perspective: 1000px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 480px) {
      grid-template-columns: repeat(2, 1fr);
      gap: 15px;
    }
  }

  .book-card {
    border-radius: 12px;
    overflow: hidden;
    background: var(--el-bg-color);
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    cursor: pointer;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transform: translateY(30px) rotateX(10deg);
    opacity: 0;

    &.visible {
      opacity: 1;
      transform: translateY(0) rotateX(0);
    }

    &:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
      z-index: 10;

      .shine-effect {
        transform: translateX(100%) skewX(-15deg);
        transition: transform 0.6s;
      }

      .book-title {
        color: var(--el-color-primary);
      }
    }

    .book-cover {
      aspect-ratio: 2/3;
      overflow: hidden;
      border-radius: 8px;
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
      position: relative;

      .el-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
      }

      &:hover .el-image {
        transform: scale(1.05);
      }

      .cover-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--el-fill-color-light);

        .el-icon {
          font-size: 32px;
          color: var(--el-text-color-secondary);
        }
      }

      .shine-effect {
        position: absolute;
        top: 0;
        left: -100%;
        width: 50%;
        height: 100%;
        background: linear-gradient(
          to right,
          rgba(255, 255, 255, 0) 0%,
          rgba(255, 255, 255, 0.3) 50%,
          rgba(255, 255, 255, 0) 100%
        );
        transform: skewX(-15deg);
        transform-origin: top left;
        transition: transform 0s;
      }
    }

    .book-title {
      margin-top: 12px;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      text-align: left;
      padding: 0 8px;
      transition: color 0.3s ease;
    }

    .book-author {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
      text-align: left;
      padding: 0 8px;
      margin-bottom: 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 浮动动画
@keyframes float {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-10px) rotate(-1deg);
  }
  50% {
    transform: translateY(-20px) rotate(0deg);
  }
  75% {
    transform: translateY(-10px) rotate(1deg);
  }
}

// 纸飞机飞行路径动画
@keyframes complexFly {
  0% {
    left: -50px;
    top: 40%;
    transform: rotate(-30deg) scale(0.8);
  }
  10% {
    left: 20%;
    top: 30%;
    transform: rotate(15deg) scale(1);
  }
  30% {
    left: 40%;
    top: 60%;
    transform: rotate(-10deg) scale(0.9);
  }
  50% {
    left: 60%;
    top: 30%;
    transform: rotate(5deg) scale(1.1);
  }
  70% {
    left: 75%;
    top: 50%;
    transform: rotate(-5deg) scale(1);
  }
  90% {
    left: 100%;
    top: 40%;
    transform: rotate(10deg) scale(0.9);
  }
  100% {
    left: 110%;
    top: 45%;
    transform: rotate(0deg) scale(0.8);
  }
}

// 纸飞机拖尾动画
@keyframes trailAnimation {
  0% {
    width: 0;
    opacity: 0.7;
  }
  50% {
    width: 50px;
    opacity: 0.5;
  }
  100% {
    width: 0;
    opacity: 0.2;
  }
}

// 移动设备优化
@media (max-width: 576px) {
  .error-illustration {
    height: 160px;
  }

  .floating-book {
    width: 30px;
    height: 40px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-message {
    font-size: 15px;
  }

  .suggestions {
    padding: 20px;
  }

  .book-card {
    .book-title {
      font-size: 13px;
    }
    .book-author {
      font-size: 11px;
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .not-found-container {
    background-image: linear-gradient(to bottom, #1a1d23 0%, #121416 100%);
  }

  .not-found-content {
    background-color: #242529;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  }

  .star {
    box-shadow: 0 0 10px 0 rgba(255, 255, 255, 0.5);
  }

  .error-image {
    filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.4)) brightness(0.95);
  }

  .book-card {
    background: #2c2d32;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);

    &:hover {
      box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3);
    }

    .shine-effect {
      background: linear-gradient(
        to right,
        rgba(255, 255, 255, 0) 0%,
        rgba(255, 255, 255, 0.1) 50%,
        rgba(255, 255, 255, 0) 100%
      );
    }
  }
}

// 交互提示效果
.bounce {
  animation: bounce 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

@keyframes bounce {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

// 点击波纹效果
@keyframes ripple {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(15);
    opacity: 0;
  }
}

// 页面加载进入动画
@keyframes slideInFromBottom {
  0% {
    transform: translateY(50px);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}

// 增加页面滚动时的视差效果
.parallax-element {
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

// 打字机效果动画
@keyframes typing {
  from {
    width: 0;
  }
  to {
    width: 100%;
  }
}

// 闪烁的光标效果
@keyframes blink-caret {
  from,
  to {
    border-color: transparent;
  }
  50% {
    border-color: var(--el-color-primary);
  }
}

// 页面离开时的淡出动画
.fade-leave-active {
  animation: fadeOut 0.3s forwards;
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

// 添加书本翻页效果
@keyframes flipPage {
  0% {
    transform: rotateY(0deg);
  }
  100% {
    transform: rotateY(180deg);
  }
}

// 增加一些响应式悬停效果
@media (hover: hover) {
  .action-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(var(--el-color-primary-rgb), 0.25);
  }

  .link-text:hover {
    text-shadow: 0 0 1px var(--el-color-primary);
  }
}
</style>