<script setup>
import { ref } from 'vue'
import { POST } from '../libs/ajax'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const name = ref('')
const phone = ref('')

const signup = async () => {
  try {
    await POST('/auth/signup', {
      email: email.value,
      password: password.value,
      name: name.value,
      phone: phone.value,
    })
    alert('회원가입 성공! 로그인 페이지로 이동합니다.')
    router.push('/login')
  } catch (error) {
    alert(`회원가입 실패: ${error.message}`)
  }
}
</script>

<template>
  <div class="max-w-md mx-auto p-6 bg-white shadow-md rounded-lg">
    <h2 class="text-2xl font-bold mb-6">회원가입</h2>
    <form @submit.prevent="signup" class="space-y-4">
      <input v-model="email" type="email" placeholder="Email" class="w-full border p-2 rounded" required />
      <input v-model="password" type="password" placeholder="Password" class="w-full border p-2 rounded" required />
      <input v-model="name" type="text" placeholder="Name" class="w-full border p-2 rounded" required />
      <input v-model="phone" type="text" placeholder="Phone" class="w-full border p-2 rounded" />
      <button type="submit" class="w-full bg-blue-500 text-white py-2 rounded">회원가입</button>
    </form>
  </div>
</template>
