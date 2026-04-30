import Mock from "mockjs";

// 配置 Mock
Mock.setup({
    timeout: "200-600", // 模拟网络延迟
});

// 引入模块
import "./book";
import "./search";
import "./user";

export default Mock;