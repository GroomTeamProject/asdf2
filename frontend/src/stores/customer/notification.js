import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi } from '@/api/customer/notificationApi'

export const useNotificationStore = defineStore('notification', () => {
  // 상태
  const notifications = ref([])
  const unreadCount = ref(0)
  const isLoading = ref(false)
  const error = ref(null)
  const sseConnection = ref(null)

  // 계산된 속성
  const hasUnreadNotifications = computed(() => unreadCount.value > 0)

  // 액션
  const fetchNotifications = async (userId, page = 0, size = 20) => {
    try {
      isLoading.value = true
      error.value = null
      
      const response = await notificationApi.getUserNotifications(userId, page, size)
      
      if (page === 0) {
        // 첫 페이지는 교체
        notifications.value = response.content || []
      } else {
        // 추가 페이지는 추가
        notifications.value = [...notifications.value, ...(response.content || [])]
      }
      
      // 읽지 않은 알림 수 계산
      unreadCount.value = notifications.value.filter(notification => !notification.isRead).length
      
      return response
    } catch (err) {
      error.value = err.message
      console.error('알림 조회 실패:', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const markAsRead = (notificationId) => {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification && !notification.isRead) {
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  const markAllAsRead = () => {
    notifications.value.forEach(notification => {
      notification.isRead = true
    })
    unreadCount.value = 0
  }

  const addNotification = (notification) => {
    notifications.value.unshift(notification)
    if (!notification.isRead) {
      unreadCount.value += 1
    }
  }

  const connectSSE = (userId) => {
    if (sseConnection.value) {
      sseConnection.value.close()
    }
    
    sseConnection.value = notificationApi.connectSSE(userId)
    
    sseConnection.value.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data)
        addNotification(notification)
      } catch (err) {
        console.error('SSE 메시지 파싱 실패:', err)
      }
    }
    
    sseConnection.value.onerror = (err) => {
      console.error('SSE 연결 오류:', err)
    }
  }

  const disconnectSSE = () => {
    if (sseConnection.value) {
      sseConnection.value.close()
      sseConnection.value = null
    }
  }

  const clearNotifications = () => {
    notifications.value = []
    unreadCount.value = 0
    error.value = null
  }

  return {
    // 상태
    notifications,
    unreadCount,
    isLoading,
    error,
    sseConnection,
    
    // 계산된 속성
    hasUnreadNotifications,
    
    // 액션
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    addNotification,
    connectSSE,
    disconnectSSE,
    clearNotifications
  }
})
