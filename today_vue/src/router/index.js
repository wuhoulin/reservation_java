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

const routes = [
    {
        path: "/",
        redirect: "/community-list" // 根路径直接跳社区列表，不进授权页
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

// ====================== 调试模式核心配置 ======================
const DEBUG_MODE = true; // 调试时设为true（跳过授权），后续恢复设为false
// 从后端日志获取的真实有效数据（直接复用，接口能正常授权）
const MOCK_OPENID = "oAnc9vgK495dktuO_F43WR3fkrzg";
const MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJvcGVuaWQiOiJvQW5jOXZnSzQ5NWRrdHVPX0Y0M1dSM2ZrcnpnIiwic2NvcGUiOiJzbnNhcGlfdXNlcmluZm8iLCJuaWNrbmFtZSI6IsOjwoDCgiIsImhlYWRpbWd1cmwiOiJodHRwczovL3RoaXJkd3gucWxvZ28uY24vbW1vcGVuL3ZpXzMyL2IzQmNFNXlYQTA2OWRYdW43TnRGYXpiRFdVeDVxTzJRa2szcVltYWszNlRtaWJjYUNqUk5qTlJVYnlQNkpuaGlicTB5NGNzeG56c2JsMjRvNDNCb29acUEvMTMyIiwiYXV0aFRpbWUiOjE3NjM4ODY4NTUwMDMsInVzZXJOYW1lIjoi5ZC05Y6a6ZyWIiwidXNlcklkIjoxMDAsInN1YiI6Im9BbmM5dmdLNDk1ZGt0dU9fRjQzV1IzZmtyemciLCJpYXQiOjE3NjM4ODY4NTUsImV4cCI6MTg1MDI4Njg1NX0.wMG50taosCDpJ_QMhwrv1Kz0lH8-B1GDQ2WfWL4xWgI";
// ==============================================================

router.beforeEach((to, from, next) => {
    console.log('🚀 路由守卫: ', {
        from: from.path,
        to: to.path,
        requiresAuth: to.matched.some(record => record.meta.requiresAuth),
        debugMode: DEBUG_MODE
    });

    // 检查目标路由是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        // 调试模式：自动写入模拟用户数据，直接放行
        if (DEBUG_MODE) {
            console.log('🔧 调试模式：跳过授权，自动写入用户数据');
            // 写入模拟数据到本地存储（接口请求会自动携带）
            localStorage.setItem('wechat_openid', MOCK_OPENID);
            localStorage.setItem('jwt_token', MOCK_TOKEN);
            localStorage.setItem('user_info', JSON.stringify({
                openid: MOCK_OPENID,
                nickname: "测试用户",
                userName: "吴厚霖",
                college: "信息工程学院",
                major: "计算机科学与技术",
                phonenumber: "13800138000",
                headimgurl: "https://thirdwx.qlogo.cn/mmopen/vi_32/b3BcE5yXA069dXun7NtFazbDWUx5qO2Qkk3qYmak36TmibcaCjRNjNRUbyP6Jnhibq0y4csxnzsbl24o43BooZqA/132"
            }));
            next(); // 直接进入目标页面，不跳授权
            return;
        }

        // 非调试模式：保留原授权逻辑（后续恢复时自动生效）
        const openid = localStorage.getItem('wechat_openid');
        const token = localStorage.getItem('jwt_token');
        if (!openid || !token) {
            next({ path: '/wechat-auth', query: { redirect: to.fullPath } });
        } else {
            next();
        }
    } else {
        // 公开路由（如授权页），直接放行
        next();
    }
});

export default router;
