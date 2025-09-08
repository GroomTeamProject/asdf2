

<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Header -->
    <header class="bg-white border-b-2 border-gray-400 p-4">
      <div class="max-w-6xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-2">
          <Store class="w-6 h-6 text-gray-600" />
          <h1 class="text-xl text-gray-800">점주 관리</h1>
        </div>
        <button 
          @click="goHome"
          class="border border-gray-400 text-gray-700 px-3 py-1 rounded hover:bg-gray-200"
        >
          역할 변경
        </button>
      </div>
    </header>

    <!-- Navigation Tabs -->
    <div class="max-w-6xl mx-auto p-4">
      <div class="grid grid-cols-4 gap-0 bg-white border-2 border-gray-400 rounded-lg overflow-hidden mb-6">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          :class="['tab-button', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.name }}
        </button>
      </div>

      <!-- Dashboard Tab -->
      <div v-if="activeTab === 'dashboard'">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">오늘 주문</p>
                <p class="text-2xl text-gray-800">{{ todayStats.orders }}</p>
              </div>
              <Package class="w-8 h-8 text-gray-600" />
            </div>
          </div>
          <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">오늘 매출</p>
                <p class="text-2xl text-gray-800">{{ formatPrice(todayStats.revenue) }}</p>
              </div>
              <DollarSign class="w-8 h-8 text-gray-600" />
            </div>
          </div>
          <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">평점</p>
                <p class="text-2xl text-gray-800">{{ restaurant.rating }}</p>
              </div>
              <Star class="w-8 h-8 text-gray-600" />
            </div>
          </div>
          <!-- 오늘 운영시간 카드 추가 -->
          <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">오늘 운영시간</p>
                <p class="text-lg text-gray-800">{{ getTodayOperatingHours() }}</p>
              </div>
              <svg class="w-8 h-8 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <!-- 영업 상태 카드 추가 -->
        <div class="border-2 border-gray-400 bg-white rounded-lg mb-6">
          <div class="border-b border-gray-300 p-4">
            <h2 class="text-gray-800">가게 현황</h2>
          </div>
          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- 현재 영업 상태 -->
              <div class="p-4 rounded-lg border-2"
                   :class="{
                     'border-green-400 bg-green-50': getCurrentOperatingStatus().status === 'open',
                     'border-red-400 bg-red-50': getCurrentOperatingStatus().status === 'closed',
                     'border-gray-400 bg-gray-50': getCurrentOperatingStatus().status === 'unknown'
                   }">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium"
                       :class="{
                         'text-green-600': getCurrentOperatingStatus().status === 'open',
                         'text-red-600': getCurrentOperatingStatus().status === 'closed',
                         'text-gray-600': getCurrentOperatingStatus().status === 'unknown'
                       }">현재 영업 상태</p>
                    <p class="text-lg font-semibold"
                       :class="{
                         'text-green-800': getCurrentOperatingStatus().status === 'open',
                         'text-red-800': getCurrentOperatingStatus().status === 'closed',
                         'text-gray-800': getCurrentOperatingStatus().status === 'unknown'
                       }">{{ getCurrentOperatingStatus().message }}</p>
                  </div>
                  <div :class="{
                         'text-green-500': getCurrentOperatingStatus().status === 'open',
                         'text-red-500': getCurrentOperatingStatus().status === 'closed',
                         'text-gray-500': getCurrentOperatingStatus().status === 'unknown'
                       }">
                    <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </div>
                </div>
              </div>
              
              <!-- 총 주문 수 -->
              <div class="border-2 border-gray-400 bg-gray-50 p-4 rounded-lg">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm text-gray-600 font-medium">총 주문 수</p>
                    <p class="text-lg font-semibold text-gray-800">{{ restaurant.totalOrders.toLocaleString() }}회</p>
                  </div>
                  <BarChart3 class="w-8 h-8 text-gray-600" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="border-2 border-gray-400 bg-white rounded-lg">
          <div class="border-b border-gray-300 p-4">
            <h2 class="text-gray-800">최근 주문</h2>
          </div>
          <div class="p-4">
            <div class="space-y-4">
              <div 
                v-for="order in recentOrders" 
                :key="order.id"
                class="flex items-center justify-between p-4 border-2 border-gray-300 rounded-lg"
              >
                <div>
                  <p class="text-gray-800">주문 #{{ order.id }} - {{ order.customerName }}</p>
                  <p class="text-sm text-gray-600">
                    {{ order.items.map(item => `$${item.name} x$${item.quantity}`).join(', ') }}
                  </p>
                  <p class="text-sm text-gray-600">{{ order.orderTime }}</p>
                </div>
                <div class="text-right">
                  <span class="inline-flex px-2 py-1 text-xs rounded border border-gray-400 bg-gray-200 text-gray-800">
                    {{ getStatusText(order.status) }}
                  </span>
                  <p class="mt-1 text-gray-800">{{ formatPrice(order.total) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Orders Tab -->
      <div v-if="activeTab === 'orders'">
        <div class="border-2 border-gray-400 bg-white rounded-lg">
          <div class="border-b border-gray-300 p-4">
            <h2 class="text-gray-800">주문 관리</h2>
          </div>
          <div class="p-4">
            <div class="space-y-4">
              <div 
                v-for="order in orders" 
                :key="order.id"
                class="border border-gray-400 rounded-lg p-4 bg-white"
              >
                <div class="flex items-center justify-between mb-4">
                  <div>
                    <h3>주문 #{{ order.id }}</h3>
                    <p class="text-sm text-gray-600">고객: {{ order.customerName }}</p>
                    <p class="text-sm text-gray-600">주문 시간: {{ order.orderTime }}</p>
                  </div>
                  <div class="text-right">
                    <span class="inline-flex px-2 py-1 text-xs rounded border border-gray-400 bg-gray-200 text-gray-800">
                      {{ getStatusText(order.status) }}
                    </span>
                    <p class="mt-1">{{ formatPrice(order.total) }}</p>
                  </div>
                </div>
                
                <div class="mb-4">
                  <h4 class="mb-2">주문 내역</h4>
                  <div 
                    v-for="item in order.items" 
                    :key="item.name"
                    class="flex justify-between text-sm"
                  >
                    <span>{{ item.name }} x{{ item.quantity }}</span>
                    <span>{{ formatPrice(item.price * item.quantity) }}</span>
                  </div>
                </div>

                <div class="flex gap-2">
                  <button 
                    v-if="order.status === 'pending'"
                    @click="updateOrderStatus(order.id, 'preparing')"
                    class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
                  >
                    조리 시작
                  </button>
                  <button 
                    v-if="order.status === 'preparing'"
                    @click="updateOrderStatus(order.id, 'ready')"
                    class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
                  >
                    조리 완료
                  </button>
                  <button 
                    v-if="order.status === 'ready'"
                    @click="updateOrderStatus(order.id, 'delivered')"
                    class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
                  >
                    배달 시작
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Menu Tab -->
      <div v-if="activeTab === 'menu'">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-gray-800">메뉴 관리</h2>
          <button 
            @click="showAddMenuModal = true"
            class="border-2 border-gray-400 bg-gray-200 text-gray-800 hover:bg-gray-300 px-4 py-2 rounded flex items-center gap-2"
          >
            <Plus class="w-4 h-4" />
            메뉴 추가
          </button>
        </div>
        <div class="grid gap-4">
          <div 
            v-for="item in menuItems" 
            :key="item.id"
            class="border-2 border-gray-400 bg-white rounded-lg"
          >
            <div class="p-4">
              <div class="flex gap-4">
                <div class="image-placeholder w-20 h-20 flex-shrink-0">
                  <span>IMAGE</span>
                </div>
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="text-gray-800">{{ item.name }}</h3>
                    <span :class="[
                      'inline-flex px-2 py-1 text-xs rounded border border-gray-400',
                      item.available ? 'bg-gray-200 text-gray-800' : 'bg-gray-400 text-gray-600'
                    ]">
                      {{ item.available ? '판매 중' : '품절' }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-600 mb-2">{{ item.description }}</p>
                  <p class="text-gray-800">{{ formatPrice(item.price) }}</p>
                </div>
                <div class="flex gap-2">
                  <button
                    @click="toggleMenuAvailability(item.id)"
                    class="border border-gray-400 text-gray-700 hover:bg-gray-200 px-3 py-1 rounded text-sm"
                  >
                    {{ item.available ? '품절 처리' : '판매 재개' }}
                  </button>
                  <button class="border border-gray-400 text-gray-700 hover:bg-gray-200 p-2 rounded">
                    <Edit class="w-4 h-4" />
                  </button>
                  <button 
                    @click="deleteMenu(item.id)"
                    class="border border-gray-400 text-gray-700 hover:bg-gray-200 p-2 rounded"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Restaurant Tab 수정 -->
      <div v-if="activeTab === 'restaurant'">
        <div class="border-2 border-gray-400 bg-white rounded-lg">
          <div class="border-b border-gray-300 p-4 flex items-center justify-between">
            <h2 class="text-gray-800">가게 정보</h2>
            <button 
              @click="openEditRestaurantModal"  
              class="border border-gray-400 text-gray-700 hover:bg-gray-200 px-3 py-1 rounded flex items-center gap-2"
            >
              <Edit class="w-4 h-4" />
              수정
            </button>
          </div>
          <div class="p-4 space-y-6">
            <!-- 가게 이미지 -->
            <div v-if="restaurant.imageUrl" class="aspect-video w-full">
              <img 
                :src="getImageUrl(restaurant.imageUrl)" 
                :alt="restaurant.name || '가게 이미지'"
                class="w-full h-full object-cover rounded-lg border border-gray-300"
                @error="onImageError"
                @load="onImageLoad"
              />
            </div>
            <div v-else class="image-placeholder aspect-video w-full">
              <span>이미지 없음</span>
            </div>

            <!-- 기본 정보 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-gray-700 text-sm block mb-1">가게명</label>
                <p class="text-gray-800 font-medium">{{ restaurant.name || '정보 없음' }}</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">카테고리</label>
                <p class="text-gray-800">{{ restaurant.category || '정보 없음' }}</p>
              </div>
              <div class="col-span-2">
                <label class="text-gray-700 text-sm block mb-1">설명</label>
                <p class="text-gray-800">{{ restaurant.description || '설명 없음' }}</p>
              </div>
              <div class="col-span-2">
                <label class="text-gray-700 text-sm block mb-1">주소</label>
                <p class="text-gray-800">{{ restaurant.address || '주소 정보 없음' }}</p>
                <p v-if="restaurant.detailAddress" class="text-sm text-gray-600 mt-1">{{ restaurant.detailAddress }}</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">전화번호</label>
                <p class="text-gray-800">{{ restaurant.phone || '정보 없음' }}</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">배달료</label>
                <p class="text-gray-800">{{ formatPrice(restaurant.deliveryFee) }}</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">최소주문금액</label>
                <p class="text-gray-800">{{ formatPrice(restaurant.minOrderAmount) }}</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">배달시간</label>
                <p class="text-gray-800">{{ restaurant.deliveryTimeMin || 0 }}분 - {{ restaurant.deliveryTimeMax || 0 }}분</p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">평점</label>
                <p class="flex items-center gap-1 text-gray-800">
                  <Star class="w-4 h-4 text-gray-600" />
                  {{ restaurant.rating || 0 }}
                </p>
              </div>
              <div>
                <label class="text-gray-700 text-sm block mb-1">총 주문 수</label>
                <p class="text-gray-800">{{ (restaurant.totalOrders || 0).toLocaleString() }}회</p>
              </div>
            </div>

            <!-- 운영시간 정보 추가 -->
            <div class="border-t border-gray-300 pt-6">
              <label class="text-gray-700 text-sm block mb-3 font-medium">운영시간</label>
              <div class="bg-gray-50 p-4 rounded-lg border border-gray-300">
                <!-- 현재 상태 -->
                <div class="mb-4 p-3 rounded border-l-4"
                     :class="{
                       'bg-green-50 border-green-400': getCurrentOperatingStatus().status === 'open',
                       'bg-red-50 border-red-400': getCurrentOperatingStatus().status === 'closed',
                       'bg-gray-50 border-gray-400': getCurrentOperatingStatus().status === 'unknown'
                     }">
                  <p class="text-sm font-medium mb-1"
                     :class="{
                       'text-green-800': getCurrentOperatingStatus().status === 'open',
                       'text-red-800': getCurrentOperatingStatus().status === 'closed',
                       'text-gray-800': getCurrentOperatingStatus().status === 'unknown'
                     }">현재 상태</p>
                  <p class="font-semibold"
                     :class="{
                       'text-green-700': getCurrentOperatingStatus().status === 'open',
                       'text-red-700': getCurrentOperatingStatus().status === 'closed',
                       'text-gray-700': getCurrentOperatingStatus().status === 'unknown'
                     }">{{ getCurrentOperatingStatus().message }}</p>
                </div>
                
                <!-- 전체 운영시간 -->
                <div>
                  <p class="text-sm font-medium text-gray-700 mb-2">주간 운영시간</p>
                  <div class="space-y-1">
                    <pre class="text-sm text-gray-600 whitespace-pre-line leading-relaxed font-mono">{{ formatOperatingHours() }}</pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Menu Modal -->
    <div v-if="showAddMenuModal" class="modal show">
      <div class="modal-content">
        <div class="border-b border-gray-300 p-4">
          <h3 class="text-gray-800">새 메뉴 추가</h3>
        </div>
        <div class="p-4 space-y-4">
          <div>
            <label class="block text-sm text-gray-700 mb-1">메뉴명</label>
            <input 
              v-model="newMenu.name"
              type="text" 
              class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-700 mb-1">설명</label>
            <textarea 
              v-model="newMenu.description"
              class="w-full border border-gray-400 rounded px-3 py-2 h-20 resize-none focus:outline-none focus:border-gray-600"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm text-gray-700 mb-1">가격</label>
            <input 
              v-model.number="newMenu.price"
              type="number" 
              class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-700 mb-1">카테고리</label>
            <select 
              v-model="newMenu.category"
              class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
            >
              <option value="">카테고리 선택</option>
              <option value="치킨">치킨</option>
              <option value="사이드">사이드</option>
              <option value="음료">음료</option>
            </select>
          </div>
        </div>
        <div class="border-t border-gray-300 p-4 flex justify-end gap-2">
          <button 
            @click="hideAddMenuModal"
            class="border border-gray-400 text-gray-700 px-4 py-2 rounded hover:bg-gray-200"
          >
            취소
          </button>
          <button 
            @click="addNewMenu"
            class="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
          >
            추가
          </button>
        </div>
      </div>
    </div>

    <!-- Edit Restaurant Modal -->
    <div v-if="showEditRestaurantModal" class="modal show">
      <div class="modal-content max-w-2xl">
        <div class="border-b border-gray-300 p-4">
          <h3 class="text-gray-800">가게 정보 수정</h3>
        </div>
        <div class="p-4 space-y-4 max-h-96 overflow-y-auto">
          <!-- 기본 정보 -->
          <div class="space-y-4">
            <h4 class="font-semibold text-gray-700 border-b pb-2">기본 정보</h4>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-700 mb-1">가게명 *</label>
                <input 
                  v-model="editRestaurant.name"
                  type="text" 
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                  placeholder="가게명을 입력하세요"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-700 mb-1">카테고리 *</label>
                <select 
                  v-model="editRestaurant.category"
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                >
                  <option value="">카테고리 선택</option>
                  <option value="KOREAN">한식</option>
                  <option value="CHINESE">중식</option>
                  <option value="JAPANESE">일식</option>
                  <option value="WESTERN">양식</option>
                  <option value="CHICKEN">치킨</option>
                  <option value="PIZZA">피자</option>
                  <option value="BURGER">버거</option>
                  <option value="DESSERT">디저트</option>
                  <option value="CAFE">카페</option>
                  <option value="OTHER">기타</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-sm text-gray-700 mb-1">가게 설명</label>
              <textarea 
                v-model="editRestaurant.description"
                class="w-full border border-gray-400 rounded px-3 py-2 h-20 resize-none focus:outline-none focus:border-gray-600"
                placeholder="가게 설명을 입력하세요"
              ></textarea>
            </div>
          </div>

          <!-- 연락처 정보 -->
          <div class="space-y-4">
            <h4 class="font-semibold text-gray-700 border-b pb-2">연락처 정보</h4>
            <div>
              <label class="block text-sm text-gray-700 mb-1">전화번호</label>
              <input 
                v-model="editContact.phone"
                type="tel" 
                class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                placeholder="010-1234-5678"
              />
            </div>
          </div>

          <!-- 위치 정보 -->
          <div class="space-y-4">
            <h4 class="font-semibold text-gray-700 border-b pb-2">위치 정보</h4>
            <div>
              <label class="block text-sm text-gray-700 mb-1">주소</label>
              <input 
                v-model="editLocation.address"
                type="text" 
                class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                placeholder="기본 주소를 입력하세요"
              />
            </div>
            <div>
              <label class="block text-sm text-gray-700 mb-1">상세 주소</label>
              <input 
                v-model="editLocation.detailAddress"
                type="text" 
                class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                placeholder="상세 주소를 입력하세요"
              />
            </div>
          </div>

          <!-- 배달 설정 -->
          <div class="space-y-4">
            <h4 class="font-semibold text-gray-700 border-b pb-2">배달 설정</h4>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-700 mb-1">배달비 (원)</label>
                <input 
                  v-model.number="editDelivery.deliveryFee"
                  type="number" 
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                  placeholder="0"
                  min="0"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-700 mb-1">최소주문금액 (원)</label>
                <input 
                  v-model.number="editDelivery.minOrderAmount"
                  type="number" 
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                  placeholder="0"
                  min="0"
                />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-700 mb-1">최소 배달시간 (분)</label>
                <input 
                  v-model.number="editDelivery.deliveryTimeMin"
                  type="number" 
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                  placeholder="30"
                  min="0"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-700 mb-1">최대 배달시간 (분)</label>
                <input 
                  v-model.number="editDelivery.deliveryTimeMax"
                  type="number" 
                  class="w-full border border-gray-400 rounded px-3 py-2 focus:outline-none focus:border-gray-600"
                  placeholder="60"
                  min="0"
                />
              </div>
            </div>
          </div>
        </div>
        
        <div class="border-t border-gray-300 p-4 flex justify-end gap-2">
          <button 
            @click="cancelEdit"
            class="border border-gray-400 text-gray-700 px-4 py-2 rounded hover:bg-gray-200"
          >
            취소
          </button>
          <button 
            @click="saveRestaurantInfo"
            :disabled="loading"
            class="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '저장 중...' : '저장' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>




<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { Store, Package, DollarSign, Star, BarChart3, Plus, Edit, Trash2 } from 'lucide-vue-next'
import { storeApi } from '@/services/storeApi' 

// 완전한 API 서비스
// const storeApi = {
//   // 가게 정보 조회
//   getMyStore: async () => {
//     const response = await fetch('/api/owner/store', {
//       method: 'GET',
//       headers: {
//         'Content-Type': 'application/json',
//         // 'Authorization': `Bearer ${localStorage.getItem('token')}` // 토큰이 있다면
//       }
//     })
    
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`)
//     }
    
//     return await response.json()
//   },

//   // 가게 정보 수정
//   updateStore: async (storeData) => {
//     const response = await fetch('/api/owner/store', {
//       method: 'PUT',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify({
//         name: storeData.name,
//         description: storeData.description,
//         category: storeData.category,
//         imageUrl: storeData.imageUrl
//       })
//     })
    
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`)
//     }
    
//     return await response.json()
//   },

//   // 연락처 수정
//   updateContact: async (contactData) => {
//     const response = await fetch('/api/owner/store/contact', {
//       method: 'PUT',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify({
//         phone: contactData.phone
//       })
//     })
    
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`)
//     }
    
//     return await response.json()
//   },

//   // 배달 설정 수정
//   updateDelivery: async (deliveryData) => {
//     const response = await fetch('/api/owner/store/delivery', {
//       method: 'PUT',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify({
//         deliveryFee: deliveryData.deliveryFee,
//         minOrderAmount: deliveryData.minOrderAmount,
//         deliveryTimeMin: deliveryData.deliveryTimeMin,
//         deliveryTimeMax: deliveryData.deliveryTimeMax
//       })
//     })
    
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`)
//     }
    
//     return await response.json()
//   },

//   // 영업 상태 변경
//   updateStoreStatus: async (statusData) => {
//     const response = await fetch('/api/owner/store/status', {
//       method: 'PUT',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify({
//         status: statusData.status,
//         message: statusData.message
//       })
//     })
    
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`)
//     }
    
//     return await response.json()
//   }
// }

// 반응형 데이터
const activeTab = ref('dashboard')
const showAddMenuModal = ref(false)
const showEditRestaurantModal = ref(false)
const loading = ref(false)
const error = ref(null)
const storeHours = ref([])

const tabs = [
  { key: 'dashboard', name: '대시보드' },
  { key: 'orders', name: '주문 관리' },
  { key: 'menu', name: '메뉴 관리' },
  { key: 'restaurant', name: '가게 정보' }
]

// 가게 정보 (API 연동)
const restaurant = ref({
  id: null,
  businessNumber: '',
  name: '',
  description: '',
  phone: '',
  address: '',
  detailAddress: '',
  category: '',
  minOrderAmount: 0,
  deliveryFee: 0,
  deliveryTimeMin: 0,
  deliveryTimeMax: 0,
  imageUrl: '',
  openTime:'',
  closeTime:'',
  status: 'OPEN',
  rating: 0,
  reviewCount: 0,
  totalOrders: 0,
  isActive: true
})

// 수정 폼 데이터
const editRestaurant = reactive({
  name: '',
  description: '',
  category: '',
  imageUrl: ''
})

const editContact = reactive({
  phone: ''
})

const editDelivery = reactive({
  deliveryFee: 0,
  minOrderAmount: 0,
  deliveryTimeMin: 0,
  deliveryTimeMax: 0
})

const editLocation = reactive({
  address: '',
  detailAddress: ''
})

// 목업 데이터 (나중에 실제 API로 교체 예정)
const orders = ref([
  {
    id: '1',
    customerName: '김고객',
    items: [
      { name: '후라이드 치킨', quantity: 1, price: 18000 },
      { name: '치킨무', quantity: 1, price: 2000 }
    ],
    total: 20000,
    status: 'pending',
    orderTime: '2024-01-15 14:30'
  },
  {
    id: '2',
    customerName: '이고객',
    items: [
      { name: '양념 치킨', quantity: 2, price: 20000 }
    ],
    total: 40000,
    status: 'preparing',
    orderTime: '2024-01-15 14:25'
  },
  {
    id: '3',
    customerName: '박고객',
    items: [
      { name: '후라이드 치킨', quantity: 1, price: 18000 }
    ],
    total: 18000,
    status: 'ready',
    orderTime: '2024-01-15 14:20'
  }
])

const menuItems = ref([
  {
    id: '1',
    name: '후라이드 치킨',
    description: '바삭바삭한 클래식 후라이드',
    price: 18000,
    category: '치킨',
    available: true
  },
  {
    id: '2',
    name: '양념 치킨',
    description: '달콤매콤한 양념치킨',
    price: 20000,
    category: '치킨',
    available: true
  },
  {
    id: '3',
    name: '치킨무',
    description: '아삭한 치킨무',
    price: 2000,
    category: '사이드',
    available: false
  }
])

const newMenu = reactive({
  name: '',
  description: '',
  price: 0,
  category: ''
})

// 계산된 속성
const recentOrders = computed(() => orders.value.slice(0, 3))
const todayStats = computed(() => ({
  orders: orders.value.length,
  revenue: orders.value.reduce((sum, order) => sum + order.total, 0)
}))

// API 호출 함수들

// 이미지 URL 처리
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  
  console.log('원본 이미지 URL:', imageUrl)
  
  // 이미 절대 URL인 경우
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  
  // 상대경로인 경우 백엔드 서버 주소 추가
  if (imageUrl.startsWith('/')) {
    const fullUrl = `http://localhost:8080${imageUrl}`
    console.log('처리된 이미지 URL:', fullUrl)
    return fullUrl
  }
  
  // 파일명만 있는 경우
  const fullUrl = `http://localhost:8080/uploads/${imageUrl}`
  console.log('처리된 이미지 URL:', fullUrl)
  return fullUrl
}

// 이미지 로드 에러 처리
const onImageError = (event) => {
  console.log('❌ 이미지 로드 실패:', event.target.src)
  // 에러 시 placeholder로 교체
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    parent.innerHTML = '<div class="image-placeholder aspect-video w-full flex items-center justify-center"><span class="text-gray-500">이미지 로드 실패</span></div>'
  }
}

// 이미지 로드 성공
const onImageLoad = (event) => {
  console.log('✅ 이미지 로드 성공:', event.target.src)
}

const loadStoreInfo = async () => {
  try {
    loading.value = true
    error.value = null
    console.log('🔄 가게 정보 로딩 중...')
    
    const response = await storeApi.getMyStore()
    console.log('📦 API 응답:', response)
    
    // 백엔드 응답에 맞게 데이터 설정 (기본값 포함)
    restaurant.value = {
      id: response.id || null,
      businessNumber: response.businessNumber || '',
      name: response.name || '',
      description: response.description || '',
      phone: response.phone || '',
      address: response.address || '',
      detailAddress: response.detailAddress || '',
      category: response.category || '',
      minOrderAmount: response.minOrderAmount || 0,
      deliveryFee: response.deliveryFee || 0,
      deliveryTimeMin: response.deliveryTimeMin || 0,
      deliveryTimeMax: response.deliveryTimeMax || 0,
      imageUrl: response.imageUrl || '',
      status: response.status || 'OPEN',
      rating: response.rating || 0,
      reviewCount: response.reviewCount || 0,
      totalOrders: response.totalOrders || 0,
      isActive: response.isActive !== undefined ? response.isActive : true
    }

    // 운영시간 정보 로드
    try {
      const hoursResponse = await storeApi.getStoreHours()
      storeHours.value = hoursResponse || []
      console.log('운영시간 로드 완료:', storeHours.value)
    } catch (hoursError) {
      console.warn('운영시간 로드 실패:', hoursError)
      storeHours.value = []
    }
    
    // 수정 폼에 현재 데이터 설정
    editRestaurant.name = restaurant.value.name
    editRestaurant.description = restaurant.value.description
    editRestaurant.category = restaurant.value.category
    editRestaurant.imageUrl = restaurant.value.imageUrl
    
    editContact.phone = restaurant.value.phone
    
    editDelivery.deliveryFee = restaurant.value.deliveryFee
    editDelivery.minOrderAmount = restaurant.value.minOrderAmount
    editDelivery.deliveryTimeMin = restaurant.value.deliveryTimeMin
    editDelivery.deliveryTimeMax = restaurant.value.deliveryTimeMax
    
    console.log('✅ 가게 정보 로드 완료:', restaurant.value.name)
    
  } catch (err) {
    console.log('❌ API 연결 실패 - 목업 데이터 사용')
    
  } finally {
    loading.value = false
  }
}

const openEditRestaurantModal = () => {
  // 현재 가게 정보를 수정 폼에 복사
  editRestaurant.name = restaurant.value.name || ''
  editRestaurant.description = restaurant.value.description || ''
  editRestaurant.category = restaurant.value.category || ''
  editRestaurant.imageUrl = restaurant.value.imageUrl || ''
  
  editContact.phone = restaurant.value.phone || ''
  
  editDelivery.deliveryFee = restaurant.value.deliveryFee || 0
  editDelivery.minOrderAmount = restaurant.value.minOrderAmount || 0
  editDelivery.deliveryTimeMin = restaurant.value.deliveryTimeMin || 0
  editDelivery.deliveryTimeMax = restaurant.value.deliveryTimeMax || 0
  
  editLocation.address = restaurant.value.address || ''
  editLocation.detailAddress = restaurant.value.detailAddress || ''
  
  showEditRestaurantModal.value = true
}

// 수정 취소
const cancelEdit = () => {
  showEditRestaurantModal.value = false
}

// 가게 정보 저장 (통합) - 기존 개별 함수들을 활용
const saveRestaurantInfo = async () => {
  try {
    if (!editRestaurant.name.trim()) {
      alert('가게명을 입력해주세요.')
      return
    }
    
    if (!editRestaurant.category) {
      alert('카테고리를 선택해주세요.')
      return
    }
    
    loading.value = true
    console.log('🔄 서버에 가게 정보 저장 중...')
    
    // 모든 API를 병렬로 호출
    const promises = []
    
    // 1. 기본 정보 수정
    console.log('📤 기본 정보 수정 API 호출')
    promises.push(
      storeApi.updateStore(editRestaurant)
        .then(response => {
          console.log('✅ 기본 정보 수정 성공')
          return { type: 'store', data: response.data }
        })
    )
    
    // 2. 연락처 수정 (변경된 경우만)
    if (editContact.phone !== restaurant.value.phone) {
      console.log('📤 연락처 수정 API 호출')
      promises.push(
        storeApi.updateContact(editContact)
          .then(response => {
            console.log('✅ 연락처 수정 성공')
            return { type: 'contact', data: response.data }
          })
      )
    }
    
    // 3. 배달 설정 수정
    console.log('📤 배달 설정 수정 API 호출')
    promises.push(
      storeApi.updateDelivery(editDelivery)
        .then(response => {
          console.log('✅ 배달 설정 수정 성공')
          return { type: 'delivery', data: response.data }
        })
    )
    
    // 4. 위치 정보 수정 (변경된 경우만)
    if (editLocation.address !== restaurant.value.address || 
        editLocation.detailAddress !== restaurant.value.detailAddress) {
      console.log('📤 위치 정보 수정 API 호출')
      promises.push(
        storeApi.updateLocation(editLocation)
          .then(response => {
            console.log('✅ 위치 정보 수정 성공')
            return { type: 'location', data: response.data }
          })
      )
    }
    
    // 모든 API 호출 실행
    const results = await Promise.all(promises)
    
    // 서버 응답으로 데이터 업데이트
    results.forEach(result => {
      if (result && result.data) {
        restaurant.value = { ...restaurant.value, ...result.data }
      }
    })
    
    // 최종적으로 서버에서 최신 데이터 다시 로드
    await loadStoreInfo()
    
    showEditRestaurantModal.value = false
    console.log('🎉 서버에 모든 정보 저장 완료!')
    alert('가게 정보가 서버에 성공적으로 저장되었습니다!')
    
  } catch (err) {
    console.error('❌ 서버 저장 실패:', err)
    
    // 구체적인 에러 메시지
    let errorMessage = '서버 저장에 실패했습니다.\n'
    
    if (err.response) {
      errorMessage += `HTTP 상태: ${err.response.status}\n`
      errorMessage += `에러 내용: ${err.response.data?.message || err.response.statusText}`
    } else if (err.request) {
      errorMessage += '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.'
    } else {
      errorMessage += `에러: ${err.message}`
    }
    
    alert(errorMessage)
    
    // 에러 발생 시에는 모달을 닫지 않음 (다시 시도할 수 있도록)
    
  } finally {
    loading.value = false
  }
}

// 가게 정보 수정 핸들러
const updateRestaurantHandler = async () => {
  try {
    loading.value = true
    console.log('🔄 가게 정보 수정 중...')
    
    const response = await storeApi.updateStore(editRestaurant)
    restaurant.value = { ...restaurant.value, ...response.data }
    
    console.log('✅ 가게 정보 수정 완료')
    alert('가게 정보가 수정되었습니다.')
    
  } catch (err) {
    console.error('❌ 가게 정보 수정 실패:', err)
    alert(`가게 정보 수정에 실패했습니다: ${err.message}`)
    throw err // 에러를 다시 던져서 상위에서 처리할 수 있게
  } finally {
    loading.value = false
  }
}

// 연락처 수정 핸들러
const updateContactHandler = async () => {
  try {
    console.log('🔄 연락처 수정 중...')
    
    const response = await storeApi.updateContact(editContact)
    restaurant.value = { ...restaurant.value, ...response.data }
    
    console.log('✅ 연락처 수정 완료')
    alert('연락처가 수정되었습니다.')
    
  } catch (err) {
    console.error('❌ 연락처 수정 실패:', err)
    alert(`연락처 수정에 실패했습니다: ${err.message}`)
    throw err
  }
}

// 배달 설정 수정 핸들러
const updateDeliveryHandler = async () => {
  try {
    console.log('🔄 배달 설정 수정 중...')
    
    const response = await storeApi.updateDelivery(editDelivery)
    restaurant.value = { ...restaurant.value, ...response.data }
    
    console.log('✅ 배달 설정 수정 완료')
    alert('배달 설정이 수정되었습니다.')
    
  } catch (err) {
    console.error('❌ 배달 설정 수정 실패:', err)
    alert(`배달 설정 수정에 실패했습니다: ${err.message}`)
    throw err
  }
}

// 영업 상태 변경 핸들러 (서버 우선)
const updateStoreStatusHandler = async () => {
  try {
    console.log('🔄 서버에 영업 상태 변경 중...')
    console.log('📤 변경할 상태:', restaurant.value.status)
    
    const response = await storeApi.updateStoreStatus({ 
      status: restaurant.value.status,
      message: `영업 상태를 ${restaurant.value.status}로 변경`
    })
    
    // 서버 응답으로 데이터 업데이트
    if (response.data) {
      // StoreStatusModifyResponse 구조에 맞게 업데이트
      restaurant.value.status = response.data.status
      console.log('📥 서버에서 받은 상태:', response.data.status)
    }
    
    console.log('✅ 서버에 영업 상태 변경 완료')
    alert('영업 상태가 서버에 성공적으로 저장되었습니다!')
    
  } catch (err) {
    console.error('❌ 서버 영업 상태 변경 실패:', err)
    
    // 구체적인 에러 메시지
    let errorMessage = '영업 상태 변경에 실패했습니다.\n'
    
    if (err.response) {
      errorMessage += `HTTP 상태: ${err.response.status}\n`
      errorMessage += `에러 내용: ${err.response.data?.message || err.response.statusText}`
    } else if (err.request) {
      errorMessage += '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.'
    } else {
      errorMessage += `에러: ${err.message}`
    }
    
    alert(errorMessage)
    
    // 실패 시 이전 상태로 되돌리기 (UI 일관성 유지)
    await loadStoreInfo() // 서버에서 최신 상태 다시 로드
  }
}
// 운영시간 포맷팅 함수들
const formatTime = (timeString) => {
  if (!timeString) return ''
  // "10:00:00" -> "10:00" 변환
  return timeString.substring(0, 5)
}

const getDayName = (dayOfWeek) => {
  const dayNames = ['일', '월', '화', '수', '목', '금', '토']
  return dayNames[dayOfWeek]
}

const formatOperatingHours = () => {
  if (!storeHours.value || storeHours.value.length === 0) {
    return '운영시간 정보가 없습니다'
  }
  
  const formattedHours = []
  
  // dayOfWeek 순서대로 정렬 (월요일부터 시작하도록)
  const sortedHours = [...storeHours.value].sort((a, b) => {
    // 일요일(0)을 마지막으로 보내기 위해 조정
    const aDay = a.dayOfWeek === 0 ? 7 : a.dayOfWeek
    const bDay = b.dayOfWeek === 0 ? 7 : b.dayOfWeek
    return aDay - bDay
  })
  
  sortedHours.forEach(hour => {
    const dayName = getDayName(hour.dayOfWeek)
    
    if (hour.isClosed) {
      formattedHours.push(`${dayName}요일: 휴무`)
    } else if (hour.openTime && hour.closeTime) {
      const openTime = formatTime(hour.openTime)
      const closeTime = formatTime(hour.closeTime)
      formattedHours.push(`${dayName}요일: ${openTime} - ${closeTime}`)
    } else {
      formattedHours.push(`${dayName}요일: 시간 미설정`)
    }
  })
  
  return formattedHours.join('\n')
}

// 오늘 운영시간 표시
const getTodayOperatingHours = () => {
  if (!storeHours.value || storeHours.value.length === 0) {
    return '운영시간 정보 없음'
  }
  
  const today = new Date().getDay() // 0=일요일, 1=월요일, ...
  const todayHour = storeHours.value.find(hour => hour.dayOfWeek === today)
  
  if (!todayHour) {
    return '오늘 운영시간 정보 없음'
  }
  
  if (todayHour.isClosed) {
    return '오늘 휴무'
  }
  
  if (todayHour.openTime && todayHour.closeTime) {
    const openTime = formatTime(todayHour.openTime)
    const closeTime = formatTime(todayHour.closeTime)
    return `${openTime} - ${closeTime}`
  }
  
  return '운영시간 미설정'
}

// 현재 영업 상태 확인
const getCurrentOperatingStatus = () => {
  if (!storeHours.value || storeHours.value.length === 0) {
    return { status: 'unknown', message: '운영시간 정보 없음' }
  }
  
  const now = new Date()
  const today = now.getDay()
  const currentTime = now.getHours() * 100 + now.getMinutes() // HHMM 형식
  
  const todayHour = storeHours.value.find(hour => hour.dayOfWeek === today)
  
  if (!todayHour || todayHour.isClosed) {
    return { status: 'closed', message: '오늘 휴무' }
  }
  
  if (todayHour.openTime && todayHour.closeTime) {
    const openTime = parseInt(todayHour.openTime.replace(':', '').substring(0, 4))
    const closeTime = parseInt(todayHour.closeTime.replace(':', '').substring(0, 4))
    
    if (currentTime >= openTime && currentTime <= closeTime) {
      return { status: 'open', message: '영업 중' }
    } else if (currentTime < openTime) {
      return { status: 'closed', message: `${formatTime(todayHour.openTime)} 오픈 예정` }
    } else {
      return { status: 'closed', message: '영업 종료' }
    }
  }
  
  return { status: 'unknown', message: '운영시간 미설정' }
}

// 유틸리티 함수들
const formatPrice = (price) => {
  return `${(price || 0).toLocaleString()}원`
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '주문 접수',
    'preparing': '조리 중',
    'ready': '조리 완료',
    'delivered': '배달 완료'
  }
  return statusMap[status] || status
}

// 기존 함수들
const updateOrderStatus = (orderId, newStatus) => {
  const order = orders.value.find(o => o.id === orderId)
  if (order) {
    order.status = newStatus
  }
}

const toggleMenuAvailability = (itemId) => {
  const item = menuItems.value.find(m => m.id === itemId)
  if (item) {
    item.available = !item.available
  }
}

const deleteMenu = (itemId) => {
  if (confirm('이 메뉴를 삭제하시겠습니까?')) {
    const index = menuItems.value.findIndex(m => m.id === itemId)
    if (index > -1) {
      menuItems.value.splice(index, 1)
    }
  }
}

const hideAddMenuModal = () => {
  showAddMenuModal.value = false
  newMenu.name = ''
  newMenu.description = ''
  newMenu.price = 0
  newMenu.category = ''
}

const addNewMenu = () => {
  if (!newMenu.name || !newMenu.price || !newMenu.category) {
    alert('모든 필수 항목을 입력해주세요.')
    return
  }

  const newItem = {
    id: Date.now().toString(),
    name: newMenu.name,
    description: newMenu.description,
    price: newMenu.price,
    category: newMenu.category,
    available: true
  }

  menuItems.value.push(newItem)
  hideAddMenuModal()
}


const goHome = () => {
  window.location.href = '../index.html'
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  console.log('🚀 점주 관리 시스템 시작')
  await loadStoreInfo()
})
</script>

<style scoped>
:root {
  --font-size: 14px;
}

html {
  font-size: var(--font-size);
}

.image-placeholder {
  @apply bg-gray-300 border border-gray-400 flex items-center justify-center;
}

.image-placeholder span {
  @apply text-gray-500 text-xs;
}

.tab-button {
  @apply px-4 py-2 border border-gray-400 text-gray-700 bg-gray-100 hover:bg-gray-200 transition-colors;
}

.tab-button.active {
  @apply bg-gray-200 border-gray-600;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 0.5rem;
  border: 2px solid #9ca3af;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}
</style>