// src/api/owner/menuApi.js
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
    console.log('🔄 메뉴 카테고리 목록 조회 중...');
    const response = await fetch('/api/owner/menus/categories', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 카테고리 목록 조회 성공');
    return data;
  }),

  createCategory: (categoryData) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 카테고리 생성 중...');
    const response = await fetch('/api/owner/menus/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: categoryData.name,
        displayOrder: categoryData.displayOrder || 0,
        isActive: categoryData.isActive !== undefined ? categoryData.isActive : true
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 카테고리 생성 성공');
    return data;
  }),

  updateCategory: (categoryId, categoryData) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 카테고리 수정 중...');
    const response = await fetch(`/api/owner/menus/categories/${categoryId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: categoryData.name,
        displayOrder: categoryData.displayOrder,
        isActive: categoryData.isActive,
        forceDeactivate: categoryData.forceDeactivate || false
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 카테고리 수정 성공');
    return data;
  }),

  deleteCategory: (categoryId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 카테고리 삭제 중...');
    const response = await fetch(`/api/owner/menus/categories/${categoryId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    console.log('✅ 카테고리 삭제 성공');
    return true;
  }),

  updateCategoryOrder: (categoryId, newPosition) => apiManager.queueRequest(async () => {
    console.log('🔄 카테고리 순서 변경 중...');
    const response = await fetch('/api/owner/menus/categories/order', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        categoryId: categoryId,
        newPosition: newPosition
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 카테고리 순서 변경 성공');
    return data;
  }),

  // ================================
  // 메뉴 관리
  // ================================

  getMenus: (categoryId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 목록 조회 중...');
    const url = categoryId ? `/api/owner/menus?categoryId=${categoryId}` : '/api/owner/menus';
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 목록 조회 성공');
    return data;
  }),

  getMenu: (menuId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 상세 조회 중...');
    const response = await fetch(`/api/owner/menus/${menuId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 상세 조회 성공');
    return data;
  }),

  createMenu: (menuData) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 생성 중...');
    const response = await fetch('/api/owner/menus', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
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
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 생성 성공');
    return data;
  }),

  updateMenu: (menuId, menuData) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 수정 중...');
    const response = await fetch(`/api/owner/menus/${menuId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
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
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 수정 성공');
    return data;
  }),

  deleteMenu: (menuId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 삭제 중...');
    const response = await fetch(`/api/owner/menus/${menuId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    console.log('✅ 메뉴 삭제 성공');
    return true;
  }),

  updateMenuStatus: (menuId, statusData) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 상태 변경 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        status: statusData.status,
        reason: statusData.reason,
        isTemporary: statusData.isTemporary || false
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 상태 변경 성공');
    return data;
  }),

  updateMenuOrder: (menuId, newPosition, categoryId, globalOrder) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 순서 변경 중...');
    const response = await fetch('/api/owner/menus/order', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        menuId: menuId,
        newPosition: newPosition,
        categoryId: categoryId,
        globalOrder: globalOrder || false
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 순서 변경 성공');
    return data;
  }),

  // ================================
  // 메뉴 이미지 관리
  // ================================

  uploadMenuImage: (menuId, file) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 이미지 업로드 중...');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`/api/owner/menus/${menuId}/images`, {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const imageUrl = await response.text();
    console.log('✅ 메뉴 이미지 업로드 성공');
    return imageUrl;
  }),

  getMenuImageInfo: (menuId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 이미지 정보 조회 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/images/info`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 메뉴 이미지 정보 조회 성공');
    return data;
  }),

  deleteMenuImage: (menuId, imageId) => apiManager.queueRequest(async () => {
    console.log('🔄 메뉴 이미지 삭제 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/images/${imageId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    console.log('✅ 메뉴 이미지 삭제 성공');
    return true;
  }),

  // ================================
  // 옵션 그룹 관리
  // ================================

  getOptionGroups: (menuId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 그룹 목록 조회 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 그룹 목록 조회 성공');
    return data;
  }),

  createOptionGroup: (menuId, groupData) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 그룹 생성 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: groupData.name,
        type: groupData.type,
        isRequired: groupData.isRequired || false,
        displayOrder: groupData.displayOrder || 0,
        items: groupData.items || []
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 그룹 생성 성공');
    return data;
  }),

  updateOptionGroup: (menuId, groupId, groupData) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 그룹 수정 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: groupData.name,
        type: groupData.type,
        isRequired: groupData.isRequired,
        displayOrder: groupData.displayOrder
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 그룹 수정 성공');
    return data;
  }),

  deleteOptionGroup: (menuId, groupId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 그룹 삭제 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    console.log('✅ 옵션 그룹 삭제 성공');
    return true;
  }),

  normalizeOptionGroupOrders: (menuId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 그룹 순서 정규화 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/normalize-order`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 그룹 순서 정규화 성공');
    return data;
  }),

  // ================================
  // 옵션 아이템 관리
  // ================================

  getOptionItems: (menuId, groupId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 목록 조회 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 아이템 목록 조회 성공');
    return data;
  }),

  createOptionItem: (menuId, groupId, itemData) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 생성 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: itemData.name,
        additionalPrice: itemData.additionalPrice || 0,
        displayOrder: itemData.displayOrder || 0,
        isActive: itemData.isActive !== undefined ? itemData.isActive : true
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 아이템 생성 성공');
    return data;
  }),

  updateOptionItem: (menuId, groupId, optionId, itemData) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 수정 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: itemData.name,
        additionalPrice: itemData.additionalPrice,
        displayOrder: itemData.displayOrder,
        isActive: itemData.isActive
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 아이템 수정 성공');
    return data;
  }),

  deleteOptionItem: (menuId, groupId, optionId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 삭제 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    console.log('✅ 옵션 아이템 삭제 성공');
    return true;
  }),

  updateOptionItemStatus: (menuId, groupId, optionId, statusData) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 상태 변경 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options/${optionId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        isActive: statusData.isActive,
        reason: statusData.reason,
        isTemporary: statusData.isTemporary || false
      })
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 아이템 상태 변경 성공');
    return data;
  }),

  normalizeOptionItemOrders: (menuId, groupId) => apiManager.queueRequest(async () => {
    console.log('🔄 옵션 아이템 순서 정규화 중...');
    const response = await fetch(`/api/owner/menus/${menuId}/option-groups/${groupId}/options/normalize-order`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    console.log('✅ 옵션 아이템 순서 정규화 성공');
    return data;
  }),
};