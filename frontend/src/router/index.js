import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Signup from '../pages/auth/Signup.vue'
import Login from '../pages/auth/Login.vue'
import Dashboard from '../pages/auth/Dashboard.vue'

// 라우트 경로 추가
const routes = [
  { path: '/', component: Home },
  { path: '/signup', component: Signup },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard }, //로그인후 이동하는 임시 페이지
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 라우터 보호 (로그인 필요 페이지) , 로그아웃하면 못 들어옴
router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/signup']
  const authRequired = !publicPages.includes(to.path)
  const loggedIn = localStorage.getItem('jwt')

  if (authRequired && !loggedIn) {
    return next('/login')
  }
  next()
})

export default router