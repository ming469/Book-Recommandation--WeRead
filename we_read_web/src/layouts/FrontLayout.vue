<!--
  文件名称：FrontLayout.vue
  文件描述：前台布局组件，包含头部、主体内容和底部
  创建日期：2025-03-21
-->

<template>
  <div class="front-layout">
    <!-- 页头组件 -->
    <header-component />

    <!-- 主体内容区域 -->
    <main class="main-content">
      <!-- 路由视图：根据路由动态渲染页面内容 -->
      <router-view />
    </main>

    <!-- 页脚组件 -->
    <footer-component />
  </div>
</template>
  
  <script>
import HeaderComponent from "@/components/layout/AppHeader.vue";
import FooterComponent from "@/components/layout/AppFooter.vue";

export default {
  name: "FrontLayout",
  components: {
    HeaderComponent,
    FooterComponent,
  },
};
</script>
  
  <style scoped>
.front-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>