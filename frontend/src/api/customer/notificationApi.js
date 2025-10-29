import { apiFetch } from '@/libs/apiFetch'

const BASE_URL = 'http://localhost:8080'

// 알림 관련 API
export const notificationApi = {
  // 사용자 알림 조회 (페이징)
  getUserNotifications: async (userId, page = 0, size = 20) => {
    try {
      const response = await apiFetch(`${BASE_URL}/api/notifications/user/${userId}?page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      return await response.json()
    } catch (error) {
      console.error('알림 조회 실패:', error)
      throw error
    }
  },

  // SSE 연결 상태 확인
  getConnectionStatus: async (userId) => {
    try {
      const response = await apiFetch(`${BASE_URL}/api/sse/status?userId=${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      return await response.json()
    } catch (error) {
      console.error('SSE 연결 상태 확인 실패:', error)
      throw error
    }
  },

  // SSE 연결
  connectSSE: (userId) => {
    const eventSource = new EventSource(`${BASE_URL}/api/sse/connect?userId=${userId}`)
    return eventSource
  }
}
