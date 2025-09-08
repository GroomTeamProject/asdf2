export const storeApi = {
  // 가게 정보 조회
  getMyStore: async () => {
    const response = await fetch('/api/owner/store', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  },

  // 가게 정보 수정
  updateStore: async (storeData) => {
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
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  },

  // 연락처 수정
  updateContact: async (contactData) => {
    const response = await fetch('/api/owner/store/contact', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: contactData.phone
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  },

  // 배달 설정 수정
  updateDelivery: async (deliveryData) => {
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
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  },

  // 영업 상태 변경
  updateStoreStatus: async (statusData) => {
    const response = await fetch('/api/owner/store/status', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        status: statusData.status,
        message: statusData.message
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  },

  // 위치 정보 수정
  updateLocation: async (locationData) => {
    const response = await fetch('/api/owner/store/location', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        address: locationData.address,
        detailAddress: locationData.detailAddress
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  },

  // ✅ 운영시간 조회 함수 수정 (fetch 사용)
  getStoreHours: async () => {
    try {
      console.log('🔄 운영시간 조회 요청 시작')
      
      const response = await fetch('/api/owner/store/hours', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      console.log('✅ 운영시간 조회 성공:', data)
      return data
    } catch (error) {
      console.error('❌ 운영시간 조회 에러:', error)
      throw new Error(`HTTP error! status: ${error.message}`)
    }
  },
}