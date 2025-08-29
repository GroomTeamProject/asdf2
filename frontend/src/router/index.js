import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
// 라우트 경로 추가
const routes = [
  { path: '/', component: Home },

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
