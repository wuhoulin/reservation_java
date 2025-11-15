import { createRouter, createWebHistory } from "vue-router";
import CommunityList from "@/views/CommunityList.vue";
import RoomDetail from "@/views/RoomDetail.vue";
import ReservationSuccess from "@/views/ReservationSuccess.vue";
import StudentReservations from "@/views/StudentReservations.vue";
import WeChatAuth from "@/views/WeChatAuth.vue";
import AuthCallback from "@/views/AuthCallback.vue";

const routes = [
    {
        path: "/",
        redirect: "/wechat-auth" // 添加根路径重定向
    },
    {
        path: "/community-list",
        name: "CommunityList",
        component: CommunityList,
        meta: { requiresAuth: true } // 添加需要登录的标记
    },
    {
        path: "/room/:roomId",
        name: "RoomDetail",
        component: RoomDetail,
        props: true,
        meta: { requiresAuth: true }
    },
    {
        path: "/reservation-success",
        name: "ReservationSuccess",
        component: ReservationSuccess,
        meta: { requiresAuth: true }
    },
    {
        path: '/student-reservations',
        name: 'StudentReservations',
        component: StudentReservations,
        meta: { requiresAuth: true }
    },
    {
        path: "/wechat-auth", // 修正路径
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
    console.log('路由守卫: ', { from: from.path, to: to.path });

    // 检查目标路由是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        // 检查用户是否已登录（通过检查localStorage中是否有openid）
        const openid = localStorage.getItem('wechat_openid');
        console.log('检查登录状态, openid:', openid);

        if (!openid) {
            // 未登录，跳转到微信授权页面
            console.log('未登录，跳转到授权页面');
            next({
                path: '/wechat-auth',
                query: { redirect: to.fullPath } // 保存目标路径，登录后跳转回来
            });
        } else {
            // 已登录，正常访问
            console.log('已登录，允许访问');
            next();
        }
    } else {
        // 不需要登录的路由，直接访问
        console.log('公开路由，允许访问');
        next();
    }
});

export default router;
