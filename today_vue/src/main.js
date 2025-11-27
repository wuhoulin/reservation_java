import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import router from '@/router'
import FcDesigner from '@form-create/designer'


import VConsole from 'vconsole'
if (process.env.NODE_ENV === 'development') {
    new VConsole()
}

const app = createApp(App)

app.use(ElementPlus, { locale: zhCn })
app.use(router)

app.use(FcDesigner)

app.mount('#app')
