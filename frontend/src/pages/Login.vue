<script setup>
import { ref } from 'vue'
import { POST } from '../libs/ajax'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')

const login = async () => {
  try {
    const res = await POST('/auth/login', {
      email: email.value,
      password: password.value,
    })
    localStorage.setItem('token', res.token) // JWT 저장
    alert('로그인 성공!')
    router.push('/')
  } catch (error) {
    alert(`로그인 실패: ${error.message}`)
  }
}
</script>

<template>
  <div class="max-w-md mx-auto p-6 bg-white shadow-md rounded-lg">
    <h2 class="text-2xl font-bold mb-6">로그인</h2>
    <form @submit.prevent="login" class="space-y-4">
      <input v-model="email" type="email" placeholder="Email" class="w-full border p-2 rounded" required />
      <input v-model="password" type="password" placeholder="Password" class="w-full border p-2 rounded" required />
      <button type="submit" class="w-full bg-green-500 text-white py-2 rounded">로그인</button>
    </form>
  </div>
</template>
