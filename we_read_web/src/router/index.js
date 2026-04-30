/**
 * @file index.js
 * @description 路由配置文件，负责管理应用的所有路由规则
 * @updated 2025-03-20
 * @module router
 */

// 第三方库导入
import { createRouter, createWebHistory } from "vue-router";

// 同步加载首页组件，确保首屏加载速度
import HomeView from "@/views/common/HomeView.vue";

/**
 * 懒加载方式引入组件
 * 通过动态导入实现代码分割，优化首屏加载性能
 */
// 布局组件
const FrontLayout = () =>
    import ("@/layouts/FrontLayout.vue");
const AdminLayout = () =>
    import ("@/layouts/AdminLayout.vue");

// 公共页面组件
const SearchView = () =>
    import ( /* webpackChunkName: "search" */ "@/views/common/SearchView.vue");
const BookDetailView = () =>
    import (
        /* webpackChunkName: "book-detail" */
        "@/views/common/BookDetailView.vue"
    );
const BookReadView = () =>
    import (
        /* webpackChunkName: "book-read" */
        "@/views/common/BookReadView.vue"
    );
const CategoryView = () =>
    import (
        /* webpackChunkName: "category" */
        "@/views/common/CategoriesView.vue"
    );
const RankingView = () =>
    import ( /* webpackChunkName: "ranking" */ "@/views/common/RankingView.vue");
const ExploreView = () =>
    import ( /* webpackChunkName: "explore" */ "@/views/common/ExploreView.vue");
const FindView = () =>
    import ( /* webpackChunkName: "find" */ "@/views/common/FindView.vue");
const TermsView = () =>
    import ( /* webpackChunkName: "terms" */ "@/views/common/TermsView.vue");
const PrivacyView = () =>
    import ( /* webpackChunkName: "privacy" */ "@/views/common/PrivacyView.vue");
// const AboutView = () =>
//     import ( /* webpackChunkName: "about" */ "@/views/common/AboutView.vue");

// 认证相关组件
const LoginView = () =>
    import ( /* webpackChunkName: "auth" */ "@/views/common/LoginView.vue");
const RegisterView = () =>
    import ( /* webpackChunkName: "auth" */ "@/views/common/RegisterView.vue");
const ResetPasswordView = () =>
    import ( /* webpackChunkName: "auth" */ "@/views/common/ResetPasswordView.vue");
const VerifyEmailView = () =>
    import ( /* webpackChunkName: "auth" */ "@/views/common/VerifyEmailView.vue");

// 错误页面组件
const NotFoundView = () =>
    import ( /* webpackChunkName: "error" */ "@/views/common/NotFoundView.vue");
const ForbiddenView = () =>
    import ( /* webpackChunkName: "error" */ "@/views/common/ForbiddenView.vue");

// 用户中心相关组件
const UserCenterView = () =>
    import (
        /* webpackChunkName: "user-center" */
        "@/views/user/UserCenterView.vue"
    );
const UserProfileView = () =>
    import (
        /* webpackChunkName: "user-profile" */
        "@/views/user/UserProfileView.vue"
    );
const UserFavoritesView = () =>
    import (
        /* webpackChunkName: "user-favorites" */
        "@/views/user/UserFavoritesView.vue"
    );
const UserShelfView = () =>
    import (
        /* webpackChunkName: "user-shelf" */
        "@/views/user/UserShelfView.vue"
    );
const UserHistoryView = () =>
    import (
        /* webpackChunkName: "user-history" */
        "@/views/user/UserHistoryView.vue"
    );
const UserSettingsView = () =>
    import (
        /* webpackChunkName: "user-settings" */
        "@/views/user/UserSettingsView.vue"
    );
const UserHelpView = () =>
    import ( /* webpackChunkName: "user-help" */ "@/views/user/UserHelpView.vue");

// 管理员相关组件
const AdminDashboard = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminDashboard.vue");
const AdminBookList = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminBookList.vue");
const AdminUserList = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminUserList.vue");
const AdminUserGroups = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminUserGroups.vue");
const AdminCategoryManage = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminCategoryManage.vue");
const AdminProfile = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminProfile.vue");
const AdminSystemConfig = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminSystemConfig.vue");
const AdminAdmins = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminAdmins.vue");
const AdminRoles = () =>
    import ( /* webpackChunkName: "admin" */ "@/views/admin/AdminRoles.vue");

// 添加刷新组件
const AdminRefresh = {
    name: "AdminRefresh",
    render: () => null,
};

/**
 * 路由配置数组
 * 每个路由项包含 path、name、component 和 meta 信息
 * @typedef {Object} RouteConfig
 * @property {string} path - 路由路径
 * @property {string} name - 路由名称
 * @property {Component} component - 路由组件
 * @property {Object} meta - 路由元信息
 * @property {string} meta.title - 页面标题
 * @property {boolean} [meta.keepAlive] - 是否缓存组件
 * @property {boolean} [meta.requiresAuth] - 是否需要登录权限
 *
 * @type {Array<RouteConfig>}
 */
const routes = [
    // 前台路由 - 使用FrontLayout作为布局组件
    {
        path: "/",
        component: FrontLayout,
        children: [
            // 主页路由
            {
                path: "",
                name: "home",
                component: HomeView,
                meta: {
                    title: "微读 - 找到感兴趣的书",
                    keepAlive: true,
                },
            },

            // 内容浏览相关路由
            {
                path: "search",
                name: "search",
                component: SearchView,
                meta: {
                    title: "搜索结果 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "book/:id",
                name: "book-detail",
                component: BookDetailView,
                meta: {
                    title: "图书详情 - 微读",
                },
                // 路由参数校验
                props: (route) => ({
                    id: isNaN(Number(route.params.id)) ? null : Number(route.params.id),
                }),
            },
            {
                path: "book/:id/read",
                name: "book-read",
                component: BookReadView,
                meta: {
                    title: "在线阅读 - 微读",
                    requiresAuth: true,
                },
            },
            {
                path: "category/:id",
                name: "category",
                component: CategoryView,
                meta: {
                    title: "图书分类 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "categories",
                name: "categories",
                component: CategoryView,
                meta: {
                    title: "全部分类 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "ranking/:type",
                name: "ranking",
                component: RankingView,
                meta: {
                    title: "图书榜单 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "explore",
                name: "explore",
                component: ExploreView,
                meta: {
                    title: "探索更多 - 微读",
                    requiresAuth: true,
                },
            },

            // 其他功能页面路由
            {
                path: "find",
                name: "find",
                component: FindView,
                meta: {
                    title: "发现我们 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "terms",
                name: "terms",
                component: TermsView,
                meta: {
                    title: "用户协议 - 微读",
                    keepAlive: true,
                },
            },
            {
                path: "privacy",
                name: "privacy",
                component: PrivacyView,
                meta: {
                    title: "隐私政策 - 微读",
                    keepAlive: true,
                },
            },
        ],
    },

    // 用户中心路由及子路由
    {
        path: "/user",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            component: UserCenterView,
            meta: { requiresAuth: true },
            redirect: "/user/profile",
            children: [{
                    path: "profile",
                    name: "user-profile",
                    component: UserProfileView,
                    meta: {
                        title: "个人资料 - 微读",
                        requiresAuth: true,
                    },
                },
                {
                    path: "favorites",
                    name: "user-favorites",
                    component: UserFavoritesView,
                    meta: {
                        title: "我的收藏 - 微读",
                        requiresAuth: true,
                    },
                },
                {
                    path: "shelf",
                    name: "user-shelf",
                    component: UserShelfView,
                    meta: {
                        title: "我的书架 - 微读",
                        requiresAuth: true,
                    },
                },
                {
                    path: "history",
                    name: "user-history",
                    component: UserHistoryView,
                    meta: {
                        title: "阅读历史 - 微读",
                        requiresAuth: true,
                    },
                },
                {
                    path: "settings",
                    name: "user-settings",
                    component: UserSettingsView,
                    meta: { title: "个人设置" },
                },
                {
                    path: "help",
                    name: "user-help",
                    component: UserHelpView,
                    meta: {
                        title: "帮助中心 - 微读",
                    },
                },
            ],
        }, ],
    },

    // 认证相关路由
    {
        path: "/login",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "login",
            component: LoginView,
            meta: {
                title: "登录 - 微读",
            },
        }, ],
    },
    {
        path: "/register",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "register",
            component: RegisterView,
            meta: {
                title: "注册 - 微读",
            },
        }, ],
    },
    {
        path: "/reset-password",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "reset-password",
            component: ResetPasswordView,
            meta: {
                title: "重置密码 - 微读",
            },
        }, ],
    },
    {
        path: "/verify-email",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "verify-email",
            component: VerifyEmailView,
            meta: {
                title: "验证邮箱 - 微读",
            },
        }, ],
    },

    // 管理员相关路由
    {
        path: "/admin",
        component: AdminLayout, // 使用管理员布局
        redirect: "/admin/dashboard",
        meta: {
            requiresAdminAuth: true,
        },
        children: [{
                path: "dashboard",
                name: "admin-dashboard",
                component: AdminDashboard,
                meta: {
                    title: "控制面板 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "books",
                name: "admin-books",
                component: AdminBookList,
                meta: {
                    title: "图书管理 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "users",
                name: "admin-users",
                component: AdminUserList,
                meta: {
                    title: "用户管理 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "admins",
                name: "admin-admins",
                component: AdminAdmins,
                meta: {
                    title: "管理员管理 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "profile",
                name: "admin-profile",
                component: AdminProfile,
                meta: {
                    title: "个人资料 - 微读管理系统",
                    requiresAdminAuth: true,
                },
            },
            {
                path: "categories",
                name: "admin-categories",
                component: AdminCategoryManage,
                meta: {
                    title: "分类管理 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "system-config",
                name: "admin-system-config",
                component: AdminSystemConfig,
                meta: {
                    title: "系统配置 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "roles",
                name: "admin-roles",
                component: AdminRoles,
                meta: {
                    title: "角色权限 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
            {
                path: "user-groups",
                name: "admin-user-groups",
                component: AdminUserGroups,
                meta: {
                    title: "用户分组 - 微读管理系统",
                    requiresAdminAuth: true,
                    keepAlive: true,
                },
            },
        ],
    },

    // 添加刷新路由
    {
        path: "/admin-refresh",
        name: "admin-refresh",
        component: AdminRefresh,
        meta: {
            title: "刷新中 - 微读管理系统",
        },
    },

    // 错误页面路由
    {
        path: "/403",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "forbidden",
            component: ForbiddenView,
            meta: {
                title: "访问受限 - 微读",
            },
        }, ],
    },
    {
        path: "/404",
        component: FrontLayout, // 使用前台布局
        children: [{
            path: "",
            name: "not-found",
            component: NotFoundView,
            meta: {
                title: "页面未找到 - 微读",
            },
        }, ],
    },

    // 捕获所有未匹配的路由并重定向到404页面
    {
        path: "/:pathMatch(.*)*",
        redirect: "/404",
    },
];

/**
 * 创建路由实例
 * @type {import('vue-router').Router}
 */
const router = createRouter({
    history: createWebHistory(process.env.VUE_APP_BASE_URL || ""),
    routes,
    /**
     * 路由滚动行为控制
     * 页面跳转时保留或重置滚动位置
     * @param {import('vue-router').RouteLocationNormalized} to - 即将进入的目标路由对象
     * @param {import('vue-router').RouteLocationNormalizedLoaded} from - 当前导航正要离开的路由对象
     * @param {Object|null} savedPosition - 已经保存的滚动位置（如果存在）
     * @returns {Object|false} 返回滚动位置对象
     */
    scrollBehavior(to, from, savedPosition) {
        // 如果有保存的位置则使用之前的位置
        if (savedPosition) {
            return savedPosition;
        }

        // 如果路由有hash，滚动到对应的锚点
        if (to.hash) {
            return {
                el: to.hash,
                behavior: "smooth",
            };
        }

        // 默认滚动到顶部
        return { top: 0, behavior: "smooth" };
    },
});

export default router;
