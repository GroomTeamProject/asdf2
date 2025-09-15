<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-2xl mx-auto p-4 space-y-6">
      <!-- 프로필 섹션 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div v-if="loading" class="flex justify-center items-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span class="ml-2 text-gray-600">사용자 정보를 불러오는 중...</span>
        </div>
        
        <div v-else>
          <div class="flex items-center gap-4 mb-6">
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
            <div>
              <h2 class="text-xl font-semibold text-gray-900">{{ userProfile?.name || '사용자님' }}</h2>
              <p class="text-gray-600">고객</p>
            </div>
          </div>
          
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p class="text-gray-600">이메일</p>
              <p class="font-medium text-gray-900">{{ userProfile?.email || 'user@example.com' }}</p>
            </div>
            <div>
              <p class="text-gray-600">전화번호</p>
              <p class="font-medium text-gray-900">{{ userProfile?.phone || '010-1234-5678' }}</p>
            </div>
          </div>
        </div>
      </div>


      <!-- 메뉴 목록 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">내 정보</h3>
        <div class="space-y-3">
          <button
            @click="goToOrderHistory"
            class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-gray-900">주문 내역</div>
                <div class="text-sm text-gray-600">이전 주문을 확인해보세요</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>

          <button
            @click="goToCart"
            class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-2.5 5M7 13l2.5 5m6-5v6a2 2 0 01-2 2H9a2 2 0 01-2-2v-6m8 0V9a2 2 0 00-2-2H9a2 2 0 00-2 2v4.01"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-gray-900">장바구니</div>
                <div class="text-sm text-gray-600">담은 메뉴를 확인해보세요</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>

          <button
            @click="goToStores"
            class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-gray-900">가게 둘러보기</div>
                <div class="text-sm text-gray-600">다양한 가게를 확인해보세요</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- 설정 및 기타 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">설정</h3>
        <div class="space-y-3">
          <button
            @click="handleLogout"
            class="w-full flex items-center justify-between p-4 bg-red-50 rounded-lg hover:bg-red-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-red-700">로그아웃</div>
                <div class="text-sm text-red-600">계정에서 로그아웃합니다</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/customer/userApi'

export default {
  name: 'MyPage',
  setup() {
    const router = useRouter()
    const userProfile = ref(null)
    const loading = ref(true)

    const goToOrderHistory = () => {
      router.push('/customer/order-history')
    }

    const goToCart = () => {
      router.push('/customer/cart')
    }

    const goToStores = () => {
      router.push('/customer/stores')
    }

    const handleLogout = async () => {
      if (confirm('정말로 로그아웃하시겠습니까?')) {
        try {
          await userApi.logout()
          router.push('/login')
        } catch (error) {
          console.error('로그아웃 실패:', error)
          // 에러가 발생해도 로컬 스토리지는 정리하고 로그인 페이지로 이동
          localStorage.removeItem('jwt')
          localStorage.removeItem('userType')
          localStorage.removeItem('userId')
          router.push('/login')
        }
      }
    }

    const fetchUserData = async () => {
      try {
        loading.value = true
        const userId = localStorage.getItem('userId')
        
        if (userId) {
          const profile = await userApi.getUserProfile(userId)
          userProfile.value = profile
        }
      } catch (error) {
        console.error('사용자 데이터 조회 실패:', error)
        // 에러 발생 시 기본값 설정
        userProfile.value = {
          name: '사용자님',
          email: 'user@example.com',
          phone: '010-1234-5678'
        }
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      fetchUserData()
    })

    return {
      userProfile,
      loading,
      goToOrderHistory,
      goToCart,
      goToStores,
      handleLogout,
    }
  },
}
</script>
