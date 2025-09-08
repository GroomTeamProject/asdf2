// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../pages/Home.vue'
import CustomerPage from '../pages/customerMain.vue'
import CartPage from '../pages/cartPage.vue'
import OrderCheck from '../pages/OrderCheckout.vue'
import PaymentWidget from "../pages/PaymentWidget.vue";
import SuccessPage from "../pages/SuccessPage.vue";
import FailPage from "../pages/FailPage.vue";
import OrderStatus from "../pages/OrderStatus.vue";

const routes = [
  { path: '/', name: 'Main', component: MainPage },
  { path: '/customer', name: 'Customer', component: CustomerPage },
  { path: '/cart', component: CartPage },
  { path: '/ordercheck', component: OrderCheck },
  { path: "/payment", component: PaymentWidget },
  { path: "/success", component: SuccessPage },
  { path: "/fail", component: FailPage },
  { path: "/orderstatus", component: OrderStatus },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router;
