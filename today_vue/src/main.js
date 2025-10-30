import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'; // 使用正确的导入路径
import router from '@/router'

import FcDesigner from '@form-create/designer';
const app = createApp(App) // 创建应用实例并赋值给变量 app
app.use(ElementPlus, { locale: zhCn }) // 使用 ElementPlus 插件并设置语言为中文
app.use(router) // 使用路由插件
app.mount('#app') // 挂载应用

app.use(FcDesigner);
app.use(FcDesigner.formCreate);


app.use(formCreate);

