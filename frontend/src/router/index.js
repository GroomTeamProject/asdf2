// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/payment/Home.vue'
import Signup from '../pages/auth/Signup.vue'
import Login from '../pages/auth/Login.vue'
import StoreRegistration from '../pages/owner/StoreRegistration.vue'

// 권한별 메인 페이지
import CustomerMain from '../pages/customer/CustomerMain.vue'
import OwnerMain from '../pages/owner/OwnerMain.vue'
import DriverMain from '../pages/driver/DriverMain.vue'

import DefaultLayout from '../layouts/DefaultLayout.vue'

import { customerRoutes } from './customerRoutes.js'
import CartPage from '../pages/payment/cartPage.vue'
import OrderCheck from '../pages/payment/OrderCheckout.vue'
import PaymentWidget from '../pages/payment/PaymentWidget.vue'
import SuccessPage from '../pages/payment/SuccessPage.vue'
import FailPage from '../pages/payment/FailPage.vue'
import OrderStatus from '../pages/payment/OrderStatus.vue'

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: '', component: Home },
      { path: '/signup', component: Signup },
      { path: '/login', component: Login },
      { path: '/customer-main', component: CustomerMain, meta: { role: 'CUSTOMER' } },
      { path: '/owner-main', component: OwnerMain, meta: { role: 'OWNER' } },
      { path: '/store-registration', component: StoreRegistration, meta: { role: 'OWNER' } },
      { path: '/driver-main', component: DriverMain, meta: { role: 'RIDER' } },
      //{ path: '/dashboard', component: Dashboard }, //로그인후 이동하는 임시 페이지

      // TODO: 레이아웃에 맞춰서 수정해주세요
      { path: '/cart', component: CartPage },
      { path: '/ordercheck', component: OrderCheck },
      { path: '/payment', component: PaymentWidget },
      { path: '/success', component: SuccessPage },
      { path: '/fail', component: FailPage },
      { path: '/orderstatus', component: OrderStatus },
    ],
  },
  ...customerRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})


//  라우터 가드 설정
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('jwt')
  const userType = localStorage.getItem('userType')

  // 로그인 필요 없는 페이지
  if (['/', '/login', '/signup'].includes(to.path)) {
    return next()
  }

  // 로그인 안 돼 있으면 로그인 페이지로
  if (!token) {
    return next('/login')
  }

  // 라우트 메타에 role이 있으면 체크
  if (to.meta.role && to.meta.role !== userType) {
    alert('접근 권한이 없습니다!')
    return next('/')
  }

  next()
})

export default router
