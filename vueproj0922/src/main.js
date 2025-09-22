import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'   // 라우터 불러오기

const app = createApp(App)
app.use(router)                 // 라우터 등록
app.mount('#app')
