<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center">
    <div class="w-full max-w-md bg-white p-8 rounded-lg shadow-lg">
      <!-- Header -->
      <h1 class="text-2xl font-bold mb-6 text-center text-gray-800">QuickDeliver 로그인</h1>

      <!-- Login Form -->
      <form @submit.prevent="onSubmit" class="space-y-4">
        <div>
          <label for="email" class="block text-gray-700 mb-1">이메일</label>
          <input
            type="email"
            id="email"
            v-model="form.email"
            required
            class="w-full border border-gray-400 px-3 py-2 rounded focus:outline-none focus:border-gray-600"
          />
        </div>

        <!-- 비밀번호 -->
        <div class="relative">
          <label for="password" class="block text-gray-700 mb-1">비밀번호</label>
          <input
            :type="showPassword ? 'text' : 'password'"
            id="password"
            v-model="form.password"
            required
            class="w-full border border-gray-400 px-3 py-2 rounded focus:outline-none focus:border-gray-600 pr-10"
          />
          <!-- 아이콘 버튼 -->
          <button
            type="button"
            class="absolute inset-y-6 bottom-0 right-0 px-3 flex items-center text-gray-500"
            @click="togglePassword"
          >
            <svg
              v-if="showPassword"
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
              />
            </svg>
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13.875 18.825A10.05 10.05 0 0112 19c-4.477 0-8.268-2.943-9.542-7a9.956 9.956 0 012.042-3.368m2.122-2.122A9.956 9.956 0 0112 5c4.477 0 8.268 2.943 9.542 7a9.956 9.956 0 01-1.046 1.927M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18" />
            </svg>
          </button>
        </div>

        <button
          type="submit"
          class="w-full bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
        >
          로그인
        </button>
      </form>

      <!-- 링크 -->
      <p class="mt-4 text-center text-gray-600">
        아직 계정이 없으신가요?
        <a href="/signup" class="text-green-500 hover:underline">회원가입</a>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        email: '',
        password: ''
      },
      showPassword: false, // 👈 비밀번호 보기 상태
      loading: false
    }
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword
    },
    async onSubmit() {
      this.loading = true
      try {
        const response = await axios.post(`${import.meta.env.VITE_API_URL}/auth/login`, this.form)
        const { token, refreshtoken, id, email, name, userType } = response.data
        localStorage.setItem('jwt', token)
        localStorage.setItem('refreshToken', refreshtoken)
        localStorage.setItem('userId', id)
        localStorage.setItem('userEmail', email)
        localStorage.setItem('userName', name)
        localStorage.setItem('userType', userType)

        alert(`로그인 성공! 환영합니다, ${name}님.`)

        if (userType === 'CUSTOMER') {
          this.$router.push('/customer')
        } else if (userType === 'OWNER') {
          this.$router.push('/owner-main')
        } else if (userType === 'RIDER') {
          this.$router.push('/driver-main')
        }
      } catch (error) {
        console.error('로그인 실패:', error.response?.data || error)
        alert('로그인 실패: ' + (error.response?.data?.error || error.message))
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
