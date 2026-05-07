import { createRouter, createWebHistory } from "vue-router";
import MainLayout from '@/layouts/MainLayout.vue';
import CommunityList from "@/views/CommunityList.vue";
import RoomDetail from "@/views/RoomDetail.vue";
import ReservationSuccess from "@/views/ReservationSuccess.vue";
import WeChatAuth from "@/views/WeChatAuth.vue";
import AuthCallback from "@/views/AuthCallback.vue";
import My from "@/views/My.vue"
import UserProfile from "@/views/UserProfile.vue";
import Feedback from "@/views/Feedback.vue";
import Favorites from "@/views/Favorites.vue";
import Reservations from "@/views/Reservations.vue";
import ReservationDetail from "@/views/ReservationDetail.vue";
import Notifications from "@/views/NotificationsPage.vue";
import CheckIn from "@/views/CheckIn.vue";
import ActivityList from "@/views/ActivityList.vue";
import ActivityDetail from "@/views/ActivityDetail.vue";

const routes = [
    {
        path: "/",
        redirect: "/wechat-auth"
    },
    {
        path: "/",
        component: MainLayout,
        children: [
            {
                path: "community-list",
                name: "CommunityList",
                component: CommunityList,
                meta: { requiresAuth: true }
            },
            {
                path: "room/:roomId",
                name: "RoomDetail",
                component: RoomDetail,
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path: "reservation-success",
                name: "ReservationSuccess",
                component: ReservationSuccess,
                meta: { requiresAuth: true }
            },
            {
                path: 'my',
                name: 'My',
                component: My,
                meta: { requiresAuth: true }
            },
            {
                path: 'user-profile',
                name: 'UserProfile',
                component: UserProfile,
                meta: { requiresAuth: true }
            },
            {
                path: 'feedback',
                name: 'Feedback',
                component: Feedback,
                meta: { requiresAuth: true }
            },
            {
                path: 'favorites',
                name: 'Favorites',
                component: Favorites,
                meta: { requiresAuth: true }
            },
            {
                path: 'reservations',
                name: 'Reservations',
                component: Reservations,
                meta: { requiresAuth: true }
            },
            {
                path: 'reservation-detail/:id',
                name: 'ReservationDetail',
                component: ReservationDetail,
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path: 'check-in/:reservationId',
                name: 'CheckIn',
                component: CheckIn,
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path: 'notifications',
                name: 'Notifications',
                component: Notifications,
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path: "activity-list",
                name: "ActivityList",
                component: ActivityList,
                meta: { requiresAuth: true }
            },
            {
                path: "activity/:id",
                name: "ActivityDetail",
                component: ActivityDetail,
                props: true,
                meta: { requiresAuth: true }
            },
        ]
    },
    {
        path: "/wechat-auth",
        name: "WeChatAuth",
        component: WeChatAuth
    },
    {
        path: "/auth-callback",
        name: "AuthCallback",
        component: AuthCallback
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    // 1. 检查目标路由是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const openid = localStorage.getItem('wechat_openid');
        const token = localStorage.getItem('jwt_token');
        // 🔥 核弹级校验：不仅要有Token，还必须是当前活跃会话(Session)
        // 如果用户关了浏览器再开，Session会丢失，这里就会强制踢回授权页
        const isSessionActive = sessionStorage.getItem('session_active');

        if (!openid || !token || !isSessionActive) {
            console.log('❌ 安全拦截：会话失效或未登录，强制重新授权');
            // 无论你之前在哪个页面，只要会话断了，全部去 /wechat-auth 清洗数据
            next({
                path: '/wechat-auth',
                query: { redirect: to.fullPath }
            });
        } else {
            console.log('✅ 会话有效，允许访问');
            next();
        }
    } else {
        // 2. 对于公开路由（/wechat-auth, /auth-callback）
        // 🔥 移除所有“智能跳过”逻辑！
        // 哪怕用户已经登录了，如果他手动访问 /wechat-auth，我们也让他进去
        // 这样可以确保 WeChatAuth.vue 里的 clearAllCache() 100% 被执行
        console.log('🛡️ 进入公开页面，不拦截');
        next();
    }
});

export default router;
