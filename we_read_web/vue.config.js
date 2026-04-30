// vue.config.js
// 该文件用于配置 Vue CLI 项目的 Webpack 相关设置
const { defineConfig } = require("@vue/cli-service");
const webpack = require("webpack");

module.exports = defineConfig({
  // 配置应用的基础路径，默认为 "/"
  publicPath: "/",

  // 允许 Babel 转译 node_modules 目录下的依赖，以提高兼容性
  transpileDependencies: true,
  lintOnSave: false,

  // Webpack 相关配置
  configureWebpack: {
    plugins: [
      // 使用 webpack.DefinePlugin 显式定义 Vue 3 的特性开关（Feature Flags）
      new webpack.DefinePlugin({
        /**
         * __VUE_OPTIONS_API__:
         * - "true"：启用 Vue 2 兼容的 Options API（如 data、methods、computed 等）。
         * - "false"：完全禁用 Options API，仅使用 Composition API。
         */
        __VUE_OPTIONS_API__: "true",

        /**
         * __VUE_PROD_DEVTOOLS__:
         * - "true"：在生产环境启用 Vue 开发者工具（可能影响性能）。
         * - "false"：在生产环境禁用 Vue 开发者工具，推荐默认设置。
         */
        __VUE_PROD_DEVTOOLS__: "false",

        // 这个标志用于 Vue 服务器端渲染（SSR）和客户端渲染（CSR）不匹配时的调试信息
        // 在生产环境通常应该禁用它
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: "false",
      }),
    ],
  },

  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
});
