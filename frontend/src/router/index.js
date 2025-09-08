import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Signup from '../pages/auth/Signup.vue'

// 라우트 경로 추가
const routes = [
  { path: '/', component: Home },
  { path: '/signup', component: Signup },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router