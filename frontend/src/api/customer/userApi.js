import api from '../index.js'

class UserApiManager {
  constructor() {
    this.requestQueue = []
    this.isProcessing = false
  }

  async processQueue() {
    if (this.isProcessing) return

    this.isProcessing = true

    while (this.requestQueue.length > 0) {
      const { apiFunction, resolve, reject } = this.requestQueue.shift()

      try {
        const result = await apiFunction()
        resolve(result)
        await new Promise((resolve) => setTimeout(resolve, 100))
      } catch (error) {
        reject(error)
      }
    }

    this.isProcessing = false
  }

  async queueRequest(apiFunction) {
    return new Promise((resolve, reject) => {
      this.requestQueue.push({ apiFunction, resolve, reject })
      this.processQueue()
    })
  }
}

const apiManager = new UserApiManager()

export const userApi = {
  /**
   * 사용자 정보 관련 API
   */
  getUserProfile: (userId) =>
    apiManager.queueRequest(async () => {
      console.log('🔄 사용자 정보 조회 중...')
      const response = await api.get(`/users/${userId}`)
      console.log('✅ 사용자 정보 조회 성공')
      return response.data
    }),

  getUserAddresses: (userId) =>
    apiManager.queueRequest(async () => {
      console.log('🔄 사용자 주소 목록 조회 중...')
      const response = await api.get(`/users/${userId}/addresses`)
      console.log('✅ 사용자 주소 목록 조회 성공')
      return response.data
    }),

  /**
   * 사용자 정보 수정 API (추후 구현 예정)
   */
  updateUserProfile: (userId, userData) =>
    apiManager.queueRequest(async () => {
      console.log('🔄 사용자 정보 수정 중...')
      // TODO: 실제 API 엔드포인트 구현 후 연결
      // const response = await api.put(`/users/${userId}`, userData)
      // console.log('✅ 사용자 정보 수정 성공')
      // return response.data
      
      throw new Error('사용자 정보 수정 API는 아직 구현되지 않았습니다.')
    }),

  /**
   * 로그아웃 API
   */
  logout: () =>
    apiManager.queueRequest(async () => {
      console.log('🔄 로그아웃 중...')
      // TODO: 실제 API 엔드포인트 구현 후 연결
      // const response = await api.post('/auth/logout')
      // console.log('✅ 로그아웃 성공')
      // return response.data
      
      // 현재는 프론트엔드에서만 처리
      localStorage.removeItem('jwt')
      localStorage.removeItem('userType')
      localStorage.removeItem('userId')
      return { success: true, message: '로그아웃되었습니다.' }
    }),
}
