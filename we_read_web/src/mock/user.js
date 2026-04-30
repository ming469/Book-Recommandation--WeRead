import Mock from "mockjs";

// 模拟用户登录
Mock.mock(/\/api\/user\/auth\/login/, "post", {
    meta: { code: 200, message: "登录成功" },
    data: {
        token: "mock-token-123456",
        user: {
            id: 1,
            username: "mock_user",
            nickname: "测试用户",
            avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
            email: "test@example.com",
            role: "USER"
        }
    }
});

// 模拟获取用户信息
Mock.mock(/\/api\/user\/profile/, "get", {
    meta: { code: 200, message: "success" },
    data: {
        id: 1,
        username: "mock_user",
        nickname: "测试用户",
        avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
        email: "test@example.com",
        role: "USER",
        bio: "这是一个模拟用户，用于前端演示。",
        createTime: "2023-01-01"
    }
});