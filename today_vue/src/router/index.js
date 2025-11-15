import { createRouter, createWebHistory } from "vue-router";
import MainLayout from '@/layouts/MainLayout.vue';
import CommunityList from "@/views/CommunityList.vue";
import RoomDetail from "@/views/RoomDetail.vue";
import ReservationSuccess from "@/views/ReservationSuccess.vue";
import StudentReservations from "@/views/StudentReservations.vue";
import WeChatAuth from "@/views/WeChatAuth.vue";
import AuthCallback from "@/views/AuthCallback.vue";
import My from "@/views/My.vue"
const routes = [
    {
        path: "/",
        redirect: "/community-list"
    },
    {
        path: "/",
        component: MainLayout, // 使用主布局
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
                path: 'student-reservations',
                name: 'StudentReservations',
                component: StudentReservations,
                meta: { requiresAuth: true }
            },
            {
                path: 'my',
                name: 'My',
                component: My,
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
    }
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
                query: { redirect: to.fullPath }
            });
        } else {
            console.log('✅ 已登录，允许访问');
            next();
        }
    } else {
        console.log('🌐 公开路由，允许访问');
        next();
    }
});

export default router;
