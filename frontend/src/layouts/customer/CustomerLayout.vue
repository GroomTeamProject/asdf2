<template>
  <div class="flex min-h-screen flex-col items-center justify-center">
    <CustomerHeader />

    <main class="flex w-full flex-grow flex-col bg-neutral-50 p-4">
      <router-view />
    </main>

    <Footer />
    
    <!-- 플로팅 장바구니 버튼 -->
    <FloatingCartButton />
  </div>
</template>

<script setup>
import CustomerHeader from './CustomerHeader.vue'
import Footer from '../Footer.vue'
import FloatingCartButton from '@/components/customer/cart/FloatingCartButton.vue'
</script>
