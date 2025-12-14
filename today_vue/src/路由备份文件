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
import FilteredReservations from "@/views/FilteredReservations.vue";
import Notifications from "@/views/NotificationsPage.vue";
import CheckIn from "@/views/CheckIn.vue";
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
                path: 'reservations/filter/:status',
                name: 'FilteredReservations',
                component: FilteredReservations,
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
            }
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
    console.log('🚀 路由守卫: ', {
        from: from.path,
        to: to.path,
        requiresAuth: to.matched.some(record => record.meta.requiresAuth)
    });

    // 检查目标路由是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const openid = localStorage.getItem('wechat_openid');
        const token = localStorage.getItem('jwt_token');

        console.log('🔐 登录状态检查:', {
            openid: openid ? '有' : '无',
            token: token ? '有' : '无'
        });

        if (!openid || !token) {
            console.log('❌ 未登录，跳转到授权页面');
            next({
                path: '/wechat-auth',
                // 携带当前需要访问的路径，授权后跳回
                query: { redirect: to.fullPath }
            });
        } else {
            console.log('✅ 已登录，允许访问');
            next();
        }
    } else {
        // 非受保护路由（如 /wechat-auth、/auth-callback）
        const openid = localStorage.getItem('wechat_openid');
        const token = localStorage.getItem('jwt_token');

        // 关键修改2：如果已登录，且当前要去授权页，自动跳转到默认页（/community-list）
        if (openid && token && to.path === '/wechat-auth') {
            console.log('✅ 已登录，跳过授权页，跳转到社区列表');
            // 优先跳回之前携带的 redirect 路径，没有则跳默认页
            const redirectPath = to.query.redirect || '/community-list';
            next(redirectPath);
        } else {
            console.log('🌐 公开路由，允许访问');
            next();
        }
    }
});

export default router;
