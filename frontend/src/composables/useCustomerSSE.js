/**
 * 사용자 SSE 훅
 * 사용자 페이지에서 사용하는 SSE 연결 및 알림 관리
 */
import { ref, onMounted, onUnmounted } from 'vue'
import sseManager from '@/utils/sse.js'

export function useCustomerSSE() {
  const isConnected = ref(false)
  const notifications = ref([])
  const currentUserId = ref(null)

  /**
   * SSE 연결 시작
   */
  const connectSSE = (userId) => {
    if (!userId) {
      console.warn('SSE 연결을 위해 userId가 필요합니다.')
      return
    }

    currentUserId.value = userId
    sseManager.connect(userId)
    
    // 연결 상태 업데이트
    isConnected.value = sseManager.getConnectionStatus()
  }

  /**
   * 알림 표시
   * @param {string} message - 알림 메시지
   * @param {string} type - 알림 타입 (info, success, warning, error)
   */
  const showNotification = (message, type = 'info') => {
    const notification = {
      id: Date.now(),
      message,
      type,
      timestamp: new Date()
    }
    
    notifications.value.push(notification)
    
    // 5초 후 자동 제거
    setTimeout(() => {
      removeNotification(notification.id)
    }, 5000)
  }

  /**
   * 알림 제거
   * @param {number} id - 알림 ID
   */
  const removeNotification = (id) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  /**
   * 모든 알림 제거
   */
  const clearAllNotifications = () => {
    notifications.value = []
  }

  /**
   * 주문 상태 업데이트 처리
   * @param {Object} data - 주문 상태 데이터
   */
  const handleOrderStatusUpdate = (data) => {
    const statusMessages = {
      'PENDING': '주문이 접수되었습니다.',
      'CONFIRMED': '주문이 확인되었습니다.',
      'PREPARING': '음식이 조리 중입니다.',
      'READY': '음식이 준비되었습니다.',
      'DELIVERING': '배달이 시작되었습니다.',
      'DELIVERED': '배달이 완료되었습니다.',
      'CANCELLED': '주문이 취소되었습니다.'
    }
    
    const message = statusMessages[data.status] || `주문 상태가 "${data.status}"로 변경되었습니다.`
    showNotification(message, 'info')
  }

  /**
   * 배달 업데이트 처리
   * @param {Object} data - 배달 데이터
   */
  const handleDeliveryUpdate = (data) => {
    const message = `배달 상태: ${data.status}`
    if (data.estimatedTime) {
      message += ` (예상 시간: ${data.estimatedTime})`
    }
    showNotification(message, 'info')
  }

  /**
   * 가게 알림 처리
   * @param {Object} data - 가게 알림 데이터
   */
  const handleStoreNotification = (data) => {
    const message = `${data.storeName || '가게'}에서 알림: ${data.message}`
    showNotification(message, 'info')
  }

  /**
   * 일반 알림 처리
   * @param {Object} data - 알림 데이터
   */
  const handleNotification = (data) => {
    showNotification(data.message, data.type || 'info')
  }

  // 컴포넌트 마운트 시 SSE 이벤트 리스너 등록
  onMounted(() => {
    // SSE 이벤트 리스너 등록
    sseManager.onNotification('notification', handleNotification)
    sseManager.onNotification('orderStatusUpdate', handleOrderStatusUpdate)
    sseManager.onNotification('deliveryUpdate', handleDeliveryUpdate)
    sseManager.onNotification('storeNotification', handleStoreNotification)

    // 사용자 ID가 있으면 자동 연결
    const userId = localStorage.getItem('userId')
    if (userId) {
      connectSSE(userId)
    }
  })

  // 컴포넌트 언마운트 시 SSE 연결 해제
  onUnmounted(() => {
    sseManager.disconnect()
    isConnected.value = false
  })

  return {
    // 상태
    isConnected,
    notifications,
    currentUserId,
    
    // 메서드
    connectSSE,
    showNotification,
    removeNotification,
    clearAllNotifications,
    
    // SSE 관리자 직접 접근 (필요한 경우)
    sseManager
  }
}
