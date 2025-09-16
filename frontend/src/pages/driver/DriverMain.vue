<!-- src/pages/driver/DriverMain.vue -->
<script>
const API_BASE = import.meta.env.VITE_API_URL;
const REJECT_KEY_PREFIX = 'rejectedOrders';

export default {
  data() {
    return {
      userName: '',
      isOnline: false,
      riderAddress: '위치 확인 중…',
      riderId: null,
      deliveries: [],
      availableOrders: [],
      currentOrder: null,
      completedDeliveries: [],
      poll: null,
      rejectedOrders: [],
      rejectKey: null,
      todayEarnings: 0,
      todayCount: 0,
      isAccepting: false,
      isPickedUp: false,
      isBusy: false, // 클릭 중 중복 방지
    }
  },

  mounted() {
    const token = localStorage.getItem('jwt');
    if (!token) { alert('로그인이 필요합니다.'); return; }

    const riderStr = localStorage.getItem('rider');
    if (riderStr) { try { this.riderId = JSON.parse(riderStr).riderId; } catch {} }

    this.rejectKey = `${REJECT_KEY_PREFIX}:${this.riderId ?? 'unknown'}`;
    const savedRejects = localStorage.getItem(this.rejectKey);
    if (savedRejects) { try { this.rejectedOrders = JSON.parse(savedRejects); } catch {} }

    const savedCurrent = localStorage.getItem('currentOrder');
    if (savedCurrent) { try { this.currentOrder = JSON.parse(savedCurrent); } catch {} }

    this.userName = localStorage.getItem('userName') || '게스트';
    this.refreshLocation();
    this.$nextTick(() => window.lucide?.createIcons?.());
  },

  beforeUnmount() {
    if (this.poll) clearInterval(this.poll);
  },

  watch: {
    currentOrder: {
      deep: true,
      handler(v) {
        if (v) localStorage.setItem('currentOrder', JSON.stringify(v));
        else localStorage.removeItem('currentOrder');
      }
    }
  },

  methods: {
    async toggleOrderStatus() {
      if (!this.currentOrder || this.isBusy) return;
      this.isBusy = true;

      try {
        if (!this.isPickedUp) {
          // 픽업
          await this.pickupCurrentOrder();
          this.isPickedUp = true; // 서버 성공 후 전환
        } else {
          // 배달 완료
          await this.completeCurrentOrder(); // 성공 시 currentOrder null 처리
          this.isPickedUp = false;
        }
      } catch (e) {
        console.error(e);
        alert('처리 실패');
      } finally {
        this.isBusy = false;
      }
    },
    async pickupCurrentOrder() {
      const id = this.currentOrder?.id;
      if (!id) throw new Error('currentOrder.id 없음');

      const token = localStorage.getItem('jwt');
      const riderId = this.riderId || JSON.parse(localStorage.getItem('rider') || '{}').riderId;

      const resp = await fetch(`${API_BASE}/rider/deliveries/${id}/pickup`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ riderId }),
      });
      if (!resp.ok) throw new Error(`픽업 실패 ${resp.status}`);
    },
    async refreshLocation() {
      if (!navigator.geolocation) { this.riderAddress = '위치 지원 안됨'; return; }
      navigator.geolocation.getCurrentPosition(
          async (pos) => {
            const { latitude, longitude } = pos.coords;
            try {
              const res = await fetch(
                  `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=ko`
              );
              const data = await res.json();
              this.riderAddress = data.display_name || `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;
            } catch {
              this.riderAddress = `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;
            }
          },
          () => { this.riderAddress = '위치 접근 거부됨' },
          { enableHighAccuracy: true, timeout: 10000 }
      );
    },

    toggleOnlineStatus(next) {
      this.isOnline = typeof next === 'boolean' ? next : !!this.isOnline;
      if (this.isOnline) {
        this.fetchDeliveries();
        this.startPolling();
      } else {
        this.availableOrders = [];
        if (this.poll) clearInterval(this.poll);
      }
    },

    async acceptOrder(deliveryId) {
      if (this.currentOrder || this.isAccepting) return;
      this.isAccepting = true;
      try {
        const token = localStorage.getItem('jwt');
        const riderId = this.riderId || JSON.parse(localStorage.getItem('rider') || '{}').riderId;

        const resp = await fetch(`${API_BASE}/rider/deliveries/${deliveryId}/accept`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
          body: JSON.stringify({ riderId }),
        });
        if (!resp.ok) {
          alert(resp.status === 409 ? '동시에 여러 건 수락 불가' : `수락 실패: ${resp.status}`);
          return;
        }

        const picked =
            this.availableOrders.find(o => String(o.id) === String(deliveryId)) || null;

        this.currentOrder = picked;
        this.isPickedUp = false; // 새 주문 수락 시 초기화
        this.availableOrders = this.availableOrders.filter(o => String(o.id) !== String(deliveryId));
        if (picked) localStorage.setItem('currentOrder', JSON.stringify(picked));
      } catch (e) {
        console.error('수락 실패:', e);
        alert('수락 실패');
      } finally {
        this.isAccepting = false;
      }
    },

    rejectOrder(deliveryId) {
      const id = String(deliveryId);
      if (!this.rejectedOrders.includes(id)) {
        this.rejectedOrders.push(id);
        if (this.rejectKey) {
          localStorage.setItem(this.rejectKey, JSON.stringify(this.rejectedOrders));
        }
      }
      this.availableOrders = this.availableOrders.filter(o => String(o.id) !== id);
    },

    startPolling() {
      if (this.poll) clearInterval(this.poll);
      this.poll = setInterval(() => {
        if (this.isOnline) this.fetchDeliveries();
      }, 3000);
    },

    async fetchDeliveries() {
      try {
        const token = localStorage.getItem('jwt');
        const res = await fetch(`${API_BASE}/rider/deliveries`, {
          headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        });
        const json = await res.json();

        // 여기부터 ↓↓↓
        const items = json?.data?.items ?? json?.data ?? [];

        const mapped = items.map((d, idx) => {
          const id = d.deliveryId ?? d.id ?? `temp-${idx}`;
          const key = `ord:${id}|${d.requestedAt ?? idx}|${d.pickupAddress ?? ''}|${d.deliveryAddress ?? ''}`;
          return {
            _key: key,                // 렌더용 고유키
            id,                       // 로직용 ID
            status: d.status,         // REQUESTED 여부 확인
            riderId: d.riderId,
            restaurantName: d.restaurantName || '가게',
            restaurantAddress: d.pickupAddress,
            customerAddress: d.deliveryAddress,
            orderTime: d.requestedAt ? new Date(d.requestedAt).toLocaleString() : '-',
            estimatedDeliveryTime: d.estimatedTime ? `${d.estimatedTime}분` : '미정',
            distance: d.distanceKm ? `${d.distanceKm}km` : '-',
            deliveryFee: d.fee ?? 0,
            items: d.items ?? [],
            total: d.totalPrice ?? 0,
          };
        });

        // REQUESTED 상태만 노출
        this.availableOrders = mapped.filter(o => o.status === 'REQUESTED');

        // 디버그 로그
        console.log('[final ids]', this.availableOrders.map(o => o.id), 'len=', this.availableOrders.length);
        // 여기까지 ↑↑↑
      } catch (e) {
        console.error('배달 목록 오류:', e);
        alert('배달 목록을 불러오지 못했습니다.');
      }
    },

    async completeCurrentOrder() {
      const co = this.currentOrder;
      if (!co?.id) throw new Error('currentOrder.id 없음');

      // 낙관적 반영 준비
      const now = new Date();
      const optimistic = {
        ...co,
        completedTime: now.toISOString(),
        completedTimeText: now.toLocaleString(),
        earnings: co.deliveryFee ?? 0,
      };

      // 낙관적 반영
      this.completedDeliveries.unshift(optimistic);

      const token = localStorage.getItem('jwt');
      const riderId = this.riderId || JSON.parse(localStorage.getItem('rider') || '{}').riderId;

      const resp = await fetch(`${API_BASE}/rider/deliveries/${co.id}/complete`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ riderId }),
      });

      if (!resp.ok) {
        // 롤백
        this.completedDeliveries.shift();
        throw new Error(`완료 실패 ${resp.status}`);
      }

      // 성공 처리
      this.currentOrder = null;
      localStorage.removeItem('currentOrder');
      this.updateStats();
    },

    updateStats() {
      const today = new Date();
      const todayDeliveries = this.completedDeliveries.filter(d => {
        const dt = new Date(d.completedTime);
        return dt.getFullYear() === today.getFullYear()
            && dt.getMonth() === today.getMonth()
            && dt.getDate() === today.getDate();
      });
      const earnings = todayDeliveries.reduce((sum, d) => sum + d.earnings, 0);

      const earnEl = document.getElementById('today-earnings');
      const cntEl = document.getElementById('today-deliveries');
      if (earnEl) earnEl.textContent = `${earnings.toLocaleString()}원`;
      if (cntEl) cntEl.textContent = `${todayDeliveries.length}건`;
    },
  },
}
</script>




<template>
  <div><!-- ✅ 하나의 루트로 감싸기 -->
  <header class="bg-white border-b-2 border-gray-400 p-4">
    <div class="max-w-6xl mx-auto flex items-center justify-between">
      <!-- 왼쪽: 제목 + 주소 -->
      <div class="flex items-center gap-2">
        <i data-lucide="truck" class="w-6 h-6 text-gray-600"></i>
        <h1 class="text-xl text-gray-800">
          {{ userName }} 라이더
          <span class="ml-2 text-sm text-gray-500">({{ riderAddress }})</span>
        </h1>
      </div>

      <!-- 오른쪽: 근무 상태 -->
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2">
          <label class="text-gray-700 text-sm">근무 상태</label>
          <label class="switch">
            <input
              id="online-switch"
              type="checkbox"
              v-model="isOnline"
              @change="toggleOnlineStatus(isOnline)"
            />
            <span class="slider"></span>
          </label>
          <span
            id="status-badge"
            class="inline-flex px-2 py-1 text-xs rounded border"
            :class="
                isOnline
                  ? 'border-gray-400 bg-gray-200 text-gray-800'
                  : 'border-gray-400 bg-gray-400 text-gray-600'
              "
          >
              {{ isOnline ? '온라인' : '오프라인' }}
            </span>
        </div>
      </div>
    </div>
  </header>
    <!-- Stats -->
    <div class="max-w-6xl mx-auto p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">오늘 수익</p>
              <p id="today-earnings" class="text-2xl text-gray-800">0원</p>
            </div>
            <i data-lucide="dollar-sign" class="w-8 h-8 text-gray-600"></i>
          </div>
        </div>
        <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">오늘 배달</p>
              <p id="today-deliveries" class="text-2xl text-gray-800">0건</p>
            </div>
            <i data-lucide="package" class="w-8 h-8 text-gray-600"></i>
          </div>
        </div>
        <div class="border-2 border-gray-400 bg-white rounded-lg p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">평균 배달 시간</p>
              <p class="text-2xl text-gray-800">28분</p>
            </div>
            <i data-lucide="timer" class="w-8 h-8 text-gray-600"></i>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Available Orders -->
  <div id="available-content" class="tab-content">
    <!-- 오프라인 -->
    <div v-if="!isOnline"
         class="border-2 border-gray-400 bg-white rounded-lg p-8 text-center">
      <i data-lucide="truck" class="w-12 h-12 mx-auto text-gray-500 mb-4"></i>
      <h3 class="mb-2 text-gray-800">오프라인 상태</h3>
      <p class="text-gray-600 mb-4">근무 상태를 온라인으로 변경하면 새로운 주문을 받을 수 있습니다.</p>
      <button
        @click="toggleOnlineStatus(true)"
        class="border-2 border-gray-400 bg-gray-200 text-gray-800 hover:bg-gray-300 px-4 py-2 rounded">
        온라인으로 변경
      </button>
    </div>

    <!-- 온라인 + 대기중 -->
    <div v-else-if="availableOrders.length === 0"
         class="border-2 border-gray-400 bg-white rounded-lg p-8 text-center">
      <i data-lucide="bell" class="w-12 h-12 mx-auto text-gray-500 mb-4"></i>
      <h3 class="mb-2 text-gray-800">새로운 주문 대기 중</h3>
      <p class="text-gray-600">새로운 배달 주문이 들어오면 알림을 보내드립니다.</p>
    </div>
    <!-- 온라인 + 주문 리스트 -->
    <div v-else class="space-y-4">
      <div v-for="order in availableOrders" :key="order._key"
           class="border-2 border-gray-400 hover:bg-gray-200 transition-colors bg-white rounded-lg p-6">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h3 class="mb-1 text-gray-800">{{ order.restaurantName }}</h3>
            <p class="text-sm text-gray-600">주문 시간: {{ order.orderTime }}</p>
          </div>
          <span class="bg-gray-200 text-gray-800 border border-gray-400 px-2 py-1 rounded text-sm">배달 가능</span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div>
            <div class="flex items-center gap-2 mb-2">
              <i data-lucide="map-pin" class="w-4 h-4 text-gray-600"></i>
              <span class="text-sm text-gray-700">픽업 주소</span>
            </div>
            <p class="text-sm pl-6 text-gray-600">{{ order.restaurantAddress }}</p>
          </div>
          <div>
            <div class="flex items-center gap-2 mb-2">
              <i data-lucide="map-pin" class="w-4 h-4 text-gray-600"></i>
              <span class="text-sm text-gray-700">배달 주소</span>
            </div>
            <p class="text-sm pl-6 text-gray-600">{{ order.customerAddress }}</p>
          </div>
        </div>

        <div class="flex items-center gap-4 mb-4 text-sm">
          <div class="flex items-center gap-1">
            <i data-lucide="clock" class="w-4 h-4 text-gray-600"></i>
            <span class="text-gray-700">예상 배달: {{ order.estimatedDeliveryTime }}</span>
          </div>
          <div class="flex items-center gap-1">
            <i data-lucide="navigation" class="w-4 h-4 text-gray-600"></i>
            <span class="text-gray-700">거리: {{ order.distance }}</span>
          </div>
          <div class="flex items-center gap-1">
            <i data-lucide="dollar-sign" class="w-4 h-4 text-gray-600"></i>
            <span class="text-gray-700">배달비: {{ order.deliveryFee.toLocaleString() }}원</span>
          </div>
        </div>

        <div class="mb-4">
          <h4 class="text-sm mb-2 text-gray-700">주문 내역</h4>
          <div class="text-sm text-gray-600">
            {{ order.items.map(i => `${i.name} x${i.quantity}`).join(', ') }}
          </div>
          <p class="text-sm mt-1 text-gray-700">총 주문금액: {{ order.total.toLocaleString() }}원</p>
        </div>

        <div class="flex gap-2">
          <button
              @click="acceptOrder(order.id)"
              :disabled="!!currentOrder || isAccepting"
              class="flex-1 bg-gray-600 text-white border-2 border-gray-800 hover:bg-gray-700 py-2 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            주문 수락
          </button>
          <button
            @click="rejectOrder(order.id)"
            class="flex-1 bg-white text-gray-800 border-2 border-gray-400 hover:bg-gray-100 py-2 rounded transition-colors">
            거절
          </button>
        </div>
      </div>
    </div>
    <!-- ✅ Current Order (같은 루트 내부) -->
    <div id="current-content" class="tab-content mt-6">
      <div v-if="!currentOrder"
           class="border-2 border-gray-400 bg-white rounded-lg p-8 text-center">
        <i data-lucide="package" class="w-12 h-12 mx-auto text-gray-500 mb-4"></i>
        <h3 class="mb-2 text-gray-800">진행 중인 배달이 없습니다</h3>
        <p class="text-gray-600">새로운 주문을 수락하면 여기에 표시됩니다.</p>
      </div>

      <div v-else class="border-2 border-gray-400 bg-white rounded-lg p-6">
        <h3 class="text-lg text-gray-800 mb-2">현재 배달</h3>
        <p class="text-sm text-gray-600">가게: {{ currentOrder.restaurantName }}</p>
        <p class="text-sm text-gray-600">배달 주소: {{ currentOrder.customerAddress }}</p>
        <p class="text-sm text-gray-600">예상 배달 시간: {{ currentOrder.estimatedDeliveryTime }}</p>

        <button
          v-if="currentOrder"
          @click="toggleOrderStatus()"
          class="mt-4 bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700">
          {{ isPickedUp ? '배달 완료' : '픽업 완료' }}
        </button>
      </div>
    </div>
  </div>
</template>



<style>
/* 토글 스위치 스타일 */
.switch { position: relative; display: inline-block; width: 46px; height: 24px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc; transition: 0.4s; border-radius: 24px; }
.slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px;
  background-color: white; transition: 0.4s; border-radius: 50%; }
input:checked + .slider { background-color: #4caf50; }
input:checked + .slider:before { transform: translateX(22px); }
</style>