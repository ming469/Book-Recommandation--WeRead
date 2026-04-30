<template>
  <div class="auth-decoration">
    <div class="decoration-content">
      <div class="illustration-container">
        <img
          :src="illustrationSrc"
          :alt="illustrationAlt"
          class="decoration-image"
        />
        <div class="decoration-overlay"></div>
      </div>

      <div class="decoration-text-container">
        <h2 class="decoration-title">{{ title }}</h2>
        <p class="decoration-subtitle">{{ subtitle }}</p>
      </div>
    </div>
  </div>
</template>
  
  <script setup>
/**
 * @file AuthDecoration.vue
 * @description 微读平台认证页面(登录/注册)左侧装饰区组件
 * @created 2025-03-05
 * @module components/auth/AuthDecoration
 */

import { defineProps } from "vue";

/**
 * 组件属性定义
 */
const props = defineProps({
  /**
   * 插图源路径
   * @type {String}
   * @default '@/assets/images/book-illustration.svg'
   */
  illustrationSrc: {
    type: String,
    default: () => require("@/assets/images/book-illustration.svg"),
  },

  /**
   * 插图替代文本
   * @type {String}
   * @default '阅读插图'
   */
  illustrationAlt: {
    type: String,
    default: "阅读插图",
  },

  /**
   * 装饰区标题
   * @type {String}
   * @default '发现阅读的无限可能'
   */
  title: {
    type: String,
    default: "发现阅读的无限可能",
  },

  /**
   * 装饰区副标题
   * @type {String}
   * @default '在微读，发现整个世界'
   */
  subtitle: {
    type: String,
    default: "在微读，发现整个世界",
  },

  /**
   * 背景渐变色起始颜色
   * @type {String}
   * @default '#2e7d32'
   */
  gradientStart: {
    type: String,
    default: "#2e7d32",
  },

  /**
   * 背景渐变色结束颜色
   * @type {String}
   * @default '#43a047'
   */
  gradientEnd: {
    type: String,
    default: "#43a047",
  },
});
</script>
  
  <style scoped>
/**
   * 微读平台 - 认证页面装饰区样式
   */
.auth-decoration {
  flex: 0 0 45%;
  background: v-bind(
    "`linear-gradient(135deg, ${props.gradientStart} 0%, ${props.gradientEnd} 100%)`"
  );
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.decoration-content {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.illustration-container {
  position: relative;
  width: 80%;
  max-width: 420px;
  margin-bottom: 48px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.decoration-image {
  width: 100%;
  height: auto;
  display: block;
  transform: scale(1.02);
  transition: transform 0.5s ease;
}

.illustration-container:hover .decoration-image {
  transform: scale(1.05);
}

.decoration-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0) 70%,
    rgba(0, 0, 0, 0.3) 100%
  );
  z-index: 1;
}

.decoration-text-container {
  text-align: center;
  color: #fff;
  max-width: 80%;
}

.decoration-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.decoration-subtitle {
  font-size: 18px;
  line-height: 1.7;
  opacity: 0.95;
  max-width: 540px;
  margin: 0 auto;
  font-weight: 300;
  letter-spacing: 0.3px;
  display: inline-block;
  margin: 0 auto;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .decoration-title {
    font-size: 28px;
  }

  .decoration-subtitle {
    font-size: 16px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .auth-decoration {
    background: v-bind(
      '`linear-gradient(135deg, ${props.gradientStart === "#2e7d32" ? "#1b5e20" : props.gradientStart} 0%, ${props.gradientEnd === "#43a047" ? "#388e3c" : props.gradientEnd} 100%)`'
    );
  }
}
</style>