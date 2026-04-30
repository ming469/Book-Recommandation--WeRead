import Mock from "mockjs";
import { getBookDefaultCover } from "@/utils/cover-helper";

const Random = Mock.Random;

// 模拟热门搜索
Mock.mock(/\/api\/user\/search\/hot/, "get", {
    "meta": { "code": 200, "message": "success" },
    "data": [
        "人工智能基础",
        "计算机视觉",
        "深度学习实战",
        "Python编程",
        "机器学习算法",
        "三体",
        "百年孤独",
        "被讨厌的勇气"
    ]
});

// 模拟搜索建议
Mock.mock(/\/api\/user\/search\/suggestions/, "get", {
    "meta": { "code": 200, "message": "success" },
    "data|5-10": [
        "@ctitle(3, 8)"
    ]
});

// 模拟搜索结果
Mock.mock(/\/api\/user\/search/, "get", () => {
    const data = [];
    for (let i = 0; i < 10; i++) {
        const id = 100 + i;
        data.push({
            id: id,
            title: Random.ctitle(4, 12),
            cover: getBookDefaultCover(id),
            author: Random.cname(),
            description: Random.cparagraph(1, 3),
            publisher: Random.ctitle(4, 6) + "出版社",
            rating: Random.float(1, 10, 1, 1),
            price: Random.float(20, 100, 2, 2)
        });
    }
    return {
        meta: { code: 200, message: "success" },
        data: {
            content: data,
            currentPage: 1,
            totalPages: 5,
            totalElements: 50,
            size: 10
        }
    };
});

// 模拟分类列表
Mock.mock(/\/api\/user\/categories/, "get", {
    "meta": { "code": 200, "message": "success" },
    "data": [
        "小说", "文学", "人文社科", "经济管理", "科技科普",
        "计算机与互联网", "成功励志", "生活", "少儿", "艺术",
        "动漫绘本", "教育考试", "杂志期刊"
    ]
});
