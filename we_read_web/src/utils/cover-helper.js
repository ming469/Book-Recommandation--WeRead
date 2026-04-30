/**
 * @file cover-helper.js
 * @description 封面图处理工具，用于根据 ID 分配本地默认封面
 */

// 书籍默认封面列表（假设有 5 张）
const bookDefaultCovers = [
    new URL('@/assets/images/book-illustration.svg',
        import.meta.url).href,
    // 以后可以添加更多本地图片路径
    // new URL('@/assets/images/covers/book-1.jpg', import.meta.url).href,
    // new URL('@/assets/images/covers/book-2.jpg', import.meta.url).href,
];

// 分类默认封面列表（假设有 8 张）
const categoryDefaultCovers = [
    new URL('@/assets/images/logo.png',
        import.meta.url).href,
    // 以后可以添加更多本地图片路径
    // new URL('@/assets/images/covers/cat-1.jpg', import.meta.url).href,
    // new URL('@/assets/images/covers/cat-2.jpg', import.meta.url).href,
];

/**
 * 获取书籍的默认封面
 * @param {number|string} bookId - 书籍 ID
 * @returns {string} 封面图片路径
 */
export const getBookDefaultCover = (bookId) => {
    if (!bookId) return bookDefaultCovers[0];
    const index = Number(bookId) % bookDefaultCovers.length;
    return bookDefaultCovers[index];
};

/**
 * 获取分类的默认封面
 * @param {number|string} categoryId - 分类 ID
 * @returns {string} 封面图片路径
 */
export const getCategoryDefaultCover = (categoryId) => {
    if (!categoryId) return categoryDefaultCovers[0];
    const index = Number(categoryId) % categoryDefaultCovers.length;
    return categoryDefaultCovers[index];
};

/**
 * 根据分类名称获取专属默认封面（优先级高于ID轮换）
 * 使用 public 下的静态资源路径，直接以根路径引用
 * @param {string} name - 分类名称
 * @returns {string|null} 封面图片路径或 null（无专属图）
 */
export const getCategoryCoverByName = (name) => {
    if (!name) return null;
    const n = String(name).trim();
    if (n === '文学') {
        return '/文学.png';
    }
    if (n === '小说') {
        return '/小说.png';
    }
    if (n === '历史') {
        return '/历史.png';
    }
    if (n === '科学') {
        return '/科学.png';
    }
    if (n === '艺术') {
        return '/艺术.png';
    }
    if (n === '哲学') {
        return '/哲学.png';
    }
    if (n === '心理学') {
        return '/心理学.png';
    }
    if (n === '经济学') {
        return '/经济学.png';
    }
    if (n === '生物') {
        return '/生物.png';
    }
    if (n === '物理') {
        return '/物理.png';
    }
    if (n === '化学') {
        return '/化学.png';
    }
    if (n === '计算机') {
        return '/计算机.png';
    }
    if (n === '设计') {
        return '/设计.png';
    }
    if (n === '商业') {
        return '/商业.png';
    }
    if (n === '旅游') {
        return '/旅游.png';
    }
    if (n === '传记') {
        return '/传记.png';
    }
    if (n === '建筑') {
        return '/建筑.png';
    }
    if (n === '电影') {
        return '/电影.png';
    }
    if (n === '漫画') {
        return '/漫画.png';
    }
    if (n === '旅行') {
        return '/旅行.png';
    }
    if (n === '美食') {
        return '/美食.png';
    }
    if (n === '职场') {
        return '/职场.png';
    }
    if (n === '教育') {
        return '/教育.png';
    }
    if (n === '经管') {
        return '/经管.png';
    }
    if (n === '科技') {
        return '/科技.png';
    }


    return null;
};