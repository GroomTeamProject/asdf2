<script setup>
import Header from './layouts/Header.vue'
import Footer from './layouts/Footer.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <!-- 상단 헤더 -->
    <Header />

    <!-- 라우트 컴포넌트 렌더링 영역 -->
    <main class="flex w-full flex-grow flex-col bg-neutral-50 p-4">
      <router-view />
    </main>

    <!-- 하단 푸터 -->
    <Footer />
  </div>
</template>

<script>
export default {}
</script>

<style>
/* 전역 스타일 예시 */
body {
  margin: 0;
  font-family: 'Arial', sans-serif;
  background-color: #f0f0f0;
}
</style>
