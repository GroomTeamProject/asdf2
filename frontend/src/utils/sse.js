/**
 * 전역 SSE 관리자
 * 앱 전체에서 실시간 알림을 관리하는 싱글톤 클래스
 */
class SSEManager {
  constructor() {
    this.eventSource = null
    this.userId = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.notificationCallbacks = []
  }

  /**
   * SSE 연결 시작
   * @param {string} userId - 사용자 ID
   */
  connect(userId) {
    if (this.isConnected && this.userId === userId) {
      console.log('SSE already connected for user:', userId)
      return
    }

    this.userId = userId
    this.disconnect() // 기존 연결이 있다면 끊기

    try {
      const baseURL = import.meta.env.VITE_API_URL
      const url = `${baseURL}/sse/connect/${userId}`
      console.log('🔄 SSE 연결 시도:', url)
      
      this.eventSource = new EventSource(url)
      this.setupEventListeners()
      
    } catch (error) {
      console.error('SSE 연결 실패:', error)
      this.handleConnectionError()
    }
  }

  /**
   * 이벤트 리스너 설정
   */
  setupEventListeners() {
    if (!this.eventSource) return

    // 연결 성공
    this.eventSource.onopen = () => {
      console.log('✅ SSE 연결 성공!')
      console.log('📡 연결된 사용자 ID:', this.userId)
      console.log('🌐 연결 URL:', this.eventSource.url)
      this.isConnected = true
      this.reconnectAttempts = 0
    }

    // 연결 오류
    this.eventSource.onerror = (error) => {
      console.error('❌ SSE 연결 오류 발생!')
      console.error('🔍 오류 상세:', error)
      console.error('📡 연결 시도한 URL:', this.eventSource?.url)
      console.error('👤 사용자 ID:', this.userId)
      console.error('🔄 재연결 시도 횟수:', this.reconnectAttempts)
      this.isConnected = false
      this.handleConnectionError()
    }

    // 서버에서 오는 이벤트들 처리
    this.eventSource.addEventListener('notification', (event) => {
      console.log('📢 알림 수신:', event.data)
      this.handleNotification(event.data)
    })

    this.eventSource.addEventListener('order-status-update', (event) => {
      console.log('📦 주문 상태 업데이트:', event.data)
      this.handleOrderStatusUpdate(event.data)
    })

    this.eventSource.addEventListener('delivery-update', (event) => {
      console.log('🚚 배달 업데이트:', event.data)
      this.handleDeliveryUpdate(event.data)
    })

    this.eventSource.addEventListener('store-notification', (event) => {
      console.log('🏪 가게 알림:', event.data)
      this.handleStoreNotification(event.data)
    })
  }

  /**
   * 일반 알림 처리
   * @param {string} message - 알림 메시지
   */
  handleNotification(message) {
    try {
      const data = JSON.parse(message)
      this.showNotification(data.message || data.content || message)
    } catch (error) {
      this.showNotification(message)
    }
  }

  /**
   * 주문 상태 업데이트 처리
   * @param {string} message - 주문 상태 메시지
   */
  handleOrderStatusUpdate(message) {
    try {
      const data = JSON.parse(message)
      const notificationMessage = `주문 상태가 "${data.status}"로 변경되었습니다.`
      this.showNotification(notificationMessage)
      
      // 주문 상태 업데이트 이벤트 발생
      this.notifyCallbacks('orderStatusUpdate', data)
    } catch (error) {
      this.showNotification(message)
    }
  }

  /**
   * 배달 업데이트 처리
   * @param {string} message - 배달 상태 메시지
   */
  handleDeliveryUpdate(message) {
    try {
      const data = JSON.parse(message)
      const notificationMessage = `배달 상태: ${data.status}`
      this.showNotification(notificationMessage)
      
      // 배달 업데이트 이벤트 발생
      this.notifyCallbacks('deliveryUpdate', data)
    } catch (error) {
      this.showNotification(message)
    }
  }

  /**
   * 가게 알림 처리
   * @param {string} message - 가게 알림 메시지
   */
  handleStoreNotification(message) {
    try {
      const data = JSON.parse(message)
      const notificationMessage = `${data.storeName || '가게'}에서 알림: ${data.message}`
      this.showNotification(notificationMessage)
      
      // 가게 알림 이벤트 발생
      this.notifyCallbacks('storeNotification', data)
    } catch (error) {
      this.showNotification(message)
    }
  }

  /**
   * 알림을 화면에 표시
   * @param {string} message - 표시할 메시지
   */
  showNotification(message) {
    // 브라우저 알림 API 사용
    if (Notification.permission === 'granted') {
      new Notification('알림', {
        body: message,
        icon: '/favicon.ico'
      })
    }
    
    // 커스텀 알림 컴포넌트에 알림 전달
    this.notifyCallbacks('notification', { message })
    
    // 임시로 alert 사용 (개발 중)
    console.log('🔔 알림:', message)
  }

  /**
   * 알림 콜백 등록
   * @param {string} eventType - 이벤트 타입
   * @param {Function} callback - 콜백 함수
   */
  onNotification(eventType, callback) {
    this.notificationCallbacks.push({ eventType, callback })
  }

  /**
   * 등록된 콜백들에게 이벤트 알림
   * @param {string} eventType - 이벤트 타입
   * @param {any} data - 이벤트 데이터
   */
  notifyCallbacks(eventType, data) {
    this.notificationCallbacks.forEach(({ eventType: type, callback }) => {
      if (type === eventType) {
        try {
          callback(data)
        } catch (error) {
          console.error('SSE 콜백 오류:', error)
        }
      }
    })
  }

  /**
   * 연결 오류 처리 및 재연결 시도
   */
  handleConnectionError() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('🚫 SSE 최대 재연결 시도 횟수 초과!')
      console.error('📊 총 시도 횟수:', this.maxReconnectAttempts)
      console.error('👤 사용자 ID:', this.userId)
      console.error('⏰ 마지막 시도 시간:', new Date().toLocaleString())
      return
    }

    this.reconnectAttempts++
    console.log(`🔄 SSE 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`)
    console.log(`⏰ ${this.reconnectInterval/1000}초 후 재연결 시도...`)
    
    setTimeout(() => {
      if (this.userId) {
        console.log(`🔄 재연결 시작 (시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
        this.connect(this.userId)
      } else {
        console.warn('⚠️ 사용자 ID가 없어 재연결을 시도하지 않습니다.')
      }
    }, this.reconnectInterval)
  }

  /**
   * SSE 연결 해제
   */
  disconnect() {
    if (this.eventSource) {
      console.log('🔌 SSE 연결 해제 중...')
      console.log('📡 해제할 URL:', this.eventSource.url)
      console.log('👤 사용자 ID:', this.userId)
      this.eventSource.close()
      this.eventSource = null
      console.log('✅ SSE 연결 해제 완료')
    } else {
      console.log('ℹ️ SSE 연결이 이미 해제되어 있습니다.')
    }
    this.isConnected = false
  }

  /**
   * 연결 상태 확인
   * @returns {boolean} 연결 상태
   */
  getConnectionStatus() {
    return this.isConnected
  }

  /**
   * 현재 사용자 ID 반환
   * @returns {string|null} 사용자 ID
   */
  getCurrentUserId() {
    return this.userId
  }
}

// 싱글톤 인스턴스 생성 및 내보내기
export default new SSEManager()