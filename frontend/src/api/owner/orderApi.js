import api from '../index.js'

class OrderApiManager {
  constructor() {
    this.requestQueue = [];
    this.isProcessing = false;
  }

  async processQueue() {
    if (this.isProcessing) return;
    
    this.isProcessing = true;
    
    while (this.requestQueue.length > 0) {
      const { apiFunction, resolve, reject } = this.requestQueue.shift();
      
      try {
        const result = await apiFunction();
        resolve(result);
        await new Promise(resolve => setTimeout(resolve, 100));
      } catch (error) {
        reject(error);
      }
    }
    
    this.isProcessing = false;
  }

  async queueRequest(apiFunction) {
    return new Promise((resolve, reject) => {
      this.requestQueue.push({ apiFunction, resolve, reject });
      this.processQueue();
    });
  }
}

const orderApiManager = new OrderApiManager();

export const orderApi = {
  // 주문 목록 조회 (가게별)
  getOrders: (storeId) => orderApiManager.queueRequest(async () => {
    console.log('🔄 주문 목록 조회 중..., 가게 ID:', storeId);
    const response = await api.get(`/orders?storeId=${storeId}`);
    console.log('✅ 주문 목록 조회 성공');
    return response.data;
  }),

  // 주문 상세 조회
  getOrderDetail: (orderId) => orderApiManager.queueRequest(async () => {
    console.log('🔄 주문 상세 조회 중..., 주문 ID:', orderId);
    const response = await api.get(`/orders/${orderId}`);
    console.log('✅ 주문 상세 조회 성공');
    return response.data;
  }),

  // 주문 수락
  acceptOrder: (orderId, acceptData) => orderApiManager.queueRequest(async () => {
    console.log('🔄 주문 수락 처리 중..., 주문 ID:', orderId);
    const response = await api.put(`/orders/${orderId}/accept`, acceptData);
    console.log('✅ 주문 수락 완료');
    return response.data;
  }),

  // 주문 거절
  rejectOrder: (orderId, rejectData) => orderApiManager.queueRequest(async () => {
    console.log('🔄 주문 거절 처리 중..., 주문 ID:', orderId);
    const response = await api.put(`/orders/${orderId}/reject`, rejectData);
    console.log('✅ 주문 거절 완료');
    return response.data;
  }),

  // 조리 시작
  startCooking: (orderId) => orderApiManager.queueRequest(async () => {
    console.log('🔄 조리 시작 처리 중..., 주문 ID:', orderId);
    const response = await api.put(`/orders/${orderId}/start-cooking`);
    console.log('✅ 조리 시작 완료');
    return response.data;
  }),

  // 조리 완료
  completeCooking: (orderId) => orderApiManager.queueRequest(async () => {
    console.log('🔄 조리 완료 처리 중..., 주문 ID:', orderId);
    const response = await api.put(`/orders/${orderId}/complete-cooking`);
    console.log('✅ 조리 완료');
    return response.data;
  }),

  // 배달 완료
  deliverOrder: (orderId) => orderApiManager.queueRequest(async () => {
    console.log('🔄 배달 완료 처리 중..., 주문 ID:', orderId);
    const response = await api.put(`/orders/${orderId}/deliver`);
    console.log('✅ 배달 완료');
    return response.data;
  })
};