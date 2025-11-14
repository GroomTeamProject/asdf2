// src/api/owner/menuApi.js
import api from '../index.js'  
import { jwtDecode } from 'jwt-decode'

function getUserIdFromJwt() {
  const token = localStorage.getItem('jwt')
  if (!token) return null
  try {
    const decoded = jwtDecode(token)
    console.log('📦 JWT Payload:', decoded)
    return decoded.userId || null
  } catch (e) {
    console.error('❌ JWT 디코딩 실패:', e)
    return null
  }
}

class MenuApiManager {
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

const apiManager = new MenuApiManager();

export const menuApi = {
  // ================================
  // 카테고리 관리
  // ================================
  
  getCategories: () => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 카테고리 목록 조회 중...');
    const response = await api.get('/owner/menus/categories', { params: { currentUser } });   
    console.log('✅ 카테고리 목록 조회 성공');
    return response.data;   
  }),

  createCategory: (categoryData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 카테고리 생성 중...');
    const response = await api.post('/owner/menus/categories', {   
      name: categoryData.name,
      displayOrder: categoryData.displayOrder || 0,
      isActive: categoryData.isActive !== undefined ? categoryData.isActive : true
    }, { params: { currentUser } });
    console.log('✅ 카테고리 생성 성공');
    return response.data;   
  }),

  updateCategory: (categoryId, categoryData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 카테고리 수정 중...');
    const response = await api.put(`/owner/menus/categories/${categoryId}`, {   
      name: categoryData.name,
      displayOrder: categoryData.displayOrder,
      isActive: categoryData.isActive,
      forceDeactivate: categoryData.forceDeactivate || false
    }, { params: { currentUser } });
    console.log('✅ 카테고리 수정 성공');
    return response.data;   
  }),

  deleteCategory: (categoryId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 카테고리 삭제 중...');
    await api.delete(`/owner/menus/categories/${categoryId}`, { params: { currentUser } });   
    console.log('✅ 카테고리 삭제 성공');
    return true;
  }),

  updateCategoryOrder: (categoryId, newPosition) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 카테고리 순서 변경 중...');
    const response = await api.put('/owner/menus/categories/order', {   
      categoryId: categoryId,
      newPosition: newPosition
    }, { params: { currentUser } });
    console.log('✅ 카테고리 순서 변경 성공');
    return response.data;   
  }),

  // ================================
  // 메뉴 관리
  // ================================

  getMenus: (categoryId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 목록 조회 중...');
    const params = { currentUser };
    if (categoryId) params.categoryId = categoryId;
    const response = await api.get('/owner/menus', { params });   
    console.log('✅ 메뉴 목록 조회 성공');
    return response.data;   
  }),

  getMenu: (menuId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 상세 조회 중...');
    const response = await api.get(`/owner/menus/${menuId}`, { params: { currentUser } });   
    console.log('✅ 메뉴 상세 조회 성공');
    return response.data;   
  }),

  createMenu: (menuData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 생성 중...');
    const response = await api.post('/owner/menus', {   
      name: menuData.name,
      description: menuData.description,
      price: menuData.price,
      categoryId: menuData.categoryId,
      imageUrl: menuData.imageUrl,
      isPopular: menuData.isPopular || false,
      isRecommended: menuData.isRecommended || false,
      status: menuData.status || 'AVAILABLE',
      displayOrder: menuData.displayOrder || 0,
      options: menuData.options || []
    }, { params: { currentUser } });
    console.log('✅ 메뉴 생성 성공');
    return response.data;   
  }),

  updateMenu: (menuId, menuData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 수정 중...');
    const response = await api.put(`/owner/menus/${menuId}`, {   
      name: menuData.name,
      description: menuData.description,
      price: menuData.price,
      categoryId: menuData.categoryId,
      imageUrl: menuData.imageUrl,
      isPopular: menuData.isPopular,
      isRecommended: menuData.isRecommended,
      status: menuData.status,
      displayOrder: menuData.displayOrder,
      removeImage: menuData.removeImage || false
    }, { params: { currentUser } });
    console.log('✅ 메뉴 수정 성공');
    return response.data;   
  }),

  deleteMenu: (menuId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 삭제 중...');
    await api.delete(`/owner/menus/${menuId}`, { params: { currentUser } });   
    console.log('✅ 메뉴 삭제 성공');
    return true;
  }),

  updateMenuStatus: (menuId, statusData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 상태 변경 중...');
    const response = await api.put(`/owner/menus/${menuId}/status`, {   
      status: statusData.status,
      reason: statusData.reason,
      isTemporary: statusData.isTemporary || false
    }, { params: { currentUser } });
    console.log('✅ 메뉴 상태 변경 성공');
    return response.data;   
  }),

  updateMenuOrder: (menuId, newPosition, categoryId, globalOrder) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 순서 변경 중...');
    const response = await api.put('/owner/menus/order', {   
      menuId: menuId,
      newPosition: newPosition,
      categoryId: categoryId,
      globalOrder: globalOrder || false
    }, { params: { currentUser } });
    console.log('✅ 메뉴 순서 변경 성공');
    return response.data;   
  }),

  // ================================
  // 메뉴 이미지 관리
  // ================================

  uploadMenuImage: (menuId, file) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 이미지 업로드 중...');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post(`/owner/menus/${menuId}/images`, formData, {   
      params: { currentUser },
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('✅ 메뉴 이미지 업로드 성공');
    return response.data;   
  }),

  getMenuImageInfo: (menuId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 이미지 정보 조회 중...');
    const response = await api.get(`/owner/menus/${menuId}/images/info`, { params: { currentUser } });   
    console.log('✅ 메뉴 이미지 정보 조회 성공');
    return response.data;   
  }),

  deleteMenuImage: (menuId, imageId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 메뉴 이미지 삭제 중...');
    await api.delete(`/owner/menus/${menuId}/images/${imageId}`, { params: { currentUser } });   
    console.log('✅ 메뉴 이미지 삭제 성공');
    return true;
  }),

  // ================================
  // 옵션 그룹 관리
  // ================================

  getOptionGroups: (menuId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 목록 조회 중...');
    const response = await api.get(`/owner/menus/${menuId}/option-groups`, { params: { currentUser } });   
    console.log('✅ 옵션 그룹 목록 조회 성공');
    return response.data;   
  }),

  createOptionGroup: (menuId, groupData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 생성 중...');
    const response = await api.post(`/owner/menus/${menuId}/option-groups`, {   
      name: groupData.name,
      type: groupData.type,
      isRequired: groupData.isRequired || false,
      displayOrder: groupData.displayOrder || 0,
      items: groupData.items || []
    }, { params: { currentUser } });
    console.log('✅ 옵션 그룹 생성 성공');
    return response.data;   
  }),

  updateOptionGroup: (menuId, groupId, groupData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 수정 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}`, {   
      name: groupData.name,
      type: groupData.type,
      isRequired: groupData.isRequired,
      displayOrder: groupData.displayOrder
    }, { params: { currentUser } });
    console.log('✅ 옵션 그룹 수정 성공');
    return response.data;   
  }),

  deleteOptionGroup: (menuId, groupId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 삭제 중...');
    await api.delete(`/owner/menus/${menuId}/option-groups/${groupId}`, { params: { currentUser } });   
    console.log('✅ 옵션 그룹 삭제 성공');
    return true;
  }),

  normalizeOptionGroupOrders: (menuId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 순서 정규화 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/normalize-order`, {}, { params: { currentUser } });   
    console.log('✅ 옵션 그룹 순서 정규화 성공');
    return response.data;   
  }),

  // ================================
  // 옵션 아이템 관리
  // ================================

  getOptionItems: (menuId, groupId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 목록 조회 중...');
    const response = await api.get(`/owner/menus/${menuId}/option-groups/${groupId}/options`, { params: { currentUser } });   
    console.log('✅ 옵션 아이템 목록 조회 성공');
    return response.data;   
  }),

  createOptionItem: (menuId, groupId, itemData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 생성 중...');
    const response = await api.post(`/owner/menus/${menuId}/option-groups/${groupId}/options`, {   
      name: itemData.name,
      additionalPrice: itemData.additionalPrice || 0,
      displayOrder: itemData.displayOrder || 0,
      isActive: itemData.isActive !== undefined ? itemData.isActive : true
    }, { params: { currentUser } });
    console.log('✅ 옵션 아이템 생성 성공');
    return response.data;   
  }),

  updateOptionItem: (menuId, groupId, optionId, itemData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 수정 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}`, {   
      name: itemData.name,
      additionalPrice: itemData.additionalPrice,
      displayOrder: itemData.displayOrder,
      isActive: itemData.isActive
    }, { params: { currentUser } });
    console.log('✅ 옵션 아이템 수정 성공');
    return response.data;   
  }),

  deleteOptionItem: (menuId, groupId, optionId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 삭제 중...');
    await api.delete(`/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}`, { params: { currentUser } });   
    console.log('✅ 옵션 아이템 삭제 성공');
    return true;
  }),

  updateOptionItemStatus: (menuId, groupId, optionId, statusData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 상태 변경 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}/status`, {   
      isActive: statusData.isActive,
      reason: statusData.reason,
      isTemporary: statusData.isTemporary || false
    }, { params: { currentUser } });
    console.log('✅ 옵션 아이템 상태 변경 성공');
    return response.data;   
  }),

  normalizeOptionItemOrders: (menuId, groupId) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 순서 정규화 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}/options/normalize-order`, {}, { params: { currentUser } });   
    console.log('✅ 옵션 아이템 순서 정규화 성공');
    return response.data;   
  }),

  // ================================
  // 옵션 순서 변경 API
  // ================================

  updateOptionGroupOrder: (menuId, groupId, orderData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 그룹 순서 변경 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}/order`, {
      groupId: groupId,
      menuId: menuId,
      newPosition: orderData.newPosition,
      reason: orderData.reason || '관리자에 의한 순서 조정'
    }, { params: { currentUser } });
    console.log('✅ 옵션 그룹 순서 변경 성공');
    return response.data;
  }),

  updateOptionItemOrder: (menuId, groupId, optionId, orderData) => apiManager.queueRequest(async () => {
    const currentUser = getUserIdFromJwt()
    console.log('🔄 옵션 아이템 순서 변경 중...');
    const response = await api.put(`/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}/order`, {
      optionId: optionId,
      groupId: groupId,
      menuId: menuId,
      newPosition: orderData.newPosition,
      reason: orderData.reason || '관리자에 의한 순서 조정'
    }, { params: { currentUser } });
    console.log('✅ 옵션 아이템 순서 변경 성공');
    return response.data;
  }),
};