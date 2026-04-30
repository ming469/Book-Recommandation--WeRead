/**
 * @file main.js
 * @description Vue 3 项目入口文件，配置全局插件、全局属性和错误处理。
 */

// 全局定义 BASE_URL，解决环境变量问题
window.BASE_URL = "/";

import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "@/assets/css/global.css";
import "@/assets/styles/iconfont.css";

// 引入 Mock 数据（仅在开发环境或演示模式下使用）
// 注意：如果后端服务已启动，请注释掉下面这行
// import "@/mock";

// 导入全局工具函数
import { formatDate, truncateText } from "@/utils/filters.js";

// 创建 Pinia 实例
const pinia = createPinia();

// 创建 Vue 应用实例
const app = createApp(App);

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
    console.error("全局错误:", err, info);
};

// 注册全局属性（过滤器）
app.config.globalProperties.$filters = {
    formatDate,
    truncateText,
};

// 使用插件
app.use(pinia);
app.use(ElementPlus, {
    locale: zhCn, // 设置 Element Plus 语言环境
    size: "default", // 组件默认大小
});
app.use(router);

// 导入权限控制
import "./router/permission";

// 挂载应用到 DOM
app.mount("#app");