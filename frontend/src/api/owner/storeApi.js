class StoreApiManager {
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

const apiManager = new StoreApiManager();

export const storeApi = {
  // 가게 정보 조회
  getMyStore: () => apiManager.queueRequest(async () => {
    console.log('🔄 가게 정보 조회 중...');
    const response = await fetch('/api/owner/store', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 가게 정보 조회 성공');
    return data;
  }),

  // 가게 정보 수정
  updateStore: (storeData) => apiManager.queueRequest(async () => {
    console.log('🔄 가게 정보 수정 중...');
    const response = await fetch('/api/owner/store', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: storeData.name,
        description: storeData.description,
        category: storeData.category,
        imageUrl: storeData.imageUrl
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 가게 정보 수정 성공');
    return data;
  }),

  // 연락처 수정
  updateContact: (contactData) => apiManager.queueRequest(async () => {
    console.log('🔄 연락처 수정 중...');
    const response = await fetch('/api/owner/store/contact', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: contactData.phone
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 연락처 수정 성공');
    return data;
  }),

  // 배달 설정 수정
  updateDelivery: (deliveryData) => apiManager.queueRequest(async () => {
    console.log('🔄 배달 설정 수정 중...');
    const response = await fetch('/api/owner/store/delivery', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        deliveryFee: deliveryData.deliveryFee,
        minOrderAmount: deliveryData.minOrderAmount,
        deliveryTimeMin: deliveryData.deliveryTimeMin,
        deliveryTimeMax: deliveryData.deliveryTimeMax
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 배달 설정 수정 성공');
    return data;
  }),

  // 영업 상태 변경
  updateStoreStatus: (statusData) => apiManager.queueRequest(async () => {
    console.log('🔄 영업 상태 변경 중...');
    const response = await fetch('/api/owner/store/status', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        status: statusData.status,
        message: statusData.message
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 영업 상태 변경 성공');
    return data;
  }),

  // 위치 정보 수정
  updateLocation: (locationData) => apiManager.queueRequest(async () => {
    console.log('🔄 위치 정보 수정 중...');
    const response = await fetch('/api/owner/store/location', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        address: locationData.address,
        detailAddress: locationData.detailAddress
      })
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('✅ 위치 정보 수정 성공');
    return data;
  }),

  // 운영시간 조회
  getStoreHours: () => apiManager.queueRequest(async () => {
    console.log('🔄 운영시간 조회 중...');
    const response = await fetch('/api/owner/store/hours', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 운영시간 조회 성공');
    return data;
  }),

  // 🆕 가게 이미지 업로드
  uploadImage: (file) => apiManager.queueRequest(async () => {
    console.log('🔄 가게 이미지 업로드 중...');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch('/api/owner/store/images', {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const imageUrl = await response.text();
    console.log('✅ 가게 이미지 업로드 성공');
    return imageUrl;
  }),

  // 🆕 가게 상태 조회
  getStoreStatus: () => apiManager.queueRequest(async () => {
    console.log('🔄 가게 상태 조회 중...');
    const response = await fetch('/api/owner/store/status', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 가게 상태 조회 성공');
    return data;
  }),

  // 🆕 휴무일 등록
  createHoliday: (holidayData) => apiManager.queueRequest(async () => {
    console.log('🔄 휴무일 등록 중...');
    const response = await fetch('/api/owner/store/holidays', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        date: holidayData.date,
        reason: holidayData.reason,
        isRecurring: holidayData.isRecurring || false
      })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.text();
    console.log('✅ 휴무일 등록 성공');
    return data;
  }),

  // 🆕 휴무일 목록 조회
  getHolidays: () => apiManager.queueRequest(async () => {
    console.log('🔄 휴무일 목록 조회 중...');
    const response = await fetch('/api/owner/store/holidays', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('✅ 휴무일 목록 조회 성공');
    return data;
  }),

  // 🆕 휴무일 삭제
  deleteHoliday: (holidayId) => apiManager.queueRequest(async () => {
    console.log('🔄 휴무일 삭제 중...');
    const response = await fetch(`/api/owner/store/holidays/${holidayId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.text();
    console.log('✅ 휴무일 삭제 성공');
    return data;
  }),
};