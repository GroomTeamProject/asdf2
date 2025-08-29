<script setup>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col items-center justify-center">
    <Header />

    <main class="flex w-full flex-grow flex-col bg-neutral-50 p-4">
      <router-view />
    </main>

    <Footer />
  </div>
</template>

<style scoped></style>
