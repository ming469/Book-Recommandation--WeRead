import Mock from "mockjs";
import { getBookDefaultCover } from "@/utils/cover-helper";

const Random = Mock.Random;

// 生成图书数据的辅助函数
const generateBooks = (count) => {
    const books = [];
    for (let i = 0; i < count; i++) {
        const id = Random.integer(1000, 9999);
        books.push({
            id: id,
            title: Random.ctitle(4, 12),
            cover: getBookDefaultCover(id),
            author: Random.cname(),
            description: Random.cparagraph(2, 5),
            publisher: Random.ctitle(4, 6) + '出版社',
            publishDate: Random.date(),
            isbn: Random.string('number', 13),
            price: Random.float(20, 100, 2, 2),
            category: Random.pick(['小说', '文学', '计算机', '历史', '心理学']),
            rating: Random.float(3, 5, 1, 1)
        });
    }
    return books;
};

// 模拟图书列表
Mock.mock(/\/api\/user\/books(\?.*)?$/, "get", () => {
    return {
        meta: { code: 200, message: "success" },
        data: {
            content: generateBooks(10),
            currentPage: 1,
            totalPages: 5,
            totalElements: 50,
            size: 10
        }
    };
});

// 模拟热门/推荐图书 (模拟IUF ItemCF算法结果)
Mock.mock(/\/api\/user\/books\/recommended/, "get", () => {
    const data = [];
    for (let i = 0; i < 8; i++) {
        const id = i + 1;
        data.push({
            id: id,
            title: Random.ctitle(4, 10),
            cover: getBookDefaultCover(id),
            author: Random.cname(),
            description: Random.cparagraph(1, 2),
            recommendReason: Random.pick([
                "根据您的阅读历史推荐",
                "与《" + Random.ctitle(3, 6) + "》相似",
                "ItemCF算法高分推荐",
                "98%的用户也喜欢",
                "近期热门必读"
            ]),
            similarity: Random.float(0.1, 1, 2, 2)
        });
    }
    return {
        meta: { code: 200, message: "success" },
        data: data
    };
});

// 模拟最新图书
Mock.mock(/\/api\/user\/books\/latest/, "get", () => {
    const data = [];
    for (let i = 0; i < 8; i++) {
        const id = 100 + i;
        data.push({
            id: id,
            title: Random.ctitle(4, 10),
            cover: getBookDefaultCover(id),
            author: Random.cname(),
            description: Random.cparagraph(1, 2),
            publishDate: Random.date('yyyy-MM-dd')
        });
    }
    return {
        meta: { code: 200, message: "success" },
        data: data
    };
});

// 模拟图书详情
Mock.mock(/\/api\/user\/books\/\d+/, "get", () => {
    const id = Random.integer(1000, 9999);
    const similarBooks = [];
    for (let i = 0; i < 4; i++) {
        const sid = 200 + i;
        similarBooks.push({
            id: sid,
            title: Random.ctitle(4, 8),
            cover: getBookDefaultCover(sid),
            author: Random.cname(),
            similarity: Random.float(0.6, 0.99, 2, 2)
        });
    }
    return {
        meta: { code: 200, message: "success" },
        data: {
            id: id,
            title: Random.ctitle(5, 15),
            cover: getBookDefaultCover(id),
            author: Random.cname(),
            description: Random.cparagraph(5, 10),
            publisher: Random.ctitle(4, 6) + "出版社",
            publishDate: Random.date('yyyy-MM-dd'),
            isbn: Random.string('number', 13),
            price: Random.float(20, 100, 2, 2),
            category: Random.pick(['小说', '文学', '计算机', '历史', '心理学']),
            rating: Random.float(3, 5, 1, 1),
            reviewCount: Random.integer(10, 1000),
            similarBooks: similarBooks
        }
    };
});