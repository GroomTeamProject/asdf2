// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../pages/Home.vue'
import CustomerPage from '../pages/customerMain.vue'
import CartPage from '../pages/cartPage.vue'
import PaymentPage from '../pages/Payment.vue'

const routes = [
  { path: '/', name: 'Main', component: MainPage },
  { path: '/customer', name: 'Customer', component: CustomerPage },
  { path: '/cart', component: CartPage },
  { path: '/payment', component: PaymentPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
