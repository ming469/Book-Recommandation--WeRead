<template>
  <!-- 
    底部组件 (FooterComponent)
    该组件用于展示网站的底部信息，包括版权声明和相关链接。
  -->
  <footer class="footer">
    <div class="footer-container">
      <!-- 版权信息 -->
      <div class="copyright">
        © 2024 - {{ currentYear }} ming469
      </div>
      <!-- 底部链接 -->
      <div class="footer-links">
        <a href="/terms" class="footer-link">用户协议</a>
        <a href="/privacy" class="footer-link">隐私政策</a>
      </div>
    </div>
  </footer>
</template>

<script>
import { computed } from "vue";

export default {
  name: "FooterComponent",

  /**
   * 组件的 setup 方法
   *
   * @returns {Object} 组件的响应式数据
   */
  setup() {
    /**
     * 计算当前年份
     *
     * @computed {Number} currentYear - 自动计算当前年份
     */
    const currentYear = computed(() => {
      return new Date().getFullYear();
    });

    return {
      currentYear,
    };
  },
};
</script>

<style scoped>
/* 
  底部样式 (Footer)
  该样式用于控制页面底部的展示效果，包括背景色、文字大小、布局等
*/

.footer {
  background-color: #f4f5f7;
  padding: 15px 0;
  margin-top: auto;
  font-size: 12px;
  color: #999;
  font-family: Arial, sans-serif;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 版权信息部分 */
.copyright {
  font-family: "Garamond", "Georgia", serif;
}

/* 底部链接样式 */
.footer-links {
  display: flex;
}

/* 链接样式 */
.footer-link {
  color: #999;
  text-decoration: none;
  margin-left: 15px;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #666;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .footer-container {
    flex-direction: column;
    text-align: center;
  }

  .footer-links {
    margin-top: 10px;
  }

  .footer-link:first-child {
    margin-left: 0;
  }
}
</style>
