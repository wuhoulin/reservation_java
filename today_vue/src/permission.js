import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false });

const whiteList = ['/login', '/auth-redirect', '/bind', '/register','/enterpriseApplicaion'];

router.beforeEach((to, from, next) => {
    NProgress.start()

    // 检查路由是否需要认证
    const requiresAuth = to.matched.some(record => !record.meta.noAuth);

    if (getToken()) {
        to.meta.title && useSettingsStore().setTitle(to.meta.title)
        if (to.path === '/login') {
            next({ path: '/' })
            NProgress.done()
        } else {
            if (useUserStore().roles.length === 0) {
                isRelogin.show = true
                useUserStore().getInfo().then(() => {
                    isRelogin.show = false
                    usePermissionStore().generateRoutes().then(accessRoutes => {
                        accessRoutes.forEach(route => {
                            if (!isHttp(route.path)) {
                                router.addRoute(route)
                            }
                        })
                        next({ ...to, replace: true })
                    })
                }).catch(err => {
                    useUserStore().logOut().then(() => {
                        ElMessage.error(err)
                        next({ path: '/' })
                    })
                })
            } else {
                next()
            }
        }
    } else {
        if (whiteList.indexOf(to.path) !== -1 || !requiresAuth) {
            // 在免登录白名单或不需要认证的路由，直接进入
            next()
        } else {
            next(`/login?redirect=${to.fullPath}`)
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})