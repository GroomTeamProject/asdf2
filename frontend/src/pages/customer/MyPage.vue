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
              <h2 class="text-xl font-semibold text-gray-900">{{ userProfile?.name || '사용자' }}님, 안녕하세요!</h2>
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


      <!-- 바로가기 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">바로가기</h3>
        <div class="space-y-3">
          <button
            @click="goToEditProfile"
            class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-yellow-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-gray-900">회원정보 수정</div>
                <div class="text-sm text-gray-600">이름, 이메일, 전화번호 수정</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>

          <button
            @click="openAddressListModal"
            class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
              </div>
              <div class="text-left">
                <div class="font-medium text-gray-900">배송지 관리</div>
                <div class="text-sm text-gray-600">배송지 목록 보기 및 관리</div>
              </div>
            </div>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>

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


    <!-- 배송지 목록 모달 -->
    <div
      v-if="showAddressListModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[80vh] overflow-hidden">
        <div class="p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">배송지 목록</h3>
            <button
              @click="closeAddressListModal"
              class="text-gray-400 hover:text-gray-600"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <div v-if="loadingAddresses" class="flex justify-center items-center py-8">
            <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            <span class="ml-2 text-gray-600">배송지를 불러오는 중...</span>
          </div>

          <div v-else-if="userAddresses.length === 0" class="text-center py-8">
            <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
            </svg>
            <p class="text-gray-500">등록된 배송지가 없습니다.</p>
          </div>

          <div v-else class="space-y-3 max-h-96 overflow-y-auto">
            <div
              v-for="address in userAddresses"
              :key="address.id"
              class="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-2">
                    <h4 class="font-medium text-gray-900">{{ address.addressName }}</h4>
                    <span
                      v-if="address.isDefault"
                      class="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
                    >
                      기본
                    </span>
                  </div>
                  <p class="text-sm text-gray-600 mb-1">{{ address.address }}</p>
                  <p class="text-sm text-gray-600">{{ address.detailAddress }}</p>
                  <p class="text-xs text-gray-500 mt-1">우편번호: {{ address.zipcode }}</p>
                </div>
                <div class="flex gap-2 ml-4">
                  <button
                    @click="setDefaultAddress(address.id)"
                    :disabled="address.isDefault"
                    class="px-3 py-1 text-xs bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {{ address.isDefault ? '기본' : '기본 설정' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-end pt-4 border-t border-gray-200 mt-4">
            <button
              @click="closeAddressListModal"
              class="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
            >
              닫기
            </button>
          </div>
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
    
    // 모달 상태
    const showAddressListModal = ref(false)
    const loadingAddresses = ref(false)
    const userAddresses = ref([])

    const goToOrderHistory = () => {
      router.push('/customer/order-history')
    }

    const goToCart = () => {
      router.push('/customer/cart')
    }

    const goToStores = () => {
      router.push('/customer/stores')
    }

    const goToEditProfile = () => {
      router.push('/customer/edit-profile')
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


    // 배송지 목록 모달 관련 함수들
    const openAddressListModal = async () => {
      showAddressListModal.value = true
      await fetchUserAddresses()
    }

    const closeAddressListModal = () => {
      showAddressListModal.value = false
      userAddresses.value = []
    }

    const fetchUserAddresses = async () => {
      try {
        loadingAddresses.value = true
        const userId = localStorage.getItem('userId')
        
        if (userId) {
          const addresses = await userApi.getUserAddresses(userId)
          userAddresses.value = addresses || []
        }
      } catch (error) {
        console.error('배송지 목록 조회 실패:', error)
        userAddresses.value = []
      } finally {
        loadingAddresses.value = false
      }
    }

    const setDefaultAddress = async (addressId) => {
      try {
        // TODO: 기본 배송지 설정 API 구현 후 연결
        alert('기본 배송지 설정 기능은 추후 구현 예정입니다.')
      } catch (error) {
        console.error('기본 배송지 설정 실패:', error)
        alert('기본 배송지 설정에 실패했습니다.')
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
      goToEditProfile,
      handleLogout,
      // 모달 관련
      showAddressListModal,
      loadingAddresses,
      userAddresses,
      // 모달 함수들
      openAddressListModal,
      closeAddressListModal,
      setDefaultAddress,
    }
  },
}
</script>
